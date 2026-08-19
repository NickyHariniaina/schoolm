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
  private final JCourseRepository jCourseRepository;
  private final JCourseMapper jCourseMapper;

  @Transactional(readOnly = true)
  public Optional<Course> findById(UUID id) {
    return jCourseRepository.findById(id).map(jCourseMapper::toDomain);
  }

  @Transactional(readOnly = true)
  public List<Course> findAll() {
    return jCourseRepository.findAll().stream().map(jCourseMapper::toDomain).toList();
  }

  @Transactional
  public Course save(Course course) {
    return jCourseMapper.toDomain(jCourseRepository.save(jCourseMapper.toEntity(course)));
  }

  @Transactional(readOnly = true)
  public boolean existsByTeacherId(UUID teacherId) {
    return jCourseRepository.existsByTeachersId(teacherId);
  }
}
