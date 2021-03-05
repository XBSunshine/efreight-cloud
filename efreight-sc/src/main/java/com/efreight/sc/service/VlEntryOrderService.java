package com.efreight.sc.service;

import com.efreight.sc.entity.VlEntryOrder;
import com.efreight.sc.entity.VlEntryOrderDetail;
import com.efreight.sc.entity.VlVehicleEntryOrder;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author qipm
 * @since 2021-01-18
 */
public interface VlEntryOrderService extends IService<VlEntryOrder> {

	List<VlEntryOrder> getListPage(VlVehicleEntryOrder bean);
	Boolean doSave(VlEntryOrder bean);
	Boolean doUpdate(VlEntryOrder bean);
	VlEntryOrder getOrderById(Integer entryOrderId);
	VlEntryOrder getVlOrder(Integer entryOrderId);
	List<VlEntryOrderDetail> getVlOrderDetail(Integer vlOrderId,String flag);
	
	Boolean doPrintVlOrder(Integer orgId,String entryOrderId,String userId);
	String doPrintVlOrder1(VlEntryOrder bean);

}
