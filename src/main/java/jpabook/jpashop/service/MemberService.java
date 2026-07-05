package jpabook.jpashop.service;

import jpabook.jpashop.Dto.MembersDto;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.exception.CustomStatusException;
import jpabook.jpashop.filter.CheckLogic;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final CheckLogic checkLogic;


    public Member login(String loginId,String password){

        Member findMember = memberRepository.findFirstByName(loginId);

        if (findMember==null)
            return null;

        if(findMember.getPassword().equals(password))
            return findMember;

        return null;

    }

    /**
     * 회원 가입
     */
    @Transactional
    public Long join(Member member){

        checkLogic.validateDuplicateMember(member); //중복 회원 검증
        memberRepository.save(member);
        return member.getId();
    }



    /**
     * 전체 회원 조회
     */
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId){
        return memberRepository.findById(memberId).
                orElseThrow(()->new CustomStatusException(HttpStatus.NOT_FOUND,"존재하지 않는 회원입니다."));
    }

    /**
     * 회원 수정
     */
    @Transactional
    public void update(Long id,String name){
        Member member = memberRepository.findById(id).
                orElseThrow(()->new CustomStatusException(HttpStatus.NOT_FOUND,"존재하지 않는 회원입니다."));

        if(memberRepository.findFirstByName(name)!=null)
         throw new CustomStatusException(HttpStatus.CONFLICT,"이미 존재하는 회원입니다.");

        member.setName(name);

    }

    @Transactional
    public void delete(Long id){
        memberRepository.deleteById(id);
    }

    public Page<MembersDto> findMemberDto(Pageable pageable){
        return memberRepository.members(pageable);
    }
}
