package hei.student.schoolm.repository.mapper;

import hei.student.schoolm.model.Student;
import hei.student.schoolm.repository.model.JStudent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JStudentMapper {
  private final JGroupMapper jGroupMapper;

  public Student toDomain(JStudent jStudent) {
    return Student.builder()
        .id(jStudent.getId())
        .reference(jStudent.getReference())
        .email(jStudent.getEmail())
        .firstName(jStudent.getFirstName())
        .lastName(jStudent.getLastName())
        .role(jStudent.getRole())
        .createdAt(jStudent.getCreatedAt())
        .updatedAt(jStudent.getUpdatedAt())
        .group(jStudent.getGroup() == null ? null : jGroupMapper.toDomain(jStudent.getGroup()))
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
        .createdAt(student.getCreatedAt())
        .updatedAt(student.getUpdatedAt())
        .group(student.getGroup() == null ? null : jGroupMapper.toEntity(student.getGroup()))
        .build();
  }
}
