package jpabook.jpashop.service;

import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.CustomStatusException;
import jpabook.jpashop.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public void save(Category category){
        categoryRepository.save(category);
    }
    public Category findOne(Long categoryIds){
      return categoryRepository.findById(categoryIds)
                .orElseThrow(() -> new CustomStatusException(HttpStatus.NOT_FOUND, "결과가 존재하지 않습니다."));

    }



    public Category findParentCategory(Long id){
        return categoryRepository.findById(id).orElseThrow(() ->
                new CustomStatusException(HttpStatus.NOT_FOUND, "부모 카테고리를 찾을 수 없습니다."));
    }

}
