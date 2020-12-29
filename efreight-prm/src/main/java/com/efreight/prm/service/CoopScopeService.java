package com.efreight.prm.service;

import java.util.Map;

import com.efreight.prm.entity.CoopScopeBean;


public interface CoopScopeService {

	/**
	  * 根据条件查询
	  * @param Map
	  * @param pageBounds
	  * @return List
	  */
	Map<String,Object> queryCoopScopeList(Integer currentPage, Integer pageSize,Map paramMap);
	

	/**
	  * 插入新数据
	  * @param Map
	  */
	Integer saveCoopScope(CoopScopeBean CoopScope);
	
	/**
	  * 修改数据
	  * @param Map
	  */
	Integer modifyCoopScope(CoopScopeBean CoopScope);
	
	/**
	  * 查看单个数据
	  * @param Map
	  */
	CoopScopeBean viewCoopScope(Map paramMap);

}
