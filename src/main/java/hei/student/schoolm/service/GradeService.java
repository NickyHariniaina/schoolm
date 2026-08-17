package hei.student.schoolm.service;

import hei.student.schoolm.dto.GradeDto;
import hei.student.schoolm.dto.GradeHistoryDto;
import hei.student.schoolm.dto.UpdateGradeRequest;
import hei.student.schoolm.repository.GradeRepository;
import hei.student.schoolm.repository.jpa.JGradeHistoryRepository;
import hei.student.schoolm.repository.mapper.JGradeMapper;
import hei.student.schoolm.repository.model.JGradeHistory;
import hei.student.schoolm.validator.GradeValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GradeService {
  private final GradeRepository gradeRepository;
  private final GradeValidator gradeValidator;
  private final JGradeHistoryRepository gradeHistoryRepository;
  private final JGradeMapper jGradeMapper;

  @Transactional
  public GradeDto updateGrade(UUID gradeId, UpdateGradeRequest request) {

    var grade = gradeValidator.checkGradeExists(gradeId);
    var oldValue = grade.getValue();

    var history =
        JGradeHistory.builder()
            .id(UUID.randomUUID())
            .gradeId(gradeId)
            .studentId(grade.getStudent().getId())
            .examId(grade.getExam().getId())
            .oldValue(oldValue)
            .newValue(request.value())
            .changeReason(request.changeReason())
            .build();
    gradeHistoryRepository.save(history);

    grade.setValue(request.value());
    grade.setChangeReason(request.changeReason());
    var updatedGrade = gradeRepository.save(grade);

    return jGradeMapper.toDto(updatedGrade);
  }

  @Transactional(readOnly = true)
  public List<GradeHistoryDto> getGradeHistory(UUID gradeId) {

    gradeValidator.checkGradeExists(gradeId);

    var history = gradeHistoryRepository.findAllByGradeIdOrderByChangedAtDesc(gradeId);

    return history.stream().map(jGradeMapper::toHistoryDto).toList();
  }
}
