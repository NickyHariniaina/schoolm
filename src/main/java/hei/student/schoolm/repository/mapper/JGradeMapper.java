package hei.student.schoolm.repository.mapper;

import hei.student.schoolm.dto.GradeDto;
import hei.student.schoolm.dto.GradeHistoryDto;
import hei.student.schoolm.model.Exam;
import hei.student.schoolm.model.Grade;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.repository.model.JGrade;
import hei.student.schoolm.repository.model.JGradeHistory;
import org.springframework.stereotype.Component;

@Component
public class JGradeMapper {
  public Grade toDomain(JGrade jGrade) {
    return Grade.builder()
        .id(jGrade.getId())
        .student(
            jGrade.getStudent() == null
                ? null
                : Student.builder().id(jGrade.getStudent().getId()).build())
        .exam(jGrade.getExam() == null ? null : Exam.builder().id(jGrade.getExam().getId()).build())
        .value(jGrade.getValue())
        .changeReason(jGrade.getChangeReason())
        .createdAt(jGrade.getCreatedAt())
        .updatedAt(jGrade.getUpdatedAt())
        .build();
  }

  public GradeDto toDto(Grade grade) {
    if (grade == null) return null;

    return new GradeDto(
        grade.getId(),
        grade.getStudent() != null ? grade.getStudent().getId() : null,
        grade.getExam() != null ? grade.getExam().getId() : null,
        grade.getValue(),
        grade.getChangeReason(),
        grade.getCreatedAt(),
        grade.getUpdatedAt());
  }

  public GradeHistoryDto toHistoryDto(JGradeHistory history) {
    if (history == null) return null;

    return new GradeHistoryDto(
        history.getId(),
        history.getGradeId(),
        history.getStudentId(),
        history.getExamId(),
        history.getOldValue(),
        history.getNewValue(),
        history.getChangeReason(),
        history.getChangedAt());
  }
}
