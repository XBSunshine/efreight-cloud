package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDate;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CSS 应收：发票明细 核销表
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_cost_invoice_detail_writeoff")
public class CssCostInvoiceDetailWriteoff implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 发票核销明细ID
     */
    @TableId(value = "invoice_detail_writeoff_id", type = IdType.AUTO)
    private Integer invoiceDetailWriteoffId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 发票申请ID
     */
    private Integer invoiceId;

    /**
     * 发票明细ID
     */
    private Integer invoiceDetailId;
    @TableField(exist = false)
    private String invoiceNum;
    @TableField(exist = false)
    private BigDecimal invoiceAmount;
    @TableField(exist = false)
    private String invoiceAmountStr;

    /**
     * 核销单号：例如 AE-PW-2001220025  Payment Write-off
     */
    private String writeoffNum;

    /**
     * 核销日期
     */
    private LocalDate writeoffDate;
    @TableField(exist = false)
    private LocalDate writeoffDateStart;
    @TableField(exist = false)
    private LocalDate writeoffDateEnd;

    /**
     * 对账单ID
     */
    private Integer paymentId;
    @TableField(exist = false)
    private String paymentNum;

    /**
     * 收款客户ID
     */
    private Integer customerId;

    /**
     * 收款客户名称
     */
    private String customerName;

    /**
     * 核销币种
     */
    private String currency;

    /**
     * 核销金额（原币）
     */
    private BigDecimal amountWriteoff;
    @TableField(exist = false)
    private String amountWriteoffStr;

    /**
     * 核销备注
     */
    private String writeoffRemark;

    /**
     * 财务：科目名称
     */
    private String financialAccountName;

    /**
     * 财务：科目代码
     */
    private String financialAccountCode;

    /**
     * 财务：科目类型   子科目、 往来单位
     */
    private String financialAccountType;

    /**
     * 财务：凭证日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate voucherDate;

    /**
     * 财务：凭证号
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer voucherNumber;

    /**
     * 财务：凭证制作人
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer voucherCreatorId;

    /**
     * 财务：凭证制作人名称
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private String voucherCreatorName;

    /**
     * 财务：凭证制作时间
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDateTime voucherCreateTime;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private LocalDateTime createTimeStart;

    @TableField(exist = false)
    private LocalDateTime createTimeEnd;

    private String rowUuid;

    @TableField(exist = false)
    private String columnStrs;

    @TableField(exist = false)
    private LocalDate invoiceDate;

    @TableField(exist = false)
    private LocalDate invoiceDateStart;

    @TableField(exist = false)
    private LocalDate invoiceDateEnd;
}

