package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.dao.ReportReceivableAgeMapper;
import com.efreight.afbase.dao.ServiceMapper;
import com.efreight.afbase.entity.exportExcel.ReportReceivableAgeDetail;
import com.efreight.afbase.entity.exportExcel.ReportReceivableAgeDetailExcel;
import com.efreight.afbase.entity.procedure.ReportReceivableAge;
import com.efreight.afbase.entity.procedure.ReportReceivableAgeExcel;
import com.efreight.afbase.service.ReportReceivableAgeService;
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
public class ReportReceivableAgeServiceImpl implements ReportReceivableAgeService {

    private final ReportReceivableAgeMapper reportReceivableAgeMapper;
    private final ServiceMapper serviceMapper;

    @Override
    public Map<String, List> getPage(Page page, ReportReceivableAge reportReceivableAge) {
        if(reportReceivableAge.getOtherOrg()!=null&&reportReceivableAge.getOtherOrg()!=-1) {
        	reportReceivableAge.setOrgId(reportReceivableAge.getOtherOrg());
    	}else if(reportReceivableAge.getOtherOrg()!=null&&reportReceivableAge.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			reportReceivableAge.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			reportReceivableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		reportReceivableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
        reportReceivableAge.setCurrentUserId(SecurityUtils.getUser().getId());
        if (reportReceivableAge.getDurationValid().equals("全部")) {
            reportReceivableAge.setDurationValid(null);
        }
        if (reportReceivableAge.getOverdueValid().equals("全部")) {
            reportReceivableAge.setOverdueValid(null);
        }
        if (StrUtil.isBlank(reportReceivableAge.getBusinessScope())) {
            reportReceivableAge.setBusinessScope(null);
        }
        if (StrUtil.isBlank(reportReceivableAge.getCountRanges())) {
            reportReceivableAge.setCountRanges(null);
        }
        if (StrUtil.isBlank(reportReceivableAge.getCustomerName())) {
            reportReceivableAge.setCustomerName(null);
        }
        if (StrUtil.isBlank(reportReceivableAge.getSalesName())) {
            reportReceivableAge.setSalesName(null);
        }
        List<List<LinkedHashMap<String, String>>> resultSet = new ArrayList<List<LinkedHashMap<String, String>>>();
        if ("AE".equals(reportReceivableAge.getBusinessScope()) || "AI".equals(reportReceivableAge.getBusinessScope())) {
            resultSet = reportReceivableAgeMapper.getPage(reportReceivableAge);
        } else if ("SE".equals(reportReceivableAge.getBusinessScope()) || "SI".equals(reportReceivableAge.getBusinessScope())) {
            resultSet = reportReceivableAgeMapper.getPageSC(reportReceivableAge);
        } else if (reportReceivableAge.getBusinessScope().startsWith("T")) {
            resultSet = reportReceivableAgeMapper.getPageTC(reportReceivableAge);
        } else if (reportReceivableAge.getBusinessScope().startsWith("L")) {
            resultSet = reportReceivableAgeMapper.getPageLC(reportReceivableAge);
        } else if (reportReceivableAge.getBusinessScope().equals("IO")) {
            resultSet = reportReceivableAgeMapper.getPageIO(reportReceivableAge);
        }
        HashMap<String, List> result = new HashMap<>();
        List<Map<String, String>> data = new ArrayList<>();
        ArrayList<String> column = new ArrayList<>();
        if (resultSet.size() == 2 && resultSet.get(0) != null && resultSet.get(0).size() > 0 && resultSet.get(0).get(0) != null && resultSet.get(1) != null && resultSet.get(1).size() > 0 && resultSet.get(1).get(0) != null) {
            //整理数据
            data.addAll(resultSet.get(0));
            resultSet.get(1).get(0).put("business_scope", "合计");
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
    public List<ReportReceivableAgeDetail> view(ReportReceivableAge reportReceivableAge) {
    	 if(reportReceivableAge.getOtherOrg()!=null&&reportReceivableAge.getOtherOrg()!=-1) {
         	reportReceivableAge.setOrgId(reportReceivableAge.getOtherOrg());
     	}else if(reportReceivableAge.getOtherOrg()!=null&&reportReceivableAge.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			reportReceivableAge.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			reportReceivableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
     		reportReceivableAge.setOrgId(SecurityUtils.getUser().getOrgId());
     	}
        if (StrUtil.isBlank(reportReceivableAge.getSalesName())) {
            reportReceivableAge.setSalesName(null);
        }
        reportReceivableAge.setCurrentUserId(SecurityUtils.getUser().getId());
        if ("AE".equals(reportReceivableAge.getBusinessScope()) || "AI".equals(reportReceivableAge.getBusinessScope())) {
            return reportReceivableAgeMapper.view(reportReceivableAge);
        } else if ("SE".equals(reportReceivableAge.getBusinessScope()) || "SI".equals(reportReceivableAge.getBusinessScope())) {
            return reportReceivableAgeMapper.viewSC(reportReceivableAge);
        } else if (reportReceivableAge.getBusinessScope().startsWith("T")) {
            return reportReceivableAgeMapper.viewTC(reportReceivableAge);
        } else if (reportReceivableAge.getBusinessScope().startsWith("L")) {
            return reportReceivableAgeMapper.viewLC(reportReceivableAge);
        } else if (reportReceivableAge.getBusinessScope().equals("IO")) {
            return reportReceivableAgeMapper.viewIO(reportReceivableAge);
        }
        return null;
    }

    @Override
    public void exportExcel(ReportReceivableAge reportReceivableAge) {
    	if(reportReceivableAge.getOtherOrg()!=null&&reportReceivableAge.getOtherOrg()!=-1) {
         	reportReceivableAge.setOrgId(reportReceivableAge.getOtherOrg());
     	}else if(reportReceivableAge.getOtherOrg()!=null&&reportReceivableAge.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			reportReceivableAge.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			reportReceivableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
     		reportReceivableAge.setOrgId(SecurityUtils.getUser().getOrgId());
     	}
        if (StrUtil.isBlank(reportReceivableAge.getSalesName())) {
            reportReceivableAge.setSalesName(null);
        }
        reportReceivableAge.setCurrentUserId(SecurityUtils.getUser().getId());
        List<ReportReceivableAgeDetail> list = new ArrayList<ReportReceivableAgeDetail>();
        String[] headers = null;
        if ("AE".equals(reportReceivableAge.getBusinessScope()) || "SE".equals(reportReceivableAge.getBusinessScope())) {
            headers = new String[]{"业务范畴", "主单号", "订单号", "订单客户代码", "订单客户", "收款客户代码", "收款客户", "责任客服", "责任销售", "开航日期", "应收金额(本币)", "已核销金额(本币)", "未核销金额(本币)"};
        } else if ("AI".equals(reportReceivableAge.getBusinessScope()) || "SI".equals(reportReceivableAge.getBusinessScope())) {
            headers = new String[]{"业务范畴", "主单号", "订单号", "订单客户代码", "订单客户", "收款客户代码", "收款客户", "责任客服", "责任销售", "到港日期", "应收金额(本币)", "已核销金额(本币)", "未核销金额(本币)"};
        } else if ("TE".equals(reportReceivableAge.getBusinessScope()) || "TI".equals(reportReceivableAge.getBusinessScope())) {
            headers = new String[]{"业务范畴", "提运单号", "订单号", "订单客户代码", "订单客户", "收款客户代码", "收款客户", "责任客服", "责任销售", "ETD", "应收金额(本币)", "已核销金额(本币)", "未核销金额(本币)"};
        } else if ("LC".equals(reportReceivableAge.getBusinessScope()) || "IO".equals(reportReceivableAge.getBusinessScope())) {
            headers = new String[]{"业务范畴", "客户单号", "订单号", "订单客户代码", "订单客户", "收款客户代码", "收款客户", "责任客服", "责任销售", "ETD", "应收金额(本币)", "已核销金额(本币)", "未核销金额(本币)"};
        }
        if ("AE".equals(reportReceivableAge.getBusinessScope()) || "AI".equals(reportReceivableAge.getBusinessScope())) {
            list = reportReceivableAgeMapper.view(reportReceivableAge);
        } else if ("SE".equals(reportReceivableAge.getBusinessScope()) || "SI".equals(reportReceivableAge.getBusinessScope())) {
            list = reportReceivableAgeMapper.viewSC(reportReceivableAge);
        } else if (reportReceivableAge.getBusinessScope().startsWith("T")) {
            list = reportReceivableAgeMapper.viewTC(reportReceivableAge);
        } else if (reportReceivableAge.getBusinessScope().startsWith("L")) {
            list = reportReceivableAgeMapper.viewLC(reportReceivableAge);
        } else if (reportReceivableAge.getBusinessScope().equals("IO")) {
            list = reportReceivableAgeMapper.viewIO(reportReceivableAge);
        }
        if (list != null && list.size() > 0) {
            List<ReportReceivableAgeDetailExcel> listTwo = list.stream().map(a -> {
                ReportReceivableAgeDetailExcel b = new ReportReceivableAgeDetailExcel();
                BeanUtils.copyProperties(a, b);
                return b;
            }).collect(Collectors.toList());
            ExportExcel<ReportReceivableAgeDetailExcel> ex = new ExportExcel<ReportReceivableAgeDetailExcel>();

            ex.exportExcel(LoginUtils.getResponse(), "导出EXCEL", headers, listTwo, "Export");
        }
    }

    @Override
    public void exportExcelList(ReportReceivableAge reportReceivableAge) {
    	if(reportReceivableAge.getOtherOrg()!=null&&reportReceivableAge.getOtherOrg()!=-1) {
         	reportReceivableAge.setOrgId(reportReceivableAge.getOtherOrg());
     	}else if(reportReceivableAge.getOtherOrg()!=null&&reportReceivableAge.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			reportReceivableAge.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			reportReceivableAge.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
     		reportReceivableAge.setOrgId(SecurityUtils.getUser().getOrgId());
     	}
        reportReceivableAge.setCurrentUserId(SecurityUtils.getUser().getId());
        if (reportReceivableAge.getDurationValid() != null && reportReceivableAge.getDurationValid().equals("全部")) {
            reportReceivableAge.setDurationValid(null);
        }
        if (reportReceivableAge.getOverdueValid().equals("全部")) {
            reportReceivableAge.setOverdueValid(null);
        }
        if (StrUtil.isBlank(reportReceivableAge.getBusinessScope())) {
            reportReceivableAge.setBusinessScope(null);
        }
        if (StrUtil.isBlank(reportReceivableAge.getCountRanges())) {
            reportReceivableAge.setCountRanges(null);
        }
        if (StrUtil.isBlank(reportReceivableAge.getCustomerName())) {
            reportReceivableAge.setCustomerName(null);
        }
        List<List<LinkedHashMap<String, String>>> resultSet = null;
        if (reportReceivableAge.getBusinessScope().startsWith("A")) {
            resultSet = reportReceivableAgeMapper.getPage(reportReceivableAge);
        } else if (reportReceivableAge.getBusinessScope().startsWith("S")) {
            resultSet = reportReceivableAgeMapper.getPageSC(reportReceivableAge);
        } else if (reportReceivableAge.getBusinessScope().startsWith("T")) {
            resultSet = reportReceivableAgeMapper.getPageTC(reportReceivableAge);
        } else if (reportReceivableAge.getBusinessScope().startsWith("L")) {
            resultSet = reportReceivableAgeMapper.getPageLC(reportReceivableAge);
        }else if (reportReceivableAge.getBusinessScope().equals("IO")) {
            resultSet = reportReceivableAgeMapper.getPageIO(reportReceivableAge);
        }
        String[] headers = {"业务范畴", "客户代码", "订单客户", "责任客服", "责任销售", "白名单", "信用等级", "授信额度(万元)","EQ", "授信期限(天)", "超期天数", "应收金额(本币)", "账期内金额(本币)", "超期金额(本币)"};
        List<ReportReceivableAgeExcel> list = new ArrayList<ReportReceivableAgeExcel>();
        ArrayList<String> column = new ArrayList<>();
        if (resultSet != null && resultSet.size() > 0 && resultSet.get(0) != null && resultSet.get(0).size() > 0) {
            Set<String> keySet = resultSet.get(0).get(0).keySet();

            List<String> listColname = keySet.stream().collect(Collectors.toList());
            for (String key : listColname) {
                if (key.contains("colName_")) {
                    column.add(key.replace("colName_", ""));
                }
            }
            list = resultSet.get(0).stream().map(o -> {
                ReportReceivableAgeExcel excel = new ReportReceivableAgeExcel();
                excel.setBusinessScope(o.get("business_scope") + "");
                excel.setCoopCode(o.get("coop_code") != null ? (o.get("coop_code") + "") : "");
                excel.setCoopName(o.get("coop_name"));
                excel.setServicerName(o.get("servicer_name"));
                excel.setSalesName(o.get("sales_name"));
                excel.setWhiteValid(o.get("white_valid") != null ? ("1".equals(o.get("white_valid")) ? "是" : "") : "");
                excel.setCreditLevel(o.get("credit_level").toString());
                excel.setCreditLimit(String.valueOf(o.get("credit_limit") == null ? "" : o.get("credit_limit")));
                excel.setCreditDuration(String.valueOf(o.get("credit_duration")));
                excel.setSettlementPeriod(String.valueOf(o.get("settlement_period")));
                excel.setOverdueDays(String.valueOf(o.get("overdue_days") == null ? "" : o.get("overdue_days")));
                excel.setFunctionalAmount(o.get("functional_amount"));
                excel.setNoFunctionalAmountWriteoffValid0(o.get("no_functional_amount_writeoff_valid_0").toString());
                excel.setNoFunctionalAmountWriteoffValid1(o.get("no_functional_amount_writeoff_valid_1").toString());
                String editionName = reportReceivableAge.getOrgEditionName();
                if (column != null && column.size() > 0) {
                    //后面需要 优化一下excel 导出  swich 容易卡死
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

            ReportReceivableAgeExcel excel2 = new ReportReceivableAgeExcel();
            excel2.setBusinessScope("合计");
            excel2.setFunctionalAmount(resultSet.get(1).get(0).get("functional_amount") + "");
            excel2.setNoFunctionalAmountWriteoffValid0(resultSet.get(1).get(0).get("no_functional_amount_writeoff_valid_0") + "");
            excel2.setNoFunctionalAmountWriteoffValid1(resultSet.get(1).get(0).get("no_functional_amount_writeoff_valid_1") + "");
            String editionName = reportReceivableAge.getOrgEditionName();
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
        ExportExcel<ReportReceivableAgeExcel> ex = new ExportExcel<ReportReceivableAgeExcel>();

        ex.exportExcel(LoginUtils.getResponse(), "导出EXCEL", headers, list, "Export");

    }
}
