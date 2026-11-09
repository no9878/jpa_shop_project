package jpabook.jpashop.repository;

import jpabook.jpashop.api.ItemApiController;
import jpabook.jpashop.api.ItemApiController.SearchFilter;
import jpabook.jpashop.domain.item.Item;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class ItemRepositoryTest {

    @Autowired ItemRepository itemRepository;

    @Test
    void 카테고리가_여러개인_상품도_페이지마다_중복없이_한번씩_출력(){
        long total = itemRepository.count();

        SearchFilter filter = new SearchFilter(null,null,null,null,null);

        List<Long> ids = new ArrayList<>();
        Page<Item> page;
        int pageNumber = 0;
        do {
            page = itemRepository.searchItems(PageRequest.of(pageNumber++,1),filter);
            page.getContent().forEach(item -> ids.add(item.getId()));
        } while (page.hasNext());

        assertThat(page.getTotalElements()).isEqualTo(total);  // 전체 개수가 맞는지 체크
        assertThat(ids).doesNotHaveDuplicates();               // 같은상품의 중복이 없는지 체크
        assertThat(ids).hasSize((int) total);                  // 빠진 상품이 없는지 체크

    }
}
