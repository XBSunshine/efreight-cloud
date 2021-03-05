package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CSS 应收：清单 币种汇总表
 * </p>
 *
 * @author xiaobo
 * @since 2019-12-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_statement_currency")
public class StatementCurrency implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 清单币种ID
     */
    @TableId(value = "statement_currency_id", type = IdType.AUTO)
    private Integer statementCurrencyId;

    /**
     * 清单ID
     */
    private Integer statementId;

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
    private BigDecimal amountWriteoff;
    /**
     * 已核销金额（本币））
     */
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
    private transient String checkBox;
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal amountInvoice;
    @TableField(exist = false)
    private BigDecimal amountInvoiceNo;
}
