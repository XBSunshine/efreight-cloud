package com.efreight.afbase.dao;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.LogBean;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface LogMapper extends BaseMapper<LogBean> {

    @Update("update af_log set" +
            " creat_time=null," +
            "creator_name=null," +
            "creator_id=null" +
            " where org_id=#{orgId}" +
            " and order_uuid=#{orderUuid} and node_name in('货物出重','单货匹配')")
    void modifyForDeleteInbound(LogBean logBean);
    
    @Update("update af_log set" +
            " creat_time=#{creat_time}," +
            "creator_name=#{creator_name}," +
            "creator_id=#{creator_id}" +
            " where org_id=#{org_id}" +
            " and order_uuid=#{order_uuid} and node_name=#{node_name}")
    void updateLog(@Param("org_id") Integer org_id,@Param("order_uuid") String order_uuid,@Param("node_name") String node_name,
    		@Param("creator_id") Integer creator_id,@Param("creator_name") String creator_name,@Param("creat_time") LocalDateTime creat_time);
    @Update("update af_log set" +
    		" creat_time=NULL," +
    		" creator_name=NULL," +
    		" creator_id=NULL" +
    		" where org_id=#{org_id}" +
    		" and order_uuid=#{order_uuid} and node_name=#{node_name}")
    void updateLog2(@Param("org_id") Integer org_id,@Param("order_uuid") String order_uuid,@Param("node_name") String node_name);
}
