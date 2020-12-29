package com.efreight.sc.service;

import com.efreight.common.remoteVo.IncomeCostList;
import com.efreight.sc.entity.LcCost;
import com.efreight.sc.entity.LcIncome;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * LC 费用录入 应收 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
public interface LcIncomeService extends IService<LcIncome> {

    List<LcIncome> getList(Integer orderId);

    void modify(LcIncome lcIncome);

    void insert(LcIncome lcIncome);

    void delete(Integer incomeId);

    LcIncome view(Integer incomeId);

    void saveOrderIncomeAndCost(IncomeCostList<LcIncome, LcCost> incomeCostList);
}
