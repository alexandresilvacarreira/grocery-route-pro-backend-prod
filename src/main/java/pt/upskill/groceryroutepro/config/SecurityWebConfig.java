package pt.upskill.groceryroutepro.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.authentication.logout.DelegatingServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.repositories.UserRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityWebConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserAuthenticationProvider userAuthenticationProvider;

    @Autowired
    UserRepository userRepository;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors()
                .and()
                .logout()
                .logoutUrl("/logout")
                .invalidateHttpSession(true) // Invalidar a sessão HTTP
                .clearAuthentication(true) // Limpar detalhes de autenticação
                .deleteCookies("JSESSIONID") // Limpar cookies de sessão
                .logoutSuccessHandler(((request, response, authentication) -> {
                    handleLogoutSuccess(request, response, authentication);
                }))
                .and()
                .formLogin()
                .loginPage("/login") // Se um utilizador pedir um recurso ao qual nao tem acesso, o Spring redirecciona para uma página de login.
                .loginProcessingUrl("/process-login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    handleSuccess(request, response, authentication);
                })
                .failureHandler((request, response, exception) -> {
                    handleFailure(request, response, exception);
                })
                .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/","/signup", "/login", "/logout", "/verify-account/", "/users/get-authenticated-user",
                        "/users/forgot-password","/users/change-password/").permitAll()
                .antMatchers("/shopping-list/**", "/products/**", "/google-maps-api/**").authenticated()
                .antMatchers("/products/create", "/products/edit", "/products/categories").hasAnyRole("STORE")
                .antMatchers("**").denyAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder managerBuilder) {
        managerBuilder.authenticationProvider(userAuthenticationProvider);
    }


    private void handleSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        User user = userRepository.getByEmail(authentication.getName());

        Map<String, Object> serverMessage = new HashMap<>();
        serverMessage.put("success", true);
        serverMessage.put("message", "Autenticado com sucesso");
        serverMessage.put("user", user);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(serverMessage);
        response.getWriter().write(json);
    }

    private void handleFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");

        String message;

        // Existem vários tipos de AuthenticationException: BadCredentialsException, InternalAuthenticationServiceException, etc.
        if (exception instanceof BadCredentialsException) {
            message = "Credenciais inválidas.";
        } else if (exception instanceof LockedException) {
            message = "Conta bloqueada.";
        } else if (exception instanceof DisabledException) {
            message = "Conta desativada.";
        } else {
            message = "Credenciais inválidas.";
        }

        Map<String, Object> serverMessage = new HashMap<>();
        serverMessage.put("success", false);
        serverMessage.put("message", message);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(serverMessage);
        response.getWriter().write(json);

    }

    private void handleLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        Map<String, Object> serverMessage = new HashMap<>();
        serverMessage.put("success", true);
        serverMessage.put("message", "Logout bem sucedido");
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(serverMessage);
        response.getWriter().write(json);
    }

}

