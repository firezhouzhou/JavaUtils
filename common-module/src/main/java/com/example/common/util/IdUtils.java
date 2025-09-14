package com.example.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * ID生成工具类
 * 提供静态方法方便调用
 */
@Component
public class IdUtils {

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    private static SnowflakeIdGenerator staticSnowflakeIdGenerator;
    
    // 静态初始化，确保在没有Spring容器时也能工作
    static {
        if (staticSnowflakeIdGenerator == null) {
            staticSnowflakeIdGenerator = new SnowflakeIdGenerator(1, 1);
        }
    }

    @PostConstruct
    public void init() {
        if (snowflakeIdGenerator != null) {
            staticSnowflakeIdGenerator = snowflakeIdGenerator;
        }
    }

    /**
     * 生成雪花算法ID
     * 
     * @return 唯一ID
     */
    public static long generateId() {
        return staticSnowflakeIdGenerator.nextId();
    }

    /**
     * 生成雪花算法ID字符串
     * 
     * @return 唯一ID字符串
     */
    public static String generateIdStr() {
        return staticSnowflakeIdGenerator.nextIdStr();
    }

    /**
     * 解析ID获取生成时间
     * 
     * @param id 雪花ID
     * @return 生成时间戳
     */
    public static long parseTimestamp(long id) {
        return staticSnowflakeIdGenerator.parseTimestamp(id);
    }

    /**
     * 解析ID获取数据中心ID
     * 
     * @param id 雪花ID
     * @return 数据中心ID
     */
    public static long parseDatacenterId(long id) {
        return staticSnowflakeIdGenerator.parseDatacenterId(id);
    }

    /**
     * 解析ID获取机器ID
     * 
     * @param id 雪花ID
     * @return 机器ID
     */
    public static long parseMachineId(long id) {
        return staticSnowflakeIdGenerator.parseMachineId(id);
    }

    /**
     * 解析ID获取序列号
     * 
     * @param id 雪花ID
     * @return 序列号
     */
    public static long parseSequence(long id) {
        return staticSnowflakeIdGenerator.parseSequence(id);
    }

    /**
     * 获取生成器信息
     * 
     * @return 生成器信息
     */
    public static String getGeneratorInfo() {
        return staticSnowflakeIdGenerator.getGeneratorInfo();
    }
}
