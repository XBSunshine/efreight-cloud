package com.efreight.sc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.remoteVo.OrderForVL;
import com.efreight.sc.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.sc.entity.view.OrderTrackVO;
import com.itextpdf.text.DocumentException;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 * CS 订单管理 SI订单 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-02
 */
public interface OrderService extends IService<Order> {

    IPage getSIPage(Page page, Order order);
    IPage getSEPage(Page page, Order order);

    Order getSITotal(Order order);
    Order getSETotal(Order order);

    Order view(Integer orderId);

    void insertSI(Order order);
    void insertSE(Order order);

    void modifySI(Order order);
    void modifySE(Order order);

    /**
     * 强制关闭SI订单
     * @param orderId 订单ID
     * @param reason 关闭理由
     */
    void forceStopSI(Integer orderId, String reason);

    /**
     * 强制关闭SE订单
     * @param orderId 订单ID
     * @param reason 关闭理由
     */
    void forceStopSE(Integer orderId, String reason);

    void exportExcelListSe(Order order);

    void exportExcelListSi(Order order);

    /**
     * 查询订单轨迹信息
     * @param orderUUID 订单UUID
     * @return
     */
    OrderTrackVO getOrderTrack(String orderUUID);

    String printHawMake(Integer orderId,String businessScope) throws IOException, DocumentException;

    void exportHawMakeExcel(Integer orderId, String businessScope);
	String printOrderLetter(Integer orderId, String businessScope) throws IOException, DocumentException;
	void exportOrderLetterExcel(Integer orderId, String businessScope);

    List<OrderForVL> getSCOrderListForVL(OrderForVL orderForVL);

    void exportTrailerPrintExcel(Integer orderId, String businessScope);

    void exportNoticeArrivalExcel(Integer orderId, String businessScope);
}
