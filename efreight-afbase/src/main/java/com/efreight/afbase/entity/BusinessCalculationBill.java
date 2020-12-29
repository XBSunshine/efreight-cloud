package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class BusinessCalculationBill implements Serializable {
    private String orgName;
    private String customerName;
    private String businessScope;
    private String serviceName;
    private String serviceId;
    private String serviceRemark;
    private String incomeFunctionalAmountStr;
    private BigDecimal incomeFunctionalAmount;
    private String costFunctionalAmountStr;
    private BigDecimal costFunctionalAmount;
    private String incomeAmountStr;
    private BigDecimal incomeAmount;
    private String costAmountStr;
    private BigDecimal costAmount;
    private String profitFunctionalAmountStr;
    private BigDecimal profitFunctionalAmount;

    private String orderCode;
    private String awbNumber;
    private String awbFrom;
    private String businessProduct;
    private String salesName;
    private String servicerName;
    private String departureStation;
    private String arrivalStation;
    private String flightNumber;
    private LocalDate flightDate;
    private String pwvInfo;
    private String chargeWeight;

    private String customerNumber;
    private String priceRemark;//AE订单使用

}
