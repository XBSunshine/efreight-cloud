package com.efreight.afbase.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CS 延伸服务 应收
 * </p>
 *
 * @author qipm
 * @since 2020-03-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sc_income")
public class ScIncome implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "income_id", type = IdType.AUTO)
    private Integer 	incomeId;
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
    private String serviceNote;

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
     * 应收核销金额（暂时作废）
     */
    private BigDecimal incomeAmountWriteoff;

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

    private String rowUuid;
}
