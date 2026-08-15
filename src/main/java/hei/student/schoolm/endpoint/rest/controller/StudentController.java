package hei.student.schoolm.endpoint.rest.controller;

import hei.student.schoolm.dto.SemesterValidationDto;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.service.StudentService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {
  private final StudentService studentService;

  @GetMapping("/{id}/semester-validation")
  public SemesterValidationDto getStudentSemesterValidation(
      @PathVariable UUID id, @RequestParam Semester semester) {
    return studentService.getStudentSemesterValidation(id, semester);
  }
}