package hei.student.schoolm.repository.model;

import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Track;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
@Table(name = "course")
public class JCourse {
  @Id private String id;

  @Column(nullable = false, unique = true)
  private String ref;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private int credit;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Track track;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Semester semester;

  @OneToMany(mappedBy = "course")
  private List<JExam> exams = new ArrayList<>();

  @ManyToMany
  @JoinTable(
      name = "course_group",
      joinColumns = @JoinColumn(name = "course_id"),
      inverseJoinColumns = @JoinColumn(name = "group_id"))
  private List<JGroup> groups = new ArrayList<>();

  @ManyToMany
  @JoinTable(
      name = "course_teacher",
      joinColumns = @JoinColumn(name = "course_id"),
      inverseJoinColumns = @JoinColumn(name = "teacher_id"))
  private List<JTeacher> teachers = new ArrayList<>();

  @CreationTimestamp
  @Column(updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column
  private Instant updatedAt;
}
