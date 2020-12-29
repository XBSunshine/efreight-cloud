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
public class CoopAddressBean implements Serializable {

	
	private Integer addr_id;
	private Integer coop_id;
	private String addr_type;
	private String short_name;
	private String full_address;
	private Integer creator_id;
	@JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
	private Date create_time;
	private Integer editor_id;
	@JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
	private Date edit_time;
	private Integer org_id;
	private Integer dept_id;
	private Integer addr_status;
	
}
