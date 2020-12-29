package com.efreight.sc.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class OrderDeliveryNotice implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String orderUuid;
	
	private String orgLogo;
	
	private String arrivalStation;
	
	private String containerList;
	
	private Integer planPieces;
	
	private BigDecimal planWeight;
	
	private BigDecimal planVolume;
	
	private String orderCode;
	
	private String containerLoadAddressCn;
	
	private String warehouseContactRemark;
	
	private String shipName;
	
	private String shipVoyageNumber;
	
	private LocalDate documentOffDate;
	
	private LocalDate customsClosingDate;
	
	private String warehouseLongitude;
	
	private String warehouseLatitude;
	
	private String warehouseAddressGps;
	private String mblNumber;
	private String hblNumber;
	private String phoneNumber;
	private String userName;
	private Integer orgId;
	private String orgUuid;
	
	

}
