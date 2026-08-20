package hei.student.schoolm.service;

import hei.student.schoolm.dto.CourseRequest;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.repository.CourseRepository;
import hei.student.schoolm.repository.jpa.JExamRepository;
import hei.student.schoolm.repository.jpa.JGradeHistoryRepository;
import hei.student.schoolm.repository.jpa.JGradeRepository;
import hei.student.schoolm.validator.CourseValidator;
import hei.student.schoolm.validator.GroupValidator;
import hei.student.schoolm.validator.TeacherValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {
  private final CourseRepository courseRepository;
  private final CourseValidator courseValidator;
  private final TeacherValidator teacherValidator;
  private final GroupValidator groupValidator;
  private final JExamRepository jExamRepository;
  private final JGradeRepository jGradeRepository;
  private final JGradeHistoryRepository jGradeHistoryRepository;

  @Transactional(readOnly = true)
  public List<Course> getAllCourses() {
    return courseRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Course getCourseById(UUID courseId) {
    return courseValidator.checkCourseExists(courseId);
  }

  @Transactional
  public Course upsert(CourseRequest request) {
    if (request.id() == null) {
      return create(request);
    }
    var course = courseValidator.checkCourseExists(request.id());
    course.setRef(request.ref());
    course.setTitle(request.title());
    course.setCredit(request.credit());
    course.setTrack(request.track());
    course.setSemester(request.semester());
    return courseRepository.save(course);
  }

  private Course create(CourseRequest request) {
    var course =
        Course.builder()
            .id(UUID.randomUUID())
            .ref(request.ref())
            .title(request.title())
            .credit(request.credit())
            .track(request.track())
            .semester(request.semester())
            .teachers(List.of())
            .groups(List.of())
            .exams(List.of())
            .grades(List.of())
            .build();
    return courseRepository.save(course);
  }

  @Transactional
  public void delete(UUID courseId) {
    courseValidator.checkCourseExists(courseId);
    var examIds =
        jExamRepository.findAllByCourseId(courseId).stream().map(exam -> exam.getId()).toList();
    for (var examId : examIds) {
      jGradeHistoryRepository.deleteAllByExamId(examId);
      jGradeRepository.deleteAllByExamId(examId);
    }
    jExamRepository.deleteAllByCourseId(courseId);
    courseRepository.deleteById(courseId);
  }

  @Transactional
  public Course assignTeachers(UUID courseId, List<UUID> teacherIds) {
    var course = courseValidator.checkCourseExists(courseId);
    var teachers = teacherValidator.checkTeachersExist(teacherIds);

    course.setTeachers(teachers);
    return courseRepository.save(course);
  }

  @Transactional
  public Course assignGroups(UUID courseId, List<UUID> groupIds) {
    var course = courseValidator.checkCourseExists(courseId);
    var groups = groupValidator.checkGroupsExist(groupIds);

    course.setGroups(groups);
    return courseRepository.save(course);
  }
}
