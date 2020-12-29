package com.efreight.afbase.service;

import java.util.List;
import java.util.Map;

import com.efreight.afbase.entity.CoopProjectContacts;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;


public interface CoopProjectContactsService extends IService<CoopProjectContacts> {

	IPage<CoopProjectContacts> getListPage(Page page, CoopProjectContacts bean);
	
	List<Map<String, Object>> selectAll(CoopProjectContacts bean);
	Boolean doSave(CoopProjectContacts bean);
	Boolean doUpdate(CoopProjectContacts bean);
}
