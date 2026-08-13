package hei.student.schoolm.exception;

import hei.student.schoolm.exception.model.ApiException;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {
  public UnauthorizedException(String message) {
    super(message, HttpStatus.UNAUTHORIZED);
  }
}
