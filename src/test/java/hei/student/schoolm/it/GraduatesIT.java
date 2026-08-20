package hei.student.schoolm.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import hei.student.schoolm.conf.FacadeIT;
import hei.student.schoolm.dto.GraduateFileDto;
import hei.student.schoolm.endpoint.rest.security.JwtService;
import hei.student.schoolm.file.bucket.BucketComponent;
import hei.student.schoolm.file.xlsx.GraduateXlsxWriter;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.model.User.Role;
import hei.student.schoolm.repository.jpa.JAdminRepository;
import hei.student.schoolm.repository.jpa.JCohortRepository;
import hei.student.schoolm.repository.jpa.JCourseAssignmentRepository;
import hei.student.schoolm.repository.jpa.JCourseRepository;
import hei.student.schoolm.repository.jpa.JExamRepository;
import hei.student.schoolm.repository.jpa.JGradeRepository;
import hei.student.schoolm.repository.jpa.JGroupRepository;
import hei.student.schoolm.repository.jpa.JStudentRepository;
import hei.student.schoolm.repository.jpa.JTeacherRepository;
import hei.student.schoolm.repository.model.JAdmin;
import hei.student.schoolm.repository.model.JCourse;
import hei.student.schoolm.repository.model.JCourseAssignment;
import hei.student.schoolm.repository.model.JExam;
import hei.student.schoolm.repository.model.JGrade;
import hei.student.schoolm.repository.model.JGroup;
import hei.student.schoolm.repository.model.JStudent;
import hei.student.schoolm.repository.model.JTeacher;
import hei.student.schoolm.service.GraduateService;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.SneakyThrows;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

class GraduatesIT extends FacadeIT {

  private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

  @Autowired private JAdminRepository adminRepository;
  @Autowired private JCohortRepository cohortRepository;
  @Autowired private JGroupRepository groupRepository;
  @Autowired private JCourseRepository courseRepository;
  @Autowired private JCourseAssignmentRepository courseAssignmentRepository;
  @Autowired private JExamRepository examRepository;
  @Autowired private JGradeRepository gradeRepository;
  @Autowired private JTeacherRepository teacherRepository;
  @Autowired private JStudentRepository studentRepository;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private JwtService jwtService;
  @Autowired private GraduateService graduateService;
  @Autowired private GraduateXlsxWriter graduateXlsxWriter;

  @MockBean private BucketComponent bucketComponent;

  @LocalServerPort int port;
  private WebTestClient webTestClient;

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
            URI.create("https://dummy-bucket.s3.eu-west-3.amazonaws.com/graduates/list.xlsx")
                .toURL());
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
    return "grad-" + SEQUENCE.incrementAndGet() + "-" + UUID.randomUUID() + "@hei.school";
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
    return jwtService.generateToken(UUID.randomUUID(), uniqueEmail(), Role.TEACHER);
  }

  private String studentToken() {
    return jwtService.generateToken(UUID.randomUUID(), uniqueEmail(), Role.STUDENT);
  }

  private hei.student.schoolm.repository.model.JCohort saveCohort() {
    return cohortRepository.save(
        hei.student.schoolm.repository.model.JCohort.builder()
            .id(UUID.randomUUID())
            .ref("GRA-COH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
            .entryYear(2023)
            .build());
  }

  private JGroup saveGroup(hei.student.schoolm.repository.model.JCohort cohort, Track track) {
    return groupRepository.save(
        JGroup.builder()
            .id(UUID.randomUUID())
            .ref("GRA-GRP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
            .track(track)
            .cohort(cohort)
            .build());
  }

  private JStudent saveStudent(JGroup group, String reference) {
    return studentRepository.save(
        JStudent.builder()
            .id(UUID.randomUUID())
            .firstName("Alan")
            .lastName("Turing")
            .email(uniqueEmail())
            .password(passwordEncoder.encode("secret123"))
            .role(Role.STUDENT)
            .reference(reference)
            .group(group)
            .build());
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

  private JCourse saveCourse(Semester semester) {
    return courseRepository.save(
        JCourse.builder()
            .id(UUID.randomUUID())
            .ref("GRA-C-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
            .title("Course")
            .credit(4)
            .track(Track.COMMON)
            .semester(semester)
            .build());
  }

  private JCourseAssignment saveAssignment(JCourse course, JGroup group) {
    return courseAssignmentRepository.save(
        JCourseAssignment.builder()
            .id(UUID.randomUUID())
            .course(course)
            .group(group)
            .teachers(List.of(saveTeacher()))
            .academicYear(2024)
            .semester(course.getSemester())
            .credits(course.getCredit())
            .build());
  }

  private JExam saveExam(JCourseAssignment assignment, JStudent student, BigDecimal value) {
    var exam =
        examRepository.save(
            JExam.builder()
                .id(UUID.randomUUID())
                .course(assignment.getCourse())
                .dateExam(LocalDate.of(2025, 6, 1))
                .coefNumerator(1)
                .coefDenominator(1)
                .build());
    gradeRepository.save(
        JGrade.builder().id(UUID.randomUUID()).exam(exam).student(student).value(value).build());
    return exam;
  }

  @Test
  void adminCanRequestGraduateList() {
    var admin = saveAdmin();
    var cohort = saveCohort();
    var group = saveGroup(cohort, Track.TN);
    var student = saveStudent(group, "STD23001");
    var course = saveCourse(Semester.S1);
    var assignment = saveAssignment(course, group);
    saveExam(assignment, student, new BigDecimal("14.00"));

    var file =
        webTestClient
            .get()
            .uri("/cohorts/" + cohort.getRef() + "/graduates?track=TN")
            .header("Authorization", "Bearer " + adminToken(admin))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(GraduateFileDto.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(file);
    assertNotNull(file.getUrl());
    assertTrue(file.getUrl().contains("dummy-bucket"));
    assertTrue(file.getFileName().contains(cohort.getRef()));
  }

  @Test
  void nonAdminCannotRequestGraduateList() {
    var cohort = saveCohort();

    webTestClient
        .get()
        .uri("/cohorts/" + cohort.getRef() + "/graduates?track=TN")
        .header("Authorization", "Bearer " + teacherToken())
        .exchange()
        .expectStatus()
        .isForbidden();

    webTestClient
        .get()
        .uri("/cohorts/" + cohort.getRef() + "/graduates?track=TN")
        .header("Authorization", "Bearer " + studentToken())
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void unknownCohortReturnsNotFound() {
    var admin = saveAdmin();

    webTestClient
        .get()
        .uri("/cohorts/UNKNOWN-COHORT/graduates?track=TN")
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void graduatesAreRankedByDescendingAverage() {
    var cohort = saveCohort();
    var group = saveGroup(cohort, Track.TN);
    var course = saveCourse(Semester.S1);
    var assignment = saveAssignment(course, group);
    var top = saveStudent(group, "STD23002");
    var bottom = saveStudent(group, "STD23003");
    saveExam(assignment, top, new BigDecimal("18.00"));
    saveExam(assignment, bottom, new BigDecimal("11.00"));

    var graduates = graduateService.computeGraduates(cohort.getRef(), Track.TN, null, null);

    assertEquals(2, graduates.size());
    assertEquals(top.getReference(), graduates.get(0).studentRef());
    assertEquals(1, graduates.get(0).rank());
    assertEquals(bottom.getReference(), graduates.get(1).studentRef());
    assertEquals(2, graduates.get(1).rank());
    assertEquals(0, new BigDecimal("18.00").compareTo(graduates.get(0).average()));
  }

  @Test
  void failingStudentIsNotAGraduate() {
    var cohort = saveCohort();
    var group = saveGroup(cohort, Track.TN);
    var course = saveCourse(Semester.S1);
    var assignment = saveAssignment(course, group);
    var student = saveStudent(group, "STD23004");
    saveExam(assignment, student, new BigDecimal("8.00"));

    var graduates = graduateService.computeGraduates(cohort.getRef(), Track.TN, null, null);

    assertTrue(graduates.isEmpty());
  }

  @Test
  void graduatesAreSplitByTrack() {
    var cohort = saveCohort();
    var tnGroup = saveGroup(cohort, Track.TN);
    var elGroup = saveGroup(cohort, Track.EL);
    var tnCourse = saveCourse(Semester.S1);
    var elCourse = saveCourse(Semester.S1);
    var tnAssignment = saveAssignment(tnCourse, tnGroup);
    var elAssignment = saveAssignment(elCourse, elGroup);
    var tnStudent = saveStudent(tnGroup, "STD23005");
    var elStudent = saveStudent(elGroup, "STD23006");
    saveExam(tnAssignment, tnStudent, new BigDecimal("15.00"));
    saveExam(elAssignment, elStudent, new BigDecimal("16.00"));

    var tnGraduates = graduateService.computeGraduates(cohort.getRef(), Track.TN, null, null);
    var elGraduates = graduateService.computeGraduates(cohort.getRef(), Track.EL, null, null);

    assertEquals(1, tnGraduates.size());
    assertEquals(1, elGraduates.size());
    assertEquals(tnStudent.getReference(), tnGraduates.get(0).studentRef());
    assertEquals(elStudent.getReference(), elGraduates.get(0).studentRef());
  }

  @Test
  @SneakyThrows
  void exportedXlsxContainsHeadersAndStudentRows() {
    var cohort = saveCohort();
    var group = saveGroup(cohort, Track.TN);
    var course = saveCourse(Semester.S1);
    var assignment = saveAssignment(course, group);
    var student = saveStudent(group, "STD23007");
    saveExam(assignment, student, new BigDecimal("14.00"));

    var file = graduateService.generateGraduateList(cohort.getRef(), Track.TN, null, null);
    assertNotNull(file);

    var capturedKey = file.getFileName();
    assertTrue(capturedKey.contains(".xlsx"));

    var graduates = graduateService.computeGraduates(cohort.getRef(), Track.TN, null, null);
    var written = graduateXlsxWriter.write(graduates, "Diplomes TN");
    try (var workbook = new XSSFWorkbook(new FileInputStream(written))) {
      var sheet = workbook.getSheet("Diplomes TN");
      assertNotNull(sheet);
      assertEquals("Rang", sheet.getRow(0).getCell(0).getStringCellValue());
      assertEquals("STD", sheet.getRow(0).getCell(1).getStringCellValue());
      assertEquals("Nom", sheet.getRow(0).getCell(2).getStringCellValue());
      assertEquals("Prenom", sheet.getRow(0).getCell(3).getStringCellValue());
      assertEquals("Moyenne generale", sheet.getRow(0).getCell(4).getStringCellValue());
      assertEquals(1, sheet.getRow(1).getCell(0).getNumericCellValue());
      assertEquals("STD23007", sheet.getRow(1).getCell(1).getStringCellValue());
      assertEquals(14.0, sheet.getRow(1).getCell(4).getNumericCellValue());
    }
    written.delete();
  }

  @Test
  @SneakyThrows
  void downloadRedirectsToPresignedUrl() {
    var admin = saveAdmin();
    var cohort = saveCohort();
    var group = saveGroup(cohort, Track.TN);
    var course = saveCourse(Semester.S1);
    var assignment = saveAssignment(course, group);
    var student = saveStudent(group, "STD23008");
    saveExam(assignment, student, new BigDecimal("14.00"));

    webTestClient
        .get()
        .uri("/cohorts/" + cohort.getRef() + "/graduates/download?track=TN")
        .header("Authorization", "Bearer " + adminToken(admin))
        .exchange()
        .expectStatus()
        .value(status -> assertEquals(HttpStatus.FOUND.value(), status))
        .expectHeader()
        .value("Location", location -> assertTrue(location.startsWith("https://dummy-bucket")));
  }
}
