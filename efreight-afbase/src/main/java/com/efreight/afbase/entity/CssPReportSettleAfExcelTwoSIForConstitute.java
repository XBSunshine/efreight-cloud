package com.efreight.afbase.entity;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssPReportSettleAfExcelTwoSIForConstitute implements Serializable{



	private String businessScope;
	
	
	private String orderCode;
	private String coopCode;
	
	private String coopName;
	
	private String awbNumber;
	private String customerNumber;
	private String orderStatus;
	private String incomeStatus;
	private String costStatus;
	
	private String departureStation;
	
	private String arrivalStation;
	
	private String expectFlight;
	
	private String routingName;
	
	private String expectDeparture;
	
	private String salesName;
	private String deptName;
	
	private String servicerName;

	private String workgroupName;
	
	private String goodsType;
	
	private String pieces;
	private String weight;
	private String volume;
	private String chargeWeight;
	
	private String incomeFunctionalAmount;
	
	private String costFunctionalAmount;
	
	private String grossProfit;
	
	private String grossProfitWeight;
	private String mainRoutingGrossProfitWeight;

}
