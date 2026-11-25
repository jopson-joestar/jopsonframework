package com.jorsyframework.starter.core.mybatis.autoconfigure;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.jorsyframework.starter.core.mybatis.entity.AuditEntity;
import org.apache.ibatis.reflection.MetaObject;

final class AuditTimeHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        if (metaObject.getOriginalObject() instanceof AuditEntity entity) {
            LocalDateTime now = LocalDateTime.now();
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject.getOriginalObject() instanceof AuditEntity entity) {
            entity.setUpdateTime(LocalDateTime.now());
        }
    }
}
