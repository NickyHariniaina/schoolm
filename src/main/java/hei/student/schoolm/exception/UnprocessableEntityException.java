package hei.student.schoolm.exception;

import hei.student.schoolm.exception.model.ApiException;
import org.springframework.http.HttpStatus;

public class UnprocessableEntityException extends ApiException {
  public UnprocessableEntityException(String message) {
    super(message, HttpStatus.UNPROCESSABLE_ENTITY);
  }
}
