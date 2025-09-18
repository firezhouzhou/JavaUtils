package com.example.auth.filter;

import com.example.common.util.JwtUtil;
import com.example.auth.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        System.out.println("=== JWT Filter Processing: " + method + " " + requestUri + " ===");
        
        String token = getTokenFromRequest(request);
        System.out.println("Token extracted: " + (token != null ? token.substring(0, Math.min(token.length(), 30)) + "..." : "null"));
        
        if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("Processing JWT authentication...");
            try {
                String username = jwtUtil.getUsernameFromToken(token);
                System.out.println("Username from token: " + username);
                
                if (StringUtils.hasText(username)) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    System.out.println("UserDetails loaded for: " + username);
                    
                    if (jwtUtil.validateToken(token, username)) {
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("JWT authentication successful for: " + username);
                    } else {
                        System.out.println("JWT token validation failed for: " + username);
                    }
                } else {
                    System.out.println("No username found in token");
                }
            } catch (Exception e) {
                System.err.println("JWT认证失败: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (StringUtils.hasText(token)) {
            System.out.println("Authentication already exists, skipping JWT processing");
        } else {
            System.out.println("No token found in request");
        }
        
        System.out.println("=== JWT Filter Complete ===");
        filterChain.doFilter(request, response);
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        System.out.println("--- Extracting token from request ---");
        
        // 首先尝试从Authorization头获取Bearer token
        String bearerToken = request.getHeader("Authorization");
        System.out.println("Authorization header: " + (bearerToken != null ? bearerToken.substring(0, Math.min(bearerToken.length(), 50)) + "..." : "null"));
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            System.out.println("Extracted Bearer token, length: " + token.length());
            return token;
        }
        
        // 然后尝试从JWT头直接获取token
        String jwtToken = request.getHeader("JWT");
        System.out.println("JWT header: " + (jwtToken != null ? jwtToken.substring(0, Math.min(jwtToken.length(), 50)) + "..." : "null"));
        
        if (StringUtils.hasText(jwtToken)) {
            System.out.println("Extracted JWT token, length: " + jwtToken.length());
            return jwtToken;
        }
        
        System.out.println("No token found in headers");
        return null;
    }
}