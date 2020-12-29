package com.efreight.afbase.service.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.CssReportBusinessAnalysisMapper;
import com.efreight.afbase.dao.ServiceMapper;
import com.efreight.afbase.entity.procedure.CssReportBusinessAnalysis;
import com.efreight.afbase.service.CssReportBusinessAnalysisService;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;


@Service
@AllArgsConstructor
public class CssReportBusinessAnalysisServiceImpl extends ServiceImpl<CssReportBusinessAnalysisMapper, CssReportBusinessAnalysis> implements CssReportBusinessAnalysisService {
	private final ServiceMapper serviceMapper;
    @Override
    public List<Map<String,String>> getListPage(CssReportBusinessAnalysis bean) {
    	if(bean.getOtherOrg()!=null&&bean.getOtherOrg()!=-1) {
    		bean.setOrgId(bean.getOtherOrg());
    	}else if(bean.getOtherOrg()!=null&&bean.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			bean.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			bean.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		bean.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
        if ("月".equals(bean.getOrderUnit())) {
        	bean.setOrderUnitValue("'%Y/%m'");
		} else if("天".equals(bean.getOrderUnit())) {
//			bean.setOrderUnitValue("'%Y/%m/%d'");
			bean.setOrderUnitValue("'%Y %b %d'");
		}else {

		}

        List<Map<String, String>> resultList = new ArrayList<Map<String,String>>();
        
        List<Map<String, String>> list=baseMapper.getListPage(bean);
        Map<String,String> map=new HashMap<String,String>();
        Map<String,String> weightMap=new HashMap<String,String>();
        Map<String,String> incomeAmountMap=new HashMap<String,String>();
        Map<String,String> amountMap=new HashMap<String,String>();
        Map<String,String> xMap=new HashMap<String,String>();
        int totalNum=0;
        double totalNum2=0.0;
        double totalNum3=0.0;
        double totalNum4=0.0;
        double totalNum5=0.0;
        for (int i = 0; i < list.size(); i++) {
        	totalNum=totalNum+ Integer.parseInt(String.valueOf(list.get(i).get("orderValue")));
        	
        	totalNum3=totalNum3+Double.parseDouble(String.valueOf(list.get(i).get("incomeAmount")));
        	totalNum4=totalNum4+Double.parseDouble(String.valueOf(list.get(i).get("costAmount")));
        	
//        	map.put(list.get(i).get("orderName"), String.valueOf(list.get(i).get("orderValue")));
//        	weightMap.put(list.get(i).get("orderName"), String.valueOf(list.get(i).get("totalWeight")));
//        	incomeAmountMap.put(list.get(i).get("orderName"), String.valueOf(list.get(i).get("incomeAmount")));
//        	amountMap.put(list.get(i).get("orderName"), String.valueOf(list.get(i).get("costAmount")));
        	String orderName="";
        	if ("天".equals(bean.getOrderUnit())) {
        		orderName=list.get(i).get("orderName").substring(5).toUpperCase();
			}else {
				orderName=list.get(i).get("orderName");
			}
        	map.put(orderName, fmtMicrometer(String.valueOf(list.get(i).get("orderValue"))));
        	
        	incomeAmountMap.put(orderName, fmtMicrometer2(String.valueOf(list.get(i).get("incomeAmount"))));
        	amountMap.put(orderName, fmtMicrometer2(String.valueOf(list.get(i).get("costAmount"))));
        	if ("SE".equals(bean.getBusinessScope())||"SI".equals(bean.getBusinessScope()) || "TE".equals(bean.getBusinessScope())) {
        		if("整箱".equals(bean.getContainerMethod())) {
        			totalNum5=totalNum5+Double.parseDouble(String.valueOf(list.get(i).get("containerNumber")));
            		xMap.put(orderName, fmtMicrometer2(String.valueOf(list.get(i).get("containerNumber"))));
        		}else {
        			totalNum2=totalNum2+Double.parseDouble(String.valueOf(list.get(i).get("totalWeight")));
         			weightMap.put(orderName, fmtMicrometer2(String.valueOf(list.get(i).get("totalWeight"))));
        			totalNum5=totalNum5+Double.parseDouble(String.valueOf(list.get(i).get("containerNumber")));
            		xMap.put(orderName, fmtMicrometer2(String.valueOf(list.get(i).get("containerNumber"))));
        		}
     		}else {
     			totalNum2=totalNum2+Double.parseDouble(String.valueOf(list.get(i).get("totalWeight")));
     			weightMap.put(orderName, fmtMicrometer2(String.valueOf(list.get(i).get("totalWeight"))));
     		}
        	
		}
        map.put("totalNum", fmtMicrometer(String.valueOf(totalNum)));
//        weightMap.put("totalNum", fmtMicrometer2(String.valueOf(totalNum2)));
        incomeAmountMap.put("totalNum", fmtMicrometer2(String.valueOf(totalNum3)));
        amountMap.put("totalNum", fmtMicrometer2(String.valueOf(totalNum4)));
        
        map.put("name", "票数");
        incomeAmountMap.put("name", "收入（万元）");
        amountMap.put("name", "毛利（万元）");
        resultList.add(map);
//        resultList.add(weightMap);
        if ("SE".equals(bean.getBusinessScope())||"SI".equals(bean.getBusinessScope()) || "TE".equals(bean.getBusinessScope())) {
        	if("整箱".equals(bean.getContainerMethod())) {
        		xMap.put("totalNum", fmtMicrometer2(String.valueOf(totalNum5)));
            	xMap.put("name", "标箱数量");
            	resultList.add(xMap);
        	}else {
        		weightMap.put("totalNum", fmtMicrometer2(String.valueOf(totalNum2)));
        		weightMap.put("name", "计重（吨）");
        		resultList.add(weightMap);
        		xMap.put("totalNum", fmtMicrometer2(String.valueOf(totalNum5)));
            	xMap.put("name", "标箱数量");
            	resultList.add(xMap);
        	}
 		}else {
    		weightMap.put("totalNum", fmtMicrometer2(String.valueOf(totalNum2)));
    		weightMap.put("name", "计重（吨）");
    		resultList.add(weightMap);
    	}
        resultList.add(incomeAmountMap);
        resultList.add(amountMap);
        return resultList;
    }
    @Override
    public List<Map<String,String>> getListPage2(CssReportBusinessAnalysis bean) {
    	if(bean.getOtherOrg()!=null&&bean.getOtherOrg()!=-1) {
    		bean.setOrgId(bean.getOtherOrg());
    	}else if(bean.getOtherOrg()!=null&&bean.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			bean.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			bean.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		bean.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
    	
    	List<Map<String, String>> resultList = new ArrayList<Map<String,String>>();
    	
    	List<Map<String, String>> list=baseMapper.getListPage2(bean);
    	Map<String,String> map=new HashMap<String,String>();
    	Map<String,String> weightMap=new HashMap<String,String>();
    	Map<String,String> incomeAmountMap=new HashMap<String,String>();
    	Map<String,String> amountMap=new HashMap<String,String>();
    	Map<String,String> xMap=new HashMap<String,String>();
    	int totalNum=0;
    	double totalNum2=0.0;
    	double totalNum3=0.0;
    	double totalNum4=0.0;
    	double totalNum5=0.0;
    	for (int i = 0; i < list.size(); i++) {
    		totalNum=totalNum+ Integer.parseInt(String.valueOf(list.get(i).get("orderValue")));
//    		totalNum2=totalNum2+Double.parseDouble(String.valueOf(list.get(i).get("totalWeight")));
    		totalNum3=totalNum3+Double.parseDouble(String.valueOf(list.get(i).get("incomeAmount")));
    		totalNum4=totalNum4+Double.parseDouble(String.valueOf(list.get(i).get("costAmount")));
 
    		map.put(getOrderName(String.valueOf(list.get(i).get("orderName"))), fmtMicrometer(String.valueOf(list.get(i).get("orderValue"))));
//    		weightMap.put(getOrderName(String.valueOf(list.get(i).get("orderName"))), fmtMicrometer2(String.valueOf(list.get(i).get("totalWeight"))));
    		incomeAmountMap.put(getOrderName(String.valueOf(list.get(i).get("orderName"))), fmtMicrometer2(String.valueOf(list.get(i).get("incomeAmount"))));
    		amountMap.put(getOrderName(String.valueOf(list.get(i).get("orderName"))), fmtMicrometer2(String.valueOf(list.get(i).get("costAmount"))));
    		if ("SE".equals(bean.getBusinessScope())||"SI".equals(bean.getBusinessScope()) ||"TE".equals(bean.getBusinessScope())) {
    			if("整箱".equals(bean.getContainerMethod())) {
    				totalNum5=totalNum5+Double.parseDouble(String.valueOf(list.get(i).get("containerNumber")));
        			xMap.put(getOrderName(String.valueOf(list.get(i).get("orderName"))), fmtMicrometer2(String.valueOf(list.get(i).get("containerNumber"))));
        		}else {
        			totalNum2=totalNum2+Double.parseDouble(String.valueOf(list.get(i).get("totalWeight")));
         			weightMap.put(getOrderName(String.valueOf(list.get(i).get("orderName"))), fmtMicrometer2(String.valueOf(list.get(i).get("totalWeight"))));
         			totalNum5=totalNum5+Double.parseDouble(String.valueOf(list.get(i).get("containerNumber")));
        			xMap.put(getOrderName(String.valueOf(list.get(i).get("orderName"))), fmtMicrometer2(String.valueOf(list.get(i).get("containerNumber"))));
        		}
    		}else {
     			totalNum2=totalNum2+Double.parseDouble(String.valueOf(list.get(i).get("totalWeight")));
     			weightMap.put(getOrderName(String.valueOf(list.get(i).get("orderName"))), fmtMicrometer2(String.valueOf(list.get(i).get("totalWeight"))));
     		}
    		
    	}
    	map.put("totalNum", fmtMicrometer(String.valueOf(totalNum)));
//    	weightMap.put("totalNum", fmtMicrometer2(String.valueOf(totalNum2)));
    	incomeAmountMap.put("totalNum", fmtMicrometer2(String.valueOf(totalNum3)));
    	amountMap.put("totalNum", fmtMicrometer2(String.valueOf(totalNum4)));
    	
    	map.put("name", "票数");
    	incomeAmountMap.put("name", "收入（万元）");
    	amountMap.put("name", "毛利（万元）");
    	resultList.add(map);
//    	resultList.add(weightMap);
    	if ("SE".equals(bean.getBusinessScope())||"SI".equals(bean.getBusinessScope()) ||"TE".equals(bean.getBusinessScope())) {
    		
    		if("整箱".equals(bean.getContainerMethod())) {
    			xMap.put("totalNum", fmtMicrometer2(String.valueOf(totalNum5)));
        		xMap.put("name", "标箱数量");
        		resultList.add(xMap);
        	}else {
        		weightMap.put("totalNum", fmtMicrometer2(String.valueOf(totalNum2)));
        		weightMap.put("name", "计重（吨）");
        		resultList.add(weightMap);
        		xMap.put("totalNum", fmtMicrometer2(String.valueOf(totalNum5)));
        		xMap.put("name", "标箱数量");
        		resultList.add(xMap);
        	}
    	}else {
    		weightMap.put("totalNum", fmtMicrometer2(String.valueOf(totalNum2)));
    		weightMap.put("name", "计重（吨）");
    		resultList.add(weightMap);
    	}
    	resultList.add(incomeAmountMap);
    	resultList.add(amountMap);
    	return resultList;
    }
    @Override
    public List<Map<String,String>> getListPage22(CssReportBusinessAnalysis bean) {
    	if(bean.getOtherOrg()!=null&&bean.getOtherOrg()!=-1) {
    		bean.setOrgId(bean.getOtherOrg());
    	}else if(bean.getOtherOrg()!=null&&bean.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			bean.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			bean.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		bean.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
    	if ("月".equals(bean.getOrderUnit())) {
    		bean.setOrderUnitValue("'%Y/%m'");
    	} else if("天".equals(bean.getOrderUnit())) {
//    		bean.setOrderUnitValue("'%Y/%m/%d'");
    		bean.setOrderUnitValue("'%Y %b %d'");
    	}else {
    		
    	}
    	
    	List<Map<String, String>> resultList = new ArrayList<Map<String,String>>();
    	
    	List<Map<String, String>> list=baseMapper.getListPage(bean);
    	Map<String,String> map=new HashMap<String,String>();
    	Map<String,String> weightMap=new HashMap<String,String>();
    	Map<String,String> incomeAmountMap=new HashMap<String,String>();
    	Map<String,String> amountMap=new HashMap<String,String>();
    	Map<String,String> xMap=new HashMap<String,String>();
    	int totalNum=0;
    	double totalNum2=0.0;
    	double totalNum3=0.0;
    	double totalNum4=0.0;
    	double totalNum5=0.0;
    	for (int i = 0; i < list.size(); i++) {
    		totalNum=totalNum+ Integer.parseInt(String.valueOf(list.get(i).get("orderValue")));
//    		totalNum2=totalNum2+Double.parseDouble(String.valueOf(list.get(i).get("totalWeight")));
    		totalNum3=totalNum3+Double.parseDouble(String.valueOf(list.get(i).get("incomeAmount")));
    		totalNum4=totalNum4+Double.parseDouble(String.valueOf(list.get(i).get("costAmount")));
    		
    		String orderName="";
        	if ("天".equals(bean.getOrderUnit())) {
        		orderName=list.get(i).get("orderName").substring(5).toUpperCase();
			}else {
				orderName=list.get(i).get("orderName");
			}
    		map.put(orderName, fmtMicrometer3(String.valueOf(list.get(i).get("orderValue"))));
//    		weightMap.put(list.get(i).get("orderName"), fmtMicrometer33(String.valueOf(list.get(i).get("totalWeight"))));
    		incomeAmountMap.put(orderName, fmtMicrometer33(String.valueOf(list.get(i).get("incomeAmount"))));
    		amountMap.put(orderName, fmtMicrometer33(String.valueOf(list.get(i).get("costAmount"))));
    		if ("SE".equals(bean.getBusinessScope())||"SI".equals(bean.getBusinessScope()) ||"TE".equals(bean.getBusinessScope())) {
    			
    			if("整箱".equals(bean.getContainerMethod())) {
    				totalNum5=totalNum5+Double.parseDouble(String.valueOf(list.get(i).get("containerNumber")));
        			xMap.put(orderName, fmtMicrometer33(String.valueOf(list.get(i).get("containerNumber"))));
        		}else {
        			totalNum2=totalNum2+Double.parseDouble(String.valueOf(list.get(i).get("totalWeight")));
        			weightMap.put(orderName, fmtMicrometer33(String.valueOf(list.get(i).get("totalWeight"))));
         			totalNum5=totalNum5+Double.parseDouble(String.valueOf(list.get(i).get("containerNumber")));
        			xMap.put(orderName, fmtMicrometer33(String.valueOf(list.get(i).get("containerNumber"))));
        		}
    		}else {
    			totalNum2=totalNum2+Double.parseDouble(String.valueOf(list.get(i).get("totalWeight")));
    			weightMap.put(orderName, fmtMicrometer33(String.valueOf(list.get(i).get("totalWeight"))));
     		}
    		
    	}
    	map.put("totalNum", fmtMicrometer3(String.valueOf(totalNum)));
//    	weightMap.put("totalNum", fmtMicrometer33(String.valueOf(totalNum2)));
    	incomeAmountMap.put("totalNum", fmtMicrometer33(String.valueOf(totalNum3)));
    	amountMap.put("totalNum", fmtMicrometer33(String.valueOf(totalNum4)));
    	
    	map.put("name", "票数");
    	incomeAmountMap.put("name", "收入（万元）");
    	amountMap.put("name", "毛利（万元）");
    	resultList.add(map);
//    	resultList.add(weightMap);
    	if ("SE".equals(bean.getBusinessScope())||"SI".equals(bean.getBusinessScope()) ||"TE".equals(bean.getBusinessScope())) {
    		
    		if("整箱".equals(bean.getContainerMethod())) {
    			xMap.put("totalNum", fmtMicrometer33(String.valueOf(totalNum5)));
        		xMap.put("name", "标箱数量");
        		resultList.add(xMap);
        	}else {
        		weightMap.put("totalNum", fmtMicrometer33(String.valueOf(totalNum2)));
        		weightMap.put("name", "计重（吨）");
        		resultList.add(weightMap);
        		xMap.put("totalNum", fmtMicrometer33(String.valueOf(totalNum5)));
        		xMap.put("name", "标箱数量");
        		resultList.add(xMap);
        	}
    	}else {
    		weightMap.put("totalNum", fmtMicrometer33(String.valueOf(totalNum2)));
    		weightMap.put("name", "计重（吨）");
    		resultList.add(weightMap);
    	}
    	resultList.add(incomeAmountMap);
    	resultList.add(amountMap);
    	return resultList;
    }
    @Override
    public List<Map<String,String>> getListPage23(CssReportBusinessAnalysis bean) {
    	if(bean.getOtherOrg()!=null&&bean.getOtherOrg()!=-1) {
    		bean.setOrgId(bean.getOtherOrg());
    	}else if(bean.getOtherOrg()!=null&&bean.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			bean.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			bean.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		bean.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
    	
    	
    	List<Map<String, String>> resultList = new ArrayList<Map<String,String>>();
    	
    	List<Map<String, String>> list=baseMapper.getListPage2(bean);
    	Map<String,String> map=new HashMap<String,String>();
    	Map<String,String> weightMap=new HashMap<String,String>();
    	Map<String,String> incomeAmountMap=new HashMap<String,String>();
    	Map<String,String> amountMap=new HashMap<String,String>();
    	Map<String,String> xMap=new HashMap<String,String>();
    	int totalNum=0;
    	double totalNum2=0.0;
    	double totalNum3=0.0;
    	double totalNum4=0.0;
    	double totalNum5=0.0;
    	for (int i = 0; i < list.size(); i++) {
    		totalNum=totalNum+ Integer.parseInt(String.valueOf(list.get(i).get("orderValue")));
//    		totalNum2=totalNum2+Double.parseDouble(String.valueOf(list.get(i).get("totalWeight")));
    		totalNum3=totalNum3+Double.parseDouble(String.valueOf(list.get(i).get("incomeAmount")));
    		totalNum4=totalNum4+Double.parseDouble(String.valueOf(list.get(i).get("costAmount")));
    		
    		map.put(getOrderName(String.valueOf(list.get(i).get("orderName"))), fmtMicrometer3(String.valueOf(list.get(i).get("orderValue"))));
//    		weightMap.put(getOrderName(String.valueOf(list.get(i).get("orderName"))), fmtMicrometer33(String.valueOf(list.get(i).get("totalWeight"))));
    		incomeAmountMap.put(getOrderName(String.valueOf(list.get(i).get("orderName"))), fmtMicrometer33(String.valueOf(list.get(i).get("incomeAmount"))));
    		amountMap.put(getOrderName(String.valueOf(list.get(i).get("orderName"))), fmtMicrometer33(String.valueOf(list.get(i).get("costAmount"))));
    		if ("SE".equals(bean.getBusinessScope())||"SI".equals(bean.getBusinessScope()) ||"TE".equals(bean.getBusinessScope())) {
    			
    			if("整箱".equals(bean.getContainerMethod())) {
    				totalNum5=totalNum5+Double.parseDouble(String.valueOf(list.get(i).get("containerNumber")));
        			xMap.put(getOrderName(String.valueOf(list.get(i).get("orderName"))), fmtMicrometer33(String.valueOf(list.get(i).get("containerNumber"))));
        		}else {
        			totalNum2=totalNum2+Double.parseDouble(String.valueOf(list.get(i).get("totalWeight")));
        			weightMap.put(getOrderName(String.valueOf(list.get(i).get("orderName"))), fmtMicrometer33(String.valueOf(list.get(i).get("totalWeight"))));
        			totalNum5=totalNum5+Double.parseDouble(String.valueOf(list.get(i).get("containerNumber")));
        			xMap.put(getOrderName(String.valueOf(list.get(i).get("orderName"))), fmtMicrometer33(String.valueOf(list.get(i).get("containerNumber"))));
        		}
    		}else {
    			totalNum2=totalNum2+Double.parseDouble(String.valueOf(list.get(i).get("totalWeight")));
    			weightMap.put(getOrderName(String.valueOf(list.get(i).get("orderName"))), fmtMicrometer33(String.valueOf(list.get(i).get("totalWeight"))));
     		}
    		
    	}
    	map.put("totalNum", fmtMicrometer3(String.valueOf(totalNum)));
//    	weightMap.put("totalNum", fmtMicrometer33(String.valueOf(totalNum2)));
    	incomeAmountMap.put("totalNum", fmtMicrometer33(String.valueOf(totalNum3)));
    	amountMap.put("totalNum", fmtMicrometer33(String.valueOf(totalNum4)));
    	
    	map.put("name", "票数");
    	incomeAmountMap.put("name", "收入（万元）");
    	amountMap.put("name", "毛利（万元）");
    	resultList.add(map);
//    	resultList.add(weightMap);
    	if ("SE".equals(bean.getBusinessScope())||"SI".equals(bean.getBusinessScope()) ||"TE".equals(bean.getBusinessScope())) {
    		
    		if("整箱".equals(bean.getContainerMethod())) {
    			xMap.put("totalNum", fmtMicrometer33(String.valueOf(totalNum5)));
        		xMap.put("name", "标箱数量");
        		resultList.add(xMap);
        	}else {
        		weightMap.put("totalNum", fmtMicrometer33(String.valueOf(totalNum2)));
        		weightMap.put("name", "计重（吨）");
        		resultList.add(weightMap);
        		xMap.put("totalNum", fmtMicrometer33(String.valueOf(totalNum5)));
        		xMap.put("name", "标箱数量");
        		resultList.add(xMap);
        	}
    	}else {
    		weightMap.put("totalNum", fmtMicrometer33(String.valueOf(totalNum2)));
    		weightMap.put("name", "计重（吨）");
    		resultList.add(weightMap);
    	}
    	resultList.add(incomeAmountMap);
    	resultList.add(amountMap);
    	return resultList;
    }
    public static String getOrderName(String name) {
    	name=name.substring(0, 4)+" week"+name.substring(4)+"";
    	return name;
    }
    public static String fmtMicrometer(String text) {
		DecimalFormat df = null;
		
		df = new DecimalFormat("###,##0");
		
		double number = 0.0;
		try {
			number = Double.parseDouble(text);
		} catch (Exception e) {
			number = 0.0;
		}
		return df.format(number);
	}
    public static String fmtMicrometer2(String text) {
		DecimalFormat df = null;
	
				df = new DecimalFormat("###,##0.00");
			
		double number = 0.0;
		try {
			number = Double.parseDouble(text);
		} catch (Exception e) {
			number = 0.0;
		}
		return df.format(number);
	}
    public static String fmtMicrometer3(String text) {
    	DecimalFormat df = null;
    	
    	df = new DecimalFormat("#####0");
    	
    	double number = 0.0;
    	try {
    		number = Double.parseDouble(text);
    	} catch (Exception e) {
    		number = 0.0;
    	}
    	return df.format(number);
    }
    public static String fmtMicrometer33(String text) {
    	DecimalFormat df = null;
    	
    	df = new DecimalFormat("#####0.00");
    	
    	double number = 0.0;
    	try {
    		number = Double.parseDouble(text);
    	} catch (Exception e) {
    		number = 0.0;
    	}
    	return df.format(number);
    }

    @Override
    public List<Map<String,String>> getList3(CssReportBusinessAnalysis bean) {
    	if(bean.getOtherOrg()!=null&&bean.getOtherOrg()!=-1) {
    		bean.setOrgId(bean.getOtherOrg());
    	}else if(bean.getOtherOrg()!=null&&bean.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			bean.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			bean.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		bean.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
    	
    	
    	List<Map<String, String>> list = baseMapper.getList3(bean);
    	
    	int totalNum=0;
    	double totalNum2=0.0;
    	double totalNum3=0.0;
    	double totalNum4=0.0;
    	for (int i = 0; i < list.size(); i++) {
    		totalNum=totalNum+ Integer.parseInt(String.valueOf(list.get(i).get("orderValue")));
    		totalNum2=totalNum2+Double.parseDouble(String.valueOf(list.get(i).get("totalWeight")));
    		totalNum3=totalNum3+Double.parseDouble(String.valueOf(list.get(i).get("incomeAmount")));
    		totalNum4=totalNum4+Double.parseDouble(String.valueOf(list.get(i).get("costAmount")));
		}
    	for (int i = 0; i < list.size(); i++) {
    		Map<String,String> map=list.get(i);
//    		System.out.println(fmtMicrometer(String.valueOf(map.get("orderValue"))));
//    		System.out.println(Double.parseDouble(String.valueOf(map.get("orderValue"))));
//    		System.out.println(Double.parseDouble(String.valueOf(map.get("orderValue")))/totalNum);
//    		System.out.println(String.valueOf(totalNum!=0?Double.parseDouble(String.valueOf(map.get("orderValue")))/totalNum:0));
//    		System.out.println(fmtMicrometer(String.valueOf(totalNum!=0?Double.parseDouble(String.valueOf(map.get("orderValue")))/totalNum:0)));
    		map.put("orderValue", fmtMicrometer(String.valueOf(map.get("orderValue")))+
    				" / "+fmtMicrometer2(String.valueOf(totalNum!=0?Double.parseDouble(String.valueOf(map.get("orderValue")))/totalNum*100:0))+"%");
    		if("整箱".equals(bean.getContainerMethod())) {
    			map.put("totalWeight", String.valueOf(map.get("totalWeight"))+
        				" / "+fmtMicrometer2(String.valueOf(totalNum2!=0?Double.parseDouble(String.valueOf(map.get("totalWeight")))/totalNum2*100:0))+"%");
    		}else {
    			map.put("totalWeight", fmtMicrometer2(String.valueOf(map.get("totalWeight")))+
        				" / "+fmtMicrometer2(String.valueOf(totalNum2!=0?Double.parseDouble(String.valueOf(map.get("totalWeight")))/totalNum2*100:0))+"%");
    		}
    		
    		
    		map.put("incomeAmount", fmtMicrometer2(String.valueOf(map.get("incomeAmount")))+
    				" / "+fmtMicrometer2(String.valueOf(totalNum3!=0?Double.parseDouble(String.valueOf(map.get("incomeAmount")))/totalNum3*100:0))+"%");
    		
    		map.put("costAmount", fmtMicrometer2(String.valueOf(map.get("costAmount")))+
    				" / "+fmtMicrometer2(String.valueOf(totalNum4!=0?Double.parseDouble(String.valueOf(map.get("costAmount")))/totalNum4*100:0))+"%");
    		
    		map.put("costAmount2", String.valueOf(list.get(i).get("costAmount2"))+"%");
		}
    	List<Map<String, String>> resultList = new ArrayList<Map<String,String>>();
    	
    	if (list.size()>5) {
			for (int i = 0; i < list.size(); i++) {
				if(i<5) {
					resultList.add(list.get(i));
				}else {
					break;
				}
			}
		} else {
			resultList.addAll(list);
		}
    	
    	return resultList;
    }
    @Override
    public List<Map<String,String>> getList4(CssReportBusinessAnalysis bean) {
    	if(bean.getOtherOrg()!=null&&bean.getOtherOrg()!=-1) {
    		bean.setOrgId(bean.getOtherOrg());
    	}else if(bean.getOtherOrg()!=null&&bean.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			bean.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			bean.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		bean.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
    	
    	List<Map<String, String>> list = baseMapper.getList4(bean);
   
    	List<Map<String, String>> resultList = new ArrayList<Map<String,String>>();
    	
    	if (list.size()>5) {
    		for (int i = 0; i < list.size(); i++) {
    			if(i<5) {
    				resultList.add(list.get(i));
    			}else {
    				break;
    			}
    		}
    	} else {
    		resultList.addAll(list);
    	}
    	
    	return resultList;
    }
    @Override
    public List<Map<String,String>> getList5(CssReportBusinessAnalysis bean) {
    	if(bean.getOtherOrg()!=null&&bean.getOtherOrg()!=-1) {
    		bean.setOrgId(bean.getOtherOrg());
    	}else if(bean.getOtherOrg()!=null&&bean.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			bean.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			bean.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		bean.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
    	
    	List<Map<String, String>> list = baseMapper.getList5(bean);
    	
    	List<Map<String, String>> resultList = new ArrayList<Map<String,String>>();
    	
    	if (list.size()>5) {
    		for (int i = 0; i < list.size(); i++) {
    			if(i<5) {
    				resultList.add(list.get(i));
    			}else {
    				break;
    			}
    		}
    	} else {
    		resultList.addAll(list);
    	}
    	
    	return resultList;
    }
}
