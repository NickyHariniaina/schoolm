package hei.student.schoolm.util;

import hei.student.schoolm.repository.jpa.JStudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StdRefGenerator {
  private final JStudentRepository jStudentRepository;

  public String generate(int entryYear) {
    var prefix = "STD" + String.format("%02d", entryYear % 100);
    return jStudentRepository
        .findTopByReferenceStartingWithOrderByReferenceDesc(prefix)
        .map(
            student -> prefix + String.format("%03d", nextSequence(student.getReference(), prefix)))
        .orElse(prefix + "001");
  }

  private int nextSequence(String lastReference, String prefix) {
    return Integer.parseInt(lastReference.substring(prefix.length())) + 1;
  }
}
