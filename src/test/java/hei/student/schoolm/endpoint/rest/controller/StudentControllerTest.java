package hei.student.schoolm.endpoint.rest.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hei.student.schoolm.dto.CourseValidationDto;
import hei.student.schoolm.dto.SemesterValidationDto;
import hei.student.schoolm.exception.NotFoundException;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.service.StudentService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StudentController.class)
class StudentControllerTest {
  @Autowired MockMvc mockMvc;
  @MockBean StudentService studentService;

  private static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID COURSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");

  @Test
  void should_return_semester_validation() throws Exception {
    var dto =
        SemesterValidationDto.builder()
            .studentId(STUDENT_ID)
            .studentRef("STD26001")
            .firstName("Tokyo")
            .lastName("Watt")
            .groupRef("L1-EL-01")
            .semester(Semester.S3)
            .totalCredits(8)
            .acquiredCredits(8)
            .validated(true)
            .courses(
                List.of(
                    CourseValidationDto.builder()
                        .courseId(COURSE_ID)
                        .ref("PROG4")
                        .title("Exploitation dans le cloud")
                        .credit(8)
                        .finalGrade(new BigDecimal("14.5"))
                        .acquired(true)
                        .build()))
            .build();
    when(studentService.getStudentSemesterValidation(STUDENT_ID, Semester.S3)).thenReturn(dto);

    mockMvc
        .perform(get("/students/{id}/semester-validation", STUDENT_ID).param("semester", "S3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.studentId").value(STUDENT_ID.toString()))
        .andExpect(jsonPath("$.studentRef").value("STD26001"))
        .andExpect(jsonPath("$.groupRef").value("L1-EL-01"))
        .andExpect(jsonPath("$.semester").value("S3"))
        .andExpect(jsonPath("$.totalCredits").value(8))
        .andExpect(jsonPath("$.acquiredCredits").value(8))
        .andExpect(jsonPath("$.validated").value(true))
        .andExpect(jsonPath("$.courses[0].ref").value("PROG4"))
        .andExpect(jsonPath("$.courses[0].credit").value(8))
        .andExpect(jsonPath("$.courses[0].finalGrade").value(14.5))
        .andExpect(jsonPath("$.courses[0].acquired").value(true));

    org.mockito.Mockito.verify(studentService)
        .getStudentSemesterValidation(STUDENT_ID, Semester.S3);
  }

  @Test
  void should_return_400_for_invalid_semester() throws Exception {
    mockMvc
        .perform(get("/students/{id}/semester-validation", STUDENT_ID).param("semester", "S7"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return_404_when_student_not_found() throws Exception {
    doThrow(new NotFoundException("Student " + STUDENT_ID + " not found"))
        .when(studentService)
        .getStudentSemesterValidation(STUDENT_ID, Semester.S3);

    mockMvc
        .perform(get("/students/{id}/semester-validation", STUDENT_ID).param("semester", "S3"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(containsString("Student " + STUDENT_ID)));
  }
}