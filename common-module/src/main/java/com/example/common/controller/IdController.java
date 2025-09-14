package com.example.common.controller;

import com.example.common.util.IdUtils;
import com.example.common.util.SnowflakeIdGenerator;
import com.example.common.web.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ID生成控制器
 * 提供雪花算法ID的生成和解析服务
 */
@RestController
@RequestMapping("/common/id")
@Api(tags = "ID生成服务")
public class IdController {

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 生成单个雪花算法ID
     */
    @GetMapping("/generate")
    @ApiOperation("生成单个雪花算法ID")
    public ApiResponse<Map<String, Object>> generateId() {
        long id = IdUtils.generateId();
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("idStr", String.valueOf(id));
        result.put("timestamp", System.currentTimeMillis());
        result.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return ApiResponse.success(result);
    }

    /**
     * 生成字符串格式的雪花算法ID
     */
    @GetMapping("/generate/string")
    @ApiOperation("生成字符串格式的雪花算法ID")
    public ApiResponse<Map<String, Object>> generateIdString() {
        String idStr = IdUtils.generateIdStr();
        
        Map<String, Object> result = new HashMap<>();
        result.put("idStr", idStr);
        result.put("id", Long.parseLong(idStr));
        result.put("timestamp", System.currentTimeMillis());
        result.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return ApiResponse.success(result);
    }

    /**
     * 批量生成雪花算法ID
     */
    @GetMapping("/generate/batch")
    @ApiOperation("批量生成雪花算法ID")
    public ApiResponse<Map<String, Object>> generateBatchIds(
            @ApiParam("生成数量，最大100") @RequestParam(defaultValue = "10") int count) {
        
        if (count <= 0 || count > 100) {
            return ApiResponse.error(400, "生成数量必须在1-100之间");
        }
        
        List<Long> ids = new ArrayList<>();
        List<String> idStrs = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            long id = IdUtils.generateId();
            ids.add(id);
            idStrs.add(String.valueOf(id));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("ids", ids);
        result.put("idStrs", idStrs);
        result.put("timestamp", System.currentTimeMillis());
        result.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return ApiResponse.success(result);
    }

    /**
     * 解析雪花算法ID
     */
    @GetMapping("/parse/{id}")
    @ApiOperation("解析雪花算法ID")
    public ApiResponse<Map<String, Object>> parseId(
            @ApiParam("要解析的雪花算法ID") @PathVariable Long id) {
        
        try {
            long timestamp = IdUtils.parseTimestamp(id);
            long datacenterId = IdUtils.parseDatacenterId(id);
            long machineId = IdUtils.parseMachineId(id);
            long sequence = IdUtils.parseSequence(id);
            
            LocalDateTime generatedTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
            
            Map<String, Object> result = new HashMap<>();
            result.put("originalId", id);
            result.put("originalIdStr", String.valueOf(id));
            result.put("timestamp", timestamp);
            result.put("generatedTime", generatedTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            result.put("datacenterId", datacenterId);
            result.put("machineId", machineId);
            result.put("sequence", sequence);
            
            // 计算ID生成到现在的时间差
            long timeDiff = System.currentTimeMillis() - timestamp;
            result.put("ageMs", timeDiff);
            result.put("ageSeconds", timeDiff / 1000);
            result.put("ageMinutes", timeDiff / (1000 * 60));
            
            return ApiResponse.success(result);
            
        } catch (Exception e) {
            return ApiResponse.error(400, "无效的雪花算法ID: " + e.getMessage());
        }
    }

    /**
     * 批量解析雪花算法ID
     */
    @PostMapping("/parse/batch")
    @ApiOperation("批量解析雪花算法ID")
    public ApiResponse<Map<String, Object>> parseBatchIds(
            @ApiParam("要解析的ID列表") @RequestBody List<Long> ids) {
        
        if (ids == null || ids.isEmpty()) {
            return ApiResponse.error(400, "ID列表不能为空");
        }
        
        if (ids.size() > 50) {
            return ApiResponse.error(400, "一次最多解析50个ID");
        }
        
        List<Map<String, Object>> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (Long id : ids) {
            try {
                long timestamp = IdUtils.parseTimestamp(id);
                long datacenterId = IdUtils.parseDatacenterId(id);
                long machineId = IdUtils.parseMachineId(id);
                long sequence = IdUtils.parseSequence(id);
                
                LocalDateTime generatedTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
                
                Map<String, Object> result = new HashMap<>();
                result.put("originalId", id);
                result.put("timestamp", timestamp);
                result.put("generatedTime", generatedTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                result.put("datacenterId", datacenterId);
                result.put("machineId", machineId);
                result.put("sequence", sequence);
                
                results.add(result);
                
            } catch (Exception e) {
                errors.add("ID " + id + " 解析失败: " + e.getMessage());
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", ids.size());
        response.put("successCount", results.size());
        response.put("errorCount", errors.size());
        response.put("results", results);
        
        if (!errors.isEmpty()) {
            response.put("errors", errors);
        }
        
        return ApiResponse.success(response);
    }

    /**
     * 获取ID生成器信息
     */
    @GetMapping("/info")
    @ApiOperation("获取ID生成器信息")
    public ApiResponse<Map<String, Object>> getGeneratorInfo() {
        String generatorInfo = IdUtils.getGeneratorInfo();
        
        // 生成一个示例ID用于展示
        long sampleId = IdUtils.generateId();
        
        Map<String, Object> result = new HashMap<>();
        result.put("generatorInfo", generatorInfo);
        result.put("sampleId", sampleId);
        result.put("sampleIdStr", String.valueOf(sampleId));
        result.put("currentTimestamp", System.currentTimeMillis());
        result.put("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // 添加雪花算法的基本信息
        Map<String, Object> algorithmInfo = new HashMap<>();
        algorithmInfo.put("name", "Snowflake Algorithm");
        algorithmInfo.put("totalBits", 64);
        algorithmInfo.put("timestampBits", 41);
        algorithmInfo.put("datacenterIdBits", 5);
        algorithmInfo.put("machineIdBits", 5);
        algorithmInfo.put("sequenceBits", 12);
        algorithmInfo.put("maxDatacenterId", 31);
        algorithmInfo.put("maxMachineId", 31);
        algorithmInfo.put("maxSequence", 4095);
        algorithmInfo.put("maxIdsPerMs", 4096);
        algorithmInfo.put("maxIdsPerSecond", 4096000);
        
        result.put("algorithmInfo", algorithmInfo);
        
        return ApiResponse.success(result);
    }

    /**
     * 健康检查 - 测试ID生成性能
     */
    @GetMapping("/health")
    @ApiOperation("ID生成器健康检查")
    public ApiResponse<Map<String, Object>> healthCheck() {
        try {
            long startTime = System.currentTimeMillis();
            
            // 生成100个ID测试性能
            List<Long> testIds = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                testIds.add(IdUtils.generateId());
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 验证ID的唯一性
            long distinctCount = testIds.stream().distinct().count();
            boolean isUnique = distinctCount == testIds.size();
            
            // 验证ID的递增性
            boolean isIncreasing = true;
            for (int i = 1; i < testIds.size(); i++) {
                if (testIds.get(i) <= testIds.get(i - 1)) {
                    isIncreasing = false;
                    break;
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "healthy");
            result.put("testCount", 100);
            result.put("durationMs", duration);
            result.put("avgTimePerIdMs", duration / 100.0);
            result.put("idsPerSecond", Math.round(100000.0 / duration));
            result.put("uniqueIds", isUnique);
            result.put("increasingOrder", isIncreasing);
            result.put("firstId", testIds.get(0));
            result.put("lastId", testIds.get(testIds.size() - 1));
            result.put("checkTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return ApiResponse.success(result);
            
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "unhealthy");
            result.put("error", e.getMessage());
            result.put("checkTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return ApiResponse.error(500, "ID生成器健康检查失败", result);
        }
    }
}
