package com.efreight.afbase.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.LcCost;

/**
 * <p>
 * LC 费用录入 成本 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
public interface LcCostService extends IService<LcCost> {
	
	List<LcCost> getCostList(LcCost lcCost);

}
