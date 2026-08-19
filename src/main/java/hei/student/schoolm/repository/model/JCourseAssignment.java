package hei.student.schoolm.repository.model;

import hei.student.schoolm.model.Semester;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
@Table(
    name = "course_assignment",
    uniqueConstraints =
        @UniqueConstraint(
            name = "course_assignment_uk",
            columnNames = {"course_id", "group_id", "academic_year", "semester"}))
public class JCourseAssignment {
  @Id private UUID id;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "course_id", nullable = false)
  private JCourse course;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "group_id", nullable = false)
  private JGroup group;

  @ManyToMany
  @JoinTable(
      name = "course_assignment_teacher",
      joinColumns = @JoinColumn(name = "course_assignment_id"),
      inverseJoinColumns = @JoinColumn(name = "teacher_id"))
  @lombok.Builder.Default
  private List<JTeacher> teachers = new ArrayList<>();

  @NotNull
  @Column(name = "academic_year", nullable = false)
  private Integer academicYear;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Semester semester;

  @Positive
  @Column(nullable = false)
  private Integer credits;

  @CreationTimestamp
  @Column(updatable = false)
  private Instant createdAt;

  @UpdateTimestamp @Column private Instant updatedAt;
}
