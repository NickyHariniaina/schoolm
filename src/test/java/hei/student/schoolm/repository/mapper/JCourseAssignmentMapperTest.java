package hei.student.schoolm.repository.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.CourseAssignment;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.repository.model.JCohort;
import hei.student.schoolm.repository.model.JCourse;
import hei.student.schoolm.repository.model.JCourseAssignment;
import hei.student.schoolm.repository.model.JGroup;
import hei.student.schoolm.repository.model.JTeacher;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JCourseAssignmentMapperTest {
  private static final UUID ASSIGNMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000701");
  private static final UUID COURSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000702");
  private static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000703");
  private static final UUID TEACHER_ID = UUID.fromString("00000000-0000-0000-0000-000000000704");

  private final JCourseAssignmentMapper mapper =
      new JCourseAssignmentMapper(
          new JCourseMapper(
              new JTeacherMapper(),
              Mockito.mock(JGroupMapper.class),
              new JExamMapper(new JGradeMapper())),
          new JGroupMapper(new JCohortMapper(), Mockito.mock(JCourseMapper.class)),
          new JTeacherMapper());

  @Test
  void toDomain_maps_fields_teachers_and_nested_objects() {
    var jAssignment =
        JCourseAssignment.builder()
            .id(ASSIGNMENT_ID)
            .course(
                JCourse.builder()
                    .id(COURSE_ID)
                    .ref("PROG4")
                    .title("Programmation 4")
                    .credit(8)
                    .track(Track.EL)
                    .semester(Semester.S3)
                    .build())
            .group(
                JGroup.builder()
                    .id(GROUP_ID)
                    .ref("K1")
                    .track(Track.EL)
                    .cohort(
                        JCohort.builder().id(UUID.randomUUID()).ref("P24").entryYear(2024).build())
                    .build())
            .teachers(
                List.of(
                    JTeacher.builder()
                        .id(TEACHER_ID)
                        .firstName("Andry")
                        .lastName("Rakoto")
                        .build()))
            .academicYear(2025)
            .semester(Semester.S3)
            .credits(8)
            .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
            .updatedAt(Instant.parse("2025-01-02T00:00:00Z"))
            .build();

    CourseAssignment assignment = mapper.toDomain(jAssignment);

    assertEquals(ASSIGNMENT_ID, assignment.getId());
    assertEquals(COURSE_ID, assignment.getCourse().getId());
    assertEquals("PROG4", assignment.getCourse().getRef());
    assertEquals(GROUP_ID, assignment.getGroup().getId());
    assertEquals("K1", assignment.getGroup().getRef());
    assertEquals(1, assignment.getTeachers().size());
    assertEquals(TEACHER_ID, assignment.getTeachers().get(0).getId());
    assertEquals(2025, assignment.getAcademicYear());
    assertEquals(Semester.S3, assignment.getSemester());
    assertEquals(8, assignment.getCredits());
    assertEquals(Instant.parse("2025-01-01T00:00:00Z"), assignment.getCreatedAt());
  }

  @Test
  void toDomain_maps_null_teachers_to_empty_list() {
    var jAssignment =
        JCourseAssignment.builder()
            .id(ASSIGNMENT_ID)
            .course(JCourse.builder().id(COURSE_ID).ref("PROG4").build())
            .group(JGroup.builder().id(GROUP_ID).ref("K1").build())
            .academicYear(2025)
            .semester(Semester.S3)
            .credits(8)
            .build();

    CourseAssignment assignment = mapper.toDomain(jAssignment);

    assertEquals(List.of(), assignment.getTeachers());
  }

  @Test
  void toDomain_returns_null_when_input_null() {
    assertNull(mapper.toDomain(null));
  }

  @Test
  void toEntity_maps_fields_and_reduces_relations_to_ids() {
    var assignment =
        CourseAssignment.builder()
            .id(ASSIGNMENT_ID)
            .course(Course.builder().id(COURSE_ID).build())
            .group(Group.builder().id(GROUP_ID).build())
            .teachers(List.of(hei.student.schoolm.model.Teacher.builder().id(TEACHER_ID).build()))
            .academicYear(2025)
            .semester(Semester.S3)
            .credits(8)
            .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
            .updatedAt(Instant.parse("2025-01-02T00:00:00Z"))
            .build();

    JCourseAssignment jAssignment = mapper.toEntity(assignment);

    assertEquals(ASSIGNMENT_ID, jAssignment.getId());
    assertEquals(COURSE_ID, jAssignment.getCourse().getId());
    assertEquals(GROUP_ID, jAssignment.getGroup().getId());
    assertEquals(TEACHER_ID, jAssignment.getTeachers().get(0).getId());
    assertEquals(2025, jAssignment.getAcademicYear());
    assertEquals(Semester.S3, jAssignment.getSemester());
    assertEquals(8, jAssignment.getCredits());
  }

  @Test
  void toEntity_maps_null_relations_and_teachers() {
    var assignment =
        CourseAssignment.builder()
            .id(ASSIGNMENT_ID)
            .teachers(null)
            .academicYear(2025)
            .semester(Semester.S3)
            .credits(8)
            .build();

    JCourseAssignment jAssignment = mapper.toEntity(assignment);

    assertNull(jAssignment.getCourse());
    assertNull(jAssignment.getGroup());
    assertEquals(List.of(), jAssignment.getTeachers());
  }

  @Test
  void toEntity_returns_null_when_input_null() {
    assertNull(mapper.toEntity(null));
  }

  @Test
  void toCourseOnly_returns_course() {
    var course = Course.builder().id(COURSE_ID).ref("PROG4").build();
    var assignment = CourseAssignment.builder().course(course).build();

    assertEquals(course, mapper.toCourseOnly(assignment));
  }
}
