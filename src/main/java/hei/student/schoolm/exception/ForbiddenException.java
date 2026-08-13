package hei.student.schoolm.exception;

import hei.student.schoolm.exception.model.ApiException;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {
  public ForbiddenException(String message) {
    super(message, HttpStatus.FORBIDDEN);
  }
}
