-- =====================================================
-- 文件模块数据库表结构 (file-module)
-- 数据库: multi_module_dev
-- 创建时间: 2025-09-15
-- =====================================================

-- 使用数据库
USE multi_module_dev;

-- =====================================================
-- 1. 文件元数据表 (file_metadata)
-- =====================================================
DROP TABLE IF EXISTS `file_metadata`;

CREATE TABLE `file_metadata` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `stored_name` varchar(255) NOT NULL COMMENT '存储文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件存储路径',
  `file_size` bigint NOT NULL COMMENT '文件大小(字节)',
  `content_type` varchar(100) DEFAULT NULL COMMENT '文件MIME类型',
  `file_extension` varchar(20) DEFAULT NULL COMMENT '文件扩展名',
  `md5_hash` varchar(32) DEFAULT NULL COMMENT '文件MD5哈希值',
  `thumbnail_path` varchar(500) DEFAULT NULL COMMENT '缩略图路径',
  `is_image` tinyint(1) DEFAULT 0 COMMENT '是否为图片(0:否, 1:是)',
  `width` int DEFAULT NULL COMMENT '图片宽度(像素)',
  `height` int DEFAULT NULL COMMENT '图片高度(像素)',
  `upload_user_id` bigint DEFAULT NULL COMMENT '上传用户ID',
  `business_type` varchar(50) DEFAULT NULL COMMENT '业务类型(avatar:头像, document:文档, image:图片等)',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态(0:删除, 1:正常)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_upload_user_id` (`upload_user_id`) COMMENT '上传用户ID索引',
  KEY `idx_business_type` (`business_type`) COMMENT '业务类型索引',
  KEY `idx_file_extension` (`file_extension`) COMMENT '文件扩展名索引',
  KEY `idx_md5_hash` (`md5_hash`) COMMENT 'MD5哈希索引',
  KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引',
  KEY `idx_status` (`status`) COMMENT '状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件元数据表';

-- =====================================================
-- 2. 插入测试数据（可选）
-- =====================================================

-- 插入一些示例数据
INSERT INTO `file_metadata` (
  `original_name`, `stored_name`, `file_path`, `file_size`, 
  `content_type`, `file_extension`, `md5_hash`, 
  `upload_user_id`, `business_type`, `status`, `create_time`
) VALUES 
(
  'example.jpg', 
  '20250915_example_123456.jpg', 
  '/tmp/uploads/2025/09/15/20250915_example_123456.jpg', 
  1024000, 
  'image/jpeg', 
  'jpg', 
  'abcdef123456789abcdef123456789ab', 
  1, 
  'image', 
  1, 
  NOW()
),
(
  'document.pdf', 
  '20250915_document_654321.pdf', 
  '/tmp/uploads/2025/09/15/20250915_document_654321.pdf', 
  2048000, 
  'application/pdf', 
  'pdf', 
  'fedcba987654321fedcba987654321fe', 
  1, 
  'document', 
  1, 
  NOW()
);

-- =====================================================
-- 3. 查看表结构
-- =====================================================
DESCRIBE `file_metadata`;

-- =====================================================
-- 4. 查看测试数据
-- =====================================================
SELECT * FROM `file_metadata` ORDER BY `create_time` DESC LIMIT 10;

-- =====================================================
-- 执行完成提示
-- =====================================================
SELECT '文件模块数据库表创建完成！' as message;
