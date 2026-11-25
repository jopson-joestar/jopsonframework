package com.jopsonframework.starter.admin.modules.basedata.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.jopsonframework.starter.admin.common.mybatis.AuditConditions;
import com.jopsonframework.starter.admin.modules.basedata.domain.entity.SysSerial;
import com.jopsonframework.starter.admin.modules.basedata.domain.param.SysSerialQuery;
import com.jopsonframework.starter.admin.modules.basedata.mapper.SysSerialMapper;
import com.jopsonframework.starter.admin.modules.basedata.service.ISysSerialService;
import com.jopsonframework.starter.core.mybatis.MybatisPages;
import com.jopsonframework.starter.core.page.PageQuery;
import com.jopsonframework.starter.core.page.PageResult;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.jopsonframework.starter.admin.common.exception.AdminBusinessException.checkWrite;
import static com.jopsonframework.starter.admin.common.exception.AdminBusinessException.requireFound;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
public class SysSerialService extends ServiceImpl<SysSerialMapper, SysSerial> implements ISysSerialService {
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Override
    public SysSerial lockCode(String code) {
        return lambdaQuery().eq(SysSerial::getSerialCode, code).last("FOR UPDATE").one();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String allocate(String code, String actor) {
        SysSerial rule = lockCode(code);
        if (rule == null) return null;
        String date = rule.getDateFormat() == null || rule.getDateFormat().isBlank() ? ""
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern(rule.getDateFormat()));
        String bucket = date.isEmpty() ? "_" : date;
        var json = JSON.createObjectNode();
        if (rule.getLastSerial() != null) {
            var saved = JSON.readTree(rule.getLastSerial());
            if (!saved.isObject()) throw new IllegalStateException("流水号状态损坏");
            json.setAll((ObjectNode) saved);
        }
        var last = json.get(bucket);
        if (last != null && (!last.isIntegralNumber() || !last.canConvertToLong() || last.asLong() < 0))
            throw new IllegalStateException("流水号状态损坏");
        long maximum = Long.parseLong("9".repeat(rule.getSerialDigit()));
        long previous = last == null ? 0 : last.asLong();
        if (previous >= maximum) throw new IllegalStateException("流水号已耗尽");
        long next = previous + 1;
        json.put(bucket, next);
        rule.setLastSerial(json.toString());
        rule.setUpdateId(actor);
        checkWrite(updateById(rule), "记录已变化，请刷新");
        return Objects.toString(rule.getSerialPrefix(), "") + date
                + String.format(Locale.ROOT, "%0" + rule.getSerialDigit() + "d", next)
                + Objects.toString(rule.getSerialSuffix(), "");
    }

    @Override
    public PageResult<SysSerial> page(SysSerialQuery param) {
        var filter = Wrappers.lambdaQuery(SysSerial.class);
        filter.like(param.serialCode() != null && !param.serialCode().isBlank(), SysSerial::getSerialCode, param.serialCode());
        filter.like(param.serialName() != null && !param.serialName().isBlank(), SysSerial::getSerialName, param.serialName());
        AuditConditions.createdBetween(filter, param.createTimeBegin(), param.createTimeEnd());
        filter.orderByDesc(SysSerial::getCreateTime).orderByDesc(SysSerial::getSysSerialId);
        var pagination = MybatisPages.<SysSerial>toPage(new PageQuery(param.currentPage(), param.pageSize()));
        return MybatisPages.toResult(page(pagination, filter));
    }

    @Override
    public SysSerial lockById(String id) {
        return requireFound(lambdaQuery().eq(SysSerial::getSysSerialId, id).last("FOR UPDATE").one());
    }

    @Override
    public void delete(SysSerial row, String actor) {
        checkWrite(update(AuditConditions.logicalDelete(SysSerial.class, row, SysSerial::getSysSerialId, actor)), "删除失败，请刷新");
    }
}
