package hei.student.schoolm.repository.mapper;

import hei.student.schoolm.model.Exam;
import hei.student.schoolm.model.Grade;
import hei.student.schoolm.repository.model.JExam;
import hei.student.schoolm.util.Fraction;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JExamMapper {
  private final JGradeMapper jGradeMapper;

  public Exam toDomain(JExam jExam) {
    var grades =
        jExam.getGrades() == null
            ? List.<Grade>of()
            : jExam.getGrades().stream().map(jGradeMapper::toDomain).toList();
    return Exam.builder()
        .id(jExam.getId())
        .dateExam(jExam.getDateExam())
        .coefficient(new Fraction(jExam.getCoefNumerator(), jExam.getCoefDenominator()))
        .grades(grades)
        .createdAt(jExam.getCreatedAt())
        .updatedAt(jExam.getUpdatedAt())
        .build();
  }
}