package hei.student.schoolm.model;

import hei.student.schoolm.util.Fraction;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {
  private UUID id;
  private String ref;
  private String title;
  private int credit;
  private Track track;
  private Semester semester;
  private Instant createdAt;
  private Instant updatedAt;
  private List<Teacher> teachers;
  private List<Group> groups;
  private List<Exam> exams;
  private List<Grade> grades;

  public List<UUID> teacherIds() {
    return teachers == null ? List.of() : teachers.stream().map(Teacher::getId).toList();
  }

  public List<UUID> groupIds() {
    return groups == null ? List.of() : groups.stream().map(Group::getId).toList();
  }

  public Fraction coefficientSum() {
    if (exams == null || exams.isEmpty()) {
      return new Fraction(0, 1);
    }
    return exams.stream().map(Exam::getCoefficient).reduce(new Fraction(0, 1), Fraction::add);
  }

  public boolean isExamSetComplete() {
    return coefficientSum().isOne();
  }

  public boolean isCompleteFor(Semester currentSemester) {
    return semester.ordinal() <= currentSemester.ordinal() && isExamSetComplete();
  }

  public BigDecimal finalGradeFor(Student student) {
    if (exams == null || exams.isEmpty()) {
      return null;
    }
    var gradesByExam =
        grades == null
            ? Map.<UUID, BigDecimal>of()
            : grades.stream()
                .filter(
                    grade ->
                        grade.getStudent() != null
                            && grade.getStudent().getId().equals(student.getId()))
                .collect(
                    Collectors.toMap(
                        grade -> grade.getExam().getId(), Grade::getValue, (a, b) -> b));
    var total = BigDecimal.ZERO;
    for (var exam : exams) {
      var value = gradesByExam.getOrDefault(exam.getId(), BigDecimal.ZERO);
      total = total.add(BigDecimal.valueOf(value.doubleValue() * exam.getCoefficient().toDouble()));
    }
    return total;
  }
}
