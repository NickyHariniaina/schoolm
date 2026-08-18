package hei.student.schoolm.endpoint.mvc.controller;

import hei.student.schoolm.model.Track;
import hei.student.schoolm.service.CohortService;
import hei.student.schoolm.service.GraduateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CohortViewController {
  private final CohortService cohortService;
  private final GraduateService graduateService;

  @GetMapping("/")
  public String cohorts(Model model) {
    model.addAttribute("cohorts", cohortService.getCohorts());
    return "cohorts";
  }

  @GetMapping("/cohorts/{ref}/graduates/download")
  public String downloadGraduates(
      @PathVariable String ref,
      @RequestParam Track track,
      @RequestParam(required = false) Integer month,
      @RequestParam(required = false) Integer year) {
    var file = graduateService.generateGraduateList(ref, track, month, year);
    return "redirect:" + file.getUrl();
  }
}
