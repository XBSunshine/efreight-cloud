package com.efreight.prm.service.impl;

import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.prm.dao.CoopAgreementMapper;
import com.efreight.prm.entity.*;
import com.efreight.prm.service.CoopAgreementService;
import com.efreight.prm.service.LogService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class CoopAgreementServiceImpl implements CoopAgreementService {
	private final CoopAgreementMapper dao;
	private final LogService logService;
	@Override
	public Map<String,Object> queryList(Integer currentPage, Integer pageSize,Map<String, Object> paramMap){
		//设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
    	if(currentPage==null||currentPage==0)
    		currentPage = 1;
    	if(pageSize==null||pageSize==0)
    		pageSize = 10;
		Page Page=PageHelper.startPage(currentPage, pageSize,true);
    	List<CoopAgreementBean> persons = dao.queryList(paramMap);
        long countNums=Page.getTotal();//总记录数
        Map<String,Object> rerultMap=new HashMap<String,Object>();
        rerultMap.put("totalNum", countNums);
        rerultMap.put("dataList", persons);
        return rerultMap;
	}

	@Override
	public Map<String,Object> queryList1(Integer currentPage, Integer pageSize,Map<String, Object> paramMap){
		//设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
		if(currentPage==null||currentPage==0)
			currentPage = 1;
		if(pageSize==null||pageSize==0)
			pageSize = 10;
		Page Page=PageHelper.startPage(currentPage, pageSize,true);
		List<CoopAgreementBean> persons = dao.queryList1(paramMap);
		long countNums=Page.getTotal();//总记录数
		Map<String,Object> rerultMap=new HashMap<String,Object>();
		rerultMap.put("totalNum", countNums);
		rerultMap.put("dataList", persons);
		return rerultMap;
	}

	@Override
	public void saveCoopAgreement(CoopAgreementBean agreement) {
		EUserDetails user = SecurityUtils.getUser();
		//保存日志
		LogBean logBean = new LogBean();
		if(agreement.getAgreement_id() == null){
			agreement.setCreator_id(user.getId());
			agreement.setCreate_time(new Date());
			agreement.setOrg_id(user.getOrgId());
			agreement.setDept_id(user.getDeptId());
			agreement.setAgreement_status(1);
			dao.doSave(agreement);
			logBean.setOp_level("高");
			logBean.setOp_type("新建合同");
			logBean.setOp_name("客商资料协议-合同");
			logBean.setOp_info("新建客商资料协议合同："+agreement.getSerial_number()+" 客商资料："+agreement.getCoop_name());
			logService.doSave(logBean);
		}else{
			agreement.setEditor_id(user.getId());
			agreement.setEdit_time(new Date());
			dao.doEdit(agreement);

			logBean.setOp_level("高");
			logBean.setOp_type("修改合同");
			logBean.setOp_name("客商资料协议-修改合同");
			logBean.setOp_info("客商资料协议-修改合同："+agreement.getSerial_number()+" 客商资料："+agreement.getCoop_name());
			logService.doSave(logBean);

		}

	}

	@Override
	public void stopAgreement(CoopAgreementBean agreement) {
		agreement.setEditor_id(SecurityUtils.getUser().getId());
		agreement.setEdit_time(new Date());
		agreement.setAgreement_status(0);
		dao.doEdit(agreement);

		//保存日志
		LogBean logBean = new LogBean();
		logBean.setOp_level("高");
		logBean.setOp_type("废止合同");
		logBean.setOp_name("客商资料协议-废止合同");
		logBean.setOp_info("客商资料协议-废止合同："+agreement.getSerial_number()+" 客商资料："+agreement.getCoop_name());
		logService.doSave(logBean);
	}

	@Override
	public List<CoopAgreementBean> selectBySerialNumber(Integer agreement_id, String serial_number) {
		return dao.queryAgreementsBySerialNumber(agreement_id,serial_number);
	}

	@Override
	public List<CoopAgreementExcelBean> queryListForExcle(Map<String, Object> paramMap){
		
		return dao.queryListForExcle(paramMap);
	}
	@Override
	public Map<String,Object> querySigningList(Integer currentPage, Integer pageSize,Map<String, Object> paramMap){
		//设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
		if(currentPage==null||currentPage==0)
			currentPage = 1;
		if(pageSize==null||pageSize==0)
			pageSize = 10;
		Page Page=PageHelper.startPage(currentPage, pageSize,true);
		List<CoopAgreementSigningBean> persons = dao.querySigningList(paramMap);
		long countNums=Page.getTotal();//总记录数
		Map<String,Object> rerultMap=new HashMap<String,Object>();
		rerultMap.put("totalNum", countNums);
		rerultMap.put("dataList", persons);
		return rerultMap;
	}
	public Map<String,Object> queryListForChoose(Map<String, Object> paramMap){
		List<Map<String,Object>> list = dao.queryListForChoose(paramMap);
		Map<String,Object> rerultMap=new HashMap<String,Object>();
		rerultMap.put("totalNum", 0);
		rerultMap.put("dataList", list);
		return rerultMap;
	}
	public Map<String,Object> selectUser(Map<String, Object> paramMap){
		List<Map<String,Object>> list = dao.selectUser(paramMap);
		Map<String,Object> rerultMap=new HashMap<String,Object>();
		rerultMap.put("totalNum", 0);
		rerultMap.put("dataList", list);
		return rerultMap;
	}
	public Map<String,Object> selectDept(Map<String, Object> paramMap){
		List<Map<String,Object>> list = dao.selectDept(paramMap);
		Map<String,Object> rerultMap=new HashMap<String,Object>();
		rerultMap.put("totalNum", 0);
		rerultMap.put("dataList", list);
		return rerultMap;
	}
	public int doSave(CoopAgreementBean bean) {
		dao.doSave(bean);	
		return bean.getAgreement_id();
	}
	public void doEdit(CoopAgreementBean bean) {
		dao.doEdit(bean);	
	}
	public void doRenew(CoopAgreementBean bean) {
		dao.doRenew(bean);	
	}
	public void doExtension(CoopAgreementBean bean) {
		dao.doExtension(bean);	
	}
	public void doStop(CoopAgreementBean bean) {
		dao.doStop(bean);	
	}
	public void signingDoSave(CoopAgreementSigningBean bean) {
		dao.signingDoSave(bean);		
	}
	public CoopAgreementBean queryByID(Integer id) {
		return dao.queryByID(id);
	}
}
