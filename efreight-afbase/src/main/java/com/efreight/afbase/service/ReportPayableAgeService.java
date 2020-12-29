package com.efreight.afbase.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.exportExcel.ReportPayableAgeDetail;
import com.efreight.afbase.entity.procedure.ReportPayableAge;

import java.util.List;
import java.util.Map;

public interface ReportPayableAgeService {

    Map<String, List> getPageForAF(Page page, ReportPayableAge reportPayableAge);

    List<ReportPayableAgeDetail> viewForAF(ReportPayableAge reportPayableAge);

    Map<String, List> getPageForSC(Page page, ReportPayableAge reportPayableAge);

    List<ReportPayableAgeDetail> viewForSC(ReportPayableAge reportPayableAge);

    void exportExcelForAF(ReportPayableAge reportPayableAge);

    void exportExcelListForAF(ReportPayableAge reportPayableAge);

    void exportExcelForSC(ReportPayableAge reportPayableAge);

    void exportExcelListForSC(ReportPayableAge reportPayableAge);
    
    Map<String, List> getPageList(Page page, ReportPayableAge reportPayableAge);
    List<ReportPayableAgeDetail> view(ReportPayableAge reportPayableAge);
    void exportExcelList(ReportPayableAge reportPayableAge);
    void exportExcel(ReportPayableAge reportPayableAge);
}
