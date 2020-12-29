package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AEOperateOrder implements Serializable {

    /**
     * 主单号
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
    private String tranFlag;

//    /**
//     * 收入完成
//     */
//    private String incomeRecorded;
//    /**
//     * 成本完成
//     */
//    private String costRecorded;
    /**
     * 预报件数
     */
    private String planPieces;
    /**
     * 预报毛重
     */
    private String planWeight;
    /**
     * 预报体积
     */
    private String planVolume;
    /**
     * 预报计重
     */
    private String planChargeWeight;
    /**
     * 密度
     */
    private String planDensity;
    /**
     * 实际件数
     */
    private String confirmPieces;
    /**
     * 实际毛重
     */
    private String confirmWeight;
    /**
     * 实际体积
     */
    private String confirmVolume;
    /**
     * 实际计重
     */
    private String confirmChargeWeight;
    /**
     * 实际密度
     */
    private String confirmDensity;
    private String storagePieces;
    private String storageWeight;
    /**
     * 航班号
     */
    private String expectFlight;
    /**
     * 航班日期
     */
    private LocalDate expectDeparture;
    /**
     * 始发港
     */
    private String departureStation;
    /**
     * 目的港
     */
    private String arrivalStation;
    /**
     * 货源地
     */
    private String goodsSourceCode;
    /**
     * 客户代码
     */
    private transient String customerCode;
    /**
     * 客户名称
     */
    private String coopName;
    /**
     * 运单来源代码
     */
    private transient String supplierCode;
    /**
     * 运单来源
     */
    private String awbFromName;
    /**
     * 提货服务
     */
    private String pickUpDeliveryService;
    /**
     * 库内操作
     */
    private String warehouseService;
    /**
     * 外场服务
     */
    private String outfieldService;
    /**
     * 报关服务
     */
    private String customsClearanceService;
    /**
     * 目的港清关
     */
    private String arrivalCustomsClearanceService;
    /**
     * 目的港派送
     */
    private String deliveryService;
    /**
     * 客户单号
     */
    private String customerNumber;
    /**
     * 服务产品
     */
    private String businessProduct;
    /**
     * 分单数
     */
    private String hawbQuantity;
    /**
     * 货站/库房
     */
    private String departureWarehouseName;
    private String departureStorehouseName;

    /**
     * 中文品名
     */
    private String goodsNameCn;
    /**
     * 货物类型
     */
    private String goodsType;
    /**
     * 电池情况
     */
    private String batteryType;

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
