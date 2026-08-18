package hei.student.schoolm.utils;

import hei.student.schoolm.model.Cohort;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Exam;
import hei.student.schoolm.model.Grade;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.util.Fraction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class GraduateTestUtils {
  public static final UUID COHORT_ID = UUID.fromString("00000000-0000-0000-0000-000000000401");
  public static final UUID GROUP_EL_ID = UUID.fromString("00000000-0000-0000-0000-000000000402");
  public static final UUID GROUP_TN_ID = UUID.fromString("00000000-0000-0000-0000-000000000403");
  public static final UUID STUDENT_PASS_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000501");
  public static final UUID STUDENT_FAIL_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000502");
  public static final UUID STUDENT_TN_ID = UUID.fromString("00000000-0000-0000-0000-000000000503");
  public static final UUID COURSE_S4_ID = UUID.fromString("00000000-0000-0000-0000-000000000601");
  public static final UUID COURSE_S5_ID = UUID.fromString("00000000-0000-0000-0000-000000000602");
  public static final UUID COURSE_S6_ID = UUID.fromString("00000000-0000-0000-0000-000000000603");

  private GraduateTestUtils() {}

  public static Cohort cohort(int entryYear) {
    return Cohort.builder()
        .id(COHORT_ID)
        .ref("P24")
        .entryYear(java.time.Year.of(entryYear))
        .build();
  }

  public static Group group(UUID id, String ref, Track track, Cohort cohort, List<Course> courses) {
    return Group.builder().id(id).ref(ref).track(track).cohort(cohort).courses(courses).build();
  }

  public static Student student(UUID id, String ref, String firstName, String lastName) {
    return Student.builder().id(id).reference(ref).firstName(firstName).lastName(lastName).build();
  }

  public static Course gradedCourse(
      UUID id,
      String ref,
      Semester semester,
      int credit,
      Map<Student, BigDecimal> gradesByStudent) {
    var exam1 = exam(UUID.randomUUID(), new Fraction(1, 2));
    var exam2 = exam(UUID.randomUUID(), new Fraction(1, 2));
    var grades =
        gradesByStudent.entrySet().stream()
            .flatMap(
                entry ->
                    List.of(
                        grade(UUID.randomUUID(), entry.getKey(), exam1, entry.getValue()),
                        grade(UUID.randomUUID(), entry.getKey(), exam2, entry.getValue()))
                        .stream())
            .toList();
    return Course.builder()
        .id(id)
        .ref(ref)
        .title("Course " + ref)
        .credit(credit)
        .track(Track.COMMON)
        .semester(semester)
        .exams(List.of(exam1, exam2))
        .grades(grades)
        .build();
  }

  public static Course courseWithoutExams(UUID id, String ref, Semester semester, int credit) {
    return Course.builder()
        .id(id)
        .ref(ref)
        .title("Course " + ref)
        .credit(credit)
        .track(Track.COMMON)
        .semester(semester)
        .exams(List.of())
        .grades(List.of())
        .build();
  }

  private static Exam exam(UUID id, Fraction coefficient) {
    return Exam.builder()
        .id(id)
        .dateExam(LocalDate.of(2026, 6, 10))
        .coefficient(coefficient)
        .build();
  }

  private static Grade grade(UUID id, Student student, Exam exam, BigDecimal value) {
    return Grade.builder().id(id).student(student).exam(exam).value(value).changeReason("").build();
  }
}
