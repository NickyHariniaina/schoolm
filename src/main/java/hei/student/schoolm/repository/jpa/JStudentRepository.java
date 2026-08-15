package hei.student.schoolm.repository.jpa;

import hei.student.schoolm.repository.model.JStudent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JStudentRepository extends JpaRepository<JStudent, UUID> {}