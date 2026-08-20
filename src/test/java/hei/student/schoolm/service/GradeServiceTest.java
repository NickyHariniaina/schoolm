package hei.student.schoolm.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import hei.student.schoolm.dto.GradeDto;
import hei.student.schoolm.dto.GradeHistoryDto;
import hei.student.schoolm.dto.GradeRequest;
import hei.student.schoolm.dto.UpdateGradeRequest;
import hei.student.schoolm.exception.BadRequestException;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Exam;
import hei.student.schoolm.model.Grade;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.repository.GradeRepository;
import hei.student.schoolm.repository.jpa.JGradeHistoryRepository;
import hei.student.schoolm.repository.mapper.JGradeMapper;
import hei.student.schoolm.repository.model.JGradeHistory;
import hei.student.schoolm.util.SecurityUtil;
import hei.student.schoolm.validator.CourseValidator;
import hei.student.schoolm.validator.ExamValidator;
import hei.student.schoolm.validator.GradeValidator;
import hei.student.schoolm.validator.StudentValidator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GradeServiceTest {
  @Mock private GradeRepository gradeRepository;

  @Mock private GradeValidator gradeValidator;

  @Mock private JGradeHistoryRepository gradeHistoryRepository;

  @Mock private JGradeMapper jGradeMapper;

  @Mock private SecurityUtil securityUtil;

  @Mock private ExamValidator examValidator;

  @Mock private StudentValidator studentValidator;

  @Mock private CourseValidator courseValidator;

  @InjectMocks private GradeService gradeService;

  private UUID gradeId;
  private UUID studentId;
  private UUID examId;
  private Grade grade;
  private UpdateGradeRequest request;
  private GradeDto expectedDto;
  private GradeHistoryDto expectedHistoryDto;

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

    expectedDto =
        new GradeDto(
            gradeId,
            studentId,
            examId,
            new BigDecimal("18.5"),
            "Correction following a complaint",
            Instant.now(),
            Instant.now());
  }

  @Test
  void should_get_grade_history() {
    var now = Instant.now();
    var historyId = UUID.randomUUID();
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

    var expectedHistoryDto =
        new GradeHistoryDto(
            historyId,
            gradeId,
            studentId,
            examId,
            new BigDecimal("15.0"),
            new BigDecimal("18.5"),
            "Correction following a complaint",
            now);

    when(gradeValidator.checkGradeExists(gradeId)).thenReturn(grade);
    when(gradeHistoryRepository.findAllByGradeIdOrderByChangedAtDesc(gradeId))
        .thenReturn(List.of(history));
    when(jGradeMapper.toHistoryDto(any(JGradeHistory.class))).thenReturn(expectedHistoryDto);

    var result = gradeService.getGradeHistory(gradeId);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(gradeId, result.get(0).getGradeId());
    assertEquals(new BigDecimal("15.0"), result.get(0).getOldValue());
    assertEquals(new BigDecimal("18.5"), result.get(0).getNewValue());
    assertEquals("Correction following a complaint", result.get(0).getChangeReason());
    assertEquals(now, result.get(0).getChangedAt());
  }

  @Test
  void should_get_grades_by_exam() {
    var exam =
        Exam.builder()
            .id(examId)
            .course(Course.builder().id(UUID.randomUUID()).teachers(List.of()).build())
            .build();
    when(examValidator.checkExamExists(examId)).thenReturn(exam);
    when(gradeRepository.findAllByExamId(examId)).thenReturn(List.of(grade));
    when(jGradeMapper.toDto(any(Grade.class))).thenReturn(expectedDto);

    var result = gradeService.getGradesByExamId(examId);

    assertEquals(1, result.size());
    verify(examValidator).checkExamExists(examId);
  }

  @Test
  void should_get_grade_by_id() {
    when(gradeValidator.checkGradeExists(gradeId)).thenReturn(grade);
    when(jGradeMapper.toDto(grade)).thenReturn(expectedDto);

    var result = gradeService.getGradeById(gradeId);

    assertNotNull(result);
    verify(gradeValidator).checkGradeExists(gradeId);
  }

  @Test
  void should_create_grade_for_exam() {
    var exam =
        Exam.builder()
            .id(examId)
            .course(Course.builder().id(UUID.randomUUID()).teachers(List.of()).build())
            .build();
    var request = new GradeRequest(null, studentId, new BigDecimal("12.0"), null);
    when(examValidator.checkExamExists(examId)).thenReturn(exam);
    when(studentValidator.checkStudentExists(studentId))
        .thenReturn(Student.builder().id(studentId).build());
    when(gradeRepository.save(any(Grade.class))).thenReturn(grade);
    when(jGradeMapper.toDto(any(Grade.class))).thenReturn(expectedDto);

    var result = gradeService.upsertGrades(examId, List.of(request));

    assertEquals(1, result.size());
    verify(gradeRepository).save(any(Grade.class));
  }

  @Test
  void should_update_existing_grade_in_exam() {
    var exam =
        Exam.builder()
            .id(examId)
            .course(Course.builder().id(UUID.randomUUID()).teachers(List.of()).build())
            .build();
    var request = new GradeRequest(gradeId, studentId, new BigDecimal("18.5"), "Reason");
    when(examValidator.checkExamExists(examId)).thenReturn(exam);
    when(gradeValidator.checkGradeExists(gradeId)).thenReturn(grade);
    when(gradeRepository.save(any(Grade.class))).thenReturn(grade);
    when(jGradeMapper.toDto(any(Grade.class))).thenReturn(expectedDto);

    var result = gradeService.upsertGrades(examId, List.of(request));

    assertEquals(1, result.size());
    verify(gradeHistoryRepository).save(any(JGradeHistory.class));
    verify(gradeRepository).save(any(Grade.class));
  }

  @Test
  void should_throw_when_updating_grade_without_change_reason() {
    var exam =
        Exam.builder()
            .id(examId)
            .course(Course.builder().id(UUID.randomUUID()).teachers(List.of()).build())
            .build();
    var request = new GradeRequest(gradeId, studentId, new BigDecimal("18.5"), null);
    when(examValidator.checkExamExists(examId)).thenReturn(exam);
    when(gradeValidator.checkGradeExists(gradeId)).thenReturn(grade);

    assertThrows(
        BadRequestException.class, () -> gradeService.upsertGrades(examId, List.of(request)));
  }

  @Test
  void should_delete_grade() {
    when(gradeValidator.checkGradeExists(gradeId)).thenReturn(grade);

    gradeService.delete(gradeId);

    verify(gradeHistoryRepository).deleteAllByGradeId(gradeId);
    verify(gradeRepository).deleteById(gradeId);
  }
}
