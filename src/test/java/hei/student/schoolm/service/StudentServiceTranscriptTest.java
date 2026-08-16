package hei.student.schoolm.service;

import static hei.student.schoolm.utils.StudentTestUtils.createCohort;
import static hei.student.schoolm.utils.StudentTestUtils.createGroup;
import static hei.student.schoolm.utils.StudentTestUtils.createStudent;
import static hei.student.schoolm.utils.TranscriptTestUtils.COURSE_S1_ID;
import static hei.student.schoolm.utils.TranscriptTestUtils.COURSE_S2_ID;
import static hei.student.schoolm.utils.TranscriptTestUtils.COURSE_S3_ID;
import static hei.student.schoolm.utils.TranscriptTestUtils.COURSE_S4_ID;
import static hei.student.schoolm.utils.TranscriptTestUtils.COURSE_S5_ID;
import static hei.student.schoolm.utils.TranscriptTestUtils.COURSE_S6_ID;
import static hei.student.schoolm.utils.TranscriptTestUtils.EXAM_1_ID;
import static hei.student.schoolm.utils.TranscriptTestUtils.EXAM_2_ID;
import static hei.student.schoolm.utils.TranscriptTestUtils.GRADE_1_ID;
import static hei.student.schoolm.utils.TranscriptTestUtils.GRADE_2_ID;
import static hei.student.schoolm.utils.TranscriptTestUtils.GROUP_ID;
import static hei.student.schoolm.utils.TranscriptTestUtils.STUDENT_ID;
import static hei.student.schoolm.utils.TranscriptTestUtils.createCourse;
import static hei.student.schoolm.utils.TranscriptTestUtils.createExam;
import static hei.student.schoolm.utils.TranscriptTestUtils.createGrade;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import hei.student.schoolm.dto.ExamGradeDto;
import hei.student.schoolm.dto.TranscriptDto;
import hei.student.schoolm.dto.TranscriptStatus;
import hei.student.schoolm.exception.BadRequestException;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.*;
import hei.student.schoolm.util.Fraction;
import hei.student.schoolm.validator.GroupValidator;
import hei.student.schoolm.validator.StudentValidator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentServiceTranscriptTest {
  @Mock private StudentValidator studentValidator;
  @Mock private GroupValidator groupValidator;
  @InjectMocks private StudentService studentService;

  private TranscriptDto getTranscript(Student student, Group group, Integer month, Integer year) {
    when(studentValidator.checkStudentExists(STUDENT_ID)).thenReturn(student);
    when(groupValidator.checkGroupExists(GROUP_ID)).thenReturn(group);
    return studentService.getTranscript(STUDENT_ID, month, year);
  }

  @Test
  void should_return_S1_for_october_of_entry_year() {
    assertEquals(Semester.S1, studentService.computeSemester(Year.of(2024), 10, 2024));
  }

  @Test
  void should_return_S2_for_september() {
    assertEquals(Semester.S2, studentService.computeSemester(Year.of(2024), 9, 2025));
  }

  @Test
  void should_return_S3_for_march() {
    assertEquals(Semester.S3, studentService.computeSemester(Year.of(2024), 3, 2026));
  }

  @Test
  void should_return_S4_for_august() {
    assertEquals(Semester.S4, studentService.computeSemester(Year.of(2024), 8, 2026));
  }

  @Test
  void should_return_S6_for_april() {
    assertEquals(Semester.S6, studentService.computeSemester(Year.of(2024), 4, 2027));
  }

  @Test
  void should_throw_when_date_before_cohort_start() {
    assertThrows(
        BadRequestException.class, () -> studentService.computeSemester(Year.of(2024), 3, 2024));
  }

  @Test
  void should_throw_when_date_in_future() {
    assertThrows(
        BadRequestException.class, () -> studentService.computeSemester(Year.of(2024), 1, 2028));
  }

  @Test
  void should_use_today_when_month_and_year_absent() {
    var cohort = createCohort(2024);
    var group =
        createGroup(
            cohort,
            List.of(
                createCourse(COURSE_S1_ID, "S1C", Semester.S1, List.of(), List.of(), Track.COMMON),
                createCourse(COURSE_S2_ID, "S2C", Semester.S2, List.of(), List.of(), Track.COMMON),
                createCourse(COURSE_S3_ID, "S3C", Semester.S3, List.of(), List.of(), Track.COMMON),
                createCourse(COURSE_S4_ID, "S4C", Semester.S4, List.of(), List.of(), Track.COMMON),
                createCourse(COURSE_S5_ID, "S5C", Semester.S5, List.of(), List.of(), Track.COMMON),
                createCourse(
                    COURSE_S6_ID, "S6C", Semester.S6, List.of(), List.of(), Track.COMMON)));
    var student = createStudent(group);

    var today = LocalDate.now();
    var expectedPair =
        studentService.semesterPair(
            studentService.computeSemester(Year.of(2024), today.getMonthValue(), today.getYear()));

    var dto = getTranscript(student, group, null, null);

    assertEquals(expectedPair, dto.getSemesters());
  }

  @Test
  void should_map_S1_to_S1_S2_pair() {
    assertEquals(List.of(Semester.S1, Semester.S2), studentService.semesterPair(Semester.S1));
  }

  @Test
  void should_map_S2_to_S1_S2_pair() {
    assertEquals(List.of(Semester.S1, Semester.S2), studentService.semesterPair(Semester.S2));
  }

  @Test
  void should_map_S3_to_S3_S4_pair() {
    assertEquals(List.of(Semester.S3, Semester.S4), studentService.semesterPair(Semester.S3));
  }

  @Test
  void should_map_S4_to_S3_S4_pair() {
    assertEquals(List.of(Semester.S3, Semester.S4), studentService.semesterPair(Semester.S4));
  }

  @Test
  void should_map_S5_to_S5_S6_pair() {
    assertEquals(List.of(Semester.S5, Semester.S6), studentService.semesterPair(Semester.S5));
  }

  @Test
  void should_map_S6_to_S5_S6_pair() {
    assertEquals(List.of(Semester.S5, Semester.S6), studentService.semesterPair(Semester.S6));
  }

  @Test
  void should_only_keep_courses_of_pair_semesters() {
    var courses =
        List.of(
            createCourse(COURSE_S4_ID, "LV2", Semester.S4, List.of(), List.of(), Track.COMMON),
            createCourse(COURSE_S3_ID, "PROG4", Semester.S3, List.of(), List.of(), Track.EL),
            createCourse(
                COURSE_S1_ID, "THEORIE1", Semester.S1, List.of(), List.of(), Track.COMMON));

    var result = studentService.filterCourses(courses, List.of(Semester.S3, Semester.S4), Track.EL);

    assertEquals(List.of("PROG4", "LV2"), result.stream().map(Course::getRef).toList());
  }

  @Test
  void should_compute_weighted_sum() {
    var course = createCourse(COURSE_S3_ID, "PROG4", Semester.S3, List.of(), List.of(), Track.EL);
    var exam1 = createExam(EXAM_1_ID, course, new Fraction(1, 2));
    var exam2 = createExam(EXAM_2_ID, course, new Fraction(1, 2));
    var student = createStudent(null);
    var grade1 = createGrade(GRADE_1_ID, student, exam1, new BigDecimal("14"));
    var grade2 = createGrade(GRADE_2_ID, student, exam2, new BigDecimal("12"));
    var fullCourse =
        createCourse(
            COURSE_S3_ID,
            "PROG4",
            Semester.S3,
            List.of(exam1, exam2),
            List.of(grade1, grade2),
            Track.EL);

    var result = studentService.computeFinalGrade(fullCourse, student);

    var expectedFinalGrade = new BigDecimal("13.0");
    assertTrue(result.compareTo(expectedFinalGrade) == 0);
  }

  @Test
  void should_count_missing_grade_as_zero() {
    var course = createCourse(COURSE_S3_ID, "PROG4", Semester.S3, List.of(), List.of(), Track.EL);
    var exam1 = createExam(EXAM_1_ID, course, new Fraction(1, 2));
    var exam2 = createExam(EXAM_2_ID, course, new Fraction(1, 2));
    var student = createStudent(null);
    var grade1 = createGrade(GRADE_1_ID, student, exam1, new BigDecimal("14"));
    var fullCourse =
        createCourse(
            COURSE_S3_ID, "PROG4", Semester.S3, List.of(exam1, exam2), List.of(grade1), Track.EL);

    var result = studentService.computeFinalGrade(fullCourse, student);

    var expectedFinalGrade = new BigDecimal("7.0");
    assertTrue(result.compareTo(expectedFinalGrade) == 0);
  }

  @Test
  void should_ignore_grade_change_reason() {
    var course = createCourse(COURSE_S3_ID, "PROG4", Semester.S3, List.of(), List.of(), Track.EL);
    var exam = createExam(EXAM_1_ID, course, new Fraction(1, 1));
    var student = createStudent(null);
    var grade =
        Grade.builder()
            .id(GRADE_1_ID)
            .student(student)
            .exam(exam)
            .value(new BigDecimal("15"))
            .changeReason("correction")
            .build();
    var fullCourse =
        createCourse(COURSE_S3_ID, "PROG4", Semester.S3, List.of(exam), List.of(grade), Track.EL);

    var result = studentService.computeFinalGrade(fullCourse, student);

    var expectedFinalGrade = new BigDecimal("15.0");
    assertTrue(result.compareTo(expectedFinalGrade) == 0);
  }

  @Test
  void should_be_COMPLET_when_coefficients_sum_to_one() {
    var course = createCourse(COURSE_S3_ID, "PROG4", Semester.S3, List.of(), List.of(), Track.EL);
    var exam1 = createExam(EXAM_1_ID, course, new Fraction(1, 2));
    var exam2 = createExam(EXAM_2_ID, course, new Fraction(1, 2));
    var fullCourse =
        createCourse(
            COURSE_S3_ID, "PROG4", Semester.S3, List.of(exam1, exam2), List.of(), Track.EL);

    var expectedStatus = TranscriptStatus.COMPLET;
    assertEquals(expectedStatus, studentService.computeStatus(fullCourse, Semester.S3));
  }

  @Test
  void should_be_INCOMPLET_when_coefficients_not_sum_to_one() {
    var course = createCourse(COURSE_S3_ID, "PROG4", Semester.S3, List.of(), List.of(), Track.EL);
    var exam1 = createExam(EXAM_1_ID, course, new Fraction(1, 3));
    var exam2 = createExam(EXAM_2_ID, course, new Fraction(1, 3));
    var fullCourse =
        createCourse(
            COURSE_S3_ID, "PROG4", Semester.S3, List.of(exam1, exam2), List.of(), Track.EL);

    var expectedStatus = TranscriptStatus.INCOMPLET;
    assertEquals(expectedStatus, studentService.computeStatus(fullCourse, Semester.S3));
  }

  @Test
  void should_be_INCOMPLET_for_future_semester_course() {
    var course = createCourse(COURSE_S4_ID, "PROG4", Semester.S4, List.of(), List.of(), Track.EL);
    var exam1 = createExam(EXAM_1_ID, course, new Fraction(1, 2));
    var exam2 = createExam(EXAM_2_ID, course, new Fraction(1, 2));
    var fullCourse =
        createCourse(
            COURSE_S4_ID, "PROG4", Semester.S4, List.of(exam1, exam2), List.of(), Track.EL);

    var expectedStatus = TranscriptStatus.INCOMPLET;
    assertEquals(expectedStatus, studentService.computeStatus(fullCourse, Semester.S3));
  }

  @Test
  void should_be_INCOMPLET_for_course_without_exams() {
    var course = createCourse(COURSE_S3_ID, "PROG4", Semester.S3, List.of(), List.of(), Track.EL);
    var student = createStudent(null);

    var expectedStatus = TranscriptStatus.INCOMPLET;
    assertEquals(expectedStatus, studentService.computeStatus(course, Semester.S3));
    assertNull(studentService.computeFinalGrade(course, student));
  }

  @Test
  void should_return_COMPLET_global_when_all_courses_COMPLET() {
    var cohort = createCohort(2024);
    var s1Course =
        createCourse(COURSE_S1_ID, "MATH1", Semester.S1, List.of(), List.of(), Track.COMMON);
    var s1Exam = createExam(EXAM_1_ID, s1Course, new Fraction(1, 1));
    var s2Course =
        createCourse(COURSE_S2_ID, "PROG2", Semester.S2, List.of(), List.of(), Track.COMMON);
    var s2Exam = createExam(EXAM_2_ID, s2Course, new Fraction(1, 1));
    var student = createStudent(null);
    var s1Full =
        createCourse(
            COURSE_S1_ID,
            "MATH1",
            Semester.S1,
            List.of(s1Exam),
            List.of(createGrade(GRADE_1_ID, student, s1Exam, new BigDecimal("15"))),
            Track.COMMON);
    var s2Full =
        createCourse(
            COURSE_S2_ID,
            "PROG2",
            Semester.S2,
            List.of(s2Exam),
            List.of(createGrade(GRADE_2_ID, student, s2Exam, new BigDecimal("16"))),
            Track.COMMON);
    var group = createGroup(cohort, List.of(s1Full, s2Full));
    var fullStudent = createStudent(group);

    var dto = getTranscript(fullStudent, group, 9, 2025);

    assertEquals(TranscriptStatus.COMPLET, dto.getStatus());
    assertEquals(2, dto.getCourses().size());
  }

  @Test
  void should_return_INCOMPLET_global_when_any_course_INCOMPLET() {
    var cohort = createCohort(2024);
    var s1Course =
        createCourse(COURSE_S1_ID, "MATH1", Semester.S1, List.of(), List.of(), Track.COMMON);
    var s1Exam1 = createExam(EXAM_1_ID, s1Course, new Fraction(1, 3));
    var s1Exam2 = createExam(EXAM_2_ID, s1Course, new Fraction(1, 3));
    var s2Course =
        createCourse(COURSE_S2_ID, "PROG2", Semester.S2, List.of(), List.of(), Track.COMMON);
    var s2Exam = createExam(EXAM_2_ID, s2Course, new Fraction(1, 1));
    var student = createStudent(null);
    var s1Full =
        createCourse(
            COURSE_S1_ID, "MATH1", Semester.S1, List.of(s1Exam1, s1Exam2), List.of(), Track.COMMON);
    var s2Full =
        createCourse(
            COURSE_S2_ID,
            "PROG2",
            Semester.S2,
            List.of(s2Exam),
            List.of(createGrade(GRADE_2_ID, student, s2Exam, new BigDecimal("16"))),
            Track.COMMON);
    var group = createGroup(cohort, List.of(s1Full, s2Full));
    var fullStudent = createStudent(group);

    var dto = getTranscript(fullStudent, group, 9, 2025);

    assertEquals(TranscriptStatus.INCOMPLET, dto.getStatus());
  }

  @Test
  void should_return_empty_courses_when_group_has_no_matching_courses() {
    var cohort = createCohort(2024);
    var s5Course = createCourse(COURSE_S5_ID, "ALGO5", Semester.S5, List.of(), List.of(), Track.EL);
    var s6Course =
        createCourse(COURSE_S6_ID, "THESE6", Semester.S6, List.of(), List.of(), Track.COMMON);
    var group = createGroup(cohort, List.of(s5Course, s6Course));
    var student = createStudent(group);

    var dto = getTranscript(student, group, 9, 2025);

    assertEquals(0, dto.getCourses().size());
    assertEquals(TranscriptStatus.COMPLET, dto.getStatus());
  }

  @Test
  void should_build_transcript_dto_with_student_group_cohort_info() {
    var cohort = createCohort(2024);
    var s1Course =
        createCourse(COURSE_S1_ID, "MATH1", Semester.S1, List.of(), List.of(), Track.COMMON);
    var s1Exam = createExam(EXAM_1_ID, s1Course, new Fraction(1, 1));
    var s2Course =
        createCourse(COURSE_S2_ID, "PROG2", Semester.S2, List.of(), List.of(), Track.COMMON);
    var s2Exam = createExam(EXAM_2_ID, s2Course, new Fraction(1, 1));
    var student = createStudent(null);
    var s1Full =
        createCourse(
            COURSE_S1_ID,
            "MATH1",
            Semester.S1,
            List.of(s1Exam),
            List.of(createGrade(GRADE_1_ID, student, s1Exam, new BigDecimal("15"))),
            Track.COMMON);
    var s2Full =
        createCourse(
            COURSE_S2_ID,
            "PROG2",
            Semester.S2,
            List.of(s2Exam),
            List.of(createGrade(GRADE_2_ID, student, s2Exam, new BigDecimal("16"))),
            Track.COMMON);
    var group = createGroup(cohort, List.of(s1Full, s2Full));
    var fullStudent = createStudent(group);

    var dto = getTranscript(fullStudent, group, 9, 2025);

    assertEquals(STUDENT_ID, dto.getStudentId());
    assertEquals("STD26001", dto.getStudentRef());
    assertEquals("Tokyo", dto.getFirstName());
    assertEquals("Watt", dto.getLastName());
    assertEquals("L1-EL-01", dto.getGroupRef());
    assertEquals("P24", dto.getCohortRef());
    assertEquals("2024-2025", dto.getAcademicYear());
    assertEquals(List.of(Semester.S1, Semester.S2), dto.getSemesters());
    assertEquals(2, dto.getCourses().size());
    assertEquals(TranscriptStatus.COMPLET, dto.getStatus());
  }

  @Test
  void should_expose_null_value_for_exam_without_grade() {
    var cohort = createCohort(2024);
    var s2Course =
        createCourse(COURSE_S2_ID, "PROG2", Semester.S2, List.of(), List.of(), Track.COMMON);
    var exam1 = createExam(EXAM_1_ID, s2Course, new Fraction(1, 2));
    var exam2 = createExam(EXAM_2_ID, s2Course, new Fraction(1, 2));
    var student = createStudent(null);
    var s2Full =
        createCourse(
            COURSE_S2_ID,
            "PROG2",
            Semester.S2,
            List.of(exam1, exam2),
            List.of(createGrade(GRADE_1_ID, student, exam1, new BigDecimal("14"))),
            Track.COMMON);
    var group = createGroup(cohort, List.of(s2Full));
    var fullStudent = createStudent(group);

    var dto = getTranscript(fullStudent, group, 9, 2025);

    var courseDto = dto.getCourses().get(0);
    var expectedFinalGrade = new BigDecimal("7.0");
    assertTrue(courseDto.getFinalGrade().compareTo(expectedFinalGrade) == 0);
    var values = courseDto.getExams().stream().map(ExamGradeDto::getValue).toList();
    assertTrue(values.contains(new BigDecimal("14")));
    assertTrue(values.contains(null));
    assertEquals(TranscriptStatus.COMPLET, courseDto.getStatus());
  }

  @Test
  void should_throw_not_found_when_student_unknown() {
    when(studentValidator.checkStudentExists(STUDENT_ID))
        .thenThrow(new NotFoundException("Student " + STUDENT_ID + " not found"));

    assertThrows(NotFoundException.class, () -> studentService.getTranscript(STUDENT_ID, 3, 2026));
  }

  @Test
  void should_throw_not_found_when_group_unknown() {
    var cohort = createCohort(2024);
    var group = createGroup(cohort, List.of());
    var student = createStudent(group);
    when(studentValidator.checkStudentExists(STUDENT_ID)).thenReturn(student);
    when(groupValidator.checkGroupExists(GROUP_ID))
        .thenThrow(new NotFoundException("Group " + GROUP_ID + " not found"));

    assertThrows(NotFoundException.class, () -> studentService.getTranscript(STUDENT_ID, 3, 2026));
  }

  @Test
  void should_throw_when_month_without_year() {
    var cohort = createCohort(2024);
    var group = createGroup(cohort, List.of());
    var student = createStudent(group);
    when(studentValidator.checkStudentExists(STUDENT_ID)).thenReturn(student);
    when(groupValidator.checkGroupExists(GROUP_ID)).thenReturn(student.getGroup());

    assertThrows(
        BadRequestException.class, () -> studentService.getTranscript(STUDENT_ID, 3, null));
  }

  @Test
  void should_throw_when_year_without_month() {
    var cohort = createCohort(2024);
    var group = createGroup(cohort, List.of());
    var student = createStudent(group);
    when(studentValidator.checkStudentExists(STUDENT_ID)).thenReturn(student);
    when(groupValidator.checkGroupExists(GROUP_ID)).thenReturn(student.getGroup());

    assertThrows(
        BadRequestException.class, () -> studentService.getTranscript(STUDENT_ID, null, 2026));
  }
}
