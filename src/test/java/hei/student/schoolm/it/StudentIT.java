package hei.student.schoolm.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import hei.student.schoolm.conf.FacadeIT;
import hei.student.schoolm.dto.GroupFlowDto;
import hei.student.schoolm.dto.MoveStudentGroupRequest;
import hei.student.schoolm.dto.SemesterValidationDto;
import hei.student.schoolm.dto.StudentRequest;
import hei.student.schoolm.dto.StudentResponse;
import hei.student.schoolm.dto.TranscriptDto;
import hei.student.schoolm.endpoint.event.EventProducer;
import hei.student.schoolm.endpoint.event.model.TranscriptEmailRequested;
import hei.student.schoolm.model.GroupFlowType;
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
import org.springframework.boot.test.context.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import hei.student.schoolm.endpoint.rest.security.JwtService;
import org.mockito.ArgumentCaptor;

class StudentIT extends FacadeIT {

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

  @MockBean private EventProducer<TranscriptEmailRequested> eventProducer;

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
    return "student-" + SEQUENCE.incrementAndGet() + "-" + UUID.randomUUID() + "@hei.school";
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

  private String studentToken(JStudent student) {
    return jwtService.generateToken(student.getId(), student.getEmail(), Role.STUDENT);
  }

  private JCohort saveCohort(String ref, int entryYear) {
    return cohortRepository.save(
        JCohort.builder().id(UUID.randomUUID()).ref(ref).entryYear(entryYear).build());
  }

  private JGroup saveGroup(JCohort cohort, String ref, Track track) {
    return groupRepository.save(
        JGroup.builder().id(UUID.randomUUID()).ref(ref).track(track).cohort(cohort).build());
  }

  private StudentResponse createStudent(String token, JGroup group, String email, String password) {
    return webTestClient
        .put()
        .uri("/students")
        .header("Authorization", "Bearer " + token)
        .bodyValue(
            StudentRequest.builder()
                .firstName("Student")
                .lastName("E" + UUID.randomUUID().toString().substring(0, 8))
                .email(email)
                .password(password)
                .groupId(group.getId())
                .build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(StudentResponse.class)
        .returnResult()
        .getResponseBody();
  }

  @Test
  void creatingAStudentGeneratesAStdRefMatchingTheEntryYear() {
    var admin = saveAdmin();
    var cohort = saveCohort("STUDENT-COHORT-2024", 2024);
    var group = saveGroup(cohort, "STUDENT-GRP-A", Track.EL);

    var student = createStudent(adminToken(admin), group, uniqueEmail(), "password123");

    assertNotNull(student.reference());
    assertTrue(student.reference().startsWith("STD24"));
    assertEquals(8, student.reference().length());
    assertEquals(group.getId(), student.groupId());
  }

  @Test
  void sequenceNumberIncrementsPerEntryYear() {
    var admin = saveAdmin();
    var cohort = saveCohort("STUDENT-COHORT-2024", 2024);
    var group = saveGroup(cohort, "STUDENT-GRP-B", Track.EL);

    var first = createStudent(adminToken(admin), group, uniqueEmail(), "password123");
    var second = createStudent(adminToken(admin), group, uniqueEmail(), "password123");

    int firstSeq = Integer.parseInt(first.reference().substring(5));
    int secondSeq = Integer.parseInt(second.reference().substring(5));
    assertEquals(firstSeq + 1, secondSeq);
  }

  @Test
  void studentCanReadTheirOwnProfile() {
    var admin = saveAdmin();
    var cohort = saveCohort("STUDENT-COHORT-2024", 2024);
    var group = saveGroup(cohort, "STUDENT-GRP-C", Track.EL);
    var email = uniqueEmail();
    var student = createStudent(adminToken(admin), group, email, "password123");

    var fetched =
        webTestClient
            .get()
            .uri("/students/" + student.id())
            .header("Authorization", "Bearer " + studentToken(studentRepository.findByEmailIgnoreCase(email).orElseThrow()))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(StudentResponse.class)
            .returnResult()
            .getResponseBody();

    assertEquals(student.id(), fetched.id());
  }

  @Test
  void studentCannotReadAnotherStudentsProfile() {
    var admin = saveAdmin();
    var cohort = saveCohort("STUDENT-COHORT-2024", 2024);
    var group = saveGroup(cohort, "STUDENT-GRP-D", Track.EL);
    var studentA = createStudent(adminToken(admin), group, uniqueEmail(), "password123");
    var studentB = createStudent(adminToken(admin), group, uniqueEmail(), "password123");

    webTestClient
        .get()
        .uri("/students/" + studentA.id())
        .header("Authorization", "Bearer " + studentToken(studentRepository.findByEmailIgnoreCase(studentB.email()).orElseThrow()))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void studentCannotListAllStudents() {
    var admin = saveAdmin();
    var cohort = saveCohort("STUDENT-COHORT-2024", 2024);
    var group = saveGroup(cohort, "STUDENT-GRP-E", Track.EL);
    var student = createStudent(adminToken(admin), group, uniqueEmail(), "password123");

    webTestClient
        .get()
        .uri("/students")
        .header("Authorization", "Bearer " + studentToken(studentRepository.findByEmailIgnoreCase(student.email()).orElseThrow()))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void movingAStudentToANewGroupUpdatesTheirCurrentGroupAndKeepsHistory() {
    var admin = saveAdmin();
    var cohort = saveCohort("STUDENT-COHORT-2024", 2024);
    var groupA = saveGroup(cohort, "STUDENT-GRP-F", Track.EL);
    var groupB = saveGroup(cohort, "STUDENT-GRP-G", Track.TN);
    var student = createStudent(adminToken(admin), groupA, uniqueEmail(), "password123");

    webTestClient
        .put()
        .uri("/students/" + student.id() + "/group-flows")
        .header("Authorization", "Bearer " + adminToken(admin))
        .bodyValue(new MoveStudentGroupRequest(groupB.getId()))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(GroupFlowDto.class)
        .returnResult()
        .getResponseBody();

    var refreshed =
        webTestClient
            .get()
            .uri("/students/" + student.id())
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(StudentResponse.class)
            .returnResult()
            .getResponseBody();

    assertEquals(groupB.getId(), refreshed.groupId());

    var flows =
        webTestClient
            .get()
            .uri("/students/" + student.id() + "/group-flows")
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new ParameterizedTypeReference<List<GroupFlowDto>>() {})
            .returnResult()
            .getResponseBody();

    assertNotNull(flows);
    assertEquals(2, flows.size());
    assertEquals(GroupFlowType.JOIN, flows.get(0).groupFlowType());
  }

  @Test
  void movingToAGroupOfAnotherCohortKeepsTheReference() {
    var admin = saveAdmin();
    var cohort2024 = saveCohort("STUDENT-COHORT-2024", 2024);
    var cohort2025 = saveCohort("STUDENT-COHORT-2025", 2025);
    var group2024 = saveGroup(cohort2024, "STUDENT-GRP-H", Track.EL);
    var group2025 = saveGroup(cohort2025, "STUDENT-GRP-I", Track.EL);
    var student = createStudent(adminToken(admin), group2024, uniqueEmail(), "password123");
    var referenceBefore = student.reference();

    webTestClient
        .put()
        .uri("/students/" + student.id() + "/group-flows")
        .header("Authorization", "Bearer " + adminToken(admin))
        .bodyValue(new MoveStudentGroupRequest(group2025.getId()))
        .exchange()
        .expectStatus()
        .isOk();

    var refreshed =
        webTestClient
            .get()
            .uri("/students/" + student.id())
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(StudentResponse.class)
            .returnResult()
            .getResponseBody();

    assertEquals(group2025.getId(), refreshed.groupId());
    assertEquals(referenceBefore, refreshed.reference());
  }

  @Test
  void creatingStudentWithoutInitialGroupIsRejected() {
    var admin = saveAdmin();

    webTestClient
        .put()
        .uri("/students")
        .header("Authorization", "Bearer " + adminToken(admin))
        .bodyValue(
            StudentRequest.builder()
                .firstName("Student")
                .lastName("Solo")
                .email(uniqueEmail())
                .password("password123")
                .build())
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void creatingStudentWithoutPasswordIsRejected() {
    var admin = saveAdmin();
    var cohort = saveCohort("STUDENT-COHORT-2024", 2024);
    var group = saveGroup(cohort, "STUDENT-GRP-J", Track.EL);

    webTestClient
        .put()
        .uri("/students")
        .header("Authorization", "Bearer " + adminToken(admin))
        .bodyValue(
            StudentRequest.builder()
                .firstName("Student")
                .lastName("NoPass")
                .email(uniqueEmail())
                .groupId(group.getId())
                .build())
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void updatingStudentWithGroupIdIsRejected() {
    var admin = saveAdmin();
    var cohort = saveCohort("STUDENT-COHORT-2024", 2024);
    var group = saveGroup(cohort, "STUDENT-GRP-K", Track.EL);
    var student = createStudent(adminToken(admin), group, uniqueEmail(), "password123");

    webTestClient
        .put()
        .uri("/students")
        .header("Authorization", "Bearer " + adminToken(admin))
        .bodyValue(
            StudentRequest.builder()
                .id(student.id())
                .firstName("Student")
                .lastName("Moved")
                .email(student.email())
                .groupId(group.getId())
                .build())
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void adminCanDeleteAStudent() {
    var admin = saveAdmin();
    var cohort = saveCohort("STUDENT-COHORT-2025", 2025);
    var group = saveGroup(cohort, "STUDENT-GRP-L", Track.EL);
    var student = createStudent(adminToken(admin), group, uniqueEmail(), "secret123");

    webTestClient
        .delete()
        .uri("/students/" + student.id())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNoContent();

    webTestClient
        .get()
        .uri("/students/" + student.id())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void deletingAnUnknownStudentReturnsNotFound() {
    var admin = saveAdmin();

    webTestClient
        .delete()
        .uri("/students/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void studentCannotDeleteAStudent() {
    var admin = saveAdmin();
    var cohort = saveCohort("STUDENT-COHORT-2025", 2025);
    var group = saveGroup(cohort, "STUDENT-GRP-M", Track.EL);
    var email = uniqueEmail();
    var student = createStudent(adminToken(admin), group, email, "secret123");

    webTestClient
        .delete()
        .uri("/students/" + student.id())
        .header("Authorization", "Bearer " + studentToken(studentRepository.findByEmailIgnoreCase(email).orElseThrow()))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void deletingAStudentCascadesGradesGroupFlowsAndHistories() {
    var admin = saveAdmin();
    var cohort = saveCohort("STUDENT-COHORT-2025", 2025);
    var group = saveGroup(cohort, "STUDENT-GRP-N", Track.EL);
    var student = createStudent(adminToken(admin), group, uniqueEmail(), "secret123");

    var course = courseRepository.save(
        JCourse.builder()
            .id(UUID.randomUUID())
            .ref("STC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
            .title("Course")
            .credit(4)
            .track(Track.COMMON)
            .semester(Semester.S1)
            .build());
    var exam =
        examRepository.save(
            JExam.builder()
                .id(UUID.randomUUID())
                .course(course)
                .dateExam(LocalDate.of(2025, 6, 1))
                .coefNumerator(1)
                .coefDenominator(1)
                .build());
    var jStudent = studentRepository.findByEmailIgnoreCase(student.email()).orElseThrow();
    var grade =
        gradeRepository.save(
            JGrade.builder()
                .id(UUID.randomUUID())
                .exam(exam)
                .student(jStudent)
                .value(new BigDecimal("12.50"))
                .build());

    webTestClient
        .delete()
        .uri("/students/" + student.id())
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNoContent();

assertFalse(gradeRepository.findById(grade.getId()).isPresent());
    assertTrue(groupRepository.findById(group.getId()).isPresent());
    assertTrue(
        jdbcTemplate
            .queryForList("select id from group_flow where student_id = ?", student.id())
            .isEmpty());
  }

  @Test
  void studentCanGetTheirOwnSemesterValidation() {
    var admin = saveAdmin();
    var cohort = saveCohort("STUDENT-COHORT-2024", 2024);
    var group = saveGroup(cohort, "STUDENT-GRP-O", Track.EL);
    var student = createStudent(adminToken(admin), group, uniqueEmail(), "password123");

    var validation =
        webTestClient
            .get()
            .uri("/students/" + student.id() + "/semester-validation?semester=S1")
            .header("Authorization", "Bearer " + studentToken(studentRepository.findByEmailIgnoreCase(student.email()).orElseThrow()))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(SemesterValidationDto.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(validation);
    assertEquals(student.id(), validation.getStudentId());
    assertEquals(Semester.S1, validation.getSemester());
  }

  @Test
  void studentCanGetTheirOwnGraduateTranscript() {
    var admin = saveAdmin();
    var cohort = saveCohort("STUDENT-COHORT-2024", 2024);
    var group = saveGroup(cohort, "STUDENT-GRP-P", Track.EL);
    var student = createStudent(adminToken(admin), group, uniqueEmail(), "password123");

    var transcript =
        webTestClient
            .get()
            .uri("/students/" + student.id() + "/graduate-transcript")
            .header("Authorization", "Bearer " + studentToken(studentRepository.findByEmailIgnoreCase(student.email()).orElseThrow()))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(TranscriptDto.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(transcript);
    assertEquals(student.id(), transcript.getStudentId());
  }

  @Test
  void emailTranscriptIsQueued() {
    var admin = saveAdmin();
    var cohort = saveCohort("STUDENT-COHORT-2024", 2024);
    var group = saveGroup(cohort, "STUDENT-GRP-Q", Track.EL);
    var student = createStudent(adminToken(admin), group, uniqueEmail(), "password123");

    webTestClient
        .post()
        .uri("/students/" + student.id() + "/transcript/email?level=L1")
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isOk();

    var captor = ArgumentCaptor.forClass(List.class);
    verify(eventProducer).accept(captor.capture());
    var events = (List<TranscriptEmailRequested>) captor.getValue();
    assertEquals(1, events.size());
    assertEquals(student.id(), events.get(0).getStudentId());
    assertEquals(hei.student.schoolm.dto.LevelRequest.L1, events.get(0).getLevel());
  }

  @Test
  void studentCannotRequestAnotherStudentsTranscriptByEmail() {
    var admin = saveAdmin();
    var cohort = saveCohort("STUDENT-COHORT-2024", 2024);
    var group = saveGroup(cohort, "STUDENT-GRP-R", Track.EL);
    var studentA = createStudent(adminToken(admin), group, uniqueEmail(), "password123");
    var studentB = createStudent(adminToken(admin), group, uniqueEmail(), "password123");

    webTestClient
        .post()
        .uri("/students/" + studentA.id() + "/transcript/email?level=L1")
        .header("Authorization", "Bearer " + studentToken(studentRepository.findByEmailIgnoreCase(studentB.email()).orElseThrow()))
        .exchange()
        .expectStatus()
        .isForbidden();
  }
}