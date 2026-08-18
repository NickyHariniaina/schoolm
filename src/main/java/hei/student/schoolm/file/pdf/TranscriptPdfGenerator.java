package hei.student.schoolm.file.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import hei.student.schoolm.dto.TranscriptPdfDto;
import hei.student.schoolm.dto.TranscriptStatus;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
public class TranscriptPdfGenerator {

  @SneakyThrows
  public File generate(TranscriptPdfDto transcript) {
    var file = File.createTempFile("transcript", ".pdf");

    try (OutputStream os = new FileOutputStream(file)) {
      var html = buildHtml(transcript);
      var renderer =
          new PdfRendererBuilder().withHtmlContent(html, null).toStream(os).buildPdfRenderer();
      renderer.createPDF();
    }

    return file;
  }

  private String buildHtml(TranscriptPdfDto transcript) {
    var rows = new StringBuilder();
    for (var course : transcript.getCourses()) {
      var grade = course.getFinalGrade();
      rows.append("<tr>")
          .append("<td>")
          .append(course.getTitle())
          .append("</td>")
          .append("<td style='text-align:center'>")
          .append(course.getCredit())
          .append("</td>")
          .append("<td style='text-align:center'>")
          .append(grade != null ? grade : "-")
          .append("</td>")
          .append("</tr>");
    }

    var statsHtml = "";
    if (transcript.getAverage() != null) {
      var avg = BigDecimal.valueOf(transcript.getAverage()).setScale(2, RoundingMode.HALF_UP);
      statsHtml += "<p><b>Moyenne générale :</b> " + avg + "</p>";
    }
    statsHtml += "<p><b>Crédits totaux :</b> " + transcript.getTotalCredits() + "</p>";
    statsHtml += "<p><b>Crédits acquis :</b> " + transcript.getAcquiredCredits() + "</p>";

    var statusText =
        transcript.getStatus() == TranscriptStatus.COMPLET ? "COMPLET" : "INCOMPLET (en cours)";

    var statusColor = transcript.getStatus() == TranscriptStatus.COMPLET ? "#27ae60" : "#e67e22";

    return """
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; color: #333; }
        h1 { text-align: center; color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }
        .header { margin-bottom: 30px; }
        .header p { margin: 5px 0; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th { background-color: #3498db; color: white; padding: 10px; text-align: left; }
        td { padding: 8px 10px; border-bottom: 1px solid #ddd; }
        tr:nth-child(even) { background-color: #f9f9f9; }
        .stats { margin-top: 20px; padding: 15px; background-color: #f1f9ff; border-left: 4px solid #3498db; }
        .stats p { margin: 5px 0; }
        .status { font-weight: bold; color: %s; }
        .footer { margin-top: 40px; font-size: 12px; color: #999; text-align: center; border-top: 1px solid #eee; padding-top: 10px; }
    </style>
</head>
<body>
    <h1>RELEVÉ DE NOTES</h1>

    <div class="header">
        <p><b>Étudiant :</b> %s %s</p>
        <p><b>Référence :</b> %s</p>
        <p><b>Promotion :</b> %s</p>
        <p><b>Année académique :</b> %s</p>
        <p><b>Statut :</b> <span class="status">%s</span></p>
    </div>

    <table>
        <thead>
            <tr>
                <th>Cours</th>
                <th style="text-align:center">Crédits</th>
                <th style="text-align:center">Note finale</th>
            </tr>
        </thead>
        <tbody>
            %s
        </tbody>
    </table>

    <div class="stats">
        %s
    </div>

    <div class="footer">
        Relevé généré automatiquement le %s
    </div>
</body>
</html>
"""
        .formatted(
            statusColor,
            transcript.getFirstName(),
            transcript.getLastName(),
            transcript.getStudentRef(),
            transcript.getCohortRef(),
            transcript.getAcademicYear(),
            statusText,
            rows,
            statsHtml,
            java.time.LocalDate.now());
  }
}
