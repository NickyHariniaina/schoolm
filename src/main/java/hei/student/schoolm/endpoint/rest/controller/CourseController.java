package hei.student.schoolm.endpoint.rest.controller;

import hei.student.schoolm.dto.CourseDto;
import hei.student.schoolm.dto.TeacherIdsRequest;
import hei.student.schoolm.mapper.CourseMapper;
import hei.student.schoolm.service.CourseService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
  private final CourseService courseService;
  private final CourseMapper courseMapper;

  @PutMapping("/{courseId}/teacher")
  public CourseDto assignTeachers(
      @PathVariable UUID courseId, @Valid @RequestBody TeacherIdsRequest request) {

    var course = courseService.assignTeachers(courseId, request.teacherIds());
    return courseMapper.toDto(course);
  }
}
