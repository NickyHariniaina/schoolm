package hei.student.schoolm.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import hei.student.schoolm.endpoint.event.EventProducer;
import hei.student.schoolm.endpoint.event.model.TranscriptEmailRequested;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.util.SecurityUtil;
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
class StudentTranscriptControllerTest {
  @Mock private EventProducer<TranscriptEmailRequested> eventProducer;

  @Mock private SecurityUtil securityUtil;

  @InjectMocks private StudentTranscriptController controller;

  @BeforeEach
  void setUp() {
    org.mockito.Mockito.lenient().when(securityUtil.isAdmin()).thenReturn(true);
  }

  @Test
  void should_send_transcript_email_request_for_s1() {
    var studentId = UUID.randomUUID();
    var semester = Semester.S1;

    controller.emailTranscript(studentId, semester);

    var captor = ArgumentCaptor.forClass(List.class);
    verify(eventProducer).accept(captor.capture());

    var events = (List<TranscriptEmailRequested>) captor.getValue();
    assertEquals(1, events.size());
    assertEquals(studentId, events.get(0).getStudentId());
    assertEquals(Semester.S1, events.get(0).getSemester());
  }

  @Test
  void should_send_transcript_email_request_for_s4() {
    var studentId = UUID.randomUUID();
    var semester = Semester.S4;

    controller.emailTranscript(studentId, semester);

    var captor = ArgumentCaptor.forClass(List.class);
    verify(eventProducer).accept(captor.capture());

    var events = (List<TranscriptEmailRequested>) captor.getValue();
    assertEquals(1, events.size());
    assertEquals(studentId, events.get(0).getStudentId());
    assertEquals(Semester.S4, events.get(0).getSemester());
  }
}
