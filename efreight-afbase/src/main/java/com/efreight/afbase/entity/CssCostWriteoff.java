package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CSS 应付：核销
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_cost_writeoff")
public class CssCostWriteoff implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应付核销单ID
     */
    @TableId(value = "cost_writeoff_id", type = IdType.AUTO)
    private Integer costWriteoffId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 应付对账单ID
     */
    private Integer paymentId;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 核销单号：例如 AE-PW-2001220025 Payables Write-off
     */
    private String writeoffNum;

    /**
     * 付款客户ID
     */
    private Integer customerId;

    /**
     * 付款客户名称
     */
    private String customerName;

    /**
     * 核销日期
     */
    private LocalDate writeoffDate;

    /**
     * 财务：科目名称
     */
    private String financialAccountName;

    /**
     * 财务：科目代码
     */
    private String financialAccountCode;

    /**
     * 财务：科目类型
     */
    private String financialAccountType;

    @TableField(exist = false)
    private LocalDate writeoffDateStart;
    @TableField(exist = false)
    private LocalDate writeoffDateEnd;

    /**
     * 核销币种
     */
    private String currency;

    /**
     * 核销金额
     */
    private BigDecimal amountWriteoff;

    @TableField(exist = false)
    private String amountWriteoffStr;

    /**
     * 核销备注
     */
    private String writeoffRemark;

    /**
     * 创建人ID
     */
    private Integer creatorId;

    /**
     * 创建人名称
     */
    private String creatorName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 对账编号
     */
    @TableField(exist = false)
    private String paymentNum;

    /**
     * 对账金额
     */
    @TableField(exist = false)
    private BigDecimal amountPayment;
    @TableField(exist = false)
    private String amountPaymentStr;

    /**
     * 主单/订单号
     */
    @TableField(exist = false)
    private String awbNumberOrOrderCode;


    @TableField(exist = false)
    private List<CssCostWriteoffDetail> cssCostWriteoffDetails;

    @TableField(exist = false)
    private String rowUuid;

    @TableField(exist = false)
    private String columnStrs;

    /**
     * 凭证日期
     */
    private Date voucherDate;

    private String voucherCreatorName;

    private String voucherNumber;

}
