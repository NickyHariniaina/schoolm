package hei.student.schoolm.repository.jpa;

import hei.student.schoolm.repository.model.JGroup;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JGroupRepository extends JpaRepository<JGroup, UUID> {}
