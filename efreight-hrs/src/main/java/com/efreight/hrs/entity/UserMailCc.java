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
public class UserMailCc implements Serializable{

	private static final long serialVersionUID = 1L;

	private Integer mailCcId;
	private Integer orgId;
	private Integer userId;
	private String permissionName;
	private Integer userIdCc;
	private LocalDateTime createTime;

}
