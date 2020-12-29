package com.efreight.sc.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OrderExcel implements Serializable {

    /**
     * 订单号
     */
    private String orderCode;

    /**
     * 主提单号
     */
    private String mblNumber;
    /**
     * 订仓编号
     */
    private String bookingNumber;
    /**
     * 合约号
     */
    private String customerNumber;
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
    private String customerName;
    /**
     * 船公司
     */
    private String carrierName;
    /**
     * 订舱代理
     */
    private String bookingAgentName;
    /**
     * 离港日期
     */
    private String expectDeparture;
    /**
     * 抵港日期
     */
    private String expectArrival;
    /**
     * 船名/船次
     */
    private String shipNameAndNumber;
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
     * 箱号+铅封号
     */
    private String containerNumberAndContainerSealNo;
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
     * 提柜服务
     */
    private String containerPickupService;
    /**
     * 装箱服务
     */
    private String containerLoadService;

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
     * 分提单号
     */
    private String hblNumber;
    /**
     * 提单类型
     */
    private String billingType;
    /**
     * 出单信息
     */
    private String outOrder;

    /**
     * 截单日期
     */
    private String documentOffDate;
    /**
     * 截关日期
     */
    private String customsClosingDate;
    /**
     * 签发日期
     */
    private String issueDate;
    /**
     * 货物类型
     */
    private String goodsType;
    /**
     * 中文品名
     */
    private String goodsNameCn;
    /**
     * 运输条款
     */
    private String transitClause;

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
