package hei.student.schoolm.endpoint.rest.controller;

import hei.student.schoolm.dto.AdminRequest;
import hei.student.schoolm.dto.AdminResponse;
import hei.student.schoolm.service.AdminService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
public class AdminController {
  private final AdminService adminService;

  @GetMapping("/{id}")
  public AdminResponse getById(@PathVariable UUID id) {
    return adminService.getById(id);
  }

  @PutMapping("/{id}")
  public AdminResponse update(@PathVariable UUID id, @Valid @RequestBody AdminRequest request) {
    return adminService.update(id, request);
  }
}
