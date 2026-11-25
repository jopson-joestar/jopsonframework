package com.jopsonframework.starter.admin.modules.rights.facade.impl;

import com.jopsonframework.starter.admin.common.exception.AdminBusinessException;
import com.jopsonframework.starter.admin.modules.rights.access.SysUserOperatorProvider.Operator;
import com.jopsonframework.starter.admin.modules.rights.access.SysUserOperatorProvider;
import com.jopsonframework.starter.admin.modules.rights.converter.SysUserConverter;
import com.jopsonframework.starter.admin.modules.rights.domain.dto.SysUserRow;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysUser;
import com.jopsonframework.starter.admin.modules.rights.domain.enums.SysUserStatus;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysUserCreateParam;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysUserDeleteParam;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysUserPageQuery;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysUserStatusParam;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysUserUpdateParam;
import com.jopsonframework.starter.admin.modules.rights.facade.ISysUserFacade;
import com.jopsonframework.starter.admin.modules.rights.service.ISysUserService;
import com.jopsonframework.starter.core.convert.ConvertContext;
import com.jopsonframework.starter.core.page.PageResult;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Validated
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SysUserFacade implements ISysUserFacade {
    private final ISysUserService users;
    private final SysUserConverter converter;
    private final SysUserOperatorProvider operators;
    private final PasswordEncoder passwords;

    @Override
    public PageResult<SysUserRow> page(SysUserPageQuery query) {
        authorized("View");
        var page = users.page(query);
        var names = auditNames(page.items());
        var rows = page.items().stream().map(user -> converter.toRow(user, names)).toList();
        return new PageResult<>(rows, page.total(), page.currentPage(), page.pageSize());
    }

    @Override
    public SysUserRow detail(String id) {
        authorized("View");
        var user = existing(id);
        return converter.toRow(user, auditNames(List.of(user)));
    }

    @Override
    @Transactional
    public String create(SysUserCreateParam param) {
        var operator = authorized("Create");
        var user = new SysUser();
        user.setSysUserCode(param.sysUserCode());
        user.setSysUserName(param.sysUserName());
        user.setSysUserMobile(param.sysUserMobile());
        user.setSysUserEmail(param.sysUserEmail());
        user.setSysUserQq(param.sysUserQq());
        user.setSysUserStatus(SysUserStatus.CREATE);
        user.setSysData(false);
        user.setCreateId(operator.userId());
        user.setSysUserPwd(passwords.encode(param.initialPassword()));
        try {
            users.insert(user);
        } catch (DuplicateKeyException duplicate) {
            throw new AdminBusinessException(CONFLICT, "登录账号已存在");
        }
        return user.getSysUserId();
    }

    @Override
    @Transactional
    public String edit(SysUserUpdateParam param) {
        var operator = authorized("Update");
        var user = existing(param.sysUserId());
        if (user.getSysUserStatus() != SysUserStatus.CREATE) {
            throw new AdminBusinessException(CONFLICT, "仅未审核用户可编辑");
        }
        requireUpdated(users.edit(param, operator.userId()));
        return user.getSysUserId();
    }

    @Override
    @Transactional
    public void changeStatus(SysUserStatusParam param) {
        var target = param.sysStatus();
        Operator operator = switch (target) {
            case CHECK -> authorized("Audit", "Enable");
            case CREATE -> authorized("CancelAudit");
            case LOCK -> authorized("Disable");
            case VOID -> {
                authorized("Audit", "CancelAudit", "Disable", "Enable");
                throw new AdminBusinessException(CONFLICT, "不支持将用户设为作废");
            }
        };
        var user = existing(param.sysUserId());
        var from = user.getSysUserStatus();
        if (target == SysUserStatus.CHECK && from == SysUserStatus.CREATE) {
            requirePermission(operator, "Audit");
        } else if (target == SysUserStatus.CHECK && from == SysUserStatus.LOCK) {
            requirePermission(operator, "Enable");
        } else if (from == SysUserStatus.CHECK && (target == SysUserStatus.CREATE || target == SysUserStatus.LOCK)) {
            protectAccount(user, operator);
        } else {
            throw new AdminBusinessException(CONFLICT, "当前状态不能执行此转换");
        }
        requireUpdated(users.changeStatus(user.getSysUserId(), param.version(), from, target, operator.userId()));
    }

    @Override
    @Transactional
    public void delete(SysUserDeleteParam param) {
        var operator = authorized("Delete");
        var user = existing(param.sysUserId());
        protectAccount(user, operator);
        var from = user.getSysUserStatus();
        if (from != SysUserStatus.CREATE && from != SysUserStatus.VOID) {
            throw new AdminBusinessException(CONFLICT, "仅未审核或作废用户可删除");
        }
        requireUpdated(users.delete(user.getSysUserId(), param.version(), from, operator.userId()));
    }

    private ConvertContext<String, String> auditNames(List<SysUser> rows) {
        var ids = rows.stream().flatMap(u -> Stream.of(u.getCreateId(), u.getUpdateId())).toList();
        return ConvertContext.load(ids, Function.identity(), users::names);
    }

    private SysUser existing(String id) {
        return users.find(id).orElseThrow(() -> new AdminBusinessException(NOT_FOUND, "用户不存在"));
    }

    private Operator authorized(String... actions) {
        var operator = operators.currentOperator()
                .orElseThrow(() -> new AdminBusinessException(UNAUTHORIZED, "请先登录"));
        requirePermission(operator, actions);
        return operator;
    }

    private void requirePermission(Operator operator, String... actions) {
        for (String action : actions) {
            if (operator.allows("Admin:SysUser:" + action)) return;
        }
        throw new AdminBusinessException(FORBIDDEN, "缺少用户管理操作权限");
    }

    private void protectAccount(SysUser user, Operator operator) {
        if (operator.userId().equalsIgnoreCase(user.getSysUserId()) || Boolean.TRUE.equals(user.getSysData())) {
            throw new AdminBusinessException(FORBIDDEN, "本人或内置账号受保护");
        }
    }

    private void requireUpdated(int rows) {
        if (rows == 0) throw new AdminBusinessException(CONFLICT, "用户记录已变化，请刷新后重试");
        if (rows != 1) throw new IllegalStateException("用户更新行数异常");
    }
}
