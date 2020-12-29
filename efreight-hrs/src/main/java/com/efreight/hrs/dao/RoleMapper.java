package com.efreight.hrs.dao;

import com.efreight.hrs.entity.Role;

import java.util.List;

import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
public interface RoleMapper extends BaseMapper<Role> {
	//Role selectByRoleId(Integer roleId);

	//void updateByRoleId(Role role);

	@Select("SELECT			hrs_role.* from 	hrs_role\n"
			+ "					LEFT JOIN hrs_user_role ON hrs_role.role_id = hrs_user_role.role_id\n"
			+ "					WHERE		hrs_role.role_status=1\n"
			+ "					AND hrs_user_role.user_id = #{userId}	")
	List<Role> listRolesByUserId(@Param("userId") Integer userId);
}
