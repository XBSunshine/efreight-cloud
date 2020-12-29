package com.efreight.sc.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 延伸服务 成本
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_cost")
public class AfCost implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成本ID
     */
    @TableId(value = "cost_id", type = IdType.AUTO)
    private Integer costId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 订单UUID
     */
    private String orderUuid;

    /**
     * 应收ID income_id
     */
    private Integer incomeId;

    /**
     * 付款客户ID
     */
    private Integer customerId;

    /**
     * 付款客户名称
     */
    private String customerName;

    /**
     * 服务id
     */
    private Integer serviceId;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务标准备注
     */
    private String serviceRemark;

    /**
     * 服务备注
     */
    private String serviceNote;

    /**
     * 单价
     */
    private BigDecimal costUnitPrice;

    /**
     * 数量
     */
    private BigDecimal costQuantity;

    /**
     * 成本金额
     */
    private BigDecimal costAmount;

    /**
     * 成本本币金额
     */
    private BigDecimal costFunctionalAmount;

    /**
     * 成本已核销金额
     */
    private BigDecimal costAmountWriteoff;

    /**
     * 成本已对账金额
     */
    private BigDecimal costAmountPayment;

    /**
     * 成本币种
     */
    private String costCurrency;

    /**
     * 币种汇率
     */
    private BigDecimal costExchangeRate;

    /**
     * 税率
     */
    private BigDecimal costAmountTaxRate;

    /**
     * 无税金额
     */
    private BigDecimal costAmountNotTax;

    /**
     * 税额
     */
    private BigDecimal costAmountTax;

    /**
     * 成本可编辑：1 是 0 否
     */
    private Boolean costEdit;

    /**
     * 费用分类：干线
     */
    private BigDecimal mainRouting;

    /**
     * 费用分类：支线
     */
    private BigDecimal feeder;

    /**
     * 费用分类：操作
     */
    private BigDecimal operation;

    /**
     * 费用分类：包装
     */
    private BigDecimal packaging;

    /**
     * 费用分类：仓储
     */
    private BigDecimal storage;

    /**
     * 费用分类：快递
     */
    private BigDecimal postage;

    /**
     * 费用分类：关检
     */
    private BigDecimal clearance;

    /**
     * 费用分类：数据
     */
    private BigDecimal exchange;

    /**
     * 付款单ID（作废 判断清单 明细表）
     */
    private Integer paymentId;

    /**
     * 财务账期（锁账日期）
     */
    private LocalDateTime financialDate;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private Integer editorId;

    private String editorName;

    private LocalDateTime editTime;

    /**
     * rowid
     */
    private String rowUuid;


}
