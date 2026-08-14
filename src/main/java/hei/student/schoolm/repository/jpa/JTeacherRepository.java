package hei.student.schoolm.repository.jpa;

import hei.student.schoolm.repository.model.JTeacher;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JTeacherRepository extends JpaRepository<JTeacher, UUID> {}
