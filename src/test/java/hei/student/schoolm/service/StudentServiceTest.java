package hei.student.schoolm.service;

import static hei.student.schoolm.utils.SemesterValidationTestUtils.COURSE_S3_1_ID;
import static hei.student.schoolm.utils.SemesterValidationTestUtils.COURSE_S3_2_ID;
import static hei.student.schoolm.utils.SemesterValidationTestUtils.COURSE_S4_ID;
import static hei.student.schoolm.utils.SemesterValidationTestUtils.GROUP_ID;
import static hei.student.schoolm.utils.SemesterValidationTestUtils.STUDENT_ID;
import static hei.student.schoolm.utils.SemesterValidationTestUtils.createCourse;
import static hei.student.schoolm.utils.SemesterValidationTestUtils.createGroup;
import static hei.student.schoolm.utils.SemesterValidationTestUtils.createStudent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.schoolm.dto.LevelRequest;
import hei.student.schoolm.dto.SemesterValidationDto;
import hei.student.schoolm.dto.TranscriptDto;
import hei.student.schoolm.exception.BadRequestException;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.mapper.StudentMapper;
import hei.student.schoolm.model.*;
import hei.student.schoolm.repository.CourseAssignmentRepository;
import hei.student.schoolm.util.SecurityUtil;
import hei.student.schoolm.validator.GroupValidator;
import hei.student.schoolm.validator.StudentValidator;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
  @Mock StudentValidator studentValidator;
  @Mock GroupValidator groupValidator;
  @Mock GroupFlowService groupFlowService;
  @Mock CourseAssignmentRepository courseAssignmentRepository;
  @Mock StudentMapper studentMapper;
  @Mock SecurityUtil securityUtil;
  @InjectMocks StudentService studentService;

  private final SemesterValidationDto anyDto = SemesterValidationDto.builder().build();

  @BeforeEach
  void setUp() {
    org.mockito.Mockito.lenient().when(securityUtil.isAdmin()).thenReturn(true);
  }

  @Test
  void should_only_keep_courses_of_requested_semester() {
    var student = createStudent();
    var courseS3 = createCourse(COURSE_S3_1_ID, "PROG4", Semester.S3, 8, List.of(), List.of());
    var courseS4 = createCourse(COURSE_S4_ID, "PROG5", Semester.S4, 8, List.of(), List.of());
    when(studentValidator.checkStudentExists(STUDENT_ID)).thenReturn(student);
    when(groupFlowService.studentGroupIds(STUDENT_ID)).thenReturn(List.of(GROUP_ID));
    when(courseAssignmentRepository.findCurriculumCourses(List.of(GROUP_ID), List.of(Semester.S3)))
        .thenReturn(List.of(courseS3));
    when(studentMapper.toSemesterValidationDto(any(), any(), any(), any())).thenReturn(anyDto);

    studentService.getStudentSemesterValidation(STUDENT_ID, Semester.S3);

    verify(studentMapper)
        .toSemesterValidationDto(
            eq(student), eq(student.getGroup()), eq(Semester.S3), eq(List.of(courseS3)));
  }

  @Test
  void should_sort_courses_by_ref() {
    var student = createStudent();
    var courseWeb1 = createCourse(COURSE_S3_1_ID, "WEB1", Semester.S3, 7, List.of(), List.of());
    var courseProg4 = createCourse(COURSE_S3_2_ID, "PROG4", Semester.S3, 8, List.of(), List.of());
    when(studentValidator.checkStudentExists(STUDENT_ID)).thenReturn(student);
    when(groupFlowService.studentGroupIds(STUDENT_ID)).thenReturn(List.of(GROUP_ID));
    when(courseAssignmentRepository.findCurriculumCourses(List.of(GROUP_ID), List.of(Semester.S3)))
        .thenReturn(List.of(courseProg4, courseWeb1));
    when(studentMapper.toSemesterValidationDto(any(), any(), any(), any())).thenReturn(anyDto);

    studentService.getStudentSemesterValidation(STUDENT_ID, Semester.S3);

    verify(studentMapper)
        .toSemesterValidationDto(
            eq(student),
            eq(student.getGroup()),
            eq(Semester.S3),
            eq(List.of(courseProg4, courseWeb1)));
  }

  @Test
  void should_pass_empty_courses_when_no_assignments() {
    var student = createStudent();
    when(studentValidator.checkStudentExists(STUDENT_ID)).thenReturn(student);
    when(groupFlowService.studentGroupIds(STUDENT_ID)).thenReturn(List.of(GROUP_ID));
    when(courseAssignmentRepository.findCurriculumCourses(List.of(GROUP_ID), List.of(Semester.S3)))
        .thenReturn(List.of());
    when(studentMapper.toSemesterValidationDto(any(), any(), any(), any())).thenReturn(anyDto);

    var result = studentService.getStudentSemesterValidation(STUDENT_ID, Semester.S3);

    verify(studentMapper)
        .toSemesterValidationDto(
            eq(student), eq(student.getGroup()), eq(Semester.S3), eq(List.of()));
    assertEquals(anyDto, result);
  }

  @Test
  void should_throw_not_found_when_student_missing() {
    when(studentValidator.checkStudentExists(STUDENT_ID))
        .thenThrow(new NotFoundException("Student " + STUDENT_ID + " not found"));

    var exception =
        assertThrows(
            NotFoundException.class,
            () -> studentService.getStudentSemesterValidation(STUDENT_ID, Semester.S3));

    assertTrue(exception.getMessage().contains(STUDENT_ID.toString()));
  }

  @Test
  void should_return_student_group() {
    var group = Group.builder().id(GROUP_ID).ref("L1-EL-01").build();
    var student = Student.builder().id(STUDENT_ID).reference("S-001").group(group).build();
    when(studentValidator.checkStudentExists(STUDENT_ID)).thenReturn(student);

    var result = studentService.getGroup(STUDENT_ID);

    assertEquals(group, result);
  }

  @Test
  void should_get_transcript_for_semester() {
    var cohort = Cohort.builder().id(UUID.randomUUID()).ref("K").entryYear(Year.of(2023)).build();

    var student = createStudent();
    var group = createGroup(List.of());
    group.setCohort(cohort);
    student.setGroup(group);

    var course = createCourse(COURSE_S3_1_ID, "PROG1", Semester.S1, 6, List.of(), List.of());
    var expectedTranscript =
        TranscriptDto.builder()
            .studentId(STUDENT_ID)
            .studentRef("STD26001")
            .firstName("Tokyo")
            .lastName("Watt")
            .build();

    when(studentValidator.checkStudentExists(STUDENT_ID)).thenReturn(student);
    when(groupValidator.checkGroupExists(GROUP_ID)).thenReturn(group);
    when(groupFlowService.studentGroupIds(STUDENT_ID)).thenReturn(List.of(GROUP_ID));
    when(courseAssignmentRepository.findCurriculumCourses(
            List.of(GROUP_ID), List.of(Semester.S1, Semester.S2)))
        .thenReturn(List.of(course));
    when(studentMapper.toTranscriptDto(any(), any(), any(), any())).thenReturn(expectedTranscript);

    var result = studentService.getTranscriptForLevel(STUDENT_ID, LevelRequest.L1);

    assertEquals(expectedTranscript, result);
    verify(studentMapper).toTranscriptDto(any(), any(), any(), any());
  }

  @Test
  void should_filter_courses_by_track_and_semester_for_el() {
    var commonCourse =
        Course.builder()
            .id(COURSE_S3_1_ID)
            .ref("COMMON_COURSE")
            .semester(Semester.S1)
            .credit(6)
            .track(Track.COMMON)
            .build();

    var elCourse =
        Course.builder()
            .id(COURSE_S3_2_ID)
            .ref("EL_COURSE")
            .semester(Semester.S1)
            .credit(6)
            .track(Track.EL)
            .build();

    var tnCourse =
        Course.builder()
            .id(COURSE_S4_ID)
            .ref("TN_COURSE")
            .semester(Semester.S1)
            .credit(6)
            .track(Track.TN)
            .build();

    var courses = List.of(commonCourse, elCourse, tnCourse);
    var pair = List.of(Semester.S1, Semester.S2);

    var result = studentService.filterCourses(courses, pair, Track.EL);

    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(c -> c.getRef().equals("COMMON_COURSE")));
    assertTrue(result.stream().anyMatch(c -> c.getRef().equals("EL_COURSE")));
    assertTrue(result.stream().noneMatch(c -> c.getRef().equals("TN_COURSE")));
  }

  @Test
  void should_filter_courses_by_track_and_semester_for_tn() {
    var commonCourse =
        Course.builder()
            .id(COURSE_S3_1_ID)
            .ref("COMMON_COURSE")
            .semester(Semester.S1)
            .credit(6)
            .track(Track.COMMON)
            .build();

    var elCourse =
        Course.builder()
            .id(COURSE_S3_2_ID)
            .ref("EL_COURSE")
            .semester(Semester.S1)
            .credit(6)
            .track(Track.EL)
            .build();

    var tnCourse =
        Course.builder()
            .id(COURSE_S4_ID)
            .ref("TN_COURSE")
            .semester(Semester.S1)
            .credit(6)
            .track(Track.TN)
            .build();

    var courses = List.of(commonCourse, elCourse, tnCourse);
    var pair = List.of(Semester.S1, Semester.S2);

    var result = studentService.filterCourses(courses, pair, Track.TN);

    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(c -> c.getRef().equals("COMMON_COURSE")));
    assertTrue(result.stream().anyMatch(c -> c.getRef().equals("TN_COURSE")));
    assertTrue(result.stream().noneMatch(c -> c.getRef().equals("EL_COURSE")));
  }

  @Test
  void should_return_empty_when_courses_is_null() {
    var result = studentService.filterCourses(null, List.of(Semester.S1), Track.EL);
    assertTrue(result.isEmpty());
  }

  @Test
  void should_return_empty_when_no_courses_match_semester() {
    var course = createCourse(COURSE_S3_1_ID, "PROG1", Semester.S1, 6, List.of(), List.of());
    var courses = List.of(course);
    var pair = List.of(Semester.S3, Semester.S4);

    var result = studentService.filterCourses(courses, pair, Track.EL);
    assertTrue(result.isEmpty());
  }

  @Test
  void should_sort_courses_by_semester_and_ref() {
    var courseS3 = createCourse(COURSE_S3_1_ID, "PROG4", Semester.S3, 8, List.of(), List.of());
    var courseS1 = createCourse(COURSE_S3_2_ID, "WEB1", Semester.S1, 7, List.of(), List.of());
    var courseS4 = createCourse(COURSE_S4_ID, "ALGO1", Semester.S4, 8, List.of(), List.of());

    var courses = List.of(courseS3, courseS1, courseS4);
    var pair = List.of(Semester.S3, Semester.S4);

    var result = studentService.filterCourses(courses, pair, Track.EL);

    assertEquals(2, result.size());
    assertEquals("PROG4", result.get(0).getRef());
    assertEquals("ALGO1", result.get(1).getRef());
  }

  @Test
  void should_throw_bad_request_when_month_invalid() {
    assertThrows(
        BadRequestException.class, () -> studentService.getTranscript(STUDENT_ID, 13, 2025));
  }

  @Test
  void should_not_throw_when_month_valid() {
    var student = createStudent();
    var group = createGroup(List.of());
    var cohort = Cohort.builder().id(UUID.randomUUID()).ref("K").entryYear(Year.of(2024)).build();
    group.setCohort(cohort);
    student.setGroup(group);

    when(studentValidator.checkStudentExists(STUDENT_ID)).thenReturn(student);
    when(studentMapper.toTranscriptDto(any(), any(), any(), any()))
        .thenReturn(TranscriptDto.builder().build());

    studentService.getTranscript(STUDENT_ID, 10, 2024);
    verify(studentMapper).toTranscriptDto(any(), any(), any(), any());
  }
}
