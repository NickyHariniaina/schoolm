package hei.student.schoolm.service;

import hei.student.schoolm.dto.CohortDto;
import hei.student.schoolm.dto.CohortRequest;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Cohort;
import hei.student.schoolm.repository.CohortRepository;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CohortService {
  private final CohortRepository repository;

  @Transactional(readOnly = true)
  public List<CohortDto> getCohorts() {
    return repository.findAll().stream()
        .sorted(
            Comparator.comparing((Cohort cohort) -> cohort.getEntryYear())
                .reversed()
                .thenComparing(Cohort::getRef))
        .map(this::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public CohortDto getById(UUID id) {
    return toDto(getEntityOrThrow(id));
  }

  @Transactional
  public CohortDto upsert(CohortRequest request) {
    var cohort =
        request.id() == null
            ? Cohort.builder().id(UUID.randomUUID()).build()
            : getEntityOrThrow(request.id());
    cohort.setRef(request.ref().toUpperCase());
    cohort.setEntryYear(Year.of(request.entryYear()));

    var saved = repository.save(cohort);
    return toDto(saved);
  }

  public Cohort getEntityOrThrow(UUID id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Cohort not found: " + id));
  }

  private CohortDto toDto(Cohort cohort) {
    var entryYear = cohort.getEntryYear().getValue();
    return CohortDto.builder()
        .id(cohort.getId())
        .ref(cohort.getRef())
        .entryYear(entryYear)
        .hasGraduates((entryYear + 3) <= Year.now().getValue())
        .build();
  }
}
