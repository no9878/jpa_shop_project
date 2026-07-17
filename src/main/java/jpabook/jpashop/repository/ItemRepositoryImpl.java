package jpabook.jpashop.repository;


import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jpabook.jpashop.api.ItemApiController;
import jpabook.jpashop.domain.QCategory;
import jpabook.jpashop.domain.QCategoryItem;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.item.QBook;
import jpabook.jpashop.domain.item.QItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static jpabook.jpashop.api.ItemApiController.*;
import static jpabook.jpashop.domain.QCategory.*;
import static jpabook.jpashop.domain.QCategoryItem.categoryItem;
import static jpabook.jpashop.domain.item.QBook.*;
import static jpabook.jpashop.domain.item.QItem.item;

public class ItemRepositoryImpl implements ItemRepositoryCustom{

    private static final String LOCK_TIMEOUT="jakarta.persistence.lock.timeout";


   private final JPAQueryFactory queryFactory;

   public ItemRepositoryImpl(EntityManager em){
       this.queryFactory = new JPAQueryFactory(em);
   }

    @Override
    public Item findOrderitemWithLock(String name, String categoryName) {
        return queryFactory.select(item)
                .from(item)
                .join(item.categoryItems, categoryItem)
                .join(categoryItem.category,category)
                .where(item.name.eq(name),
                        category.name.eq(categoryName))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .setHint(LOCK_TIMEOUT,3000)
                .fetchFirst();
    }

    @Override
    public Page<Item> searchItems(Pageable pageable, SearchFilter filter) {
        List<Item> result = queryFactory.select(item)
                .from(item)
                .join(item.categoryItems, categoryItem)
                .join(categoryItem.category, category)
                .where(itemNameContains(filter),
                        categoryNameEq(filter),
                        itemPriceLoe(filter),
                        itemPriceGoe(filter))
                .orderBy(sortCustom(filter))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        JPAQuery<Long> count = queryFactory.select(item.count())
                .from(item)
                .join(item.categoryItems, categoryItem)
                .join(categoryItem.category, category)
                .where(itemNameContains(filter),
                        categoryNameEq(filter),
                        itemPriceLoe(filter),
                        itemPriceGoe(filter));

        return PageableExecutionUtils.getPage(result,pageable,count::fetchOne);

    }

    private OrderSpecifier<?> sortCustom(SearchFilter filter){
       if(filter.getSortFilter()==null) {
           return item.localDateTime.desc();
       }
    switch (filter.getSortFilter()){
        case "oldDate":
            return item.localDateTime.asc();
        case "highPrice":
            return item.price.desc();
        case "lowPrice":
            return item.price.asc();
        case "newDate":
        default:
            return item.localDateTime.desc();

    }

    }

    private BooleanExpression itemNameContains(SearchFilter filter){
       return StringUtils.hasText(filter.getItemName())?item.name.contains(filter.getItemName()):null;
    }
    private BooleanExpression categoryNameEq(SearchFilter filter){
       return StringUtils.hasText(filter.getCategoryName())?category.name.eq(filter.getCategoryName()):null;
    }

    private BooleanExpression itemPriceLoe(SearchFilter filter){
       return filter.getMaxPrice()!=null?item.price.loe(filter.getMaxPrice()):null;
    }
    private BooleanExpression itemPriceGoe(SearchFilter filter){
       return filter.getMinPrice()!=null?item.price.goe(filter.getMinPrice()) :null;
    }


}
