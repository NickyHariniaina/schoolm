package hei.student.schoolm.endpoint.rest.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

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
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class JUserDetailsServiceTest {
  @Mock private UserRepository userRepository;

  @InjectMocks private JUserDetailsService userDetailsService;

  @Test
  void should_load_user_by_email() {
    var user =
        new AuthUser(UUID.randomUUID(), "Andry", "Rakoto", "andry@hei.school", "enc", Role.TEACHER);
    when(userRepository.findByEmailIgnoreCase("andry@hei.school")).thenReturn(Optional.of(user));

    var details = userDetailsService.loadUserByUsername("andry@hei.school");

    assertEquals("andry@hei.school", details.getUsername());
    assertEquals("enc", details.getPassword());
    assertEquals(1, details.getAuthorities().size());
    assertEquals("ROLE_TEACHER", details.getAuthorities().iterator().next().getAuthority());
  }

  @Test
  void should_throw_when_email_unknown() {
    when(userRepository.findByEmailIgnoreCase("nobody@hei.school")).thenReturn(Optional.empty());

    assertThrows(
        UsernameNotFoundException.class,
        () -> userDetailsService.loadUserByUsername("nobody@hei.school"));
  }
}
