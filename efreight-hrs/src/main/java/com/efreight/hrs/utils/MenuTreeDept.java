
package com.efreight.hrs.utils;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.efreight.hrs.entity.Dept;
import com.efreight.hrs.entity.Permission;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhanghw
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = true)
public class MenuTreeDept extends TreeNodeDept  implements Serializable{
	private Integer deptId;
    private String deptCode;
    private String deptName;
    private String shortName;
    private String fullName;
    private Integer managerId;
    private Boolean isProfitunit;
    private Boolean isFinalProfitunit;
    private Integer budgetHc;
    private Integer creatorId;
    private LocalDateTime createTime;
    private Integer editorId;
    private LocalDateTime editTime;
    private LocalDateTime stopDate;
    private Integer stopId;
    private Integer orgId;
    private Boolean deptStatus;
    private String managerName;
    private Integer actualHc;

	public MenuTreeDept() {
	}

	public MenuTreeDept(int id, String name, String deptcode) {
		this.id = id;
//		this.parentId = parentId;
		this.deptName = name;
		this.code=deptcode;
	}

	public MenuTreeDept(int id, String name, MenuTreeDept parent) {
		this.id = id;
//		this.parentId = parent.getId();
		this.deptName = name;
		this.code=parent.getCode();
	}

	public MenuTreeDept(Dept menuVo) {
		this.id = menuVo.getDeptId();
		this.code = menuVo.getDeptCode();
		this.deptId = menuVo.getDeptId();
		this.deptCode=menuVo.getDeptCode();
		this.deptName = menuVo.getDeptName();
		this.shortName = menuVo.getShortName();
		this.fullName = menuVo.getFullName();
		this.managerId = menuVo.getManagerId();
		this.isProfitunit = menuVo.getIsProfitunit();
		this.isFinalProfitunit = menuVo.getIsFinalProfitunit();
		this.budgetHc = menuVo.getBudgetHc();
		this.creatorId = menuVo.getCreatorId();
		this.createTime = menuVo.getCreateTime();
		this.editorId = menuVo.getEditorId();
		this.editTime = menuVo.getEditTime();
		this.stopDate = menuVo.getStopDate();
		this.stopId = menuVo.getStopId();
		this.orgId = menuVo.getOrgId();
		this.deptStatus = menuVo.getDeptStatus();
		this.managerName = menuVo.getManagerName();
		this.actualHc = menuVo.getActualHc();
	}
}
