package com.efreight.ws.afbase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.ws.afbase.entity.WSAwbNumber;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface WSAwbNumberMapper extends BaseMapper<WSAwbNumber> {

    /**
     * 查询主单号
     * @param awbNumber
     * @param orgId 企业ID
     * @return
     */
    @Select("select * from af_awb_number where awb_status='未使用' and org_id = #{orgId} and awb_number=#{awbNumber}")
    WSAwbNumber getAwbNumberByAwbNumber(@Param("orgId") Integer orgId, @Param("awbNumber") String awbNumber);
}
