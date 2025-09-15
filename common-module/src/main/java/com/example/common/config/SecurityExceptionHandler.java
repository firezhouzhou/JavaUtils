package com.example.common.config;

import com.example.common.web.ApiResponse;
import com.example.common.web.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Spring Security 异常处理器
 * 只有在Spring Security存在时才会加载
 */
@RestControllerAdvice
@ConditionalOnClass({BadCredentialsException.class, AuthenticationException.class})
public class SecurityExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityExceptionHandler.class);
    
    /**
     * 处理认证异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<String> handleBadCredentialsException(BadCredentialsException e) {
        logger.warn("认证失败: {}", e.getMessage());
        return ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), e.getMessage());
    }
    
    /**
     * 处理其他认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<String> handleAuthenticationException(AuthenticationException e) {
        logger.warn("认证异常: {}", e.getMessage());
        return ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), "认证失败");
    }
}
