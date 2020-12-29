package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DebitNoteTree {

    private Boolean checkBox = false;
    private String debitNoteId;
    private String customerName;
    private String currency;
    private BigDecimal amountTaxRate;
    private Boolean isParent = true;
    private List<DebitNote> children;
    
    private String currencyAmount;
    private String currencyAmount2;
    private BigDecimal functionalAmount;
    private BigDecimal functionalAmountWriteoff;

}
