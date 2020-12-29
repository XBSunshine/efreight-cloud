package com.efreight.sc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.sc.entity.IoOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * IO 订单管理 其他业务订单 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
public interface IoOrderService extends IService<IoOrder> {

    IPage getPage(Page page, IoOrder ioOrder);

    IoOrder total(IoOrder ioOrder);

    void insert(IoOrder ioOrder);

    void modify(IoOrder ioOrder);

    void delete(Integer orderId);

    IoOrder view(Integer orderId);

    void incomeComplete(Integer orderId);

    void costComplete(Integer orderId);

    void forceStop(String reason, String orderId);

    void exportExcel(IoOrder ioOrder);
}
