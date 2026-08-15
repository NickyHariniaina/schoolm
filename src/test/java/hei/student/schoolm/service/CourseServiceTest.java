package hei.student.schoolm.service;

import static hei.student.schoolm.utils.GroupTestUtils.createGroup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.repository.CourseRepository;
import hei.student.schoolm.repository.GroupRepository;
import hei.student.schoolm.repository.TeacherRepository;
import hei.student.schoolm.validator.CourseValidator;
import hei.student.schoolm.validator.GroupValidator;
import hei.student.schoolm.validator.TeacherValidator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
  private static final UUID COURSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID TEACHER_TOKY = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID TEACHER_YUME = UUID.fromString("00000000-0000-0000-0000-000000000003");
  private static final UUID GROUP_L1_EL_01 =
      UUID.fromString("00000000-0000-0000-0000-000000000004");
  private static final UUID GROUP_L1_EL_02 =
      UUID.fromString("00000000-0000-0000-0000-000000000005");

  @Mock CourseRepository courseRepository;
  @Mock TeacherRepository teacherRepository;
  @Mock GroupRepository groupRepository;
  @Mock CourseValidator courseValidator;
  @Mock TeacherValidator teacherValidator;
  @Mock GroupValidator groupValidator;
  @InjectMocks CourseService courseService;

  private Course createCourse() {
    return Course.builder()
        .id(COURSE_ID)
        .ref("PROG4")
        .title("Exploitation dans le cloud")
        .credit(8)
        .track(Track.EL)
        .semester(Semester.S3)
        .build();
  }

  private Teacher createTeacher(UUID id) {
    return Teacher.builder().id(id).build();
  }

  @Test
  void should_throw_not_found_when_course_does_not_exist() {
    var unknownCourse = UUID.fromString("99999999-9999-9999-9999-999999999999");
    when(courseValidator.checkCourseExists(unknownCourse))
        .thenThrow(new NotFoundException("Course " + unknownCourse + " not found"));

    var exception =
        assertThrows(
            NotFoundException.class,
            () -> courseService.assignTeachers(unknownCourse, List.of(TEACHER_TOKY)));

    assertTrue(exception.getMessage().contains(unknownCourse.toString()));
  }

  @Test
  void should_throw_not_found_when_teacher_does_not_exist() {
    var unknownTeacher = UUID.fromString("99999999-9999-9999-9999-999999999998");
    when(courseValidator.checkCourseExists(COURSE_ID)).thenReturn(createCourse());
    when(teacherValidator.checkTeachersExist(List.of(TEACHER_TOKY, unknownTeacher)))
        .thenThrow(new NotFoundException("Teacher " + unknownTeacher + " not found"));

    var exception =
        assertThrows(
            NotFoundException.class,
            () -> courseService.assignTeachers(COURSE_ID, List.of(TEACHER_TOKY, unknownTeacher)));

    assertTrue(exception.getMessage().contains(unknownTeacher.toString()));
  }

  @Test
  void should_assign_teachers_and_return_course() {
    var course = createCourse();
    var toky = createTeacher(TEACHER_TOKY);
    var yume = createTeacher(TEACHER_YUME);
    when(courseValidator.checkCourseExists(COURSE_ID)).thenReturn(course);
    when(teacherValidator.checkTeachersExist(List.of(TEACHER_TOKY, TEACHER_YUME)))
        .thenReturn(List.of(toky, yume));
    when(courseRepository.save(course)).thenReturn(course);

    var result = courseService.assignTeachers(COURSE_ID, List.of(TEACHER_TOKY, TEACHER_YUME));

    assertEquals(COURSE_ID, result.getId());
    assertEquals("PROG4", result.getRef());
    assertEquals("Exploitation dans le cloud", result.getTitle());
    assertEquals(8, result.getCredit());
    assertEquals(Track.EL, result.getTrack());
    assertEquals(Semester.S3, result.getSemester());
    assertEquals(
        List.of(TEACHER_TOKY, TEACHER_YUME),
        result.getTeachers().stream().map(Teacher::getId).toList());

    verify(courseRepository).save(course);
    assertEquals(List.of(toky, yume), course.getTeachers());
  }

  @Test
  void should_keep_teacher_already_assigned() {
    var course = createCourse();
    var toky = createTeacher(TEACHER_TOKY);
    course.setTeachers(List.of(toky));
    when(courseValidator.checkCourseExists(COURSE_ID)).thenReturn(course);
    when(teacherValidator.checkTeachersExist(List.of(TEACHER_TOKY))).thenReturn(List.of(toky));
    when(courseRepository.save(course)).thenReturn(course);

    var result = courseService.assignTeachers(COURSE_ID, List.of(TEACHER_TOKY));

    assertEquals(List.of(TEACHER_TOKY), result.getTeachers().stream().map(Teacher::getId).toList());
    assertEquals(List.of(toky), course.getTeachers());
    verify(courseRepository).save(course);
  }

  @Test
  void should_throw_not_found_when_group_does_not_exist() {
    var unknownGroup = UUID.fromString("99999999-9999-9999-9999-999999999997");
    when(courseValidator.checkCourseExists(COURSE_ID)).thenReturn(createCourse());
    when(groupValidator.checkGroupsExist(List.of(GROUP_L1_EL_01, unknownGroup)))
        .thenThrow(new NotFoundException("Group " + unknownGroup + " not found"));

    var exception =
        assertThrows(
            NotFoundException.class,
            () -> courseService.assignGroups(COURSE_ID, List.of(GROUP_L1_EL_01, unknownGroup)));

    assertTrue(exception.getMessage().contains(unknownGroup.toString()));
  }

  @Test
  void should_assign_groups_and_return_course() {
    var course = createCourse();
    var group1 = createGroup(GROUP_L1_EL_01);
    var group2 = createGroup(GROUP_L1_EL_02);
    when(courseValidator.checkCourseExists(COURSE_ID)).thenReturn(course);
    when(groupValidator.checkGroupsExist(List.of(GROUP_L1_EL_01, GROUP_L1_EL_02)))
        .thenReturn(List.of(group1, group2));
    when(courseRepository.save(course)).thenReturn(course);

    var result = courseService.assignGroups(COURSE_ID, List.of(GROUP_L1_EL_01, GROUP_L1_EL_02));

    assertEquals(COURSE_ID, result.getId());
    assertEquals("PROG4", result.getRef());
    assertEquals(
        List.of(GROUP_L1_EL_01, GROUP_L1_EL_02),
        result.getGroups().stream().map(Group::getId).toList());

    verify(courseRepository).save(course);
    assertEquals(List.of(group1, group2), course.getGroups());
  }

  @Test
  void should_keep_group_already_assigned() {
    var course = createCourse();
    var group1 = createGroup(GROUP_L1_EL_01);
    course.setGroups(List.of(group1));
    when(courseValidator.checkCourseExists(COURSE_ID)).thenReturn(course);
    when(groupValidator.checkGroupsExist(List.of(GROUP_L1_EL_01))).thenReturn(List.of(group1));
    when(courseRepository.save(course)).thenReturn(course);

    var result = courseService.assignGroups(COURSE_ID, List.of(GROUP_L1_EL_01));

    assertEquals(List.of(GROUP_L1_EL_01), result.getGroups().stream().map(Group::getId).toList());
    assertEquals(List.of(group1), course.getGroups());
    verify(courseRepository).save(course);
  }
}
