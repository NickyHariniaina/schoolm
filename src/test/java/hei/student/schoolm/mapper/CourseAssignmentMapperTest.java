package hei.student.schoolm.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import hei.student.schoolm.dto.CourseAssignmentResponse;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.CourseAssignment;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Teacher;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CourseAssignmentMapperTest {
  private static final UUID ASSIGNMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000901");
  private static final UUID COURSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000902");
  private static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000903");
  private static final UUID TEACHER_ID = UUID.fromString("00000000-0000-0000-0000-000000000904");

  private final CourseAssignmentMapper mapper = new CourseAssignmentMapper();

  private CourseAssignment assignment() {
    return CourseAssignment.builder()
        .id(ASSIGNMENT_ID)
        .course(Course.builder().id(COURSE_ID).ref("PROG4").title("Programmation 4").build())
        .group(Group.builder().id(GROUP_ID).ref("K1").build())
        .teachers(List.of(Teacher.builder().id(TEACHER_ID).build()))
        .academicYear(2025)
        .semester(Semester.S3)
        .credits(8)
        .build();
  }

  @Test
  void toResponse_maps_all_fields() {
    CourseAssignmentResponse response = mapper.toResponse(assignment());

    assertEquals(ASSIGNMENT_ID, response.id());
    assertEquals(COURSE_ID, response.courseId());
    assertEquals("PROG4", response.courseRef());
    assertEquals("Programmation 4", response.courseTitle());
    assertEquals(GROUP_ID, response.groupId());
    assertEquals("K1", response.groupRef());
    assertEquals(List.of(TEACHER_ID), response.teacherIds());
    assertEquals(2025, response.academicYear());
    assertEquals(Semester.S3, response.semester());
    assertEquals(8, response.credits());
  }

  @Test
  void toResponse_handles_null_course_and_group() {
    var assignment =
        CourseAssignment.builder()
            .id(ASSIGNMENT_ID)
            .academicYear(2025)
            .semester(Semester.S3)
            .credits(8)
            .build();

    CourseAssignmentResponse response = mapper.toResponse(assignment);

    assertNull(response.courseId());
    assertNull(response.courseRef());
    assertNull(response.courseTitle());
    assertNull(response.groupId());
    assertNull(response.groupRef());
  }

  @Test
  void toResponseList_maps_each_element() {
    var responses = mapper.toResponseList(List.of(assignment()));

    assertEquals(1, responses.size());
    assertEquals(ASSIGNMENT_ID, responses.get(0).id());
  }

  @Test
  void toResponseList_returns_empty_when_input_null() {
    assertEquals(List.of(), mapper.toResponseList(null));
  }
}
