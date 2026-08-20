package hei.student.schoolm.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import hei.student.schoolm.dto.CourseAssignmentResponse;
import hei.student.schoolm.dto.CurriculumStatusResponse;
import hei.student.schoolm.endpoint.rest.security.JwtAuthenticationFilter;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.service.CourseAssignmentService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = CourseAssignmentController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class CourseAssignmentControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockBean private CourseAssignmentService courseAssignmentService;

  private static final UUID ASSIGNMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000001101");
  private static final UUID COURSE_ID = UUID.fromString("00000000-0000-0000-0000-000000001102");
  private static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000001103");
  private static final UUID TEACHER_ID = UUID.fromString("00000000-0000-0000-0000-000000001104");

  private CourseAssignmentResponse response() {
    return new CourseAssignmentResponse(
        ASSIGNMENT_ID,
        COURSE_ID,
        "PROG4",
        "Programmation 4",
        GROUP_ID,
        "K1",
        List.of(TEACHER_ID),
        2025,
        Semester.S3,
        8);
  }

  @Test
  void should_list_assignments_by_filter() throws Exception {
    var pageable = PageRequest.of(0, 10);
    when(courseAssignmentService.getByFilter(eq(GROUP_ID), any(), any(), any(), any()))
        .thenReturn(new PageImpl<>(List.of(response()), pageable, 1));

    mockMvc
        .perform(get("/course-assignments").param("groupId", GROUP_ID.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].courseRef").value("PROG4"))
        .andExpect(jsonPath("$.content[0].groupRef").value("K1"))
        .andExpect(jsonPath("$.content[0].academicYear").value(2025))
        .andExpect(jsonPath("$.content[0].semester").value("S3"))
        .andExpect(jsonPath("$.content[0].credits").value(8));
  }

  @Test
  void should_get_assignment_by_id() throws Exception {
    when(courseAssignmentService.getById(ASSIGNMENT_ID)).thenReturn(response());

    mockMvc
        .perform(get("/course-assignments/{id}", ASSIGNMENT_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(ASSIGNMENT_ID.toString()))
        .andExpect(jsonPath("$.courseRef").value("PROG4"));
  }

  @Test
  void should_return_404_when_assignment_missing() throws Exception {
    when(courseAssignmentService.getById(ASSIGNMENT_ID))
        .thenThrow(
            new NotFoundException("CourseAssignment with id: " + ASSIGNMENT_ID + " not found"));

    mockMvc
        .perform(get("/course-assignments/{id}", ASSIGNMENT_ID))
        .andExpect(status().isNotFound());
  }

  @Test
  void should_upsert_assignments() throws Exception {
    var body =
        """
[{"courseId":"%s","groupId":"%s","teacherIds":["%s"],"academicYear":2025,"semester":"S3","credits":8}]
"""
            .formatted(COURSE_ID, GROUP_ID, TEACHER_ID);
    when(courseAssignmentService.upsert(any())).thenReturn(List.of(response()));

    mockMvc
        .perform(put("/course-assignments").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].courseRef").value("PROG4"));
  }

  @Test
  void should_return_400_when_request_invalid() throws Exception {
    mockMvc
        .perform(put("/course-assignments").contentType(MediaType.APPLICATION_JSON).content("[{}]"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return_curriculum_status() throws Exception {
    var status =
        new CurriculumStatusResponse(Semester.S3, 30, 30, true, List.of(), List.of(response()));
    when(courseAssignmentService.getCurriculumStatus(GROUP_ID, 2025, Semester.S3))
        .thenReturn(status);

    mockMvc
        .perform(
            get("/course-assignments/curriculum-status")
                .param("groupId", GROUP_ID.toString())
                .param("academicYear", "2025")
                .param("semester", "S3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.semester").value("S3"))
        .andExpect(jsonPath("$.assignedCredits").value(30))
        .andExpect(jsonPath("$.complete").value(true));
  }

  @Test
  void should_delete_assignment() throws Exception {
    mockMvc
        .perform(delete("/course-assignments/{id}", ASSIGNMENT_ID))
        .andExpect(status().isNoContent());
  }

  @Test
  void should_return_404_when_deleting_missing_assignment() throws Exception {
    doThrow(new NotFoundException("CourseAssignment with id: " + ASSIGNMENT_ID + " not found"))
        .when(courseAssignmentService)
        .delete(ASSIGNMENT_ID);

    mockMvc
        .perform(delete("/course-assignments/{id}", ASSIGNMENT_ID))
        .andExpect(status().isNotFound());
  }
}
