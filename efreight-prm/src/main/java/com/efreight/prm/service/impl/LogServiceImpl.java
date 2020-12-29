package com.efreight.prm.service.impl;

import com.efreight.common.security.util.SecurityUtils;
import com.efreight.prm.dao.LogDao;
import com.efreight.prm.entity.LogBean;
import com.efreight.prm.service.LogService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class LogServiceImpl  implements LogService {

	@Autowired
	private LogDao dao;
	
	@Override
	public Map<String,Object> queryList(Integer currentPage, Integer pageSize,Map<String, Object> paramMap){
		//设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
    	if(currentPage==null||currentPage==0)
    		currentPage = 1;
    	if(pageSize==null||pageSize==0)
    		pageSize = 10;
		Page Page=PageHelper.startPage(currentPage, pageSize,true);
    	List<LogBean> persons = dao.queryList(paramMap);
        long countNums=Page.getTotal();//总记录数
        Map<String,Object> rerultMap=new HashMap<String,Object>();
        rerultMap.put("totalNum", countNums);
        rerultMap.put("dataList", persons);
        return rerultMap;
	}
	@Transactional
	public void doSave(LogBean bean) {
		try {
			bean.setCreator_id(SecurityUtils.getUser().getId());
			bean.setCreate_time(new Date());
			bean.setOrg_id(SecurityUtils.getUser().getOrgId());
			bean.setDept_id(SecurityUtils.getUser().getDeptId());
			dao.doSave(bean);		
		} catch (Exception e) {
			
		}
	}
}
