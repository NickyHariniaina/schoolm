package hei.student.schoolm.endpoint.rest.controller;

import hei.student.schoolm.dto.ExamDto;
import hei.student.schoolm.dto.ExamRequest;
import hei.student.schoolm.dto.GradeDto;
import hei.student.schoolm.dto.GradeRequest;
import hei.student.schoolm.service.ExamService;
import hei.student.schoolm.service.GradeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {
  private final ExamService examService;
  private final GradeService gradeService;

  @GetMapping("/{id}")
  public ExamDto getById(@PathVariable UUID id) {
    return examService.getById(id);
  }

  @PutMapping
  public ExamDto upsert(@Valid @RequestBody ExamRequest request) {
    return examService.upsert(request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    examService.delete(id);
  }

  @GetMapping("/{examId}/grades")
  public List<GradeDto> getGrades(@PathVariable UUID examId) {
    return gradeService.getGradesByExamId(examId);
  }

  @PutMapping("/{examId}/grades")
  public List<GradeDto> upsertGrades(
      @PathVariable UUID examId, @Valid @RequestBody List<GradeRequest> requests) {
    return gradeService.upsertGrades(examId, requests);
  }
}
