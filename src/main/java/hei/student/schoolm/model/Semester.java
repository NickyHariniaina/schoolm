package hei.student.schoolm.model;

import hei.student.schoolm.exception.BadRequestException;
import java.time.Year;
import java.util.List;

public enum Semester {
  S1,
  S2,
  S3,
  S4,
  S5,
  S6;

  private static final int MAX_SEMESTER_NUMBER = 6;

  public static Semester from(Year entryYear, int month, int year) {
    int semesterNumber = ((year - entryYear.getValue()) * 12 + (month - 10)) / 6 + 1;
    if (semesterNumber < 1 || semesterNumber > MAX_SEMESTER_NUMBER) {
      throw new BadRequestException("Date is out of the valid semester range");
    }
    return values()[semesterNumber - 1];
  }

  public List<Semester> pair() {
    var firstIndex = ordinal() / 2 * 2;
    return List.of(values()[firstIndex], values()[firstIndex + 1]);
  }

  public String academicYear(Year entryYear) {
    var pairIndex = ordinal() / 2 + 1;
    var startYear = entryYear.getValue() + pairIndex - 1;
    return startYear + "-" + (startYear + 1);
  }
}