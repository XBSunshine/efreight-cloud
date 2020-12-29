package com.efreight.prm.dao;

import com.efreight.prm.entity.CoopAgreementBean;
import com.efreight.prm.entity.CoopAgreementExcelBean;
import com.efreight.prm.entity.CoopAgreementSigningBean;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CoopAgreementMapper {
	//查询列表
	List<CoopAgreementBean> queryList(Map<String, Object> paramMap);
	List<CoopAgreementExcelBean> queryListForExcle(Map<String, Object> paramMap);
	List<CoopAgreementSigningBean> querySigningList(Map<String, Object> paramMap);
	List<Map<String,Object>> queryListForChoose(Map<String, Object> paramMap);
	List<Map<String,Object>> selectUser(Map<String, Object> paramMap);
	List<Map<String,Object>> selectDept(Map<String, Object> paramMap);
	
	int doSave(CoopAgreementBean bean);
	void doEdit(CoopAgreementBean bean);
	void doRenew(CoopAgreementBean bean);
	void doExtension(CoopAgreementBean bean);
	void doStop(CoopAgreementBean bean);
	void signingDoSave(CoopAgreementSigningBean bean);
	
	public CoopAgreementBean queryByID(Integer id);

	List<CoopAgreementBean> queryList1(Map<String, Object> paramMap);
	List<CoopAgreementBean> queryAgreementsByCoopId(Map paramMap);

	List<CoopAgreementBean> queryAgreementsBySerialNumber(@Param("agreement_id") Integer agreement_id,@Param("serial_number") String serial_number);
}
