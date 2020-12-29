package com.efreight.ws.afbase.pojo.order.detail;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderIncome implements Serializable {
    private String customerCode;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private String currency;
    private BigDecimal amount;
    private BigDecimal functionalAmount;

}
