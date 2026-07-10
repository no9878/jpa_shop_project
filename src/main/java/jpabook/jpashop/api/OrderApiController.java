package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jpabook.jpashop.Dto.ApiResponse;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.filter.CheckLogic;
import jpabook.jpashop.repository.OrderRepositoryOld;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.MemberService;
import jpabook.jpashop.service.OrderService;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;
import static jpabook.jpashop.filter.CheckLogic.adminCheck;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepositoryOld orderRepositoryold;
    private final OrderQueryRepository orderQueryRepository;
    private final OrderService orderService;
    private final MemberService memberService;

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


    @Getter
    public static class Result{
    private List<OrdersResponse> result;

    private Result(List<OrdersResponse> orders){
        this.result = orders;
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


    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2(){
        List<Order> orders = orderRepositoryold.findAll(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return result;
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3(){
        List<Order> orders = orderRepositoryold.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(o-> new OrderDto(o))
                .collect(toList());

        return result;

    }

    /**
     * V3.1 엔티티를 조회해서 DTO로 변환 페이징 고려
     * - ToOne 관계만 우선 모두 페치 조인으로 최적화
     * - 컬렉션 관계는 hibernate.default_batch_fetch_size, @BatchSize로 최적화
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value = "offset",
    defaultValue = "0")int offset,
                                        @RequestParam(value = "limit",
                                                defaultValue = "100")int limit){
        List<Order> orders = orderRepositoryold.findAllWithMemberDelivery(offset,limit);
        List<OrderDto> result = orders.stream()
                .map(o->new OrderDto(o))
                .collect(toList());

        return result;
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4(){
        return orderQueryRepository.findOrderQueryDtos();


    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5(){
        return orderQueryRepository.findAllByDto_optimization();
    }

    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> orderV6(){
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        return flats.stream()
                .collect(groupingBy(o->new OrderQueryDto(o.getOrderId(),
                        o.getName(), o.getOrderDate(),o.getOrderStatus(),o.getAddress()),
                        mapping(o->new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(),o.getOrderPrice(),o.getCount()),toList())))
                .entrySet().stream()
                .map(e->new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(),e.getKey().getOrderDate(),e.getKey().getOrderStatus(),
                        e.getKey().getAddress(),e.getValue()))
                .collect(toList());
    }


    @Data
    static class OrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order){
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate =order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(toList());

        }
    }
    @Data
    static class OrderItemDto{
        private String itemName;
        private int orderPrice;
        private int count;
        public OrderItemDto(OrderItem orderItem){
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}

