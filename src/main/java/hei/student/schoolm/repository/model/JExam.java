package hei.student.schoolm.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
@Table(name = "exam")
public class JExam {
  @Id private String id;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "course_id", nullable = false)
  private JCourse course;

  @NotNull
  @Column(nullable = false)
  private LocalDate dateExam;

  @Positive
  @Column(nullable = false)
  private int coefNumerator;

  @Positive
  @Column(nullable = false)
  private int coefDenominator;

  @OneToMany(mappedBy = "exam")
  private List<JGrade> grades = new ArrayList<>();

  @CreationTimestamp
  @Column(updatable = false)
  private Instant createdAt;

  @UpdateTimestamp @Column private Instant updatedAt;
}
