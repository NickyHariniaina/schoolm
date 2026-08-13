package hei.student.schoolm.endpoint.rest.controller;

import hei.student.schoolm.dto.CourseDto;
import hei.student.schoolm.dto.TeacherIdsRequest;
import hei.student.schoolm.service.CourseService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
  private final CourseService courseService;

  @PutMapping("/{courseId}/teacher")
  public ResponseEntity<CourseDto> assignTeachers(
      @PathVariable UUID courseId, @Valid @RequestBody TeacherIdsRequest request) {

    CourseDto course = courseService.assignTeachers(courseId, request.getTeacherIds());
    return ResponseEntity.ok(course);
  }
}
