
package com.efreight.prm.util;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.efreight.prm.entity.Coop;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wangxx
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = true)
public class MenuTreeCoop extends TreeNodeCoop  implements Serializable{
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
	
	private String credit_level;
	private String contacts_name;
	private String group_type;

	public MenuTreeCoop() {
	}

	public MenuTreeCoop(int id, String name, String deptcode) {
		this.id = id;
//		this.parentId = parentId;
		this.coop_name = name;
		this.code=deptcode;
	}

	public MenuTreeCoop(int id, String name, MenuTreeCoop parent) {
		this.id = id;
//		this.parentId = parent.getId();
		this.coop_name = name;
		this.code=parent.getCode();
	}

	public MenuTreeCoop(Coop menuVo) {
		this.id = menuVo.getCoop_id();
		this.code = menuVo.getCoop_code();
		this.coop_id = menuVo.getCoop_id();
		this.coop_code = menuVo.getCoop_code();
		this.coop_type = menuVo.getCoop_type();
		this.coop_mnemonic = menuVo.getCoop_mnemonic();
		this.coop_name = menuVo.getCoop_name();
		this.short_name = menuVo.getShort_name();
		this.coop_ename = menuVo.getCoop_ename();
		this.short_ename = menuVo.getShort_ename();
		this.social_credit_code = menuVo.getSocial_credit_code();
		this.bank_name = menuVo.getBank_name();
		this.bank_number = menuVo.getBank_number();
		this.phone_number = menuVo.getPhone_number();
		this.coop_address = menuVo.getCoop_address();
		this.coop_remark = menuVo.getCoop_remark();
		this.lock_date = menuVo.getLock_date();
		this.lock_reason = menuVo.getLock_reason();
		this.blacklist_date = menuVo.getBlacklist_date();
		this.blacklist_reason = menuVo.getBlacklist_reason();
		this.whitelist_date = menuVo.getWhitelist_date();
		this.whitelist_reason = menuVo.getWhitelist_reason();
		this.creator_id = menuVo.getCreator_id();
		this.create_time = menuVo.getCreate_time();
		this.editor_id = menuVo.getEditor_id();
		this.edit_time = menuVo.getEdit_time();
		this.org_id = menuVo.getOrg_id();
		this.dept_id = menuVo.getDept_id();
		this.coop_status = menuVo.getCoop_status();
		this.white_valid = menuVo.getWhite_valid();
		this.black_valid = menuVo.getBlack_valid();
		this.credit_level = menuVo.getCredit_level();
		this.contacts_name = menuVo.getContacts_name();
		this.group_type = menuVo.getGroup_type();
	}
}
