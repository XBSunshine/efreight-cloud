package com.efreight.ws.afbase.ws.impl;


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
import com.efreight.ws.afbase.service.WSAEOrderService;
import com.efreight.ws.afbase.ws.AEOrderWS;
import com.efreight.ws.common.annotation.EFWSAuthorize;
import com.efreight.ws.common.contant.EFConstant;
import com.efreight.ws.common.pojo.WSException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.validation.executable.ValidateOnExecution;
import javax.xml.ws.WebServiceContext;
import java.util.Map;

@WebService(serviceName = "AEOrderWS",
        targetNamespace = AFConstant.ORDER_NAMESPACE,
        endpointInterface = "com.efreight.ws.afbase.ws.AEOrderWS")
@Slf4j
public class AEOrderWSImpl implements AEOrderWS {


    @Resource
    private WebServiceContext context;
    @Resource
    private WSAEOrderService wsOrderService;

    @Override
    @EFWSAuthorize("AE_ORDER_CREATE")
    public WSCreateOrderResponse createOrder(CreateOrderRequest orderRequest) {
        Integer orgId = getOrgId();
        WSCreateOrderResponse createOrderResponse;
        try{
            createOrderResponse = wsOrderService.createOrder(orgId, orderRequest);
        }catch (Exception e){
            createOrderResponse = new WSCreateOrderResponse();
            createOrderResponse.setCode(500);
            if(e instanceof WSException){
                WSException wsException = (WSException)e;
                createOrderResponse.setMessage(wsException.getMessage());
                createOrderResponse.setBusinessCode(wsException.getCode());
            }else{
                log.error(e.getMessage());
                createOrderResponse.setMessage("服务异常");
                createOrderResponse.setBusinessCode(-1);
            }
        }
        return createOrderResponse;
    }

    @Override
    @EFWSAuthorize("AE_ORDER_WEIGHT")
    public WSInboundOrderResponse inboundOrder(InboundOrderRequest inboundRequest) {
        Integer orgId = getOrgId();
        WSInboundOrderResponse wsInboundOrderResponse;
        try{
            wsInboundOrderResponse = wsOrderService.inboundOrder(orgId, inboundRequest);
        }catch (Exception e){
            wsInboundOrderResponse = new WSInboundOrderResponse();
            wsInboundOrderResponse.setCode(500);
            if(e instanceof WSException){
                wsInboundOrderResponse.setMessage(e.getMessage());
                wsInboundOrderResponse.setBusinessCode(((WSException) e).getCode());
            }else{
                log.error(e.getMessage());
                wsInboundOrderResponse.setMessage("服务异常");
                wsInboundOrderResponse.setBusinessCode(-1);
            }
        }
        return wsInboundOrderResponse;
    }

    @Override
    @EFWSAuthorize("AE_ORDER_LIST")
    @ValidateOnExecution
    public WSListOrderResponse listOrder(ListOrderRequest listOrderRequest) throws WSException {
        WSListOrderResponse wsListOrderResponse;
        Integer orgId = getOrgId();
        try{
            wsListOrderResponse = wsOrderService.listOrder(orgId, listOrderRequest);
        }catch (Exception e){
            wsListOrderResponse = new WSListOrderResponse();
            wsListOrderResponse.setCode(500);
            if(e instanceof WSException){
                wsListOrderResponse.setMessage(e.getMessage());
                wsListOrderResponse.setBusinessCode(((WSException) e).getCode());
            }else{
                log.error(e.getMessage());
                wsListOrderResponse.setMessage("服务异常");
                wsListOrderResponse.setBusinessCode(-1);
            }
        }

        return wsListOrderResponse;
    }

    @Override
    @EFWSAuthorize("AE_ORDER_DETAIL")
    public WSDetailOrderResponse detailOrder(String orderCode) {
        WSDetailOrderResponse response;
        try{
            Integer orgId = getOrgId();
            response = wsOrderService.detailOrder(orgId, orderCode);
            response.setCode(200);
            response.setMessage("操作成功");
        }catch (Exception e){
            response = new WSDetailOrderResponse();
            response.setCode(500);
            if(e instanceof WSException){
                response.setMessage(e.getMessage());
                response.setBusinessCode(((WSException) e).getCode());
            }else{
                log.error(e.getMessage());
                response.setMessage("服务异常");
                response.setBusinessCode(-1);
            }
        }
        return response;
    }

    @Override
    @EFWSAuthorize("AE_ORDER_EDIT")
    public WSEditOrderResponse editOrder(EditOrderRequest orderRequest) {
        WSEditOrderResponse response;
        try{
            Integer orgId = getOrgId();
            response = wsOrderService.editOrder(orgId, orderRequest);
            response.setCode(200);
            response.setMessage("操作成功");
        }catch (Exception e){
            response = new WSEditOrderResponse();
            response.setCode(500);
            if(e instanceof WSException){
                response.setMessage(e.getMessage());
                response.setBusinessCode(((WSException) e).getCode());
            }else{
                log.error(e.getMessage());
                response.setMessage("服务异常");
                response.setBusinessCode(-1);
            }
        }
        return response;
    }

    @Override
    @EFWSAuthorize("AE_ORDER_EDIT_WEIGHT")
    public WSEditInboundOrderResponse editInboundOrder(EditInboundOrderRequest orderRequest) {
        WSEditInboundOrderResponse response;
        try{
            Integer orgId = getOrgId();
            response = wsOrderService.editInboundOrder(orgId, orderRequest);
            response.setCode(200);
            response.setMessage("操作成功");
        }catch (Exception e){
            response = new WSEditInboundOrderResponse();
            response.setCode(500);
            if(e instanceof WSException){
                response.setMessage(e.getMessage());
                response.setBusinessCode(((WSException) e).getCode());
            }else{
                log.error(e.getMessage());
                response.setMessage("服务异常");
                response.setBusinessCode(-1);
            }
        }
        return response;
    }

    private Integer getOrgId(){
        Map<String, Object> headers = context.getMessageContext();
        String orgId = headers.getOrDefault(EFConstant.KEY_ORG_ID, "").toString();
        if(StringUtils.isEmpty(orgId)){
            return null;
        }
        return Integer.valueOf(orgId);
    }
}
