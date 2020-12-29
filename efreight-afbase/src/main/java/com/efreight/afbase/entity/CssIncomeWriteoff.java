package com.efreight.afbase.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.util.Date;
import java.util.List;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CSS 应收：核销
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_income_writeoff")
public class CssIncomeWriteoff implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应收核销ID
     */
    @TableId(value = "income_writeoff_id", type = IdType.AUTO)
    private Integer incomeWriteoffId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 应收账单ID
     */
    private Integer debitNoteId;

    /**
     * 应收清单ID
     */
    private Integer statementId;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 核销单号：例如 AE-HX-2001220025
     */
    private String writeoffNum;

    /**
     * 收款客户ID
     */
    private Integer customerId;

    /**
     * 收款客户名称
     */
    private String customerName;

    /**
     * 核销日期
     */
    private String writeoffDate;
    private transient String writeoffDateStart;
    private transient String writeoffDateEnd;
    /**
     * 核销币种
     */
    private String currency;

    /**
     * 核销金额
     */
    private BigDecimal amountWriteoff;

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
    private Date createTime;

    /**
     * 凭证日期
     */
    private Date voucherDate;

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

    private transient List<CssDebitNoteCurrency> debitCurrencyList;
    private transient List<StatementCurrency> listCurrencyList;
    
    
    private transient String rowUuid;
    private transient String createTimeBegin;
    private transient String createTimeEnd;
    private transient String currencyAmount;
    private transient String currencyAmount2;
    private transient String debitNoteNumStatementNum;
    private transient String debitNoteNum;
    private transient String statementNum;
    private transient String orderCode;
    private transient BigDecimal functionalAmount;
    
    private transient BigDecimal functionalAmountWriteoff;
    @TableField(exist = false)
    private String columnStrs;

    @TableField(exist = false)
    private CssIncomeWriteoffDetail cssIncomeWriteoffDetail;
    
    
}
