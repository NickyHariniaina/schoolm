package hei.student.schoolm.endpoint.rest.controller;

import static hei.student.schoolm.utils.TranscriptTestUtils.createTranscriptDto;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.service.TranscriptService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StudentController.class)
class StudentControllerTest {
  @Autowired MockMvc mockMvc;
  @MockBean TranscriptService service;

  private static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Test
  void should_return_transcript_when_month_and_year_provided() throws Exception {
    when(service.getTranscript(STUDENT_ID, 3, 2026)).thenReturn(createTranscriptDto());

    mockMvc
        .perform(
            get("/student/{id}/graduate-transcript", STUDENT_ID)
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
    when(service.getTranscript(STUDENT_ID, null, null)).thenReturn(createTranscriptDto());

    mockMvc.perform(get("/student/{id}/graduate-transcript", STUDENT_ID)).andExpect(status().isOk());
  }

  @Test
  void should_return_400_when_month_out_of_range() throws Exception {
    mockMvc
        .perform(
            get("/student/{id}/graduate-transcript", STUDENT_ID)
                .param("month", "13")
                .param("year", "2026"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return_404_when_student_not_found() throws Exception {
    when(service.getTranscript(STUDENT_ID, 3, 2026))
        .thenThrow(new NotFoundException("Student " + STUDENT_ID + " not found"));

    mockMvc
        .perform(
            get("/student/{id}/graduate-transcript", STUDENT_ID)
                .param("month", "3")
                .param("year", "2026"))
        .andExpect(status().isNotFound());
  }
}