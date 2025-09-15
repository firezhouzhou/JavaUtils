# 📁 文件模块数据库设置指南

## 🚨 问题说明

你遇到的错误：
```
Table 'multi_module_dev.file_metadata' doesn't exist
```

这是因为文件模块需要一个 `file_metadata` 表来存储文件的元数据信息，但该表尚未创建。

## 🛠️ 解决方案

我为你准备了3种方式来创建数据库表：

### 方式1：一键执行脚本 (推荐)

```bash
cd /Users/zhangsan/githubproject/JavaUtils
./setup-file-database.sh
```

这个脚本会：
- 自动检查数据库连接
- 创建数据库（如果不存在）
- 创建 `file_metadata` 表
- 显示表结构确认

### 方式2：手动执行SQL文件

```bash
# 进入项目根目录
cd /Users/zhangsan/githubproject/JavaUtils

# 执行简化版SQL（推荐）
mysql -h localhost -u root -p multi_module_dev < create-file-metadata-table.sql

# 或执行完整版SQL（包含测试数据）
mysql -h localhost -u root -p multi_module_dev < file-module-database.sql
```

### 方式3：直接执行SQL语句

连接到MySQL后执行：

```sql
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
```

## 📋 表结构说明

`file_metadata` 表包含以下字段：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `id` | bigint | 主键ID，自增 |
| `original_name` | varchar(255) | 原始文件名 |
| `stored_name` | varchar(255) | 存储文件名（重命名后） |
| `file_path` | varchar(500) | 文件存储路径 |
| `file_size` | bigint | 文件大小（字节） |
| `content_type` | varchar(100) | MIME类型 |
| `file_extension` | varchar(20) | 文件扩展名 |
| `md5_hash` | varchar(32) | MD5哈希值 |
| `thumbnail_path` | varchar(500) | 缩略图路径 |
| `is_image` | tinyint(1) | 是否为图片 |
| `width` | int | 图片宽度 |
| `height` | int | 图片高度 |
| `upload_user_id` | bigint | 上传用户ID |
| `business_type` | varchar(50) | 业务类型 |
| `status` | int | 状态（0:删除, 1:正常） |
| `create_time` | datetime | 创建时间 |
| `update_time` | datetime | 更新时间 |

## 🔍 验证表是否创建成功

```sql
USE multi_module_dev;
DESCRIBE file_metadata;
SELECT COUNT(*) FROM file_metadata;
```

## 🚀 创建表后测试文件上传

表创建成功后，你可以重新测试文件上传：

```bash
curl -X POST "http://localhost:8083/file/upload" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@test-upload.txt" \
  -F "businessType=test"
```

应该会返回成功的响应而不是数据库错误。

## 📁 相关文件

- `create-file-metadata-table.sql` - 简化建表SQL
- `file-module-database.sql` - 完整建表SQL（包含测试数据）
- `setup-file-database.sh` - 一键执行脚本
- `FILE-DATABASE-SETUP.md` - 本说明文档
