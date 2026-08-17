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
public class GradeHistoryDto {
  private UUID id;
  private UUID gradeId;
  private UUID studentId;
  private UUID examId;
  private BigDecimal oldValue;
  private BigDecimal newValue;
  private String changeReason;
  private Instant changedAt;
}
