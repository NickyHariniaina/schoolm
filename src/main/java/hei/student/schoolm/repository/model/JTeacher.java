package hei.student.schoolm.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
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
@Table(name = "teacher")
public class JTeacher extends JUser {
  @Id private String id;

  @ManyToMany(mappedBy = "teachers")
  private List<JCourse> courses = new ArrayList<>();
}
