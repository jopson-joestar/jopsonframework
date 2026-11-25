package com.jorsyframework.starter.admin.modules.rights.access;

import com.jorsyframework.starter.admin.common.exception.AdminBusinessException;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpStatus;

@FunctionalInterface
public interface SysUserOperatorProvider {
    /** 由消费站点从可信身份上下文取得当前操作者，匿名访问返回 empty。 */
    Optional<Operator> currentOperator();

    default String require(String permission) {
        var operator = currentOperator().orElseThrow(() -> new AdminBusinessException(HttpStatus.UNAUTHORIZED, "请先登录"));
        if (!operator.allows(permission)) throw AdminBusinessException.forbidden("缺少操作权限");
        return operator.userId();
    }

    record Operator(String userId, Set<String> permissions, boolean administrator) {
        public Operator(String userId, Set<String> permissions) {
            this(userId, permissions, false);
        }

        public boolean allows(String permission) {
            return administrator || permissions.contains(permission);
        }

        public Operator {
            if (userId == null || userId.isBlank() || userId.length() > 32) {
                throw new IllegalArgumentException("操作者 ID 必须为 1 至 32 个字符");
            }
            permissions = Set.copyOf(permissions);
        }
    }
}
