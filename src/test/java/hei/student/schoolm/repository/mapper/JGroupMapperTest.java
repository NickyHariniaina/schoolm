package hei.student.schoolm.repository.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.repository.model.JCohort;
import hei.student.schoolm.repository.model.JCourse;
import hei.student.schoolm.repository.model.JGroup;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JGroupMapperTest {
  private static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID COHORT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID COURSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

  private final JGroupMapper jGroupMapper =
      new JGroupMapper(
          new JCohortMapper(),
          new JCourseMapper(
              new JTeacherMapper(),
              Mockito.mock(JGroupMapper.class),
              new JExamMapper(new JGradeMapper())));

  @Test
  void toDomain_maps_fields_and_cohort() {
    var jGroup =
        JGroup.builder()
            .id(GROUP_ID)
            .ref("L1-EL-01")
            .track(Track.EL)
            .cohort(JCohort.builder().id(COHORT_ID).ref("P24").entryYear(2024).build())
            .build();

    var group = jGroupMapper.toDomain(jGroup);

    assertEquals(GROUP_ID, group.getId());
    assertEquals("L1-EL-01", group.getRef());
    assertEquals(Track.EL, group.getTrack());
    assertEquals(COHORT_ID, group.getCohort().getId());
    assertEquals("P24", group.getCohort().getRef());
  }

  @Test
  void toDomain_maps_null_cohort() {
    var jGroup = JGroup.builder().id(GROUP_ID).ref("L1-EL-01").track(Track.EL).build();

    var group = jGroupMapper.toDomain(jGroup);

    assertNull(group.getCohort());
  }

  @Test
  void toDomainWithCourses_maps_courses_without_groups() {
    var jCourse =
        JCourse.builder()
            .id(COURSE_ID)
            .ref("PROG4")
            .title("Exploitation dans le cloud")
            .credit(8)
            .track(Track.EL)
            .semester(Semester.S3)
            .build();
    var jGroup =
        JGroup.builder()
            .id(GROUP_ID)
            .ref("L1-EL-01")
            .track(Track.EL)
            .cohort(JCohort.builder().id(COHORT_ID).ref("P24").entryYear(2024).build())
            .courses(List.of(jCourse))
            .build();

    var group = jGroupMapper.toDomainWithCourses(jGroup);

    assertEquals(GROUP_ID, group.getId());
    assertEquals(1, group.getCourses().size());
    Course course = group.getCourses().get(0);
    assertEquals(COURSE_ID, course.getId());
    assertEquals("PROG4", course.getRef());
    assertEquals(List.of(), course.getGroups());
  }
}
