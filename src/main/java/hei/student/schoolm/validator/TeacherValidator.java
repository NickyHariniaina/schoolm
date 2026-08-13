package hei.student.schoolm.validator;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.repository.TeacherRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeacherValidator {
  private final TeacherRepository teacherRepository;

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
