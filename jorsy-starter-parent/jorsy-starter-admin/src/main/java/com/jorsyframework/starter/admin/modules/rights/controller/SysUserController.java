package com.jorsyframework.starter.admin.modules.rights.controller;

import com.jorsyframework.starter.admin.modules.rights.domain.dto.SysUserRow;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysUserCreateParam;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysUserDeleteParam;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysUserPageQuery;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysUserStatusParam;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysUserUpdateParam;
import com.jorsyframework.starter.admin.modules.rights.facade.ISysUserFacade;
import com.jorsyframework.starter.core.page.PageResult;
import com.jorsyframework.starter.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/sys-user")
@RequiredArgsConstructor
@Tag(name = "系统用户管理")
public class SysUserController {
    private final ISysUserFacade users;

    @PostMapping("/page")
    @Operation(summary = "分页查询用户")
    public R<PageResult<SysUserRow>> page(@Valid @RequestBody SysUserPageQuery query) {
        return R.success(users.page(query));
    }

    @PostMapping("/get-by-id")
    @Operation(summary = "查询用户详情")
    public R<SysUserRow> detail(@RequestParam @NotBlank @Size(min = 32, max = 32) String sysUserId) {
        return R.success(users.detail(sysUserId));
    }

    @PostMapping("/create")
    @Operation(summary = "创建未审核用户")
    public R<String> create(@Valid @RequestBody SysUserCreateParam param) {
        return R.success(users.create(param));
    }

    @PostMapping("/update-by-id")
    @Operation(summary = "编辑未审核用户资料")
    public R<String> edit(@Valid @RequestBody SysUserUpdateParam param) {
        return R.success(users.edit(param));
    }

    @PostMapping("/update-status")
    @Operation(summary = "审核、撤审、停用或启用用户")
    public R<Void> changeStatus(@Valid @ModelAttribute @ParameterObject SysUserStatusParam param) {
        users.changeStatus(param);
        return R.success(null);
    }

    @PostMapping("/delete-by-id")
    @Operation(summary = "逻辑删除用户")
    public R<Void> delete(@Valid @ModelAttribute @ParameterObject SysUserDeleteParam param) {
        users.delete(param);
        return R.success(null);
    }
}
