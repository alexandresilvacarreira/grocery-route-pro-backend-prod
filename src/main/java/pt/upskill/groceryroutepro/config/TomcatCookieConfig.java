package pt.upskill.groceryroutepro.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.http.LegacyCookieProcessor;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatCookieConfig {

    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        return new TomcatServletWebServerFactory() {
            @Override
            protected void customizeConnector(Connector connector) {
                super.customizeConnector(connector);
                connector.setPort(8080);
            }
        };
    }

    @Bean
    public TomcatContextCustomizer tomcatContextCustomizer() {
        return context -> {
            if (context instanceof StandardContext) {
                context.setCookieProcessor(new LegacyCookieProcessor());
            }
        };
    }
}
