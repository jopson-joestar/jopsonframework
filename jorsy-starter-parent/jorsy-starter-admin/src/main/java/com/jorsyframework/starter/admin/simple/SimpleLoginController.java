package com.jorsyframework.starter.admin.simple;

import java.security.Principal;
import java.util.List;
import java.util.Set;

import com.jorsyframework.starter.admin.modules.rights.domain.dto.SysLoginMenu;
import com.jorsyframework.starter.admin.modules.rights.domain.dto.SysLoginUser;
import com.jorsyframework.starter.core.result.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/sys-login")
public class SimpleLoginController {
    @GetMapping("/current")
    public R<SysLoginUser> current(Principal principal) {
        String username = principal.getName();
        return R.success(new SysLoginUser(username, username, username, null, List.of(), Set.of(), false, List.of()));
    }

    @GetMapping("/menu")
    public R<List<SysLoginMenu>> menu() {
        return R.success(List.of());
    }
}
