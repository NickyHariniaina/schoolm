package hei.student.schoolm.repository.mapper;

import hei.student.schoolm.model.Admin;
import hei.student.schoolm.repository.model.JAdmin;
import org.springframework.stereotype.Component;

@Component
public class JAdminMapper {
  public Admin toDomain(JAdmin jAdmin) {
    return Admin.builder()
        .id(jAdmin.getId())
        .email(jAdmin.getEmail())
        .firstName(jAdmin.getFirstName())
        .lastName(jAdmin.getLastName())
        .role(jAdmin.getRole())
        .password(jAdmin.getPassword())
        .createdAt(jAdmin.getCreatedAt())
        .updatedAt(jAdmin.getUpdatedAt())
        .build();
  }

  public JAdmin toEntity(Admin admin) {
    return JAdmin.builder()
        .id(admin.getId())
        .email(admin.getEmail())
        .firstName(admin.getFirstName())
        .lastName(admin.getLastName())
        .role(admin.getRole())
        .password(admin.getPassword())
        .createdAt(admin.getCreatedAt())
        .updatedAt(admin.getUpdatedAt())
        .build();
  }
}
