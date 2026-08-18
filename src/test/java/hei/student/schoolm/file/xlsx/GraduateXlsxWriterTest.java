package hei.student.schoolm.file.xlsx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hei.student.schoolm.dto.GraduateEntry;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.List;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class GraduateXlsxWriterTest {

  private final GraduateXlsxWriter writer = new GraduateXlsxWriter();

  @Test
  void should_write_graduate_entries_with_header_and_rows() throws Exception {
    var entries =
        List.of(
            new GraduateEntry(1, "STD24001", "Alice", "Durand", new BigDecimal("14.50")),
            new GraduateEntry(2, "STD24002", "Bob", "Martin", new BigDecimal("11.25")));

    var file = writer.write(entries, "Diplomes EL");

    try (var workbook = new XSSFWorkbook(new FileInputStream(file))) {
      var sheet = workbook.getSheet("Diplomes EL");
      assertEquals(3, sheet.getPhysicalNumberOfRows());
      assertEquals("Rang", sheet.getRow(0).getCell(0).getStringCellValue());
      assertEquals("STD", sheet.getRow(0).getCell(1).getStringCellValue());
      assertEquals("Nom", sheet.getRow(0).getCell(2).getStringCellValue());
      assertEquals("Prenom", sheet.getRow(0).getCell(3).getStringCellValue());
      assertEquals("Moyenne generale", sheet.getRow(0).getCell(4).getStringCellValue());

      assertEquals(1.0, sheet.getRow(1).getCell(0).getNumericCellValue());
      assertEquals("STD24001", sheet.getRow(1).getCell(1).getStringCellValue());
      assertEquals("Durand", sheet.getRow(1).getCell(2).getStringCellValue());
      assertEquals("Alice", sheet.getRow(1).getCell(3).getStringCellValue());
      assertEquals(14.5, sheet.getRow(1).getCell(4).getNumericCellValue());

      assertEquals(2.0, sheet.getRow(2).getCell(0).getNumericCellValue());
      assertEquals("STD24002", sheet.getRow(2).getCell(1).getStringCellValue());
      assertEquals(11.25, sheet.getRow(2).getCell(4).getNumericCellValue());
    } finally {
      file.delete();
    }
  }

  @Test
  void should_write_empty_sheet_with_header_only() throws Exception {
    var file = writer.write(List.of(), "Diplomes TN");

    try (var workbook = new XSSFWorkbook(new FileInputStream(file))) {
      var sheet = workbook.getSheet("Diplomes TN");
      assertEquals(1, sheet.getPhysicalNumberOfRows());
      assertEquals("Rang", sheet.getRow(0).getCell(0).getStringCellValue());
      assertEquals(CellType.STRING, sheet.getRow(0).getCell(0).getCellType());
      assertTrue(workbook.getNumberOfSheets() == 1);
    } finally {
      file.delete();
    }
  }
}
