package hei.student.schoolm.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
  @Id private UUID id;

  @NotBlank
  @Column(nullable = false, unique = true)
  private String reference;

  @ManyToOne
  @JoinColumn(name = "group_id")
  private JGroup group;

  @OneToMany(mappedBy = "student")
  private List<JGrade> grades = new ArrayList<>();
}
