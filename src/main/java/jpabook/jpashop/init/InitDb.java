package jpabook.jpashop.init;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.api.OrderApiController;
import jpabook.jpashop.api.OrderApiController.NewOrderRequest;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.CategoryService;
import jpabook.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static jpabook.jpashop.api.OrderApiController.NewOrderRequest.*;

@Component
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;

    @PostConstruct
    public void init(){
        initService.createCategoryAndItem();
        initService.adminInit();
        initService.dbInit1();
        initService.dbInit2();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager em;
        private final CategoryService categoryService;
        private final OrderService orderService;
        private Item item1;
        private Item item2;
        private Item item3;
        private Item item4;

            public void dbInit1 () {
                Member member = createMember("userA", "서울", "2", "2222", Role.GUEST);
                member.setPassword("2222");
                em.persist(member);

////                ArrayList<OrderItems>  list= new ArrayList<>();
////                OrderItems book1 = new OrderItems(item1.getName(), "BOOK", 1);
////                OrderItems book2 = new OrderItems(item2.getName(), "BOOK", 2);
////                list.add(book1);
////                list.add(book2);
////                NewOrderRequest request = new NewOrderRequest(member.getAddress(), list);
////                Order order = orderService.createOrder(2L, request.getAddress(), request.getOrderItems());
//                em.persist(order);

            }

            public void dbInit2 () {
                Member member = createMember("userB", "진주", "3", "3333", Role.GUEST);
                member.setPassword("3333");
                em.persist(member);

            }

            public void createCategoryAndItem(){
                Category category1 = createCategory("ITEM");
                Category category2 = createCategory("BOOK");
                Category category3 = createCategory("ALBUM");
                Category category4 = createCategory("MOVIE");
                category1.addChildCategory(category2);
                category1.addChildCategory(category3);
                category1.addChildCategory(category4);
                em.persist(category1);
                em.persist(category2);

                this.item1 = createBook("JPA1 BOOK", 10000, 100, "author1", "isbn1");
                createCategoryAndItem(category1, item1);
                createCategoryAndItem(category2, item1);
                em.persist(item1);

                this.item2 = createBook("JPA2 BOOK", 20000, 200, "author2", "isbn2");
                createCategoryAndItem(category1, item2);
                createCategoryAndItem(category2, item2);
                em.persist(item2);

                this.item3= createBook("SPRING1 BOOK", 30000, 300, "author3", "isbn3");
                createCategoryAndItem(category1,item3);
                createCategoryAndItem(category2,item3);
                em.persist(item3);
                this.item4 = createBook("SPRING2 BOOK", 40000, 300, "author3", "isbn4");
                createCategoryAndItem(category1,item4);
                createCategoryAndItem(category2,item4);
                em.persist(item4);
            }


            public void adminInit () {
                Member admin = createMember("ADMIN", "1", "1", "1", Role.ADMIN);
                admin.setPassword("1234");
                em.persist(admin);
            }



            private Category createCategory (String name){
                Category category = new Category();
                category.setName(name);
                categoryService.save(category);
                return category;
            }

            private static void createCategoryAndItem(Category category, Item item){
                CategoryItem categoryItem = new CategoryItem();
                categoryItem.setCategory(category);
                item.addCategoryItem(categoryItem);
            }

            private Member createMember (String name, String city, String street, String zipcode, Role role){
                Member member = new Member();
                member.setName(name);
                member.setAddress(new Address(city, street, zipcode));
                member.setRole(role);
                return member;
            }
            private Book createBook (String name,int price, int stockQuantity, String author, String isbn){
                Book book = new Book();
                book.setName(name);
                book.setPrice(price);
                book.setStockQuantity(stockQuantity);
                book.setAuthor(author);
                book.setIsbn(isbn);
                book.setLocalDateTime(LocalDateTime.now());
                return book;
            }
            private Delivery createDelivery (Member member){
                Delivery delivery = new Delivery();
                delivery.setAddress(member.getAddress());
                delivery.setStatus(DeliveryStatus.READY);
                return delivery;
            }

    }
}
