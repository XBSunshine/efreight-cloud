package com.efreight.prm.dao;

import com.efreight.prm.entity.LogBean;

import java.util.List;
import java.util.Map;



public interface LogDao  {

	//查询列表
	List<LogBean> queryList(Map<String, Object> paramMap);

	void doSave(LogBean bean);
	
}
