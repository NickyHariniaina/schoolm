package hei.student.schoolm.repository;

import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.repository.jpa.JTeacherRepository;
import hei.student.schoolm.repository.mapper.JTeacherMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class TeacherRepository {
  private final JTeacherRepository repository;
  private final JTeacherMapper mapper;

  @Transactional(readOnly = true)
  public List<Teacher> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public List<Teacher> findAllById(List<UUID> ids) {
    return repository.findAllById(ids).stream().map(mapper::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public Optional<Teacher> findById(UUID id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Transactional
  public Teacher save(Teacher teacher) {
    return mapper.toDomain(repository.save(mapper.toEntity(teacher)));
  }

  @Transactional
  public void deleteById(UUID id) {
    repository.deleteById(id);
  }
}
