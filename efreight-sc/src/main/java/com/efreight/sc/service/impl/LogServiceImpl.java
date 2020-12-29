package com.efreight.sc.service.impl;

import com.efreight.sc.entity.Log;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.dao.LogMapper;
import com.efreight.sc.service.LogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

/**
 * <p>
 * AF 基础信息 操作日志 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, Log> implements LogService {
	@Override
    public void saveLog(Log logBean) {
        logBean.setCreatorId(SecurityUtils.getUser().getId());
        logBean.setCreatorName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
        logBean.setCreatTime(LocalDateTime.now());
        logBean.setOrgId(SecurityUtils.getUser().getOrgId());
        baseMapper.insert(logBean);
    }
}
