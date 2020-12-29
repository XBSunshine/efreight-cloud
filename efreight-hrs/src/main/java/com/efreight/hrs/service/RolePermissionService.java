package com.efreight.hrs.service;

import com.efreight.hrs.entity.RolePermission;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
public interface RolePermissionService extends IService<RolePermission> {
	public Boolean saveRolePermission(Integer orgId, Integer roleId, String menuIds) ;
}
