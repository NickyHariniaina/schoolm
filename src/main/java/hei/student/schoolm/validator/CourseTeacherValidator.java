package hei.student.schoolm.validator;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.repository.CourseRepository;
import hei.student.schoolm.repository.TeacherRepository;
import hei.student.schoolm.repository.model.JCourse;
import hei.student.schoolm.repository.model.JTeacher;
import java.util.HashSet;
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

  public JCourse checkCourseExists(UUID courseId) {
    return courseRepository
        .findById(courseId)
        .orElseThrow(() -> new NotFoundException("Course " + courseId + " not found"));
  }

  public List<JTeacher> checkTeachersExist(List<UUID> teacherIds) {
    List<UUID> uniqueIds = teacherIds.stream().distinct().toList();
    List<JTeacher> teachers = teacherRepository.findAllById(uniqueIds);
    Set<UUID> foundIds = teachers.stream().map(JTeacher::getId).collect(Collectors.toSet());
    List<UUID> missing = uniqueIds.stream().filter(id -> !foundIds.contains(id)).toList();
    if (!missing.isEmpty()) {
      throw new NotFoundException("Teacher(s) not found: " + missing);
    }
    return teachers;
  }
}
