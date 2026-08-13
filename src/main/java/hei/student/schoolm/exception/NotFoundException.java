package hei.student.schoolm.exception;

import hei.student.schoolm.exception.model.ApiException;
import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {
  public NotFoundException(String message) {
    super(message, HttpStatus.NOT_FOUND);
  }
}
