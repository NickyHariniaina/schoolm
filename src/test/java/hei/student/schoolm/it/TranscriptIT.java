package hei.student.schoolm.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.schoolm.conf.FacadeIT;
import hei.student.schoolm.dto.LevelRequest;
import hei.student.schoolm.endpoint.event.EventProducer;
import hei.student.schoolm.endpoint.event.model.TranscriptEmailRequested;
import hei.student.schoolm.endpoint.rest.security.JwtService;
import hei.student.schoolm.file.bucket.BucketComponent;
import hei.student.schoolm.mail.Mailer;
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
import hei.student.schoolm.service.event.TranscriptEmailRequestedService;
import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.SneakyThrows;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

class TranscriptIT extends FacadeIT {

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
  @Autowired private TranscriptEmailRequestedService transcriptEmailRequestedService;

  @MockBean private EventProducer<TranscriptEmailRequested> eventProducer;
  @MockBean private BucketComponent bucketComponent;
  @MockBean private Mailer mailer;

  @LocalServerPort int port;
  private WebTestClient webTestClient;
  private byte[] uploadedPdfBytes;

  @BeforeEach
  @SneakyThrows
  void setUp() {
    webTestClient =
        WebTestClient.bindToServer()
            .baseUrl("http://localhost:" + port)
            .responseTimeout(Duration.ofSeconds(30))
            .build();
    when(bucketComponent.presign(any(), any()))
        .thenReturn(
            URI.create("https://dummy-bucket.s3.eu-west-3.amazonaws.com/transcripts/test.pdf")
                .toURL());
    // The service deletes the temp PDF right after upload, so snapshot the bytes at upload time.
    doAnswer(
            invocation -> {
              var file = (File) invocation.getArgument(0);
              uploadedPdfBytes = Files.readAllBytes(file.toPath());
              return null;
            })
        .when(bucketComponent)
        .upload(any(), any());
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
    return "transcript-" + SEQUENCE.incrementAndGet() + "-" + UUID.randomUUID() + "@hei.school";
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

  private JGroup saveGroup() {
    return groupRepository.save(
        JGroup.builder()
            .id(UUID.randomUUID())
            .ref("TRG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
            .track(Track.COMMON)
            .cohort(
                cohortRepository.save(
                    JCohort.builder()
                        .id(UUID.randomUUID())
                        .ref("TRC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                        .entryYear(2024)
                        .build()))
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
            .reference("STD24001")
            .group(group)
            .build());
  }

  private String studentToken(JStudent student) {
    return jwtService.generateToken(student.getId(), student.getEmail(), Role.STUDENT);
  }

  private JCourse saveCourse() {
    return courseRepository.save(
        JCourse.builder()
            .id(UUID.randomUUID())
            .ref("TRC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
            .title("Course")
            .credit(4)
            .track(Track.COMMON)
            .semester(Semester.S1)
            .build());
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

  @Test
  void adminCanRequestTranscriptAndEventIsEnqueued() {
    var admin = saveAdmin();
    var group = saveGroup();
    var student = saveStudent(group);

    webTestClient
        .post()
        .uri("/students/" + student.getId() + "/transcript/email?level=L1")
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isOk();

    var captor = ArgumentCaptor.forClass(List.class);
    verify(eventProducer).accept(captor.capture());
    var events = (List<TranscriptEmailRequested>) captor.getValue();
    assertEquals(1, events.size());
    assertEquals(student.getId(), events.get(0).getStudentId());
    assertEquals(LevelRequest.L1, events.get(0).getLevel());
  }

  @Test
  void studentCanRequestOwnTranscript() {
    var group = saveGroup();
    var student = saveStudent(group);

    webTestClient
        .post()
        .uri("/students/" + student.getId() + "/transcript/email?level=L1")
        .header("Authorization", "Bearer " + studentToken(student))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void teacherCannotRequestTranscript() {
    var teacher = saveTeacher();
    var group = saveGroup();
    var student = saveStudent(group);

    webTestClient
        .post()
        .uri("/students/" + student.getId() + "/transcript/email?level=L1")
        .header(
            "Authorization",
            "Bearer " + jwtService.generateToken(teacher.getId(), teacher.getEmail(), Role.TEACHER))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void anonymousCannotRequestTranscript() {
    var group = saveGroup();
    var student = saveStudent(group);

    webTestClient
        .post()
        .uri("/students/" + student.getId() + "/transcript/email?level=L1")
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @SneakyThrows
  private String pdfText(byte[] pdf) {
    try (var document = PDDocument.load(pdf)) {
      return new PDFTextStripper().getText(document);
    }
  }
}
