package hei.student.schoolm.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import hei.student.schoolm.conf.FacadeIT;
import hei.student.schoolm.dto.GradeDto;
import hei.student.schoolm.dto.GradeHistoryDto;
import hei.student.schoolm.dto.GradeRequest;
import hei.student.schoolm.dto.UpdateGradeRequest;
import hei.student.schoolm.endpoint.rest.security.JwtService;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.model.User.Role;
import hei.student.schoolm.repository.jpa.JAdminRepository;
import hei.student.schoolm.repository.jpa.JCohortRepository;
import hei.student.schoolm.repository.jpa.JCourseRepository;
import hei.student.schoolm.repository.jpa.JExamRepository;
import hei.student.schoolm.repository.jpa.JGradeRepository;
import hei.student.schoolm.repository.jpa.JGroupRepository;
import hei.student.schoolm.repository.jpa.JStudentRepository;
import hei.student.schoolm.repository.jpa.JTeacherRepository;
import hei.student.schoolm.repository.model.JAdmin;
import hei.student.schoolm.repository.model.JCohort;
import hei.student.schoolm.repository.model.JCourse;
import hei.student.schoolm.repository.model.JExam;
import hei.student.schoolm.repository.model.JGrade;
import hei.student.schoolm.repository.model.JGroup;
import hei.student.schoolm.repository.model.JStudent;
import hei.student.schoolm.repository.model.JTeacher;
import java.math.BigDecimal;
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

class GradeIT extends FacadeIT {

  private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

  @Autowired private JAdminRepository adminRepository;
  @Autowired private JCohortRepository cohortRepository;
  @Autowired private JGroupRepository groupRepository;
  @Autowired private JCourseRepository courseRepository;
  @Autowired private JExamRepository examRepository;
  @Autowired private JGradeRepository gradeRepository;
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
    return "grade-" + SEQUENCE.incrementAndGet() + "-" + UUID.randomUUID() + "@hei.school";
  }

  private String uniqueRef() {
    return "GRC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
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

  private String teacherToken(JTeacher teacher) {
    return jwtService.generateToken(teacher.getId(), teacher.getEmail(), Role.TEACHER);
  }

  private String studentToken(JStudent student) {
    return jwtService.generateToken(student.getId(), student.getEmail(), Role.STUDENT);
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

  private JStudent saveStudent() {
    var group =
        groupRepository.save(
            JGroup.builder()
                .id(UUID.randomUUID())
                .ref("GRP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .track(Track.EL)
                .cohort(
                    cohortRepository.save(
                        JCohort.builder()
                            .id(UUID.randomUUID())
                            .ref(
                                "COH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                            .entryYear(2025)
                            .build()))
                .build());
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

  private JCourse saveCourseTaughtBy(JTeacher teacher) {
    var course = saveCourse();
    course.getTeachers().add(teacher);
    return courseRepository.save(course);
  }

  private JExam saveExam(JCourse course) {
    return examRepository.save(
        JExam.builder()
            .id(UUID.randomUUID())
            .course(course)
            .dateExam(LocalDate.of(2025, 6, 1))
            .coefNumerator(1)
            .coefDenominator(1)
            .build());
  }

  private JGrade saveGrade(JExam exam, JStudent student, BigDecimal value) {
    return gradeRepository.save(
        JGrade.builder().id(UUID.randomUUID()).exam(exam).student(student).value(value).build());
  }

  private GradeRequest gradeRequest(UUID studentId, BigDecimal value) {
    return GradeRequest.builder().studentId(studentId).value(value).build();
  }

  private List<GradeDto> upsertGrades(String token, UUID examId, List<GradeRequest> requests) {
    var body =
        webTestClient
            .put()
            .uri("/exams/" + examId + "/grades")
            .header("Authorization", "Bearer " + token)
            .bodyValue(requests)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new ParameterizedTypeReference<List<GradeDto>>() {})
            .returnResult()
            .getResponseBody();
    assertNotNull(body);
    return body;
  }

  @Test
  void adminCanUpsertGradesForExam() {
    var admin = saveAdmin();
    var exam = saveExam(saveCourse());
    var student = saveStudent();

    var created =
        upsertGrades(
            adminToken(admin),
            exam.getId(),
            List.of(gradeRequest(student.getId(), new BigDecimal("15.5"))));

    assertEquals(1, created.size());
    assertEquals(student.getId(), created.get(0).getStudentId());
    assertEquals(0, new BigDecimal("15.5").compareTo(created.get(0).getValue()));
  }

  @Test
  void adminCanListGradesForExam() {
    var admin = saveAdmin();
    var exam = saveExam(saveCourse());
    var student = saveStudent();
    upsertGrades(
        adminToken(admin),
        exam.getId(),
        List.of(gradeRequest(student.getId(), new BigDecimal("12.0"))));

    var listed =
        webTestClient
            .get()
            .uri("/exams/" + exam.getId() + "/grades")
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new ParameterizedTypeReference<List<GradeDto>>() {})
            .returnResult()
            .getResponseBody();

    assertNotNull(listed);
    assertEquals(1, listed.size());
  }

  @Test
  void teacherCanUpsertGradesForCourseTheyTeach() {
    var teacher = saveTeacher();
    var exam = saveExam(saveCourseTaughtBy(teacher));
    var student = saveStudent();

    var created =
        upsertGrades(
            teacherToken(teacher),
            exam.getId(),
            List.of(gradeRequest(student.getId(), new BigDecimal("12.0"))));

    assertEquals(1, created.size());
    assertEquals(student.getId(), created.get(0).getStudentId());
  }

  @Test
  void teacherCannotUpsertGradesForCourseTheyDoNotTeach() {
    var teacher = saveTeacher();
    var otherTeacher = saveTeacher();
    var exam = saveExam(saveCourseTaughtBy(otherTeacher));
    var student = saveStudent();

    webTestClient
        .put()
        .uri("/exams/" + exam.getId() + "/grades")
        .header("Authorization", "Bearer " + teacherToken(teacher))
        .bodyValue(List.of(gradeRequest(student.getId(), new BigDecimal("10.0"))))
        .exchange()
        .expectStatus()
        .isForbidden();

    webTestClient
        .get()
        .uri("/exams/" + exam.getId() + "/grades")
        .header("Authorization", "Bearer " + teacherToken(teacher))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void studentCannotAccessExamGrades() {
    var student = saveStudent();
    var exam = saveExam(saveCourse());

    webTestClient
        .get()
        .uri("/exams/" + exam.getId() + "/grades")
        .header("Authorization", "Bearer " + studentToken(student))
        .exchange()
        .expectStatus()
        .isForbidden();

    webTestClient
        .put()
        .uri("/exams/" + exam.getId() + "/grades")
        .header("Authorization", "Bearer " + studentToken(student))
        .bodyValue(List.of(gradeRequest(student.getId(), new BigDecimal("10.0"))))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void unknownExamReturnsNotFound() {
    var admin = saveAdmin();

    webTestClient
        .get()
        .uri("/exams/" + UUID.randomUUID() + "/grades")
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void adminCanReadGradeById() {
    var admin = saveAdmin();
    var exam = saveExam(saveCourse());
    var student = saveStudent();
    var grade = saveGrade(exam, student, new BigDecimal("12.0"));

    var fetched =
        webTestClient
            .get()
            .uri("/grades/" + grade.getId())
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(GradeDto.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(fetched);
    assertEquals(grade.getId(), fetched.getId());
    assertEquals(student.getId(), fetched.getStudentId());
  }

  @Test
  void readingAnUnknownGradeReturnsNotFound() {
    var admin = saveAdmin();

    webTestClient
        .get()
        .uri("/grades/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void adminCanUpdateGradeWithMandatoryChangeReason() {
    var admin = saveAdmin();
    var exam = saveExam(saveCourse());
    var student = saveStudent();
    var grade = saveGrade(exam, student, new BigDecimal("14.0"));

    var updated =
        webTestClient
            .put()
            .uri("/grades/" + grade.getId())
            .header("Authorization", "Bearer " + adminToken(admin))
            .bodyValue(new UpdateGradeRequest(new BigDecimal("16.5"), "Recheck"))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(GradeDto.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(updated);
    assertEquals(0, new BigDecimal("16.5").compareTo(updated.getValue()));
    assertEquals("Recheck", updated.getChangeReason());
  }

  @Test
  void updatingGradeWithoutChangeReasonIsRejected() {
    var admin = saveAdmin();
    var exam = saveExam(saveCourse());
    var student = saveStudent();
    var grade = saveGrade(exam, student, new BigDecimal("14.0"));

    webTestClient
        .put()
        .uri("/grades/" + grade.getId())
        .header("Authorization", "Bearer " + adminToken(admin))
        .bodyValue(new UpdateGradeRequest(new BigDecimal("16.5"), " "))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void adminCanReadGradeHistory() {
    var admin = saveAdmin();
    var exam = saveExam(saveCourse());
    var student = saveStudent();
    var grade = saveGrade(exam, student, new BigDecimal("14.0"));
    webTestClient
        .put()
        .uri("/grades/" + grade.getId())
        .header("Authorization", "Bearer " + adminToken(admin))
        .bodyValue(new UpdateGradeRequest(new BigDecimal("16.5"), "Recheck"))
        .exchange()
        .expectStatus()
        .isOk();

    var history =
        webTestClient
            .get()
            .uri("/grades/" + grade.getId() + "/history")
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new ParameterizedTypeReference<List<GradeHistoryDto>>() {})
            .returnResult()
            .getResponseBody();

    assertNotNull(history);
    assertEquals(1, history.size());
    assertEquals(grade.getId(), history.get(0).getGradeId());
    assertEquals("Recheck", history.get(0).getChangeReason());
    assertEquals(0, new BigDecimal("16.5").compareTo(history.get(0).getNewValue()));
  }

  @Test
  void adminCanDeleteGrade() {
    var admin = saveAdmin();
    var exam = saveExam(saveCourse());
    var student = saveStudent();
    var grade = saveGrade(exam, student, new BigDecimal("12.0"));

    webTestClient
        .delete()
        .uri("/grades/" + grade.getId())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNoContent();

    webTestClient
        .get()
        .uri("/grades/" + grade.getId())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void deletingAnUnknownGradeReturnsNotFound() {
    var admin = saveAdmin();

    webTestClient
        .delete()
        .uri("/grades/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void studentCannotUpdateGrade() {
    var student = saveStudent();
    var exam = saveExam(saveCourse());
    var grade = saveGrade(exam, student, new BigDecimal("12.0"));

    webTestClient
        .put()
        .uri("/grades/" + grade.getId())
        .header("Authorization", "Bearer " + studentToken(student))
        .bodyValue(new UpdateGradeRequest(new BigDecimal("18.0"), "Recheck"))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void studentCannotDeleteGrade() {
    var student = saveStudent();
    var exam = saveExam(saveCourse());
    var grade = saveGrade(exam, student, new BigDecimal("12.0"));

    webTestClient
        .delete()
        .uri("/grades/" + grade.getId())
        .header("Authorization", "Bearer " + studentToken(student))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void gradeOutOfRangeIsRejected() {
    var admin = saveAdmin();
    var exam = saveExam(saveCourse());
    var student = saveStudent();

    webTestClient
        .put()
        .uri("/exams/" + exam.getId() + "/grades")
        .header("Authorization", "Bearer " + adminToken(admin))
        .bodyValue(List.of(gradeRequest(student.getId(), new BigDecimal("25.0"))))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }
}
