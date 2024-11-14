package com.tear.upgrade.t1tot2upgrade.configuration;

import com.tear.upgrade.t1tot2upgrade.security.CustomUserDetailService;
import com.tear.upgrade.t1tot2upgrade.service.JwtToken;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtToken jwtTokenService;

    @Autowired
    private CustomUserDetailService userDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtTokenService.extractUserName(token);
                log.info("JWT token extracted for user: {}", username);
            } catch (IllegalArgumentException e) {
                log.error("Unable to get JWT token from request: {}", e.getMessage());
                throw new RuntimeException("Unable to get JWT token");
            } catch (ExpiredJwtException e) {
                log.error("JWT token has expired: {}", token);
                throw new RuntimeException("Jwt token has expired");
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            log.info("JWT token validated successfully for user: {}", username);
            UserDetails userDetails = userDetailService.loadUserByUsername(username);

            if (jwtTokenService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.info("User authentication successful for: {}", username);
            } else {
                log.warn("JWT token validation failed for user: {}", username);
            }
        }
        filterChain.doFilter(request, response);
    }
}
