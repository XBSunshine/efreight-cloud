package com.efreight.afbase.entity.procedure;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SettleStatement {

    private String orgName;
    private String customerName;
    private String orgSeal;
    private String orgLogo;
    private String bankInfo;

    private String serial;
    private String awbNumber;
    private String customerNumber;
    private LocalDate flightDate;
    private LocalDate receiptDate;
    private LocalDate inboundDate;
    private Integer debitNoteId;
    private String debitNoteNum;
    private String functionalAmountSum;
    private BigDecimal chargeableWeight;
    private BigDecimal weight;
    private BigDecimal volume;
    private Integer pieces;
    private String flightNo;

    private String serviceName;
    private Integer serviceId;
    private String currencyName;
    private String  incomeCurrency;
    private BigDecimal functionalAmount;
    private String functionalAmountStr;
    private Integer incomeId;
    
    private String statementNum;
    private String statementAmountBig;
    private String statementAmount;

    private List<SettleStatement> currencyList;
    private List<SettleStatement> serviceList;
}
