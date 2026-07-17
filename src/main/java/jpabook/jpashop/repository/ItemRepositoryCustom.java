package jpabook.jpashop.repository;

import jakarta.persistence.LockModeType;
import jpabook.jpashop.api.ItemApiController;
import jpabook.jpashop.domain.item.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;

public interface ItemRepositoryCustom {
    Item findOrderitemWithLock(String name, String categoryName);

    Page<Item> searchItems(Pageable pageable, ItemApiController.SearchFilter filter);
}
