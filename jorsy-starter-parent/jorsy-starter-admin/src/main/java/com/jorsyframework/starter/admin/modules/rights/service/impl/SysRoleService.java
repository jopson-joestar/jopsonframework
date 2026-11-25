package com.jorsyframework.starter.admin.modules.rights.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.jorsyframework.starter.admin.common.mybatis.AuditConditions;
import com.jorsyframework.starter.admin.modules.rights.domain.entity.SysRole;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysRoleQuery;
import com.jorsyframework.starter.admin.modules.rights.mapper.SysRoleMapper;
import com.jorsyframework.starter.admin.modules.rights.service.ISysRoleService;
import com.jorsyframework.starter.core.mybatis.MybatisPages;
import com.jorsyframework.starter.core.page.PageQuery;
import com.jorsyframework.starter.core.page.PageResult;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

import static com.jorsyframework.starter.admin.common.exception.AdminBusinessException.checkWrite;
import static com.jorsyframework.starter.admin.common.exception.AdminBusinessException.requireFound;

@Service
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
    /** 序列化角色层级、资源树与授权集合的变更，避免并发撤权和下级授予交错。 */
    @Override
    public void lockHierarchy() {
        getOne(Wrappers.lambdaQuery(SysRole.class).eq(SysRole::getSysRoleCode, "administrator").last("FOR UPDATE"));
    }

    @Override
    public Set<String> descendants(String id) {
        Map<String, List<String>> children = new HashMap<>();
        for (var role : ordered()) {
            children.computeIfAbsent(role.getParentId(), ignored -> new ArrayList<>()).add(role.getSysRoleId());
        }
        var result = new LinkedHashSet<String>();
        var queue = new ArrayDeque<>(children.getOrDefault(id, List.of()));
        while (!queue.isEmpty()) {
            String child = queue.removeFirst();
            if (result.add(child)) queue.addAll(children.getOrDefault(child, List.of()));
        }
        return result;
    }

    @Override
    public PageResult<SysRole> page(SysRoleQuery param) {
        var filter = Wrappers.lambdaQuery(SysRole.class);
        filter.like(param.sysRoleCode() != null && !param.sysRoleCode().isBlank(), SysRole::getSysRoleCode, param.sysRoleCode());
        filter.like(param.sysRoleName() != null && !param.sysRoleName().isBlank(), SysRole::getSysRoleName, param.sysRoleName());
        AuditConditions.createdBetween(filter, param.createTimeBegin(), param.createTimeEnd());
        filter.orderByDesc(SysRole::getCreateTime).orderByDesc(SysRole::getSysRoleId);
        var pagination = MybatisPages.<SysRole>toPage(new PageQuery(param.currentPage(), param.pageSize()));
        return MybatisPages.toResult(page(pagination, filter));
    }

    @Override
    public SysRole lockById(String id) {
        return requireFound(lambdaQuery().eq(SysRole::getSysRoleId, id).last("FOR UPDATE").one());
    }

    @Override
    public void delete(SysRole row, String actor) {
        checkWrite(update(AuditConditions.logicalDelete(SysRole.class, row, SysRole::getSysRoleId, actor)), "删除失败，请刷新");
    }

    @Override
    public List<SysRole> ordered() {
        return lambdaQuery().orderByAsc(SysRole::getSysRoleId).list();
    }
}
