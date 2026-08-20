package hei.student.schoolm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.schoolm.dto.ExamRequest;
import hei.student.schoolm.exception.BadRequestException;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Exam;
import hei.student.schoolm.repository.ExamRepository;
import hei.student.schoolm.repository.GradeRepository;
import hei.student.schoolm.repository.jpa.JGradeHistoryRepository;
import hei.student.schoolm.util.Fraction;
import hei.student.schoolm.validator.CourseValidator;
import hei.student.schoolm.validator.ExamValidator;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {
  private static final UUID COURSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID EXAM_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Mock ExamRepository examRepository;
  @Mock ExamValidator examValidator;
  @Mock CourseValidator courseValidator;
  @Mock GradeRepository gradeRepository;
  @Mock JGradeHistoryRepository jGradeHistoryRepository;
  @InjectMocks ExamService examService;

  private Course createCourse() {
    return Course.builder().id(COURSE_ID).build();
  }

  private Exam createExam() {
    return Exam.builder()
        .id(EXAM_ID)
        .course(createCourse())
        .dateExam(LocalDate.of(2026, 3, 1))
        .coefficient(new Fraction(1, 2))
        .build();
  }

  @Test
  void should_list_exams_by_course() {
    when(courseValidator.checkCourseExists(COURSE_ID)).thenReturn(createCourse());
    when(examRepository.findAllByCourseId(COURSE_ID)).thenReturn(List.of(createExam()));

    var result = examService.getExamsByCourseId(COURSE_ID);

    assertEquals(1, result.size());
    assertEquals(EXAM_ID, result.get(0).id());
    assertEquals(1, result.get(0).coefNumerator());
    assertEquals(2, result.get(0).coefDenominator());
    verify(courseValidator).checkCourseExists(COURSE_ID);
  }

  @Test
  void should_get_exam_by_id() {
    when(examValidator.checkExamExists(EXAM_ID)).thenReturn(createExam());

    var result = examService.getById(EXAM_ID);

    assertEquals(EXAM_ID, result.id());
    assertEquals(COURSE_ID, result.courseId());
  }

  @Test
  void should_create_exam() {
    var request =
        ExamRequest.builder()
            .courseId(COURSE_ID)
            .dateExam(LocalDate.of(2026, 3, 1))
            .coefNumerator(1)
            .coefDenominator(2)
            .build();
    when(courseValidator.checkCourseExists(COURSE_ID)).thenReturn(createCourse());
    when(examRepository.save(any(), eq(COURSE_ID))).thenReturn(createExam());

    var result = examService.upsert(request);

    assertNotNull(result.id());
    assertEquals(COURSE_ID, result.courseId());
    verify(examRepository).save(any(), eq(COURSE_ID));
  }

  @Test
  void should_update_exam() {
    var request =
        ExamRequest.builder()
            .id(EXAM_ID)
            .courseId(COURSE_ID)
            .dateExam(LocalDate.of(2026, 3, 1))
            .coefNumerator(1)
            .coefDenominator(1)
            .build();
    when(courseValidator.checkCourseExists(COURSE_ID)).thenReturn(createCourse());
    when(examValidator.checkExamExists(EXAM_ID)).thenReturn(createExam());

    var result = examService.upsert(request);

    assertEquals(EXAM_ID, result.id());
    verify(examRepository).save(any(), eq(COURSE_ID));
  }

  @Test
  void should_throw_when_moving_exam_to_another_course() {
    var request =
        ExamRequest.builder()
            .id(EXAM_ID)
            .courseId(UUID.fromString("00000000-0000-0000-0000-000000000009"))
            .dateExam(LocalDate.of(2026, 3, 1))
            .coefNumerator(1)
            .coefDenominator(1)
            .build();
    when(courseValidator.checkCourseExists(any())).thenReturn(createCourse());
    when(examValidator.checkExamExists(EXAM_ID)).thenReturn(createExam());

    assertThrows(BadRequestException.class, () -> examService.upsert(request));
  }

  @Test
  void should_throw_when_course_missing() {
    var unknown = UUID.fromString("99999999-9999-9999-9999-999999999999");
    var request =
        ExamRequest.builder()
            .courseId(unknown)
            .dateExam(LocalDate.of(2026, 3, 1))
            .coefNumerator(1)
            .coefDenominator(2)
            .build();
    when(courseValidator.checkCourseExists(unknown))
        .thenThrow(new NotFoundException("Course " + unknown + " not found"));

    assertThrows(NotFoundException.class, () -> examService.upsert(request));
  }

  @Test
  void should_delete_exam_and_cascade() {
    when(examValidator.checkExamExists(EXAM_ID)).thenReturn(createExam());

    examService.delete(EXAM_ID);

    verify(jGradeHistoryRepository).deleteAllByExamId(EXAM_ID);
    verify(gradeRepository).deleteAllByExamId(EXAM_ID);
    verify(examRepository).deleteById(EXAM_ID);
  }
}
