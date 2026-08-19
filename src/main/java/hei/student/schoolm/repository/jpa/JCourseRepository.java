package hei.student.schoolm.repository.jpa;

import hei.student.schoolm.repository.model.JCourse;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JCourseRepository extends JpaRepository<JCourse, UUID> {
  boolean existsByTeachersId(UUID teacherId);
}
