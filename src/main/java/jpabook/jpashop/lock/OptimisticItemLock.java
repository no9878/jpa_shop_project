package jpabook.jpashop.lock;

import jpabook.jpashop.api.OrderApiController;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.exception.CustomStatusException;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OptimisticItemLock {

    private final ItemService itemService;

    public OrderItem createOrderItemRetry(OrderApiController.NewOrderRequest.OrderItems orderItems){
        int maxTries = 5;

        for (int attempt = 1; attempt <=maxTries; attempt++) {
            try {
                return itemService.createOrderItem(orderItems);
            }
            catch (ObjectOptimisticLockingFailureException e){
                if (attempt==maxTries){
                    throw new CustomStatusException(HttpStatus.CONFLICT,"주문량이 많아 처리할수 없습니다.");
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ex);
                }

            }
        }
        throw new CustomStatusException(HttpStatus.FORBIDDEN,"주문 처리 실패");
    }
}
