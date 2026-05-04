package it.prismaprogetti.aimusei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootApplication
@PropertySource("file:./config/config.properties")
@ComponentScan(basePackages = {"it.prismaprogetti.aimusei", "com.leonardo.aiservice"})
@EnableAsync
@EnableScheduling
public class AimuseiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AimuseiApplication.class, args);
	}

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz.requestMatchers("/**").permitAll())
            .csrf(AbstractHttpConfigurer::disable)
            // CORS is handled by GlobalCorsFilter at servlet level
            .cors(AbstractHttpConfigurer::disable);
        return http.build();
    }

}
