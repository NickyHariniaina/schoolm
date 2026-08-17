package hei.student.schoolm.endpoint.rest.controller;

import hei.student.schoolm.dto.GradeDto;
import hei.student.schoolm.dto.UpdateGradeRequest;
import hei.student.schoolm.service.GradeService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/grades")
public class GradeController {
  private final GradeService gradeService;

  @PutMapping("/{gradeId}")
  public GradeDto updateGrade(
      @PathVariable UUID gradeId, @Valid @RequestBody UpdateGradeRequest request) {
    return gradeService.updateGrade(gradeId, request);
  }
}
