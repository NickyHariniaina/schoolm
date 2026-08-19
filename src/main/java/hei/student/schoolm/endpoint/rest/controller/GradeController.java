package hei.student.schoolm.endpoint.rest.controller;

import hei.student.schoolm.dto.GradeDto;
import hei.student.schoolm.dto.GradeHistoryDto;
import hei.student.schoolm.dto.UpdateGradeRequest;
import hei.student.schoolm.service.GradeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/grades")
@RequiredArgsConstructor
public class GradeController {
  private final GradeService gradeService;

  @GetMapping("/{gradeId}")
  public GradeDto getGradeById(@PathVariable UUID gradeId) {
    return gradeService.getGradeById(gradeId);
  }

  @PutMapping("/{gradeId}")
  public GradeDto updateGrade(
      @PathVariable UUID gradeId, @Valid @RequestBody UpdateGradeRequest request) {
    return gradeService.updateGrade(gradeId, request);
  }

  @DeleteMapping("/{gradeId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteGrade(@PathVariable UUID gradeId) {
    gradeService.delete(gradeId);
  }

  @GetMapping("/{gradeId}/history")
  public List<GradeHistoryDto> getGradeHistory(@PathVariable UUID gradeId) {
    return gradeService.getGradeHistory(gradeId);
  }
}
