package hei.student.schoolm.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import hei.student.schoolm.conf.FacadeIT;
import hei.student.schoolm.dto.TeacherRequest;
import hei.student.schoolm.dto.TeacherResponse;
import hei.student.schoolm.endpoint.rest.security.JwtService;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.model.User.Role;
import hei.student.schoolm.repository.jpa.JAdminRepository;
import hei.student.schoolm.repository.jpa.JCohortRepository;
import hei.student.schoolm.repository.jpa.JCourseRepository;
import hei.student.schoolm.repository.jpa.JGroupRepository;
import hei.student.schoolm.repository.jpa.JStudentRepository;
import hei.student.schoolm.repository.jpa.JTeacherRepository;
import hei.student.schoolm.repository.model.JAdmin;
import hei.student.schoolm.repository.model.JCourse;
import hei.student.schoolm.repository.model.JTeacher;
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

class TeacherIT extends FacadeIT {

  private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

  @Autowired private JAdminRepository adminRepository;
  @Autowired private JCohortRepository cohortRepository;
  @Autowired private JGroupRepository groupRepository;
  @Autowired private JCourseRepository courseRepository;
  @Autowired private JTeacherRepository teacherRepository;
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
    return "teacher-" + SEQUENCE.incrementAndGet() + "-" + UUID.randomUUID() + "@hei.school";
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

  private JTeacher saveTeacher() {
    return teacherRepository.save(
        JTeacher.builder()
            .id(UUID.randomUUID())
            .firstName("Grace")
            .lastName("Hopper")
            .email(uniqueEmail())
            .password(passwordEncoder.encode("secret123"))
            .role(Role.TEACHER)
            .build());
  }

  private TeacherResponse createTeacher(String token, String email, String password) {
    return webTestClient
        .put()
        .uri("/teachers")
        .header("Authorization", "Bearer " + token)
        .bodyValue(
            TeacherRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .password(password)
                .build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(TeacherResponse.class)
        .returnResult()
        .getResponseBody();
  }

  @Test
  void adminCanCreateATeacher() {
    var admin = saveAdmin();
    var email = uniqueEmail();

    var teacher = createTeacher(adminToken(admin), email, "password123");

    assertNotNull(teacher.id());
    assertEquals("John", teacher.firstName());
    assertEquals("Doe", teacher.lastName());
    assertEquals(email, teacher.email());
  }

  @Test
  void adminCanUpdateAnExistingTeacher() {
    var admin = saveAdmin();
    var teacher = createTeacher(adminToken(admin), uniqueEmail(), "password123");

    var updated =
        webTestClient
            .put()
            .uri("/teachers")
            .header("Authorization", "Bearer " + adminToken(admin))
            .bodyValue(
                TeacherRequest.builder()
                    .id(teacher.id())
                    .firstName("Jane")
                    .lastName("Smith")
                    .email(teacher.email())
                    .build())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(TeacherResponse.class)
            .returnResult()
            .getResponseBody();

    assertEquals(teacher.id(), updated.id());
    assertEquals("Jane", updated.firstName());
    assertEquals("Smith", updated.lastName());
  }

  @Test
  void creatingATeacherWithoutPasswordIsRejected() {
    var admin = saveAdmin();

    webTestClient
        .put()
        .uri("/teachers")
        .header("Authorization", "Bearer " + adminToken(admin))
        .bodyValue(
            TeacherRequest.builder().firstName("John").lastName("Doe").email(uniqueEmail()).build())
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void adminCanListTeachers() {
    var admin = saveAdmin();
    var emailA = uniqueEmail();
    var emailB = uniqueEmail();
    createTeacher(adminToken(admin), emailA, "password123");
    createTeacher(adminToken(admin), emailB, "password123");

    var teachers =
        webTestClient
            .get()
            .uri("/teachers")
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new ParameterizedTypeReference<List<TeacherResponse>>() {})
            .returnResult()
            .getResponseBody();

    assertNotNull(teachers);
    assertEquals(2, teachers.size());
  }

  @Test
  void adminCanReadATeacherById() {
    var admin = saveAdmin();
    var teacher = createTeacher(adminToken(admin), uniqueEmail(), "password123");

    var fetched =
        webTestClient
            .get()
            .uri("/teachers/" + teacher.id())
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(TeacherResponse.class)
            .returnResult()
            .getResponseBody();

    assertEquals(teacher.id(), fetched.id());
    assertEquals(teacher.email(), fetched.email());
  }

  @Test
  void readingAnUnknownTeacherReturnsNotFound() {
    var admin = saveAdmin();

    webTestClient
        .get()
        .uri("/teachers/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void teacherCanReadTheirOwnProfile() {
    var teacher = saveTeacher();

    var fetched =
        webTestClient
            .get()
            .uri("/teachers/" + teacher.getId())
            .header(
                "Authorization",
                "Bearer "
                    + jwtService.generateToken(teacher.getId(), teacher.getEmail(), Role.TEACHER))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(TeacherResponse.class)
            .returnResult()
            .getResponseBody();

    assertEquals(teacher.getId(), fetched.id());
  }

  @Test
  void studentCannotReadATeacherProfile() {
    var teacher = saveTeacher();

    webTestClient
        .get()
        .uri("/teachers/" + teacher.getId())
        .header("Authorization", "Bearer " + studentToken())
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void teacherCannotCreateATeacher() {
    var teacher = saveTeacher();

    webTestClient
        .put()
        .uri("/teachers")
        .header(
            "Authorization",
            "Bearer " + jwtService.generateToken(teacher.getId(), teacher.getEmail(), Role.TEACHER))
        .bodyValue(
            TeacherRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email(uniqueEmail())
                .password("password123")
                .build())
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void adminCanDeleteATeacher() {
    var admin = saveAdmin();
    var teacher = createTeacher(adminToken(admin), uniqueEmail(), "password123");

    webTestClient
        .delete()
        .uri("/teachers/" + teacher.id())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNoContent();

    webTestClient
        .get()
        .uri("/teachers/" + teacher.id())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void deletingAnUnknownTeacherReturnsNotFound() {
    var admin = saveAdmin();

    webTestClient
        .delete()
        .uri("/teachers/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void teacherCannotDeleteATeacher() {
    var teacher = saveTeacher();

    webTestClient
        .delete()
        .uri("/teachers/" + teacher.getId())
        .header(
            "Authorization",
            "Bearer " + jwtService.generateToken(teacher.getId(), teacher.getEmail(), Role.TEACHER))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void adminCannotDeleteATeacherAssignedToACourse() {
    var admin = saveAdmin();
    var teacher = saveTeacher();
    var course =
        courseRepository.save(
            JCourse.builder()
                .id(UUID.randomUUID())
                .ref("TC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .title("Course")
                .credit(4)
                .track(Track.COMMON)
                .semester(Semester.S1)
                .build());
    course.getTeachers().add(teacher);
    courseRepository.save(course);

    webTestClient
        .delete()
        .uri("/teachers/" + teacher.getId())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isEqualTo(org.springframework.http.HttpStatus.CONFLICT);
  }
}
