package hei.student.schoolm.repository.jpa;

import hei.student.schoolm.repository.model.JCohort;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JCohortRepository extends JpaRepository<JCohort, UUID> {
  Optional<JCohort> findByRef(String ref);
}
