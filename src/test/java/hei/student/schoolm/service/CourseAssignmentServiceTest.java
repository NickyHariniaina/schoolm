package hei.student.schoolm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.schoolm.dto.CourseAssignmentRequest;
import hei.student.schoolm.dto.CurriculumStatusResponse;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.mapper.CourseAssignmentMapper;
import hei.student.schoolm.mapper.CourseMapper;
import hei.student.schoolm.model.Cohort;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.CourseAssignment;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.repository.CourseAssignmentRepository;
import hei.student.schoolm.validator.CourseAssignmentValidator;
import hei.student.schoolm.validator.CourseValidator;
import hei.student.schoolm.validator.GroupValidator;
import hei.student.schoolm.validator.TeacherValidator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class CourseAssignmentServiceTest {
  @Mock private CourseAssignmentRepository courseAssignmentRepository;
  @Mock private CourseValidator courseValidator;
  @Mock private GroupValidator groupValidator;
  @Mock private TeacherValidator teacherValidator;
  @Mock private CourseAssignmentMapper courseAssignmentMapper;
  @Mock private CourseMapper courseMapper;
  @Mock private CourseAssignmentValidator validator;
  @InjectMocks private CourseAssignmentService courseAssignmentService;

  private static final UUID COURSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000901");
  private static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000902");
  private static final UUID TEACHER_ID = UUID.fromString("00000000-0000-0000-0000-000000000903");
  private static final UUID ASSIGNMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000904");

  private Course course(Semester semester) {
    return Course.builder()
        .id(COURSE_ID)
        .ref("PROG4")
        .title("Programmation 4")
        .credit(8)
        .track(Track.COMMON)
        .semester(semester)
        .build();
  }

  private Group group() {
    return Group.builder()
        .id(GROUP_ID)
        .ref("K1")
        .track(Track.EL)
        .cohort(Cohort.builder().id(UUID.randomUUID()).ref("K").build())
        .build();
  }

  @Test
  void should_return_filtered_page() {
    var pageable = PageRequest.of(0, 10);
    var assignment =
        CourseAssignment.builder()
            .id(ASSIGNMENT_ID)
            .course(course(Semester.S3))
            .group(group())
            .academicYear(2025)
            .semester(Semester.S3)
            .credits(8)
            .build();
    Page<CourseAssignment> page = new PageImpl<>(List.of(assignment), pageable, 1);
    when(courseAssignmentRepository.findFilterPaged(
            GROUP_ID, TEACHER_ID, COURSE_ID, 2025, pageable))
        .thenReturn(page);

    var result =
        courseAssignmentService.getByFilter(GROUP_ID, TEACHER_ID, COURSE_ID, 2025, pageable);

    assertEquals(1, result.getTotalElements());
    verify(courseAssignmentMapper).toResponse(assignment);
  }

  @Test
  void should_throw_when_assignment_not_found() {
    when(courseAssignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> courseAssignmentService.getById(ASSIGNMENT_ID));
  }

  @Test
  void should_upsert_batch_of_assignments() {
    var request =
        new CourseAssignmentRequest(
            null, COURSE_ID, GROUP_ID, List.of(TEACHER_ID), 2025, Semester.S3, 8);
    var course = course(Semester.S3);
    var group = group();
    when(courseValidator.checkCourseExists(COURSE_ID)).thenReturn(course);
    when(groupValidator.checkGroupExists(GROUP_ID)).thenReturn(group);
    when(teacherValidator.checkTeachersExist(List.of(TEACHER_ID)))
        .thenReturn(
            List.of(
                hei.student.schoolm.model.Teacher.builder()
                    .id(TEACHER_ID)
                    .firstName("Andry")
                    .build()));
    var saved =
        CourseAssignment.builder()
            .id(ASSIGNMENT_ID)
            .course(course)
            .group(group)
            .academicYear(2025)
            .semester(Semester.S3)
            .credits(8)
            .build();
    when(courseAssignmentRepository.save(any(CourseAssignment.class))).thenReturn(saved);

    var result = courseAssignmentService.upsert(List.of(request));

    assertEquals(1, result.size());
    verify(courseAssignmentMapper).toResponse(saved);
  }

  @Test
  void should_throw_when_duplicate_assignment() {
    var request =
        new CourseAssignmentRequest(
            null, COURSE_ID, GROUP_ID, List.of(TEACHER_ID), 2025, Semester.S3, 8);
    when(courseValidator.checkCourseExists(COURSE_ID)).thenReturn(course(Semester.S3));
    when(groupValidator.checkGroupExists(GROUP_ID)).thenReturn(group());
    when(teacherValidator.checkTeachersExist(List.of(TEACHER_ID)))
        .thenReturn(
            List.of(
                hei.student.schoolm.model.Teacher.builder()
                    .id(TEACHER_ID)
                    .firstName("Andry")
                    .build()));
    doThrow(new hei.student.schoolm.exception.BadRequestException("duplicate"))
        .when(validator)
        .validateNotDuplicate(any(), any(), any(), org.mockito.ArgumentMatchers.anyInt(), any());

    assertThrows(
        hei.student.schoolm.exception.BadRequestException.class,
        () -> courseAssignmentService.upsert(List.of(request)));
  }

  @Test
  void should_delete_assignment() {
    var assignment =
        CourseAssignment.builder()
            .id(ASSIGNMENT_ID)
            .course(course(Semester.S3))
            .group(group())
            .academicYear(2025)
            .semester(Semester.S3)
            .credits(8)
            .build();
    when(courseAssignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));

    courseAssignmentService.delete(ASSIGNMENT_ID);

    verify(courseAssignmentRepository).delete(assignment);
  }

  @Test
  void should_report_curriculum_status_complete() {
    var courseS3 = course(Semester.S3);
    var group = group();
    var assignment =
        CourseAssignment.builder()
            .id(ASSIGNMENT_ID)
            .course(courseS3)
            .group(group)
            .academicYear(2025)
            .semester(Semester.S3)
            .credits(30)
            .build();
    when(groupValidator.checkGroupExists(GROUP_ID)).thenReturn(group);
    when(courseAssignmentRepository.findByGroupIdAndAcademicYearAndSemester(
            GROUP_ID, 2025, Semester.S3))
        .thenReturn(List.of(assignment));
    when(validator.creditsPerSemester()).thenReturn(30);
    when(courseValidator.getAllCourses()).thenReturn(List.of(courseS3));

    var result = courseAssignmentService.curriculumStatus(GROUP_ID, 2025, Semester.S3);

    assertTrue(result.complete());
    assertEquals(30, result.assignedCredits());
  }

  @Test
  void should_report_missing_courses_when_curriculum_incomplete() {
    var courseS3 = course(Semester.S3);
    var group = group();
    when(groupValidator.checkGroupExists(GROUP_ID)).thenReturn(group);
    when(courseAssignmentRepository.findByGroupIdAndAcademicYearAndSemester(
            GROUP_ID, 2025, Semester.S3))
        .thenReturn(List.of());
    when(validator.creditsPerSemester()).thenReturn(30);
    when(courseValidator.getAllCourses()).thenReturn(List.of(courseS3));

    CurriculumStatusResponse result =
        courseAssignmentService.curriculumStatus(GROUP_ID, 2025, Semester.S3);

    assertFalse(result.complete());
    assertEquals(0, result.assignedCredits());
    assertEquals(1, result.missingCourses().size());
  }
}
