package jpabook.jpashop.domain.item;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jpabook.jpashop.api.ItemApiController;
import jpabook.jpashop.api.ItemApiController.CreateItemRequest;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("B")
@Getter @Setter
public class Book extends Item{

    private String author;
    private String isbn;

    public static Book createBook(CreateItemRequest request){
        Book book = new Book();
        book.setName(request.getName());
        book.setPrice(request.getPrice());
        book.setStockQuantity(request.getStockQuantity());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        return book;
    }
}
