package jpabook.jpashop.repository;


import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
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

import static jpabook.jpashop.domain.QCategory.*;
import static jpabook.jpashop.domain.QCategoryItem.categoryItem;
import static jpabook.jpashop.domain.item.QBook.*;
import static jpabook.jpashop.domain.item.QItem.item;

public class ItemRepositoryImpl implements ItemRepositoryCustom{


   private final JPAQueryFactory queryFactory;

   public ItemRepositoryImpl(EntityManager em){
       this.queryFactory = new JPAQueryFactory(em);
   }

    @Override
    public Item findOrderitem(String name, String categoryName) {
        return queryFactory.select(item)
                .from(item)
                .join(item.categoryItems, categoryItem)
                .join(categoryItem.category,category)
                .where(item.name.eq(name),
                        category.name.eq(categoryName))
                .fetchFirst();
    }

    @Override
    public Page<Item> searchItems(Pageable pageable, String itemName, String categoryName, Integer maxPrice, Integer minPrice) {
        List<Item> result = queryFactory.select(item)
                .from(item)
                .join(item.categoryItems, categoryItem)
                .join(categoryItem.category, category)
                .where(itemNameContains(itemName),
                        categoryNameEq(categoryName),
                        itemPriceLoe(maxPrice),
                        itemPriceGoe(minPrice))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        JPAQuery<Long> count = queryFactory.select(item.count())
                .from(item)
                .join(item.categoryItems, categoryItem)
                .join(categoryItem.category, category)
                .where(itemNameContains(itemName),
                        categoryNameEq(categoryName),
                        itemPriceLoe(maxPrice),
                        itemPriceGoe(minPrice));

        return PageableExecutionUtils.getPage(result,pageable,count::fetchOne);

    }

    private BooleanExpression itemNameContains(String itemName){
       return StringUtils.hasText(itemName)?item.name.contains(itemName):null;
    }
    private BooleanExpression categoryNameEq(String categoryName){
       return StringUtils.hasText(categoryName)?category.name.eq(categoryName):null;
    }

    private BooleanExpression itemPriceLoe(Integer maxPrice){
       return maxPrice!=null?item.price.loe(maxPrice):null;
    }
    private BooleanExpression itemPriceGoe(Integer minPrice){
       return minPrice!=null?item.price.goe(minPrice) :null;
    }


}
