package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CSS 应收：发票申请表
 * </p>
 *
 * @author cwd
 * @since 2020-12-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssIncomeInvoice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 发票申请ID
     */
    @TableId(value = "invoice_id", type = IdType.AUTO)
    private Integer invoiceId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 账单ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer debitNoteId;

    /**
     * 清单ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer statementId;

    /**
     * 状态：-1 未开票 0 部分开票 1 开票完毕
     */
    private Integer invoiceStatus;

    /**
     * 收款客户ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer customerId;

    /**
     * 收款客户名称
     */
    private String customerName;

    /**
     * 发票类型
     */
    private String invoiceType;

    /**
     * 发票抬头
     */
    private String invoiceTitle;

    /**
     * 纳税人识别号
     */
    private String taxpayerNum;

    /**
     * 地址
     */
    private String address;

    /**
     * 电话
     */
    private String phoneNumber;

    /**
     * 开户行
     */
    private String bankName;

    /**
     * 银行账号
     */
    private String bankNumber;

    /**
     * 开票要求
     */
    private String applyRemark;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;
    @TableField(exist = false)
    private String openInvoiceTitle;
    @TableField(exist = false)
    private String openInvoiceType;

    /**
     * rowid
     */
    private String rowUuid;
    /**
     * query
     */
    @TableField(exist = false)
    private String type;
    @TableField(exist = false)
    private String checkRowUuid;
    @TableField(exist = false)
    private String invoiceStatusStr;
    @TableField(exist = false)
    private String createTimeStart;
    @TableField(exist = false)
    private String createTimeEnd;
    @TableField(exist = false)
    private String businessNum;
    @TableField(exist = false)
    private String openInvoiceTimeStart;
    @TableField(exist = false)
    private String openInvoiceTimeEnd;
    @TableField(exist = false)
    private String openInvoiceUserName;
    @TableField(exist = false)
    private String orderAwbNumber;
    @TableField(exist = false)
    private String invoiceDateStart;
    @TableField(exist = false)
    private String invoiceDateEnd;
    
    /**
     * detil 
     */
    @TableField(exist = false)
    private String invoiceNum;
    @TableField(exist = false)
    private BigDecimal amountWriteoffNo;
    @TableField(exist = false)
    private BigDecimal amountWriteoff;
    @TableField(exist = false)
    private BigDecimal amount;
    @TableField(exist = false)
    private LocalDateTime openInvoiceTime;
    @TableField(exist = false)
    private String openInvoiceRemark;
    @TableField(exist = false)
    private Integer writeoffComplete;
    @TableField(exist = false)
    private Integer invoiceDetailId;
    @TableField(exist = false)
    private String currency;
    @TableField(exist = false)
    private String columnStrs;
    @TableField(exist = false)
    private String businessRowUuid;
    
    /**
     * 账单/清单金额
     */
    @TableField(exist = false)
    private String  busniessAmount;
    @TableField(exist = false)
    private String files;
    @TableField(exist = false)
    private Integer businessWriteoffComplete;
    @TableField(exist = false)
    private LocalDate invoiceDate;
}
