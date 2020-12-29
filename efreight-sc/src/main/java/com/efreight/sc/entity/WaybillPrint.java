package com.efreight.sc.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CS 订单管理 海运制单
 * </p>
 *
 * @author xiaobo
 * @since 2020-08-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sc_waybill_print")
public class WaybillPrint implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * DGD制单ID
     */
    @TableId(value = "waybill_print_id", type = IdType.AUTO)
    private Integer waybillPrintId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 业务范畴：SE、SI
     */
    private String businessScope;

    /**
     * MBL 主提单号（只主提单保存）
     */
    private String mblNumber;

    /**
     * 发货人Print
     */
    private String shipperPrint;

    @TableField(exist = false)
    private WaybillPrintShipperConsignee shipper;

    /**
     * 收货人Print
     */
    private String consigneePrint;

    @TableField(exist = false)
    private WaybillPrintShipperConsignee consignee;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 订舱编号
     */
    private String bookingNumber;

    /**
     * SO编号
     */
    private String soNumber;

    /**
     * 提单号
     */
    private String blNumber;

    /**
     * 收货港
     */
    private String portOfReceipt;

    /**
     * 装货港
     */
    private String portOfLoading;

    /**
     * 目的港
     */
    private String portOfDischarge;

    /**
     * notify
     */
    private String notify;

    /**
     * 参考号
     */
    private String referenceNo;

    /**
     * 头程运输
     */
    private String preCarriageBy;

    /**
     * 船名
     */
    private String shipName;

    /**
     * 船次
     */
    private String shipVoyageNumber;

    @TableField(exist = false)
    private String shipNameAndShipVoyageNumber;

    /**
     * 交货地
     */
    private String placeOfDelivery;


    /**
     * 目的港代理
     */
    private String destinationAgent;

    /**
     * 货物尺寸
     */
    private String goodsSize;

    /**
     * 集装箱备注
     */
    private String containersRemark;

    /**
     * 舱位
     */
    private String shippingSpace;

    /**
     * 运费条款
     */
    private String payment;

    /**
     * 运费
     */
    private String freight;

    /**
     * 贸易方式
     */
    private String tradeTerms;

    /**
     * 货币汇率
     */
    private String exRate;

    /**
     * 预付地点
     */
    private String prepaidAt;

    /**
     * 付款地点
     */
    private String payableAt;

    /**
     * 正/副本份数
     */
    private String ofOriginalNumber;

    /**
     * 签发地点
     */
    private String placeOfIssue;

    /**
     * 签发日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate dateOfIssue;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 承运人签字
     */
    private String carrierSignature;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private Integer editorId;

    private String editorName;

    private LocalDateTime editTime;

    @TableField(exist = false)
    private List<WaybillPrintDetails> waybillPrintDetailsList;

}
