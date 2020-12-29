package com.efreight.hrs.controller;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.hrs.entity.Role;
import com.efreight.hrs.service.RolePermissionService;
import com.efreight.hrs.service.RoleService;

import lombok.AllArgsConstructor;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
@RestController
@AllArgsConstructor
@RequestMapping("/role")
public class RoleController {
	private final RoleService roleService;
	private final RolePermissionService rolePermissionService;

	/**
	 * 通过ID查询角色信息
	 *
	 * @param id ID
	 * @return 角色信息
	 */
	@GetMapping("/{id}")
	public MessageInfo getById(@PathVariable Integer id) {
		return MessageInfo.ok(roleService.getById(id));
	}

	/**
	 * 添加角色
	 *
	 * @param role 角色信息
	 * @return success、false
	 */
	// @SysLog("添加角色")
	@PostMapping("/save")
	@PreAuthorize("@pms.hasPermission('sys_role_add')")
	public MessageInfo save(@Valid @RequestBody Role role) {
		return MessageInfo.ok(roleService.saveRole(role));
	}

	/**
	 * 修改角色
	 *
	 * @param role 角色信息
	 * @return success/false
	 */
//	@SysLog("修改角色")
	@PutMapping("/edit")
	@PreAuthorize("@pms.hasPermission('sys_role_edit')")
	public MessageInfo update(@Valid @RequestBody Role role) {
		return MessageInfo.ok(roleService.updateRoleById(role));
	}

	/**
	 * 删除角色
	 *
	 * @param id
	 * @return
	 */
//	@SysLog("删除角色")
	@DeleteMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('sys_role_del')")
	public MessageInfo removeById(@PathVariable Integer id) {
		return MessageInfo.ok(roleService.removeRoleById(id));
	}

	/**
	 * 获取角色列表
	 *
	 * @return 角色列表
	 */
	@GetMapping("/list")
	public MessageInfo listRoles() {
		return MessageInfo.ok(roleService.list());
	}

	/**
	 * 分页查询角色信息
	 *
	 * @param page 分页对象
	 * @return 分页对象
	 */
	@GetMapping("/page")
	public MessageInfo getRolePage(Page page,Role role) {
		return MessageInfo.ok(roleService.getRolePage(page,role));
	}

	/**
	 * 更新角色菜单
	 *
	 * @param para
	 * @param
	 * @return success、false
	 */
//	@SysLog("更新角色菜单")
	@PutMapping("/menu")
	@PreAuthorize("@pms.hasPermission('sys_role_edit')")
	public MessageInfo saveRoleMenus(@RequestBody Map<String, Object> para) {
		Role role = roleService.getById(Integer.parseInt(para.get("roleId").toString()));
		return MessageInfo.ok(rolePermissionService.saveRolePermission(Integer.parseInt(para.get("orgId").toString()),
				Integer.parseInt(para.get("roleId").toString()), para.get("permissionIds").toString()));
	}

}
