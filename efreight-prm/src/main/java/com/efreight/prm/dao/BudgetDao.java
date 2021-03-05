package com.efreight.prm.dao;

import com.efreight.prm.entity.budget.BudgetListBean;
import com.efreight.prm.entity.budget.BudgetQuery;
import com.efreight.prm.entity.budget.BudgetServiceBean;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author lc
 * @date 2021/2/25 13:02
 */
public interface BudgetDao {

    /**
     * 预算分析
     * @param query
     * @return
     */
    @Select("CALL prm_P_budget_EF(#{query.serviceId}, #{query.zoneCode}, #{query.startDate}, #{query.endDate}, #{query.saleIdsStr})")
    List<BudgetListBean> budgetList(@Param("query") BudgetQuery query);

    /**
     * 预算服务数据
     * @return
     */
    @Select("SELECT service_id AS serviceId,service_name serviceName FROM prm_coop_agreement_settlement_service\n" +
            "WHERE is_valid = 1\n" +
            "AND LENGTH(service_code)=3\n" +
            "ORDER BY service_code\n")
    List<BudgetServiceBean> budgetService();
}
