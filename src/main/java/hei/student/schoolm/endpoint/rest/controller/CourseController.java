package hei.student.schoolm.endpoint.rest.controller;

import hei.student.schoolm.dto.CourseDto;
import hei.student.schoolm.dto.CourseRequest;
import hei.student.schoolm.dto.ExamDto;
import hei.student.schoolm.dto.GroupIdsRequest;
import hei.student.schoolm.dto.TeacherIdsRequest;
import hei.student.schoolm.mapper.CourseMapper;
import hei.student.schoolm.service.CourseService;
import hei.student.schoolm.service.ExamService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
  private final CourseService courseService;
  private final CourseMapper courseMapper;
  private final ExamService examService;

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

  @PutMapping
  public CourseDto upsertCourse(@Valid @RequestBody CourseRequest request) {
    var course = courseService.upsert(request);
    return courseMapper.toDto(course);
  }

  @DeleteMapping("/{courseId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCourse(@PathVariable UUID courseId) {
    courseService.delete(courseId);
  }

  @GetMapping("/{courseId}/exams")
  public List<ExamDto> getCourseExams(@PathVariable UUID courseId) {
    return examService.getExamsByCourseId(courseId);
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
