package hei.student.schoolm.repository;

import hei.student.schoolm.model.Cohort;
import hei.student.schoolm.repository.jpa.JCohortRepository;
import hei.student.schoolm.repository.mapper.JCohortMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class CohortRepository {
  private final JCohortRepository repository;
  private final JCohortMapper mapper;

  @Transactional(readOnly = true)
  public List<Cohort> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public Optional<Cohort> findById(UUID id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  public Optional<Cohort> findByRef(String ref) {
    return repository.findByRef(ref).map(mapper::toDomain);
  }

  @Transactional
  public Cohort save(Cohort cohort) {
    return mapper.toDomain(repository.save(mapper.toEntity(cohort)));
  }
}
