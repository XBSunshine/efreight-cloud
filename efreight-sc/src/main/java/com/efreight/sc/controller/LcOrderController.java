package com.efreight.sc.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.LcOrder;
import com.efreight.sc.service.LcOrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * LC 订单管理 LC陆运订单 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
@RestController
@RequestMapping("/lcOrder")
@AllArgsConstructor
@Slf4j
public class LcOrderController {
    private final LcOrderService lcOrderService;

    /**
     * 分页查询
     *
     * @param page
     * @param lcOrder
     * @return
     */
    @GetMapping
    public MessageInfo page(Page page, LcOrder lcOrder) {
        try {
            IPage result = lcOrderService.getPage(page, lcOrder);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 合计
     *
     * @param lcOrder
     * @return
     */
    @GetMapping("/total")
    public MessageInfo total(LcOrder lcOrder) {
        try {
            LcOrder total = lcOrderService.total(lcOrder);
            return MessageInfo.ok(total);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看详情
     *
     * @param orderId
     * @return
     */
    @GetMapping("/{orderId}")
    public MessageInfo<LcOrder> view(@PathVariable("orderId") Integer orderId) {
        try {
            LcOrder lcOrder = lcOrderService.view(orderId);
            return MessageInfo.ok(lcOrder);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }


    /**
     * 新增
     *
     * @param lcOrder
     * @return
     */
    @PostMapping
    @PreAuthorize("@pms.hasPermission('lc_order_save')")
    public MessageInfo save(@RequestBody LcOrder lcOrder) {
        try {
            lcOrderService.insert(lcOrder);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 修改
     *
     * @param lcOrder
     * @return
     */
    @PutMapping
    @PreAuthorize("@pms.hasPermission('lc_order_edit')")
    public MessageInfo modify(@RequestBody LcOrder lcOrder) {
        try {
            lcOrderService.modify(lcOrder);
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
    @DeleteMapping
    public MessageInfo delete(@PathVariable("orderId") Integer orderId) {
        try {
            lcOrderService.delete(orderId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 收入完成
     *
     * @param orderId
     * @return
     */
    @PutMapping("/incomeComplete/{orderId}")
    public MessageInfo incomeComplete(@PathVariable("orderId") Integer orderId) {
        try {
            lcOrderService.incomeComplete(orderId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 成本完成
     *
     * @param orderId
     * @return
     */
    @PutMapping("/costComplete/{orderId}")
    public MessageInfo costComplete(@PathVariable("orderId") Integer orderId) {
        try {
            lcOrderService.costComplete(orderId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 强制关闭
     *
     * @param param
     * @return
     */
    @PreAuthorize("@pms.hasPermission('lc_order_specialHandle_forceStop')")
    @PutMapping("/forceStop")
    public MessageInfo forceStop(@RequestBody Map<String, String> param) {
        try {
            lcOrderService.forceStop(param.get("reason"), param.get("orderId"));
            return MessageInfo.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }


    /**
     * 导出Excel
     *
     * @param lcOrder
     */
    @PostMapping("/exportExcel")
    public void exportExcel(LcOrder lcOrder) {
        try {
            lcOrderService.exportExcel(lcOrder);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}

