package com.efreight.sc.controller;


import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.VlEntryOrder;
import com.efreight.sc.entity.VlVehicleEntryOrder;
import com.efreight.sc.service.VlEntryOrderService;

import lombok.AllArgsConstructor;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author qipm
 * @since 2021-01-18
 */
@RestController
@AllArgsConstructor
@RequestMapping("/vl-entry-order")
public class VlEntryOrderController {
	private final VlEntryOrderService service;
	
	@GetMapping("/getListPage")
    public MessageInfo getListPage(VlVehicleEntryOrder bean) {
        return MessageInfo.ok(service.getListPage(bean));
    }
	
    @PostMapping(value = "/doSave")
    public MessageInfo doSave(@Valid @RequestBody VlEntryOrder bean) {
        try {
            return MessageInfo.ok(service.doSave(bean));
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }
    @PostMapping(value = "/doUpdate")
    public MessageInfo doUpdate(@Valid @RequestBody VlEntryOrder bean) {
    	try {
    		return MessageInfo.ok(service.doUpdate(bean));
    	} catch (Exception e) {
    		e.printStackTrace();
    		return MessageInfo.failed(e.getMessage());
    	}
    }
    
    @GetMapping(value="/view/{id}")
    public MessageInfo<VlEntryOrder> getOrderById(@PathVariable Integer id) {
        return MessageInfo.ok(service.getOrderById(id));
    }
    @GetMapping(value="/getVlOrder/{id}")
    public MessageInfo<VlEntryOrder> getVlOrder(@PathVariable Integer id) {
    	return MessageInfo.ok(service.getVlOrder(id));
    }
    
    @GetMapping(value="/getVlOrderDetail/{id}/{flag}")
    public MessageInfo getVlOrderDetail(@PathVariable Integer id,@PathVariable String flag) {
        return MessageInfo.ok(service.getVlOrderDetail(id,flag));
    }
    
    @PostMapping("/doPrintVlOrder")
    public MessageInfo doPrintVlOrder(HttpServletRequest request) {
        return MessageInfo.ok(service.doPrintVlOrder(Integer.parseInt(request.getParameter("orgId")), request.getParameter("entryOrderId"), request.getParameter("userId")));
    }
    @PostMapping("/doPrintVlOrder1")
    public MessageInfo doPrintVlOrder1(@Valid @RequestBody VlEntryOrder bean) {
    	return MessageInfo.ok(service.doPrintVlOrder1(bean));
    }
}

