package com.efreight.prm.service;

import java.util.Map;

import com.efreight.prm.entity.CoopLockBean;


public interface CoopLockService {

	/**
	  * 根据条件查询
	  * @param Map
	  * @param pageBounds
	  * @return List
	  */
	Map<String,Object> queryCoopLockList(Integer currentPage, Integer pageSize,Map paramMap);
	

	/**
	  * 插入新数据
	  * @param Map
	  */
	Integer saveCoopLock(CoopLockBean CoopLock);
	
	/**
	  * 修改数据
	  * @param Map
	  */
	Integer modifyCoopLock(CoopLockBean CoopLock);
	
	/**
	  * 查看单个数据
	  * @param Map
	  */
	CoopLockBean viewCoopLock(Map paramMap);

}
