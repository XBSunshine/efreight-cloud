package com.efreight.afbase.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;

import com.efreight.afbase.entity.AwbNumber;
import com.efreight.afbase.entity.CoopProject;
import com.efreight.afbase.entity.LogBean;
import com.efreight.afbase.dao.AwbNumberMapper;
import com.efreight.afbase.dao.CoopProjectMapper;
import com.efreight.afbase.service.CoopProjectService;
import com.efreight.afbase.service.LogService;
import com.efreight.common.security.util.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@AllArgsConstructor
public class CoopProjectServiceImpl extends ServiceImpl<CoopProjectMapper, CoopProject> implements CoopProjectService {
	private final LogService logService;
	@Override
	public IPage<CoopProject> getListPage(Page page, CoopProject bean) {
		QueryWrapper<CoopProject> queryWrapper = new QueryWrapper<>();
		if(bean.getBusinessScope()!=null && !"".equals(bean.getBusinessScope())) {
			queryWrapper.eq("business_scope", bean.getBusinessScope());
		}
		if(bean.getProjectCode()!=null && !"".equals(bean.getProjectCode())) {
			queryWrapper.eq("project_code", bean.getProjectCode());
		}
		if(bean.getBusinessScope()!=null && !"".equals(bean.getBusinessScope())) {
			queryWrapper.like("business_scope", bean.getBusinessScope());
		}
		if(bean.getProjectName()!=null && !"".equals(bean.getProjectName())) {
			queryWrapper.like("project_name", bean.getProjectName());
		}
		if(bean.getServicerName()!=null && !"".equals(bean.getServicerName())) {
			queryWrapper.like("servicer_name", bean.getServicerName());
		}
		if(bean.getSalesName()!=null && !"".equals(bean.getSalesName())) {
			queryWrapper.like("sales_name", bean.getSalesName());
		}
		if(bean.getSalesManagerName()!=null && !"".equals(bean.getSalesManagerName())) {
			queryWrapper.like("sales_manager_name", bean.getSalesManagerName());
		}
		if(bean.getIsOverseas()!=null && !"".equals(bean.getIsOverseas())) {
			queryWrapper.eq("is_overseas", bean.getIsOverseas());
		}
		if(bean.getIsHeadquarters()!=null && !"".equals(bean.getIsHeadquarters())) {
			queryWrapper.eq("is_headquarters", bean.getIsHeadquarters());
		}
		if(bean.getIsStop()!=null && !"".equals(bean.getIsStop())) {
			queryWrapper.eq("is_stop", bean.getIsStop());
		}
		if(bean.getIsLock()!=null && !"".equals(bean.getIsLock())) {
			queryWrapper.eq("is_lock", bean.getIsLock());
		}
//		if("0".equals(bean.getIsLock())) {
//			queryWrapper.isNull("open_user_id");
//		}
//		if("1".equals(bean.getIsLock())) {
//			queryWrapper.isNotNull("open_user_id");
//		}
		queryWrapper.eq("org_id", SecurityUtils.getUser().getOrgId());
		queryWrapper.orderByAsc("business_scope","project_code");
		return baseMapper.selectPage(page, queryWrapper);
	}
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean doSave(CoopProject bean) {
		bean.setCreateTime(new Date());
		bean.setCreatorId(SecurityUtils.getUser().getId());
		bean.setCreatorName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
		bean.setOrgId(SecurityUtils.getUser().getOrgId());
		baseMapper.insert(bean);
		return true;
	}
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean doUpdate(CoopProject bean) {
		bean.setEditTime(new Date());
		bean.setEditorId(SecurityUtils.getUser().getId());
		bean.setEditorName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
		UpdateWrapper<CoopProject> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("project_id", bean.getProjectId());
		baseMapper.update(bean, updateWrapper);
		return true;
	}
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean doStop(CoopProject bean) {
		bean.setStopUserId(SecurityUtils.getUser().getId());
		bean.setStopUserName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
		UpdateWrapper<CoopProject> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("project_id", bean.getProjectId());
		baseMapper.update(bean, updateWrapper);
		return true;
	}
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean doLock(CoopProject bean) {
		bean.setLockTime(new Date());
		bean.setLockUserId(SecurityUtils.getUser().getId());
		bean.setLockUserName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
		UpdateWrapper<CoopProject> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("project_id", bean.getProjectId());
		baseMapper.update(bean, updateWrapper);
		return true;
	}
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean doUnLock(CoopProject bean) {
		bean.setOpenTime(new Date());
		bean.setOpenUserId(SecurityUtils.getUser().getId());
		bean.setOpenUserName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
		UpdateWrapper<CoopProject> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("project_id", bean.getProjectId());
		baseMapper.update(bean, updateWrapper);
		return true;
	}
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean doOpenTime(CoopProject bean) {
		bean.setOpenTime(new Date());
		bean.setOpenUserId(SecurityUtils.getUser().getId());
		bean.setOpenUserName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
		UpdateWrapper<CoopProject> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("project_id", bean.getProjectId());
		baseMapper.update(bean, updateWrapper);
		//日志
		try {

			LogBean logBean=new LogBean();
			logBean.setBusinessScope("AE");
			logBean.setLogType("信控日志");
			logBean.setNodeName("延期解锁");
			logBean.setPageName("客户项目");
			logBean.setPageFunction("延期解锁");
			logBean.setLogRemark("延期解锁：<"+bean.getOpenLimit()+bean.getOpenReason()+">");

			logService.saveLog(logBean);
		} catch (Exception e) {
		}
		return true;
	}
	@Override
	public List<Map<String, Object>> selectCurrency() {
		return baseMapper.selectCurrency(SecurityUtils.getUser().getOrgId());
	}
}
