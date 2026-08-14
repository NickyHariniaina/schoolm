package hei.student.schoolm.endpoint.rest.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.mapper.CourseMapper;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Teacher;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.service.CourseService;
import hei.student.schoolm.utils.GroupTestUtils;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CourseController.class)
@Import(CourseMapper.class)
class CourseControllerTest {
  @Autowired MockMvc mockMvc;
  @MockBean CourseService courseService;

  private static final UUID COURSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID TEACHER_TOKY = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID TEACHER_YUME = UUID.fromString("00000000-0000-0000-0000-000000000003");
  private static final UUID GROUP_L1_EL_01 =
      UUID.fromString("00000000-0000-0000-0000-000000000004");
  private static final UUID GROUP_L1_EL_02 =
      UUID.fromString("00000000-0000-0000-0000-000000000005");

  private Course createCourse(UUID id, List<UUID> teacherIds) {
    var teachers = teacherIds.stream().map(this::createTeacher).toList();
    return Course.builder()
        .id(id)
        .ref("PROG4")
        .title("Exploitation dans le cloud")
        .credit(8)
        .track(Track.EL)
        .semester(Semester.S3)
        .teachers(teachers)
        .build();
  }

  private Teacher createTeacher(UUID id) {
    return Teacher.builder().id(id).build();
  }

  private Course createGroupedCourse(UUID id, List<UUID> groupIds) {
    var groups = groupIds.stream().map(GroupTestUtils::createGroup).toList();
    return Course.builder()
        .id(id)
        .ref("PROG4")
        .title("Exploitation dans le cloud")
        .credit(8)
        .track(Track.EL)
        .semester(Semester.S3)
        .groups(groups)
        .build();
  }

  @Test
  void should_assign_teacher_to_course() throws Exception {
    var course = createCourse(COURSE_ID, List.of(TEACHER_TOKY, TEACHER_YUME));
    when(courseService.assignTeachers(COURSE_ID, List.of(TEACHER_TOKY, TEACHER_YUME)))
        .thenReturn(course);

    mockMvc
        .perform(
            put("/courses/{courseId}/teacher", COURSE_ID)
                .contentType(APPLICATION_JSON)
                .content("{\"teacherIds\":[\"" + TEACHER_TOKY + "\",\"" + TEACHER_YUME + "\"]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(COURSE_ID.toString()))
        .andExpect(jsonPath("$.ref").value("PROG4"))
        .andExpect(jsonPath("$.title").value("Exploitation dans le cloud"))
        .andExpect(jsonPath("$.credit").value(8))
        .andExpect(jsonPath("$.track").value("EL"))
        .andExpect(jsonPath("$.semester").value("S3"))
        .andExpect(jsonPath("$.teacherIds[0]").value(TEACHER_TOKY.toString()))
        .andExpect(jsonPath("$.teacherIds[1]").value(TEACHER_YUME.toString()));

    verify(courseService).assignTeachers(COURSE_ID, List.of(TEACHER_TOKY, TEACHER_YUME));
  }

  @Test
  void should_return_404_with_nonexistent_teacher() throws Exception {
    var unknownTeacher = UUID.fromString("99999999-9999-9999-9999-999999999998");
    doThrow(new NotFoundException("Teacher not found: " + unknownTeacher))
        .when(courseService)
        .assignTeachers(COURSE_ID, List.of(unknownTeacher));

    mockMvc
        .perform(
            put("/courses/{courseId}/teacher", COURSE_ID)
                .contentType(APPLICATION_JSON)
                .content("{\"teacherIds\":[\"" + unknownTeacher + "\"]}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(containsString(unknownTeacher.toString())));
  }

  @Test
  void should_return_404_with_nonexistent_course() throws Exception {
    var unknownCourse = UUID.fromString("99999999-9999-9999-9999-999999999999");
    doThrow(new NotFoundException("Course not found: " + unknownCourse))
        .when(courseService)
        .assignTeachers(unknownCourse, List.of(TEACHER_TOKY));

    mockMvc
        .perform(
            put("/courses/{courseId}/teacher", unknownCourse)
                .contentType(APPLICATION_JSON)
                .content("{\"teacherIds\":[\"" + TEACHER_TOKY + "\"]}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(containsString(unknownCourse.toString())));
  }

  @Test
  void should_return_400_for_empty_ids() throws Exception {
    mockMvc
        .perform(
            put("/courses/{courseId}/teacher", COURSE_ID)
                .contentType(APPLICATION_JSON)
                .content("{\"teacherIds\":[]}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return_400_for_missing_ids() throws Exception {
    mockMvc
        .perform(
            put("/courses/{courseId}/teacher", COURSE_ID)
                .contentType(APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return_400_for_blank_teacher_id() throws Exception {
    mockMvc
        .perform(
            put("/courses/{courseId}/teacher", COURSE_ID)
                .contentType(APPLICATION_JSON)
                .content("{\"teacherIds\":[\"\"]}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_assign_group_to_course() throws Exception {
    var course = createGroupedCourse(COURSE_ID, List.of(GROUP_L1_EL_01, GROUP_L1_EL_02));
    when(courseService.assignGroups(COURSE_ID, List.of(GROUP_L1_EL_01, GROUP_L1_EL_02)))
        .thenReturn(course);

    mockMvc
        .perform(
            put("/courses/{courseId}/group", COURSE_ID)
                .contentType(APPLICATION_JSON)
                .content("{\"groupIds\":[\"" + GROUP_L1_EL_01 + "\",\"" + GROUP_L1_EL_02 + "\"]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(COURSE_ID.toString()))
        .andExpect(jsonPath("$.ref").value("PROG4"))
        .andExpect(jsonPath("$.title").value("Exploitation dans le cloud"))
        .andExpect(jsonPath("$.credit").value(8))
        .andExpect(jsonPath("$.track").value("EL"))
        .andExpect(jsonPath("$.semester").value("S3"))
        .andExpect(jsonPath("$.groupIds[0]").value(GROUP_L1_EL_01.toString()))
        .andExpect(jsonPath("$.groupIds[1]").value(GROUP_L1_EL_02.toString()));

    verify(courseService).assignGroups(COURSE_ID, List.of(GROUP_L1_EL_01, GROUP_L1_EL_02));
  }

  @Test
  void should_return_404_with_nonexistent_group() throws Exception {
    var unknownGroup = UUID.fromString("99999999-9999-9999-9999-999999999997");
    doThrow(new NotFoundException("Group not found: " + unknownGroup))
        .when(courseService)
        .assignGroups(COURSE_ID, List.of(unknownGroup));

    mockMvc
        .perform(
            put("/courses/{courseId}/group", COURSE_ID)
                .contentType(APPLICATION_JSON)
                .content("{\"groupIds\":[\"" + unknownGroup + "\"]}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(containsString(unknownGroup.toString())));
  }

  @Test
  void should_return_404_with_nonexistent_course_for_groups() throws Exception {
    var unknownCourse = UUID.fromString("99999999-9999-9999-9999-999999999999");
    doThrow(new NotFoundException("Course not found: " + unknownCourse))
        .when(courseService)
        .assignGroups(unknownCourse, List.of(GROUP_L1_EL_01));

    mockMvc
        .perform(
            put("/courses/{courseId}/group", unknownCourse)
                .contentType(APPLICATION_JSON)
                .content("{\"groupIds\":[\"" + GROUP_L1_EL_01 + "\"]}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(containsString(unknownCourse.toString())));
  }

  @Test
  void should_return_400_for_empty_group_ids() throws Exception {
    mockMvc
        .perform(
            put("/courses/{courseId}/group", COURSE_ID)
                .contentType(APPLICATION_JSON)
                .content("{\"groupIds\":[]}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return_400_for_missing_group_ids() throws Exception {
    mockMvc
        .perform(
            put("/courses/{courseId}/group", COURSE_ID).contentType(APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return_400_for_blank_group_id() throws Exception {
    mockMvc
        .perform(
            put("/courses/{courseId}/group", COURSE_ID)
                .contentType(APPLICATION_JSON)
                .content("{\"groupIds\":[\"\"]}"))
        .andExpect(status().isBadRequest());
  }
}
