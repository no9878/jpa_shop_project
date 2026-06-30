//package jpabook.jpashop;
//
//import jpabook.jpashop.domain.Address;
//import jpabook.jpashop.domain.Member;
//import jpabook.jpashop.domain.item.Book;
//import jpabook.jpashop.service.ItemService;
//import jpabook.jpashop.service.MemberService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//@Component
//@RequiredArgsConstructor
//public class Init {
//
//    private final MemberService memberService;
//    private final ItemService itemService;
//
//    @EventListener(ApplicationReadyEvent.class)
//    @Transactional
//    public void run() {
//        createMember();
//        createBook();
//    }
//
//    private void createMember() {
//        Member member = new Member();
//        member.setName("Kim");
//        Address address = new Address("goyang", "gogo", "1234");
//        member.setAddress(address);
//        memberService.join(member);
//        Member member2 = new Member();
//        member2.setName("Kim2");
//        Address address2 = new Address("goyang2", "gogo2", "1234");
//        member2.setAddress(address2);
//        memberService.join(member2);
//    }
//
//    private void createBook() {
//        Book book = new Book();
//        book.setName("book");
//        book.setPrice(1000);
//        book.setStockQuantity(10);
//        book.setAuthor("kim");
//        book.setIsbn("111");
//        itemService.saveItem(book);
//    }
//}
