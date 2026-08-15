package hei.student.schoolm.repository.mapper;

import static hei.student.schoolm.utils.GroupTestUtils.createGroup;
import static hei.student.schoolm.utils.GroupTestUtils.createJGroup;
import static org.junit.jupiter.api.Assertions.assertEquals;

import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.model.User;
import hei.student.schoolm.repository.model.JCourse;
import hei.student.schoolm.repository.model.JExam;
import hei.student.schoolm.repository.model.JGrade;
import hei.student.schoolm.repository.model.JGroup;
import hei.student.schoolm.repository.model.JStudent;
import hei.student.schoolm.repository.model.JTeacher;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JCourseMapperTest {
  private static final UUID COURSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID TEACHER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
  private static final UUID EXAM_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
  private static final UUID GRADE_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
  private static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000006");

  private final JCourseMapper jCourseMapper =
      new JCourseMapper(
          new JTeacherMapper(),
          new JGroupMapper(new JCohortMapper(), Mockito.mock(JCourseMapper.class)),
          new JExamMapper(new JGradeMapper()));

  @Test
  void toDomain_maps_all_fields_and_teachers_and_groups() {
    var jCourse =
        JCourse.builder()
            .id(COURSE_ID)
            .ref("PROG4")
            .title("Exploitation dans le cloud")
            .credit(8)
            .track(Track.EL)
            .semester(Semester.S3)
            .teachers(List.of(createJTeacher()))
            .groups(List.of(createJGroup(GROUP_ID, "L1-EL-01", Track.EL)))
            .build();

    var course = jCourseMapper.toDomain(jCourse);

    assertEquals(COURSE_ID, course.getId());
    assertEquals("PROG4", course.getRef());
    assertEquals("Exploitation dans le cloud", course.getTitle());
    assertEquals(8, course.getCredit());
    assertEquals(Track.EL, course.getTrack());
    assertEquals(Semester.S3, course.getSemester());
    assertEquals(List.of(TEACHER_ID), course.getTeachers().stream().map(Teacher::getId).toList());
    assertEquals(List.of(GROUP_ID), course.getGroups().stream().map(Group::getId).toList());
  }

  @Test
  void toEntity_maps_all_fields_and_teachers_and_groups() {
    var course =
        Course.builder()
            .id(COURSE_ID)
            .ref("PROG4")
            .title("Exploitation dans le cloud")
            .credit(8)
            .track(Track.EL)
            .semester(Semester.S3)
            .teachers(List.of(createTeacher()))
            .groups(List.of(createGroup(GROUP_ID, "L1-EL-01", Track.EL)))
            .build();

    var jCourse = jCourseMapper.toEntity(course);

    assertEquals(COURSE_ID, jCourse.getId());
    assertEquals("PROG4", jCourse.getRef());
    assertEquals("Exploitation dans le cloud", jCourse.getTitle());
    assertEquals(8, jCourse.getCredit());
    assertEquals(Track.EL, jCourse.getTrack());
    assertEquals(Semester.S3, jCourse.getSemester());
    assertEquals(List.of(TEACHER_ID), jCourse.getTeachers().stream().map(JTeacher::getId).toList());
    assertEquals(List.of(GROUP_ID), jCourse.getGroups().stream().map(JGroup::getId).toList());
  }

  @Test
  void toDomain_maps_exams_and_grades() {
    var jGrade =
        JGrade.builder()
            .id(GRADE_ID)
            .student(JStudent.builder().id(STUDENT_ID).build())
            .exam(JExam.builder().id(EXAM_ID).build())
            .value(new BigDecimal("14.5"))
            .build();
    var jExam =
        JExam.builder()
            .id(EXAM_ID)
            .dateExam(LocalDate.of(2026, 1, 15))
            .coefNumerator(1)
            .coefDenominator(2)
            .grades(List.of(jGrade))
            .build();
    var jCourse =
        JCourse.builder()
            .id(COURSE_ID)
            .ref("PROG4")
            .title("Exploitation dans le cloud")
            .credit(8)
            .track(Track.EL)
            .semester(Semester.S3)
            .exams(List.of(jExam))
            .build();

    var course = jCourseMapper.toDomain(jCourse);

    assertEquals(1, course.getExams().size());
    assertEquals(EXAM_ID, course.getExams().get(0).getId());
    assertEquals(0.5, course.getExams().get(0).getCoefficient().toDouble());
    assertEquals(1, course.getGrades().size());
    assertEquals(STUDENT_ID, course.getGrades().get(0).getStudent().getId());
    assertEquals(EXAM_ID, course.getGrades().get(0).getExam().getId());
    assertEquals(new BigDecimal("14.5"), course.getGrades().get(0).getValue());
  }

  @Test
  void toDomainWithoutGroups_maps_no_groups() {
    var jCourse =
        JCourse.builder()
            .id(COURSE_ID)
            .ref("PROG4")
            .title("Exploitation dans le cloud")
            .credit(8)
            .track(Track.EL)
            .semester(Semester.S3)
            .groups(List.of(createJGroup(GROUP_ID, "L1-EL-01", Track.EL)))
            .build();

    var course = jCourseMapper.toDomainWithoutGroups(jCourse);

    assertEquals(List.of(), course.getGroups());
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
