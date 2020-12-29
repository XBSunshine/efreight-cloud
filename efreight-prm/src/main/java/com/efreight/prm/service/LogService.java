package com.efreight.prm.service;

import java.util.Map;

import com.efreight.prm.entity.LogBean;


public interface LogService {

	/**
	  * 根据条件查询
	  * @param Map
	  * @param pageBounds
	  * @return List
	  */
	Map<String,Object> queryList(Integer currentPage, Integer pageSize,Map<String, Object> paramMap);
	
	public void doSave(LogBean bean);
}
