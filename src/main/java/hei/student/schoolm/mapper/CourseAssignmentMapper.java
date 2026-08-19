package hei.student.schoolm.mapper;

import hei.student.schoolm.dto.CourseAssignmentResponse;
import hei.student.schoolm.model.CourseAssignment;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CourseAssignmentMapper {
  public CourseAssignmentResponse toResponse(CourseAssignment assignment) {
    return new CourseAssignmentResponse(
        assignment.getId(),
        assignment.getCourse() == null ? null : assignment.getCourse().getId(),
        assignment.getCourse() == null ? null : assignment.getCourse().getRef(),
        assignment.getCourse() == null ? null : assignment.getCourse().getTitle(),
        assignment.getGroup() == null ? null : assignment.getGroup().getId(),
        assignment.getGroup() == null ? null : assignment.getGroup().getRef(),
        assignment.teacherIds(),
        assignment.getAcademicYear(),
        assignment.getSemester(),
        assignment.getCredits());
  }

  public List<CourseAssignmentResponse> toResponseList(List<CourseAssignment> assignments) {
    if (assignments == null) {
      return List.of();
    }
    return assignments.stream().map(this::toResponse).toList();
  }
}
