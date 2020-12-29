package com.efreight.prm.entity;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CoopShare implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String isBind;
	private String isShare;
	private String coopName;
	private String isOrgShare;
	private String coopType;
	private Integer orgId;
	
	private String coopCode;
	private String orgUuid;
	private String orgName;
	private Integer coopOrgCoopId;
	
	private Integer coopId;
	private Integer coopOrgId;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date bindTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date bindTimeEnd;
	private String[] coopTypes;
}
