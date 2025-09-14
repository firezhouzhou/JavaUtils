package com.example.log.controller;

import com.example.log.entity.AccessLog;
import com.example.log.service.AccessLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 访问日志控制器
 */
@RestController
@RequestMapping("/log")
@Api(tags = "访问日志管理")
public class AccessLogController {
    
    @Autowired
    private AccessLogService accessLogService;
    
    /**
     * 根据用户ID查询访问日志
     */
    @GetMapping("/user/{userId}")
    @ApiOperation("根据用户ID查询访问日志")
    public ResponseEntity<Map<String, Object>> getAccessLogsByUserId(
            @ApiParam("用户ID") @PathVariable Long userId,
            @ApiParam("页码") @RequestParam(defaultValue = "0") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size) {
        
        Page<AccessLog> logs = accessLogService.getAccessLogsByUserId(userId, page, size);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", logs.getContent());
        result.put("total", logs.getTotalElements());
        result.put("totalPages", logs.getTotalPages());
        result.put("currentPage", page);
        result.put("pageSize", size);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 根据用户ID和时间范围查询访问日志
     */
    @GetMapping("/user/{userId}/range")
    @ApiOperation("根据用户ID和时间范围查询访问日志")
    public ResponseEntity<Map<String, Object>> getAccessLogsByUserIdAndTimeRange(
            @ApiParam("用户ID") @PathVariable Long userId,
            @ApiParam("开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @ApiParam("页码") @RequestParam(defaultValue = "0") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size) {
        
        Page<AccessLog> logs = accessLogService.getAccessLogsByUserIdAndTimeRange(userId, startTime, endTime, page, size);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", logs.getContent());
        result.put("total", logs.getTotalElements());
        result.put("totalPages", logs.getTotalPages());
        result.put("currentPage", page);
        result.put("pageSize", size);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 根据IP地址查询访问日志
     */
    @GetMapping("/ip/{ipAddress}")
    @ApiOperation("根据IP地址查询访问日志")
    public ResponseEntity<Map<String, Object>> getAccessLogsByIpAddress(
            @ApiParam("IP地址") @PathVariable String ipAddress,
            @ApiParam("页码") @RequestParam(defaultValue = "0") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size) {
        
        Page<AccessLog> logs = accessLogService.getAccessLogsByIpAddress(ipAddress, page, size);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", logs.getContent());
        result.put("total", logs.getTotalElements());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 根据请求URL模糊查询
     */
    @GetMapping("/url")
    @ApiOperation("根据请求URL模糊查询")
    public ResponseEntity<Map<String, Object>> getAccessLogsByUrl(
            @ApiParam("请求URL") @RequestParam String url,
            @ApiParam("页码") @RequestParam(defaultValue = "0") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size) {
        
        Page<AccessLog> logs = accessLogService.getAccessLogsByUrl(url, page, size);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", logs.getContent());
        result.put("total", logs.getTotalElements());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 查询异常日志
     */
    @GetMapping("/exceptions")
    @ApiOperation("查询异常日志")
    public ResponseEntity<Map<String, Object>> getExceptionLogs(
            @ApiParam("页码") @RequestParam(defaultValue = "0") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size) {
        
        Page<AccessLog> logs = accessLogService.getExceptionLogs(page, size);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", logs.getContent());
        result.put("total", logs.getTotalElements());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 查询所有访问日志
     */
    @GetMapping("/all")
    @ApiOperation("查询所有访问日志")
    public ResponseEntity<Map<String, Object>> getAllAccessLogs(
            @ApiParam("页码") @RequestParam(defaultValue = "0") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size) {
        
        Page<AccessLog> logs = accessLogService.getAllAccessLogs(page, size);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", logs.getContent());
        result.put("total", logs.getTotalElements());
        result.put("totalPages", logs.getTotalPages());
        result.put("currentPage", page);
        result.put("pageSize", size);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取访问日志详情
     */
    @GetMapping("/{id}")
    @ApiOperation("获取访问日志详情")
    public ResponseEntity<Map<String, Object>> getAccessLogById(
            @ApiParam("日志ID") @PathVariable Long id) {
        
        AccessLog log = accessLogService.getAccessLogById(id);
        
        Map<String, Object> result = new HashMap<>();
        if (log != null) {
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", log);
        } else {
            result.put("code", 404);
            result.put("message", "日志不存在");
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取访问量统计
     */
    @GetMapping("/statistics/access")
    @ApiOperation("获取访问量统计")
    public ResponseEntity<Map<String, Object>> getAccessStatistics(
            @ApiParam("开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime) {
        
        List<Map<String, Object>> statistics = accessLogService.getAccessStatistics(startTime);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", statistics);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取用户访问统计
     */
    @GetMapping("/statistics/user")
    @ApiOperation("获取用户访问统计")
    public ResponseEntity<Map<String, Object>> getUserAccessStatistics(
            @ApiParam("开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam("限制数量") @RequestParam(defaultValue = "10") int limit) {
        
        List<Map<String, Object>> statistics = accessLogService.getUserAccessStatistics(startTime, limit);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", statistics);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取接口访问统计
     */
    @GetMapping("/statistics/api")
    @ApiOperation("获取接口访问统计")
    public ResponseEntity<Map<String, Object>> getApiAccessStatistics(
            @ApiParam("开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam("限制数量") @RequestParam(defaultValue = "10") int limit) {
        
        List<Map<String, Object>> statistics = accessLogService.getApiAccessStatistics(startTime, limit);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", statistics);
        
        return ResponseEntity.ok(result);
    }
}
