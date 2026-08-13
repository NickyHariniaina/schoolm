package hei.student.schoolm.exception;

import hei.student.schoolm.exception.model.ApiException;
import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {
  public ConflictException(String message) {
    super(message, HttpStatus.CONFLICT);
  }
}
