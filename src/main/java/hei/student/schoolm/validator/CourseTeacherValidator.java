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
    List<UUID> uniqueIds = teacherIds.stream().distinct().toList();
    List<Teacher> teachers = teacherRepository.findAllById(uniqueIds);
    Set<UUID> foundIds = teachers.stream().map(Teacher::getId).collect(Collectors.toSet());
    List<UUID> missing = uniqueIds.stream().filter(id -> !foundIds.contains(id)).toList();
    if (!missing.isEmpty()) {
      throw new NotFoundException("Teacher(s) not found: " + missing);
    }
    return teachers;
  }
}
