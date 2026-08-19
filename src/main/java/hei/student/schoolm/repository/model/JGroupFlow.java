package hei.student.schoolm.repository.model;

import hei.student.schoolm.model.GroupFlowType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "group_flow")
public class JGroupFlow {
  @Id private UUID id;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "student_id", nullable = false)
  private JStudent student;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "group_id", nullable = false)
  private JGroup group;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "group_flow_type", nullable = false)
  private GroupFlowType groupFlowType;

  @CreationTimestamp
  @Column(updatable = false)
  private Instant createdAt;
}
