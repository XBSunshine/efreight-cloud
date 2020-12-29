package com.efreight.afbase.service;

import java.util.List;

import com.efreight.afbase.entity.AfCost;
import com.efreight.afbase.entity.AfIncome;
import com.efreight.afbase.entity.AfIncomeCost;
import com.efreight.afbase.entity.AfIncomeCostTree;
import com.efreight.afbase.entity.IncomeCostList;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * AF 延伸服务 应收 服务类
 * </p>
 *
 * @author qipm
 * @since 2019-10-12
 */
public interface AfIncomeCostService extends IService<AfIncomeCost> {

	List<AfIncomeCostTree> getListTree(Integer order_id,String businessScope);
	
	IncomeCostList getIncomeCostList(AfIncome bean);
	
	Boolean doEdit(IncomeCostList bean);
	List<AfIncome> addIncomeTemplate(AfIncome bean);
	List<AfCost> addCostTemplate(AfIncome bean);
}
