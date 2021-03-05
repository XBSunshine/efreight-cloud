package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDate;

import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CSS 应付：发票明细表
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_cost_invoice_detail")
public class CssCostInvoiceDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 发票明细ID
     */
    @TableId(value = "invoice_detail_id", type = IdType.AUTO)
    private Integer invoiceDetailId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 付款申请ID
     */
    private Integer invoiceId;

    /**
     * 对账单ID
     */
    private Integer paymentId;

    /**
     * 付款客户ID（供应商）
     */
    private Integer customerId;

    /**
     * 付款客户名称（供应商）
     */
    private String customerName;

    /**
     * 发票状态：1 完全核销   0 部分核销  NULL 未核销
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer writeoffComplete;

    /**
     * 发票号
     */
    private String invoiceNum;

    /**
     * 发票类型
     */
    private String invoiceType;

    /**
     * 发票日期
     */
    private LocalDate invoiceDate;
    @TableField(exist = false)
    private LocalDate invoiceDateStart;
    @TableField(exist = false)
    private LocalDate invoiceDateEnd;

    /**
     * 纳税人识别号
     */
    private String taxpayerNum;

    /**
     * 币种
     */
    private String currency;

    /**
     * 发票金额
     */
    private BigDecimal amount;

    @TableField(exist = false)
    private String amountStr;

    /**
     * 发票已核销金额
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal amountWriteoff;
    @TableField(exist = false)
    private String amountWriteoffStr;

    /**
     * 发票未核销金额
     */
    @TableField(exist = false)
    private BigDecimal amountNoWriteoff;
    @TableField(exist = false)
    private String amountNoWriteoffStr;

    /**
     * 开户行
     */
    private String bankName;

    /**
     * 银行账号
     */
    private String bankNumber;

    /**
     * 地址
     */
    private String address;

    /**
     * 电话
     */
    private String phoneNumber;

    /**
     * 发票备注
     */
    private String invoiceRemark;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;
    @TableField(exist = false)
    private LocalDateTime createTimeStart;
    @TableField(exist = false)
    private LocalDateTime createTimeEnd;

    /**
     * rowid
     */
    private String rowUuid;

    /**
     * 账单rowUuid
     */
    @TableField(exist = false)
    private String paymentRowUuid;
    /**
     * 发票申请rowUuid
     */
    @TableField(exist = false)
    private String invoiceRowUuid;

    @TableField(exist = false)
    private String status;

    /**
     * 付款申请备注
     */
    @TableField(exist = false)
    private String applyRemark;

    /**
     * 付款申请人
     */
    @TableField(exist = false)
    private String invoiceName;

    /**
     * 付款申请时间
     */
    @TableField(exist = false)
    private LocalDateTime invoiceTime;
    @TableField(exist = false)
    private LocalDateTime invoiceTimeStart;
    @TableField(exist = false)
    private LocalDateTime invoiceTimeEnd;

    @TableField(exist = false)
    private String paymentNum;

    /**
     * 主单号或订单号
     */
    @TableField(exist = false)
    private String awbNumberOrOrderCode;

    @TableField(exist = false)
    private String columnStrs;

    /**
     * 发票抬头
     */
    @TableField(exist = false)
    private String invoiceTitle;

    /**
     * 财务：凭证日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate voucherDate;

    /**
     * 财务：凭证号
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private String voucherNumber;

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

    @TableField(exist = false)
    private Boolean ifAutoWriteoff;
    @TableField(exist = false)
    private LocalDate writeoffDate;
    @TableField(exist = false)
    private String writeoffRemark;
    @TableField(exist = false)
    private String financialAccountName;
    @TableField(exist = false)
    private String financialAccountCode;
    @TableField(exist = false)
    private String financialAccountType;

}
