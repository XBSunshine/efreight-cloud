package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDate;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CSS 成本对账单
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_payment")
public class CssPayment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成本对账单ID
     */
    @TableId(value = "payment_id", type = IdType.AUTO)
    private Integer paymentId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 对账单编号：例如 AE-CP-2001220025
     */
    private String paymentNum;

    /**
     * 对账单日期
     */
    private LocalDate paymentDate;

    @TableField(exist = false)
    private LocalDate paymentDateStart;
    @TableField(exist = false)
    private LocalDate paymentDateEnd;

    /**
     * 付款客户ID
     */
    private Integer customerId;

    /**
     * 付款客户名称
     */
    private String customerName;

    /**
     * 付款币种
     */
    private String currency;

    /**
     * 付款金额（原币）
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal amountPayment;

    @TableField(exist = false)
    private String amountPaymentStr;

    /**
     * 付款金额（本币）
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal functionalAmountPayment;

    /**
     * 已核销金额（原币）
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal amountPaymentWriteoff;

    @TableField(exist = false)
    private String amountPaymentWriteoffStr;

    /**
     * 已核销金额（本币）
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal functionalAmountPaymentWriteoff;

    /**
     * 未核销金额（原币）
     */
    @TableField(exist = false)
    private BigDecimal amountPaymentNoWriteoff;
    @TableField(exist = false)
    private String amountPaymentNoWriteoffStr;

    /**
     * 是否完全核销，NULL 未核销，1，完全核销，0 未完全核销
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer writeoffComplete;
    @TableField(exist = false)
    private String writeoffCompletes;
    /**
     * 汇率
     */
    private BigDecimal exchangeRate;

    /**
     * 付款单备注
     */
    private String paymentRemark;

    /**
     * 发票备注
     */
    private String invoiceRemark;
    private String invoiceNum;
    private String invoiceTitle;

    private LocalDate invoiceDate;

    /**
     * 发票申请日期
     */
    @TableField(exist = false)
    private LocalDateTime invoiceTime;

    /**
     * 发票申请创建人
     */
    @TableField(exist = false)
    private String invoiceCreatorName;

    /**
     * 发票申请日期开始
     */
    @TableField(exist = false)
    private String invoiceDateStart;

    /**
     * 发票申请日期结束
     */
    @TableField(exist = false)
    private String invoiceDateEnd;

    /**
     * 发票申请备注
     */
    @TableField(exist = false)
    private String invoiceInqurityRemark;

    private Integer creatorId;

    /**
     * 创建者
     */
    private String creatorName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private Integer editorId;

    /**
     * 修改人
     */
    private String editorName;

    /**
     * 修改时间
     */
    private LocalDateTime editTime;

    /**
     * 是否修改标记
     */
    private String rowUuid;

    /**
     * 主单或订单
     */
    @TableField(exist = false)
    private String awbNumberOrOrderCode;

    @TableField(exist = false)
    private List<CssPaymentDetail> details;

    @TableField(exist = false)
    private String paymentStatus;
    @TableField(exist = false)
    private String writeoffNum;
    @TableField(exist = false)
    private String columnStrs;

    private BigDecimal amountPaymentInvoice;
}
