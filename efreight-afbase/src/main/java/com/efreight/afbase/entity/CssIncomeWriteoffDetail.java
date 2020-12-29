package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CSS 应收：核销 明细
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_income_writeoff_detail")
public class CssIncomeWriteoffDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应收核销明细ID
     */
    @TableId(value = "income_writeoff_detail_id", type = IdType.AUTO)
    private Integer incomeWriteoffDetailId;

    /**
     * 应收核销ID
     */
    private Integer incomeWriteoffId;

    /**
     * 应收清单ID
     */
    private Integer statementId;

    /**
     * 应收账单ID
     */
    private Integer debitNoteId;

    @TableField(exist = false)
    private String debitNoteNum;

    /**
     * 核销币种
     */
    private String currency;

    @TableField(exist = false)
    private BigDecimal exchangeRate;
    /**
     * 核销金额（原币）
     */
    private BigDecimal amountWriteoff;

    /**
     * 核销金额（本币）
     */
    private BigDecimal functionalAmountWriteoff;

    @TableField(exist = false)
    private BigDecimal amount;

    @TableField(exist = false)
    private BigDecimal functionalAmount;

    @TableField(exist = false)
    private String awbNumber;
}
