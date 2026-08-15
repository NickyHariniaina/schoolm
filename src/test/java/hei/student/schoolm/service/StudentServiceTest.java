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

import hei.student.schoolm.dto.SemesterValidationDto;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.mapper.StudentMapper;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.validator.GroupValidator;
import hei.student.schoolm.validator.StudentValidator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
  @Mock StudentValidator studentValidator;
  @Mock GroupValidator groupValidator;
  @Mock StudentMapper studentMapper;
  @InjectMocks StudentService studentService;

  private final SemesterValidationDto anyDto = SemesterValidationDto.builder().build();

  @Test
  void should_only_keep_courses_of_requested_semester() {
    var student = createStudent();
    var courseS3 = createCourse(COURSE_S3_1_ID, "PROG4", Semester.S3, 8, List.of(), List.of());
    var courseS4 = createCourse(COURSE_S4_ID, "PROG5", Semester.S4, 8, List.of(), List.of());
    var group = createGroup(List.of(courseS3, courseS4));
    when(studentValidator.checkStudentExists(STUDENT_ID)).thenReturn(student);
    when(groupValidator.checkGroupExists(GROUP_ID)).thenReturn(group);
    when(studentMapper.toSemesterValidationDto(any(), any(), any(), any())).thenReturn(anyDto);

    studentService.getStudentSemesterValidation(STUDENT_ID, Semester.S3);

    verify(studentMapper)
        .toSemesterValidationDto(eq(student), eq(group), eq(Semester.S3), eq(List.of(courseS3)));
  }

  @Test
  void should_sort_courses_by_ref() {
    var student = createStudent();
    var courseWeb1 = createCourse(COURSE_S3_1_ID, "WEB1", Semester.S3, 7, List.of(), List.of());
    var courseProg4 = createCourse(COURSE_S3_2_ID, "PROG4", Semester.S3, 8, List.of(), List.of());
    var group = createGroup(List.of(courseWeb1, courseProg4));
    when(studentValidator.checkStudentExists(STUDENT_ID)).thenReturn(student);
    when(groupValidator.checkGroupExists(GROUP_ID)).thenReturn(group);
    when(studentMapper.toSemesterValidationDto(any(), any(), any(), any())).thenReturn(anyDto);

    studentService.getStudentSemesterValidation(STUDENT_ID, Semester.S3);

    verify(studentMapper)
        .toSemesterValidationDto(
            eq(student), eq(group), eq(Semester.S3), eq(List.of(courseProg4, courseWeb1)));
  }

  @Test
  void should_pass_empty_courses_when_group_has_no_courses() {
    var student = createStudent();
    var group = createGroup(null);
    when(studentValidator.checkStudentExists(STUDENT_ID)).thenReturn(student);
    when(groupValidator.checkGroupExists(GROUP_ID)).thenReturn(group);
    when(studentMapper.toSemesterValidationDto(any(), any(), any(), any())).thenReturn(anyDto);

    var result = studentService.getStudentSemesterValidation(STUDENT_ID, Semester.S3);

    verify(studentMapper)
        .toSemesterValidationDto(eq(student), eq(group), eq(Semester.S3), eq(List.of()));
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
  void should_throw_not_found_when_group_missing() {
    var student = createStudent();
    when(studentValidator.checkStudentExists(STUDENT_ID)).thenReturn(student);
    when(groupValidator.checkGroupExists(GROUP_ID))
        .thenThrow(new NotFoundException("Group " + GROUP_ID + " not found"));

    var exception =
        assertThrows(
            NotFoundException.class,
            () -> studentService.getStudentSemesterValidation(STUDENT_ID, Semester.S3));

    assertTrue(exception.getMessage().contains(GROUP_ID.toString()));
  }

  @Test
  void should_return_student_group() {
    var group = Group.builder().id(GROUP_ID).ref("L1-EL-01").build();
    var student = Student.builder().id(STUDENT_ID).reference("S-001").group(group).build();
    when(studentValidator.checkStudentExists(STUDENT_ID)).thenReturn(student);

    var result = studentService.getGroup(STUDENT_ID);

    assertEquals(group, result);
  }
}