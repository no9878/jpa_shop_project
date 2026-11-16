package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item,Long>,ItemRepositoryCustom {
    List<Item> findByName(String name);
    Item findFirstByName(String name);
    @Modifying
    @Query("update Item i set i.stockQuantity = i.stockQuantity - :qty "+
    "where i.id = :id and i.stockQuantity >= :qty")
    int decreaseStock(@Param("id") Long id, @Param("qty")int qty);
}
