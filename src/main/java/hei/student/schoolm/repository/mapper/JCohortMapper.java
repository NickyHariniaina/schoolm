package hei.student.schoolm.repository.mapper;

import hei.student.schoolm.model.Cohort;
import hei.student.schoolm.repository.model.JCohort;
import java.time.Year;
import org.springframework.stereotype.Component;

@Component
public class JCohortMapper {
  public Cohort toDomain(JCohort jCohort) {
    return Cohort.builder()
        .id(jCohort.getId())
        .ref(jCohort.getRef())
        .entryYear(Year.of(jCohort.getEntryYear()))
        .createdAt(jCohort.getCreatedAt())
        .updatedAt(jCohort.getUpdatedAt())
        .build();
  }
}
