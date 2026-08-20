package hei.student.schoolm.validator;

import hei.student.schoolm.dto.CourseAssignmentRequest;
import hei.student.schoolm.exception.BadRequestException;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.repository.CourseAssignmentRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseAssignmentValidator {
  private static final int MAX_CREDITS_PER_SEMESTER = 30;

  private final CourseAssignmentRepository repository;

  public void validateCurriculum(Course course, Group group, Semester semester) {
    if (course.getSemester() != semester) {
      throw new BadRequestException(
          "Course "
              + course.getRef()
              + " is a "
              + course.getSemester()
              + " course, not compatible with "
              + semester);
    }
    if (course.getTrack() != null
        && course.getTrack() != hei.student.schoolm.model.Track.COMMON
        && course.getTrack() != group.getTrack()) {
      throw new BadRequestException(
          "Course "
              + course.getRef()
              + " belongs to track "
              + course.getTrack()
              + " but the group "
              + group.getRef()
              + " is on track "
              + group.getTrack());
    }
  }

  public void validateNotDuplicate(
      UUID id, UUID courseId, UUID groupId, int academicYear, Semester semester) {
    boolean duplicate =
        repository.existsByCourseIdAndGroupIdAndAcademicYearAndSemester(
            courseId, groupId, academicYear, semester);
    if (duplicate && id == null) {
      throw new BadRequestException(
          "This course is already assigned to this group, for this academic year and semester");
    }
  }

  public void validateCreditCeilings(List<CourseAssignmentRequest> requests) {
    requests.stream()
        .map(r -> new GroupYearSemester(r.groupId(), r.academicYear(), r.semester()))
        .distinct()
        .forEach(triple -> validateCreditCeiling(triple, requests));
  }

  public int creditsPerSemester() {
    return MAX_CREDITS_PER_SEMESTER;
  }

  private void validateCreditCeiling(
      GroupYearSemester triple, List<CourseAssignmentRequest> requests) {
    var existing =
        repository.findByGroupIdAndAcademicYearAndSemester(
            triple.groupId(), triple.academicYear(), triple.semester());
    var replacedIds =
        requests.stream().filter(r -> r.id() != null).map(CourseAssignmentRequest::id).toList();
    int existingCredits =
        existing.stream()
            .filter(a -> !replacedIds.contains(a.getId()))
            .mapToInt(a -> a.getCredits())
            .sum();
    int incomingCredits =
        requests.stream()
            .filter(
                r ->
                    r.groupId().equals(triple.groupId())
                        && r.academicYear() == triple.academicYear()
                        && r.semester() == triple.semester())
            .mapToInt(CourseAssignmentRequest::credits)
            .sum();
    if (existingCredits + incomingCredits > MAX_CREDITS_PER_SEMESTER) {
      throw new BadRequestException(
          "Total credits "
              + (existingCredits + incomingCredits)
              + " exceed the "
              + MAX_CREDITS_PER_SEMESTER
              + "-credit ceiling for one semester");
    }
  }

  private record GroupYearSemester(UUID groupId, int academicYear, Semester semester) {}
}
