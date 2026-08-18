package hei.student.schoolm.file.xlsx;

import hei.student.schoolm.dto.GraduateEntry;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class GraduateXlsxWriter {

  private static final String[] HEADERS = {"Rang", "STD", "Nom", "Prenom", "Moyenne generale"};

  @SneakyThrows
  public File write(List<GraduateEntry> entries, String sheetName) {
    var file = File.createTempFile("graduate-list", ".xlsx");
    try (var workbook = new XSSFWorkbook()) {
      var sheet = workbook.createSheet(sheetName);
      var headerRow = sheet.createRow(0);
      for (int i = 0; i < HEADERS.length; i++) {
        headerRow.createCell(i).setCellValue(HEADERS[i]);
      }
      for (int i = 0; i < entries.size(); i++) {
        var entry = entries.get(i);
        var row = sheet.createRow(i + 1);
        row.createCell(0).setCellValue(entry.rank());
        row.createCell(1).setCellValue(entry.studentRef());
        row.createCell(2).setCellValue(entry.lastName());
        row.createCell(3).setCellValue(entry.firstName());
        row.createCell(4).setCellValue(entry.average().setScale(2).doubleValue());
      }
      for (int i = 0; i < HEADERS.length; i++) {
        sheet.autoSizeColumn(i);
      }
      try (var output = new FileOutputStream(file)) {
        workbook.write(output);
      }
    }
    return file;
  }
}
