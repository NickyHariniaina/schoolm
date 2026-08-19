package hei.student.schoolm.endpoint.mvc.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import hei.student.schoolm.dto.CohortDto;
import hei.student.schoolm.dto.GraduateFileDto;
import hei.student.schoolm.endpoint.rest.security.JwtAuthenticationFilter;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.service.CohortService;
import hei.student.schoolm.service.GraduateService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = CohortViewController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class CohortViewControllerTest {

  @Autowired MockMvc mockMvc;

  @MockBean CohortService cohortService;

  @MockBean GraduateService graduateService;

  @Test
  void should_render_cohorts_page() throws Exception {
    when(cohortService.getCohorts())
        .thenReturn(List.of(CohortDto.builder().ref("P23").entryYear(2023).build()));

    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("cohorts"))
        .andExpect(model().attributeExists("cohorts"));
  }

  @Test
  void should_redirect_to_graduate_file_url() throws Exception {
    var url = "https://bucket.s3.amazonaws.com/graduates/P23_EL.xlsx";
    when(graduateService.generateGraduateList("P23", Track.EL, null, null))
        .thenReturn(
            GraduateFileDto.builder()
                .fileName("graduate-list_P23_EL.xlsx")
                .url(url)
                .expiresAt(Instant.now())
                .build());

    mockMvc
        .perform(get("/cohorts/{ref}/graduates/download", "P23").param("track", "EL"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(url));
  }
}
