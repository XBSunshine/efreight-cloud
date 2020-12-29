package com.efreight.sc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.sc.entity.OrderDeliveryNotice;
import com.efreight.sc.entity.ScWarehouse;
import com.efreight.sc.entity.TcWarehouse;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * TC 基础信息 堆场仓库 服务类
 * </p>
 *
 * @author caiwd
 * @since 2020-07-14
 */
public interface TcWarehouseService extends IService<TcWarehouse> {

	IPage getPage(Page page, TcWarehouse bean);

	List<TcWarehouse> getList(TcWarehouse bean);

	boolean saveWarehouse(TcWarehouse bean);

	boolean modifyWarehouse(TcWarehouse bean);

	OrderDeliveryNotice getOrderDeliveryNotice(String orderUUID);
}
