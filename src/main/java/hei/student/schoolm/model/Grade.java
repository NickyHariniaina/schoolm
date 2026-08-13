package hei.student.schoolm.model;

import java.math.BigDecimal;
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
public class Grade {
  private UUID id;
  private Student student;
  private Exam exam;
  private BigDecimal value;
  private String changeReason;
  private Instant createdAt;
  private Instant updatedAt;
}
