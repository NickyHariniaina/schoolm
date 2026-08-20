package hei.student.schoolm.repository.mapper;

import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Exam;
import hei.student.schoolm.model.Grade;
import hei.student.schoolm.repository.model.JCourse;
import hei.student.schoolm.repository.model.JExam;
import hei.student.schoolm.util.Fraction;
import java.util.List;
import java.util.UUID;
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
        .course(
            jExam.getCourse() == null
                ? null
                : Course.builder().id(jExam.getCourse().getId()).build())
        .dateExam(jExam.getDateExam())
        .coefficient(new Fraction(jExam.getCoefNumerator(), jExam.getCoefDenominator()))
        .grades(grades)
        .createdAt(jExam.getCreatedAt())
        .updatedAt(jExam.getUpdatedAt())
        .build();
  }

  public JExam toEntity(Exam exam, UUID courseId) {
    return JExam.builder()
        .id(exam.getId())
        .course(JCourse.builder().id(courseId).build())
        .dateExam(exam.getDateExam())
        .coefNumerator(exam.getCoefficient().numerator())
        .coefDenominator(exam.getCoefficient().denominator())
        .build();
  }
}
