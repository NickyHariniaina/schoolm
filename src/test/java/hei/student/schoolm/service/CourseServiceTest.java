package hei.student.schoolm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.schoolm.endpoint.rest.CourseResponse;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.repository.JCourseRepository;
import hei.student.schoolm.repository.JTeacherRepository;
import hei.student.schoolm.repository.model.JCourse;
import hei.student.schoolm.repository.model.JTeacher;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {
  @Mock JCourseRepository jCourseRepository;
  @Mock JTeacherRepository jTeacherRepository;
  @InjectMocks CourseService courseService;

  private static final String COURSE_ID = "course-prog4";
  private static final String TEACHER_TOKY = "teacher-toky";
  private static final String TEACHER_YUME = "teacher-yume";

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

  private JTeacher createTeacher(String id) {
    return JTeacher.builder().id(id).build();
  }

  @Test
  void should_throw_not_found_when_course_does_not_exist() {
    var unknownCourse = "course-unknown";
    when(jCourseRepository.findById(unknownCourse)).thenReturn(Optional.empty());

    var exception =
        assertThrows(
            NotFoundException.class,
            () -> courseService.assignTeachers(unknownCourse, List.of(TEACHER_TOKY)));

    assertTrue(exception.getMessage().contains(unknownCourse));
  }

  @Test
  void should_throw_not_found_when_teacher_does_not_exist() {
    var unknownTeacher = "teacher-unknown";
    when(jCourseRepository.findById(COURSE_ID)).thenReturn(Optional.of(createCourse()));
    when(jTeacherRepository.findAllById(List.of(TEACHER_TOKY, unknownTeacher)))
        .thenReturn(List.of(createTeacher(TEACHER_TOKY)));

    var exception =
        assertThrows(
            NotFoundException.class,
            () -> courseService.assignTeachers(COURSE_ID, List.of(TEACHER_TOKY, unknownTeacher)));

    assertTrue(exception.getMessage().contains(unknownTeacher));
  }

  @Test
  void should_assign_teachers_and_return_course() {
    var course = createCourse();
    var toky = createTeacher(TEACHER_TOKY);
    var yume = createTeacher(TEACHER_YUME);
    when(jCourseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
    when(jTeacherRepository.findAllById(List.of(TEACHER_TOKY, TEACHER_YUME)))
        .thenReturn(List.of(toky, yume));

    CourseResponse response =
        courseService.assignTeachers(COURSE_ID, List.of(TEACHER_TOKY, TEACHER_YUME));

    // TODO: Refactor this later.
    assertEquals(COURSE_ID, response.id());
    assertEquals("PROG4", response.ref());
    assertEquals("Exploitation dans le cloud", response.title());
    assertEquals(8, response.credit());
    assertEquals(Track.EL, response.track());
    assertEquals(Semester.S3, response.semester());
    assertEquals(List.of(TEACHER_TOKY, TEACHER_YUME), response.teacherIds());

    verify(jCourseRepository).save(course);
    assertEquals(List.of(toky, yume), course.getTeachers());
  }

  @Test
  void should_keep_teacher_already_assigned() {
    var course = createCourse();
    var toky = createTeacher(TEACHER_TOKY);
    course.setTeachers(List.of(toky));
    when(jCourseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
    when(jTeacherRepository.findAllById(List.of(TEACHER_TOKY))).thenReturn(List.of(toky));

    CourseResponse response = courseService.assignTeachers(COURSE_ID, List.of(TEACHER_TOKY));

    assertEquals(List.of(TEACHER_TOKY), response.teacherIds());
    assertEquals(List.of(toky), course.getTeachers());
    verify(jCourseRepository).save(course);
  }
}
