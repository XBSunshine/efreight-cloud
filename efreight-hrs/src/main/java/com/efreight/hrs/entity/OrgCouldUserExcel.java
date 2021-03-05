package com.efreight.hrs.entity;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OrgCouldUserExcel implements Serializable{

	private String orgType;
	private String orgCode;
	private String orgName;
//	private String oneStopCode;
	private String intendedUser;
	private String demandPersonName;
	private String orgEditionName;
	private Integer userCount;
	private String createTime;
	private String stopDate;
	private String statusFlag;
	private Integer orgUserCount;
	private Integer orgCoopCount;
	private Integer orgOrderCount;
	private String orderTime;
	private Integer subscriptionNum;
	private String subscriptionTime;
	private String orgFromRemark;
	private String adminName;
	private String adminEmail;
	private String adminTel;
	private String orgRemark;
}
