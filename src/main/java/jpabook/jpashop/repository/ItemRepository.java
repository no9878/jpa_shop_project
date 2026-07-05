package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item,Long>,ItemRepositoryCustom {
    List<Item> findByName(String name);
    Item findFirstByName(String name);
}
