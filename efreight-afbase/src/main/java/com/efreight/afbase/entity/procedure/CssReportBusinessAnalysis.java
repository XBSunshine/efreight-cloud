package com.efreight.afbase.entity.procedure;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssReportBusinessAnalysis {

	private Integer orgId;
	private String businessScope;
	private String orderUnit;
	private String orderUnitValue;
	private String flightDateStart;
	private String flightDateEnd;
	private String caliber;
	private String orderStatus;
	private String businessProduct;
	private String containerMethod;
	private String awbFromType;
	private List<Map<String,String>> filedMap;
	private Integer otherOrg;
	
}
