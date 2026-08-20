package hei.student.schoolm.service;

import hei.student.schoolm.dto.CourseAssignmentRequest;
import hei.student.schoolm.dto.CourseAssignmentResponse;
import hei.student.schoolm.dto.CurriculumStatusResponse;
import hei.student.schoolm.exception.ForbiddenException;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.mapper.CourseAssignmentMapper;
import hei.student.schoolm.mapper.CourseMapper;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.CourseAssignment;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.repository.CourseAssignmentRepository;
import hei.student.schoolm.repository.StudentRepository;
import hei.student.schoolm.util.SecurityUtil;
import hei.student.schoolm.validator.CourseAssignmentValidator;
import hei.student.schoolm.validator.CourseValidator;
import hei.student.schoolm.validator.GroupValidator;
import hei.student.schoolm.validator.TeacherValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseAssignmentService {
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final CourseValidator courseValidator;
  private final GroupValidator groupValidator;
  private final TeacherValidator teacherValidator;
  private final CourseAssignmentMapper courseAssignmentMapper;
  private final CourseMapper courseMapper;
  private final CourseAssignmentValidator validator;
  private final SecurityUtil securityUtil;
  private final StudentRepository studentRepository;

  @Transactional(readOnly = true)
  public Page<CourseAssignmentResponse> getByFilter(
      UUID groupId, UUID teacherId, UUID courseId, Integer academicYear, Pageable pageable) {
    if (securityUtil.isTeacher()) {
      teacherId = securityUtil.getCurrentUserIdOrThrow();
    }
    if (securityUtil.isStudent()) {
      var student =
          studentRepository
              .findById(securityUtil.getCurrentUserIdOrThrow())
              .orElseThrow(() -> new NotFoundException("Student not found"));
      groupId = student.getGroup() == null ? null : student.getGroup().getId();
    }
    return courseAssignmentRepository
        .findFilterPaged(groupId, teacherId, courseId, academicYear, pageable)
        .map(courseAssignmentMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public CourseAssignmentResponse getById(UUID id) {
    var entity = findEntityOrThrow(id);
    if (securityUtil.isTeacher()) {
      var teacherId = securityUtil.getCurrentUserIdOrThrow();
      if (!entity.teacherIds().contains(teacherId)) {
        throw new ForbiddenException("You may only access course assignments you teach");
      }
    }
    if (securityUtil.isStudent()) {
      var student =
          studentRepository
              .findById(securityUtil.getCurrentUserIdOrThrow())
              .orElseThrow(() -> new NotFoundException("Student not found"));
      var currentGroupId = student.getGroup() == null ? null : student.getGroup().getId();
      if (!entity.getGroup().getId().equals(currentGroupId)) {
        throw new ForbiddenException("This course assignment is not part of your curriculum");
      }
    }
    return courseAssignmentMapper.toResponse(entity);
  }

  @Transactional
  public List<CourseAssignmentResponse> upsert(List<CourseAssignmentRequest> requests) {
    validator.validateCreditCeilings(requests);
    return requests.stream().map(this::upsertOne).toList();
  }

  private CourseAssignmentResponse upsertOne(CourseAssignmentRequest request) {
    var course = courseValidator.checkCourseExists(request.courseId());
    var group = groupValidator.checkGroupExists(request.groupId());
    validator.validateCurriculum(course, group, request.semester());

    var teachers = teacherValidator.checkTeachersExist(request.teacherIds());
    var assignment =
        request.id() == null
            ? newAssignment(request, course, group, teachers)
            : updateAssignment(request, course, group, teachers);
    return courseAssignmentMapper.toResponse(courseAssignmentRepository.save(assignment));
  }

  private CourseAssignment newAssignment(
      CourseAssignmentRequest request, Course course, Group group, List<Teacher> teachers) {
    validator.validateNotDuplicate(
        null, course.getId(), group.getId(), request.academicYear(), request.semester());
    return CourseAssignment.builder()
        .id(UUID.randomUUID())
        .course(course)
        .group(group)
        .teachers(teachers)
        .academicYear(request.academicYear())
        .semester(request.semester())
        .credits(request.credits())
        .build();
  }

  private CourseAssignment updateAssignment(
      CourseAssignmentRequest request, Course course, Group group, List<Teacher> teachers) {
    var entity = findEntityOrThrow(request.id());
    entity.setCourse(course);
    entity.setGroup(group);
    entity.setTeachers(teachers);
    entity.setAcademicYear(request.academicYear());
    entity.setSemester(request.semester());
    entity.setCredits(request.credits());
    return entity;
  }

  @Transactional
  public void delete(UUID id) {
    if (!securityUtil.isAdmin()) {
      throw new ForbiddenException("Only an admin can delete a course assignment");
    }
    var entity = findEntityOrThrow(id);
    courseAssignmentRepository.delete(entity);
  }

  @Transactional(readOnly = true)
  public CurriculumStatusResponse getCurriculumStatus(
      UUID groupId, int academicYear, Semester semester) {
    var group = groupValidator.checkGroupExists(groupId);
    var assignments =
        courseAssignmentRepository.findByGroupIdAndAcademicYearAndSemester(
            groupId, academicYear, semester);
    int assignedCredits = assignments.stream().mapToInt(CourseAssignment::getCredits).sum();
    var target = validator.creditsPerSemester();

    var assignedCourseIds = assignments.stream().map(a -> a.getCourse().getId()).toList();
    var allCourses = courseValidator.getAllCourses();
    var missing =
        allCourses.stream()
            .filter(c -> c.getSemester() == semester)
            .filter(
                c ->
                    c.getTrack() == null
                        || c.getTrack() == Track.COMMON
                        || c.getTrack() == group.getTrack())
            .filter(c -> !assignedCourseIds.contains(c.getId()))
            .map(courseMapper::toDto)
            .toList();

    return new CurriculumStatusResponse(
        semester,
        assignedCredits,
        target,
        assignedCredits == target && missing.isEmpty(),
        missing,
        courseAssignmentMapper.toResponseList(assignments));
  }

  private CourseAssignment findEntityOrThrow(UUID id) {
    return courseAssignmentRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("CourseAssignment with id: " + id + " not found"));
  }
}
