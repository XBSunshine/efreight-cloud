package com.efreight.sc.service;

import com.efreight.common.remoteVo.IncomeCostList;
import com.efreight.sc.entity.IoCost;
import com.efreight.sc.entity.IoIncome;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * IO 费用录入 应收 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
public interface IoIncomeService extends IService<IoIncome> {

    List<IoIncome> getList(Integer orderId);

    void modify(IoIncome ioIncome);

    void insert(IoIncome ioIncome);

    void delete(Integer incomeId);

    IoIncome view(Integer incomeId);

    void saveOrderIncomeAndCost(IncomeCostList<IoIncome, IoCost> incomeCostList);
}
