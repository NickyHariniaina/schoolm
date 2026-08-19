package hei.student.schoolm.repository;

import hei.student.schoolm.model.AuthUser;
import hei.student.schoolm.repository.jpa.JAdminRepository;
import hei.student.schoolm.repository.jpa.JStudentRepository;
import hei.student.schoolm.repository.jpa.JTeacherRepository;
import hei.student.schoolm.repository.model.JAdmin;
import hei.student.schoolm.repository.model.JStudent;
import hei.student.schoolm.repository.model.JTeacher;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class UserRepository {
  private final JAdminRepository jAdminRepository;
  private final JTeacherRepository jTeacherRepository;
  private final JStudentRepository jStudentRepository;

  @Transactional(readOnly = true)
  public Optional<AuthUser> findByEmailIgnoreCase(String email) {
    return jAdminRepository
        .findByEmailIgnoreCase(email)
        .map(this::toAuthUser)
        .or(() -> jTeacherRepository.findByEmailIgnoreCase(email).map(this::toAuthUser))
        .or(() -> jStudentRepository.findByEmailIgnoreCase(email).map(this::toAuthUser));
  }

  private AuthUser toAuthUser(JAdmin user) {
    return new AuthUser(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        user.getPassword(),
        user.getRole());
  }

  private AuthUser toAuthUser(JTeacher user) {
    return new AuthUser(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        user.getPassword(),
        user.getRole());
  }

  private AuthUser toAuthUser(JStudent user) {
    return new AuthUser(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        user.getPassword(),
        user.getRole());
  }
}
