package hei.student.schoolm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.schoolm.dto.LoginRequest;
import hei.student.schoolm.endpoint.rest.security.JwtService;
import hei.student.schoolm.exception.UnauthorizedException;
import hei.student.schoolm.model.AuthUser;
import hei.student.schoolm.model.User.Role;
import hei.student.schoolm.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  @Mock private UserRepository userRepository;
  @Mock private JwtService jwtService;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private AuthService authService;

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final String EMAIL = "admin@hei.school";
  private static final String PASSWORD = "password";

  @Test
  void should_login_with_valid_credentials() {
    var user = new AuthUser(USER_ID, "Admin", "Principal", EMAIL, "encoded", Role.ADMIN);
    when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(PASSWORD, "encoded")).thenReturn(true);
    when(jwtService.generateToken(USER_ID, EMAIL, Role.ADMIN)).thenReturn("token");

    var result = authService.login(new LoginRequest(EMAIL, PASSWORD));

    assertEquals("token", result.token());
    assertEquals(USER_ID, result.user().id());
    assertEquals(EMAIL, result.user().email());
    assertEquals(Role.ADMIN, result.user().role());
    verify(jwtService).generateToken(USER_ID, EMAIL, Role.ADMIN);
  }

  @Test
  void should_throw_when_email_unknown() {
    when(userRepository.findByEmailIgnoreCase("nobody@hei.school")).thenReturn(Optional.empty());

    assertThrows(
        UnauthorizedException.class,
        () -> authService.login(new LoginRequest("nobody@hei.school", PASSWORD)));
  }

  @Test
  void should_throw_when_password_wrong() {
    var user = new AuthUser(USER_ID, "Admin", "Principal", EMAIL, "encoded", Role.ADMIN);
    when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

    assertThrows(
        UnauthorizedException.class, () -> authService.login(new LoginRequest(EMAIL, "wrong")));
  }
}
