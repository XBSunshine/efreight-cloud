package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 延伸服务 成本
 * </p>
 *
 * @author qipm
 * @since 2019-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_cost")
public class AfCost implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "cost_id", type = IdType.AUTO)
    private Integer costId;
    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 订单号
     */
    @TableField(exist = false)
    private String orderCode;

    /**
     * 主单号
     */
    @TableField(exist = false)
    private String awbNumber;

    /**
     * 分单号
     */
    @TableField(exist = false)
    private String hawbNumber;

    /**
     * 订单UUID
     */
    private String orderUuid;

    /**
     * 应收ID income_id
     */
    private Integer incomeId;

    /**
     * 付款客户ID
     */
    private Integer customerId;

    /**
     * 付款客户名称
     */
    private String customerName;

    /**
     * 付款客户类型
     */
    @TableField(exist = false)
    private String customerType;

    /**
     * 服务id
     */
    private Integer serviceId;

    @TableField(exist = false)
    private String serviceIds;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务备注
     */
    private String serviceRemark;
    private String serviceNote;
    /**
     * 单价
     */
    private BigDecimal costUnitPrice;

    /**
     * 数量
     */
    private BigDecimal costQuantity;

    /**
     * 成本金额
     */
    private BigDecimal costAmount;

    @TableField(exist = false)
    private String costAmountStr;

    /**
     * 成本本币金额
     */
    private BigDecimal costFunctionalAmount;
    @TableField(exist = false)
    private String costFunctionalAmountStr;

    /**
     * 成本已对账金额
     */
    private BigDecimal costAmountPayment;

    @TableField(exist = false)
    private String costAmountPaymentStr;

    /**
     * 未对账金额
     */
    @TableField(exist = false)
    private BigDecimal costAmountNoPayment;
    @TableField(exist = false)
    private String costAmountNoPaymentStr;

    /**
     * 成本已核销金额
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal costAmountWriteoff;
    @TableField(exist = false)
    private String costAmountWriteoffStr;

    /**
     * 未核销金额
     */
    @TableField(exist = false)
    private BigDecimal costAmountNoWriteoff;
    @TableField(exist = false)
    private String costAmountNoWriteoffStr;
    @TableField(exist = false)
    private BigDecimal costFunctionalAmountNoWriteoff;
    @TableField(exist = false)
    private String costFunctionalAmountNoWriteoffStr;

    /**
     * 成本币种
     */
    private String costCurrency;

    /**
     * 币种汇率
     */
    private BigDecimal costExchangeRate;

    /**
     * 税率
     */
    private BigDecimal costAmountTaxRate;

    /**
     * 无税金额
     */
    private BigDecimal costAmountNotTax;

    /**
     * 税额
     */
    private BigDecimal costAmountTax;

    /**
     * 成本可编辑：1 是 0 否
     */
    private Boolean costEdit;

    /**
     * 费用分类：干线运输
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal mainRouting;

    /**
     * 费用分类：支线运输
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal feeder;

    /**
     * 费用分类：操作服务
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal operation;

    /**
     * 费用分类：包装服务
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal packaging;

    /**
     * 费用分类：仓储服务
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal storage;

    /**
     * 费用分类：邮资快递
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal postage;

    /**
     * 费用分类：政府检验
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal clearance;

    /**
     * 费用分类：数据交换
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal exchange;

    /**
     * 付款单ID
     */
    private Integer paymentId;

    /**
     * 财务账期
     */
    private LocalDateTime financialDate;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private Integer editorId;

    private String editorName;

    private LocalDateTime editTime;

    @TableField(exist = false)
    private Integer deleteFlag;
    /**
     * 不包含的costId,以','分隔
     */
    @TableField(exist = false)
    private String noCostIds;

    /**
     * 对上个页面删除的对账明细的cost特殊处理已对账金额
     */
    @TableField(exist = false)
    private String deleteCostIds;


    /**
     * 开航日期
     */
    @TableField(exist = false)
    private LocalDate flightDate;
    /**
     * 开航日期-开始时间
     */
    @TableField(exist = false)
    private LocalDate flightDateStart;
    /**
     * 开航日期-结束时间
     */
    @TableField(exist = false)
    private LocalDate flightDateEnd;

    @TableField(exist = false)
    private String awbOrOrderNumbers;

    /**
     * 本次对账金额
     */
    @TableField(exist = false)
    private String costCurrAmountPayment = "0";

    /**
     * 自动匹配对账金额
     */
    @TableField(exist = false)
    private BigDecimal automatchAmount;

    @TableField(exist = false)
    private boolean groupSum;
    @TableField(exist = false)
    private boolean showOrigin;
    /*
     * 已对账金额
     */
    @TableField(exist = false)
    private double amountPayment;
    @TableField(exist = false)
    private double amountFunctionalPayment;
    @TableField(exist = false)
    private String amountPaymentStr;
    @TableField(exist = false)
    private String amountFunctionalPaymentStr;

    private transient String debitNoteNum;
    private transient String paymentNum;

    @TableField(exist = false)
    private String coopName;
    @TableField(exist = false)
    private String salesName;
    @TableField(exist = false)
    private String servicerName;
    @TableField(exist = false)
    private Integer confirmPieces;
    @TableField(exist = false)
    private BigDecimal confirmWeight;
    @TableField(exist = false)
    private String confirmWeightStr;
    @TableField(exist = false)
    private Double confirmVolume;
    @TableField(exist = false)
    private String confirmVolumeStr;
    @TableField(exist = false)
    private Double confirmChargeWeight;
    @TableField(exist = false)
    private String confirmChargeWeightStr;
    private String rowUuid;
    @TableField(exist = false)
    private BigDecimal serviceAmountMin;
    @TableField(exist = false)
    private BigDecimal serviceAmountMax;
    @TableField(exist = false)
    private Integer serviceAmountDigits;
    @TableField(exist = false)
    private String serviceAmountCarry;

    /**
     * 客户单号
     */
    @TableField(exist = false)
    private String customerNumber;
    @TableField(exist = false)
    private String columnStrs;

}
