package com.efreight.afbase.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CSS 应收：核销单 明细（清单）
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssIncomeWriteoffStatementDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应收核销明细ID
     */
    @TableId(value = "income_writeoff_statement_detail_id", type = IdType.AUTO)
    private Integer incomeWriteoffStatementDetailId;

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

    /**
     * 核销币种
     */
    private String currency;

    /**
     * 核销金额（原币）
     */
    private BigDecimal amountWriteoff;

    /**
     * 核销金额（本币）
     */
    private BigDecimal functionalAmountWriteoff;

    /**
     * 应收金额
     */
    @TableField(exist = false)
    private BigDecimal amount;

    /**
     * 本币应收金额
     */
    @TableField(exist = false)
    private BigDecimal functionalAmount;

    @TableField(exist = false)
    private String debitNoteNum;


    @TableField(exist = false)
    private String awbNumber;

    @TableField(exist = false)
    private BigDecimal exchangeRate;

}
