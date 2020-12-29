package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_financial_account")
public class FinancialAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "financial_account_id", type = IdType.AUTO)
    private Integer financialAccountId;

    private Integer orgId;

    private Integer parentId;

    private String businessScope;

    private String financialAccountType;

    private String financialAccountName;

    private String financialAccountCode;

    private String manageMode;

    private String subsidiaryAccount;

    private String accountRemark;

    @TableField("financial_account_class_01")
    private Integer financialAccountClass01;

    @TableField("financial_account_class_02")
    private Integer financialAccountClass02;

    @TableField("financial_account_class_03")
    private Integer financialAccountClass03;

    @TableField("financial_account_class_04")
    private Integer financialAccountClass04;

    @TableField("financial_account_class_05")
    private Integer financialAccountClass05;

    private Integer isValid;
    //创建人id
    private Integer creatorId;
    //创建人
    private String creatorName;
    //创建时间
    private LocalDateTime createTime;
    //修改人ID
    private Integer editorId;
    //修改人
    private String editorName;
    //修改时间
    private LocalDateTime editTime;

    private transient Integer idParent;//保存父级的financial_account_id

    private transient String financialAccountCodeOld;
    

    
}
