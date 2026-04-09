package it.prismaprogetti.aimusei.config;

import java.io.IOException;
import java.util.Set;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Low-level servlet filter that handles CORS for ALL requests,
 * regardless of the path (double slashes, trailing slashes, etc.).
 * Runs before Spring Security so that preflight OPTIONS requests
 * always get proper CORS headers.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalCorsFilter implements Filter {

    private static final Set<String> ALLOWED_ORIGINS = Set.of(
        "https://akamaicdn.museiitaliani.it",
        "https://portale.museiitaliani.it",
        "https://portale-coll.museiitaliani.it",
        "https://smn-coll.museiitaliani.it",
        "https://museiitaliani.it",
        "https://sistemamusealenazionale.beniculturali.it",
        "http://localhost:8000",
        "http://localhost:3000",
        "http://localhost:4200"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        String origin = request.getHeader("Origin");

        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Max-Age", "3600");
        }

        // For preflight OPTIONS requests, return 200 immediately
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(req, res);
    }
}
