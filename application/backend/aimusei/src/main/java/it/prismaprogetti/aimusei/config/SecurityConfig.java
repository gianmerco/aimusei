package it.prismaprogetti.aimusei.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import it.prismaprogetti.aimusei.security.UnifiedAuthFilter;

@Configuration
@EnableWebSecurity
@OpenAPIDefinition(
        info = @Info(title = "AI Musei API", version = "1.0"),
        security = @SecurityRequirement(name = "bearerAuth") // applica a tutte le operazioni
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SecurityConfig {

	@Autowired
    private UnifiedAuthFilter unifiedAuthFilter;
	
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	        // TODO CSRF da verificare se è necessario disabilitarlo o configurarlo correttamente
	        .csrf(AbstractHttpConfigurer::disable)
	        .cors(AbstractHttpConfigurer::disable) // Disabilita CORS completamente
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .authorizeHttpRequests(authz -> authz
	            // Permetti l'accesso pubblico a Swagger UI e alle specifiche OpenAPI
//	            .requestMatchers(
//	                "/swagger-ui/**", 
//	                "/v3/api-docs/**",
//	                "/swagger-ui.html" // se usi anche questo path
//	            ).permitAll()
	            // Tutte le altre richieste richiedono autenticazione
	            .anyRequest().authenticated()
	        )
	        .addFilterBefore(unifiedAuthFilter, UsernamePasswordAuthenticationFilter.class);
	    return http.build();
	}
	
	
}