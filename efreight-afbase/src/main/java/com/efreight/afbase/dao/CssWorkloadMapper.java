package com.efreight.afbase.dao;

import java.util.List;
import java.util.Map;

import com.efreight.afbase.entity.procedure.CssWorkloadDetail;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.procedure.CssWorkload;


public interface CssWorkloadMapper extends BaseMapper<CssWorkload>{
	 @Select({"<script>",
         "CALL css_P_workload(#{bean.businessScope},#{bean.flightDateStart},#{bean.flightDateEnd},#{bean.orderStatus},#{bean.workloadType},#{bean.orgId},#{bean.dept})\n",
         "</script>"})
   List<CssWorkload> getCssWorkloadList(@Param("bean") CssWorkload bean);
	 
//	@Select({"<script>",
//		 "select ",
//		 "* ",
//		 "from af_order",
//		 "where ",
//		 "business_scope = #{bean.businessScope} ",
//		 "<when test='bean.orderStatus!=null'>",
//	     " and order_status = #{bean.orderStatus} ",
//	     "</when>",
//	     "<when test='bean.workloadType!=null and bean.workloadType==\"责任销售\"'>",
//	     " and sales_id = #{bean.userId} ",
//	     "</when>",
//	     "<when test='bean.workloadType!=null  and bean.workloadType==\"责任客服\"'>",
//	     " and servicer_id = #{bean.userId} ",
//	     "</when>",
//	     "<when test='bean.workloadType!=null  and bean.workloadType==\"责任操作\"'>",
//	     " and creator_id = #{bean.userId}",
//	     "</when>",
//	     "</script>"})
//   List<Map> getCssWorkloadDetail_A(@Param("bean") CssWorkload bean);
	 @Select({"<script>",
         "CALL css_P_workload_detail(#{bean.businessScope},#{bean.flightDateStart},#{bean.flightDateEnd},#{bean.orderStatus},#{bean.workloadType},#{bean.userId},#{bean.orgId})\n",
         "</script>"})
	 List<Map> getCssWorkloadDetail(@Param("bean") CssWorkload bean);
//	@Select({"<script>",
//		"select * from sc_order where business_scope = #{bean.businessScope}",
//		 "<when test='bean.orderStatus!=null and bean.orderStatus!=\"\"'>",
//	     " and order_status = #{bean.orderStatus}",
//	     "</when>",
//	     "<when test='bean.workloadType!=null and bean.workloadType!=\"\" and bean.workloadType==\"责任销售\"'>",
//	     " and sales_id = #{bean.userId}",
//	     "</when>",
//	     "<when test='bean.workloadType!=null and bean.workloadType!=\"\" and bean.workloadType==\"责任客服\"'>",
//	     " and servicer_id = #{bean.userId}",
//	     "</when>",
//	     "<when test='bean.workloadType!=null and bean.workloadType!=\"\" and bean.workloadType==\"责任操作\"'>",
//	     " and creator_id = #{bean.userId}",
//	     "</when>",
//	     "</script>"})
//  List<Map> getCssWorkloadDetail_S(@Param("bean") CssWorkload bean);
	@Select({"<script>",
			"CALL css_P_workload_detail(#{bean.businessScope},#{bean.flightDateStart},#{bean.flightDateEnd},#{bean.orderStatus},#{bean.workloadType},#{bean.userId},#{bean.orgId})\n",
			"</script>"})
	List<CssWorkloadDetail> getCssWorkloadDetailForExcel(@Param("bean") CssWorkload bean);

}
