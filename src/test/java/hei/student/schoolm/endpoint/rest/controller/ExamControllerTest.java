package hei.student.schoolm.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import hei.student.schoolm.dto.ExamDto;
import hei.student.schoolm.dto.ExamRequest;
import hei.student.schoolm.dto.GradeDto;
import hei.student.schoolm.dto.GradeRequest;
import hei.student.schoolm.endpoint.rest.security.JwtAuthenticationFilter;
import hei.student.schoolm.service.ExamService;
import hei.student.schoolm.service.GradeService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = ExamController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class ExamControllerTest {
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @MockBean ExamService examService;
  @MockBean GradeService gradeService;

  private static final UUID COURSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID EXAM_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
  private static final UUID GRADE_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");

  private ExamDto createExamDto() {
    return new ExamDto(
        EXAM_ID, COURSE_ID, LocalDate.of(2026, 3, 1), 1, 2, Instant.now(), Instant.now());
  }

  @Test
  void should_get_exam_by_id() throws Exception {
    when(examService.getById(EXAM_ID)).thenReturn(createExamDto());

    mockMvc
        .perform(get("/exams/{id}", EXAM_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(EXAM_ID.toString()))
        .andExpect(jsonPath("$.courseId").value(COURSE_ID.toString()))
        .andExpect(jsonPath("$.dateExam").value("2026-03-01"))
        .andExpect(jsonPath("$.coefNumerator").value(1))
        .andExpect(jsonPath("$.coefDenominator").value(2));

    verify(examService).getById(EXAM_ID);
  }

  @Test
  void should_create_exam() throws Exception {
    when(examService.upsert(any(ExamRequest.class))).thenReturn(createExamDto());

    mockMvc
        .perform(
            put("/exams")
                .contentType(APPLICATION_JSON)
                .content(
                    "{\"courseId\":\""
                        + COURSE_ID
                        + "\",\"dateExam\":\"2026-03-01\","
                        + "\"coefNumerator\":1,\"coefDenominator\":2}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(EXAM_ID.toString()))
        .andExpect(jsonPath("$.courseId").value(COURSE_ID.toString()));

    verify(examService).upsert(any(ExamRequest.class));
  }

  @Test
  void should_update_exam() throws Exception {
    when(examService.upsert(any(ExamRequest.class))).thenReturn(createExamDto());

    mockMvc
        .perform(
            put("/exams")
                .contentType(APPLICATION_JSON)
                .content(
                    "{\"id\":\""
                        + EXAM_ID
                        + "\",\"courseId\":\""
                        + COURSE_ID
                        + "\",\"dateExam\":\"2026-03-01\",\"coefNumerator\":1,\"coefDenominator\":1}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(EXAM_ID.toString()));

    verify(examService).upsert(any(ExamRequest.class));
  }

  @Test
  void should_return_400_when_exam_body_invalid() throws Exception {
    mockMvc
        .perform(put("/exams").contentType(APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_delete_exam() throws Exception {
    mockMvc.perform(delete("/exams/{id}", EXAM_ID)).andExpect(status().isNoContent());

    verify(examService).delete(EXAM_ID);
  }

  @Test
  void should_upsert_grades_for_exam() throws Exception {
    var gradeRequest = new GradeRequest(null, STUDENT_ID, new BigDecimal("15.0"), null);
    var gradeDto =
        new GradeDto(
            GRADE_ID,
            STUDENT_ID,
            EXAM_ID,
            new BigDecimal("15.0"),
            null,
            Instant.now(),
            Instant.now());
    when(gradeService.upsertGrades(any(UUID.class), any(List.class))).thenReturn(List.of(gradeDto));

    mockMvc
        .perform(
            put("/exams/{examId}/grades", EXAM_ID)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(gradeRequest))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(GRADE_ID.toString()))
        .andExpect(jsonPath("$[0].value").value(15.0));

    verify(gradeService).upsertGrades(EXAM_ID, any(List.class));
  }

  @Test
  void should_get_grades_for_exam() throws Exception {
    var gradeDto =
        new GradeDto(
            GRADE_ID,
            STUDENT_ID,
            EXAM_ID,
            new BigDecimal("15.0"),
            null,
            Instant.now(),
            Instant.now());
    when(gradeService.getGradesByExamId(EXAM_ID)).thenReturn(List.of(gradeDto));

    mockMvc
        .perform(get("/exams/{examId}/grades", EXAM_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(GRADE_ID.toString()));

    verify(gradeService).getGradesByExamId(EXAM_ID);
  }
}
