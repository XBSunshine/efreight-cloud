package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PaymentBatchDetail {
    /**
     * 主单或订单号
     */
    private String code;

    /**
     * 主单或订单id
     */
    private Integer id;
    /**
     * true 指订单 ; false 指主单
     */
    private Boolean isOrderCode;

    /**
     * 原成本金额
     */
    private BigDecimal amount;
    private String amountStr;

    /**
     * 可对账金额
     */
    private BigDecimal noPaymentAmount;
    private String noPaymentAmountStr;

    /**
     * 供应商(Excel)金额
     */
    private BigDecimal uploadAmount;
    private String uploadAmountStr;

    /**
     * 误差金额
     */
    private BigDecimal errorAmount;
    private String errorAmountStr;

    private LocalDate flightDate;

    private LocalDate paymentDate;

    private Integer customerId;

    private String currency;

    private BigDecimal exchangeRate;

    private String paymentRemark;

    private Integer serviceId;

    private String serviceIds;

    private String serviceName;

    private Boolean ifAdjust;

    private LocalDate financialDate;

    /**
     * 批量对账系统未对账金额（不调整对账金额）
     */
    private BigDecimal noPaymentAmountSum;

    /**
     * 批量对账Excel上传对账金额（调整对账金额）
     */
    private BigDecimal uploadAmountSum;

    private String businessScope;
    private List<PaymentBatchDetail> list;

    private String orderRowUuid;
}
