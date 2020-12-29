package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.efreight.afbase.dao.CssReportNoWriteoffMapper;
import com.efreight.afbase.dao.ServiceMapper;
import com.efreight.afbase.entity.CssReportNoWriteoff;
import com.efreight.afbase.entity.CssReportNoWriteoffDetail;
import com.efreight.afbase.entity.exportExcel.CssReportCostNoWriteoffExcel;
import com.efreight.afbase.entity.exportExcel.CssReportIncomeNoWriteoffExcel;
import com.efreight.afbase.service.CssReportNoWriteoffService;
import com.efreight.afbase.utils.LoginUtils;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CssReportNoWriteoffServiceImpl implements CssReportNoWriteoffService {

    private final CssReportNoWriteoffMapper cssReportNoWriteoffMapper;
    private final ServiceMapper serviceMapper;

    @Override
    public List<CssReportNoWriteoff> getList(CssReportNoWriteoff cssReportNoWriteoff) {
    	if(cssReportNoWriteoff.getOtherOrg()!=null&&cssReportNoWriteoff.getOtherOrg()!=-1) {
    		cssReportNoWriteoff.setOrgId(cssReportNoWriteoff.getOtherOrg());
    	}else if(cssReportNoWriteoff.getOtherOrg()!=null&&cssReportNoWriteoff.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			cssReportNoWriteoff.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			cssReportNoWriteoff.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		cssReportNoWriteoff.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
        List<CssReportNoWriteoff> list = cssReportNoWriteoffMapper.getList(cssReportNoWriteoff);
        list.stream().forEach(item -> {
            item.setCostNoWriteoffFunctionalAmountStr(FormatUtils.formatWithQWF(item.getCostNoWriteoffFunctionalAmount(), 2));
            item.setIncomeNoWriteoffFunctionalAmountStr(FormatUtils.formatWithQWF(item.getIncomeNoWriteoffFunctionalAmount(), 2));
            item.setFunctionalAmountSubstractionStr(FormatUtils.formatWithQWF(item.getFunctionalAmountSubstraction(), 2));
        });
        return list;
    }

    @Override
    public List<CssReportNoWriteoffDetail> view(Integer coopId, Integer type,Integer otherOrg) {
    	Integer orgId = null;
    	if(otherOrg!=null&&otherOrg!=-1) {
    		orgId = otherOrg;
    	}else if(otherOrg!=null&&otherOrg==-1) {
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			orgId = map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId();
    		}else {
    			//以防有破损数据 做一下默认值
    			orgId = SecurityUtils.getUser().getOrgId();
    		}
    	}else{
    		orgId = SecurityUtils.getUser().getOrgId();
    	}
        List<CssReportNoWriteoffDetail> list = cssReportNoWriteoffMapper.view(coopId,orgId, type,otherOrg);
        list.stream().forEach(cssReportNoWriteoffDetail -> {
            if (cssReportNoWriteoffDetail.getAmount() != null) {
                cssReportNoWriteoffDetail.setAmountStr(FormatUtils.formatWithQWF(cssReportNoWriteoffDetail.getAmount(), 2));
            }
            if (cssReportNoWriteoffDetail.getAmountNoWriteoff() != null) {
                cssReportNoWriteoffDetail.setAmountNoWriteoffStr(FormatUtils.formatWithQWF(cssReportNoWriteoffDetail.getAmountNoWriteoff(), 2));
            }
            if (cssReportNoWriteoffDetail.getFunctionalAmount() != null) {
                cssReportNoWriteoffDetail.setFunctionalAmountStr(FormatUtils.formatWithQWF(cssReportNoWriteoffDetail.getFunctionalAmount(), 2));
            }
            if (cssReportNoWriteoffDetail.getFunctionalAmountWriteoff() != null) {
                cssReportNoWriteoffDetail.setFunctionalAmountWriteoffStr(FormatUtils.formatWithQWF(cssReportNoWriteoffDetail.getFunctionalAmountWriteoff(), 2));
            }
            if (cssReportNoWriteoffDetail.getFunctionalAmountNoWriteoff() != null) {
                cssReportNoWriteoffDetail.setFunctionalAmountNoWriteoffStr(FormatUtils.formatWithQWF(cssReportNoWriteoffDetail.getFunctionalAmountNoWriteoff(), 2));
            }
        });
        return list;
    }

    @Override
    public void exportExcelList(CssReportNoWriteoff cssReportNoWriteoff) {
        List<CssReportNoWriteoff> list = getList(cssReportNoWriteoff);

        List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
        if (StrUtil.isNotBlank(cssReportNoWriteoff.getColumnStrs())) {
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(cssReportNoWriteoff.getColumnStrs());
            String[] headers = new String[jsonArr.size()];
            String[] colunmStrs = new String[jsonArr.size()];
            //生成表头跟字段
            HashMap<String, BigDecimal> sumMap = new HashMap<>();
            sumMap.put("incomeNoWriteoffFunctionalAmount", BigDecimal.ZERO);
            sumMap.put("costNoWriteoffFunctionalAmount", BigDecimal.ZERO);
            sumMap.put("functionalAmountSubstraction", BigDecimal.ZERO);
            if (jsonArr != null && jsonArr.size() > 0) {
                for (int i = 0; i < jsonArr.size(); i++) {
                    JSONObject job = jsonArr.getJSONObject(i);
                    headers[i] = job.getString("label");
                    colunmStrs[i] = job.getString("prop");
                    //统计合计
                    if (i > 0) {
                        if ("incomeNoWriteoffFunctionalAmount".equals(job.getString("prop"))) {
                            list.stream().forEach(item -> {
                                if (item.getIncomeNoWriteoffFunctionalAmount() != null) {
                                    sumMap.put("incomeNoWriteoffFunctionalAmount", sumMap.get("incomeNoWriteoffFunctionalAmount").add(item.getIncomeNoWriteoffFunctionalAmount()));
                                }
                            });
                        }
                        if ("costNoWriteoffFunctionalAmount".equals(job.getString("prop"))) {
                            list.stream().forEach(item -> {
                                if (item.getCostNoWriteoffFunctionalAmount() != null) {
                                    sumMap.put("costNoWriteoffFunctionalAmount", sumMap.get("costNoWriteoffFunctionalAmount").add(item.getCostNoWriteoffFunctionalAmount()));
                                }
                            });
                        }
                        if ("functionalAmountSubstraction".equals(job.getString("prop"))) {
                            list.stream().forEach(item -> {
                                if (item.getFunctionalAmountSubstraction() != null) {
                                    sumMap.put("functionalAmountSubstraction", sumMap.get("functionalAmountSubstraction").add(item.getFunctionalAmountSubstraction()));
                                }
                            });
                        }
                    }
                }
            }
            if (list != null && list.size() > 0) {
                for (CssReportNoWriteoff excel : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], excel));
                    }
                    listExcel.add(map);
                }
            }
            if (list != null && list.size() > 0) {
                LinkedHashMap map = new LinkedHashMap();
                map.put(colunmStrs[0], "合计：");
                for (int j = 1; j < colunmStrs.length; j++) {
                    if ("incomeNoWriteoffFunctionalAmount".equals(colunmStrs[j])) {
                        map.put(colunmStrs[j], FormatUtils.formatWithQWF(sumMap.get("incomeNoWriteoffFunctionalAmount"), 2));
                    } else if ("costNoWriteoffFunctionalAmount".equals(colunmStrs[j])) {
                        map.put(colunmStrs[j], FormatUtils.formatWithQWF(sumMap.get("costNoWriteoffFunctionalAmount"), 2));
                    } else if ("functionalAmountSubstraction".equals(colunmStrs[j])) {
                        map.put(colunmStrs[j], FormatUtils.formatWithQWF(sumMap.get("functionalAmountSubstraction"), 2));
                    } else {
                        map.put(colunmStrs[j], "");
                    }
                }
                listExcel.add(map);

            }
            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(LoginUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");
        }
    }

    @Override
    public void exportExcel(Integer coopId, Integer type,Integer otherOrg) {
        List<CssReportNoWriteoffDetail> list = view(coopId, type,otherOrg);
        if (type == 0) {
            String[] headers = {"业务范畴", "主单号", "订单", "客户单号", "开航/到港日期", "订单客户代码", "订单客户", "收款客户代码", "收款客户", "责任客服", "责任销售", "应收金额(本币)", "已核销金额(本币)", "未核销金额(本币)"};
            List<CssReportIncomeNoWriteoffExcel> listExcel = list.stream().map(cssReportNoWriteoffDetail -> {
                CssReportIncomeNoWriteoffExcel cssReportIncomeNoWriteoffExcel = new CssReportIncomeNoWriteoffExcel();
                BeanUtils.copyProperties(cssReportNoWriteoffDetail, cssReportIncomeNoWriteoffExcel);
                if (StrUtil.isNotBlank(cssReportIncomeNoWriteoffExcel.getServicerName())) {
                    cssReportIncomeNoWriteoffExcel.setServicerName(cssReportIncomeNoWriteoffExcel.getServicerName().split(" ")[0]);
                }
                if (StrUtil.isNotBlank(cssReportIncomeNoWriteoffExcel.getSalesName())) {
                    cssReportIncomeNoWriteoffExcel.setSalesName(cssReportIncomeNoWriteoffExcel.getSalesName().split(" ")[0]);
                }
                return cssReportIncomeNoWriteoffExcel;
            }).collect(Collectors.toList());
            ExportExcel<CssReportIncomeNoWriteoffExcel> ex = new ExportExcel<CssReportIncomeNoWriteoffExcel>();
            ex.exportExcel(LoginUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");
        } else if (type == 1) {
            String[] headers = {"业务范畴", "主单号", "订单", "客户单号", "开航/到港日期", "订单客户代码", "订单客户", "付款客户代码", "付款客户", "责任客服", "责任销售", "币种", "付款金额(原币)", "付款金额(本币)", "未核销金额(原币)", "未核销金额(本币)"};
            List<CssReportCostNoWriteoffExcel> listExcel = list.stream().map(cssReportNoWriteoffDetail -> {
                CssReportCostNoWriteoffExcel cssReportCostNoWriteoffExcel = new CssReportCostNoWriteoffExcel();
                BeanUtils.copyProperties(cssReportNoWriteoffDetail, cssReportCostNoWriteoffExcel);
                if (StrUtil.isNotBlank(cssReportCostNoWriteoffExcel.getServicerName())) {
                    cssReportCostNoWriteoffExcel.setServicerName(cssReportCostNoWriteoffExcel.getServicerName().split(" ")[0]);
                }
                if (StrUtil.isNotBlank(cssReportCostNoWriteoffExcel.getSalesName())) {
                    cssReportCostNoWriteoffExcel.setSalesName(cssReportCostNoWriteoffExcel.getSalesName().split(" ")[0]);
                }
                return cssReportCostNoWriteoffExcel;
            }).collect(Collectors.toList());
            ExportExcel<CssReportCostNoWriteoffExcel> ex = new ExportExcel<CssReportCostNoWriteoffExcel>();
            ex.exportExcel(LoginUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");
        }


    }
}
