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
 * CSS 应付：核销 明细
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_cost_writeoff_detail")
public class CssCostWriteoffDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应付核销明细ID
     */
    @TableId(value = "cost_writeoff_detail_id", type = IdType.AUTO)
    private Integer costWriteoffDetailId;

    /**
     * 应付核销ID
     */
    private Integer costWriteoffId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 成本对账单ID
     */
    private Integer paymentId;

    /**
     * 成本ID
     */
    private Integer costId;

    /**
     * 核销币种
     */
    private String currency;

    /**
     * 核销金额（原币）
     */
    private BigDecimal amountWriteoff;

    @TableField(exist = false)
    private String amountWriteoffStr;

    /**
     * 未核销金额
     */
    @TableField(exist = false)
    private BigDecimal amountPaymentNoWriteoff;
    @TableField(exist = false)
    private String amountPaymentNoWriteoffStr;

    /**
     * 已核销金额
     */
    @TableField(exist = false)
    private BigDecimal amountPaymentWriteoff;
    @TableField(exist = false)
    private String amountPaymentWriteoffStr;
    /**
     * 对账金额（原币）
     */
    @TableField(exist = false)
    private BigDecimal amountPayment;
    @TableField(exist = false)
    private String amountPaymentStr;

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
     * 服务
     */
    @TableField(exist = false)
    private String serviceName;




}
