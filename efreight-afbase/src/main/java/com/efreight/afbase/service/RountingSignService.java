package com.efreight.afbase.service;

import com.efreight.afbase.entity.RountingSign;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * AF 订单管理 出口订单 签单表 服务类
 * </p>
 *
 * @author cwd
 * @since 2020-11-18
 */
public interface RountingSignService extends IService<RountingSign> {
	
	Map checkOrderCost(RountingSign bean);
	void saveOrModify(RountingSign bean);
	
	RountingSign getRountingSign(RountingSign bean);
	Map concelSign(RountingSign bean);
	
	Map checkCostRecord(RountingSign bean);

}
