package jpabook.jpashop.init;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("local")
@RequiredArgsConstructor
public class InterceptorConfig implements WebMvcConfigurer {
    private final InitAdminLogin initAdminLogin;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(initAdminLogin)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**","/js/**","/image/**","/error");
    }
}
