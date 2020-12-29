package com.efreight.afbase.webService.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.efreight.afbase.dao.AfAwbRouteApiMessageMapper;
import com.efreight.afbase.entity.AfAwbRoute;
import com.efreight.afbase.entity.AfAwbRouteApiMessage;
import com.efreight.afbase.service.AfAwbRouteService;
import com.efreight.afbase.webService.AfAwbRouteWebService;

import cn.hutool.core.util.StrUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;



/**
 * 
 * @author caiwd
 *
 */

@WebService(serviceName = "AfAwbRouteWebService",
        targetNamespace = "http://WebService.afbase.efreight.com", 
        endpointInterface = "com.efreight.afbase.webService.AfAwbRouteWebService") 

@Component
public class AfAwbRouteWebServiceImpl implements AfAwbRouteWebService{
	@Autowired
	private  AfAwbRouteService afAwbRouteService;
	@Autowired
	private AfAwbRouteApiMessageMapper afAwbRouteApiMessageMapper;
	@Autowired
	Configuration configuration;
    
	/**
	 * 扫描未被跟踪的运单 后将订单状态标记成已被跟踪
	 */
	@Override
	public String queryAfAwbRoute(String xml) {
		String content = "";
		Template t;
		try {
//			Document paramXml = DocumentHelper.parseText(xml);
//			Element rootElt = paramXml.getRootElement();
//			String awbNumber = rootElt.elementTextTrim("MawbNum");
			String awbNumberStr = null;
			String[] listParam = null;
//			if(StrUtil.isBlank(awbNumber)) {
//				//主单号为空全表检索
//			}else {
//				awbNumberStr = "1";
//				if(awbNumber.contains(",")) {
//					listParam = awbNumber.split(",");
//				}else {
//					listParam = new String[] {awbNumber};
//				}
//			}
			List<AfAwbRoute> list = afAwbRouteService.queryAfAwbRoute(awbNumberStr, listParam);
			 t = configuration.getTemplate("afAwbRoute.ftl"); 
			Map<String,List<AfAwbRoute>> map = new HashMap<String,List<AfAwbRoute>>();
			map.put("list", list);
			content = FreeMarkerTemplateUtils.processTemplateIntoString(t, map);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				t = configuration.getTemplate("error.ftl");
				Map mapError = new HashMap();
				mapError.put("msg", e.getMessage());
				content = FreeMarkerTemplateUtils.processTemplateIntoString(t, mapError);
			} catch (Exception e1) {
				e1.printStackTrace();
			} 
			
		}
		return content;
	}

	@Override
	public String trackReceipt(String xml) {
		Template t;
		String content = "";
		try {
			Document paramXml = DocumentHelper.parseText(xml);
			Element rootElt = paramXml.getRootElement();
			Iterator iter = rootElt.elementIterator("Mawb");
			List<AfAwbRouteApiMessage> list = new ArrayList<AfAwbRouteApiMessage>();
			while (iter.hasNext()) {
				Element recordEle = (Element) iter.next();
				AfAwbRouteApiMessage routeMsg = new AfAwbRouteApiMessage();
				routeMsg.setIEFlag(recordEle.elementTextTrim("I_E_Flag"));
				routeMsg.setMawbNum(recordEle.elementTextTrim("MawbNum"));//MawbNum 运单号
				routeMsg.setHawb(recordEle.elementTextTrim("Hawb"));//Hawb 分单号
				routeMsg.setErrorState(recordEle.elementTextTrim("ErrorState"));//ErrorState 异常状态代码
				routeMsg.setFlightStatus(recordEle.elementTextTrim("FlightStatus"));//FlightStatus 轨迹类型代码
				routeMsg.setStatusText(recordEle.elementTextTrim("StatusText"));//StatusText 轨迹内容描述
				routeMsg.setMftRecCode(recordEle.elementTextTrim("MftRecCode"));//MftRecCode 海关舱单回执代码
				routeMsg.setDeclareCode(recordEle.elementTextTrim("DeclareCode"));//DeclareCode 报关单单号
				routeMsg.setBatch(recordEle.elementTextTrim("Batch"));//Batch  批次
				routeMsg.setSourceSyscode(recordEle.elementTextTrim("SourceSyscode"));//SourceSyscode  主键
				routeMsg.setFlightNumber(recordEle.elementTextTrim("FlightNumber"));//FlightNumber 航班号/航次号
				routeMsg.setFlightDate(recordEle.elementTextTrim("FlightDate"));//FlightDate 航班日期
				routeMsg.setTakeOffTime(recordEle.elementTextTrim("TakeoffTime"));//TakeoffTime 起飞轨迹（DEP）中的轨迹发生时间
				routeMsg.setLandingTime(recordEle.elementTextTrim("LandingTime"));//LandingTime 降落轨迹中的轨迹发生时间
				routeMsg.setEventDateTime(recordEle.elementTextTrim("EventDateTime"));//EventDateTime 轨迹发生时间
				if(recordEle.elementTextTrim("EventLocationCode")!=null&&recordEle.elementTextTrim("EventLocationCode")!="") {
					if(recordEle.elementTextTrim("EventLocationCode").contains("(")) {
						routeMsg.setEventLocationCode(recordEle.elementTextTrim("EventLocationCode").substring(0,recordEle.elementTextTrim("EventLocationCode").indexOf("(")));
					}else {
						routeMsg.setEventLocationCode(recordEle.elementTextTrim("EventLocationCode")); //EventLocationCode 轨迹发生地点
					}
				}
				if(recordEle.elementTextTrim("Origin")!=null&&recordEle.elementTextTrim("Origin")!="") {
					if(recordEle.elementTextTrim("Origin").contains("(")) {
						routeMsg.setOrigin(recordEle.elementTextTrim("Origin").substring(0,recordEle.elementTextTrim("Origin").indexOf("(")));//Origin  轨迹始发港
					}else {
						routeMsg.setOrigin(recordEle.elementTextTrim("Origin"));//Origin  轨迹始发港
					}
				}
				
				if(recordEle.elementTextTrim("Destination")!=null&&recordEle.elementTextTrim("Destination")!="") {
					if(recordEle.elementTextTrim("Destination").contains("(")) {
						routeMsg.setDestination(recordEle.elementTextTrim("Destination").substring(0,recordEle.elementTextTrim("Destination").indexOf("(")));//Destination 轨迹目的港 
					}else {
						routeMsg.setDestination(recordEle.elementTextTrim("Destination"));//Destination 轨迹目的港 
					}
				}
				if(!StrUtil.isBlank(recordEle.elementTextTrim("ShipmentQuantity"))) {
					routeMsg.setShipmentQuantity(Integer.valueOf(recordEle.elementTextTrim("ShipmentQuantity")).intValue());//ShipmentQuantity 件数
				}
				routeMsg.setQuantityUnit(recordEle.elementTextTrim("QuantityUnit"));//QuantityUnit  件数单位
				if(!StrUtil.isBlank(recordEle.elementTextTrim("GrossWeight"))) {
					routeMsg.setGrossWeight(new BigDecimal(recordEle.elementTextTrim("GrossWeight")).setScale(5));//GrossWeight  轨迹中的货物 毛重
				}
				routeMsg.setGrossWeightUnit(recordEle.elementTextTrim("GrossWeightUnit"));//GrossWeightUnit  毛重单位
                //Transportation
				routeMsg.setFlightRemark(recordEle.elementTextTrim("FlightRemark")); //FlightRemark 备注
				routeMsg.setMessageTransmissionTime(recordEle.elementTextTrim("MessageTransmissionTime"));  //MessageTransmissionTime 获取轨迹时间 
				routeMsg.setCreateTime(LocalDateTime.now());
				if(StrUtil.isNotBlank(recordEle.elementTextTrim("StatusType"))) {
					routeMsg.setStatusType(recordEle.elementTextTrim("StatusType"));
				}
				if(StrUtil.isNotBlank(recordEle.elementTextTrim("PsStatus"))) {
					routeMsg.setPsStatus(recordEle.elementTextTrim("PsStatus"));
				}
//				list.add(routeMsg);
				afAwbRouteApiMessageMapper.insert(routeMsg);
			}
//			this.saveBatch(list);
			t = configuration.getTemplate("success.ftl");
			Map mapError = new HashMap();
			content = FreeMarkerTemplateUtils.processTemplateIntoString(t, mapError);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				t = configuration.getTemplate("error.ftl");
				Map mapError = new HashMap();
				mapError.put("msg", e.getMessage());
				content = FreeMarkerTemplateUtils.processTemplateIntoString(t, mapError);
			} catch (Exception e1) {
				e1.printStackTrace();
			} 
		}
		
		return content;
	}

}
