package com.efreight.hrs.service;

import com.efreight.hrs.entity.User;
import com.efreight.hrs.entity.UserRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
public interface UserRoleService extends IService<UserRole> {
	Boolean UserRole(UserRole userRole);
	Boolean removeRoleByUserId(Integer userId);
}
