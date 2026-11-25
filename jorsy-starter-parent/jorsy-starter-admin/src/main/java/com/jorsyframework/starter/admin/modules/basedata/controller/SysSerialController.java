package com.jorsyframework.starter.admin.modules.basedata.controller;

import com.jorsyframework.starter.admin.modules.basedata.domain.dto.SysSerialRow;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysSerialCreateParam;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysSerialQuery;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysSerialUpdateParam;
import com.jorsyframework.starter.admin.modules.basedata.facade.ISysSerialFacade;
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
@RequestMapping("/admin/sys-serial")
@RequiredArgsConstructor
@Tag(name = "序列管理")
public class SysSerialController {
    private final ISysSerialFacade facade;

    @PostMapping("/page")
    @Operation(summary = "分页查询序列")
    public R<PageResult<SysSerialRow>> page(@Valid @RequestBody SysSerialQuery param) {
        return R.success(facade.page(param));
    }

    @PostMapping("/get-by-id")
    @Operation(summary = "查询序列详情")
    public R<SysSerialRow> detail(@RequestParam @NotBlank @Size(min = 32, max = 32) String sysSerialId) {
        return R.success(facade.detail(sysSerialId));
    }

    @PostMapping("/create")
    @Operation(summary = "创建序列")
    public R<String> create(@Valid @RequestBody SysSerialCreateParam param) {
        return R.success(facade.create(param));
    }

    @PostMapping("/update-by-id")
    @Operation(summary = "编辑序列")
    public R<String> edit(@Valid @RequestBody SysSerialUpdateParam param) {
        return R.success(facade.edit(param));
    }

    @PostMapping("/delete-by-id")
    @Operation(summary = "删除序列")
    public R<Void> delete(@RequestParam @NotBlank @Size(min = 32, max = 32) String sysSerialId, @RequestParam @PositiveOrZero long version) {
        facade.delete(sysSerialId, version);
        return R.success(null);
    }

    @PostMapping("/next-value")
    @Operation(summary = "分配流水号")
    public R<String> nextValue(@RequestParam @NotBlank @Size(max = 50) String serialCode) {
        return R.success(facade.nextValue(serialCode));
    }
}
