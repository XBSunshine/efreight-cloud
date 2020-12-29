
package com.efreight.hrs.utils;

import java.io.Serializable;

import com.efreight.hrs.entity.Permission;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhanghw
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = true)
public class MenuTree extends TreeNode  implements Serializable{
	private String icon;
	private String name;
	private boolean spread = false;
	private String path;
	private String url;
	private String authority;
	private String redirect;
	private String keepAlive;
	private String code;
	private String type;
	private String label;
	private Integer sort;
	private String disabled1;
	private String adminDefault;

	public MenuTree() {
	}

	public MenuTree(int id, String name, int parentId) {
		this.id = id;
		this.parentId = parentId;
		this.name = name;
		this.label = name;
	}

	public MenuTree(int id, String name, MenuTree parent) {
		this.id = id;
		this.parentId = parent.getId();
		this.name = name;
		this.label = name;
	}

	public MenuTree(Permission menuVo) {
		this.id = menuVo.getPermissionId();
		this.parentId = menuVo.getParentId();
		this.icon = menuVo.getIcon();
		this.name = menuVo.getPermissionName();
		this.path = menuVo.getPath();
		this.url = menuVo.getUrl();
		this.type = menuVo.getPermissionType();
		this.label = menuVo.getPermissionName();
		this.sort = menuVo.getSort();
		this.disabled1 = menuVo.getDisabled();
		this.adminDefault = menuVo.getAdminDefault();
		//this.keepAlive = menuVo.g;
	}
}
