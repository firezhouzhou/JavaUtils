-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt编码）',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账户是否未过期',
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账户是否未锁定',
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE COMMENT '凭证是否未过期',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '用户角色',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_at DATETIME COMMENT '最后登录时间',
    login_count INT NOT NULL DEFAULT 0 COMMENT '登录次数',
    
    -- 索引
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_enabled (enabled),
    INDEX idx_created_at (created_at),
    INDEX idx_last_login_at (last_login_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入默认管理员用户（如果不存在）
INSERT IGNORE INTO users (
    username, 
    password, 
    email, 
    role, 
    enabled, 
    account_non_expired, 
    account_non_locked, 
    credentials_non_expired,
    created_at,
    updated_at
) VALUES (
    'admin',
    '$2a$10$ztESRnI3.iwi4XYDJlN0GOJLWh5q0k8ERYQIxp0Fe.dDbBe0toTT.', -- admin123
    'admin@example.com',
    'ADMIN',
    TRUE,
    TRUE,
    TRUE,
    TRUE,
    NOW(),
    NOW()
);

-- 查看表结构
DESCRIBE users;

-- 查看初始数据
SELECT id, username, email, role, enabled, created_at, login_count FROM users;
