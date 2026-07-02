package jpabook.jpashop.Dto;

import com.querydsl.core.annotations.QueryProjection;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Role;
import lombok.Getter;

@Getter
public class MembersDto {
    private Long id;
    private String username;
    private Address address;
    private Role role;

    @QueryProjection
    public MembersDto(Long id, String username, Address address, Role role) {
        this.id = id;
        this.username = username;
        this.address = address;
        this.role = role;
    }
}
