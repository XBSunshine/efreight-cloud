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
public class CoopScopeBean implements Serializable {

	private Integer scope_id;
	private Integer coop_id;
	private Integer is_key_client;
	private Integer incharge_id;
	private String business_scope;
	private String credit_level;
	private Double credit_limit;
	private String invoice_type;
	private Integer payment_dept_id;
	private Double income_tax_rate;
	private Integer credit_duration;
	private Integer creator_id;
	@JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
	private Date create_time;
	private Integer editor_id;
	@JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
	private Date edit_time;
	private Integer org_id;
	private Integer dept_id;
	private Integer scope_status;
	private Integer settlement_period;
	/**
	 * 负责人用户名
	 */
	@TableField(exist = false)
	private String incharge_user_name;

	/**
	 * EQ
	 */
	@TableField(exist = false)
	private String settlement_period_name;

}
