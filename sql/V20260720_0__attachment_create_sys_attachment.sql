-- 功能描述：创建系统附件表，用于托管模式下集中管理附件
-- 创建时间：2026-07-20

CREATE TABLE IF NOT EXISTS `sys_attachment` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `file_name` varchar(200) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件存储路径/URL',
  `file_size` bigint DEFAULT 0 COMMENT '文件大小（字节）',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型（pdf/doc/xlsx/image等）',
  `mime_type` varchar(100) DEFAULT NULL COMMENT 'MIME类型',
  `storage_type` varchar(20) NOT NULL COMMENT '存储方式（local/minio/alioss）',
  `biz_code` varchar(50) DEFAULT NULL COMMENT '业务标识，格式：{table_name}.{field_name}',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `delete_by` varchar(32) DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `owner_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门编码',
  PRIMARY KEY (`id`),
  INDEX `idx_biz_code` (`biz_code`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统附件表';
