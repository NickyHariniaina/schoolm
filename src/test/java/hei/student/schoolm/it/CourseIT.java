package hei.student.schoolm.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hei.student.schoolm.conf.FacadeIT;
import hei.student.schoolm.dto.CourseDto;
import hei.student.schoolm.dto.CourseRequest;
import hei.student.schoolm.dto.ExamDto;
import hei.student.schoolm.dto.GroupIdsRequest;
import hei.student.schoolm.dto.TeacherIdsRequest;
import hei.student.schoolm.endpoint.rest.security.JwtService;
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
import hei.student.schoolm.repository.model.JCohort;
import hei.student.schoolm.repository.model.JExam;
import hei.student.schoolm.repository.model.JGroup;
import hei.student.schoolm.repository.model.JTeacher;
import java.time.LocalDate;
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

class CourseIT extends FacadeIT {

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
    return "course-" + SEQUENCE.incrementAndGet() + "-" + UUID.randomUUID() + "@hei.school";
  }

  private String uniqueRef() {
    return "C" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
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

  private CourseRequest courseRequest(
      String ref, String title, int credit, Track track, Semester semester) {
    return CourseRequest.builder()
        .ref(ref)
        .title(title)
        .credit(credit)
        .track(track)
        .semester(semester)
        .build();
  }

  private CourseDto createCourse(
      String token, String ref, String title, int credit, Track track, Semester semester) {
    return webTestClient
        .put()
        .uri("/courses")
        .header("Authorization", "Bearer " + token)
        .bodyValue(courseRequest(ref, title, credit, track, semester))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(CourseDto.class)
        .returnResult()
        .getResponseBody();
  }

  private JGroup saveGroup(JCohort cohort, String ref, Track track) {
    return groupRepository.save(
        JGroup.builder().id(UUID.randomUUID()).ref(ref).track(track).cohort(cohort).build());
  }

  private JCohort saveCohort(String ref, int entryYear) {
    return cohortRepository.save(
        JCohort.builder().id(UUID.randomUUID()).ref(ref).entryYear(entryYear).build());
  }

  @Test
  void adminCanCreateACourse() {
    var admin = saveAdmin();
    var ref = uniqueRef();

    var course = createCourse(adminToken(admin), ref, "Test course", 4, Track.COMMON, Semester.S1);

    assertNotNull(course.getId());
    assertEquals(ref, course.getRef());
    assertEquals("Test course", course.getTitle());
    assertEquals(4, course.getCredit());
    assertEquals("COMMON", course.getTrack());
    assertEquals("S1", course.getSemester());
  }

  @Test
  void adminCanUpdateAnExistingCourse() {
    var admin = saveAdmin();
    var ref = uniqueRef();
    var created = createCourse(adminToken(admin), ref, "Original course", 4, Track.EL, Semester.S2);

    var updated =
        webTestClient
            .put()
            .uri("/courses")
            .header("Authorization", "Bearer " + adminToken(admin))
            .bodyValue(
                CourseRequest.builder()
                    .id(created.getId())
                    .ref(ref)
                    .title("Renamed course")
                    .credit(8)
                    .track(Track.TN)
                    .semester(Semester.S3)
                    .build())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(CourseDto.class)
            .returnResult()
            .getResponseBody();

    assertEquals(created.getId(), updated.getId());
    assertEquals("Renamed course", updated.getTitle());
    assertEquals(8, updated.getCredit());
    assertEquals("TN", updated.getTrack());
    assertEquals("S3", updated.getSemester());
  }

  @Test
  void adminCanListAllCourses() {
    var admin = saveAdmin();
    var refA = uniqueRef();
    var refB = uniqueRef();
    createCourse(adminToken(admin), refA, "Course A", 4, Track.COMMON, Semester.S1);
    createCourse(adminToken(admin), refB, "Course B", 3, Track.EL, Semester.S2);

    var courses =
        webTestClient
            .get()
            .uri("/courses")
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new ParameterizedTypeReference<List<CourseDto>>() {})
            .returnResult()
            .getResponseBody();

    assertNotNull(courses);
    assertTrue(courses.stream().anyMatch(c -> refA.equals(c.getRef())));
    assertTrue(courses.stream().anyMatch(c -> refB.equals(c.getRef())));
  }

  @Test
  void adminCanReadACourseById() {
    var admin = saveAdmin();
    var created =
        createCourse(adminToken(admin), uniqueRef(), "Test course", 4, Track.COMMON, Semester.S1);

    var fetched =
        webTestClient
            .get()
            .uri("/courses/" + created.getId())
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(CourseDto.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(fetched);
    assertEquals(created.getId(), fetched.getId());
  }

  @Test
  void readingAnUnknownCourseReturnsNotFound() {
    var admin = saveAdmin();

    webTestClient
        .get()
        .uri("/courses/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void adminCanDeleteACourse() {
    var admin = saveAdmin();
    var created =
        createCourse(adminToken(admin), uniqueRef(), "Doomed course", 4, Track.COMMON, Semester.S1);

    webTestClient
        .delete()
        .uri("/courses/" + created.getId())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNoContent();

    webTestClient
        .get()
        .uri("/courses/" + created.getId())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void deletingAnUnknownCourseReturnsNotFound() {
    var admin = saveAdmin();

    webTestClient
        .delete()
        .uri("/courses/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void adminCanAssignTeachersToACourse() {
    var admin = saveAdmin();
    var course =
        createCourse(adminToken(admin), uniqueRef(), "Course", 4, Track.COMMON, Semester.S1);
    var teacher =
        teacherRepository.save(
            JTeacher.builder()
                .id(UUID.randomUUID())
                .firstName("Alan")
                .lastName("Turing")
                .email(uniqueEmail())
                .password(passwordEncoder.encode("secret123"))
                .role(Role.TEACHER)
                .build());

    var updated =
        webTestClient
            .put()
            .uri("/courses/" + course.getId() + "/teacher")
            .header("Authorization", "Bearer " + adminToken(admin))
            .bodyValue(new TeacherIdsRequest(List.of(teacher.getId())))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(CourseDto.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(updated);
    assertEquals(1, updated.getTeacherIds().size());
    assertTrue(updated.getTeacherIds().contains(teacher.getId()));
  }

  @Test
  void adminCanAssignGroupsToACourse() {
    var admin = saveAdmin();
    var cohort = saveCohort("COURSE-COHORT-2025", 2025);
    var group = saveGroup(cohort, "COURSE-GRP-A", Track.EL);
    var course =
        createCourse(adminToken(admin), uniqueRef(), "Course", 4, Track.COMMON, Semester.S1);

    var updated =
        webTestClient
            .put()
            .uri("/courses/" + course.getId() + "/group")
            .header("Authorization", "Bearer " + adminToken(admin))
            .bodyValue(new GroupIdsRequest(List.of(group.getId())))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(CourseDto.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(updated);
    assertEquals(1, updated.getGroupIds().size());
    assertTrue(updated.getGroupIds().contains(group.getId()));
  }

  @Test
  void adminCanListExamsOfACourse() {
    var admin = saveAdmin();
    var course =
        createCourse(adminToken(admin), uniqueRef(), "Course", 4, Track.COMMON, Semester.S1);
    var jCourse = courseRepository.getReferenceById(course.getId());
    examRepository.save(
        JExam.builder()
            .id(UUID.randomUUID())
            .course(jCourse)
            .dateExam(LocalDate.of(2025, 6, 1))
            .coefNumerator(1)
            .coefDenominator(1)
            .build());

    var exams =
        webTestClient
            .get()
            .uri("/courses/" + course.getId() + "/exams")
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new ParameterizedTypeReference<List<ExamDto>>() {})
            .returnResult()
            .getResponseBody();

    assertNotNull(exams);
    assertEquals(1, exams.size());
    assertEquals(course.getId(), exams.get(0).courseId());
  }

  @Test
  void unauthenticatedCourseCreationIsRejected() {
    webTestClient
        .put()
        .uri("/courses")
        .bodyValue(courseRequest(uniqueRef(), "x", 4, Track.COMMON, Semester.S1))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void studentCannotCreateACourse() {
    webTestClient
        .put()
        .uri("/courses")
        .header("Authorization", "Bearer " + studentToken())
        .bodyValue(courseRequest(uniqueRef(), "x", 4, Track.COMMON, Semester.S1))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void teacherCannotCreateACourse() {
    webTestClient
        .put()
        .uri("/courses")
        .header("Authorization", "Bearer " + teacherToken())
        .bodyValue(courseRequest(uniqueRef(), "x", 4, Track.COMMON, Semester.S1))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void studentCannotDeleteACourse() {
    var admin = saveAdmin();
    var course =
        createCourse(adminToken(admin), uniqueRef(), "Course", 4, Track.COMMON, Semester.S1);

    webTestClient
        .delete()
        .uri("/courses/" + course.getId())
        .header("Authorization", "Bearer " + studentToken())
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void teacherCanListCourses() {
    var admin = saveAdmin();
    createCourse(adminToken(admin), uniqueRef(), "Course", 4, Track.COMMON, Semester.S1);

    webTestClient
        .get()
        .uri("/courses")
        .header("Authorization", "Bearer " + teacherToken())
        .exchange()
        .expectStatus()
        .isOk();
  }
}
