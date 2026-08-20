package hei.student.schoolm.repository;

import hei.student.schoolm.model.Exam;
import hei.student.schoolm.repository.jpa.JExamRepository;
import hei.student.schoolm.repository.mapper.JExamMapper;
import hei.student.schoolm.repository.model.JExam;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ExamRepository {
  private final JExamRepository repository;
  private final JExamMapper mapper;

  @Transactional(readOnly = true)
  public Optional<Exam> findById(UUID id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  public List<Exam> findAllByCourseId(UUID courseId) {
    return repository.findAllByCourseId(courseId).stream().map(mapper::toDomain).toList();
  }

  @Transactional
  public Exam save(Exam exam, UUID courseId) {
    JExam jExam = mapper.toEntity(exam, courseId);
    var saved = repository.save(jExam);
    return mapper.toDomain(saved);
  }

  @Transactional
  public void deleteById(UUID id) {
    repository.deleteById(id);
  }
}
