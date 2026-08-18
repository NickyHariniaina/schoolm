package hei.student.schoolm.repository.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.model.User;
import hei.student.schoolm.repository.model.JTeacher;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JTeacherMapperTest {
  private static final UUID TEACHER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private final JTeacherMapper jTeacherMapper = new JTeacherMapper();

  @Test
  void toDomain_maps_all_fields() {
    var createdAt = Instant.parse("2024-10-01T08:00:00Z");
    var updatedAt = Instant.parse("2025-01-01T08:00:00Z");
    var jTeacher =
        JTeacher.builder()
            .id(TEACHER_ID)
            .email("toky@hei.school")
            .firstName("Toky")
            .lastName("Rakoto")
            .role(User.Role.TEACHER)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();

    var teacher = jTeacherMapper.toDomain(jTeacher);

    assertEquals(TEACHER_ID, teacher.getId());
    assertEquals("toky@hei.school", teacher.getEmail());
    assertEquals("Toky", teacher.getFirstName());
    assertEquals("Rakoto", teacher.getLastName());
    assertEquals(User.Role.TEACHER, teacher.getRole());
    assertEquals(createdAt, teacher.getCreatedAt());
    assertEquals(updatedAt, teacher.getUpdatedAt());
  }

  @Test
  void toEntity_maps_all_fields() {
    var createdAt = Instant.parse("2024-10-01T08:00:00Z");
    var updatedAt = Instant.parse("2025-01-01T08:00:00Z");
    var teacher =
        Teacher.builder()
            .id(TEACHER_ID)
            .email("toky@hei.school")
            .firstName("Toky")
            .lastName("Rakoto")
            .role(User.Role.TEACHER)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();

    var jTeacher = jTeacherMapper.toEntity(teacher);

    assertEquals(TEACHER_ID, jTeacher.getId());
    assertEquals("toky@hei.school", jTeacher.getEmail());
    assertEquals("Toky", jTeacher.getFirstName());
    assertEquals("Rakoto", jTeacher.getLastName());
    assertEquals(User.Role.TEACHER, jTeacher.getRole());
    assertEquals(createdAt, jTeacher.getCreatedAt());
    assertEquals(updatedAt, jTeacher.getUpdatedAt());
  }
}
