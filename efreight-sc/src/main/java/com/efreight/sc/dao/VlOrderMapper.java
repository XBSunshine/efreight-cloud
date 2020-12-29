package com.efreight.sc.dao;

import com.efreight.common.remoteVo.Service;
import com.efreight.sc.entity.VlOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.sc.entity.VlOrderDetailOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * VL 订单管理 派车订单 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-31
 */
public interface VlOrderMapper extends BaseMapper<VlOrder> {

    @Select("select UUID()")
    String getUuid();

    @Update("update af_order set cost_status=#{status},row_uuid=#{rowUuid} where order_id=#{orderId}")
    void updateAfOrderCostStatus(@Param("status") String status, @Param("orderId") Integer orderId, @Param("rowUuid") String rowUuid);

    @Select("select sum(IFNULL(confirm_charge_weight,IFNULL(plan_charge_weight,0))) as weightSum,sum(IFNULL(confirm_volume,IFNULL(plan_volume,0))) as volumeSum from af_order where org_id=#{orgId} and order_id in (${afOrderIds})")
    Map<String, Double> getWeightAndVolumeSumForAF(@Param("afOrderIds") String afOrderIds, @Param("orgId") Integer orgId);

    @Select("select sum(IFNULL(plan_charge_weight,0)) as weightSum,sum(IFNULL(plan_volume,0)) as volumeSum from sc_order where org_id=#{orgId} and order_id in (${scOrderIds})")
    Map<String, BigDecimal> getWeightAndVolumeSumForSC(@Param("scOrderIds") String scOrderIds, @Param("orgId") Integer orgId);

    @Select("select sum(IFNULL(plan_charge_weight,0)) as weightSum,sum(IFNULL(plan_volume,0)) as volumeSum from tc_order where org_id=#{orgId} and order_id in (${tcOrderIds})")
    Map<String, BigDecimal> getWeightAndVolumeSumForTC(@Param("tcOrderIds") String tcOrderIds, @Param("orgId") Integer orgId);

    @Select("select sum(IFNULL(confirm_charge_weight,IFNULL(plan_charge_weight,0))) as weightSum,sum(IFNULL(confirm_volume,IFNULL(plan_volume,0))) as volumeSum from lc_order where org_id=#{orgId} and order_id in (${lcOrderIds})")
    Map<String, BigDecimal> getWeightAndVolumeSumForLC(@Param("lcOrderIds") String lcOrderIds, @Param("orgId") Integer orgId);

    @Select("SELECT\n" +
            "\ta.*,\n" +
            "\taf.plan_charge_weight AS plan_charge_weight_new,\n" +
            "\taf.plan_density as plan_density_new,\n" +
            "\taf.plan_pieces as plan_pieces_new,\n" +
            "\taf.plan_volume as plan_volume_new,\n" +
            "\taf.plan_weight as plan_weight_new,\n" +
            "\taf.confirm_charge_weight as confirm_charge_weight_new,\n" +
            "\taf.confirm_pieces as confirm_pieces_new,\n" +
            "\taf.confirm_volume as confirm_volume_new,\n" +
            "\taf.confirm_weight as confirm_weight_new,\n" +
            "\taf.awb_number,\n" +
            "\taf.hawb_number,\n" +
            "\taf.order_code,\n" +
            "\taf.order_uuid,\n" +
            "\taf.customer_number\n" +
            "FROM\n" +
            "\tvl_order_detail_order a\n" +
            "\tINNER JOIN af_order af ON a.vl_order_id = #{orderId} \n" +
            "\tAND a.order_id = af.order_id \n" +
            "\tAND a.business_scope = af.business_scope \n" +
            "\tWHERE a.org_id = #{orgId}\n" +
            "UNION ALL\n" +
            "SELECT\n" +
            "\ta.*,\n" +
            "  af.plan_charge_weight AS plan_charge_weight_new,\n" +
            "\tnull as plan_density_new,\n" +
            "\taf.plan_pieces as plan_pieces_new,\n" +
            "\taf.plan_volume as plan_volume_new,\n" +
            "\taf.plan_weight as plan_weight_new,\n" +
            "\tnull as confirm_charge_weight_new,\n" +
            "\tnull as confirm_pieces_new,\n" +
            "\tnull as confirm_volume_new,\n" +
            "\tnull as confirm_weight_new,\n" +
            "\taf.mbl_number as awb_number,\n" +
            "\taf.hbl_number as hawb_number,\n" +
            "\taf.order_code,\n" +
            "\taf.order_uuid,\n" +
            "\taf.customer_number\n" +
            "FROM\n" +
            "\tvl_order_detail_order a\n" +
            "\tINNER JOIN sc_order af ON a.vl_order_id = #{orderId} \n" +
            "\tAND a.order_id = af.order_id \n" +
            "\tAND a.business_scope = af.business_scope\n" +
            "\tWHERE a.org_id = #{orgId}\n" +
            "UNION ALL\n" +
            "SELECT\n" +
            "\ta.*,\n" +
            "\taf.plan_charge_weight AS plan_charge_weight_new,\n" +
            "\tnull as plan_density_new,\n" +
            "\taf.plan_pieces as plan_pieces_new,\n" +
            "\taf.plan_volume as plan_volume_new,\n" +
            "\taf.plan_weight as plan_weight_new,\n" +
            "\tnull as confirm_charge_weight_new,\n" +
            "\tnull as confirm_pieces_new,\n" +
            "\tnull as confirm_volume_new,\n" +
            "\tnull as confirm_weight_new,\n" +
            "\taf.rwb_number as awb_number,\n" +
            "\tnull as hawb_number,\n" +
            "\taf.order_code,\n" +
            "\taf.order_uuid,\n" +
            "\taf.customer_number\n" +
            "FROM\n" +
            "\tvl_order_detail_order a\n" +
            "\tINNER JOIN tc_order af ON a.vl_order_id = #{orderId} \n" +
            "\tAND a.order_id = af.order_id \n" +
            "\tAND a.business_scope = af.business_scope\n" +
            "\tWHERE a.org_id = #{orgId}\n" +
            "UNION ALL\n" +
            "SELECT\n" +
            "\ta.*,\n" +
            "\taf.plan_charge_weight AS plan_charge_weight_new,\n" +
            "\taf.plan_density as plan_density_new,\n" +
            "\taf.plan_pieces as plan_pieces_new,\n" +
            "\taf.plan_volume as plan_volume_new,\n" +
            "\taf.plan_weight as plan_weight_new,\n" +
            "\taf.confirm_charge_weight as confirm_charge_weight_new,\n" +
            "\taf.confirm_pieces as confirm_pieces_new,\n" +
            "\taf.confirm_volume as confirm_volume_new,\n" +
            "\taf.confirm_weight as confirm_weight_new,\n" +
            "\taf.customer_number as awb_number,\n" +
            "\tnull as hawb_number,\n" +
            "\taf.order_code,\n" +
            "\taf.order_uuid,\n" +
            "\taf.customer_number\n" +
            "FROM\n" +
            "\tvl_order_detail_order a\n" +
            "\tINNER JOIN lc_order af ON a.vl_order_id = #{orderId} \n" +
            "\tAND a.order_id = af.order_id \n" +
            "\tAND a.business_scope = af.business_scope\n" +
            "\tWHERE a.org_id = #{orgId}")
    List<VlOrderDetailOrder> selectVlOrderDetailOrderList(@Param("orgId") Integer orgId, @Param("orderId") Integer orderId);

    @Select({"<script>",
            " SELECT  ",
            "  a.*,CONCAT(a.service_mnemonic,' - ',a.service_type,' - ',a.service_name_cn,CASE WHEN a.service_name_en IS NOT NULL AND a.service_name_en !='' THEN CONCAT('  <![CDATA[<]]>',IFNULL(a.service_name_en,''),'<![CDATA[>]]>') ELSE '' END) value ,",
            " b.currency_rate incomeExchangeRate,",
            " c.currency_rate costExchangeRate",
            " FROM af_service a",
            "LEFT JOIN af_V_currency_rate b ON a.income_currency=b.currency_code",
            "LEFT JOIN af_V_currency_rate c ON a.cost_currency=c.currency_code",
            "LEFT JOIN af_V_prm_category d ON a.service_type=d.param_text ",
            "	where a.org_id = #{org_id} and b.org_id=#{org_id} and c.org_id=#{org_id} AND d.category_name='服务类别' and a.is_valid=1 and a.business_scope in (${business_scope} ) \n",
            " order by a.service_mnemonic ASC,a.service_name_cn ASC \n",
            "</script>"})
    List<Service> getServices(@Param("org_id") Integer org_id, @Param("business_scope") String business_scope);
}
