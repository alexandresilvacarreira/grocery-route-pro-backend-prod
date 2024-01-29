package pt.upskill.groceryroutepro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Configuration
public class CookieConfig extends WebMvcConfigurerAdapter  implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getSession(false) != null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("JSESSIONID".equals(cookie.getName())) {
                        response.setHeader("Set-Cookie", modifyJSessionIdCookie(cookie));
                    }

                }
            }
        }
        return true;
    }

    private String modifyJSessionIdCookie(Cookie cookie) {
        String updatedCookie = String.format("%s=%s; HttpOnly=true; Secure=true; SameSite=None", cookie.getName(), cookie.getValue());
        return updatedCookie;
    }


}
