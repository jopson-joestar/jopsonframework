package com.jorsyframework.starter.admin.modules.rights.controller;

import com.jorsyframework.starter.admin.modules.rights.access.SysLoginSession;
import com.jorsyframework.starter.admin.modules.rights.domain.dto.SysLoginMenu;
import com.jorsyframework.starter.admin.modules.rights.domain.dto.SysLoginUser;
import com.jorsyframework.starter.admin.modules.rights.facade.ISysLoginFacade;
import com.jorsyframework.starter.core.result.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "jorsy.admin.login.enabled", havingValue = "true")
public class SysLoginController {
    private final SysLoginSession session;
    private final ISysLoginFacade login;

    public record Csrf(String headerName, String token) {}
    public record SwitchRights(@NotBlank @Size(min = 32, max = 32) String sysRightsId) {}

    @GetMapping("/auth/csrf")
    public R<Csrf> csrf(CsrfToken token) {
        return R.success(new Csrf(token.getHeaderName(), token.getToken()));
    }

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
