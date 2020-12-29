package com.efreight.hrs.service;

import com.efreight.hrs.entity.Permission;

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
public interface PermissionService extends IService<Permission> {
	Boolean savePermission(Permission permission);

	IPage<Permission> getUserPage(Page page, Permission permission);

	Permission getPermissionByID(Integer permissionId);

	List<Permission> getAllPermissionByRoleID(Integer roleId,Integer orgId);

	List<Permission> getAllPermissionByRoleID1(String roles,Integer orgId);

	List<Permission> getPermissionByRoleID(Integer roleId,Integer orgId);
	List<Permission> getRoleTree();
	List<String> getButtonInfo();

	Boolean updatePermission(Permission permission);
	
	Boolean removeById(Integer permissionId);
	
	List<Permission> getPermissionByOrgID(Integer orgId);

    List<Permission> getPermissionAll();

    List<Permission> getPermissionTreeForHomePage();
}
