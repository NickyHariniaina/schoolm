package hei.student.schoolm.validator;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Exam;
import hei.student.schoolm.repository.ExamRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExamValidator {
  private final ExamRepository repository;

  public Exam checkExamExists(UUID examId) {
    return repository
        .findById(examId)
        .orElseThrow(() -> new NotFoundException("Exam " + examId + " not found"));
  }
}
