package com.efreight.sc.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.entity.Order;
import com.efreight.sc.entity.view.OrderTrackVO;
import com.efreight.sc.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * CS 订单管理 SI订单 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-02
 */
@RestController
@RequestMapping("/scOrder")
@AllArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * si-分页查询
     * @param page
     * @param order
     * @return
     */
    @GetMapping("/si")
    public MessageInfo getSIPage(Page page, Order order) {
        try {
            IPage result = orderService.getSIPage(page, order);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * si-合计
     * @param order
     * @return
     */
    @GetMapping("/si/total")
    public MessageInfo getSITotal(Order order){
        try {
            Order total = orderService.getSITotal(order);
            return MessageInfo.ok(total);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * se-分页查询
     * @param page
     * @param order
     * @return
     */
    @GetMapping("/se")
    public MessageInfo getSEPage(Page page, Order order) {
        try {
        	order.setOrgId(SecurityUtils.getUser().getOrgId());
            IPage result = orderService.getSEPage(page, order);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * se-合计
     * @param order
     * @return
     */
    @GetMapping("/se/total")
    public MessageInfo getSETotal(Order order){
        try {
            Order total = orderService.getSETotal(order);
            return MessageInfo.ok(total);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * 查看订单详情
     * @param orderId
     * @return
     */
    @GetMapping("/{orderId}")
    public MessageInfo view(@PathVariable("orderId") Integer orderId){
        try {
            Order order = orderService.view(orderId);
            return MessageInfo.ok(order);
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * si-订单新建
     * @param order
     * @return
     */
    @PostMapping("/si")
    @PreAuthorize("@pms.hasPermission('si_order_save')")
    public MessageInfo saveSI(@RequestBody Order order){
        try {
            orderService.insertSI(order);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * si-修改订单
     * @param order
     * @return
     */
    @PutMapping("/si")
    @PreAuthorize("@pms.hasPermission('si_order_edit')")
    public MessageInfo updateSI(@RequestBody Order order){
        try {
            orderService.modifySI(order);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * si-强制关闭
     * @param orderId
     * @return
     */
    @PutMapping("/si/forceStop/{orderId}")
    @PreAuthorize("@pms.hasPermission('si_order_specialHandle_forceStop')")
    public MessageInfo forceStopSI(@PathVariable("orderId") Integer orderId, @RequestBody String reason){
        try {
            orderService.forceStopSI(orderId, reason);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * SE-订单新建
     * @param order
     * @return
     */
    @PostMapping("/se")
    @PreAuthorize("@pms.hasPermission('se_order_save')")
    public MessageInfo saveSE(@RequestBody Order order){
        try {
            orderService.insertSE(order);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * SE-修改订单
     * @param order
     * @return
     */
    @PutMapping("/se")
    @PreAuthorize("@pms.hasPermission('se_order_edit')")
    public MessageInfo updateSE(@RequestBody Order order){
        try {
            orderService.modifySE(order);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * SE-强制关闭
     * @param orderId
     * @return
     */
    @PutMapping("/se/forceStop/{orderId}")
    @PreAuthorize("@pms.hasPermission('se_order_specialHandle_forceStop')")
    public MessageInfo forceStopSE(@PathVariable("orderId") Integer orderId, @RequestBody String reason){
        try {
            orderService.forceStopSE(orderId, reason);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 导出SE订单列表
     *
     * @param
     * @return
     */
    @PostMapping("/exportExcelListSe")
    @PreAuthorize("@pms.hasPermission('se_order_export')")
    public void exportExcelListSe(Order order) {
        try {
            orderService.exportExcelListSe(order);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    /**
     * 导出SI订单列表
     *
     * @param
     * @return
     */
    @PostMapping("/exportExcelListSi")
    @PreAuthorize("@pms.hasPermission('si_order_export')")
    public void exportExcelListSi(Order order) {
        try {
            orderService.exportExcelListSi(order);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    /**
     * 订单轨迹信息
     * @param orderUUID 订单ID
     * @return
     */
    @GetMapping(value = "/orderTrack/{orderUUID}")
    public MessageInfo orderTrack(@PathVariable("orderUUID") String orderUUID){
        try{
            OrderTrackVO orderTrack = orderService.getOrderTrack(orderUUID);
            return MessageInfo.ok(orderTrack);
        }catch (Exception e){
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * SE订单 分提单制作
     *
     * @param orderId
     * @param businessScope
     * @return
     */
    @PostMapping("/printHawMake/{orderId}/{businessScope}")
    public MessageInfo printHawMake(@PathVariable Integer orderId, @PathVariable String businessScope) {
        try {
            return MessageInfo.ok(orderService.printHawMake(orderId, businessScope));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * SE订单 订舱托书打印
     *
     * @param orderId
     * @param businessScope
     * @return
     */
    @PostMapping("/printOrderLetter/{orderId}/{businessScope}")
    public MessageInfo printOrderLetter(@PathVariable Integer orderId, @PathVariable String businessScope) {
        try {
            return MessageInfo.ok(orderService.printOrderLetter(orderId, businessScope));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * SE订单 分提单制作 导出
     *
     * @param orderId
     * @param businessScope
     * @return
     */
    @PostMapping("/exportHawMakeExcel/{orderId}/{businessScope}")
    public void exportHawMakeExcel(@PathVariable Integer orderId, @PathVariable String businessScope) {
        try {
            orderService.exportHawMakeExcel(orderId, businessScope);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * SE订单 订舱托书 导出
     *
     * @param orderId
     * @param businessScope
     * @return
     */
    @PostMapping("/exportOrderLetterExcel/{orderId}/{businessScope}")
    public void exportOrderLetterExcel(@PathVariable Integer orderId, @PathVariable String businessScope) {
        try {
            orderService.exportOrderLetterExcel(orderId, businessScope);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * SE订单 拖车委托书 导出
     *
     * @param orderId
     * @param businessScope
     * @return
     */
    @PostMapping("/exportTrailerPrintExcel/{orderId}/{businessScope}")
    public void exportTrailerPrintExcel(@PathVariable Integer orderId, @PathVariable String businessScope) {
        try {
            orderService.exportTrailerPrintExcel(orderId, businessScope);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * SE订单 到货通知 导出
     *
     * @param orderId
     * @param businessScope
     * @return
     */
    @PostMapping("/exportNoticeArrivalExcel/{orderId}/{businessScope}")
    public void exportNoticeArrivalExcel(@PathVariable Integer orderId, @PathVariable String businessScope) {
        try {
            orderService.exportNoticeArrivalExcel(orderId, businessScope);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

}

