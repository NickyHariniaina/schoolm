package hei.student.schoolm.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import hei.student.schoolm.dto.UpdateGradeRequest;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Exam;
import hei.student.schoolm.model.Grade;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.repository.GradeRepository;
import hei.student.schoolm.repository.jpa.JGradeHistoryRepository;
import hei.student.schoolm.repository.mapper.JGradeMapper;
import hei.student.schoolm.repository.model.JGradeHistory;
import hei.student.schoolm.validator.GradeValidator;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GradeServiceTest {
  @Mock private GradeRepository gradeRepository;

  @Mock private GradeValidator gradeValidator;

  @Mock private JGradeHistoryRepository gradeHistoryRepository;

  @Mock private JGradeMapper jGradeMapper;

  @InjectMocks private GradeService gradeService;

  private UUID gradeId;
  private UUID studentId;
  private UUID examId;
  private Grade grade;
  private UpdateGradeRequest request;

  @BeforeEach
  void setUp() {
    gradeId = UUID.randomUUID();
    studentId = UUID.randomUUID();
    examId = UUID.randomUUID();

    grade =
        Grade.builder()
            .id(gradeId)
            .student(Student.builder().id(studentId).build())
            .exam(Exam.builder().id(examId).build())
            .value(new BigDecimal("15.0"))
            .changeReason("Initial grade")
            .build();

    request = new UpdateGradeRequest(new BigDecimal("18.5"), "Correction following a complaint");
  }

  @Test
  void should_update_grade_successfully() {
    when(gradeValidator.checkGradeExists(gradeId)).thenReturn(grade);
    when(gradeRepository.save(any(Grade.class))).thenReturn(grade);

    var result = gradeService.updateGrade(gradeId, request);

    assertNotNull(result);
    verify(gradeHistoryRepository).save(any(JGradeHistory.class));
    verify(gradeRepository).save(grade);
    assertEquals(request.value(), grade.getValue());
    assertEquals(request.changeReason(), grade.getChangeReason());
  }

  @Test
  void should_throw_not_found_when_grade_does_not_exist() {
    when(gradeValidator.checkGradeExists(gradeId))
        .thenThrow(new NotFoundException("Grade not found"));

    assertThrows(NotFoundException.class, () -> gradeService.updateGrade(gradeId, request));
    verify(gradeRepository, never()).save(any());
    verify(gradeHistoryRepository, never()).save(any());
  }

  @Test
  void should_save_history_before_updating_grade() {
    var captor = ArgumentCaptor.forClass(JGradeHistory.class);
    when(gradeValidator.checkGradeExists(gradeId)).thenReturn(grade);
    when(gradeRepository.save(any(Grade.class))).thenReturn(grade);

    gradeService.updateGrade(gradeId, request);

    verify(gradeHistoryRepository).save(captor.capture());
    var savedHistory = captor.getValue();
    assertEquals(gradeId, savedHistory.getGradeId());
    assertEquals(studentId, savedHistory.getStudentId());
    assertEquals(examId, savedHistory.getExamId());
    assertEquals(new BigDecimal("15.0"), savedHistory.getOldValue());
    assertEquals(new BigDecimal("18.5"), savedHistory.getNewValue());
    assertEquals("Correction following a complaint", savedHistory.getChangeReason());
  }

  @Test
  void should_get_grade_history() {
    var now = java.time.Instant.now();
    var history =
        JGradeHistory.builder()
            .id(UUID.randomUUID())
            .gradeId(gradeId)
            .studentId(studentId)
            .examId(examId)
            .oldValue(new BigDecimal("15.0"))
            .newValue(new BigDecimal("18.5"))
            .changeReason("Correction following a complaint")
            .changedAt(now)
            .build();

    when(gradeValidator.checkGradeExists(gradeId)).thenReturn(grade);
    when(gradeHistoryRepository.findAllByGradeIdOrderByChangedAtDesc(gradeId))
        .thenReturn(List.of(history));

    var result = gradeService.getGradeHistory(gradeId);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(gradeId, result.getFirst().getGradeId());
    assertEquals(new BigDecimal("15.0"), result.getFirst().getOldValue());
    assertEquals(new BigDecimal("18.5"), result.getFirst().getNewValue());
    assertEquals("Correction following a complaint", result.getFirst().getChangeReason());
    assertEquals(now, result.getFirst().getChangedAt());
  }
}
