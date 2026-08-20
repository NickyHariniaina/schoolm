package hei.student.schoolm.service.event;

import hei.student.schoolm.dto.TranscriptPdfDto;
import hei.student.schoolm.dto.TranscriptStatus;
import hei.student.schoolm.endpoint.event.model.TranscriptEmailRequested;
import hei.student.schoolm.file.bucket.BucketComponent;
import hei.student.schoolm.file.pdf.TranscriptPdfGenerator;
import hei.student.schoolm.mail.Email;
import hei.student.schoolm.mail.Mailer;
import hei.student.schoolm.mapper.StudentMapper;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.service.StudentService;
import hei.student.schoolm.validator.StudentValidator;
import jakarta.mail.internet.InternetAddress;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TranscriptEmailRequestedService implements Consumer<TranscriptEmailRequested> {
  private static final Duration S3_URL_EXPIRATION = Duration.ofHours(1);
  private static final String PDF_KEY_PREFIX = "transcripts";

  private final StudentService service;
  private final StudentValidator validator;
  private final StudentMapper mapper;
  private final TranscriptPdfGenerator generator;
  private final BucketComponent bucketComponent;
  private final Mailer mailer;

  @SneakyThrows
  @Override
  public void accept(TranscriptEmailRequested event) {
    var transcript = service.getTranscriptForLevel(event.getStudentId(), event.getLevel());
    var student = validator.checkStudentExists(event.getStudentId());

    var group = service.getGroup(event.getStudentId());

    var semester = getSemesterForLevel(event.getLevel());

    var pdfDto = mapper.toPdfDto(student, group, semester, transcript.getCourses());

    var bucketKey =
        PDF_KEY_PREFIX + "/" + student.getReference() + "_" + transcript.getAcademicYear() + ".pdf";
    var file = generator.generate(pdfDto);

    URL presignedUrl;
    try {
      bucketComponent.upload(file, bucketKey);
      presignedUrl = bucketComponent.presign(bucketKey, S3_URL_EXPIRATION);
    } finally {
      file.delete();
    }

    var recipient = new InternetAddress(student.getEmail());
    mailer.accept(
        new Email(
            recipient,
            List.of(),
            List.of(),
            "Votre relevé de notes - " + transcript.getAcademicYear(),
            buildHtmlBody(pdfDto, presignedUrl),
            List.of()));
  }

  private Semester getSemesterForLevel(hei.student.schoolm.dto.LevelRequest level) {
    return switch (level) {
      case L1 -> Semester.S1;
      case L2 -> Semester.S3;
      case L3 -> Semester.S5;
    };
  }

  private String getLevelName(TranscriptPdfDto transcript) {
    var semesters = transcript.getSemesters();
    if (semesters == null || semesters.isEmpty()) {
      return "Niveau inconnu";
    }
    var firstSemester = semesters.get(0);
    return switch (firstSemester) {
      case S1, S2 -> "L1";
      case S3, S4 -> "L2";
      case S5, S6 -> "L3";
    };
  }

  private String buildHtmlBody(TranscriptPdfDto transcript, URL downloadUrl) {
    var rows =
        transcript.getCourses().stream()
            .map(
                c ->
                    "<tr><td>%s</td><td style='text-align:center'>%d</td><td style='text-align:center'>%s</td></tr>"
                        .formatted(
                            c.getTitle(),
                            c.getCredit(),
                            c.getFinalGrade() == null ? "-" : c.getFinalGrade()))
            .collect(Collectors.joining());

    if (transcript.getStatus() == TranscriptStatus.NOT_STARTED
        || transcript.getCourses().isEmpty()) {
      var levelName = getLevelName(transcript);
      var message =
          transcript.getStatus() == TranscriptStatus.NOT_STARTED
              ? "Le niveau "
                  + levelName
                  + " n'a pas encore commencé. Aucun cours n'est disponible pour le moment."
              : "Aucun cours trouvé pour ce niveau.";

      return """
             <html><body>
             <p>Bonjour %s,</p>
             <p>%s</p>
             <p><em>Relevé non disponible - niveau non commencé</em></p>
             </body></html>
             """
          .formatted(transcript.getFirstName(), message);
    }

    var statsHtml = "";
    if (transcript.getAverage() != null) {
      var avg = BigDecimal.valueOf(transcript.getAverage()).setScale(2, RoundingMode.HALF_UP);
      statsHtml += "<p><strong>Moyenne générale :</strong> " + avg + "</p>";
    }
    var creditsDisplay = transcript.getAcquiredCredits() + "/" + transcript.getTotalCredits();
    statsHtml += "<p><strong>Crédits acquis :</strong> " + creditsDisplay + "</p>";

    var coursesMessage =
        transcript.getCourses().isEmpty()
            ? "<p><em>Aucun cours trouvé pour ce niveau.</em></p>"
            : "";

    return """
           <html><body>
           <p>Bonjour %s,</p>
           <p>Voici votre relevé de notes pour l'année %s (%s).</p>
           %s
           <table border="1" cellpadding="4" style="border-collapse:collapse">
             <tr>
               <th style="border:1px solid #ddd;padding:8px">Cours</th>
               <th style="border:1px solid #ddd;padding:8px">Crédits</th>
               <th style="border:1px solid #ddd;padding:8px">Note finale</th>
             </tr>
             %s
           </table>
           <p><a href="%s">Télécharger le relevé complet (PDF)</a></p>
           </body></html>
           """
        .formatted(
            transcript.getFirstName(),
            transcript.getAcademicYear(),
            statusMessage(transcript),
            coursesMessage,
            statsHtml,
            rows,
            downloadUrl);
  }

  private String statusMessage(TranscriptPdfDto transcript) {
    if (transcript.getStatus() == TranscriptStatus.NOT_STARTED) {
      return "Niveau non commencé - Aucun cours disponible";
    }

    if (transcript.getCourses().isEmpty()) {
      return "Aucun cours disponible pour ce niveau";
    }
    var hasAnyGrade = transcript.getCourses().stream().anyMatch(c -> c.getFinalGrade() != null);
    if (!hasAnyGrade) {
      return "Aucune note disponible pour le moment";
    }
    return transcript.getStatus() == TranscriptStatus.COMPLET
        ? "Relevé complet"
        : "Relevé incomplet (en cours)";
  }
}
