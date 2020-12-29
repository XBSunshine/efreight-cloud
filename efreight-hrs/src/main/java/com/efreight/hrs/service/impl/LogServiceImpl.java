package com.efreight.hrs.service.impl;

import java.time.LocalDateTime;
import java.util.Date;

import com.efreight.common.security.util.SecurityUtils;
import com.efreight.hrs.entity.Log;
import com.efreight.hrs.dao.LogMapper;
import com.efreight.hrs.service.LogService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, Log> implements
		LogService {

	@Override
	public IPage<Log> getLogList(Page page, Log bean) {
		
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		return baseMapper.getLogList(page, bean);
	}

	public void doSave(Log bean) {
		bean.setCreatorId(SecurityUtils.getUser().getId());
		bean.setCreateTime(LocalDateTime.now());
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		bean.setDeptId(SecurityUtils.getUser().getDeptId());
		baseMapper.insert(bean);
	}
}
