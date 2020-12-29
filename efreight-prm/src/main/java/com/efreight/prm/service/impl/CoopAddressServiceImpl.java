package com.efreight.prm.service.impl;

import com.efreight.prm.dao.CoopAddressDao;
import com.efreight.prm.dao.CoopAgreementMapper;
import com.efreight.prm.entity.CoopAddressBean;
import com.efreight.prm.entity.CoopAgreementBean;
import com.efreight.prm.service.CoopAddressService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CoopAddressServiceImpl  implements CoopAddressService {

	@Autowired(required = false)
	private CoopAddressDao coopAddressDao;
	@Autowired(required = false)
	private CoopAgreementMapper coopAgreementDao;
	
	@Override
	public Map<String,Object> queryCoopTabsList(Integer currentPage, Integer pageSize,Map paramMap, String tabType){
		Map<String,Object> rerultMap=new HashMap<String,Object>();
		//设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
    	if(currentPage==null||currentPage==0)
    		currentPage = 1;
    	if(pageSize==null||pageSize==0)
    		pageSize = 10;
		Page Page=PageHelper.startPage(currentPage, pageSize,true);
//    	List<CoopAddressBean> persons = coopAddressDao.queryCoopAddressList(paramMap);
		if("agreement".equals(tabType)){
			List<CoopAgreementBean> map = coopAgreementDao.queryAgreementsByCoopId(paramMap);
			rerultMap.put("dataList", map);
		}else if("address".equals(tabType)){
			List<CoopAddressBean> map = coopAddressDao.queryCoopAddressList(paramMap);
			rerultMap.put("dataList", map);
		}
        long countNums=Page.getTotal();//总记录数
        rerultMap.put("totalNum", countNums);
//        rerultMap.put("dataList", persons);
        return rerultMap;
	}
	
	@Override
	public Integer saveCoopAddress(CoopAddressBean coopAddressBean){
    	Integer CoopAddress_id = coopAddressDao.saveCoopAddress(coopAddressBean);
       
        return CoopAddress_id;
	}
	
	@Override
	public Integer modifyCoopAddress(CoopAddressBean coopAddressBean){
    	Integer CoopAddress_id = coopAddressDao.modifyCoopAddress(coopAddressBean);
       
        return CoopAddress_id;
	}
	
	@Override
	public CoopAddressBean viewCoopAddress(Map paramMap){
		CoopAddressBean reCoopAddress = coopAddressDao.viewCoopAddress(paramMap);
       
        return reCoopAddress;
	}
}
