-- 创建文件元数据表
-- 数据库: multi_module_dev

USE multi_module_dev;

CREATE TABLE IF NOT EXISTS `file_metadata` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `stored_name` varchar(255) NOT NULL COMMENT '存储文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件存储路径',
  `file_size` bigint NOT NULL COMMENT '文件大小(字节)',
  `content_type` varchar(100) DEFAULT NULL COMMENT '文件MIME类型',
  `file_extension` varchar(20) DEFAULT NULL COMMENT '文件扩展名',
  `md5_hash` varchar(32) DEFAULT NULL COMMENT '文件MD5哈希值',
  `thumbnail_path` varchar(500) DEFAULT NULL COMMENT '缩略图路径',
  `is_image` tinyint(1) DEFAULT 0 COMMENT '是否为图片',
  `width` int DEFAULT NULL COMMENT '图片宽度',
  `height` int DEFAULT NULL COMMENT '图片高度',
  `upload_user_id` bigint DEFAULT NULL COMMENT '上传用户ID',
  `business_type` varchar(50) DEFAULT NULL COMMENT '业务类型',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态(0:删除, 1:正常)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_upload_user_id` (`upload_user_id`),
  KEY `idx_business_type` (`business_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件元数据表';
