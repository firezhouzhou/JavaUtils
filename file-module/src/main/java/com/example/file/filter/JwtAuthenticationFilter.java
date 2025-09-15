package com.example.file.filter;

import com.example.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 文件服务JWT认证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String token = extractToken(request);
        
        if (StringUtils.hasText(token)) {
            try {
                String username = jwtUtil.getUsernameFromToken(token);
                
                if (StringUtils.hasText(username) && !jwtUtil.isTokenExpired(token)) {
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置到Security上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // 将用户ID添加到请求属性中，方便控制器使用
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    request.setAttribute("userId", userId);
                    request.setAttribute("username", username);
                }
            } catch (Exception e) {
                logger.warn("JWT token validation failed: " + e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取token
     */
    private String extractToken(HttpServletRequest request) {
        // 优先从Authorization头获取Bearer token
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        // 兼容JWT头（用于Swagger测试）
        String jwtToken = request.getHeader("JWT");
        if (StringUtils.hasText(jwtToken)) {
            return jwtToken;
        }
        
        return null;
    }
}
