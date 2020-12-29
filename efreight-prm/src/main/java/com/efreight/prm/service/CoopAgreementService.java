package com.efreight.prm.service;

import java.util.List;
import java.util.Map;

import com.efreight.prm.entity.CoopAgreementBean;
import com.efreight.prm.entity.CoopAgreementExcelBean;
import com.efreight.prm.entity.CoopAgreementSigningBean;

public interface CoopAgreementService{

	Map<String,Object> queryList(Integer currentPage, Integer pageSize,Map<String, Object> paramMap);
	List<CoopAgreementExcelBean> queryListForExcle(Map<String, Object> paramMap);
	Map<String,Object> querySigningList(Integer currentPage, Integer pageSize,Map<String, Object> paramMap);
	Map<String,Object> queryListForChoose(Map<String, Object> paramMap);
	Map<String,Object> selectUser(Map<String, Object> paramMap);
	Map<String,Object> selectDept(Map<String, Object> paramMap);
	
	public int doSave(CoopAgreementBean bean);
	public void doEdit(CoopAgreementBean bean);
	public void doRenew(CoopAgreementBean bean);
	public void doExtension(CoopAgreementBean bean);
	public void doStop(CoopAgreementBean bean);
	public void signingDoSave(CoopAgreementSigningBean bean);
	
	public CoopAgreementBean queryByID(Integer id);

	Map<String,Object> queryList1(Integer currentPage, Integer pageSize,Map<String, Object> paramMap);

    void saveCoopAgreement(CoopAgreementBean agreement);

	void stopAgreement(CoopAgreementBean agreement);

	List<CoopAgreementBean> selectBySerialNumber(Integer agreement_id, String serial_number);
}
