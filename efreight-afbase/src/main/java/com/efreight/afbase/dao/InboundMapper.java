package com.efreight.afbase.dao;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.AfRountingSign;
import com.efreight.afbase.entity.Inbound;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface InboundMapper extends BaseMapper<Inbound> {

    @Select("<script>SELECT \n" +
            " MAX(A.awb_uuid) AS awbUuid\n" +
            " ,MAX(A.awb_id) AS awbId\n" +
            " ,MAX(A.awb_number) AS awbNumber\n" +
            " ,MAX(A.order_uuid) AS orderUuid\n" +
            " ,GROUP_CONCAT(A.order_code) AS orderCode\n" +
            " ,CASE WHEN MAX(A.awb_id) IS NULL THEN MAX(B.order_pieces) ELSE MAX(B.awb_pieces)  END AS inboundPieces\n" +
            " ,CASE WHEN MAX(A.awb_id) IS NULL THEN MAX(B.order_gross_weight) ELSE MAX(B.awb_gross_weight)  END AS inboundGrossWeight\n" +
            " ,CASE WHEN MAX(A.awb_id) IS NULL THEN MAX(B.order_volume) ELSE MAX(B.awb_volume)  END AS inboundVolume\n" +
            " ,CASE WHEN MAX(A.awb_id) IS NULL THEN MAX(B.order_charge_weight) ELSE MAX(B.awb_charge_weight)  END AS inboundChargeWeight\n" +
			" ,CASE WHEN (MAX(A.awb_id) IS NOT NULL AND MAX(B.awb_charge_weight) is not null) OR (MAX(A.awb_id) IS NULL AND MAX(B.order_charge_weight) is not null)  THEN Max(B.create_time) ELSE NULL END AS createTime\n" +
			" ,CASE WHEN (MAX(A.awb_id) IS NOT NULL AND MAX(B.awb_charge_weight) is not null) OR (MAX(A.awb_id) IS NULL AND MAX(B.order_charge_weight) is not null)  THEN Max(B.creator_name) ELSE NULL END AS creatorName\n" +
            "FROM af_order A \n" +
            "LEFT JOIN af_inbound B ON A.order_id=B.order_id\n" +
            "WHERE A.org_id=#{inbound.orgId}\n" +
            "and A.business_product!='放单业务' and  A.order_status!='强制关闭'" +
            "<when test='inbound.awbNumber!=null and inbound.awbNumber!=\"\"'>" +
            " AND A.awb_number like  \"%\"#{inbound.awbNumber}\"%\"" +
            "</when>" +
            "<when test='inbound.orderCode!=null and inbound.orderCode!=\"\"'>" +
            " AND (A.order_code like  \"%\"#{inbound.orderCode}\"%\" or A.awb_id in (select awb_id from af_order where awb_id is not null and order_code like  \"%\"#{inbound.orderCode}\"%\" group by awb_id))" +
            "</when>" +
            "<when test='inbound.departureStation!=null and inbound.departureStation!=\"\"'>" +
            " AND A.departure_station =  #{inbound.departureStation}" +
            "</when>" +
            "<when test='inbound.inboundDateStart!=null'>" +
            " AND B.create_time <![CDATA[>=]]>  #{inbound.inboundDateStart}" +
            "</when>" +
            "<when test='inbound.inboundDateEnd!=null'>" +
            " AND B.create_time <![CDATA[<=]]>  #{inbound.inboundDateEnd}" +
            "</when>" +
            "<when test='inbound.inboundStatus!=null and inbound.inboundStatus!=\"\" and inbound.inboundStatus == \"已出重\"'>" +
            " AND ((A.awb_id is not null AND B.awb_charge_weight is not null) or (A.awb_id is null AND B.order_charge_weight is not null))" +
            "</when>" +
            "<when test='inbound.inboundStatus!=null and inbound.inboundStatus!=\"\" and inbound.inboundStatus == \"未出重\"'>" +
            " AND ((A.awb_id is not null AND B.awb_charge_weight is null) or (A.awb_id is null AND B.order_charge_weight is null))" +
            "</when>" +
            "GROUP BY IFNULL(A.awb_id,A.order_id)</script>")
    IPage<Inbound> getPage(Page page, @Param("inbound") Inbound inbound);

    @Update("<script>" +
            "update af_order set confirm_pieces=null,confirm_weight=null,confirm_volume=null,confirm_charge_weight=null,confirm_dimensions=null,confirm_density=null,order_status=#{nodeName},row_uuid=#{rowUuid}" +
            " where order_id = #{orderId} and org_id = #{orgId}" +
            "</script>")
    void updateOrderWhenDeleteInbound(@Param("orderId") Integer orderId, @Param("orgId") Integer orgId, @Param("nodeName")String nodeName, @Param("rowUuid") String rowUuid);


    @Select("<script> \n" +
            " SELECT " +
            "  '' AS inbound_id" +
            " ,MAX(B.awb_number) AS awbNumber" +
            " ,'' AS order_code" +
            " ,'' AS customer_number" +
            " ,MAX(A.order_size) AS order_size" +
            " ,MAX(A.awb_pieces) AS inboundPieces" +
            " ,MAX(A.awb_gross_weight) AS inboundGrossWeight" +
            " ,MAX(A.awb_volume) AS inboundVolume" +
            " ,MAX(A.awb_volume)/0.006 AS inboundVolumeWeight" +
            " ,MAX(A.awb_charge_weight) AS inboundChargeWeight" +
            " ,'' AS order_file_name" +
            " ,'' AS order_file_url from" +
            " (select max(awb_id) as awb_id" +
            " ,GROUP_CONCAT(order_size separator ';') AS order_size" +
            " ,max(awb_pieces) as awb_pieces" +
            " ,max(awb_gross_weight) as awb_gross_weight" +
            " ,max(awb_volume) as awb_volume" +
            " ,max(awb_charge_weight) as awb_charge_weight" +
            " FROM af_inbound" +
            " WHERE awb_id is not null and org_id=#{org_id} and awb_uuid=#{awb_uuid}" +
            " GROUP BY awb_uuid) A" +
            " INNER JOIN af_order B ON A.awb_id=B.awb_id" +
            " GROUP BY B.awb_uuid" +
            " UNION ALL" +
            " SELECT " +
            "  A.inbound_id As inbound_id" +
            " ,case when B.awb_id is null then B.order_code else B.awb_number end awbNumber" +
            " ,B.order_code As order_code" +
            " ,B.customer_number AS customer_number" +
            " ,A.order_size AS order_size" +
            " ,A.order_pieces AS inboundPieces" +
            " ,A.order_gross_weight AS inboundGrossWeight" +
            " ,A.order_volume AS inboundVolume" +
            "  ,A.order_volume/0.006 AS inboundVolumeWeight" +
            " ,A.order_charge_weight AS inboundChargeWeight" +
            " ,A.order_file_name" +
            " ,A.order_file_url" +
            " FROM af_inbound A" +
            " INNER JOIN af_order B ON A.order_id=B.order_id" +
            " WHERE A.org_id=#{org_id} and (A.awb_uuid=#{awb_uuid}" +
            " OR A.order_uuid=#{order_uuid})" +
            "</script>")
    List<Inbound> selectInbounds(@Param("org_id") Integer org_id, @Param("awb_uuid") String awb_uuid, @Param("order_uuid") String order_uuid);

    @Select({"<script>",
            "select * from af_rounting_sign\n",
            "	where order_id = #{order_id}",
            "</script>"})
    AfRountingSign getSignStateByOrderId(@Param("order_id") Integer order_id);
}
