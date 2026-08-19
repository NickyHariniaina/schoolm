package hei.student.schoolm.repository.jpa;

import hei.student.schoolm.repository.model.JGroupFlow;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JGroupFlowRepository extends JpaRepository<JGroupFlow, UUID> {
  List<JGroupFlow> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

  List<JGroupFlow> findByStudentId(UUID studentId);

  void deleteAllByStudentId(UUID studentId);
}
