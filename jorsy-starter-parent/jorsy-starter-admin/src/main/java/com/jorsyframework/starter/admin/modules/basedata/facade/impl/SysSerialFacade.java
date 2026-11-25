package com.jorsyframework.starter.admin.modules.basedata.facade.impl;

import cn.hutool.core.util.IdUtil;
import com.jorsyframework.starter.admin.common.exception.AdminBusinessException;
import com.jorsyframework.starter.admin.modules.basedata.converter.SysSerialConverter;
import com.jorsyframework.starter.admin.modules.basedata.domain.dto.SysSerialRow;
import com.jorsyframework.starter.admin.modules.basedata.domain.entity.SysSerial;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysSerialCreateParam;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysSerialQuery;
import com.jorsyframework.starter.admin.modules.basedata.domain.param.SysSerialUpdateParam;
import com.jorsyframework.starter.admin.modules.basedata.facade.ISysSerialFacade;
import com.jorsyframework.starter.admin.modules.basedata.service.ISysSerialService;
import com.jorsyframework.starter.admin.modules.rights.access.SysUserOperatorProvider;
import com.jorsyframework.starter.core.page.PageResult;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.jorsyframework.starter.admin.common.exception.AdminBusinessException.checkVersion;
import static com.jorsyframework.starter.admin.common.exception.AdminBusinessException.checkWrite;
import static com.jorsyframework.starter.admin.common.exception.AdminBusinessException.requireFound;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SysSerialFacade implements ISysSerialFacade {
    private final SysUserOperatorProvider access;
    private final ISysSerialService sysSerialService;
    private final SysSerialConverter sysSerialConverter;

    @Override
    public PageResult<SysSerialRow> page(SysSerialQuery param) {
        access.require("Admin:SysSerial:View");
        var page = sysSerialService.page(param);
        return new PageResult<>(page.items().stream().map(sysSerialConverter::row).toList(), page.total(), page.currentPage(), page.pageSize());
    }

    @Override
    public SysSerialRow detail(String id) {
        access.require("Admin:SysSerial:View");
        return sysSerialConverter.row(requireFound(sysSerialService.getById(id)));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String create(SysSerialCreateParam param) {
        String actor = access.require("Admin:SysSerial:Create");
        var row = sysSerialConverter.create(param);
        validate(row);
        row.setCreateId(actor);
        checkWrite(sysSerialService.save(row), "新增失败");
        return row.getSysSerialId();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String edit(SysSerialUpdateParam param) {
        String actor = access.require("Admin:SysSerial:Update");
        var row = sysSerialService.lockById(param.sysSerialId());
        checkVersion(row.getVersion(), param.version());
        var before = List.of(Objects.toString(row.getDateFormat(), ""), Objects.toString(row.getSerialPrefix(), ""),
                Objects.toString(row.getSerialSuffix(), ""), row.getSerialDigit().toString());
        sysSerialConverter.edit(param, row);
        validate(row);
        if (row.getLastSerial() != null && !before.equals(List.of(Objects.toString(row.getDateFormat(), ""),
                Objects.toString(row.getSerialPrefix(), ""), Objects.toString(row.getSerialSuffix(), ""),
                row.getSerialDigit().toString()))) {
            row.setLastSerial("{}");
        }
        row.setUpdateId(actor);
        checkWrite(sysSerialService.updateById(row), "记录已变化，请刷新");
        return row.getSysSerialId();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(String id, long version) {
        String actor = access.require("Admin:SysSerial:Delete");
        var row = sysSerialService.lockById(id);
        checkVersion(row.getVersion(), version);
        if (Boolean.TRUE.equals(row.getSysData()) || row.getLastSerial() != null) throw AdminBusinessException.conflict("内置或已取号的规则不能删除");
        sysSerialService.delete(row, actor);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String nextValue(String serialCode) {
        String actor = access.require("Admin:SysSerial:NextValue");
        try {
            if (!"SNOWFLAKE".equals(serialCode)) {
                String value = sysSerialService.allocate(serialCode, actor);
                if (value != null) return value;
                LOG.warn("未配置流水号规则，改用雪花号");
            }
            return IdUtil.getSnowflakeNextIdStr();
        } catch (RuntimeException failure) {
            LOG.warn("流水号分配失败，改用备用号，原因 {}", failure.getClass().getSimpleName());
            return IdUtil.objectId().toUpperCase(java.util.Locale.ROOT);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(SysSerialFacade.class);

    private static void validate(SysSerial row) {
        if ("SNOWFLAKE".equalsIgnoreCase(row.getSerialCode())) throw AdminBusinessException.invalid("SNOWFLAKE 是保留编码");
        if (row.getDateFormat() == null || row.getDateFormat().isBlank()) return;
        try {
            DateTimeFormatter.ofPattern(row.getDateFormat()).format(LocalDateTime.now());
        } catch (IllegalArgumentException | DateTimeException invalidFormat) {
            throw AdminBusinessException.invalid("日期格式无效");
        }
    }
}
