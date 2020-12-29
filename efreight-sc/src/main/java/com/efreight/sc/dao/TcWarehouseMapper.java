package com.efreight.sc.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.sc.entity.OrderDeliveryNotice;
import com.efreight.sc.entity.TcWarehouse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * TC 基础信息 堆场仓库 Mapper 接口
 * </p>
 *
 * @author caiwd
 * @since 2020-07-14
 */
public interface TcWarehouseMapper extends BaseMapper<TcWarehouse> {

    @Select({"<script>",
            " select a.warehouse_id,a.org_id,a.business_scope,a.warehouse_code,a.ap_code,a.warehouse_name_cn,a.warehouse_name_en" ,
            ",a.warehouse_longitude,a.warehouse_latitude,a.warehouse_address_gps,a.warehouse_status,a.customs_supervision,a.customs_code",
            ",a.warehouse_contact_remark,a.creator_id,a.creator_name,a.create_time,a.editor_id,a.editor_name,a.edit_time,IF(IFNULL(a.edit_time,'')='',a.create_time,a.edit_time) AS operate_time",
            ",IF(IFNULL(a.edit_time,'')='',a.creator_name,a.editor_name) AS operate_name ",
            " from tc_warehouse a ",
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
    IPage<TcWarehouse> getPage(Page page, @Param("bean") TcWarehouse bean);

    @Select({"<script>",
            " select a.warehouse_id,a.org_id,a.business_scope,a.warehouse_code,a.ap_code,a.warehouse_name_cn,a.warehouse_name_en" ,
            ",a.warehouse_longitude,a.warehouse_latitude,a.warehouse_address_gps,a.warehouse_status,a.customs_supervision,a.customs_code",
            ",a.warehouse_contact_remark,a.creator_id,a.creator_name,a.create_time,a.editor_id,a.editor_name,a.edit_time,IF(IFNULL(a.edit_time,'')='',a.create_time,a.edit_time) AS operate_time",
            ",IF(IFNULL(a.edit_time,'')='',a.creator_name,a.editor_name) AS operate_name ",
            " from tc_warehouse a ",
            " where a.warehouse_status=1 ",
            "<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
            " AND a.business_scope = #{bean.businessScope}",
            "</when>",
            "<when test='bean.orgId!=null'>",
            " AND a.org_id = #{bean.orgId}",
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
            "</script>"})
    List<TcWarehouse> getList(@Param("bean") TcWarehouse bean);

    @Select({"<script>",
            "SELECT " ,
            "O.order_uuid,",
            "ORG.org_logo,",
            "O.arrival_station AS arrival_station,",
            "O.container_list,",
            "O.plan_pieces,",
            "O.plan_weight,",
            "O.plan_volume,",
            "O.order_code," ,
            "O.container_load_address_cn," ,
            "ware.warehouse_contact_remark," ,
            "ware.warehouse_longitude," ,
            "ware.warehouse_latitude," ,
            "huser.phone_number," ,
            "huser.user_name," ,
            "ware.warehouse_address_gps, " ,
            "O.org_id, " ,
            "ORG.org_uuid " ,
            "FROM tc_order O" ,
            "LEFT JOIN hrs_org ORG ON ORG.org_id = O.org_id" ,
            "LEFT JOIN hrs_user huser ON O.servicer_id = huser.user_id" ,
            "LEFT JOIN tc_warehouse ware on O.container_load_warehouse_id = ware.warehouse_id  and ware.warehouse_status=1 " ,
            "WHERE O.order_uuid = #{orderUUID}" ,
            "</script>"})
    OrderDeliveryNotice getOrderDeliveryNotice(String orderUUID);
}
