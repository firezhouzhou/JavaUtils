package com.example.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * 雪花算法ID生成器测试工具类
 * 包含Main方法，用于测试和演示雪花算法ID生成功能
 */
public class SnowflakeIdTestTool {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    public static void main(String[] args) {
        System.out.println("=== 雪花算法ID生成器测试工具 ===\n");
        
        // 显示配置信息
        showConfiguration();
        
        // 基础功能测试
        testBasicGeneration();
        
        // 批量生成测试
        testBatchGeneration();
        
        // ID解析测试
        testIdParsing();
        
        // 性能测试
        testPerformance();
        
        // 并发测试
        testConcurrency();
        
        // 唯一性测试
        testUniqueness();
        
        // 递增性测试
        testIncrement();
        
        System.out.println("\n=== 所有测试完成 ===");
    }
    
    /**
     * 显示当前配置信息
     */
    private static void showConfiguration() {
        System.out.println("📋 配置信息:");
        System.out.println("  生成器信息: " + IdUtils.getGeneratorInfo());
        System.out.println("  当前时间: " + LocalDateTime.now().format(FORMATTER));
        System.out.println();
    }
    
    /**
     * 基础ID生成测试
     */
    private static void testBasicGeneration() {
        System.out.println("🔧 基础ID生成测试:");
        
        // 生成单个ID
        Long id1 = IdUtils.generateId();
        String idStr1 = IdUtils.generateIdStr();
        System.out.println("  单个ID: " + id1);
        System.out.println("  字符串ID: " + idStr1);
        
        // 连续生成几个ID
        System.out.println("  连续生成5个ID:");
        for (int i = 0; i < 5; i++) {
            Long id = IdUtils.generateId();
            System.out.println("    ID " + (i + 1) + ": " + id);
            try {
                Thread.sleep(1); // 确保时间戳不同
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println();
    }
    
    /**
     * 批量生成测试
     */
    private static void testBatchGeneration() {
        System.out.println("📦 批量生成测试:");
        
        int count = 10;
        List<Long> ids = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            ids.add(IdUtils.generateId());
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
     * ID解析测试
     */
    private static void testIdParsing() {
        System.out.println("🔍 ID解析测试:");
        
        Long testId = IdUtils.generateId();
        System.out.println("  测试ID: " + testId);
        
        try {
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
            
        } catch (Exception e) {
            System.out.println("  解析失败: " + e.getMessage());
        }
        System.out.println();
    }
    
    /**
     * 性能测试
     */
    private static void testPerformance() {
        System.out.println("⚡ 性能测试:");
        
        int testCount = 100000;
        
        // 单线程性能测试
        System.out.println("  单线程性能测试 (" + testCount + " 个ID):");
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < testCount; i++) {
            IdUtils.generateId();
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
     * 并发测试
     */
    private static void testConcurrency() {
        System.out.println("🔄 并发测试:");
        
        int threadCount = 10;
        int idsPerThread = 1000;
        Set<Long> allIds = ConcurrentHashMap.newKeySet();
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        long startTime = System.currentTimeMillis();
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < idsPerThread; j++) {
                    Long id = IdUtils.generateId();
                    allIds.add(id);
                }
                System.out.println("    线程 " + threadIndex + " 完成");
            }, executor);
            futures.add(future);
        }
        
        // 等待所有线程完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        int totalIds = threadCount * idsPerThread;
        
        System.out.println("  并发测试结果:");
        System.out.println("    线程数: " + threadCount);
        System.out.println("    每线程ID数: " + idsPerThread);
        System.out.println("    总ID数: " + totalIds);
        System.out.println("    唯一ID数: " + allIds.size());
        System.out.println("    总耗时: " + duration + "ms");
        System.out.println("    每秒生成: " + (totalIds * 1000 / duration) + " 个ID");
        System.out.println("    唯一性: " + (allIds.size() == totalIds ? "✅ 通过" : "❌ 失败"));
        
        executor.shutdown();
        System.out.println();
    }
    
    /**
     * 唯一性测试
     */
    private static void testUniqueness() {
        System.out.println("🔑 唯一性测试:");
        
        int testCount = 100000;
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        
        long startTime = System.currentTimeMillis();
        
        IntStream.range(0, testCount).parallel().forEach(i -> {
            Long id = IdUtils.generateId();
            ids.add(id);
        });
        
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
    private static void testIncrement() {
        System.out.println("📈 递增性测试:");
        
        int testCount = 1000;
        List<Long> ids = new ArrayList<>();
        
        // 生成测试ID
        for (int i = 0; i < testCount; i++) {
            ids.add(IdUtils.generateId());
            try {
                Thread.sleep(1); // 确保时间戳递增
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
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
     * 生成指定数量的ID并保存到文件（可选功能）
     */
    public static void generateIdsToFile(int count, String filename) {
        try {
            java.io.PrintWriter writer = new java.io.PrintWriter(filename);
            
            writer.println("# 雪花算法生成的ID列表");
            writer.println("# 生成时间: " + LocalDateTime.now().format(FORMATTER));
            writer.println("# 生成数量: " + count);
            writer.println("# 格式: ID,时间戳,数据中心ID,机器ID,序列号");
            writer.println();
            
            for (int i = 0; i < count; i++) {
                Long id = IdUtils.generateId();
                long timestamp = IdUtils.parseTimestamp(id);
                long datacenterId = IdUtils.parseDatacenterId(id);
                long machineId = IdUtils.parseMachineId(id);
                long sequence = IdUtils.parseSequence(id);
                
                writer.printf("%d,%d,%d,%d,%d%n", id, timestamp, datacenterId, machineId, sequence);
            }
            
            writer.close();
            System.out.println("✅ ID已保存到文件: " + filename);
            
        } catch (Exception e) {
            System.err.println("❌ 保存文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 比较不同时间间隔生成的ID差异
     */
    public static void testTimeInterval() {
        System.out.println("⏰ 时间间隔测试:");
        
        List<Long> ids = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>();
        
        // 在不同时间间隔生成ID
        int[] intervals = {0, 1, 10, 100, 1000}; // 毫秒
        
        for (int interval : intervals) {
            Long id = IdUtils.generateId();
            long timestamp = IdUtils.parseTimestamp(id);
            
            ids.add(id);
            timestamps.add(timestamp);
            
            System.out.println("  间隔 " + interval + "ms: ID=" + id + ", 时间戳=" + timestamp);
            
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println();
    }
}
