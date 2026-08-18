package hei.student.schoolm.repository;

import hei.student.schoolm.model.Cohort;
import hei.student.schoolm.repository.jpa.JCohortRepository;
import hei.student.schoolm.repository.mapper.JCohortMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class CohortRepository {
  private final JCohortRepository jCohortRepository;
  private final JCohortMapper jCohortMapper;

  @Transactional(readOnly = true)
  public List<Cohort> findAll() {
    return jCohortRepository.findAll().stream().map(jCohortMapper::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public Optional<Cohort> findByRef(String ref) {
    return jCohortRepository.findByRef(ref).map(jCohortMapper::toDomain);
  }
}
