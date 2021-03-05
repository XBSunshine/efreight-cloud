package com.efreight.afbase.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.entity.procedure.AfPAwbSubmitPrintProcedure;
import com.efreight.afbase.entity.procedure.AirCargoManifestPrint;
import com.efreight.afbase.entity.view.OrderDeliveryNotice;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.StatementType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AfOperateOrderMapper extends BaseMapper<AfOperateOrder> {

    @Select({"<script>",
            "    SELECT '总计' awb_number,",
//            "CONCAT(sum(a.plan_pieces),'/',IFNULL(sum(a.confirm_pieces),'-')) plan_pieces ,",
//        	"CONCAT(FORMAT(sum(a.plan_weight),1),'/',IFNULL(FORMAT(sum(a.confirm_weight),1),'-')) plan_weight ," ,
//        	"CONCAT(FORMAT(sum(a.plan_volume),3),'/',IFNULL(FORMAT(sum(a.confirm_volume),3),'-')) plan_volume ," ,
//        	"CONCAT(FORMAT(sum(a.plan_charge_weight),1),'/',IFNULL(FORMAT(sum(a.confirm_charge_weight),1),'-')) plan_charge_weight ," ,
//        	"CONCAT(sum(a.plan_density),'/',IFNULL(sum(a.confirm_density),'-')) plan_density ",
            " sum(a.plan_pieces) plan_pieces ,",
            " sum(a.plan_weight) plan_weight ,",
            " sum(a.plan_volume) plan_volume ,",
            " sum(a.plan_charge_weight) plan_charge_weight ,",
            " sum(a.plan_density) plan_density ,",
            " IFNULL(sum(a.confirm_pieces),'-') confirm_pieces ,",
            " IFNULL(sum(a.confirm_weight),'-') confirm_weight ,",
            " IFNULL(sum(a.confirm_volume),'-') confirm_volume ,",
            " IFNULL(sum(a.confirm_charge_weight),'-') confirm_charge_weight ,",
            " sum(a.storage_pieces) storage_pieces ,",
            " sum(a.storage_weight) storage_weight ,",
            " IFNULL(sum(a.confirm_density),'-') confirm_density ",
            "	FROM af_order a",
            "	LEFT JOIN af_awb_number g",
            "	ON a.awb_id=g.awb_id and g.awb_id is not null",
            "	LEFT JOIN prm_coop h",
            "	ON g.awb_from_id=h.coop_id",
            "	LEFT JOIN prm_coop b",
            "	ON a.coop_id=b.coop_id",
            " LEFT JOIN af_warehouse c",
            " ON a.departure_warehouse_id=c.warehouse_id",
            " LEFT JOIN af_warehouse d",
            " ON a.departure_storehouse_id=d.warehouse_id",
            " LEFT JOIN (select GROUP_CONCAT(g.contacts_name) as contactName,max(p.order_id) as order_id from af_order_contacts p LEFT JOIN prm_coop_contacts g on p.project_contacts_id = g.contacts_id where p.org_id=#{bean.orgId} GROUP BY p.order_id  ) e",
            " ON a.order_id=e.order_id",
            " LEFT JOIN af_inbound f",
            " ON a.order_id=f.order_id",
            " WHERE 1=1 and a.org_id=#{bean.orgId} ",
            "<when test='bean.orderStatus!=null and bean.orderStatus==\"1\"'>",
            " AND a.order_status = '财务锁账'",
            "</when>",
            "<when test='bean.orderStatus!=null and bean.orderStatus==\"0\"'>",
            " AND a.order_status != '财务锁账' and a.order_status!='强制关闭'",
            "</when>",
            "<when test='bean.orderStatus!=\"1\" and bean.orderStatus!=\"0\"'>",
            " and a.order_status!='强制关闭'",
            "</when>",
            "<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
            " AND a.business_scope = #{bean.businessScope}",
            "</when>",
            "<when test='bean.awbNumber!=null and bean.awbNumber!=\"\"'>",
            " AND (a.awb_number like  \"%\"#{bean.awbNumber}\"%\" or a.hawb_number like  \"%\"#{bean.awbNumber}\"%\")",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\"'>",
            " AND a.order_code like  \"%\"#{bean.orderCode}\"%\"",
            "</when>",
            "<when test='bean.customerNumber!=null and bean.customerNumber!=\"\"'>",
            " AND a.customer_number like  \"%\"#{bean.customerNumber}\"%\"",
            "</when>",
            "<when test='bean.businessProduct!=null and bean.businessProduct!=\"\"'>",
            " AND a.business_product = #{bean.businessProduct}",
            "</when>",
            "<when test='bean.departureStation!=null and bean.departureStation!=\"\"'>",
            " AND a.departure_station = #{bean.departureStation}",
            "</when>",
            "<when test='bean.arrivalStation!=null and bean.arrivalStation!=\"\"'>",
            " AND a.arrival_station = #{bean.arrivalStation}",
            "</when>",
            "<when test='bean.expectFlight!=null and bean.expectFlight!=\"\"'>",
            " AND a.expect_flight like  \"%\"#{bean.expectFlight}\"%\"",
            "</when>",
            "<when test='bean.flightDateBegin!=null and bean.flightDateBegin!=\"\" and bean.businessScope==\"AE\"'>",
            " AND a.expect_departure  <![CDATA[ >= ]]> #{bean.flightDateBegin}",
            "</when>",
            "<when test='bean.flightDateEnd!=null and bean.flightDateEnd!=\"\" and bean.businessScope==\"AE\"'>",
            " AND a.expect_departure <![CDATA[ <= ]]> #{bean.flightDateEnd}",
            "</when>",
            "<when test='bean.flightDateBegin!=null and bean.flightDateBegin!=\"\" and bean.businessScope==\"AI\"'>",
            " AND a.expect_arrival  <![CDATA[ >= ]]> #{bean.flightDateBegin}",
            "</when>",
            "<when test='bean.flightDateEnd!=null and bean.flightDateEnd!=\"\" and bean.businessScope==\"AI\"'>",
            " AND a.expect_arrival <![CDATA[ <= ]]> #{bean.flightDateEnd}",
            "</when>",
            "<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
            " AND a.create_time  <![CDATA[ >= ]]> #{bean.createTimeBegin}",
            "</when>",
            "<when test='bean.createTimeEnd!=null and bean.createTimeEnd!=\"\"'>",
            " AND a.create_time <![CDATA[ <= ]]> #{bean.createTimeEnd}",
            "</when>",
            "<when test='bean.servicerName!=null and bean.servicerName!=\"\"'>",
            " AND servicer_name like  \"%\"#{bean.servicerName}\"%\"",
            "</when>",
            "<when test='bean.salesName!=null and bean.salesName!=\"\"'>",
            " AND sales_name like  \"%\"#{bean.salesName}\"%\"",
            "</when>",
            "<when test='bean.coopName!=null and bean.coopName!=\"\"'>",
            " AND (b.coop_name like  \"%\"#{bean.coopName}\"%\" or b.coop_code like  \"%\"#{bean.coopName}\"%\")",
            "</when>",
            "<when test='bean.creatorName!=null and bean.creatorName!=\"\"'>",
            " AND a.creator_name like  \"%\"#{bean.creatorName}\"%\"",
            "</when>",
            "<when test='bean.awbFromName!=null and bean.awbFromName!=\"\"'>",
            " AND (g.awb_from_name like  \"%\"#{bean.awbFromName}\"%\" or h.coop_code like  \"%\"#{bean.awbFromName}\"%\")",
            "</when>",
            "<when test='bean.incomeRecorded == true'>",
            " AND a.income_recorded = #{bean.incomeRecorded}",
            "</when>",
            "<when test='bean.incomeRecorded == false'>",
            " AND (a.income_recorded = #{bean.incomeRecorded} or a.income_recorded is null)",
            "</when>",
            "<when test='bean.costRecorded == true'>",
            " AND a.cost_recorded = #{bean.costRecorded}",
            "</when>",
            "<when test='bean.costRecorded == false'>",
            " AND (a.cost_recorded = #{bean.costRecorded} or a.cost_recorded is null)",
            "</when>",
            " order by a.order_id desc",
            "</script>"})
    List<AEOperateOrder> exportAeExcelSUM(@Param("bean") AfOperateOrder bean);

    @Select({"<script>",
            "    SELECT a.awb_number,a.order_code,a.order_status,",
//    	"CONCAT(a.plan_pieces,'/',IFNULL(a.confirm_pieces,'-')) plan_pieces ,",
//    	"CONCAT(FORMAT(a.plan_weight,1),'/',IFNULL(FORMAT(a.confirm_weight,1),'-')) plan_weight ," ,
//    	"CONCAT(FORMAT(a.plan_volume,3),'/',IFNULL(FORMAT(a.confirm_volume,3),'-')) plan_volume ," ,
//    	"CONCAT(FORMAT(a.plan_charge_weight,1),'/',IFNULL(FORMAT(a.confirm_charge_weight,1),'-')) plan_charge_weight ," ,
//    	"CONCAT(a.plan_density,'/',IFNULL(a.confirm_density,'-')) plan_density ,",
            "a.plan_pieces plan_pieces ,",
            "a.plan_weight plan_weight ,",
            "a.plan_volume plan_volume ,",
            "a.plan_charge_weight plan_charge_weight ,",
            "a.plan_density plan_density ,",
            "IFNULL(a.confirm_pieces,'-') confirm_pieces ,",
            "IFNULL(a.confirm_weight,'-') confirm_weight ,",
            "IFNULL(a.confirm_volume,'-') confirm_volume ,",
            "IFNULL(a.confirm_charge_weight,'-') confirm_charge_weight ,",
            "IFNULL(a.confirm_density,'-') confirm_density ,",
            "a.expect_flight,a.expect_departure,a.departure_station,a.arrival_station,a.goods_source_code,",
            "b.coop_code as customerCode,",
            "b.coop_name,",
            "h.coop_code as supplierCode,",
            "g.awb_from_name as awbFromName,",
//            "case when a.pick_up_delivery_service=1 then '是' else '否' end pick_up_delivery_service,",
//            "case when a.warehouse_service=1 then '是' else '否' end warehouse_service,",
//            "case when a.outfield_service=1 then '是' else '否' end outfield_service,",
//            "case when a.customs_clearance_service=1 then '是' else '否' end customs_clearance_service,",
//            "case when a.arrival_customs_clearance_service=1 then '是' else '否' end arrival_customs_clearance_service,",
//            "case when a.delivery_service=1 then '是' else '否' end delivery_service,",
//            "a.customer_number,",
//    	"CONCAT(a.business_product,'/',case when a.hawb_quantity=0 then '直单' else CONCAT('分单',a.hawb_quantity) end) business_product ,",
//            "a.business_product business_product ,",
//            "case when a.hawb_quantity=0 then '直单' else CONCAT('分单',a.hawb_quantity) end hawb_quantity ,",
            "case when a.hawb_quantity=0 then '直单' else a.hawb_quantity end hawb_quantity ,",
            "a.cargo_flow_remark,",
//            "CONCAT(c.warehouse_name_cn,'/',d.warehouse_name_cn) departureStorehouseName ,",
            "IFNULL(c.warehouse_name_cn,'空') departureWarehouseName ,",
            "IFNULL(d.warehouse_name_cn,'空') departureStorehouseName ,",
            "a.goods_name_cn,a.goods_type,a.battery_type,",
            "a.storage_pieces storage_pieces ,",
            "a.storage_weight storage_weight ,",
            "case when a.tran_flag=0 then '本关' when a.tran_flag=1 then '转关' else '大通关' end tran_flag,",
            "SUBSTRING_INDEX(a.sales_name, ' ',  1) AS salesName,SUBSTRING_INDEX(a.servicer_name, ' ',  1) AS servicerName,SUBSTRING_INDEX(a.creator_name, ' ',  1) AS creatorName ,a.order_remark",
            "	FROM af_order a",
            "	LEFT JOIN af_awb_number g",
            "	ON a.awb_id=g.awb_id and g.awb_id is not null",
            "	LEFT JOIN prm_coop h",
            "	ON g.awb_from_id=h.coop_id",
            "	LEFT JOIN prm_coop b",
            "	ON a.coop_id=b.coop_id",
            " LEFT JOIN af_warehouse c",
            " ON a.departure_warehouse_id=c.warehouse_id",
            " LEFT JOIN af_warehouse d",
            " ON a.departure_storehouse_id=d.warehouse_id",
            " LEFT JOIN (select GROUP_CONCAT(g.contacts_name) as contactName,max(p.order_id) as order_id from af_order_contacts p LEFT JOIN prm_coop_contacts g on p.project_contacts_id = g.contacts_id where p.org_id=#{bean.orgId} GROUP BY p.order_id  ) e",
            " ON a.order_id=e.order_id",
            " LEFT JOIN af_inbound f",
            " ON a.order_id=f.order_id",
            " WHERE 1=1 and a.org_id=#{bean.orgId} ",
            "<when test='bean.orderStatus!=null and bean.orderStatus==\"1\"'>",
            " AND a.order_status = '财务锁账'",
            "</when>",
            "<when test='bean.orderStatus!=null and bean.orderStatus==\"0\"'>",
            " AND a.order_status != '财务锁账' and a.order_status!='强制关闭'",
            "</when>",
            "<when test='bean.orderStatus!=\"1\" and bean.orderStatus!=\"0\"'>",
            " and a.order_status!='强制关闭'",
            "</when>",
            "<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
            " AND a.business_scope = #{bean.businessScope}",
            "</when>",
            "<when test='bean.awbNumber!=null and bean.awbNumber!=\"\"'>",
            " AND (a.awb_number like  \"%\"#{bean.awbNumber}\"%\" or a.hawb_number like  \"%\"#{bean.awbNumber}\"%\")",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\"'>",
            " AND a.order_code like  \"%\"#{bean.orderCode}\"%\"",
            "</when>",
            "<when test='bean.customerNumber!=null and bean.customerNumber!=\"\"'>",
            " AND a.customer_number like  \"%\"#{bean.customerNumber}\"%\"",
            "</when>",
            "<when test='bean.businessProduct!=null and bean.businessProduct!=\"\"'>",
            " AND a.business_product = #{bean.businessProduct}",
            "</when>",
            "<when test='bean.departureStation!=null and bean.departureStation!=\"\"'>",
            " AND a.departure_station = #{bean.departureStation}",
            "</when>",
            "<when test='bean.arrivalStation!=null and bean.arrivalStation!=\"\"'>",
            " AND a.arrival_station = #{bean.arrivalStation}",
            "</when>",
            "<when test='bean.expectFlight!=null and bean.expectFlight!=\"\"'>",
            " AND a.expect_flight like  \"%\"#{bean.expectFlight}\"%\"",
            "</when>",
            "<when test='bean.flightDateBegin!=null and bean.flightDateBegin!=\"\" and bean.businessScope==\"AE\"'>",
            " AND a.expect_departure  <![CDATA[ >= ]]> #{bean.flightDateBegin}",
            "</when>",
            "<when test='bean.flightDateEnd!=null and bean.flightDateEnd!=\"\" and bean.businessScope==\"AE\"'>",
            " AND a.expect_departure <![CDATA[ <= ]]> #{bean.flightDateEnd}",
            "</when>",
            "<when test='bean.flightDateBegin!=null and bean.flightDateBegin!=\"\" and bean.businessScope==\"AI\"'>",
            " AND a.expect_arrival  <![CDATA[ >= ]]> #{bean.flightDateBegin}",
            "</when>",
            "<when test='bean.flightDateEnd!=null and bean.flightDateEnd!=\"\" and bean.businessScope==\"AI\"'>",
            " AND a.expect_arrival <![CDATA[ <= ]]> #{bean.flightDateEnd}",
            "</when>",
            "<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
            " AND a.create_time  <![CDATA[ >= ]]> #{bean.createTimeBegin}",
            "</when>",
            "<when test='bean.createTimeEnd!=null and bean.createTimeEnd!=\"\"'>",
            " AND a.create_time <![CDATA[ <= ]]> #{bean.createTimeEnd}",
            "</when>",
            "<when test='bean.servicerName!=null and bean.servicerName!=\"\"'>",
            " AND servicer_name like  \"%\"#{bean.servicerName}\"%\"",
            "</when>",
            "<when test='bean.salesName!=null and bean.salesName!=\"\"'>",
            " AND sales_name like  \"%\"#{bean.salesName}\"%\"",
            "</when>",
            "<when test='bean.coopName!=null and bean.coopName!=\"\"'>",
            " AND (b.coop_name like  \"%\"#{bean.coopName}\"%\" or b.coop_code like  \"%\"#{bean.coopName}\"%\")",
            "</when>",
            "<when test='bean.creatorName!=null and bean.creatorName!=\"\"'>",
            " AND a.creator_name like  \"%\"#{bean.creatorName}\"%\"",
            "</when>",
            "<when test='bean.awbFromName!=null and bean.awbFromName!=\"\"'>",
            " AND (g.awb_from_name like  \"%\"#{bean.awbFromName}\"%\" or h.coop_code like  \"%\"#{bean.awbFromName}\"%\")",
            "</when>",
            "<when test='bean.incomeRecorded == true'>",
            " AND a.income_recorded = #{bean.incomeRecorded}",
            "</when>",
            "<when test='bean.incomeRecorded == false'>",
            " AND (a.income_recorded = #{bean.incomeRecorded} or a.income_recorded is null)",
            "</when>",
            "<when test='bean.costRecorded == true'>",
            " AND a.cost_recorded = #{bean.costRecorded}",
            "</when>",
            "<when test='bean.costRecorded == false'>",
            " AND (a.cost_recorded = #{bean.costRecorded} or a.cost_recorded is null)",
            "</when>",
            " order by a.order_id desc",
            "</script>"})
    List<AEOperateOrder> exportAeExcel(@Param("bean") AfOperateOrder bean);

    @Select({"<script>",
            "    SELECT '总计' awb_number,",
            "sum(a.plan_pieces) plan_pieces,sum(a.plan_weight) plan_weight,",
            "sum(a.plan_volume) plan_volume,sum(a.plan_charge_weight) plan_charge_weight",
            "	FROM af_order a",
            "	LEFT JOIN af_awb_number g",
            "	ON a.awb_id=g.awb_id and g.awb_id is not null",
            "	LEFT JOIN prm_coop b",
            "	ON a.coop_id=b.coop_id",
            " LEFT JOIN af_warehouse c",
            " ON a.departure_warehouse_id=c.warehouse_id",
            " LEFT JOIN af_warehouse d",
            " ON a.departure_storehouse_id=d.warehouse_id",
            " LEFT JOIN (select GROUP_CONCAT(g.contacts_name) as contactName,max(p.order_id) as order_id from af_order_contacts p LEFT JOIN prm_coop_contacts g on p.project_contacts_id = g.contacts_id where p.org_id=#{bean.orgId} GROUP BY p.order_id  ) e",
            " ON a.order_id=e.order_id",
            " LEFT JOIN af_inbound f",
            " ON a.order_id=f.order_id",
            " WHERE 1=1 and a.org_id=#{bean.orgId} ",
            "<when test='bean.orderStatus!=null and bean.orderStatus==\"1\"'>",
            " AND a.order_status = '财务锁账'",
            "</when>",
            "<when test='bean.orderStatus!=null and bean.orderStatus==\"0\"'>",
            " AND a.order_status != '财务锁账' and a.order_status!='强制关闭'",
            "</when>",
            "<when test='bean.orderStatus!=\"1\" and bean.orderStatus!=\"0\"'>",
            " and a.order_status!='强制关闭'",
            "</when>",
            "<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
            " AND a.business_scope = #{bean.businessScope}",
            "</when>",
            "<when test='bean.awbNumber!=null and bean.awbNumber!=\"\"'>",
            " AND (a.awb_number like  \"%\"#{bean.awbNumber}\"%\" or a.hawb_number like  \"%\"#{bean.awbNumber}\"%\")",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\"'>",
            " AND a.order_code like  \"%\"#{bean.orderCode}\"%\"",
            "</when>",
            "<when test='bean.customerNumber!=null and bean.customerNumber!=\"\"'>",
            " AND a.customer_number like  \"%\"#{bean.customerNumber}\"%\"",
            "</when>",
            "<when test='bean.businessProduct!=null and bean.businessProduct!=\"\"'>",
            " AND a.business_product = #{bean.businessProduct}",
            "</when>",
            "<when test='bean.departureStation!=null and bean.departureStation!=\"\"'>",
            " AND a.departure_station = #{bean.departureStation}",
            "</when>",
            "<when test='bean.arrivalStation!=null and bean.arrivalStation!=\"\"'>",
            " AND a.arrival_station = #{bean.arrivalStation}",
            "</when>",
            "<when test='bean.expectFlight!=null and bean.expectFlight!=\"\"'>",
            " AND a.expect_flight like  \"%\"#{bean.expectFlight}\"%\"",
            "</when>",
            "<when test='bean.flightDateBegin!=null and bean.flightDateBegin!=\"\" and bean.businessScope==\"AE\"'>",
            " AND a.expect_departure  <![CDATA[ >= ]]> #{bean.flightDateBegin}",
            "</when>",
            "<when test='bean.flightDateEnd!=null and bean.flightDateEnd!=\"\" and bean.businessScope==\"AE\"'>",
            " AND a.expect_departure <![CDATA[ <= ]]> #{bean.flightDateEnd}",
            "</when>",
            "<when test='bean.flightDateBegin!=null and bean.flightDateBegin!=\"\" and bean.businessScope==\"AI\"'>",
            " AND a.expect_arrival  <![CDATA[ >= ]]> #{bean.flightDateBegin}",
            "</when>",
            "<when test='bean.flightDateEnd!=null and bean.flightDateEnd!=\"\" and bean.businessScope==\"AI\"'>",
            " AND a.expect_arrival <![CDATA[ <= ]]> #{bean.flightDateEnd}",
            "</when>",
            "<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
            " AND a.create_time  <![CDATA[ >= ]]> #{bean.createTimeBegin}",
            "</when>",
            "<when test='bean.createTimeEnd!=null and bean.createTimeEnd!=\"\"'>",
            " AND a.create_time <![CDATA[ <= ]]> #{bean.createTimeEnd}",
            "</when>",
            "<when test='bean.servicerName!=null and bean.servicerName!=\"\"'>",
            " AND servicer_name like  \"%\"#{bean.servicerName}\"%\"",
            "</when>",
            "<when test='bean.salesName!=null and bean.salesName!=\"\"'>",
            " AND sales_name like  \"%\"#{bean.salesName}\"%\"",
            "</when>",
            "<when test='bean.coopName!=null and bean.coopName!=\"\"'>",
            " AND b.coop_name like  \"%\"#{bean.coopName}\"%\"",
            "</when>",
            "<when test='bean.creatorName!=null and bean.creatorName!=\"\"'>",
            " AND a.creator_name like  \"%\"#{bean.creatorName}\"%\"",
            "</when>",
            "<when test='bean.awbFromName!=null and bean.awbFromName!=\"\"'>",
            " AND g.awb_from_name like  \"%\"#{bean.awbFromName}\"%\"",
            "</when>",
            "<when test='bean.incomeRecorded == true'>",
            " AND a.income_recorded = #{bean.incomeRecorded}",
            "</when>",
            "<when test='bean.incomeRecorded == false'>",
            " AND (a.income_recorded = #{bean.incomeRecorded} or a.income_recorded is null)",
            "</when>",
            "<when test='bean.costRecorded == true'>",
            " AND a.cost_recorded = #{bean.costRecorded}",
            "</when>",
            "<when test='bean.costRecorded == false'>",
            " AND (a.cost_recorded = #{bean.costRecorded} or a.cost_recorded is null)",
            "</when>",
            " order by a.order_id desc",
            "</script>"})
    List<AIOrder> exportAiExcelSUM(@Param("bean") AfOperateOrder bean);

    @Select({"<script>",
            "    SELECT CASE WHEN IFNULL(a.awb_number,'')!='' and  IFNULL(a.hawb_number,'')!='' THEN CONCAT(a.awb_number,' / ',a.hawb_number) WHEN IFNULL(a.hawb_number,'')!='' THEN a.hawb_number ELSE a.awb_number END AS awb_number,a.order_code,a.order_status,IF(a.income_recorded=1 or IFNULL(a.income_status,'未录收入')!='未录收入','√','') AS income_recorded,IF(a.cost_recorded=1 or IFNULL(a.cost_status,'未录成本')!='未录成本','√','') AS cost_recorded,",
            "b.coop_name, ",
            "a.expect_flight,a.expect_arrival,a.departure_station,a.arrival_station,",
            "a.plan_pieces,a.plan_weight,a.plan_volume,a.plan_charge_weight,",
            "case when a.switch_awb_service=1 then '是' else '否' end switch_awb_service,",
            "case when a.warehouse_service=1 then '是' else '否' end warehouse_service,",
            "case when a.customs_clearance_service=1 then '是' else '否' end customs_clearance_service,",
            "case when a.delivery_service=1 then '是' else '否' end delivery_service,",
            "a.customer_number,a.cargo_flow_type,a.cargo_flow_remark,",
            "a.goods_type,a.goods_name_cn,a.damage_remark,",
            "SUBSTRING_INDEX(a.sales_name, ' ',  1) AS salesName,SUBSTRING_INDEX(a.servicer_name, ' ',  1) AS servicerName,SUBSTRING_INDEX(a.creator_name, ' ',  1) AS creatorName ,a.order_remark",
            "	FROM af_order a",
            "	LEFT JOIN af_awb_number g",
            "	ON a.awb_id=g.awb_id and g.awb_id is not null",
            "	LEFT JOIN prm_coop b",
            "	ON a.coop_id=b.coop_id",
            " LEFT JOIN af_warehouse c",
            " ON a.departure_warehouse_id=c.warehouse_id",
            " LEFT JOIN af_warehouse d",
            " ON a.departure_storehouse_id=d.warehouse_id",
            " LEFT JOIN (select GROUP_CONCAT(g.contacts_name) as contactName,max(p.order_id) as order_id from af_order_contacts p LEFT JOIN prm_coop_contacts g on p.project_contacts_id = g.contacts_id where p.org_id=#{bean.orgId} GROUP BY p.order_id  ) e",
            " ON a.order_id=e.order_id",
            " LEFT JOIN af_inbound f",
            " ON a.order_id=f.order_id",
            " WHERE 1=1 and a.org_id=#{bean.orgId} ",
            "<when test='bean.orderStatus!=null and bean.orderStatus==\"1\"'>",
            " AND a.order_status = '财务锁账'",
            "</when>",
            "<when test='bean.orderStatus!=null and bean.orderStatus==\"0\"'>",
            " AND a.order_status != '财务锁账' and a.order_status!='强制关闭'",
            "</when>",
            "<when test='bean.orderStatus!=\"1\" and bean.orderStatus!=\"0\"'>",
            " and a.order_status!='强制关闭'",
            "</when>",
            "<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
            " AND a.business_scope = #{bean.businessScope}",
            "</when>",
            "<when test='bean.awbNumber!=null and bean.awbNumber!=\"\"'>",
            " AND (a.awb_number like  \"%\"#{bean.awbNumber}\"%\" or a.hawb_number like  \"%\"#{bean.awbNumber}\"%\")",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\"'>",
            " AND a.order_code like  \"%\"#{bean.orderCode}\"%\"",
            "</when>",
            "<when test='bean.customerNumber!=null and bean.customerNumber!=\"\"'>",
            " AND a.customer_number like  \"%\"#{bean.customerNumber}\"%\"",
            "</when>",
            "<when test='bean.businessProduct!=null and bean.businessProduct!=\"\"'>",
            " AND a.business_product = #{bean.businessProduct}",
            "</when>",
            "<when test='bean.departureStation!=null and bean.departureStation!=\"\"'>",
            " AND a.departure_station = #{bean.departureStation}",
            "</when>",
            "<when test='bean.arrivalStation!=null and bean.arrivalStation!=\"\"'>",
            " AND a.arrival_station = #{bean.arrivalStation}",
            "</when>",
            "<when test='bean.expectFlight!=null and bean.expectFlight!=\"\"'>",
            " AND a.expect_flight like  \"%\"#{bean.expectFlight}\"%\"",
            "</when>",
            "<when test='bean.flightDateBegin!=null and bean.flightDateBegin!=\"\" and bean.businessScope==\"AE\"'>",
            " AND a.expect_departure  <![CDATA[ >= ]]> #{bean.flightDateBegin}",
            "</when>",
            "<when test='bean.flightDateEnd!=null and bean.flightDateEnd!=\"\" and bean.businessScope==\"AE\"'>",
            " AND a.expect_departure <![CDATA[ <= ]]> #{bean.flightDateEnd}",
            "</when>",
            "<when test='bean.flightDateBegin!=null and bean.flightDateBegin!=\"\" and bean.businessScope==\"AI\"'>",
            " AND a.expect_arrival  <![CDATA[ >= ]]> #{bean.flightDateBegin}",
            "</when>",
            "<when test='bean.flightDateEnd!=null and bean.flightDateEnd!=\"\" and bean.businessScope==\"AI\"'>",
            " AND a.expect_arrival <![CDATA[ <= ]]> #{bean.flightDateEnd}",
            "</when>",
            "<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
            " AND a.create_time  <![CDATA[ >= ]]> #{bean.createTimeBegin}",
            "</when>",
            "<when test='bean.createTimeEnd!=null and bean.createTimeEnd!=\"\"'>",
            " AND a.create_time <![CDATA[ <= ]]> #{bean.createTimeEnd}",
            "</when>",
            "<when test='bean.servicerName!=null and bean.servicerName!=\"\"'>",
            " AND servicer_name like  \"%\"#{bean.servicerName}\"%\"",
            "</when>",
            "<when test='bean.salesName!=null and bean.salesName!=\"\"'>",
            " AND sales_name like  \"%\"#{bean.salesName}\"%\"",
            "</when>",
            "<when test='bean.coopName!=null and bean.coopName!=\"\"'>",
            " AND b.coop_name like  \"%\"#{bean.coopName}\"%\"",
            "</when>",
            "<when test='bean.creatorName!=null and bean.creatorName!=\"\"'>",
            " AND a.creator_name like  \"%\"#{bean.creatorName}\"%\"",
            "</when>",
            "<when test='bean.awbFromName!=null and bean.awbFromName!=\"\"'>",
            " AND g.awb_from_name like  \"%\"#{bean.awbFromName}\"%\"",
            "</when>",
            "<when test='bean.incomeRecorded == true'>",
            " AND a.income_recorded = #{bean.incomeRecorded}",
            "</when>",
            "<when test='bean.incomeRecorded == false'>",
            " AND (a.income_recorded = #{bean.incomeRecorded} or a.income_recorded is null)",
            "</when>",
            "<when test='bean.costRecorded == true'>",
            " AND a.cost_recorded = #{bean.costRecorded}",
            "</when>",
            "<when test='bean.costRecorded == false'>",
            " AND (a.cost_recorded = #{bean.costRecorded} or a.cost_recorded is null)",
            "</when>",
            " order by a.order_id desc",
            "</script>"})
    List<AIOrder> exportAiExcel(@Param("bean") AfOperateOrder bean);

    @Select({"<script>",
            "    SELECT a.* ,",
//		   "b.servicer_name,b.sales_name,b.sales_manager_name,b.project_name ",
            "b.coop_name,b.coop_code customerCode,h.coop_code supplierCode",
            " ,c.warehouse_name_cn departureWarehouseName,d.warehouse_name_cn departureStorehouseName,e.contactName as contactName",
            " ,f.order_charge_weight as inboundOrderChargeWeight,f.awb_charge_weight as inboundAwbChargeWeight,g.awb_from_name as awbFromName",
            "	FROM af_order a",
            "	LEFT JOIN af_awb_number g",
            "	ON a.awb_id=g.awb_id and g.awb_id is not null",
            "	LEFT JOIN prm_coop h",
            "	ON g.awb_from_id=h.coop_id",
            "	LEFT JOIN prm_coop b",
            "	ON a.coop_id=b.coop_id",
            " LEFT JOIN af_warehouse c",
            " ON a.departure_warehouse_id=c.warehouse_id",
            " LEFT JOIN af_warehouse d",
            " ON a.departure_storehouse_id=d.warehouse_id",
            " LEFT JOIN (select GROUP_CONCAT(g.contacts_name) as contactName,max(p.order_id) as order_id from af_order_contacts p LEFT JOIN prm_coop_contacts g on p.project_contacts_id = g.contacts_id where p.org_id=#{bean.orgId} GROUP BY p.order_id  ) e",
            " ON a.order_id=e.order_id",
            " LEFT JOIN af_inbound f",
            " ON a.order_id=f.order_id",
            " WHERE 1=1 and a.org_id=#{bean.orgId} ",
            "<when test='bean.orderStatus!=null and bean.orderStatus==\"1\"'>",
            " AND a.order_status = '财务锁账'",
            "</when>",
            "<when test='bean.orderStatus!=null and bean.orderStatus==\"0\"'>",
            " AND a.order_status != '财务锁账' and a.order_status!='强制关闭'",
            "</when>",
            "<when test='bean.orderStatus!=\"1\" and bean.orderStatus!=\"0\"'>",
            " and a.order_status!='强制关闭'",
            "</when>",
            "<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
            " AND a.business_scope = #{bean.businessScope}",
            "</when>",
            "<when test='bean.awbNumber!=null and bean.awbNumber!=\"\"'>",
            " AND (a.awb_number like  \"%\"#{bean.awbNumber}\"%\" or a.hawb_number like  \"%\"#{bean.awbNumber}\"%\")",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\"'>",
            " AND a.order_code like  \"%\"#{bean.orderCode}\"%\"",
            "</when>",
            "<when test='bean.customerNumber!=null and bean.customerNumber!=\"\"'>",
            " AND a.customer_number like  \"%\"#{bean.customerNumber}\"%\"",
            "</when>",
            "<when test='bean.businessProduct!=null and bean.businessProduct!=\"\"'>",
            " AND a.business_product = #{bean.businessProduct}",
            "</when>",
            "<when test='bean.departureStation!=null and bean.departureStation!=\"\"'>",
            " AND a.departure_station = #{bean.departureStation}",
            "</when>",
            "<when test='bean.arrivalStation!=null and bean.arrivalStation!=\"\"'>",
            " AND a.arrival_station = #{bean.arrivalStation}",
            "</when>",
            "<when test='bean.expectFlight!=null and bean.expectFlight!=\"\"'>",
            " AND a.expect_flight like  \"%\"#{bean.expectFlight}\"%\"",
            "</when>",
            "<when test='bean.flightDateBegin!=null and bean.flightDateBegin!=\"\" and bean.businessScope==\"AE\"'>",
            " AND a.expect_departure  <![CDATA[ >= ]]> #{bean.flightDateBegin}",
            "</when>",
            "<when test='bean.flightDateEnd!=null and bean.flightDateEnd!=\"\" and bean.businessScope==\"AE\"'>",
            " AND a.expect_departure <![CDATA[ <= ]]> #{bean.flightDateEnd}",
            "</when>",
            "<when test='bean.flightDateBegin!=null and bean.flightDateBegin!=\"\" and bean.businessScope==\"AI\"'>",
            " AND a.expect_arrival  <![CDATA[ >= ]]> #{bean.flightDateBegin}",
            "</when>",
            "<when test='bean.flightDateEnd!=null and bean.flightDateEnd!=\"\" and bean.businessScope==\"AI\"'>",
            " AND a.expect_arrival <![CDATA[ <= ]]> #{bean.flightDateEnd}",
            "</when>",
            "<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
            " AND a.create_time  <![CDATA[ >= ]]> #{bean.createTimeBegin}",
            "</when>",
            "<when test='bean.createTimeEnd!=null and bean.createTimeEnd!=\"\"'>",
            " AND a.create_time <![CDATA[ <= ]]> #{bean.createTimeEnd}",
            "</when>",
            "<when test='bean.servicerName!=null and bean.servicerName!=\"\"'>",
            " AND servicer_name like  \"%\"#{bean.servicerName}\"%\"",
            "</when>",
            "<when test='bean.salesName!=null and bean.salesName!=\"\"'>",
            " AND sales_name like  \"%\"#{bean.salesName}\"%\"",
            "</when>",
            "<when test='bean.coopName!=null and bean.coopName!=\"\"'>",
            " AND (b.coop_name like  \"%\"#{bean.coopName}\"%\" or b.coop_code like  \"%\"#{bean.coopName}\"%\")",
            "</when>",
            "<when test='bean.creatorName!=null and bean.creatorName!=\"\"'>",
            " AND a.creator_name like  \"%\"#{bean.creatorName}\"%\"",
            "</when>",
            "<when test='bean.awbFromName!=null and bean.awbFromName!=\"\"'>",
            " AND (g.awb_from_name like  \"%\"#{bean.awbFromName}\"%\" or h.coop_code like  \"%\"#{bean.awbFromName}\"%\")",
            "</when>",
            "<when test='bean.incomeRecorded == true'>",
            " AND a.income_recorded = #{bean.incomeRecorded}",
            "</when>",
            "<when test='bean.incomeRecorded == false'>",
            " AND (a.income_recorded = #{bean.incomeRecorded} or a.income_recorded is null)",
            "</when>",
            "<when test='bean.costRecorded == true'>",
            " AND a.cost_recorded = #{bean.costRecorded}",
            "</when>",
            "<when test='bean.costRecorded == false'>",
            " AND (a.cost_recorded = #{bean.costRecorded} or a.cost_recorded is null)",
            "</when>",
            " order by a.order_id desc",
            "</script>"})
    IPage<AfOperateOrder> getListPage(Page page, @Param("bean") AfOperateOrder bean);

    @Select({"<script>",
            "    SELECT a.* ,",
//		   "b.servicer_name,b.sales_name,b.sales_manager_name,b.project_name ",
            "b.coop_name ",
            " ,c.warehouse_name_cn departureWarehouseName ,d.warehouse_name_cn departureStorehouseName,e.contactName as contactName",
            "	FROM af_order a",
            " LEFT JOIN af_awb_number g",
            "	ON a.awb_id=g.awb_id and g.awb_id is not null",
            "	LEFT JOIN prm_coop h",
            "	ON g.awb_from_id=h.coop_id",
            "	LEFT JOIN prm_coop b",
            "	ON a.coop_id=b.coop_id",
            " LEFT JOIN af_warehouse c",
            " ON a.departure_warehouse_id=c.warehouse_id",
            " LEFT JOIN af_warehouse d",
            " ON a.departure_storehouse_id=d.warehouse_id",
            " LEFT JOIN (select GROUP_CONCAT(g.contacts_name) as contactName,max(p.order_id) as order_id from af_order_contacts p LEFT JOIN prm_coop_contacts g on p.project_contacts_id = g.contacts_id where p.org_id=#{bean.orgId} GROUP BY p.order_id  ) e",
            " ON a.order_id=e.order_id",
            " WHERE 1=1 and a.org_id=#{bean.orgId}",
            "<when test='bean.orderStatus!=null and bean.orderStatus==\"1\"'>",
            " AND a.order_status = '财务锁账'",
            "</when>",
            "<when test='bean.orderStatus!=null and bean.orderStatus==\"0\"'>",
            " AND a.order_status != '财务锁账' and a.order_status!='强制关闭'",
            "</when>",
            "<when test='bean.orderStatus!=\"1\" and bean.orderStatus!=\"0\"'>",
            " and a.order_status!='强制关闭'",
            "</when>",
            "<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
            " AND a.business_scope = #{bean.businessScope}",
            "</when>",
            "<when test='bean.awbNumber!=null and bean.awbNumber!=\"\"'>",
            " AND (a.awb_number like  \"%\"#{bean.awbNumber}\"%\" or a.hawb_number like  \"%\"#{bean.awbNumber}\"%\")",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\"'>",
            " AND a.order_code like  \"%\"#{bean.orderCode}\"%\"",
            "</when>",
            "<when test='bean.customerNumber!=null and bean.customerNumber!=\"\"'>",
            " AND a.customer_number like  \"%\"#{bean.customerNumber}\"%\"",
            "</when>",
            "<when test='bean.businessProduct!=null and bean.businessProduct!=\"\"'>",
            " AND a.business_product = #{bean.businessProduct}",
            "</when>",
            "<when test='bean.departureStation!=null and bean.departureStation!=\"\"'>",
            " AND a.departure_station = #{bean.departureStation}",
            "</when>",
            "<when test='bean.arrivalStation!=null and bean.arrivalStation!=\"\"'>",
            " AND a.arrival_station = #{bean.arrivalStation}",
            "</when>",
            "<when test='bean.expectFlight!=null and bean.expectFlight!=\"\"'>",
            " AND a.expect_flight like  \"%\"#{bean.expectFlight}\"%\"",
            "</when>",
            "<when test='bean.flightDateBegin!=null and bean.flightDateBegin!=\"\" and bean.businessScope==\"AE\"'>",
            " AND a.expect_departure  <![CDATA[ >= ]]> #{bean.flightDateBegin}",
            "</when>",
            "<when test='bean.flightDateEnd!=null and bean.flightDateEnd!=\"\" and bean.businessScope==\"AE\"'>",
            " AND a.expect_departure <![CDATA[ <= ]]> #{bean.flightDateEnd}",
            "</when>",
            "<when test='bean.flightDateBegin!=null and bean.flightDateBegin!=\"\" and bean.businessScope==\"AI\"'>",
            " AND a.expect_arrival  <![CDATA[ >= ]]> #{bean.flightDateBegin}",
            "</when>",
            "<when test='bean.flightDateEnd!=null and bean.flightDateEnd!=\"\" and bean.businessScope==\"AI\"'>",
            " AND a.expect_arrival <![CDATA[ <= ]]> #{bean.flightDateEnd}",
            "</when>",
            "<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
            " AND a.create_time  <![CDATA[ >= ]]> #{bean.createTimeBegin}",
            "</when>",
            "<when test='bean.createTimeEnd!=null and bean.createTimeEnd!=\"\"'>",
            " AND a.create_time <![CDATA[ <= ]]> #{bean.createTimeEnd}",
            "</when>",
            "<when test='bean.servicerName!=null and bean.servicerName!=\"\"'>",
            " AND servicer_name like  \"%\"#{bean.servicerName}\"%\"",
            "</when>",
            "<when test='bean.salesName!=null and bean.salesName!=\"\"'>",
            " AND sales_name like  \"%\"#{bean.salesName}\"%\"",
            "</when>",
            "<when test='bean.coopName!=null and bean.coopName!=\"\"'>",
            " AND (b.coop_name like  \"%\"#{bean.coopName}\"%\" or b.coop_code like  \"%\"#{bean.coopName}\"%\")",
            "</when>",
            "<when test='bean.creatorName!=null and bean.creatorName!=\"\"'>",
            " AND a.creator_name like  \"%\"#{bean.creatorName}\"%\"",
            "</when>",
            "<when test='bean.awbFromName!=null and bean.awbFromName!=\"\"'>",
            " AND (g.awb_from_name like  \"%\"#{bean.awbFromName}\"%\" or h.coop_code like  \"%\"#{bean.awbFromName}\"%\")",
            "</when>",
            "<when test='bean.incomeRecorded == true'>",
            " AND a.income_recorded = #{bean.incomeRecorded}",
            "</when>",
            "<when test='bean.incomeRecorded == false'>",
            " AND (a.income_recorded = #{bean.incomeRecorded} or a.income_recorded is null)",
            "</when>",
            "<when test='bean.costRecorded == true'>",
            " AND a.cost_recorded = #{bean.costRecorded}",
            "</when>",
            "<when test='bean.costRecorded == false'>",
            " AND (a.cost_recorded = #{bean.costRecorded} or a.cost_recorded is null)",
            "</when>",
            " order by a.order_id desc",
            "</script>"})
    List<AfOperateOrder> getTatol(@Param("bean") AfOperateOrder bean);

    @Select({"<script>",
            "SELECT * FROM af_V_prm_coop\n",
            "	where org_id=#{bean.orgId} and  business_scope_AE='AE' AND coop_type in ('外部客户','互为代理')",
            " <when test='bean.coopName!=null and bean.coopName!=\"\"'>",
            " AND (upper(coop_name) like  \"%\"#{bean.coopName}\"%\" or upper(coop_code)  like \"%\"#{bean.coopName}\"%\" or upper(short_name)  like \"%\"#{bean.coopName}\"%\") ",
            "</when>",
            "</script>"})
    IPage<VPrmCoop> selectCoop(Page page, @Param("bean") VPrmCoop bean);
    @Select({"<script>",
    	"SELECT * FROM af_V_prm_coop\n",
    	"	where org_id=#{orgId} and  business_scope_AE='AE' AND coop_type in ('干线承运人','互为代理')",
    "</script>"})
    List<VPrmCoop> selectVPrmCoop(@Param("orgId") Integer orgId);
    @Insert("insert into af_awb_number \n"
            + " ( awb_uuid,org_id,awb_number,awb_status,departure_station,awb_from_type,awb_from_id,awb_from_name,creator_id,creator_name,creat_time) \n"
            + "	 values (uuid(),#{bean.orgId},#{bean.awbNumber},'已配单',#{bean.departureStation},#{bean.awbFromType},#{bean.awbFromId},#{bean.awbFromName}"
            + " ,#{bean.creatorId},#{bean.creatorName},#{bean.creatTime})\n")
    void insertAwbNumber(@Param("bean") AwbNumber bean);
    @Select({"<script>",
            "SELECT * FROM af_V_prm_coop\n",
            "	where org_id=#{bean.orgId} and  business_scope_AI='AI' AND coop_type in ('外部客户','互为代理','海外代理')",
            " <when test='bean.coopMnemonic!=null and bean.coopMnemonic!=\"\"'>",
            " AND coop_mnemonic like  \"%\"#{bean.coopMnemonic}\"%\"",
            "</when>",
            " <when test='bean.coopName!=null and bean.coopName!=\"\"'>",
            " AND coop_name like  \"%\"#{bean.coopName}\"%\"",
            "</when>",
            "</script>"})
    IPage<VPrmCoop> selectAICoop(Page page, @Param("bean") VPrmCoop bean);

    @Select({"<script>",
            "SELECT * FROM af_V_prm_coop\n",
            "	where org_id=#{bean.orgId} and  business_scope_AE='AE' AND coop_type in ('互为代理','海外代理','业务类结算对象')",
            " <when test='bean.coopMnemonic!=null and bean.coopMnemonic!=\"\"'>",
            " AND coop_mnemonic like  \"%\"#{bean.coopMnemonic}\"%\"",
            "</when>",
            " <when test='bean.coopName!=null and bean.coopName!=\"\"'>",
            " AND coop_name like  \"%\"#{bean.coopName}\"%\"",
            "</when>",
            "</script>"})
    IPage<VPrmCoop> selectPrmCoop(Page page, @Param("bean") VPrmCoop bean);

    @Select({"<script>",
            "SELECT UUID()\n",
            "</script>"})
    String getUUID();

    @Select({"<script>",
            "SELECT coop_name FROM prm_coop WHERE coop_id=#{coop_id}\n",
            "</script>"})
    String getCoopName(@Param("coop_id") Integer coop_id);

    @Select({"<script>",
            "select * from af_awb_number\n",
            "	where org_id = #{org_id} and awb_number = #{awb_number} and awb_status='未使用'\n",
            " order by awb_id desc\n",
            "</script>"})
    List<AwbNumber> selectAwb(@Param("org_id") Integer org_id, @Param("awb_number") String awb_number);

    @Select({"<script>",
            "select * from af_order\n",
            "	where org_id = #{org_id} and order_code like  \"%\"#{order_code}\"%\"",
            " order by order_id desc\n",
            "</script>"})
    List<AfOperateOrder> selectCode(@Param("org_id") Integer org_id, @Param("order_code") String order_code);

    @Select({"<script>",
            "select a.*,b.coop_name from af_order a",
            "left join prm_coop b on a.coop_id=b.coop_id",
            "	where a.org_id = #{org_id} and a.order_uuid = #{order_uuid}",
            "</script>"})
    AfOperateOrder getOrderByUUID(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Select({"<script>",
            "select a.*,b.coop_name from sc_order a",
            "left join prm_coop b on a.coop_id=b.coop_id",
            "	where a.org_id = #{org_id} and a.order_uuid = #{order_uuid}",
            "</script>"})
    AfOperateOrder getSEOrderByUUID(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Select({"<script>",
            "select a.*,b.coop_name from tc_order a",
            "left join prm_coop b on a.coop_id=b.coop_id",
            "	where a.org_id = #{org_id} and a.order_uuid = #{order_uuid}",
            "</script>"})
    AfOperateOrder getTCOrderByUUID(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Select({"<script>",
            "select a.*,b.coop_name from lc_order a",
            "left join prm_coop b on a.coop_id=b.coop_id",
            "	where a.org_id = #{org_id} and a.order_uuid = #{order_uuid}",
            "</script>"})
    AfOperateOrder getLCOrderByUUID(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Update("update af_awb_number set\n"
            + " awb_status=#{awb_status}\n"
            + " where  awb_id = #{awb_id} AND org_id=#{org_id}\n")
    void updateAwbStatus(@Param("awb_id") Integer awb_id, @Param("awb_status") String awb_status, @Param("org_id") Integer org_id);

    @Update("update af_order set\n"
            + " awb_number=NULL,awb_id=NULL,awb_uuid=NULL,order_status=#{order_status}\n"
            + " where  order_uuid = #{order_uuid} AND org_id=#{org_id}\n")
    void updateOrderNumber(@Param("order_uuid") String order_uuid, @Param("order_status") String order_status, @Param("org_id") Integer org_id);

    @Update("update af_order set\n"
            + " income_recorded=1 "
            + " where  order_uuid = #{order_uuid} AND org_id=#{org_id}")
    void doIncome(@Param("order_uuid") String order_uuid, @Param("org_id") Integer org_id, @Param("row_uuid") String row_uuid);

    @Update("update sc_order set\n"
            + " income_recorded=1 "
            + " where  order_uuid = #{order_uuid} AND org_id=#{org_id}")
    void doIncomeSE(@Param("order_uuid") String order_uuid, @Param("org_id") Integer org_id, @Param("row_uuid") String row_uuid);

    @Update("update tc_order set\n"
            + " income_recorded=1 "
            + " where  order_uuid = #{order_uuid} AND org_id=#{org_id}")
    void doIncomeTC(@Param("order_uuid") String order_uuid, @Param("org_id") Integer org_id, @Param("row_uuid") String row_uuid);

    @Update("update af_order set\n"
            + " cost_recorded=1 "
            + " where  order_uuid = #{order_uuid} AND org_id=#{org_id}")
    void doCost(@Param("order_uuid") String order_uuid, @Param("org_id") Integer org_id, @Param("row_uuid") String row_uuid);

    @Update("update sc_order set\n"
            + " cost_recorded=1 "
            + " where  order_uuid = #{order_uuid} AND org_id=#{org_id}")
    void doCostSE(@Param("order_uuid") String order_uuid, @Param("org_id") Integer org_id, @Param("row_uuid") String row_uuid);

    @Update("update tc_order set\n"
            + " cost_recorded=1 "
            + " where  order_uuid = #{order_uuid} AND org_id=#{org_id}")
    void doCostTC(@Param("order_uuid") String order_uuid, @Param("org_id") Integer org_id, @Param("row_uuid") String row_uuid);

    @Update("update af_order set\n"
            + " order_status=#{order_status},income_recorded=1,cost_recorded=1,row_uuid=#{row_uuid}\n"
            + " where  order_uuid = #{order_uuid}\n")
    void updateOrder(@Param("order_uuid") String order_uuid, @Param("order_status") String order_status, @Param("row_uuid") String row_uuid);

    @Update("update sc_order set\n"
            + " order_status=#{order_status},row_uuid=#{row_uuid}\n"
            + " where  order_uuid = #{order_uuid}\n")
    void updateOrderSE(@Param("order_uuid") String order_uuid, @Param("order_status") String order_status, @Param("row_uuid") String row_uuid);

    @Update("update tc_order set\n"
            + " order_status=#{order_status},row_uuid=#{row_uuid}\n"
            + " where  order_uuid = #{order_uuid}\n")
    void updateOrderTC(@Param("order_uuid") String order_uuid, @Param("order_status") String order_status, @Param("row_uuid") String row_uuid);

    @Update("update af_income set\n"
            + " financial_date=#{financial_date}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and financial_date IS NULL \n")
    void updateIncome(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("financial_date") LocalDate financial_date);

    @Update("update sc_income set\n"
            + " financial_date=#{financial_date}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and financial_date IS NULL \n")
    void updateIncomeSE(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("financial_date") LocalDate financial_date);

    @Update("update tc_income set\n"
            + " financial_date=#{financial_date}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and financial_date IS NULL \n")
    void updateIncomeTC(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("financial_date") LocalDate financial_date);

    @Update("update af_cost set\n"
            + " financial_date=#{financial_date}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and financial_date IS NULL \n")
    void updateCost(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("financial_date") LocalDate financial_date);

    @Update("update sc_cost set\n"
            + " financial_date=#{financial_date}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and financial_date IS NULL \n")
    void updateCostSE(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("financial_date") LocalDate financial_date);

    @Update("update tc_cost set\n"
            + " financial_date=#{financial_date}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and financial_date IS NULL \n")
    void updateCostTC(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("financial_date") LocalDate financial_date);

    @Update("update af_income set\n"
            + " financial_date= NULL \n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} \n")
    void updateIncome2(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Update("update af_cost set\n"
            + " financial_date= NULL \n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid}  \n")
    void updateCost2(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Select("SELECT node_name FROM af_log " +
            " WHERE page_function = '新建' " +
            " AND page_name='订单管理'" +
            " AND order_uuid=#{order_uuid} " +
            " AND creat_time IS NOT NULL " +
            " ORDER BY log_id desc LIMIT 1")
    String getNodeName(@Param("order_uuid") String order_uui);

    @Insert("insert into af_order_contacts \n"
            + " ( org_id,order_id,project_contacts_id) \n"
            + "	 values (#{org_id},#{order_id},#{project_contacts_id})\n")
    void insertOrderContacts(@Param("org_id") Integer org_id, @Param("order_id") Integer order_id, @Param("project_contacts_id") Integer project_contacts_id);

    @Delete("delete from af_order_contacts where org_id=#{org_id} and order_id=#{order_id}")
    void deleteOrderContacts(@Param("org_id") Integer org_id, @Param("order_id") Integer order_id);

    @Delete("delete from af_order_shipper_consignee where org_id=#{org_id} and order_id=#{order_id}")
    void deleteOrderShipperConsignee(@Param("org_id") Integer org_id, @Param("order_id") Integer order_id);

    @Select({"<script>",
            "select project_contacts_id from af_order_contacts\n",
            "	where org_id = #{org_id} and order_id = #{order_id}",
            "</script>"})
    List<Integer> getorderContacts(@Param("org_id") Integer org_id, @Param("order_id") Integer order_id);

    @Select({"<script>",
            "select * from af_order_shipper_consignee\n",
            "	where org_id = #{org_id} and order_id = #{order_id} and sc_type = #{sc_type} and sl_id is null",
            "</script>"})
    AfOrderShipperConsignee getAfOrderShipperConsignee(@Param("org_id") Integer org_id, @Param("order_id") Integer order_id, @Param("sc_type") Integer sc_type);

    @Select({"<script>",
            "select log_id from af_log\n",
            "	where node_name = #{node_name} and order_uuid = #{order_uuid} and creator_id IS NOT NULL",
            "</script>"})
    List<Integer> selectOrderStatus(@Param("node_name") String node_name, @Param("order_uuid") String order_uuid);

    @Select({"<script>",
            "select order_id from af_order ",
            "	where org_id = #{org_id} and order_id = #{order_id} and income_recorded =1",
            "</script>"})
    List<Integer> getAfList(@Param("org_id") Integer org_id, @Param("order_id") Integer order_id);

    @Select({"<script>",
            "select order_id from sc_order ",
            "	where org_id = #{org_id} and order_id = #{order_id} and income_recorded =1",
            "</script>"})
    List<Integer> getScList(@Param("org_id") Integer org_id, @Param("order_id") Integer order_id);

    @Select({"<script>",
            "select order_id from tc_order ",
            "	where org_id = #{org_id} and order_id = #{order_id} and income_recorded =1",
            "</script>"})
    List<Integer> getTcList(@Param("org_id") Integer org_id, @Param("order_id") Integer order_id);

    @Select({"<script>",
            "select order_id from af_order ",
            "	where org_id = #{org_id} and order_id = #{order_id} and cost_recorded =1",
            "</script>"})
    List<Integer> getAfList2(@Param("org_id") Integer org_id, @Param("order_id") Integer order_id);

    @Select({"<script>",
            "select order_id from sc_order ",
            "	where org_id = #{org_id} and order_id = #{order_id} and cost_recorded =1",
            "</script>"})
    List<Integer> getScList2(@Param("org_id") Integer org_id, @Param("order_id") Integer order_id);

    @Select({"<script>",
            "select order_id from tc_order ",
            "	where org_id = #{org_id} and order_id = #{order_id} and cost_recorded =1",
            "</script>"})
    List<Integer> getTcList2(@Param("org_id") Integer org_id, @Param("order_id") Integer order_id);

    @Select({"<script>",
            "select order_id from af_order\n",
            "	where order_status = #{order_status} and order_uuid = #{order_uuid} ",
            "</script>"})
    List<Integer> getOrderStatus(@Param("order_status") String order_status, @Param("order_uuid") String order_uuid);

    @Select({"<script>",
            "select order_id from sc_order\n",
            "	where order_status = #{order_status} and order_uuid = #{order_uuid} ",
            "</script>"})
    List<Integer> getSEOrderStatus(@Param("order_status") String order_status, @Param("order_uuid") String order_uuid);

    @Select({"<script>",
            "select order_id from tc_order\n",
            "	where order_status = #{order_status} and order_uuid = #{order_uuid} ",
            "</script>"})
    List<Integer> getTEOrderStatus(@Param("order_status") String order_status, @Param("order_uuid") String order_uuid);



    @Select({"<script>",
            "SELECT a.*,b.coop_name,c.currency_rate FROM af_order a\n",
            "	LEFT JOIN prm_coop b",
            "	ON a.coop_id=b.coop_id",
            "	LEFT JOIN af_V_currency_rate c",
            "	ON a.currecny_code=c.currency_code AND c.org_id=#{org_id}",
            "	where a.org_id = #{org_id} and a.awb_uuid = #{awb_uuid}",
            "</script>"})
    List<AfOperateOrder> getOrders(@Param("org_id") Integer org_id, @Param("awb_uuid") String awb_uuid);

    @Update("CALL af_P_order_income_cost_create(#{orgId},'AE_CREATE_ORDER',NULL,#{orderUuid})")
    void createIncomeAndCost(@Param("orderUuid") String orderUuid, @Param("orgId") Integer orgId);

    @Select({"<script>",
            "CALL af_P_letter_print(#{org_id},'AE_LETTER_AGENT',null,#{orderUuid},#{userId})\n",
            "</script>"})
    List<OrderLetters> printOrderLetter(@Param("org_id") Integer org_id, @Param("orderUuid") String orderUuid, @Param("userId") String userId);

    @Select({"<script>",
            "SELECT\n" +
                    "\tcount(org_api_config_id) as shippingDataNum\n" +
                    "FROM\n" +
                    "\thrs_org_api_config \n" +
                    "WHERE\n" +
                    "\torg_id = #{orgId} \n" +
                    "\tAND api_type = #{apiType} \n" +
                    "\tAND enable =1",
            "</script>"})
    Integer getShippingData(@Param("orgId") Integer orgId, @Param("apiType") String apiType);

    @Select({"<script>",
            "SELECT awb_print_id AS awbPrintId FROM af_awb_print\n",
            "	where awb_uuid = #{awbUuid} AND awb_type= 0",
            "</script>"})
    String getAwbPrintId(@Param("awbUuid") String awbUuid);

    @Select({"<script>",
            "SELECT\n" +
                    "\tIFNULL( B.flight_number, A.expect_flight ) AS flightNumber\n" +
                    "FROM\n" +
                    "\t(\n" +
                    "\tSELECT\n" +
                    "\t  max(awb_uuid) as awb_uuid,\n" +
                    "\t\tmax( awb_id ) AS awb_id,\n" +
                    "\t\tmax( expect_flight ) expect_flight\n" +
                    "\tFROM\n" +
                    "\t\taf_order \n" +
                    "\tWHERE\n" +
                    "\t\torg_id = 1 \n" +
                    "\tGROUP BY\n" +
                    "\t\tawb_id \n" +
                    "\tHAVING\n" +
                    "\t\tawb_id IS NOT NULL \n" +
                    "\t) A\n" +
                    "\tLEFT JOIN af_awb_print B ON A.awb_id = B.awb_id \n" +
                    "\tAND B.awb_type = 0 \n" +
                    "WHERE\n" +
                    "\tA.awb_uuid = #{awbUuid}",
            "</script>"})
    String getFlightNumber(@Param("awbUuid") String awbUuid);

    @Select("CALL af_P_API_HAWB(#{orderUUID}, #{orgId})")
    String getHAWBXML(@Param("orderUUID") String orderUUID, @Param("orgId") Integer orgId);

    @Select("CALL af_P_API_MAWB(#{orderUUID}, #{userId}, #{apiType})")
    String getMAWBXML(@Param("orderUUID") String orderUUID, @Param("userId") Integer userId, @Param("apiType") String apiType);

    @Select("CALL af_P_API_HAWB_MFT(#{orderUUID}, #{letterIds}, #{userId}, #{apiType})")
    String getNewHAWBXML(@Param("orderUUID") String orderUUID, @Param("letterIds") String letterIds, @Param("userId") Integer userId, @Param("apiType") String apiType);

    @Select("CALL af_P_API_MFT(#{orderUUID}, #{userId}, #{apiType})")
    String getNewMAWBXML(@Param("orderUUID") String orderUUID, @Param("userId") Integer userId, @Param("apiType") String apiType);

    @Select("CALL af_P_API_MAWB_check(#{apiType}, #{hasMwb}, #{orderUUID}, #{letterIds}, #{userId})")
    String getMAWBXMLCheck(
            @Param("apiType") String apiType,
            @Param("hasMwb") String hasMwb,
            @Param("orderUUID") String orderUUID,
            @Param("userId") Integer userId,
            @Param("letterIds") String letterIds);

    @Delete("delete from af_awb_number where org_id=#{org_id} and awb_id=#{awb_id}")
    void deleteByAwbId(@Param("awb_id") Integer awb_id, @Param("org_id") Integer org_id);

    @Select("SELECT \n" +
            "\tA.org_id\n" +
            "\t,A.business_scope\n" +
            "\t,'票数' AS type\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 6 DAY), '%Y-%m-%d') THEN 1 ELSE 0 END), 0) AS num_6\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 5 DAY), '%Y-%m-%d') THEN 1 ELSE 0 END), 0) AS num_5\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 4 DAY), '%Y-%m-%d') THEN 1 ELSE 0 END), 0) AS num_4\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 3 DAY), '%Y-%m-%d') THEN 1 ELSE 0 END), 0) AS num_3\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 2 DAY), '%Y-%m-%d') THEN 1 ELSE 0 END), 0) AS num_2\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 DAY), '%Y-%m-%d') THEN 1 ELSE 0 END), 0) AS num_1\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 0 DAY), '%Y-%m-%d') THEN 1 ELSE 0 END), 0) AS num_0\n" +
            "\tFROM af_order A\n" +
            "\tWHERE A.org_id=#{orgId}\n" +
            "\t  AND A.order_status <> '强制关闭'\n" +
            "\tAND DATE_FORMAT(A.create_time, '%Y-%m-%d')<=DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 0 DAY), '%Y-%m-%d') -- 7天内数据\n" +
            "\tAND DATE_FORMAT(A.create_time, '%Y-%m-%d')>=DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 6 DAY), '%Y-%m-%d') -- 7天内数据\n" +
            "\tGROUP BY A.org_id,A.business_scope\n" +
            "\tUNION all\n" +
            "\tSELECT \n" +
            "\tA.org_id\n" +
            "\t,A.business_scope\n" +
            "\t,'计重(吨)' AS type\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 6 DAY), '%Y-%m-%d') THEN ROUND(IFNULL(A.confirm_charge_weight,A.plan_charge_weight)/1000,1) ELSE 0.0 END), 1) AS weight_6\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 5 DAY), '%Y-%m-%d') THEN ROUND(IFNULL(A.confirm_charge_weight,A.plan_charge_weight)/1000,1) ELSE 0.0 END), 1) AS weight_5\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 4 DAY), '%Y-%m-%d') THEN ROUND(IFNULL(A.confirm_charge_weight,A.plan_charge_weight)/1000,1) ELSE 0.0 END), 1) AS weight_4\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 3 DAY), '%Y-%m-%d') THEN ROUND(IFNULL(A.confirm_charge_weight,A.plan_charge_weight)/1000,1) ELSE 0.0 END), 1) AS weight_3\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 2 DAY), '%Y-%m-%d') THEN ROUND(IFNULL(A.confirm_charge_weight,A.plan_charge_weight)/1000,1) ELSE 0.0 END), 1) AS weight_2\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 DAY), '%Y-%m-%d') THEN ROUND(IFNULL(A.confirm_charge_weight,A.plan_charge_weight)/1000,1) ELSE 0.0 END), 1) AS weight_1\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 0 DAY), '%Y-%m-%d') THEN ROUND(IFNULL(A.confirm_charge_weight,A.plan_charge_weight)/1000,1) ELSE 0.0 END), 1) AS weight_0\n" +
            "\tFROM af_order A\n" +
            "\tWHERE A.org_id=#{orgId}\n" +
            "\t  AND A.order_status <> '强制关闭'\n" +
            "\tAND DATE_FORMAT(A.create_time, '%Y-%m-%d')<=DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 0 DAY), '%Y-%m-%d') -- 7天内数据\n" +
            "\tAND DATE_FORMAT(A.create_time, '%Y-%m-%d')>=DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 6 DAY), '%Y-%m-%d') -- 7天内数据\n" +
            "\tGROUP BY A.org_id,A.business_scope\n" +
            "\t-- 海运 \n" +
            "\tUNION all\n" +
            "\tSELECT \n" +
            "\tA.org_id\n" +
            "\t,A.business_scope\n" +
            "\t,'票数' AS type\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 6 DAY), '%Y-%m-%d') THEN 1 ELSE 0 END), 0) AS num_6\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 5 DAY), '%Y-%m-%d') THEN 1 ELSE 0 END), 0) AS num_5\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 4 DAY), '%Y-%m-%d') THEN 1 ELSE 0 END), 0) AS num_4\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 3 DAY), '%Y-%m-%d') THEN 1 ELSE 0 END), 0) AS num_3\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 2 DAY), '%Y-%m-%d') THEN 1 ELSE 0 END), 0) AS num_2\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 DAY), '%Y-%m-%d') THEN 1 ELSE 0 END), 0) AS num_1\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 0 DAY), '%Y-%m-%d') THEN 1 ELSE 0 END), 0) AS num_0\n" +
            "\tFROM sc_order A\n" +
            "\tWHERE A.org_id=#{orgId}\n" +
            "\t  AND A.order_status <> '强制关闭'\n" +
            "\tAND DATE_FORMAT(A.create_time, '%Y-%m-%d')<=DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 0 DAY), '%Y-%m-%d') -- 7天内数据\n" +
            "\tAND DATE_FORMAT(A.create_time, '%Y-%m-%d')>=DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 6 DAY), '%Y-%m-%d') -- 7天内数据\n" +
            "\tGROUP BY A.org_id,A.business_scope\n" +
            "\tUNION all\n" +
            "\tSELECT \n" +
            "\tA.org_id\n" +
            "\t,A.business_scope\n" +
            "\t,'计费(吨)' AS type\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 6 DAY), '%Y-%m-%d') THEN IF(A.container_method = '整箱', 0, ROUND(IFNULL(A.plan_charge_weight,0),1)) ELSE 0.0 END), 1) AS weight_6\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 5 DAY), '%Y-%m-%d') THEN IF(A.container_method = '整箱', 0, ROUND(IFNULL(A.plan_charge_weight,0),1)) ELSE 0.0 END), 1) AS weight_5\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 4 DAY), '%Y-%m-%d') THEN IF(A.container_method = '整箱', 0, ROUND(IFNULL(A.plan_charge_weight,0),1)) ELSE 0.0 END), 1) AS weight_4\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 3 DAY), '%Y-%m-%d') THEN IF(A.container_method = '整箱', 0, ROUND(IFNULL(A.plan_charge_weight,0),1)) ELSE 0.0 END), 1) AS weight_3\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 2 DAY), '%Y-%m-%d') THEN IF(A.container_method = '整箱', 0, ROUND(IFNULL(A.plan_charge_weight,0),1)) ELSE 0.0 END), 1) AS weight_2\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 DAY), '%Y-%m-%d') THEN IF(A.container_method = '整箱', 0, ROUND(IFNULL(A.plan_charge_weight,0),1)) ELSE 0.0 END), 1) AS weight_1\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 0 DAY), '%Y-%m-%d') THEN IF(A.container_method = '整箱', 0, ROUND(IFNULL(A.plan_charge_weight,0),1)) ELSE 0.0 END), 1) AS weight_0\n" +
            "\tFROM sc_order A\n" +
            "\tWHERE A.org_id=#{orgId}\n" +
            "\tAND A.order_status <> '强制关闭'\n" +
            "\tAND DATE_FORMAT(A.create_time, '%Y-%m-%d')<=DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 0 DAY), '%Y-%m-%d') -- 7天内数据\n" +
            "\tAND DATE_FORMAT(A.create_time, '%Y-%m-%d')>=DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 6 DAY), '%Y-%m-%d') -- 7天内数据\n" +
            "\tGROUP BY A.org_id,A.business_scope\n" +
            "\tUNION all\n" +
            "\tSELECT \n" +
            "\tA.org_id\n" +
            "\t,A.business_scope\n" +
            "\t,'标箱量' AS type\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 6 DAY), '%Y-%m-%d') THEN IF(A.container_method <> '整箱', 0, IFNULL(A.container_number,0)) ELSE 0 END), 0) AS container_number_6\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 5 DAY), '%Y-%m-%d') THEN IF(A.container_method <> '整箱', 0, IFNULL(A.container_number,0)) ELSE 0 END), 0) AS container_number_5\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 4 DAY), '%Y-%m-%d') THEN IF(A.container_method <> '整箱', 0, IFNULL(A.container_number,0)) ELSE 0 END), 0) AS container_number_4\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 3 DAY), '%Y-%m-%d') THEN IF(A.container_method <> '整箱', 0, IFNULL(A.container_number,0)) ELSE 0 END), 0) AS container_number_3\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 2 DAY), '%Y-%m-%d') THEN IF(A.container_method <> '整箱', 0, IFNULL(A.container_number,0)) ELSE 0 END), 0) AS container_number_2\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 DAY), '%Y-%m-%d') THEN IF(A.container_method <> '整箱', 0, IFNULL(A.container_number,0)) ELSE 0 END), 0) AS container_number_1\n" +
            "\t,FORMAT(SUM(CASE WHEN DATE_FORMAT(A.create_time, '%Y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 0 DAY), '%Y-%m-%d') THEN IF(A.container_method <> '整箱', 0, IFNULL(A.container_number,0)) ELSE 0 END), 0) AS container_number_0\n" +
            "\tFROM sc_order A\n" +
            "\tWHERE A.org_id=#{orgId}\n" +
            "\tAND A.order_status <> '强制关闭'\n" +
            "\tAND DATE_FORMAT(A.create_time, '%Y-%m-%d')<=DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 0 DAY), '%Y-%m-%d') -- 7天内数据\n" +
            "\tAND DATE_FORMAT(A.create_time, '%Y-%m-%d')>=DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 6 DAY), '%Y-%m-%d') -- 7天内数据\n" +
            "\tGROUP BY A.org_id,A.business_scope\n" +
            "\t\n" +
            "\t")
    List<Map<String, Object>> homeStatistics(@Param("orgId") Integer orgId);

    @Update("update af_order set\n"
            + " income_status=#{income_status},row_uuid=#{row_uuid}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} ")
    void updateOrderIncomeStatus2(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("income_status") String income_status, @Param("row_uuid") String row_uuid);

    @Update("update sc_order set\n"
            + " income_status=#{income_status},row_uuid=#{row_uuid}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} ")
    void updateOrderIncomeStatusSE2(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("income_status") String income_status, @Param("row_uuid") String row_uuid);

    @Update("update tc_order set\n"
            + " income_status=#{income_status},row_uuid=#{row_uuid}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} ")
    void updateOrderIncomeStatusTE2(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("income_status") String income_status, @Param("row_uuid") String row_uuid);

    @Update("update lc_order set\n"
            + " income_status=#{income_status},row_uuid=#{row_uuid}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} ")
    void updateOrderIncomeStatusLC2(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("income_status") String income_status, @Param("row_uuid") String row_uuid);

    @Update("update tc_order set\n"
            + " income_status=#{income_status},row_uuid=#{row_uuid}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} ")
    void updateOrderIncomeStatusTC2(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("income_status") String income_status, @Param("row_uuid") String row_uuid);

    @Update("update af_order set\n"
            + " income_status=#{income_status},row_uuid=#{row_uuid}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and (income_status='已制账单' or income_status='已录收入')\n")
    void updateOrderIncomeStatus3(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("income_status") String income_status, @Param("row_uuid") String row_uuid);

    @Update("update sc_order set\n"
            + " income_status=#{income_status},row_uuid=#{row_uuid}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and (income_status='已制账单' or income_status='已录收入')\n")
    void updateOrderIncomeStatusSE3(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("income_status") String income_status, @Param("row_uuid") String row_uuid);

    @Update("update tc_order set\n"
            + " income_status=#{income_status},row_uuid=#{row_uuid}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and (income_status='已制账单' or income_status='已录收入')\n")
    void updateOrderIncomeStatusTC3(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("income_status") String income_status, @Param("row_uuid") String row_uuid);

    @Update("update lc_order set\n"
            + " income_status=#{income_status},row_uuid=#{row_uuid}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and (income_status='已制账单' or income_status='已录收入')\n")
    void updateOrderIncomeStatusLC(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("income_status") String income_status, @Param("row_uuid") String row_uuid);

    @Update("update af_order set\n"
            + " income_status='部分核销' "
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and income_status='核销完毕' ")
    void updateOrderIncomeStatus4(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Update("update sc_order set\n"
            + " income_status='部分核销' "
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and income_status='核销完毕' ")
    void updateOrderIncomeStatusSE4(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Update("update sc_order set\n"
            + " income_status='部分核销' "
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and income_status='核销完毕' ")
    void updateOrderIncomeStatusTC4(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Update("update af_order set\n"
            + " cost_status='部分核销' "
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and cost_status='核销完毕' ")
    void updateOrderCostStatus4(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Update("update sc_order set\n"
            + " cost_status='部分核销' "
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and cost_status='核销完毕' ")
    void updateOrderCostStatusSE4(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Update("update tc_order set\n"
            + " cost_status='部分核销' "
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and cost_status='核销完毕' ")
    void updateOrderCostStatusTC4(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Update("update af_order set\n"
            + " income_status='核销完毕' "
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} ")
    void updateOrderIncomeStatus5(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Update("update sc_order set\n"
            + " income_status='核销完毕' "
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} ")
    void updateOrderIncomeStatusSE5(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Update("update tc_order set\n"
            + " income_status='核销完毕' "
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} ")
    void updateOrderIncomeStatusTC5(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Update("update af_order set\n"
            + " income_status=#{income_status},row_uuid=#{row_uuid}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and (income_status='未录收入' or income_status='已录收入')\n")
    void updateOrderIncomeStatus(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("income_status") String income_status, @Param("row_uuid") String row_uuid);

    @Update("update af_order set\n"
            + " cost_status=#{cost_status},row_uuid=#{row_uuid}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and (cost_status='未录成本' or cost_status='已录成本')\n")
    void updateOrderCostStatus(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("cost_status") String cost_status, @Param("row_uuid") String row_uuid);

    @Update("update sc_order set\n"
            + " income_status=#{income_status},row_uuid=#{row_uuid}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and (income_status='未录收入' or income_status='已录收入')\n")
    void updateOrderIncomeStatusSE(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("income_status") String income_status, @Param("row_uuid") String row_uuid);

    @Update("update tc_order set\n"
            + " income_status=#{income_status},row_uuid=#{row_uuid}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and (income_status='未录收入' or income_status='已录收入')\n")
    void updateOrderIncomeStatusTC(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("income_status") String income_status, @Param("row_uuid") String row_uuid);

    @Update("update sc_order set\n"
            + " cost_status=#{cost_status},row_uuid=#{row_uuid}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and (cost_status='未录成本' or cost_status='已录成本')\n")
    void updateOrderCostStatusSE(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("cost_status") String cost_status, @Param("row_uuid") String row_uuid);

    @Update("update tc_order set\n"
            + " cost_status=#{cost_status},row_uuid=#{row_uuid}\n"
            + " where  org_id = #{org_id} and order_uuid = #{order_uuid} and (cost_status='未录成本' or cost_status='已录成本')\n")
    void updateOrderCostStatusTC(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid, @Param("cost_status") String cost_status, @Param("row_uuid") String row_uuid);

    @Select({"<script>",
            "select income_id from af_income ",
            "	where org_id = #{org_id} and order_uuid = #{order_uuid} and debit_note_id IS NULL",
            "</script>"})
    List<Integer> getOrderIcome(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Select({"<script>",
            "select income_id from af_income ",
            "	where org_id = #{org_id} and order_uuid = #{order_uuid} and debit_note_id IS NOT NULL",
            "</script>"})
    List<Integer> getOrderIcome2(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Select({"<script>",
            "select income_id from sc_income ",
            "	where org_id = #{org_id} and order_uuid = #{order_uuid} and debit_note_id IS NULL",
            "</script>"})
    List<Integer> getSEOrderIcome(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Select({"<script>",
            "select income_id from tc_income ",
            "	where org_id = #{org_id} and order_uuid = #{order_uuid} and debit_note_id IS NULL",
            "</script>"})
    List<Integer> getTEOrderIcome(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Select({"<script>",
            "select income_id from lc_income ",
            "	where org_id = #{org_id} and order_uuid = #{order_uuid} and debit_note_id IS NULL",
            "</script>"})
    List<Integer> getLCOrderIcome(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Select({"<script>",
            "select income_id from tc_income ",
            "	where org_id = #{org_id} and order_uuid = #{order_uuid} and debit_note_id IS NULL",
            "</script>"})
    List<Integer> getTCOrderIcome(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Select({"<script>",
            "select income_id from sc_income ",
            "	where org_id = #{org_id} and order_uuid = #{order_uuid} and debit_note_id IS NOT NULL",
            "</script>"})
    List<Integer> getSEOrderIcome2(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Select({"<script>",
            "select income_id from tc_income ",
            "	where org_id = #{org_id} and order_uuid = #{order_uuid} and debit_note_id IS NOT NULL",
            "</script>"})
    List<Integer> getTCOrderIcome2(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Select({"<script>",
            "select debit_note_id from css_debit_note ",
            "	where org_id = #{org_id} and order_uuid = #{order_uuid} ",
            "</script>"})
    List<Integer> getOrderIncomeStatus(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Select({"<script>",
            "select order_uuid from css_debit_note ",
            "	where org_id = #{org_id} and debit_note_id = #{debit_note_id} ",
            "</script>"})
    List<String> getOrderUUID(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id);

    @Select({"<script>",
            "select order_uuid from css_debit_note ",
            "	where org_id = #{org_id} and statement_id = #{statement_id} ",
            "GROUP BY order_uuid ",
            "</script>"})
    List<String> getOrderUUID2(@Param("org_id") Integer org_id, @Param("statement_id") Integer statement_id);

    @Select({"<script>",
            "select * from css_debit_note ",
            "	where org_id = #{org_id} and order_uuid = #{order_uuid} ",
            "</script>"})
    List<CssDebitNote> getOrderBill(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);

    @Insert("insert into hrs_log (op_level,op_type,op_name,op_info,creator_id,create_time,org_id,dept_id) "
            + " VALUES ('高','强制关闭',#{op_name},#{op_info},#{creator_id},#{create_time},#{org_id},#{dept_id})")
    void insertHrsLog(@Param("op_name") String op_name, @Param("op_info") String op_info,
                      @Param("creator_id") Integer creator_id, @Param("create_time") LocalDateTime create_time,
                      @Param("org_id") Integer org_id, @Param("dept_id") Integer dept_id);

    @Select("SELECT\n" +
            "  MIN(CASE WHEN IFNULL(cost_amount_writeoff,0)=cost_amount THEN '1' ELSE '0' END) AS completeWriteoffFlag\n" +
            " ,MAX(CASE WHEN IFNULL(cost_amount_writeoff,0) <> 0 THEN '1' ELSE '0' END) AS writeoffFlag\n" +
            " ,MAX(CASE WHEN IFNULL(cost_amount_payment,0)<>0 THEN '1' ELSE '0' END) AS paymentFlag\n" +
            " ,'1' AS costFlag\n" +
            "FROM af_cost where order_id=#{orderId}\n" +
            "GROUP BY order_id")
    Map<String, String> getOrderCostStatusForAF(Integer orderId);

    @Select("SELECT\n" +
            "  MIN(CASE WHEN IFNULL(cost_amount_writeoff,0)=cost_amount THEN '1' ELSE '0' END) AS completeWriteoffFlag\n" +
            " ,MAX(CASE WHEN IFNULL(cost_amount_writeoff,0) <> 0 THEN '1' ELSE '0' END) AS writeoffFlag\n" +
            " ,MAX(CASE WHEN IFNULL(cost_amount_payment,0)<>0 THEN '1' ELSE '0' END) AS paymentFlag\n" +
            " ,'1' AS costFlag\n" +
            "FROM lc_cost where order_id=#{orderId}\n" +
            "GROUP BY order_id")
    Map<String, String> getOrderCostStatusForLC(Integer orderId);

    @Select("SELECT\n" +
            "  MIN(CASE WHEN IFNULL(cost_amount_writeoff,0)=cost_amount THEN '1' ELSE '0' END) AS completeWriteoffFlag\n" +
            " ,MAX(CASE WHEN IFNULL(cost_amount_writeoff,0) <> 0 THEN '1' ELSE '0' END) AS writeoffFlag\n" +
            " ,MAX(CASE WHEN IFNULL(cost_amount_payment,0)<>0 THEN '1' ELSE '0' END) AS paymentFlag\n" +
            " ,'1' AS costFlag\n" +
            "FROM sc_cost where order_id=#{orderId}\n" +
            "GROUP BY order_id")
    Map<String, String> getOrderCostStatusForSC(Integer orderId);

    @Select("SELECT\n" +
            "  MIN(CASE WHEN IFNULL(cost_amount_writeoff,0)=cost_amount THEN '1' ELSE '0' END) AS completeWriteoffFlag\n" +
            " ,MAX(CASE WHEN IFNULL(cost_amount_writeoff,0) <> 0 THEN '1' ELSE '0' END) AS writeoffFlag\n" +
            " ,MAX(CASE WHEN IFNULL(cost_amount_payment,0)<>0 THEN '1' ELSE '0' END) AS paymentFlag\n" +
            " ,'1' AS costFlag\n" +
            "FROM tc_cost where order_id=#{orderId}\n" +
            "GROUP BY order_id")
    Map<String, String> getOrderCostStatusForTC(Integer orderId);

    @Update("update sc_order set\n"
            + " cost_status=#{status},row_uuid=#{rowUuid}\n"
            + " where order_id = #{orderId}\n")
    void updateOrderCostStatusForSC(@Param("orderId") Integer orderId, @Param("status") String status, @Param("rowUuid") String rowUuid);

    @Update("update tc_order set\n"
            + " cost_status=#{status},row_uuid=#{rowUuid}\n"
            + " where order_id = #{orderId}\n")
    void updateOrderCostStatusForTC(@Param("orderId") Integer orderId, @Param("status") String status, @Param("rowUuid") String rowUuid);

    @Select(
            "SELECT DISTINCT appraisal_company " +
                    " FROM " +
                    " ( " +
                    " SELECT appraisal_company FROM af_order " +
                    " WHERE org_id=#{org_id} " +
                    " AND business_scope='AE'  " +
                    " AND order_status<>'强制关闭'  " +
                    " AND IFNULL(appraisal_company,'')<>''  " +
                    " ORDER BY order_id DESC " +
                    " LIMIT 1000 " +
                    " ) AS T  " +
                    " LIMIT 5 "
    )
    List<Map<String, Object>> selectCompany(@Param("org_id") Integer org_id);

    @Select("select max(b.service_name) service_name,b.service_id,MAX(b.service_remark) service_remark,SUM(b.income_functional_amount) income_functional_amount,SUM(b.cost_functional_amount) cost_functional_amount,SUM(b.income_functional_amount)-SUM(b.cost_functional_amount) profit_functional_amount from (select service_id,service_name,service_remark,CASE flag when 'in' then functional_amount when 'co' then 0 END AS income_functional_amount,CASE flag when 'co' then functional_amount when 'in' then 0 END AS cost_functional_amount from \n" +
            "(select service_id,service_name,service_remark,income_functional_amount functional_amount,'in' flag from sc_income where order_id=#{orderId}\n" +
            "UNION ALL\n" +
            "select service_id,service_name,service_remark,cost_functional_amount functional_amount,'co' flag from sc_cost where order_id=#{orderId}) a) b GROUP BY b.service_id")
    List<BusinessCalculationBill> selectBusinessCalculationForSC(Integer orderId);

    @Select("select max(b.service_name) service_name,b.service_id,MAX(b.service_remark) service_remark,SUM(b.income_functional_amount) income_functional_amount,SUM(b.cost_functional_amount) cost_functional_amount,SUM(b.income_functional_amount)-SUM(b.cost_functional_amount) profit_functional_amount from (select service_id,service_name,service_remark,CASE flag when 'in' then functional_amount when 'co' then 0 END AS income_functional_amount,CASE flag when 'co' then functional_amount when 'in' then 0 END AS cost_functional_amount from \n" +
            "(select service_id,service_name,service_remark,income_functional_amount functional_amount,'in' flag from tc_income where order_id=#{orderId}\n" +
            "UNION ALL\n" +
            "select service_id,service_name,service_remark,cost_functional_amount functional_amount,'co' flag from tc_cost where order_id=#{orderId}) a) b GROUP BY b.service_id")
    List<BusinessCalculationBill> selectBusinessCalculationForTC(Integer orderId);

    @Select("select max(b.service_name) service_name,b.service_id,MAX(b.service_remark) service_remark,SUM(b.income_functional_amount) income_functional_amount,SUM(b.cost_functional_amount) cost_functional_amount,SUM(b.income_functional_amount)-SUM(b.cost_functional_amount) profit_functional_amount from (select service_id,service_name,service_remark,CASE flag when 'in' then functional_amount when 'co' then 0 END AS income_functional_amount,CASE flag when 'co' then functional_amount when 'in' then 0 END AS cost_functional_amount from \n" +
            "(select service_id,service_name,service_remark,income_functional_amount functional_amount,'in' flag from lc_income where order_id=#{orderId}\n" +
            "UNION ALL\n" +
            "select service_id,service_name,service_remark,cost_functional_amount functional_amount,'co' flag from lc_cost where order_id=#{orderId}) a) b GROUP BY b.service_id")
    List<BusinessCalculationBill> selectBusinessCalculationForLC(Integer orderId);

    @Select("select max(b.service_name) service_name,b.service_id,MAX(b.service_remark) service_remark,SUM(b.income_functional_amount) income_functional_amount,SUM(b.cost_functional_amount) cost_functional_amount,SUM(b.income_functional_amount)-SUM(b.cost_functional_amount) profit_functional_amount from (select service_id,service_name,service_remark,CASE flag when 'in' then functional_amount when 'co' then 0 END AS income_functional_amount,CASE flag when 'co' then functional_amount when 'in' then 0 END AS cost_functional_amount from \n" +
            "(select service_id,service_name,service_remark,income_functional_amount functional_amount,'in' flag from af_income where order_id=#{orderId}\n" +
            "UNION ALL\n" +
            "select service_id,service_name,service_remark,cost_functional_amount functional_amount,'co' flag from af_cost where order_id=#{orderId}) a) b GROUP BY b.service_id")
    List<BusinessCalculationBill> selectBusinessCalculationForAF(Integer orderId);

    @Select({"<script>",
            "CALL af_P_awb_print(#{org_id},#{awbprintType},#{orderUuid},NULL,0)\n",
            "</script>"})
    AfPAwbSubmitPrintProcedure printAwbSubmit(@Param("org_id") Integer org_id, @Param("orderUuid") String orderUuid, @Param("awbprintType") String awbprintType);

    @Select({"<script>",
            "CALL af_P_awb_print(#{org_id},#{awbprintType},#{orderUuid},NULL,0)\n",
            "</script>"})
    List<AfPAwbSubmitPrintProcedure> printHawbSubmit(@Param("org_id") Integer org_id, @Param("orderUuid") String orderUuid, @Param("awbprintType") String awbprintType);

    @Select("SELECT \n" +
            "\tO.order_uuid as orderUuid, \n" +
            "\tORG.org_logo as logoUrl,\n" +
            "\tO.arrival_station as arrivalStation,\n" +
            "\tO.plan_pieces as planPieces,\n" +
            "\tO.package_type as packageType,\n" +
            "\tO.plan_weight as planWeight,\n" +
            "\tO.plan_volume as planVolume,\n" +
            "\tIFNULL(O.customer_number, O.order_code) as inboundNumber,\n" +
            "\tO.awb_number as awbNumber,\n" +
            "\tDATE_FORMAT(IF(#{flag}='storehouse', O.inbound_date, O.receipt_date), '%Y-%m-%d') as inboundDate,\n" +
            "\tIF(#{flag}='storehouse', SH.warehouse_name_cn, W.warehouse_name_cn) as sendTo,\n" +
            "\tIF(#{flag}='storehouse', SH.warehouse_contact_remark, W.warehouse_contact_remark) as consignee,\n" +
            "\tuser.user_name as sales_name,\n" +
            "\tuser.phone_number as phone_number,\n" +
            "\tIF(#{flag}='storehouse', SH.warehouse_address_gps, W.warehouse_address_gps) as address,\n" +
            "\tIF(#{flag}='storehouse', SH.warehouse_longitude, W.warehouse_longitude) as addressLongitude,\n" +
            "\tO.org_id,\n" +
            "\tORG.org_uuid,\n" +
            "\tIF(#{flag}='storehouse', SH.warehouse_latitude, W.warehouse_latitude) as addressLatitude\n" +
            "\n" +
            "\tFROM `af_order` O\n" +
            "\tLEFT JOIN hrs_org ORG ON ORG.org_id = O.org_id\n" +
            "\tLEFT JOIN hrs_user user ON O.servicer_id = user.user_id\n" +
            "\t-- 普货仓库\n" +
            "\tLEFT JOIN af_warehouse SH ON SH.warehouse_id = O.departure_storehouse_id\n" +
            "\t-- 交货货站\n" +
            "\tLEFT JOIN af_warehouse W ON W.warehouse_id = O.departure_warehouse_id\n" +
            "\tWHERE O.order_uuid = #{orderUUID}\n" +
            "\t")
    OrderDeliveryNotice getOrderDeliveryNotice(@Param("orderUUID") String orderUuid, @Param("flag")String flag);

    @Select("select port_name_en from sc_port_maintenance where port_code=#{portCode}")
    Map<String, String> queryScPortMaintenanceByCode(@Param("portCode") String portCode);


    @Select({"<script>",
            "SELECT * FROM af_V_prm_coop\n",
            " where org_id=#{bean.orgId}",
            " <when test='bean.businessScope==\"AE\"'>",
            " AND coop_type in ('外部客户','互为代理') AND business_scope_AE='AE'",
            "</when>",
            " <when test='bean.businessScope==\"TE\"'>",
            " AND coop_type in ('外部客户','互为代理') AND business_scope_TE='TE'",
            "</when>",
            " <when test='bean.businessScope==\"TI\"'>",
            " AND coop_type in ('外部客户','互为代理','海外代理') AND business_scope_TI='TI'",
            "</when>",
            " <when test='bean.businessScope==\"SE\"'>",
            " AND coop_type in ('外部客户','互为代理') AND business_scope_SE='SE'",
            "</when>",
            " <when test='bean.businessScope==\"AI\"'>",
            " AND coop_type in ('外部客户','互为代理','海外代理') AND business_scope_AI='AI'",
            "</when>",
            " <when test='bean.businessScope==\"SI\"'>",
            " AND coop_type in ('外部客户','互为代理','海外代理') AND business_scope_SI='SI'",
            "</when>",
            " <when test='bean.businessScope==\"LC\"'>",
            " AND coop_type in ('外部客户','互为代理','海外代理') AND business_scope_LC='LC'",
            "</when>",
            " <when test='bean.businessScope==\"VL\"'>",
            " AND coop_type in ('外部客户','互为代理','海外代理')",
            "</when>",
            " <when test='bean.coopName!=null and bean.coopName!=\"\"'>",
            " AND (coop_name like  \"%\"#{bean.coopName}\"%\" or coop_code like  \"%\"#{bean.coopName}\"%\")",
            "</when>",
            "</script>"})
    IPage<VPrmCoop> getCoopList(Page page, @Param("bean") VPrmCoop bean);

    @Select("SELECT\n" +
            "\tA.awb_number\n" +
            "\t,IFNULL(B.remark,C.remark) AS departure_station\n" +
            "\t,D.remark AS arrival_station\n" +
            "\t,C.flight_num,C.event_time\n" +
            "FROM \n" +
            "(\n" +
            "\tSELECT #{awbNumber} AS awb_number\n" +
            ") AS A\n" +
            "LEFT JOIN (\n" +
            "\tSELECT awb_number,REPLACE(LEFT(remark,4),' ','') AS remark FROM af_awb_route_track_awb\n" +
            "\tWHERE flight_status_code IN ('RCS')\n" +
            "\tAND awb_number=#{awbNumber}\n" +
            "\tORDER BY source_syscode ASC\n" +
            "\tLIMIT 1\n" +
            ") AS B ON A.awb_number=B.awb_number\n" +
            "LEFT JOIN (\n" +
            "\tSELECT awb_number,REPLACE(LEFT(remark,4),' ','') AS remark,flight_num,event_time FROM af_awb_route_track_awb\n" +
            "\tWHERE flight_status_code IN ('DEP')\n" +
            "\tAND awb_number=#{awbNumber}\n" +
            "\tORDER BY source_syscode ASC\n" +
            "\tLIMIT 1\n" +
            ") AS C ON A.awb_number=C.awb_number\n" +
            "LEFT JOIN (\n" +
            "\tSELECT awb_number,REPLACE(LEFT(remark,4),' ','') AS remark FROM af_awb_route_track_awb\n" +
            "\tWHERE flight_status_code IN ('DLV')\n" +
            "\tAND awb_number=#{awbNumber}\n" +
            "\tORDER BY source_syscode ASC\n" +
            "\tLIMIT 1\n" +
            ") AS D ON A.awb_number=D.awb_number")
    Map<String, Object> selectAwbTrackInfo(@Param("awbNumber") String awbNumber);


    @Select({"<script>",
            "SELECT ap_name_en AS apNameEn FROM af_airport\n",
            "	where ap_code = #{destinationCode} AND ap_status = 1",
            "</script>"})
    String getApNameEnByCode(@Param("destinationCode") String destinationCode);

//    @Options(statementType = StatementType.CALLABLE)
//    @ResultMap({"airCargoManifest1", "airCargoManifest2"})
//    @Select("CALL af_P_air_cargo_manifest_print(#{orderId},#{orgId})")
//    List<List<AirCargoManifestPrint>> airCargoManifestPrint(@Param("orderId") Integer orderId, @Param("orgId") Integer orgId);

    //    @Update("update af_order set\n"
//            + " manifest_status=#{manifestStatus}\n"
//            + " where order_uuid = #{orderUuid}")
    @Update("CALL af_P_orderOrShipperUpdateManifestStatus(#{hasMwb},#{orderUuid},#{letterIds},#{manifestStatus})")
    void updateManifestStatus(@Param("hasMwb") String hasMwb, @Param("orderUuid") String orderUuid, @Param("letterIds") String letterIds, @Param("manifestStatus") String manifestStatus);

    @Options(statementType = StatementType.CALLABLE)
    @Select("CALL af_P_operaLook_queryList(#{bean.orgId},#{bean.awbNumber},#{bean.hawbNumber},#{bean.coopName}," +
            "#{bean.departureStation},#{bean.arrivalStation},#{bean.expectFlight}," +
            "#{bean.flightDateBegin},#{bean.flightDateEnd},#{bean.departureWarehouseName}" +
            ",#{bean.presets},#{bean.arrived},#{bean.passed},#{bean.ams},#{bean.entryPlate},null,null)")
    List<Map<String,Object>> getOpreationLookList(@Param("bean") AfOrder bean);
    @Options(statementType = StatementType.CALLABLE)
    @Select("CALL af_P_operaLook_queryList(#{bean.orgId},#{bean.awbNumber},#{bean.hawbNumber},#{bean.coopName}," +
            "#{bean.departureStation},#{bean.arrivalStation},#{bean.expectFlight}," +
            "#{bean.flightDateBegin},#{bean.flightDateEnd},#{bean.departureWarehouseName}" +
            ",#{bean.presets},#{bean.arrived},#{bean.passed},#{bean.ams},#{bean.entryPlate},#{current},#{sizes})")
//    IPage<Map<String, Object>> getOpreationLookPageList(Page page,@Param("bean") AfOrder bean);

    List<Map<String, Object>> getOpreationLookPageList(@Param("bean") AfOrder bean, @Param("current") int current, @Param("sizes") int sizes);

    @Select({"<script>",
            "SELECT org_name FROM hrs_org\n",
            "	where org_id = #{org_id} ",
            "</script>"})
    String getOrgName(@Param("org_id") Integer org_id);
    @Select({"<script>",
        "SELECT currency_rate FROM af_V_currency_rate",
        "where org_id=#{org_id} and currency_code=#{currency_code} ORDER BY currency_code asc",
        "</script>"})
    BigDecimal getCurrencyRate(@Param("org_id") Integer org_id,@Param("currency_code") String currency_code);
    @Select({"<script>",
		"SELECT cargo_type cargoType,GROUP_CONCAT(goods_cnnames) goodsCnnames FROM af_cargo_goodsnames ",
		"	where order_id = #{order_id}",
		"GROUP BY cargo_type",
		"ORDER BY cargo_type DESC",
	"</script>"})
    List<CargoGoodsnames> queryGoodsNamelist(@Param("order_id") Integer order_id);
    
    @Insert("insert into af_order_extend \n"
            + " ( org_id,order_id,pallet_material,special_package,celsius_require,thermometer,is_celsius_require) \n"
            + "	 values (#{org_id},#{bean.orderId},#{bean.palletMaterial},#{bean.specialPackage},#{bean.celsiusRequire},#{bean.thermometer},#{bean.isCelsiusRequire})\n")
    void insertOrderExtend(@Param("org_id") Integer org_id, @Param("bean") AfOperateOrder bean);
    @Delete("delete from af_order_extend where org_id=#{org_id} and order_id=#{order_id}")
    void deleteOrderExtend(@Param("org_id") Integer org_id, @Param("order_id") Integer order_id);
    
    @Select({"<script>",
        "select * from af_order_extend\n",
        "	where org_id = #{org_id} and order_id = #{order_id}",
        "</script>"})
    AfOrderExtend getOrderExtend(@Param("org_id") Integer org_id, @Param("order_id") Integer order_id);
}
