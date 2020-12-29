package com.efreight.afbase.service;

import com.efreight.afbase.entity.ScCost;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * CS 延伸服务 成本 服务类
 * </p>
 *
 * @author qipm
 * @since 2020-03-06
 */
public interface ScCostService extends IService<ScCost> {
	List<ScCost> getCostList(ScCost scCost);

}
