package com.efreight.afbase.service;

import java.util.List;
import java.util.Map;

import com.efreight.afbase.entity.CoopProject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;


public interface CoopProjectService extends IService<CoopProject> {

	IPage<CoopProject> getListPage(Page page, CoopProject bean);
	
	Boolean doSave(CoopProject bean);
	Boolean doUpdate(CoopProject bean);
	Boolean doStop(CoopProject bean);
	Boolean doLock(CoopProject bean);
	Boolean doUnLock(CoopProject bean);
	Boolean doOpenTime(CoopProject bean);
	List<Map<String, Object>> selectCurrency();
}
