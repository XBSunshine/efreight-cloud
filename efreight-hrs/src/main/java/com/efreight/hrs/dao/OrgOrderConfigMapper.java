package com.efreight.hrs.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.hrs.entity.OrgOrderConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author lc
 * @date 2020/7/28 16:29
 */
public interface OrgOrderConfigMapper extends BaseMapper<OrgOrderConfig> {

    /**
     * 根据企业ID和业务范畴来修改数据，且字段可以被设置为null值
     * @param orgOrderConfig 数据体
     * @return 影响行数
     */
    @Update("update hrs_org_order_config " +
            "set finance_lock_view = #{param.financeLockView}, " +
            "business_product = #{param.businessProduct}, " +
            "goods_type = #{param.goodsType}, " +
            "battery_type = #{param.batteryType}, " +
            "customs_status_code = #{param.customsStatusCode}, " +
            "container_method = #{param.containerMethod}, " +
            "cargo_flow_type = #{param.cargoFlowType}, " +
            "shipping_method = #{param.shippingMethod}, " +
            "billing_type = #{param.billingType}, " +
            "iata_code = #{param.iataCode}, " +
            "rounting_sign = #{param.rountingSign}, " +
            "rounting_sign_business_product = #{param.rountingSignBusinessProduct}, " +
            "cata_certified_sales_agents = #{param.cataCertifiedSalesAgents}, " +
            "mft2201_save = #{param.mft2201Save} " +
            "where org_id = #{param.orgId} and business_scope= #{param.businessScope}"
    )
    int updateWithOrgIdAndBusinessScope(@Param("param") OrgOrderConfig orgOrderConfig);


}
