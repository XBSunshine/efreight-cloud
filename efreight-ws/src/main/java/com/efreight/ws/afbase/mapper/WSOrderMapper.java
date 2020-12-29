package com.efreight.ws.afbase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.ws.afbase.entity.WSAirPort;
import com.efreight.ws.afbase.entity.WSOrder;
import com.efreight.ws.afbase.entity.WSWarehouse;
import com.efreight.ws.afbase.pojo.order.detail.OrderCost;
import com.efreight.ws.afbase.pojo.order.detail.OrderDetail;
import com.efreight.ws.afbase.pojo.order.detail.OrderIncome;
import com.efreight.ws.afbase.pojo.order.detail.ShipperLetter;
import com.efreight.ws.hrs.entity.WSUser;
import com.efreight.ws.prm.entity.WSCoop;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface WSOrderMapper extends BaseMapper<WSOrder> {

    /**
     * 查询客户信息
     * @param orgId 企业ID
     * @param coopCode 客户号
     * @return
     */
    @Select("select * from prm_coop where coop_status = 1 and  org_id = #{orgId} and coop_code = #{coopCode}")
    WSCoop getCoopByCode(@Param("orgId") Integer orgId, @Param("coopCode") String coopCode);

    /**
     * 查询货栈
      * @param orgId 企业ID
     * @param warehouseCode 货栈代码
     * @return
     */
    @Select("select * from af_warehouse where customs_supervision='普货库' " +
            "and warehouse_status=1 and org_id=#{orgId} and warehouse_code=#{warehouseCode}")
    WSWarehouse getWarehouseByCodeAndApCode(@Param("orgId") Integer orgId, @Param("warehouseCode") String warehouseCode);

    /**
     * 查询机场信息
     * @param apCode 机场代码
     * @return
     */
    @Select("select * from af_airport where ap_status=1 and ap_code=#{apCode}")
    WSAirPort getAriPortByApCode(@Param("apCode") String apCode);

    /**
     * 根据中文查询启用用户信息
     * @param userName 中文用户名
     * @param orgId 企业ID
     * @return
     */
    @Select("select * from hrs_user where user_status = 1 and org_id = #{orgId} and user_name = #{userName} ")
    WSUser getUserByName(@Param("orgId")Integer orgId, @Param("userName") String userName);

    /**
     * 查询最大订单号
     * @param orgId 企业ID
     * @param businessScope 业务域
     * @return
     */
    @Select("select max(order_code) from af_order where org_id=#{orgId} and  order_code like CONCAT(#{businessScope}, '%')")
    String maxOrderCode(@Param("orgId") Integer orgId, @Param("businessScope") String businessScope);

    /**
     * 查询订单信息
     * @param orgId 企业ID
     * @param orderCode 订单号
     * @return
     */
    @Select("select * from af_order where org_id = #{orgId} and order_code = #{orderCode}")
    WSOrder getOrderByOrderCode(@Param("orgId") Integer orgId, @Param("orderCode") String orderCode);

    /**
     * 订单出重：货栈代码信息查询
     * @param orgId 企业ID
     * @param warehouseCode 货栈代码
     * @param apCode 机场代码
     * @return
     */
    @Select("select * from af_warehouse where customs_supervision in('一级监管','二级监管')" +
            "and warehouse_status=1 and org_id=#{orgId} and warehouse_code=#{warehouseCode} and ap_code=#{apCode}")
    WSWarehouse getInboundWarehouse(@Param("orgId") Integer orgId, @Param("warehouseCode") String warehouseCode, @Param("apCode") String apCode);

    /**
     * 订单详情：基本信息
     * @param orgId 企业ID
     * @param orderCode 订单代码
     * @return
     */
    @Select("call ws_detail_order(#{orgId}, #{orderCode})")
    OrderDetail getOrderDetail(@Param("orgId") Integer orgId, @Param("orderCode") String orderCode);

    /**
     * 订单详情：获取分单信息
     * @param orgId 企业ID
     * @param orderId 订单ID
     * @return
     */
    @Select("select " +
            "hawb_number as hawbNumber," +
            "arrival_station as arrivalStation," +
            "goods_name_cn as goodsNameCn," +
            "plan_pieces as planPieces," +
            "plan_weight as planWeight" +
            " from af_shipper_letter where sl_type='HAWB' and org_id=#{orgId} and order_id=#{orderId}")
    List<ShipperLetter> getOrderShipperLetter(@Param("orgId") Integer orgId, @Param("orderId")Integer orderId);

    /**
     * 订单详情：获取应付信息
     * @param orgId 企业ID
     * @param orderId 订单ID
     * @return
     */
    @Select("select " +
            "PC.coop_code as customerCode," +
            "I.income_quantity as quantity," +
            "I.income_unit_price as unitPrice," +
            "I.income_currency as currency," +
            "I.income_amount as amount," +
            "I.income_functional_amount as functionalAmount" +
            " from af_income I left join prm_coop PC " +
            " on I.customer_id = PC.coop_id " +
            " where I.org_id=#{orgId} and I.order_id=#{orderId}")
    List<OrderIncome> getOrderIncome(@Param("orgId")Integer orgId, @Param("orderId") Integer orderId);

    /**
     * 订单详情：获取应付消息
     * @param orgId 企业Id
     * @param orderId 订单ID
     * @return
     */
    @Select("select " +
            "financial_date as financialDate," +
            "customer_name as customerName" +
            " from af_cost where org_id=#{orgId} and order_id=#{orderId}")
    List<OrderCost> getOrderCost(@Param("orgId")Integer orgId, @Param("orderId") Integer orderId);
}
