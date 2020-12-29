
package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author zhanghw
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = true)
public class FinancialAccountTree extends TreeNode  implements Serializable{

	private Integer orgId;

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

	//创建人
	private String creatorName;

	//创建时间
	private LocalDateTime createTime;

	//修改人
	private String editorName;

	//修改时间
	private LocalDateTime editTime;

	private String financialAccountCodeParent;

	public FinancialAccountTree() {
	}

	public FinancialAccountTree(int id, String name, int parentId) {
		this.id = id;
		this.parentId = parentId;
	}

	public FinancialAccountTree(int id, String name, FinancialAccountTree parent) {
		this.id = id;
		this.parentId = parent.getId();
	}

	public FinancialAccountTree(FinancialAccount menuVo) {
		this.id = menuVo.getFinancialAccountId();
		this.parentId = menuVo.getParentId();
		this.orgId = menuVo.getOrgId();
		this.businessScope = menuVo.getBusinessScope();
		this.financialAccountType = menuVo.getFinancialAccountType();
		this.financialAccountName = menuVo.getFinancialAccountName();
		this.financialAccountCode = menuVo.getFinancialAccountCode();
		this.manageMode = menuVo.getManageMode();
		this.subsidiaryAccount = menuVo.getSubsidiaryAccount();
		this.accountRemark = menuVo.getAccountRemark();
		this.financialAccountClass01 = menuVo.getFinancialAccountClass01();
		this.financialAccountClass02 = menuVo.getFinancialAccountClass02();
		this.financialAccountClass03 = menuVo.getFinancialAccountClass03();
		this.financialAccountClass04 = menuVo.getFinancialAccountClass04();
		this.financialAccountClass05 = menuVo.getFinancialAccountClass05();
		this.isValid = menuVo.getIsValid();
		this.creatorName = menuVo.getCreatorName();
		this.createTime = menuVo.getCreateTime();
		this.editorName = menuVo.getEditorName();
		this.editTime = menuVo.getEditTime();
	}
}
