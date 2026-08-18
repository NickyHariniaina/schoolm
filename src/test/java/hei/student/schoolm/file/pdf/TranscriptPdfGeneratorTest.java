package hei.student.schoolm.file.pdf;

import static org.junit.jupiter.api.Assertions.*;

import hei.student.schoolm.dto.CourseGradeDto;
import hei.student.schoolm.dto.TranscriptPdfDto;
import hei.student.schoolm.dto.TranscriptStatus;
import hei.student.schoolm.model.Semester;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TranscriptPdfGeneratorTest {
  private final TranscriptPdfGenerator generator = new TranscriptPdfGenerator();

  @Test
  void should_generate_pdf_file() {
    var transcript = createTranscriptDto();

    var file = generator.generate(transcript);

    assertNotNull(file);
    assertTrue(file.exists());
    assertTrue(file.length() > 0);
    assertTrue(file.getName().startsWith("transcript"));
    assertTrue(file.getName().endsWith(".pdf"));

    file.delete();
  }

  @Test
  void should_generate_pdf_with_all_data() {
    var transcript = createTranscriptDto();

    var file = generator.generate(transcript);

    assertNotNull(file);
    assertTrue(file.exists());
    assertTrue(file.length() > 1000);

    file.delete();
  }

  @Test
  void should_generate_pdf_with_null_average() {
    var transcript = createTranscriptDto();
    transcript.setAverage(null);

    var file = generator.generate(transcript);

    assertNotNull(file);
    assertTrue(file.exists());
    assertTrue(file.length() > 0);

    file.delete();
  }

  @Test
  void should_generate_pdf_with_empty_courses() {
    var transcript = createTranscriptDto();
    transcript.setCourses(List.of());

    var file = generator.generate(transcript);

    assertNotNull(file);
    assertTrue(file.exists());
    assertTrue(file.length() > 0);

    file.delete();
  }

  @Test
  void should_generate_pdf_with_null_grades() {
    var transcript = createTranscriptDto();
    var course =
        CourseGradeDto.builder()
            .courseId(UUID.randomUUID())
            .ref("PROG1")
            .title("Programmation 1 - Bases de la programmation")
            .credit(6)
            .finalGrade(null)
            .status(TranscriptStatus.INCOMPLET)
            .build();
    transcript.setCourses(List.of(course));

    var file = generator.generate(transcript);

    assertNotNull(file);
    assertTrue(file.exists());
    assertTrue(file.length() > 0);

    file.delete();
  }

  @Test
  void should_generate_pdf_with_incomplete_status() {
    var transcript = createTranscriptDto();
    transcript.setStatus(TranscriptStatus.INCOMPLET);

    var file = generator.generate(transcript);

    assertNotNull(file);
    assertTrue(file.exists());
    assertTrue(file.length() > 0);

    file.delete();
  }

  private TranscriptPdfDto createTranscriptDto() {
    var course1 =
        CourseGradeDto.builder()
            .courseId(UUID.randomUUID())
            .ref("PROG1")
            .title("Programmation 1 - Bases de la programmation")
            .credit(6)
            .finalGrade(new BigDecimal("15.5"))
            .status(TranscriptStatus.COMPLET)
            .build();

    var course2 =
        CourseGradeDto.builder()
            .courseId(UUID.randomUUID())
            .ref("WEB1")
            .title("Web 1 - Bases du developpement web")
            .credit(6)
            .finalGrade(new BigDecimal("12.0"))
            .status(TranscriptStatus.COMPLET)
            .build();

    return TranscriptPdfDto.builder()
        .studentId(UUID.randomUUID())
        .studentRef("STD26001")
        .firstName("Tokyo")
        .lastName("Watt")
        .groupRef("K1")
        .cohortRef("K")
        .academicYear("2024-2025")
        .semesters(List.of(Semester.S1, Semester.S2))
        .courses(List.of(course1, course2))
        .status(TranscriptStatus.COMPLET)
        .average(13.75)
        .totalCredits(30)
        .acquiredCredits(30)
        .build();
  }
}
