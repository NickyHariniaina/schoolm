package hei.student.schoolm.endpoint.rest.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hei.student.schoolm.dto.TeacherRequest;
import hei.student.schoolm.dto.TeacherResponse;
import hei.student.schoolm.endpoint.rest.security.JwtAuthenticationFilter;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.service.TeacherService;
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
    value = TeacherController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class TeacherControllerTest {
  private static final UUID TEACHER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Autowired MockMvc mockMvc;
  @MockBean TeacherService teacherService;

  @Test
  void should_list_teachers() throws Exception {
    when(teacherService.getAll())
        .thenReturn(List.of(new TeacherResponse(TEACHER_ID, "John", "Doe", "john.doe@hei.school")));

    mockMvc
        .perform(get("/teachers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].firstName", is("John")))
        .andExpect(jsonPath("$[0].email", is("john.doe@hei.school")));
  }

  @Test
  void should_get_teacher_by_id() throws Exception {
    when(teacherService.getById(TEACHER_ID))
        .thenReturn(new TeacherResponse(TEACHER_ID, "John", "Doe", "john.doe@hei.school"));

    mockMvc
        .perform(get("/teachers/{id}", TEACHER_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(TEACHER_ID.toString())))
        .andExpect(jsonPath("$.lastName", is("Doe")));
  }

  @Test
  void should_return_404_when_teacher_missing() throws Exception {
    when(teacherService.getById(TEACHER_ID))
        .thenThrow(new NotFoundException("Teacher not found: " + TEACHER_ID));

    mockMvc.perform(get("/teachers/{id}", TEACHER_ID)).andExpect(status().isNotFound());
  }

  @Test
  void should_create_teacher() throws Exception {
    when(teacherService.upsert(
            new TeacherRequest(null, "John", "Doe", "john.doe@hei.school", "secret")))
        .thenReturn(new TeacherResponse(TEACHER_ID, "John", "Doe", "john.doe@hei.school"));

    mockMvc
        .perform(
            put("/teachers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@hei.school\",\"password\":\"secret\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName", is("John")));
  }

  @Test
  void should_return_400_when_creating_teacher_without_email() throws Exception {
    mockMvc
        .perform(
            put("/teachers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"password\":\"secret\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_delete_teacher() throws Exception {
    doNothing().when(teacherService).delete(TEACHER_ID);

    mockMvc.perform(delete("/teachers/{id}", TEACHER_ID)).andExpect(status().isNoContent());
  }
}
