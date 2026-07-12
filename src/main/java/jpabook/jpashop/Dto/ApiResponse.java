package jpabook.jpashop.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jpabook.jpashop.api.OrderApiController;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;

    public ApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

}
