package com.efreight.sc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.entity.LcLog;
import com.efreight.sc.entity.VlLog;
import com.efreight.sc.dao.VlLogMapper;
import com.efreight.sc.service.VlLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * VL 派車單操作日志 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-31
 */
@Service
public class VlLogServiceImpl extends ServiceImpl<VlLogMapper, VlLog> implements VlLogService {

    @Override
    public void insert(VlLog vlLog) {
        vlLog.setCreatorId(SecurityUtils.getUser().getId());
        vlLog.setCreatorName(SecurityUtils.getUser().buildOptName());
        vlLog.setCreatTime(LocalDateTime.now());
        vlLog.setOrgId(SecurityUtils.getUser().getOrgId());
        save(vlLog);
    }

    @Override
    public IPage getPage(Page page, VlLog vlLog) {
        LambdaQueryWrapper<VlLog> wrapper = Wrappers.<VlLog>lambdaQuery();
        if (StrUtil.isNotBlank(vlLog.getCreatorName())) {
            wrapper.like(VlLog::getCreatorName, vlLog.getCreatorName());
        }
        if (StrUtil.isNotBlank(vlLog.getOperationTimeStart())) {
            wrapper.ge(VlLog::getCreatTime, vlLog.getOperationTimeStart() + " 00:00:00");
        }
        if (StrUtil.isNotBlank(vlLog.getOperationTimeEnd())) {
            wrapper.le(VlLog::getCreatTime, vlLog.getOperationTimeEnd() + " 23:59:59");
        }
        if (StrUtil.isNotBlank(vlLog.getLogName())) {
            wrapper.and(i -> i.like(VlLog::getPageName, vlLog.getLogName())
                    .or().like(VlLog::getPageFunction, vlLog.getLogName())
                    .or().like(VlLog::getLogRemark, vlLog.getLogName()));
        }

        wrapper.eq(VlLog::getOrderId, vlLog.getOrderId()).eq(VlLog::getOrgId, SecurityUtils.getUser().getOrgId()).orderByDesc(VlLog::getLogId);
        return page(page, wrapper);
    }
}
