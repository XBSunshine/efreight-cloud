package com.efreight.sc.service;

import com.efreight.common.remoteVo.OrderForVL;
import com.efreight.sc.entity.TcOrder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * TC 订单管理 TE、TI 订单 服务类
 * </p>
 *
 * @author caiwd
 * @since 2020-07-13
 */
public interface TcOrderService extends IService<TcOrder> {
	
	void saveTE(TcOrder order);
	IPage getTEPage(Page page, TcOrder order);
	TcOrder getTETotal(TcOrder order);
	TcOrder view(Integer orderId);
	void forceStopTE(Integer orderId,String reason);
	void modifyTE(TcOrder order);
	void exportExcelListTe(TcOrder order);

    List<OrderForVL> getTCOrderListForVL(OrderForVL orderForVL);
}
