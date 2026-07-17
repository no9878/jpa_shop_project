package jpabook.jpashop.service;

import jpabook.jpashop.api.OrderApiController.NewOrderRequest.OrderItems;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.exception.CustomStatusException;
import jpabook.jpashop.lock.OptimisticItemLock;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final ItemService itemService;
    private final OptimisticItemLock optimisticItemLock;


    @Transactional
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    public Order findOrder(Long id){
       return orderRepository.findById(id).orElseThrow(()->new CustomStatusException(HttpStatus.NOT_FOUND,"주문이 존재하지 않습니다."));
    }

    public Page<Order> findOrders(Pageable pageable, Long id){
        return orderRepository.findOrders(pageable, id);
    }


    @Transactional
    public Order createOrder(Long loginMemberId,Address address,List<OrderItems> orderItems) {

        Member member = memberRepository.findById(loginMemberId).orElseThrow(() -> new CustomStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 멤버입니다."));
        Delivery delivery = Delivery.createDelivery(address, DeliveryStatus.READY);


    List<OrderItem> list = orderItems.stream()
//            .map(dto -> optimisticItemLock.createOrderItemRetry(dto))
            .map(dto -> itemService.createOrderItem(dto))
            .toList();


        Order order = Order.createOrder(member, delivery, list);
        orderRepository.save(order);
        return order;
}
    @Transactional
    public Order createOrder(Long loginMemberId,Address address,OrderItem...orderItems) {

        Member member = memberRepository.findById(loginMemberId).orElseThrow(() -> new CustomStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 멤버입니다."));
        Delivery delivery = Delivery.createDelivery(address, DeliveryStatus.READY);


        List<OrderItem> list = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            list.add(orderItem);
        }


        Order order = Order.createOrder(member, delivery, list);
        orderRepository.save(order);
        return order;

    }
}
