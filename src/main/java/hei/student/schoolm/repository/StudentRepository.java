package hei.student.schoolm.repository;

import hei.student.schoolm.model.Student;
import hei.student.schoolm.repository.jpa.JStudentRepository;
import hei.student.schoolm.repository.mapper.JStudentMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class StudentRepository {
  private final JStudentRepository repository;
  private final JStudentMapper mapper;

  @Transactional(readOnly = true)
  public List<Student> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public Optional<Student> findById(UUID id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  public List<Student> findAllByGroupId(UUID groupId) {
    return repository.findAllByGroupId(groupId).stream().map(mapper::toDomain).toList();
  }

  @Transactional
  public Student save(Student student) {
    return mapper.toDomain(repository.save(mapper.toEntity(student)));
  }

  @Transactional
  public void deleteById(UUID id) {
    repository.deleteById(id);
  }
}
