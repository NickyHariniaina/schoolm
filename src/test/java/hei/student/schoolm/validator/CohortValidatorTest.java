package hei.student.schoolm.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Cohort;
import hei.student.schoolm.repository.CohortRepository;
import java.time.Year;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CohortValidatorTest {
  private static final UUID COHORT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Mock CohortRepository cohortRepository;
  @InjectMocks CohortValidator cohortValidator;

  @Test
  void should_return_cohort_when_it_exists() {
    var cohort = Cohort.builder().id(COHORT_ID).ref("P24").entryYear(Year.of(2024)).build();
    when(cohortRepository.findByRef("P24")).thenReturn(Optional.of(cohort));

    var result = cohortValidator.checkCohortExists("P24");

    assertEquals(cohort, result);
  }

  @Test
  void should_throw_not_found_when_cohort_does_not_exist() {
    when(cohortRepository.findByRef("UNKNOWN")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> cohortValidator.checkCohortExists("UNKNOWN"));
  }
}
