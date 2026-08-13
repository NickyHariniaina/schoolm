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
public class Group {
  private UUID id;
  private String ref;
  private Cohort cohort;
  private Instant createdAt;
  private Instant updatedAt;
}
