package hei.student.schoolm.validator;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.repository.CourseRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseValidator {
  private final CourseRepository courseRepository;

  public Course checkCourseExists(UUID courseId) {
    return courseRepository
        .findById(courseId)
        .orElseThrow(() -> new NotFoundException("Course " + courseId + " not found"));
  }
}