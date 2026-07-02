package jpabook.jpashop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomStatusException.class)
    public ResponseEntity<String> handleCustomStatusException(CustomStatusException e){
        return ResponseEntity.status(e.getStatus()).body(e.getMessage());
    }
}
