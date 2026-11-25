package com.jorsyframework.starter.admin.common.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.jorsyframework.starter.admin.common.exception.AdminBusinessException;
import com.jorsyframework.starter.core.mybatis.entity.CrudEntity;
import java.time.LocalDateTime;

public final class AuditConditions {
    private AuditConditions() {}

    public static <T extends CrudEntity> void createdBetween(LambdaQueryWrapper<T> filter, LocalDateTime begin, LocalDateTime end) {
        if (begin != null && end != null && begin.isAfter(end)) throw AdminBusinessException.invalid("起始时间不能晚于结束时间");
        if (begin != null) filter.ge(CrudEntity::getCreateTime, begin);
        if (end != null) filter.le(CrudEntity::getCreateTime, end.withNano(999_000_000));
    }

    public static <T extends CrudEntity> LambdaUpdateWrapper<T> logicalDelete(Class<T> entityType, T row, SFunction<T, String> id, String actor) {
        // 指定实际表的实体类型，让继承的审计字段也使用这张表的映射。
        return Wrappers.lambdaUpdate(entityType).eq(id, id.apply(row)).eq(CrudEntity::getVersion, row.getVersion())
                .set(CrudEntity::getDeleted, true).set(CrudEntity::getUpdateId, actor)
                .set(CrudEntity::getUpdateTime, LocalDateTime.now()).setIncrBy(CrudEntity::getVersion, 1);
    }
}
