package hei.student.schoolm.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeDto {
  private UUID id;
  private UUID studentId;
  private UUID examId;
  private BigDecimal value;
  private String changeReason;
  private Instant createdAt;
  private Instant updatedAt;
}
