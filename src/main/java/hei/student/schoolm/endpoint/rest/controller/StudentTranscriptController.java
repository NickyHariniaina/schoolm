package hei.student.schoolm.endpoint.rest.controller;

import hei.student.schoolm.dto.LevelRequest;
import hei.student.schoolm.endpoint.event.EventProducer;
import hei.student.schoolm.endpoint.event.model.TranscriptEmailRequested;
import hei.student.schoolm.util.SecurityUtil;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StudentTranscriptController {
  private final EventProducer<TranscriptEmailRequested> eventProducer;
  private final SecurityUtil securityUtil;

  @PostMapping("/students/{id}/transcript/email")
  public void emailTranscript(@PathVariable UUID id, @RequestParam LevelRequest level) {
    securityUtil.requireSelfOrAdmin(id);
    eventProducer.accept(List.of(new TranscriptEmailRequested(id, level)));
  }
}
