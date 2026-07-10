package jpabook.jpashop.service;

import jpabook.jpashop.api.OrderApiController;
import jpabook.jpashop.api.OrderApiController.NewOrderRequest.OrderItems;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.CustomStatusException;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderRepositoryOld;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final ItemService itemService;

    @Transactional
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Order createOrder(Long loginMemberId,Address address,List<OrderItems> orderItems) {

        Member member = memberRepository.findById(loginMemberId).orElseThrow(() -> new CustomStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 멤버입니다."));
        Delivery delivery = Delivery.createDelivery(address, DeliveryStatus.READY);


    List<OrderItem> list = orderItems.stream()
            .map(dto -> itemService.createOrderItem(dto))
            .toList();


    return Order.createOrder(member, delivery, list);
}
}
