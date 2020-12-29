package com.efreight.sc.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SiOrderExcel implements Serializable {

    /**
     * 订单号
     */
    private String orderCode;
    /**
     * 客户单号
     */
    private String customerNumber;
    /**
     * 主提单号
     */
    private String mblNumber;
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
     * 到港日期
     */
    private String expectArrival;
    /**
     * 起运港
     */
    private String departureStation;

    /**
     * 目的港
     */
    private String arrivalStation;
    /**
     * 装箱方式
     */
    private String containerMethod;
    /**
     * 集装箱量
     */
    private String containerList;
    /**
     * 标箱数量(TEU)
     */
    private String containerNumber;
    /**
     * 件数
     */
    private String planPieces;
    /**
     * 毛重(KG)
     */
    private String planWeight;
    /**
     * 体积(CBM)
     */
    private String planVolume;
    /**
     * 计费吨(TON)
     */
    private String planChargeWeight;
    /**
     * 换单服务
     */
    private String changeOrderService;
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
     * 分提单号
     */
    private String hblNumber;
    /**
     * 船名/船次
     */
    private String shipNameAndNumber;
    /**
     * 运输条款
     */
    private String transitClause;

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
     * 责任客服
     */
    private String servicerName;
    /**
     * 责任销售
     */
    private String salesName;
    /**
     * 责任操作
     */
    private String creatorName;
    /**
     * 订单备注
     */
    private String orderRemark;
}
