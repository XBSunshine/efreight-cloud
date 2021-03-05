package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CSS 应付：发票申请表
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_cost_invoice")
public class CssCostInvoice implements Serializable {

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
     * 对账单ID
     */
    private Integer paymentId;

    /**
     * 状态：-1 未收票 0 部分收票 1 收票完毕
     */
    private Integer invoiceStatus;

    /**
     * 付款客户ID（供应商ID）
     */
    private Integer customerId;

    /**
     * 付款客户名称（供应商名称）
     */
    private String customerName;

    /**
     * 付款申请备注
     */
    private String applyRemark;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    /**
     * rowid
     */
    private String rowUuid;

    @TableField(exist = false)
    private List<CssCostInvoiceDetail> list;
    @TableField(exist = false)
    private String paymentNum;
    @TableField(exist = false)
    private String currency;

    @TableField(exist = false)
    private BigDecimal amount;
    @TableField(exist = false)
    private String amountStr;
}
