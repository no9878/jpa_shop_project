package jpabook.jpashop.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jpabook.jpashop.Dto.ApiResponse;
import jpabook.jpashop.Dto.MembersDto;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Role;
import jpabook.jpashop.exception.CustomStatusException;
import jpabook.jpashop.service.MemberService;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;


    /**
     * 등록 V2: 요청 값으로 Member 엔티티 대신에 별도의 DTO를 받는다.
     */
    @PostMapping("/api/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request){
        Member member = new Member();
        member.setName(request.getName());
        member.setPassword(request.getPassword());
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * 수정 API
     */
    @PutMapping("/api/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
                                               @RequestBody @Valid UpdateMemberRequest request){
        memberService.update(id, request.getName());
        Member findMember=  memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(),findMember.getName());
    }

    @Data
    static class UpdateMemberRequest{
        private String name;
    }
    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse{
        private Long id;
        private String name;
    }

    @Data
    static class CreateMemberRequest{
        private String name;
        private String password;
    }

    @Data
    static class CreateMemberResponse{
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }


    @GetMapping("/api/members")
    public ApiResponse<?> membersV2(@SessionAttribute(name = "loginMember",required = false)Member loginMember, Pageable pageable){

        adminCheck(loginMember);
        Page<MembersDto> memberDto = memberService.findMemberDto(pageable);
        return new ApiResponse<>(memberDto);

    }


    /**
     * 관리자 로그인 기능 추가
     */
    @PostMapping("/api/login")
    public Map<String, String> login(@RequestBody @Valid LoginRequestDto loginRequestDto, HttpServletRequest request){
        Member loginMember = memberService.login(loginRequestDto.getLoginId(),loginRequestDto.getPassword());

        if (loginMember==(null)){
             throw new CustomStatusException(HttpStatus.BAD_REQUEST,"아이디가 존재하지 않거나 비밀번호가 틀렸습니다.");
        }

        HttpSession session = request.getSession();
        session.setAttribute("loginMember",loginMember);
        return Map.of("message","로그인 성공");
    }

    private void adminCheck(@SessionAttribute(name = "loginMember",required = false) Member loginMember){

        if (loginMember==null){
            throw new CustomStatusException(HttpStatus.FORBIDDEN,"로그인이 필요합니다.");
        }

    if (loginMember.getRole()!=Role.ADMIN){
        throw new CustomStatusException(HttpStatus.FORBIDDEN,"관리자 권한이 필요합니다.");
    }

    }


    @Getter @Setter
    static class LoginRequestDto{
        private String loginId;
        private String password;
    }
}
