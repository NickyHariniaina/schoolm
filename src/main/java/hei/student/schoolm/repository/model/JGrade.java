package hei.student.schoolm.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "grade")
public class JGrade {
  @Id private String id;

  @ManyToOne
  @JoinColumn(name = "student_id", nullable = false)
  private JStudent student;

  @ManyToOne
  @JoinColumn(name = "exam_id", nullable = false)
  private JExam exam;

  @Column(nullable = false)
  private BigDecimal value;

  @Column private String changeReason;

  @CreationTimestamp
  @Column(updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column
  private Instant updatedAt;
}
