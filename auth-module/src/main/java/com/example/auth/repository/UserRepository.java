package com.example.auth.repository;

import com.example.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据用户名或邮箱查找用户
     */
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 查找启用的用户
     */
    List<User> findByEnabledTrue();
    
    /**
     * 根据角色查找用户
     */
    List<User> findByRole(String role);
    
    /**
     * 查找最近登录的用户
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :since ORDER BY u.lastLoginAt DESC")
    List<User> findRecentlyLoggedInUsers(@Param("since") LocalDateTime since);
    
    /**
     * 更新用户登录信息
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime, u.loginCount = u.loginCount + 1 WHERE u.id = :userId")
    void updateLoginInfo(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);
    
    /**
     * 统计用户总数
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countEnabledUsers();
    
    /**
     * 根据角色统计用户数
     */
    long countByRole(String role);
    
    /**
     * 查找指定时间后注册的用户
     */
    List<User> findByCreatedAtAfter(LocalDateTime createdAt);
    
    /**
     * 软删除用户（设置为禁用）
     */
    @Modifying
    @Query("UPDATE User u SET u.enabled = false WHERE u.id = :userId")
    void softDeleteUser(@Param("userId") Long userId);
    
    /**
     * 批量启用/禁用用户
     */
    @Modifying
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.id IN :userIds")
    void updateUsersEnabled(@Param("userIds") List<Long> userIds, @Param("enabled") Boolean enabled);
}
