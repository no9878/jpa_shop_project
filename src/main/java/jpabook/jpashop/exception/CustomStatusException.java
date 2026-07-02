package jpabook.jpashop.exception;

import org.aspectj.bridge.Message;
import org.springframework.http.HttpStatus;

public class CustomStatusException extends RuntimeException{
    private final HttpStatus status;

    public CustomStatusException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus(){
        return this.status;
    }
}
