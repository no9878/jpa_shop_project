package jpabook.jpashop.service;

import jpabook.jpashop.api.OrderApiController;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.CustomStatusException;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public Item saveItem(Item item){
        return itemRepository.save(item);
    }

    /**
     * 영속성 컨텍스트가 자동 변경
     */
    @Transactional
    public void updateItem(Long id, String name, int price, int stockQuantity){
        Item findItem = itemRepository.findById(id).orElseThrow(() -> new CustomStatusException(HttpStatus.NOT_FOUND, "해당 상품이 존재하지 않습니다."));
        findItem.setName(name);
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity);
    }

    public List<Item> findItems(){
        return itemRepository.findAll();
    }
    public Item findOne(Long itemId){
        return itemRepository.findById(itemId).orElseThrow(()-> new CustomStatusException(HttpStatus.NOT_FOUND,"해당 상품이 존재하지 않습니다."));
    }
    public Item findOrderItem(String name, String categoryName){
        return itemRepository.findOrderitem(name,categoryName);
    }

    @Transactional
    public OrderItem createOrderItem(OrderApiController.NewOrderRequest.OrderItems orderItems){
        Item item = findOrderItem(orderItems.getItemName(), orderItems.getCategoryName());
      return OrderItem.createOrderItem(item,item.getPrice(), orderItems.getQuantity());
    }
}
