package com.efreight.afbase.dao;

import com.efreight.afbase.entity.AfCost;
import com.efreight.afbase.entity.RountingSign;
import com.efreight.afbase.entity.Service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * AF 订单管理 出口订单 签单表 Mapper 接口
 * </p>
 *
 * @author cwd
 * @since 2020-11-18
 */
public interface RountingSignMapper extends BaseMapper<RountingSign> {
	
	@Select({"<script>",
        "select * from hrs_org_order_config ",
        "	where business_scope=#{businessScope} and org_id = #{orgId}",
        "</script>"})
    Map<String, Object> getOrgConfigForWhere(@Param("orgId") Integer orgId,@Param("businessScope") String businessScope);

	@Select({"<script>",
        "select * from af_cost where",
        " org_id = #{orgId} and order_id =#{orderId} AND service_name='干线 - 空运费'",
        " AND (financial_date IS NOT NULL OR IFNULL(cost_amount_payment,0)!=0)",
//        " cost_amount_payment is not null",
        "</script>"})
	List<Map> getCostByWhere(@Param("orgId") Integer orgId,@Param("orderId") Integer orderId);
	@Select({"<script>",
        "select * from af_cost where",
        " org_id = #{orgId} and order_id =#{orderId} AND service_name='干线 - 空运费'",
        " AND financial_date IS  NULL and IFNULL(cost_amount_payment,0)=0 ",
        "</script>"})
	List<AfCost> getCostByWhere2(@Param("orgId") Integer orgId,@Param("orderId") Integer orderId);
	@Select({"<script>",
        "select * from af_cost where",
        " org_id = #{orgId} and order_id =#{orderId} AND service_name!='干线 - 空运费'",
        "</script>"})
	List<AfCost> getCostByWhere3(@Param("orgId") Integer orgId,@Param("orderId") Integer orderId);
	@Select({"<script>",
        "select * from af_service where",
        " org_id = #{orgId} and service_type ='干线' AND service_name_cn='空运费'",
        "</script>"})
	List<Map> getAfService(@Param("orgId") Integer orgId);
	@Select({"<script>",
        "select * from af_rounting_sign where",
        " org_id = #{bean.orgId} and order_id = #{bean.orderId} and business_scope=#{bean.businessScope}",
        "<when test='bean.signState!=null'>",
        " AND sign_state=#{bean.signState}",
        "</when>",
        "</script>"})
	RountingSign getRountingSign(@Param("bean") RountingSign bean);
	
	
	@Select({"<script>",
        "SELECT SUM(cost_functional_amount) AS msr_unitprice",
        " FROM af_cost",
        " where org_id = #{bean.orgId} and order_id = #{bean.orderId} and service_name='干线 - 空运费'",
        " GROUP BY order_id",
        "</script>"})
	RountingSign getAfCostForWhere(@Param("bean") RountingSign bean);
	
}
