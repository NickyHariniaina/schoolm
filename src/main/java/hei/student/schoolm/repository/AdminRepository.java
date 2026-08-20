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
  private final JAdminRepository repository;
  private final JAdminMapper mapper;

  @Transactional(readOnly = true)
  public Optional<Admin> findById(UUID id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Transactional
  public Admin save(Admin admin) {
    return mapper.toDomain(repository.save(mapper.toEntity(admin)));
  }
}
