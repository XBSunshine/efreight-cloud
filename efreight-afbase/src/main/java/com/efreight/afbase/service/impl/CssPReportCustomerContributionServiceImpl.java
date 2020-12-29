package com.efreight.afbase.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.cassandra.thrift.Cassandra.system_add_column_family_args;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.alibaba.druid.util.StringUtils;
import com.efreight.afbase.dao.CssPReportCustomerContributionMapper;
import com.efreight.afbase.dao.ServiceMapper;
import com.efreight.afbase.entity.CssPReportCustomerContribution;
import com.efreight.afbase.entity.CssPReportSettleAfExcelTwo;
import com.efreight.afbase.entity.CustomerInContributionInfo;
import com.efreight.afbase.service.CssPReportCustomerContributionService;
import com.efreight.common.security.util.SecurityUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CssPReportCustomerContributionServiceImpl implements CssPReportCustomerContributionService {
	
	private final CssPReportCustomerContributionMapper mapper;
	private final ServiceMapper serviceMapper;

	@Override
	public List<Map> getAfList(CssPReportCustomerContribution bean) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy");
		if(bean.getEndDate()!=null) {
			bean.setEndDateYear(dtf.format(bean.getEndDate()));
		}
		if(StringUtils.isEmpty(bean.getBusinessProduct())) {
			bean.setBusinessProduct(null);
		}
		if(StringUtils.isEmpty(bean.getGoodsType())) {
			bean.setGoodsType(null);
		}
		if(StringUtils.isEmpty(bean.getOrderStatus())) {
			bean.setOrderStatus(null);
		}
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
		return mapper.getAfList(bean);
	}

	@Override
	public List<Map> getScList(CssPReportCustomerContribution bean) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy");
		if(bean.getEndDate()!=null) {
			bean.setEndDateYear(dtf.format(bean.getEndDate()));
		}
		if(StringUtils.isEmpty(bean.getContainerMethod())) {
			bean.setContainerMethod(null);
		}
		if(StringUtils.isEmpty(bean.getGoodsType())) {
			bean.setGoodsType(null);
		}
		if(StringUtils.isEmpty(bean.getOrderStatus())) {
			bean.setOrderStatus(null);
		}
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
		if("TE".equals(bean.getBusinessScope())) {
			return mapper.getTcList(bean);
		}else if("LC".equals(bean.getBusinessScope())) {
			return mapper.getLcList(bean);
		}else if("IO".equals(bean.getBusinessScope())){
			return mapper.getIoList(bean);
		}else {
			return mapper.getScList(bean);
		}
		
	}

	@Override
	public List<Map> getAfListDetail(CssPReportCustomerContribution bean) {
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
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy");
		if(StringUtils.isEmpty(bean.getContainerMethod())) {
			bean.setContainerMethod(null);
		}
		if(StringUtils.isEmpty(bean.getGoodsType())) {
			bean.setGoodsType(null);
		}
		if(StringUtils.isEmpty(bean.getBusinessProduct())) {
			bean.setBusinessProduct(null);
		}
		if(StringUtils.isEmpty(bean.getOrderStatus())) {
			bean.setOrderStatus(null);
		}
		if("YEAR".equals(bean.getEndDateType())) {
			bean.setEndDateYear(dtf.format(bean.getEndDate()));
			bean.setStartDate(null);
			bean.setEndDate(null);
		}else {
			bean.setEndDateYear(null);
		}
		if(StringUtils.isEmpty(bean.getDep())) {
			bean.setDep(null);
		}
		if(StringUtils.isEmpty(bean.getArr())) {
			bean.setArr(null);
		}
		if(StringUtils.isEmpty(bean.getCoopType())) {
			bean.setCoopType(null);
		}
		if(StringUtils.isEmpty(bean.getSupplierName())) {
			bean.setSupplierName(null);
		}
		if(StringUtils.isEmpty(bean.getChooseRoutingNames())) {
			bean.setChooseRoutingNames(null);
		}
		if(StringUtils.isEmpty(bean.getCountry())) {
			bean.setCountry(null);
		}
		if(StringUtils.isEmpty(bean.getArea())) {
			bean.setArea(null);
		}
		if(StringUtils.isEmpty(bean.getCustomerName())) {
			bean.setCustomerName(null);
		}
		return mapper.getAfListDetail(bean);
	}

	@Override
	public List<Map> getScListDetail(CssPReportCustomerContribution bean) {
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
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy");
		if(StringUtils.isEmpty(bean.getContainerMethod())) {
			bean.setContainerMethod(null);
		}
		if(StringUtils.isEmpty(bean.getGoodsType())) {
			bean.setGoodsType(null);
		}
		if(StringUtils.isEmpty(bean.getOrderStatus())) {
			bean.setOrderStatus(null);
		}
		if("YEAR".equals(bean.getEndDateType())) {
			bean.setEndDateYear(dtf.format(bean.getEndDate()));
			bean.setStartDate(null);
			bean.setEndDate(null);
		}else {
			bean.setEndDateYear(null);
		}
		if(StringUtils.isEmpty(bean.getDep())) {
			bean.setDep(null);
		}
		if(StringUtils.isEmpty(bean.getArr())) {
			bean.setArr(null);
		}
		if(StringUtils.isEmpty(bean.getCoopType())) {
			bean.setCoopType(null);
		}
		if(StringUtils.isEmpty(bean.getSupplierName())) {
			bean.setSupplierName(null);
		}
		if(StringUtils.isEmpty(bean.getChooseRoutingNames())) {
			bean.setChooseRoutingNames(null);
		}

		if(StringUtils.isEmpty(bean.getCountry())) {
			bean.setCountry(null);
		}
		if(StringUtils.isEmpty(bean.getArea())) {
			bean.setArea(null);
		}
		if(StringUtils.isEmpty(bean.getCustomerName())) {
			bean.setCustomerName(null);
		}
		if("TE".equals(bean.getBusinessScope())) {
			return mapper.getTcListDetail(bean);
		}else if("LC".equals(bean.getBusinessScope())) {
			return mapper.getLcListDetail(bean);
		}else if("IO".equals(bean.getBusinessScope())){
			return mapper.getIoListDetail(bean);
		}else {
			return mapper.getScListDetail(bean);
		}
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map getCustomerDetail(CssPReportCustomerContribution bean) {
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
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy");
		if(StringUtils.isEmpty(bean.getContainerMethod())) {
			bean.setContainerMethod(null);
		}
		if(StringUtils.isEmpty(bean.getGoodsType())) {
			bean.setGoodsType(null);
		}
		if(StringUtils.isEmpty(bean.getBusinessProduct())) {
			bean.setBusinessProduct(null);
		}
		if(StringUtils.isEmpty(bean.getOrderStatus())) {
			bean.setOrderStatus(null);
		}
		if(StringUtils.isEmpty(bean.getDep())) {
			bean.setDep(null);
		}
		if(StringUtils.isEmpty(bean.getArr())) {
			bean.setArr(null);
		}
		if(StringUtils.isEmpty(bean.getCoopType())) {
			bean.setCoopType(null);
		}
		if(StringUtils.isEmpty(bean.getSupplierName())) {
			bean.setSupplierName(null);
		}
		if(StringUtils.isEmpty(bean.getChooseRoutingNames())) {
			bean.setChooseRoutingNames(null);
		}
//		else {
//			bean.setChooseRoutingNames("'"+bean.getChooseRoutingNames().replace(",", "','")+"'");
//		}
		if(StringUtils.isEmpty(bean.getCountry())) {
			bean.setCountry(null);
		}
		if(StringUtils.isEmpty(bean.getArea())) {
			bean.setArea(null);
		}
		if(StringUtils.isEmpty(bean.getCustomerName())) {
			bean.setCustomerName(null);
		}
		if("YEAR".equals(bean.getEndDateType())) {
			bean.setEndDateYear(dtf.format(bean.getEndDate()));
			bean.setStartDate(null);
			bean.setEndDate(null);
		}else {
			bean.setEndDateYear(null);
		}
		List<CustomerInContributionInfo> list = null;
		List<Map> listInfo = null;
//		System.out.println(LocalDateTime.now().getSecond());
		if(bean.getBusinessScope().startsWith("A")) {
			 list = mapper.getCustomerInfoAfDetail(bean);
//			 listInfo = mapper.getAfListDetail(bean);
		}
        if(bean.getBusinessScope().startsWith("S")) {
        	 list = mapper.getCustomerInfoScDetail(bean);
//        	 listInfo = mapper.getScListDetail(bean);
		}
        if(bean.getBusinessScope().startsWith("T")) {
        	list = mapper.getCustomerInfoTcDetail(bean);
//        	listInfo = mapper.getTcListDetail(bean);
        }
        if(bean.getBusinessScope().startsWith("L")) {
        	list = mapper.getCustomerInfoLcDetail(bean);
//        	listInfo = mapper.getLcListDetail(bean);
        }
        if(bean.getBusinessScope().startsWith("I")) {
        	list = mapper.getCustomerInfoIoDetail(bean);
//        	listInfo = mapper.getLcListDetail(bean);
        }
//        list = this.copyCustomerInContributionInfo(listInfo);
//        System.out.println(LocalDateTime.now().getSecond());
        //加总票数 查询条件结果
        int poll = 0;
        BigDecimal planChargeWeight = BigDecimal.ZERO;
        BigDecimal incomeFunctionalAmountCount = BigDecimal.ZERO;
        BigDecimal grossProfit = BigDecimal.ZERO;
        BigDecimal unitGrossProfit = BigDecimal.ZERO;
        BigDecimal grossProfitMargin = BigDecimal.ZERO;
        BigDecimal costFunctionalAmountCount = BigDecimal.ZERO;
        //数组数据依次为 票数 计重  收入  毛利 成本
        BigDecimal[] arrDep = new BigDecimal[] {BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO};
        BigDecimal[] arrArr = new BigDecimal[] {BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO};
        Map<String,BigDecimal[]> mapDep = new HashMap();
        Map<String,BigDecimal[]> mapArr = new HashMap();
        Map<String,BigDecimal[]> mapArrTwo = new HashMap();
        Map<String,BigDecimal[]> mapDepL = new HashMap();
        Map<String,BigDecimal[]> mapArrL = new HashMap();
        List<Object[]> dep = new ArrayList<>();
        List<Object[]> arr = new ArrayList<>();
        List<Map> listCount = new ArrayList<>();
        //加总票数  前推时间段结果
        int pollTwo = 0;
        BigDecimal planChargeWeightTwo = BigDecimal.ZERO;
        BigDecimal incomeFunctionalAmountCountTwo = BigDecimal.ZERO;
        BigDecimal grossProfitTwo = BigDecimal.ZERO;
        BigDecimal unitGrossProfitTwo = BigDecimal.ZERO;
        BigDecimal grossProfitMarginTwo = BigDecimal.ZERO;
        BigDecimal costFunctionalAmountCountTwo = BigDecimal.ZERO;
        
        if(list!=null&&list.size()>0) {
        	List<CustomerInContributionInfo> listTwo = null;
        	//存在数据再查询前始发时间的前一个时间段数据
        	long days = bean.getEndDate().toEpochDay()-bean.getStartDate().toEpochDay();
        	bean.setEndDate(bean.getStartDate().minusDays(1));
        	bean.setStartDate(bean.getStartDate().minusDays(days+1));
    		if(bean.getBusinessScope().startsWith("A")) {
    			listTwo = mapper.getCustomerInfoAfDetail(bean);
    		}
            if(bean.getBusinessScope().startsWith("S")) {
            	listTwo = mapper.getCustomerInfoScDetail(bean);
    		}
            if(bean.getBusinessScope().startsWith("T")) {
            	listTwo = mapper.getCustomerInfoTcDetail(bean);
    		}
            if(bean.getBusinessScope().startsWith("L")) {
            	listTwo = mapper.getCustomerInfoLcDetail(bean);
    		}
            if(bean.getBusinessScope().startsWith("I")) {
            	listTwo = mapper.getCustomerInfoIoDetail(bean);
    		}
            
        	for(CustomerInContributionInfo info:list) {
        		planChargeWeight = planChargeWeight.add(info.getPlanChargeWeight());
        		incomeFunctionalAmountCount = incomeFunctionalAmountCount.add(info.getIncomeFunctionalAmountCount());
        		costFunctionalAmountCount = costFunctionalAmountCount.add(info.getCostFunctionalAmountCount());
        		grossProfit = grossProfit.add(info.getGrossProfit());
//        		unitGrossProfit = unitGrossProfit.add(info.getUnitGrossProfit());
        		//始发港
        		if(mapDep!=null&&mapDep.containsKey(info.getDepartureStation())) {
        			BigDecimal[] arrDepInfo = mapDep.get(info.getDepartureStation());
        			arrDepInfo[0]= arrDepInfo[0].add(new BigDecimal(1));
        			arrDepInfo[1]= arrDepInfo[1].add(info.getPlanChargeWeight());
        			arrDepInfo[2]= arrDepInfo[2].add(info.getIncomeFunctionalAmountCount());
        			arrDepInfo[3]= arrDepInfo[3].add(info.getGrossProfit());
        			arrDepInfo[4]= arrDepInfo[4].add(info.getCostFunctionalAmountCount());
        			mapDep.put(info.getDepartureStation(), arrDepInfo);
        		}else {
        			BigDecimal[] arrDepInfo = new BigDecimal[] {new BigDecimal(1),info.getPlanChargeWeight(),info.getIncomeFunctionalAmountCount(),info.getGrossProfit(),info.getCostFunctionalAmountCount()};
        			mapDep.put(info.getDepartureStation(), arrDepInfo);
        		}
        	    //目的港
        		if(mapArr!=null&&mapArr.containsKey(info.getArrivalStation())) {
        			BigDecimal[] arrDepInfo = mapArr.get(info.getArrivalStation());
        			arrDepInfo[0]= arrDepInfo[0].add(new BigDecimal(1));
        			arrDepInfo[1]= arrDepInfo[1].add(info.getPlanChargeWeight());
        			arrDepInfo[2]= arrDepInfo[2].add(info.getIncomeFunctionalAmountCount());
        			arrDepInfo[3]= arrDepInfo[3].add(info.getGrossProfit());
        			arrDepInfo[4]= arrDepInfo[4].add(info.getCostFunctionalAmountCount());
        			mapArr.put(info.getArrivalStation(), arrDepInfo);
        		}else {	
        			BigDecimal[] arrDepInfo = new BigDecimal[] {new BigDecimal(1),info.getPlanChargeWeight(),info.getIncomeFunctionalAmountCount(),info.getGrossProfit(),info.getCostFunctionalAmountCount()};
        			mapArr.put(info.getArrivalStation(), arrDepInfo);
        		}
        		//经纬度
        		if(info.getDepLongitude()!=null&&info.getDepLatitude()!=null) {
        			if(mapDepL!=null&&mapDepL.containsKey(info.getDepartureStation())) {
            			//todo
            		}else {
            			BigDecimal[] arrDepInfo =new BigDecimal[] {info.getDepLongitude(),info.getDepLatitude()};
            			mapDepL.put(info.getDepartureStation(), arrDepInfo);
            		}
        		}
        		if(info.getArrLongitude()!=null&&info.getArrLatitude()!=null) {
        			if(mapArrL!=null&&mapArrL.containsKey(info.getArrivalStation())) {
            			//todo
            		}else {
            			BigDecimal[] arrDepInfo =new BigDecimal[] {info.getArrLongitude(),info.getArrLatitude()};
            			mapArrL.put(info.getArrivalStation(), arrDepInfo);
            		}
        		}
        		
        		poll++;
        	}
        	
        	if(planChargeWeight.compareTo(BigDecimal.ZERO)>0) {
        		unitGrossProfit = grossProfit.divide(planChargeWeight,2,BigDecimal.ROUND_HALF_UP);
        	}
        	if(incomeFunctionalAmountCount.compareTo(BigDecimal.ZERO)>0) {
        		grossProfitMargin = grossProfit.multiply(new BigDecimal(100)).divide(incomeFunctionalAmountCount,2,BigDecimal.ROUND_HALF_UP);
        	}
        	
            //遍历前推时间段的数据
        	for(CustomerInContributionInfo info:listTwo) {
        		planChargeWeightTwo = planChargeWeightTwo.add(info.getPlanChargeWeight());
        		incomeFunctionalAmountCountTwo = incomeFunctionalAmountCountTwo.add(info.getIncomeFunctionalAmountCount());
        		costFunctionalAmountCountTwo = costFunctionalAmountCountTwo.add(info.getCostFunctionalAmountCount());
        		grossProfitTwo = grossProfitTwo.add(info.getGrossProfit());
        		//目的港
        		if(mapArrTwo!=null&&mapArrTwo.containsKey(info.getArrivalStation())) {
        			BigDecimal[] arrDepInfo = mapArrTwo.get(info.getArrivalStation());
        			arrDepInfo[0]= arrDepInfo[0].add(new BigDecimal(1));
        			arrDepInfo[1]= arrDepInfo[1].add(info.getPlanChargeWeight());
        			arrDepInfo[2]= arrDepInfo[2].add(info.getIncomeFunctionalAmountCount());
        			arrDepInfo[3]= arrDepInfo[3].add(info.getGrossProfit());
        			arrDepInfo[4]= arrDepInfo[4].add(info.getCostFunctionalAmountCount());
        			mapArrTwo.put(info.getArrivalStation(), arrDepInfo);
        		}else {	
        			BigDecimal[] arrDepInfo = new BigDecimal[] {new BigDecimal(1),info.getPlanChargeWeight(),info.getIncomeFunctionalAmountCount(),info.getGrossProfit(),info.getCostFunctionalAmountCount()};
        			mapArrTwo.put(info.getArrivalStation(), arrDepInfo);
        		}
        		
        		pollTwo++;
        	}
        	
        	if(planChargeWeightTwo.compareTo(BigDecimal.ZERO)>0) {
        		unitGrossProfitTwo = grossProfitTwo.divide(planChargeWeightTwo,2,BigDecimal.ROUND_HALF_UP);
        	}
        	if(incomeFunctionalAmountCountTwo.compareTo(BigDecimal.ZERO)>0) {
        		grossProfitMarginTwo = grossProfitTwo.multiply(new BigDecimal(100)).divide(incomeFunctionalAmountCountTwo,2,BigDecimal.ROUND_HALF_UP);
        	}
        	
        	pollTwo = poll-pollTwo;
            planChargeWeightTwo = planChargeWeight.subtract(planChargeWeightTwo);
            incomeFunctionalAmountCountTwo = incomeFunctionalAmountCount.subtract(incomeFunctionalAmountCountTwo);
            costFunctionalAmountCountTwo = costFunctionalAmountCount.subtract(costFunctionalAmountCountTwo);
            grossProfitTwo = grossProfit.subtract(grossProfitTwo);
            unitGrossProfitTwo = unitGrossProfit.subtract(unitGrossProfitTwo);
            grossProfitMarginTwo = grossProfitMargin.subtract(grossProfitMarginTwo);
        	//遍历 始发港
        	if(mapDep!=null) {
        		for(String key:mapDep.keySet()) {
        			Object[] obj = new Object[8];
        			obj[0] = key;//港
        			obj[1] = mapDep.get(key)[0];//票数
        			if(bean.getBusinessScope().startsWith("A")||bean.getBusinessScope().startsWith("L")||bean.getBusinessScope().startsWith("I")) {
        				obj[2] = mapDep.get(key)[1].divide(new BigDecimal(1000),2,BigDecimal.ROUND_HALF_UP);//计重
        			}
        			if(bean.getBusinessScope().startsWith("S")||bean.getBusinessScope().startsWith("T")) {
        				obj[2] = mapDep.get(key)[1];
        			}
        			obj[3] = mapDep.get(key)[2].divide(new BigDecimal(10000),2,BigDecimal.ROUND_HALF_UP);//收入	
        			obj[4] = mapDep.get(key)[3].divide(new BigDecimal(10000),2,BigDecimal.ROUND_HALF_UP);//毛利
        			if(mapDep.get(key)[1].compareTo(BigDecimal.ZERO)>0) {
        			  obj[5] = mapDep.get(key)[3].divide(mapDep.get(key)[1],2,BigDecimal.ROUND_HALF_UP);//单位毛利
        			}else {
        				obj[5] = 0;
        			}
        			if(mapDep.get(key)[2].compareTo(BigDecimal.ZERO)>0) {//毛利率
        				obj[6] = mapDep.get(key)[3].multiply(new BigDecimal(100)).divide(mapDep.get(key)[2],2,BigDecimal.ROUND_HALF_UP);
        			}else {
        				obj[6] = 0;
        			}
        			obj[7] = mapDep.get(key)[4].divide(new BigDecimal(10000),2,BigDecimal.ROUND_HALF_UP);//成本
        			dep.add(obj);
        		}
        	}
        	if(mapArr!=null) {
        		for(String key:mapArr.keySet()) {
        			Map mapCount = new HashMap();
        			Object[] obj = new Object[8];
        			obj[0] = key;//港
        			mapCount.put("arr", key);
        			obj[1] = mapArr.get(key)[0];//票数
        			mapCount.put("piao", obj[1]);
        			if(mapArrTwo!=null&&mapArrTwo.containsKey(key)) {
        				mapCount.put("piaoTwo", mapArr.get(key)[0].subtract(mapArrTwo.get(key)[0]));
        			}else {
        				mapCount.put("piaoTwo", mapArr.get(key)[0]);
        			}
        			if(bean.getBusinessScope().startsWith("A")||bean.getBusinessScope().startsWith("L")||bean.getBusinessScope().startsWith("I")) {
        				obj[2] = mapArr.get(key)[1].divide(new BigDecimal(1000),2,BigDecimal.ROUND_HALF_UP);//计重
        				mapCount.put("weight", obj[2]);
        				if(mapArrTwo!=null&&mapArrTwo.containsKey(key)) {
        					mapCount.put("weightTwo", (mapArr.get(key)[1].subtract(mapArrTwo.get(key)[1])).divide(new BigDecimal(1000),2,BigDecimal.ROUND_HALF_UP));
            			}else {
            				mapCount.put("weightTwo",obj[2]);
            			}
            			
        			}
        			if(bean.getBusinessScope().startsWith("S")||bean.getBusinessScope().startsWith("T")) {
        				obj[2] = mapArr.get(key)[1];//计重
        				mapCount.put("weight", obj[2]);
        				if(mapArrTwo!=null&&mapArrTwo.containsKey(key)) {
        					mapCount.put("weightTwo", mapArr.get(key)[1].subtract(mapArrTwo.get(key)[1]));
            			}else {
            				mapCount.put("weightTwo", obj[2]);
            			}
        				
        			}
        			obj[3] = mapArr.get(key)[2].divide(new BigDecimal(10000),2,BigDecimal.ROUND_HALF_UP);//收入
        			mapCount.put("shouru", obj[3]);
        			if(mapArrTwo!=null&&mapArrTwo.containsKey(key)) {
        				mapCount.put("shouruTwo", (mapArr.get(key)[2].subtract(mapArrTwo.get(key)[2])).divide(new BigDecimal(10000),2,BigDecimal.ROUND_HALF_UP));
        			}else {
        				mapCount.put("shouruTwo",obj[3]);
        			}
        			obj[4] = mapArr.get(key)[3].divide(new BigDecimal(10000),2,BigDecimal.ROUND_HALF_UP);//毛利
        			mapCount.put("maoli", obj[4]);
        			if(mapArrTwo!=null&&mapArrTwo.containsKey(key)) {
            			mapCount.put("maoliTwo", (mapArr.get(key)[3].subtract(mapArrTwo.get(key)[3])).divide(new BigDecimal(10000),2,BigDecimal.ROUND_HALF_UP));
        			}else {
        				mapCount.put("maoliTwo",obj[4]);
        			}
        			if(mapArr.get(key)[1].compareTo(BigDecimal.ZERO)!=0) {
        			  obj[5] = mapArr.get(key)[3].divide(mapArr.get(key)[1],2,BigDecimal.ROUND_HALF_UP);//单位毛利
        			  mapCount.put("danweimaoli", obj[5]);
        			  if(mapArrTwo!=null&&mapArrTwo.containsKey(key)&&mapArrTwo.get(key)[1].compareTo(BigDecimal.ZERO)!=0) {
        				  mapCount.put("danweimaoliTwo", (mapArr.get(key)[3].divide(mapArr.get(key)[1],2,BigDecimal.ROUND_HALF_UP)).subtract(mapArrTwo.get(key)[3].divide(mapArrTwo.get(key)[1],2,BigDecimal.ROUND_HALF_UP))); 
        			  }else {
        				  mapCount.put("danweimaoliTwo", obj[5]);
        			  }
        			}else {
        				obj[5] = 0;
        				mapCount.put("danweimaoli", obj[5]);
        				if(mapArrTwo!=null&&mapArrTwo.containsKey(key)&&mapArrTwo.get(key)[1].compareTo(BigDecimal.ZERO)!=0) {
        					mapCount.put("danweimaoliTwo",mapArrTwo.get(key)[3].divide(mapArrTwo.get(key)[1],2,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(-1))); 
        				}else {
        					mapCount.put("danweimaoliTwo", obj[5]);
        				}
        			}
        			if(mapArr.get(key)[2].compareTo(BigDecimal.ZERO)!=0) {//毛利率
        				obj[6] = mapArr.get(key)[3].multiply(new BigDecimal(100)).divide(mapArr.get(key)[2],2,BigDecimal.ROUND_HALF_UP);
        				mapCount.put("maolilv", obj[6]);
	          			if(mapArrTwo!=null&&mapArrTwo.containsKey(key)&&mapArrTwo.get(key)[2].compareTo(BigDecimal.ZERO)!=0) {
	          				  mapCount.put("maolilvTwo", (mapArr.get(key)[3].multiply(new BigDecimal(100)).divide(mapArr.get(key)[2],2,BigDecimal.ROUND_HALF_UP)).subtract(mapArrTwo.get(key)[3].multiply(new BigDecimal(100)).divide(mapArrTwo.get(key)[2],2,BigDecimal.ROUND_HALF_UP))); 
	          			}else {
	          				  mapCount.put("maolilvTwo", obj[6]);
	          			}
        			
        			}else {
        				obj[6] = 0;
        				mapCount.put("maolilv", obj[6]);
        				if(mapArrTwo!=null&&mapArrTwo.containsKey(key)&&mapArrTwo.get(key)[2].compareTo(BigDecimal.ZERO)!=0) {
        					mapCount.put("maolilvTwo",mapArrTwo.get(key)[3].divide(mapArrTwo.get(key)[2],2,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(-1))); 
        				}else {
        					mapCount.put("maolilvTwo", obj[6]);
        				}
        			}
        			obj[7] = mapArr.get(key)[4].divide(new BigDecimal(10000),2,BigDecimal.ROUND_HALF_UP);//成本
        			mapCount.put("chengben", obj[7]);
        			if(mapArrTwo!=null&&mapArrTwo.containsKey(key)) {
            			mapCount.put("chengbenTwo", (mapArr.get(key)[4].subtract(mapArrTwo.get(key)[4])).divide(new BigDecimal(10000),2,BigDecimal.ROUND_HALF_UP));
        			}else {
        				mapCount.put("chengbenTwo", obj[7]);
        			}
        			arr.add(obj);
        			listCount.add(mapCount);
        		}
        	}
        }
        Map mapResult = new HashMap();
        mapResult.put("poll", poll);//票数
        if(bean.getBusinessScope().startsWith("A")||bean.getBusinessScope().startsWith("L")||bean.getBusinessScope().startsWith("I")) {
        	planChargeWeight = planChargeWeight.divide(new BigDecimal(1000),2,BigDecimal.ROUND_HALF_UP);//计重
		}
        mapResult.put("planChargeWeight", planChargeWeight);//计重
        mapResult.put("incomeFunctionalAmountCount", incomeFunctionalAmountCount.divide(new BigDecimal(10000),2,BigDecimal.ROUND_HALF_UP));//收入
        mapResult.put("costFunctionalAmountCount", costFunctionalAmountCount.divide(new BigDecimal(10000),2,BigDecimal.ROUND_HALF_UP));//收入
        mapResult.put("grossProfit", grossProfit.divide(new BigDecimal(10000),2,BigDecimal.ROUND_HALF_UP));//毛利
        mapResult.put("unitGrossProfit", unitGrossProfit);//单位毛利
        mapResult.put("grossProfitMargin", grossProfitMargin);//毛利率
        mapResult.put("mapDep", dep);//始发info
        mapResult.put("mapArr", arr);//目的info
        mapResult.put("mapDepL", mapDepL);//始发 经纬度
        mapResult.put("mapArrL", mapArrL);//目的经纬度
        
        mapResult.put("mapCount", listCount);//统计
        //当前数据跟前推数据之差
        mapResult.put("pollTwo", pollTwo);//票数
        if(bean.getBusinessScope().startsWith("A")||bean.getBusinessScope().startsWith("L")||bean.getBusinessScope().startsWith("I")) {
        	planChargeWeightTwo = planChargeWeightTwo.divide(new BigDecimal(1000),2,BigDecimal.ROUND_HALF_UP);//计重
		}
        mapResult.put("planChargeWeightTwo", planChargeWeightTwo);//计重
        mapResult.put("incomeFunctionalAmountCountTwo", incomeFunctionalAmountCountTwo.divide(new BigDecimal(10000),2,BigDecimal.ROUND_HALF_UP));//收入
        mapResult.put("costFunctionalAmountCountTwo", costFunctionalAmountCountTwo.divide(new BigDecimal(10000),2,BigDecimal.ROUND_HALF_UP));//成本
        mapResult.put("grossProfitTwo", grossProfitTwo.divide(new BigDecimal(10000),2,BigDecimal.ROUND_HALF_UP));//毛利
        mapResult.put("unitGrossProfitTwo", unitGrossProfitTwo);//单位毛利
        mapResult.put("grossProfitMarginTwo", grossProfitMarginTwo);//毛利率
        mapResult.put("customer_info_list", listInfo);
		return mapResult;
	}
	
//	private List<CustomerInContributionInfo>  copyCustomerInContributionInfo(List<Map> list){
////		List<CustomerInContributionInfo> listResult = new ArrayList<CustomerInContributionInfo>();
//		List<CustomerInContributionInfo> listResult = list.stream().map(a -> {
//			CustomerInContributionInfo b = new CustomerInContributionInfo();
//            b.setDepartureStation(a.get("departure_station")==null?"":a.get("departure_station").toString());
//            b.setArrivalStation(a.get("arrival_station")==null?"":a.get("arrival_station").toString());
//            b.setPlanChargeWeight(new BigDecimal(a.get("plan_charge_weight").toString()));
//            b.setIncomeFunctionalAmountCount(new BigDecimal(a.get("income_functional_amount_count").toString()));
//            b.setGrossProfit(new BigDecimal(a.get("gross_profit").toString()));
//            b.setUnitGrossProfit(new BigDecimal(a.get("unit_gross_profit").toString()));
//            b.setGrossProfitMargin(a.get("gross_profit_margin").toString());
//            b.setDepLongitude(a.get("dep_longitude")==null?null:new BigDecimal(a.get("dep_longitude").toString()));
//            b.setDepLatitude(a.get("dep_latitude")==null?null:new BigDecimal(a.get("dep_latitude").toString()));
//            b.setArrLongitude(a.get("arr_longitude")==null?null:new BigDecimal(a.get("arr_longitude").toString()));
//            b.setArrLatitude(a.get("arr_latitude")==null?null:new BigDecimal(a.get("arr_latitude").toString()));
//            b.setCostFunctionalAmountCount(new BigDecimal(a.get("cost_functional_amount_count").toString()));
//            return b;
//        }).collect(Collectors.toList());
//		return listResult;
//	}
}
