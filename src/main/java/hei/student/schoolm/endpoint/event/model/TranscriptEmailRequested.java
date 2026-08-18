package hei.student.schoolm.endpoint.event.model;

import hei.student.schoolm.model.Semester;
import java.time.Duration;
import java.util.UUID;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class TranscriptEmailRequested extends PojaEvent {
  private UUID studentId;
  private Semester semester;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(45);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }
}
