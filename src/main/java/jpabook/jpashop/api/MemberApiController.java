package jpabook.jpashop.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jpabook.jpashop.Dto.ApiResponse;
import jpabook.jpashop.Dto.MembersDto;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Role;
import jpabook.jpashop.exception.CustomStatusException;
import jpabook.jpashop.service.MemberService;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static jpabook.jpashop.filter.CheckLogic.adminCheck;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;


    /**
     * 멤버등록
     * 파라미터: name,password,city,street,zipcode
     */
    @PostMapping("/api/join")
    public ApiResponse<JoinMemberResponse> saveMemberV2(@RequestBody @Valid MemberApiController.JoinMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());
        member.setPassword(request.getPassword());
        Address address = new Address(request.getCity(), request.getStreet(), request.getZipcode());
        member.setAddress(address);
        member.setRole(Role.GUEST);
        Long id = memberService.join(member);
        Member newMember = memberService.findOne(id);
        return new ApiResponse<>("success", "멤버 등록 성공.", new JoinMemberResponse(id, (newMember.getName())));
    }


    /**
     * 멤버 이름 수정
     * 파라미터: name
     */
    @PutMapping("/api/update/{id}")
    public ApiResponse<UpdateMemberResponse> updateMemberV2(@PathVariable("id") Long id,
                                                            @RequestBody @Valid UpdateMemberRequest request,
                                                            @SessionAttribute(name = "loginMember", required = false) Member loginMember) {

        adminCheck(loginMember);
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new ApiResponse<>("success", "멤버 수정 성공.", new UpdateMemberResponse(findMember.getId(), findMember.getName()));
    }


    /**
     * 멤버관련 정보 조회(관리자)
     */
    @GetMapping("/api/members")
    public ApiResponse<Page<MembersDto>> membersV2(@SessionAttribute(name = "loginMember", required = false) Member loginMember, Pageable pageable) {

        adminCheck(loginMember);
        Page<MembersDto> memberDto = memberService.findMemberDto(pageable);
        return new ApiResponse<>("success", "멤버 조회 성공.", memberDto);

    }

    /**
     * 멤버삭제
     */
    @DeleteMapping("/api/delete/{id}")
    public ApiResponse<DeleteMemberResponse> deleteMember(@PathVariable("id") Long id, @SessionAttribute(name = "loginMember", required = false) Member loginMember) {
        adminCheck(loginMember);
        Member findMember = memberService.findOne(id);
        DeleteMemberResponse deleteMemberResponse = new DeleteMemberResponse(findMember.getId(), findMember.getName());
        memberService.delete(id);
        return new ApiResponse<>("success", "멤버 삭제 완료.", deleteMemberResponse);
    }


    @Data
    @AllArgsConstructor
    public static class DeleteMemberResponse {
        private Long id;
        private String name;
    }

    /**
     * 로그인
     * 파라미터: loginId,password
     */
    @PostMapping("/api/login")
    public ApiResponse<Map<String, String>> login(@RequestBody @Valid LoginRequestDto loginRequestDto, HttpServletRequest request) {
        Member loginMember = memberService.login(loginRequestDto.getLoginId(), loginRequestDto.getPassword());

        if (loginMember == (null)) {
            throw new CustomStatusException(HttpStatus.BAD_REQUEST, "아이디가 존재하지 않거나 비밀번호가 틀렸습니다.");
        }

        HttpSession session = request.getSession();
        session.setAttribute("loginMember", loginMember);
        return new ApiResponse<>("success", "로그인 성공.", Map.of("message", "로그인 성공."));
    }


    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JoinMemberRequest {
        @NotBlank(message = "필수 입력 정보가 누락되었습니다.")
        private String name;
        @NotBlank(message = "필수 입력 정보가 누락되었습니다.")
        private String password;
        @NotBlank(message = "필수 입력 정보가 누락되었습니다.")
        private String city;
        @NotBlank(message = "필수 입력 정보가 누락되었습니다.")
        private String street;
        @NotBlank(message = "필수 입력 정보가 누락되었습니다.")
        private String zipcode;
    }


    @Getter
    @AllArgsConstructor
    public static class JoinMemberResponse {
        private Long id;
        private String name;
    }


    @Data
    public static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    public static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequestDto {
        private String loginId;
        private String password;
    }
}