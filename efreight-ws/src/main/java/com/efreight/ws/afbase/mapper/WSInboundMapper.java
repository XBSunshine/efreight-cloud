package com.efreight.ws.afbase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.ws.afbase.entity.WSInbound;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface WSInboundMapper extends BaseMapper<WSInbound> {
    /**
     * 查询出重信息
     * @param orderId 订单ID
     * @return
     */
    @Select("select * from af_inbound where order_id = #{orderId}")
    List<WSInbound> getByOrderId(@Param("orderId") Integer orderId);
}
