package hei.student.schoolm.validator;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Admin;
import hei.student.schoolm.repository.AdminRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminValidator {
  private final AdminRepository repository;

  public Admin checkAdminExists(UUID adminId) {
    return repository
        .findById(adminId)
        .orElseThrow(() -> new NotFoundException("Admin " + adminId + " not found"));
  }
}
