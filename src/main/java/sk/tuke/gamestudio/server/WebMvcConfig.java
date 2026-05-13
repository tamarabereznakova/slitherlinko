package sk.tuke.gamestudio.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sk.tuke.gamestudio.server.controller.helpers.slitherfriends.LastSeenInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LastSeenInterceptor lastSeenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(lastSeenInterceptor).excludePathPatterns("/css/**", "/js/**", "/images/**", "/api/spectate/**");
    }
}