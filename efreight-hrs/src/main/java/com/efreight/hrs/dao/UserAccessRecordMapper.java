package com.efreight.hrs.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.hrs.entity.UserAccessRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author lc
 * @date 2020/5/12 14:56
 */
public interface UserAccessRecordMapper extends BaseMapper<UserAccessRecord> {

    /**
     * 记录数据自增1
     * @param path
     * @return
     */
    @Update("UPDATE hrs_user_access_record SET records_number = records_number + 1, edit_time = NOW() WHERE user_id=#{userId} and path=#{path}")
    int incrementRecord(@Param("userId")Integer userId, @Param("path") String path);

}
