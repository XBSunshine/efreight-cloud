package com.efreight.afbase.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.TcCost;

/**
 * <p>
 * TC 费用录入 成本 服务类
 * </p>
 *
 * @author caiwd
 * @since 2020-07-15
 */
public interface TcCostService extends IService<TcCost> {
	
	List<TcCost> etCostList(TcCost tcCost);

}
