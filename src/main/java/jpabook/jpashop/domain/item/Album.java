package jpabook.jpashop.domain.item;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jpabook.jpashop.api.ItemApiController;
import jpabook.jpashop.api.ItemApiController.CreateItemRequest;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("A")
@Getter @Setter
public class Album extends Item{

    private String artist;
    private String etc;

    public static Album createAlbum(CreateItemRequest request){
        Album album = new Album();
        album.setName(request.getName());
        album.setPrice(request.getPrice());
        album.setStockQuantity(request.getStockQuantity());
        album.setArtist(request.getArtist());
        album.setEtc(request.getEtc());
        return album;
    }
}
