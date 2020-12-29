package com.efreight.afbase.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.SendMapper;
import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.CargoGoodsnames;
import com.efreight.afbase.entity.LogBean;
import com.efreight.afbase.entity.OrgInterface;
import com.efreight.afbase.entity.shipping.APIType;
import com.efreight.afbase.service.LogService;
import com.efreight.afbase.service.SendService;
import com.efreight.afbase.utils.SendUtils;
import com.efreight.afbase.utils.XmlApiUtils;
import com.efreight.common.core.exception.CheckedException;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.SecurityUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentHelper;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
@Slf4j
public class SendServiceImpl extends ServiceImpl<SendMapper, AfOrder> implements SendService {
	private final LogService logService;
	@Override
	public Map<String, Object> doEsdDecleare(String hasMwb, String orderUUID, String letterIds) {
		Map<String,Object> map = new HashMap<String,Object>();
		EUserDetails user = SecurityUtils.getUser();
        String apiType = APIType.AE_ESD_POST_MAWB;
        OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
        if (config == null ) {
			throw new CheckedException("没有配置，请联系管理员");
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
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		if (config == null ) {
			throw new CheckedException("没有配置，请联系管理员");
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
		String apiType = APIType.AE_ESD_POST_MAWB;
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		if (config == null ) {
			throw new CheckedException("没有配置，请联系管理员");
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
	@Override
	public Map<String, Object> doEawbPreDeleteAwb(String AWBNumber,String orderCode) {
		Map<String,Object> map = new HashMap<String,Object>();
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.AE_ESD_POST_MAWB;
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		if (config == null ) {
			throw new CheckedException("没有配置，请联系管理员");
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
		String apiType = APIType.AE_ESD_POST_MAWB;
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		if (config == null ) {
			throw new CheckedException("没有配置，请联系管理员");
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
        OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
        if (config == null ) {
			throw new CheckedException("没有配置，请联系管理员");
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
        OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
        if (config == null ) {
			throw new CheckedException("没有配置，请联系管理员");
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
        OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
        if (config == null ) {
			throw new CheckedException("没有配置，请联系管理员");
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
        	 builder.append(mawbXML);
        }
        if (StringUtils.isNotBlank(hawbXML)) {
        	if (mawbXML.length()>0) {
        		builder= new StringBuilder(builder.toString().replace("</AirwayBill>", 
            		"<ConsolidationList>"+hawbXML.substring(hawbXML.indexOf("<ConsolidationList>")+19, hawbXML.indexOf("</ConsolidationList>"))+"</ConsolidationList>"));
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
		if (config == null ) {
			throw new CheckedException("没有配置，请联系管理员");
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
    public Boolean insertAfLog(LogBean logBean) {
        try{
            String uuid = logBean.getOrderUuid();
            AfOrder bean = baseMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), uuid);
            logBean.setPageName("AE订单");

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
    		AfOrder bean = baseMapper.getOrderNumber(SecurityUtils.getUser().getOrgId(), orderNumber);
    		logBean.setPageName("AE订单");
    		
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
		if (config == null ) {
			throw new CheckedException("没有配置，请联系管理员");
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
	private String nullToEmpty(Object str) {
	    if (str == null) {
	            return "";
	    } else {
	        return str.toString().trim();
	    }	
	}

}
