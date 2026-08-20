package hei.student.schoolm.endpoint.rest.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hei.student.schoolm.dto.CohortDto;
import hei.student.schoolm.dto.CohortRequest;
import hei.student.schoolm.dto.GraduateFileDto;
import hei.student.schoolm.endpoint.rest.security.JwtAuthenticationFilter;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.service.CohortService;
import hei.student.schoolm.service.GraduateService;
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
    value = CohortController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class CohortControllerTest {
  @Autowired MockMvc mockMvc;
  @MockBean CohortService cohortService;
  @MockBean GraduateService graduateService;

  @Test
  void should_return_list_of_cohorts() throws Exception {
    when(cohortService.getCohorts())
        .thenReturn(
            List.of(
                CohortDto.builder().ref("J").entryYear(2023).build(),
                CohortDto.builder().ref("K").entryYear(2024).build()));

    mockMvc
        .perform(get("/cohorts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].ref", is("J")))
        .andExpect(jsonPath("$[0].entryYear", is(2023)));
  }

  @Test
  void should_return_graduate_file_url() throws Exception {
    var dto =
        GraduateFileDto.builder()
            .fileName("graduate-list_J_EL.xlsx")
            .url("https://bucket.s3.amazonaws.com/graduates/J_EL.xlsx")
            .expiresAt(Instant.parse("2026-08-18T12:00:00Z"))
            .build();
    when(graduateService.generateGraduateList("J", Track.EL, null, null)).thenReturn(dto);

    mockMvc
        .perform(get("/cohorts/{ref}/graduates", "J").param("track", "EL"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fileName", is("graduate-list_J_EL.xlsx")))
        .andExpect(jsonPath("$.url", is("https://bucket.s3.amazonaws.com/graduates/J_EL.xlsx")))
        .andExpect(jsonPath("$.expiresAt", is("2026-08-18T12:00:00Z")));
  }

  @Test
  void should_forward_month_and_year_to_service() throws Exception {
    when(graduateService.generateGraduateList("J", Track.TN, 8, 2026))
        .thenReturn(GraduateFileDto.builder().fileName("graduate-list_J_TN.xlsx").build());

    mockMvc
        .perform(
            get("/cohorts/{ref}/graduates", "J")
                .param("track", "TN")
                .param("month", "8")
                .param("year", "2026"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fileName", is("graduate-list_J_TN.xlsx")));
  }

  @Test
  void should_return_404_when_cohort_does_not_exist() throws Exception {
    when(graduateService.generateGraduateList("UNKNOWN", Track.EL, null, null))
        .thenThrow(new NotFoundException("Cohort UNKNOWN not found"));

    mockMvc
        .perform(get("/cohorts/{ref}/graduates", "UNKNOWN").param("track", "EL"))
        .andExpect(status().isNotFound());
  }

  @Test
  void should_return_400_when_track_is_invalid() throws Exception {
    mockMvc
        .perform(get("/cohorts/{ref}/graduates", "J").param("track", "INVALID"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_get_cohort_by_id() throws Exception {
    when(cohortService.getById(COHORT_ID))
        .thenReturn(CohortDto.builder().id(COHORT_ID).ref("J").entryYear(2024).build());

    mockMvc
        .perform(get("/cohorts/{id}", COHORT_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(COHORT_ID.toString())))
        .andExpect(jsonPath("$.ref", is("J")))
        .andExpect(jsonPath("$.entryYear", is(2024)));
  }

  @Test
  void should_return_404_when_getting_missing_cohort() throws Exception {
    when(cohortService.getById(COHORT_ID))
        .thenThrow(new NotFoundException("Cohort not found: " + COHORT_ID));

    mockMvc.perform(get("/cohorts/{id}", COHORT_ID)).andExpect(status().isNotFound());
  }

  @Test
  void should_upsert_cohort() throws Exception {
    when(cohortService.upsert(new CohortRequest(null, "promo", 2026)))
        .thenReturn(CohortDto.builder().id(COHORT_ID).ref("PROMO").entryYear(2026).build());

    mockMvc
        .perform(
            put("/cohorts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ref\":\"promo\",\"entryYear\":2026}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ref", is("PROMO")))
        .andExpect(jsonPath("$.entryYear", is(2026)));
  }

  @Test
  void should_return_400_when_upserting_without_required_fields() throws Exception {
    mockMvc
        .perform(put("/cohorts").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  private static final UUID COHORT_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");
}
