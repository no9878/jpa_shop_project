package jpabook.jpashop.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.Dto.MembersDto;
import jpabook.jpashop.Dto.QMembersDto;
import jpabook.jpashop.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static jpabook.jpashop.domain.QMember.*;
import static jpabook.jpashop.domain.QOrder.order;

public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<MembersDto> members(Pageable pageable) {
        List<MembersDto> result = queryFactory
                .select(new QMembersDto(member.id,
                        member.name,
                        member.address,
                        member.role
                ))
                .from(member)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(member.count())
                .from(member)
                .fetchOne();

        return new PageImpl<>(result,pageable,total);


    }




}
