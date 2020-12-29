package com.efreight.afbase.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.exportExcel.ReportReceivableAgeDetail;
import com.efreight.afbase.entity.procedure.ReportReceivableAge;

import java.util.List;
import java.util.Map;

public interface ReportReceivableAgeService {

    Map<String,List> getPage(Page page, ReportReceivableAge reportReceivableAge);

    List<ReportReceivableAgeDetail> view(ReportReceivableAge reportReceivableAge);

    void exportExcel(ReportReceivableAge reportReceivableAge);
    void exportExcelList(ReportReceivableAge reportReceivableAge);
}
