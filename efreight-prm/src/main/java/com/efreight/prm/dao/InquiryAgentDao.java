package com.efreight.prm.dao;

import com.efreight.prm.entity.InquiryAgent;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * PRM 询盘代理 Mapper 接口
 * </p>
 *
 * @author qipm
 * @since 2020-05-18
 */
public interface InquiryAgentDao {

	List<Map<String,Object>> queryContactsList(Map<String, Object> paramMap);
	List<InquiryAgent> queryList(Map<String, Object> paramMap);
	List<InquiryAgent> getInquiryAgentList(Map<String, Object> paramMap);
	int doSave(InquiryAgent bean);
	InquiryAgent queryById(Map<String, Object> paramMap);
	void doEdit(InquiryAgent bean);
	
	
	List<Map<String,Object>> selectCarrierCode(Map<String, Object> paramMap);
	List<Map<String,Object>> selectAirport(Map<String, Object> paramMap);
	List<Map<String,Object>> selectNation(Map<String, Object> paramMap);
	List<Map<String,Object>> selectContacts(Map<String, Object> paramMap);

    List<InquiryAgent> exportExcel(InquiryAgent bean);

    Integer countInquiryOrderAmount(String inquiryId);
}
