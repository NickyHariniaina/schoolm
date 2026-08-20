package hei.student.schoolm.validator;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Cohort;
import hei.student.schoolm.repository.CohortRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CohortValidator {
  private final CohortRepository repository;

  public Cohort checkCohortExists(String ref) {
    return repository
        .findByRef(ref)
        .orElseThrow(() -> new NotFoundException("Cohort " + ref + " not found"));
  }

  public Cohort checkCohortExists(UUID id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Cohort not found: " + id));
  }
}
