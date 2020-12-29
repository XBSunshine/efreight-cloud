package com.efreight.afbase.entity;

import java.math.BigDecimal;

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
 * AF 操作管理 运单制单
 * </p>
 *
 * @author xiaobo
 * @since 2019-10-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_awb_print")
public class AwbPrint implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 运单制单ID
     */
    @TableId(value = "awb_print_id", type = IdType.AUTO)
    private String awbPrintId;

    @TableField(exist = false)
    private Integer awbPrintIdCopy;
    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 主单号ID
     */
    private Integer awbId;

    /**
     * 托书ID
     */
    private Integer slId;

    /**
     * 主单号UUID
     */
    private String awbUuid;

    /**
     * 主单号
     */
    private String awbNumber;


    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 订单UUID
     */
    private String orderUuid;

    /**
     * 运单所属
     */
    @TableField(exist = false)
    private String awbFromType;

    /**
     * 分单号
     */
    private String hawbNumber;

    /**
     * 运单状态：已打印 已制单 未制单等
     */
    private String awbStatus;

    /**
     * 运单类型：0 主单，1分单
     */
    private Integer awbType;

    /**
     * 收货人ID
     */
    private Integer consigneeId;

    /**
     * 收货人地址
     */
    private String consigneeAddress;

    /**
     * 发货人ID
     */
    private Integer shipperId;

    /**
     * 发货人地址
     */
    private String shipperAddress;

    /**
     * 航司名称
     */
    private String carrierName;

    /**
     * 货物說明
     */
    private String goodsDescription;

    /**
     * 运单小件数
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private Integer awbPiecesSlac;

    /**
     * 运单件数
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private Integer awbPieces;

    /**
     * 运单毛重
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal awbGrossWeight;

    /**
     * 运单体积
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal awbVolume;

    /**
     * 运单计重
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal awbChargeWeight;

    /**
     * 商品名編號
     */
    private String commodityItemNo;

    /**
     * 运价等级
     */
    private String rateClass;

    /**
     * 付款方式
     */
    private String payMethod;

    /**
     * 付款方式(其他)
     */
    private String payMethodOther;

    /**
     * 币种
     */
    private String awbCurrency;

    /**
     * 费率
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal rateCharge;

    /**
     * 运费合计
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal freightCombined;

    /**
     * 其他杂费
     */
    private String otherFee;

    /**
     * 航班号
     */
    private String flightNumber;

    /**
     * 航班日期
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private LocalDate flightDate;

    @TableField(exist = false)
    private LocalDate flightDateStart;
    @TableField(exist = false)
    private LocalDate flightDateEnd;

    /**
     * 续程航班号
     */
    @TableField("flight_number_2")
    private String flightNumber2;

    /**
     * 续程航班日期
     */
    @TableField("flight_date_2")
    private LocalDate flightDate2;

    /**
     * 起始港
     */
    private String departureStation;

    /**
     * 始发港名称
     */
    private String departureStationName;

    /**
     * 中转港
     */
    private String transitStation;

    /**
     * 中转港2
     */
    @TableField("transit_station_2")
    private String transitStation2;

    /**
     * 中转港3
     */
    @TableField("transit_station_3")
    private String transitStation3;

    /**
     * 中转港承运人
     */
    private String transitStationBy;

    /**
     * 中转港承运人2
     */
    @TableField("transit_station_by_2")
    private String transitStationBy2;

    /**
     * 中转港承运人3
     */
    @TableField("transit_station_by_3")
    private String transitStationBy3;

    /**
     * 目的港
     */
    private String arrivalStation;

    /**
     * 目的港名称
     */
    private String arrivalStationName;

    /**
     * 保险价值
     */
    private String amountOfInsurance;

    /**
     * 操作事项handing_information
     */
    private String handingInformation;

    /**
     * 操作事項代碼
     */
    private String handingInformationSpecialHandlingCode;

    /**
     * 财务事项accounting_information
     */
    private String accountingInformation;

    /**
     * 代理人名称
     */
    private String issueAgentName;

    /**
     * 填开货运单的代理人名称和城市
     */
    private String issueAgentNameCity;

    /**
     * 代理人 IATA 代码
     */
    private String agentIataCode;

    /**
     * 代理人 账号
     */
    private String accountNo;

    /**
     * CHGS代码
     */
    private String chgsCode;

    /**
     * 声明价值
     */
    private String declaredValueOfCarriage;

    /**
     * 海关声明价值
     */
    private String declaredValueOfCustoms;

    /**
     * 预付-重量价值费
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal prepaidWeightCharge;

    /**
     * 到付-重量价值费
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal collectWeightCharge;

    /**
     * 预付-声明价值费
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal prepaidValuationCharge;

    /**
     * 到付-声明价值费
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal collectValuationCharge;

    /**
     * 预付-税款
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal prepaidTax;

    /**
     * 到付-税款
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal collectTax;

    /**
     * 预付-代理人的其它费用总额
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal prepaidTotalOtherChargeDueAgent;

    /**
     * 到付-代理人的其它费用总额
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal collectTotalOtherChargeDueAgent;

    /**
     * 预付-承运人的其它费用总额
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal prepaidTotalOtherChargeDueCarrier;

    /**
     * 到付-承运人的其它费用总额
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal collectTotalOtherChargeDueCarrier;

    /**
     * 预付总计
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal totalPrepaid;

    /**
     * 到付总计
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal totalCollect;

    /**
     * 汇率
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal currencyConversionRates;

    /**
     * 到付费用(目的国货币
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal ccChargeInDestCurrency;

    /**
     * 目的国收费
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal chargeAtDestination;

    /**
     * 运单日期
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private LocalDate awbDate;

    /**
     * 发运地
     */
    private String executedAt;

    /**
     * 承运人或代理签字
     */
    private String carrierSignature;

    /**
     * 到付费用总计
     */
    @TableField(strategy= FieldStrategy.IGNORED)
    private BigDecimal totalCollectCharge;

    private String status;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private Integer editorId;

    private String editorName;

    private LocalDateTime editTime;

    /**
     * 航司两字码(第一承运人)
     */
    private String airlineCode;

    /**
     * 备注 唛头
     */
    private String remarkMark;

    /**
     * 发货人或代理签字
     */
    private String shipperSignature;

    /**
     * 商品编号
     */
    private String goodsNo;

    /**
     * 目的港英文名
     */
    private String destNameEn;

    /**
     * 记录主单完成前的状态
     */
    private String nodeStatus;

    /**
     * 单证费
     */
    private BigDecimal documentFee;

    /**
     * 燃油费
     */
    private BigDecimal fuelCharge;

    /**
     * 安全费
     */
    private BigDecimal safetyFee;

    /**
     * 战争费
     */
    private BigDecimal warFee;

    /**
     * 费用文本
     */
    private String totalText;

    /**
     * 发货人代理名称
     */
    private String signatureofShipperorhisAgent;

    /**
     * 版本id随机号
     */
    private String rowid;

    private Integer apiStatus;

    private Integer apiSenderId;

    private String apiSenderName;

    private LocalDateTime apiSenderTime;


    /**
     * 尺寸
     */
    private String awbSizes;

    @TableField(exist = false)
    private List<AwbPrintSize> awbPrintSizeList;

    /**
     * 其他费用明细
     */
    @TableField(exist = false)
    private List<AwbPrintChargesOther> awbPrintChargesOtherList;

    /**
     * 分单集合
     */
    @TableField(exist = false)
    private List<AwbPrint> hawbs;

    @TableField(exist = false)
    private Boolean checked = false;

    //收发货人
    private transient AfOrderShipperConsignee afOrderShipperConsignee1;
    private transient AfOrderShipperConsignee afOrderShipperConsignee2;

}
