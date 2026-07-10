package jpabook.jpashop.init;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Profile("local")
public class InitAdminLogin implements HandlerInterceptor {

    private final MemberService memberService;

    @Value("${dev.auth.admin-id}")
    private String adminId;

    @Value("${dev.auth.admin-pw}")
    private String adminPw;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession();
        if (session.getAttribute("loginMember")==null){
            Member admin = memberService.login(adminId, adminPw);
            session.setAttribute("loginMember",admin);

        }
        return true;
    }
}
