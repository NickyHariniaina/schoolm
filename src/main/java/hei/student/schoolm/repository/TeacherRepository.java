package hei.student.schoolm.repository;

import hei.student.schoolm.repository.model.JTeacher;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<JTeacher, UUID> {}
