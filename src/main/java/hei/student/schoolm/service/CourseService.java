package hei.student.schoolm.service;

import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.repository.CourseRepository;
import hei.student.schoolm.validator.CourseTeacherValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {
  private final CourseRepository courseRepository;
  private final CourseTeacherValidator validator;

  @Transactional
  public Course assignTeachers(UUID courseId, List<UUID> teacherIds) {
    Course course = validator.checkCourseExists(courseId);
    List<Teacher> teachers = validator.checkTeachersExist(teacherIds);

    course.setTeachers(teachers);
    return courseRepository.save(course);
  }
}
