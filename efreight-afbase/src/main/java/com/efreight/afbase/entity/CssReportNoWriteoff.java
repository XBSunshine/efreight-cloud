package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssReportNoWriteoff {

    private Integer coopId;
    /**
     * 客户代码
     */
    private String coopCode;
    /**
     * 客户名臣
     */
    private String coopName;
    /**
     * 应收未核销本币金额
     */
    private BigDecimal incomeNoWriteoffFunctionalAmount;
    private String incomeNoWriteoffFunctionalAmountStr;

    /**
     * 应付未核销本币金额
     */
    private BigDecimal costNoWriteoffFunctionalAmount;
    private String costNoWriteoffFunctionalAmountStr;

    /**
     * 余额
     */
    private BigDecimal functionalAmountSubstraction;
    private String functionalAmountSubstractionStr;

    private Integer orgId;

    private String columnStrs;
    
    private Integer otherOrg;

}
