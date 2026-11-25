package com.jopsonframework.starter.core.mybatis.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AuditEntity {
    @TableField(value = "create_id", updateStrategy = FieldStrategy.NEVER)
    private String createId;

    @TableField(value = "create_time", fill = FieldFill.INSERT, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField("update_id")
    private String updateId;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
