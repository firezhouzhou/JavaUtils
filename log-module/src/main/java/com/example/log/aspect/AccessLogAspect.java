package com.example.log.aspect;

import com.alibaba.fastjson.JSON;
import com.example.log.entity.AccessLog;
import com.example.log.service.AccessLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 访问日志AOP切面
 */
@Aspect
@Component
public class AccessLogAspect {
    
    @Autowired
    private AccessLogService accessLogService;
    
    /**
     * 定义切点：拦截所有Controller方法
     */
    @Pointcut("execution(* com.example.*.controller.*.*(..))")
    public void controllerPointcut() {}
    
    /**
     * 环绕通知：记录访问日志
     */
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        
        HttpServletRequest request = attributes.getRequest();
        AccessLog accessLog = new AccessLog();
        
        try {
            // 填充请求信息
            fillRequestInfo(accessLog, request, joinPoint);
            
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            accessLog.setExecutionTime(executionTime);
            
            // 填充响应信息
            fillResponseInfo(accessLog, result, 200);
            
            return result;
            
        } catch (Exception e) {
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            accessLog.setExecutionTime(executionTime);
            
            // 填充异常信息
            fillExceptionInfo(accessLog, e);
            
            throw e;
            
        } finally {
            // 异步保存日志
            accessLogService.saveAccessLog(accessLog);
        }
    }
    
    /**
     * 填充请求信息
     */
    private void fillRequestInfo(AccessLog accessLog, HttpServletRequest request, JoinPoint joinPoint) {
        // 基本请求信息
        accessLog.setRequestMethod(request.getMethod());
        accessLog.setRequestUrl(request.getRequestURL().toString());
        accessLog.setRequestTime(LocalDateTime.now());
        
        // 获取用户信息（从请求头中获取，由Gateway或其他模块设置）
        String userIdHeader = request.getHeader("X-User-Id");
        String usernameHeader = request.getHeader("X-Username");
        
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            try {
                accessLog.setUserId(Long.parseLong(userIdHeader));
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }
        
        if (usernameHeader != null && !usernameHeader.isEmpty()) {
            accessLog.setUsername(usernameHeader);
        }
        
        // 获取请求参数
        Map<String, String> paramMap = getRequestParams(request);
        if (!paramMap.isEmpty()) {
            accessLog.setRequestParams(JSON.toJSONString(paramMap));
        }
        
        // 获取请求体（对于POST/PUT请求）
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            try {
                // 过滤掉HttpServletRequest、HttpServletResponse等参数
                Object requestBody = null;
                for (Object arg : args) {
                    if (arg != null && 
                        !arg.getClass().getName().startsWith("javax.servlet") &&
                        !arg.getClass().getName().startsWith("org.springframework.web")) {
                        requestBody = arg;
                        break;
                    }
                }
                
                if (requestBody != null) {
                    String bodyJson = JSON.toJSONString(requestBody);
                    // 限制请求体长度，避免过长
                    if (bodyJson.length() > 5000) {
                        bodyJson = bodyJson.substring(0, 5000) + "...";
                    }
                    accessLog.setRequestBody(bodyJson);
                }
            } catch (Exception e) {
                // 忽略序列化错误
            }
        }
        
        // 获取IP地址
        accessLog.setIpAddress(getClientIp(request));
        
        // 获取User-Agent
        accessLog.setUserAgent(request.getHeader("User-Agent"));
        
        // 获取模块名称（从类名中提取）
        String className = joinPoint.getTarget().getClass().getSimpleName();
        if (className.endsWith("Controller")) {
            String moduleName = className.substring(0, className.length() - 10); // 去掉"Controller"
            accessLog.setModuleName(moduleName.toLowerCase());
        }
        
        // 获取操作描述（方法名）
        String methodName = joinPoint.getSignature().getName();
        accessLog.setOperationDesc(methodName);
    }
    
    /**
     * 填充响应信息
     */
    private void fillResponseInfo(AccessLog accessLog, Object result, int status) {
        accessLog.setResponseStatus(status);
        
        if (result != null) {
            try {
                String responseJson = JSON.toJSONString(result);
                // 限制响应体长度
                if (responseJson.length() > 5000) {
                    responseJson = responseJson.substring(0, 5000) + "...";
                }
                accessLog.setResponseBody(responseJson);
            } catch (Exception e) {
                // 忽略序列化错误
            }
        }
    }
    
    /**
     * 填充异常信息
     */
    private void fillExceptionInfo(AccessLog accessLog, Exception e) {
        accessLog.setResponseStatus(500);
        
        // 记录异常信息
        StringBuilder exceptionInfo = new StringBuilder();
        exceptionInfo.append("Exception: ").append(e.getClass().getName()).append("\n");
        exceptionInfo.append("Message: ").append(e.getMessage()).append("\n");
        
        // 记录堆栈跟踪（只记录前10行）
        StackTraceElement[] stackTrace = e.getStackTrace();
        exceptionInfo.append("StackTrace:\n");
        for (int i = 0; i < Math.min(stackTrace.length, 10); i++) {
            exceptionInfo.append("  at ").append(stackTrace[i].toString()).append("\n");
        }
        
        accessLog.setExceptionInfo(exceptionInfo.toString());
    }
    
    /**
     * 获取请求参数
     */
    private Map<String, String> getRequestParams(HttpServletRequest request) {
        Map<String, String> paramMap = new HashMap<>();
        
        // 获取URL参数
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            paramMap.put(paramName, paramValue);
        }
        
        return paramMap;
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        String proxyClientIp = request.getHeader("Proxy-Client-IP");
        if (proxyClientIp != null && !proxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(proxyClientIp)) {
            return proxyClientIp;
        }
        
        String wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP");
        if (wlProxyClientIp != null && !wlProxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(wlProxyClientIp)) {
            return wlProxyClientIp;
        }
        
        return request.getRemoteAddr();
    }
}
