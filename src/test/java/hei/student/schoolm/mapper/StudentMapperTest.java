package hei.student.schoolm.mapper;

import static hei.student.schoolm.utils.SemesterValidationTestUtils.COURSE_S3_1_ID;
import static hei.student.schoolm.utils.SemesterValidationTestUtils.COURSE_S3_2_ID;
import static hei.student.schoolm.utils.SemesterValidationTestUtils.EXAM_1_ID;
import static hei.student.schoolm.utils.SemesterValidationTestUtils.EXAM_2_ID;
import static hei.student.schoolm.utils.SemesterValidationTestUtils.GRADE_1_ID;
import static hei.student.schoolm.utils.SemesterValidationTestUtils.GRADE_2_ID;
import static hei.student.schoolm.utils.SemesterValidationTestUtils.createCourse;
import static hei.student.schoolm.utils.SemesterValidationTestUtils.createExam;
import static hei.student.schoolm.utils.SemesterValidationTestUtils.createGrade;
import static hei.student.schoolm.utils.SemesterValidationTestUtils.createGroup;
import static hei.student.schoolm.utils.SemesterValidationTestUtils.createStudent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hei.student.schoolm.dto.SemesterValidationDto;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.util.Fraction;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class StudentMapperTest {
  private final StudentMapper studentMapper = new StudentMapper();

  private SemesterValidationDto map(Course... courses) {
    return studentMapper.toSemesterValidationDto(
        createStudent(), createGroup(List.of(courses)), Semester.S3, List.of(courses));
  }

  @Test
  void should_validate_when_all_courses_acquired() {
    var result =
        map(
            createCourse(
                COURSE_S3_1_ID,
                "PROG4",
                Semester.S3,
                8,
                List.of(createExam(EXAM_1_ID, new Fraction(1, 1))),
                List.of(createGrade(GRADE_1_ID, EXAM_1_ID, new BigDecimal("14.5")))),
            createCourse(
                COURSE_S3_2_ID,
                "WEB1",
                Semester.S3,
                7,
                List.of(createExam(EXAM_2_ID, new Fraction(1, 1))),
                List.of(createGrade(GRADE_2_ID, EXAM_2_ID, new BigDecimal("12")))));

    assertTrue(result.isValidated());
    assertEquals(15, result.getTotalCredits());
    assertEquals(15, result.getAcquiredCredits());
    assertEquals(2, result.getCourses().size());
    assertTrue(result.getCourses().get(0).isAcquired());
    assertEquals(new BigDecimal("14.5"), result.getCourses().get(0).getFinalGrade());
  }

  @Test
  void should_not_validate_when_course_below_10() {
    var result =
        map(
            createCourse(
                COURSE_S3_1_ID,
                "PROG4",
                Semester.S3,
                8,
                List.of(createExam(EXAM_1_ID, new Fraction(1, 1))),
                List.of(createGrade(GRADE_1_ID, EXAM_1_ID, new BigDecimal("14")))),
            createCourse(
                COURSE_S3_2_ID,
                "WEB1",
                Semester.S3,
                7,
                List.of(createExam(EXAM_2_ID, new Fraction(1, 1))),
                List.of(createGrade(GRADE_2_ID, EXAM_2_ID, new BigDecimal("8")))));

    assertFalse(result.isValidated());
    assertEquals(15, result.getTotalCredits());
    assertEquals(8, result.getAcquiredCredits());
    assertTrue(result.getCourses().get(0).isAcquired());
    assertFalse(result.getCourses().get(1).isAcquired());
  }

  @Test
  void should_not_acquire_course_with_missing_grade() {
    var result =
        map(
            createCourse(
                COURSE_S3_1_ID,
                "PROG4",
                Semester.S3,
                8,
                List.of(createExam(EXAM_1_ID, new Fraction(1, 1))),
                List.of()));

    assertFalse(result.isValidated());
    assertEquals(0, result.getAcquiredCredits());
    assertEquals(new BigDecimal("0.0"), result.getCourses().get(0).getFinalGrade());
    assertFalse(result.getCourses().get(0).isAcquired());
  }

  @Test
  void should_not_acquire_course_without_exams() {
    var result = map(createCourse(COURSE_S3_1_ID, "PROG4", Semester.S3, 8, List.of(), List.of()));

    assertFalse(result.isValidated());
    assertNull(result.getCourses().get(0).getFinalGrade());
    assertFalse(result.getCourses().get(0).isAcquired());
  }

  @Test
  void should_not_validate_when_no_courses() {
    var result = map();

    assertFalse(result.isValidated());
    assertEquals(0, result.getTotalCredits());
    assertEquals(0, result.getAcquiredCredits());
    assertTrue(result.getCourses().isEmpty());
  }

  @Test
  void should_compute_weighted_final_grade() {
    var result =
        map(
            createCourse(
                COURSE_S3_1_ID,
                "PROG4",
                Semester.S3,
                8,
                List.of(
                    createExam(EXAM_1_ID, new Fraction(1, 2)),
                    createExam(EXAM_2_ID, new Fraction(1, 2))),
                List.of(
                    createGrade(GRADE_1_ID, EXAM_1_ID, new BigDecimal("12")),
                    createGrade(GRADE_2_ID, EXAM_2_ID, new BigDecimal("8")))));

    assertEquals(new BigDecimal("10.0"), result.getCourses().get(0).getFinalGrade());
    assertTrue(result.getCourses().get(0).isAcquired());
    assertTrue(result.isValidated());
  }
}
