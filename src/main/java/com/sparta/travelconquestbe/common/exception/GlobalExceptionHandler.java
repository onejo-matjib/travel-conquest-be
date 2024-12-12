package com.sparta.travelconquestbe.common.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
    return new ResponseEntity<>(
        ErrorResponse.builder()
            .errorCode(e.getErrorCode())
            .errorMessage(e.getErrorMessage())
            .httpStatus(e.getHttpStatus().value())
            .timestamp(System.currentTimeMillis())
            .build(),
        e.getHttpStatus());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    ErrorResponse response =
        new ErrorResponse("COMMON_001", errorMessage, 400, System.currentTimeMillis());
    return new ResponseEntity<>(response, HttpStatusCode.valueOf(response.getHttpStatus()));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(
      ConstraintViolationException ex) {
    String errorMessage = ex.getMessage();
    ErrorResponse response =
        new ErrorResponse("COMMON_001", errorMessage, 400, System.currentTimeMillis());
    return new ResponseEntity<>(response, HttpStatusCode.valueOf(response.getHttpStatus()));
  }
}
