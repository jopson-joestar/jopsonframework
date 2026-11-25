package com.jorsyframework.starter.admin.modules.rights.access;

import com.jorsyframework.starter.admin.modules.rights.domain.dto.SysLoginUser;
import com.jorsyframework.starter.admin.modules.rights.facade.ISysLoginFacade;
import com.jorsyframework.starter.admin.modules.rights.access.SysLoginAuthenticationProvider.Principal;
import com.jorsyframework.starter.admin.common.exception.AdminBusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.Optional;

@RequiredArgsConstructor
public class SysLoginSession implements SysUserOperatorProvider {
    private static final String CURRENT = SysLoginSession.class.getName();
    private final ISysLoginFacade login;
    private final SecurityContextRepository contexts;

    public SysLoginUser current(HttpServletRequest request) {
        var cached = (SysLoginUser) request.getAttribute(CURRENT);
        if (cached != null) return cached;
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Principal person))
            throw new InsufficientAuthenticationException("请先登录");
        var user = login.current(person.userId(), person.rightsId()).orElseThrow(() -> {
            var session = request.getSession(false);
            if (session != null) session.invalidate();
            SecurityContextHolder.clearContext();
            return new InsufficientAuthenticationException("登录身份已失效，请重新登录");
        });
        request.setAttribute(CURRENT, user);
        return user;
    }

    public SysLoginUser switchRights(String rightsId, HttpServletRequest request, HttpServletResponse response) {
        var user = current(request);
        var next = login.current(user.userId(), rightsId)
                .orElseThrow(() -> AdminBusinessException.forbidden("无法切换到该身份"));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(SysLoginAuthenticationProvider.authenticated(new Principal(user.userId(), rightsId)));
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
        request.setAttribute(CURRENT, next);
        return next;
    }

    @Override
    public Optional<Operator> currentOperator() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs))
            return Optional.empty();
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Principal)) return Optional.empty();
        var user = current(attrs.getRequest());
        return Optional.of(new Operator(user.userId(), user.permissions(), user.administrator()));
    }
}
