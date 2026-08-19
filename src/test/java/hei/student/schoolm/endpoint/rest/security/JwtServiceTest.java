package hei.student.schoolm.endpoint.rest.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hei.student.schoolm.model.User.Role;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
  private static final String SECRET =
      "6F6B4E33716B7A2E4D4C2B59334A563866587447624D2A7265324F793F38584E";

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService(SECRET, 3600000);
  }

  @Test
  void should_roundtrip_token() {
    var userId = UUID.randomUUID();
    var email = "student@hei.school";

    var token = jwtService.generateToken(userId, email, Role.STUDENT);
    var claims = jwtService.parseToken(token);

    assertEquals(userId.toString(), claims.getSubject());
    assertEquals(email, claims.get("email", String.class));
    assertEquals(Role.STUDENT.name(), claims.get("role", String.class));
  }

  @Test
  void should_reject_token_signed_with_different_secret() {
    var token =
        new JwtService("SECRET2SECRET2SECRET2SECRET2SECRET2SECRET2SECRET2SECRET2", 3600000)
            .generateToken(UUID.randomUUID(), "x@hei.school", Role.ADMIN);

    assertThrows(Exception.class, () -> jwtService.parseToken(token));
  }
}
