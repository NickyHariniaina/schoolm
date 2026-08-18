package hei.student.schoolm.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.model.Track;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CourseMapperTest {
  private static final UUID COURSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID TEACHER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

  private final CourseMapper courseMapper = new CourseMapper();

  @Test
  void toDto_maps_all_fields_and_ids() {
    var course =
        Course.builder()
            .id(COURSE_ID)
            .ref("PROG4")
            .title("Exploitation dans le cloud")
            .credit(8)
            .track(Track.EL)
            .semester(Semester.S3)
            .teachers(List.of(Teacher.builder().id(TEACHER_ID).build()))
            .groups(List.of(Group.builder().id(GROUP_ID).build()))
            .build();

    var dto = courseMapper.toDto(course);

    assertEquals(COURSE_ID, dto.getId());
    assertEquals("PROG4", dto.getRef());
    assertEquals("Exploitation dans le cloud", dto.getTitle());
    assertEquals(8, dto.getCredit());
    assertEquals("EL", dto.getTrack());
    assertEquals("S3", dto.getSemester());
    assertEquals(List.of(TEACHER_ID), dto.getTeacherIds());
    assertEquals(List.of(GROUP_ID), dto.getGroupIds());
  }

  @Test
  void toDtoList_returns_empty_when_null() {
    assertTrue(courseMapper.toDtoList(null).isEmpty());
  }

  @Test
  void toDtoList_maps_each_course() {
    var course =
        Course.builder().id(COURSE_ID).ref("PROG4").track(Track.EL).semester(Semester.S3).build();

    var result = courseMapper.toDtoList(List.of(course));

    assertEquals(1, result.size());
    assertEquals("PROG4", result.get(0).getRef());
  }
}
