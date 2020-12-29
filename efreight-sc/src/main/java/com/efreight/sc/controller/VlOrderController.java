package com.efreight.sc.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.remoteVo.OrderForVL;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.VlOrder;
import com.efreight.sc.service.VlOrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * VL 订单管理 派车订单 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-31
 */
@RestController
@RequestMapping("/vlOrder")
@Slf4j
@AllArgsConstructor
public class VlOrderController {

    private final VlOrderService vlOrderService;

    /**
     * 分页查询
     *
     * @param page
     * @param vlOrder
     * @return
     */
    @GetMapping
    public MessageInfo page(Page page, VlOrder vlOrder) {
        try {
            IPage result = vlOrderService.getPage(page, vlOrder);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 合计
     *
     * @param vlOrder
     * @return
     */
    @GetMapping("/total")
    public MessageInfo total(VlOrder vlOrder) {
        try {
            VlOrder total = vlOrderService.total(vlOrder);
            return MessageInfo.ok(total);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 详情
     *
     * @param orderId
     * @return
     */
    @GetMapping("/{orderId}")
    public MessageInfo view(@PathVariable("orderId") Integer orderId) {
        try {
            VlOrder vlOrder = vlOrderService.view(orderId);
            return MessageInfo.ok(vlOrder);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 详情 for 完成订单查询使用
     *
     * @param orderId
     * @return
     */
    @GetMapping("/viewForFinishOrder/{orderId}")
    public MessageInfo viewForFinishOrder(@PathVariable("orderId") Integer orderId) {
        try {
            VlOrder vlOrder = vlOrderService.viewForFinishOrder(orderId);
            return MessageInfo.ok(vlOrder);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }


    /**
     * 新增
     *
     * @param vlOrder
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody VlOrder vlOrder) {
        try {
            vlOrderService.insert(vlOrder);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 修改
     *
     * @param vlOrder
     * @return
     */
    @PutMapping
    public MessageInfo modify(@RequestBody VlOrder vlOrder) {
        try {
            vlOrderService.modify(vlOrder);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }


    /**
     * 删除
     *
     * @param orderId
     * @return
     */
    @DeleteMapping("/{orderId}")
    public MessageInfo delete(@PathVariable("orderId") Integer orderId) {
        try {
            vlOrderService.delete(orderId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }


    /**
     * 强制关闭
     *
     * @param orderId
     * @return
     */
    @PutMapping("/stop/{orderId}/{reason}/{rowUuid}")
    public MessageInfo stop(@PathVariable("orderId") Integer orderId, @PathVariable("reason") String reason, @PathVariable("rowUuid") String rowUuid) {
        try {
            vlOrderService.stop(orderId, reason, rowUuid);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 完成订单
     *
     * @param vlOrder
     * @return
     */
    @PutMapping("/finish")
    public MessageInfo finish(@RequestBody VlOrder vlOrder) {
        try {
            vlOrderService.finish(vlOrder);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 派车单获取订单列表
     * @param orderForVL
     * @return
     */
    @GetMapping("/getOrderList")
    public MessageInfo getOrderList(OrderForVL orderForVL) {
        try {
            List<OrderForVL> list = vlOrderService.getOrderList(orderForVL);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 导出Excel
     *
     * @param vlOrder
     */
    @PostMapping("/exportExcel")
    public void exportExcel(VlOrder vlOrder) {
        try {
            vlOrderService.exportExcel(vlOrder);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}

