package hei.student.schoolm.repository.mapper;

import hei.student.schoolm.model.Student;
import hei.student.schoolm.repository.model.JStudent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JStudentMapper {
  private final JGroupMapper mapper;

  public Student toDomain(JStudent jStudent) {
    return Student.builder()
        .id(jStudent.getId())
        .reference(jStudent.getReference())
        .email(jStudent.getEmail())
        .firstName(jStudent.getFirstName())
        .lastName(jStudent.getLastName())
        .role(jStudent.getRole())
        .password(jStudent.getPassword())
        .createdAt(jStudent.getCreatedAt())
        .updatedAt(jStudent.getUpdatedAt())
        .group(jStudent.getGroup() == null ? null : mapper.toDomain(jStudent.getGroup()))
        .build();
  }

  public JStudent toEntity(Student student) {
    return JStudent.builder()
        .id(student.getId())
        .reference(student.getReference())
        .email(student.getEmail())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .role(student.getRole())
        .password(student.getPassword())
        .createdAt(student.getCreatedAt())
        .updatedAt(student.getUpdatedAt())
        .group(student.getGroup() == null ? null : mapper.toEntity(student.getGroup()))
        .build();
  }
}
