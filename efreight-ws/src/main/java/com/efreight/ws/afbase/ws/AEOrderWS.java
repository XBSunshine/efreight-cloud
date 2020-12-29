package com.efreight.ws.afbase.ws;

import com.efreight.ws.afbase.contant.AFConstant;
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
import com.efreight.ws.common.pojo.WSException;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService(name = "AEOrderWS", targetNamespace = AFConstant.ORDER_NAMESPACE)
public interface AEOrderWS {
    /**
     * AE-订单创建服务
     * @param orderRequest 请求数据
     * @return
     */
    @WebMethod
    @WebResult(name = "wsCreateOrderResponse")
    WSCreateOrderResponse createOrder(@WebParam(name = "createOrderRequest") CreateOrderRequest orderRequest);

    /**
     * AE-订单出重服务
     * @param inboundRequest 请求数据
     * @return
     */
    @WebMethod
    @WebResult(name = "wsInboundOrderResponse")
    WSInboundOrderResponse inboundOrder(@WebParam(name = "inboundOrderRequest") InboundOrderRequest inboundRequest);

    /**
     * AE-订单查询服务
     * @param listOrderRequest 请求数据
     * @return
     * @throws WSException
     */
    @WebMethod
    @WebResult(name = "wsListOrderResponse ")
    WSListOrderResponse listOrder(@WebParam(name = "listOrderRequest") ListOrderRequest listOrderRequest) throws WSException;

    /**
     * AE-订单详情服务
     * @param orderCode 订单号
     * @return
     */
    @WebMethod
    @WebResult(name = "wsDetailOrderResponse")
    WSDetailOrderResponse detailOrder(@WebParam(name = "orderCode") String orderCode);

    /**
     * AE-编辑订单服务
     * @param orderRequest 请求数据
     * @return
     */
    @WebMethod
    @WebResult(name = "wsEditOrderResponse")
    WSEditOrderResponse editOrder(@WebParam(name = "editOrderRequest") EditOrderRequest orderRequest);

    /**
     * AE-编辑订单出重服务
     * @param orderRequest 请求数据
     * @return
     */
    @WebMethod
    @WebResult(name = "wsEditInboundOrderResponse")
    WSEditInboundOrderResponse editInboundOrder(@WebParam(name = "editInboundOrderRequest") EditInboundOrderRequest orderRequest);
}
