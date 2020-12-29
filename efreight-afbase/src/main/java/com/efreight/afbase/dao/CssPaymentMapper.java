package com.efreight.afbase.dao;

import com.efreight.afbase.entity.CssPayment;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * CSS 成本对账单 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-05
 */
public interface CssPaymentMapper extends BaseMapper<CssPayment> {
	
	@Select({"<script>",
			"SELECT * FROM css_payment A \n" + 
			"INNER JOIN css_payment_detail B ON A.payment_id=B.payment_id \n" + 
			"WHERE A.org_id=#{orgId} \n" + 
			"AND A.business_scope=#{businessScope} \n" + 
			"AND B.cost_id IN (select C.cost_id from ",
			"<when test='businessScope==\"AI\" or businessScope==\"AE\"'>",
			"af_cost ",
	        "</when>",
	        "<when test='businessScope==\"SI\" or businessScope==\"SE\"'>",
	        "sc_cost ",
	        "</when>",
			"C WHERE C.org_id=#{orgId} AND C.order_uuid=#{orderUuid})",
			"</script>"})
	List<CssPayment> queryCssPaymentListForWhere(@Param("orgId") Integer orgId,@Param("businessScope") String businessScope,@Param("orderUuid") String orderUuid);

	@Select("select port_name_en from sc_port_maintenance where port_code=#{portCode}")
	Map<String,String> getPortName(@Param("portCode") String portCode);
}
