package com.efreight.sc.dao;

import com.efreight.sc.entity.VlEntryOrder;
import com.efreight.sc.entity.VlEntryOrderDetail;
import com.efreight.sc.entity.VlOrder;
import com.efreight.sc.entity.VlOrderDetailOrder;
import com.efreight.sc.entity.VlVehicleEntryOrder;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author qipm
 * @since 2021-01-18
 */
public interface VlEntryOrderMapper extends BaseMapper<VlEntryOrder> {

	@Select({"<script>",
        "    SELECT a.* ",
        "	FROM vl_vehicle_entry_order b",
        "	LEFT JOIN vl_entry_order a",
        "	ON b.entry_order_id=a.entry_order_id and b.vl_order_id =#{bean.vlOrderId} ",
        
        " WHERE 1=1 and a.org_id=#{bean.orgId} ",
        
        " order by a.entry_order_id ",
        "</script>"})
	List<VlEntryOrder> getListPage(@Param("bean") VlVehicleEntryOrder bean);
	@Select({"<script>",
		"    SELECT * ",
		"	FROM vl_entry_order_detail ",
		" WHERE 1=1 and entry_order_id=#{entryOrderId} ",
		
		" order by entry_order_detail_id ",
	"</script>"})
	List<VlEntryOrderDetail> getVlEntryOrderDetails(@Param("entryOrderId") Integer entryOrderId);
	@Select({"<script>",
		"   SELECT b.order_id,a.awb_number mawbNumber,a.plan_pieces pieces,a.plan_weight totalWeight,",
		"a.expect_flight airlineName,a.goods_name_en goodsEname ,",
		"a.expect_flight flightNo,",
		"a.expect_departure flightDate,",
		"a.arrival_station destination,",
		"a.goods_name_cn goodsName,",
		"a.plan_volume predictionVolume,",
		
		"a.goods_type classType,",
		"a.pack_size packageSize,",
		"a.build_up_company battleName,",
		"a.ware_name wareName,",
		"a.cargo_type cargoType,",
		"a.outfield_delivery_handling_company handlingCompanyName,",
		
		"CASE WHEN c.customs_code='5100100017600' THEN '514101/5141'",
		"WHEN c.customs_code='5100725066955' THEN '514102/5141'",
		"ELSE c.customs_code",
		"END warehouseCode",
		"FROM vl_order_detail_order b",
		"LEFT JOIN af_order a ON b.order_id=a.order_id",
		"LEFT JOIN af_warehouse c ON a.departure_warehouse_id=c.warehouse_id",
		"WHERE 1=1 AND b.vl_order_id=#{vlOrderId} ",
	
		" order by vl_detail_order_id ",
	"</script>"})
	List<VlEntryOrderDetail> getVlOrderDetail(@Param("vlOrderId") Integer vlOrderId);
	@Select({"<script>",
		"   SELECT b.order_id,a.awb_number mawbNumber,b.hawb_number hawb_number,b.plan_pieces pieces,",
		"b.plan_weight totalWeight,LEFT(a.expect_flight, 2) airlineName,b.goods_name_en goodsEname ,",
		"a.goods_type classType,",
		"a.pack_size packageSize,",
		"a.build_up_company battleName,",
		"a.ware_name wareName,",
		"CASE WHEN c.customs_code='5100100017600' THEN '514101/5141'",
		"WHEN c.customs_code='5100725066955' THEN '514102/5141'",
		"ELSE c.customs_code",
		"END warehouseCode",
		"FROM af_shipper_letter b",
		"LEFT JOIN af_order a ON b.order_id=a.order_id",
		"LEFT JOIN af_warehouse c ON a.departure_warehouse_id=c.warehouse_id",
		"WHERE 1=1 AND b.order_id=#{orderId} ",
		" order by sl_id ",
	"</script>"})
	List<VlEntryOrderDetail> getFHL(@Param("orderId") Integer orderId);
	
	@Delete({"<script>",
		"   DELETE  FROM vl_entry_order_detail",
		"WHERE 1=1 AND entry_order_id=#{entryOrderId} ",
	"</script>"})
	void doDeleteDetails(@Param("entryOrderId") Integer entryOrderId);
	
	@Select({"<script>",
		"   SELECT * FROM vl_order_detail_order ",
		"WHERE 1=1 AND vl_order_id=#{vlOrderId} ",
		" order by vl_detail_order_id ",
	"</script>"})
	List<VlOrderDetailOrder> getVlOrderDetailOrders(@Param("vlOrderId") Integer vlOrderId);
	@Select({"<script>",
		" SELECT DISTINCT c.truck_number  FROM vl_order_detail_order  a",
		" LEFT JOIN vl_order b ON a.vl_order_id=b.order_id",
		" LEFT JOIN lc_truck c ON b.truck_id=c.truck_id",
		" WHERE a.vl_order_id!=#{vlOrderId} AND a.order_id=#{orderId}",
	"</script>"})
	List<VlOrder> getVlOrders(@Param("vlOrderId") Integer vlOrderId,@Param("orderId") Integer orderId);
	@Select({"<script>",
		" SELECT org_name FROM hrs_org",
		" WHERE org_id=#{org_id}",
	"</script>"})
	String getOrgName(@Param("org_id") Integer org_id);
}
