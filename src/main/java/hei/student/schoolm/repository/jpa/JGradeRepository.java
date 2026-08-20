package hei.student.schoolm.repository.jpa;

import hei.student.schoolm.repository.model.JGrade;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JGradeRepository extends JpaRepository<JGrade, UUID> {
  List<JGrade> findAllByStudentId(UUID studentId);

  List<JGrade> findAllByExamId(UUID examId);

  void deleteAllByStudentId(UUID studentId);

  void deleteAllByExamId(UUID examId);
}
