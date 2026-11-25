package com.jopsonframework.starter.admin.modules.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.jopsonframework.starter.admin.modules.basedata.domain.entity.SysDict;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysDictMapper extends BaseMapper<SysDict> {}
