package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jpabook.jpashop.Dto.ApiResponse;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.domain.CategoryItem;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.CustomStatusException;
import jpabook.jpashop.filter.CheckLogic;
import jpabook.jpashop.service.CategoryService;
import jpabook.jpashop.service.ItemService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static jpabook.jpashop.domain.item.Album.createAlbum;
import static jpabook.jpashop.domain.item.Book.createBook;
import static jpabook.jpashop.domain.item.Movie.createMovie;
import static jpabook.jpashop.filter.CheckLogic.adminCheck;

@RestController
@RequiredArgsConstructor
public class ItemApiController {

    private final ItemService itemService;
    private final CheckLogic checkLogic;
    private final CategoryService categoryService;

    /**
     * 상품생성
     * 공통 파라미터: category(2:BOOK,3:ALBUM,4:MOVIE),categoryIds(1:ITEM,2:BOOK,3:ALBUM,4:MOVIE),name,price,stockQuantity
     * 항목별 파라미터:
     * Book: author,isbn
     * Album: artist,etc
     * Movie: director,actor
     */
    @PostMapping("/api/item/new")
    public ApiResponse<CreateItemResponse> setting(@RequestBody @Valid CreateItemRequest request,
                                                   @SessionAttribute(name = "loginMember",required = false) Member loginMember){

        adminCheck(loginMember);

        Item item=null;

        if(request.getCategory()==2){
            checkLogic.validateDuplicateItem(request);
             item = createBook(request);
        }
        else if(request.getCategory()==3){
            checkLogic.validateDuplicateItem(request);
            item = createAlbum(request);
        }
        else if(request.getCategory()==4){
            checkLogic.validateDuplicateItem(request);
            item = createMovie(request);
        }
        else{
            throw new CustomStatusException(HttpStatus.BAD_REQUEST,"존재하지 않는 카테고리 입니다.");
        }


        if (request.getCategoryIds()!=null){
            for (Long categoryId : request.getCategoryIds()) {
                Category category = categoryService.findOne(categoryId);
                CategoryItem categoryItem = new CategoryItem();
                categoryItem.setCategory(category);
                item.addCategoryItem(categoryItem);
            }
        }
        Item newItem = itemService.saveItem(item);

        return new ApiResponse<>("success","상품 생성 성공.",new CreateItemResponse(newItem.getId(), newItem.getName()));
    }


    /**
     * 모든 상품 정보 조회
     */
    @GetMapping("/api/items")
    public ApiResponse<List<ItemDto>> itemStatus(@SessionAttribute(name = "loginMember",required = false)Member loginMember){
        adminCheck(loginMember);
        List<Item> items = itemService.findItems();
        List<ItemDto> result = new ArrayList<>();
        for (Item item : items) {
            result.add(new ItemDto(item));
        }
        return new ApiResponse<>("success","상품 조회 성공",result);

    }

    @Data
    @AllArgsConstructor
    public static class ItemDto{
        private Long id;
        private String name;
        private Integer price;
        private Integer stockQuantity;
        private List<String> categoryItems;

        public ItemDto(Item item){
            this.id = item.getId();
            this.name = item.getName();
            this.price = item.getPrice();
            this.stockQuantity = item.getStockQuantity();
            this.categoryItems = item.getCategoryItems().stream()
                    .filter(categoryItem ->categoryItem.getItem()!=null)
                    .map(categoryItem -> categoryItem.getCategory().getName())
                    .toList();
        }
    }

    @Data
    public static class CreateItemRequest{
        @NotNull(message = "카테고리는 필수 입력 항목입니다.")
        private Integer category;

        private List<Long> categoryIds;

        @NotBlank(message = "이름은 필수 입력 항목입니다.")
        private String name;
        @NotNull(message = "가격은 필수 입력 항목입니다.")
        @Min(value = 0,message = "0 이상의 수를 입력해 주세요.")
        private Integer price;
        @NotNull(message = "수량은 필수 입력 항목입니다.")
        @Min(value = 0,message = "0 이상의 수를 입력해 주세요.")
        private Integer stockQuantity;

        private String author;
        private String isbn;
        private String artist;
        private String etc;
        private String director;
        private String actor;

        @AssertTrue(message = "author,isbn은 필수 입력 항목입니다.")
        public boolean isBookFieldsValid(){
            if (category==null||category!=2) return true;
            return hasText(author)&&hasText(isbn);
        }
        @AssertTrue(message = "artist,etc는 필수 입력 항목입니다.")
        public boolean isAlbumFieldsValid(){
            if (category==null||category!=3) return true;
            return hasText(artist)&&hasText(etc);
        }
        @AssertTrue(message = "director,actor는 필수 입력 항목입니다.")
        public boolean isMovieFieldsValid(){
            if (category==null||category!=4) return true;
            return hasText(director)&&hasText(actor);
        }

        private boolean hasText(String string){
            return string !=null && !string.trim().isEmpty();
        }
    }



    @Data
    @AllArgsConstructor
    public static class CreateItemResponse{
        private Long id;
        private String name;
    }

}
