package com.efreight.common.security.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CoopVo implements Serializable {

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
    private String workAddress;
    private String registerAddress;
    private Integer is_share;
    private Integer coop_org_id;
    private Integer coop_org_coop_id;
    private Integer is_internal;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date bind_time;
    private String binder;
    private Integer binder_id;
    private String financialCode;
    private String cataCertifiedSalesAgents;
    private String iataCode;
    private String coopCodeThree;
    private Integer transactor_id;


}
