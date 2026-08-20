package hei.student.schoolm.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import hei.student.schoolm.conf.FacadeIT;
import hei.student.schoolm.dto.ExamDto;
import hei.student.schoolm.dto.ExamRequest;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.model.User.Role;
import hei.student.schoolm.repository.jpa.JAdminRepository;
import hei.student.schoolm.repository.jpa.JCohortRepository;
import hei.student.schoolm.repository.jpa.JCourseRepository;
import hei.student.schoolm.repository.jpa.JExamRepository;
import hei.student.schoolm.repository.jpa.JGroupRepository;
import hei.student.schoolm.repository.jpa.JStudentRepository;
import hei.student.schoolm.repository.jpa.JTeacherRepository;
import hei.student.schoolm.repository.model.JAdmin;
import hei.student.schoolm.repository.model.JCourse;
import hei.student.schoolm.repository.model.JExam;
import hei.student.schoolm.repository.model.JGroup;
import hei.student.schoolm.repository.model.JTeacher;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import hei.student.schoolm.endpoint.rest.security.JwtService;

class ExamIT extends FacadeIT {

  private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

  @Autowired private JAdminRepository adminRepository;
  @Autowired private JCohortRepository cohortRepository;
  @Autowired private JGroupRepository groupRepository;
  @Autowired private JCourseRepository courseRepository;
  @Autowired private JExamRepository examRepository;
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
    return "exam-" + SEQUENCE.incrementAndGet() + "-" + UUID.randomUUID() + "@hei.school";
  }

  private String uniqueRef() {
    return "EXC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
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

  private String studentToken() {
    return jwtService.generateToken(UUID.randomUUID(), uniqueEmail(), Role.STUDENT);
  }

  private JCourse saveCourse() {
    return courseRepository.save(
        JCourse.builder()
            .id(UUID.randomUUID())
            .ref(uniqueRef())
            .title("Course")
            .credit(4)
            .track(Track.COMMON)
            .semester(Semester.S1)
            .build());
  }

  private ExamRequest examRequest(UUID courseId, int coefNum, int coefDen) {
    return ExamRequest.builder()
        .courseId(courseId)
        .dateExam(LocalDate.of(2025, 6, 1))
        .coefNumerator(coefNum)
        .coefDenominator(coefDen)
        .build();
  }

  @Test
  void adminCanCreateAnExam() {
    var admin = saveAdmin();
    var course = saveCourse();

    var exam =
        webTestClient
            .put()
            .uri("/exams")
            .header("Authorization", "Bearer " + adminToken(admin))
            .bodyValue(examRequest(course.getId(), 1, 2))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ExamDto.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(exam);
    assertNotNull(exam.id());
    assertEquals(course.getId(), exam.courseId());
    assertEquals(1, exam.coefNumerator());
    assertEquals(2, exam.coefDenominator());
  }

  @Test
  void adminCanUpdateAnExistingExam() {
    var admin = saveAdmin();
    var course = saveCourse();
    var created =
        webTestClient
            .put()
            .uri("/exams")
            .header("Authorization", "Bearer " + adminToken(admin))
            .bodyValue(examRequest(course.getId(), 1, 2))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ExamDto.class)
            .returnResult()
            .getResponseBody();

    var updated =
        webTestClient
            .put()
            .uri("/exams")
            .header("Authorization", "Bearer " + adminToken(admin))
            .bodyValue(
                ExamRequest.builder()
                    .id(created.id())
                    .courseId(course.getId())
                    .dateExam(LocalDate.of(2025, 6, 15))
                    .coefNumerator(1)
                    .coefDenominator(2)
                    .build())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ExamDto.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(updated);
    assertEquals(created.id(), updated.id());
    assertEquals(LocalDate.of(2025, 6, 15), updated.dateExam());
  }

  @Test
  void adminCanReadAnExamById() {
    var admin = saveAdmin();
    var course = saveCourse();
    var created =
        webTestClient
            .put()
            .uri("/exams")
            .header("Authorization", "Bearer " + adminToken(admin))
            .bodyValue(examRequest(course.getId(), 1, 2))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ExamDto.class)
            .returnResult()
            .getResponseBody();

    var fetched =
        webTestClient
            .get()
            .uri("/exams/" + created.id())
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ExamDto.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(fetched);
    assertEquals(created.id(), fetched.id());
  }

  @Test
  void readingAnUnknownExamReturnsNotFound() {
    var admin = saveAdmin();

    webTestClient
        .get()
        .uri("/exams/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void adminCanDeleteAnExam() {
    var admin = saveAdmin();
    var course = saveCourse();
    var created =
        webTestClient
            .put()
            .uri("/exams")
            .header("Authorization", "Bearer " + adminToken(admin))
            .bodyValue(examRequest(course.getId(), 1, 2))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ExamDto.class)
            .returnResult()
            .getResponseBody();

    webTestClient
        .delete()
        .uri("/exams/" + created.id())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNoContent();

    webTestClient
        .get()
        .uri("/exams/" + created.id())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void deletingAnUnknownExamReturnsNotFound() {
    var admin = saveAdmin();

    webTestClient
        .delete()
        .uri("/exams/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void studentCannotCreateAnExam() {
    var course = saveCourse();

    webTestClient
        .put()
        .uri("/exams")
        .header("Authorization", "Bearer " + studentToken())
        .bodyValue(examRequest(course.getId(), 1, 2))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void teacherCannotCreateAnExam() {
    var course = saveCourse();

    webTestClient
        .put()
        .uri("/exams")
        .header("Authorization", "Bearer " + teacherToken())
        .bodyValue(examRequest(course.getId(), 1, 2))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void creatingAnExamWithZeroDenominatorIsRejected() {
    var admin = saveAdmin();
    var course = saveCourse();

    webTestClient
        .put()
        .uri("/exams")
        .header("Authorization", "Bearer " + adminToken(admin))
        .bodyValue(examRequest(course.getId(), 1, 0))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void teacherCanReadExamOfCourseTheyTeach() {
    var teacher = teacherRepository.save(
            JTeacher.builder()
                .id(UUID.randomUUID())
                .firstName("Grace")
                .lastName("Hopper")
                .email(uniqueEmail())
                .password(passwordEncoder.encode("secret123"))
                .role(Role.TEACHER)
                .build());
    var course = saveCourse();
    course.getTeachers().add(teacher);
    courseRepository.save(course);
    var exam =
        webTestClient
            .put()
            .uri("/exams")
            .header("Authorization", "Bearer " + adminToken(saveAdmin()))
            .bodyValue(examRequest(course.getId(), 1, 2))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ExamDto.class)
            .returnResult()
            .getResponseBody();

    webTestClient
        .get()
        .uri("/exams/" + exam.id())
        .header("Authorization", "Bearer " + teacherToken())
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void studentCannotReadExamGrades() {
    var admin = saveAdmin();
    var course = saveCourse();
    var exam =
        webTestClient
            .put()
            .uri("/exams")
            .header("Authorization", "Bearer " + adminToken(admin))
            .bodyValue(examRequest(course.getId(), 1, 2))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ExamDto.class)
            .returnResult()
            .getResponseBody();

    webTestClient
        .get()
        .uri("/exams/" + exam.id() + "/grades")
        .header("Authorization", "Bearer " + studentToken())
        .exchange()
        .expectStatus()
        .isForbidden();
  }
}