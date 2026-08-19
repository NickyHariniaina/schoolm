package hei.student.schoolm.endpoint.event.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hei.student.schoolm.dto.LevelRequest;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TranscriptEmailRequestedTest {
  @Test
  void should_create_transcript_email_requested() {
    var studentId = UUID.randomUUID();
    var level = LevelRequest.L1;

    var event = TranscriptEmailRequested.builder().studentId(studentId).level(level).build();

    assertEquals(studentId, event.getStudentId());
    assertEquals(level, event.getLevel());
  }

  @Test
  void should_have_max_consumer_duration() {
    var event = new TranscriptEmailRequested();
    assertEquals(Duration.ofSeconds(45), event.maxConsumerDuration());
  }

  @Test
  void should_have_max_consumer_backoff() {
    var event = new TranscriptEmailRequested();
    assertEquals(Duration.ofSeconds(30), event.maxConsumerBackoffBetweenRetries());
  }
}
