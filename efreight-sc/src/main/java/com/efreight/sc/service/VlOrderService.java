package com.efreight.sc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.remoteVo.OrderForVL;
import com.efreight.sc.entity.VlOrder;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * VL 订单管理 派车订单 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-31
 */
public interface VlOrderService extends IService<VlOrder> {

    IPage getPage(Page page, VlOrder vlOrder);

    VlOrder view(Integer orderId);

    void insert(VlOrder vlOrder);

    void modify(VlOrder vlOrder);

    void delete(Integer orderId);

    void stop(Integer orderId, String reason, String rowUuid);

    VlOrder total(VlOrder vlOrder);

    VlOrder viewForFinishOrder(Integer orderId);

    void finish(VlOrder vlOrder);

    List<OrderForVL> getOrderList(OrderForVL orderForVL);

    void exportExcel(VlOrder vlOrder);
}
