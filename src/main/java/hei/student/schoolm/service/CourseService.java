package hei.student.schoolm.service;

import hei.student.schoolm.dto.CourseDto;
import hei.student.schoolm.mapper.CourseMapper;
import hei.student.schoolm.repository.CourseRepository;
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
  private final CourseMapper mapper;

  @Transactional
  public CourseDto assignTeachers(UUID courseId, List<UUID> teacherIds) {
    JCourse course = validator.checkCourseExists(courseId);
    List<JTeacher> teachers = validator.checkTeachersExist(teacherIds);

    course.setTeachers(teachers);
    JCourse saved = courseRepository.save(course);

    return mapper.toDto(saved);
  }
}
