package jpabook.jpashop.lock;

import jpabook.jpashop.api.OrderApiController;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.exception.CustomStatusException;
import jpabook.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class OrderFacade {
    private final OrderService orderService;
    private final AtomicInteger retryCount = new AtomicInteger();

    public Order createOrderWithRetry(Long memberId, Address address, List<OrderApiController.NewOrderRequest.OrderItems> items){
        int maxTries = 10;


        for (int attempt =1 ; attempt <=maxTries; attempt++) {
            try {
                Order order = orderService.createOrder(memberId,address,items);
                System.out.println(Thread.currentThread().getName()+" 성공, 시도 "+attempt+"회");
                return order;
            }
            catch (ObjectOptimisticLockingFailureException | CannotAcquireLockException e) {
                if (attempt==maxTries){
                    System.out.println(Thread.currentThread().getName()+" 포기, 시도 " + attempt + "회");
                    throw new CustomStatusException(HttpStatus.CONFLICT,"주문량이 많아 처리할수 없습니다.");
                }
                long backoff = 50 + ThreadLocalRandom.current().nextLong(0,51);
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ex);
                }
                retryCount.incrementAndGet();
            }

            }

        throw new CustomStatusException(HttpStatus.FORBIDDEN,"주문 처리 실패");
    }
    public int getRetryCount(){
        return retryCount.get();
    }

}


