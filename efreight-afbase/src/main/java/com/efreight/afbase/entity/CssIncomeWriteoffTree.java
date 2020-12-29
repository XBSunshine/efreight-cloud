package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssIncomeWriteoffTree {

    private Boolean checkBox = false;
    private String incomeWriteoffId;
    private String customerName;
    private String currency;
    private BigDecimal amountTaxRate;
    private Boolean isParent = true;
    private List<CssIncomeWriteoff> children;

}
