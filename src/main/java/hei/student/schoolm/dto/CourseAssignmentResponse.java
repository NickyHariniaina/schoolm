package hei.student.schoolm.dto;

import hei.student.schoolm.model.Semester;
import java.util.List;
import java.util.UUID;

public record CourseAssignmentResponse(
    UUID id,
    UUID courseId,
    String courseRef,
    String courseTitle,
    UUID groupId,
    String groupRef,
    List<UUID> teacherIds,
    int academicYear,
    Semester semester,
    int credits) {}
