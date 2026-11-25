package com.jopsonframework.starter.admin.modules.rights.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysUser;
import com.jopsonframework.starter.admin.modules.rights.domain.enums.SysUserStatus;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysUserPageQuery;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysUserUpdateParam;
import com.jopsonframework.starter.admin.modules.rights.mapper.SysUserMapper;
import com.jopsonframework.starter.admin.modules.rights.service.ISysUserService;
import com.jopsonframework.starter.core.mybatis.MybatisPages;
import com.jopsonframework.starter.core.page.PageResult;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Override
    public PageResult<SysUser> page(SysUserPageQuery query) {
        var filter = Wrappers.<SysUser>lambdaQuery()
                .like(StringUtils.hasText(query.sysUserCode()), SysUser::getSysUserCode, query.sysUserCode())
                .like(StringUtils.hasText(query.sysUserName()), SysUser::getSysUserName, query.sysUserName())
                .eq(query.sysUserStatus() != null, SysUser::getSysUserStatus, query.sysUserStatus())
                .ge(query.createTimeBegin() != null, SysUser::getCreateTime, query.createTimeBegin());
        if (query.createTimeEnd() != null) {
            filter.le(SysUser::getCreateTime, query.createTimeEnd().withNano(999_000_000));
        }
        filter.orderByDesc(SysUser::getCreateTime, SysUser::getSysUserId);
        return MybatisPages.toResult(baseMapper.selectPage(MybatisPages.toPage(query.pagination()), filter));
    }

    @Override
    public Optional<SysUser> loginCandidate(String code) {
        return lambdaQuery().select(SysUser::getSysUserId, SysUser::getSysUserPwd, SysUser::getSysUserStatus)
                .eq(SysUser::getSysUserCode, code).oneOpt();
    }

    @Override
    public Optional<SysUser> find(String id) {
        return Optional.ofNullable(baseMapper.selectById(id));
    }

    @Override
    public Map<String, String> names(Set<String> ids) {
        if (ids.isEmpty()) return Map.of();
        var result = new HashMap<String, String>();
        for (var user : baseMapper.selectList(Wrappers.<SysUser>lambdaQuery()
                .select(SysUser::getSysUserId, SysUser::getSysUserName).in(SysUser::getSysUserId, ids))) {
            result.put(user.getSysUserId(), user.getSysUserName());
        }
        return result;
    }

    @Override
    public void insert(SysUser user) {
        if (!save(user)) throw new IllegalStateException("用户新增行数异常");
    }

    @Override
    public int edit(SysUserUpdateParam form, String operatorId) {
        return baseMapper.update(write(form.sysUserId(), form.version(), SysUserStatus.CREATE, operatorId)
                .set(SysUser::getSysUserName, form.sysUserName()).set(SysUser::getSysUserMobile, form.sysUserMobile())
                .set(SysUser::getSysUserEmail, form.sysUserEmail()).set(SysUser::getSysUserQq, form.sysUserQq()));
    }

    @Override
    public int changeStatus(String id, long version, SysUserStatus from, SysUserStatus to, String operatorId) {
        return baseMapper.update(write(id, version, from, operatorId).set(SysUser::getSysUserStatus, to));
    }

    @Override
    public int delete(String id, long version, SysUserStatus from, String operatorId) {
        return baseMapper.update(write(id, version, from, operatorId).set(SysUser::getDeleted, true));
    }

    private LambdaUpdateWrapper<SysUser> write(String id, long version, SysUserStatus from, String operatorId) {
        // MyBatis-Plus adds deleted = 0 to this entity's updates.
        return Wrappers.<SysUser>lambdaUpdate().eq(SysUser::getSysUserId, id)
                .eq(SysUser::getVersion, version).eq(SysUser::getSysUserStatus, from)
                .set(SysUser::getUpdateId, operatorId).set(SysUser::getUpdateTime, LocalDateTime.now())
                .setIncrBy(SysUser::getVersion, 1);
    }
}
