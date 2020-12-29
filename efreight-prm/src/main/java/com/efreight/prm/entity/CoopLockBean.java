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
public class CoopLockBean implements Serializable {

	private Integer lock_id;
	private Integer coop_id;
	private String lock_type;
	private String lock_reason;
	private Integer creator_id;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date create_time;
	private Integer org_id;

}
