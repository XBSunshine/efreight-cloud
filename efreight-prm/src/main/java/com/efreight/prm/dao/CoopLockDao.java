package com.efreight.prm.dao;

import java.util.List;
import java.util.Map;

import com.efreight.prm.entity.CoopLockBean;
import com.efreight.prm.entity.CoopLockBean;



public interface CoopLockDao  {

	//查询列表
	List<CoopLockBean> queryCoopLockList(Map<String, Object> paramMap);

	//插入
	Integer saveCoopLock(CoopLockBean bean);
	
	//修改
	Integer modifyCoopLock(CoopLockBean bean);
	//查看
	CoopLockBean viewCoopLock(Map paramMap);
}
