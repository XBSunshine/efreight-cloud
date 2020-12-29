package com.efreight.afbase.dao;

import java.util.List;

import com.efreight.afbase.entity.WarehouseLetter;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.Letters;
import com.efreight.afbase.entity.OperationPlan;
import com.efreight.afbase.entity.OperationPlanExcel;

public interface OperationPlanMapper extends BaseMapper<OperationPlan> {
	@Select({"<script>",
		   "    SELECT a.* ,",
		   "b.coop_name,b.coop_code ",
		   " ,c.warehouse_name_cn departureWarehouseName,d.warehouse_name_cn departureStorehouseName",
			 "	FROM af_order a",
			 "	LEFT JOIN prm_coop b",
			 "	ON a.coop_id=b.coop_id",
			 " LEFT JOIN af_warehouse c",
			 " ON a.departure_warehouse_id=c.warehouse_id",
			 " LEFT JOIN af_warehouse d",
			 " ON a.departure_storehouse_id=d.warehouse_id",
			 " WHERE 1=1 and a.org_id=#{bean.orgId} and a.order_status!='强制关闭' and a.order_status!='财务锁账'",
			 "<when test='bean.awbNumber!=null and bean.awbNumber!=\"\"'>",
			    " AND a.awb_number like  \"%\"#{bean.awbNumber}\"%\"",
			    "</when>",
			    "<when test='bean.orderCode!=null and bean.orderCode!=\"\"'>",
			    " AND a.order_code like  \"%\"#{bean.orderCode}\"%\"",
			    "</when>",
			    "<when test='bean.customerNumber!=null and bean.customerNumber!=\"\"'>",
			    " AND a.customer_number like  \"%\"#{bean.customerNumber}\"%\"",
			    "</when>",
			    "<when test='bean.businessProduct!=null and bean.businessProduct!=\"\"'>",
			    " AND a.business_product != #{bean.businessProduct}",
			    "</when>",
			    "<when test='bean.departureStation!=null and bean.departureStation!=\"\"'>",
			    " AND a.departure_station = #{bean.departureStation}",
			    "</when>",
			    "<when test='bean.arrivalStation!=null and bean.arrivalStation!=\"\"'>",
			    " AND a.arrival_station = #{bean.arrivalStation}",
			    "</when>",
			    "<when test='bean.departureWarehouseId!=null and bean.departureWarehouseId!=\"\"'>",
			    " AND a.departure_warehouse_id = #{bean.departureWarehouseId}",
			    "</when>",
			    "<when test='bean.departureStorehouseId!=null and bean.departureStorehouseId!=\"\"'>",
			    " AND a.departure_storehouse_id = #{bean.departureStorehouseId}",
			    "</when>",
			    "<when test='bean.expectFlight!=null and bean.expectFlight!=\"\"'>",
			    " AND a.expect_flight like  \"%\"#{bean.expectFlight}\"%\"",
			    "</when>",
			    "<when test='bean.flightDateBegin!=null and bean.flightDateBegin!=\"\"'>",
			    " AND a.expect_departure  <![CDATA[ >= ]]> #{bean.flightDateBegin}",
			    "</when>",
			    "<when test='bean.flightDateEnd!=null and bean.flightDateEnd!=\"\"'>",
			    " AND a.expect_departure <![CDATA[ <= ]]> #{bean.flightDateEnd}",
			    "</when>",
			    "<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
			    " AND a.create_time  <![CDATA[ >= ]]> #{bean.createTimeBegin}",
			    "</when>",
			    "<when test='bean.createTimeEnd!=null and bean.createTimeEnd!=\"\"'>",
			    " AND a.create_time <![CDATA[ <= ]]> #{bean.createTimeEnd}",
			    "</when>",
			    "<when test='bean.receiptDateStart!=null and bean.receiptDateStart!=\"\"'>",
			    " AND IFNULL(a.receipt_date,a.expect_departure)  <![CDATA[ >= ]]> #{bean.receiptDateStart}",
			    "</when>",
			    "<when test='bean.receiptDateEnd!=null and bean.receiptDateEnd!=\"\"'>",
			    " AND IFNULL(a.receipt_date,a.expect_departure) <![CDATA[ <= ]]> #{bean.receiptDateEnd}",
			    "</when>",
			    "<when test='bean.servicerName!=null and bean.servicerName!=\"\"'>",
			    " AND servicer_name like  \"%\"#{bean.servicerName}\"%\"",
			    "</when>",
			    "<when test='bean.salesName!=null and bean.salesName!=\"\"'>",
			    " AND sales_name like  \"%\"#{bean.salesName}\"%\"",
			    "</when>",
			    "<when test='bean.salesId!=null and bean.salesId!=\"\"'>",
			    " AND sales_id = #{bean.salesId}",
			    "</when>",
			    "<when test='bean.servicerId!=null and bean.servicerId!=\"\"'>",
			    " AND servicer_id = #{bean.servicerId}",
			    "</when>",
			    "<when test='bean.coopName!=null and bean.coopName!=\"\"'>",
			    " AND (b.coop_name like  \"%\"#{bean.coopName}\"%\" or upper(b.coop_code) like  \"%\"#{bean.coopName}\"%\")",
			    "</when>",
			    "<when test='bean.creatorName!=null and bean.creatorName!=\"\"'>",
			    " AND a.creator_name like  \"%\"#{bean.creatorName}\"%\"",
			    "</when>",
			    "<when test='bean.inboundDateStart!=null and bean.inboundDateStart!=\"\"'>",
			    " AND a.inbound_date  <![CDATA[ >= ]]> #{bean.inboundDateStart}",
			    "</when>",
			    "<when test='bean.inboundDateEnd!=null and bean.inboundDateEnd!=\"\"'>",
			    " AND a.inbound_date <![CDATA[ <= ]]> #{bean.inboundDateEnd}",
			    "</when>",
			    "<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
			    " AND a.business_scope  = #{bean.businessScope}",
			    "</when>",
		    " order by a.receipt_date ",
		  "</script>"})
	IPage<OperationPlan> getListPage(Page page,@Param("bean") OperationPlan bean);
	@Select({"<script>",
		"CALL af_P_shipper_letter_print(#{org_id},#{awbUUIds})\n",
	"</script>"})
	List<Letters> printLetters(@Param("org_id") Integer org_id,@Param("awbUUIds") String awbUUIds);
	@Select({"<script>",
		" SELECT  W.business_scope AS businessScope,W.warehouse_name_cn AS warehouseNameCn,E.shipper_template_file AS letter_pdf,B.awb_number AS Input001 ",
		" FROM af_order B ",
		" LEFT JOIN af_warehouse W ON W.warehouse_id=B.departure_warehouse_id ",
		" LEFT JOIN af_warehouse_letter E ON E.warehouse_letter_id=W.shipper_template ",
		" WHERE B.org_id=#{org_id} and B.awb_uuid in (${awb_uuid}) \n",
	"</script>"})
	List<Letters> checkLetters(@Param("org_id") Integer org_id,@Param("awb_uuid") String awb_uuid);
	@Select({"<script>",
		" SELECT ",
		" b.coop_code,",
		" b.coop_name,",
		" a.awb_number,",
		" a.customer_number,",
		" CONCAT(a.plan_pieces,' ',a.package_type,'/',a.plan_weight,'/',a.plan_volume) plan_pieces,",
		" CONCAT('品名：',a.goods_name_cn,'\n','电池：',a.battery_type,'\n','鉴定：',IFNULL(a.appraisal_note,''))  AS goodsNameCn,",
		" CONCAT('分单情况：',CASE a.hawb_quantity WHEN 0 THEN '直单;' ELSE CONCAT('1主',a.hawb_quantity,'分;','\n',d.hawb_number_str,';') END,'\n','操作要求：',IFNULL(a.operation_remark,'')) hawbQuantity,",
		" CONCAT('目的港：',a.arrival_station,'\n','预订航班：',a.expect_flight,'\n','航班日期：',a.expect_departure) arrival_station,",
		" left(a.servicer_name,LOCATE(' ',a.servicer_name)-1) AS servicer_name",
		" FROM af_order a",
		" LEFT JOIN prm_coop b",
		" ON a.coop_id=b.coop_id",
		/*" LEFT JOIN (",
		"  SELECT A.awb_id",
		" , CAST(GROUP_CONCAT(DISTINCT sl_remark) AS CHAR)  AS mawb_sl_remark ",
		" FROM af_shipper_letter A ",
		" WHERE A.sl_type='MAWB'",
		" GROUP BY A.awb_id",
		" ) AS c ON a.awb_id=c.awb_id",*/
		" LEFT JOIN (",
		"  SELECT A.order_id,",
		"  REPLACE(GROUP_CONCAT(CONCAT(A.hawb_number,'/',A.plan_pieces,'件')),',',';\n') AS hawb_number_str ",
		/*"  ,CAST(GROUP_CONCAT(DISTINCT sl_remark) AS CHAR)  AS hawb_sl_remark",*/
		"  FROM af_shipper_letter A",
		"  WHERE A.sl_type='HAWB'",
		" GROUP BY A.order_id",
		" ) AS d ON a.order_id=d.order_id",
		 " where a.org_id=#{org_id} and a.order_id in (${orderIds})\n",
			" order by a.receipt_date ",
	"</script>"})
	List<OperationPlanExcel> queryListForExcle(@Param("org_id") Integer org_id,@Param("orderIds") String orderIds);

    @Select({"<script>",
            " select E.* from  af_warehouse_letter E\n" +
                    "where E.is_valid = 1 and E.ap_code in (SELECT\n" +
                    " W.ap_code  \n" +
                    "FROM\n" +
                    " af_order B\n" +
                    " LEFT JOIN af_warehouse W ON W.warehouse_id = B.departure_warehouse_id\n" +
                    " WHERE\n" +
                    " B.org_id = #{org_id} \n" +
                    " AND B.awb_uuid IN (${awb_uuid})) " ,
            "</script>"})
    List<WarehouseLetter> checkWarehouseLetter(@Param("org_id") Integer org_id, @Param("awb_uuid") String awb_uuid);

	@Select({"<script>",
			" select E.* from  af_warehouse_letter E\n" +
					"where E.is_valid = 1 and E.ap_code in (SELECT\n" +
					" W.ap_code  \n" +
					"FROM\n" +
					" af_order B\n" +
					" LEFT JOIN af_warehouse W ON W.warehouse_id = B.departure_warehouse_id\n" +
					" WHERE\n" +
					" B.org_id = #{org_id} \n" +
					" AND B.awb_uuid IN (${awb_uuid})) " ,
			"</script>"})
	IPage<WarehouseLetter> selectTemplate(Page page,@Param("org_id") Integer org_id, @Param("awb_uuid") String awb_uuid);

	@Select({"<script>",
			"CALL af_P_order_goods_arrival_plan(#{org_id},#{orderIds})\n",
			"</script>"})
	List<OperationPlanExcel> callListForExcel(@Param("org_id") Integer org_id,@Param("orderIds") String orderIds);

	@Select({"<script>",
			" SELECT E.shipper_template_file_excel AS letter_excel ",
			" FROM af_order B ",
			" LEFT JOIN af_warehouse W ON W.warehouse_id=B.departure_warehouse_id ",
			" LEFT JOIN af_warehouse_letter E ON E.warehouse_letter_id=W.shipper_template ",
			" WHERE B.org_id=#{org_id} and B.awb_uuid in (${awb_uuid}) \n",
			"</script>"})
	List<Letters> isExistExcelTemplate(@Param("org_id") Integer org_id,@Param("awb_uuid") String awb_uuid);

	@Select({"<script>",
			"    SELECT a.* ,",
			"b.coop_name,b.coop_code ",
			" ,c.warehouse_name_cn departureWarehouseName,d.warehouse_name_cn departureStorehouseName",
			"	FROM af_order a",
			"	LEFT JOIN prm_coop b",
			"	ON a.coop_id=b.coop_id",
			" LEFT JOIN af_warehouse c",
			" ON a.departure_warehouse_id=c.warehouse_id",
			" LEFT JOIN af_warehouse d",
			" ON a.departure_storehouse_id=d.warehouse_id",
			" WHERE 1=1 and a.org_id=#{bean.orgId} and a.order_status!='强制关闭' and a.order_status!='财务锁账'",
			"<when test='bean.awbNumber!=null and bean.awbNumber!=\"\"'>",
			" AND a.awb_number like  \"%\"#{bean.awbNumber}\"%\"",
			"</when>",
			"<when test='bean.orderCode!=null and bean.orderCode!=\"\"'>",
			" AND a.order_code like  \"%\"#{bean.orderCode}\"%\"",
			"</when>",
			"<when test='bean.customerNumber!=null and bean.customerNumber!=\"\"'>",
			" AND a.customer_number like  \"%\"#{bean.customerNumber}\"%\"",
			"</when>",
			"<when test='bean.businessProduct!=null and bean.businessProduct!=\"\"'>",
			" AND a.business_product != #{bean.businessProduct}",
			"</when>",
			"<when test='bean.departureStation!=null and bean.departureStation!=\"\"'>",
			" AND a.departure_station = #{bean.departureStation}",
			"</when>",
			"<when test='bean.arrivalStation!=null and bean.arrivalStation!=\"\"'>",
			" AND a.arrival_station = #{bean.arrivalStation}",
			"</when>",
			"<when test='bean.departureWarehouseId!=null and bean.departureWarehouseId!=\"\"'>",
			" AND a.departure_warehouse_id = #{bean.departureWarehouseId}",
			"</when>",
			"<when test='bean.departureStorehouseId!=null and bean.departureStorehouseId!=\"\"'>",
			" AND a.departure_storehouse_id = #{bean.departureStorehouseId}",
			"</when>",
			"<when test='bean.expectFlight!=null and bean.expectFlight!=\"\"'>",
			" AND a.expect_flight like  \"%\"#{bean.expectFlight}\"%\"",
			"</when>",
			"<when test='bean.flightDateBegin!=null and bean.flightDateBegin!=\"\"'>",
			" AND a.expect_departure  <![CDATA[ >= ]]> #{bean.flightDateBegin}",
			"</when>",
			"<when test='bean.flightDateEnd!=null and bean.flightDateEnd!=\"\"'>",
			" AND a.expect_departure <![CDATA[ <= ]]> #{bean.flightDateEnd}",
			"</when>",
			"<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
			" AND a.create_time  <![CDATA[ >= ]]> #{bean.createTimeBegin}",
			"</when>",
			"<when test='bean.createTimeEnd!=null and bean.createTimeEnd!=\"\"'>",
			" AND a.create_time <![CDATA[ <= ]]> #{bean.createTimeEnd}",
			"</when>",
			"<when test='bean.receiptDateStart!=null and bean.receiptDateStart!=\"\"'>",
			" AND IFNULL(a.receipt_date,a.expect_departure)  <![CDATA[ >= ]]> #{bean.receiptDateStart}",
			"</when>",
			"<when test='bean.receiptDateEnd!=null and bean.receiptDateEnd!=\"\"'>",
			" AND IFNULL(a.receipt_date,a.expect_departure) <![CDATA[ <= ]]> #{bean.receiptDateEnd}",
			"</when>",
			"<when test='bean.servicerName!=null and bean.servicerName!=\"\"'>",
			" AND servicer_name like  \"%\"#{bean.servicerName}\"%\"",
			"</when>",
			"<when test='bean.salesName!=null and bean.salesName!=\"\"'>",
			" AND sales_name like  \"%\"#{bean.salesName}\"%\"",
			"</when>",
			"<when test='bean.salesId!=null and bean.salesId!=\"\"'>",
			" AND sales_id = #{bean.salesId}",
			"</when>",
			"<when test='bean.servicerId!=null and bean.servicerId!=\"\"'>",
			" AND servicer_id = #{bean.servicerId}",
			"</when>",
			"<when test='bean.coopName!=null and bean.coopName!=\"\"'>",
			" AND (b.coop_name like  \"%\"#{bean.coopName}\"%\" or upper(b.coop_code) like  \"%\"#{bean.coopName}\"%\")",
			"</when>",
			"<when test='bean.creatorName!=null and bean.creatorName!=\"\"'>",
			" AND a.creator_name like  \"%\"#{bean.creatorName}\"%\"",
			"</when>",
			"<when test='bean.inboundDateStart!=null and bean.inboundDateStart!=\"\"'>",
			" AND a.inbound_date  <![CDATA[ >= ]]> #{bean.inboundDateStart}",
			"</when>",
			"<when test='bean.inboundDateEnd!=null and bean.inboundDateEnd!=\"\"'>",
			" AND a.inbound_date <![CDATA[ <= ]]> #{bean.inboundDateEnd}",
			"</when>",
			"<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
			" AND a.business_scope  = #{bean.businessScope}",
			"</when>",
			" order by a.receipt_date ",
			"</script>"})
	List<OperationPlan> exportOperationPlanExcel(@Param("bean") OperationPlan bean);
}
