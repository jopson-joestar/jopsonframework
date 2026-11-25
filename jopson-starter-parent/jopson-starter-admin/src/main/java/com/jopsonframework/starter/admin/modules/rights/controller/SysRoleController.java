package com.jopsonframework.starter.admin.modules.rights.controller;

import com.jopsonframework.starter.admin.modules.rights.domain.dto.SysRoleRow;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysRoleCreateParam;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysRoleQuery;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysRoleUpdateParam;
import com.jopsonframework.starter.admin.modules.rights.facade.ISysRoleFacade;
import com.jopsonframework.starter.core.page.PageResult;
import com.jopsonframework.starter.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/sys-role")
@RequiredArgsConstructor
@Tag(name = "角色管理")
public class SysRoleController {
    private final ISysRoleFacade facade;

    @PostMapping("/page")
    @Operation(summary = "分页查询角色")
    public R<PageResult<SysRoleRow>> page(@Valid @RequestBody SysRoleQuery param) {
        return R.success(facade.page(param));
    }

    @PostMapping("/get-by-id")
    @Operation(summary = "查询角色详情")
    public R<SysRoleRow> detail(@RequestParam @NotBlank @Size(min = 32, max = 32) String sysRoleId) {
        return R.success(facade.detail(sysRoleId));
    }

    @PostMapping("/create")
    @Operation(summary = "创建角色")
    public R<String> create(@Valid @RequestBody SysRoleCreateParam param) {
        return R.success(facade.create(param));
    }

    @PostMapping("/update-by-id")
    @Operation(summary = "编辑角色")
    public R<String> edit(@Valid @RequestBody SysRoleUpdateParam param) {
        return R.success(facade.edit(param));
    }

    @PostMapping("/delete-by-id")
    @Operation(summary = "删除角色")
    public R<Void> delete(@RequestParam @NotBlank @Size(min = 32, max = 32) String sysRoleId, @RequestParam @PositiveOrZero long version) {
        facade.delete(sysRoleId, version);
        return R.success(null);
    }

    @PostMapping("/tree")
    @Operation(summary = "查询角色树")
    public R<List<SysRoleRow>> tree() {
        return R.success(facade.tree());
    }
}
