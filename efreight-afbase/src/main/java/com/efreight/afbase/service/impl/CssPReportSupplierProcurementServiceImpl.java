package com.efreight.afbase.service.impl;

import com.efreight.afbase.dao.CssPReportSupplierProcurementMapper;
import com.efreight.afbase.dao.ServiceMapper;
import com.efreight.afbase.entity.CssPReportSupplierProcurement;
import com.efreight.afbase.entity.CssPReportSupplierProcurementDetail;
import com.efreight.afbase.service.CssPReportSupplierProcurementService;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class CssPReportSupplierProcurementServiceImpl implements CssPReportSupplierProcurementService {

    private final CssPReportSupplierProcurementMapper cssPReportSupplierProcurementMapper;
    private final ServiceMapper serviceMapper;

    @Override
    public List<CssPReportSupplierProcurement> getList(CssPReportSupplierProcurement cssPReportSupplierProcurement) {
        if(cssPReportSupplierProcurement.getOtherOrg()!=null&&cssPReportSupplierProcurement.getOtherOrg()!=-1) {
        	cssPReportSupplierProcurement.setOrgId(cssPReportSupplierProcurement.getOtherOrg());
    	}else if(cssPReportSupplierProcurement.getOtherOrg()!=null&&cssPReportSupplierProcurement.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			cssPReportSupplierProcurement.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			cssPReportSupplierProcurement.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		cssPReportSupplierProcurement.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
        List<CssPReportSupplierProcurement> list = new ArrayList<CssPReportSupplierProcurement>();
        if (cssPReportSupplierProcurement.getBusinessScope().startsWith("A")) {
            list = cssPReportSupplierProcurementMapper.getAFList(cssPReportSupplierProcurement);
        }
        if (cssPReportSupplierProcurement.getBusinessScope().startsWith("S")) {
            list = cssPReportSupplierProcurementMapper.getSCList(cssPReportSupplierProcurement);
        }
        if (cssPReportSupplierProcurement.getBusinessScope().startsWith("T")) {
            list = cssPReportSupplierProcurementMapper.getTCList(cssPReportSupplierProcurement);
        }
        if (cssPReportSupplierProcurement.getBusinessScope().startsWith("L")) {
            list = cssPReportSupplierProcurementMapper.getLCList(cssPReportSupplierProcurement);
        }
        if (cssPReportSupplierProcurement.getBusinessScope().equals("IO")) {
            list = cssPReportSupplierProcurementMapper.getIOList(cssPReportSupplierProcurement);
        }
        CssPReportSupplierProcurement sumForList = getSumForList(list);
        list.stream().forEach(supplierProcurement -> {
            if (sumForList.getOrderCount() != 0) {
                supplierProcurement.setOrderCountRatio(BigDecimal.valueOf(supplierProcurement.getOrderCount()).divide(BigDecimal.valueOf(sumForList.getOrderCount()), 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
            if (sumForList.getChargeWeight().compareTo(BigDecimal.ZERO) != 0 && supplierProcurement.getChargeWeight() != null) {
                supplierProcurement.setChargeWeightRatio(supplierProcurement.getChargeWeight().divide(sumForList.getChargeWeight(), 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
            if (sumForList.getCostFunctionalAmount().compareTo(BigDecimal.ZERO) != 0 && supplierProcurement.getCostFunctionalAmount() != null) {
                supplierProcurement.setCostFunctionalAmountRatio(supplierProcurement.getCostFunctionalAmount().divide(sumForList.getCostFunctionalAmount(), 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
        });
        return list;
    }

    @Override
    public List<CssPReportSupplierProcurementDetail> viewDetail(CssPReportSupplierProcurementDetail cssPReportSupplierProcurementDetail) {
    	if(cssPReportSupplierProcurementDetail.getOtherOrg()!=null&&cssPReportSupplierProcurementDetail.getOtherOrg()!=-1) {
    		cssPReportSupplierProcurementDetail.setOrgId(cssPReportSupplierProcurementDetail.getOtherOrg());
     	}else if(cssPReportSupplierProcurementDetail.getOtherOrg()!=null&&cssPReportSupplierProcurementDetail.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			cssPReportSupplierProcurementDetail.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			cssPReportSupplierProcurementDetail.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
     		cssPReportSupplierProcurementDetail.setOrgId(SecurityUtils.getUser().getOrgId());
     	}
        List<CssPReportSupplierProcurementDetail> list = new ArrayList<CssPReportSupplierProcurementDetail>();
        if (cssPReportSupplierProcurementDetail.getBusinessScope().startsWith("A")) {
            list = cssPReportSupplierProcurementMapper.viewAFDetail(cssPReportSupplierProcurementDetail);
        }
        if (cssPReportSupplierProcurementDetail.getBusinessScope().startsWith("S")) {
            list = cssPReportSupplierProcurementMapper.viewSCDetail(cssPReportSupplierProcurementDetail);
        }
        if (cssPReportSupplierProcurementDetail.getBusinessScope().startsWith("T")) {
            list = cssPReportSupplierProcurementMapper.viewTCDetail(cssPReportSupplierProcurementDetail);
        }
        if (cssPReportSupplierProcurementDetail.getBusinessScope().startsWith("L")) {
            list = cssPReportSupplierProcurementMapper.viewLCDetail(cssPReportSupplierProcurementDetail);
        }
        if (cssPReportSupplierProcurementDetail.getBusinessScope().equals("IO")) {
            list = cssPReportSupplierProcurementMapper.viewIODetail(cssPReportSupplierProcurementDetail);
        }

        return list;
    }

    private CssPReportSupplierProcurement getSumForList(List<CssPReportSupplierProcurement> list) {
        HashMap<String, Integer> intSumMap = new HashMap<>();
        intSumMap.put("orderCount", 0);
        intSumMap.put("yearOrderCount", 0);
        HashMap<String, BigDecimal> doubleSumMap = new HashMap<>();
        doubleSumMap.put("chargeWeight", BigDecimal.ZERO);
        doubleSumMap.put("costFunctionalAmount", BigDecimal.ZERO);
        doubleSumMap.put("yearChargeWeight", BigDecimal.ZERO);
        doubleSumMap.put("yearCostFunctionalAmount", BigDecimal.ZERO);
        list.stream().forEach(cssPReportSupplierProcurement -> {
            intSumMap.put("orderCount", intSumMap.get("orderCount") + (cssPReportSupplierProcurement.getOrderCount() == null ? 0 : cssPReportSupplierProcurement.getOrderCount()));
            intSumMap.put("yearOrderCount", intSumMap.get("yearOrderCount") + (cssPReportSupplierProcurement.getYearOrderCount() == null ? 0 : cssPReportSupplierProcurement.getYearOrderCount()));
            doubleSumMap.put("chargeWeight", doubleSumMap.get("chargeWeight").add(cssPReportSupplierProcurement.getChargeWeight() == null ? BigDecimal.ZERO : cssPReportSupplierProcurement.getChargeWeight()));
            doubleSumMap.put("yearChargeWeight", doubleSumMap.get("yearChargeWeight").add(cssPReportSupplierProcurement.getYearChargeWeight() == null ? BigDecimal.ZERO : cssPReportSupplierProcurement.getYearChargeWeight()));
            doubleSumMap.put("costFunctionalAmount", doubleSumMap.get("costFunctionalAmount").add(cssPReportSupplierProcurement.getCostFunctionalAmount() == null ? BigDecimal.ZERO : cssPReportSupplierProcurement.getCostFunctionalAmount()));
            doubleSumMap.put("yearCostFunctionalAmount", doubleSumMap.get("yearCostFunctionalAmount").add(cssPReportSupplierProcurement.getYearCostFunctionalAmount() == null ? BigDecimal.ZERO : cssPReportSupplierProcurement.getYearCostFunctionalAmount()));
        });
        CssPReportSupplierProcurement cssPReportSupplierProcurement = new CssPReportSupplierProcurement();
        cssPReportSupplierProcurement.setOrderCount(intSumMap.get("orderCount"));
        cssPReportSupplierProcurement.setYearOrderCount(intSumMap.get("yearOrderCount"));
        cssPReportSupplierProcurement.setChargeWeight(doubleSumMap.get("chargeWeight"));
        cssPReportSupplierProcurement.setYearChargeWeight(doubleSumMap.get("yearChargeWeight"));
        cssPReportSupplierProcurement.setCostFunctionalAmount(doubleSumMap.get("costFunctionalAmount"));
        cssPReportSupplierProcurement.setYearCostFunctionalAmount(doubleSumMap.get("yearCostFunctionalAmount"));
        return cssPReportSupplierProcurement;
    }

}
