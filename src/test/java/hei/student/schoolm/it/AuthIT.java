package hei.student.schoolm.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import hei.student.schoolm.conf.FacadeIT;
import hei.student.schoolm.dto.AuthResponse;
import hei.student.schoolm.dto.LoginRequest;
import hei.student.schoolm.model.User.Role;
import hei.student.schoolm.repository.jpa.JAdminRepository;
import hei.student.schoolm.repository.model.JAdmin;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

class AuthIT extends FacadeIT {

  private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

  @Autowired private JAdminRepository adminRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private hei.student.schoolm.endpoint.rest.security.JwtService jwtService;

  @LocalServerPort int port;
  private WebTestClient webTestClient;

  @BeforeEach
  void setUp() {
    webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
  }

  private String uniqueEmail(String prefix) {
    return prefix + "-" + SEQUENCE.incrementAndGet() + "-" + UUID.randomUUID() + "@hei.school";
  }

  private JAdmin saveAdmin(String email, String rawPassword) {
    return adminRepository.save(
        JAdmin.builder()
            .id(UUID.randomUUID())
            .firstName("Ada")
            .lastName("Lovelace")
            .email(email)
            .password(passwordEncoder.encode(rawPassword))
            .role(Role.ADMIN)
            .build());
  }

  @Test
  void pingShouldBePublic() {
    webTestClient.get().uri("/ping").exchange().expectStatus().isOk();
  }

  @Test
  void shouldLoginWithValidCredentials() {
    var email = uniqueEmail("admin");
    saveAdmin(email, "secret123");

    var response =
        webTestClient
            .post()
            .uri("/auth/login")
            .bodyValue(new LoginRequest(email, "secret123"))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AuthResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(response);
    assertNotNull(response.token());
    assertEquals(email, response.user().email());
    assertEquals(Role.ADMIN, response.user().role());
  }

  @Test
  void shouldRejectWrongPassword() {
    var email = uniqueEmail("admin");
    saveAdmin(email, "correct");

    webTestClient
        .post()
        .uri("/auth/login")
        .bodyValue(new LoginRequest(email, "wrong"))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void shouldRejectUnknownEmail() {
    webTestClient
        .post()
        .uri("/auth/login")
        .bodyValue(new LoginRequest("nobody@hei.school", "whatever"))
        .exchange()
        .expectStatus()
        .isUnauthorized()
        .expectBody()
        .jsonPath("$.error")
        .isEqualTo("Invalid credentials");
  }

  @Test
  void shouldRejectRequestsWithoutToken() {
    webTestClient.get().uri("/courses").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void shouldRejectInvalidToken() {
    webTestClient
        .get()
        .uri("/courses")
        .header("Authorization", "Bearer invalid-token")
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void shouldRejectTokenOfUnknownUser() {
    var token = jwtService.generateToken(UUID.randomUUID(), "ghost@hei.school", Role.ADMIN);

    webTestClient
        .get()
        .uri("/courses")
        .header("Authorization", "Bearer " + token)
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void unknownEndpointReturnsNotFoundInsteadOfUnauthorized() {
    webTestClient
        .get()
        .uri("/definitely-not-an-endpoint")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void protectedEndpointStillChallengesWithBasicAuth() {
    webTestClient
        .get()
        .uri("/courses")
        .exchange()
        .expectStatus()
        .isUnauthorized()
        .expectHeader()
        .valueEquals("WWW-Authenticate", "Basic realm=\"hei\"");
  }
}