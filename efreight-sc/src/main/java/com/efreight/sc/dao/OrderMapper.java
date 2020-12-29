package com.efreight.sc.dao;

import com.efreight.sc.entity.Order;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.efreight.sc.entity.SeHawMakeProcedure;
import com.efreight.sc.entity.SeOrderLetterPrint;

import com.efreight.sc.entity.SeTrailerPrint;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * CS 订单管理 SI订单 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-02
 */
public interface OrderMapper extends BaseMapper<Order> {

    @Select("select UUID()")
    String getUuid();
    @Select({"<script>",
        "select a.*,b.coop_name customerName from sc_order a",
        "left join prm_coop b on a.coop_id=b.coop_id",
        "	where a.org_id = #{org_id} and a.order_uuid = #{order_uuid}",
        "</script>"})
    Order getOrderByUUID(@Param("org_id") Integer org_id, @Param("order_uuid") String order_uuid);
    @Select("select * from css_payment p inner join css_payment_detail d on p.payment_id=d.payment_id and p.business_scope='SI' and p.org_id=#{orgId} inner join sc_cost c on d.org_id=#{orgId} and c.cost_id=d.cost_id and c.order_id=#{orderId}")
    List queryPaymentForIfForceStop(@Param("orderId") Integer orderId, @Param("orgId")Integer orgId);
    @Insert("insert into hrs_log (op_level,op_type,op_name,op_info,creator_id,create_time,org_id,dept_id) "
    		+ " VALUES ('高','强制关闭',#{op_name},#{op_info},#{creator_id},#{create_time},#{org_id},#{dept_id})")
    void insertHrsLog(@Param("op_name") String op_name,@Param("op_info") String op_info,
    		@Param("creator_id") Integer creator_id,@Param("create_time") LocalDateTime create_time,
    		@Param("org_id") Integer org_id,@Param("dept_id") Integer dept_id);
    
    
    
    @Select({"<script>",
    		 " select a.*,e.contact_name AS contact_name_se from (select * from sc_order ${ew.customSqlSegment}) a ",
    		 " left join sc_port_maintenance b on a.departure_station=b.port_code ",
    		 " left join sc_port_maintenance c on a.arrival_station=c.port_code ",
    		 " LEFT JOIN (select GROUP_CONCAT(p.contacts_name) as contact_name,max(p.coop_id) as coop_id from prm_coop_contacts p  where p.org_id=#{bean.orgId} GROUP BY p.coop_id  ) e",
    		 " ON a.coop_id=e.coop_id",
    		 " where 1=1 ",
    		"<when test='bean.departureStation!=null and bean.departureStation!=\"\"'>",
            " AND (b.port_code like  \"%\"#{bean.departureStation}\"%\" or b.port_name_en like  \"%\"#{bean.departureStation}\"%\" )",
            "</when>",
            "<when test='bean.arrivalStation!=null and bean.arrivalStation!=\"\"'>",
            " AND (c.port_code like  \"%\"#{bean.arrivalStation}\"%\" or c.port_name_en like  \"%\"#{bean.arrivalStation}\"%\" )",
            "</when> order by a.order_id desc",
    		"</script>"})
	IPage<Order> getPageForSE(Page page,@Param(Constants.WRAPPER) LambdaQueryWrapper<Order> wrapperForSC,@Param("bean") Order bean);
    @Select({"<script>",
    	" select a.* from (select * from sc_order ${ew.customSqlSegment}) a ",
    	" left join sc_port_maintenance b on a.departure_station=b.port_code ",
    	" left join sc_port_maintenance c on a.arrival_station=c.port_code ",
    	" where 1=1 ",
    	"<when test='bean.departureStation!=null and bean.departureStation!=\"\"'>",
    	" AND (b.port_code like  \"%\"#{bean.departureStation}\"%\" or b.port_name_en like  \"%\"#{bean.departureStation}\"%\" )",
    	"</when>",
    	"<when test='bean.arrivalStation!=null and bean.arrivalStation!=\"\"'>",
    	" AND (c.port_code like  \"%\"#{bean.arrivalStation}\"%\" or c.port_name_en like  \"%\"#{bean.arrivalStation}\"%\" )",
    	"</when> order by a.order_id desc",
    "</script>"})
    List<Order> getPageForSE2(@Param(Constants.WRAPPER) LambdaQueryWrapper<Order> wrapperForSC,@Param("bean") Order bean);

	@Select({"<script>",
			"CALL sc_P_hbl_print(#{org_id},#{orderId},#{businessScope})\n",
			"</script>"})
	SeHawMakeProcedure printHawMake(@Param("org_id") Integer org_id, @Param("orderId") Integer orderId , @Param("businessScope") String businessScope);
	@Select({"<script>",
		"CALL sc_P_letter_print(#{org_id},#{orderId},#{businessScope})\n",
		"</script>"})
    SeOrderLetterPrint printOrderLetter(@Param("org_id") Integer org_id, @Param("orderId") Integer orderId , @Param("businessScope") String businessScope);

	@Select({"<script>",
			"CALL sc_P_letter_trailer_print(#{org_id},#{orderId},#{businessScope})\n",
			"</script>"})
	SeTrailerPrint exportTrailerPrintExcel(@Param("org_id") Integer org_id, @Param("orderId") Integer orderId , @Param("businessScope") String businessScope);

	@Select({"<script>",
			"CALL sc_P_notice_arrival(#{org_id},#{orderId},#{businessScope})\n",
			"</script>"})
	SeTrailerPrint exportNoticeArrivalExcel(@Param("org_id") Integer org_id, @Param("orderId") Integer orderId , @Param("businessScope") String businessScope);

	@Select("select workgroup_id from hrs_user_workgroup_detail where user_id = #{currentUserId}")
	List<Integer> getWorkgroupIds(@Param("currentUserId") Integer currentUserId);
}
