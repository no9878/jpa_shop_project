package jpabook.jpashop.filter;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.api.ItemApiController.CreateItemRequest;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Role;
import jpabook.jpashop.exception.CustomStatusException;
import jpabook.jpashop.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

import static jpabook.jpashop.domain.item.QAlbum.album;
import static jpabook.jpashop.domain.item.QBook.book;
import static jpabook.jpashop.domain.item.QItem.item;
import static jpabook.jpashop.domain.item.QMovie.movie;

@Component
public class CheckLogic {

    private final MemberRepository memberRepository;
    private final JPAQueryFactory queryFactory;

    public CheckLogic(MemberRepository memberRepository, EntityManager em){
        this.memberRepository = memberRepository;
        this.queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 관리자 권한 체크
     */
    public static void adminCheck(@SessionAttribute(name = "loginMember",required = false) Member loginMember){

        if (loginMember==null){
            throw new CustomStatusException(HttpStatus.FORBIDDEN,"로그인이 필요합니다.");
        }

        if (loginMember.getRole()!= Role.ADMIN){
            throw new CustomStatusException(HttpStatus.FORBIDDEN,"관리자 권한이 필요합니다.");
        }
    }
    /**
     * 멤버 중복체크
     */
    public void validateDuplicateMember(Member member){
        List<Member> findMembers =
                memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()){
            throw new CustomStatusException(HttpStatus.CONFLICT,"이미 존재하는 회원입니다.");
        }
    }

    /**
     * 상품 중복체크
     */
    public void validateDuplicateItem(CreateItemRequest request){
        if (request.getCategory()==1) {
            if (bookExistCheck(request) == 1) {
                throw new CustomStatusException(HttpStatus.CONFLICT, "이미 존재하는 상품입니다.");
            }
        }
        else if (request.getCategory()==2) {
            if (albumExistCheck(request) == 1) {
                throw new CustomStatusException(HttpStatus.CONFLICT, "이미 존재하는 상품입니다.");
            }
        }
        else if (request.getCategory()==3) {
            if (movieExistCheck(request) == 1) {
                throw new CustomStatusException(HttpStatus.CONFLICT, "이미 존재하는 상품입니다.");
            }
        }

    }



    private int bookExistCheck(CreateItemRequest request) {

        Integer result = queryFactory
                .selectOne()
                .from(book)
                .where(book.name.eq(request.getName())
                        , authorEq(request.getAuthor()), isbnEq(request.getIsbn()))
                .fetchFirst();
        return result!=null?1:0;
    }
    public int albumExistCheck(CreateItemRequest request) {
        Integer result = queryFactory
                .selectOne()
                .from(album)
                .where(album.name.eq(request.getName()), artistEq(request.getArtist()), etcEq(request.getEtc()))
                .fetchFirst();
        return result!=null?1:0;
    }

    public int movieExistCheck(CreateItemRequest request) {
        Integer result = queryFactory
                .selectOne()
                .from(movie)
                .where(movie.name.eq(request.getName()), directorEq(request.getDirector()), actorEq(request.getActor()))
                .fetchFirst();
        return result!=null?1:0;
    }


    private BooleanExpression authorEq(String author){
        return author !=null?book.author.eq(author):null;
    }
    private BooleanExpression isbnEq(String isbn){
        return isbn !=null?book.isbn.eq(isbn):null;
    }
    private BooleanExpression artistEq(String artist){
        return artist !=null?album.artist.eq(artist):null;
    }
    private BooleanExpression etcEq(String etc){
        return etc !=null?album.etc.eq(etc):null;
    }
    private BooleanExpression directorEq(String director){
        return director !=null?movie.director.eq(director):null;
    }
    private BooleanExpression actorEq(String actor){
        return actor !=null?movie.actor.eq(actor):null;
    }


}
