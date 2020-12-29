package com.efreight.sc.dao;

import com.efreight.sc.entity.TcOrder;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * TC 订单管理 TE、TI 订单 Mapper 接口
 * </p>
 *
 * @author caiwd
 * @since 2020-07-13
 */
public interface TcOrderMapper extends BaseMapper<TcOrder> {
	@Select("select UUID()")
    String getUuid();
	@Insert("insert into hrs_log (op_level,op_type,op_name,op_info,creator_id,create_time,org_id,dept_id) "
	    		+ " VALUES ('高','强制关闭',#{op_name},#{op_info},#{creator_id},#{create_time},#{org_id},#{dept_id})")
    void insertHrsLog(@Param("op_name") String op_name,@Param("op_info") String op_info,
	    		@Param("creator_id") Integer creator_id,@Param("create_time") LocalDateTime create_time,
	    		@Param("org_id") Integer org_id,@Param("dept_id") Integer dept_id);
	@Select("select p.payment_id from css_payment p inner join css_payment_detail d on p.payment_id=d.payment_id and p.business_scope=#{businessScope} and p.org_id=#{orgId} inner join tc_cost c on d.org_id=#{orgId} and c.cost_id=d.cost_id and c.order_id=#{orderId}")
    List<Integer> queryPaymentForIfForceStop(@Param("orderId") Integer orderId, @Param("orgId")Integer orgId,@Param("businessScope") String businessScope);

	@Select("select workgroup_id from hrs_user_workgroup_detail where user_id = #{currentUserId}")
	List<Integer> getWorkgroupIds(@Param("currentUserId") Integer currentUserId);

}
