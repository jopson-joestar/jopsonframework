package com.jorsyframework.starter.admin.modules.rights.controller;

import com.jorsyframework.starter.admin.modules.rights.domain.dto.ResourceImportReport;
import com.jorsyframework.starter.admin.modules.rights.domain.dto.SysResRow;
import com.jorsyframework.starter.admin.modules.rights.domain.param.ResourceImport;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysResCreateParam;
import com.jorsyframework.starter.admin.modules.rights.domain.param.SysResUpdateParam;
import com.jorsyframework.starter.admin.modules.rights.facade.ISysResFacade;
import com.jorsyframework.starter.core.result.R;
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
@RequestMapping("/admin/sys-res")
@RequiredArgsConstructor
@Tag(name = "资源管理")
public class SysResController {
    private final ISysResFacade facade;

    @PostMapping("/get-by-id")
    @Operation(summary = "查询资源详情")
    public R<SysResRow> detail(@RequestParam @NotBlank @Size(min = 32, max = 32) String sysResId) {
        return R.success(facade.detail(sysResId));
    }

    @PostMapping("/create")
    @Operation(summary = "创建资源")
    public R<String> create(@Valid @RequestBody SysResCreateParam param) {
        return R.success(facade.create(param));
    }

    @PostMapping("/update-by-id")
    @Operation(summary = "编辑资源")
    public R<String> edit(@Valid @RequestBody SysResUpdateParam param) {
        return R.success(facade.edit(param));
    }

    @PostMapping("/delete-by-id")
    @Operation(summary = "删除资源")
    public R<Void> delete(@RequestParam @NotBlank @Size(min = 32, max = 32) String sysResId, @RequestParam @PositiveOrZero long version) {
        facade.delete(sysResId, version);
        return R.success(null);
    }

    @PostMapping("/tree")
    @Operation(summary = "查询资源树")
    public R<List<SysResRow>> tree() {
        return R.success(facade.tree());
    }

    @PostMapping("/import-json")
    @Operation(summary = "导入资源树")
    public R<ResourceImportReport> importJson(@Valid @RequestBody ResourceImport param) {
        return R.success(facade.importJson(param));
    }
}
