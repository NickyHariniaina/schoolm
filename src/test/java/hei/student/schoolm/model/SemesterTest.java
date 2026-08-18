package hei.student.schoolm.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hei.student.schoolm.exception.BadRequestException;
import java.time.Year;
import java.util.List;
import org.junit.jupiter.api.Test;

class SemesterTest {

  @Test
  void from_should_map_entry_year_and_reference_date_to_semester() {
    var entryYear = Year.of(2024);

    assertEquals(Semester.S1, Semester.from(entryYear, 10, 2024));
    assertEquals(Semester.S1, Semester.from(entryYear, 3, 2025));
    assertEquals(Semester.S3, Semester.from(entryYear, 10, 2025));
    assertEquals(Semester.S4, Semester.from(entryYear, 4, 2026));
    assertEquals(Semester.S5, Semester.from(entryYear, 10, 2026));
    assertEquals(Semester.S6, Semester.from(entryYear, 4, 2027));
  }

  @Test
  void from_should_throw_when_date_is_out_of_range() {
    var entryYear = Year.of(2024);

    assertThrows(BadRequestException.class, () -> Semester.from(entryYear, 4, 2024));
    assertThrows(BadRequestException.class, () -> Semester.from(entryYear, 10, 2027));
  }

  @Test
  void pair_should_return_semester_pairs() {
    assertEquals(List.of(Semester.S1, Semester.S2), Semester.S1.pair());
    assertEquals(List.of(Semester.S1, Semester.S2), Semester.S2.pair());
    assertEquals(List.of(Semester.S3, Semester.S4), Semester.S3.pair());
    assertEquals(List.of(Semester.S3, Semester.S4), Semester.S4.pair());
    assertEquals(List.of(Semester.S5, Semester.S6), Semester.S5.pair());
    assertEquals(List.of(Semester.S5, Semester.S6), Semester.S6.pair());
  }

  @Test
  void academicYear_should_map_semester_to_academic_year() {
    var entryYear = Year.of(2024);

    assertEquals("2024-2025", Semester.S1.academicYear(entryYear));
    assertEquals("2024-2025", Semester.S2.academicYear(entryYear));
    assertEquals("2025-2026", Semester.S3.academicYear(entryYear));
    assertEquals("2025-2026", Semester.S4.academicYear(entryYear));
    assertEquals("2026-2027", Semester.S5.academicYear(entryYear));
    assertEquals("2026-2027", Semester.S6.academicYear(entryYear));
  }
}
