package hei.student.schoolm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.model.User;
import hei.student.schoolm.repository.CourseRepository;
import hei.student.schoolm.repository.TeacherRepository;
import hei.student.schoolm.repository.mapper.JCourseMapper;
import hei.student.schoolm.repository.mapper.JTeacherMapper;
import hei.student.schoolm.repository.model.JCourse;
import hei.student.schoolm.repository.model.JTeacher;
import hei.student.schoolm.validator.CourseTeacherValidator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CourseServiceTest {
  private static final UUID COURSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID TEACHER_TOKY = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID TEACHER_YUME = UUID.fromString("00000000-0000-0000-0000-000000000003");

  private final CourseRepository courseRepository = Mockito.mock(CourseRepository.class);
  private final TeacherRepository teacherRepository = Mockito.mock(TeacherRepository.class);
  private final CourseService courseService =
      new CourseService(
          courseRepository,
          new CourseTeacherValidator(courseRepository, teacherRepository),
          new JCourseMapper(new JTeacherMapper()));

  private JCourse createCourse() {
    return JCourse.builder()
        .id(COURSE_ID)
        .ref("PROG4")
        .title("Exploitation dans le cloud")
        .credit(8)
        .track(Track.EL)
        .semester(Semester.S3)
        .build();
  }

  private JTeacher createTeacher(UUID id) {
    return JTeacher.builder()
        .id(id)
        .email("teacher@hei.school")
        .firstName("Teacher")
        .lastName(id.toString())
        .role(User.Role.TEACHER)
        .build();
  }

  @Test
  void should_throw_not_found_when_course_does_not_exist() {
    var unknownCourse = UUID.fromString("99999999-9999-9999-9999-999999999999");
    when(courseRepository.findById(unknownCourse)).thenReturn(Optional.empty());

    var exception =
        assertThrows(
            NotFoundException.class,
            () -> courseService.assignTeachers(unknownCourse, List.of(TEACHER_TOKY)));

    assertTrue(exception.getMessage().contains(unknownCourse.toString()));
  }

  @Test
  void should_throw_not_found_when_teacher_does_not_exist() {
    var unknownTeacher = UUID.fromString("99999999-9999-9999-9999-999999999998");
    when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(createCourse()));
    when(teacherRepository.findAllById(List.of(TEACHER_TOKY, unknownTeacher)))
        .thenReturn(List.of(createTeacher(TEACHER_TOKY)));

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
    when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
    when(teacherRepository.findAllById(List.of(TEACHER_TOKY, TEACHER_YUME)))
        .thenReturn(List.of(toky, yume));
    when(courseRepository.save(course)).thenReturn(course);

    Course result = courseService.assignTeachers(COURSE_ID, List.of(TEACHER_TOKY, TEACHER_YUME));

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
    when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
    when(teacherRepository.findAllById(List.of(TEACHER_TOKY))).thenReturn(List.of(toky));
    when(courseRepository.save(course)).thenReturn(course);

    Course result = courseService.assignTeachers(COURSE_ID, List.of(TEACHER_TOKY));

    assertEquals(List.of(TEACHER_TOKY), result.getTeachers().stream().map(Teacher::getId).toList());
    assertEquals(List.of(toky), course.getTeachers());
    verify(courseRepository).save(course);
  }
}
