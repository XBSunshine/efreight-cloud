package com.efreight.afbase.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.RountingSign;
import com.efreight.afbase.service.AfOrderService;
import com.efreight.afbase.service.RountingSignService;
import com.efreight.common.security.util.MessageInfo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * AF 订单管理 出口订单 签单表 前端控制器
 * </p>
 *
 * @author cwd
 * @since 2020-11-18
 */
@RestController
@AllArgsConstructor
@RequestMapping("/rountingsign")
@Slf4j
public class RountingSignController {
	private final RountingSignService rountingSignService;
	
	/**
	 * 校验 当前订单是否可以航线签单
	 * @param bean
	 * @return
	 */
	@GetMapping("/check/{orderId}")
    public MessageInfo checkOrderCost(@PathVariable Integer orderId) {
		RountingSign bean = new RountingSign();
		bean.setOrderId(orderId);
		try {
			return MessageInfo.ok(rountingSignService.checkOrderCost(bean));
		} catch (Exception e) {
			e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
		}
    }
	
	/**
	 * 订单撤销航线签单
	 * @param bean
	 * @return
	 */
	@GetMapping("/concelSign/{orderId}")
    public MessageInfo concelSign(@PathVariable Integer orderId) {
		RountingSign bean = new RountingSign();
		bean.setOrderId(orderId);
		try {
			return MessageInfo.ok(rountingSignService.concelSign(bean));
		} catch (Exception e) {
			e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
		}
    }
	
	
	
	@GetMapping("/view/{orderId}/{businessScope}")
    public MessageInfo getRountingSign(@PathVariable Integer orderId,@PathVariable String businessScope) {
		RountingSign bean = new RountingSign();
		bean.setOrderId(orderId);
		bean.setBusinessScope(businessScope);
		try {
			return MessageInfo.ok(rountingSignService.getRountingSign(bean));
		} catch (Exception e) {
			e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
		}
    }
	
	
   @PostMapping(value = "/modify")
   public MessageInfo saveOrModify(@RequestBody RountingSign bean) {
	   try {
		   rountingSignService.saveOrModify(bean);
		   return MessageInfo.ok();
	   } catch (Exception e) {
			e.printStackTrace();
	        return MessageInfo.failed(e.getMessage());
	   }
    }
	
   /**
	 * 校验 当前订单 是否可以做成本收入完成(目前只有AE使用)
	 * @param bean
	 * @return
	 */
	@GetMapping("/check/costRecord/{orderId}")
   public MessageInfo checkCostRecord(@PathVariable Integer orderId) {
		RountingSign bean = new RountingSign();
		bean.setOrderId(orderId);
		try {
			return MessageInfo.ok(rountingSignService.checkCostRecord(bean));
		} catch (Exception e) {
			e.printStackTrace();
           return MessageInfo.failed(e.getMessage());
		}
   }
   
	

}

