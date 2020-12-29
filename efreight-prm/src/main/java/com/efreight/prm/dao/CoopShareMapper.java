package com.efreight.prm.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.efreight.prm.entity.CoopShare;
import com.efreight.prm.entity.CoopShareEmail;
import com.efreight.prm.entity.PrmCoopShareFields;
import com.efreight.prm.entity.ShareFields;


public interface CoopShareMapper {

	List<CoopShare> selectList(CoopShare coopShare);
	
	void modifyShare(CoopShare coopShare);
	CoopShare getCoopShareInfo(CoopShare coopShare);
	void updateOperCoop(CoopShareEmail coopShareEmail);
	void updateShareCoop(CoopShareEmail coopShareEmail);
	List<ShareFields> getSharefields(HashMap map);
	List<PrmCoopShareFields> getPrmCoopShareFields(HashMap map);
	void deleteShareFields(HashMap map);
	void saveShareFields(HashMap map);
}
