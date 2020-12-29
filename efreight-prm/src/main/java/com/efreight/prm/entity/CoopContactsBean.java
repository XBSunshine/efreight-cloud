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
public class CoopContactsBean implements Serializable {

	private Integer contacts_id;
	private Integer coop_id;
	private String contacts_type;
	private String contacts_name;
	private String phone_number;
	private String email;
	private String dept_name;
	private String job_position;
	private String tel_number;
	private Integer creator_id;
	@JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
	private Date create_time;
	private Integer editor_id;
	@JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
	private Date edit_time;
	private Integer org_id;
	private Integer dept_id;
	private Integer contacts_status;

}
