package com.efreight.sc.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.sc.entity.OrderDeliveryNotice;
import com.efreight.sc.entity.ScWarehouse;

public interface ScWarehouseService extends IService<ScWarehouse>{
	
	IPage getPage(Page page, ScWarehouse bean);
	boolean saveWarehouse(ScWarehouse bean);
	boolean modifyWarehouse(ScWarehouse bean);
	List<ScWarehouse> getList(ScWarehouse bean);
	OrderDeliveryNotice getOrderDeliveryNotice(String uuid);
}
