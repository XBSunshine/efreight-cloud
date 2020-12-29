package com.efreight.prm.entity;


import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PrmCoopShareFields implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String fieldsName;
	private String isShare;
	private String isSubscribe;
	
	//前段访问
	private String name;
	private Integer coopId; 
	private String businessScope;
	private List<PrmCoopShareFields> subList;

}
