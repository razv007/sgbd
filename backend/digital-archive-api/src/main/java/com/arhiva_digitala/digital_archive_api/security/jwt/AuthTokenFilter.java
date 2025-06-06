package com.arhiva_digitala.digital_archive_api.security.jwt;

import com.arhiva_digitala.digital_archive_api.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {
  private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.debug("AuthTokenFilter: Processing request to {}", request.getRequestURI());
        try {
            String jwt = parseJwt(request);
            logger.debug("AuthTokenFilter: Parsed JWT: {}", (jwt != null ? jwt.substring(0, Math.min(jwt.length(), 30)) + "..." : "null"));

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                logger.debug("AuthTokenFilter: JWT is valid.");
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                logger.debug("AuthTokenFilter: Username from JWT: {}", username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.debug("AuthTokenFilter: UserDetails loaded for username: {}", userDetails.getUsername());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails,
                                null,
                                userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("AuthTokenFilter: Authentication set in SecurityContextHolder for user: {}", username);
            } else {
                if (jwt == null) {
                    logger.debug("AuthTokenFilter: JWT is null.");
                } else {
                    // jwtUtils.validateJwtToken will log the specific reason for invalidity
                    logger.debug("AuthTokenFilter: JWT is invalid or validation failed.");
                }
            }
        } catch (Exception e) {
            logger.error("AuthTokenFilter: Cannot set user authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
        logger.debug("AuthTokenFilter: Finished processing request to {}", request.getRequestURI());
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        logger.debug("AuthTokenFilter: Authorization header: {}", headerAuth);

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            // Log only the beginning of the token for brevity and security
            logger.debug("AuthTokenFilter: Extracted Bearer token: {}", token.substring(0, Math.min(token.length(), 30)) + "...");
            return token;
        }
        logger.debug("AuthTokenFilter: No Bearer token found in Authorization header.");
        return null;
    }
}
