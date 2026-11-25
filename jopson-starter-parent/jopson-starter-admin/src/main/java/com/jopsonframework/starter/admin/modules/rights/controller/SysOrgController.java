package com.jopsonframework.starter.admin.modules.rights.controller;

import com.jopsonframework.starter.admin.modules.rights.domain.dto.SysOrgRow;
import com.jopsonframework.starter.admin.modules.rights.domain.param.OrgCodes;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysOrgCreateParam;
import com.jopsonframework.starter.admin.modules.rights.domain.param.SysOrgUpdateParam;
import com.jopsonframework.starter.admin.modules.rights.facade.ISysOrgFacade;
import com.jopsonframework.starter.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/sys-org")
@RequiredArgsConstructor
@Tag(name = "组织管理")
public class SysOrgController {
    private final ISysOrgFacade facade;

    @PostMapping("/get-by-id")
    @Operation(summary = "查询组织详情")
    public R<SysOrgRow> detail(@RequestParam @NotBlank @Size(min = 32, max = 32) String sysOrgId) {
        return R.success(facade.detail(sysOrgId));
    }

    @PostMapping("/create")
    @Operation(summary = "创建组织")
    public R<String> create(@Valid @RequestBody SysOrgCreateParam param) {
        return R.success(facade.create(param));
    }

    @PostMapping("/update-by-id")
    @Operation(summary = "编辑组织")
    public R<String> edit(@Valid @RequestBody SysOrgUpdateParam param) {
        return R.success(facade.edit(param));
    }

    @PostMapping("/delete-by-id")
    @Operation(summary = "删除组织")
    public R<Void> delete(@RequestParam @NotBlank @Size(min = 32, max = 32) String sysOrgId, @RequestParam @PositiveOrZero long version) {
        facade.delete(sysOrgId, version);
        return R.success(null);
    }

    @PostMapping("/tree")
    @Operation(summary = "查询组织树")
    public R<List<SysOrgRow>> tree() {
        return R.success(facade.tree());
    }

    @PostMapping("/get-names-by-codes")
    @Operation(summary = "批量查询组织名称")
    public R<Map<String, String>> names(@Valid @RequestBody OrgCodes param) {
        return R.success(facade.names(param.codes()));
    }

    @GetMapping("/get-name-by-code")
    @Operation(summary = "按编码查询组织名称")
    public R<String> name(@RequestParam @NotBlank String sysOrgCode) {
        return R.success(facade.name(sysOrgCode));
    }
}
