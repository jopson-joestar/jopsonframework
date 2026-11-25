package com.jopsonframework.starter.admin.modules.rights.controller;

import com.jopsonframework.starter.admin.modules.rights.access.SysLoginSession;
import com.jopsonframework.starter.admin.modules.rights.domain.dto.SysLoginMenu;
import com.jopsonframework.starter.admin.modules.rights.domain.dto.SysLoginUser;
import com.jopsonframework.starter.admin.modules.rights.facade.ISysLoginFacade;
import com.jopsonframework.starter.core.result.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SysLoginController {
    private final SysLoginSession session;
    private final ISysLoginFacade login;

    public record SwitchRights(@NotBlank @Size(min = 32, max = 32) String sysRightsId) {}

    @GetMapping("/admin/sys-login/current")
    public R<SysLoginUser> current(HttpServletRequest request) {
        return R.success(session.current(request));
    }

    @GetMapping("/admin/sys-login/menu")
    public R<List<SysLoginMenu>> menu(HttpServletRequest request) {
        return R.success(login.menu(session.current(request)));
    }

    @PostMapping("/admin/sys-login/switch-rights")
    public R<SysLoginUser> switchRights(@Valid @RequestBody SwitchRights param,
                                        HttpServletRequest request, HttpServletResponse response) {
        return R.success(session.switchRights(param.sysRightsId(), request, response));
    }
}
