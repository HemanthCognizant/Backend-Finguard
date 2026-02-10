package com.finguard.config;

import io.jsonwebtoken.Claims;

import jakarta.servlet.FilterChain;

import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.extractClaims(token);
//                String username = claims.getSubject();
//                String role = claims.get("role", String.class);
//
//                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//                    // Ensure the role doesn't already have ROLE_ to avoid ROLE_ROLE_ prefix
//                    String formattedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
//                    var authority = new SimpleGrantedAuthority(formattedRole);
//
//                    var authToken = new UsernamePasswordAuthenticationToken(
//                            username,
//                            null,
//                            List.of(authority)
//                    );
                String username = claims.getSubject();
                String roleFromToken = claims.get("role", String.class);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null && roleFromToken != null) {

                    // 1. Force to Uppercase to match .hasRole("BANKER")
//                    String role = roleFromToken.toUpperCase();
                    String role = claims.get("role", String.class);
                    if (role != null) {
                        var authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()); // 👈 Add .toUpperCase()
                        var authToken = new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(authority)
                        );
                        // ... rest of your code
                    }

                    // 2. Safely add the ROLE_ prefix
                    String formattedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    var authority = new SimpleGrantedAuthority(formattedRole);

                    var authToken = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(authority)
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                // If token is invalid/expired, we just let it fail naturally
                // so the SecurityConfig can handle the 403/401 response.
                logger.error("Could not set user authentication in security context", e);
            }
        }
        filterChain.doFilter(request, response);
    }
}