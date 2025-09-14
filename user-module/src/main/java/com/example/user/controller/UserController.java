package com.example.user.controller;

import com.example.common.web.ApiResponse;
import com.example.common.web.PageResponse;
import com.example.common.util.JwtUtil;
import com.example.user.entity.User;
import com.example.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 用户控制器
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @ApiOperation("获取当前用户信息")
    @GetMapping("/profile")
    public ApiResponse<User> getCurrentUser(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        
        User user = userService.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 不返回敏感信息
        user.setPassword(null);
        
        return ApiResponse.success(user);
    }
    
    @ApiOperation("更新用户信息")
    @PutMapping("/profile")
    public ApiResponse<User> updateProfile(@Valid @RequestBody UpdateUserRequest request,
                                         HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        
        User updateUser = new User();
        updateUser.setNickname(request.getNickname());
        updateUser.setEmail(request.getEmail());
        updateUser.setPhone(request.getPhone());
        updateUser.setGender(request.getGender());
        updateUser.setBirthday(request.getBirthday());
        
        User updatedUser = userService.updateUser(userId, updateUser);
        updatedUser.setPassword(null);
        
        return ApiResponse.success("更新成功", updatedUser);
    }

    @ApiOperation("修改密码")
    @PutMapping("/password")
    public ApiResponse<String> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        
        return ApiResponse.success("密码修改成功");
    }

    @ApiOperation("上传头像")
    @PostMapping("/avatar")
    public ApiResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                          HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        
        String avatarUrl = userService.uploadAvatar(userId, file);
        
        return ApiResponse.success("头像上传成功", avatarUrl);
    }

    @ApiOperation("启用/禁用用户")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ApiResponse<String> toggleUserStatus(@PathVariable Long id,
                                              @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        
        String message = status == 1 ? "用户已启用" : "用户已禁用";
        return ApiResponse.success(message);
    }

    @ApiOperation("删除用户")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("用户删除成功");
    }

    @ApiOperation("批量删除用户")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/batch")
    public ApiResponse<String> batchDeleteUsers(@RequestBody BatchDeleteRequest request) {
        userService.batchDeleteUsers(request.getIds());
        return ApiResponse.success("批量删除成功");
    }

    @ApiOperation("重置用户密码")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reset-password")
    public ApiResponse<String> resetPassword(@PathVariable Long id) {
        String newPassword = userService.resetPassword(id);
        return ApiResponse.success("密码重置成功，新密码：" + newPassword);
    }

    @ApiOperation("检查用户名是否可用")
    @GetMapping("/check-username")
    public ApiResponse<Boolean> checkUsername(@RequestParam String username) {
        boolean available = userService.isUsernameAvailable(username);
        return ApiResponse.success(available);
    }

    @ApiOperation("检查邮箱是否可用")
    @GetMapping("/check-email")
    public ApiResponse<Boolean> checkEmail(@RequestParam String email) {
        boolean available = userService.isEmailAvailable(email);
        return ApiResponse.success(available);
    }

    @ApiOperation("获取用户统计信息")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public ApiResponse<UserStatistics> getUserStatistics() {
        UserStatistics statistics = userService.getUserStatistics();
        return ApiResponse.success(statistics);
    }
    
    @ApiOperation("分页查询用户列表")
    @GetMapping("/list")
    public ApiResponse<PageResponse<User>> getUserList(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size,
            @ApiParam("搜索关键字") @RequestParam(required = false) String keyword) {
        
        PageResponse<User> result = userService.findUsers(page, size, keyword);
        
        // 不返回密码
        result.getRecords().forEach(user -> user.setPassword(null));
        
        return ApiResponse.success(result);
    }
    
    @ApiOperation("根据ID获取用户信息")
    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        User user = userService.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setPassword(null);
        return ApiResponse.success(user);
    }
    
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public ApiResponse<String> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request.getUsername(), request.getPassword(), request.getEmail());
        return ApiResponse.success("注册成功");
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            throw new RuntimeException("未登录或token已过期");
        }
        
        Long userId = userService.getLoginUserId(token);
        if (userId == null) {
            throw new RuntimeException("未登录或token已过期");
        }
        
        return userId;
    }
    
    /**
     * 从请求中提取token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 内部类定义请求对象
    public static class UpdateUserRequest {
        @Size(max = 50, message = "昵称长度不能超过50个字符")
        private String nickname;
        
        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱长度不能超过100个字符")
        private String email;
        
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;
        
        @Pattern(regexp = "^(MALE|FEMALE|UNKNOWN)$", message = "性别只能是MALE、FEMALE或UNKNOWN")
        private String gender;
        
        private LocalDateTime birthday;
        
        // Getters and Setters
        public String getNickname() {
            return nickname;
        }
        
        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
        
        public String getGender() {
            return gender;
        }
        
        public void setGender(String gender) {
            this.gender = gender;
        }
        
        public LocalDateTime getBirthday() {
            return birthday;
        }
        
        public void setBirthday(LocalDateTime birthday) {
            this.birthday = birthday;
        }
    }
    
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
        private String username;
        
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
        private String password;
        
        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱长度不能超过100个字符")
        private String email;
        
        // Getters and Setters
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
    
    public static class ChangePasswordRequest {
        @NotBlank(message = "原密码不能为空")
        private String oldPassword;
        
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间")
        private String newPassword;
        
        // Getters and Setters
        public String getOldPassword() {
            return oldPassword;
        }
        
        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }
        
        public String getNewPassword() {
            return newPassword;
        }
        
        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
    
    public static class BatchDeleteRequest {
        @NotBlank(message = "用户ID列表不能为空")
        private java.util.List<Long> ids;
        
        public java.util.List<Long> getIds() {
            return ids;
        }
        
        public void setIds(java.util.List<Long> ids) {
            this.ids = ids;
        }
    }
    
    public static class UserStatistics {
        private Long totalUsers;
        private Long activeUsers;
        private Long inactiveUsers;
        private Long todayRegistrations;
        private Long weekRegistrations;
        private Long monthRegistrations;
        
        public UserStatistics() {}
        
        public UserStatistics(Long totalUsers, Long activeUsers, Long inactiveUsers, 
                            Long todayRegistrations, Long weekRegistrations, Long monthRegistrations) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.inactiveUsers = inactiveUsers;
            this.todayRegistrations = todayRegistrations;
            this.weekRegistrations = weekRegistrations;
            this.monthRegistrations = monthRegistrations;
        }
        
        // Getters and Setters
        public Long getTotalUsers() {
            return totalUsers;
        }
        
        public void setTotalUsers(Long totalUsers) {
            this.totalUsers = totalUsers;
        }
        
        public Long getActiveUsers() {
            return activeUsers;
        }
        
        public void setActiveUsers(Long activeUsers) {
            this.activeUsers = activeUsers;
        }
        
        public Long getInactiveUsers() {
            return inactiveUsers;
        }
        
        public void setInactiveUsers(Long inactiveUsers) {
            this.inactiveUsers = inactiveUsers;
        }
        
        public Long getTodayRegistrations() {
            return todayRegistrations;
        }
        
        public void setTodayRegistrations(Long todayRegistrations) {
            this.todayRegistrations = todayRegistrations;
        }
        
        public Long getWeekRegistrations() {
            return weekRegistrations;
        }
        
        public void setWeekRegistrations(Long weekRegistrations) {
            this.weekRegistrations = weekRegistrations;
        }
        
        public Long getMonthRegistrations() {
            return monthRegistrations;
        }
        
        public void setMonthRegistrations(Long monthRegistrations) {
            this.monthRegistrations = monthRegistrations;
        }
    }
}