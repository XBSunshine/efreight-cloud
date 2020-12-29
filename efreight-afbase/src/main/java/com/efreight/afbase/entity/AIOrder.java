package com.efreight.afbase.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AIOrder implements Serializable {

	 /**
     * 运单号
     */
    private String awbNumber;	
    /**
     * 订单号
     */
    private String orderCode;
    /**
     * 操作节点
     */
    private String orderStatus;
    /**
     * 收入完成
     */
    private String incomeRecorded;
    /**
     * 成本完成
     */
    private String costRecorded;
    /**
     * 客户名称
     */
    private String coopName;
    /**
     * 航班号
     */
    private String expectFlight;
    /**
     * 到港日期
     */
    private LocalDate expectArrival;
    /**
     * 始发港
     */
    private String departureStation;
    /**
     * 目的港
     */
    private String arrivalStation;
    /**
     * 件数
     */
    private Integer planPieces;
    /**
     * 毛重
     */
    private BigDecimal planWeight;
    /**
     * 体积
     */
    private BigDecimal planVolume;
    /**
     * 计重
     */
    private BigDecimal planChargeWeight;
    /**
     * 外库调单
     */
    private String switchAwbService;
    /**
     * 库内操作
     */
    private String warehouseService;
    /**
     * 报关服务
     */
    private String customsClearanceService;
    /**
     * 派送服务
     */
    private String deliveryService;
    /**
     * 客户单号
     */
    private String customerNumber;
    /**
     * 货物流向
     */
    private String cargoFlowType;
    /**
     * 流向备注
     */
    private String cargoFlowRemark;
    /**
     * 货物类型
     */
    private String goodsType;
    /**
     * 中文品名
     */
    private String goodsNameCn;
    /**
     * 破损记录
     */
    private String damageRemark;
    /**
     * 责任销售
     */
    private String salesName;
    /**
     * 责任客服
     */
    private String servicerName;
    /**
     * 责任操作
     */
    private String creatorName;
    /**
     * 订单备注
     */
    private String orderRemark;
    
}
