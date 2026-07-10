package jpabook.jpashop.repository;

import jpabook.jpashop.api.ItemApiController.CreateItemRequest;
import jpabook.jpashop.domain.item.Item;

public interface ItemRepositoryCustom {
    Item findOrderitem(String name, String CategoryName);
}
