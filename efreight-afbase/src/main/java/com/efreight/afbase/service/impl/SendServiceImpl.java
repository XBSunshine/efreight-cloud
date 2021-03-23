package com.efreight.afbase.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.SendMapper;
import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.CargoGoodsnames;
import com.efreight.afbase.entity.LogBean;
import com.efreight.afbase.entity.OperationLook;
import com.efreight.afbase.entity.OrgInterface;
import com.efreight.afbase.entity.VlEntryOrder;
import com.efreight.afbase.entity.VlEntryOrderDetail;
import com.efreight.afbase.entity.shipping.APIType;
import com.efreight.afbase.service.LogService;
import com.efreight.afbase.service.SendService;
import com.efreight.afbase.utils.SendUtils;
import com.efreight.common.core.exception.CheckedException;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.SecurityUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
@AllArgsConstructor
@Slf4j
public class SendServiceImpl extends ServiceImpl<SendMapper, AfOrder> implements SendService {

	private final LogService logService;
	@Resource
	private SendMapper sendMapper;

	@Override
	public Map<String, Object> doEsdDecleare(String hasMwb, String orderUUID, String letterIds) {

		Map<String,Object> map = new HashMap<String,Object>();
		EUserDetails user = SecurityUtils.getUser();
        String apiType = APIType.AE_ESD_POST_MAWB;

		String type = APIType.getAPIType(apiType);
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
        if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
        //获取总单数据XMl
        String mawbXML = "";
        //获取分单数据xml
        String hawbXML = "";
        if(!"hwb".equals(hasMwb)){
            mawbXML = this.baseMapper.getNewMAWBXML(orderUUID, user.getId(), apiType);
        }
        if("all".equals(hasMwb) || StringUtils.isNotEmpty(letterIds)){
            hawbXML = this.baseMapper.getNewHAWBXML(orderUUID, letterIds, user.getId(), apiType);
        }

        String builder = "<data>";
        if (StringUtils.isNotBlank(mawbXML)) {
        	mawbXML=mawbXML.substring(0,mawbXML.indexOf("<CargoTerminal>"))+"<IsOverride>1</IsOverride>"+  mawbXML.substring(mawbXML.indexOf("<CargoTerminal>"));
        	builder=builder+mawbXML;
        }
        if (StringUtils.isNotBlank(hawbXML)) {
        	if (mawbXML.length()>0) {
        		builder= builder.toString().replace("</AirwayBill>", 
            		"<ConsolidationList>"+hawbXML.substring(hawbXML.indexOf("<ConsolidationList>")+19, hawbXML.indexOf("</ConsolidationList>"))+"</ConsolidationList>");
        		builder= builder + "</AirwayBill>";
        	} else {
        		hawbXML=hawbXML.substring(0,hawbXML.indexOf("<CargoTerminal>"))+"<IsOverride>1</IsOverride>"+  hawbXML.substring(hawbXML.indexOf("<CargoTerminal>"));
        		builder=builder+hawbXML;
			}
        }
        builder=builder+"</data>";
        log.info("参数开始"+builder.toString()+"参数结束");
//        ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"EAWB_PRE",builder.toString());
//        String objStr = responseEntity.getBody();
        HashMap<String, String> bodyMap=new HashMap<String, String>();
        bodyMap.put("data",builder.toString());
        String objStr = SendUtils.doSend(config.getUrlPost()+"EAWB_PRE",bodyMap);
        log.info("结果开始"+objStr+"结果结束");
        JSONObject jsonO = JSONObject.parseObject(objStr);
        String message  = jsonO.getString("messageInfo");
        map.put("status","success");
        map.put("message","发送预录入成功");

        LogBean logBean = new LogBean();
        if("01".equals(jsonO.getString("code"))) {
        	logBean.setLogRemark("发送成功");
        }else{
        	logBean.setLogRemark("发送失败："+message);
            map.put("message","发送预录入报文异常：" + message);
            map.put("status","exception");
        }
        logBean.setHasMwb(hasMwb);
        logBean.setOrderUuid(orderUUID);
        logBean.setLetterIds(letterIds);
        logBean.setPageFunction("发送预录入");
        logBean.setLogRemarkLarge(builder.toString());
        insertAfLog(logBean);
        return map;
	}
	@Override
	public Map<String, Object> doEsdDecleareFHL(String orderUUID, String letterIds) {
		Map<String,Object> map = new HashMap<String,Object>();
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.AE_ESD_POST_MAWB;
		String type = APIType.getAPIType(apiType);
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		//获取分单数据xml
		String hawbXML = "";
		hawbXML = this.baseMapper.getNewHAWBXML(orderUUID, letterIds, user.getId(), apiType);
		String builder = "<data>";
		if (StringUtils.isNotBlank(hawbXML)) {
			builder=builder+hawbXML;
		}
		builder=builder+"</data>";
		log.info("参数开始"+builder.toString()+"参数结束");
//        ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"EAWB_PRE",builder.toString());
//        String objStr = responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
		bodyMap.put("data",builder.toString());
		String objStr = SendUtils.doSend(config.getUrlPost()+"EAWB_PRE",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		JSONObject jsonO = JSONObject.parseObject(objStr);
		String message  = jsonO.getString("messageInfo");
		map.put("status","success");
		map.put("message","发送预录入成功");
		
		LogBean logBean = new LogBean();
		if("01".equals(jsonO.getString("code"))) {
			logBean.setLogRemark("发送成功");
		}else{
			logBean.setLogRemark("发送失败："+message);
			map.put("message","发送预录入报文异常：" + message);
			map.put("status","exception");
		}
//		logBean.setHasMwb(hasMwb);
		logBean.setOrderUuid(orderUUID);
		logBean.setLetterIds(letterIds);
		logBean.setPageFunction("发送预录入");
		logBean.setLogRemarkLarge(builder.toString());
		insertAfLog(logBean);
		return map;
	}
	@Override
	public Map<String, Object> doEawbPreDelete(String AWBNumber,String orderUUID) {
		Map<String,Object> map = new HashMap<String,Object>();
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.ALL_WORK;
		String type = APIType.getAPIType(apiType);
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		StringBuffer xml = new StringBuffer("");
		xml.append("<data>");
		xml.append("<AirwayBill>");
		xml.append("<AWBNumber>"+AWBNumber+"</AWBNumber>");
		xml.append("<Forwarder>"+config.getAppid()+"</Forwarder>");
		xml.append("<Handler>"+config.getAppid()+"</Handler>");
		xml.append("<HWBNumber></HWBNumber>");
		xml.append("</AirwayBill>");
		xml.append("</data>");
		
		
		log.info(xml.toString());
//		ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"EAWB_PRE_Delete",xml.toString());
//		String objStr = responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
        bodyMap.put("data",xml.toString());
        String objStr = SendUtils.doSend(config.getUrlPost()+"EAWB_PRE_Delete",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		JSONObject jsonO = JSONObject.parseObject(objStr);
		String message  = jsonO.getString("messageInfo");
		map.put("status","success");
		map.put("message","删除预录入成功");
		
		LogBean logBean = new LogBean();
		if("01".equals(jsonO.getString("code"))) {
			logBean.setLogRemark("发送成功");
		}else{
			logBean.setLogRemark("发送失败："+message);
			map.put("message","删除预录入报文异常：" + message);
			map.put("status","exception");
		}
		logBean.setOrderUuid(orderUUID);
		logBean.setPageFunction("删除预录入");
		logBean.setLogRemarkLarge(xml.toString());
		insertAfLog(logBean);
		return map;
	}

	/**
	 * 删除预配
	 * @param awbNumber
	 * @param orderCode
	 * @return
	 */
	@Override
	public Map<String, Object> doMft2201_Delete(String awbNumber,String hawbNumber, String orderCode,String contactTel, String deleteReason) {
		Map<String,Object> map = new HashMap();
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.AE_CD_AWB;
		String type = APIType.getAPIType(apiType);
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		StringBuffer xml = new StringBuffer("");
		xml.append("<data>");
		xml.append("<AirwayBill>");
		xml.append("<AWBNumber>"+awbNumber+"</AWBNumber>");
		xml.append("<Forwarder>"+config.getAppid()+"</Forwarder>");
		xml.append("<Handler>"+config.getAppid()+"</Handler>");
		if(hawbNumber!=null && !hawbNumber.equals(" ")&&!hawbNumber.equals("null")){
			xml.append("<HWBNumber>"+hawbNumber+"</HWBNumber>");
		}else{
			xml.append("<HWBNumber></HWBNumber>");
		}
		xml.append("<FlightNo></FlightNo>");
		xml.append("<FlightDate></FlightDate>");
		xml.append("<ContactTel>"+contactTel+"</ContactTel>");
		xml.append("<DeleteReason>"+deleteReason+"</DeleteReason>");
		xml.append("</AirwayBill>");
		xml.append("</data>");

		log.info(xml.toString());
//		ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"EAWB_PRE_Delete",xml.toString());
//		String objStr = responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
		bodyMap.put("data",xml.toString());
		String objStr = SendUtils.doSend(config.getUrlPost()+"Mft2201_Delete",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		JSONObject jsonO = JSONObject.parseObject(objStr);
		String message  = jsonO.getString("messageInfo");
		map.put("status","success");
		map.put("message","删除预配成功");

		LogBean logBean = new LogBean();
		if("01".equals(jsonO.getString("code"))) {
			logBean.setLogRemark("发送成功");
		}else{
			logBean.setLogRemark("发送失败："+message);
			map.put("message","删除预配报文异常：" + message);
			map.put("status","exception");
		}
		logBean.setOrderNumber(orderCode);
		logBean.setPageFunction("删除预配");
		logBean.setLogRemarkLarge(xml.toString());
		insertAfLog2(logBean);
		return map;
	}

	@Override
	public Map<String, Object> doEawbPreDeleteAwb(String AWBNumber,String orderCode) {
		Map<String,Object> map = new HashMap<String,Object>();
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.ALL_WORK;
		String type = APIType.getAPIType(apiType);
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		StringBuffer xml = new StringBuffer("");
		xml.append("<data>");
		xml.append("<AirwayBill>");
		xml.append("<AWBNumber>"+AWBNumber+"</AWBNumber>");
		xml.append("<Forwarder>"+config.getAppid()+"</Forwarder>");
		xml.append("<Handler>"+config.getAppid()+"</Handler>");
		xml.append("<HWBNumber></HWBNumber>");
		xml.append("</AirwayBill>");
		xml.append("</data>");
		
		
		log.info(xml.toString());
//		ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"EAWB_PRE_Delete",xml.toString());
//		String objStr = responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
		bodyMap.put("data",xml.toString());
		String objStr = SendUtils.doSend(config.getUrlPost()+"EAWB_PRE_Delete",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		JSONObject jsonO = JSONObject.parseObject(objStr);
		String message  = jsonO.getString("messageInfo");
		map.put("status","success");
		map.put("message","删除预录入成功");
		
		LogBean logBean = new LogBean();
		if("01".equals(jsonO.getString("code"))) {
			logBean.setLogRemark("发送成功");
		}else{
			logBean.setLogRemark("发送失败："+message);
			map.put("message","删除预录入报文异常：" + message);
			map.put("status","exception");
		}
		logBean.setOrderNumber(orderCode);
		logBean.setPageFunction("删除预录入");
		logBean.setLogRemarkLarge(xml.toString());
		insertAfLog2(logBean);
		return map;
	}
	@Override
	public Map<String, Object> doEawbPreDeleteFHL(String AWBNumber,String HWBNumber,String orderCode) {
		Map<String,Object> map = new HashMap<String,Object>();
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.ALL_WORK;
		String type = APIType.getAPIType(apiType);
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		StringBuffer xml = new StringBuffer("");
		xml.append("<data>");
		xml.append("<AirwayBill>");
		xml.append("<AWBNumber>"+AWBNumber+"</AWBNumber>");
		xml.append("<Forwarder>"+config.getAppid()+"</Forwarder>");
		xml.append("<Handler>"+config.getAppid()+"</Handler>");
		xml.append("<HWBNumber>"+HWBNumber+"</HWBNumber>");
		xml.append("</AirwayBill>");
		xml.append("</data>");
		
		
		log.info(xml.toString());
//		ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"EAWB_PRE_Delete",xml.toString());
//		String objStr = responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
		bodyMap.put("data",xml.toString());
		String objStr = SendUtils.doSend(config.getUrlPost()+"EAWB_PRE_Delete",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		JSONObject jsonO = JSONObject.parseObject(objStr);
		String message  = jsonO.getString("messageInfo");
		map.put("status","success");
		map.put("message","删除预录入成功");
		
		LogBean logBean = new LogBean();
		if("01".equals(jsonO.getString("code"))) {
			logBean.setLogRemark("发送成功");
		}else{
			logBean.setLogRemark("发送失败："+message);
			map.put("message","删除预录入报文异常：" + message);
			map.put("status","exception");
		}
		logBean.setOrderNumber(orderCode);
		logBean.setPageFunction("删除预录入");
		logBean.setLogRemarkLarge(xml.toString());
		insertAfLog2(logBean);
		return map;
	}
	@Override
	public Map<String, Object> doEAWB_WH(String hasMwb, String orderUUID, String letterIds) {
		Map<String,Object> map = new HashMap<String,Object>();
		EUserDetails user = SecurityUtils.getUser();
        String apiType = APIType.AE_ESD_POST_MAWB;
		String type =APIType. getAPIType(apiType);
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
        if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
        //获取总单数据XMl
        String mawbXML = "";
        //获取分单数据xml
        String hawbXML = "";
        if(!"hwb".equals(hasMwb)){
            mawbXML = this.baseMapper.getNewMAWBXML(orderUUID, user.getId(), apiType);
        }
        if("all".equals(hasMwb) || StringUtils.isNotEmpty(letterIds)){
            hawbXML = this.baseMapper.getNewHAWBXML(orderUUID, letterIds, user.getId(), apiType);
        }

        String builder = "<data>";
        if (StringUtils.isNotBlank(mawbXML)) {
        	mawbXML=mawbXML.substring(0,mawbXML.indexOf("<CargoTerminal>"))+"<IsOverride>1</IsOverride>"+  mawbXML.substring(mawbXML.indexOf("<CargoTerminal>"));
        	builder=builder+mawbXML;
        }
        if (StringUtils.isNotBlank(hawbXML)) {
        	if (mawbXML.length()>0) {
        		builder= builder.toString().replace("</AirwayBill>", 
            		"<ConsolidationList>"+hawbXML.substring(hawbXML.indexOf("<ConsolidationList>")+19, hawbXML.indexOf("</ConsolidationList>"))+"</ConsolidationList>");
        		builder= builder + "</AirwayBill>";
        	} 
//        	else {
//        		hawbXML=hawbXML.substring(0,hawbXML.indexOf("<CargoTerminal>"))+"<IsOverride></IsOverride>"+  hawbXML.substring(hawbXML.indexOf("<CargoTerminal>"));
//        		builder=builder+hawbXML;
//			}
        }
        builder=builder+"</data>";
       log.info("参数开始"+builder.toString()+"参数结束");
//        ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"EAWB_WH",builder.toString());
//        String objStr = responseEntity.getBody();
        HashMap<String, String> bodyMap=new HashMap<String, String>();
        bodyMap.put("data",builder.toString());
        String objStr = SendUtils.doSend(config.getUrlPost()+"EAWB_WH",bodyMap);
        log.info("结果开始"+objStr+"结果结束");
        JSONObject jsonO = JSONObject.parseObject(objStr);
        String message  = jsonO.getString("messageInfo");
        map.put("status","success");
        map.put("message","发送入库成功");

        LogBean logBean = new LogBean();
        if("01".equals(jsonO.getString("code"))) {
        	logBean.setLogRemark("发送成功");
        }else{
        	logBean.setLogRemark("发送失败："+message);
            map.put("message","发送入库报文异常：" + message);
            map.put("status","exception");
        }
        logBean.setHasMwb(hasMwb);
        logBean.setOrderUuid(orderUUID);
        logBean.setLetterIds(letterIds);
        logBean.setPageFunction("发送入库");
        logBean.setLogRemarkLarge(builder.toString());
        insertAfLog(logBean);
        return map;
	}
	@Override
	public Map<String, Object> doOneDecleare_ForSend(String hasMwb, String orderUUID, String letterIds) {
		Map<String,Object> map = new HashMap<String,Object>();
		EUserDetails user = SecurityUtils.getUser();
        String apiType = APIType.AE_ESD_POST_MAWB;
		String type =APIType. getAPIType(apiType);
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
        if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
        //获取总单数据XMl
        String mawbXML = "";
        //获取分单数据xml
        String hawbXML = "";
        if(!"hwb".equals(hasMwb)){
            mawbXML = this.baseMapper.getNewMAWBXML(orderUUID, user.getId(), apiType);
        }
        if("all".equals(hasMwb) || StringUtils.isNotEmpty(letterIds)){
            hawbXML = this.baseMapper.getNewHAWBXML(orderUUID, letterIds, user.getId(), apiType);
        }

        String builder = "<data>";
        if (StringUtils.isNotBlank(mawbXML)) {
        	mawbXML=mawbXML.substring(0,mawbXML.indexOf("<CargoTerminal>"))+"<OneDeclarationData>"+  mawbXML.substring(mawbXML.indexOf("<CargoTerminal>"));
        	mawbXML= mawbXML.replace("</AirwayBill>","</OneDeclarationData></AirwayBill>");
        	builder=builder+mawbXML;
        }
        if (StringUtils.isNotBlank(hawbXML)) {
        	if (mawbXML.length()>0) {
        		builder= builder.toString().replace("</OneDeclarationData></AirwayBill>", 
            		"<ConsolidationList>"+hawbXML.substring(hawbXML.indexOf("<ConsolidationList>")+19, hawbXML.indexOf("</ConsolidationList>"))+"</ConsolidationList>");
        		builder= builder + "</OneDeclarationData></AirwayBill>";
        	} 
//        	else {
//        		hawbXML=hawbXML.substring(0,hawbXML.indexOf("<CargoTerminal>"))+"<OneDeclarationData>"+  hawbXML.substring(hawbXML.indexOf("<CargoTerminal>"));
//        		builder=builder+hawbXML;
//			}
        }
        builder=builder+"</data>";
       log.info("参数开始"+builder.toString()+"参数结束");
//        ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"OneDecleare_ForSend",builder.toString());
//        String objStr = responseEntity.getBody();
        HashMap<String, String> bodyMap=new HashMap<String, String>();
        bodyMap.put("data",builder.toString());
        String objStr = SendUtils.doSend(config.getUrlPost()+"OneDecleare_ForSend",bodyMap);
        log.info("结果开始"+objStr+"结果结束");
        JSONObject jsonO = JSONObject.parseObject(objStr);
        String message  = jsonO.getString("messageInfo");
        map.put("status","success");
        map.put("message","发送一键申报成功");

        LogBean logBean = new LogBean();
        if("01".equals(jsonO.getString("code"))) {
        	logBean.setLogRemark("发送成功");
        }else{
        	logBean.setLogRemark("发送失败："+message);
            map.put("message","发送一键申报报文异常：" + message);
            map.put("status","exception");
        }
        logBean.setHasMwb(hasMwb);
        logBean.setOrderUuid(orderUUID);
        logBean.setLetterIds(letterIds);
        logBean.setPageFunction("发送一键申报");
        logBean.setLogRemarkLarge(builder.toString());
        insertAfLog(logBean);
        return map;
	}
	@Override
	public Map<String, Object> doMft2201_Decleare(String hasMwb, String orderUUID, String letterIds) {
		Map<String,Object> map = new HashMap<String,Object>();
		EUserDetails user = SecurityUtils.getUser();
        String apiType = APIType.AE_CD_AWB;
		AfOrder order = baseMapper.getOrderByUUID(user.getOrgId(), orderUUID);
		String scope = order.getBusinessScope();
		String s = sendMapper.selectMft2201SaveByOrgId(user.getOrgId(), scope);
		String mft2201Save = "Send";
		if(s!=null && !"".equals(s)){
			if(s.equals("1")){
				mft2201Save = "Save";
			}
		}

		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);

		String type = APIType.getAPIType(apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
        //获取总单数据XMl
        String mawbXML = "";
        //获取分单数据xml
        String hawbXML = "";
        if(!"hwb".equals(hasMwb)){
            mawbXML = this.baseMapper.getNewMAWBXML(orderUUID, user.getId(), apiType);
        }
        if("all".equals(hasMwb) || StringUtils.isNotEmpty(letterIds)){
            hawbXML = this.baseMapper.getNewHAWBXML(orderUUID, letterIds, user.getId(), apiType);
        }
		StringBuilder builder = new StringBuilder();
        builder.append("<data>");
        if (StringUtils.isNotBlank(mawbXML)) {
			mawbXML=mawbXML.substring(0,mawbXML.indexOf("<CargoTerminal>"))+"<IsOverride>1</IsOverride>"+  mawbXML.substring(mawbXML.indexOf("<CargoTerminal>"));
			String newMawbXML = mawbXML.replace("<ManifestStatus>{mft2201Save}</ManifestStatus>", "<ManifestStatus>" + mft2201Save + "</ManifestStatus>");

        	 builder.append(newMawbXML);
        }
        if (StringUtils.isNotBlank(hawbXML)) {
        	if (mawbXML.length()>0) {
        		builder= new StringBuilder(builder.toString().replace("</AirwayBill>", 
            		"<ConsolidationList>"+hawbXML.substring(hawbXML.indexOf("<ConsolidationList>")+19, hawbXML.indexOf("</ConsolidationList>"))+"</ConsolidationList>"));
				//builder.append("<ManifestStatus>"+mft2201Save+"</ManifestStatus>");
        		 builder.append("</AirwayBill>");
        	} else {
        		hawbXML=hawbXML.substring(0,hawbXML.indexOf("<CargoTerminal>"))+"<IsOverride>1</IsOverride>"+  hawbXML.substring(hawbXML.indexOf("<CargoTerminal>"));
        		 builder.append(hawbXML);
			}
        }

        builder.append("</data>");
        log.info("参数开始"+builder.toString()+"参数结束");
//        ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"Mft2201_Decleare",builder.toString());
//        String objStr = responseEntity.getBody();
        HashMap<String, String> bodyMap=new HashMap<String, String>();
        bodyMap.put("data",builder.toString());
        String objStr = SendUtils.doSend(config.getUrlPost()+"Mft2201_Decleare",bodyMap);
        log.info("结果开始"+objStr+"结果结束");
        JSONObject jsonO = JSONObject.parseObject(objStr);
        String message  = jsonO.getString("messageInfo");
        map.put("status","success");
        map.put("message","发送预配舱单成功");
		//System.out.println(builder);
        LogBean logBean = new LogBean();
        if("01".equals(jsonO.getString("code"))) {
        	logBean.setLogRemark("发送成功");
        }else{
        	logBean.setLogRemark("发送失败："+message);
            map.put("message","发送预配舱单报文异常：" + message);
            map.put("status","exception");
        }
        logBean.setHasMwb(hasMwb);
        logBean.setOrderUuid(orderUUID);
        logBean.setLetterIds(letterIds);
        logBean.setPageFunction("发送预配舱单");
        logBean.setLogRemarkLarge(builder.toString());
        insertAfLog(logBean);
        return map;
	}
	@Override
	public Map<String, Object> doEAWB_AMS(String hasMwb, String orderUUID, String letterIds) {
		Map<String,Object> map = new HashMap<String,Object>();
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.AE_DZ_MAWB;
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		String type = APIType.getAPIType(apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		//获取总单数据XMl
		String mawbXML = "";
		//获取分单数据xml
		String hawbXML = "";
		if(!"hwb".equals(hasMwb)){
			mawbXML = this.baseMapper.getNewMAWBXML(orderUUID, user.getId(), apiType);
		}
		if("all".equals(hasMwb) || StringUtils.isNotEmpty(letterIds)){
			hawbXML = this.baseMapper.getNewHAWBXML(orderUUID, letterIds, user.getId(), apiType);
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("<data>");
		if (StringUtils.isNotBlank(mawbXML)) {
			builder.append(mawbXML);
		}
		if (StringUtils.isNotBlank(hawbXML)) {
			if (mawbXML.length()>0) {
				builder= new StringBuilder(builder.toString().replace("</AirwayBill>", 
						"<ConsolidationList>"+hawbXML.substring(hawbXML.indexOf("<ConsolidationList>")+19, hawbXML.indexOf("</ConsolidationList>"))+"</ConsolidationList>"));
				builder.append("</AirwayBill>");
			} else {
				builder.append(hawbXML);
			}
		}
		builder.append("</data>");
		log.info("参数开始"+builder.toString()+"参数结束");
//		ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"EAWB_AMS",builder.toString());
//		String objStr = responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
        bodyMap.put("data",builder.toString());
        String objStr = SendUtils.doSend(config.getUrlPost()+"EAWB_AMS",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		JSONObject jsonO = JSONObject.parseObject(objStr);
		String message  = jsonO.getString("messageInfo");
		map.put("status","success");
		map.put("message","发送电子运单成功");
		
		LogBean logBean = new LogBean();
		if("01".equals(jsonO.getString("code"))) {
			logBean.setLogRemark("发送成功");
		}else{
			logBean.setLogRemark("发送失败："+message);
			map.put("message","发送电子运单报文异常：" + message);
			map.put("status","exception");
		}
		logBean.setHasMwb(hasMwb);
		logBean.setOrderUuid(orderUUID);
		logBean.setLetterIds(letterIds);
		logBean.setPageFunction("发送电子运单");
		logBean.setLogRemarkLarge(builder.toString());
		insertAfLog(logBean);
		return map;
	}
	@Override
	public Map<String, Object> doEAWB_TB(String hasMwb, String orderUUID, String letterIds) {
		Map<String,Object> map = new HashMap<String,Object>();
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.AE_DZ_MAWB;
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		if (config == null ) {
			String type = APIType.getAPIType(apiType);
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		//获取总单数据XMl
		String mawbXML = "";
		//获取分单数据xml
		String hawbXML = "";
		if(!"hwb".equals(hasMwb)){
			mawbXML = this.baseMapper.getNewMAWBXML(orderUUID, user.getId(), apiType);
		}
		if("all".equals(hasMwb) || StringUtils.isNotEmpty(letterIds)){
			hawbXML = this.baseMapper.getNewHAWBXML(orderUUID, letterIds, user.getId(), apiType);
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("<data>");
		if (StringUtils.isNotBlank(mawbXML)) {
			builder.append(mawbXML);
		}
		if (StringUtils.isNotBlank(hawbXML)) {
			if (mawbXML.length()>0) {
				builder= new StringBuilder(builder.toString().replace("</AirwayBill>", 
						"<ConsolidationList>"+hawbXML.substring(hawbXML.indexOf("<ConsolidationList>")+19, hawbXML.indexOf("</ConsolidationList>"))+"</ConsolidationList>"));
				builder.append("</AirwayBill>");
			} else {
				builder.append(hawbXML);
			}
		}
		builder.append("</data>");
		log.info("参数开始"+builder.toString()+"参数结束");
//		ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"EAWB_AMS",builder.toString());
//		String objStr = responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
		bodyMap.put("data",builder.toString());
		String objStr = SendUtils.doSend(config.getUrlPost()+"EAWB_TB",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		JSONObject jsonO = JSONObject.parseObject(objStr);
		String message  = jsonO.getString("messageInfo");
		map.put("status","success");
		map.put("message","发送货站成功");
		
		LogBean logBean = new LogBean();
		if("01".equals(jsonO.getString("code"))) {
			logBean.setLogRemark("发送成功");
		}else{
			logBean.setLogRemark("发送失败："+message);
			map.put("message","发送货站报文异常：" + message);
			map.put("status","exception");
		}
		logBean.setHasMwb(hasMwb);
		logBean.setOrderUuid(orderUUID);
		logBean.setLetterIds(letterIds);
		logBean.setPageFunction("发送货站");
		logBean.setLogRemarkLarge(builder.toString());
		insertAfLog(logBean);
		return map;
	}
    public Boolean insertAfLog(LogBean logBean) {
        try{
            String uuid = logBean.getOrderUuid();
            AfOrder bean = baseMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), uuid);
            logBean.setPageName("操作订单");

            logBean.setBusinessScope("AE");
            logBean.setOrderNumber(bean.getOrderCode());
            logBean.setOrderId(bean.getOrderId());
            logBean.setOrgId(SecurityUtils.getUser().getOrgId());
            logService.saveLog(logBean);

            //更新订单表/分单表 manifest_status
//            this.baseMapper.updateManifestStatus(logBean.getHasMwb(),uuid,logBean.getLetterIds(),"HAS_SEND");
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }
    public Boolean insertAfLog2(LogBean logBean) {
    	try{
    		String orderNumber = logBean.getOrderNumber();
    		if(orderNumber== null && orderNumber.equals("")){
    			log.error("未查询到该订单的订单号");
    			return false;
			}
    		AfOrder bean = baseMapper.getOrderNumber(SecurityUtils.getUser().getOrgId(), orderNumber);
    		if(bean==null){
    			log.error("未找到该订单信息");
    			return false;
			}
    		logBean.setPageName("操作看板");
    		
    		logBean.setBusinessScope("AE");
    		logBean.setOrderUuid(bean.getOrderUuid());
    		logBean.setOrderId(bean.getOrderId());
    		logBean.setOrgId(SecurityUtils.getUser().getOrgId());
    		logService.saveLog(logBean);
    		
    		//更新订单表/分单表 manifest_status
//            this.baseMapper.updateManifestStatus(logBean.getHasMwb(),uuid,logBean.getLetterIds(),"HAS_SEND");
    		return true;
    	}catch (Exception e){
    		e.printStackTrace();
    		return false;
    	}
    	
    }
	@Override
	public Map<String, Object> doSendGoodsName(Integer orderId, String orderUUID,String awbNumber) {
		Map<String,Object> map = new HashMap<String,Object>();
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.AE_CD_IMP_HAWB;
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		String type = APIType.getAPIType(apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		//
		List<CargoGoodsnames> list=baseMapper.querylist(orderId);

		StringBuilder builder = new StringBuilder();
		builder.append("<Service>");
		builder.append("  <ServiceURL>CargoGoodsNames</ServiceURL>");
		builder.append("  <ServiceAction>UserWebSend</ServiceAction>");
		builder.append("  <ServiceData>");
		builder.append("  <CargoGoods>");
		builder.append("    <Mawb>"+awbNumber+"</Mawb>");
		builder.append("    <Forwarder>"+config.getAppid()+"</Forwarder>");
    	builder.append("    <Handler>"+SecurityUtils.getUser().getUserCname()+"</Handler>");
		for (int i = 0; i < list.size(); i++) {
			builder.append("    <GoodsName>");
				builder.append("    <NameCN>"+nullToEmpty(list.get(i).getGoodsCnnames())+"</NameCN>");
				builder.append("    <NameEN>"+nullToEmpty(list.get(i).getGoodsEnnames())+"</NameEN>");
				builder.append("    <Quantity>"+nullToEmpty(list.get(i).getQuantity())+"</Quantity>");
				builder.append("    <CargoType>"+nullToEmpty(list.get(i).getCargoType())+"</CargoType>");
			builder.append("    </GoodsName>");
		}
		builder.append("  </CargoGoods>");
		builder.append("  </ServiceData>");
		builder.append("</Service>");
		
		
		
		log.info("参数开始"+builder.toString()+"参数结束");
//		ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"EAWB_AMS",builder.toString());
//		String objStr = responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
        bodyMap.put("serviceXml",builder.toString());
        String objStr = SendUtils.doSend(config.getUrlPost()+"",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		LogBean logBean = new LogBean();
		try {
		org.dom4j.Document document = DocumentHelper.parseText(objStr);
		org.dom4j.Element rootElement = document.getRootElement();
		
//		JSONObject jsonO = JSONObject.parseObject(objStr);
//		String message  = jsonO.getString("ResultContent");
		String message  = rootElement.element("ResultContent").getText();
		map.put("status","success");
		map.put("message","发送物品清单成功");
		
		
		if("1".equals(rootElement.element("ResultCode").getText())) {
			logBean.setLogRemark("发送成功");
		}else{
			logBean.setLogRemark("发送失败："+message);
			map.put("message","发送物品清单报文异常：" + message);
			map.put("status","exception");
		}
		}catch (Exception e) {
    		
    	}
//		logBean.setHasMwb(hasMwb);
		logBean.setOrderUuid(orderUUID);
//		logBean.setLetterIds(letterIds);
		logBean.setPageFunction("发送物品清单");
		logBean.setLogRemarkLarge(builder.toString());
		insertAfLog(logBean);
		return map;
	}
	@Override
	public Map<String, Object> doSendVEEntry(Integer entryOrderId) {
		Map<String,Object> map = new HashMap<String,Object>();
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.ALL_WORK;
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		String type = APIType.getAPIType(apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		//
		VlEntryOrder vlEntryOrder=baseMapper.getVlEntryOrder(entryOrderId);
		List<VlEntryOrderDetail> detailList=baseMapper.getVlEntryOrderDetails(entryOrderId);
		
		StringBuilder builder = new StringBuilder();
		builder.append("<VehicleDeliveryOrder>");
		builder.append("    <Forwarder>"+config.getAppid()+"</Forwarder>");
		builder.append("    <Handler>"+SecurityUtils.getUser().getUserCname()+"</Handler>");
		builder.append("    <WH_Code>"+nullToEmpty(vlEntryOrder.getWarehouseCode())+"</WH_Code>");
		builder.append("    <MasterCustom>5141</MasterCustom>");
		builder.append("    <MessageType>ENTRY</MessageType>");
		
		if(vlEntryOrder.getMft8802024MessageId()!=null&&vlEntryOrder.getMft8802024MessageId().length()>0) {
			builder.append("    <DeclareFlag>1</DeclareFlag>");
		}else {
			builder.append("    <DeclareFlag>0</DeclareFlag>");
		}
		builder.append("  <Mft8802024MSGID>"+nullToEmpty(vlEntryOrder.getMft8802024MessageId())+"</Mft8802024MSGID>");
		builder.append("  <VehicleICIssue>");
		builder.append("    <VehicleNo>"+nullToEmpty(vlEntryOrder.getVehicleNo())+"</VehicleNo>");
		builder.append("    <VehTeamFlag>"+nullToEmpty(vlEntryOrder.getVehteamFlag())+"</VehTeamFlag>");
		builder.append("    <VehTeamNo>"+nullToEmpty(vlEntryOrder.getVehteamNo())+"</VehTeamNo>");
		builder.append("    <DriverInfo>"+nullToEmpty(vlEntryOrder.getDriverInfo())+"</DriverInfo>");
		builder.append("    <TransInFlag>"+nullToEmpty(vlEntryOrder.getTransInFlag())+"</TransInFlag>");
		builder.append("    <TransInFlag>"+nullToEmpty(vlEntryOrder.getTransInFlag())+"</TransInFlag>");
		builder.append("    <AppointmentDate></AppointmentDate>");
		builder.append("    <AppointmentTime></AppointmentTime>");
		builder.append("    <TransportType>"+nullToEmpty(vlEntryOrder.getTransportType())+"</TransportType>");
		builder.append("    <TrailerNoIn>"+nullToEmpty(vlEntryOrder.getTrailerNoIn())+"</TrailerNoIn>");
		builder.append("    <TrailerWtIn>"+nullToEmpty(vlEntryOrder.getTrailerWeightIn())+"</TrailerWtIn>");
		builder.append("    <TrailerNoOut>"+nullToEmpty(vlEntryOrder.getTrailerNoOut())+"</TrailerNoOut>");
		builder.append("    <TrailerWtOut>"+nullToEmpty(vlEntryOrder.getTrailerWeightOut())+"</TrailerWtOut>");
		builder.append("    <ContWtIn>"+nullToEmpty(vlEntryOrder.getContWeightIn())+"</ContWtIn>");
		builder.append("    <ContWtOut>"+nullToEmpty(vlEntryOrder.getContWeightOut())+"</ContWtOut>");
		builder.append("    <Note>"+nullToEmpty(vlEntryOrder.getNote())+"</Note>");
		builder.append("  <VehLoadingGoodsList>");
		for (int i = 0; i < detailList.size(); i++) {
			builder.append("    <VehLoadingGoods>");
			builder.append("    <SeqNo>"+i+1+"</SeqNo>");
			builder.append("    <MawbCode>"+nullToEmpty(detailList.get(i).getMawbNumber())+"</MawbCode>");
			builder.append("    <HawbCode>"+nullToEmpty(detailList.get(i).getHawbNumber())+"</HawbCode>");
			builder.append("    <DocNo>"+nullToEmpty(detailList.get(i).getDocNo())+"</DocNo>");
			builder.append("    <ClassType>"+nullToEmpty(detailList.get(i).getClassType())+"</ClassType>");
			builder.append("    <CargoType>"+nullToEmpty(detailList.get(i).getCargoType())+"</CargoType>");
			builder.append("    <FlightNo>"+nullToEmpty(detailList.get(i).getFlightNo())+"</FlightNo>");
			builder.append("    <FlightDate>"+nullToEmpty(detailList.get(i).getFlightDate())+"</FlightDate>");
			builder.append("    <Destination>"+nullToEmpty(detailList.get(i).getDestination())+"</Destination>");
			builder.append("    <GoodsName>"+nullToEmpty(detailList.get(i).getGoodsName())+"</GoodsName>");
			builder.append("    <GoodsEName>"+nullToEmpty(detailList.get(i).getGoodsEname())+"</GoodsEName>");
			builder.append("    <QuantityQuantity>"+nullToEmpty(detailList.get(i).getPieces())+"</QuantityQuantity>");
			builder.append("    <TotalGrossMassMeasure>"+nullToEmpty(detailList.get(i).getTotalWeight())+"</TotalGrossMassMeasure>");
			builder.append("    <PredictionVolume>"+nullToEmpty(detailList.get(i).getPredictionVolume())+"</PredictionVolume>");
			builder.append("    <HandlingCompany>"+nullToEmpty(detailList.get(i).getHandlingCompanyName())+"</HandlingCompany>");
			String handlingCompanyName=baseMapper.getHandlingCompanyName(detailList.get(i).getHandlingCompanyName(),"搬运公司");
			builder.append("    <HandlingCompanyName>"+nullToEmpty(handlingCompanyName)+"</HandlingCompanyName>");
			builder.append("    <PackageSize>"+nullToEmpty(detailList.get(i).getPackageSize())+"</PackageSize>");
			builder.append("    <BattleName>"+nullToEmpty(detailList.get(i).getBattleName())+"</BattleName>");
			builder.append("    <WareName>"+nullToEmpty(detailList.get(i).getWareName())+"</WareName>");
			builder.append("    <AirlineName>"+nullToEmpty(detailList.get(i).getAirlineName())+"</AirlineName>");
			builder.append("    </VehLoadingGoods>");
		}
		builder.append("  </VehLoadingGoodsList>");
		builder.append("  </VehicleICIssue>");
		builder.append("</VehicleDeliveryOrder>");
		
		
		
		log.info("参数开始"+builder.toString()+"参数结束");
//		ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"EAWB_AMS",builder.toString());
//		String objStr = responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
        bodyMap.put("data",builder.toString());
        String objStr = SendUtils.doSend(config.getUrlPost()+"VE_Entry",bodyMap);
        log.info("结果开始"+objStr+"结果结束");
        JSONObject jsonO = JSONObject.parseObject(objStr);
        String message  = jsonO.getString("messageInfo");
        map.put("status","success");
        map.put("message","发送卡口入区登记成功");

        LogBean logBean = new LogBean();
        if("01".equals(jsonO.getString("code"))) {
        	String mft8802024_message_id="";
        	if (message!=null && message.length()>0) {
        		mft8802024_message_id=message;
			}
        	baseMapper.updateVEEntry(entryOrderId,mft8802024_message_id,SecurityUtils.getUser().getId(),SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        	logBean.setLogRemark("发送成功");
        }else{
        	logBean.setLogRemark("发送失败："+message);
            map.put("message","发送卡口入区登记报文异常：" + message);
            map.put("status","exception");
        }

		return map;
	}
	@Override
	public Map<String, Object> doVEEntryConfirm(Integer entryOrderId) {
		Map<String,Object> map = new HashMap<String,Object>();
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.ALL_WORK;
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		String type = APIType.getAPIType(apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		//
		VlEntryOrder vlEntryOrder=baseMapper.getVlEntryOrder(entryOrderId);
		List<VlEntryOrderDetail> detailList=baseMapper.getVlEntryOrderDetails(entryOrderId);
		
		StringBuilder builder = new StringBuilder();
		builder.append("<VehicleDeliveryOrder>");
		builder.append("    <Forwarder>"+config.getAppid()+"</Forwarder>");
		builder.append("    <Handler>"+SecurityUtils.getUser().getUserCname()+"</Handler>");
		builder.append("    <WH_Code>"+nullToEmpty(vlEntryOrder.getWarehouseCode())+"</WH_Code>");
		builder.append("    <MasterCustom>5141</MasterCustom>");
		builder.append("    <MessageType>ENTRY</MessageType>");
		
		builder.append("    <DeclareFlag>2</DeclareFlag>");
		builder.append("  <Mft8802024MSGID>"+nullToEmpty(vlEntryOrder.getMft8802024MessageId())+"</Mft8802024MSGID>");
		builder.append("  <VehicleICIssue>");
		builder.append("    <VehicleNo>"+nullToEmpty(vlEntryOrder.getVehicleNo())+"</VehicleNo>");
		builder.append("    <VehTeamFlag>"+nullToEmpty(vlEntryOrder.getVehteamFlag())+"</VehTeamFlag>");
		builder.append("    <VehTeamNo>"+nullToEmpty(vlEntryOrder.getVehteamNo())+"</VehTeamNo>");
		builder.append("    <DriverInfo>"+nullToEmpty(vlEntryOrder.getDriverInfo())+"</DriverInfo>");
		builder.append("    <TransInFlag>"+nullToEmpty(vlEntryOrder.getTransInFlag())+"</TransInFlag>");
		builder.append("    <TransInFlag>"+nullToEmpty(vlEntryOrder.getTransInFlag())+"</TransInFlag>");
		builder.append("    <AppointmentDate></AppointmentDate>");
		builder.append("    <AppointmentTime></AppointmentTime>");
		builder.append("    <TransportType>"+nullToEmpty(vlEntryOrder.getTransportType())+"</TransportType>");
		builder.append("    <TrailerNoIn>"+nullToEmpty(vlEntryOrder.getTrailerNoIn())+"</TrailerNoIn>");
		builder.append("    <TrailerWtIn>"+nullToEmpty(vlEntryOrder.getTrailerWeightIn())+"</TrailerWtIn>");
		builder.append("    <TrailerNoOut>"+nullToEmpty(vlEntryOrder.getTrailerNoOut())+"</TrailerNoOut>");
		builder.append("    <TrailerWtOut>"+nullToEmpty(vlEntryOrder.getTrailerWeightOut())+"</TrailerWtOut>");
		builder.append("    <ContWtIn>"+nullToEmpty(vlEntryOrder.getContWeightIn())+"</ContWtIn>");
		builder.append("    <ContWtOut>"+nullToEmpty(vlEntryOrder.getContWeightOut())+"</ContWtOut>");
		builder.append("    <Note>"+nullToEmpty(vlEntryOrder.getNote())+"</Note>");
		builder.append("  <VehLoadingGoodsList>");
		for (int i = 0; i < detailList.size(); i++) {
			builder.append("    <VehLoadingGoods>");
			builder.append("    <SeqNo>"+i+1+"</SeqNo>");
			builder.append("    <MawbCode>"+nullToEmpty(detailList.get(i).getMawbNumber())+"</MawbCode>");
			builder.append("    <HawbCode>"+nullToEmpty(detailList.get(i).getHawbNumber())+"</HawbCode>");
			builder.append("    <DocNo>"+nullToEmpty(detailList.get(i).getDocNo())+"</DocNo>");
			builder.append("    <ClassType>"+nullToEmpty(detailList.get(i).getClassType())+"</ClassType>");
			builder.append("    <CargoType>"+nullToEmpty(detailList.get(i).getCargoType())+"</CargoType>");
			builder.append("    <FlightNo>"+nullToEmpty(detailList.get(i).getFlightNo())+"</FlightNo>");
			builder.append("    <FlightDate>"+nullToEmpty(detailList.get(i).getFlightDate())+"</FlightDate>");
			builder.append("    <Destination>"+nullToEmpty(detailList.get(i).getDestination())+"</Destination>");
			builder.append("    <GoodsName>"+nullToEmpty(detailList.get(i).getGoodsName())+"</GoodsName>");
			builder.append("    <GoodsEName>"+nullToEmpty(detailList.get(i).getGoodsEname())+"</GoodsEName>");
			builder.append("    <QuantityQuantity>"+nullToEmpty(detailList.get(i).getPieces())+"</QuantityQuantity>");
			builder.append("    <TotalGrossMassMeasure>"+nullToEmpty(detailList.get(i).getTotalWeight())+"</TotalGrossMassMeasure>");
			builder.append("    <PredictionVolume>"+nullToEmpty(detailList.get(i).getPredictionVolume())+"</PredictionVolume>");
			builder.append("    <HandlingCompany>"+nullToEmpty(detailList.get(i).getHandlingCompanyName())+"</HandlingCompany>");
			String handlingCompanyName=baseMapper.getHandlingCompanyName(detailList.get(i).getHandlingCompanyName(),"搬运公司");
			builder.append("    <HandlingCompanyName>"+nullToEmpty(handlingCompanyName)+"</HandlingCompanyName>");
			builder.append("    <PackageSize>"+nullToEmpty(detailList.get(i).getPackageSize())+"</PackageSize>");
			builder.append("    <BattleName>"+nullToEmpty(detailList.get(i).getBattleName())+"</BattleName>");
			builder.append("    <WareName>"+nullToEmpty(detailList.get(i).getWareName())+"</WareName>");
			builder.append("    <AirlineName>"+nullToEmpty(detailList.get(i).getAirlineName())+"</AirlineName>");
			builder.append("    </VehLoadingGoods>");
		}
		builder.append("  </VehLoadingGoodsList>");
		builder.append("  </VehicleICIssue>");
		builder.append("</VehicleDeliveryOrder>");
		
		
		
		log.info("参数开始"+builder.toString()+"参数结束");
//		ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"EAWB_AMS",builder.toString());
//		String objStr = responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
		bodyMap.put("data",builder.toString());
		String objStr = SendUtils.doSend(config.getUrlPost()+"VE_Entry_Confirm",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		JSONObject jsonO = JSONObject.parseObject(objStr);
		String message  = jsonO.getString("messageInfo");
		map.put("status","success");
		map.put("message","发送卡口入区登记强制核销成功");
		
		LogBean logBean = new LogBean();
		if("01".equals(jsonO.getString("code"))) {
			String mft8802024_message_id="";
			if (message!=null && message.length()>0) {
        		mft8802024_message_id=message;
			}
        	baseMapper.updateVEEntry(entryOrderId,mft8802024_message_id,SecurityUtils.getUser().getId(),SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
			logBean.setLogRemark("发送成功");
		}else{
			logBean.setLogRemark("发送失败："+message);
			map.put("message","发送卡口入区登记强制核销报文异常：" + message);
			map.put("status","exception");
		}
		
		return map;
	}
	@Override
	public Map<String, Object> doSendVEAppoint(Integer entryOrderId) {
		Map<String,Object> map = new HashMap<String,Object>();
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.ALL_WORK;
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		String type = APIType.getAPIType(apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		//
		VlEntryOrder vlEntryOrder=baseMapper.getVlEntryOrder(entryOrderId);
		List<VlEntryOrderDetail> detailList=baseMapper.getVlEntryOrderDetails(entryOrderId);
		
		StringBuilder builder = new StringBuilder();
		builder.append("<VehicleDeliveryOrder>");
		builder.append("    <Forwarder>"+config.getAppid()+"</Forwarder>");
		builder.append("    <Handler>"+SecurityUtils.getUser().getUserCname()+"</Handler>");
		builder.append("    <WH_Code>"+nullToEmpty(vlEntryOrder.getWarehouseCode())+"</WH_Code>");
		builder.append("    <MasterCustom>5141</MasterCustom>");
		builder.append("    <MessageType>APPOINT</MessageType>");
		
		if(vlEntryOrder.getMft8802024MessageId()!=null&&vlEntryOrder.getMft8802024MessageId().length()>0) {
			builder.append("    <DeclareFlag>1</DeclareFlag>");
		}else {
			builder.append("    <DeclareFlag>0</DeclareFlag>");
		}
		builder.append("    <Mft8802024MSGID>"+nullToEmpty(vlEntryOrder.getMft8802024MessageId())+"</Mft8802024MSGID>");
		builder.append("  <VehicleICIssue>");
		builder.append("    <VehicleNo>"+nullToEmpty(vlEntryOrder.getVehicleNo())+"</VehicleNo>");
		builder.append("    <VehTeamFlag>"+nullToEmpty(vlEntryOrder.getVehteamFlag())+"</VehTeamFlag>");
		builder.append("    <VehTeamNo>"+nullToEmpty(vlEntryOrder.getVehteamNo())+"</VehTeamNo>");
		builder.append("    <DriverInfo>"+nullToEmpty(vlEntryOrder.getDriverInfo())+"</DriverInfo>");
		builder.append("    <TransInFlag>"+nullToEmpty(vlEntryOrder.getTransInFlag())+"</TransInFlag>");
		builder.append("    <TransInFlag>"+nullToEmpty(vlEntryOrder.getTransInFlag())+"</TransInFlag>");
		
		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		String localTime = df.format(vlEntryOrder.getAppointmentDatetime());
		if (localTime!=null && localTime.length()>0) {
			builder.append("    <AppointmentDate>"+localTime.split(" ")[0]+"</AppointmentDate>");
			String time=localTime.split(" ")[1].split(":")[0];
			builder.append("    <AppointmentTime>"+(time.length()>1?time.substring(1):time)+"</AppointmentTime>");
		}else {
			builder.append("    <AppointmentDate></AppointmentDate>");
			builder.append("    <AppointmentTime></AppointmentTime>");
		}
		
		builder.append("    <TransportType>"+nullToEmpty(vlEntryOrder.getTransportType())+"</TransportType>");
		builder.append("    <TrailerNoIn>"+nullToEmpty(vlEntryOrder.getTrailerNoIn())+"</TrailerNoIn>");
		builder.append("    <TrailerWtIn>"+nullToEmpty(vlEntryOrder.getTrailerWeightIn())+"</TrailerWtIn>");
		builder.append("    <TrailerNoOut>"+nullToEmpty(vlEntryOrder.getTrailerNoOut())+"</TrailerNoOut>");
		builder.append("    <TrailerWtOut>"+nullToEmpty(vlEntryOrder.getTrailerWeightOut())+"</TrailerWtOut>");
		builder.append("    <ContWtIn>"+nullToEmpty(vlEntryOrder.getContWeightIn())+"</ContWtIn>");
		builder.append("    <ContWtOut>"+nullToEmpty(vlEntryOrder.getContWeightOut())+"</ContWtOut>");
		builder.append("    <Note>"+nullToEmpty(vlEntryOrder.getNote())+"</Note>");
		builder.append("  <VehLoadingGoodsList>");
		for (int i = 0; i < detailList.size(); i++) {
			builder.append("    <VehLoadingGoods>");
			builder.append("    <SeqNo>"+i+1+"</SeqNo>");
			builder.append("    <MawbCode>"+nullToEmpty(detailList.get(i).getMawbNumber())+"</MawbCode>");
			builder.append("    <HawbCode>"+nullToEmpty(detailList.get(i).getHawbNumber())+"</HawbCode>");
			builder.append("    <DocNo>"+nullToEmpty(detailList.get(i).getDocNo())+"</DocNo>");
			builder.append("    <ClassType>"+nullToEmpty(detailList.get(i).getClassType())+"</ClassType>");
			builder.append("    <CargoType>"+nullToEmpty(detailList.get(i).getCargoType())+"</CargoType>");
			builder.append("    <FlightNo>"+nullToEmpty(detailList.get(i).getFlightNo())+"</FlightNo>");
			builder.append("    <FlightDate>"+nullToEmpty(detailList.get(i).getFlightDate())+"</FlightDate>");
			builder.append("    <Destination>"+nullToEmpty(detailList.get(i).getDestination())+"</Destination>");
			builder.append("    <GoodsName>"+nullToEmpty(detailList.get(i).getGoodsName())+"</GoodsName>");
			builder.append("    <GoodsEName>"+nullToEmpty(detailList.get(i).getGoodsEname())+"</GoodsEName>");
			builder.append("    <QuantityQuantity>"+nullToEmpty(detailList.get(i).getPieces())+"</QuantityQuantity>");
			builder.append("    <TotalGrossMassMeasure>"+nullToEmpty(detailList.get(i).getTotalWeight())+"</TotalGrossMassMeasure>");
			builder.append("    <PredictionVolume>"+nullToEmpty(detailList.get(i).getPredictionVolume())+"</PredictionVolume>");
			builder.append("    <HandlingCompany>"+nullToEmpty(detailList.get(i).getHandlingCompanyName())+"</HandlingCompany>");
			String handlingCompanyName=baseMapper.getHandlingCompanyName(detailList.get(i).getHandlingCompanyName(),"搬运公司");
			builder.append("    <HandlingCompanyName>"+nullToEmpty(handlingCompanyName)+"</HandlingCompanyName>");
			builder.append("    <PackageSize>"+nullToEmpty(detailList.get(i).getPackageSize())+"</PackageSize>");
			builder.append("    <BattleName>"+nullToEmpty(detailList.get(i).getBattleName())+"</BattleName>");
			builder.append("    <WareName>"+nullToEmpty(detailList.get(i).getWareName())+"</WareName>");
			builder.append("    <AirlineName>"+nullToEmpty(detailList.get(i).getAirlineName())+"</AirlineName>");
			builder.append("    </VehLoadingGoods>");
		}
		builder.append("  </VehLoadingGoodsList>");
		builder.append("  </VehicleICIssue>");
		builder.append("</VehicleDeliveryOrder>");
		
		
		
		log.info("参数开始"+builder.toString()+"参数结束");
//		ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"EAWB_AMS",builder.toString());
//		String objStr = responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
		bodyMap.put("data",builder.toString());
		String objStr = SendUtils.doSend(config.getUrlPost()+"VE_Appoint",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		JSONObject jsonO = JSONObject.parseObject(objStr);
		String message  = jsonO.getString("messageInfo");
		map.put("status","success");
		map.put("message","发送卡口入区预约成功");
		
		LogBean logBean = new LogBean();
		if("01".equals(jsonO.getString("code"))) {
			baseMapper.updateVEAppoint(entryOrderId,SecurityUtils.getUser().getId(),SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
			logBean.setLogRemark("发送成功");
		}else{
			logBean.setLogRemark("发送失败："+message);
			map.put("message","发送卡口入区预约报文异常：" + message);
			map.put("status","exception");
		}

		return map;
	}
	@Override
	public Map<String, Object> doVEEntryQuery(Integer entryOrderId) {
		Map<String,Object> map = new HashMap<String,Object>();
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.ALL_WORK;
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		String type = APIType.getAPIType(apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		//
		VlEntryOrder vlEntryOrder=baseMapper.getVlEntryOrder(entryOrderId);
		if (vlEntryOrder.getMft8802024MessageId()==null || vlEntryOrder.getMft8802024MessageId().length()==0) {
			throw new CheckedException("MessageId不允许为空");
		}
		StringBuilder builder = new StringBuilder();
		builder.append("<VehicleDeliveryOrder>");
		builder.append("    <Forwarder>"+config.getAppid()+"</Forwarder>");
		builder.append("    <Handler>"+SecurityUtils.getUser().getUserCname()+"</Handler>");
		builder.append("    <Mft8802024MSGID>"+nullToEmpty(vlEntryOrder.getMft8802024MessageId())+"</Mft8802024MSGID>");
		
		builder.append("</VehicleDeliveryOrder>");
		
		
		
		log.info("参数开始"+builder.toString()+"参数结束");
//		ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"EAWB_AMS",builder.toString());
//		String objStr = responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
		bodyMap.put("forwarder",config.getAppid());
		bodyMap.put("Handler",SecurityUtils.getUser().getUserCname());
		bodyMap.put("Mft8802024MSGID",nullToEmpty(vlEntryOrder.getMft8802024MessageId()));
		String objStr = SendUtils.doSend(config.getUrlPost()+"VE_Entry_Query",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		JSONObject jsonO = JSONObject.parseObject(objStr);
		String message  = jsonO.getString("messageInfo");
		map.put("status","success");
		map.put("message","发送卡口入区预约成功");
		String Symbol="";
		String Text="";
		String IC_SEQ="";
		String DeclareDate="";
		LogBean logBean = new LogBean();
		if("01".equals(jsonO.getString("code"))) {
			try {
				List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
				list=JSON.parseObject(jsonO.getJSONObject("data").getString("records"), List.class);
				Symbol=nullToEmpty(list.get(0).get("Symbol"));
				Text=nullToEmpty(list.get(0).get("Text"));
				IC_SEQ=nullToEmpty(list.get(0).get("IC_SEQ"));
				DeclareDate=nullToEmpty(list.get(0).get("DeclareDate"));
			} catch (Exception e) {
			}
			
			logBean.setLogRemark("发送成功");
		}else{
			logBean.setLogRemark("发送失败："+message);
			map.put("message","发送卡口入区预约报文异常：" + message);
			map.put("status","exception");
		}
		map.put("Symbol",Symbol);
		map.put("Text",Text);
		map.put("IC_SEQ",IC_SEQ);
		map.put("DeclareDate",DeclareDate);
		return map;
	}
	private String nullToEmpty(Object str) {
	    if (str == null) {
	            return "";
	    } else {
	        return str.toString().trim();
	    }	
	}


}
