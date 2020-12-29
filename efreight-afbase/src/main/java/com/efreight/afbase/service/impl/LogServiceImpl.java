package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.LogMapper;
import com.efreight.afbase.dao.ScLogMapper;
import com.efreight.afbase.dao.TcLogMapper;
import com.efreight.afbase.entity.LogBean;
import com.efreight.afbase.entity.ScLog;
import com.efreight.afbase.entity.TcLog;
import com.efreight.afbase.service.LogService;
import com.efreight.common.security.util.SecurityUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class LogServiceImpl extends ServiceImpl<LogMapper, LogBean> implements LogService {
    private final ScLogMapper scLogMapper;
    private final TcLogMapper tcLogMapper;

    @Override
    public IPage<LogBean> getPage(Page page, LogBean logBean) {
        if ("AE".equals(logBean.getBusinessScope()) || "AI".equals(logBean.getBusinessScope())) {
            QueryWrapper<LogBean> queryWrapper = new QueryWrapper<>();
            if (StrUtil.isNotBlank(logBean.getBusinessScope())) {
                queryWrapper.eq("business_scope", logBean.getBusinessScope());
            }
            if (StrUtil.isNotBlank(logBean.getOrderNumber())) {
                queryWrapper.like("order_number", logBean.getOrderNumber());
            }
            if (StrUtil.isNotBlank(logBean.getCreatorName())) {
                queryWrapper.like("creator_name", logBean.getCreatorName());
            }
            if (StrUtil.isNotBlank(logBean.getOperationTimeStart())) {
                queryWrapper.ge("creat_time", logBean.getOperationTimeStart() + " 00:00:00");
            }
            if (StrUtil.isNotBlank(logBean.getOperationTimeEnd())) {
                queryWrapper.le("creat_time", logBean.getOperationTimeEnd() + " 23:59:59");
            }
            if (StrUtil.isNotBlank(logBean.getLogName())) {
                queryWrapper.and(wrapper -> wrapper.like("page_name", logBean.getLogName())
                        .or().like("page_function", logBean.getLogName())
                        .or().like("log_remark", logBean.getLogName()));
            }
            queryWrapper.eq("org_id", SecurityUtils.getUser().getOrgId());
            queryWrapper.orderByDesc("log_id");
            return baseMapper.selectPage(page, queryWrapper);
        } else if ("SE".equals(logBean.getBusinessScope()) || "SI".equals(logBean.getBusinessScope())) {
            QueryWrapper<ScLog> queryWrapper = new QueryWrapper<>();
            if (StrUtil.isNotBlank(logBean.getBusinessScope())) {
                queryWrapper.eq("business_scope", logBean.getBusinessScope());
            }
            if (StrUtil.isNotBlank(logBean.getOrderNumber())) {
                queryWrapper.like("order_number", logBean.getOrderNumber());
            }
            if (StrUtil.isNotBlank(logBean.getCreatorName())) {
                queryWrapper.like("creator_name", logBean.getCreatorName());
            }
            if (StrUtil.isNotBlank(logBean.getOperationTimeStart())) {
                queryWrapper.ge("creat_time", logBean.getOperationTimeStart() + " 00:00:00");
            }
            if (StrUtil.isNotBlank(logBean.getOperationTimeEnd())) {
                queryWrapper.le("creat_time", logBean.getOperationTimeEnd() + " 23:59:59");
            }
            if (StrUtil.isNotBlank(logBean.getLogName())) {
                queryWrapper.and(wrapper -> wrapper.like("page_name", logBean.getLogName())
                        .or().like("page_function", logBean.getLogName())
                        .or().like("log_remark", logBean.getLogName()));
            }
            queryWrapper.eq("org_id", SecurityUtils.getUser().getOrgId());
            queryWrapper.orderByDesc("log_id");
            return scLogMapper.selectPage(page, queryWrapper);
        } else if (logBean.getBusinessScope().startsWith("T")) {
            QueryWrapper<TcLog> queryWrapper = new QueryWrapper<>();
            if (StrUtil.isNotBlank(logBean.getBusinessScope())) {
                queryWrapper.eq("business_scope", logBean.getBusinessScope());
            }
            if (StrUtil.isNotBlank(logBean.getOrderNumber())) {
                queryWrapper.like("order_number", logBean.getOrderNumber());
            }
            if (StrUtil.isNotBlank(logBean.getCreatorName())) {
                queryWrapper.like("creator_name", logBean.getCreatorName());
            }
            if (StrUtil.isNotBlank(logBean.getOperationTimeStart())) {
                queryWrapper.ge("creat_time", logBean.getOperationTimeStart() + " 00:00:00");
            }
            if (StrUtil.isNotBlank(logBean.getOperationTimeEnd())) {
                queryWrapper.le("creat_time", logBean.getOperationTimeEnd() + " 23:59:59");
            }
            if (StrUtil.isNotBlank(logBean.getLogName())) {
                queryWrapper.and(wrapper -> wrapper.like("page_name", logBean.getLogName())
                        .or().like("page_function", logBean.getLogName())
                        .or().like("log_remark", logBean.getLogName()));
            }
            queryWrapper.eq("org_id", SecurityUtils.getUser().getOrgId());
            queryWrapper.orderByDesc("log_id");
            return tcLogMapper.selectPage(page, queryWrapper);
        }


        return null;
    }

    @Override
    public void saveLog(LogBean logBean) {
        logBean.setCreatorId(SecurityUtils.getUser().getId());
        logBean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        logBean.setCreatTime(LocalDateTime.now());
        logBean.setOrgId(SecurityUtils.getUser().getOrgId());
        baseMapper.insert(logBean);
    }

    @Override
    public void modifyForDeleteInbound(LogBean logBean) {
        baseMapper.modifyForDeleteInbound(logBean);
    }
}
