package hei.student.schoolm.endpoint.rest.controller;

import hei.student.schoolm.dto.CourseDto;
import hei.student.schoolm.dto.GroupIdsRequest;
import hei.student.schoolm.dto.TeacherIdsRequest;
import hei.student.schoolm.mapper.CourseMapper;
import hei.student.schoolm.service.CourseService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
  private final CourseService courseService;
  private final CourseMapper courseMapper;

  @GetMapping
  public List<CourseDto> getAllCourses() {
    var courses = courseService.getAllCourses();
    return courseMapper.toDtoList(courses);
  }

  @GetMapping("/{courseId}")
  public CourseDto getCourseById(@PathVariable UUID courseId) {
    var course = courseService.getCourseById(courseId);
    return courseMapper.toDto(course);
  }

  @PutMapping("/{courseId}/teacher")
  public CourseDto assignTeachers(
      @PathVariable UUID courseId, @Valid @RequestBody TeacherIdsRequest request) {

    var course = courseService.assignTeachers(courseId, request.teacherIds());
    return courseMapper.toDto(course);
  }

  @PutMapping("/{courseId}/group")
  public CourseDto assignGroups(
      @PathVariable UUID courseId, @Valid @RequestBody GroupIdsRequest request) {

    var course = courseService.assignGroups(courseId, request.groupIds());
    return courseMapper.toDto(course);
  }
}
