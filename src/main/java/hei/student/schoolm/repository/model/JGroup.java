package hei.student.schoolm.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
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
@Table(name = "student_group")
public class JGroup {
  @Id private String id;

  @Column(nullable = false)
  private String ref;

  @ManyToOne
  @JoinColumn(name = "cohort_id", nullable = false)
  private JCohort cohort;

  @OneToMany(mappedBy = "group")
  private List<JStudent> students = new ArrayList<>();

  @ManyToMany(mappedBy = "groups")
  private List<JCourse> courses = new ArrayList<>();

  @CreationTimestamp
  @Column(updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column
  private Instant updatedAt;
}
