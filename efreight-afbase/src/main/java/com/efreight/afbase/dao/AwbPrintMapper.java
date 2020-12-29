package com.efreight.afbase.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.AfAwbPrintShipperConsignee;
import com.efreight.afbase.entity.AfOrderShipperConsignee;
import com.efreight.afbase.entity.AwbPrint;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.procedure.AfPAwbPrintForMawbPrintProcedure;
import com.efreight.afbase.entity.procedure.AfPAwbPrintProcedure;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * AF 操作管理 运单制单 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2019-10-11
 */
public interface AwbPrintMapper extends BaseMapper<AwbPrint> {


    @Select("<script>SELECT \n" +
            " B.awb_print_id AS awbPrintId\n" +
            " ,A.awb_uuid AS awbUuid\n" +
            " ,A.awb_id AS awbId\n" +
            " ,A.awb_number AS awbNumber\n" +
            " ,IFNULL(B.awb_pieces,A.awb_pieces) AS awbPieces\n" +
            " ,IFNULL(B.awb_gross_weight,A.awb_gross_weight) AS awbGrossWeight\n" +
            " ,IFNULL(B.awb_volume,A.awb_volume) AS awbVolume\n" +
            " ,IFNULL(B.awb_charge_weight,A.awb_charge_weight) AS awbChargeWeight\n" +
            " ,CASE WHEN B.awb_status IS NULL THEN '待制单' ELSE B.awb_status  END AS awbStatus\n" +
            " ,CASE WHEN B.departure_station IS NULL THEN A.departure_station ELSE B.departure_station  END AS departureStation\n" +
            " ,IFNULL(B.arrival_station,A.arrival_station) as arrivalStation" +
            " ,IFNULL(B.flight_number,A.expect_flight) as flightNumber" +
            " ,IFNULL(B.flight_date,A.expect_departure) as flightDate" +
            " ,B.create_time AS createTime\n" +
            "FROM " +
            "  (select \n" +
            " max(awb_uuid) as awb_uuid,max(awb_id) as awb_id,max(awb_number) awb_number,max(departure_station) departure_station,max(arrival_station) arrival_station" +
            ",max(expect_flight) expect_flight,max(expect_departure) expect_departure,sum(confirm_pieces) awb_pieces,sum(confirm_weight) awb_gross_weight,sum(confirm_volume) awb_volume,sum(confirm_charge_weight) awb_charge_weight" +
            "  from " +
            "  af_order where org_id = #{awbPrint.orgId} group by awb_id having awb_id is not null) A \n" +
            " LEFT JOIN af_awb_print B ON A.awb_id=B.awb_id and B.awb_type=0\n" +
            " LEFT JOIN af_awb_number C ON A.awb_id=C.awb_id\n" +
            " WHERE 1=1\n" +
            "<when test='awbPrint.awbNumber!=null and awbPrint.awbNumber!=\"\"'>" +
            " AND A.awb_number like  \"%\"#{awbPrint.awbNumber}\"%\"" +
            "</when>" +
            "<when test='awbPrint.flightNumber!=null and awbPrint.flightNumber!=\"\"'>" +
            " AND ((A.expect_flight like  #{awbPrint.flightNumber}\"%\" and B.flight_number is null)  or (B.flight_number is not null and B.flight_number like  #{awbPrint.flightNumber}\"%\"))" +
            "</when>" +
            "<when test='awbPrint.departureStation!=null and awbPrint.departureStation!=\"\"'>" +
            " AND ((A.departure_station =  #{awbPrint.departureStation} and B.departure_station is null) or (B.departure_station is not null and B.departure_station =  #{awbPrint.departureStation}))" +
            "</when>" +
            "<when test='awbPrint.arrivalStation!=null and awbPrint.arrivalStation!=\"\"'>" +
            " AND ((A.arrival_station =  #{awbPrint.arrivalStation} and B.arrival_station is null) or(B.arrival_station is not null and B.arrival_station =  #{awbPrint.arrivalStation}))" +
            "</when>" +
            "<when test='awbPrint.flightDateStart!=null and awbPrint.flightDateEnd!=null'>" +
            " AND ((B.flight_Date <![CDATA[>=]]>  #{awbPrint.flightDateStart} and B.flight_Date <![CDATA[<=]]>  #{awbPrint.flightDateEnd} and B.flight_Date is not null) or " +
            "  (A.expect_departure <![CDATA[>=]]>  #{awbPrint.flightDateStart} and A.expect_departure <![CDATA[<=]]>  #{awbPrint.flightDateEnd} and B.flight_Date is null))" +
            "</when>" +
            "<when test='awbPrint.flightDateStart==null and awbPrint.flightDateEnd!=null'>" +
            " AND ((B.flight_Date <![CDATA[<=]]>  #{awbPrint.flightDateEnd} and B.flight_Date is not null) or (A.expect_departure <![CDATA[<=]]>  #{awbPrint.flightDateEnd} and B.flight_Date is null))" +
            "</when>" +
            "<when test='awbPrint.flightDateStart!=null and awbPrint.flightDateEnd==null'>" +
            " AND ((B.flight_Date <![CDATA[>=]]>  #{awbPrint.flightDateStart} and B.flight_Date is not null) or (A.expect_departure <![CDATA[>=]]>  #{awbPrint.flightDateStart} and B.flight_Date is null))" +
            "</when>" +
            "<when test='awbPrint.awbStatus!=null and awbPrint.awbStatus!=\"\" and awbPrint.awbStatus !=\"全部\" and awbPrint.awbStatus == \"待制单\"'>" +
            " AND B.awb_status IS NULL" +
            "</when>" +
            "<when test='awbPrint.awbStatus!=null and awbPrint.awbStatus!=\"\" and awbPrint.awbStatus !=\"全部\" and awbPrint.awbStatus != \"待制单\"'>" +
            " AND B.awb_status =#{awbPrint.awbStatus}" +
            "</when>" +
            "<when test='awbPrint.awbFromType!=null and awbPrint.awbFromType!=\"\"'>" +
            " AND C.awb_from_type =  #{awbPrint.awbFromType}" +
            "</when>" +
            "</script>")
    IPage<AwbPrint> getPage(Page page, @Param("awbPrint") AwbPrint awbPrint);


    @Select("CALL af_P_awb_print(#{orgId},#{awbPrintType},#{orderUuid},NULL,#{slId})")
    AwbPrint callAfPAwbPrint(AfPAwbPrintProcedure afPAwbPrintProcedure);


    @Select("CALL af_P_awb_print(#{orgId},#{awbPrintType},#{orderUuid},NULL,NULL)")
    AfPAwbPrintForMawbPrintProcedure callAfPAwbPrintForMawbPrint(AfPAwbPrintProcedure afPAwbPrintProcedure);

    @Select("CALL af_P_awb_print(#{orgId},#{awbPrintType},#{orderUuid},#{awbPrintId},NULL)")
    AfPAwbPrintForMawbPrintProcedure callAfPAwbPrintForHawbPrint(AfPAwbPrintProcedure afPAwbPrintProcedure);

    @Select({"<script>",
            "SELECT\n" +
                    "\tc.* \n" +
                    "FROM\n" +
                    "\taf_order_shipper_consignee c\n" +
                    "\tINNER JOIN af_order o ON c.order_id = o.order_id\n" +
                    "\twhere o.order_uuid = #{orderUuid} and c.org_id = #{org_id} and c.sc_type = #{sc_type} and c.sl_id is null\n",
            "</script>"})
    AfOrderShipperConsignee getShipperConsigneeInfo(@Param("org_id") Integer org_id, @Param("orderUuid") String orderUuid, @Param("sc_type") Integer sc_type);

    @Select({"<script>",
            "SELECT\n" +
                    "\tc.* \n" +
                    "FROM\n" +
                    "\taf_order_shipper_consignee c\n" +
                    "\tINNER JOIN af_order o ON c.order_id = o.order_id\n" +
                    "\twhere o.order_uuid = #{orderUuid} and c.org_id = #{org_id} and c.sc_type = #{sc_type} and c.sl_id=#{slId}\n",
            "</script>"})
    AfOrderShipperConsignee getHawbShipperConsigneeInfo(@Param("org_id") Integer org_id, @Param("orderUuid") String orderUuid, @Param("sc_type") Integer sc_type, @Param("slId") Integer slId);

    @Select({"<script>",
            "SELECT\n" +
                    "\tc.* \n" +
                    "FROM\n" +
                    "\taf_awb_print_shipper_consignee c\n" +
                    "\twhere c.awb_print_id = #{awbPrintId} and c.org_id = #{org_id} and c.sc_type = #{sc_type}\n",
            "</script>"})
    AfOrderShipperConsignee getShipperConsigneeInfoByAwbPrintId(@Param("org_id") Integer org_id, @Param("awbPrintId") Integer awbPrintId, @Param("sc_type") Integer sc_type);

    @Select({"<script>",
            "SELECT\n" +
                    "\tc.* \n" +
                    "FROM\n" +
                    "\taf_awb_print_shipper_consignee c\n" +
                    "\twhere c.awb_print_id = #{awbPrintId} and c.org_id = #{org_id} and c.sc_type = #{sc_type}\n",
            "</script>"})
    AfAwbPrintShipperConsignee getAwbPrintShipperConsigneeInfoByAwbPrintId(@Param("org_id") Integer org_id, @Param("awbPrintId") Integer awbPrintId, @Param("sc_type") Integer sc_type);

    @Select({"<script>",
            "SELECT\n" +
                    "\tc.* \n" +
                    "FROM\n" +
                    "\taf_awb_print c\n" +
                    "\twhere c.org_id = #{orgId} and c.awb_print_id = #{awbPrintId}\n",
            "</script>"})
    AwbPrint getAwbPrintById(@Param("orgId") Integer orgId, @Param("awbPrintId") Integer awbPrintId);

    @Select("CALL af_P_API_AMS_MAWB_check(#{type}, #{awbNumber}, #{letterId}, #{userId})")
    String getAmsDataCheck(
            @Param("type") String type,
            @Param("awbNumber") String awbNumber,
            @Param("letterId") String letterId,
            @Param("userId") Integer userId);

    @Update("CALL af_P_AwbPrintpdateSendStatus(#{type},#{awbUuid},#{letterId},#{flag})")
    void updateAwbPrintStatus( @Param("type") String type,  @Param("awbUuid") String awbUuid,  @Param("letterId") String letterId,  @Param("flag") boolean flag);
}
