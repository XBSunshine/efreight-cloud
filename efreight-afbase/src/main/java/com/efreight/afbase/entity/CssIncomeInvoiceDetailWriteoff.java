package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;

import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CSS 应收：发票明细 核销表
 * </p>
 *
 * @author cwd
 * @since 2020-12-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssIncomeInvoiceDetailWriteoff implements Serializable {

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

    /**
     * 核销单号：例如 AE-RW-2001220025  Receivables Write-off
     */
    private String writeoffNum;

    /**
     * 账单ID
     */
    private Integer debitNoteId;

    /**
     * 清单ID
     */
    private Integer statementId;

    /**
     * 核销币种
     */
    private String currency;

    /**
     * 核销金额（原币）
     */
    private BigDecimal amountWriteoff;

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
    private String rowUuid;

    /**
     * 财务：凭证制作时间
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDateTime voucherCreateTime;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;
    
    private LocalDate writeoffDate;
    @TableField(exist = false)
    private String invoiceRowUuid;

    private Integer customerId;
    private String customerName;
    @TableField(exist = false)
    private String writeoffDateStart;
    @TableField(exist = false)
    private String writeoffDateEnd;
    @TableField(exist = false)
    private String createTimeStart;
    @TableField(exist = false)
    private String createTimeEnd;
    @TableField(exist = false)
    private String invoiceNum;
    @TableField(exist = false)
    private String businessNum;
    @TableField(exist = false)
    private BigDecimal amount;
    @TableField(exist = false)
    private String columnStrs;
    @TableField(exist = false)
    private String invoiceDateStart;
    @TableField(exist = false)
    private String invoiceDateEnd;
    @TableField(exist = false)
    private LocalDate invoiceDate;
    @TableField(exist = false)
    private String invoiceTitle;
    @TableField(exist = false)
    private String invoiceType;
    @TableField(exist = false)
    private String taxpayerNum;
    @TableField(exist = false)
    private String bankName;
    @TableField(exist = false)
    private String bankNumber;
    @TableField(exist = false)
    private String address;
    @TableField(exist = false)
    private String phoneNumber;
    @TableField(exist = false)
    private String invoiceRemark;
    @TableField(exist = false)
    private String titleName;

}
