package hei.student.schoolm.repository;

import hei.student.schoolm.model.Course;
import hei.student.schoolm.repository.jpa.JCourseRepository;
import hei.student.schoolm.repository.mapper.JCourseMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class CourseRepository {
  private final JCourseRepository repository;
  private final JCourseMapper mapper;

  @Transactional(readOnly = true)
  public Optional<Course> findById(UUID id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  public List<Course> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  @Transactional
  public Course save(Course course) {
    return mapper.toDomain(repository.save(mapper.toEntity(course)));
  }

  @Transactional(readOnly = true)
  public boolean existsByTeacherId(UUID teacherId) {
    return repository.existsByTeachersId(teacherId);
  }

  @Transactional
  public void deleteById(UUID id) {
    repository.deleteById(id);
  }
}
