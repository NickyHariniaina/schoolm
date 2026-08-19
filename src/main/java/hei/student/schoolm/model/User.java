package hei.student.schoolm.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract sealed class User permits Student, Teacher, Admin {
  public enum Role {
    STUDENT,
    TEACHER,
    ADMIN
  }

  private UUID id;
  private String email;
  private String firstName;
  private String lastName;
  private Role role;
  private String password;
  private Instant createdAt;
  private Instant updatedAt;
}
