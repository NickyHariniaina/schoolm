package hei.student.schoolm.endpoint.rest.controller;

import hei.student.schoolm.dto.CohortDto;
import hei.student.schoolm.dto.CohortRequest;
import hei.student.schoolm.dto.GraduateFileDto;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.service.CohortService;
import hei.student.schoolm.service.GraduateService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cohorts")
@RequiredArgsConstructor
public class CohortController {
  private final CohortService cohortService;
  private final GraduateService graduateService;

  @GetMapping
  public List<CohortDto> getCohorts() {
    return cohortService.getCohorts();
  }

  @GetMapping("/{id}")
  public CohortDto getById(@PathVariable UUID id) {
    return cohortService.getById(id);
  }

  @PutMapping
  public CohortDto upsert(@Valid @RequestBody CohortRequest request) {
    return cohortService.upsert(request);
  }

  @GetMapping("/{ref}/graduates")
  public GraduateFileDto getGraduates(
      @PathVariable String ref,
      @RequestParam Track track,
      @RequestParam(required = false) Integer month,
      @RequestParam(required = false) Integer year) {
    return graduateService.generateGraduateList(ref, track, month, year);
  }
}
