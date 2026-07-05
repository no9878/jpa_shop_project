package jpabook.jpashop.domain.item;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jpabook.jpashop.api.ItemApiController;
import jpabook.jpashop.api.ItemApiController.CreateItemRequest;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("M")
@Getter @Setter
public class Movie extends Item{

    private String director;
    private String actor;
    public static Movie createMovie(CreateItemRequest request){
        Movie movie = new Movie();
        movie.setName(request.getName());
        movie.setPrice(request.getPrice());
        movie.setStockQuantity(request.getStockQuantity());
        movie.setDirector(request.getDirector());
        movie.setActor(request.getActor());
        return movie;
    }
}
