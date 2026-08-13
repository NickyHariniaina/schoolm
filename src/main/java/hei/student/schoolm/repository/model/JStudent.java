package hei.student.schoolm.repository.model;

import hei.student.schoolm.model.Track;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "student")
public class JStudent extends JUser {
  @Id private String id;

  @Column(nullable = false, unique = true)
  private String reference;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Track track;

  @ManyToOne
  @JoinColumn(name = "group_id")
  private JGroup group;

  @OneToMany(mappedBy = "student")
  private List<JGrade> grades = new ArrayList<>();
}
