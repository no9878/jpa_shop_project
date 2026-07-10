package jpabook.jpashop.repository;


import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.QCategory;
import jpabook.jpashop.domain.QCategoryItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.item.QBook;
import jpabook.jpashop.domain.item.QItem;

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
}
