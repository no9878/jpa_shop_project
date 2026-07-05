package jpabook.jpashop.exception;

import jpabook.jpashop.Dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomStatusException.class)
    public ResponseEntity<ApiResponse<String>> handleCustomStatusException(CustomStatusException e){
        return ResponseEntity.status(e.getStatus()).body(new ApiResponse<>("fail",e.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidException(MethodArgumentNotValidException e){
        return ResponseEntity.status(e.getStatusCode()).body(new ApiResponse<>("fail",e.getBindingResult()
                .getAllErrors().get(0).getDefaultMessage()));
    }
}
