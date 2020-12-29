package com.efreight.prm.service;

import java.util.List;
import java.util.Map;

import com.efreight.prm.entity.CoopShare;
import com.efreight.prm.entity.CoopShareEmail;
import com.efreight.prm.entity.PrmCoopShareFields;
import com.efreight.prm.entity.ShareFields;


public interface CoopShareService {
	Map<String, Object> getPage(Integer current, Integer size, CoopShare coopShare);
	void modifyShare(Integer coopId,String shareType);
	void coopShareWithEmail(CoopShareEmail coopShareEmail)throws Exception;
	void coopShareBind(CoopShareEmail coopShareEmail)throws Exception;
	CoopShare getCoopShareInfo(Integer orgId,Integer coopId);
	List<ShareFields> sharefieldsInfo(String businessScope,Integer coopId);
	void saveShareFields(PrmCoopShareFields prmCoopShareFields);
	
	CoopShare checkBindNew(Integer orgId,Integer coopOrgId);

}
