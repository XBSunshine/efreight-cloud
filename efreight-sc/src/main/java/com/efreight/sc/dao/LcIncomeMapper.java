package com.efreight.sc.dao;

import com.efreight.sc.entity.LcIncome;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * LC 费用录入 应收 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
public interface LcIncomeMapper extends BaseMapper<LcIncome> {

    @Select("select * from css_debit_note where org_id = #{orgId} and order_id = #{orderId} and business_scope='LC'")
    List<Map<String, Object>> getOrderBill(@Param("orgId") Integer orgId, @Param("orderId") Integer orderId);

}
