package com.jorsyframework.starter.admin.modules.basedata.controller;

import com.jorsyframework.starter.admin.modules.basedata.domain.dto.SysConfigRow;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysConfigCreateParam;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysConfigQuery;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysConfigUpdateParam;
import com.jorsyframework.starter.admin.modules.basedata.facade.ISysConfigFacade;
import com.jorsyframework.starter.core.page.PageResult;
import com.jorsyframework.starter.core.result.R;
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
@RequestMapping("/admin/sys-config")
@RequiredArgsConstructor
@Tag(name = "配置管理")
public class SysConfigController {
    private final ISysConfigFacade facade;

    @PostMapping("/page")
    @Operation(summary = "分页查询配置")
    public R<PageResult<SysConfigRow>> page(@Valid @RequestBody SysConfigQuery param) {
        return R.success(facade.page(param));
    }

    @PostMapping("/get-by-id")
    @Operation(summary = "查询配置详情")
    public R<SysConfigRow> detail(@RequestParam @NotBlank @Size(min = 32, max = 32) String sysConfigId) {
        return R.success(facade.detail(sysConfigId));
    }

    @PostMapping("/create")
    @Operation(summary = "创建配置")
    public R<String> create(@Valid @RequestBody SysConfigCreateParam param) {
        return R.success(facade.create(param));
    }

    @PostMapping("/update-by-id")
    @Operation(summary = "编辑配置")
    public R<String> edit(@Valid @RequestBody SysConfigUpdateParam param) {
        return R.success(facade.edit(param));
    }

    @PostMapping("/delete-by-id")
    @Operation(summary = "删除配置")
    public R<Void> delete(@RequestParam @NotBlank @Size(min = 32, max = 32) String sysConfigId, @RequestParam @PositiveOrZero long version) {
        facade.delete(sysConfigId, version);
        return R.success(null);
    }

    @PostMapping("/get-value-by-code")
    @Operation(summary = "查配置值")
    public R<String> value(@RequestParam @NotBlank String sysConfigCode) {
        return R.success(facade.value(sysConfigCode));
    }
}
