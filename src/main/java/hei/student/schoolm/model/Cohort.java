package hei.student.schoolm.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cohort {
  private UUID id;
  private String ref;
  private int entryYear;
  private Instant createdAt;
  private Instant updatedAt;
}
