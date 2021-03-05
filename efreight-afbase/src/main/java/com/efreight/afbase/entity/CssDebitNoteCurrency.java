package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CSS 应收：账单 币种汇总表
 * </p>
 *
 * @author qipm
 * @since 2019-12-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_debit_note_currency")
public class CssDebitNoteCurrency implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "debit_note_currency_id", type = IdType.AUTO)
    private Integer 	debitNoteCurrencyId;
    /**
     * 账单ID
     */
    private Integer debitNoteId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 币种汇率
     */
    private BigDecimal exchangeRate;

    /**
     * 本币金额
     */
    private BigDecimal functionalAmount;
    /**
     * 已核销金额（原币）
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal amountWriteoff;
    /**
     * 已核销金额（本币））
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal functionalAmountWriteoff;
    
    /**
     * 未核销金额（原币）
     */
    private transient BigDecimal amountWriteoffNo;
    /**
     * 未核销金额（本币）
     */
    private transient BigDecimal functionalAmountWriteoffNo;

    /**
     * 本次核销金额（原币）
     */
    private transient BigDecimal amountWriteoff2;
    /**
     * 本次核销金额（本币））
     */
    private transient BigDecimal functionalAmountWriteoff2;
    private transient String name1;
    private transient String name2;
    private transient String checkBox;
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal amountInvoice;
    @TableField(exist = false)
    private BigDecimal amountInvoiceNo;
}
