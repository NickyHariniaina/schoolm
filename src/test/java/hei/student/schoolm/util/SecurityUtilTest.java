package hei.student.schoolm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hei.student.schoolm.exception.ForbiddenException;
import hei.student.schoolm.exception.UnauthorizedException;
import hei.student.schoolm.model.User.Role;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityUtilTest {
  private final SecurityUtil securityUtil = new SecurityUtil();

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticate(UUID userId, Role role) {
    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    var auth = new UsernamePasswordAuthenticationToken(userId.toString(), null, authorities);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void should_return_current_user_id() {
    var userId = UUID.randomUUID();
    authenticate(userId, Role.ADMIN);

    assertEquals(userId, securityUtil.getCurrentUserIdOrThrow());
  }

  @Test
  void should_return_current_role() {
    authenticate(UUID.randomUUID(), Role.TEACHER);

    assertTrue(securityUtil.isTeacher());
  }

  @Test
  void should_require_self() {
    var userId = UUID.randomUUID();
    authenticate(userId, Role.STUDENT);

    securityUtil.requireSelf(userId);
    assertThrows(ForbiddenException.class, () -> securityUtil.requireSelf(UUID.randomUUID()));
  }

  @Test
  void should_allow_admin_for_anyone() {
    var adminId = UUID.randomUUID();
    authenticate(adminId, Role.ADMIN);

    securityUtil.requireSelfOrAdmin(UUID.randomUUID());
    securityUtil.requireSelfOrStaff(UUID.randomUUID());
  }

  @Test
  void should_allow_staff_to_look_up_students() {
    authenticate(UUID.randomUUID(), Role.TEACHER);

    securityUtil.requireSelfOrStaff(UUID.randomUUID());
  }

  @Test
  void should_throw_when_not_authenticated() {
    assertThrows(UnauthorizedException.class, securityUtil::getCurrentUserIdOrThrow);
  }
}
