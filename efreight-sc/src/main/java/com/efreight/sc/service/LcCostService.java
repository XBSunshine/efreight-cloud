package com.efreight.sc.service;

import com.efreight.sc.entity.LcCost;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * LC 费用录入 成本 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
public interface LcCostService extends IService<LcCost> {

    List<LcCost> getList(Integer orderId);

    void modify(LcCost lcCost);

    void insert(LcCost lcCost);

    void delete(Integer costId);

    LcCost view(Integer costId);

    String getOrderCostStatusForLC(Integer orderId);
}
