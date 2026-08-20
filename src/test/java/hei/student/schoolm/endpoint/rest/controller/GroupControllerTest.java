package hei.student.schoolm.endpoint.rest.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hei.student.schoolm.dto.GroupRequest;
import hei.student.schoolm.dto.GroupResponse;
import hei.student.schoolm.dto.StudentResponse;
import hei.student.schoolm.endpoint.rest.security.JwtAuthenticationFilter;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.service.GroupService;
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
    value = GroupController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class GroupControllerTest {
  private static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID COHORT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Autowired MockMvc mockMvc;
  @MockBean GroupService groupService;

  @Test
  void should_list_groups() throws Exception {
    when(groupService.getAll(null))
        .thenReturn(List.of(new GroupResponse(GROUP_ID, COHORT_ID, "P24", "L1-EL-01", Track.EL)));

    mockMvc
        .perform(get("/groups"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].ref", is("L1-EL-01")))
        .andExpect(jsonPath("$[0].cohortRef", is("P24")))
        .andExpect(jsonPath("$[0].track", is("EL")));
  }

  @Test
  void should_list_groups_filtered_by_cohort() throws Exception {
    when(groupService.getAll(COHORT_ID)).thenReturn(List.of());

    mockMvc
        .perform(get("/groups").param("cohortId", COHORT_ID.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void should_get_group_by_id() throws Exception {
    when(groupService.getById(GROUP_ID))
        .thenReturn(new GroupResponse(GROUP_ID, COHORT_ID, "P24", "L1-EL-01", Track.EL));

    mockMvc
        .perform(get("/groups/{id}", GROUP_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(GROUP_ID.toString())))
        .andExpect(jsonPath("$.ref", is("L1-EL-01")));
  }

  @Test
  void should_return_404_when_group_missing() throws Exception {
    when(groupService.getById(GROUP_ID))
        .thenThrow(new NotFoundException("Group not found: " + GROUP_ID));

    mockMvc.perform(get("/groups/{id}", GROUP_ID)).andExpect(status().isNotFound());
  }

  @Test
  void should_list_students_of_group() throws Exception {
    when(groupService.getStudents(GROUP_ID))
        .thenReturn(
            List.of(
                StudentResponse.builder()
                    .id(UUID.randomUUID())
                    .reference("STD-1")
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@hei.school")
                    .groupId(GROUP_ID)
                    .groupRef("L1-EL-01")
                    .build()));

    mockMvc
        .perform(get("/groups/{id}/students", GROUP_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].reference", is("STD-1")))
        .andExpect(jsonPath("$[0].firstName", is("John")));
  }

  @Test
  void should_upsert_group() throws Exception {
    when(groupService.upsert(new GroupRequest(null, COHORT_ID, "l1-el-01", Track.EL)))
        .thenReturn(new GroupResponse(GROUP_ID, COHORT_ID, "P24", "L1-EL-01", Track.EL));

    mockMvc
        .perform(
            put("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"cohortId\":\"" + COHORT_ID + "\",\"ref\":\"l1-el-01\",\"track\":\"EL\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ref", is("L1-EL-01")))
        .andExpect(jsonPath("$.track", is("EL")));
  }

  @Test
  void should_return_400_when_upserting_without_required_fields() throws Exception {
    mockMvc
        .perform(put("/groups").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }
}
