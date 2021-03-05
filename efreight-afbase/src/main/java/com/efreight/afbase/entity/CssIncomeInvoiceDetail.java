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
 * CSS 应收：发票明细表
 * </p>
 *
 * @author cwd
 * @since 2020-12-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssIncomeInvoiceDetail implements Serializable {

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
     * 发票申请ID
     */
    private Integer invoiceId;

    /**
     * 账单ID
     */
    private Integer debitNoteId;

    /**
     * 清单ID
     */
    private Integer statementId;

    /**
     * 收款客户ID
     */
    private Integer customerId;

    /**
     * 收款客户名称
     */
    private String customerName;

    /**
     * 发票号
     */
    private String invoiceNum;

    /**
     * 发票类型
     */
    private String invoiceType;

    /**
     * 发票抬头
     */
    private String invoiceTitle;

    /**
     * 发票日期
     */
    private LocalDate invoiceDate;

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

    /**
     * rowid
     */
    private String rowUuid;
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer writeoffComplete;
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal amountWriteoff;

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
    private String financialAccountName;
    @TableField(exist = false)
    private String financialAccountCode;
    @TableField(exist = false)
    private String financialAccountType;
    @TableField(exist = false)
    private LocalDate writeoffDate;
    @TableField(exist = false)
    private Integer isAutoWriteoff;
    @TableField(exist = false)
    private String writeoffRemark;

}
