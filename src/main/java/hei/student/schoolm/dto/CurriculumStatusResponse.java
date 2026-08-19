package hei.student.schoolm.dto;

import hei.student.schoolm.model.Semester;
import java.util.List;

public record CurriculumStatusResponse(
    Semester semester,
    int assignedCredits,
    int targetCredits,
    boolean complete,
    List<CourseDto> missingCourses,
    List<CourseAssignmentResponse> assignments) {}
