package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.TactPublic;

import java.util.List;

public interface TactPublicService extends IService<TactPublic> {
	IPage<TactPublic> getListPage(Page page, TactPublic bean);
	void deleteTactById(Integer tactId);
	int saveTack(TactPublic tact);
	List<TactPublic> queryListForExcel(TactPublic bean);
}
