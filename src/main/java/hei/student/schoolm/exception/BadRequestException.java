package hei.student.schoolm.exception;

import hei.student.schoolm.exception.model.ApiException;
import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {
  public BadRequestException(String message) {
    super(message, HttpStatus.BAD_REQUEST);
  }
}
