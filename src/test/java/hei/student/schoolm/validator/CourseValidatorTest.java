package hei.student.schoolm.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.repository.CourseRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseValidatorTest {
  private static final UUID COURSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Mock CourseRepository courseRepository;
  @InjectMocks CourseValidator courseValidator;

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

  @Test
  void should_return_course_when_exists() {
    var course = createCourse();
    when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

    var result = courseValidator.checkCourseExists(COURSE_ID);

    assertEquals(course, result);
  }

  @Test
  void should_throw_not_found_when_course_missing() {
    when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.empty());

    var exception =
        assertThrows(NotFoundException.class, () -> courseValidator.checkCourseExists(COURSE_ID));

    assertTrue(exception.getMessage().contains(COURSE_ID.toString()));
  }
}
