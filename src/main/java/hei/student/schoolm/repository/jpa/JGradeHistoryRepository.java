package hei.student.schoolm.repository.jpa;

import hei.student.schoolm.repository.model.JGradeHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JGradeHistoryRepository extends JpaRepository<JGradeHistory, UUID> {
  List<JGradeHistory> findAllByGradeIdOrderByChangedAtDesc(UUID gradeId);

  void deleteAllByStudentId(UUID studentId);
}
