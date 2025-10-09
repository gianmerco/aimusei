package it.prismaprogetti.aimusei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootApplication
@PropertySource("file:./config/config.properties")
public class AimuseiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AimuseiApplication.class, args);
	}


    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz.requestMatchers("/**").permitAll())
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors-> cors.disable()); // Disable CORS completely
        return http.build();
    }
	
	
}
