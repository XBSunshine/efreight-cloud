package com.efreight.common.security.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserInfo implements Serializable {
	/**
	 * 用户基本信息
	 */
	private UserVo userVo;
	/**
	 * 权限标识集合
	 */
	private String[] permissions;

	/**
	 * 角色集合
	 */
	private Integer[] roles;
}
