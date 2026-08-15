package hei.student.schoolm.repository.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hei.student.schoolm.model.User.Role;
import hei.student.schoolm.repository.model.JGroup;
import hei.student.schoolm.repository.model.JStudent;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JStudentMapperTest {
  private static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  private final JStudentMapper jStudentMapper =
      new JStudentMapper(
          new JGroupMapper(new JCohortMapper(), Mockito.mock(JCourseMapper.class)));

  @Test
  void toDomain_maps_all_fields_and_group() {
    var jStudent =
        JStudent.builder()
            .id(STUDENT_ID)
            .reference("STD26001")
            .email("toky@hei.school")
            .firstName("Tokyo")
            .lastName("Watt")
            .role(Role.STUDENT)
            .group(JGroup.builder().id(GROUP_ID).ref("L1-EL-01").build())
            .build();

    var student = jStudentMapper.toDomain(jStudent);

    assertEquals(STUDENT_ID, student.getId());
    assertEquals("STD26001", student.getReference());
    assertEquals("toky@hei.school", student.getEmail());
    assertEquals("Tokyo", student.getFirstName());
    assertEquals("Watt", student.getLastName());
    assertEquals(Role.STUDENT, student.getRole());
    assertEquals(GROUP_ID, student.getGroup().getId());
    assertEquals("L1-EL-01", student.getGroup().getRef());
  }

  @Test
  void toEntity_maps_all_fields_and_group() {
    var student =
        hei.student.schoolm.model.Student.builder()
            .id(STUDENT_ID)
            .reference("STD26001")
            .email("toky@hei.school")
            .firstName("Tokyo")
            .lastName("Watt")
            .role(Role.STUDENT)
            .group(hei.student.schoolm.model.Group.builder().id(GROUP_ID).ref("L1-EL-01").build())
            .build();

    var jStudent = jStudentMapper.toEntity(student);

    assertEquals(STUDENT_ID, jStudent.getId());
    assertEquals("STD26001", jStudent.getReference());
    assertEquals("toky@hei.school", jStudent.getEmail());
    assertEquals("Tokyo", jStudent.getFirstName());
    assertEquals("Watt", jStudent.getLastName());
    assertEquals(Role.STUDENT, jStudent.getRole());
    assertEquals(GROUP_ID, jStudent.getGroup().getId());
    assertEquals("L1-EL-01", jStudent.getGroup().getRef());
  }
}