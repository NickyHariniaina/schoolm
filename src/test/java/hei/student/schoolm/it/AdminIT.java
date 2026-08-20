package hei.student.schoolm.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import hei.student.schoolm.conf.FacadeIT;
import hei.student.schoolm.dto.AdminRequest;
import hei.student.schoolm.dto.AdminResponse;
import hei.student.schoolm.endpoint.rest.security.JwtService;
import hei.student.schoolm.model.User.Role;
import hei.student.schoolm.repository.jpa.JAdminRepository;
import hei.student.schoolm.repository.jpa.JTeacherRepository;
import hei.student.schoolm.repository.model.JAdmin;
import hei.student.schoolm.repository.model.JTeacher;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

class AdminIT extends FacadeIT {

  private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

  @Autowired private JAdminRepository adminRepository;
  @Autowired private JTeacherRepository teacherRepository;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private JwtService jwtService;

  @LocalServerPort int port;
  private WebTestClient webTestClient;

  @BeforeEach
  void setUp() {
    webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    teardown();
  }

  private void teardown() {
    jdbcTemplate.execute("delete from grade_history");
    jdbcTemplate.execute("delete from grade");
    jdbcTemplate.execute("delete from group_flow");
    jdbcTemplate.execute("delete from course_assignment_teacher");
    jdbcTemplate.execute("delete from course_assignment");
    jdbcTemplate.execute("delete from exam");
    jdbcTemplate.execute("delete from course_teacher");
    jdbcTemplate.execute("delete from course_group");
    jdbcTemplate.execute("delete from course");
    jdbcTemplate.execute("delete from student");
    jdbcTemplate.execute("delete from teacher");
    jdbcTemplate.execute("delete from \"group\"");
    jdbcTemplate.execute("delete from cohort");
    jdbcTemplate.execute("delete from admin");
  }

  private String uniqueEmail() {
    return "admin-" + SEQUENCE.incrementAndGet() + "-" + UUID.randomUUID() + "@hei.school";
  }

  private JAdmin saveAdmin() {
    return adminRepository.save(
        JAdmin.builder()
            .id(UUID.randomUUID())
            .firstName("Ada")
            .lastName("Lovelace")
            .email(uniqueEmail())
            .password(passwordEncoder.encode("secret123"))
            .role(Role.ADMIN)
            .build());
  }

  private String adminToken(JAdmin admin) {
    return jwtService.generateToken(admin.getId(), admin.getEmail(), Role.ADMIN);
  }

  private String teacherToken() {
    var teacher =
        teacherRepository.save(
            JTeacher.builder()
                .id(UUID.randomUUID())
                .firstName("Grace")
                .lastName("Hopper")
                .email(uniqueEmail())
                .password(passwordEncoder.encode("secret123"))
                .role(Role.TEACHER)
                .build());
    return jwtService.generateToken(teacher.getId(), teacher.getEmail(), Role.TEACHER);
  }

  private AdminRequest adminRequest(String firstname, String lastname, String email) {
    return AdminRequest.builder().firstname(firstname).lastname(lastname).email(email).build();
  }

  @Test
  void adminCanReadTheirOwnProfile() {
    var admin = saveAdmin();

    var fetched =
        webTestClient
            .get()
            .uri("/admins/" + admin.getId())
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AdminResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(fetched);
    assertEquals(admin.getId(), fetched.id());
    assertEquals(admin.getEmail(), fetched.email());
  }

  @Test
  void adminCanUpdateOwnProfile() {
    var admin = saveAdmin();

    var updated =
        webTestClient
            .put()
            .uri("/admins/" + admin.getId())
            .header("Authorization", "Bearer " + adminToken(admin))
            .bodyValue(adminRequest("Grace", "Hopper", uniqueEmail()))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AdminResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(updated);
    assertEquals(admin.getId(), updated.id());
    assertEquals("Grace", updated.firstName());
    assertEquals("Hopper", updated.lastName());
  }

  @Test
  void adminCannotReadAnotherAdminsProfile() {
    var adminA = saveAdmin();
    var adminB = saveAdmin();

    webTestClient
        .get()
        .uri("/admins/" + adminB.getId())
        .header("Authorization", "Bearer " + adminToken(adminA))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void adminCannotUpdateAnotherAdminsProfile() {
    var adminA = saveAdmin();
    var adminB = saveAdmin();

    webTestClient
        .put()
        .uri("/admins/" + adminB.getId())
        .header("Authorization", "Bearer " + adminToken(adminA))
        .bodyValue(adminRequest("Grace", "Hopper", uniqueEmail()))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void teacherCannotReadAdminProfile() {
    var admin = saveAdmin();

    webTestClient
        .get()
        .uri("/admins/" + admin.getId())
        .header("Authorization", "Bearer " + teacherToken())
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void readingAnUnknownAdminReturnsNotFound() {
    var admin = saveAdmin();

    webTestClient
        .get()
        .uri("/admins/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void unauthenticatedAccessIsRejected() {
    var admin = saveAdmin();

    webTestClient.get().uri("/admins/" + admin.getId()).exchange().expectStatus().isUnauthorized();
  }
}
