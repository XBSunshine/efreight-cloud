package com.efreight.ws.hrs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.ws.hrs.entity.WSAPIConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface WSAPIConfigMapper extends BaseMapper<WSAPIConfig> {

    /**
     * 根据token和接口权限获取企业信息
     * @param authToken 企业值
     * @param apiType 接口权限
     * @return
     */
    @Select("select org_api_config_id, org_id, api_type, auth_token from hrs_org_api_config where enable = 1 and api_type=#{apiType} and auth_token=#{authToken}")
    List<WSAPIConfig> getByApiTypeAndAuth(@Param("authToken") String authToken, @Param("apiType") String apiType);

}
