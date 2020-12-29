package com.efreight.ws.afbase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.ws.afbase.entity.WSOrder;
import com.efreight.ws.afbase.pojo.order.create.CreateOrderRequest;
import com.efreight.ws.afbase.pojo.order.create.WSCreateOrderResponse;
import com.efreight.ws.afbase.pojo.order.detail.WSDetailOrderResponse;
import com.efreight.ws.afbase.pojo.order.edit.EditOrderRequest;
import com.efreight.ws.afbase.pojo.order.edit.WSEditOrderResponse;
import com.efreight.ws.afbase.pojo.order.edit.inbound.EditInboundOrderRequest;
import com.efreight.ws.afbase.pojo.order.edit.inbound.WSEditInboundOrderResponse;
import com.efreight.ws.afbase.pojo.order.inbound.InboundOrderRequest;
import com.efreight.ws.afbase.pojo.order.inbound.WSInboundOrderResponse;
import com.efreight.ws.afbase.pojo.order.list.ListOrderRequest;
import com.efreight.ws.afbase.pojo.order.list.WSListOrderResponse;

public interface WSAEOrderService extends IService<WSOrder> {
    /**
     * 创建订单服务
     * @param orgId 企业ID
     * @param orderRequest 订单信息
     * @return
     */
    WSCreateOrderResponse createOrder(Integer orgId, CreateOrderRequest orderRequest);

    /**
     * 订单出重
     * @param orgId 企业ID
     * @param orderRequest 订单信息
     * @return
     */
    WSInboundOrderResponse inboundOrder(Integer orgId, InboundOrderRequest orderRequest);

    /**
     * 订单查询
     * @param orgId 企业ID
     * @param orderRequest 订单信息
     * @return
     */
    WSListOrderResponse listOrder(Integer orgId, ListOrderRequest orderRequest);

    /**
     * 订单详情
     * @param orgId 企业ID
     * @param orderCode 订单号
     * @return
     */
    WSDetailOrderResponse detailOrder(Integer orgId, String orderCode);

    /**
     * 编辑订单
     * @param orgId 企业ID
     * @param orderRequest 请求信息
     * @return
     */
    WSEditOrderResponse editOrder(Integer orgId, EditOrderRequest orderRequest);

    /**
     * 编辑出重
     * @param orgId 企业ID
     * @param orderRequest 请求数据
     * @return
     */
    WSEditInboundOrderResponse editInboundOrder(Integer orgId, EditInboundOrderRequest orderRequest);


}
