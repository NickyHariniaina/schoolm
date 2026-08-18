package hei.student.schoolm.service;

import hei.student.schoolm.dto.CohortDto;
import hei.student.schoolm.repository.CohortRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CohortService {
  private final CohortRepository cohortRepository;

  @Transactional(readOnly = true)
  public List<CohortDto> getCohorts() {
    return cohortRepository.findAll().stream()
        .sorted(
            Comparator.comparing((hei.student.schoolm.model.Cohort cohort) -> cohort.getEntryYear())
                .reversed()
                .thenComparing(hei.student.schoolm.model.Cohort::getRef))
        .map(
            cohort ->
                CohortDto.builder().ref(cohort.getRef()).entryYear(cohort.getEntryYear()).build())
        .toList();
  }
}
