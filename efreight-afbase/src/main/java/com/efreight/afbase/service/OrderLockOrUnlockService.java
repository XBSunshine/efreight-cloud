package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.OrderLockOrUnlock;

import java.time.LocalDateTime;

public interface OrderLockOrUnlockService {
    IPage page(Page page, OrderLockOrUnlock orderLockOrUnlock);

    void lock(String orderIds, String businessScope, LocalDateTime lockDate);

    void unlock(Integer orderId, String businessScope);

    void rollBackToFinishIncome(Integer orderId, String businessScope);

    void rollBackToFinishCost(Integer orderId, String businessScope);

    void exportExcel(OrderLockOrUnlock orderLockOrUnlock);

    String printBusinessCalculationBillMany(String busnessScope, String orderIds);
}
