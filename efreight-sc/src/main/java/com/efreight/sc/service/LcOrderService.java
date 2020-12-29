package com.efreight.sc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.remoteVo.OrderForVL;
import com.efreight.sc.entity.LcOrder;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * LC 订单管理 LC陆运订单 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
public interface LcOrderService extends IService<LcOrder> {

    IPage getPage(Page page, LcOrder lcOrder);

    LcOrder total(LcOrder lcOrder);

    void insert(LcOrder lcOrder);

    void modify(LcOrder lcOrder);

    void delete(Integer orderId);

    LcOrder view(Integer orderId);

    void incomeComplete(Integer orderId);

    void costComplete(Integer orderId);

    void forceStop(String reason, String orderId);

    void exportExcel(LcOrder lcOrder);

    List<OrderForVL> getLCOrderListForVL(OrderForVL orderForVL);
}
