package com.efreight.prm.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Coop implements Serializable {

	private Integer coop_id;
	private String coop_code;
	private String coop_type;
	private String coop_mnemonic;
	private String coop_name;
	private String short_name;
	private String coop_ename;
	private String short_ename;
	private String social_credit_code;
	private String bank_name;
	private String bank_number;
	private String phone_number;
	private String coop_address;
	private String coop_remark;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date lock_date;
	private String lock_reason;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date blacklist_date;
	private String blacklist_reason;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date whitelist_date;
	private String whitelist_reason;
	private Integer creator_id;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date create_time;
	private Integer editor_id;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date edit_time;
	private Integer org_id;
	private Integer dept_id;
	private Integer coop_status;
	private Integer white_valid;
	private Integer black_valid;

	private Boolean hasChildren;
	
	private String credit_level;
	private String contacts_name;
	private String group_type;
	private String[] coop_types;
	private String id;
	private List<Coop> children;
	private Integer transactor_id;
	@TableField(exist = false)
	@ApiModelProperty(value = "负责人")
	private String transactor_user;

	private Integer lock_valid;
	private String business_scope;
	private String workAddress;
	private String registerAddress;

	//以下用于批量导入
	private String full_address;//注册地址
	private String contacts_name1;//联系人_姓名
	private String phone_number1;//联系人_电话
	private String email;//联系人_邮箱
	private String dept_name;//联系人_部门
	private String job_position;//联系人_职务
	private String errorMessage;//错误信息

	private String coopCodeErrorFlag;//客商资料代码是否错误：1没有，2有
	private String coopTypeErrorFlag;//客商资料代码是否错误：1没有，2有
	private String coopNameErrorFlag;//客商资料中文名称是否错误：1没有，2有
	private String coopENameErrorFlag;//客商资料英文名称是否错误：1没有，2有
	private String socialCreditCodeErrorFlag;//社会信用代码是否错误：1没有，2有
	private String bankNameErrorFlag;//开户行是否错误：1没有，2有
	private String bankNumberErrorFlag;//银行账号是否错误：1没有，2有
	private String phoneNumberErrorFlag;//手机号码是否错误：1没有，2有
	private String coopAddressErrorFlag;//地址是否错误：1没有，2有
	private String coopRemarkErrorFlag;//备注是否错误：1没有，2有
	private String fullAddressErrorFlag;//注册地址是否错误：1没有，2有
	private String contactsName1ErrorFlag;//联系人_姓名是否错误：1没有，2有
	private String emailErrorFlag;//联系人_邮箱是否错误：1没有，2有
	private String phoneNumber1ErrorFlag;//联系人_电话是否错误：1没有，2有
	private String deptNameErrorFlag;//联系人_部门是否错误：1没有，2有
	private String jobPositionErrorFlag;//联系人_职务是否错误：1没有，2有

	private String columnStrs;
	private Integer is_share;
	private Integer coop_org_id;
	private Integer coop_org_coop_id;

	private String coopCodeThree;
	private String iataCode;
	private String cataCertifiedSalesAgents;
	private String financialCode;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date bind_time;
	private String binder;
	private Integer binder_id;
	private Integer is_internal;

}
