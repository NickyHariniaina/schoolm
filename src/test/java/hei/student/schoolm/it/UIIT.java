package hei.student.schoolm.it;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hei.student.schoolm.conf.FacadeIT;
import hei.student.schoolm.endpoint.rest.security.JwtAuthenticationFilter;
import hei.student.schoolm.model.User.Role;
import hei.student.schoolm.repository.jpa.JAdminRepository;
import hei.student.schoolm.repository.model.JAdmin;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

class UIIT extends FacadeIT {

  private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

  @Autowired private JAdminRepository adminRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  @LocalServerPort int port;
  private WebTestClient webTestClient;

  @BeforeEach
  void setUp() {
    webTestClient =
        WebTestClient.bindToServer()
            .baseUrl("http://localhost:" + port)
            .responseTimeout(Duration.ofSeconds(30))
            .build();
    adminRepository.deleteAll();
  }

  private JAdmin saveAdmin() {
    return adminRepository.save(
        JAdmin.builder()
            .id(UUID.randomUUID())
            .firstName("Ada")
            .lastName("Lovelace")
            .email("ui-" + SEQUENCE.incrementAndGet() + "@hei.school")
            .password(passwordEncoder.encode("secret123"))
            .role(Role.ADMIN)
            .build());
  }

  @Test
  void loginPageIsPublic() {
    webTestClient
        .get()
        .uri("/ui/login")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .value(body -> assertTrue(body.contains("Connexion")))
        .value(body -> assertTrue(body.contains("action=\"/ui/login\"")));
  }

  @Test
  void cohortsPageRequiresAuthentication() {
    webTestClient
        .get()
        .uri("/")
        .exchange()
        .expectStatus()
        .isUnauthorized()
        .expectHeader()
        .valueEquals("WWW-Authenticate", "Basic realm=\"hei\"");
  }

  @Test
  void loginSetsCookieAndRedirectsToCohorts() {
    var admin = saveAdmin();

    var response =
        webTestClient
            .post()
            .uri("/ui/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("email=" + admin.getEmail() + "&password=secret123")
            .exchange()
            .expectStatus()
            .is3xxRedirection()
            .expectHeader()
            .valueEquals(HttpHeaders.LOCATION, "/")
            .returnResult(byte[].class);

    var setCookie = response.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
    assertNotNull(setCookie);
    assertTrue(setCookie.startsWith(JwtAuthenticationFilter.TOKEN_COOKIE + "="));

    var token =
        setCookie
            .substring(
                (JwtAuthenticationFilter.TOKEN_COOKIE + "=").length(), setCookie.indexOf(";"))
            .trim();
    assertTrue(token.length() > 0);

    webTestClient
        .get()
        .uri("/")
        .cookie(JwtAuthenticationFilter.TOKEN_COOKIE, token)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .value(body -> assertTrue(body.contains("Liste des promotions")));
  }

  @Test
  void loginWithWrongCredentialsRendersError() {
    saveAdmin();

    webTestClient
        .post()
        .uri("/ui/login")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .bodyValue("email=admin@hei.school&password=wrong-password")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .value(body -> assertTrue(body.contains("Connexion")))
        .value(body -> assertTrue(body.contains("Invalid credentials")));
  }

  @Test
  void logoutClearsCookieAndRevokesAccess() {
    var admin = saveAdmin();

    var login =
        webTestClient
            .post()
            .uri("/ui/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("email=" + admin.getEmail() + "&password=secret123")
            .exchange()
            .expectStatus()
            .is3xxRedirection()
            .returnResult(byte[].class);
    var setCookie = login.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
    var token =
        setCookie
            .substring(
                (JwtAuthenticationFilter.TOKEN_COOKIE + "=").length(), setCookie.indexOf(";"))
            .trim();

    webTestClient
        .get()
        .uri("/")
        .cookie(JwtAuthenticationFilter.TOKEN_COOKIE, token)
        .exchange()
        .expectStatus()
        .isOk();

    webTestClient
        .post()
        .uri("/ui/logout")
        .exchange()
        .expectStatus()
        .is3xxRedirection()
        .expectHeader()
        .valueEquals(HttpHeaders.LOCATION, "/ui/login");

    webTestClient
        .get()
        .uri("/")
        .cookie(JwtAuthenticationFilter.TOKEN_COOKIE, token)
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void basicAuthGrantsAccessToCohortsPage() {
    var admin = saveAdmin();

    webTestClient
        .get()
        .uri("/")
        .headers(headers -> headers.setBasicAuth(admin.getEmail(), "secret123"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .value(body -> assertTrue(body.contains("Liste des promotions")));
  }
}
