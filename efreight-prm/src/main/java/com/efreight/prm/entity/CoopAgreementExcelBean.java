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
public class CoopAgreementExcelBean implements Serializable {


	private String coop_name;
	private String agreement_type;
	private String template;
	private String serial_number;
	private String begin_date;
	private String end_date;
	private String incharge_name;
	private String business_scope;
	private String settlement_period;
	
	private String signing_dept_name;
	private String payment_standard;
	private String payment_period;
	private Double total_amount;

	private String agreement_status;
	

	


}
