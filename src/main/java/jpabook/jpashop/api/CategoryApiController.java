package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jpabook.jpashop.Dto.ApiResponse;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.domain.CategoryItem;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.filter.CheckLogic;
import jpabook.jpashop.service.CategoryService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static jpabook.jpashop.filter.CheckLogic.adminCheck;

@RestController
@RequiredArgsConstructor
public class CategoryApiController {

    private final CategoryService categoryService;
    private final CheckLogic checkLogic;

    /**
     * 카테고리 생성
     * 필수파라미터: name
     * 선택파라미터: isChild,parentId
     */
    @PostMapping("/api/category/new")
    public ApiResponse<NewCategoryResponse> createCategory(@SessionAttribute(name = "loginMember",required = false)Member loginMember,
                                                           @RequestBody @Valid NewCategoryRequest request){
        adminCheck(loginMember);
        Category category = new Category();
        category.setName(request.getName());


        if (request.getIsChild() != null && request.getIsChild() == 1) {
            Category parentCategory = categoryService.findParentCategory(request.getParentId());
            parentCategory.addChildCategory(category);
        }
        categoryService.save(category);
        return new ApiResponse<>("success","카테고리 생성 성공.",new NewCategoryResponse(category.getId(),category.getName()));
    }



    @Data
    @AllArgsConstructor
    public static class NewCategoryRequest{
        @NotBlank
        private String name;

        private Integer isChild;
        private Long parentId;
    }

    @Getter
    @AllArgsConstructor
    public static class NewCategoryResponse{
        private Long id;
        private String name;
    }
}
