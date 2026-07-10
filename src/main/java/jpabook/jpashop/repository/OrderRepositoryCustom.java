package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryCustom {
    Page<Order> findOrders(Pageable pageable, Long id);

}
