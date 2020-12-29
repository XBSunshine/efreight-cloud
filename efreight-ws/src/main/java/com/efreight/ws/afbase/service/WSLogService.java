package com.efreight.ws.afbase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.ws.afbase.entity.WSLog;
import com.efreight.ws.afbase.entity.WSOrder;

public interface WSLogService extends IService<WSLog> {
    /**
     * 添加订单创建日志信息
     * @param wsOrder
     * @return
     */
    int addOrderCreateLog(WSOrder wsOrder);

    /**
     * 添加订单出生日志信息
     * @param wsOrder
     * @return
     */
    int addOrderInboundLog(WSOrder wsOrder);

    /**
     * 添加订单编辑日志信息
     * @param wsOrder
     * @return
     */
    int addOrderEditLog(WSOrder wsOrder);

    /**
     * 添加订单出重编辑日志
     * @param wsOrder
     * @return
     */
    int addOrderInboundEditLog(WSOrder wsOrder);
}
