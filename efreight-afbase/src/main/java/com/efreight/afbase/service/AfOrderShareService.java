package com.efreight.afbase.service;

import java.util.HashMap;
import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.AfOrderShare;

public interface AfOrderShareService  extends IService<AfOrderShare>{
	IPage<HashMap> getCoopList(Page page, AfOrderShare bean);
	boolean afOrderShareCheck(Integer orderId,Integer coopId,Integer shareOrgId);
	
	List<String> queryPrmCoopShareFields(Integer orgId,Integer coopId);
	List<String> queryPrmCoopShareFieldsTwo(Integer orgId,Integer coopId);
	AfOrderShare afOrderShareInfo(Integer orgId,Integer coopId,Integer orderId,String type);
	void shareInbound(AfOrderShare bean);
	boolean afOrderShareCheckOrder(Integer orderId);
	boolean checkShareScope(Integer orderId,String shareScope,String orderUuid);
	void shareOrderFiles(AfOrderShare bean);
	void shareWayBillMake(AfOrderShare bean);

}
