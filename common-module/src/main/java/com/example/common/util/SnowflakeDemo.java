package com.example.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 雪花算法ID生成器交互式演示工具
 * 提供菜单选择不同的功能测试
 */
public class SnowflakeDemo {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        System.out.println("🎯 雪花算法ID生成器交互式演示工具");
        System.out.println("=====================================\n");
        
        // 创建生成器实例
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
        
        boolean running = true;
        while (running) {
            showMenu();
            int choice = getIntInput("请选择功能 (0-8): ");
            
            switch (choice) {
                case 1:
                    generateSingleId(generator);
                    break;
                case 2:
                    generateMultipleIds(generator);
                    break;
                case 3:
                    parseId();
                    break;
                case 4:
                    performanceTest(generator);
                    break;
                case 5:
                    uniquenessTest(generator);
                    break;
                case 6:
                    incrementTest(generator);
                    break;
                case 7:
                    compareConfigurations();
                    break;
                case 8:
                    saveIdsToFile(generator);
                    break;
                case 0:
                    running = false;
                    System.out.println("👋 感谢使用雪花算法ID生成器演示工具！");
                    break;
                default:
                    System.out.println("❌ 无效选择，请重新输入！\n");
            }
            
            if (running && choice != 0) {
                System.out.println("\n按回车键继续...");
                scanner.nextLine();
                System.out.println();
            }
        }
        
        scanner.close();
    }
    
    /**
     * 显示菜单
     */
    private static void showMenu() {
        System.out.println("📋 功能菜单:");
        System.out.println("  1. 生成单个ID");
        System.out.println("  2. 批量生成ID");
        System.out.println("  3. 解析ID信息");
        System.out.println("  4. 性能测试");
        System.out.println("  5. 唯一性测试");
        System.out.println("  6. 递增性测试");
        System.out.println("  7. 不同配置比较");
        System.out.println("  8. 保存ID到文件");
        System.out.println("  0. 退出程序");
        System.out.println();
    }
    
    /**
     * 生成单个ID
     */
    private static void generateSingleId(SnowflakeIdGenerator generator) {
        System.out.println("🔧 生成单个ID:");
        Long id = generator.nextId();
        System.out.println("  生成的ID: " + id);
        System.out.println("  生成时间: " + LocalDateTime.now().format(FORMATTER));
    }
    
    /**
     * 批量生成ID
     */
    private static void generateMultipleIds(SnowflakeIdGenerator generator) {
        int count = getIntInput("请输入要生成的ID数量 (1-100): ");
        if (count < 1 || count > 100) {
            System.out.println("❌ 数量必须在1-100之间！");
            return;
        }
        
        System.out.println("📦 批量生成 " + count + " 个ID:");
        
        long startTime = System.currentTimeMillis();
        List<Long> ids = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            ids.add(generator.nextId());
        }
        
        long endTime = System.currentTimeMillis();
        
        System.out.println("  生成耗时: " + (endTime - startTime) + "ms");
        System.out.println("  生成的ID:");
        
        for (int i = 0; i < ids.size(); i++) {
            System.out.println("    " + (i + 1) + ": " + ids.get(i));
        }
    }
    
    /**
     * 解析ID信息
     */
    private static void parseId() {
        System.out.println("🔍 ID解析功能:");
        
        // 先生成一个示例ID
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
        Long exampleId = generator.nextId();
        
        System.out.println("  示例ID: " + exampleId);
        System.out.print("  是否使用示例ID？(y/n): ");
        String useExample = scanner.nextLine().trim().toLowerCase();
        
        Long idToParse;
        if (useExample.equals("y") || useExample.equals("yes")) {
            idToParse = exampleId;
        } else {
            System.out.print("  请输入要解析的ID: ");
            try {
                idToParse = Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("❌ 无效的ID格式！");
                return;
            }
        }
        
        try {
            long timestamp = IdUtils.parseTimestamp(idToParse);
            long datacenterId = IdUtils.parseDatacenterId(idToParse);
            long machineId = IdUtils.parseMachineId(idToParse);
            long sequence = IdUtils.parseSequence(idToParse);
            
            LocalDateTime generatedTime = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(timestamp), 
                java.time.ZoneId.systemDefault()
            );
            
            System.out.println("  解析结果:");
            System.out.println("    ID: " + idToParse);
            System.out.println("    时间戳: " + timestamp);
            System.out.println("    生成时间: " + generatedTime.format(FORMATTER));
            System.out.println("    数据中心ID: " + datacenterId);
            System.out.println("    机器ID: " + machineId);
            System.out.println("    序列号: " + sequence);
            
            // 计算ID年龄
            long age = System.currentTimeMillis() - timestamp;
            System.out.println("    ID年龄: " + age + "ms");
            
        } catch (Exception e) {
            System.out.println("❌ 解析失败: " + e.getMessage());
        }
    }
    
    /**
     * 性能测试
     */
    private static void performanceTest(SnowflakeIdGenerator generator) {
        System.out.println("⚡ 性能测试:");
        
        int[] testCounts = {1000, 10000, 100000, 1000000};
        
        for (int count : testCounts) {
            System.out.println("  测试 " + count + " 个ID:");
            
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                generator.nextId();
            }
            long endTime = System.currentTimeMillis();
            
            long duration = endTime - startTime;
            double avgTime = (double) duration / count;
            double idsPerSecond = count * 1000.0 / duration;
            
            System.out.println("    耗时: " + duration + "ms");
            System.out.println("    平均每个ID: " + String.format("%.6f", avgTime) + "ms");
            System.out.println("    每秒生成: " + String.format("%.0f", idsPerSecond) + " 个ID");
        }
    }
    
    /**
     * 唯一性测试
     */
    private static void uniquenessTest(SnowflakeIdGenerator generator) {
        System.out.println("🔑 唯一性测试:");
        
        int count = getIntInput("请输入测试数量 (1000-100000): ");
        if (count < 1000 || count > 100000) {
            System.out.println("❌ 数量必须在1000-100000之间！");
            return;
        }
        
        System.out.println("  测试生成 " + count + " 个ID的唯一性...");
        
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < count; i++) {
            ids.add(generator.nextId());
        }
        
        long endTime = System.currentTimeMillis();
        
        System.out.println("  生成ID数量: " + count);
        System.out.println("  唯一ID数量: " + ids.size());
        System.out.println("  唯一性测试: " + (ids.size() == count ? "✅ 通过" : "❌ 失败"));
        System.out.println("  测试耗时: " + (endTime - startTime) + "ms");
    }
    
    /**
     * 递增性测试
     */
    private static void incrementTest(SnowflakeIdGenerator generator) {
        System.out.println("📈 递增性测试:");
        
        int count = getIntInput("请输入测试数量 (10-1000): ");
        if (count < 10 || count > 1000) {
            System.out.println("❌ 数量必须在10-1000之间！");
            return;
        }
        
        System.out.println("  测试 " + count + " 个ID的递增性...");
        
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(generator.nextId());
        }
        
        boolean isIncreasing = true;
        int nonIncreasingCount = 0;
        
        for (int i = 1; i < ids.size(); i++) {
            if (ids.get(i) <= ids.get(i - 1)) {
                isIncreasing = false;
                nonIncreasingCount++;
            }
        }
        
        System.out.println("  递增性测试: " + (isIncreasing ? "✅ 通过" : "❌ 失败"));
        if (!isIncreasing) {
            System.out.println("  非递增数量: " + nonIncreasingCount);
        }
        
        // 显示部分ID
        System.out.println("  前3个ID:");
        for (int i = 0; i < Math.min(3, ids.size()); i++) {
            System.out.println("    " + (i + 1) + ": " + ids.get(i));
        }
        
        if (ids.size() > 3) {
            System.out.println("  后3个ID:");
            for (int i = ids.size() - 3; i < ids.size(); i++) {
                System.out.println("    " + (i + 1) + ": " + ids.get(i));
            }
        }
    }
    
    /**
     * 比较不同配置
     */
    private static void compareConfigurations() {
        System.out.println("⚙️  不同配置比较:");
        
        SnowflakeIdGenerator gen1 = new SnowflakeIdGenerator(1, 1);
        SnowflakeIdGenerator gen2 = new SnowflakeIdGenerator(1, 2);
        SnowflakeIdGenerator gen3 = new SnowflakeIdGenerator(2, 1);
        
        System.out.println("  数据中心1-机器1: " + gen1.nextId());
        System.out.println("  数据中心1-机器2: " + gen2.nextId());
        System.out.println("  数据中心2-机器1: " + gen3.nextId());
        
        // 解析这些ID
        System.out.println("\n  解析结果:");
        Long id1 = gen1.nextId();
        Long id2 = gen2.nextId();
        Long id3 = gen3.nextId();
        
        System.out.println("  ID " + id1 + ":");
        System.out.println("    数据中心ID: " + IdUtils.parseDatacenterId(id1));
        System.out.println("    机器ID: " + IdUtils.parseMachineId(id1));
        
        System.out.println("  ID " + id2 + ":");
        System.out.println("    数据中心ID: " + IdUtils.parseDatacenterId(id2));
        System.out.println("    机器ID: " + IdUtils.parseMachineId(id2));
        
        System.out.println("  ID " + id3 + ":");
        System.out.println("    数据中心ID: " + IdUtils.parseDatacenterId(id3));
        System.out.println("    机器ID: " + IdUtils.parseMachineId(id3));
    }
    
    /**
     * 保存ID到文件
     */
    private static void saveIdsToFile(SnowflakeIdGenerator generator) {
        System.out.println("💾 保存ID到文件:");
        
        int count = getIntInput("请输入要生成的ID数量 (1-10000): ");
        if (count < 1 || count > 10000) {
            System.out.println("❌ 数量必须在1-10000之间！");
            return;
        }
        
        System.out.print("  请输入文件名 (默认: snowflake_ids.txt): ");
        String filename = scanner.nextLine().trim();
        if (filename.isEmpty()) {
            filename = "snowflake_ids.txt";
        }
        
        try {
            java.io.PrintWriter writer = new java.io.PrintWriter(filename);
            
            writer.println("# 雪花算法生成的ID列表");
            writer.println("# 生成时间: " + LocalDateTime.now().format(FORMATTER));
            writer.println("# 生成数量: " + count);
            writer.println("# 格式: 序号,ID,时间戳,数据中心ID,机器ID,序列号");
            writer.println();
            
            System.out.println("  正在生成 " + count + " 个ID...");
            
            for (int i = 0; i < count; i++) {
                Long id = generator.nextId();
                long timestamp = IdUtils.parseTimestamp(id);
                long datacenterId = IdUtils.parseDatacenterId(id);
                long machineId = IdUtils.parseMachineId(id);
                long sequence = IdUtils.parseSequence(id);
                
                writer.printf("%d,%d,%d,%d,%d,%d%n", 
                    i + 1, id, timestamp, datacenterId, machineId, sequence);
                
                if ((i + 1) % 1000 == 0) {
                    System.out.println("    已生成 " + (i + 1) + " 个ID...");
                }
            }
            
            writer.close();
            System.out.println("✅ 成功保存 " + count + " 个ID到文件: " + filename);
            
        } catch (Exception e) {
            System.out.println("❌ 保存文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取整数输入
     */
    private static int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("❌ 请输入有效的数字！");
            }
        }
    }
}
