package jpabook.jpashop.service;

import jpabook.jpashop.api.OrderApiController.NewOrderRequest.OrderItems;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.CustomStatusException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderServiceTest {

    @Autowired OrderService orderService;
    @Autowired ItemService itemService;

    private final Address address = new Address("서울","1","1111");

    @Test
    void 여러상품_주문중_하나라도_재고부족이면_전체_롤백(){
        int stockBefore = itemService.findOne(1L).getStockQuantity();  // JPA1 BOOK
        int orderBefore = orderService.findAllOrders().size();
        List<OrderItems> request = List.of(
                new OrderItems("JPA1 BOOK","BOOK",1),
                new OrderItems("JPA2 BOOK","BOOK",100000));

        assertThrows(CustomStatusException.class,
                ()->orderService.createOrder(2L,address,request));

        assertThat(itemService.findOne(1L).getStockQuantity()).isEqualTo(stockBefore);
        assertThat(orderService.findAllOrders().size()).isEqualTo(orderBefore);
    }

    @Test
    void 동시에_재고보다_많이_주문해도_재고가_음수가_되지_않는다() throws Exception{
        int stock = 10;
        int threads = 20;

        Item item = itemService.findOne(3L);
        itemService.updateItem(3L,item.getName(),item.getPrice(),stock);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger stockFail = new AtomicInteger();
        AtomicInteger otherFail = new AtomicInteger();
        for (int i = 0; i < threads; i++) {
            pool.submit(()->{
                try {
                    ready.countDown();
                    start.await();
                    orderService.createOrder(2L,address,
                            List.of(new OrderItems("SPRING1 BOOK","BOOK",1)));
                    success.incrementAndGet();
                }catch (CustomStatusException e){
                    stockFail.incrementAndGet();
                }catch (Exception e){
                    otherFail.incrementAndGet();
                    e.printStackTrace();

                }finally {
                    done.countDown();
                }
            });
        }
        ready.await();
        start.countDown();
        assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        pool.shutdown();

        assertThat(success.get()).isEqualTo(stock);
        assertThat(stockFail.get()).isEqualTo(threads-stock);
        assertThat(otherFail.get()).isZero();
        assertThat(itemService.findOne(3L).getStockQuantity()).isZero();

    }

    @Test
    void 두_사용자가_서로_상품을_반대순서로_주문시_데드락_테스트() throws InterruptedException {
        int stock1 = 10;
        int stock2 = 20;
        int thread = 2;
        Item item1 = itemService.findOne(3L);
        Item item2 = itemService.findOne(4L);

        itemService.updateItem(3L,item1.getName(),item1.getPrice(),stock1);
        itemService.updateItem(4L,item2.getName(),item2.getPrice(),stock2);

        ExecutorService pool = Executors.newFixedThreadPool(thread);
        CountDownLatch ready = new CountDownLatch(thread);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(thread);
        AtomicInteger success = new AtomicInteger();


        pool.submit(()->{
            try {
                    ready.countDown();
                    start.await();
                    orderService.createOrder(2L,address,
                            List.of(new OrderItems("SPRING1 BOOK","BOOK",1),
                                    new OrderItems("SPRING2 BOOK","BOOK",1)));
                    success.incrementAndGet();
            }catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                done.countDown();
            }
        });
        pool.submit(()->{
            try {
                ready.countDown();
                start.await();
                orderService.createOrder(3L,address,
                        List.of(new OrderItems("SPRING2 BOOK","BOOK",1),
                                new OrderItems("SPRING1 BOOK","BOOK",1)));
                success.incrementAndGet();
            }catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                done.countDown();
            }
        });

        ready.await();
        start.countDown();
        assertThat(done.await(30,TimeUnit.SECONDS)).isTrue();
        pool.shutdown();
        assertThat(success.get()).isEqualTo(thread);
        assertThat(itemService.findOne(3L).getStockQuantity()).isEqualTo(8);
        assertThat(itemService.findOne(4L).getStockQuantity()).isEqualTo(18);
    }
}
