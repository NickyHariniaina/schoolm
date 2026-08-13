package hei.student.schoolm.repository.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.model.User;
import hei.student.schoolm.repository.model.JCourse;
import hei.student.schoolm.repository.model.JTeacher;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JCourseMapperTest {
  private static final UUID COURSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID TEACHER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  private final JCourseMapper jCourseMapper = new JCourseMapper(new JTeacherMapper());

  @Test
  void toDomain_maps_all_fields_and_teachers() {
    JCourse jCourse =
        JCourse.builder()
            .id(COURSE_ID)
            .ref("PROG4")
            .title("Exploitation dans le cloud")
            .credit(8)
            .track(Track.EL)
            .semester(Semester.S3)
            .teachers(List.of(createJTeacher()))
            .build();

    Course course = jCourseMapper.toDomain(jCourse);

    assertEquals(COURSE_ID, course.getId());
    assertEquals("PROG4", course.getRef());
    assertEquals("Exploitation dans le cloud", course.getTitle());
    assertEquals(8, course.getCredit());
    assertEquals(Track.EL, course.getTrack());
    assertEquals(Semester.S3, course.getSemester());
    assertEquals(List.of(TEACHER_ID), course.getTeachers().stream().map(Teacher::getId).toList());
  }

  @Test
  void toEntity_maps_all_fields_and_teachers() {
    Course course =
        Course.builder()
            .id(COURSE_ID)
            .ref("PROG4")
            .title("Exploitation dans le cloud")
            .credit(8)
            .track(Track.EL)
            .semester(Semester.S3)
            .teachers(List.of(createTeacher()))
            .build();

    JCourse jCourse = jCourseMapper.toEntity(course);

    assertEquals(COURSE_ID, jCourse.getId());
    assertEquals("PROG4", jCourse.getRef());
    assertEquals("Exploitation dans le cloud", jCourse.getTitle());
    assertEquals(8, jCourse.getCredit());
    assertEquals(Track.EL, jCourse.getTrack());
    assertEquals(Semester.S3, jCourse.getSemester());
    assertEquals(
        List.of(TEACHER_ID), jCourse.getTeachers().stream().map(JTeacher::getId).toList());
  }

  private JTeacher createJTeacher() {
    return JTeacher.builder()
        .id(TEACHER_ID)
        .email("toky@hei.school")
        .firstName("Toky")
        .lastName("Rakoto")
        .role(User.Role.TEACHER)
        .build();
  }

  private Teacher createTeacher() {
    return Teacher.builder()
        .id(TEACHER_ID)
        .email("toky@hei.school")
        .firstName("Toky")
        .lastName("Rakoto")
        .role(User.Role.TEACHER)
        .build();
  }
}
