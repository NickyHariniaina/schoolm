package hei.student.schoolm.service;

import hei.student.schoolm.dto.SemesterValidationDto;
import hei.student.schoolm.dto.StudentRequest;
import hei.student.schoolm.dto.StudentResponse;
import hei.student.schoolm.dto.TranscriptDto;
import hei.student.schoolm.exception.BadRequestException;
import hei.student.schoolm.exception.ForbiddenException;
import hei.student.schoolm.mapper.StudentMapper;
import hei.student.schoolm.model.*;
import hei.student.schoolm.repository.CourseAssignmentRepository;
import hei.student.schoolm.repository.GradeRepository;
import hei.student.schoolm.repository.GroupFlowRepository;
import hei.student.schoolm.repository.StudentRepository;
import hei.student.schoolm.repository.jpa.JGradeHistoryRepository;
import hei.student.schoolm.util.SecurityUtil;
import hei.student.schoolm.util.StdRefGenerator;
import hei.student.schoolm.validator.StudentValidator;
import java.time.LocalDate;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {
  private final StudentValidator studentValidator;
  private final GroupFlowService groupFlowService;
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final StudentMapper studentMapper;
  private final SecurityUtil securityUtil;
  private final StudentRepository studentRepository;
  private final GroupService groupService;
  private final GroupFlowRepository groupFlowRepository;
  private final GradeRepository gradeRepository;
  private final JGradeHistoryRepository jGradeHistoryRepository;
  private final StdRefGenerator stdRefGenerator;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  public List<StudentResponse> getAll() {
    return studentRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public StudentResponse getById(UUID studentId) {
    securityUtil.requireSelfOrStaff(studentId);
    return toResponse(studentValidator.checkStudentExists(studentId));
  }

  @Transactional
  public StudentResponse upsert(StudentRequest request) {
    if (request.id() == null) {
      return create(request);
    }
    return update(request);
  }

  private StudentResponse create(StudentRequest request) {
    if (request.groupId() == null) {
      throw new BadRequestException("groupId is required when creating a student");
    }
    if (request.password() == null || request.password().isBlank()) {
      throw new BadRequestException("password is required when creating a student");
    }
    var group = groupService.getEntityOrThrow(request.groupId());
    var reference = stdRefGenerator.generate(group.getCohort().getEntryYear().getValue());

    var student =
        studentRepository.save(
            Student.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(User.Role.STUDENT)
                .password(passwordEncoder.encode(request.password()))
                .reference(reference)
                .group(group)
                .build());

    groupFlowRepository.save(
        GroupFlow.builder()
            .student(student)
            .group(group)
            .groupFlowType(GroupFlowType.JOIN)
            .build());

    return toResponse(student);
  }

  private StudentResponse update(StudentRequest request) {
    if (request.groupId() != null) {
      throw new BadRequestException(
          "groupId cannot be changed on update; moves must go through /students/{id}/group-flows");
    }
    var student = studentValidator.checkStudentExists(request.id());
    student.setEmail(request.email());
    student.setFirstName(request.firstName());
    student.setLastName(request.lastName());
    if (request.password() != null && !request.password().isBlank()) {
      student.setPassword(passwordEncoder.encode(request.password()));
    }

    return toResponse(studentRepository.save(student));
  }

  @Transactional
  public void delete(UUID studentId) {
    if (!securityUtil.isAdmin()) {
      throw new ForbiddenException("Only an admin can delete a student");
    }
    studentValidator.checkStudentExists(studentId);
    gradeRepository.deleteAllByStudentId(studentId);
    jGradeHistoryRepository.deleteAllByStudentId(studentId);
    groupFlowRepository.deleteAllByStudentId(studentId);
    studentRepository.deleteById(studentId);
  }

  @Transactional(readOnly = true)
  public SemesterValidationDto getStudentSemesterValidation(UUID studentId, Semester semester) {
    securityUtil.requireSelfOrAdmin(studentId);
    var student = studentValidator.checkStudentExists(studentId);
    var courses = coursesForStudent(studentId, List.of(semester));
    var group = student.getGroup();
    return studentMapper.toSemesterValidationDto(student, group, semester, courses);
  }

  public Group getGroup(UUID studentId) {
    var student = studentValidator.checkStudentExists(studentId);
    return student.getGroup();
  }

  public TranscriptDto getTranscript(UUID studentId, Integer month, Integer year) {
    securityUtil.requireSelfOrAdmin(studentId);
    if (month != null && (month < 1 || month > 12)) {
      throw new BadRequestException("month must be between 1 and 12");
    }
    var student = studentValidator.checkStudentExists(studentId);
    var group = student.getGroup();
    var entryYear = group.getCohort().getEntryYear();
    var semester = resolveSemester(month, year, entryYear);
    var pair = semester.pair();
    var studentTrack = group.getTrack();

    var filteredCourses = filterCourses(coursesForStudent(studentId, pair), pair, studentTrack);

    return studentMapper.toTranscriptDto(student, group, semester, filteredCourses);
  }

  public TranscriptDto getTranscriptForSemester(UUID studentId, Semester targetSemester) {
    var student = studentValidator.checkStudentExists(studentId);
    var group = student.getGroup();
    return buildTranscript(student, group, targetSemester);
  }

  private TranscriptDto buildTranscript(Student student, Group group, Semester semester) {
    var filteredCourses =
        filterCourses(
            coursesForStudent(student.getId(), semester.pair()), semester.pair(), group.getTrack());
    return studentMapper.toTranscriptDto(student, group, semester, filteredCourses);
  }

  private List<Course> coursesForStudent(UUID studentId, List<Semester> semesters) {
    var groupIds = groupFlowService.studentGroupIds(studentId);
    return courseAssignmentRepository.findCurriculumCourses(groupIds, semesters).stream()
        .sorted(
            Comparator.comparingInt((Course course) -> course.getSemester().ordinal())
                .thenComparing(Course::getRef))
        .toList();
  }

  public List<Course> filterCourses(List<Course> courses, List<Semester> pair, Track studentTrack) {
    if (courses == null) {
      return List.of();
    }

    return courses.stream()
        .filter(course -> pair.contains(course.getSemester()))
        .filter(course -> course.getTrack() == Track.COMMON || course.getTrack() == studentTrack)
        .sorted(
            Comparator.comparingInt((Course course) -> course.getSemester().ordinal())
                .thenComparing(Course::getRef))
        .toList();
  }

  private Semester resolveSemester(Integer month, Integer year, Year entryYear) {
    if (month == null && year == null) {
      var today = LocalDate.now();
      return Semester.from(entryYear, today.getMonthValue(), today.getYear());
    }
    if (month == null || year == null) {
      throw new BadRequestException("month and year must both be provided");
    }
    return Semester.from(entryYear, month, year);
  }

  private StudentResponse toResponse(Student student) {
    return new StudentResponse(
        student.getId(),
        student.getReference(),
        student.getFirstName(),
        student.getLastName(),
        student.getEmail(),
        student.getGroup() == null ? null : student.getGroup().getId(),
        student.getGroup() == null ? null : student.getGroup().getRef());
  }
}
