package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> ,CategoryRepositoryCustom{
}
