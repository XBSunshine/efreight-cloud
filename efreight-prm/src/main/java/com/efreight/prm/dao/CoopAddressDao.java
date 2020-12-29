package com.efreight.prm.dao;

import java.util.List;
import java.util.Map;

import com.efreight.prm.entity.CoopAddressBean;



public interface CoopAddressDao  {

	//查询列表
		List<CoopAddressBean> queryCoopAddressList(Map<String, Object> paramMap);
		//插入
		Integer saveCoopAddress(CoopAddressBean coopAddressBean);
		
		//修改
		Integer modifyCoopAddress(CoopAddressBean coopAddressBean);
		//查看
		CoopAddressBean viewCoopAddress(Map paramMap);
}
