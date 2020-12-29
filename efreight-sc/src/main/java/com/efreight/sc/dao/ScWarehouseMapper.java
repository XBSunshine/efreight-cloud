package com.efreight.sc.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.sc.entity.OrderDeliveryNotice;
import com.efreight.sc.entity.ScWarehouse;

public interface ScWarehouseMapper extends BaseMapper<ScWarehouse>{
	
	 @Select({"<script>",
		 " select a.warehouse_id,a.org_id,a.business_scope,a.warehouse_code,a.ap_code,a.warehouse_name_cn,a.warehouse_name_en" ,
		 ",a.warehouse_longitude,a.warehouse_latitude,a.warehouse_address_gps,a.warehouse_status,a.customs_supervision,a.customs_code",
		 ",a.warehouse_contact_remark,a.creator_id,a.creator_name,a.create_time,a.editor_id,a.editor_name,a.edit_time,IF(IFNULL(a.edit_time,'')='',a.create_time,a.edit_time) AS operate_time",
		 ",IF(IFNULL(a.edit_time,'')='',a.creator_name,a.editor_name) AS operate_name,b.port_name_en ",
		 " from sc_warehouse a ",
		 " left join sc_port_maintenance b on a.ap_code=b.port_code ",
		 " where a.warehouse_status=1 ",
		"<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
        " AND a.business_scope = #{bean.businessScope}",
        "</when>",
        "<when test='bean.orgId!=null'>",
        " AND a.org_id = #{bean.orgId}",
        "</when>",
        "<when test='bean.warehouseNameCn!=null and bean.warehouseNameCn!=\"\"'>",
        " AND (a.warehouse_code like  \"%\"#{bean.warehouseNameCn}\"%\" or a.warehouse_name_cn like  \"%\"#{bean.warehouseNameCn}\"%\" or a.warehouse_name_en like  \"%\"#{bean.warehouseNameCn}\"%\")",
        "</when> ",
        "<when test='bean.warehouseCode!=null and bean.warehouseCode!=\"\"'>",
        " AND (a.warehouse_code like  \"%\"#{bean.warehouseCode}\"%\" or a.warehouse_name_cn like  \"%\"#{bean.warehouseCode}\"%\" or a.warehouse_name_en like  \"%\"#{bean.warehouseCode}\"%\")",
        "</when> ",
        "order by a.create_time desc",
		"</script>"})
     IPage<ScWarehouse> getPage(Page page,@Param("bean") ScWarehouse bean);
	 
	 @Select({"<script>",
		 " select a.warehouse_id,a.org_id,a.business_scope,a.warehouse_code,a.ap_code,a.warehouse_name_cn,a.warehouse_name_en" ,
		 ",a.warehouse_longitude,a.warehouse_latitude,a.warehouse_address_gps,a.warehouse_status,a.customs_supervision,a.customs_code",
		 ",a.warehouse_contact_remark,a.creator_id,a.creator_name,a.create_time,a.editor_id,a.editor_name,a.edit_time,IF(IFNULL(a.edit_time,'')='',a.create_time,a.edit_time) AS operate_time",
		 ",IF(IFNULL(a.edit_time,'')='',a.creator_name,a.editor_name) AS operate_name,b.port_name_en ",
		 " from sc_warehouse a ",
		 " left join sc_port_maintenance b on a.ap_code=b.port_code ",
		 " where a.warehouse_status=1 ",
		"<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
        " AND a.business_scope = #{bean.businessScope}",
        "</when>",
        "<when test='bean.orgId!=null'>",
        " AND a.org_id = #{bean.orgId}",
        "</when>",
        "<when test='bean.apCode!=null and bean.apCode!=\"\"'>",
        " AND a.ap_code = #{bean.apCode}",
        "</when>",
        "<when test='bean.warehouseCodeCheck!=null and bean.warehouseCodeCheck!=\"\"'>",
        " AND a.warehouse_code = #{bean.warehouseCodeCheck}",
        "</when>",
        "<when test='bean.warehouseNameCnCheck!=null and bean.warehouseNameCnCheck!=\"\"'>",
        " AND a.warehouse_name_cn = #{bean.warehouseNameCnCheck}",
        "</when>",
        "<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
        " AND a.business_scope = #{bean.businessScope}",
        "</when>",
        "<when test='bean.warehouseNameCn!=null and bean.warehouseNameCn!=\"\"'>",
        " AND (a.warehouse_code like  \"%\"#{bean.warehouseNameCn}\"%\" or a.warehouse_name_cn like  \"%\"#{bean.warehouseNameCn}\"%\" or a.warehouse_name_en like  \"%\"#{bean.warehouseNameCn}\"%\")",
        "</when> ",
        "<when test='bean.warehouseCode!=null and bean.warehouseCode!=\"\"'>",
        " AND (a.warehouse_code like  \"%\"#{bean.warehouseCode}\"%\" or a.warehouse_name_cn like  \"%\"#{bean.warehouseCode}\"%\" or a.warehouse_name_en like  \"%\"#{bean.warehouseCode}\"%\")",
        "</when> ",
//        "order by a.create_time desc",
		"</script>"})
     List<ScWarehouse> getList(@Param("bean") ScWarehouse bean);
	 
	    @Select({"<script>",
	    	    "SELECT " ,
	            "O.order_uuid,",
	            "ORG.org_logo,",
	            "scport.port_name_en AS arrival_station,",
	            "O.container_list,",
	            "O.mbl_number,",
	            "O.hbl_number,",
	            "O.plan_pieces,",
	            "O.plan_weight,",
	            "O.plan_volume,",
	            "O.order_code," ,
	            "O.container_load_address_cn," ,
	            "ware.warehouse_contact_remark," ,
	            "O.ship_name," ,
	            "O.ship_voyage_number," ,
	            "O.document_off_date," ,
	            "O.customs_closing_date," ,
	            "ware.warehouse_longitude," ,
	            "ware.warehouse_latitude," ,
	            "huser.phone_number," ,
	            "huser.user_name," ,
	            "O.org_id," ,
				"ORG.org_uuid," ,
	            "ware.warehouse_address_gps " ,
	            "FROM sc_order O" ,
	            "LEFT JOIN hrs_org ORG ON ORG.org_id = O.org_id" ,
	            "LEFT JOIN hrs_user huser ON O.servicer_id = huser.user_id" ,
	            "LEFT JOIN sc_warehouse ware on O.container_load_warehouse_id = ware.warehouse_id  and ware.warehouse_status=1 " ,
	            "LEFT JOIN sc_port_maintenance scport on scport.port_code = O.arrival_station " ,
	            "WHERE O.order_uuid = #{orderUUID}" ,
	            "</script>"})
	    OrderDeliveryNotice getOrderDeliveryNotice(@Param("orderUUID") String orderUUID);
	 

}
