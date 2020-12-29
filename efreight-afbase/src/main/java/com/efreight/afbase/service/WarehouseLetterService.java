package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.WarehouseLetter;

public interface WarehouseLetterService extends IService<WarehouseLetter> {

	IPage<WarehouseLetter> getListPage(Page page, WarehouseLetter bean);
	
	Boolean doSave(WarehouseLetter bean);

	Boolean doUpdate(WarehouseLetter bean);

	WarehouseLetter getWarehouseLetter(Integer warehouseLetterId);
}
