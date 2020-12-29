package com.efreight.afbase.entity.procedure;


import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssWorkloadDetailExcel implements Serializable{

	private static final long serialVersionUID = 1L;

	private String order_code;
	private String orderId;
	private String order_uuid;
	private String awb_number;
	private String mbl_number;
	private String hawb_number;
	private String business_product;
	private String businessScope;
	private String container_method;
	private String coop_name;
	private String awb_from_name;
	private String departure_station;
	private String arrival_station;
	private String expect_flight;
	private String expect_departure;
	private String pieces;
	private String weight;
	private String volume;
	private String chargeWeight;
	private String containerNumber;
	private String containerList;
	private String salesName;
	private String servicerName;
	private String creatorName;
	private String ship_name;
	private String ship_voyage_number;

}
