package hei.student.schoolm.service;

import hei.student.schoolm.dto.SemesterValidationDto;
import hei.student.schoolm.dto.TranscriptDto;
import hei.student.schoolm.exception.BadRequestException;
import hei.student.schoolm.mapper.StudentMapper;
import hei.student.schoolm.model.*;
import hei.student.schoolm.repository.CourseAssignmentRepository;
import hei.student.schoolm.validator.StudentValidator;
import java.time.LocalDate;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {
  private final StudentValidator studentValidator;
  private final GroupFlowService groupFlowService;
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final StudentMapper studentMapper;

  @Transactional(readOnly = true)
  public SemesterValidationDto getStudentSemesterValidation(UUID studentId, Semester semester) {
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
}
