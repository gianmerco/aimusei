package it.prismaprogetti.aimusei.security;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.reactive.function.client.WebClient;

import com.nimbusds.jwt.SignedJWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import reactor.core.publisher.Mono;

@Component
public class UnifiedAuthFilter extends OncePerRequestFilter {

    private final WebClient webClient;
    private final String smnIssuerUri;
    private final String smnBaseUrl;
    private final String keycloakIssuerUri;
    private final String keycloakUserinfoUri;
    private final String secret;

    public UnifiedAuthFilter(
            @Value("${smn.auth.issuer-uri}") String smnIssuerUri,
            @Value("${smn.auth.base-url}") String smnBaseUrl,
            @Value("${keycloak.auth.issuer-uri}") String keycloakIssuerUri,
            @Value("${keycloak.auth.userinfo-uri}") String keycloakUserinfoUri,
            @Value("${secret}") String secret,
            WebClient.Builder webClientBuilder) {
    	this.secret = secret;
        this.smnIssuerUri = smnIssuerUri;
        this.smnBaseUrl = smnBaseUrl;
        this.keycloakIssuerUri = keycloakIssuerUri;
        this.keycloakUserinfoUri = keycloakUserinfoUri;
        this.webClient = webClientBuilder.build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

		Authentication auth = null;

		String path = request.getRequestURI();
		String method = request.getMethod();

		String header = request.getHeader("Authorization");

		if (!("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method))
				|| (path != null && path.contains("public/texts/massive"))) {
              auth = dummyAuth();
              SecurityContextHolder.getContext().setAuthentication(auth);
              chain.doFilter(request, response);
              return;
          }
          
          if (header!=null	&&	secret.equals(header.substring(7))) {
			  auth = dummyAuth();
			  SecurityContextHolder.getContext().setAuthentication(auth);
			  chain.doFilter(request, response);
			  return;
		}

        // Se già autenticato (es. da un filtro precedente), salta
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        // Estrai issuer dal token (solo parsing, non validazione firma)
        String issuer;
        SignedJWT signedJWT;
        try {
             signedJWT = SignedJWT.parse(token);
            issuer = signedJWT.getJWTClaimsSet().getIssuer();
        } catch (ParseException e) {
            // Token malformato -> nessuna autenticazione
            chain.doFilter(request, response);
            return;
        }

        if (smnIssuerUri.equals(issuer)) {
            auth = authenticateWithSmn(signedJWT);
        } else if (keycloakIssuerUri.equals(issuer)) {
            auth = authenticateWithKeycloak(signedJWT);
        } // altrimenti issuer sconosciuto -> auth rimane null

        if (auth != null) {
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }

    
    private Authentication authenticateWithSmn(SignedJWT signedJWT) {
    	String token = signedJWT.getParsedString();
        try {
            // Chiamata POST a /authenticate, attesa risposta in testo semplice
             webClient.post()
                    .uri(smnBaseUrl + "/authenticate")
                    .contentType(MediaType.TEXT_PLAIN)
                    .bodyValue(token)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> 
                        Mono.error(new RuntimeException("SMN auth failed: " + response.statusCode()))
                    )
                    .toBodilessEntity()
                    .block();
             
             String principal=signedJWT.getJWTClaimsSet().getSubject(); // subject come principal
             
             return new UsernamePasswordAuthenticationToken(principal, token, Collections.emptyList());
             
        } catch (Exception e) {
            logger.debug("Autenticazione SMN fallita", e);
            return null;
        }
    }

    private Authentication authenticateWithKeycloak(SignedJWT signedJWT) {
    	String token = signedJWT.getParsedString();
        try {
            // Chiamata GET a userinfo con Bearer token, risposta JSON in Map
             webClient.get()
                    .uri(keycloakUserinfoUri)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> 
                        Mono.error(new RuntimeException("Keycloak userinfo failed: " + response.statusCode()))
                    )
                    .toBodilessEntity()
                    .block();


             String principal=signedJWT.getJWTClaimsSet().getClaimAsString("email");

            return new UsernamePasswordAuthenticationToken(principal, token, Collections.emptyList());
        } catch (Exception e) {
            logger.debug("Autenticazione Keycloak fallita", e);
            return null;
        }
    }
    
    private Authentication dummyAuth() {
		return new UsernamePasswordAuthenticationToken("dummyUser", "dummyToken", Collections.emptyList());    	
    }
}