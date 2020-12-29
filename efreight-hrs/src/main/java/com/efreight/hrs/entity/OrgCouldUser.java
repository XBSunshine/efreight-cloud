package com.efreight.hrs.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OrgCouldUser implements Serializable{/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 查询
	 */
	private String isStatus;
	private String demandPersonId;
	private String orgEditionId;
	private LocalDateTime createTimeStart;
	private LocalDateTime createTimeEnd;
	private Integer intendedUser;//是否为意向客户
	
	/**
	 * 结果
	 */
	private Integer orgId;
	private String orgCode;
	private String oneStopCode;
	private String orgName;
	private String demandPersonName;
	private LocalDateTime createTime;
	private String orgEditionName;
	private Integer orgUserCount;
	private LocalDateTime stopDate;
	private Integer orgCoopCount;
	private Integer orgOrderCount;
	private String adminEmail;
	private String adminTel;
	private String adminName;
	private String adminId;
	private String rcEmail;
	private String orgRemark;
	private Integer orgType;
	private String adminInternationalCountryCode;
	private Integer userCount;
	private boolean orgStatus;
	private LocalDate createTimeAf;
	private LocalDate createTimeSc;
	private Integer subscriptionNum;
	private LocalDateTime subscriptionTime;
	private Integer groupId;
	private String isSubOrg;
	private Integer suborgCount;
	private String parentOrg;
	private String orgFromRemark;

}
