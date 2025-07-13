package org.example.exeption;

import org.example.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerUrlNotFound (UrlNotFoundException ex) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .isSucceed(false)
                    .message(ex.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(PasswordUrlIncorrectException.class)
    public ResponseEntity<ErrorResponse> handlerPasswordUrlIncorrect (PasswordUrlIncorrectException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .isSucceed(false)
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
}
