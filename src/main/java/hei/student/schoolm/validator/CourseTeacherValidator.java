package hei.student.schoolm.validator;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.repository.CourseRepository;
import hei.student.schoolm.repository.TeacherRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseTeacherValidator {
  private final CourseRepository courseRepository;
  private final TeacherRepository teacherRepository;

  public Course checkCourseExists(UUID courseId) {
    return courseRepository
        .findById(courseId)
        .orElseThrow(() -> new NotFoundException("Course " + courseId + " not found"));
  }

  public List<Teacher> checkTeachersExist(List<UUID> teacherIds) {
    var uniqueIds = teacherIds.stream().distinct().toList();
    var teachers = teacherRepository.findAllById(uniqueIds);
    var foundIds = teachers.stream().map(Teacher::getId).collect(Collectors.toSet());
    var missing = uniqueIds.stream().filter(id -> !foundIds.contains(id)).toList();
    if (!missing.isEmpty()) {
      throw new NotFoundException("Teacher(s) not found: " + missing);
    }
    return teachers;
  }
}
