package pt.upskill.groceryroutepro.config;
import org.apache.tomcat.util.http.LegacyCookieProcessor;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.naming.Context;

@Configuration
public class TomcatCookieConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            TomcatServletWebServerFactory tomcat = factory;
            tomcat.addContextCustomizers(context -> context.setCookieProcessor(new LegacyCookieProcessor()));
        };
    }
}
