package com.jopsonframework.starter.admin.modules.rights.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jopsonframework.starter.admin.modules.rights.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
