package com.example.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单的雪花算法测试工具
 * 独立运行，不依赖Spring容器
 */
public class SimpleSnowflakeTest {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    public static void main(String[] args) {
        System.out.println("=== 雪花算法ID生成器简单测试 ===\n");
        
        // 创建生成器实例
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
        
        // 显示配置信息
        showInfo(generator);
        
        // 基础功能测试
        testBasicGeneration(generator);
        
        // 批量生成测试
        testBatchGeneration(generator);
        
        // 性能测试
        testPerformance(generator);
        
        // 唯一性测试
        testUniqueness(generator);
        
        // 递增性测试
        testIncrement(generator);
        
        System.out.println("\n=== 测试完成 ===");
    }
    
    /**
     * 显示配置信息
     */
    private static void showInfo(SnowflakeIdGenerator generator) {
        System.out.println("📋 配置信息:");
        System.out.println("  数据中心ID: 1");
        System.out.println("  机器ID: 1");
        System.out.println("  当前时间: " + LocalDateTime.now().format(FORMATTER));
        System.out.println();
    }
    
    /**
     * 基础ID生成测试
     */
    private static void testBasicGeneration(SnowflakeIdGenerator generator) {
        System.out.println("🔧 基础ID生成测试:");
        
        // 生成单个ID
        long id1 = generator.nextId();
        System.out.println("  单个ID: " + id1);
        
        // 连续生成几个ID
        System.out.println("  连续生成5个ID:");
        for (int i = 0; i < 5; i++) {
            long id = generator.nextId();
            System.out.println("    ID " + (i + 1) + ": " + id);
        }
        System.out.println();
    }
    
    /**
     * 批量生成测试
     */
    private static void testBatchGeneration(SnowflakeIdGenerator generator) {
        System.out.println("📦 批量生成测试:");
        
        int count = 10;
        List<Long> ids = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            ids.add(generator.nextId());
        }
        long endTime = System.currentTimeMillis();
        
        System.out.println("  生成 " + count + " 个ID耗时: " + (endTime - startTime) + "ms");
        System.out.println("  生成的ID:");
        for (int i = 0; i < ids.size(); i++) {
            System.out.println("    " + (i + 1) + ": " + ids.get(i));
        }
        System.out.println();
    }
    
    /**
     * 性能测试
     */
    private static void testPerformance(SnowflakeIdGenerator generator) {
        System.out.println("⚡ 性能测试:");
        
        int testCount = 100000;
        
        System.out.println("  单线程性能测试 (" + testCount + " 个ID):");
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < testCount; i++) {
            generator.nextId();
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double avgTimePerId = (double) duration / testCount;
        double idsPerSecond = testCount * 1000.0 / duration;
        
        System.out.println("    总耗时: " + duration + "ms");
        System.out.println("    平均每个ID: " + String.format("%.4f", avgTimePerId) + "ms");
        System.out.println("    每秒生成: " + String.format("%.0f", idsPerSecond) + " 个ID");
        System.out.println();
    }
    
    /**
     * 唯一性测试
     */
    private static void testUniqueness(SnowflakeIdGenerator generator) {
        System.out.println("🔑 唯一性测试:");
        
        int testCount = 100000;
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < testCount; i++) {
            Long id = generator.nextId();
            ids.add(id);
        }
        
        long endTime = System.currentTimeMillis();
        
        System.out.println("  生成ID数量: " + testCount);
        System.out.println("  唯一ID数量: " + ids.size());
        System.out.println("  唯一性测试: " + (ids.size() == testCount ? "✅ 通过" : "❌ 失败"));
        System.out.println("  测试耗时: " + (endTime - startTime) + "ms");
        System.out.println();
    }
    
    /**
     * 递增性测试
     */
    private static void testIncrement(SnowflakeIdGenerator generator) {
        System.out.println("📈 递增性测试:");
        
        int testCount = 100;
        List<Long> ids = new ArrayList<>();
        
        // 生成测试ID
        for (int i = 0; i < testCount; i++) {
            ids.add(generator.nextId());
        }
        
        // 检查递增性
        boolean isIncreasing = true;
        int nonIncreasingCount = 0;
        
        for (int i = 1; i < ids.size(); i++) {
            if (ids.get(i) <= ids.get(i - 1)) {
                isIncreasing = false;
                nonIncreasingCount++;
            }
        }
        
        System.out.println("  测试ID数量: " + testCount);
        System.out.println("  递增性测试: " + (isIncreasing ? "✅ 通过" : "❌ 失败"));
        if (!isIncreasing) {
            System.out.println("  非递增数量: " + nonIncreasingCount);
        }
        
        // 显示前几个和后几个ID
        System.out.println("  前5个ID:");
        for (int i = 0; i < Math.min(5, ids.size()); i++) {
            System.out.println("    " + (i + 1) + ": " + ids.get(i));
        }
        
        if (ids.size() > 5) {
            System.out.println("  后5个ID:");
            for (int i = ids.size() - 5; i < ids.size(); i++) {
                System.out.println("    " + (i + 1) + ": " + ids.get(i));
            }
        }
        System.out.println();
    }
    
    /**
     * 演示ID解析功能
     */
    private static void testIdParsing() {
        System.out.println("🔍 ID解析测试:");
        
        // 创建生成器
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
        Long testId = generator.nextId();
        
        System.out.println("  测试ID: " + testId);
        
        // 解析ID的各个部分
        long timestamp = IdUtils.parseTimestamp(testId);
        long datacenterId = IdUtils.parseDatacenterId(testId);
        long machineId = IdUtils.parseMachineId(testId);
        long sequence = IdUtils.parseSequence(testId);
        
        LocalDateTime generatedTime = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp), 
            java.time.ZoneId.systemDefault()
        );
        
        System.out.println("  解析结果:");
        System.out.println("    时间戳: " + timestamp);
        System.out.println("    生成时间: " + generatedTime.format(FORMATTER));
        System.out.println("    数据中心ID: " + datacenterId);
        System.out.println("    机器ID: " + machineId);
        System.out.println("    序列号: " + sequence);
        
        // 计算ID生成到现在的时间差
        long age = System.currentTimeMillis() - timestamp;
        System.out.println("    ID年龄: " + age + "ms");
        System.out.println();
    }
    
    /**
     * 演示不同配置的生成器
     */
    private static void testDifferentConfigurations() {
        System.out.println("⚙️  不同配置测试:");
        
        // 创建不同配置的生成器
        SnowflakeIdGenerator generator1 = new SnowflakeIdGenerator(1, 1);
        SnowflakeIdGenerator generator2 = new SnowflakeIdGenerator(1, 2);
        SnowflakeIdGenerator generator3 = new SnowflakeIdGenerator(2, 1);
        
        System.out.println("  数据中心1-机器1: " + generator1.nextId());
        System.out.println("  数据中心1-机器2: " + generator2.nextId());
        System.out.println("  数据中心2-机器1: " + generator3.nextId());
        System.out.println();
    }
}
