package com.cssz.base;

import java.util.Date;

import org.jeecg.common.system.base.entity.JeecgEntity;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 实体基类，扩展自 JeecgEntity，增加删除人和所有者部门编码等公共字段
 */
public class CsEntity extends JeecgEntity {
    @Schema(description = "删除人")
    private String deleteBy;

    @Schema(description = "删除时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deleteTime;

    @Schema(description = "所有者部门编码")
    private String ownerOrgCode;
}
