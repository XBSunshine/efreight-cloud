package com.efreight.afbase.entity.procedure;


import java.io.Serializable;
import java.time.LocalDate;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssWorkload implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * 返回参数
	 */
	private String userName;
	private Integer userId;
	private String votes;
	private String pieces;
	private String weight;
	private String volume;
	private String chargeWeight;
	private String containerNumber;
	private String deptName;
	
	/*
	 * 请求参数
	 */
	private String workloadType;
	private String orderStatus;
	private LocalDate flightDateStart;
	private LocalDate flightDateEnd;
	private String businessScope;
	private Integer orgId;
	private String dept;
//	private LocalDate startDate;
//	private LocalDate endDate;
    private String columnStrs;
	private String incomeFunctionalAmount;
	private String costFunctionalAmount;
	private String grossProfit;

}
