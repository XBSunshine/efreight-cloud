package com.efreight.hrs.dao;

import com.efreight.hrs.entity.User;
import com.efreight.hrs.entity.UserDept;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author wangxx
 * @since 2019-07-15
 */
public interface UserDeptMapper extends BaseMapper<UserDept> {

    @Update("update hrs_user_dept g set g.dept_id=#{deptId}, g.job_position=#{jobPosition} where g.user_id=#{userId} and g.org_id=#{orgId} and g.isMain = true")
    void updateByUserIdAndDeptId(UserDept userDept);

    @Select("select g.*,d.dept_code,d.dept_name,d.dept_status,u.user_name creator_name from hrs_user_dept g left join hrs_dept d on g.dept_id=d.dept_id left join hrs_user u on g.creator_id=u.user_id where g.user_id=#{userId} and g.org_id = #{orgId}")
    List<UserDept> getByUserId(@Param("userId") String userId, @Param("orgId") Integer orgId);

    @Select("select * from hrs_user g where g.user_id=#{userId} and g.org_id = #{orgId}")
    User selectUser(@Param("userId") String userId, @Param("orgId") String orgId);
}
