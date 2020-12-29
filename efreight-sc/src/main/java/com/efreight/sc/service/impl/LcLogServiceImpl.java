package com.efreight.sc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.entity.LcLog;
import com.efreight.sc.dao.LcLogMapper;
import com.efreight.sc.service.LcLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * LC 订单操作日志 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
@Service
public class LcLogServiceImpl extends ServiceImpl<LcLogMapper, LcLog> implements LcLogService {

    @Override
    public void insert(LcLog lcLog) {
        lcLog.setCreatorId(SecurityUtils.getUser().getId());
        lcLog.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        lcLog.setCreatTime(LocalDateTime.now());
        lcLog.setOrgId(SecurityUtils.getUser().getOrgId());
        save(lcLog);
    }

    @Override
    public IPage getPage(Page page, LcLog lcLog) {
        LambdaQueryWrapper<LcLog> wrapper = Wrappers.<LcLog>lambdaQuery();
        if (StrUtil.isNotBlank(lcLog.getCreatorName())) {
            wrapper.like(LcLog::getCreatorName, lcLog.getCreatorName());
        }
        if (StrUtil.isNotBlank(lcLog.getOperationTimeStart())) {
            wrapper.ge(LcLog::getCreatTime, lcLog.getOperationTimeStart() + " 00:00:00");
        }
        if (StrUtil.isNotBlank(lcLog.getOperationTimeEnd())) {
            wrapper.le(LcLog::getCreatTime, lcLog.getOperationTimeEnd() + " 23:59:59");
        }
        if (StrUtil.isNotBlank(lcLog.getLogName())) {
            wrapper.and(i -> i.like(LcLog::getPageName, lcLog.getLogName())
                    .or().like(LcLog::getPageFunction, lcLog.getLogName())
                    .or().like(LcLog::getLogRemark, lcLog.getLogName()));
        }

        wrapper.eq(LcLog::getOrderId, lcLog.getOrderId()).eq(LcLog::getOrgId, SecurityUtils.getUser().getOrgId()).orderByDesc(LcLog::getLogId);
        return page(page, wrapper);
    }
}
