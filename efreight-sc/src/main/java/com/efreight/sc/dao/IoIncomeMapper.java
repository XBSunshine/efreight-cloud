package com.efreight.sc.dao;

import com.efreight.sc.entity.IoIncome;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * IO 费用录入 应收 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
public interface IoIncomeMapper extends BaseMapper<IoIncome> {

    @Select("select * from css_debit_note where org_id = #{orgId} and order_id = #{orderId} and business_scope='IO'")
    List<Map<String, Object>> getOrderBill(@Param("orgId") Integer orgId, @Param("orderId") Integer orderId);

}
