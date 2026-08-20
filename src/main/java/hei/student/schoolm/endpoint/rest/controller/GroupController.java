package hei.student.schoolm.endpoint.rest.controller;

import hei.student.schoolm.dto.GroupRequest;
import hei.student.schoolm.dto.GroupResponse;
import hei.student.schoolm.dto.StudentResponse;
import hei.student.schoolm.service.GroupService;
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
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {
  private final GroupService service;

  @GetMapping
  public List<GroupResponse> getAll(@RequestParam(required = false) UUID cohortId) {
    return service.getAll(cohortId);
  }

  @GetMapping("/{id}")
  public GroupResponse getById(@PathVariable UUID id) {
    return service.getById(id);
  }

  @GetMapping("/{id}/students")
  public List<StudentResponse> getStudents(@PathVariable UUID id) {
    return service.getStudents(id);
  }

  @PutMapping
  public GroupResponse upsert(@Valid @RequestBody GroupRequest request) {
    return service.upsert(request);
  }
}
