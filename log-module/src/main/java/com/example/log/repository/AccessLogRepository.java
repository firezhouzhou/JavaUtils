package com.example.log.repository;

import com.example.log.entity.AccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 访问日志Repository
 */
@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
    
    /**
     * 根据用户ID查询访问日志
     */
    Page<AccessLog> findByUserIdOrderByRequestTimeDesc(Long userId, Pageable pageable);
    
    /**
     * 根据用户ID和时间范围查询访问日志
     */
    Page<AccessLog> findByUserIdAndRequestTimeBetweenOrderByRequestTimeDesc(
            Long userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 根据IP地址查询访问日志
     */
    Page<AccessLog> findByIpAddressOrderByRequestTimeDesc(String ipAddress, Pageable pageable);
    
    /**
     * 根据请求URL模糊查询
     */
    Page<AccessLog> findByRequestUrlContainingOrderByRequestTimeDesc(String url, Pageable pageable);
    
    /**
     * 根据模块名称查询
     */
    Page<AccessLog> findByModuleNameOrderByRequestTimeDesc(String moduleName, Pageable pageable);
    
    /**
     * 查询异常日志
     */
    Page<AccessLog> findByExceptionInfoIsNotNullOrderByRequestTimeDesc(Pageable pageable);
    
    /**
     * 根据响应状态码查询
     */
    Page<AccessLog> findByResponseStatusOrderByRequestTimeDesc(Integer responseStatus, Pageable pageable);
    
    /**
     * 统计用户访问次数
     */
    @Query("SELECT COUNT(a) FROM AccessLog a WHERE a.userId = :userId AND a.requestTime >= :startTime")
    Long countByUserIdAndRequestTimeAfter(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);
    
    /**
     * 统计IP访问次数
     */
    @Query("SELECT COUNT(a) FROM AccessLog a WHERE a.ipAddress = :ipAddress AND a.requestTime >= :startTime")
    Long countByIpAddressAndRequestTimeAfter(@Param("ipAddress") String ipAddress, @Param("startTime") LocalDateTime startTime);
    
    /**
     * 获取访问量统计
     */
    @Query("SELECT DATE(a.requestTime) as date, COUNT(a) as count " +
           "FROM AccessLog a " +
           "WHERE a.requestTime >= :startTime " +
           "GROUP BY DATE(a.requestTime) " +
           "ORDER BY DATE(a.requestTime)")
    List<Object[]> getAccessStatistics(@Param("startTime") LocalDateTime startTime);
    
    /**
     * 获取用户访问统计
     */
    @Query("SELECT a.userId, a.username, COUNT(a) as count " +
           "FROM AccessLog a " +
           "WHERE a.userId IS NOT NULL AND a.requestTime >= :startTime " +
           "GROUP BY a.userId, a.username " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> getUserAccessStatistics(@Param("startTime") LocalDateTime startTime, Pageable pageable);
    
    /**
     * 获取接口访问统计
     */
    @Query("SELECT a.requestUrl, COUNT(a) as count, AVG(a.executionTime) as avgTime " +
           "FROM AccessLog a " +
           "WHERE a.requestTime >= :startTime " +
           "GROUP BY a.requestUrl " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> getApiAccessStatistics(@Param("startTime") LocalDateTime startTime, Pageable pageable);
}
