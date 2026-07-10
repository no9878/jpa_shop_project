package jpabook.jpashop.repository;

import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static jpabook.jpashop.domain.QOrder.order;

public class OrderRepositoryImpl implements OrderRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public OrderRepositoryImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Order> findOrders(Pageable pageable,Long id) {
        List<Order> result = queryFactory.selectFrom(order)
                .where(order.member.id.eq(id))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(order.count())
                .from(order)
                .where(order.member.id.eq(id));


        return PageableExecutionUtils.getPage(result,pageable,countQuery::fetchOne);

    }
}
