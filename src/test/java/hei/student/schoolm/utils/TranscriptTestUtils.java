package hei.student.schoolm.utils;

import hei.student.schoolm.dto.CourseGradeDto;
import hei.student.schoolm.dto.ExamGradeDto;
import hei.student.schoolm.dto.TranscriptDto;
import hei.student.schoolm.dto.TranscriptStatus;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Exam;
import hei.student.schoolm.model.Grade;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.util.Fraction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class TranscriptTestUtils {
  public static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  public static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  public static final UUID COHORT_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
  public static final UUID COURSE_S1_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");
  public static final UUID COURSE_S2_ID = UUID.fromString("00000000-0000-0000-0000-000000000012");
  public static final UUID COURSE_S3_ID = UUID.fromString("00000000-0000-0000-0000-000000000013");
  public static final UUID COURSE_S4_ID = UUID.fromString("00000000-0000-0000-0000-000000000014");
  public static final UUID COURSE_S5_ID = UUID.fromString("00000000-0000-0000-0000-000000000015");
  public static final UUID COURSE_S6_ID = UUID.fromString("00000000-0000-0000-0000-000000000016");
  public static final UUID EXAM_1_ID = UUID.fromString("00000000-0000-0000-0000-000000000021");
  public static final UUID EXAM_2_ID = UUID.fromString("00000000-0000-0000-0000-000000000022");
  public static final UUID GRADE_1_ID = UUID.fromString("00000000-0000-0000-0000-000000000031");
  public static final UUID GRADE_2_ID = UUID.fromString("00000000-0000-0000-0000-000000000032");

  private TranscriptTestUtils() {}

  public static Course createCourse(
      UUID id, String ref, Semester semester, List<Exam> exams, List<Grade> grades) {
    return Course.builder()
        .id(id)
        .ref(ref)
        .title("Course " + ref)
        .credit(8)
        .track(Track.EL)
        .semester(semester)
        .exams(exams)
        .grades(grades)
        .build();
  }

  public static Exam createExam(UUID id, Course course, Fraction coefficient) {
    return Exam.builder()
        .id(id)
        .course(course)
        .dateExam(LocalDate.of(2026, 1, 15))
        .coefficient(coefficient)
        .build();
  }

  public static Grade createGrade(UUID id, Student student, Exam exam, BigDecimal value) {
    return Grade.builder().id(id).student(student).exam(exam).value(value).changeReason("").build();
  }

  public static TranscriptDto createTranscriptDto() {
    return TranscriptDto.builder()
        .studentId(STUDENT_ID)
        .studentRef("STD26001")
        .firstName("Tokyo")
        .lastName("Watt")
        .groupRef("L1-EL-01")
        .cohortRef("P24")
        .academicYear("2025-2026")
        .semesters(List.of(Semester.S3, Semester.S4))
        .courses(
            List.of(
                CourseGradeDto.builder()
                    .courseId(COURSE_S3_ID)
                    .ref("PROG4")
                    .title("Exploitation dans le cloud")
                    .semester(Semester.S3)
                    .credit(8)
                    .coefficientSum(1.0)
                    .exams(
                        List.of(
                            ExamGradeDto.builder()
                                .examId(EXAM_1_ID)
                                .date("2025-11-20")
                                .coefficient(0.5)
                                .value(new BigDecimal("14.5"))
                                .build()))
                    .finalGrade(new BigDecimal("14.5"))
                    .status(TranscriptStatus.COMPLET)
                    .build()))
        .status(TranscriptStatus.COMPLET)
        .build();
  }
}
