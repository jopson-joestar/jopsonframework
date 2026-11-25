package com.jorsyframework.starter.admin.modules.basedata.controller;

import com.jorsyframework.starter.admin.modules.basedata.domain.dto.DictOption;
import com.jorsyframework.starter.admin.modules.basedata.domain.dto.DictSyncReport;
import com.jorsyframework.starter.admin.modules.basedata.domain.dto.SysDictRow;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.DictSyncRequest;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysDictCreateParam;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysDictQuery;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysDictUpdateParam;
import com.jorsyframework.starter.admin.modules.basedata.facade.ISysDictFacade;
import com.jorsyframework.starter.core.page.PageResult;
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
@RequestMapping("/admin/sys-dict")
@RequiredArgsConstructor
@Tag(name = "字典管理")
public class SysDictController {
    private final ISysDictFacade facade;

    @PostMapping("/page")
    @Operation(summary = "分页查询字典")
    public R<PageResult<SysDictRow>> page(@Valid @RequestBody SysDictQuery param) {
        return R.success(facade.page(param));
    }

    @PostMapping("/get-by-id")
    @Operation(summary = "查询字典详情")
    public R<SysDictRow> detail(@RequestParam @NotBlank @Size(min = 32, max = 32) String sysDictId) {
        return R.success(facade.detail(sysDictId));
    }

    @PostMapping("/create")
    @Operation(summary = "创建字典")
    public R<String> create(@Valid @RequestBody SysDictCreateParam param) {
        return R.success(facade.create(param));
    }

    @PostMapping("/update-by-id")
    @Operation(summary = "编辑字典")
    public R<String> edit(@Valid @RequestBody SysDictUpdateParam param) {
        return R.success(facade.edit(param));
    }

    @PostMapping("/delete-by-id")
    @Operation(summary = "删除字典")
    public R<Void> delete(@RequestParam @NotBlank @Size(min = 32, max = 32) String sysDictId, @RequestParam @PositiveOrZero long version) {
        facade.delete(sysDictId, version);
        return R.success(null);
    }

    @PostMapping("/options")
    @Operation(summary = "按编码取字典选项")
    public R<List<DictOption>> options(@RequestParam @NotBlank String sysDictCode) {
        return R.success(facade.options(sysDictCode));
    }

    @PostMapping("/sync-by-enum")
    @Operation(summary = "从已登记枚举同步字典")
    public R<DictSyncReport> syncByEnum(@Valid @RequestBody DictSyncRequest param) {
        return R.success(facade.syncByEnum(param));
    }
}
