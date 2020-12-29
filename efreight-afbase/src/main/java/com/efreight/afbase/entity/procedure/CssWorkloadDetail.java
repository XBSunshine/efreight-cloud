package com.efreight.afbase.entity.procedure;


import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssWorkloadDetail implements Serializable{

	private static final long serialVersionUID = 1L;

	@JSONField(name = "order_code")
	private String orderCode;
	private String orderId;
	@JSONField(name = "order_uuid")
	private String orderUuid;
	@JSONField(name = "awb_number")
	private String awbNumber;
	@JSONField(name = "mbl_number")
	private String mblNumber;
	@JSONField(name = "hawb_number")
	private String hawbNumber;
	@JSONField(name = "business_product")
	private String businessProduct;
	private String businessScope;
	@JSONField(name = "container_method")
	private String containerMethod;
	@JSONField(name = "coop_name")
	private String coopName;
	@JSONField(name = "awb_from_name")
	private String awbFromName;
	@JSONField(name = "departure_station")
	private String departureStation;
	@JSONField(name = "arrival_station")
	private String arrivalStation;
	@JSONField(name = "expect_flight")
	private String expectFlight;
	@JSONField(name = "expect_departure")
	private String expectDeparture;
	private String pieces;
	private String weight;
	private String volume;

	private String chargeWeight;
	private String containerNumber;
	private String containerList;
	private String salesName;
	private String servicerName;
	private String creatorName;
	@JSONField(name = "ship_name")
	private String shipName;
	@JSONField(name = "ship_voyage_number")
	private String shipVoyageNumber;

}
