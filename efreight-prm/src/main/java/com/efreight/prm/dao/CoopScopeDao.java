package com.efreight.prm.dao;

import java.util.List;
import java.util.Map;

import com.efreight.prm.entity.CoopScopeBean;



public interface CoopScopeDao  {

	//查询列表
	List<CoopScopeBean> queryCoopScopeList(Map<String, Object> paramMap);
	//插入
	Integer saveCoopScope(CoopScopeBean bean);
	
	//修改
	Integer modifyCoopScope(CoopScopeBean bean);
	//查看
	CoopScopeBean viewCoopScope(Map paramMap);
	
}
