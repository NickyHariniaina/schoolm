package hei.student.schoolm.service;

import hei.student.schoolm.dto.ExamDto;
import hei.student.schoolm.dto.ExamRequest;
import hei.student.schoolm.exception.BadRequestException;
import hei.student.schoolm.model.Exam;
import hei.student.schoolm.repository.ExamRepository;
import hei.student.schoolm.repository.GradeRepository;
import hei.student.schoolm.repository.jpa.JGradeHistoryRepository;
import hei.student.schoolm.util.Fraction;
import hei.student.schoolm.validator.CourseValidator;
import hei.student.schoolm.validator.ExamValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExamService {
  private final ExamRepository examRepository;
  private final ExamValidator examValidator;
  private final CourseValidator courseValidator;
  private final GradeRepository gradeRepository;
  private final JGradeHistoryRepository jGradeHistoryRepository;

  @Transactional(readOnly = true)
  public List<ExamDto> getExamsByCourseId(UUID courseId) {
    courseValidator.checkCourseExists(courseId);
    return examRepository.findAllByCourseId(courseId).stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public ExamDto getById(UUID examId) {
    return toDto(examValidator.checkExamExists(examId));
  }

  @Transactional
  public ExamDto upsert(ExamRequest request) {
    var course = courseValidator.checkCourseExists(request.courseId());
    var coefficient = new Fraction(request.coefNumerator(), request.coefDenominator());

    Exam exam;
    if (request.id() == null) {
      exam =
          Exam.builder()
              .id(UUID.randomUUID())
              .course(course)
              .dateExam(request.dateExam())
              .coefficient(coefficient)
              .build();
    } else {
      exam = examValidator.checkExamExists(request.id());
      if (exam.getCourse() == null || !exam.getCourse().getId().equals(request.courseId())) {
        throw new BadRequestException("exam already belongs to another course");
      }
      exam.setDateExam(request.dateExam());
      exam.setCoefficient(coefficient);
    }

    examRepository.save(exam, request.courseId());
    return toDto(exam);
  }

  @Transactional
  public void delete(UUID examId) {
    examValidator.checkExamExists(examId);
    jGradeHistoryRepository.deleteAllByExamId(examId);
    gradeRepository.deleteAllByExamId(examId);
    examRepository.deleteById(examId);
  }

  private ExamDto toDto(Exam exam) {
    return new ExamDto(
        exam.getId(),
        exam.getCourse() == null ? null : exam.getCourse().getId(),
        exam.getDateExam(),
        exam.getCoefficient().numerator(),
        exam.getCoefficient().denominator(),
        exam.getCreatedAt(),
        exam.getUpdatedAt());
  }
}
