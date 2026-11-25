package com.jopsonframework.starter.admin.modules.rights.controller;

import com.jopsonframework.starter.admin.modules.rights.domain.dto.SysRightsRow;
import com.jopsonframework.starter.admin.modules.rights.domain.param.RightsEnabled;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysRightsCreateParam;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysRightsQuery;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysRightsUpdateParam;
import com.jopsonframework.starter.admin.modules.rights.facade.ISysRightsFacade;
import com.jopsonframework.starter.core.page.PageResult;
import com.jopsonframework.starter.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/sys-rights")
@RequiredArgsConstructor
@Tag(name = "权限身份管理")
public class SysRightsController {
    private final ISysRightsFacade facade;

    @PostMapping("/page")
    @Operation(summary = "分页查询权限身份")
    public R<PageResult<SysRightsRow>> page(@Valid @RequestBody SysRightsQuery param) {
        return R.success(facade.page(param));
    }

    @PostMapping("/get-by-id")
    @Operation(summary = "查询权限身份详情")
    public R<SysRightsRow> detail(@RequestParam @NotBlank @Size(min = 32, max = 32) String sysRightsId) {
        return R.success(facade.detail(sysRightsId));
    }

    @PostMapping("/create")
    @Operation(summary = "创建权限身份")
    public R<String> create(@Valid @RequestBody SysRightsCreateParam param) {
        return R.success(facade.create(param));
    }

    @PostMapping("/update-by-id")
    @Operation(summary = "编辑权限身份")
    public R<String> edit(@Valid @RequestBody SysRightsUpdateParam param) {
        return R.success(facade.edit(param));
    }

    @PostMapping("/delete-by-id")
    @Operation(summary = "删除权限身份")
    public R<Void> delete(@RequestParam @NotBlank @Size(min = 32, max = 32) String sysRightsId, @RequestParam @PositiveOrZero long version) {
        facade.delete(sysRightsId, version);
        return R.success(null);
    }

    @PostMapping("/update-enabled")
    @Operation(summary = "启停权限身份")
    public R<Void> enabled(@Valid @RequestBody RightsEnabled param) {
        facade.enabled(param);
        return R.success(null);
    }
}
