package hei.student.schoolm.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hei.student.schoolm.conf.FacadeIT;
import hei.student.schoolm.dto.CohortDto;
import hei.student.schoolm.dto.CohortRequest;
import hei.student.schoolm.model.User.Role;
import hei.student.schoolm.repository.jpa.JAdminRepository;
import hei.student.schoolm.repository.jpa.JCohortRepository;
import hei.student.schoolm.repository.jpa.JStudentRepository;
import hei.student.schoolm.repository.model.JAdmin;
import hei.student.schoolm.repository.model.JCohort;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import hei.student.schoolm.endpoint.rest.security.JwtService;

class CohortIT extends FacadeIT {

  private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

  @Autowired private JAdminRepository adminRepository;
  @Autowired private JCohortRepository cohortRepository;
  @Autowired private JStudentRepository studentRepository;
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
    jdbcTemplate.execute("delete from group_flow");
    jdbcTemplate.execute("delete from grade_history");
    jdbcTemplate.execute("delete from grade");
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
    return "cohort-" + SEQUENCE.incrementAndGet() + "-" + UUID.randomUUID() + "@hei.school";
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

  private String studentToken() {
    var studentId = UUID.randomUUID();
    return jwtService.generateToken(studentId, uniqueEmail(), Role.STUDENT);
  }

  private CohortRequest cohortRequest(String ref, int entryYear) {
    return CohortRequest.builder().ref(ref).entryYear(entryYear).build();
  }

  private CohortDto createCohort(String token, String ref, int entryYear) {
    return webTestClient
        .put()
        .uri("/cohorts")
        .header("Authorization", "Bearer " + token)
        .bodyValue(cohortRequest(ref, entryYear))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(CohortDto.class)
        .returnResult()
        .getResponseBody();
  }

  @Test
  void adminCanCreateACohort() {
    var admin = saveAdmin();

    var cohort = createCohort(adminToken(admin), "COHORT-2025", 2025);

    assertNotNull(cohort);
    assertNotNull(cohort.getId());
    assertEquals("COHORT-2025", cohort.getRef());
    assertEquals(2025, cohort.getEntryYear());
  }

  @Test
  void cohortRefIsUppercased() {
    var admin = saveAdmin();

    var cohort = createCohort(adminToken(admin), "cohort-2025", 2025);

    assertEquals("COHORT-2025", cohort.getRef());
  }

  @Test
  void adminCanUpdateACohort() {
    var admin = saveAdmin();
    var created = createCohort(adminToken(admin), "COHORT-2025", 2025);

    var updated =
        webTestClient
            .put()
            .uri("/cohorts")
            .header("Authorization", "Bearer " + adminToken(admin))
            .bodyValue(CohortRequest.builder().id(created.getId()).ref("COHORT-2026").entryYear(2026).build())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(CohortDto.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(updated);
    assertEquals(created.getId(), updated.getId());
    assertEquals("COHORT-2026", updated.getRef());
    assertEquals(2026, updated.getEntryYear());
  }

  @Test
  void adminCanListCohorts() {
    var admin = saveAdmin();
    createCohort(adminToken(admin), "COHORT-A", 2025);
    createCohort(adminToken(admin), "COHORT-B", 2026);

    var cohorts =
        webTestClient
            .get()
            .uri("/cohorts")
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new ParameterizedTypeReference<List<CohortDto>>() {})
            .returnResult()
            .getResponseBody();

    assertNotNull(cohorts);
    assertEquals(2, cohorts.size());
  }

  @Test
  void adminCanReadACohortById() {
    var admin = saveAdmin();
    var created = createCohort(adminToken(admin), "COHORT-2025", 2025);

    var fetched =
        webTestClient
            .get()
            .uri("/cohorts/" + created.getId())
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(CohortDto.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(fetched);
    assertEquals(created.getId(), fetched.getId());
    assertEquals("COHORT-2025", fetched.getRef());
  }

  @Test
  void readingAnUnknownCohortReturnsNotFound() {
    var admin = saveAdmin();

    webTestClient
        .get()
        .uri("/cohorts/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void hasGraduatesIsTrueForCohortsThatFinished() {
    var admin = saveAdmin();
    var finished = createCohort(adminToken(admin), "COHORT-OLD", 2000);
    var ongoing = createCohort(adminToken(admin), "COHORT-NEW", 2027);

    assertTrue(finished.isHasGraduates());
    assertFalse(ongoing.isHasGraduates());
  }

  @Test
  void unauthenticatedCohortCreationIsRejected() {
    webTestClient
        .put()
        .uri("/cohorts")
        .bodyValue(cohortRequest("COHORT-2025", 2025))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void studentCannotCreateACohort() {
    webTestClient
        .put()
        .uri("/cohorts")
        .header("Authorization", "Bearer " + studentToken())
        .bodyValue(cohortRequest("COHORT-2025", 2025))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void studentCanListCohorts() {
    var admin = saveAdmin();
    createCohort(adminToken(admin), "COHORT-2025", 2025);

    webTestClient
        .get()
        .uri("/cohorts")
        .header("Authorization", "Bearer " + studentToken())
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void persistedCohortCanBeFoundByRef() {
    var admin = saveAdmin();
    createCohort(adminToken(admin), "COHORT-REFR", 2025);

    var found = cohortRepository.findByRef("COHORT-REFR");

    assertTrue(found.isPresent());
    assertEquals("COHORT-REFR", found.get().getRef());
  }
}