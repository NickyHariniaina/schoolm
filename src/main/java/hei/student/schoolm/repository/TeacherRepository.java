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
  private final JTeacherRepository jTeacherRepository;
  private final JTeacherMapper jTeacherMapper;

  @Transactional(readOnly = true)
  public List<Teacher> findAll() {
    return jTeacherRepository.findAll().stream().map(jTeacherMapper::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public List<Teacher> findAllById(List<UUID> ids) {
    return jTeacherRepository.findAllById(ids).stream().map(jTeacherMapper::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public Optional<Teacher> findById(UUID id) {
    return jTeacherRepository.findById(id).map(jTeacherMapper::toDomain);
  }

  @Transactional
  public Teacher save(Teacher teacher) {
    return jTeacherMapper.toDomain(jTeacherRepository.save(jTeacherMapper.toEntity(teacher)));
  }

  @Transactional
  public void deleteById(UUID id) {
    jTeacherRepository.deleteById(id);
  }
}
