package com.efreight.afbase.service;

import com.efreight.afbase.entity.CssReportIncomeStatement;

import java.util.List;
import java.util.Map;

public interface CssReportIncomeStatementService {
    List<Map<String, Object>> list(CssReportIncomeStatement cssReportIncomeStatement);

    void export(CssReportIncomeStatement cssReportIncomeStatement);
}
