package com.efreight.hrs.service;

import com.efreight.hrs.entity.Role;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
public interface RoleService extends IService<Role> {
	Boolean saveRole(Role role);

	IPage<Role> getRolePage(Page page, Role role);

	Role getRoleByID(Integer roleId);

	Boolean updateRoleById(Role role);

	List<Role> listRolesByUserId(Integer userId);

	Boolean removeRoleById(Integer id);

	Role getRoleByID1(Integer roleId);
}
