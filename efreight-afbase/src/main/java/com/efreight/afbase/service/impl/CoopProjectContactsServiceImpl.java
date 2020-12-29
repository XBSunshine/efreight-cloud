package com.efreight.afbase.service.impl;

import java.util.List;
import java.util.Map;

import com.efreight.afbase.entity.CoopProjectContacts;
import com.efreight.afbase.dao.CoopProjectContactsMapper;
import com.efreight.afbase.service.CoopProjectContactsService;
import com.efreight.common.security.util.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CoopProjectContactsServiceImpl extends ServiceImpl<CoopProjectContactsMapper, CoopProjectContacts> implements CoopProjectContactsService {

	@Override
	public IPage<CoopProjectContacts> getListPage(Page page, CoopProjectContacts bean) {
		QueryWrapper<CoopProjectContacts> queryWrapper = new QueryWrapper<>();
		
		queryWrapper.eq("project_id", bean.getProjectId());
		queryWrapper.eq("org_id", SecurityUtils.getUser().getOrgId());
//		queryWrapper.orderByAsc("business_scope","project_code");
		return baseMapper.selectPage(page, queryWrapper);
	}
	@Override
	public List<Map<String, Object>> selectAll(CoopProjectContacts bean) {		
		return baseMapper.selectAll( bean.getProjectId(), SecurityUtils.getUser().getOrgId());
	}
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean doSave(CoopProjectContacts bean) {		
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		baseMapper.insert(bean);
		return true;
	}
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean doUpdate(CoopProjectContacts bean) {
		UpdateWrapper<CoopProjectContacts> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("contacts_id", bean.getContactsId());
		baseMapper.update(bean, updateWrapper);
		return true;
	}
}
