package com.example.user.service;

import com.example.common.util.PasswordUtil;
import com.example.common.web.PageResponse;
import com.example.user.entity.User;
import com.example.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String USER_CACHE_PREFIX = "user:";
    private static final String LOGIN_CACHE_PREFIX = "login:";
    
    /**
     * 用户注册
     */
    public User register(String username, String password, String email) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsernameAndDeleted(username, 0)) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (email != null && userRepository.existsByEmailAndDeleted(email, 0)) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.encode(password));
        user.setEmail(email);
        user.setNickname(username);
        
        return userRepository.save(user);
    }
    
    /**
     * 根据用户名或邮箱查找用户
     */
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        // 先从缓存中查找
        String cacheKey = USER_CACHE_PREFIX + usernameOrEmail;
        User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            return Optional.of(cachedUser);
        }
        
        // 从数据库查找
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(usernameOrEmail);
        if (userOpt.isPresent()) {
            // 缓存用户信息，过期时间30分钟
            redisTemplate.opsForValue().set(cacheKey, userOpt.get(), 30, TimeUnit.MINUTES);
        }
        
        return userOpt;
    }
    
    /**
     * 根据ID查找用户
     */
    public Optional<User> findById(Long id) {
        String cacheKey = USER_CACHE_PREFIX + "id:" + id;
        User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            return Optional.of(cachedUser);
        }
        
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            redisTemplate.opsForValue().set(cacheKey, userOpt.get(), 30, TimeUnit.MINUTES);
        }
        
        return userOpt;
    }
    
    /**
     * 更新用户信息
     */
    public User updateUser(Long id, User updateUser) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 更新字段
        if (updateUser.getNickname() != null) {
            existingUser.setNickname(updateUser.getNickname());
        }
        if (updateUser.getEmail() != null) {
            existingUser.setEmail(updateUser.getEmail());
        }
        if (updateUser.getPhone() != null) {
            existingUser.setPhone(updateUser.getPhone());
        }
        if (updateUser.getAvatar() != null) {
            existingUser.setAvatar(updateUser.getAvatar());
        }
        if (updateUser.getGender() != null) {
            existingUser.setGender(updateUser.getGender());
        }
        if (updateUser.getBirthday() != null) {
            existingUser.setBirthday(updateUser.getBirthday());
        }
        
        existingUser.setUpdateTime(LocalDateTime.now());
        
        User savedUser = userRepository.save(existingUser);
        
        // 清除缓存
        clearUserCache(existingUser.getUsername(), existingUser.getId());
        
        return savedUser;
    }
    
    /**
     * 更新登录信息
     */
    public void updateLoginInfo(Long userId, String loginIp) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(loginIp);
        user.setUpdateTime(LocalDateTime.now());
        
        userRepository.save(user);
        
        // 清除缓存
        clearUserCache(user.getUsername(), userId);
    }
    
    /**
     * 分页查询用户
     */
    public PageResponse<User> findUsers(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<User> userPage;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 这里可以添加关键字搜索逻辑
            userPage = userRepository.findAll(pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        
        return new PageResponse<>(
            (long) page,
            (long) size,
            userPage.getTotalElements(),
            userPage.getContent()
        );
    }
    
    /**
     * 缓存登录状态
     */
    public void cacheLoginStatus(String token, Long userId, int expireSeconds) {
        String cacheKey = LOGIN_CACHE_PREFIX + token;
        redisTemplate.opsForValue().set(cacheKey, userId, expireSeconds, TimeUnit.SECONDS);
    }
    
    /**
     * 获取登录用户ID
     */
    public Long getLoginUserId(String token) {
        String cacheKey = LOGIN_CACHE_PREFIX + token;
        return (Long) redisTemplate.opsForValue().get(cacheKey);
    }
    
    /**
     * 清除登录状态
     */
    public void clearLoginStatus(String token) {
        String cacheKey = LOGIN_CACHE_PREFIX + token;
        redisTemplate.delete(cacheKey);
    }
    
    /**
     * 修改密码
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证原密码
        if (!PasswordUtil.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        // 更新密码
        user.setPassword(PasswordUtil.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        
        userRepository.save(user);
        
        // 清除缓存
        clearUserCache(user.getUsername(), userId);
    }
    
    /**
     * 上传头像
     */
    public String uploadAvatar(Long userId, org.springframework.web.multipart.MultipartFile file) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 这里应该调用文件服务上传头像，简化处理
        String avatarUrl = "/uploads/avatar/" + userId + "_" + System.currentTimeMillis() + ".jpg";
        
        user.setAvatar(avatarUrl);
        user.setUpdateTime(LocalDateTime.now());
        
        userRepository.save(user);
        
        // 清除缓存
        clearUserCache(user.getUsername(), userId);
        
        return avatarUrl;
    }
    
    /**
     * 更新用户状态
     */
    public void updateUserStatus(Long userId, Integer status) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        
        userRepository.save(user);
        
        // 清除缓存
        clearUserCache(user.getUsername(), userId);
    }
    
    /**
     * 删除用户（逻辑删除）
     */
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setDeleted(1);
        user.setUpdateTime(LocalDateTime.now());
        
        userRepository.save(user);
        
        // 清除缓存
        clearUserCache(user.getUsername(), userId);
    }
    
    /**
     * 批量删除用户
     */
    public void batchDeleteUsers(java.util.List<Long> userIds) {
        for (Long userId : userIds) {
            try {
                deleteUser(userId);
            } catch (Exception e) {
                // 记录错误但继续处理其他用户
                System.err.println("删除用户失败: " + userId + ", 错误: " + e.getMessage());
            }
        }
    }
    
    /**
     * 重置密码
     */
    public String resetPassword(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 生成随机密码
        String newPassword = generateRandomPassword();
        
        user.setPassword(PasswordUtil.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        
        userRepository.save(user);
        
        // 清除缓存
        clearUserCache(user.getUsername(), userId);
        
        return newPassword;
    }
    
    /**
     * 检查用户名是否可用
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsernameAndDeleted(username, 0);
    }
    
    /**
     * 检查邮箱是否可用
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmailAndDeleted(email, 0);
    }
    
    /**
     * 获取用户统计信息
     */
    public com.example.user.controller.UserController.UserStatistics getUserStatistics() {
        // 模拟统计数据，实际项目中应该从数据库查询
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatusAndDeleted(1, 0);
        long inactiveUsers = userRepository.countByStatusAndDeleted(0, 0);
        
        // 今日注册用户数
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayRegistrations = userRepository.countByCreateTimeAfterAndDeleted(startOfDay, 0);
        
        // 本周注册用户数
        LocalDateTime startOfWeek = LocalDateTime.now().minusDays(7);
        long weekRegistrations = userRepository.countByCreateTimeAfterAndDeleted(startOfWeek, 0);
        
        // 本月注册用户数
        LocalDateTime startOfMonth = LocalDateTime.now().minusDays(30);
        long monthRegistrations = userRepository.countByCreateTimeAfterAndDeleted(startOfMonth, 0);
        
        return new com.example.user.controller.UserController.UserStatistics(
            totalUsers, activeUsers, inactiveUsers, todayRegistrations, weekRegistrations, monthRegistrations);
    }
    
    /**
     * 生成随机密码
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    /**
     * 清除用户缓存
     */
    private void clearUserCache(String username, Long userId) {
        redisTemplate.delete(USER_CACHE_PREFIX + username);
        redisTemplate.delete(USER_CACHE_PREFIX + "id:" + userId);
    }
}
