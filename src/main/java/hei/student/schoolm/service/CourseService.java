package hei.student.schoolm.service;

import hei.student.schoolm.model.Course;
import hei.student.schoolm.repository.CourseRepository;
import hei.student.schoolm.repository.mapper.JCourseMapper;
import hei.student.schoolm.repository.model.JCourse;
import hei.student.schoolm.repository.model.JTeacher;
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
  private final JCourseMapper jCourseMapper;

  @Transactional
  public Course assignTeachers(UUID courseId, List<UUID> teacherIds) {
    JCourse course = validator.checkCourseExists(courseId);
    List<JTeacher> teachers = validator.checkTeachersExist(teacherIds);

    course.setTeachers(teachers);
    JCourse saved = courseRepository.save(course);

    return jCourseMapper.toDomain(saved);
  }
}
