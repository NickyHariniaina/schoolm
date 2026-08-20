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
  private final JExamRepository jExamRepository;
  private final JExamMapper jExamMapper;

  @Transactional(readOnly = true)
  public Optional<Exam> findById(UUID id) {
    return jExamRepository.findById(id).map(jExamMapper::toDomain);
  }

  @Transactional(readOnly = true)
  public List<Exam> findAllByCourseId(UUID courseId) {
    return jExamRepository.findAllByCourseId(courseId).stream().map(jExamMapper::toDomain).toList();
  }

  @Transactional
  public Exam save(Exam exam, UUID courseId) {
    JExam jExam = jExamMapper.toEntity(exam, courseId);
    var saved = jExamRepository.save(jExam);
    return jExamMapper.toDomain(saved);
  }

  @Transactional
  public void deleteById(UUID id) {
    jExamRepository.deleteById(id);
  }
}
