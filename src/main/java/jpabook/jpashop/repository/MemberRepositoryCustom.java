package jpabook.jpashop.repository;

import jpabook.jpashop.Dto.MembersDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {
    Page<MembersDto> members(Pageable pageable);
}
