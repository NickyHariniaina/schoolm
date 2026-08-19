package hei.student.schoolm.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import hei.student.schoolm.dto.CourseAssignmentRequest;
import hei.student.schoolm.exception.BadRequestException;
import hei.student.schoolm.model.Cohort;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.CourseAssignment;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.repository.CourseAssignmentRepository;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseAssignmentValidatorTest {
  @Mock private CourseAssignmentRepository courseAssignmentRepository;
  @InjectMocks private CourseAssignmentValidator validator;

  private static final UUID COURSE_ID = UUID.fromString("00000000-0000-0000-0000-000000001001");
  private static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000001002");
  private static final UUID TEACHER_ID = UUID.fromString("00000000-0000-0000-0000-000000001003");

  private Course course(Semester semester, Track track) {
    return Course.builder()
        .id(COURSE_ID)
        .ref("PROG4")
        .credit(8)
        .track(track)
        .semester(semester)
        .build();
  }

  private Group group(Track track) {
    return Group.builder()
        .id(GROUP_ID)
        .ref("K1")
        .track(track)
        .cohort(Cohort.builder().id(UUID.randomUUID()).ref("K").entryYear(Year.of(2024)).build())
        .build();
  }

  @Test
  void should_accept_course_of_matching_semester_and_track() {
    var course = course(Semester.S3, Track.COMMON);
    var group = group(Track.EL);

    assertDoesNotThrow(() -> validator.validateCurriculum(course, group, Semester.S3));
  }

  @Test
  void should_reject_course_of_wrong_semester() {
    var course = course(Semester.S4, Track.COMMON);
    var group = group(Track.EL);

    assertThrows(
        BadRequestException.class, () -> validator.validateCurriculum(course, group, Semester.S3));
  }

  @Test
  void should_reject_track_specific_course_assigned_to_other_track() {
    var course = course(Semester.S5, Track.TN);
    var group = group(Track.EL);

    assertThrows(
        BadRequestException.class, () -> validator.validateCurriculum(course, group, Semester.S5));
  }

  @Test
  void should_reject_duplicate_assignment_for_new_id() {
    when(courseAssignmentRepository.existsByCourseIdAndGroupIdAndAcademicYearAndSemester(
            COURSE_ID, GROUP_ID, 2025, Semester.S3))
        .thenReturn(true);

    assertThrows(
        BadRequestException.class,
        () -> validator.validateNotDuplicate(null, COURSE_ID, GROUP_ID, 2025, Semester.S3));
  }

  @Test
  void should_allow_duplicate_for_existing_id() {
    when(courseAssignmentRepository.existsByCourseIdAndGroupIdAndAcademicYearAndSemester(
            COURSE_ID, GROUP_ID, 2025, Semester.S3))
        .thenReturn(true);
    var existingId = UUID.fromString("00000000-0000-0000-0000-000000001004");

    assertDoesNotThrow(
        () -> validator.validateNotDuplicate(existingId, COURSE_ID, GROUP_ID, 2025, Semester.S3));
  }

  @Test
  void should_reject_credit_ceiling_exceeded() {
    var existing =
        CourseAssignment.builder()
            .id(UUID.randomUUID())
            .academicYear(2025)
            .semester(Semester.S3)
            .credits(28)
            .build();
    when(courseAssignmentRepository.findByGroupIdAndAcademicYearAndSemester(
            GROUP_ID, 2025, Semester.S3))
        .thenReturn(List.of(existing));
    var request =
        new CourseAssignmentRequest(
            null, COURSE_ID, GROUP_ID, List.of(TEACHER_ID), 2025, Semester.S3, 8);

    assertThrows(
        BadRequestException.class, () -> validator.validateCreditCeilings(List.of(request)));
  }

  @Test
  void should_accept_credit_ceiling_respected() {
    when(courseAssignmentRepository.findByGroupIdAndAcademicYearAndSemester(
            GROUP_ID, 2025, Semester.S3))
        .thenReturn(List.of());
    var request =
        new CourseAssignmentRequest(
            null, COURSE_ID, GROUP_ID, List.of(TEACHER_ID), 2025, Semester.S3, 8);

    assertDoesNotThrow(() -> validator.validateCreditCeilings(List.of(request)));
  }

  @Test
  void should_expose_credits_per_semester() {
    assertEquals(30, validator.creditsPerSemester());
  }
}
