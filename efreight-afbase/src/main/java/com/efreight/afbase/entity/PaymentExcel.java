package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PaymentExcel {
    /**
     * 对账编号、对账日期、主单号、订单号、始发港、目的港、航班号、开航日期、件数、毛重、体积、计重、客户代码、客户名称、付款代码、付款客户、币种、对账金额、已核销金额、未核销金额
     */
    private String paymentNum;
    private LocalDate paymentDate;
    private String paymentStatus;
    private String awbNumber;
    private String orderCode;
    private String departureStation;
    private String arrivalStation;
    private String flightNumber;
    private LocalDate flightDate;
    private Integer pieces;
    private String weight;
    private String volume;
    private String chargeWeight;
    private String coopCode;
    private String coopName;
    private String customerCode;
    private String customerName;
    private String currency;
    private String amountPayment;
    private String costAmount;
    private String amountNoWriteOffPayment;

}
