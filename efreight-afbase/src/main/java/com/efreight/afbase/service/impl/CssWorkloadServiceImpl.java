package com.efreight.afbase.service.impl;

import java.util.List;
import java.util.Map;

import com.efreight.afbase.entity.procedure.CssWorkloadDetail;
import org.springframework.stereotype.Service;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.CssWorkloadMapper;
import com.efreight.afbase.entity.procedure.CssWorkload;
import com.efreight.afbase.service.CssWorkloadService;
import com.efreight.common.security.util.SecurityUtils;

import lombok.AllArgsConstructor;


@Service
@AllArgsConstructor
public class CssWorkloadServiceImpl extends ServiceImpl<CssWorkloadMapper,CssWorkload> implements CssWorkloadService{
	
	private final CssWorkloadMapper cssWorkloadMapper;

	@Override
	public List<CssWorkload> getCssWorkloadList(CssWorkload cssWorkload) {
		if(StringUtils.isEmpty(cssWorkload.getOrderStatus())) {
			cssWorkload.setOrderStatus(null);
		}
		if(StringUtils.isEmpty(cssWorkload.getDept())) {
			cssWorkload.setDept(null);
		}
		cssWorkload.setOrgId(SecurityUtils.getUser().getOrgId());
		return baseMapper.getCssWorkloadList(cssWorkload);
	}

	@Override
	public List<Map> getCssWorkloadDetail(CssWorkload cssWorkload) {
		cssWorkload.setUserId(cssWorkload.getUserId().intValue());
		if(StringUtils.isEmpty(cssWorkload.getOrderStatus())) {
			cssWorkload.setOrderStatus(null);
		}
//		if("AE".equals(cssWorkload.getBusinessScope())||"AI".equals(cssWorkload.getBusinessScope())) {
//			
//			return cssWorkloadMapper.getCssWorkloadDetail_A(cssWorkload);
//		}else {
//			return cssWorkloadMapper.getCssWorkloadDetail_S(cssWorkload);
//		}
		cssWorkload.setOrgId(SecurityUtils.getUser().getOrgId());
		return cssWorkloadMapper.getCssWorkloadDetail(cssWorkload);
	}

	@Override
	public List<CssWorkloadDetail> getCssWorkloadDetailForExcel(CssWorkload cssWorkload) {
		cssWorkload.setUserId(cssWorkload.getUserId().intValue());
		if(StringUtils.isEmpty(cssWorkload.getOrderStatus())) {
			cssWorkload.setOrderStatus(null);
		}
		cssWorkload.setOrgId(SecurityUtils.getUser().getOrgId());
		return cssWorkloadMapper.getCssWorkloadDetailForExcel(cssWorkload);
	}

}
