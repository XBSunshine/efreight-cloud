package com.efreight.sc.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.IoOrder;
import com.efreight.sc.service.IoOrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * IO 订单管理 其他业务订单 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
@RestController
@RequestMapping("/ioOrder")
@AllArgsConstructor
@Slf4j
public class IoOrderController {

    private final IoOrderService ioOrderService;

    /**
     * 分页查询
     *
     * @param page
     * @param ioOrder
     * @return
     */
    @GetMapping
    public MessageInfo page(Page page, IoOrder ioOrder) {
        try {
            IPage result = ioOrderService.getPage(page, ioOrder);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 合计
     *
     * @param ioOrder
     * @return
     */
    @GetMapping("/total")
    public MessageInfo total(IoOrder ioOrder) {
        try {
            IoOrder total = ioOrderService.total(ioOrder);
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
    public MessageInfo<IoOrder> view(@PathVariable("orderId") Integer orderId) {
        try {
            IoOrder ioOrder = ioOrderService.view(orderId);
            return MessageInfo.ok(ioOrder);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }


    /**
     * 新增
     *
     * @param ioOrder
     * @return
     */
    @PostMapping
    @PreAuthorize("@pms.hasPermission('io_order_save')")
    public MessageInfo save(@RequestBody IoOrder ioOrder) {
        try {
            ioOrderService.insert(ioOrder);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 修改
     *
     * @param ioOrder
     * @return
     */
    @PutMapping
    @PreAuthorize("@pms.hasPermission('io_order_edit')")
    public MessageInfo modify(@RequestBody IoOrder ioOrder) {
        try {
            ioOrderService.modify(ioOrder);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
            ioOrderService.delete(orderId);
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
            ioOrderService.incomeComplete(orderId);
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
            ioOrderService.costComplete(orderId);
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
    @PreAuthorize("@pms.hasPermission('io_order_specialHandle_forceStop')")
    @PutMapping("/forceStop")
    public MessageInfo forceStop(@RequestBody Map<String, String> param) {
        try {
            ioOrderService.forceStop(param.get("reason"), param.get("orderId"));
            return MessageInfo.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }


    /**
     * 导出Excel
     *
     * @param ioOrder
     */
    @PostMapping("/exportExcel")
    public void exportExcel(IoOrder ioOrder) {
        try {
            ioOrderService.exportExcel(ioOrder);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}

