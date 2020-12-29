package com.efreight.afbase.entity;


import java.io.Serializable;
import java.time.LocalDate;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssPReportCustomerContribution implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer orgId;
	private String businessScope;
	private LocalDate startDate;
	private LocalDate endDate;
	private String businessProduct;
	private String orderStatus;
	private String goodsType;
	private String isAllUser;
	private String endDateYear;
	private String countType;
	private String containerMethod;
	private String endDateType;
	private Integer coopId;
	private String columnStrs;
	private boolean showConstituteFlag;
	
	private String dep;
	private String arr;
    private String coopType;
    private String supplierName;
    private String chooseRoutingNames;
    private String country;
    private String area;
    private Integer otherOrg;
    private String customerName;
}
