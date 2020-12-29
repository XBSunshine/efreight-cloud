package com.efreight.prm.service;

import java.util.List;
import java.util.Map;

import com.efreight.prm.entity.CoopContactsBean;


public interface CoopContactsService {

	/**
	  * 根据条件查询
	  * @param Map
	  * @param pageBounds
	  * @return List
	  */
	Map<String,Object> queryCoopContactsList(Integer currentPage, Integer pageSize,Map paramMap);
	

	/**
	  * 插入新数据
	  * @param Map
	  */
	void saveCoopContacts(CoopContactsBean CoopContacts);

	/**
	 * 插入新数据
	 * @param Map
	 */
	Integer saveCoopContacts1(CoopContactsBean CoopContacts);
	
	/**
	  * 修改数据
	  * @param Map
	  */
	Integer modifyCoopContacts(CoopContactsBean CoopContacts);
	
	/**
	  * 查看单个数据
	  * @param Map
	  */
	CoopContactsBean viewCoopContacts(Map paramMap);

    List<CoopContactsBean> queryContactsIsValidByCoopId(String coopId);

	List<CoopContactsBean> queryContactsIsValidByCoopId1(String coopId,Integer contactsId);
}
