package hei.student.schoolm.endpoint.rest.controller;

import hei.student.schoolm.dto.GroupFlowDto;
import hei.student.schoolm.dto.MoveStudentGroupRequest;
import hei.student.schoolm.dto.SemesterValidationDto;
import hei.student.schoolm.dto.TranscriptDto;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.service.GroupFlowService;
import hei.student.schoolm.service.StudentService;
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
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {
  private final StudentService studentService;
  private final GroupFlowService groupFlowService;

  @GetMapping("/{id}/semester-validation")
  public SemesterValidationDto getStudentSemesterValidation(
      @PathVariable UUID id, @RequestParam Semester semester) {
    return studentService.getStudentSemesterValidation(id, semester);
  }

  @GetMapping("/{id}/graduate-transcript")
  public TranscriptDto getGraduateTranscript(
      @PathVariable UUID id,
      @RequestParam(required = false) Integer month,
      @RequestParam(required = false) Integer year) {
    return studentService.getTranscript(id, month, year);
  }

  @GetMapping("/{id}/group-flows")
  public List<GroupFlowDto> getGroupFlows(@PathVariable UUID id) {
    return groupFlowService.getHistory(id);
  }

  @PutMapping("/{id}/group-flows")
  public GroupFlowDto moveStudentToGroup(
      @PathVariable UUID id, @Valid @RequestBody MoveStudentGroupRequest request) {
    return groupFlowService.move(id, request);
  }
}
