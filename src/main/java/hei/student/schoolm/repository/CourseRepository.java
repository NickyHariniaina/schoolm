package hei.student.schoolm.repository;

import hei.student.schoolm.repository.model.JCourse;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<JCourse, UUID> {}
