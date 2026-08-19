package hei.student.schoolm.repository.mapper;

import static org.junit.jupiter.api.Assertions.*;

import hei.student.schoolm.model.Exam;
import hei.student.schoolm.model.Grade;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.repository.model.JGrade;
import hei.student.schoolm.repository.model.JGradeHistory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class JGradeMapperTest {
  private final JGradeMapper mapper = new JGradeMapper();

  @Test
  void should_map_JGrade_to_domain() {
    var id = UUID.randomUUID();
    var studentId = UUID.randomUUID();
    var examId = UUID.randomUUID();
    var jGrade =
        JGrade.builder().id(id).value(new BigDecimal("15.0")).changeReason("Initial grade").build();

    var result = mapper.toDomain(jGrade);

    assertNotNull(result);
    assertEquals(id, result.getId());
    assertEquals(new BigDecimal("15.0"), result.getValue());
    assertEquals("Initial grade", result.getChangeReason());
  }

  @Test
  void should_return_null_when_JGrade_is_null() {
    var result = mapper.toDomain(null);

    assertNull(result);
  }

  @Test
  void should_map_grade_to_dto() {
    var id = UUID.randomUUID();
    var studentId = UUID.randomUUID();
    var examId = UUID.randomUUID();
    var now = Instant.now();
    var grade =
        Grade.builder()
            .id(id)
            .student(Student.builder().id(studentId).build())
            .exam(Exam.builder().id(examId).build())
            .value(new BigDecimal("15.0"))
            .changeReason("Initial grade")
            .createdAt(now)
            .updatedAt(now)
            .build();

    var result = mapper.toDto(grade);

    assertNotNull(result);
    assertEquals(id, result.getId());
    assertEquals(studentId, result.getStudentId());
    assertEquals(examId, result.getExamId());
    assertEquals(new BigDecimal("15.0"), result.getValue());
    assertEquals("Initial grade", result.getChangeReason());
    assertEquals(now, result.getCreatedAt());
    assertEquals(now, result.getUpdatedAt());
  }

  @Test
  void should_return_null_when_grade_is_null() {
    var result = mapper.toDto(null);

    assertNull(result);
  }

  @Test
  void should_map_JGradeHistory_toDto() {
    var id = UUID.randomUUID();
    var gradeId = UUID.randomUUID();
    var studentId = UUID.randomUUID();
    var examId = UUID.randomUUID();
    var now = Instant.now();

    var history =
        JGradeHistory.builder()
            .id(id)
            .gradeId(gradeId)
            .studentId(studentId)
            .examId(examId)
            .oldValue(new BigDecimal("15.0"))
            .newValue(new BigDecimal("18.5"))
            .changeReason("Correction following a complaint")
            .changedAt(now)
            .build();

    var result = mapper.toHistoryDto(history);

    assertNotNull(result);
    assertEquals(id, result.getId());
    assertEquals(gradeId, result.getGradeId());
    assertEquals(studentId, result.getStudentId());
    assertEquals(examId, result.getExamId());
    assertEquals(new BigDecimal("15.0"), result.getOldValue());
    assertEquals(new BigDecimal("18.5"), result.getNewValue());
    assertEquals("Correction following a complaint", result.getChangeReason());
    assertEquals(now, result.getChangedAt());
  }

  @Test
  void should_return_null_when_JGradeHistory_isNull() {
    var result = mapper.toHistoryDto(null);

    assertNull(result);
  }
}
