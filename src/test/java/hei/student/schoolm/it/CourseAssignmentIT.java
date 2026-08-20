package hei.student.schoolm.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hei.student.schoolm.conf.FacadeIT;
import hei.student.schoolm.dto.CourseAssignmentRequest;
import hei.student.schoolm.dto.CourseAssignmentResponse;
import hei.student.schoolm.dto.CurriculumStatusResponse;
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
import hei.student.schoolm.repository.model.JCohort;
import hei.student.schoolm.repository.model.JCourse;
import hei.student.schoolm.repository.model.JGroup;
import hei.student.schoolm.repository.model.JStudent;
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

class CourseAssignmentIT extends FacadeIT {

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
    return "ca-" + SEQUENCE.incrementAndGet() + "-" + UUID.randomUUID() + "@hei.school";
  }

  private String uniqueRef() {
    return "CAC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
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

  private JStudent saveStudent(JGroup group) {
    return studentRepository.save(
        JStudent.builder()
            .id(UUID.randomUUID())
            .firstName("Alan")
            .lastName("Turing")
            .email(uniqueEmail())
            .password(passwordEncoder.encode("secret123"))
            .role(Role.STUDENT)
            .reference("STD25001")
            .group(group)
            .build());
  }

  private JGroup saveGroup() {
    return groupRepository.save(
        JGroup.builder()
            .id(UUID.randomUUID())
            .ref("CAG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
            .track(Track.EL)
            .cohort(
                cohortRepository.save(
                    JCohort.builder()
                        .id(UUID.randomUUID())
                        .ref("CACOH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                        .entryYear(2025)
                        .build()))
            .build());
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

  private CourseAssignmentRequest assignmentRequest(
      UUID courseId, UUID groupId, List<UUID> teacherIds, int credits) {
    return new CourseAssignmentRequest(
        null, courseId, groupId, teacherIds, 2025, Semester.S1, credits);
  }

  private List<CourseAssignmentResponse> upsertAssignments(
      String token, List<CourseAssignmentRequest> requests) {
    var body =
        webTestClient
            .put()
            .uri("/course-assignments")
            .header("Authorization", "Bearer " + token)
            .bodyValue(requests)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new ParameterizedTypeReference<List<CourseAssignmentResponse>>() {})
            .returnResult()
            .getResponseBody();
    assertNotNull(body);
    return body;
  }

  @Test
  void adminCanCreateACourseAssignment() {
    var admin = saveAdmin();
    var group = saveGroup();
    var course = saveCourse();
    var teacher = saveTeacher();

    var assignment =
        upsertAssignments(
                adminToken(admin),
                List.of(
                    assignmentRequest(
                        course.getId(),
                        group.getId(),
                        List.of(teacher.getId()),
                        course.getCredit())))
            .get(0);

    assertNotNull(assignment);
    assertNotNull(assignment.id());
    assertEquals(course.getId(), assignment.courseId());
    assertEquals(group.getId(), assignment.groupId());
    assertEquals(course.getCredit(), assignment.credits());
    assertEquals(1, assignment.teacherIds().size());
    assertTrue(assignment.teacherIds().contains(teacher.getId()));
  }

  @Test
  void cannotAssignAStudentAsTeacher() {
    var admin = saveAdmin();
    var group = saveGroup();
    var course = saveCourse();
    var student = saveStudent(group);

    webTestClient
        .put()
        .uri("/course-assignments")
        .header("Authorization", "Bearer " + adminToken(admin))
        .bodyValue(
            List.of(
                assignmentRequest(
                    course.getId(), group.getId(), List.of(student.getId()), course.getCredit())))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void duplicateAssignmentIsRejected() {
    var admin = saveAdmin();
    var group = saveGroup();
    var course = saveCourse();
    var teacher = saveTeacher();
    var request =
        List.of(
            assignmentRequest(
                course.getId(), group.getId(), List.of(teacher.getId()), course.getCredit()));

    upsertAssignments(adminToken(admin), request);

    webTestClient
        .put()
        .uri("/course-assignments")
        .header("Authorization", "Bearer " + adminToken(admin))
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void sameCourseCanBeCoTaughtByTwoTeachers() {
    var admin = saveAdmin();
    var group = saveGroup();
    var course = saveCourse();
    var teacherA = saveTeacher();
    var teacherB = saveTeacher();

    var assignment =
        upsertAssignments(
                adminToken(admin),
                List.of(
                    assignmentRequest(
                        course.getId(),
                        group.getId(),
                        List.of(teacherA.getId(), teacherB.getId()),
                        course.getCredit())))
            .get(0);

    assertNotNull(assignment);
    assertEquals(2, assignment.teacherIds().size());
    assertTrue(assignment.teacherIds().contains(teacherA.getId()));
    assertTrue(assignment.teacherIds().contains(teacherB.getId()));
  }

  @Test
  void teacherScopedListIgnoresQueryParamOverride() {
    var teacher = saveTeacher();
    var otherTeacher = saveTeacher();
    var group = saveGroup();
    var course = saveCourse();
    var otherCourse = saveCourse();

    upsertAssignments(
        adminToken(saveAdmin()),
        List.of(assignmentRequest(course.getId(), group.getId(), List.of(teacher.getId()), 4)));
    upsertAssignments(
        adminToken(saveAdmin()),
        List.of(
            assignmentRequest(
                otherCourse.getId(), group.getId(), List.of(otherTeacher.getId()), 4)));

    var results =
        webTestClient
            .get()
            .uri("/course-assignments?teacherId=" + otherTeacher.getId())
            .header(
                "Authorization",
                "Bearer "
                    + jwtService.generateToken(teacher.getId(), teacher.getEmail(), Role.TEACHER))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new ParameterizedTypeReference<TestPage<CourseAssignmentResponse>>() {})
            .returnResult()
            .getResponseBody()
            .content();

    assertNotNull(results);
    assertEquals(1, results.size());
    assertTrue(results.get(0).teacherIds().contains(teacher.getId()));
  }

  @Test
  void studentCanOnlySeeAssignmentsOfTheirCurrentGroup() {
    var groupA = saveGroup();
    var groupB = saveGroup();
    var courseA = saveCourse();
    var courseB = saveCourse();
    var teacher = saveTeacher();
    var student = saveStudent(groupA);
    var admin = saveAdmin();

    upsertAssignments(
        adminToken(admin),
        List.of(assignmentRequest(courseA.getId(), groupA.getId(), List.of(teacher.getId()), 4)));
    upsertAssignments(
        adminToken(admin),
        List.of(assignmentRequest(courseB.getId(), groupB.getId(), List.of(teacher.getId()), 4)));

    var results =
        webTestClient
            .get()
            .uri("/course-assignments")
            .header(
                "Authorization",
                "Bearer "
                    + jwtService.generateToken(student.getId(), student.getEmail(), Role.STUDENT))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new ParameterizedTypeReference<TestPage<CourseAssignmentResponse>>() {})
            .returnResult()
            .getResponseBody()
            .content();

    assertNotNull(results);
    assertTrue(results.stream().allMatch(a -> a.groupId().equals(groupA.getId())));
  }

  @Test
  void adminCanFilterAssignmentsByCourse() {
    var admin = saveAdmin();
    var group = saveGroup();
    var course = saveCourse();
    var otherCourse = saveCourse();
    var teacher = saveTeacher();

    upsertAssignments(
        adminToken(admin),
        List.of(assignmentRequest(course.getId(), group.getId(), List.of(teacher.getId()), 4)));
    upsertAssignments(
        adminToken(admin),
        List.of(
            assignmentRequest(otherCourse.getId(), group.getId(), List.of(teacher.getId()), 4)));

    var results =
        webTestClient
            .get()
            .uri("/course-assignments?courseId=" + course.getId())
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new ParameterizedTypeReference<TestPage<CourseAssignmentResponse>>() {})
            .returnResult()
            .getResponseBody()
            .content();

    assertNotNull(results);
    assertEquals(1, results.size());
    assertEquals(course.getId(), results.get(0).courseId());
  }

  @Test
  void curriculumStatusIsAdminOnly() {
    var admin = saveAdmin();
    var group = saveGroup();
    var course = saveCourse();
    var teacher = saveTeacher();
    var student = saveStudent(group);

    upsertAssignments(
        adminToken(admin),
        List.of(assignmentRequest(course.getId(), group.getId(), List.of(teacher.getId()), 4)));

    var uri =
        "/course-assignments/curriculum-status?groupId="
            + group.getId()
            + "&academicYear=2025&semester=S1";

    var status =
        webTestClient
            .get()
            .uri(uri)
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(CurriculumStatusResponse.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(status);
    assertEquals(Semester.S1, status.semester());
    assertEquals(4, status.assignedCredits());
    assertEquals(30, status.targetCredits());

    webTestClient
        .get()
        .uri(uri)
        .header(
            "Authorization",
            "Bearer " + jwtService.generateToken(teacher.getId(), teacher.getEmail(), Role.TEACHER))
        .exchange()
        .expectStatus()
        .isForbidden();

    webTestClient
        .get()
        .uri(uri)
        .header(
            "Authorization",
            "Bearer " + jwtService.generateToken(student.getId(), student.getEmail(), Role.STUDENT))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void adminCanDeleteACourseAssignment() {
    var admin = saveAdmin();
    var group = saveGroup();
    var course = saveCourse();
    var teacher = saveTeacher();

    var assignment =
        upsertAssignments(
                adminToken(admin),
                List.of(
                    assignmentRequest(
                        course.getId(),
                        group.getId(),
                        List.of(teacher.getId()),
                        course.getCredit())))
            .get(0);

    webTestClient
        .delete()
        .uri("/course-assignments/" + assignment.id())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNoContent();

    webTestClient
        .get()
        .uri("/course-assignments/" + assignment.id())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void teacherCannotCreateACourseAssignment() {
    var group = saveGroup();
    var course = saveCourse();
    var teacher = saveTeacher();

    webTestClient
        .put()
        .uri("/course-assignments")
        .header(
            "Authorization",
            "Bearer " + jwtService.generateToken(teacher.getId(), teacher.getEmail(), Role.TEACHER))
        .bodyValue(
            List.of(
                assignmentRequest(
                    course.getId(), group.getId(), List.of(teacher.getId()), course.getCredit())))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void studentCannotCreateACourseAssignment() {
    var group = saveGroup();
    var course = saveCourse();
    var teacher = saveTeacher();
    var student = saveStudent(group);

    webTestClient
        .put()
        .uri("/course-assignments")
        .header(
            "Authorization",
            "Bearer " + jwtService.generateToken(student.getId(), student.getEmail(), Role.STUDENT))
        .bodyValue(
            List.of(
                assignmentRequest(
                    course.getId(), group.getId(), List.of(teacher.getId()), course.getCredit())))
        .exchange()
        .expectStatus()
        .isForbidden();
  }
}
