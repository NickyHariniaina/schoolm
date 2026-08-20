package hei.student.schoolm.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hei.student.schoolm.dto.AdminRequest;
import hei.student.schoolm.dto.AdminResponse;
import hei.student.schoolm.endpoint.rest.security.JwtAuthenticationFilter;
import hei.student.schoolm.service.AdminService;
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
    value = AdminController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {
  @Autowired MockMvc mockMvc;
  @MockBean AdminService adminService;

  private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Test
  void should_get_admin_by_id() throws Exception {
    var response = new AdminResponse(ADMIN_ID, "Nicky", "Hariniaina", "admin@hei.school");
    when(adminService.getById(ADMIN_ID)).thenReturn(response);

    mockMvc
        .perform(get("/admins/{id}", ADMIN_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(ADMIN_ID.toString()))
        .andExpect(jsonPath("$.firstName").value("Nicky"))
        .andExpect(jsonPath("$.email").value("admin@hei.school"));

    verify(adminService).getById(ADMIN_ID);
  }

  @Test
  void should_update_admin() throws Exception {
    var response = new AdminResponse(ADMIN_ID, "NewName", "Hariniaina", "admin@hei.school");
    when(adminService.update(any(UUID.class), any(AdminRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            put("/admins/{id}", ADMIN_ID)
                .contentType(APPLICATION_JSON)
                .content(
                    "{\"firstName\":\"NewName\",\"lastName\":\"Hariniaina\","
                        + "\"email\":\"admin@hei.school\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(ADMIN_ID.toString()))
        .andExpect(jsonPath("$.firstName").value("NewName"));

    verify(adminService).update(any(UUID.class), any(AdminRequest.class));
  }

  @Test
  void should_return_400_when_admin_body_invalid() throws Exception {
    mockMvc
        .perform(put("/admins/{id}", ADMIN_ID).contentType(APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }
}
