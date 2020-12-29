package com.efreight.afbase.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.CssPReportSettleAfMapper;
import com.efreight.afbase.dao.ServiceMapper;
import com.efreight.afbase.entity.CssPReportSettleAfExcel;
import com.efreight.afbase.entity.CssPReportSettleAfExcelAI;
import com.efreight.afbase.entity.CssPReportSettleAfExcelSE;
import com.efreight.afbase.entity.CssPReportSettleAfExcelSI;
import com.efreight.afbase.entity.CssPReportSettleExcel;
import com.efreight.afbase.entity.procedure.CssPReportSettleAfProcedure;
import com.efreight.afbase.service.CssPReportSettleAfService;
import com.efreight.common.security.util.SecurityUtils;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;


@Service
@AllArgsConstructor
public class CssPReportSettleAfServiceImpl extends ServiceImpl<CssPReportSettleAfMapper, CssPReportSettleAfProcedure> implements CssPReportSettleAfService {
    private final CssPReportSettleAfMapper cssPReportSettleAfMapper;
    private final ServiceMapper serviceMapper;

    @Override
    public HashMap getListPage(Page page, CssPReportSettleAfProcedure bean) {
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
        bean.setCurrentUserId(SecurityUtils.getUser().getId());
        if (StrUtil.isBlank(bean.getCoopName())) {
            bean.setCoopName(null);
        }
        if (StrUtil.isBlank(bean.getFlightDateStart())) {
            bean.setFlightDateStart(null);
        }
        if (StrUtil.isBlank(bean.getFlightDateEnd())) {
            bean.setFlightDateEnd(null);
        } else {
            bean.setFlightDateEnd(bean.getFlightDateEnd() + " 23:59:59");
        }
        if (StrUtil.isBlank(bean.getGrossProfitStr())) {
            bean.setGrossProfitStr(null);
        }
        if (StrUtil.isBlank(bean.getAwbFromType())) {
            bean.setAwbFromType(null);
        }
        if (StrUtil.isBlank(bean.getAwbFromName())) {
            bean.setAwbFromName(null);
        }
        if (StrUtil.isBlank(bean.getFinancialDateStart())) {
            bean.setFinancialDateStart(null);
        }
        if (StrUtil.isBlank(bean.getFinancialDateEnd())) {
            bean.setFinancialDateEnd(null);
        }
        if (StrUtil.isBlank(bean.getDepartureStation())) {
            bean.setDepartureStation(null);
        }
        if (StrUtil.isBlank(bean.getArrivalStation())) {
            bean.setArrivalStation(null);
        }
        if (StrUtil.isBlank(bean.getTransitStation())) {
            bean.setTransitStation(null);
        }
        if (StrUtil.isBlank(bean.getBusinessProduct())) {
            bean.setBusinessProduct(null);
        }
        if (StrUtil.isBlank(bean.getGoodsType())) {
            bean.setGoodsType(null);
        }
        if (StrUtil.isBlank(bean.getAwbNumber())) {
            bean.setAwbNumber(null);
        }
        if (StrUtil.isBlank(bean.getOrderCode())) {
            bean.setOrderCode(null);
        }
        if (StrUtil.isBlank(bean.getRoutingName())) {
            bean.setRoutingName(null);
        }
        if (StrUtil.isBlank(bean.getSalesName())) {
            bean.setSalesName(null);
        }
        if (StrUtil.isBlank(bean.getServicerName())) {
            bean.setServicerName(null);
        }
        if (StrUtil.isBlank(bean.getRoutingPersonName())) {
            bean.setRoutingPersonName(null);
        }
        if (StrUtil.isBlank(bean.getOrderStatus())) {
            bean.setOrderStatus(null);
        }
        if (StrUtil.isBlank(bean.getExpectFlight())) {
            bean.setExpectFlight(null);
        }
        if (StrUtil.isBlank(bean.getShipVoyageNumber())) {
            bean.setShipVoyageNumber(null);
        }
        if (StrUtil.isBlank(bean.getSalesDep())) {
            bean.setSalesDep(null);
        }
        if (StrUtil.isBlank(bean.getCustomerNumber())) {
            bean.setCustomerNumber(null);
        }
        if (StrUtil.isBlank(bean.getGoodsSourceCode())) {
            bean.setGoodsSourceCode(null);
        }
        if (StrUtil.isBlank(bean.getExitPort())) {
            bean.setExitPort(null);
        }
        if (StrUtil.isBlank(bean.getShippingMethod())) {
            bean.setShippingMethod(null);
        }

        if(StrUtil.isBlank(bean.getBusinessMethod())){
            bean.setBusinessMethod(null);
        }
//		 IPage<CssPReportSettleAfProcedure> iPage = new Page<>(page.getCurrent(), page.getSize());
        List<List<Map<String, String>>> list = new ArrayList<List<Map<String, String>>>();
        if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
            list = cssPReportSettleAfMapper.getListPage(bean);
        } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
            list = cssPReportSettleAfMapper.getListPageSC(bean);
        } else if ("TE".equals(bean.getBusinessScope())) {
            list = cssPReportSettleAfMapper.getListPageTC(bean);
        } else if ("LC".equals(bean.getBusinessScope())) {
            list = cssPReportSettleAfMapper.getListPageLC(bean);
        } else if ("IO".equals(bean.getBusinessScope())) {
            list = cssPReportSettleAfMapper.getListPageIO(bean);
        }

        List<Map<String, String>> result = new ArrayList<>();
        HashMap map = new HashMap();
        if (list != null && list.size() > 0 && list.get(0) != null && list.get(0).size() > 0) {
            result.add(list.get(1).get(0));
            map.put("resultOne", list.get(0));
            map.put("resultTwo", list.get(1).get(0));
        }
        return map;
    }

    @Override
    public List<CssPReportSettleAfExcel> getListForExcel(CssPReportSettleAfProcedure bean) {
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
        bean.setCurrentUserId(SecurityUtils.getUser().getId());
        if (StrUtil.isBlank(bean.getCoopName())) {
            bean.setCoopName(null);
        }
        if (StrUtil.isBlank(bean.getFlightDateStart())) {
            bean.setFlightDateStart(null);
        }
        if (StrUtil.isBlank(bean.getFlightDateEnd())) {
            bean.setFlightDateEnd(null);
        }
        if (StrUtil.isBlank(bean.getGrossProfitStr())) {
            bean.setGrossProfitStr(null);
        }
//		 if(StrUtil.isBlank(bean.getAwbFrom())) {
//			 bean.setAwbFrom(null);
//		 }
        if (StrUtil.isBlank(bean.getAwbFromType())) {
            bean.setAwbFromType(null);
        }
        if (StrUtil.isBlank(bean.getAwbFromName())) {
            bean.setAwbFromName(null);
        }
        if (StrUtil.isBlank(bean.getFinancialDateStart())) {
            bean.setFinancialDateStart(null);
        }
        if (StrUtil.isBlank(bean.getFinancialDateEnd())) {
            bean.setFinancialDateEnd(null);
        }
        if (StrUtil.isBlank(bean.getDepartureStation())) {
            bean.setDepartureStation(null);
        }
        if (StrUtil.isBlank(bean.getArrivalStation())) {
            bean.setArrivalStation(null);
        }
        if (StrUtil.isBlank(bean.getTransitStation())) {
            bean.setTransitStation(null);
        }
        if (StrUtil.isBlank(bean.getBusinessProduct())) {
            bean.setBusinessProduct(null);
        }
        if (StrUtil.isBlank(bean.getGoodsType())) {
            bean.setGoodsType(null);
        }
        if (StrUtil.isBlank(bean.getAwbNumber())) {
            bean.setAwbNumber(null);
        }
        if (StrUtil.isBlank(bean.getOrderCode())) {
            bean.setOrderCode(null);
        }
        if (StrUtil.isBlank(bean.getRoutingName())) {
            bean.setRoutingName(null);
        }
        if (StrUtil.isBlank(bean.getSalesName())) {
            bean.setSalesName(null);
        }
        if (StrUtil.isBlank(bean.getServicerName())) {
            bean.setServicerName(null);
        }
        if (StrUtil.isBlank(bean.getRoutingPersonName())) {
            bean.setRoutingPersonName(null);
        }
        if (StrUtil.isBlank(bean.getOrderStatus())) {
            bean.setOrderStatus(null);
        }
        if (StrUtil.isBlank(bean.getExpectFlight())) {
            bean.setExpectFlight(null);
        }
        if (StrUtil.isBlank(bean.getShipVoyageNumber())) {
            bean.setShipVoyageNumber(null);
        }
        if (StrUtil.isBlank(bean.getSalesDep())) {
            bean.setSalesDep(null);
        }
        if (StrUtil.isBlank(bean.getCustomerNumber())) {
            bean.setCustomerNumber(null);
        }
        if (StrUtil.isBlank(bean.getGoodsSourceCode())) {
            bean.setGoodsSourceCode(null);
        }
        if (StrUtil.isBlank(bean.getExitPort())) {
            bean.setExitPort(null);
        }
        if (StrUtil.isBlank(bean.getShippingMethod())) {
            bean.setShippingMethod(null);
        }
        List<List<Map<String, String>>> list = cssPReportSettleAfMapper.getListPage(bean);
        List<CssPReportSettleAfExcel> listR = cssPReportSettleAfMapper.getListPageExcel(bean);
        List<Map<String, String>> result = new ArrayList<>();
        if (list != null && list.size() > 0 && list.get(0) != null && list.get(0).size() > 0) {
//			 result.addAll(list.get(0)); 
//			 list.get(1).get(0).put("businessScope","合计");
            CssPReportSettleAfExcel ce = new CssPReportSettleAfExcel();
            ce.setBusinessScope("合计");
            ce.setPieces(list.get(1).get(0).get("pieces"));
            ce.setWeight(list.get(1).get(0).get("weight"));
            ce.setChargeWeight(list.get(1).get(0).get("chargeWeight"));
            ce.setIncomeFunctionalAmount(list.get(1).get(0).get("incomeFunctionalAmount"));
            ce.setCostFunctionalAmount(list.get(1).get(0).get("costFunctionalAmount"));
            ce.setGrossProfit(list.get(1).get(0).get("grossProfit"));
            ce.setMainRouting(list.get(1).get(0).get("main_routing"));
            ce.setMainRoutingIncome(list.get(1).get(0).get("main_routing_income"));
            ce.setMainRoutingCost(list.get(1).get(0).get("main_routing_cost"));
            ce.setFeeder(list.get(1).get(0).get("feeder"));
            ce.setFeederIncome(list.get(1).get(0).get("feeder_income"));
            ce.setFeederCost(list.get(1).get(0).get("feeder_cost"));
            ce.setOperation(list.get(1).get(0).get("operation"));
            ce.setOperationCost(list.get(1).get(0).get("operation_cost"));
            ce.setOperationIncome(list.get(1).get(0).get("operation_income"));
            ce.setPackaging(list.get(1).get(0).get("packaging"));
            ce.setPackagingIncome(list.get(1).get(0).get("packaging_income"));
            ce.setPackagingCost(list.get(1).get(0).get("packaging_cost"));
            ce.setStorage(list.get(1).get(0).get("storage"));
            ce.setStorageIncome(list.get(1).get(0).get("storage_income"));
            ce.setStorageCost(list.get(1).get(0).get("storage_cost"));
            ce.setPostage(list.get(1).get(0).get("postage"));
            ce.setPostageIncome(list.get(1).get(0).get("postage_income"));
            ce.setPostageCost(list.get(1).get(0).get("postage_cost"));
            ce.setClearance(list.get(1).get(0).get("clearance"));
            ce.setClearanceIncome(list.get(1).get(0).get("clearance_income"));
            ce.setClearanceCost(list.get(1).get(0).get("clearance_cost"));
            ce.setExchange(list.get(1).get(0).get("exchange"));
            ce.setExchangeIncome(list.get(1).get(0).get("exchange_income"));
            ce.setExchangeCost(list.get(1).get(0).get("exchange_cost"));
            ce.setOrderCode("共" + list.get(1).get(0).get("order_sum") + "票");
//             result.add(list.get(1).get(0)); 
            listR.add(ce);

        }
        return listR;
    }

    @Override
    public List<CssPReportSettleAfExcelAI> getListForExcelAI(CssPReportSettleAfProcedure bean) {
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
        bean.setCurrentUserId(SecurityUtils.getUser().getId());
        if (StrUtil.isBlank(bean.getCoopName())) {
            bean.setCoopName(null);
        }
        if (StrUtil.isBlank(bean.getFlightDateStart())) {
            bean.setFlightDateStart(null);
        }
        if (StrUtil.isBlank(bean.getFlightDateEnd())) {
            bean.setFlightDateEnd(null);
        }
        if (StrUtil.isBlank(bean.getGrossProfitStr())) {
            bean.setGrossProfitStr(null);
        }
//		 if(StrUtil.isBlank(bean.getAwbFrom())) {
//			 bean.setAwbFrom(null);
//		 }
        if (StrUtil.isBlank(bean.getAwbFromType())) {
            bean.setAwbFromType(null);
        }
        if (StrUtil.isBlank(bean.getAwbFromName())) {
            bean.setAwbFromName(null);
        }
        if (StrUtil.isBlank(bean.getFinancialDateStart())) {
            bean.setFinancialDateStart(null);
        }
        if (StrUtil.isBlank(bean.getFinancialDateEnd())) {
            bean.setFinancialDateEnd(null);
        }
        if (StrUtil.isBlank(bean.getDepartureStation())) {
            bean.setDepartureStation(null);
        }
        if (StrUtil.isBlank(bean.getArrivalStation())) {
            bean.setArrivalStation(null);
        }
        if (StrUtil.isBlank(bean.getTransitStation())) {
            bean.setTransitStation(null);
        }
        if (StrUtil.isBlank(bean.getBusinessProduct())) {
            bean.setBusinessProduct(null);
        }
        if (StrUtil.isBlank(bean.getGoodsType())) {
            bean.setGoodsType(null);
        }
        if (StrUtil.isBlank(bean.getAwbNumber())) {
            bean.setAwbNumber(null);
        }
        if (StrUtil.isBlank(bean.getOrderCode())) {
            bean.setOrderCode(null);
        }
        if (StrUtil.isBlank(bean.getRoutingName())) {
            bean.setRoutingName(null);
        }
        if (StrUtil.isBlank(bean.getSalesName())) {
            bean.setSalesName(null);
        }
        if (StrUtil.isBlank(bean.getServicerName())) {
            bean.setServicerName(null);
        }
        if (StrUtil.isBlank(bean.getRoutingPersonName())) {
            bean.setRoutingPersonName(null);
        }
        if (StrUtil.isBlank(bean.getOrderStatus())) {
            bean.setOrderStatus(null);
        }
        if (StrUtil.isBlank(bean.getExpectFlight())) {
            bean.setExpectFlight(null);
        }
        if (StrUtil.isBlank(bean.getShipVoyageNumber())) {
            bean.setShipVoyageNumber(null);
        }
        if (StrUtil.isBlank(bean.getSalesDep())) {
            bean.setSalesDep(null);
        }
        if (StrUtil.isBlank(bean.getCustomerNumber())) {
            bean.setCustomerNumber(null);
        }
        if (StrUtil.isBlank(bean.getGoodsSourceCode())) {
            bean.setGoodsSourceCode(null);
        }
//        List<List<Map<String, String>>> list = cssPReportSettleAfMapper.getListPage(bean);
//        List<CssPReportSettleAfExcel> listR = cssPReportSettleAfMapper.getListPageExcel(bean);

        List<List<Map<String, String>>> list = cssPReportSettleAfMapper.getListPage(bean);
        List<CssPReportSettleAfExcelAI> listR = cssPReportSettleAfMapper.getListPageExcelAI(bean);
//    	if ("AE".equals(bean.getBusinessScope()) ||"AI".equals(bean.getBusinessScope())) {
//    		list =cssPReportSettleAfMapper.getListPage(bean);
//    		listR = cssPReportSettleAfMapper.getListPageExcelAI(bean);
//    	} else if ("SE".equals(bean.getBusinessScope()) ||"SI".equals(bean.getBusinessScope())){
//    		list =cssPReportSettleAfMapper.getListPageSC(bean);
//    		listR = cssPReportSettleAfMapper.getListPageExcelAISC(bean);
//    	}
        List<Map<String, String>> result = new ArrayList<>();
        if (list != null && list.size() > 0 && list.get(0) != null && list.get(0).size() > 0) {
//			 result.addAll(list.get(0)); 
//			 list.get(1).get(0).put("businessScope","合计");
            CssPReportSettleAfExcelAI ce = new CssPReportSettleAfExcelAI();
            ce.setBusinessScope("合计");
            ce.setPieces(list.get(1).get(0).get("pieces"));
            ce.setWeight(list.get(1).get(0).get("weight"));
            ce.setChargeWeight(list.get(1).get(0).get("chargeWeight"));
            ce.setIncomeFunctionalAmount(list.get(1).get(0).get("incomeFunctionalAmount"));
            ce.setCostFunctionalAmount(list.get(1).get(0).get("costFunctionalAmount"));
            ce.setGrossProfit(list.get(1).get(0).get("grossProfit"));
            ce.setMainRouting(list.get(1).get(0).get("main_routing"));
            ce.setMainRoutingIncome(list.get(1).get(0).get("main_routing_income"));
            ce.setMainRoutingCost(list.get(1).get(0).get("main_routing_cost"));
            ce.setFeeder(list.get(1).get(0).get("feeder"));
            ce.setFeederIncome(list.get(1).get(0).get("feeder_income"));
            ce.setFeederCost(list.get(1).get(0).get("feeder_cost"));
            ce.setOperation(list.get(1).get(0).get("operation"));
            ce.setOperationCost(list.get(1).get(0).get("operation_cost"));
            ce.setOperationIncome(list.get(1).get(0).get("operation_income"));
            ce.setPackaging(list.get(1).get(0).get("packaging"));
            ce.setPackagingIncome(list.get(1).get(0).get("packaging_income"));
            ce.setPackagingCost(list.get(1).get(0).get("packaging_cost"));
            ce.setStorage(list.get(1).get(0).get("storage"));
            ce.setStorageIncome(list.get(1).get(0).get("storage_income"));
            ce.setStorageCost(list.get(1).get(0).get("storage_cost"));
            ce.setPostage(list.get(1).get(0).get("postage"));
            ce.setPostageIncome(list.get(1).get(0).get("postage_income"));
            ce.setPostageCost(list.get(1).get(0).get("postage_cost"));
            ce.setClearance(list.get(1).get(0).get("clearance"));
            ce.setClearanceIncome(list.get(1).get(0).get("clearance_income"));
            ce.setClearanceCost(list.get(1).get(0).get("clearance_cost"));
            ce.setExchange(list.get(1).get(0).get("exchange"));
            ce.setExchangeIncome(list.get(1).get(0).get("exchange_income"));
            ce.setExchangeCost(list.get(1).get(0).get("exchange_cost"));
            ce.setOrderCode("共" + list.get(1).get(0).get("order_sum") + "票");
//             result.add(list.get(1).get(0)); 
            listR.add(ce);

        }
        return listR;
    }

    @Override
    public List<CssPReportSettleAfExcelSE> getListForExcelSE(CssPReportSettleAfProcedure bean) {
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
        bean.setCurrentUserId(SecurityUtils.getUser().getId());
        if (StrUtil.isBlank(bean.getCoopName())) {
            bean.setCoopName(null);
        }
        if (StrUtil.isBlank(bean.getFlightDateStart())) {
            bean.setFlightDateStart(null);
        }
        if (StrUtil.isBlank(bean.getFlightDateEnd())) {
            bean.setFlightDateEnd(null);
        }
        if (StrUtil.isBlank(bean.getGrossProfitStr())) {
            bean.setGrossProfitStr(null);
        }
//		 if(StrUtil.isBlank(bean.getAwbFrom())) {
//			 bean.setAwbFrom(null);
//		 }
        if (StrUtil.isBlank(bean.getAwbFromType())) {
            bean.setAwbFromType(null);
        }
        if (StrUtil.isBlank(bean.getAwbFromName())) {
            bean.setAwbFromName(null);
        }
        if (StrUtil.isBlank(bean.getFinancialDateStart())) {
            bean.setFinancialDateStart(null);
        }
        if (StrUtil.isBlank(bean.getFinancialDateEnd())) {
            bean.setFinancialDateEnd(null);
        }
        if (StrUtil.isBlank(bean.getDepartureStation())) {
            bean.setDepartureStation(null);
        }
        if (StrUtil.isBlank(bean.getArrivalStation())) {
            bean.setArrivalStation(null);
        }
        if (StrUtil.isBlank(bean.getBusinessProduct())) {
            bean.setBusinessProduct(null);
        }
        if (StrUtil.isBlank(bean.getGoodsType())) {
            bean.setGoodsType(null);
        }
        if (StrUtil.isBlank(bean.getAwbNumber())) {
            bean.setAwbNumber(null);
        }
        if (StrUtil.isBlank(bean.getOrderCode())) {
            bean.setOrderCode(null);
        }
        if (StrUtil.isBlank(bean.getRoutingName())) {
            bean.setRoutingName(null);
        }
        if (StrUtil.isBlank(bean.getSalesName())) {
            bean.setSalesName(null);
        }
        if (StrUtil.isBlank(bean.getServicerName())) {
            bean.setServicerName(null);
        }
        if (StrUtil.isBlank(bean.getOrderStatus())) {
            bean.setOrderStatus(null);
        }
        if (StrUtil.isBlank(bean.getExpectFlight())) {
            bean.setExpectFlight(null);
        }
        if (StrUtil.isBlank(bean.getShipVoyageNumber())) {
            bean.setShipVoyageNumber(null);
        }
        if (StrUtil.isBlank(bean.getSalesDep())) {
            bean.setSalesDep(null);
        }
        if (StrUtil.isBlank(bean.getCustomerNumber())) {
            bean.setCustomerNumber(null);
        }
//        List<List<Map<String, String>>> list = cssPReportSettleAfMapper.getListPage(bean);
//        List<CssPReportSettleAfExcel> listR = cssPReportSettleAfMapper.getListPageExcel(bean);

        List<List<Map<String, String>>> list = cssPReportSettleAfMapper.getListPageSC(bean);
        List<CssPReportSettleAfExcelSE> listR = cssPReportSettleAfMapper.getListPageExcelSE(bean);
//    	if ("AE".equals(bean.getBusinessScope()) ||"AI".equals(bean.getBusinessScope())) {
//    		list =cssPReportSettleAfMapper.getListPage(bean);
//    		listR = cssPReportSettleAfMapper.getListPageExcelAI(bean);
//    	} else if ("SE".equals(bean.getBusinessScope()) ||"SI".equals(bean.getBusinessScope())){
//    		list =cssPReportSettleAfMapper.getListPageSC(bean);
//    		listR = cssPReportSettleAfMapper.getListPageExcelAISC(bean);
//    	}
        List<Map<String, String>> result = new ArrayList<>();
        if (list != null && list.size() > 0 && list.get(0) != null && list.get(0).size() > 0) {
//			 result.addAll(list.get(0)); 
//			 list.get(1).get(0).put("businessScope","合计");
            CssPReportSettleAfExcelSE ce = new CssPReportSettleAfExcelSE();
            ce.setBusinessScope("合计");
            ce.setPieces(list.get(1).get(0).get("pieces"));
            ce.setWeight(list.get(1).get(0).get("weight"));
            ce.setChargeWeight(list.get(1).get(0).get("chargeWeight"));
            ce.setIncomeFunctionalAmount(list.get(1).get(0).get("incomeFunctionalAmount"));
            ce.setCostFunctionalAmount(list.get(1).get(0).get("costFunctionalAmount"));
            ce.setGrossProfit(list.get(1).get(0).get("grossProfit"));
            ce.setMainRouting(list.get(1).get(0).get("main_routing"));
            ce.setMainRoutingIncome(list.get(1).get(0).get("main_routing_income"));
            ce.setMainRoutingCost(list.get(1).get(0).get("main_routing_cost"));
            ce.setFeeder(list.get(1).get(0).get("feeder"));
            ce.setFeederIncome(list.get(1).get(0).get("feeder_income"));
            ce.setFeederCost(list.get(1).get(0).get("feeder_cost"));
            ce.setOperation(list.get(1).get(0).get("operation"));
            ce.setOperationCost(list.get(1).get(0).get("operation_cost"));
            ce.setOperationIncome(list.get(1).get(0).get("operation_income"));
            ce.setPackaging(list.get(1).get(0).get("packaging"));
            ce.setPackagingIncome(list.get(1).get(0).get("packaging_income"));
            ce.setPackagingCost(list.get(1).get(0).get("packaging_cost"));
            ce.setStorage(list.get(1).get(0).get("storage"));
            ce.setStorageIncome(list.get(1).get(0).get("storage_income"));
            ce.setStorageCost(list.get(1).get(0).get("storage_cost"));
            ce.setPostage(list.get(1).get(0).get("postage"));
            ce.setPostageIncome(list.get(1).get(0).get("postage_income"));
            ce.setPostageCost(list.get(1).get(0).get("postage_cost"));
            ce.setClearance(list.get(1).get(0).get("clearance"));
            ce.setClearanceIncome(list.get(1).get(0).get("clearance_income"));
            ce.setClearanceCost(list.get(1).get(0).get("clearance_cost"));
            ce.setExchange(list.get(1).get(0).get("exchange"));
            ce.setExchangeIncome(list.get(1).get(0).get("exchange_income"));
            ce.setExchangeCost(list.get(1).get(0).get("exchange_cost"));
            ce.setOrderCode("共" + list.get(1).get(0).get("order_sum") + "票");
//             result.add(list.get(1).get(0)); 
            listR.add(ce);

        }
        return listR;
    }

    @Override
    public List<CssPReportSettleAfExcelSI> getListForExcelSI(CssPReportSettleAfProcedure bean) {
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
        bean.setCurrentUserId(SecurityUtils.getUser().getId());
        if (StrUtil.isBlank(bean.getCoopName())) {
            bean.setCoopName(null);
        }
        if (StrUtil.isBlank(bean.getFlightDateStart())) {
            bean.setFlightDateStart(null);
        }
        if (StrUtil.isBlank(bean.getFlightDateEnd())) {
            bean.setFlightDateEnd(null);
        }
        if (StrUtil.isBlank(bean.getGrossProfitStr())) {
            bean.setGrossProfitStr(null);
        }
//		 if(StrUtil.isBlank(bean.getAwbFrom())) {
//			 bean.setAwbFrom(null);
//		 }
        if (StrUtil.isBlank(bean.getAwbFromType())) {
            bean.setAwbFromType(null);
        }
        if (StrUtil.isBlank(bean.getAwbFromName())) {
            bean.setAwbFromName(null);
        }
        if (StrUtil.isBlank(bean.getFinancialDateStart())) {
            bean.setFinancialDateStart(null);
        }
        if (StrUtil.isBlank(bean.getFinancialDateEnd())) {
            bean.setFinancialDateEnd(null);
        }
        if (StrUtil.isBlank(bean.getDepartureStation())) {
            bean.setDepartureStation(null);
        }
        if (StrUtil.isBlank(bean.getArrivalStation())) {
            bean.setArrivalStation(null);
        }
        if (StrUtil.isBlank(bean.getBusinessProduct())) {
            bean.setBusinessProduct(null);
        }
        if (StrUtil.isBlank(bean.getGoodsType())) {
            bean.setGoodsType(null);
        }
        if (StrUtil.isBlank(bean.getAwbNumber())) {
            bean.setAwbNumber(null);
        }
        if (StrUtil.isBlank(bean.getOrderCode())) {
            bean.setOrderCode(null);
        }
        if (StrUtil.isBlank(bean.getRoutingName())) {
            bean.setRoutingName(null);
        }
        if (StrUtil.isBlank(bean.getSalesName())) {
            bean.setSalesName(null);
        }
        if (StrUtil.isBlank(bean.getServicerName())) {
            bean.setServicerName(null);
        }
        if (StrUtil.isBlank(bean.getOrderStatus())) {
            bean.setOrderStatus(null);
        }
        if (StrUtil.isBlank(bean.getExpectFlight())) {
            bean.setExpectFlight(null);
        }
        if (StrUtil.isBlank(bean.getShipVoyageNumber())) {
            bean.setShipVoyageNumber(null);
        }
        if (StrUtil.isBlank(bean.getSalesDep())) {
            bean.setSalesDep(null);
        }
        if (StrUtil.isBlank(bean.getCustomerNumber())) {
            bean.setCustomerNumber(null);
        }
//        List<List<Map<String, String>>> list = cssPReportSettleAfMapper.getListPage(bean);
//        List<CssPReportSettleAfExcel> listR = cssPReportSettleAfMapper.getListPageExcel(bean);

        List<List<Map<String, String>>> list = cssPReportSettleAfMapper.getListPageSC(bean);
        List<CssPReportSettleAfExcelSI> listR = cssPReportSettleAfMapper.getListPageExcelSI(bean);
//    	if ("AE".equals(bean.getBusinessScope()) ||"AI".equals(bean.getBusinessScope())) {
//    		list =cssPReportSettleAfMapper.getListPage(bean);
//    		listR = cssPReportSettleAfMapper.getListPageExcelAI(bean);
//    	} else if ("SE".equals(bean.getBusinessScope()) ||"SI".equals(bean.getBusinessScope())){
//    		list =cssPReportSettleAfMapper.getListPageSC(bean);
//    		listR = cssPReportSettleAfMapper.getListPageExcelAISC(bean);
//    	}
        List<Map<String, String>> result = new ArrayList<>();
        if (list != null && list.size() > 0 && list.get(0) != null && list.get(0).size() > 0) {
//			 result.addAll(list.get(0)); 
//			 list.get(1).get(0).put("businessScope","合计");
            CssPReportSettleAfExcelSI ce = new CssPReportSettleAfExcelSI();
            ce.setBusinessScope("合计");
            ce.setPieces(list.get(1).get(0).get("pieces"));
            ce.setWeight(list.get(1).get(0).get("weight"));
            ce.setChargeWeight(list.get(1).get(0).get("chargeWeight"));
            ce.setIncomeFunctionalAmount(list.get(1).get(0).get("incomeFunctionalAmount"));
            ce.setCostFunctionalAmount(list.get(1).get(0).get("costFunctionalAmount"));
            ce.setGrossProfit(list.get(1).get(0).get("grossProfit"));
            ce.setMainRouting(list.get(1).get(0).get("main_routing"));
            ce.setMainRoutingIncome(list.get(1).get(0).get("main_routing_income"));
            ce.setMainRoutingCost(list.get(1).get(0).get("main_routing_cost"));
            ce.setFeeder(list.get(1).get(0).get("feeder"));
            ce.setFeederIncome(list.get(1).get(0).get("feeder_income"));
            ce.setFeederCost(list.get(1).get(0).get("feeder_cost"));
            ce.setOperation(list.get(1).get(0).get("operation"));
            ce.setOperationCost(list.get(1).get(0).get("operation_cost"));
            ce.setOperationIncome(list.get(1).get(0).get("operation_income"));
            ce.setPackaging(list.get(1).get(0).get("packaging"));
            ce.setPackagingIncome(list.get(1).get(0).get("packaging_income"));
            ce.setPackagingCost(list.get(1).get(0).get("packaging_cost"));
            ce.setStorage(list.get(1).get(0).get("storage"));
            ce.setStorageIncome(list.get(1).get(0).get("storage_income"));
            ce.setStorageCost(list.get(1).get(0).get("storage_cost"));
            ce.setPostage(list.get(1).get(0).get("postage"));
            ce.setPostageIncome(list.get(1).get(0).get("postage_income"));
            ce.setPostageCost(list.get(1).get(0).get("postage_cost"));
            ce.setClearance(list.get(1).get(0).get("clearance"));
            ce.setClearanceIncome(list.get(1).get(0).get("clearance_income"));
            ce.setClearanceCost(list.get(1).get(0).get("clearance_cost"));
            ce.setExchange(list.get(1).get(0).get("exchange"));
            ce.setExchangeIncome(list.get(1).get(0).get("exchange_income"));
            ce.setExchangeCost(list.get(1).get(0).get("exchange_cost"));
            String a = list.get(1).get(0).get("order_sum");
            ce.setOrderCode("共" + list.get(1).get(0).get("order_sum") + "票");
//             result.add(list.get(1).get(0)); 
            listR.add(ce);

        }
        return listR;
    }

    @Override
    public List<CssPReportSettleExcel> getListForExcelNew(CssPReportSettleAfProcedure bean) {
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
        bean.setCurrentUserId(SecurityUtils.getUser().getId());
        if (StrUtil.isBlank(bean.getCoopName())) {
            bean.setCoopName(null);
        }
        if (StrUtil.isBlank(bean.getFlightDateStart())) {
            bean.setFlightDateStart(null);
        }
        if (StrUtil.isBlank(bean.getFlightDateEnd())) {
            bean.setFlightDateEnd(null);
        }
        if (StrUtil.isBlank(bean.getGrossProfitStr())) {
            bean.setGrossProfitStr(null);
        }
        if (StrUtil.isBlank(bean.getAwbFrom())) {
            bean.setAwbFrom(null);
        }
        if (StrUtil.isBlank(bean.getAwbFromType())) {
            bean.setAwbFromType(null);
        }
        if (StrUtil.isBlank(bean.getAwbFromName())) {
            bean.setAwbFromName(null);
        }
        if (StrUtil.isBlank(bean.getFinancialDateStart())) {
            bean.setFinancialDateStart(null);
        }
        if (StrUtil.isBlank(bean.getFinancialDateEnd())) {
            bean.setFinancialDateEnd(null);
        }
        if (StrUtil.isBlank(bean.getDepartureStation())) {
            bean.setDepartureStation(null);
        }
        if (StrUtil.isBlank(bean.getArrivalStation())) {
            bean.setArrivalStation(null);
        }
        if (StrUtil.isBlank(bean.getBusinessProduct())) {
            bean.setBusinessProduct(null);
        }
        if (StrUtil.isBlank(bean.getGoodsType())) {
            bean.setGoodsType(null);
        }
        if (StrUtil.isBlank(bean.getAwbNumber())) {
            bean.setAwbNumber(null);
        }
        if (StrUtil.isBlank(bean.getOrderCode())) {
            bean.setOrderCode(null);
        }
        if (StrUtil.isBlank(bean.getRoutingName())) {
            bean.setRoutingName(null);
        }
        if (StrUtil.isBlank(bean.getSalesName())) {
            bean.setSalesName(null);
        }
        if (StrUtil.isBlank(bean.getServicerName())) {
            bean.setServicerName(null);
        }
        if (StrUtil.isBlank(bean.getOrderStatus())) {
            bean.setOrderStatus(null);
        }
        if (StrUtil.isBlank(bean.getExpectFlight())) {
            bean.setExpectFlight(null);
        }
        if (StrUtil.isBlank(bean.getShipVoyageNumber())) {
            bean.setShipVoyageNumber(null);
        }
        if (StrUtil.isBlank(bean.getSalesDep())) {
            bean.setSalesDep(null);
        }
        if (StrUtil.isBlank(bean.getCustomerNumber())) {
            bean.setCustomerNumber(null);
        }
        if (StrUtil.isBlank(bean.getGoodsSourceCode())) {
            bean.setGoodsSourceCode(null);
        }
        if (StrUtil.isBlank(bean.getExitPort())) {
            bean.setExitPort(null);
        }
        if (StrUtil.isBlank(bean.getShippingMethod())) {
            bean.setShippingMethod(null);
        }
        if (StrUtil.isBlank(bean.getBusinessMethod())) {
            bean.setBusinessMethod(null);
        }
        List<List<Map<String, String>>> list = null;
        List<CssPReportSettleExcel> listR = null;
        if ("TE".equals(bean.getBusinessScope())) {
            list = cssPReportSettleAfMapper.getListPageTC(bean);
            listR = cssPReportSettleAfMapper.getListPageTCExcel(bean);
        }
        if ("LC".equals(bean.getBusinessScope())) {
            list = cssPReportSettleAfMapper.getListPageLC(bean);
            listR = cssPReportSettleAfMapper.getListPageLCExcel(bean);
        }
        if ("IO".equals(bean.getBusinessScope())) {
            list = cssPReportSettleAfMapper.getListPageIO(bean);
            listR = cssPReportSettleAfMapper.getListPageIOExcel(bean);
        }
        List<Map<String, String>> result = new ArrayList<>();
        if (list != null && list.size() > 0 && list.get(0) != null && list.get(0).size() > 0) {
            CssPReportSettleExcel ce = new CssPReportSettleExcel();
            ce.setBusinessScope("合计");
            ce.setPieces(list.get(1).get(0).get("pieces"));
            ce.setWeight(list.get(1).get(0).get("weight"));
            ce.setChargeWeight(list.get(1).get(0).get("chargeWeight"));
            ce.setIncomeFunctionalAmount(list.get(1).get(0).get("incomeFunctionalAmount"));
            ce.setCostFunctionalAmount(list.get(1).get(0).get("costFunctionalAmount"));
            ce.setGrossProfit(list.get(1).get(0).get("grossProfit"));
            ce.setMainRouting(list.get(1).get(0).get("main_routing"));
            ce.setMainRoutingIncome(list.get(1).get(0).get("main_routing_income"));
            ce.setMainRoutingCost(list.get(1).get(0).get("main_routing_cost"));
            ce.setFeeder(list.get(1).get(0).get("feeder"));
            ce.setFeederIncome(list.get(1).get(0).get("feeder_income"));
            ce.setFeederCost(list.get(1).get(0).get("feeder_cost"));
            ce.setOperation(list.get(1).get(0).get("operation"));
            ce.setOperationCost(list.get(1).get(0).get("operation_cost"));
            ce.setOperationIncome(list.get(1).get(0).get("operation_income"));
            ce.setPackaging(list.get(1).get(0).get("packaging"));
            ce.setPackagingIncome(list.get(1).get(0).get("packaging_income"));
            ce.setPackagingCost(list.get(1).get(0).get("packaging_cost"));
            ce.setStorage(list.get(1).get(0).get("storage"));
            ce.setStorageIncome(list.get(1).get(0).get("storage_income"));
            ce.setStorageCost(list.get(1).get(0).get("storage_cost"));
            ce.setPostage(list.get(1).get(0).get("postage"));
            ce.setPostageIncome(list.get(1).get(0).get("postage_income"));
            ce.setPostageCost(list.get(1).get(0).get("postage_cost"));
            ce.setClearance(list.get(1).get(0).get("clearance"));
            ce.setClearanceIncome(list.get(1).get(0).get("clearance_income"));
            ce.setClearanceCost(list.get(1).get(0).get("clearance_cost"));
            ce.setExchange(list.get(1).get(0).get("exchange"));
            ce.setExchangeIncome(list.get(1).get(0).get("exchange_income"));
            ce.setExchangeCost(list.get(1).get(0).get("exchange_cost"));
            String a = list.get(1).get(0).get("order_sum");
            ce.setOrderCode("共" + list.get(1).get(0).get("order_sum") + "票");
            listR.add(ce);

        }
        return listR;
    }

}
