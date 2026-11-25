package com.jorsyframework.starter.admin.modules.rights.facade;

import com.jorsyframework.starter.admin.modules.rights.domain.dto.SysUserRow;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysUserCreateParam;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysUserDeleteParam;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysUserPageQuery;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysUserStatusParam;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysUserUpdateParam;
import com.jorsyframework.starter.core.page.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public interface ISysUserFacade {
    PageResult<SysUserRow> page(@Valid @NotNull SysUserPageQuery query);
    SysUserRow detail(@NotBlank @Size(min = 32, max = 32) String id);
    String create(@Valid @NotNull SysUserCreateParam param);
    String edit(@Valid @NotNull SysUserUpdateParam param);
    void changeStatus(@Valid @NotNull SysUserStatusParam param);
    void delete(@Valid @NotNull SysUserDeleteParam param);
}
