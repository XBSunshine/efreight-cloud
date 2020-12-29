package com.efreight.prm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CoopExcel {
    private Integer coop_id;
    private String coop_code;
    private String coop_type;
    private String coop_name;
    private String coop_ename;
    private String social_credit_code;
    private String bank_name;
    private String bank_number;
    private String phone_number;
    private String coop_address;
    private String business_scope;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String lock_date;
    private String lock_reason;
    private String black_valid;
    private String blacklist_reason;
    private String white_valid;
    private String whitelist_reason;
    private String creator_name;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date create_time;
    private String editor_name;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date edit_time;
    private String coop_remark;
    private String transactor_user;
    private String lock_status;
    private String black_status;
    private String white_status;
}
