package com.efreight.sc.controller;



import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.OrderDeliveryNotice;
import com.efreight.sc.entity.view.OrderTrackVO;
import com.efreight.sc.service.OrderService;
import com.efreight.sc.service.ScWarehouseService;

import com.efreight.sc.service.TcWarehouseService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 外部访问服务
 */
@RestController
@AllArgsConstructor
@RequestMapping("/external")
@Slf4j
public class ExternalAPIController {

    private final OrderService service;
    
    private final ScWarehouseService scWarehouseService;

    private final TcWarehouseService tcWarehouseService;

    @GetMapping(value = "/order/orderTrack")
    public MessageInfo getOrderTrack(@RequestParam("o") String orderUUID){
        try{
            //前两位为业务域
            if(StringUtils.isNotBlank(orderUUID)){
                orderUUID = orderUUID.substring(2);
            }
            OrderTrackVO orderTrack = service.getOrderTrack(orderUUID);
            return MessageInfo.ok(orderTrack);
        }catch (Exception e){
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }
    
    @GetMapping(value = "/order/orderDeliveryNotice")
    public MessageInfo orderDeliveryNotice(@RequestParam("o") String orderUUID){
        try{
            OrderDeliveryNotice orderDeliveryNotice = scWarehouseService.getOrderDeliveryNotice(orderUUID);
            return MessageInfo.ok(orderDeliveryNotice);
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping(value = "/tcOrder/orderDeliveryNotice")
    public MessageInfo tcOrderDeliveryNotice(@RequestParam("o") String orderUUID){
        try{
            OrderDeliveryNotice orderDeliveryNotice = tcWarehouseService.getOrderDeliveryNotice(orderUUID);
            return MessageInfo.ok(orderDeliveryNotice);
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    
    
}
