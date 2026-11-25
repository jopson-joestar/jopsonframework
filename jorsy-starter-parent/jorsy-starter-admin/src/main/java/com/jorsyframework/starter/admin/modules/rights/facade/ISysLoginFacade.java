package com.jorsyframework.starter.admin.modules.rights.facade;

import com.jorsyframework.starter.admin.modules.rights.domain.dto.SysLoginMenu;
import com.jorsyframework.starter.admin.modules.rights.domain.dto.SysLoginUser;
import java.util.List;
import java.util.Optional;

public interface ISysLoginFacade {
    Optional<String> defaultRightsId(String userId);
    Optional<SysLoginUser> current(String userId, String rightsId);
    List<SysLoginMenu> menu(SysLoginUser user);
}
