package com.efreight.afbase.service.impl;

import com.efreight.afbase.entity.ScLog;
import com.efreight.afbase.dao.ScLogMapper;
import com.efreight.afbase.service.ScLogService;
import com.efreight.common.security.util.SecurityUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

/**
 * <p>
 * AF 基础信息 操作日志 服务实现类
 * </p>
 *
 * @author qipm
 * @since 2020-03-09
 */
@Service
public class ScLogServiceImpl extends ServiceImpl<ScLogMapper, ScLog> implements ScLogService {
	@Override
    public void saveLog(ScLog logBean) {
        logBean.setCreatorId(SecurityUtils.getUser().getId());
        logBean.setCreatorName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
        logBean.setCreatTime(LocalDateTime.now());
        logBean.setOrgId(SecurityUtils.getUser().getOrgId());
        baseMapper.insert(logBean);
    }
}
