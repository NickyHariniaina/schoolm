package hei.student.schoolm.validator;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Cohort;
import hei.student.schoolm.repository.CohortRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CohortValidator {
  private final CohortRepository cohortRepository;

  public Cohort checkCohortExists(String ref) {
    return cohortRepository
        .findByRef(ref)
        .orElseThrow(() -> new NotFoundException("Cohort " + ref + " not found"));
  }
}
