package com.cssz.modules.file.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cssz.base.CsEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 系统附件
 */
@Data
@TableName("sys_attachment")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "系统附件")
public class SysAttachment extends CsEntity {
    private static final long serialVersionUID = 1L;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "文件存储路径/URL")
    private String filePath;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件类型（pdf/doc/xlsx/image等）")
    private String fileType;

    @Schema(description = "MIME类型")
    private String mimeType;

    @Schema(description = "存储方式（local/minio/alioss）")
    private String storageType;

    @Schema(description = "业务标识，格式：{table_name}.{field_name}")
    private String bizCode;
}
