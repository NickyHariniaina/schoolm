package hei.student.schoolm.service;

import hei.student.schoolm.dto.AdminRequest;
import hei.student.schoolm.dto.AdminResponse;
import hei.student.schoolm.model.Admin;
import hei.student.schoolm.repository.AdminRepository;
import hei.student.schoolm.util.SecurityUtil;
import hei.student.schoolm.validator.AdminValidator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {
  private final AdminRepository repository;
  private final AdminValidator validator;
  private final SecurityUtil securityUtil;

  @Transactional(readOnly = true)
  public AdminResponse getById(UUID adminId) {
    securityUtil.requireSelf(adminId);
    return toResponse(validator.checkAdminExists(adminId));
  }

  @Transactional
  public AdminResponse update(UUID adminId, AdminRequest request) {
    securityUtil.requireSelf(adminId);
    var admin = validator.checkAdminExists(adminId);
    admin.setFirstName(request.firstName());
    admin.setLastName(request.lastName());
    admin.setEmail(request.email());
    return toResponse(repository.save(admin));
  }

  private AdminResponse toResponse(Admin admin) {
    return new AdminResponse(
        admin.getId(), admin.getFirstName(), admin.getLastName(), admin.getEmail());
  }
}
