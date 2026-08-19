package hei.student.schoolm.repository.jpa;

import hei.student.schoolm.repository.model.JExam;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JExamRepository extends JpaRepository<JExam, UUID> {
  List<JExam> findAllByCourseId(UUID courseId);

  void deleteAllByCourseId(UUID courseId);
}
