package com.efreight.hrs.service.impl;

import com.efreight.hrs.entity.Role;
import com.efreight.hrs.entity.UserRole;
import com.efreight.hrs.dao.UserRoleMapper;
import com.efreight.hrs.service.UserRoleService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

	@Override
	public Boolean UserRole(UserRole userRole) {
		baseMapper.insert(userRole);
		return true;
	}

	@Override
	public Boolean removeRoleByUserId(Integer userId) {
		// TODO Auto-generated method stub
		Map<String,Object> queryWrapper = new HashMap<String,Object>();
		queryWrapper.put("user_Id", userId);
		baseMapper.deleteByMap(queryWrapper);
		return true;
	}

}
