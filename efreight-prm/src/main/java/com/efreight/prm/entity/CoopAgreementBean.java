package com.efreight.prm.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CoopAgreementBean implements Serializable {

	private Integer agreement_id;
	private Integer coop_id;
	private String agreement_type;
	private String template;
	private String serial_number;
	@JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
	private Date begin_date;
	@JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
	private Date end_date;
	private Integer incharge_id;
	private String business_scope;
	private String settlement_period;
	private String settlement_remark;
	private Integer signing_dept_id;
	private String payment_standard;
	private String payment_period;
	private Double total_amount;
	private String agreement_remark;
	private String stop_remark;
	private Integer creator_id;
	private Date create_time;
	private Integer editor_id;
	private Date edit_time;
	private Integer org_id;
	private Integer dept_id;
	private Integer agreement_status;
	private String file_name;
	private String file_url;

	private String coop_name;
	private String incharge_name;
	private String signing_dept_name;
	private String creator_name;
	private Date create_time_begin;
	private Date create_time_end;

	private String coop_code;
	private String coop_mnemonic;
	private Integer transactor_id;
	private String user_name;


}
