package hei.student.schoolm.utils;

import hei.student.schoolm.model.Cohort;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Student;
import java.time.Year;
import java.util.List;
import java.util.UUID;

public final class StudentTestUtils {
  private StudentTestUtils() {}

  public static Student createStudent(Group group) {
    return Student.builder()
        .id(TranscriptTestUtils.STUDENT_ID)
        .reference("STD26001")
        .firstName("Tokyo")
        .lastName("Watt")
        .group(group)
        .build();
  }

  public static Group createGroup(Cohort cohort, List<Course> courses) {
    return Group.builder()
        .id(TranscriptTestUtils.GROUP_ID)
        .ref("L1-EL-01")
        .cohort(cohort)
        .courses(courses)
        .build();
  }

  public static Group createGroup(UUID id, String ref) {
    return Group.builder().id(id).ref(ref).build();
  }

  public static Cohort createCohort(int entryYear) {
    return Cohort.builder()
        .id(TranscriptTestUtils.COHORT_ID)
        .ref("P24")
        .entryYear(Year.of(entryYear))
        .build();
  }
}
