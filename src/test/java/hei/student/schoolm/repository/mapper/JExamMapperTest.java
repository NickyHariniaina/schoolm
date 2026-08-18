package hei.student.schoolm.repository.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hei.student.schoolm.model.Grade;
import hei.student.schoolm.repository.model.JExam;
import hei.student.schoolm.repository.model.JGrade;
import hei.student.schoolm.repository.model.JStudent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JExamMapperTest {
  private static final UUID EXAM_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID GRADE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

  private final JExamMapper jExamMapper = new JExamMapper(new JGradeMapper());

  @Test
  void toDomain_maps_all_fields_and_grades() {
    var date = LocalDate.of(2026, 1, 15);
    var createdAt = Instant.parse("2026-01-01T08:00:00Z");
    var updatedAt = Instant.parse("2026-01-16T08:00:00Z");
    var jExam =
        JExam.builder()
            .id(EXAM_ID)
            .dateExam(date)
            .coefNumerator(1)
            .coefDenominator(2)
            .grades(
                List.of(
                    JGrade.builder()
                        .id(GRADE_ID)
                        .student(JStudent.builder().id(STUDENT_ID).build())
                        .value(new BigDecimal("14.5"))
                        .build()))
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();

    var exam = jExamMapper.toDomain(jExam);

    assertEquals(EXAM_ID, exam.getId());
    assertEquals(date, exam.getDateExam());
    assertEquals(0.5, exam.getCoefficient().toDouble());
    assertEquals(createdAt, exam.getCreatedAt());
    assertEquals(updatedAt, exam.getUpdatedAt());
    assertEquals(1, exam.getGrades().size());
    Grade grade = exam.getGrades().get(0);
    assertEquals(GRADE_ID, grade.getId());
    assertEquals(STUDENT_ID, grade.getStudent().getId());
    assertEquals(new BigDecimal("14.5"), grade.getValue());
  }

  @Test
  void toDomain_maps_empty_grades_when_null() {
    var jExam = JExam.builder().id(EXAM_ID).coefNumerator(1).coefDenominator(2).build();

    var exam = jExamMapper.toDomain(jExam);

    assertTrue(exam.getGrades().isEmpty());
  }
}
