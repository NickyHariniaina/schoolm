package hei.student.schoolm.endpoint.rest.controller;

import hei.student.schoolm.dto.CourseAssignmentRequest;
import hei.student.schoolm.dto.CourseAssignmentResponse;
import hei.student.schoolm.dto.CurriculumStatusResponse;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.service.CourseAssignmentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/course-assignments")
public class CourseAssignmentController {
  private final CourseAssignmentService service;

  @GetMapping
  public Page<CourseAssignmentResponse> getByFilter(
      @RequestParam(required = false) UUID groupId,
      @RequestParam(required = false) UUID teacherId,
      @RequestParam(required = false) UUID courseId,
      @RequestParam(required = false) Integer academicYear,
      Pageable pageable) {
    return service.getByFilter(groupId, teacherId, courseId, academicYear, pageable);
  }

  @GetMapping("/{id}")
  public CourseAssignmentResponse getById(@PathVariable UUID id) {
    return service.getById(id);
  }

  @GetMapping("/curriculum-status")
  public CurriculumStatusResponse getCurriculumStatus(
      @RequestParam UUID groupId, @RequestParam int academicYear, @RequestParam Semester semester) {
    return service.getCurriculumStatus(groupId, academicYear, semester);
  }

  @PutMapping
  public List<CourseAssignmentResponse> upsert(
      @RequestBody @Valid List<@Valid CourseAssignmentRequest> requests) {
    return service.upsert(requests);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }
}
