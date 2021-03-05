package com.efreight.afbase.service.impl;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dom4j.DocumentHelper;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.AfOrderMapper;
import com.efreight.afbase.dao.AfOrderShipperConsigneeMapper;
import com.efreight.afbase.dao.FhlOperationMapper;
import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.AfOrderShipperConsignee;
import com.efreight.afbase.entity.BranchLine;
import com.efreight.afbase.entity.FhlLook;
import com.efreight.afbase.entity.FhlOperation;
import com.efreight.afbase.entity.OperationLook;
import com.efreight.afbase.entity.OrgInterface;
import com.efreight.afbase.entity.shipping.APIType;
import com.efreight.afbase.service.FhlOperationService;
import com.efreight.afbase.utils.SendUtils;
import com.efreight.afbase.utils.XmlApiUtils;
import com.efreight.common.core.exception.CheckedException;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.SecurityUtils;
import com.sun.org.apache.xpath.internal.XPathAPI;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@AllArgsConstructor
@Slf4j
public class FhlOperationServiceImpl extends ServiceImpl<FhlOperationMapper, AfOrder> implements FhlOperationService {
	private final AfOrderMapper aiMapper;
	private final AfOrderShipperConsigneeMapper afOrderShipperConsigneeMapper;
	@Override
	public Map<String,Object> queryList(Integer currentPage, Integer pageSize,FhlOperation bean){
		//设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
    	if(currentPage==null||currentPage==0)
    		currentPage = 1;
    	if(pageSize==null||pageSize==0)
    		pageSize = 10;
    	EUserDetails user = SecurityUtils.getUser();
        String apiType = APIType.AE_CD_IMP_HAWB;
		String type = APIType.getAPIType(apiType);
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
        if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
    	List<FhlOperation> list = new ArrayList<FhlOperation>();

    	StringBuilder builder = new StringBuilder();
    	builder.append("<Service>");
    	builder.append("  <ServiceURL>AirwayBill_Imp_AdapterForNKG</ServiceURL>");
    	builder.append("  <ServiceAction>QueryImportAirWayBillStatus</ServiceAction>");
    	builder.append("  <ServiceData>");
    	builder.append("    <Forwarder>"+config.getAppid()+"</Forwarder>");
//    	builder.append("    <Forwarder>CTSNKG</Forwarder>");
    	builder.append("    <Handler></Handler>");
    	builder.append("    <MawbCode>"+nullToEmpty(bean.getAwbnumber())+"</MawbCode>");
    	builder.append("    <FlightNo>"+nullToEmpty(bean.getFlightno())+"</FlightNo>");
    	builder.append("    <StartDate>"+nullToEmpty(bean.getStartdate())+"</StartDate>");
    	builder.append("    <EndDate>"+nullToEmpty(bean.getEnddate())+"</EndDate>");
    	builder.append("	<PageSize>"+pageSize+"</PageSize>");
    	builder.append("	<CurrentPage>"+currentPage+"</CurrentPage>");
    	builder.append("  </ServiceData>");
    	builder.append("</Service>");
    	log.info("参数开始"+builder.toString()+"参数结束");
//		ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"",builder.toString());
//		String  objStr= responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
        bodyMap.put("serviceXml",builder.toString());
        String objStr = SendUtils.doSend(config.getUrlPost()+"",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
    	long countNums=0;//总记录数
    	try {
    		org.dom4j.Document document = DocumentHelper.parseText(objStr);
			org.dom4j.Element rootElement = document.getRootElement();
    		countNums=Long.parseLong(rootElement.element("ResultCount").getText());
    		
    		Document document2=XmlApiUtils.parseXML(objStr,false);
    		NodeList nodelist = XPathAPI.selectNodeList(document2, "//ImportAirWayBillStatus");
			if(nodelist!=null){
				for(int i=0;i<nodelist.getLength();i++){
					Node node = nodelist.item(i);
					
					
					FhlOperation fhl=new FhlOperation();
					fhl.setSyscode(XmlApiUtils.getNodeText(node, "syscode"));
					fhl.setAwbnumber(XmlApiUtils.getNodeText(node, "awbnumber"));
					fhl.setHwbnumber(XmlApiUtils.getNodeText(node, "hwbnumber"));
					fhl.setNumberid(XmlApiUtils.getNodeText(node, "numberid"));
					fhl.setInputdate(XmlApiUtils.getNodeText(node, "inputdate"));
					fhl.setCreatedate(XmlApiUtils.getNodeText(node, "createdate"));
					fhl.setBilltype(XmlApiUtils.getNodeText(node, "billtype"));
					fhl.setTotalpiecequantity(XmlApiUtils.getNodeText(node, "totalpiecequantity"));
					fhl.setPiecequantity(XmlApiUtils.getNodeText(node, "piecequantity"));
					fhl.setTotalgrossweight(XmlApiUtils.getNodeText(node, "totalgrossweight"));
					fhl.setTotalvolumnamount(XmlApiUtils.getNodeText(node, "totalvolumnamount"));
					fhl.setFlightno(XmlApiUtils.getNodeText(node, "flightno"));
					fhl.setFlightdate(XmlApiUtils.getNodeText(node, "flightdate"));
					fhl.setDeparture(XmlApiUtils.getNodeText(node, "departure"));
					fhl.setDestination(XmlApiUtils.getNodeText(node, "destination"));
					fhl.setGoodsname(XmlApiUtils.getNodeText(node, "goodsname"));
					fhl.setForwarder(XmlApiUtils.getNodeText(node, "forwarder"));
					fhl.setOrigin(XmlApiUtils.getNodeText(node, "origin"));
					fhl.setAwbtype(XmlApiUtils.getNodeText(node, "awbtype"));
					fhl.setAwbtypename(XmlApiUtils.getNodeText(node, "awbtypename"));
					fhl.setBusinesstype(XmlApiUtils.getNodeText(node, "businesstype"));
					fhl.setBusinessname(XmlApiUtils.getNodeText(node, "businessname"));
					fhl.setIsediawb(XmlApiUtils.getNodeText(node, "isediawb"));
					fhl.setMftstatus(XmlApiUtils.getNodeText(node, "mftstatus"));
					fhl.setMftresponse(XmlApiUtils.getNodeText(node, "mftresponse"));
					fhl.setTallystatus(XmlApiUtils.getNodeText(node, "tallystatus"));
					fhl.setTallyresponse(XmlApiUtils.getNodeText(node, "tallyresponse"));
					fhl.setShpcode(XmlApiUtils.getNodeText(node, "shpcode"));
					fhl.setShipper(XmlApiUtils.getNodeText(node, "shipper"));
					fhl.setShpaddress(XmlApiUtils.getNodeText(node, "shpaddress"));
					fhl.setShpcountrycode(XmlApiUtils.getNodeText(node, "shpcountrycode"));
					fhl.setShptelephone(XmlApiUtils.getNodeText(node, "shptelephone"));
					fhl.setCnecode(XmlApiUtils.getNodeText(node, "cnecode"));
					fhl.setConsignee(XmlApiUtils.getNodeText(node, "consignee"));
					fhl.setCneaddress(XmlApiUtils.getNodeText(node, "cneaddress"));
					fhl.setCnecountrycode(XmlApiUtils.getNodeText(node, "cnecountrycode"));
					fhl.setCnetelephone(XmlApiUtils.getNodeText(node, "cnetelephone"));
					fhl.setCnecontactname(XmlApiUtils.getNodeText(node, "cnecontactname"));
					fhl.setCnecontacttelephone(XmlApiUtils.getNodeText(node, "cnecontacttelephone"));
					fhl.setNfycode(XmlApiUtils.getNodeText(node, "nfycode"));
					fhl.setNfyname(XmlApiUtils.getNodeText(node, "nfyname"));
					fhl.setNfyaddress(XmlApiUtils.getNodeText(node, "nfyaddress"));
					fhl.setNfytelephone(XmlApiUtils.getNodeText(node, "nfytelephone"));
					fhl.setIsmftdeclare(XmlApiUtils.getNodeText(node, "ismftdeclare"));
					
					fhl.setIstallydeclare(XmlApiUtils.getNodeText(node, "istallydeclare"));
					fhl.setDeclaredate(XmlApiUtils.getNodeText(node, "declaredate"));
					fhl.setCarrierid(XmlApiUtils.getNodeText(node, "carrierid"));
					fhl.setArrivaldatetime(XmlApiUtils.getNodeText(node, "arrivaldatetime"));
					fhl.setDeparturedatetime(XmlApiUtils.getNodeText(node, "departuredatetime"));
					fhl.setLoadingdate(XmlApiUtils.getNodeText(node, "loadingdate"));
					fhl.setArrivaldate(XmlApiUtils.getNodeText(node, "arrivaldate"));
					fhl.setMethodcode(XmlApiUtils.getNodeText(node, "methodcode"));
					fhl.setTranshipmentlocationid(XmlApiUtils.getNodeText(node, "transhipmentlocationid"));
					fhl.setTransitdestinationid(XmlApiUtils.getNodeText(node, "transitdestinationid"));
					fhl.setTransportsplitindicator(XmlApiUtils.getNodeText(node, "transportsplitindicator"));
					list.add(fhl);	
				}
			}
    	}catch (Exception e) {
    		
    	}
        Map<String,Object> rerultMap=new HashMap<String,Object>();
        rerultMap.put("totalNum", countNums);
        rerultMap.put("dataList", list);
        return rerultMap;
	}
	@Override
	public Map<String,Object> queryLineList(Integer currentPage, Integer pageSize,BranchLine bean){
		//设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
		if(currentPage==null||currentPage==0)
			currentPage = 1;
		if(pageSize==null||pageSize==0)
			pageSize = 10;
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.AE_CD_IMP_HAWB;
		String type = APIType.getAPIType(apiType);
		
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		List<BranchLine> list = new ArrayList<BranchLine>();
		
		StringBuilder builder = new StringBuilder();
		builder.append("<Service>");
		builder.append("  <ServiceURL>ConsolidationList_Exp_Adapter</ServiceURL>");
		builder.append("  <ServiceAction>GetExpBranchData</ServiceAction>");
		builder.append("  <ServiceData>");
		builder.append("  <AirwayBill>");
		builder.append("    <Forwarder>"+config.getAppid()+"</Forwarder>");
//    	builder.append("    <Forwarder>CTSNKG</Forwarder>");
		builder.append("    <Handler>"+config.getAppid()+"</Handler>");
		builder.append("    <AWBNumber>"+nullToEmpty(bean.getAwbnumber())+"</AWBNumber>");
		builder.append("    <HWBNumber>"+nullToEmpty(bean.getHwbnumber())+"</HWBNumber>");
		builder.append("    <BeginDate>"+nullToEmpty(bean.getBegindate())+"</BeginDate>");
		builder.append("    <EndDate>"+nullToEmpty(bean.getEnddate())+"</EndDate>");
		builder.append("	<PageSize>"+pageSize+"</PageSize>");
		builder.append("	<CurrentPage>"+currentPage+"</CurrentPage>");
		builder.append("  </AirwayBill>");
		builder.append("  </ServiceData>");
		builder.append("</Service>");
		log.info("参数开始"+builder.toString()+"参数结束");
//		ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"",builder.toString());
//		String  objStr= responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
        bodyMap.put("serviceXml",builder.toString());
        String objStr = SendUtils.doSend(config.getUrlPost()+"",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		long countNums=0;//总记录数
		try {
			org.dom4j.Document document = DocumentHelper.parseText(objStr);
			org.dom4j.Element rootElement = document.getRootElement();
			countNums=Long.parseLong(rootElement.element("ResultCount").getText());
			
			Document document2=XmlApiUtils.parseXML(objStr,false);
			NodeList nodelist = XPathAPI.selectNodeList(document2, "//AirwayBill");
			if(nodelist!=null){
				for(int i=0;i<nodelist.getLength();i++){
					Node node = nodelist.item(i);

					BranchLine fhl=new BranchLine();
//					fhl.setSyscode(XmlApiUtils.getNodeText(node, "syscode"));
					fhl.setAwbnumber(XmlApiUtils.getNodeText(node, "awbnumber"));
					fhl.setHwbnumber(XmlApiUtils.getNodeText(node, "hwbnumber"));
					fhl.setNumberid(XmlApiUtils.getNodeText(node, "numberid"));
					fhl.setRearchid(XmlApiUtils.getNodeText(node, "rearchid"));
					fhl.setTotalpiecequantity(XmlApiUtils.getNodeText(node, "totalpiecequantity"));
					fhl.setTotalgrossweight(XmlApiUtils.getNodeText(node, "totalgrossweight"));
					fhl.setTotalvolumnamount(XmlApiUtils.getNodeText(node, "totalvolumnamount"));
					fhl.setFlightno(XmlApiUtils.getNodeText(node, "flightno"));
					fhl.setFlightdate(XmlApiUtils.getNodeText(node, "flightdate"));
					fhl.setDeparture(XmlApiUtils.getNodeText(node, "departure"));
					fhl.setDestination(XmlApiUtils.getNodeText(node, "destination"));
					fhl.setGoodsname(XmlApiUtils.getNodeText(node, "goodsname"));
					fhl.setGoodscnname(XmlApiUtils.getNodeText(node, "goodscnname"));
					fhl.setSpecialgoodscode(XmlApiUtils.getNodeText(node, "specialgoodscode"));
					fhl.setCustomscode(XmlApiUtils.getNodeText(node, "customscode"));
					fhl.setTransportmode(XmlApiUtils.getNodeText(node, "transportmode"));
					fhl.setFreightpaymentmethod(XmlApiUtils.getNodeText(node, "freightpaymentmethod"));
					fhl.setCneecity(XmlApiUtils.getNodeText(node, "cneecity"));
					fhl.setCneecountry(XmlApiUtils.getNodeText(node, "cneecountry"));
					fhl.setMft2201status(XmlApiUtils.getNodeText(node, "mft2201status"));
					fhl.setMft3201status(XmlApiUtils.getNodeText(node, "mft3201status"));
					fhl.setMft9999status(XmlApiUtils.getNodeText(node, "mft9999status"));
					fhl.setMft4201status(XmlApiUtils.getNodeText(node, "mft4201status"));
					fhl.setMft5202status(XmlApiUtils.getNodeText(node, "mft5202status"));
					fhl.setResponse(XmlApiUtils.getNodeText(node, "response"));
					
					list.add(fhl);	
				}
			}
		}catch (Exception e) {
			
		}
		Map<String,Object> rerultMap=new HashMap<String,Object>();
		rerultMap.put("totalNum", countNums);
		rerultMap.put("dataList", list);
		return rerultMap;
	}
	@Override
	public Map<String,Object> queryLookList(Integer currentPage, Integer pageSize,OperationLook bean){
		//设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
		if(currentPage==null||currentPage==0)
			currentPage = 1;
		if(pageSize==null||pageSize==0)
			pageSize = 10;
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.ALL_WORK;
		String type = APIType.getAPIType(apiType);
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		List<OperationLook> list = new ArrayList<OperationLook>();
 
        HashMap<String, String> bodyMap=new HashMap<String, String>();
        bodyMap.put("size",""+pageSize);
        bodyMap.put("current",""+currentPage);
        bodyMap.put("forwarder",config.getAppid());
        
        bodyMap.put("mawbCode",bean.getMawbCode());
        bodyMap.put("hawbCode",bean.getHawbCode());
        bodyMap.put("orderCode",bean.getOrderCode());
        bodyMap.put("createTimeStart",bean.getCreateTimeStart());
        bodyMap.put("createTimeEnd",bean.getCreateTimeEnd());
        bodyMap.put("mft2201Status",bean.getMft2201Status());
        
//        ResponseEntity<String> responseEntity = sendByMap(config.getUrlPost(),"DashboardM",bodyMap);
//
//		String  objStr= responseEntity.getBody();
        String objStr = SendUtils.doSend(config.getUrlPost()+"DashboardM",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		long countNums=0;//总记录数
		JSONObject jsonO = JSONObject.parseObject(objStr);
        String code  = jsonO.getString("code");
        String message  = jsonO.getString("messageInfo");
        Map<String,Object> rerultMap=new HashMap<String,Object>();
        if ("01".equals(code)) {
        	list=JSON.parseObject(jsonO.getJSONObject("data").getString("records"), List.class);
        	countNums=Long.parseLong(jsonO.getJSONObject("data").getString("total"));
		}
        List<OperationLook> resultlist = new ArrayList<OperationLook>();
		for (int i = 0; i < list.size(); i++) {
			List<FhlLook> fhlList = new ArrayList<FhlLook>();
			OperationLook look=JSON.parseObject(JSONObject.toJSONString(list.get(i)),OperationLook.class);
			
			 HashMap<String, String> fhlMap=new HashMap<String, String>();
			 fhlMap.put("forwarder",config.getAppid());		        
			 fhlMap.put("mawbCode",look.getMawbCode());
//			 ResponseEntity<String> fhlEntity = sendByMap(config.getUrlPost(),"DashboardH",fhlMap);
//			 
//			 String  fhlStr= fhlEntity.getBody();
			 String fhlStr = SendUtils.doSend(config.getUrlPost()+"DashboardH",fhlMap);
			 JSONObject jsonfhl = JSONObject.parseObject(fhlStr);
			 String fhlcode  = jsonfhl.getString("code");
			 if ("01".equals(fhlcode)) {
				 fhlList=JSON.parseObject(jsonfhl.getJSONObject("data").getString("records"), List.class);
				 look.setFhlLook(fhlList);
			}
			 resultlist.add(look);
		}
		rerultMap.put("totalNum", countNums);
		rerultMap.put("dataList", resultlist);
		rerultMap.put("code", code);
		rerultMap.put("message", message);
		return rerultMap;
	}

	/**
	 * 日志查询--操作看板
	 * @param bean
	 * @return
	 */
	@Override
	public Map<String,Object> queryLogList(OperationLook bean){
		
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.ALL_WORK;
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		String type = APIType.getAPIType(apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		
		HashMap<String, String> bodyMap=new HashMap<String, String>();
		bodyMap.put("size","9999");
	    bodyMap.put("current","1");
		bodyMap.put("forwarder",config.getAppid());		
		bodyMap.put("mawbCode",bean.getMawbCode());
		bodyMap.put("hawbCode",bean.getHawbCode());

		String objStr = SendUtils.doSend(config.getUrlPost()+"DashboardStatusLog",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		long countNums=0;//总记录数
		JSONObject jsonO = JSONObject.parseObject(objStr);
		String code  = jsonO.getString("code");
		String message  = jsonO.getString("messageInfo");
		Map<String,Object> rerultMap=new HashMap<String,Object>();
		if ("01".equals(code)) {
			list=JSON.parseObject(jsonO.getJSONObject("data").getString("records"), List.class);
			countNums=Long.parseLong(jsonO.getJSONObject("data").getString("total"));
		}
		
		rerultMap.put("totalNum", countNums);
		rerultMap.put("dataList", list);
		rerultMap.put("code", code);
		rerultMap.put("message", message);
		return rerultMap;
	}

	/**
	 *状态查询--操作看板
	 * @param bean
	 * @return
	 */
	@Override
	public Map<String,Object> queryStatus(OperationLook bean){
		
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.ALL_WORK;
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		String type = APIType.getAPIType(apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		
		HashMap<String, String> bodyMap=new HashMap<String, String>();
		bodyMap.put("size","9999");
	    bodyMap.put("current","1");
		bodyMap.put("forwarder",config.getAppid());		
		bodyMap.put("mawbCode",bean.getMawbCode());
		bodyMap.put("hawbCode",bean.getHawbCode());
		
		String objStr = SendUtils.doSend(config.getUrlPost()+"Query_MftRecv",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		long countNums=0;//总记录数
		JSONObject jsonO = JSONObject.parseObject(objStr);
		String code  = jsonO.getString("code");
		String message  = jsonO.getString("messageInfo");
		Map<String,Object> rerultMap=new HashMap<String,Object>();
		if ("01".equals(code)) {
			list=JSON.parseObject(jsonO.getJSONObject("data").getString("records"), List.class);
			countNums=Long.parseLong(jsonO.getJSONObject("data").getString("total"));
		}
		List<Map<String,Object>> MT2201List = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> MT3201List = new ArrayList<Map<String,Object>>();
		for (int i = 0; i < list.size(); i++) {
			Map<String,Object> lookBean=list.get(i);
			if ("MT2201".equals(lookBean.get("messageType"))) {
				MT2201List.add(lookBean);
			} else {
				MT3201List.add(lookBean);
			}
		}
		rerultMap.put("totalNum", countNums);
		rerultMap.put("MT2201List", MT2201List);
		rerultMap.put("MT3201List", MT3201List);
		rerultMap.put("code", code);
		rerultMap.put("message", message);
		return rerultMap;
	}
	private String nullToEmpty(Object str) {
	    if (str == null) {
	            return "";
	    } else {
	        return str.toString().trim();
	    }	
	}
//	private ResponseEntity<String> sendToThirdUrl(String url,String method, String data) {
//        RestTemplate restTemplate = new RestTemplate();
//        List<HttpMessageConverter<?>> httpMessageConverters = restTemplate.getMessageConverters();
//        httpMessageConverters.stream().forEach(httpMessageConverter -> {
//            if(httpMessageConverter instanceof StringHttpMessageConverter){
//                StringHttpMessageConverter messageConverter = (StringHttpMessageConverter) httpMessageConverter;
//                messageConverter.setDefaultCharset(Charset.forName("UTF-8"));
//            }
//        });
//        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("serviceXml",data);
//        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url+method, body, String.class);
//
//        return responseEntity;
//    }
//	private ResponseEntity<String> sendByMap(String url,String method, HashMap<String, String> bodyMap) {
//		RestTemplate restTemplate = new RestTemplate();
//		LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//		for (String bodykey : bodyMap.keySet()) {
//			body.add(bodykey,bodyMap.get(bodykey));
//            
//        }
//		ResponseEntity<String> responseEntity = restTemplate.postForEntity(url+method, body, String.class);
//		
//		return responseEntity;
//	}
	@Override
	public Boolean doSave(FhlOperation bean) {
		EUserDetails user = SecurityUtils.getUser();
        String apiType = APIType.AE_CD_IMP_HAWB;
        OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		String type = APIType.getAPIType(apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
        StringBuilder builder = new StringBuilder();
    	builder.append("<Service>");
    	builder.append("  <ServiceURL>AirwayBill_Imp_AdapterForNKG</ServiceURL>");
    	builder.append("  <ServiceAction>AddImportAirWayBillInfo</ServiceAction>");
    	builder.append("  <ServiceData>");
    	builder.append("  <ImportAirWayBillInfo>");
    	builder.append("    <Forwarder>"+config.getAppid()+"</Forwarder>");
    	builder.append("    <SYSCODE>"+nullToEmpty(bean.getSyscode())+"</SYSCODE>");
//    	builder.append("    <Forwarder>CTSNKG</Forwarder>");
    	builder.append("    <Handler>"+user.getUserCname()+"</Handler>");
    	builder.append("    <MawbCode>"+nullToEmpty(bean.getAwbnumber())+"</MawbCode>");
    	builder.append("    <HawbCode>"+nullToEmpty(bean.getHwbnumber())+"</HawbCode>");
    	builder.append("    <NumberID>"+nullToEmpty(bean.getNumberid())+"</NumberID>");
    	builder.append("    <FlightNo>"+nullToEmpty(bean.getFlightno())+"</FlightNo>");
    	builder.append("    <FlightDate>"+nullToEmpty(bean.getFlightdate())+"</FlightDate>");
    	
    	builder.append("    <OperationType></OperationType>");
    	builder.append("    <TotalPieceQuantity>"+nullToEmpty(bean.getTotalpiecequantity())+"</TotalPieceQuantity>");
    	builder.append("    <TotalGrossweight>"+nullToEmpty(bean.getTotalgrossweight())+"</TotalGrossweight>");
    	builder.append("    <TotalVolumnAmount>"+nullToEmpty(bean.getTotalvolumnamount())+"</TotalVolumnAmount>");
    	builder.append("    <Departure>"+nullToEmpty(bean.getDeparture())+"</Departure>");
    	builder.append("    <Destination>"+nullToEmpty(bean.getDestination())+"</Destination>");
    	builder.append("    <Origin>"+nullToEmpty(bean.getOrigin())+"</Origin>");
    	builder.append("    <CarrierID>"+nullToEmpty(bean.getCarrierid())+"</CarrierID>");
    	builder.append("    <ArrivalDateTime>"+nullToEmpty(bean.getArrivaldatetime())+"</ArrivalDateTime>");
    	builder.append("    <DepartureDateTime>"+nullToEmpty(bean.getDeparturedatetime())+"</DepartureDateTime>");
    	
    	builder.append("    <LoadingDate>"+nullToEmpty(bean.getLoadingdate())+"</LoadingDate>");
    	builder.append("    <ArrivalDate>"+nullToEmpty(bean.getArrivaldate())+"</ArrivalDate>");
    	builder.append("    <MethodCode>"+nullToEmpty(bean.getMethodcode())+"</MethodCode>");
    	builder.append("    <TranshipmentLocationID>"+nullToEmpty(bean.getTranshipmentlocationid())+"</TranshipmentLocationID>");
    	builder.append("    <TransitDestinationID>"+nullToEmpty(bean.getTransitdestinationid())+"</TransitDestinationID>");
    	builder.append("    <ItemQuantityQuantity>"+nullToEmpty(bean.getTotalpiecequantity())+"</ItemQuantityQuantity>");
    	builder.append("    <ItemGrossmassMeasure>"+nullToEmpty(bean.getTotalgrossweight())+"</ItemGrossmassMeasure>");
    	builder.append("    <BusinessType>"+nullToEmpty(bean.getBusinesstype())+"</BusinessType>");
    	builder.append("    <SHPCode>"+nullToEmpty(bean.getShpcode())+"</SHPCode>");
    	builder.append("    <Shipper>"+nullToEmpty(bean.getShipper())+"</Shipper>");
    	
    	builder.append("    <SHPAddress>"+nullToEmpty(bean.getShpaddress())+"</SHPAddress>");
    	builder.append("    <SHPCountryCode>"+nullToEmpty(bean.getShpcountrycode())+"</SHPCountryCode>");
    	builder.append("    <SHPTelephone>"+nullToEmpty(bean.getShptelephone())+"</SHPTelephone>");
    	builder.append("    <CNECode>"+nullToEmpty(bean.getCnecode())+"</CNECode>");
    	builder.append("    <Consignee>"+nullToEmpty(bean.getConsignee())+"</Consignee>");
    	builder.append("    <CNEAddress>"+nullToEmpty(bean.getCneaddress())+"</CNEAddress>");
    	builder.append("    <CNECountryCode>"+nullToEmpty(bean.getCnecountrycode())+"</CNECountryCode>");
    	builder.append("    <CNETelephone>"+nullToEmpty(bean.getCnetelephone())+"</CNETelephone>");
    	builder.append("    <CNEContactName>"+nullToEmpty(bean.getCnecontactname())+"</CNEContactName>");
    	builder.append("    <CNEContactTelephone>"+nullToEmpty(bean.getCnecontacttelephone())+"</CNEContactTelephone>");
    	
    	builder.append("    <NFYCode>"+nullToEmpty(bean.getNfycode())+"</NFYCode>");
    	builder.append("    <NFYName>"+nullToEmpty(bean.getNfyname())+"</NFYName>");
    	builder.append("    <NFYAddress>"+nullToEmpty(bean.getNfyaddress())+"</NFYAddress>");
    	builder.append("    <NFYTelephone>"+nullToEmpty(bean.getNfytelephone())+"</NFYTelephone>");
    	builder.append("    <TransportSplitIndicator>"+nullToEmpty(bean.getTransportsplitindicator())+"</TransportSplitIndicator>");
    	builder.append("    <CargoDescription>"+nullToEmpty(bean.getGoodsname())+"</CargoDescription>");
    	
    	builder.append("  </ImportAirWayBillInfo>");
    	builder.append("  </ServiceData>");
    	builder.append("</Service>");
    	log.info("参数开始"+builder.toString()+"参数结束");
//		ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"",builder.toString());
//		String  objStr= responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
        bodyMap.put("serviceXml",builder.toString());
        String objStr = SendUtils.doSend(config.getUrlPost()+"",bodyMap);
		log.info("结果开始"+objStr+"结果结束");

		HashMap<String,String> resultMap = getResultData(objStr);

		if ("1".equals(resultMap.get("ResultCode"))|| "010601".equals(resultMap.get("ResultCode"))) {
			//af_order
			String AwbNumber=bean.getAwbnumber().substring(0,3)+"-"+bean.getAwbnumber().substring(3);
			AfOrder order=baseMapper.getAiOrder(user.getOrgId(),AwbNumber,bean.getHwbnumber());
			if (order==null) {
				order=new AfOrder();
				order.setOrderStatus("订单创建");
				//生成订单号
		        String code = getAICode();
		        List<AfOrder> codeList = aiMapper.selectCode(user.getOrgId(), code);
		        if (codeList.size() == 0) {
		        	order.setOrderCode(code + "0001");
		        } else {
		        	if ((code + "9999").equals(codeList.get(0).getOrderCode())) {
		        		throw new CheckedException("每天最多可以创建9999个AI订单");
		            } else {
		                String str = codeList.get(0).getOrderCode();
		                str = str.substring(str.length() - 4);
		                order.setOrderCode(code + String.format("%04d", Integer.parseInt(str) + 1));
		            }
		            
		        }
		        order.setCustomerNumber(order.getOrderCode());
		        order.setRowUuid(UUID.randomUUID().toString());
		        order.setOrderUuid(aiMapper.getUUID());
		        order.setCreateTime(new Date());
		        order.setCreatorId(SecurityUtils.getUser().getId());
		        order.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
		        order.setOrgId(SecurityUtils.getUser().getOrgId());
		        order.setBusinessScope("AI");
		        AfOrderShipperConsignee afOrderShipperConsignee=new AfOrderShipperConsignee();
		        
		        afOrderShipperConsignee.setScType(0);
		        afOrderShipperConsignee.setScName(bean.getShipper());
		        afOrderShipperConsignee.setScAddress(bean.getShpaddress());
		        afOrderShipperConsignee.setNationCode(bean.getShpcountrycode());
		        afOrderShipperConsignee.setTelNumber(bean.getShptelephone());
		        
		        AfOrderShipperConsignee afOrderShipperConsignee2=new AfOrderShipperConsignee();
		        afOrderShipperConsignee2.setScType(1);
		        afOrderShipperConsignee2.setScName(bean.getConsignee());
		        afOrderShipperConsignee2.setScAddress(bean.getCneaddress());
		        afOrderShipperConsignee2.setNationCode(bean.getCnecountrycode());
		        afOrderShipperConsignee2.setTelNumber(bean.getCnetelephone());
		        
		        
		        order.setAwbNumber(AwbNumber);
		        order.setHawbNumber(bean.getHwbnumber());
		        order.setExpectFlight(bean.getFlightno());
		        order.setExpectArrival(LocalDate.parse(bean.getFlightdate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		        order.setDepartureStation(bean.getDeparture());
		        order.setArrivalStation(bean.getDestination());
		        order.setGoodsNameEn(bean.getGoodsname());
		        order.setPlanPieces(Integer.parseInt(bean.getTotalpiecequantity()));
		        order.setPlanWeight(new BigDecimal(bean.getTotalgrossweight()));
		        order.setPlanVolume(Double.parseDouble(bean.getTotalvolumnamount()));
		        order.setPlanChargeWeight(this.getPlanChargeWeight(bean.getTotalgrossweight(),bean.getTotalvolumnamount()));
		        order.setSalesId(user.getId());
		        order.setServicerId(user.getId());
		        aiMapper.insert(order);
		        
//		        AfOrderShipperConsignee afOrderShipperConsignee = order.getAfOrderShipperConsignee1();
		        if (afOrderShipperConsignee != null) {
		            afOrderShipperConsignee.setOrderId(order.getOrderId());
		            afOrderShipperConsignee.setCreateTime(LocalDateTime.now());
		            afOrderShipperConsignee.setCreatorId(user.getId());
		            afOrderShipperConsignee.setCreatorName(user.getUserCname() + " " + user.getUserEmail());
		            afOrderShipperConsignee.setOrgId(user.getOrgId());
		            
		            afOrderShipperConsignee.setScPrintRemark(this.getPrintRemark(afOrderShipperConsignee));
		            
		            afOrderShipperConsigneeMapper.insert(afOrderShipperConsignee);
		        }
//		        AfOrderShipperConsignee afOrderShipperConsignee2 = order.getAfOrderShipperConsignee2();
		        if (afOrderShipperConsignee2 != null) {
		            afOrderShipperConsignee2.setOrderId(order.getOrderId());
		            afOrderShipperConsignee2.setCreateTime(LocalDateTime.now());
		            afOrderShipperConsignee2.setCreatorId(user.getId());
		            afOrderShipperConsignee2.setCreatorName(user.getUserCname() + " " + user.getUserEmail());
		            afOrderShipperConsignee2.setOrgId(user.getOrgId());
		            afOrderShipperConsignee2.setScPrintRemark(this.getPrintRemark(afOrderShipperConsignee2));
		            afOrderShipperConsigneeMapper.insert(afOrderShipperConsignee2);
		        }
			}else {
				order.setEditTime(new Date());
				order.setEditorId(user.getId());
				order.setEditorName(user.getUserCname() + " " + user.getUserEmail());
				order.setOrgId(user.getOrgId());
				order.setRowUuid(UUID.randomUUID().toString());
		        
				order.setGoodsNameEn(bean.getGoodsname());
		        order.setPlanPieces(Integer.parseInt(bean.getTotalpiecequantity()));
		        order.setPlanWeight(new BigDecimal(bean.getTotalgrossweight()));
		        order.setPlanVolume(Double.parseDouble(bean.getTotalvolumnamount()));
		        order.setPlanChargeWeight(this.getPlanChargeWeight(bean.getTotalgrossweight(),bean.getTotalvolumnamount()));
		        order.setSalesId(user.getId());
		        order.setServicerId(user.getId());
		        aiMapper.updateById(order);
		        
		        AfOrderShipperConsignee afOrderShipperConsignee = baseMapper.getShipperConsignee(user.getOrgId(),order.getOrderId(),0);
		        if (afOrderShipperConsignee != null) {
		            afOrderShipperConsignee.setEditTime(LocalDateTime.now());
		            afOrderShipperConsignee.setEditorId(user.getId());
		            afOrderShipperConsignee.setEditorName(user.getUserCname() + " " + user.getUserEmail());

		            afOrderShipperConsignee.setScName(bean.getShipper());
			        afOrderShipperConsignee.setScAddress(bean.getShpaddress());
			        afOrderShipperConsignee.setNationCode(bean.getShpcountrycode());
			        afOrderShipperConsignee.setTelNumber(bean.getShptelephone());
			        afOrderShipperConsignee.setScPrintRemark(this.getPrintRemark(afOrderShipperConsignee));
		            afOrderShipperConsigneeMapper.updateById(afOrderShipperConsignee);
		        }
		        AfOrderShipperConsignee afOrderShipperConsignee2 = baseMapper.getShipperConsignee(user.getOrgId(),order.getOrderId(),1);
		        if (afOrderShipperConsignee2 != null) {
		            afOrderShipperConsignee2.setEditTime(LocalDateTime.now());
		            afOrderShipperConsignee2.setEditorId(user.getId());
		            afOrderShipperConsignee2.setEditorName(user.getUserCname() + " " + user.getUserEmail());
		            
		            afOrderShipperConsignee2.setScName(bean.getConsignee());
			        afOrderShipperConsignee2.setScAddress(bean.getCneaddress());
			        afOrderShipperConsignee2.setNationCode(bean.getCnecountrycode());
			        afOrderShipperConsignee2.setTelNumber(bean.getCnetelephone());
			        afOrderShipperConsignee2.setScPrintRemark(this.getPrintRemark(afOrderShipperConsignee2));
		            afOrderShipperConsigneeMapper.updateById(afOrderShipperConsignee2);
		        }
			}
		}else {
			throw new CheckedException(resultMap.get("ResultContent"));
		}
		return true;
	}
	
	@Override
	public Boolean doDelete(FhlOperation bean) {
		EUserDetails user = SecurityUtils.getUser();
        String apiType = APIType.AE_CD_IMP_HAWB;
		String type = APIType.getAPIType(apiType);
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
        if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
        StringBuilder builder = new StringBuilder();
    	builder.append("<Service>");
    	builder.append("  <ServiceURL>AirwayBill_Imp_AdapterForNKG</ServiceURL>");
    	builder.append("  <ServiceAction>DeleteImportAirWayBillInfo</ServiceAction>");
    	builder.append("  <ServiceData>");
    	builder.append("    <Forwarder>"+config.getAppid()+"</Forwarder>");
    	builder.append("    <SYSCODE>"+nullToEmpty(bean.getSyscode())+"</SYSCODE>");
//    	builder.append("    <Forwarder>CTSNKG</Forwarder>");
    	builder.append("    <Handler>"+user.getUserCname()+"</Handler>");
    	builder.append("    <MawbCode>"+nullToEmpty(bean.getAwbnumber())+"</MawbCode>");
    	builder.append("    <HawbCode>"+nullToEmpty(bean.getHwbnumber())+"</HawbCode>");
    	builder.append("    <FlightNo></FlightNo>");
    	builder.append("    <FlightDate></FlightDate>");
    	builder.append("  </ServiceData>");
    	builder.append("</Service>");
    	log.info("参数开始"+builder.toString()+"参数结束");
//		ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"",builder.toString());
//		String  objStr= responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
        bodyMap.put("serviceXml",builder.toString());
        String objStr = SendUtils.doSend(config.getUrlPost()+"",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		
		HashMap<String,String> resultMap = getResultData(objStr);

		if ("1".equals(resultMap.get("ResultCode"))|| "010601".equals(resultMap.get("ResultCode"))) {
			//删除分单
			String AwbNumber=bean.getAwbnumber().substring(0,3)+"-"+bean.getAwbnumber().substring(3);
			baseMapper.deleteFhl(user.getOrgId(),AwbNumber,bean.getHwbnumber());
		}else {
			throw new CheckedException(resultMap.get("ResultContent"));
		}
		return true;
	}
	@Override
	public Boolean doDeclare(FhlOperation bean) {
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.AE_CD_IMP_HAWB;
		String type = APIType.getAPIType(apiType);
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		StringBuilder builder = new StringBuilder();
		builder.append("<Service>");
		builder.append("  <ServiceURL>AirwayBill_Imp_AdapterForNKG</ServiceURL>");
		builder.append("  <ServiceAction>DeclareManifest</ServiceAction>");
		builder.append("  <ServiceData>");
    	builder.append("    <Forwarder>"+config.getAppid()+"</Forwarder>");
		builder.append("    <SYSCODE>"+nullToEmpty(bean.getSyscode())+"</SYSCODE>");
//		builder.append("    <Forwarder>CTSNKG</Forwarder>");
		builder.append("    <Handler>"+user.getUserCname()+"</Handler>");
		builder.append("    <MawbCode>"+nullToEmpty(bean.getAwbnumber())+"</MawbCode>");
		builder.append("    <HawbCode>"+nullToEmpty(bean.getHwbnumber())+"</HawbCode>");
		builder.append("    <MessageType>"+nullToEmpty(bean.getMessageType())+"</MessageType>");
		builder.append("    <FlightNo></FlightNo>");
		builder.append("    <FlightDate></FlightDate>");
		builder.append("  </ServiceData>");
		builder.append("</Service>");
		log.info("参数开始"+builder.toString()+"参数结束");
//		ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"",builder.toString());
//		String  objStr= responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
        bodyMap.put("serviceXml",builder.toString());
        String objStr = SendUtils.doSend(config.getUrlPost()+"",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		HashMap<String,String> resultMap = getResultData(objStr);

		if ("1".equals(resultMap.get("ResultCode")) || "010601".equals(resultMap.get("ResultCode"))  ) {
			
		}else {
			throw new CheckedException(resultMap.get("ResultContent"));
		}
		return true;
	}
	private String getAICode() {
        Date dt = new Date();

        String year = String.format("%ty", dt);

        String mon = String.format("%tm", dt);

        String day = String.format("%td", dt);
        return "AI-" + year + mon + day;
    }
	private double getPlanChargeWeight(String totalgrossweight,String totalvolumnamount) {
		double planChargeWeight = Double.parseDouble(totalvolumnamount) * 1000000 / 6000;
		if (Double.parseDouble(totalgrossweight) > planChargeWeight) {		
			planChargeWeight = Double.parseDouble(totalgrossweight);			
		} 
		return (double)Math.round(planChargeWeight*100)/100;
	}
	private String getPrintRemark(AfOrderShipperConsignee bean) {
        String result="";
        if(!StringUtils.isEmpty(bean.getScName())) {
			result += bean.getScName() + "\n";
		}
		if(!StringUtils.isEmpty(bean.getScAddress())) {
			result += bean.getScAddress() + ' ';
		}
		if(!StringUtils.isEmpty(bean.getNationCode())) {

			result += bean.getNationCode();
		}
		if(!StringUtils.isEmpty(bean.getTelNumber())) {

			result += ",TEL:"+bean.getTelNumber();
		}
        return result;
    }
	private HashMap<String,String> getResultData(String objStr){
		HashMap<String,String> resultMap = new HashMap<String,String>();
		try {
			org.dom4j.Document document = DocumentHelper.parseText(objStr.substring(objStr.indexOf("<ServiceResult>")+15,objStr.indexOf("</ServiceResult>")));
			org.dom4j.Element rootElement = document.getRootElement();
			resultMap.put("ResultCode",rootElement.element("ResultCode").getText());
			resultMap.put("ResultContent",rootElement.element("ResultContent").getText());
		}catch (Exception e) {
			
		}
		return resultMap;
	}
	
//	public static String xml2JSON(String xml){
//		return new XMLSerializer(). .read(xml).toString();
//	}
	
	@Override
	public Boolean doMerge(BranchLine bean) {
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.AE_CD_IMP_HAWB;
		String type = APIType.getAPIType(apiType);
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		String strs[]=bean.getRearchid().split(",");
		StringBuilder builder = new StringBuilder();
		builder.append("<Service>");
		builder.append("  <ServiceURL>ConsolidationList_Exp_Adapter</ServiceURL>");
		builder.append("  <ServiceAction>MergingBranch</ServiceAction>");
		builder.append("  <ServiceData>");
		builder.append("  <AirwayBill>");
    	builder.append("    <Forwarder>"+config.getAppid()+"</Forwarder>");
//		builder.append("    <SYSCODE>"+nullToEmpty(bean.getSyscode())+"</SYSCODE>");
//		builder.append("    <Forwarder>CTSNKG</Forwarder>");
		builder.append("    <Handler>"+user.getUserCname()+"</Handler>");
		builder.append("    <AWBNumber>"+nullToEmpty(bean.getAwbnumber())+"</AWBNumber>");
		builder.append("    <HWBNumber>"+nullToEmpty(bean.getHwbnumber())+"</HWBNumber>");
		builder.append("    <AllocateInfos>");
		
		for (int i = 0; i < strs.length; i++) {
			builder.append("    <AllocateInfo>");
			builder.append("    	<AllocateNo>"+strs[i]+"</AllocateNo>");
			builder.append("    </AllocateInfo>");
		}
		
		builder.append("    </AllocateInfos>");
		builder.append("  </AirwayBill>");
		builder.append("  </ServiceData>");
		builder.append("</Service>");
		log.info("参数开始"+builder.toString()+"参数结束");
//		ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"",builder.toString());
//		String  objStr= responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
        bodyMap.put("serviceXml",builder.toString());
        String objStr = SendUtils.doSend(config.getUrlPost()+"",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		HashMap<String,String> resultMap = getResultData(objStr);

		if ("1".equals(resultMap.get("ResultCode")) || "010601".equals(resultMap.get("ResultCode"))  ) {
			
		}else {
			throw new CheckedException(resultMap.get("ResultContent"));
		}
		return true;
	}
	@Override
	public Boolean doReset(BranchLine bean) {
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.AE_CD_IMP_HAWB;
		String type = APIType.getAPIType(apiType);
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		StringBuilder builder = new StringBuilder();
		builder.append("<Service>");
		builder.append("  <ServiceURL>ConsolidationList_Exp_Adapter</ServiceURL>");
		builder.append("  <ServiceAction>ResetBranch</ServiceAction>");
		builder.append("  <ServiceData>");
		builder.append("  <AirwayBill>");
		builder.append("    <Forwarder>"+config.getAppid()+"</Forwarder>");
//		builder.append("    <SYSCODE>"+nullToEmpty(bean.getSyscode())+"</SYSCODE>");
//		builder.append("    <Forwarder>CTSNKG</Forwarder>");
		builder.append("    <Handler>"+user.getUserCname()+"</Handler>");
		builder.append("    <AWBNumber>"+nullToEmpty(bean.getAwbnumber())+"</AWBNumber>");
		builder.append("    <HWBNumber>"+nullToEmpty(bean.getHwbnumber())+"</HWBNumber>");
		builder.append("    <AllocateInfo>");
		builder.append("    	<AllocateNo>"+nullToEmpty(bean.getRearchid())+"</AllocateNo>");
		builder.append("    	<OldAllocateNo>"+nullToEmpty(bean.getOldrearchid())+"</OldAllocateNo>");
		builder.append("    	<Peices>"+nullToEmpty(bean.getTotalpiecequantity())+"</Peices>");
		builder.append("    	<GrossWeight>"+nullToEmpty(bean.getTotalgrossweight())+"</GrossWeight>");
		builder.append("    	<Volume>"+nullToEmpty(bean.getTotalvolumnamount())+"</Volume>");
		builder.append("    </AllocateInfo>");
		builder.append("  </AirwayBill>");
		builder.append("  </ServiceData>");
		builder.append("</Service>");
		log.info("参数开始"+builder.toString()+"参数结束");
//		ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"",builder.toString());
//		String  objStr= responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
        bodyMap.put("serviceXml",builder.toString());
        String objStr = SendUtils.doSend(config.getUrlPost()+"",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		HashMap<String,String> resultMap = getResultData(objStr);
		
		if ("1".equals(resultMap.get("ResultCode")) || "010601".equals(resultMap.get("ResultCode"))  ) {
			
		}else {
			throw new CheckedException(resultMap.get("ResultContent"));
		}
		return true;
	}
	@Override
	public Boolean doSplit(BranchLine bean) {
		EUserDetails user = SecurityUtils.getUser();
		String apiType = APIType.AE_CD_IMP_HAWB;
		String type = APIType.getAPIType(apiType);
		OrgInterface config = baseMapper.getShippingBillConfig(user.getOrgId(), apiType);
		if (config == null ) {
			throw new CheckedException("对不起，贵司没有开通"+type+"的权限，请联系管理员开通相关权限！");
		}
		StringBuilder builder = new StringBuilder();
		builder.append("<Service>");
		builder.append("  <ServiceURL>ConsolidationList_Exp_Adapter</ServiceURL>");
		builder.append("  <ServiceAction>SplitBranch</ServiceAction>");
		builder.append("  <ServiceData>");
		builder.append("  <AirwayBill>");
		builder.append("    <Forwarder>"+config.getAppid()+"</Forwarder>");
//		builder.append("    <SYSCODE>"+nullToEmpty(bean.getSyscode())+"</SYSCODE>");
//		builder.append("    <Forwarder>CTSNKG</Forwarder>");
		builder.append("    <Handler>"+user.getUserCname()+"</Handler>");
		builder.append("    <AWBNumber>"+nullToEmpty(bean.getAwbnumber())+"</AWBNumber>");
		builder.append("    <HWBNumber>"+nullToEmpty(bean.getHwbnumber())+"</HWBNumber>");
		builder.append("    <AllocateInfos>");
		builder.append("    <AllocateInfo>");
		builder.append("    	<AllocateNo>"+nullToEmpty(bean.getRearchid())+"</AllocateNo>");
		builder.append("    	<Peices>"+nullToEmpty(bean.getTotalpiecequantity())+"</Peices>");
		builder.append("    	<GrossWeight>"+nullToEmpty(bean.getTotalgrossweight())+"</GrossWeight>");
		builder.append("    	<Volume>"+nullToEmpty(bean.getTotalvolumnamount())+"</Volume>");
		builder.append("    </AllocateInfo>");
		builder.append("    </AllocateInfos>");
		builder.append("  </AirwayBill>");
		builder.append("  </ServiceData>");
		builder.append("</Service>");
		log.info("参数开始"+builder.toString()+"参数结束");
//		ResponseEntity<String> responseEntity = sendToThirdUrl(config.getUrlPost(),"",builder.toString());
//		String  objStr= responseEntity.getBody();
		HashMap<String, String> bodyMap=new HashMap<String, String>();
        bodyMap.put("serviceXml",builder.toString());
        String objStr = SendUtils.doSend(config.getUrlPost()+"",bodyMap);
		log.info("结果开始"+objStr+"结果结束");
		HashMap<String,String> resultMap = getResultData(objStr);
		
		if ("1".equals(resultMap.get("ResultCode")) || "010601".equals(resultMap.get("ResultCode"))  ) {
			
		}else {
			throw new CheckedException(resultMap.get("ResultContent"));
		}
		return true;
	}
}
