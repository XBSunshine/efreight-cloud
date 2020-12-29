package com.efreight.afbase.entity.view;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class VCurrencyRate implements Serializable {

    private Integer orgId;

    private String currencyCode;

    private BigDecimal currencyRate;
}
