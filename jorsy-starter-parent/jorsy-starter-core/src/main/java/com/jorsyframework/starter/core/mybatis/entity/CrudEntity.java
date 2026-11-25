package com.jorsyframework.starter.core.mybatis.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CrudEntity extends AuditEntity {
    @Version
    @TableField("version")
    private Long version = 0L;

    @TableLogic(value = "0", delval = "1")
    @TableField("deleted")
    private Boolean deleted = false;
}
