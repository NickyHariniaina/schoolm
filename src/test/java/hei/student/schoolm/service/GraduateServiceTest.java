package hei.student.schoolm.service;

import static hei.student.schoolm.utils.GraduateTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.schoolm.exception.BadRequestException;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.file.bucket.BucketComponent;
import hei.student.schoolm.file.xlsx.GraduateXlsxWriter;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.repository.CourseAssignmentRepository;
import hei.student.schoolm.repository.GroupRepository;
import hei.student.schoolm.repository.StudentRepository;
import hei.student.schoolm.validator.CohortValidator;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraduateServiceTest {
  @Mock private CohortValidator cohortValidator;
  @Mock private GroupRepository groupRepository;
  @Mock private StudentRepository studentRepository;
  @Mock private GroupFlowService groupFlowService;
  @Mock private CourseAssignmentRepository courseAssignmentRepository;
  @Mock private GraduateXlsxWriter graduateXlsxWriter;
  @Mock private BucketComponent bucketComponent;

  private GraduateService graduateService;

  @BeforeEach
  void setUp() {
    graduateService =
        new GraduateService(
            cohortValidator,
            groupRepository,
            studentRepository,
            groupFlowService,
            courseAssignmentRepository,
            graduateXlsxWriter,
            bucketComponent);
  }

  private void mockCohort() {
    when(cohortValidator.checkCohortExists("P24")).thenReturn(cohort(2024));
  }

  private List<Semester> semestersUpTo(Semester current) {
    var result = new ArrayList<Semester>();
    for (var s : Semester.values()) {
      if (s.ordinal() <= current.ordinal()) {
        result.add(s);
      }
    }
    return result;
  }

  private void mockStudentCourses(Student student, Group group, Semester currentSemester) {
    when(groupFlowService.studentGroupIds(student.getId())).thenReturn(List.of(group.getId()));
    var courses =
        group.getCourses() == null
            ? List.<Course>of()
            : group.getCourses().stream()
                .filter(c -> c.getSemester().ordinal() <= currentSemester.ordinal())
                .toList();
    when(courseAssignmentRepository.findCurriculumCourses(
            List.of(group.getId()), semestersUpTo(currentSemester)))
        .thenReturn(courses);
  }

  private void mockElGroup(Semester currentSemester) {
    var studentPass = student(STUDENT_PASS_ID, "STD24001", "Alice", "Durand");
    var studentFail = student(STUDENT_FAIL_ID, "STD24002", "Bob", "Martin");
    var prog4 =
        gradedCourse(
            COURSE_S4_ID,
            "PROG4",
            Semester.S4,
            8,
            Map.of(studentPass, new BigDecimal("12"), studentFail, new BigDecimal("12")));
    var projet1 =
        gradedCourse(
            COURSE_S5_ID,
            "PROJET1",
            Semester.S5,
            12,
            Map.of(studentPass, new BigDecimal("14"), studentFail, new BigDecimal("8")));
    var stage = courseWithoutExams(COURSE_S6_ID, "PRO4", Semester.S6, 14);
    var group =
        group(GROUP_EL_ID, "P24-EL", Track.EL, cohort(2024), List.of(prog4, projet1, stage));
    when(groupRepository.findAllByCohortId(COHORT_ID)).thenReturn(List.of(group));
    when(studentRepository.findAllByGroupId(GROUP_EL_ID))
        .thenReturn(List.of(studentPass, studentFail));
    mockStudentCourses(studentPass, group, currentSemester);
    mockStudentCourses(studentFail, group, currentSemester);
  }

  @Test
  void should_rank_graduates_by_average_and_skip_failing_student() throws Exception {
    mockCohort();
    mockElGroup(Semester.S6);
    var file = File.createTempFile("graduate", ".xlsx");
    var url = new URL("https://bucket.s3.amazonaws.com/graduates/P24_EL.xlsx");
    when(graduateXlsxWriter.write(anyList(), eq("Diplomes EL"))).thenReturn(file);
    when(bucketComponent.presign(eq("graduates/P24_EL.xlsx"), any())).thenReturn(url);

    var result = graduateService.generateGraduateList("P24", Track.EL, 4, 2027);

    assertEquals("graduate-list_P24_EL.xlsx", result.getFileName());
    assertEquals(url.toString(), result.getUrl());
    assertNotNull(result.getExpiresAt());
    verify(bucketComponent).upload(eq(file), eq("graduates/P24_EL.xlsx"));

    var expectedAverage = new BigDecimal("13.20");
    var entry = graduateService.computeGraduates("P24", Track.EL, 4, 2027).get(0);
    assertEquals(1, entry.rank());
    assertEquals("STD24001", entry.studentRef());
    assertEquals("Durand", entry.lastName());
    assertEquals("Alice", entry.firstName());
    assertEquals(0, expectedAverage.compareTo(entry.average()));
  }

  @Test
  void should_exclude_courses_from_future_semesters() throws Exception {
    mockCohort();
    mockElGroup(Semester.S4);

    var result = graduateService.computeGraduates("P24", Track.EL, 8, 2026);

    assertEquals(2, result.size());
  }

  @Test
  void should_only_consider_groups_of_requested_track() {
    mockCohort();
    var studentPass = student(STUDENT_PASS_ID, "STD24001", "Alice", "Durand");
    var studentTn = student(STUDENT_TN_ID, "STD24003", "Cedric", "Roux");
    var elCourse =
        gradedCourse(
            COURSE_S4_ID, "PROG4", Semester.S4, 8, Map.of(studentPass, new BigDecimal("12")));
    var tnCourse =
        gradedCourse(COURSE_S5_ID, "TN4", Semester.S5, 6, Map.of(studentTn, new BigDecimal("15")));
    var elGroup = group(GROUP_EL_ID, "P24-EL", Track.EL, cohort(2024), List.of(elCourse));
    var tnGroup = group(GROUP_TN_ID, "P24-TN", Track.TN, cohort(2024), List.of(tnCourse));
    when(groupRepository.findAllByCohortId(COHORT_ID)).thenReturn(List.of(elGroup, tnGroup));
    when(studentRepository.findAllByGroupId(GROUP_EL_ID)).thenReturn(List.of(studentPass));
    mockStudentCourses(studentPass, elGroup, Semester.S4);

    var result = graduateService.computeGraduates("P24", Track.EL, 8, 2026);

    assertEquals(1, result.size());
    assertEquals("STD24001", result.get(0).studentRef());
  }

  @Test
  void should_throw_not_found_when_cohort_does_not_exist() {
    when(cohortValidator.checkCohortExists("UNKNOWN"))
        .thenThrow(new NotFoundException("Cohort UNKNOWN not found"));

    assertThrows(
        NotFoundException.class,
        () -> graduateService.generateGraduateList("UNKNOWN", Track.EL, null, null));
  }

  @Test
  void should_reject_month_out_of_range() {
    mockCohort();

    assertThrows(
        BadRequestException.class,
        () -> graduateService.generateGraduateList("P24", Track.EL, 13, 2027));
  }

  @Test
  void should_reject_month_without_year() {
    mockCohort();

    assertThrows(
        BadRequestException.class,
        () -> graduateService.generateGraduateList("P24", Track.EL, 8, null));
  }
}
