package com.efreight.afbase.entity;


import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CustomerInContributionInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String departureStation;
	
	private String arrivalStation;
	
	private BigDecimal planChargeWeight;
	
	private BigDecimal incomeFunctionalAmountCount;
	
	private BigDecimal grossProfit;
	
	private BigDecimal unitGrossProfit;
	
	private String grossProfitMargin;
	
	private BigDecimal depLongitude;
	
	private BigDecimal depLatitude;
	private BigDecimal arrLongitude;
	
	private BigDecimal arrLatitude;
	
	private BigDecimal costFunctionalAmountCount;

}
