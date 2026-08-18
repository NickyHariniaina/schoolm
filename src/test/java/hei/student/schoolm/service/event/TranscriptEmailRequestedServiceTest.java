package hei.student.schoolm.service.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import hei.student.schoolm.dto.CourseGradeDto;
import hei.student.schoolm.dto.TranscriptDto;
import hei.student.schoolm.dto.TranscriptPdfDto;
import hei.student.schoolm.dto.TranscriptStatus;
import hei.student.schoolm.endpoint.event.model.TranscriptEmailRequested;
import hei.student.schoolm.file.bucket.BucketComponent;
import hei.student.schoolm.file.pdf.TranscriptPdfGenerator;
import hei.student.schoolm.mail.Email;
import hei.student.schoolm.mail.Mailer;
import hei.student.schoolm.mapper.StudentMapper;
import hei.student.schoolm.model.Group;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.model.Student;
import hei.student.schoolm.service.StudentService;
import hei.student.schoolm.validator.StudentValidator;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TranscriptEmailRequestedServiceTest {
  @Mock private StudentService studentService;

  @Mock private StudentValidator studentValidator;

  @Mock private StudentMapper studentMapper;

  @Mock private TranscriptPdfGenerator pdfGenerator;

  @Mock private BucketComponent bucketComponent;

  @Mock private Mailer mailer;

  @InjectMocks private TranscriptEmailRequestedService service;

  private UUID studentId;
  private Student student;
  private Group group;
  private TranscriptDto transcriptDto;
  private TranscriptPdfDto pdfDto;
  private File pdfFile;

  @BeforeEach
  void setUp() throws Exception {
    studentId = UUID.randomUUID();

    group = Group.builder().id(UUID.randomUUID()).ref("K1").build();

    student =
        Student.builder()
            .id(studentId)
            .reference("STD26001")
            .firstName("Tokyo")
            .lastName("Watt")
            .email("tokyo.watt@mail.hei.school")
            .group(group)
            .build();

    var course =
        CourseGradeDto.builder()
            .courseId(UUID.randomUUID())
            .ref("PROG1")
            .title("Programmation 1 - Bases de la programmation")
            .credit(6)
            .finalGrade(new BigDecimal("15.5"))
            .status(TranscriptStatus.COMPLET)
            .build();

    transcriptDto =
        TranscriptDto.builder()
            .studentId(studentId)
            .studentRef("STD26001")
            .firstName("Tokyo")
            .lastName("Watt")
            .academicYear("2024-2025")
            .courses(List.of(course))
            .status(TranscriptStatus.COMPLET)
            .build();

    pdfDto =
        TranscriptPdfDto.builder()
            .studentId(studentId)
            .studentRef("STD26001")
            .firstName("Tokyo")
            .lastName("Watt")
            .academicYear("2024-2025")
            .courses(List.of(course))
            .status(TranscriptStatus.COMPLET)
            .average(15.5)
            .totalCredits(30)
            .acquiredCredits(30)
            .build();

    pdfFile = File.createTempFile("test", ".pdf");
  }

  @Test
  void should_send_transcript_email() throws Exception {
    var event = new TranscriptEmailRequested(studentId, Semester.S1);
    var presignedUrl = new URL("https://s3.amazonaws.com/transcript.pdf");

    when(studentService.getTranscriptForSemester(studentId, Semester.S1)).thenReturn(transcriptDto);
    when(studentValidator.checkStudentExists(studentId)).thenReturn(student);
    when(studentService.getGroup(studentId)).thenReturn(group);
    when(studentMapper.toPdfDto(any(), any(), any(), any())).thenReturn(pdfDto);
    when(pdfGenerator.generate(any(TranscriptPdfDto.class))).thenReturn(pdfFile);
    when(bucketComponent.presign(anyString(), any())).thenReturn(presignedUrl);

    service.accept(event);

    verify(studentService).getTranscriptForSemester(studentId, Semester.S1);
    verify(studentValidator).checkStudentExists(studentId);
    verify(studentService).getGroup(studentId);
    verify(studentMapper).toPdfDto(any(), any(), any(), any());
    verify(pdfGenerator).generate(any(TranscriptPdfDto.class));
    verify(bucketComponent).upload(any(File.class), anyString());
    verify(bucketComponent).presign(anyString(), any());
    verify(mailer).accept(any(Email.class));
  }

  @Test
  void should_delete_file_after_upload() throws Exception {
    var event = new TranscriptEmailRequested(studentId, Semester.S1);
    var presignedUrl = new URL("https://s3.amazonaws.com/transcript.pdf");
    var spyFile = spy(pdfFile);

    when(studentService.getTranscriptForSemester(studentId, Semester.S1)).thenReturn(transcriptDto);
    when(studentValidator.checkStudentExists(studentId)).thenReturn(student);
    when(studentService.getGroup(studentId)).thenReturn(group);
    when(studentMapper.toPdfDto(any(), any(), any(), any())).thenReturn(pdfDto);
    when(pdfGenerator.generate(any(TranscriptPdfDto.class))).thenReturn(spyFile);
    when(bucketComponent.presign(anyString(), any())).thenReturn(presignedUrl);

    service.accept(event);

    verify(spyFile).delete();
  }

  @Test
  void should_build_html_body_with_stats() throws Exception {
    var event = new TranscriptEmailRequested(studentId, Semester.S1);
    var presignedUrl = new URL("https://s3.amazonaws.com/transcript.pdf");

    when(studentService.getTranscriptForSemester(studentId, Semester.S1)).thenReturn(transcriptDto);
    when(studentValidator.checkStudentExists(studentId)).thenReturn(student);
    when(studentService.getGroup(studentId)).thenReturn(group);
    when(studentMapper.toPdfDto(any(), any(), any(), any())).thenReturn(pdfDto);
    when(pdfGenerator.generate(any(TranscriptPdfDto.class))).thenReturn(pdfFile);
    when(bucketComponent.presign(anyString(), any())).thenReturn(presignedUrl);

    var captor = ArgumentCaptor.forClass(Email.class);

    service.accept(event);

    verify(mailer).accept(captor.capture());
    var email = captor.getValue();
    assertNotNull(email);
    assertEquals("Votre relevé de notes - 2024-2025", email.subject());
    assertTrue(email.htmlBody().contains("Moyenne générale"));
    assertTrue(email.htmlBody().contains("Crédits totaux"));
    assertTrue(email.htmlBody().contains("Crédits acquis"));
  }
}
