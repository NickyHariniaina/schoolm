package hei.student.schoolm.utils;

import hei.student.schoolm.model.Cohort;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Exam;
import hei.student.schoolm.model.Grade;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.model.User;
import hei.student.schoolm.util.Fraction;
import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.UUID;

public final class SemesterValidationTestUtils {
  public static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  public static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  public static final UUID COHORT_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
  public static final UUID COURSE_S3_1_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");
  public static final UUID COURSE_S3_2_ID = UUID.fromString("00000000-0000-0000-0000-000000000012");
  public static final UUID COURSE_S4_ID = UUID.fromString("00000000-0000-0000-0000-000000000013");
  public static final UUID EXAM_1_ID = UUID.fromString("00000000-0000-0000-0000-000000000021");
  public static final UUID EXAM_2_ID = UUID.fromString("00000000-0000-0000-0000-000000000022");
  public static final UUID GRADE_1_ID = UUID.fromString("00000000-0000-0000-0000-000000000031");
  public static final UUID GRADE_2_ID = UUID.fromString("00000000-0000-0000-0000-000000000032");

  private SemesterValidationTestUtils() {}

  public static Student createStudent() {
    return Student.builder()
        .id(STUDENT_ID)
        .reference("STD26001")
        .email("toky@hei.school")
        .firstName("Tokyo")
        .lastName("Watt")
        .role(User.Role.STUDENT)
        .group(Group.builder().id(GROUP_ID).build())
        .build();
  }

  public static Group createGroup(List<Course> courses) {
    return Group.builder()
        .id(GROUP_ID)
        .ref("L1-EL-01")
        .track(Track.EL)
        .cohort(Cohort.builder().id(COHORT_ID).ref("P24").entryYear(Year.of(2024)).build())
        .courses(courses)
        .build();
  }

  public static Course createCourse(
      UUID id, String ref, Semester semester, int credit, List<Exam> exams, List<Grade> grades) {
    return Course.builder()
        .id(id)
        .ref(ref)
        .title("Course " + ref)
        .credit(credit)
        .track(Track.EL)
        .semester(semester)
        .exams(exams)
        .grades(grades)
        .build();
  }

  public static Exam createExam(UUID id, Fraction coefficient) {
    return Exam.builder().id(id).coefficient(coefficient).build();
  }

  public static Grade createGrade(UUID id, UUID examId, BigDecimal value) {
    return Grade.builder()
        .id(id)
        .student(Student.builder().id(STUDENT_ID).build())
        .exam(Exam.builder().id(examId).build())
        .value(value)
        .build();
  }
}
