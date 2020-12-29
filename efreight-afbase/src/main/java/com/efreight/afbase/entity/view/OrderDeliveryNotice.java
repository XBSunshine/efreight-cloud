package com.efreight.afbase.entity.view;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单送货通知
 */
@Data
public class OrderDeliveryNotice implements Serializable {
    /**
     * 订单UUID
     */
    private String orderUuid;
    /**
     * 公司logo
     */
    private String logoUrl;
    /**
     * 目的港
     */
    private String arrivalStation;
    /**
     * 预计件数
     */
    private Integer planPieces;
    /**
     * 包装类型
     */
    private String packageType;
    /**
     * 预计毛重
     */
    private BigDecimal planWeight;
    /**
     * 预计体积
     */
    private Double planVolume;
    /**
     * 进仓编号
     */
    private String inboundNumber;
    /**
     * 主运单号
     */
    private String awbNumber;
    /**
     * 入库日期
     */
    private String inboundDate;
    /**
     * 到货日期
     */
    private String receiptDate;
    /**
     * 送至
     */
    private String sendTo;
    /**
     * 接货联系人
     */
    private String consignee;
    /**
     * 导航地址
     */
    private String address;
    /**
     * 导航地址-经度
     */
    private String addressLongitude;
    /**
     * 导航地址-纬度
     */
    private String addressLatitude;

    private String salesName;

    private String phoneNumber;

    private String orgId;

    private String orgUuid;


}
