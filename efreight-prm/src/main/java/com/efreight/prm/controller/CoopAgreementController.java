package com.efreight.prm.controller;


import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.efreight.common.core.utils.ExportExcel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.efreight.common.security.util.SecurityUtils;
import com.efreight.prm.entity.CoopAgreementBean;
import com.efreight.prm.entity.CoopAgreementExcelBean;
import com.efreight.prm.entity.CoopAgreementSigningBean;
import com.efreight.prm.entity.LogBean;
import com.efreight.prm.service.CoopAgreementService;
import com.efreight.prm.service.LogService;
import com.efreight.prm.util.MessageInfo;



@RestController
@RequestMapping("/coopagreement")
public class CoopAgreementController {

	@Autowired
	private CoopAgreementService service;
	@Autowired
	private LogService logService;
	
	@RequestMapping(value = "/queryList", method = RequestMethod.POST)
    public MessageInfo queryList(Integer currentPage, Integer pageSize,@ModelAttribute("bean") CoopAgreementBean bean) {
		String message = "success";
		int code =200;
		Map<String,Object> dataMap = new HashMap();
		try{
//			if ( bean.getOrg_id()==null || "".equals( bean.getOrg_id())) {
//				throw new Exception("企业代码不能为空");
//			}
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Map<String, Object> paramMap=new HashMap<String, Object>();
			paramMap.put("coop_name", bean.getCoop_name());
			paramMap.put("business_scope", bean.getBusiness_scope());
			paramMap.put("agreement_type", bean.getAgreement_type());
			if (bean.getBegin_date()!=null) {
				paramMap.put("begin_date", df.format(bean.getBegin_date())+" 00:00:00");
			}
			if (bean.getEnd_date()!=null) {
				paramMap.put("end_date", df.format(bean.getEnd_date())+" 23:59:59");
			}
			paramMap.put("serial_number", bean.getSerial_number());
			paramMap.put("signing_dept_name", bean.getSigning_dept_name());
			paramMap.put("incharge_name", bean.getIncharge_name());
			paramMap.put("agreement_status", bean.getAgreement_status());
			if (bean.getAgreement_status()!=null) {
				paramMap.put("now_date", df.format(new Date())+" 00:00:00");
			}
			paramMap.put("org_id",SecurityUtils.getUser().getOrgId());
			dataMap = service.queryList(currentPage, pageSize,paramMap);
			
		}catch(Exception e){
			message=e.getMessage();
			code = 400;
		}
		
		MessageInfo messageInfo =new MessageInfo(dataMap,message);
		messageInfo.setCode(code);
        //return  JSONObject.toJSONStringWithDateFormat(messageInfo, "yyyy-MM-dd HH:mm:ss", SerializerFeature.WriteMapNullValue);
        return  messageInfo;
    }

	@RequestMapping(value = "/queryList1", method = RequestMethod.POST)
	public MessageInfo queryList1(Integer currentPage, Integer pageSize,@ModelAttribute("bean") CoopAgreementBean bean) {
		String message = "success";
		int code =200;
		Map<String,Object> dataMap = new HashMap();
		try{
			Map<String, Object> paramMap=new HashMap<String, Object>();
			paramMap.put("coop_name", bean.getCoop_name());
			paramMap.put("coop_code", bean.getCoop_code());
			paramMap.put("org_id",SecurityUtils.getUser().getOrgId());
			dataMap = service.queryList1(currentPage, pageSize,paramMap);

		}catch(Exception e){
			message=e.getMessage();
			code = 400;
		}

		MessageInfo messageInfo =new MessageInfo(dataMap,message);
		messageInfo.setCode(code);
		//return  JSONObject.toJSONStringWithDateFormat(messageInfo, "yyyy-MM-dd HH:mm:ss", SerializerFeature.WriteMapNullValue);
		return  messageInfo;
	}

	/*
	 * 导出EXCLE
	 */
	@PreAuthorize("@pms.hasPermission('sys_coop_agreement_export')")
	@RequestMapping(value="/exportExcel", method=RequestMethod.POST)
	public void  exportExcel(HttpServletRequest request,HttpServletResponse response,@ModelAttribute("bean") CoopAgreementBean bean) throws IOException{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Map<String, Object> paramMap=new HashMap<String, Object>();
		paramMap.put("coop_name", bean.getCoop_name());
		paramMap.put("business_scope", bean.getBusiness_scope());
		paramMap.put("agreement_type", bean.getAgreement_type());
		if (bean.getBegin_date()!=null) {
			paramMap.put("begin_date", df.format(bean.getBegin_date())+" 00:00:00");
		}
		if (bean.getEnd_date()!=null) {
			paramMap.put("end_date", df.format(bean.getEnd_date())+" 23:59:59");
		}
		paramMap.put("serial_number", bean.getSerial_number());
		paramMap.put("signing_dept_name", bean.getSigning_dept_name());
		paramMap.put("incharge_name", bean.getIncharge_name());
		paramMap.put("agreement_status", bean.getAgreement_status());
		if (bean.getAgreement_status()!=null) {
			paramMap.put("now_date", df.format(new Date())+" 00:00:00");
		}
		paramMap.put("org_id",SecurityUtils.getUser().getOrgId());
		    
		List<CoopAgreementExcelBean> list = service.queryListForExcle(paramMap);
		
		 LogBean logBean = new LogBean();
	     logBean.setOp_level("低");
	     logBean.setOp_type("导出");
	     logBean.setOp_name("客商资料协议");
	     logBean.setOp_info("导出客商资料协议列表：" + bean.getCoop_name());
	     logService.doSave(logBean);
	        
		//导出日志数据
		  ExportExcel<CoopAgreementExcelBean> ex = new ExportExcel<CoopAgreementExcelBean>();
	        String[] headers =   {"客户名称", "合同类型","合同模板" , "合同编号", "合同开始时间","合同结束时间","经办人","业务范畴","结算周期","签约主体"
	        		,"账期标准","合同账期","合同金额","生效状态"};  
	        ex.exportExcel(response,"导出EXCEL",headers, list,"Export"); 
	}
	

	/*
	 * 签约记录查询
	 */
	@RequestMapping(value = "/querySigningList", method = RequestMethod.POST)
	@PreAuthorize("@pms.hasPermission('sys_coop_agreement_list')")
	public MessageInfo querySigningList(Integer currentPage, Integer pageSize,@ModelAttribute("bean") CoopAgreementSigningBean bean) {
		String message = "success";
		int code =200;
		Map<String,Object> dataMap = new HashMap();
		try{
//			if ( bean.getOrg_id()==null || "".equals( bean.getOrg_id())) {
//				throw new Exception("企业代码不能为空");
//			}
			Map<String, Object> paramMap=new HashMap<String, Object>();
			paramMap.put("agreement_id", bean.getAgreement_id());
//			paramMap.put("coop_id", bean.getCoop_id());
//			paramMap.put("business_scope", bean.getBusiness_scope());
//			paramMap.put("agreement_type", bean.getAgreement_type());
//			paramMap.put("begin_date", bean.getBegin_date());
//			paramMap.put("end_date", bean.getEnd_date());
//			paramMap.put("serial_number", bean.getSerial_number());
//			paramMap.put("signing_dept_id", bean.getSigning_dept_id());
//			paramMap.put("incharge_id", bean.getIncharge_id());
//			paramMap.put("agreement_status", bean.getAgreement_status());
			paramMap.put("org_id",SecurityUtils.getUser().getOrgId());
			dataMap = service.querySigningList(currentPage, pageSize,paramMap);
			
		}catch(Exception e){
			message=e.getMessage();
			code = 400;
		}
		
		MessageInfo messageInfo =new MessageInfo(dataMap,message);
		messageInfo.setCode(code);
		return  messageInfo;
	}
	/*
	 * 选择客商资料
	 */
	@RequestMapping(value = "/queryListForChoose", method = RequestMethod.POST)
	public MessageInfo queryListForChoose() {
		String message = "success";
		int code =200;
		Map<String,Object> dataMap = new HashMap();
		try{
			Map<String, Object> paramMap=new HashMap<String, Object>();
			paramMap.put("org_id",SecurityUtils.getUser().getOrgId());
			dataMap = service.queryListForChoose(paramMap);
		}catch(Exception e){
			message=e.getMessage();
			code = 400;
		}
		MessageInfo messageInfo =new MessageInfo(dataMap,message);
		messageInfo.setCode(code);
		return  messageInfo;
	}
	/*
	 * 选择经办人
	 */
	@RequestMapping(value = "/selectUser", method = RequestMethod.POST)
	public MessageInfo selectUser() {
		String message = "success";
		int code =200;
		Map<String,Object> dataMap = new HashMap();
		try{
			Map<String, Object> paramMap=new HashMap<String, Object>();
			paramMap.put("org_id",SecurityUtils.getUser().getOrgId());
			dataMap = service.selectUser(paramMap);
		}catch(Exception e){
			message=e.getMessage();
			code = 400;
		}
		MessageInfo messageInfo =new MessageInfo(dataMap,message);
		messageInfo.setCode(code);
		return  messageInfo;
	}
	/*
	 * 选择主体
	 */
	@RequestMapping(value = "/selectDept", method = RequestMethod.POST)
	public MessageInfo selectDept() {
		String message = "success";
		int code =200;
		Map<String,Object> dataMap = new HashMap();
		try{
			Map<String, Object> paramMap=new HashMap<String, Object>();
			paramMap.put("org_id",SecurityUtils.getUser().getOrgId());
			dataMap = service.selectDept(paramMap);
		}catch(Exception e){
			message=e.getMessage();
			code = 400;
		}
		MessageInfo messageInfo =new MessageInfo(dataMap,message);
		messageInfo.setCode(code);
		return  messageInfo;
	}
	/*
	 * 增加
	 */
	@PreAuthorize("@pms.hasPermission('sys_coop_agreement_add')")
	@RequestMapping(value = "/doSave", method = RequestMethod.POST)
    public MessageInfo doSave(@ModelAttribute("bean") CoopAgreementBean bean) {
		String message = "success";
		int code =200;
		Map<String,Object> dataMap = new HashMap();
		try{
			bean.setCreator_id(SecurityUtils.getUser().getId());
			bean.setCreate_time(new Date());
			bean.setOrg_id(SecurityUtils.getUser().getOrgId());
			bean.setDept_id(SecurityUtils.getUser().getDeptId());
			bean.setAgreement_status(1);
			int agreement_id=service.doSave(bean);
			
			//保存签约记录
			CoopAgreementSigningBean signingBean=new CoopAgreementSigningBean();
			signingBean.setAgreement_id(agreement_id);
			signingBean.setCoop_id(bean.getCoop_id());
			signingBean.setSigning_type("新签");   
			signingBean.setSerial_number(bean.getSerial_number());  
			signingBean.setPre_serial_number(bean.getSerial_number());  
			signingBean.setBegin_date(bean.getBegin_date());  
			signingBean.setEnd_date(bean.getEnd_date()); 
			signingBean.setPre_begin_date(bean.getBegin_date());
			signingBean.setPre_end_date(bean.getEnd_date());
			signingBean.setIncharge_id(bean.getIncharge_id());
			signingBean.setPre_incharge_id(bean.getIncharge_id());
			signingBean.setPayment_period(bean.getPayment_period());
			signingBean.setPre_payment_period(bean.getPayment_period());
			signingBean.setTotal_amount(bean.getTotal_amount());
			signingBean.setPre_total_amount(bean.getTotal_amount());
			signingBean.setRemark(bean.getAgreement_remark());
			
			signingBean.setCreator_id(SecurityUtils.getUser().getId());
			signingBean.setCreate_time(new Date());
			signingBean.setOrg_id(SecurityUtils.getUser().getOrgId());
			signingBean.setDept_id(SecurityUtils.getUser().getDeptId());
			service.signingDoSave(signingBean);
			//保存日志
			LogBean logBean = new LogBean();
			logBean.setOp_level("高");
			logBean.setOp_type("新建");
			logBean.setOp_name("客商资料协议");
			logBean.setOp_info("新建客商资料协议："+bean.getSerial_number()+" 客商资料："+bean.getCoop_name());
			logService.doSave(logBean);
		}catch(Exception e){
			message=e.getMessage();
			code = 400;
		}
		
		MessageInfo messageInfo =new MessageInfo(dataMap,message);
		messageInfo.setCode(code);
        return messageInfo;
    }
	/*
	 * 修改
	 */
	@PreAuthorize("@pms.hasPermission('sys_coop_agreement_edit')")
	@RequestMapping(value = "/doEdit", method = RequestMethod.POST)
	public MessageInfo doEdit(@ModelAttribute("bean") CoopAgreementBean newbean) {
		String message = "success";
		int code =200;
		Map<String,Object> dataMap = new HashMap();
		try{
			//查询原协议
			CoopAgreementBean bean=service.queryByID(newbean.getAgreement_id());
			newbean.setEditor_id(SecurityUtils.getUser().getId());
			newbean.setEdit_time(new Date());
			newbean.setOrg_id(SecurityUtils.getUser().getOrgId());
			newbean.setDept_id(SecurityUtils.getUser().getDeptId());
			newbean.setAgreement_status(1);
			service.doEdit(newbean);
			
			//保存签约记录
			CoopAgreementSigningBean signingBean=new CoopAgreementSigningBean();
			signingBean.setAgreement_id(bean.getAgreement_id());
			signingBean.setCoop_id(bean.getCoop_id());
			signingBean.setSigning_type("修改");   
			signingBean.setSerial_number(bean.getSerial_number());  
			signingBean.setPre_serial_number(bean.getSerial_number());  
			signingBean.setBegin_date(bean.getBegin_date());  
			signingBean.setEnd_date(bean.getEnd_date()); 
			signingBean.setPre_begin_date(bean.getBegin_date());
			signingBean.setPre_end_date(bean.getEnd_date());
			//经办人
			signingBean.setIncharge_id(newbean.getIncharge_id());
			signingBean.setPre_incharge_id(bean.getIncharge_id());
			//合同账期
			signingBean.setPayment_period(newbean.getPayment_period());
			signingBean.setPre_payment_period(bean.getPayment_period());
			//合同金额
			signingBean.setTotal_amount(newbean.getTotal_amount());
			signingBean.setPre_total_amount(bean.getTotal_amount());
			//合同备注
			signingBean.setRemark(newbean.getAgreement_remark());
			
			signingBean.setCreator_id(SecurityUtils.getUser().getId());
			signingBean.setCreate_time(new Date());
			signingBean.setOrg_id(SecurityUtils.getUser().getOrgId());
			signingBean.setDept_id(SecurityUtils.getUser().getDeptId());
			service.signingDoSave(signingBean);
			//保存日志
			LogBean logBean = new LogBean();
			logBean.setOp_level("高");
			logBean.setOp_type("修改");
			logBean.setOp_name("客商资料协议");
			logBean.setOp_info("修改客商资料协议："+bean.getSerial_number()+" 客商资料："+newbean.getCoop_name());
			logService.doSave(logBean);
		}catch(Exception e){
			message=e.getMessage();
			System.out.println("We got unexpected:" + e.getMessage());
			code = 400;
		}
		
		MessageInfo messageInfo =new MessageInfo(dataMap,message);
		messageInfo.setCode(code);
		return messageInfo;
	}
	/*
	 * 续签
	 */
	@PreAuthorize("@pms.hasPermission('sys_coop_agreement_renew')")
	@RequestMapping(value = "/doRenew", method = RequestMethod.POST)
	public MessageInfo doRenew(@ModelAttribute("bean") CoopAgreementBean newbean) {
		String message = "success";
		int code =200;
		Map<String,Object> dataMap = new HashMap();
		try{
			//查询原协议
			CoopAgreementBean bean=service.queryByID(newbean.getAgreement_id());
			newbean.setEditor_id(SecurityUtils.getUser().getId());
			newbean.setEdit_time(new Date());
			newbean.setOrg_id(SecurityUtils.getUser().getOrgId());
			newbean.setDept_id(SecurityUtils.getUser().getDeptId());
			newbean.setAgreement_status(1);
//			service.doRenew(newbean);
			MessageInfo info=doSave(newbean);
			if (400==info.getCode()) {
				throw new Exception(info.getMessageInfo());
			} 
			//保存签约记录
			CoopAgreementSigningBean signingBean=new CoopAgreementSigningBean();
			signingBean.setAgreement_id(bean.getAgreement_id());
			signingBean.setCoop_id(bean.getCoop_id());
			signingBean.setSigning_type("续签"); 
			//合同编号
			signingBean.setSerial_number(newbean.getSerial_number());  
			signingBean.setPre_serial_number(bean.getSerial_number());
			//合同有效期
			signingBean.setBegin_date(newbean.getBegin_date());  
			signingBean.setEnd_date(newbean.getEnd_date()); 
			signingBean.setPre_begin_date(bean.getBegin_date());
			signingBean.setPre_end_date(bean.getEnd_date());
			//经办人
			signingBean.setIncharge_id(newbean.getIncharge_id());
			signingBean.setPre_incharge_id(bean.getIncharge_id());
			//合同账期
			signingBean.setPayment_period(newbean.getPayment_period());
			signingBean.setPre_payment_period(bean.getPayment_period());
			//合同金额
			signingBean.setTotal_amount(newbean.getTotal_amount());
			signingBean.setPre_total_amount(bean.getTotal_amount());
			//合同备注
			signingBean.setRemark(bean.getAgreement_remark());
			
			signingBean.setCreator_id(SecurityUtils.getUser().getId());
			signingBean.setCreate_time(new Date());
			signingBean.setOrg_id(SecurityUtils.getUser().getOrgId());
			signingBean.setDept_id(SecurityUtils.getUser().getDeptId());
			service.signingDoSave(signingBean);
			//保存日志
			LogBean logBean = new LogBean();
			logBean.setOp_level("高");
			logBean.setOp_type("续签");
			logBean.setOp_name("客商资料协议");
			logBean.setOp_info("续签客商资料协议："+bean.getSerial_number()+" 客商资料："+newbean.getCoop_name());
			logService.doSave(logBean);
		}catch(Exception e){
			message=e.getMessage();
			System.out.println("We got unexpected:" + e.getMessage());
			code = 400;
		}
		
		MessageInfo messageInfo =new MessageInfo(dataMap,message);
		messageInfo.setCode(code);
		return messageInfo;
	}
	/*
	 * 延期
	 */
	@PreAuthorize("@pms.hasPermission('sys_coop_agreement_extension')")
	@RequestMapping(value = "/doExtension", method = RequestMethod.POST)
	public MessageInfo doExtension(@ModelAttribute("bean") CoopAgreementBean newbean) {
		String message = "success";
		int code =200;
		Map<String,Object> dataMap = new HashMap();
		try{
			//查询原协议
			CoopAgreementBean bean=service.queryByID(newbean.getAgreement_id());
			newbean.setEditor_id(SecurityUtils.getUser().getId());
			newbean.setEdit_time(new Date());
			newbean.setOrg_id(SecurityUtils.getUser().getOrgId());
			newbean.setDept_id(SecurityUtils.getUser().getDeptId());
			newbean.setAgreement_status(1);
			service.doExtension(newbean);
			
			//保存签约记录
			CoopAgreementSigningBean signingBean=new CoopAgreementSigningBean();
			signingBean.setAgreement_id(bean.getAgreement_id());
			signingBean.setCoop_id(bean.getCoop_id());
			signingBean.setSigning_type("延期"); 
			//合同编号
			signingBean.setSerial_number(bean.getSerial_number());  
			signingBean.setPre_serial_number(bean.getSerial_number());
			//合同有效期
			signingBean.setBegin_date(newbean.getBegin_date());  
			signingBean.setEnd_date(newbean.getEnd_date()); 
			signingBean.setPre_begin_date(bean.getBegin_date());
			signingBean.setPre_end_date(bean.getEnd_date());
			//经办人
			signingBean.setIncharge_id(newbean.getIncharge_id());
			signingBean.setPre_incharge_id(bean.getIncharge_id());
			//合同账期
			signingBean.setPayment_period(bean.getPayment_period());
			signingBean.setPre_payment_period(bean.getPayment_period());
			//合同金额
			signingBean.setTotal_amount(bean.getTotal_amount());
			signingBean.setPre_total_amount(bean.getTotal_amount());
			//合同备注
			signingBean.setRemark(bean.getAgreement_remark());
			
			signingBean.setCreator_id(SecurityUtils.getUser().getId());
			signingBean.setCreate_time(new Date());
			signingBean.setOrg_id(SecurityUtils.getUser().getOrgId());
			signingBean.setDept_id(SecurityUtils.getUser().getDeptId());
			service.signingDoSave(signingBean);
			//保存日志
			LogBean logBean = new LogBean();
			logBean.setOp_level("高");
			logBean.setOp_type("延期");
			logBean.setOp_name("客商资料协议");
			logBean.setOp_info("延期客商资料协议："+bean.getSerial_number()+" 客商资料："+newbean.getCoop_name());
			logService.doSave(logBean);
		}catch(Exception e){
			message=e.getMessage();
			System.out.println("We got unexpected:" + e.getMessage());
			code = 400;
		}
		
		MessageInfo messageInfo =new MessageInfo(dataMap,message);
		messageInfo.setCode(code);
		return messageInfo;
	}
	public Date getEndDate(Date oldDate,Date nowDate){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(nowDate);
		calendar.add(Calendar.DATE, -1);

		
		Date returnDate=oldDate;
		try {
			Date newDate=df.parse(df.format(calendar.getTime()));
			oldDate=df.parse(df.format(oldDate));
			System.out.println(newDate +"<"+oldDate  +"="+(newDate.before(oldDate)));
			if (newDate.before(oldDate)) {
				returnDate=newDate;
			}
		} catch (ParseException e) {
		}
		return returnDate;
	}
	/*
	 * 废止
	 */
	@PreAuthorize("@pms.hasPermission('sys_coop_agreement_stop')")
	@RequestMapping(value = "/doStop", method = RequestMethod.POST)
	public MessageInfo doStop(@ModelAttribute("bean") CoopAgreementBean newbean) {
		String message = "success";
		int code =200;
		Map<String,Object> dataMap = new HashMap();
		try{
			//查询原协议
			CoopAgreementBean bean=service.queryByID(newbean.getAgreement_id());
			
			Date date=new Date();
			Date end_date=getEndDate(bean.getEnd_date(),date);
			newbean.setEnd_date(end_date); 
			newbean.setEditor_id(SecurityUtils.getUser().getId());
			newbean.setEdit_time(date);
			newbean.setOrg_id(SecurityUtils.getUser().getOrgId());
			newbean.setDept_id(SecurityUtils.getUser().getDeptId());
			newbean.setAgreement_status(0);
			service.doStop(newbean);
			
			//保存签约记录
			CoopAgreementSigningBean signingBean=new CoopAgreementSigningBean();
			signingBean.setAgreement_id(bean.getAgreement_id());
			signingBean.setCoop_id(bean.getCoop_id());
			signingBean.setSigning_type("废止"); 
			//合同编号
			signingBean.setSerial_number(bean.getSerial_number());  
			signingBean.setPre_serial_number(bean.getSerial_number());
			//合同有效期
			
			signingBean.setBegin_date(bean.getBegin_date());  
			signingBean.setEnd_date(end_date); 
			signingBean.setPre_begin_date(bean.getBegin_date());
			signingBean.setPre_end_date(bean.getEnd_date());
			//经办人
			signingBean.setIncharge_id(bean.getIncharge_id());
			signingBean.setPre_incharge_id(bean.getIncharge_id());
			//合同账期
			signingBean.setPayment_period(bean.getPayment_period());
			signingBean.setPre_payment_period(bean.getPayment_period());
			//合同金额
			signingBean.setTotal_amount(bean.getTotal_amount());
			signingBean.setPre_total_amount(bean.getTotal_amount());
			//合同备注
			signingBean.setRemark(bean.getAgreement_remark());
			
			signingBean.setCreator_id(SecurityUtils.getUser().getId());
			signingBean.setCreate_time(new Date());
			signingBean.setOrg_id(SecurityUtils.getUser().getOrgId());
			signingBean.setDept_id(SecurityUtils.getUser().getDeptId());
			service.signingDoSave(signingBean);
			//保存日志
			LogBean logBean = new LogBean();
			logBean.setOp_level("高");
			logBean.setOp_type("废止");
			logBean.setOp_name("客商资料协议");
			logBean.setOp_info("废止客商资料协议："+bean.getSerial_number()+" 客商资料："+newbean.getCoop_name());
			logService.doSave(logBean);
		}catch(Exception e){
			message=e.getMessage();
			System.out.println("We got unexpected:" + e.getMessage());
			code = 400;
		}
		
		MessageInfo messageInfo =new MessageInfo(dataMap,message);
		messageInfo.setCode(code);
		return messageInfo;
	}
}

