package com.efreight.sc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.entity.IoLog;
import com.efreight.sc.dao.IoLogMapper;
import com.efreight.sc.service.IoLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * IO 订单操作日志 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
@Service
public class IoLogServiceImpl extends ServiceImpl<IoLogMapper, IoLog> implements IoLogService {

    @Override
    public void insert(IoLog ioLog) {
        ioLog.setCreatorId(SecurityUtils.getUser().getId());
        ioLog.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        ioLog.setCreatTime(LocalDateTime.now());
        ioLog.setOrgId(SecurityUtils.getUser().getOrgId());
        save(ioLog);
    }

    @Override
    public IPage getPage(Page page, IoLog ioLog) {
        LambdaQueryWrapper<IoLog> wrapper = Wrappers.<IoLog>lambdaQuery();
        if (StrUtil.isNotBlank(ioLog.getCreatorName())) {
            wrapper.like(IoLog::getCreatorName, ioLog.getCreatorName());
        }
        if (StrUtil.isNotBlank(ioLog.getOperationTimeStart())) {
            wrapper.ge(IoLog::getCreatTime, ioLog.getOperationTimeStart() + " 00:00:00");
        }
        if (StrUtil.isNotBlank(ioLog.getOperationTimeEnd())) {
            wrapper.le(IoLog::getCreatTime, ioLog.getOperationTimeEnd() + " 23:59:59");
        }
        if (StrUtil.isNotBlank(ioLog.getLogName())) {
            wrapper.and(i -> i.like(IoLog::getPageName, ioLog.getLogName())
                    .or().like(IoLog::getPageFunction, ioLog.getLogName())
                    .or().like(IoLog::getLogRemark, ioLog.getLogName()));
        }

        wrapper.eq(IoLog::getOrderId, ioLog.getOrderId()).eq(IoLog::getOrgId, SecurityUtils.getUser().getOrgId()).orderByDesc(IoLog::getLogId);
        return page(page, wrapper);
    }
}
