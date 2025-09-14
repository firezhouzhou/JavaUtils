package com.example.log.service;

import com.example.log.entity.AccessLog;
import com.example.log.repository.AccessLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 访问日志服务
 */
@Service
public class AccessLogService {
    
    @Autowired
    private AccessLogRepository accessLogRepository;
    
    /**
     * 异步保存访问日志
     */
    @Async
    public void saveAccessLog(AccessLog accessLog) {
        try {
            accessLogRepository.save(accessLog);
        } catch (Exception e) {
            // 记录日志保存失败，但不影响主业务
            e.printStackTrace();
        }
    }
    
    /**
     * 根据用户ID分页查询访问日志
     */
    public Page<AccessLog> getAccessLogsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accessLogRepository.findByUserIdOrderByRequestTimeDesc(userId, pageable);
    }
    
    /**
     * 根据用户ID和时间范围查询访问日志
     */
    public Page<AccessLog> getAccessLogsByUserIdAndTimeRange(Long userId, LocalDateTime startTime, 
                                                           LocalDateTime endTime, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accessLogRepository.findByUserIdAndRequestTimeBetweenOrderByRequestTimeDesc(
                userId, startTime, endTime, pageable);
    }
    
    /**
     * 根据IP地址查询访问日志
     */
    public Page<AccessLog> getAccessLogsByIpAddress(String ipAddress, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accessLogRepository.findByIpAddressOrderByRequestTimeDesc(ipAddress, pageable);
    }
    
    /**
     * 根据请求URL模糊查询
     */
    public Page<AccessLog> getAccessLogsByUrl(String url, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accessLogRepository.findByRequestUrlContainingOrderByRequestTimeDesc(url, pageable);
    }
    
    /**
     * 根据模块名称查询
     */
    public Page<AccessLog> getAccessLogsByModule(String moduleName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accessLogRepository.findByModuleNameOrderByRequestTimeDesc(moduleName, pageable);
    }
    
    /**
     * 查询异常日志
     */
    public Page<AccessLog> getExceptionLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accessLogRepository.findByExceptionInfoIsNotNullOrderByRequestTimeDesc(pageable);
    }
    
    /**
     * 根据响应状态码查询
     */
    public Page<AccessLog> getAccessLogsByStatus(Integer responseStatus, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accessLogRepository.findByResponseStatusOrderByRequestTimeDesc(responseStatus, pageable);
    }
    
    /**
     * 分页查询所有访问日志
     */
    public Page<AccessLog> getAllAccessLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accessLogRepository.findAll(pageable);
    }
    
    /**
     * 统计用户访问次数
     */
    public Long countUserAccess(Long userId, LocalDateTime startTime) {
        return accessLogRepository.countByUserIdAndRequestTimeAfter(userId, startTime);
    }
    
    /**
     * 统计IP访问次数
     */
    public Long countIpAccess(String ipAddress, LocalDateTime startTime) {
        return accessLogRepository.countByIpAddressAndRequestTimeAfter(ipAddress, startTime);
    }
    
    /**
     * 获取访问量统计
     */
    public List<Object> getAccessStatistics(LocalDateTime startTime) {
        List<Object[]> results = accessLogRepository.getAccessStatistics(startTime);
        return results.stream()
                .map(result -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", result[0]);
                    map.put("count", result[1]);
                    return map;
                })
                .collect(java.util.stream.Collectors.toList())
                .stream()
                .map(item -> (Object) item)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 获取用户访问统计
     */
    public List<Map<String, Object>> getUserAccessStatistics(LocalDateTime startTime, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = accessLogRepository.getUserAccessStatistics(startTime, pageable);
        return results.stream()
                .map(result -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", result[0]);
                    map.put("username", result[1]);
                    map.put("count", result[2]);
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 获取接口访问统计
     */
    public List<Map<String, Object>> getApiAccessStatistics(LocalDateTime startTime, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = accessLogRepository.getApiAccessStatistics(startTime, pageable);
        return results.stream()
                .map(result -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("requestUrl", result[0]);
                    map.put("count", result[1]);
                    map.put("avgTime", result[2]);
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 根据ID查询访问日志详情
     */
    public AccessLog getAccessLogById(Long id) {
        return accessLogRepository.findById(id).orElse(null);
    }
}
