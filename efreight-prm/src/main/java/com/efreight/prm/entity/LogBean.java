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
public class LogBean implements Serializable {

	private Integer log_id;
	private String op_level;
	private String op_type;
	private String op_name;
	private String op_info;
	private Integer creator_id;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date create_time;
	private Integer org_id;
	private Integer dept_id;
	
	private String creator_name;
	private Date create_time_begin;
	private Date create_time_end;


}
