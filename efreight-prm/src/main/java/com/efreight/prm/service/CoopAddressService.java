package com.efreight.prm.service;

import java.util.Map;

import com.efreight.prm.entity.CoopAddressBean;


public interface CoopAddressService {

	/**
	 *
	 * @param currentPage
	 * @param pageSize
	 * @param paramMap
	 * @return
	 */
	Map<String,Object> queryCoopTabsList(Integer currentPage, Integer pageSize,Map paramMap,String tabType);
	

	/**
	  * 插入新数据
	  * @param CoopAddress
	  */
	Integer saveCoopAddress(CoopAddressBean CoopAddress);
	
	/**
	  * 修改数据
	  * @param CoopAddress
	  */
	Integer modifyCoopAddress(CoopAddressBean CoopAddress);
	
	/**
	  * 查看单个数据
	  * @param paramMap
	  */
	CoopAddressBean viewCoopAddress(Map paramMap);
}
