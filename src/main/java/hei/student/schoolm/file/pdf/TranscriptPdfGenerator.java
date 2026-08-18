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

    var html = new StringBuilder();
    html.append("<html>");
    html.append("<head>");
    html.append("<style>");
    html.append("body { font-family: Arial, sans-serif; margin: 40px; color: #333; }");
    html.append(
        "h1 { text-align: center; color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom:"
            + " 10px; }");
    html.append(".header { margin-bottom: 30px; }");
    html.append(".header p { margin: 5px 0; }");
    html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
    html.append("th { background-color: #3498db; color: white; padding: 10px; text-align: left; }");
    html.append("td { padding: 8px 10px; border-bottom: 1px solid #ddd; }");
    html.append("tr:nth-child(even) { background-color: #f9f9f9; }");
    html.append(
        ".stats { margin-top: 20px; padding: 15px; background-color: #f1f9ff; border-left: 4px"
            + " solid #3498db; }");
    html.append(".stats p { margin: 5px 0; }");
    html.append(".status { font-weight: bold; color: ").append(statusColor).append("; }");
    html.append(
        ".footer { margin-top: 40px; font-size: 12px; color: #999; text-align: center; border-top:"
            + " 1px solid #eee; padding-top: 10px; }");
    html.append("</style>");
    html.append("</head>");
    html.append("<body>");
    html.append("<h1>RELEVÉ DE NOTES</h1>");
    html.append("<div class='header'>");
    html.append("<p><b>Étudiant :</b> ")
        .append(transcript.getFirstName())
        .append(" ")
        .append(transcript.getLastName())
        .append("</p>");
    html.append("<p><b>Référence :</b> ").append(transcript.getStudentRef()).append("</p>");
    html.append("<p><b>Promotion :</b> ").append(transcript.getCohortRef()).append("</p>");
    html.append("<p><b>Année académique :</b> ")
        .append(transcript.getAcademicYear())
        .append("</p>");
    html.append("<p><b>Statut :</b> <span class='status'>")
        .append(statusText)
        .append("</span></p>");
    html.append("</div>");
    html.append("<table>");
    html.append("<thead>");
    html.append("<tr>");
    html.append("<th>Cours</th>");
    html.append("<th style='text-align:center'>Crédits</th>");
    html.append("<th style='text-align:center'>Note finale</th>");
    html.append("</tr>");
    html.append("</thead>");
    html.append("<tbody>");
    html.append(rows);
    html.append("</tbody>");
    html.append("</table>");
    html.append("<div class='stats'>");
    html.append(statsHtml);
    html.append("</div>");
    html.append("<div class='footer'>");
    html.append("Relevé généré automatiquement le ").append(java.time.LocalDate.now());
    html.append("</div>");
    html.append("</body>");
    html.append("</html>");

    return html.toString();
  }
}
