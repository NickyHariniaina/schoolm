package hei.student.schoolm.repository;

import hei.student.schoolm.model.Admin;
import hei.student.schoolm.repository.jpa.JAdminRepository;
import hei.student.schoolm.repository.mapper.JAdminMapper;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class AdminRepository {
  private final JAdminRepository jAdminRepository;
  private final JAdminMapper jAdminMapper;

  @Transactional(readOnly = true)
  public Optional<Admin> findById(UUID id) {
    return jAdminRepository.findById(id).map(jAdminMapper::toDomain);
  }

  @Transactional
  public Admin save(Admin admin) {
    return jAdminMapper.toDomain(jAdminRepository.save(jAdminMapper.toEntity(admin)));
  }
}
