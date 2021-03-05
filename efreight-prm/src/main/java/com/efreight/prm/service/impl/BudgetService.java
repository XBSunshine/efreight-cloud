package com.efreight.prm.service.impl;

import com.efreight.prm.entity.budget.BudgetListBean;
import com.efreight.prm.entity.budget.BudgetQuery;
import com.efreight.prm.entity.budget.BudgetServiceBean;

import java.util.List;

/**
 * @author lc
 * @date 2021/2/25 13:01
 */
public interface BudgetService  {

    /**
     * 预算分析列表数据
     * @param budgetQuery
     * @return
     */
    List<BudgetListBean> queryList(BudgetQuery budgetQuery);

    /**
     * 预算服务数据
     * @return
     */
    List<BudgetServiceBean> queryService();
}
