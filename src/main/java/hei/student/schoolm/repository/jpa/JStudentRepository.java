package hei.student.schoolm.repository.jpa;

import hei.student.schoolm.repository.model.JStudent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JStudentRepository extends JpaRepository<JStudent, UUID> {
  List<JStudent> findAllByGroupId(UUID groupId);

  Optional<JStudent> findByEmailIgnoreCase(String email);

  Optional<JStudent> findTopByReferenceStartingWithOrderByReferenceDesc(String prefix);
}
