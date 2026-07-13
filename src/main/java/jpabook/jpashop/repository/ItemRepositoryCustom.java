package jpabook.jpashop.repository;

import jpabook.jpashop.api.ItemApiController;
import jpabook.jpashop.domain.item.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemRepositoryCustom {
    Item findOrderitem(String name, String categoryName);
    Page<Item> searchItems(Pageable pageable, ItemApiController.SearchFilter filter);
}
