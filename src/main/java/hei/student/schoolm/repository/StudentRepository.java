package hei.student.schoolm.repository;

import hei.student.schoolm.model.Student;
import hei.student.schoolm.repository.jpa.JStudentRepository;
import hei.student.schoolm.repository.mapper.JStudentMapper;
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
  public Optional<Student> findById(UUID id) {
    return jStudentRepository.findById(id).map(jStudentMapper::toDomain);
  }
}
