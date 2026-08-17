package hei.student.schoolm.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "grade_history")
public class JGradeHistory {
  @Id private UUID id;

  @NotNull
  @Column(name = "grade_id", nullable = false)
  private UUID gradeId;

  @NotNull
  @Column(name = "student_id", nullable = false)
  private UUID studentId;

  @NotNull
  @Column(name = "exam_id", nullable = false)
  private UUID examId;

  @NotNull
  @DecimalMin("0.0")
  @DecimalMax("20.0")
  @Column(name = "old_value", nullable = false)
  private BigDecimal oldValue;

  @NotNull
  @DecimalMin("0.0")
  @DecimalMax("20.0")
  @Column(name = "new_value", nullable = false)
  private BigDecimal newValue;

  @NotNull
  @Column(name = "change_reason", nullable = false)
  private String changeReason;

  @CreationTimestamp
  @Column(name = "changed_at", updatable = false)
  private Instant changedAt;
}
