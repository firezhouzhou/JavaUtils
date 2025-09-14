package com.example.user.repository;

import com.example.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsernameAndDeleted(String username, Integer deleted);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmailAndDeleted(String email, Integer deleted);
    
    /**
     * 根据手机号查找用户
     */
    Optional<User> findByPhoneAndDeleted(String phone, Integer deleted);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsernameAndDeleted(String username, Integer deleted);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmailAndDeleted(String email, Integer deleted);
    
    /**
     * 根据用户名或邮箱查找用户
     */
    @Query("SELECT u FROM User u WHERE (u.username = :usernameOrEmail OR u.email = :usernameOrEmail) AND u.deleted = 0")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);
    
    /**
     * 根据状态和删除标记统计用户数量
     */
    long countByStatusAndDeleted(Integer status, Integer deleted);
    
    /**
     * 统计指定时间后创建的用户数量
     */
    long countByCreateTimeAfterAndDeleted(LocalDateTime createTime, Integer deleted);
}
