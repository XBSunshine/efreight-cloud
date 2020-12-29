package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDate;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CSS 成本对账单 明细
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_payment_detail")
public class CssPaymentDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 付款单明细ID
     */
    @TableId(value = "payment_id_detail_id", type = IdType.AUTO)
    private Integer paymentIdDetailId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 应收核销ID
     */
    private Integer paymentId;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 单号（主单号为空取订单号）
     */
    @TableField(exist = false)
    private String awbOrOrderNumber;

    /**
     * 开航日期
     */
    @TableField(exist = false)
    private LocalDate flightDate;

    /**
     * 成本ID
     */
    private Integer costId;

    /**
     * 付款币种
     */
    private String currency;

    /**
     * 已核销金额
     */
    private BigDecimal amountPaymentWriteoff;

    /**
     * 本次付款金额（原币）
     */
    private BigDecimal amountPayment;
    @TableField(exist = false)
    private String amountPaymentStr;

    /**
     * 成本金额
     */
    @TableField(exist = false)
    private BigDecimal costAmount;
    @TableField(exist = false)
    private String costAmountStr;

    /**
     * 服务名称
     */
    @TableField(exist = false)
    private String serviceName;

    /**
     * 刷新标记
     */
    @TableField(exist = false)
    private String rowUuid;

    /**
     * 客户单号
     */
    @TableField(exist = false)
    private String customerNumber;

}
