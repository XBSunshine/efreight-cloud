package com.efreight.afbase.service.impl;

import com.efreight.afbase.dao.CssReportIncomeStatementMapper;
import com.efreight.afbase.dao.ServiceMapper;
import com.efreight.afbase.entity.CssReportIncomeStatement;
import com.efreight.afbase.service.CssReportIncomeStatementService;
import com.efreight.afbase.utils.LoginUtils;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class CssReportIncomeStatementServiceImpl implements CssReportIncomeStatementService {

    private final CssReportIncomeStatementMapper cssReportIncomeStatementMapper;
    private final ServiceMapper serviceMapper;

    @Override
    public List<Map<String, Object>> list(CssReportIncomeStatement cssReportIncomeStatement) {
    	if(cssReportIncomeStatement.getOtherOrg()!=null&&cssReportIncomeStatement.getOtherOrg()!=-1) {
    		cssReportIncomeStatement.setOrgId(cssReportIncomeStatement.getOtherOrg());
    	}else if(cssReportIncomeStatement.getOtherOrg()!=null&&cssReportIncomeStatement.getOtherOrg()==-1){
    		Map map = serviceMapper.queryGroupIdByOrgId(SecurityUtils.getUser().getOrgId());
    		if(map!=null&&map.containsKey("group_id")) {
    			cssReportIncomeStatement.setOrgId(map.get("group_id")!=null?Integer.valueOf(map.get("group_id").toString()):SecurityUtils.getUser().getOrgId());
    		}else {
    			//以防有破损数据 做一下默认值
    			cssReportIncomeStatement.setOrgId(SecurityUtils.getUser().getOrgId());
    		}
    	}else {
    		cssReportIncomeStatement.setOrgId(SecurityUtils.getUser().getOrgId());
    	}
        if (cssReportIncomeStatement.getLockDateChecked() == null) {
            cssReportIncomeStatement.setLockDateChecked(false);
        }
        if (cssReportIncomeStatement.getVoucherDateChecked() == null) {
            cssReportIncomeStatement.setVoucherDateChecked(false);
        }
        return cssReportIncomeStatementMapper.list(cssReportIncomeStatement);
    }

    @Override
    public void export(CssReportIncomeStatement cssReportIncomeStatement) {
        List<Map<String, Object>> list = list(cssReportIncomeStatement);
        List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
        String[] headers = new String[3];
        headers[0] = "项目";
        headers[1] = "本月金额";
        headers[2] = "本年累计金额";
        if (list != null && list.size() > 0) {
            for (Map<String, Object> excel : list) {
                LinkedHashMap map = new LinkedHashMap();
                for (int j = 0; j < headers.length; j++) {
                    if (!"项目".equals(headers[j])) {
                        map.put(headers[j], FormatUtils.formatWithQWF((BigDecimal) excel.get(headers[j]), 2));
                    } else {
                        map.put(headers[j], excel.get(headers[j]));
                    }
                }
                listExcel.add(map);
            }
        }
        ExcelExportUtils u = new ExcelExportUtils();
        u.exportExcelLinkListMap(LoginUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");
    }
}
