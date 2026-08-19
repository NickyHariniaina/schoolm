package hei.student.schoolm.endpoint.rest.controller;

import static hei.student.schoolm.utils.TranscriptTestUtils.createTranscriptDto;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hei.student.schoolm.dto.CourseValidationDto;
import hei.student.schoolm.dto.GroupFlowDto;
import hei.student.schoolm.dto.MoveStudentGroupRequest;
import hei.student.schoolm.dto.SemesterValidationDto;
import hei.student.schoolm.dto.StudentRequest;
import hei.student.schoolm.dto.StudentResponse;
import hei.student.schoolm.endpoint.rest.security.JwtAuthenticationFilter;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.GroupFlowType;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.service.GroupFlowService;
import hei.student.schoolm.service.StudentService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = StudentController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class StudentControllerTest {
  @Autowired MockMvc mockMvc;
  @MockBean StudentService studentService;
  @MockBean GroupFlowService groupFlowService;

  private static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID COURSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");

  @Test
  void should_return_semester_validation() throws Exception {
    var dto =
        SemesterValidationDto.builder()
            .studentId(STUDENT_ID)
            .studentRef("STD26001")
            .firstName("Tokyo")
            .lastName("Watt")
            .groupRef("L1-EL-01")
            .semester(Semester.S3)
            .totalCredits(8)
            .acquiredCredits(8)
            .validated(true)
            .courses(
                List.of(
                    CourseValidationDto.builder()
                        .courseId(COURSE_ID)
                        .ref("PROG4")
                        .title("Exploitation dans le cloud")
                        .credit(8)
                        .finalGrade(new BigDecimal("14.5"))
                        .acquired(true)
                        .build()))
            .build();
    when(studentService.getStudentSemesterValidation(STUDENT_ID, Semester.S3)).thenReturn(dto);

    mockMvc
        .perform(get("/students/{id}/semester-validation", STUDENT_ID).param("semester", "S3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.studentId").value(STUDENT_ID.toString()))
        .andExpect(jsonPath("$.studentRef").value("STD26001"))
        .andExpect(jsonPath("$.groupRef").value("L1-EL-01"))
        .andExpect(jsonPath("$.semester").value("S3"))
        .andExpect(jsonPath("$.totalCredits").value(8))
        .andExpect(jsonPath("$.acquiredCredits").value(8))
        .andExpect(jsonPath("$.validated").value(true))
        .andExpect(jsonPath("$.courses[0].ref").value("PROG4"))
        .andExpect(jsonPath("$.courses[0].credit").value(8))
        .andExpect(jsonPath("$.courses[0].finalGrade").value(14.5))
        .andExpect(jsonPath("$.courses[0].acquired").value(true));

    verify(studentService).getStudentSemesterValidation(STUDENT_ID, Semester.S3);
  }

  @Test
  void should_return_400_for_invalid_semester() throws Exception {
    mockMvc
        .perform(get("/students/{id}/semester-validation", STUDENT_ID).param("semester", "S7"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return_404_when_student_not_found() throws Exception {
    doThrow(new NotFoundException("Student " + STUDENT_ID + " not found"))
        .when(studentService)
        .getStudentSemesterValidation(STUDENT_ID, Semester.S3);

    mockMvc
        .perform(get("/students/{id}/semester-validation", STUDENT_ID).param("semester", "S3"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(containsString("Student " + STUDENT_ID)));
  }

  @Test
  void should_return_transcript_when_month_and_year_provided() throws Exception {
    when(studentService.getTranscript(STUDENT_ID, 3, 2026)).thenReturn(createTranscriptDto());

    mockMvc
        .perform(
            get("/students/{id}/graduate-transcript", STUDENT_ID)
                .param("month", "3")
                .param("year", "2026"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLET"))
        .andExpect(jsonPath("$.studentRef").value("STD26001"))
        .andExpect(jsonPath("$.academicYear").value("2025-2026"))
        .andExpect(jsonPath("$.courses[0].ref").value("PROG4"));
  }

  @Test
  void should_default_to_today_when_no_params() throws Exception {
    when(studentService.getTranscript(STUDENT_ID, null, null)).thenReturn(createTranscriptDto());

    mockMvc
        .perform(get("/students/{id}/graduate-transcript", STUDENT_ID))
        .andExpect(status().isOk());
  }

  @Test
  void should_return_400_when_year_not_a_number() throws Exception {
    mockMvc
        .perform(
            get("/students/{id}/graduate-transcript", STUDENT_ID)
                .param("month", "3")
                .param("year", "abc"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return_404_when_student_not_found_for_transcript() throws Exception {
    when(studentService.getTranscript(STUDENT_ID, 3, 2026))
        .thenThrow(new NotFoundException("Student " + STUDENT_ID + " not found"));

    mockMvc
        .perform(
            get("/students/{id}/graduate-transcript", STUDENT_ID)
                .param("month", "3")
                .param("year", "2026"))
        .andExpect(status().isNotFound());
  }

  @Test
  void should_return_group_flow_history() throws Exception {
    var groupId = UUID.randomUUID();
    var flow =
        new GroupFlowDto(UUID.randomUUID(), STUDENT_ID, groupId, GroupFlowType.JOIN, Instant.now());
    when(groupFlowService.getHistory(STUDENT_ID)).thenReturn(List.of(flow));

    mockMvc
        .perform(get("/students/{id}/group-flows", STUDENT_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].studentId").value(STUDENT_ID.toString()))
        .andExpect(jsonPath("$[0].groupId").value(groupId.toString()))
        .andExpect(jsonPath("$[0].groupFlowType").value("JOIN"));
  }

  @Test
  void should_move_student_to_group() throws Exception {
    var groupId = UUID.randomUUID();
    var flow =
        new GroupFlowDto(UUID.randomUUID(), STUDENT_ID, groupId, GroupFlowType.JOIN, Instant.now());
    when(groupFlowService.move(eq(STUDENT_ID), any(MoveStudentGroupRequest.class)))
        .thenReturn(flow);

    mockMvc
        .perform(
            put("/students/{id}/group-flows", STUDENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"" + groupId + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.groupId").value(groupId.toString()))
        .andExpect(jsonPath("$.groupFlowType").value("JOIN"));
  }

  @Test
  void should_return_400_when_group_id_missing() throws Exception {
    mockMvc
        .perform(
            put("/students/{id}/group-flows", STUDENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_list_students() throws Exception {
    var response =
        new StudentResponse(
            STUDENT_ID,
            "STD26001",
            "Tokyo",
            "Watt",
            "t@hei.school",
            UUID.fromString("00000000-0000-0000-0000-000000000021"),
            "L1-EL-01");
    when(studentService.getAll()).thenReturn(List.of(response));

    mockMvc
        .perform(get("/students"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(STUDENT_ID.toString()))
        .andExpect(jsonPath("$[0].reference").value("STD26001"))
        .andExpect(jsonPath("$[0].firstName").value("Tokyo"))
        .andExpect(jsonPath("$[0].lastName").value("Watt"));

    verify(studentService).getAll();
  }

  @Test
  void should_get_student_by_id() throws Exception {
    var response =
        new StudentResponse(
            STUDENT_ID,
            "STD26001",
            "Tokyo",
            "Watt",
            "t@hei.school",
            UUID.fromString("00000000-0000-0000-0000-000000000021"),
            "L1-EL-01");
    when(studentService.getById(STUDENT_ID)).thenReturn(response);

    mockMvc
        .perform(get("/students/{id}", STUDENT_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(STUDENT_ID.toString()))
        .andExpect(jsonPath("$.reference").value("STD26001"));

    verify(studentService).getById(STUDENT_ID);
  }

  @Test
  void should_create_student() throws Exception {
    var response =
        new StudentResponse(
            STUDENT_ID,
            "STD26001",
            "Tokyo",
            "Watt",
            "t@hei.school",
            UUID.fromString("00000000-0000-0000-0000-000000000021"),
            "L1-EL-01");
    when(studentService.upsert(any(StudentRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            put("/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"firstName\":\"Tokyo\",\"lastName\":\"Watt\",\"email\":\"t@hei.school\","
                        + "\"password\":\"secret\",\"groupId\":\"00000000-0000-0000-0000-000000000021\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(STUDENT_ID.toString()))
        .andExpect(jsonPath("$.reference").value("STD26001"));

    verify(studentService).upsert(any(StudentRequest.class));
  }

  @Test
  void should_return_400_when_creating_student_without_email() throws Exception {
    mockMvc
        .perform(
            put("/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Tokyo\",\"lastName\":\"Watt\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_delete_student() throws Exception {
    mockMvc.perform(delete("/students/{id}", STUDENT_ID)).andExpect(status().isNoContent());

    verify(studentService).delete(STUDENT_ID);
  }
}
