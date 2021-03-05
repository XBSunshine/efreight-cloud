package com.efreight.afbase.service.impl;

import java.util.ArrayList;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.dao.ReportPayableAgeMapper;
import com.efreight.afbase.dao.ServiceMapper;
import com.efreight.afbase.entity.exportExcel.ReportPayableAgeDetail;
import com.efreight.afbase.entity.exportExcel.ReportPayableAgeDetailForExcel;
import com.efreight.afbase.entity.procedure.ReportPayableAge;
import com.efreight.afbase.entity.procedure.ReportPayableAgeExcel;
import com.efreight.afbase.service.ReportPayableAgeService;
import com.efreight.afbase.utils.LoginUtils;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ReportPayableAgeServiceImpl implements ReportPayableAgeService {

    private final ReportPayableAgeMapper reportPayableAgeMapper;
    private final ServiceMapper serviceMapper;

    @Override
    public Map<String, List> getPageForAF(Page page, ReportPayableAge reportPayableAge) {
    	if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()!=-1) {
    		reportPayableAge.setOrgId(reportPayableAge.getOtherOrg());
    	}else if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			reportPayableAge.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
        reportPayableAge.setCurrentUserId(SecurityUtils.getUser().getId());
        if (StrUtil.isBlank(reportPayableAge.getBusinessScope())) {
            reportPayableAge.setBusinessScope(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getCountRanges())) {
            reportPayableAge.setCountRanges(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getCustomerName())) {
            reportPayableAge.setCustomerName(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getCustomerType())) {
            reportPayableAge.setCustomerType(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getSalesName())) {
            reportPayableAge.setSalesName(null);
        }
        List<List<LinkedHashMap<String, String>>> resultSet = reportPayableAgeMapper.getPageForAF(reportPayableAge);

        HashMap<String, List> result = new HashMap<>();
        List<Map<String, String>> data = new ArrayList<>();
        ArrayList<String> column = new ArrayList<>();
        if (resultSet.size() == 2 && resultSet.get(0) != null && resultSet.get(0).size() > 0 && resultSet.get(0).get(0) != null && resultSet.get(1) != null && resultSet.get(1).size() > 0 && resultSet.get(1).get(0) != null) {
            //整理数据
            data.addAll(resultSet.get(0));
//            if(reportPayableAge.getOrgEditionName()!=null&&!"".equals(reportPayableAge.getOrgEditionName())) {
//            	if(!"专业版".equals(reportPayableAge.getOrgEditionName())&&
//            			!"试用版".equals(reportPayableAge.getOrgEditionName())) {
//            	}else {
            resultSet.get(1).get(0).put("business_scope", "合计");
            data.addAll(resultSet.get(1));
//            	}
//            	
//            }
            //整理扩展列
            if (resultSet.get(0).size() != 0) {
                Set<String> keySet = resultSet.get(0).get(0).keySet();

                List<String> list = keySet.stream().collect(Collectors.toList());
                for (String key : list) {
                    if (key.contains("colName_")) {
                        column.add(key.replace("colName_", ""));
                    }
                }
            }
        }
        result.put("data", data);
        result.put("column", column);
        return result;
    }

    @Override
    public List<ReportPayableAgeDetail> viewForAF(ReportPayableAge reportPayableAge) {
    	if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()!=-1) {
    		reportPayableAge.setOrgId(reportPayableAge.getOtherOrg());
    	}else if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			reportPayableAge.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
        reportPayableAge.setCurrentUserId(SecurityUtils.getUser().getId());
        return reportPayableAgeMapper.viewForAF(reportPayableAge);
    }

    @Override
    public void exportExcelForAF(ReportPayableAge reportPayableAge) {
    	if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()!=-1) {
    		reportPayableAge.setOrgId(reportPayableAge.getOtherOrg());
    	}else if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			reportPayableAge.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
        if (StrUtil.isBlank(reportPayableAge.getSalesName())) {
            reportPayableAge.setSalesName(null);
        }
        reportPayableAge.setCurrentUserId(SecurityUtils.getUser().getId());
        List<ReportPayableAgeDetail> list = reportPayableAgeMapper.viewForAF(reportPayableAge);
        List<ReportPayableAgeDetailForExcel> listExcel = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                ReportPayableAgeDetailForExcel reportPayableAgeDetailForExcel = new ReportPayableAgeDetailForExcel();
                BeanUtils.copyProperties(list.get(i), reportPayableAgeDetailForExcel);
                listExcel.add(reportPayableAgeDetailForExcel);
            }
        }
        ExportExcel<ReportPayableAgeDetailForExcel> ex = new ExportExcel<ReportPayableAgeDetailForExcel>();
        if ("AE".equals(reportPayableAge.getBusinessScope())) {
            String[] headers = {"业务范畴", "主单号", "订单", "开航日期", "订单客户代码", "订单客户", "付款客户代码", "付款客户", "责任客服", "责任销售", "币种", "付款金额(原币)", "付款金额(本币)", "未核销金额(原币)", "未核销金额(本币)"};
            ex.exportExcel(LoginUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");
        } else {
            String[] headers = {"业务范畴", "主单号", "订单", "到港日期", "订单客户代码", "订单客户", "付款客户代码", "付款客户", "责任客服", "责任销售", "币种", "付款金额(原币)", "付款金额(本币)", "未核销金额(原币)", "未核销金额(本币)"};
            ex.exportExcel(LoginUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");
        }

    }

    @Override
    public void exportExcelListForAF(ReportPayableAge reportPayableAge) {
    	if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()!=-1) {
    		reportPayableAge.setOrgId(reportPayableAge.getOtherOrg());
    	}else if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			reportPayableAge.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
        reportPayableAge.setCurrentUserId(SecurityUtils.getUser().getId());
        if (StrUtil.isBlank(reportPayableAge.getBusinessScope())) {
            reportPayableAge.setBusinessScope(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getCountRanges())) {
            reportPayableAge.setCountRanges(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getCustomerName())) {
            reportPayableAge.setCustomerName(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getCustomerType())) {
            reportPayableAge.setCustomerType(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getSalesName())) {
            reportPayableAge.setSalesName(null);
        }
        List<List<LinkedHashMap<String, String>>> resultSet = reportPayableAgeMapper.getPageForAF(reportPayableAge);
        String[] headers = {"业务范畴", "供应商代码", "供应商", "供应商类型", "应付金额(本币)"};
        ArrayList<String> column = new ArrayList<>();
        List<ReportPayableAgeExcel> list = new ArrayList<ReportPayableAgeExcel>();
        if (resultSet != null && resultSet.size() > 0 && resultSet.get(0) != null && resultSet.get(0).size() > 0) {
            Set<String> keySet = resultSet.get(0).get(0).keySet();
            List<String> listColName = keySet.stream().collect(Collectors.toList());
            for (String key : listColName) {
                if (key.contains("colName_")) {
                    column.add(key.replace("colName_", ""));
                }
            }
            list = resultSet.get(0).stream().map(o -> {
                ReportPayableAgeExcel excel = new ReportPayableAgeExcel();
                excel.setBusinessScope(o.get("business_scope") + "");
                excel.setCoopCode(o.get("coop_code") != null ? (o.get("coop_code") + "") : "");
                excel.setCoopName(o.get("coop_name") + "");
                excel.setCoopType(o.get("coop_type") + "");
                excel.setNoFunctionalAmountWriteoff(o.get("no_functional_amount_writeoff") + "");
                String editionName = reportPayableAge.getOrgEditionName();
                if (column != null && column.size() > 0) {
                    //后面需要 优化一下excel 导出
                    for (int i = 0; i < column.size(); i++) {
                        if (editionName.contains("专业版")||editionName.contains("标准版")||editionName.contains("旗舰版")) {
                            if (i == 0) {
                                excel.setColName_1(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 1) {
                                excel.setColName_2(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 2) {
                                excel.setColName_3(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 3) {
                                excel.setColName_4(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 4) {
                                excel.setColName_5(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 5) {
                                excel.setColName_6(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 6) {
                                excel.setColName_7(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 7) {
                                excel.setColName_8(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 8) {
                                excel.setColName_9(o.get("colName_" + column.get(i)).toString());
                            }
                        }
                    }
                }
//                excel.setColName_1(o.get("colName_1-15") + "");
//                excel.setColName_2(o.get("colName_16-30") + "");
//                excel.setColName_3(o.get("colName_31-60") + "");
//                excel.setColName_4(o.get("colName_61-90") + "");
//                excel.setColName_5(o.get("colName_90+") + "");
                return excel;
            }).collect(Collectors.toList());

            ReportPayableAgeExcel excel2 = new ReportPayableAgeExcel();
            excel2.setBusinessScope("合计");
            excel2.setNoFunctionalAmountWriteoff(resultSet.get(1).get(0).get("no_functional_amount_writeoff") + "");
            String editionName = reportPayableAge.getOrgEditionName();
            if (column != null && column.size() > 0) {
                for (int i = 0; i < column.size(); i++) {
                    headers = ArrayUtils.add(headers, column.get(i));
                    if (editionName.contains("专业版")||editionName.contains("标准版")||editionName.contains("旗舰版")) {
                        if (i == 0) {
                            excel2.setColName_1(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 1) {
                            excel2.setColName_2(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 2) {
                            excel2.setColName_3(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 3) {
                            excel2.setColName_4(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 4) {
                            excel2.setColName_5(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 5) {
                            excel2.setColName_6(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 6) {
                            excel2.setColName_7(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 7) {
                            excel2.setColName_8(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 8) {
                            excel2.setColName_9(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                    }
                }
            }
//            excel2.setColName_1(resultSet.get(1).get(0).get("colName_1-15") + "");
//            excel2.setColName_2(resultSet.get(1).get(0).get("colName_16-30") + "");
//            excel2.setColName_3(resultSet.get(1).get(0).get("colName_31-60") + "");
//            excel2.setColName_4(resultSet.get(1).get(0).get("colName_61-90") + "");
//            excel2.setColName_5(resultSet.get(1).get(0).get("colName_90+") + "");
            list.add(excel2);
        } else {
            headers = ArrayUtils.add(headers, "1-15");
            headers = ArrayUtils.add(headers, "16-30");
            headers = ArrayUtils.add(headers, "31-60");
            headers = ArrayUtils.add(headers, "61-90");
            headers = ArrayUtils.add(headers, "90+");
        }
        ExportExcel<ReportPayableAgeExcel> ex = new ExportExcel<ReportPayableAgeExcel>();

        ex.exportExcel(LoginUtils.getResponse(), "导出EXCEL", headers, list, "Export");

    }

    @Override
    public Map<String, List> getPageForSC(Page page, ReportPayableAge reportPayableAge) {
    	if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()!=-1) {
    		reportPayableAge.setOrgId(reportPayableAge.getOtherOrg());
    	}else if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			reportPayableAge.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
        reportPayableAge.setCurrentUserId(SecurityUtils.getUser().getId());
        if (StrUtil.isBlank(reportPayableAge.getBusinessScope())) {
            reportPayableAge.setBusinessScope(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getCountRanges())) {
            reportPayableAge.setCountRanges(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getCustomerName())) {
            reportPayableAge.setCustomerName(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getCustomerType())) {
            reportPayableAge.setCustomerType(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getSalesName())) {
            reportPayableAge.setSalesName(null);
        }
        List<List<LinkedHashMap<String, String>>> resultSet = reportPayableAgeMapper.getPageForSC(reportPayableAge);

        HashMap<String, List> result = new HashMap<>();
        List<Map<String, String>> data = new ArrayList<>();
        ArrayList<String> column = new ArrayList<>();
        if (resultSet.size() == 2 && resultSet.get(0) != null && resultSet.get(0).size() > 0 && resultSet.get(0).get(0) != null && resultSet.get(1) != null && resultSet.get(1).size() > 0 && resultSet.get(1).get(0) != null) {
            //整理数据
            data.addAll(resultSet.get(0));
            data.addAll(resultSet.get(1));

            //整理扩展列
            if (resultSet.get(0).size() != 0) {
                Set<String> keySet = resultSet.get(0).get(0).keySet();

                List<String> list = keySet.stream().collect(Collectors.toList());
                for (String key : list) {
                    if (key.contains("colName_")) {
                        column.add(key.replace("colName_", ""));
                    }
                }
            }
        }
        result.put("data", data);
        result.put("column", column);
        return result;
    }

    @Override
    public List<ReportPayableAgeDetail> viewForSC(ReportPayableAge reportPayableAge) {
    	if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()!=-1) {
    		reportPayableAge.setOrgId(reportPayableAge.getOtherOrg());
    	}else if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			reportPayableAge.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
        if (StrUtil.isBlank(reportPayableAge.getSalesName())) {
            reportPayableAge.setSalesName(null);
        }
        reportPayableAge.setCurrentUserId(SecurityUtils.getUser().getId());
        return reportPayableAgeMapper.viewForSC(reportPayableAge);
    }

    @Override
    public void exportExcelForSC(ReportPayableAge reportPayableAge) {
    	if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()!=-1) {
    		reportPayableAge.setOrgId(reportPayableAge.getOtherOrg());
    	}else if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			reportPayableAge.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
        if (StrUtil.isBlank(reportPayableAge.getSalesName())) {
            reportPayableAge.setSalesName(null);
        }
        reportPayableAge.setCurrentUserId(SecurityUtils.getUser().getId());
        List<ReportPayableAgeDetail> list = reportPayableAgeMapper.viewForSC(reportPayableAge);
        List<ReportPayableAgeDetailForExcel> listExcel = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                ReportPayableAgeDetailForExcel reportPayableAgeDetailForExcel = new ReportPayableAgeDetailForExcel();
                BeanUtils.copyProperties(list.get(i), reportPayableAgeDetailForExcel);
                listExcel.add(reportPayableAgeDetailForExcel);
            }
        }
        ExportExcel<ReportPayableAgeDetailForExcel> ex = new ExportExcel<ReportPayableAgeDetailForExcel>();
        if ("SE".equals(reportPayableAge.getBusinessScope())) {
            String[] headers = {"业务范畴", "主单号", "订单", "开航日期", "订单客户代码", "订单客户", "付款客户代码", "付款客户", "责任客服", "责任销售", "币种", "付款金额(原币)", "付款金额(本币)", "未核销金额(原币)", "未核销金额(本币)"};
            ex.exportExcel(LoginUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");
        } else {
            String[] headers = {"业务范畴", "主单号", "订单", "到港日期", "订单客户代码", "订单客户", "付款客户代码", "付款客户", "责任客服", "责任销售", "币种", "付款金额(原币)", "付款金额(本币)", "未核销金额(原币)", "未核销金额(本币)"};
            ex.exportExcel(LoginUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");
        }
    }

    @Override
    public void exportExcelListForSC(ReportPayableAge reportPayableAge) {
    	if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()!=-1) {
    		reportPayableAge.setOrgId(reportPayableAge.getOtherOrg());
    	}else if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			reportPayableAge.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
        reportPayableAge.setCurrentUserId(SecurityUtils.getUser().getId());
        if (StrUtil.isBlank(reportPayableAge.getBusinessScope())) {
            reportPayableAge.setBusinessScope(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getCountRanges())) {
            reportPayableAge.setCountRanges(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getCustomerName())) {
            reportPayableAge.setCustomerName(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getCustomerType())) {
            reportPayableAge.setCustomerType(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getSalesName())) {
            reportPayableAge.setSalesName(null);
        }
        String[] headers = {"业务范畴", "供应商代码", "供应商", "供应商类型", "应付金额(本币)"};
        List<List<LinkedHashMap<String, String>>> resultSet = reportPayableAgeMapper.getPageForSC(reportPayableAge);
        List<ReportPayableAgeExcel> list = new ArrayList<ReportPayableAgeExcel>();
        if (resultSet != null && resultSet.size() > 0 && resultSet.get(0) != null && resultSet.get(0).size() > 0) {
            Set<String> keySet = resultSet.get(0).get(0).keySet();
            ArrayList<String> column = new ArrayList<>();
            List<String> listColName = keySet.stream().collect(Collectors.toList());
            for (String key : listColName) {
                if (key.contains("colName_")) {
                    column.add(key.replace("colName_", ""));
                }
            }
            list = resultSet.get(0).stream().map(o -> {
                ReportPayableAgeExcel excel = new ReportPayableAgeExcel();
                excel.setBusinessScope(o.get("business_scope") + "");
                excel.setCoopCode(o.get("coop_code") != null ? (o.get("coop_code") + "") : "");
                excel.setCoopName(o.get("coop_name") + "");
                excel.setCoopType(o.get("coop_type") + "");
                excel.setNoFunctionalAmountWriteoff(o.get("no_functional_amount_writeoff") + "");
                String editionName = reportPayableAge.getOrgEditionName();
                if (column != null && column.size() > 0) {
                    //后面需要 优化一下excel 导出
                    for (int i = 0; i < column.size(); i++) {
                        if (editionName.contains("专业版")||editionName.contains("标准版")||editionName.contains("旗舰版")) {
                            if (i == 0) {
                                excel.setColName_1(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 1) {
                                excel.setColName_2(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 2) {
                                excel.setColName_3(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 3) {
                                excel.setColName_4(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 4) {
                                excel.setColName_5(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 5) {
                                excel.setColName_6(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 6) {
                                excel.setColName_7(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 7) {
                                excel.setColName_8(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 8) {
                                excel.setColName_9(o.get("colName_" + column.get(i)).toString());
                            }
                        }
                    }
                }
                return excel;
            }).collect(Collectors.toList());

            ReportPayableAgeExcel excel2 = new ReportPayableAgeExcel();
            excel2.setBusinessScope("合计");
            String editionName = reportPayableAge.getOrgEditionName();
            excel2.setNoFunctionalAmountWriteoff(resultSet.get(1).get(0).get("no_functional_amount_writeoff") + "");
            if (column != null && column.size() > 0) {
                for (int i = 0; i < column.size(); i++) {
                    headers = ArrayUtils.add(headers, column.get(i));
                    if (editionName.contains("专业版")||editionName.contains("标准版")||editionName.contains("旗舰版")) {
                        if (i == 0) {
                            excel2.setColName_1(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 1) {
                            excel2.setColName_2(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 2) {
                            excel2.setColName_3(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 3) {
                            excel2.setColName_4(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 4) {
                            excel2.setColName_5(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 5) {
                            excel2.setColName_6(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 6) {
                            excel2.setColName_7(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 7) {
                            excel2.setColName_8(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 8) {
                            excel2.setColName_9(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                    }
                }
            }
            list.add(excel2);
        } else {
            headers = ArrayUtils.add(headers, "1-15");
            headers = ArrayUtils.add(headers, "16-30");
            headers = ArrayUtils.add(headers, "31-60");
            headers = ArrayUtils.add(headers, "61-90");
            headers = ArrayUtils.add(headers, "90+");
        }
        ExportExcel<ReportPayableAgeExcel> ex = new ExportExcel<ReportPayableAgeExcel>();

        ex.exportExcel(LoginUtils.getResponse(), "导出EXCEL", headers, list, "Export");

    }

    @Override
    public Map<String, List> getPageList(Page page, ReportPayableAge reportPayableAge) {
    	if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()!=-1) {
    		reportPayableAge.setOrgId(reportPayableAge.getOtherOrg());
    	}else if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			reportPayableAge.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
        reportPayableAge.setCurrentUserId(SecurityUtils.getUser().getId());
        if (StrUtil.isBlank(reportPayableAge.getBusinessScope())) {
            reportPayableAge.setBusinessScope(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getCountRanges())) {
            reportPayableAge.setCountRanges(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getCustomerName())) {
            reportPayableAge.setCustomerName(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getCustomerType())) {
            reportPayableAge.setCustomerType(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getSalesName())) {
            reportPayableAge.setSalesName(null);
        }
        List<List<LinkedHashMap<String, String>>> resultSet = null;
        if (reportPayableAge.getBusinessScope().startsWith("T")) {
            resultSet = reportPayableAgeMapper.getPageForTC(reportPayableAge);
        } else if (reportPayableAge.getBusinessScope().startsWith("L")) {
            resultSet = reportPayableAgeMapper.getPageForLC(reportPayableAge);
        } else if (reportPayableAge.getBusinessScope().equals("IO")) {
            resultSet = reportPayableAgeMapper.getPageForIO(reportPayableAge);
        }
        HashMap<String, List> result = new HashMap<>();
        List<Map<String, String>> data = new ArrayList<>();
        ArrayList<String> column = new ArrayList<>();
        if (resultSet != null && resultSet.size() == 2 && resultSet.get(0) != null && resultSet.get(0).size() > 0 && resultSet.get(0).get(0) != null && resultSet.get(1) != null && resultSet.get(1).size() > 0 && resultSet.get(1).get(0) != null) {
            //整理数据
            data.addAll(resultSet.get(0));
            data.addAll(resultSet.get(1));

            //整理扩展列
            if (resultSet.get(0).size() != 0) {
                Set<String> keySet = resultSet.get(0).get(0).keySet();

                List<String> list = keySet.stream().collect(Collectors.toList());
                for (String key : list) {
                    if (key.contains("colName_")) {
                        column.add(key.replace("colName_", ""));
                    }
                }
            }
        }
        result.put("data", data);
        result.put("column", column);
        return result;
    }

    @Override
    public List<ReportPayableAgeDetail> view(ReportPayableAge reportPayableAge) {
    	if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()!=-1) {
    		reportPayableAge.setOrgId(reportPayableAge.getOtherOrg());
    	}else if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			reportPayableAge.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
        if (StrUtil.isBlank(reportPayableAge.getSalesName())) {
            reportPayableAge.setSalesName(null);
        }
        reportPayableAge.setCurrentUserId(SecurityUtils.getUser().getId());
        if (reportPayableAge.getBusinessScope().startsWith("T")) {
            return reportPayableAgeMapper.viewForTC(reportPayableAge);
        } else if (reportPayableAge.getBusinessScope().startsWith("L")) {
            return reportPayableAgeMapper.viewForLC(reportPayableAge);
        } else if (reportPayableAge.getBusinessScope().equals("IO")) {
            return reportPayableAgeMapper.viewForIO(reportPayableAge);
        }
        return null;

    }

    @Override
    public void exportExcelList(ReportPayableAge reportPayableAge) {
    	if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()!=-1) {
    		reportPayableAge.setOrgId(reportPayableAge.getOtherOrg());
    	}else if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			reportPayableAge.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
        if (StrUtil.isBlank(reportPayableAge.getBusinessScope())) {
            reportPayableAge.setBusinessScope(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getCountRanges())) {
            reportPayableAge.setCountRanges(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getCustomerName())) {
            reportPayableAge.setCustomerName(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getCustomerType())) {
            reportPayableAge.setCustomerType(null);
        }
        if (StrUtil.isBlank(reportPayableAge.getSalesName())) {
            reportPayableAge.setSalesName(null);
        }
        reportPayableAge.setCurrentUserId(SecurityUtils.getUser().getId());
        String[] headers = {"业务范畴", "供应商代码", "供应商", "供应商类型", "应付金额(本币)"};
        List<List<LinkedHashMap<String, String>>> resultSet = null;
        if (reportPayableAge.getBusinessScope().startsWith("T")) {
            resultSet = reportPayableAgeMapper.getPageForTC(reportPayableAge);
        } else if (reportPayableAge.getBusinessScope().startsWith("L")) {
            resultSet = reportPayableAgeMapper.getPageForLC(reportPayableAge);
        }else if (reportPayableAge.getBusinessScope().equals("IO")) {
            resultSet = reportPayableAgeMapper.getPageForIO(reportPayableAge);
        }
        List<ReportPayableAgeExcel> list = new ArrayList<ReportPayableAgeExcel>();
        if (resultSet != null && resultSet.size() > 0 && resultSet.get(0) != null && resultSet.get(0).size() > 0) {
            Set<String> keySet = resultSet.get(0).get(0).keySet();
            ArrayList<String> column = new ArrayList<>();
            List<String> listColName = keySet.stream().collect(Collectors.toList());
            for (String key : listColName) {
                if (key.contains("colName_")) {
                    column.add(key.replace("colName_", ""));
                }
            }
            list = resultSet.get(0).stream().map(o -> {
                ReportPayableAgeExcel excel = new ReportPayableAgeExcel();
                excel.setBusinessScope(o.get("business_scope") + "");
                excel.setCoopCode(o.get("coop_code") != null ? (o.get("coop_code") + "") : "");
                excel.setCoopName(o.get("coop_name") + "");
                excel.setCoopType(o.get("coop_type") + "");
                excel.setNoFunctionalAmountWriteoff(o.get("no_functional_amount_writeoff") + "");
                String editionName = reportPayableAge.getOrgEditionName();
                if (column != null && column.size() > 0) {
                    //后面需要 优化一下excel 导出
                    for (int i = 0; i < column.size(); i++) {
                        if (editionName.contains("专业版")||editionName.contains("标准版")||editionName.contains("旗舰版")) {
                            if (i == 0) {
                                excel.setColName_1(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 1) {
                                excel.setColName_2(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 2) {
                                excel.setColName_3(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 3) {
                                excel.setColName_4(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 4) {
                                excel.setColName_5(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 5) {
                                excel.setColName_6(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 6) {
                                excel.setColName_7(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 7) {
                                excel.setColName_8(o.get("colName_" + column.get(i)).toString());
                            }
                            if (i == 8) {
                                excel.setColName_9(o.get("colName_" + column.get(i)).toString());
                            }
                        }
                    }
                }
                return excel;
            }).collect(Collectors.toList());

            ReportPayableAgeExcel excel2 = new ReportPayableAgeExcel();
            excel2.setBusinessScope("合计");
            excel2.setNoFunctionalAmountWriteoff(resultSet.get(1).get(0).get("no_functional_amount_writeoff") + "");
            String editionName = reportPayableAge.getOrgEditionName();
            if (column != null && column.size() > 0) {
                for (int i = 0; i < column.size(); i++) {
                    headers = ArrayUtils.add(headers, column.get(i));
                    if (editionName.contains("专业版")||editionName.contains("标准版")||editionName.contains("旗舰版")) {
                        if (i == 0) {
                            excel2.setColName_1(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 1) {
                            excel2.setColName_2(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 2) {
                            excel2.setColName_3(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 3) {
                            excel2.setColName_4(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 4) {
                            excel2.setColName_5(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 5) {
                            excel2.setColName_6(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 6) {
                            excel2.setColName_7(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 7) {
                            excel2.setColName_8(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                        if (i == 8) {
                            excel2.setColName_9(resultSet.get(1).get(0).get("colName_" + column.get(i)) + "");
                        }
                    }
                }
            }
            list.add(excel2);
        } else {
            headers = ArrayUtils.add(headers, "1-15");
            headers = ArrayUtils.add(headers, "16-30");
            headers = ArrayUtils.add(headers, "31-60");
            headers = ArrayUtils.add(headers, "61-90");
            headers = ArrayUtils.add(headers, "90+");
        }
        ExportExcel<ReportPayableAgeExcel> ex = new ExportExcel<ReportPayableAgeExcel>();

        ex.exportExcel(LoginUtils.getResponse(), "导出EXCEL", headers, list, "Export");

    }

    @Override
    public void exportExcel(ReportPayableAge reportPayableAge) {
    	if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()!=-1) {
    		reportPayableAge.setOrgId(reportPayableAge.getOtherOrg());
    	}else if(reportPayableAge.getOtherOrg()!=null&&reportPayableAge.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			reportPayableAge.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		reportPayableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
        if (StrUtil.isBlank(reportPayableAge.getSalesName())) {
            reportPayableAge.setSalesName(null);
        }
        reportPayableAge.setCurrentUserId(SecurityUtils.getUser().getId());
        List<ReportPayableAgeDetail> list = null;
        List<ReportPayableAgeDetailForExcel> listExcel = new ArrayList<>();
        String[] headers = {"业务范畴", "主单号", "订单", "发车日期", "订单客户代码", "订单客户", "付款客户代码", "付款客户", "责任客服", "责任销售", "币种", "付款金额(原币)", "付款金额(本币)", "未核销金额(原币)", "未核销金额(本币)"};
        if (reportPayableAge.getBusinessScope().startsWith("T")) {
            list = reportPayableAgeMapper.viewForTC(reportPayableAge);
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    ReportPayableAgeDetailForExcel reportPayableAgeDetailForExcel = new ReportPayableAgeDetailForExcel();
                    BeanUtils.copyProperties(list.get(i), reportPayableAgeDetailForExcel);
                    listExcel.add(reportPayableAgeDetailForExcel);
                }
            }
            if("TI".equals(reportPayableAge.getBusinessScope())){
                headers = new String[]{"业务范畴", "主单号", "订单", "到达日期", "订单客户代码", "订单客户", "付款客户代码", "付款客户", "责任客服", "责任销售", "币种", "付款金额(原币)", "付款金额(本币)", "未核销金额(原币)", "未核销金额(本币)"};
            }
        } else if (reportPayableAge.getBusinessScope().startsWith("L")) {
            list = reportPayableAgeMapper.viewForLC(reportPayableAge);
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    ReportPayableAgeDetailForExcel reportPayableAgeDetailForExcel = new ReportPayableAgeDetailForExcel();
                    BeanUtils.copyProperties(list.get(i), reportPayableAgeDetailForExcel);
                    listExcel.add(reportPayableAgeDetailForExcel);
                }
            }
            headers = new String[]{"业务范畴", "客户单号", "订单", "用车日期", "订单客户代码", "订单客户", "付款客户代码", "付款客户", "责任客服", "责任销售", "币种", "付款金额(原币)", "付款金额(本币)", "未核销金额(原币)", "未核销金额(本币)"};
        }else if (reportPayableAge.getBusinessScope().equals("IO")) {
            list = reportPayableAgeMapper.viewForIO(reportPayableAge);
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    ReportPayableAgeDetailForExcel reportPayableAgeDetailForExcel = new ReportPayableAgeDetailForExcel();
                    BeanUtils.copyProperties(list.get(i), reportPayableAgeDetailForExcel);
                    listExcel.add(reportPayableAgeDetailForExcel);
                }
            }
            headers = new String[]{"业务范畴", "客户单号", "订单", "业务日期", "订单客户代码", "订单客户", "付款客户代码", "付款客户", "责任客服", "责任销售", "币种", "付款金额(原币)", "付款金额(本币)", "未核销金额(原币)", "未核销金额(本币)"};
        }
        ExportExcel<ReportPayableAgeDetailForExcel> ex = new ExportExcel<ReportPayableAgeDetailForExcel>();

        ex.exportExcel(LoginUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");

    }
}
