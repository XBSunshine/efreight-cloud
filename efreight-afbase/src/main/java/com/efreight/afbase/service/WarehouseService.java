package com.efreight.afbase.service;

import com.efreight.afbase.entity.Warehouse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.WarehouseLetter;

import java.util.List;


public interface WarehouseService extends IService<Warehouse> {

	IPage<Warehouse> getListPage(Page page, Warehouse bean);
	
	Boolean doSave(Warehouse bean);
	Boolean doUpdate(Warehouse bean);

    List<Warehouse> getListByDeparture(String departureStation, String type);

    List<WarehouseLetter> findshipperTemplates(String apCode);

    /**
     * 根据数据ID删除数据
     * @param warehouseId 数据ID
     * @return
     */
    int deleteById(Integer warehouseId);

    List<Warehouse> getList(String type);
}
