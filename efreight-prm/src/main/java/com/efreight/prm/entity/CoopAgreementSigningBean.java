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
public class CoopAgreementSigningBean implements Serializable {

	private Integer signing_id;
	private Integer agreement_id;
	private Integer coop_id;
	private String signing_type;
	private String serial_number;
	private String pre_serial_number;
	@JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
	private Date begin_date;
	@JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
	private Date end_date;
	@JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
	private Date pre_begin_date;
	@JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
	private Date pre_end_date;
	private Integer incharge_id;
	private Integer pre_incharge_id;
	private String payment_period;
	private Double total_amount;
	private String pre_payment_period;
	private Double pre_total_amount;
	private String remark;
	private String document_file;
	
	private Integer creator_id;
	private Date create_time;
	private Integer editor_id;
	private Date edit_time;
	private Integer org_id;
	private Integer dept_id;
	
	private String coop_name;
	private String incharge_name;
	private String pre_incharge_name;
	private String creator_name;
	private Date create_time_begin;
	private Date create_time_end;


}
