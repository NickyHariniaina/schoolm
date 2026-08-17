package hei.student.schoolm.validator;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Grade;
import hei.student.schoolm.repository.GradeRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GradeValidator {
  private final GradeRepository gradeRepository;

  public Grade checkGradeExists(UUID gradeId) {
    return gradeRepository
        .findById(gradeId)
        .orElseThrow(() -> new NotFoundException("Grade " + gradeId + " not found"));
  }
}
