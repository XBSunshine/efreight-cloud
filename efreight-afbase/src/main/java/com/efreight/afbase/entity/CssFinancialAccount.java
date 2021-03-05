package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CSS 财务科目
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_financial_account")
public class CssFinancialAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 科目ID
     */
    @TableId(value = "financial_account_id", type = IdType.AUTO)
    private Integer financialAccountId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 父ID，根目录 父ID=0
     */
    private Integer parentId;

    /**
     * 科目类别：系统
     */
    private String financialAccountType;

    /**
     * 科目名称
     */
    private String financialAccountName;

    /**
     * 科目代码
     */
    private String financialAccountCode;

    /**
     * 管理模式：空/子科目/辅助账
     */
    private String manageMode;

    /**
     * 辅助账设置：空、往来单位、人员、部门
     */
    private String subsidiaryAccount;

    /**
     * 备注
     */
    private String accountRemark;

    /**
     * 科目分类：可设置子集， 1是，0否
     */
    @TableField("financial_account_class_01")
    private Boolean financialAccountClass01;

    /**
     * 科目分类：银行科目，1是，0否
     */
    @TableField("financial_account_class_02")
    private Boolean financialAccountClass02;

    /**
     * 科目分类：费用科目，1是，0否
     */
    @TableField("financial_account_class_03")
    private Boolean financialAccountClass03;

    /**
     * 科目分类：预留
     */
    @TableField("financial_account_class_04")
    private Boolean financialAccountClass04;

    /**
     * 科目分类：预留
     */
    @TableField("financial_account_class_05")
    private Boolean financialAccountClass05;

    /**
     * 是否有效 1 是，0否
     */
    private Boolean isValid;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private Integer editorId;

    private String editorName;

    private LocalDateTime editTime;


}
