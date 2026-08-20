package hei.student.schoolm.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hei.student.schoolm.conf.FacadeIT;
import hei.student.schoolm.dto.GroupRequest;
import hei.student.schoolm.dto.GroupResponse;
import hei.student.schoolm.dto.StudentResponse;
import hei.student.schoolm.endpoint.rest.security.JwtService;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.model.User.Role;
import hei.student.schoolm.repository.jpa.JAdminRepository;
import hei.student.schoolm.repository.jpa.JCohortRepository;
import hei.student.schoolm.repository.jpa.JGroupRepository;
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

class GroupIT extends FacadeIT {

  private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

  @Autowired private JAdminRepository adminRepository;
  @Autowired private JCohortRepository cohortRepository;
  @Autowired private JGroupRepository groupRepository;
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
    return "group-" + SEQUENCE.incrementAndGet() + "-" + UUID.randomUUID() + "@hei.school";
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
    return jwtService.generateToken(UUID.randomUUID(), uniqueEmail(), Role.STUDENT);
  }

  private JCohort saveCohort(String ref, int entryYear) {
    return cohortRepository.save(
        JCohort.builder().id(UUID.randomUUID()).ref(ref).entryYear(entryYear).build());
  }

  private GroupResponse createGroup(String token, UUID cohortId, String ref, Track track) {
    return webTestClient
        .put()
        .uri("/groups")
        .header("Authorization", "Bearer " + token)
        .bodyValue(GroupRequest.builder().cohortId(cohortId).ref(ref).track(track).build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(GroupResponse.class)
        .returnResult()
        .getResponseBody();
  }

  @Test
  void adminCanCreateAGroup() {
    var admin = saveAdmin();
    var cohort = saveCohort("GROUP-COHORT-2025", 2025);

    var group = createGroup(adminToken(admin), cohort.getId(), "GRP-A", Track.EL);

    assertNotNull(group);
    assertNotNull(group.id());
    assertEquals(cohort.getId(), group.cohortId());
    assertEquals(cohort.getRef(), group.cohortRef());
    assertEquals("GRP-A", group.ref());
    assertEquals(Track.EL, group.track());
  }

  @Test
  void groupRefIsUppercased() {
    var admin = saveAdmin();
    var cohort = saveCohort("GROUP-COHORT-2025", 2025);

    var group = createGroup(adminToken(admin), cohort.getId(), "grp-b", Track.TN);

    assertEquals("GRP-B", group.ref());
  }

  @Test
  void trackIsPreservedOnUpdate() {
    var admin = saveAdmin();
    var cohort = saveCohort("GROUP-COHORT-2025", 2025);
    var created = createGroup(adminToken(admin), cohort.getId(), "GRP-C", Track.EL);

    var updated =
        webTestClient
            .put()
            .uri("/groups")
            .header("Authorization", "Bearer " + adminToken(admin))
            .bodyValue(
                GroupRequest.builder()
                    .id(created.id())
                    .cohortId(cohort.getId())
                    .ref("GRP-C")
                    .track(Track.EL)
                    .build())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(GroupResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(updated);
    assertEquals(created.id(), updated.id());
    assertEquals(Track.EL, updated.track());
    assertEquals("GRP-C", updated.ref());
  }

  @Test
  void adminCanListGroupsFilteredByCohort() {
    var admin = saveAdmin();
    var cohortA = saveCohort("COHORT-A", 2025);
    var cohortB = saveCohort("COHORT-B", 2026);
    createGroup(adminToken(admin), cohortA.getId(), "GRP-A1", Track.EL);
    createGroup(adminToken(admin), cohortA.getId(), "GRP-A2", Track.TN);
    createGroup(adminToken(admin), cohortB.getId(), "GRP-B1", Track.EL);

    var groups =
        webTestClient
            .get()
            .uri("/groups?cohortId=" + cohortA.getId())
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new ParameterizedTypeReference<List<GroupResponse>>() {})
            .returnResult()
            .getResponseBody();

    assertNotNull(groups);
    assertEquals(2, groups.size());
    assertTrue(groups.stream().allMatch(g -> g.cohortId().equals(cohortA.getId())));
  }

  @Test
  void adminCanReadAGroupById() {
    var admin = saveAdmin();
    var cohort = saveCohort("COHORT-2025", 2025);
    var created = createGroup(adminToken(admin), cohort.getId(), "GRP-D", Track.TN);

    var fetched =
        webTestClient
            .get()
            .uri("/groups/" + created.id())
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(GroupResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(fetched);
    assertEquals(created.id(), fetched.id());
  }

  @Test
  void readingAnUnknownGroupReturnsNotFound() {
    var admin = saveAdmin();

    webTestClient
        .get()
        .uri("/groups/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void adminCanListStudentsOfAGroup() {
    var admin = saveAdmin();
    var cohort = saveCohort("COHORT-2025", 2025);
    var group = createGroup(adminToken(admin), cohort.getId(), "GRP-E", Track.EL);

    var studentId = UUID.randomUUID();
    var jGroup = groupRepository.findById(group.id()).orElseThrow();
    studentRepository.save(
        hei.student.schoolm.repository.model.JStudent.builder()
            .id(studentId)
            .firstName("Alan")
            .lastName("Turing")
            .email(uniqueEmail())
            .password(passwordEncoder.encode("secret123"))
            .role(Role.STUDENT)
            .reference("STD25001")
            .group(jGroup)
            .build());

    var students =
        webTestClient
            .get()
            .uri("/groups/" + group.id() + "/students")
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new ParameterizedTypeReference<List<StudentResponse>>() {})
            .returnResult()
            .getResponseBody();

    assertNotNull(students);
    assertEquals(1, students.size());
    assertEquals(studentId, students.get(0).id());
  }

  @Test
  void studentCannotCreateAGroup() {
    var cohort = saveCohort("COHORT-2025", 2025);

    webTestClient
        .put()
        .uri("/groups")
        .header("Authorization", "Bearer " + studentToken())
        .bodyValue(
            GroupRequest.builder().cohortId(cohort.getId()).ref("GRP-X").track(Track.EL).build())
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void unauthenticatedGroupCreationIsRejected() {
    var cohort = saveCohort("COHORT-2025", 2025);

    webTestClient
        .put()
        .uri("/groups")
        .bodyValue(
            GroupRequest.builder().cohortId(cohort.getId()).ref("GRP-X").track(Track.EL).build())
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }
}
