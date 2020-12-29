package com.efreight.sc.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.entity.Order;
import com.efreight.sc.entity.TcOrder;
import com.efreight.sc.service.TcOrderService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * TC 订单管理 TE、TI 订单 前端控制器
 * </p>
 *
 * @author caiwd
 * @since 2020-07-13
 */
@RestController
@RequestMapping("/tcOrder")
@AllArgsConstructor
@Slf4j
public class TcOrderController {
	
	private final TcOrderService tcOrderService;
	
	 /**
     * te-分页查询
     * @param page
     * @param order
     * @return
     */
    @GetMapping("/te/page")
    public MessageInfo getTEPage(Page page, TcOrder order) {
        try {
        	order.setOrgId(SecurityUtils.getUser().getOrgId());
            order.setCurrentUserId(SecurityUtils.getUser().getId());
            IPage result = tcOrderService.getTEPage(page, order);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    @GetMapping("/te/total")
    public MessageInfo getTETotal(TcOrder order) {
        try {
        	order.setOrgId(SecurityUtils.getUser().getOrgId());
            order.setCurrentUserId(SecurityUtils.getUser().getId());
            TcOrder result = tcOrderService.getTETotal(order);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    @GetMapping("/{orderId}")
    public MessageInfo getView(@PathVariable("orderId") Integer orderId) {
        try {
            TcOrder order = tcOrderService.view(orderId);
            return MessageInfo.ok(order);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
	  /**
     * TE-订单新建
     * @param order
     * @return
     */
    @PostMapping("/te")
    public MessageInfo saveTE(@RequestBody TcOrder order){
        try {
        	tcOrderService.saveTE(order);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    
    /**
     * TE-强制关闭
     * @param orderId
     * @return
     */
    @PutMapping("/te/forceStop/{orderId}")
    public MessageInfo forceStopTE(@PathVariable("orderId") Integer orderId, @RequestBody String reason){
        try {
        	tcOrderService.forceStopTE(orderId, reason);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * TE-修改订单
     * @param order
     * @return
     */
    @PostMapping("/te/modify")
    public MessageInfo updateSE(@RequestBody TcOrder order){
        try {
            tcOrderService.modifyTE(order);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    
    /**
     * 导出TE订单列表
     *
     * @param
     * @return
     */
    @PostMapping("/exportExcelListTe")
    public void exportExcelListTe(TcOrder order) {
        try {
            order.setCurrentUserId(SecurityUtils.getUser().getId());
            tcOrderService.exportExcelListTe(order);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

}

