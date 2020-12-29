package com.efreight.sc.dao;

import com.efreight.sc.entity.IoCost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * IO 费用录入 成本 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
public interface IoCostMapper extends BaseMapper<IoCost> {

    @Select("select * from css_payment_detail a inner join css_payment b on a.payment_id=b.payment_id and b.business_scope='IO' where a.org_id=#{orgId} and a.cost_id=#{costId}")
    List<Map<String, Object>> getPaymentDetailByCostId(@Param("costId") Integer costId, @Param("orgId") Integer orgId);
}
