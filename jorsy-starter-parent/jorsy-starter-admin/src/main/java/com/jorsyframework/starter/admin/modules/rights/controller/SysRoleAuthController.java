package com.jorsyframework.starter.admin.modules.rights.controller;

import com.jorsyframework.starter.admin.modules.rights.domain.dto.RoleResourceTree;
import com.jorsyframework.starter.admin.modules.rights.domain.param.RoleResources;
import com.jorsyframework.starter.admin.modules.rights.facade.ISysRoleAuthFacade;
import com.jorsyframework.starter.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/sys-role-auth")
@RequiredArgsConstructor
@Tag(name = "角色资源授权")
public class SysRoleAuthController {
    private final ISysRoleAuthFacade facade;

    @PostMapping("/list-auth-res")
    @Operation(summary = "读取资源树、可授资源和当前授权")
    public R<RoleResourceTree> list(@RequestParam @NotBlank @Size(min = 32, max = 32) String sysRoleId) {
        return R.success(facade.list(sysRoleId));
    }

    @PostMapping("/auth-res")
    @Operation(summary = "保存角色的资源授权集合")
    public R<Void> authorize(@Valid @RequestBody RoleResources param) {
        facade.authorize(param);
        return R.success(null);
    }
}
