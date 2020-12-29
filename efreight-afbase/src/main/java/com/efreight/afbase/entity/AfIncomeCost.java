package com.efreight.afbase.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 延伸服务 应收
 * </p>
 *
 * @author qipm
 * @since 2019-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AfIncomeCost implements Serializable {

    private static final long serialVersionUID = 1L;

    private String treeId;
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
     * 订单UUID
     */
    private String orderUuid;

    /**
     * 收款客户ID
     */
    private Integer customerId;

    /**
     * 收款客户名称
     */
    private String customerName;

    /**
     * 服务id
     */
    private Integer serviceId;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务备注
     */
    private String serviceRemark;

    /**
     * 单价
     */
    private BigDecimal incomeUnitPrice;

    /**
     * 数量
     */
    private BigDecimal incomeQuantity;

    /**
     * 应收金额
     */
    private BigDecimal incomeAmount;

    /**
     * 应收本币金额
     */
    private BigDecimal incomeFunctionalAmount;

    /**
     * 应收币种
     */
    private String incomeCurrency;

    /**
     * 币种汇率
     */
    private BigDecimal incomeExchangeRate;

    /**
     * 税率
     */
    private BigDecimal incomeAmountTaxRate;

    /**
     * 无税金额
     */
    private BigDecimal incomeAmountNotTax;

    /**
     * 税额
     */
    private BigDecimal incomeAmountTax;

    /**
     * 收入可编辑：1 是 0 否
     */
    private Boolean incomeEdit;

    /**
     * 账单ID
     */
    private Integer debitNoteId;

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

    /**
     * 费用分类：干线运输
     */
    private BigDecimal mainRouting;

    /**
     * 费用分类：支线运输
     */
    private BigDecimal feeder;

    /**
     * 费用分类：操作服务
     */
    private BigDecimal operation;

    /**
     * 费用分类：包装服务
     */
    private BigDecimal packaging;

    /**
     * 费用分类：仓储服务
     */
    private BigDecimal storage;

    /**
     * 费用分类：邮资快递
     */
    private BigDecimal postage;

    /**
     * 费用分类：政府检验
     */
    private BigDecimal clearance;

    /**
     * 费用分类：数据交换
     */
    private BigDecimal exchange;

    //------------------------


    /**
     * 应收ID income_id
     */
    private Integer incomeId;
    private Integer costId;

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

    /**
     * 成本本币金额
     */
    private BigDecimal costFunctionalAmount;

    /**
     * 成本已对账金额
     */
    private BigDecimal costAmountPayment;

    /**
     * 成本已核销金额
     */
    private BigDecimal costAmountWriteoff;

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
     * 付款单ID
     */
    private Integer paymentId;
    
    private BigDecimal profitAmount;
    private BigDecimal noTaxProfitAmount ;
    private Integer sstype;



}
