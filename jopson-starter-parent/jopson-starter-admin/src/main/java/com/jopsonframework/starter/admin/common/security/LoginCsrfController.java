package com.jopsonframework.starter.admin.common.security;

import com.jopsonframework.starter.core.result.R;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginCsrfController {
    public record Csrf(String headerName, String token) {}

    @GetMapping("/auth/csrf")
    public R<Csrf> csrf(CsrfToken token) {
        return R.success(new Csrf(token.getHeaderName(), token.getToken()));
    }
}
