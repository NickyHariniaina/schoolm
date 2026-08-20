package hei.student.schoolm.endpoint.rest.controller;

import hei.student.schoolm.dto.TeacherRequest;
import hei.student.schoolm.dto.TeacherResponse;
import hei.student.schoolm.service.TeacherService;
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
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {
  private final TeacherService service;

  @GetMapping
  public List<TeacherResponse> getAll() {
    return service.getAll();
  }

  @GetMapping("/{id}")
  public TeacherResponse getById(@PathVariable UUID id) {
    return service.getById(id);
  }

  @PutMapping
  public TeacherResponse upsert(@Valid @RequestBody TeacherRequest request) {
    return service.upsert(request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }
}
