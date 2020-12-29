package com.efreight.ws.afbase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.ws.afbase.entity.WSOrderShipperConsignee;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface WSOrderShipperConsigneeMapper extends BaseMapper<WSOrderShipperConsignee> {

    /**
     * 获取收货人信息
     * @param orderId 订单ID
     * @return
     */
    @Select("select * from af_order_shipper_consignee where order_id=#{orderId} and sc_type = 1")
    WSOrderShipperConsignee getConsigneeByOrderId(@Param("orderId") Integer orderId);

    /**
     * 获取发货人信息
     * @param orderId 订单ID
     * @return
     */
    @Select("select * from af_order_shipper_consignee where order_id=#{orderId} and sc_type = 0")
    WSOrderShipperConsignee getConsignorByOrderId(@Param("orderId") Integer orderId);
}
