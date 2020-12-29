package com.efreight.sc.dao;

import com.efreight.sc.entity.LcOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * LC 订单管理 LC陆运订单 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
public interface LcOrderMapper extends BaseMapper<LcOrder> {

    @Select("select UUID()")
    String getUuid();

    @Select("select * from css_payment_detail a inner join css_payment b on a.payment_id=b.payment_id and b.business_scope=#{businessScope} where a.order_id=#{orderId} and a.org_id=#{orgId}")
    List<Map<String, Object>> getPaymentDetailByOrderId(@Param("orderId") Integer orderId,@Param("businessScope") String businessScope,@Param("orgId") Integer orgId);

    @Select("select workgroup_id from hrs_user_workgroup_detail where user_id = #{currentUserId}")
    List<Integer> getWorkgroupIds(@Param("currentUserId") Integer currentUserId);

    @Select("SELECT city_name_cn FROM af_city WHERE nation_code = 'CN' AND city_code = #{departureStation} LIMIT 1")
    String getCityNameCn(@Param("departureStation") String departureStation);
}
