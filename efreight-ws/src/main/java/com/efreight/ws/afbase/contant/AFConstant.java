package com.efreight.ws.afbase.contant;

import com.efreight.ws.common.contant.EFConstant;

public interface AFConstant {
    /**
     * 订单服务域
     */
    String ORDER_NAMESPACE = EFConstant.BASE_NAMESPACE+"/af/order";

    /**
     * 业务范畴
     */
    interface SERVICE_SCOPE {
        String AE = "AE";
    }

    /**
     * 收货人
     */
    Integer SHIPPER_CONSIGNEE = 1;
    /**
     * 发货人
     */
    Integer SHIPPER_CONSIGNOR = 0;

    /**
     * 订单状态
     */
    interface ORDER_STATUS {
        String FORCE_CLOSE = "强制关闭";
        String FINANCIAL_ACCOUNT_LOCK = "财务锁账";
    }

    /**
     * 最大订单号
     */
    Integer MAX_ORDER_NO = 9999;
}
