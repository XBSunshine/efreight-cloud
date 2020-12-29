package com.efreight.sc.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CS 延伸服务 成本
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sc_cost")
public class Cost implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成本ID
     */
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
     * 订单UUID
     */
    private String orderUuid;

    /**
     * 应收ID income_id
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer incomeId;

    /**
     * 付款客户ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer customerId;

    /**
     * 付款客户名称
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
     * 服务标准备注
     */
    private String serviceRemark;

    /**
     * 服务备注
     */
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

    /**
     * 成本本币金额
     */
    private BigDecimal costFunctionalAmount;

    /**
     * 成本已核销金额
     */
    private BigDecimal costAmountWriteoff;

    /**
     * 成本已对账金额
     */
    private BigDecimal costAmountPayment;

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

    /**
     * 付款单ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer paymentId;

    /**
     * 财务账期
     */
    private LocalDateTime financialDate;

    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer editorId;

    private String editorName;

    private LocalDateTime editTime;

    private String rowUuid;


}
