package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CSS 应收：清单
 * </p>
 *
 * @author xiaobo
 * @since 2019-12-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_statement")
public class Statement implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应收清单ID
     */
    @TableId(value = "statement_id", type = IdType.AUTO)
    private Integer statementId;

    /**
     * 签约公司ID
     */
    private Integer orgId;
    private String rowUuid;
    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 应收清单编号
     */
    private String statementNum;

    /**
     * 应收清单日期
     */
    private String statementDate;

    @TableField(exist = false)
    private String statementDateStart;

    @TableField(exist = false)
    private String statementDateEnd;
    /**
     * 收款对象的ID
     */
    private String customerId;

    /**
     * 收款公司全称
     */
    private String customerName;

    /**
     * 清单金额
     */
    private BigDecimal amount;

    @TableField(exist = false)
    private String currencyAmount;
    @TableField(exist = false)
    private String currencyAmount2;

    /**
     * 清单币种
     */
    private String currency;

    /**
     * 清单币种汇率
     */
    private BigDecimal exchangeRate;

    /**
     * 清单本币金额
     */
    private BigDecimal functionalAmount;
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal functionalAmountWriteoff;
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer writeoffComplete;

    /**
     * 发票id
     */
    private Integer invoiceId;

    /**
     * 清单税率
     */
    private BigDecimal amountTaxRate;

    /**
     * 清单无税金额
     */
    private BigDecimal amountNotTax;

    /**
     * 清单税额
     */
    private BigDecimal amountTax;

    /**
     * 清单备注
     */
    private String statementRemark;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private Integer editorId;

    private String editorName;

    private LocalDateTime editTime;
    
    private transient String debitNoteIds;
    
    private transient List<StatementCurrency> currencyList;
    
    private transient List<CssDebitNote> billList;
    
    /**
     * 主单号
     */
    @TableField(exist = false)
    private String awbNumber;
    /**
     * 订单号
     */
    @TableField(exist = false)
    private String orderCode;

    /**
     * 状态
     */
    @TableField(exist = false)
    private String statementStatus;

    /**
     * 客户单号
     */
    @TableField(exist = false)
    private String customerNumber;
    @TableField(exist = false)
    private String writeoffNum;
    /**
     * 账单编号
     */
    @TableField(exist = false)
    private String debitNoteNum;
    private String invoiceRemark;
    private String invoiceNum;
    private String invoiceTitle;
    private LocalDate invoiceDate;
    @TableField(exist = false)
    private String invoiceDateStart;
    @TableField(exist = false)
    private String invoiceDateEnd;

    private transient List<Integer> servicerIdList;//责任客服
    private transient List<Integer> salesIdList;//责任销售
    @TableField(exist = false)
    private String columnStrs;
    @TableField(exist = false)
    private Integer invoiceStatus;
    @TableField(exist = false)
    private String currencyStr;
    @TableField(exist = false)
    private String socialCreditCode;
    /**
     * 账单公司抬头ID
     */
    private Integer orgBankConfigId;
    @TableField(exist = false)
    private Integer invoiceQuery;
    @TableField(exist = false)
    private String coopAddress;
    @TableField(exist = false)
    private String phoneNumber;
    @TableField(exist = false)
    private String bankNumber;
    @TableField(exist = false)
    private String bankName;

    @TableField(exist = false)
    private String invoiceCreatorName;
    @TableField(exist = false)
    private LocalDateTime invoiceCreateTime;
    @TableField(exist = false)
    private String invoiceCreateTimeStart;
    @TableField(exist = false)
    private String invoiceCreateTimeEnd;
    @TableField(exist = false)
    private String applyRemark;
    
}
