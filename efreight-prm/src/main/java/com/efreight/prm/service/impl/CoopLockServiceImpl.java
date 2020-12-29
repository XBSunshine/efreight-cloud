package com.efreight.prm.service.impl;

import com.efreight.prm.dao.CoopLockDao;
import com.efreight.prm.entity.CoopLockBean;
import com.efreight.prm.service.CoopLockService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CoopLockServiceImpl  implements CoopLockService {

	@Autowired
	private CoopLockDao coopLockDao;
	
	@Override
	public Map<String,Object> queryCoopLockList(Integer currentPage, Integer pageSize,Map paramMap){
		//设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
    	if(currentPage==null||currentPage==0)
    		currentPage = 1;
    	if(pageSize==null||pageSize==0)
    		pageSize = 10;
		Page Page=PageHelper.startPage(currentPage, pageSize,true);
    	List<CoopLockBean> persons = coopLockDao.queryCoopLockList(paramMap);
        long countNums=Page.getTotal();//总记录数
        Map<String,Object> rerultMap=new HashMap<String,Object>();
        rerultMap.put("totalNum", countNums);
        rerultMap.put("dataList", persons);
        return rerultMap;
	}
	
	@Override
	public Integer saveCoopLock(CoopLockBean CoopLockBean){
    	Integer CoopLock_id = coopLockDao.saveCoopLock(CoopLockBean);
       
        return CoopLock_id;
	}
	
	@Override
	public Integer modifyCoopLock(CoopLockBean CoopLockBean){
    	Integer CoopLock_id = coopLockDao.modifyCoopLock(CoopLockBean);
       
        return CoopLock_id;
	}
	
	@Override
	public CoopLockBean viewCoopLock(Map paramMap){
		CoopLockBean reCoopLock = coopLockDao.viewCoopLock(paramMap);
       
        return reCoopLock;
	}
}
