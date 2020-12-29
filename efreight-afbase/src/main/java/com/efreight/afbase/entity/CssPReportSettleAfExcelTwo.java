package com.efreight.afbase.entity;


import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssPReportSettleAfExcelTwo implements Serializable{



	private String businessScope;
	
	
	private String orderCode;

	private String coopCode;
	
	private String coopName;
	
	private String awbNumber;
	private String customerNumber;
	private String orderStatus;
	private String incomeStatus;
	private String costStatus;
	private String supplierCode;
	private String awbFrom;
	
	private String departureStation;
	
	private String arrivalStation;
	private String transitStation;

	@TableField("transit_station_2")
	private String transitStation2;
	private String goodsSourceCode;
	
	private String expectFlight;
	
	private String routingName;
	
	private String expectDeparture;
	
	private String businessProduct;
	
	private String salesName;
	private String deptName;
	
	private String servicerName;

	private String routingPersonName;
	
	private String goodsType;
	
	private String pieces;
	private String weight;
	private String volume;
	
	private String chargeWeight;
	
//	private String incomeAmount;
//	
//	private String costAmount;
	
	private String incomeFunctionalAmount;
	
	private String costFunctionalAmount;
	
	private String grossProfit;
	
	private String grossProfitWeight;
	private String mainRoutingGrossProfitWeight;
	private String mainRoutingIncome;
	private String mainRoutingCost;
	private String mainRouting;

	private String feederIncome;
	private String feederCost;
	private String feeder;

	private String operationIncome;
	private String operationCost;
	private String operation;

	private String packagingIncome;
	private String packagingCost;
	private String packaging;

	private String storageIncome;
	private String storageCost;
	private String storage;

	private String postageIncome;
	private String postageCost;
	private String postage;

	private String clearanceIncome;
	private String clearanceCost;
	private String clearance;

	private String exchangeIncome;
	private String exchangeCost;
	private String exchange;

}
