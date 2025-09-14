package com.example.common.config;

import com.example.common.util.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * 雪花算法配置类
 */
@Configuration
public class SnowflakeConfig {

    private static final Logger logger = LoggerFactory.getLogger(SnowflakeConfig.class);

    @Value("${snowflake.datacenter-id:#{null}}")
    private Long datacenterId;

    @Value("${snowflake.machine-id:#{null}}")
    private Long machineId;

    /**
     * 雪花算法ID生成器Bean
     * 如果没有配置数据中心ID和机器ID，会自动根据IP地址生成
     */
    @Bean
    @ConditionalOnMissingBean
    public SnowflakeIdGenerator snowflakeIdGenerator() {
        long finalDatacenterId = datacenterId != null ? datacenterId : getDatacenterIdFromIp();
        long finalMachineId = machineId != null ? machineId : getMachineIdFromIp();
        
        logger.info("初始化雪花算法ID生成器: datacenterId={}, machineId={}", finalDatacenterId, finalMachineId);
        
        return new SnowflakeIdGenerator(finalDatacenterId, finalMachineId);
    }

    /**
     * 根据IP地址生成数据中心ID
     * 取IP地址最后一段的后5位作为数据中心ID
     */
    private long getDatacenterIdFromIp() {
        try {
            String ip = getLocalIpAddress();
            if (ip != null) {
                String[] segments = ip.split("\\.");
                if (segments.length == 4) {
                    int lastSegment = Integer.parseInt(segments[3]);
                    long id = lastSegment & 0x1F; // 取后5位
                    logger.info("根据IP地址{}生成数据中心ID: {}", ip, id);
                    return id;
                }
            }
        } catch (Exception e) {
            logger.warn("根据IP生成数据中心ID失败，使用默认值", e);
        }
        return 1L; // 默认数据中心ID
    }

    /**
     * 根据IP地址生成机器ID
     * 取IP地址倒数第二段的后5位作为机器ID
     */
    private long getMachineIdFromIp() {
        try {
            String ip = getLocalIpAddress();
            if (ip != null) {
                String[] segments = ip.split("\\.");
                if (segments.length == 4) {
                    int secondLastSegment = Integer.parseInt(segments[2]);
                    long id = secondLastSegment & 0x1F; // 取后5位
                    logger.info("根据IP地址{}生成机器ID: {}", ip, id);
                    return id;
                }
            }
        } catch (Exception e) {
            logger.warn("根据IP生成机器ID失败，使用默认值", e);
        }
        return 1L; // 默认机器ID
    }

    /**
     * 获取本机IP地址
     */
    private String getLocalIpAddress() {
        try {
            // 优先获取非回环地址
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }

            // 如果没有找到合适的IP，使用localhost
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            logger.error("获取本机IP地址失败", e);
            return null;
        }
    }
}
