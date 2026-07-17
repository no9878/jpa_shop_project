package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jpabook.jpashop.Dto.ApiResponse;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.service.MemberService;
import jpabook.jpashop.service.OrderService;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static jpabook.jpashop.filter.CheckLogic.adminCheck;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderService orderService;

    /**
     * 주문 생성
     * 파라미터: address,orderItems(itemName,categoryName(최하위 카테고리 입력),quantity)
     */
    @PostMapping("/api/order/new")
    public ApiResponse<NewOrderResponse> newOrder(@SessionAttribute(name = "loginMember")Member loginMember,
            @RequestBody @Valid NewOrderRequest request){

        Order order = orderService.createOrder(loginMember.getId(), request.getAddress(),request.getOrderItems());

        return new ApiResponse<>("success","주문 생성 성공",new NewOrderResponse(order.getId(),order.getMember().getName(),order.getTotalPrice()));

    }

    /**
     * 모든 주문 조회
     */
    @GetMapping("/api/orders")
    public ApiResponse<Result> Orders(@SessionAttribute(name = "loginMember",required = false)Member loginMember){
        adminCheck(loginMember);

        List<Order> allOrders = orderService.findAllOrders();
        List<OrdersResponse> result = allOrders.stream()
                .map(order -> new OrdersResponse(order))
                .toList();
        Result result1 = new Result(result);
        return new ApiResponse<>("success","모든 주문 조회 성공",result1);

    }

    /**
     * 특정멤버 주문 조회
     */
    @GetMapping("/api/member-orders")
    public ApiResponse<Result> memberOrder(@SessionAttribute(name = "loginMember")Member loginMember,
                                                   Pageable pageable){

        Long id = loginMember.getId();
        Page<Order> orders = orderService.findOrders(pageable, id);
        List<OrdersResponse> list = orders.stream()
                .map(dto -> new OrdersResponse(dto))
                .toList();
        Result result = new Result(list);
        return new ApiResponse<>("success","주문 조회 성공.",result);
    }

    @Getter
    public static class Result{
        private List<OrdersResponse> result;

        public Result(List<OrdersResponse> orders){
            this.result = orders;
        }

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewOrderRequest{

        @NotNull
        private Address address;

        @NotEmpty
        private List<OrderItems> orderItems;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class OrderItems{
            @NotBlank
            private String itemName;
            @NotBlank
            private String categoryName;
            @NotNull
            @Min(value = 1)
            private Integer quantity;
        }
        }




    @Data
    @AllArgsConstructor
    public static class NewOrderResponse{
        private Long orderId;
        private String memberName;
        private Integer totalPrice;
    }

    @Getter
    @AllArgsConstructor
    public static class OrdersResponse{
        private Long orderId;
        private String memberName;
        private List<Dto> orderItems;
        private DeliveryStatus deliveryStatus;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Integer totalPrice;

        private OrdersResponse(Order order){
            this.orderId = order.getId();
            this.memberName = order.getMember().getName();
            this.orderItems = order.getOrderItems().stream()
                    .map(orderItem->new Dto(orderItem.getItem().getName(),orderItem.getOrderPrice(),orderItem.getCount()))
                    .toList();
            this.deliveryStatus = order.getDelivery().getStatus();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.totalPrice = order.getTotalPrice();
        }

        @Getter
        @AllArgsConstructor
        private static class Dto{
            private String name;
            private Integer price;
            private Integer quantity;
        }
    }



}

