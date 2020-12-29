package com.efreight.hrs.dao;

import com.efreight.hrs.entity.UserRole;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
public interface UserRoleMapper extends BaseMapper<UserRole> {
	//Boolean deleteByUserId(@Param("userId") Integer userId);
}
