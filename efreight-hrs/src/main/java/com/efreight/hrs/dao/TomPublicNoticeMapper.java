package com.efreight.hrs.dao;

import com.efreight.hrs.entity.TomPublicNotice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.hrs.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author wangxx
 * @since 2019-07-15
 */
public interface TomPublicNoticeMapper extends BaseMapper<TomPublicNotice> {
    @Select("select * from hrs_user")
    List<User> findAllUser();

    @Select("select * from hrs_user g where g.user_id in (select admin_id from hrs_org)")
    List<User> findAllAdmin();

    @Select("select * from hrs_user g where g.org_id = #{org}")
    List<User> findAllUserByOrgId(@Param("org") Integer org);
}
