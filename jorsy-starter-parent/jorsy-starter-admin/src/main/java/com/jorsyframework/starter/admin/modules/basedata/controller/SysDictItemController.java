package com.jorsyframework.starter.admin.modules.basedata.controller;

import com.jorsyframework.starter.admin.modules.basedata.domain.dto.SysDictItemRow;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysDictItemCreateParam;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysDictItemQuery;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysDictItemUpdateParam;
import com.jorsyframework.starter.admin.modules.basedata.facade.ISysDictItemFacade;
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
@RequestMapping("/admin/sys-dict-item")
@RequiredArgsConstructor
@Tag(name = "字典明细管理")
public class SysDictItemController {
    private final ISysDictItemFacade facade;

    @PostMapping("/page")
    @Operation(summary = "分页查询字典明细")
    public R<PageResult<SysDictItemRow>> page(@Valid @RequestBody SysDictItemQuery param) {
        return R.success(facade.page(param));
    }

    @PostMapping("/get-by-id")
    @Operation(summary = "查询字典明细详情")
    public R<SysDictItemRow> detail(@RequestParam @NotBlank @Size(min = 32, max = 32) String sysDictItemId) {
        return R.success(facade.detail(sysDictItemId));
    }

    @PostMapping("/create")
    @Operation(summary = "创建字典明细")
    public R<String> create(@Valid @RequestBody SysDictItemCreateParam param) {
        return R.success(facade.create(param));
    }

    @PostMapping("/update-by-id")
    @Operation(summary = "编辑字典明细")
    public R<String> edit(@Valid @RequestBody SysDictItemUpdateParam param) {
        return R.success(facade.edit(param));
    }

    @PostMapping("/delete-by-id")
    @Operation(summary = "删除字典明细")
    public R<Void> delete(@RequestParam @NotBlank @Size(min = 32, max = 32) String sysDictItemId, @RequestParam @PositiveOrZero long version) {
        facade.delete(sysDictItemId, version);
        return R.success(null);
    }
}
