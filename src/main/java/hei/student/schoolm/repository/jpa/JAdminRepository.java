package hei.student.schoolm.repository.jpa;

import hei.student.schoolm.repository.model.JAdmin;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JAdminRepository extends JpaRepository<JAdmin, UUID> {
  Optional<JAdmin> findByEmailIgnoreCase(String email);
}
