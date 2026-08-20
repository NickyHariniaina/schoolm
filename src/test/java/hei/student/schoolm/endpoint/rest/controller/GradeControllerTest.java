package hei.student.schoolm.endpoint.rest.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hei.student.schoolm.dto.GradeDto;
import hei.student.schoolm.dto.GradeHistoryDto;
import hei.student.schoolm.endpoint.rest.security.JwtAuthenticationFilter;
import hei.student.schoolm.service.GradeService;
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
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = GradeController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
public class GradeControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private GradeService gradeService;

  private final UUID gradeId = UUID.randomUUID();
  private final UUID studentId = UUID.randomUUID();
  private final UUID examId = UUID.randomUUID();

  @Test
  void should_get_grade_by_id() throws Exception {
    var gradeDto =
        new GradeDto(
            gradeId, studentId, examId, new BigDecimal("15.0"), null, Instant.now(), Instant.now());
    when(gradeService.getGradeById(gradeId)).thenReturn(gradeDto);

    mockMvc
        .perform(get("/grades/{gradeId}", gradeId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(gradeId.toString()))
        .andExpect(jsonPath("$.studentId").value(studentId.toString()))
        .andExpect(jsonPath("$.examId").value(examId.toString()))
        .andExpect(jsonPath("$.value").value(15.0));

    verify(gradeService).getGradeById(gradeId);
  }

  @Test
  void should_delete_grade() throws Exception {
    mockMvc.perform(delete("/grades/{gradeId}", gradeId)).andExpect(status().isNoContent());

    verify(gradeService).delete(gradeId);
  }

  @Test
  void should_get_grade_history() throws Exception {
    var history =
        List.of(
            new GradeHistoryDto(
                UUID.randomUUID(),
                gradeId,
                studentId,
                examId,
                new BigDecimal("15.0"),
                new BigDecimal("18.5"),
                "Correction following a complaint",
                Instant.now()));

    when(gradeService.getGradeHistory(gradeId)).thenReturn(history);

    mockMvc
        .perform(get("/grades/{gradeId}/history", gradeId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].gradeId").value(gradeId.toString()))
        .andExpect(jsonPath("$[0].oldValue").value(15.0))
        .andExpect(jsonPath("$[0].newValue").value(18.5))
        .andExpect(jsonPath("$[0].changeReason").value("Correction following a complaint"));
  }
}
