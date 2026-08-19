package hei.student.schoolm.repository;

import hei.student.schoolm.model.Grade;
import hei.student.schoolm.repository.jpa.JGradeRepository;
import hei.student.schoolm.repository.mapper.JGradeMapper;
import hei.student.schoolm.repository.model.JExam;
import hei.student.schoolm.repository.model.JGrade;
import hei.student.schoolm.repository.model.JStudent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class GradeRepository {
  private final JGradeRepository jGradeRepository;
  private final JGradeMapper jGradeMapper;

  @Transactional(readOnly = true)
  public Optional<Grade> findById(UUID id) {
    return jGradeRepository.findById(id).map(jGradeMapper::toDomain);
  }

  @Transactional(readOnly = true)
  public List<Grade> findAll() {
    return jGradeRepository.findAll().stream().map(jGradeMapper::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public List<Grade> findAllByStudentId(UUID studentId) {
    return jGradeRepository.findAllByStudentId(studentId).stream()
        .map(jGradeMapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<Grade> findAllByExamId(UUID examId) {
    return jGradeRepository.findAllByExamId(examId).stream().map(jGradeMapper::toDomain).toList();
  }

  @Transactional
  public Grade save(Grade grade) {
    var jGrade =
        JGrade.builder()
            .id(grade.getId())
            .value(grade.getValue())
            .changeReason(grade.getChangeReason())
            .build();

    if (grade.getStudent() != null) {
      jGrade.setStudent(JStudent.builder().id(grade.getStudent().getId()).build());
    }
    if (grade.getExam() != null) {
      jGrade.setExam(JExam.builder().id(grade.getExam().getId()).build());
    }

    var saved = jGradeRepository.save(jGrade);
    return jGradeMapper.toDomain(saved);
  }

  @Transactional
  public void deleteAllByStudentId(UUID studentId) {
    jGradeRepository.deleteAllByStudentId(studentId);
  }

  @Transactional
  public void deleteAllByExamId(UUID examId) {
    jGradeRepository.deleteAllByExamId(examId);
  }

  @Transactional
  public void deleteById(UUID id) {
    jGradeRepository.deleteById(id);
  }
}
