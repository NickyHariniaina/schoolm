package hei.student.schoolm.service;

import hei.student.schoolm.dto.GraduateEntry;
import hei.student.schoolm.dto.GraduateFileDto;
import hei.student.schoolm.exception.BadRequestException;
import hei.student.schoolm.file.bucket.BucketComponent;
import hei.student.schoolm.file.xlsx.GraduateXlsxWriter;
import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.model.Track;
import hei.student.schoolm.repository.GroupRepository;
import hei.student.schoolm.repository.StudentRepository;
import hei.student.schoolm.validator.CohortValidator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GraduateService {
  private static final Duration PRESIGN_DURATION = Duration.ofMinutes(15);
  private static final String XLSX_KEY_PREFIX = "graduates";

  private final CohortValidator cohortValidator;
  private final GroupRepository groupRepository;
  private final StudentRepository studentRepository;
  private final GroupFlowService groupFlowService;
  private final GraduateXlsxWriter graduateXlsxWriter;
  private final BucketComponent bucketComponent;

  public GraduateFileDto generateGraduateList(
      String cohortRef, Track track, Integer month, Integer year) {
    var graduates = computeGraduates(cohortRef, track, month, year);

    var bucketKey = XLSX_KEY_PREFIX + "/" + cohortRef + "_" + track.name() + ".xlsx";
    var fileName = "graduate-list_" + cohortRef + "_" + track.name() + ".xlsx";
    var sheetName = "Diplomes " + track.name();
    var file = graduateXlsxWriter.write(graduates, sheetName);
    try {
      bucketComponent.upload(file, bucketKey);
    } finally {
      file.delete();
    }
    var url = bucketComponent.presign(bucketKey, PRESIGN_DURATION);

    return GraduateFileDto.builder()
        .fileName(fileName)
        .url(url.toString())
        .expiresAt(Instant.now().plus(PRESIGN_DURATION))
        .build();
  }

  public List<GraduateEntry> computeGraduates(
      String cohortRef, Track track, Integer month, Integer year) {
    var cohort = cohortValidator.checkCohortExists(cohortRef);
    var currentSemester = resolveSemester(month, year, cohort.getEntryYear());

    var graduates = new ArrayList<GraduateEntry>();
    var groups =
        groupRepository.findAllByCohortId(cohort.getId()).stream()
            .filter(group -> group.getTrack() == track)
            .toList();
    for (var group : groups) {
      for (var student : studentRepository.findAllByGroupId(group.getId())) {
        var completedCourses = completedCourses(student, currentSemester);
        if (completedCourses.isEmpty()
            || completedCourses.stream().anyMatch(course -> !student.validate(course))) {
          continue;
        }
        graduates.add(
            new GraduateEntry(
                0,
                student.getReference(),
                student.getFirstName(),
                student.getLastName(),
                average(completedCourses, student)));
      }
    }

    graduates.sort(Comparator.comparing(GraduateEntry::average).reversed());
    var ranked = new ArrayList<GraduateEntry>(graduates.size());
    for (int i = 0; i < graduates.size(); i++) {
      var entry = graduates.get(i);
      ranked.add(
          new GraduateEntry(
              i + 1, entry.studentRef(), entry.firstName(), entry.lastName(), entry.average()));
    }
    return ranked;
  }

  private List<Course> completedCourses(Student student, Semester currentSemester) {
    var groups =
        groupRepository.findAllByIdWithCourses(groupFlowService.studentGroupIds(student.getId()));
    var coursesById = new LinkedHashMap<UUID, Course>();
    for (var group : groups) {
      if (group.getCourses() == null) {
        continue;
      }
      for (var course : group.getCourses()) {
        if (course.getSemester().ordinal() <= currentSemester.ordinal()
            && course.finalGradeFor(student) != null) {
          coursesById.put(course.getId(), course);
        }
      }
    }
    return List.copyOf(coursesById.values());
  }

  private BigDecimal average(List<Course> courses, Student student) {
    var totalCredit = courses.stream().mapToInt(Course::getCredit).sum();
    var weightedSum =
        courses.stream()
            .map(
                course ->
                    course.finalGradeFor(student).multiply(BigDecimal.valueOf(course.getCredit())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return weightedSum.divide(BigDecimal.valueOf(totalCredit), 2, RoundingMode.HALF_UP);
  }

  private Semester resolveSemester(Integer month, Integer year, Year entryYear) {
    if (month != null && (month < 1 || month > 12)) {
      throw new BadRequestException("month must be between 1 and 12");
    }
    if (month == null && year == null) {
      var today = LocalDate.now();
      return Semester.from(entryYear, today.getMonthValue(), today.getYear());
    }
    if (month == null || year == null) {
      throw new BadRequestException("month and year must both be provided");
    }
    return Semester.from(entryYear, month, year);
  }
}
