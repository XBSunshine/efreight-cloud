package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 清单
 * </p>
 *
 * @author qipm
 * @since 2019-11-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_debit_note")
public class CssDebitNote implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "debit_note_id", type = IdType.AUTO)
    private Integer debitNoteId;
    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 订单UUID
     */
    private String orderUuid;
    private String rowUuid;
    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 账单编号
     */
    private String debitNoteNum;

    /**
     * 账单日期
     */
    private String debitNoteDate;

    /**
     * 收款客户ID
     */
    private String customerId;

    /**
     * 收款客户名称
     */
    private String customerName;

    /**
     * 清单ID
     */
    private Integer statementId;

    /**
     * 账单金额
     */
    private BigDecimal amount;

    @TableField(exist = false)
    private String currencyAmount;

    /**
     * 账单币种
     */
    private String currency;

    /**
     * 账单币种汇率
     */
    private BigDecimal exchangeRate;

    /**
     * 账单本币金额
     */
    private BigDecimal functionalAmount;
    private BigDecimal functionalAmountWriteoff;
    private transient BigDecimal functionalAmountWriteoffNo;
    private Integer writeoffComplete;

    /**
     * 账单税率
     */
    private BigDecimal amountTaxRate;

    /**
     * 账单无税金额
     */
    private BigDecimal amountNotTax;

    /**
     * 账单税额
     */
    private BigDecimal amountTax;

    /**
     * 账单备注
     */
    private String debitNoteRemark;

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
     * 修改人ID
     */
    private Integer editorId;

    /**
     * 修改人名称
     */
    private String editorName;

    /**
     * 修改时间
     */
    private LocalDateTime editTime;

    /**
     * 账单公司抬头ID
     */
    private Integer orgBankConfigId;
    
    private transient String incomeIds;
    private transient String incomeRowUuids;
    private transient List<CssDebitNoteCurrency> debitCurrencyList;
    


}
