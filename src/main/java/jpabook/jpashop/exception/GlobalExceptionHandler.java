package jpabook.jpashop.exception;

import jpabook.jpashop.Dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomStatusException.class)
    public ResponseEntity<ApiResponse<String>> handleCustomStatusException(CustomStatusException e){
        log.error(e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(new ApiResponse<>("fail",e.getMessage()));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidException(MethodArgumentNotValidException e){
        log.error(e.getMessage());
        return ResponseEntity.status(e.getStatusCode()).body(new ApiResponse<>("fail",e.getBindingResult()
                .getAllErrors().get(0).getDefaultMessage()));
    }
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodValidation(HandlerMethodValidationException e){
        log.error(e.getMessage());
        return ResponseEntity.status(e.getStatusCode()).body(new ApiResponse<>("fail",e.getAllErrors().get(0).getDefaultMessage()));
    }
}
