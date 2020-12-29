package com.efreight.afbase.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.OrderLockOrUnlock;
import com.efreight.afbase.service.OrderLockOrUnlockService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/orderLockOrUnlock")
@AllArgsConstructor
@Slf4j
public class OrderLockOrUnlockController {

    private final OrderLockOrUnlockService orderLockOrUnlockService;

    /**
     * 财务锁账列表查询
     *
     * @param page
     * @param orderLockOrUnlock
     * @return
     */
    @GetMapping
    public MessageInfo page(Page page, OrderLockOrUnlock orderLockOrUnlock) {
        try {
            IPage result = orderLockOrUnlockService.page(page, orderLockOrUnlock);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 锁账
     *
     * @param orderIds
     * @return
     */
    @PutMapping("/lock/{businessScope}/{orderIds}/{lockDate}")
    public MessageInfo lock(@PathVariable("orderIds") String orderIds, @PathVariable("businessScope") String businessScope, @PathVariable("lockDate") LocalDateTime lockDate) {
        try {
            orderLockOrUnlockService.lock(orderIds, businessScope, lockDate);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 解锁
     *
     * @param orderId
     * @return
     */
    @PutMapping("/unlock/{businessScope}/{orderId}")
    public MessageInfo unlock(@PathVariable("orderId") Integer orderId, @PathVariable("businessScope") String businessScope) {
        try {
            orderLockOrUnlockService.unlock(orderId, businessScope);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 退回收入完成
     *
     * @param orderId
     * @return
     */
    @PutMapping("/rollBackToFinishIncome/{businessScope}/{orderId}")
    public MessageInfo rollBackToFinishIncome(@PathVariable("orderId") Integer orderId, @PathVariable("businessScope") String businessScope) {
        try {
            orderLockOrUnlockService.rollBackToFinishIncome(orderId, businessScope);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 退回成本完成
     *
     * @param orderId
     * @return
     */
    @PutMapping("/rollBackToFinishCost/{businessScope}/{orderId}")
    public MessageInfo rollBackToFinishCost(@PathVariable("orderId") Integer orderId, @PathVariable("businessScope") String businessScope) {
        try {
            orderLockOrUnlockService.rollBackToFinishCost(orderId, businessScope);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 导出Excel
     *
     * @param orderLockOrUnlock
     * @throws IOException
     */
    @PostMapping("/exportExcel")
    public void exportExcel(OrderLockOrUnlock orderLockOrUnlock) throws IOException {
        try {
            orderLockOrUnlockService.exportExcel(orderLockOrUnlock);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    /**
     * 批量打印核算单
     * @param busnessScope
     * @param orderIds
     * @return
     */
    @GetMapping("/printBusinessCalculationBillMany/{businessScope}/{orderIds}")
    public MessageInfo printBusinessCalculationBillMany(@PathVariable("businessScope") String busnessScope, @PathVariable("orderIds") String orderIds) {
        try {
            String path = orderLockOrUnlockService.printBusinessCalculationBillMany(busnessScope, orderIds);
            return MessageInfo.ok(path);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}
