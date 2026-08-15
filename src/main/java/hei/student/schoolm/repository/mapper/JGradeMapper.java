package hei.student.schoolm.repository.mapper;

import hei.student.schoolm.model.Exam;
import hei.student.schoolm.model.Grade;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.repository.model.JGrade;
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
        .exam(
            jGrade.getExam() == null
                ? null
                : Exam.builder().id(jGrade.getExam().getId()).build())
        .value(jGrade.getValue())
        .changeReason(jGrade.getChangeReason())
        .createdAt(jGrade.getCreatedAt())
        .updatedAt(jGrade.getUpdatedAt())
        .build();
  }
}