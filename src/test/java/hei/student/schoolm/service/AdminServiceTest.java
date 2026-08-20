package hei.student.schoolm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.schoolm.dto.AdminRequest;
import hei.student.schoolm.model.Admin;
import hei.student.schoolm.model.User;
import hei.student.schoolm.repository.AdminRepository;
import hei.student.schoolm.util.SecurityUtil;
import hei.student.schoolm.validator.AdminValidator;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
  private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Mock AdminRepository adminRepository;
  @Mock AdminValidator adminValidator;
  @Mock SecurityUtil securityUtil;
  @InjectMocks AdminService adminService;

  private Admin createAdmin() {
    return Admin.builder()
        .id(ADMIN_ID)
        .firstName("Nicky")
        .lastName("Hariniaina")
        .email("admin@hei.school")
        .role(User.Role.ADMIN)
        .build();
  }

  @Test
  void should_get_admin_by_id() {
    when(adminValidator.checkAdminExists(ADMIN_ID)).thenReturn(createAdmin());

    var result = adminService.getById(ADMIN_ID);

    assertEquals(ADMIN_ID, result.id());
    assertEquals("Nicky", result.firstName());
    assertEquals("admin@hei.school", result.email());
    verify(securityUtil).requireSelf(ADMIN_ID);
  }

  @Test
  void should_update_admin() {
    var request =
        AdminRequest.builder()
            .firstName("NewName")
            .lastName("Hariniaina")
            .email("admin@hei.school")
            .build();
    when(adminValidator.checkAdminExists(ADMIN_ID)).thenReturn(createAdmin());
    when(adminRepository.save(any(Admin.class))).thenReturn(createAdmin());

    var result = adminService.update(ADMIN_ID, request);

    assertEquals(ADMIN_ID, result.id());
    verify(adminRepository).save(any(Admin.class));
    verify(securityUtil).requireSelf(ADMIN_ID);
  }
}
