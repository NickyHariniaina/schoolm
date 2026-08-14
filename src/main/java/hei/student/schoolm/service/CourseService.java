package hei.student.schoolm.service;

import hei.student.schoolm.model.Course;
import hei.student.schoolm.repository.CourseRepository;
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

  @Transactional(readOnly = true)
  public List<Course> getAllCourses() {
    return courseRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Course getCourseById(UUID courseId) {
    return courseValidator.checkCourseExists(courseId);
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
