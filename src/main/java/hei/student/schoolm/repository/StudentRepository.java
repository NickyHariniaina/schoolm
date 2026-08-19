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
  private final JStudentRepository jStudentRepository;
  private final JStudentMapper jStudentMapper;

  @Transactional(readOnly = true)
  public List<Student> findAll() {
    return jStudentRepository.findAll().stream().map(jStudentMapper::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public Optional<Student> findById(UUID id) {
    return jStudentRepository.findById(id).map(jStudentMapper::toDomain);
  }

  @Transactional(readOnly = true)
  public List<Student> findAllByGroupId(UUID groupId) {
    return jStudentRepository.findAllByGroupId(groupId).stream()
        .map(jStudentMapper::toDomain)
        .toList();
  }

  @Transactional
  public Student save(Student student) {
    return jStudentMapper.toDomain(jStudentRepository.save(jStudentMapper.toEntity(student)));
  }

  @Transactional
  public void deleteById(UUID id) {
    jStudentRepository.deleteById(id);
  }
}
