package hei.student.schoolm.repository.mapper;

import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.repository.model.JTeacher;
import org.springframework.stereotype.Component;

@Component
public class JTeacherMapper {
  public Teacher toDomain(JTeacher jTeacher) {
    return Teacher.builder()
        .id(jTeacher.getId())
        .email(jTeacher.getEmail())
        .firstName(jTeacher.getFirstName())
        .lastName(jTeacher.getLastName())
        .role(jTeacher.getRole())
        .createdAt(jTeacher.getCreatedAt())
        .updatedAt(jTeacher.getUpdatedAt())
        .build();
  }

  public JTeacher toEntity(Teacher teacher) {
    return JTeacher.builder()
        .id(teacher.getId())
        .email(teacher.getEmail())
        .firstName(teacher.getFirstName())
        .lastName(teacher.getLastName())
        .role(teacher.getRole())
        .createdAt(teacher.getCreatedAt())
        .updatedAt(teacher.getUpdatedAt())
        .build();
  }
}
