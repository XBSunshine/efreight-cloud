package com.efreight.afbase.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 清单
 * </p>
 *
 * @author xiaobo
 * @since 2019-11-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_debit_note")
public class DebitNote implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账单ID
     */
    @TableId(value = "debit_note_id", type = IdType.AUTO)
    private Integer debitNoteId;
    private transient String debitNoteIds;
    private transient String debitNoteNums;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 订单ID
     */
    private Integer orderId;
    private transient Integer serviceId;
    private transient String serviceIdStr;

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
     * 主运单号
     */
    @TableField(exist = false)
    private String awbNumber;
    @TableField(exist = false)
    private String hawbNumber;
    @TableField(exist = false)
    private String awbUuid;

    /**
     * 订单号
     */
    @TableField(exist = false)
    private String orderCode;

    /**
     * 账单编号
     */
    private String debitNoteNum;

    /**
     * 账单日期
     */
    private LocalDate debitNoteDate;
    @TableField(exist = false)
    private String debitNoteDateStart;
    @TableField(exist = false)
    private String debitNoteDateEnd;

    /**
     * 开航日期
     */
    @TableField(exist = false)
    private LocalDate flightDate;
    @TableField(exist = false)
    private String flightDateStart;
    @TableField(exist = false)
    private String flightDateEnd;

    /**
     * 航班号
     */
    @TableField(exist = false)
    private String flightNo;
    /**
     * 收款客户ID
     */
    private String customerId;

    /**
     * 收款客户名称
     */
    private String customerName;

    /**
     * 客户单号
     */
    @TableField(exist = false)
    private String customerNumber;

    /**
     * 清单ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer statementId;

    /**
     * 发票ID
     */
    private Integer invoiceId;

    /**
     * 清单编号
     */
    @TableField(exist = false)
    private String statementNum;
    @TableField(exist = false)
    private String writeoffNum;

    /**
     * 账单金额
     */
    private BigDecimal amount;

    @TableField(exist = false)
    private String currencyAmount;
    @TableField(exist = false)
    private String currencyAmount2;

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
     * 账单状态
     */
    @TableField(exist = false)
    private String debitNoteStatus;

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
    private String invoiceRemark;
    private String invoiceNum;
    private String invoiceTitle;
    private LocalDate invoiceDate;
    @TableField(exist = false)
    private String invoiceRemark2;
    @TableField(exist = false)
    private String invoiceNum2;
    @TableField(exist = false)
    private String invoiceTitle2;
    @TableField(exist = false)
    private LocalDate invoiceDate2;
    @TableField(exist = false)
    private String invoiceDateStart;
    @TableField(exist = false)
    private String invoiceDateEnd;

    /**
     * 修改时间
     */
    private LocalDateTime editTime;

    @TableField(exist = false)
    private Boolean checkBox = false;

    /**
     * 是否核销
     */
    @TableField(exist = false)
    private Boolean ifWriteoff;
    //状态多选
    @TableField(exist = false)
    private String billStatus;
    @TableField(exist = false)
    private String billStatus1;
    @TableField(exist = false)
    private String billStatus2;
    @TableField(exist = false)
    private String billStatus3;
    @TableField(exist = false)
    private String billStatus4;
    @TableField(exist = false)
    private String columnStrs;
    @TableField(exist = false)
    private String shippingMethod;
    @TableField(exist = false)
    private String businessMethod;
    @TableField(exist = false)
    private String productType;
    
}
