package hei.student.schoolm.endpoint.rest.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hei.student.schoolm.endpoint.rest.CourseResponse;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.service.CourseService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CourseController.class)
public class CourseControllerTest {
  @Autowired MockMvc mockMvc;
  @MockBean CourseService courseService;

  private static final String COURSE_ID = "course-prog4";
  private static final String TEACHER_TOKY = "teacher-toky";
  private static final String TEACHER_YUME = "teacher-yume";
  private static final String TEACHER_IDS_BODY =
      "{\"teacherIds\":[\"" + TEACHER_TOKY + "\",\"" + TEACHER_YUME + "\"]}";

  @Test
  void should_assign_teacher_to_course() throws Exception {
    var courseResponse =
        new CourseResponse(
            COURSE_ID,
            "PROG4",
            "Exploitation dans le cloud",
            8,
            Track.EL,
            Semester.S3,
            List.of(TEACHER_TOKY, TEACHER_YUME));

    when(courseService.assignTeachers(COURSE_ID, List.of(TEACHER_TOKY, TEACHER_YUME)))
        .thenReturn(courseResponse);

    mockMvc
        .perform(
            put("/course/{courseId}/teacher", COURSE_ID)
                .contentType(APPLICATION_JSON)
                .content(TEACHER_IDS_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(COURSE_ID))
        .andExpect(jsonPath("$.ref").value("PROG4"))
        .andExpect(jsonPath("$.title").value("Exploitation dans le cloud"))
        .andExpect(jsonPath("$.credit").value(8))
        .andExpect(jsonPath("$.track").value("EL"))
        .andExpect(jsonPath("$.semester").value("S3"))
        .andExpect(jsonPath("$.teacherIds[0]").value(TEACHER_TOKY))
        .andExpect(jsonPath("$.teacherIds[1]").value(TEACHER_YUME));

    verify(courseService).assignTeachers(COURSE_ID, List.of(TEACHER_TOKY, TEACHER_YUME));
  }

  @Test
  void should_return_404_with_nonexistent_teacher() throws Exception {
    var unknownTeacher = "teacher-unknown";
    doThrow(new NotFoundException("Teacher not found: " + unknownTeacher))
        .when(courseService)
        .assignTeachers(COURSE_ID, List.of(unknownTeacher));

    mockMvc
        .perform(
            put("/course/{courseId}/teacher", COURSE_ID)
                .contentType(APPLICATION_JSON)
                .content("{\"teacherIds\":[\"" + unknownTeacher + "\"]}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(containsString(unknownTeacher)));
  }

  @Test
  void should_return_404_with_nonexistent_course() throws Exception {
    var unknownCourse = "course-unknown";
    doThrow(new NotFoundException("Course not found: " + unknownCourse))
        .when(courseService)
        .assignTeachers(unknownCourse, List.of(TEACHER_TOKY));

    mockMvc
        .perform(
            put("/course/{courseId}/teacher", unknownCourse)
                .contentType(APPLICATION_JSON)
                .content("{\"teacherIds\":[\"" + TEACHER_TOKY + "\"]}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(containsString(unknownCourse)));
  }

  @Test
  void should_return_400_for_empty_ids() throws Exception {
    mockMvc
        .perform(
            put("/course/{courseId}/teacher", COURSE_ID)
                .contentType(APPLICATION_JSON)
                .content("{\"teacherIds\":[]}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return_400_for_missing_ids() throws Exception {
    mockMvc
        .perform(
            put("/course/{courseId}/teacher", COURSE_ID)
                .contentType(APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return_400_for_blank_teacher_id() throws Exception {
    mockMvc
        .perform(
            put("/course/{courseId}/teacher", COURSE_ID)
                .contentType(APPLICATION_JSON)
                .content("{\"teacherIds\":[\"\"]}"))
        .andExpect(status().isBadRequest());
  }
}
