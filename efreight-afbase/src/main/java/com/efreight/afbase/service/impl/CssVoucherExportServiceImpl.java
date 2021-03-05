package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.dao.CssVoucherExportMapper;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.service.*;
import com.efreight.afbase.utils.FilePathUtils;
import com.efreight.afbase.utils.LoginUtils;
import com.efreight.common.core.feign.RemoteCoopService;
import com.efreight.common.core.feign.RemoteServiceToHRS;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.core.utils.JxlsUtils;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.common.security.vo.CoopVo;
import com.efreight.common.security.vo.OrgVo;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class CssVoucherExportServiceImpl implements CssVoucherExportService {

    private final CssVoucherExportMapper cssVoucherExportMapper;

    private final CssFinancialVoucherNumberLogService cssFinancialVoucherNumberLogService;

    private final CssPaymentService cssPaymentService;

    private final CssDebitNoteService cssDebitNoteService;

    private final CssDebitNoteCurrencyService cssDebitNoteCurrencyService;

    private final StatementService statementService;

    private final StatementCurrencyService statementCurrencyService;

    private final CssCostInvoiceDetailService cssCostInvoiceDetailService;

    private final CssIncomeInvoiceDetailService cssIncomeInvoiceDetailService;

    private final CssCostInvoiceDetailWriteoffService cssCostInvoiceDetailWriteoffService;

    private final CssIncomeInvoiceDetailWriteoffService cssIncomeInvoiceDetailWriteoffService;

    private final RemoteServiceToHRS remoteServiceToHRS;

    private final RemoteCoopService remoteCoopService;

    private final CssFinancialAccountService cssFinancialAccountService;

    @Override
    public IPage getPage(Page page, CssVoucherExport cssVoucherExport) {
        IPage<CssVoucherExport> result = new Page<CssVoucherExport>();
        cssVoucherExport.setOrgId(SecurityUtils.getUser().getOrgId());
        if (cssVoucherExport.getType() == 0) {
            LambdaQueryWrapper<CssIncomeInvoiceDetail> cssIncomeInvoiceDetailWrapper = Wrappers.<CssIncomeInvoiceDetail>lambdaQuery();
            cssIncomeInvoiceDetailWrapper.eq(CssIncomeInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssIncomeInvoiceDetail::getBusinessScope, cssVoucherExport.getBusinessScope());
            if (cssVoucherExport.getVoucherStatus() != null) {
                if (cssVoucherExport.getVoucherStatus()) {
                    cssIncomeInvoiceDetailWrapper.isNotNull(CssIncomeInvoiceDetail::getVoucherDate);
                } else {
                    cssIncomeInvoiceDetailWrapper.isNull(CssIncomeInvoiceDetail::getVoucherDate);
                }
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getInvoiceNum())) {
                cssIncomeInvoiceDetailWrapper.like(CssIncomeInvoiceDetail::getInvoiceNum, cssVoucherExport.getInvoiceNum());
            }

            if (cssVoucherExport.getInvoiceDateStart() != null) {
                cssIncomeInvoiceDetailWrapper.ge(CssIncomeInvoiceDetail::getInvoiceDate, cssVoucherExport.getInvoiceDateStart());
            }

            if (cssVoucherExport.getInvoiceDateEnd() != null) {
                cssIncomeInvoiceDetailWrapper.le(CssIncomeInvoiceDetail::getInvoiceDate, cssVoucherExport.getInvoiceDateEnd());
            }

            if (StrUtil.isNotBlank(cssVoucherExport.getCustomerName())) {
                cssIncomeInvoiceDetailWrapper.like(CssIncomeInvoiceDetail::getCustomerName, cssVoucherExport.getCustomerName());
            }

            if (StrUtil.isNotBlank(cssVoucherExport.getVoucherCreatorName())) {
                cssIncomeInvoiceDetailWrapper.like(CssIncomeInvoiceDetail::getVoucherCreatorName, cssVoucherExport.getVoucherCreatorName());
            }

            if (cssVoucherExport.getVoucherDateStart() != null) {
                cssIncomeInvoiceDetailWrapper.ge(CssIncomeInvoiceDetail::getVoucherDate, cssVoucherExport.getVoucherDateStart());
            }

            if (cssVoucherExport.getVoucherDateEnd() != null) {
                cssIncomeInvoiceDetailWrapper.le(CssIncomeInvoiceDetail::getVoucherDate, cssVoucherExport.getVoucherDateEnd());
            }
            cssIncomeInvoiceDetailWrapper.orderByDesc(CssIncomeInvoiceDetail::getInvoiceDetailId);
            IPage<CssIncomeInvoiceDetail> cssIncomeInvoiceDetailPage = cssIncomeInvoiceDetailService.page(page, cssIncomeInvoiceDetailWrapper);
            List<CssVoucherExport> cssVoucherExportList = cssIncomeInvoiceDetailPage.getRecords().stream().map(cssIncomeInvoiceDetail -> {
                CssVoucherExport voucherExport = new CssVoucherExport();
                BeanUtils.copyProperties(cssIncomeInvoiceDetail, voucherExport);
                voucherExport.setAmountStr(FormatUtils.formatWithQWF(cssIncomeInvoiceDetail.getAmount(), 2) + " (" + cssIncomeInvoiceDetail.getCurrency() + ")");
                return voucherExport;
            }).collect(Collectors.toList());
            result.setCurrent(cssIncomeInvoiceDetailPage.getCurrent());
            result.setSize(cssIncomeInvoiceDetailPage.getSize());
            result.setTotal(cssIncomeInvoiceDetailPage.getTotal());
            result.setRecords(cssVoucherExportList);
        } else if (cssVoucherExport.getType() == 1) {
            LambdaQueryWrapper<CssCostInvoiceDetail> cssCostInvoiceDetailWrapper = Wrappers.<CssCostInvoiceDetail>lambdaQuery();
            cssCostInvoiceDetailWrapper.eq(CssCostInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoiceDetail::getBusinessScope, cssVoucherExport.getBusinessScope());
            if (cssVoucherExport.getVoucherStatus() != null) {
                if (cssVoucherExport.getVoucherStatus()) {
                    cssCostInvoiceDetailWrapper.isNotNull(CssCostInvoiceDetail::getVoucherDate);
                } else {
                    cssCostInvoiceDetailWrapper.isNull(CssCostInvoiceDetail::getVoucherDate);
                }
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getInvoiceNum())) {
                cssCostInvoiceDetailWrapper.like(CssCostInvoiceDetail::getInvoiceNum, cssVoucherExport.getInvoiceNum());
            }

            if (cssVoucherExport.getInvoiceDateStart() != null) {
                cssCostInvoiceDetailWrapper.ge(CssCostInvoiceDetail::getInvoiceDate, cssVoucherExport.getInvoiceDateStart());
            }

            if (cssVoucherExport.getInvoiceDateEnd() != null) {
                cssCostInvoiceDetailWrapper.le(CssCostInvoiceDetail::getInvoiceDate, cssVoucherExport.getInvoiceDateEnd());
            }

            if (StrUtil.isNotBlank(cssVoucherExport.getCustomerName())) {
                cssCostInvoiceDetailWrapper.like(CssCostInvoiceDetail::getCustomerName, cssVoucherExport.getCustomerName());
            }

            if (StrUtil.isNotBlank(cssVoucherExport.getVoucherCreatorName())) {
                cssCostInvoiceDetailWrapper.like(CssCostInvoiceDetail::getVoucherCreatorName, cssVoucherExport.getVoucherCreatorName());
            }

            if (cssVoucherExport.getVoucherDateStart() != null) {
                cssCostInvoiceDetailWrapper.ge(CssCostInvoiceDetail::getVoucherDate, cssVoucherExport.getVoucherDateStart());
            }

            if (cssVoucherExport.getVoucherDateEnd() != null) {
                cssCostInvoiceDetailWrapper.le(CssCostInvoiceDetail::getVoucherDate, cssVoucherExport.getVoucherDateEnd());
            }
            cssCostInvoiceDetailWrapper.orderByDesc(CssCostInvoiceDetail::getInvoiceDetailId);
            IPage<CssCostInvoiceDetail> cssCostInvoiceDetailPage = cssCostInvoiceDetailService.page(page, cssCostInvoiceDetailWrapper);
            List<CssVoucherExport> cssVoucherExportList = cssCostInvoiceDetailPage.getRecords().stream().map(cssCostInvoiceDetail -> {
                CssVoucherExport voucherExport = new CssVoucherExport();
                BeanUtils.copyProperties(cssCostInvoiceDetail, voucherExport);
                voucherExport.setAmountStr(FormatUtils.formatWithQWF(cssCostInvoiceDetail.getAmount(), 2) + " (" + cssCostInvoiceDetail.getCurrency() + ")");
                return voucherExport;
            }).collect(Collectors.toList());
            result.setCurrent(cssCostInvoiceDetailPage.getCurrent());
            result.setSize(cssCostInvoiceDetailPage.getSize());
            result.setTotal(cssCostInvoiceDetailPage.getTotal());
            result.setRecords(cssVoucherExportList);


        } else if (cssVoucherExport.getType() == 2) {
            LambdaQueryWrapper<CssIncomeInvoiceDetailWriteoff> cssIncomeInvoiceDetailWriteoffWrapper = Wrappers.<CssIncomeInvoiceDetailWriteoff>lambdaQuery();
            cssIncomeInvoiceDetailWriteoffWrapper.eq(CssIncomeInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssIncomeInvoiceDetailWriteoff::getBusinessScope, cssVoucherExport.getBusinessScope());
            if (StrUtil.isNotBlank(cssVoucherExport.getCustomerName())) {
                cssIncomeInvoiceDetailWriteoffWrapper.like(CssIncomeInvoiceDetailWriteoff::getCustomerName, cssVoucherExport.getCustomerName());
            }
            if (cssVoucherExport.getVoucherStatus() != null) {
                if (cssVoucherExport.getVoucherStatus()) {
                    cssIncomeInvoiceDetailWriteoffWrapper.isNotNull(CssIncomeInvoiceDetailWriteoff::getVoucherDate);
                } else {
                    cssIncomeInvoiceDetailWriteoffWrapper.isNull(CssIncomeInvoiceDetailWriteoff::getVoucherDate);
                }
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getWriteoffNum())) {
                cssIncomeInvoiceDetailWriteoffWrapper.like(CssIncomeInvoiceDetailWriteoff::getWriteoffNum, cssVoucherExport.getWriteoffNum());
            }
            if (cssVoucherExport.getWriteoffDateStart() != null) {
                cssIncomeInvoiceDetailWriteoffWrapper.ge(CssIncomeInvoiceDetailWriteoff::getWriteoffDate, cssVoucherExport.getWriteoffDateStart());
            }
            if (cssVoucherExport.getWriteoffDateEnd() != null) {
                cssIncomeInvoiceDetailWriteoffWrapper.le(CssIncomeInvoiceDetailWriteoff::getWriteoffDate, cssVoucherExport.getWriteoffDateEnd());
            }

            if (cssVoucherExport.getVoucherDateStart() != null) {
                cssIncomeInvoiceDetailWriteoffWrapper.ge(CssIncomeInvoiceDetailWriteoff::getVoucherDate, cssVoucherExport.getVoucherDateStart());
            }
            if (cssVoucherExport.getVoucherDateEnd() != null) {
                cssIncomeInvoiceDetailWriteoffWrapper.le(CssIncomeInvoiceDetailWriteoff::getVoucherDate, cssVoucherExport.getVoucherDateEnd());
            }

            if (cssVoucherExport.getInvoiceDateStart() != null || cssVoucherExport.getInvoiceDateEnd() != null || StrUtil.isNotBlank(cssVoucherExport.getInvoiceNum())) {
                LambdaQueryWrapper<CssIncomeInvoiceDetail> cssIncomeInvoiceDetailWrapper = Wrappers.<CssIncomeInvoiceDetail>lambdaQuery();
                cssIncomeInvoiceDetailWrapper.eq(CssIncomeInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssIncomeInvoiceDetail::getBusinessScope, cssVoucherExport.getBusinessScope());
                if (cssVoucherExport.getInvoiceDateStart() != null) {
                    cssIncomeInvoiceDetailWrapper.ge(CssIncomeInvoiceDetail::getInvoiceDate, cssVoucherExport.getInvoiceDateStart());
                }
                if (cssVoucherExport.getInvoiceDateEnd() != null) {
                    cssIncomeInvoiceDetailWrapper.le(CssIncomeInvoiceDetail::getInvoiceDate, cssVoucherExport.getInvoiceDateEnd());
                }
                if (StrUtil.isNotBlank(cssVoucherExport.getInvoiceNum())) {
                    cssIncomeInvoiceDetailWrapper.like(CssIncomeInvoiceDetail::getInvoiceNum, cssVoucherExport.getInvoiceNum());
                }
                List<Integer> invoiceDetailIds = cssIncomeInvoiceDetailService.list(cssIncomeInvoiceDetailWrapper).stream().map(CssIncomeInvoiceDetail::getInvoiceDetailId).collect(Collectors.toList());
                if (invoiceDetailIds.isEmpty()) {
                    result.setCurrent(page.getCurrent());
                    result.setSize(page.getSize());
                    result.setTotal(page.getTotal());
                    result.setRecords(new ArrayList<>());
                    return result;
                }
                cssIncomeInvoiceDetailWriteoffWrapper.in(CssIncomeInvoiceDetailWriteoff::getInvoiceDetailId, invoiceDetailIds);
            }

            if (StrUtil.isNotBlank(cssVoucherExport.getVoucherCreatorName())) {
                cssIncomeInvoiceDetailWriteoffWrapper.like(CssIncomeInvoiceDetailWriteoff::getVoucherCreatorName, cssVoucherExport.getVoucherCreatorName());
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getVoucherNumber())) {
                cssIncomeInvoiceDetailWriteoffWrapper.like(CssIncomeInvoiceDetailWriteoff::getVoucherNumber, cssVoucherExport.getVoucherNumber());
            }
            cssIncomeInvoiceDetailWriteoffWrapper.orderByDesc(CssIncomeInvoiceDetailWriteoff::getInvoiceDetailWriteoffId);
            IPage<CssIncomeInvoiceDetailWriteoff> cssIncomeInvoiceDetailWriteoffIPage = cssIncomeInvoiceDetailWriteoffService.page(page, cssIncomeInvoiceDetailWriteoffWrapper);
            List<CssVoucherExport> voucherExportList = cssIncomeInvoiceDetailWriteoffIPage.getRecords().stream().map(cssIncomeInvoiceDetailWriteoff -> {
                CssVoucherExport voucherExport = new CssVoucherExport();
                BeanUtils.copyProperties(cssIncomeInvoiceDetailWriteoff, voucherExport);
                voucherExport.setWriteoffAmount(cssIncomeInvoiceDetailWriteoff.getAmountWriteoff());
                voucherExport.setWriteoffAmountStr(FormatUtils.formatWithQWF(voucherExport.getWriteoffAmount(), 2) + " (" + voucherExport.getCurrency() + ")");
                if (cssIncomeInvoiceDetailWriteoff.getVoucherNumber() != null) {
                    voucherExport.setVoucherNumber(cssIncomeInvoiceDetailWriteoff.getVoucherNumber().toString());
                }
                CssIncomeInvoiceDetail cssIncomeInvoiceDetail = cssIncomeInvoiceDetailService.getById(cssIncomeInvoiceDetailWriteoff.getInvoiceDetailId());
                if (cssIncomeInvoiceDetail != null) {
                    voucherExport.setBankFinancialAccount(StrUtil.isBlank(voucherExport.getFinancialAccountName()) ? "" : voucherExport.getFinancialAccountName().split(" ")[0]);
                    voucherExport.setInvoiceTitle(cssIncomeInvoiceDetail.getInvoiceTitle());
                    voucherExport.setInvoiceNum(cssIncomeInvoiceDetail.getInvoiceNum());
                    voucherExport.setInvoiceDate(cssIncomeInvoiceDetail.getInvoiceDate());
                    voucherExport.setInvoiceType(cssIncomeInvoiceDetail.getInvoiceType());
                    voucherExport.setAmount(cssIncomeInvoiceDetail.getAmount());
                    voucherExport.setAmountStr(FormatUtils.formatWithQWF(cssIncomeInvoiceDetail.getAmount(), 2) + " (" + cssIncomeInvoiceDetail.getCurrency() + ")");
                }
                return voucherExport;
            }).collect(Collectors.toList());
            result.setTotal(cssIncomeInvoiceDetailWriteoffIPage.getTotal());
            result.setRecords(voucherExportList);
            result.setCurrent(cssIncomeInvoiceDetailWriteoffIPage.getCurrent());
            result.setSize(cssIncomeInvoiceDetailWriteoffIPage.getSize());
        } else if (cssVoucherExport.getType() == 3) {
            LambdaQueryWrapper<CssCostInvoiceDetailWriteoff> cssCostInvoiceDetailWriteoffWrapper = Wrappers.<CssCostInvoiceDetailWriteoff>lambdaQuery();
            cssCostInvoiceDetailWriteoffWrapper.eq(CssCostInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoiceDetailWriteoff::getBusinessScope, cssVoucherExport.getBusinessScope());
            if (StrUtil.isNotBlank(cssVoucherExport.getCustomerName())) {
                cssCostInvoiceDetailWriteoffWrapper.like(CssCostInvoiceDetailWriteoff::getCustomerName, cssVoucherExport.getCustomerName());
            }
            if (cssVoucherExport.getVoucherStatus() != null) {
                if (cssVoucherExport.getVoucherStatus()) {
                    cssCostInvoiceDetailWriteoffWrapper.isNotNull(CssCostInvoiceDetailWriteoff::getVoucherDate);
                } else {
                    cssCostInvoiceDetailWriteoffWrapper.isNull(CssCostInvoiceDetailWriteoff::getVoucherDate);
                }
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getWriteoffNum())) {
                cssCostInvoiceDetailWriteoffWrapper.like(CssCostInvoiceDetailWriteoff::getWriteoffNum, cssVoucherExport.getWriteoffNum());
            }
            if (cssVoucherExport.getWriteoffDateStart() != null) {
                cssCostInvoiceDetailWriteoffWrapper.ge(CssCostInvoiceDetailWriteoff::getWriteoffDate, cssVoucherExport.getWriteoffDateStart());
            }
            if (cssVoucherExport.getWriteoffDateEnd() != null) {
                cssCostInvoiceDetailWriteoffWrapper.le(CssCostInvoiceDetailWriteoff::getWriteoffDate, cssVoucherExport.getWriteoffDateEnd());
            }

            if (cssVoucherExport.getVoucherDateStart() != null) {
                cssCostInvoiceDetailWriteoffWrapper.ge(CssCostInvoiceDetailWriteoff::getVoucherDate, cssVoucherExport.getVoucherDateStart());
            }
            if (cssVoucherExport.getVoucherDateEnd() != null) {
                cssCostInvoiceDetailWriteoffWrapper.le(CssCostInvoiceDetailWriteoff::getVoucherDate, cssVoucherExport.getVoucherDateEnd());
            }

            if (cssVoucherExport.getInvoiceDateStart() != null || cssVoucherExport.getInvoiceDateEnd() != null || StrUtil.isNotBlank(cssVoucherExport.getInvoiceNum())) {
                LambdaQueryWrapper<CssCostInvoiceDetail> cssCostInvoiceDetailWrapper = Wrappers.<CssCostInvoiceDetail>lambdaQuery();
                cssCostInvoiceDetailWrapper.eq(CssCostInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoiceDetail::getBusinessScope, cssVoucherExport.getBusinessScope());
                if (cssVoucherExport.getInvoiceDateStart() != null) {
                    cssCostInvoiceDetailWrapper.ge(CssCostInvoiceDetail::getInvoiceDate, cssVoucherExport.getInvoiceDateStart());
                }
                if (cssVoucherExport.getInvoiceDateEnd() != null) {
                    cssCostInvoiceDetailWrapper.le(CssCostInvoiceDetail::getInvoiceDate, cssVoucherExport.getInvoiceDateEnd());
                }
                if (StrUtil.isNotBlank(cssVoucherExport.getInvoiceNum())) {
                    cssCostInvoiceDetailWrapper.like(CssCostInvoiceDetail::getInvoiceNum, cssVoucherExport.getInvoiceNum());
                }
                List<Integer> invoiceDetailIds = cssCostInvoiceDetailService.list(cssCostInvoiceDetailWrapper).stream().map(CssCostInvoiceDetail::getInvoiceDetailId).collect(Collectors.toList());
                if (invoiceDetailIds.isEmpty()) {
                    result.setCurrent(page.getCurrent());
                    result.setSize(page.getSize());
                    result.setTotal(page.getTotal());
                    result.setRecords(new ArrayList<>());
                    return result;
                }
                cssCostInvoiceDetailWriteoffWrapper.in(CssCostInvoiceDetailWriteoff::getInvoiceDetailId, invoiceDetailIds);
            }

            if (StrUtil.isNotBlank(cssVoucherExport.getVoucherCreatorName())) {
                cssCostInvoiceDetailWriteoffWrapper.like(CssCostInvoiceDetailWriteoff::getVoucherCreatorName, cssVoucherExport.getVoucherCreatorName());
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getVoucherNumber())) {
                cssCostInvoiceDetailWriteoffWrapper.like(CssCostInvoiceDetailWriteoff::getVoucherNumber, cssVoucherExport.getVoucherNumber());
            }
            cssCostInvoiceDetailWriteoffWrapper.orderByDesc(CssCostInvoiceDetailWriteoff::getInvoiceDetailWriteoffId);
            IPage<CssCostInvoiceDetailWriteoff> cssCostInvoiceDetailWriteoffIPage = cssCostInvoiceDetailWriteoffService.page(page, cssCostInvoiceDetailWriteoffWrapper);
            List<CssVoucherExport> voucherExportList = cssCostInvoiceDetailWriteoffIPage.getRecords().stream().map(cssCostInvoiceDetailWriteoff -> {
                CssVoucherExport voucherExport = new CssVoucherExport();
                BeanUtils.copyProperties(cssCostInvoiceDetailWriteoff, voucherExport);
                voucherExport.setWriteoffAmount(cssCostInvoiceDetailWriteoff.getAmountWriteoff());
                voucherExport.setWriteoffAmountStr(FormatUtils.formatWithQWF(voucherExport.getWriteoffAmount(), 2) + " (" + voucherExport.getCurrency() + ")");
                if (cssCostInvoiceDetailWriteoff.getVoucherNumber() != null) {
                    voucherExport.setVoucherNumber(cssCostInvoiceDetailWriteoff.getVoucherNumber().toString());
                }
                CssCostInvoiceDetail cssCostInvoiceDetail = cssCostInvoiceDetailService.getById(cssCostInvoiceDetailWriteoff.getInvoiceDetailId());
                if (cssCostInvoiceDetail != null) {
                    voucherExport.setBankFinancialAccount(voucherExport.getFinancialAccountName());
                    voucherExport.setInvoiceNum(cssCostInvoiceDetail.getInvoiceNum());
                    voucherExport.setInvoiceDate(cssCostInvoiceDetail.getInvoiceDate());
                    voucherExport.setInvoiceType(cssCostInvoiceDetail.getInvoiceType());
                    voucherExport.setAmount(cssCostInvoiceDetail.getAmount());
                    voucherExport.setAmountStr(FormatUtils.formatWithQWF(cssCostInvoiceDetail.getAmount(), 2) + " (" + cssCostInvoiceDetail.getCurrency() + ")");
                }
                return voucherExport;
            }).collect(Collectors.toList());
            result.setTotal(cssCostInvoiceDetailWriteoffIPage.getTotal());
            result.setRecords(voucherExportList);
            result.setCurrent(cssCostInvoiceDetailWriteoffIPage.getCurrent());
            result.setSize(cssCostInvoiceDetailWriteoffIPage.getSize());
        } else if (cssVoucherExport.getType() == 4) {
            result = cssVoucherExportMapper.pageExpenseReport(page, cssVoucherExport);
            result.getRecords().stream().forEach(voucher -> {
                voucher.setExpenseAmountStr(FormatUtils.formatWithQWF(voucher.getExpenseAmount(), 2));
                voucher.setFinancialAccount((StrUtil.isBlank(voucher.getExpenseFinancialAccountCode()) ? "" : voucher.getExpenseFinancialAccountCode()) + " " + (StrUtil.isBlank(voucher.getExpenseFinancialAccountName()) ? "" : voucher.getExpenseFinancialAccountName()));
                voucher.setBankFinancialAccount((StrUtil.isBlank(voucher.getBankFinancialAccountCode()) ? "" : voucher.getBankFinancialAccountCode()) + " " + (StrUtil.isBlank(voucher.getBankFinancialAccountName()) ? "" : voucher.getBankFinancialAccountName()));
            });
        }
        return result;
    }

    @Override
    public void exportExcel(CssVoucherExport cssVoucherExport) {
        List<CssVoucherExport> list = getList(cssVoucherExport);
        List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
        if (StrUtil.isNotBlank(cssVoucherExport.getColumnStrs())) {
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(cssVoucherExport.getColumnStrs());
            String[] headers = new String[jsonArr.size()];
            String[] colunmStrs = new String[jsonArr.size()];

            //生成表头跟字段
            if (jsonArr != null && jsonArr.size() > 0) {
                for (int i = 0; i < jsonArr.size(); i++) {
                    JSONObject job = jsonArr.getJSONObject(i);
                    headers[i] = job.getString("label");
                    colunmStrs[i] = job.getString("prop");
                }
            }

            if (list != null && list.size() > 0) {
                CssVoucherExport total = total(cssVoucherExport);
                list.add(total);
                if (cssVoucherExport.getType() == 0) {
                    for (CssVoucherExport excel : list) {
                        LinkedHashMap map = new LinkedHashMap();
                        for (int j = 0; j < colunmStrs.length; j++) {
                            if ("voucherCreatorName".equals(colunmStrs[j])) {
                                if (StrUtil.isNotBlank(excel.getVoucherCreatorName())) {
                                    map.put(colunmStrs[j], excel.getVoucherCreatorName().split(" ")[0]);
                                } else {
                                    map.put(colunmStrs[j], "");
                                }
                            } else if ("amount".equals(colunmStrs[j])) {
                                map.put(colunmStrs[j], excel.getAmountStr().replace("  ", "\n"));
                            } else {
                                map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], excel));
                            }
                        }
                        listExcel.add(map);
                    }
                } else if (cssVoucherExport.getType() == 1) {
                    for (CssVoucherExport excel : list) {
                        LinkedHashMap map = new LinkedHashMap();
                        for (int j = 0; j < colunmStrs.length; j++) {
                            if ("voucherCreatorName".equals(colunmStrs[j])) {
                                if (StrUtil.isNotBlank(excel.getVoucherCreatorName())) {
                                    map.put(colunmStrs[j], excel.getVoucherCreatorName().split(" ")[0]);
                                } else {
                                    map.put(colunmStrs[j], "");
                                }
                            } else if ("amount".equals(colunmStrs[j])) {
                                map.put(colunmStrs[j], excel.getAmountStr().replace("  ", "\n"));
                            } else {
                                map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], excel));
                            }
                        }
                        listExcel.add(map);
                    }
                } else if (cssVoucherExport.getType() == 2) {
                    for (CssVoucherExport excel : list) {
                        LinkedHashMap map = new LinkedHashMap();
                        for (int j = 0; j < colunmStrs.length; j++) {
                            if ("voucherCreatorName".equals(colunmStrs[j])) {
                                if (StrUtil.isNotBlank(excel.getVoucherCreatorName())) {
                                    map.put(colunmStrs[j], excel.getVoucherCreatorName().split(" ")[0]);
                                } else {
                                    map.put(colunmStrs[j], "");
                                }
                            } else if ("writeoffAmount".equals(colunmStrs[j])) {
                                map.put(colunmStrs[j], excel.getWriteoffAmountStr().replace("  ", "\n"));
                            } else if ("amount".equals(colunmStrs[j])) {
                                map.put(colunmStrs[j], excel.getAmountStr().replace("  ", "\n"));
                            } else {
                                map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], excel));
                            }
                        }
                        listExcel.add(map);
                    }
                } else if (cssVoucherExport.getType() == 3) {
                    for (CssVoucherExport excel : list) {
                        LinkedHashMap map = new LinkedHashMap();
                        for (int j = 0; j < colunmStrs.length; j++) {
                            if ("voucherCreatorName".equals(colunmStrs[j])) {
                                if (StrUtil.isNotBlank(excel.getVoucherCreatorName())) {
                                    map.put(colunmStrs[j], excel.getVoucherCreatorName().split(" ")[0]);
                                } else {
                                    map.put(colunmStrs[j], "");
                                }
                            } else if ("writeoffAmount".equals(colunmStrs[j])) {
                                map.put(colunmStrs[j], excel.getWriteoffAmountStr().replace("  ", "\n"));
                            } else if ("amount".equals(colunmStrs[j])) {
                                map.put(colunmStrs[j], excel.getAmountStr().replace("  ", "\n"));
                            } else {
                                map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], excel));
                            }
                        }
                        listExcel.add(map);
                    }
                } else if (cssVoucherExport.getType() == 4) {
                    for (CssVoucherExport excel : list) {
                        LinkedHashMap map = new LinkedHashMap();
                        for (int j = 0; j < colunmStrs.length; j++) {
                            if ("voucherCreatorName".equals(colunmStrs[j])) {
                                if (StrUtil.isNotBlank(excel.getVoucherCreatorName())) {
                                    map.put(colunmStrs[j], excel.getVoucherCreatorName().split(" ")[0]);
                                } else {
                                    map.put(colunmStrs[j], "");
                                }
                            } else if ("expenseCreatorName".equals(colunmStrs[j])) {
                                if (StrUtil.isNotBlank(excel.getExpenseCreatorName())) {
                                    map.put(colunmStrs[j], excel.getExpenseCreatorName().split(" ")[0]);
                                } else {
                                    map.put(colunmStrs[j], "");
                                }
                            } else if ("approvalFinancialUserName".equals(colunmStrs[j])) {
                                if (StrUtil.isNotBlank(excel.getApprovalFinancialUserName())) {
                                    map.put(colunmStrs[j], excel.getApprovalFinancialUserName().split(" ")[0]);
                                } else {
                                    map.put(colunmStrs[j], "");
                                }
                            } else if ("expenseAmount".equals(colunmStrs[j])) {
                                map.put(colunmStrs[j], excel.getExpenseAmountStr());
                            } else {
                                map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], excel));
                            }
                        }
                        listExcel.add(map);
                    }
                }
                listExcel.get(listExcel.size() - 1).put(colunmStrs[0], "合计");
            }
            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(LoginUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");
        }
    }

    private List<CssVoucherExport> getList(CssVoucherExport cssVoucherExport) {
        List<CssVoucherExport> list = new ArrayList<>();
        cssVoucherExport.setOrgId(SecurityUtils.getUser().getOrgId());
        if (cssVoucherExport.getType() == 0) {
            LambdaQueryWrapper<CssIncomeInvoiceDetail> cssIncomeInvoiceDetailWrapper = Wrappers.<CssIncomeInvoiceDetail>lambdaQuery();
            cssIncomeInvoiceDetailWrapper.eq(CssIncomeInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssIncomeInvoiceDetail::getBusinessScope, cssVoucherExport.getBusinessScope());
            if (cssVoucherExport.getVoucherStatus() != null) {
                if (cssVoucherExport.getVoucherStatus()) {
                    cssIncomeInvoiceDetailWrapper.isNotNull(CssIncomeInvoiceDetail::getVoucherDate);
                } else {
                    cssIncomeInvoiceDetailWrapper.isNull(CssIncomeInvoiceDetail::getVoucherDate);
                }
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getInvoiceNum())) {
                cssIncomeInvoiceDetailWrapper.like(CssIncomeInvoiceDetail::getInvoiceNum, cssVoucherExport.getInvoiceNum());
            }

            if (cssVoucherExport.getInvoiceDateStart() != null) {
                cssIncomeInvoiceDetailWrapper.ge(CssIncomeInvoiceDetail::getInvoiceDate, cssVoucherExport.getInvoiceDateStart());
            }

            if (cssVoucherExport.getInvoiceDateEnd() != null) {
                cssIncomeInvoiceDetailWrapper.le(CssIncomeInvoiceDetail::getInvoiceDate, cssVoucherExport.getInvoiceDateEnd());
            }

            if (StrUtil.isNotBlank(cssVoucherExport.getCustomerName())) {
                cssIncomeInvoiceDetailWrapper.like(CssIncomeInvoiceDetail::getCustomerName, cssVoucherExport.getCustomerName());
            }

            if (StrUtil.isNotBlank(cssVoucherExport.getVoucherCreatorName())) {
                cssIncomeInvoiceDetailWrapper.like(CssIncomeInvoiceDetail::getVoucherCreatorName, cssVoucherExport.getVoucherCreatorName());
            }

            if (cssVoucherExport.getVoucherDateStart() != null) {
                cssIncomeInvoiceDetailWrapper.ge(CssIncomeInvoiceDetail::getVoucherDate, cssVoucherExport.getVoucherDateStart());
            }

            if (cssVoucherExport.getVoucherDateEnd() != null) {
                cssIncomeInvoiceDetailWrapper.le(CssIncomeInvoiceDetail::getVoucherDate, cssVoucherExport.getVoucherDateEnd());
            }
            cssIncomeInvoiceDetailWrapper.orderByDesc(CssIncomeInvoiceDetail::getInvoiceDetailId);
            List<CssIncomeInvoiceDetail> cssIncomeInvoiceDetailList = cssIncomeInvoiceDetailService.list(cssIncomeInvoiceDetailWrapper);
            list = cssIncomeInvoiceDetailList.stream().map(cssIncomeInvoiceDetail -> {
                CssVoucherExport voucherExport = new CssVoucherExport();
                BeanUtils.copyProperties(cssIncomeInvoiceDetail, voucherExport);
                voucherExport.setAmountStr(FormatUtils.formatWithQWF(cssIncomeInvoiceDetail.getAmount(), 2) + " (" + cssIncomeInvoiceDetail.getCurrency() + ")");
                return voucherExport;
            }).collect(Collectors.toList());
        } else if (cssVoucherExport.getType() == 1) {
            LambdaQueryWrapper<CssCostInvoiceDetail> cssCostInvoiceDetailWrapper = Wrappers.<CssCostInvoiceDetail>lambdaQuery();
            cssCostInvoiceDetailWrapper.eq(CssCostInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoiceDetail::getBusinessScope, cssVoucherExport.getBusinessScope());
            if (cssVoucherExport.getVoucherStatus() != null) {
                if (cssVoucherExport.getVoucherStatus()) {
                    cssCostInvoiceDetailWrapper.isNotNull(CssCostInvoiceDetail::getVoucherDate);
                } else {
                    cssCostInvoiceDetailWrapper.isNull(CssCostInvoiceDetail::getVoucherDate);
                }
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getInvoiceNum())) {
                cssCostInvoiceDetailWrapper.like(CssCostInvoiceDetail::getInvoiceNum, cssVoucherExport.getInvoiceNum());
            }

            if (cssVoucherExport.getInvoiceDateStart() != null) {
                cssCostInvoiceDetailWrapper.ge(CssCostInvoiceDetail::getInvoiceDate, cssVoucherExport.getInvoiceDateStart());
            }

            if (cssVoucherExport.getInvoiceDateEnd() != null) {
                cssCostInvoiceDetailWrapper.le(CssCostInvoiceDetail::getInvoiceDate, cssVoucherExport.getInvoiceDateEnd());
            }

            if (StrUtil.isNotBlank(cssVoucherExport.getCustomerName())) {
                cssCostInvoiceDetailWrapper.like(CssCostInvoiceDetail::getCustomerName, cssVoucherExport.getCustomerName());
            }

            if (StrUtil.isNotBlank(cssVoucherExport.getVoucherCreatorName())) {
                cssCostInvoiceDetailWrapper.like(CssCostInvoiceDetail::getVoucherCreatorName, cssVoucherExport.getVoucherCreatorName());
            }

            if (cssVoucherExport.getVoucherDateStart() != null) {
                cssCostInvoiceDetailWrapper.ge(CssCostInvoiceDetail::getVoucherDate, cssVoucherExport.getVoucherDateStart());
            }

            if (cssVoucherExport.getVoucherDateEnd() != null) {
                cssCostInvoiceDetailWrapper.le(CssCostInvoiceDetail::getVoucherDate, cssVoucherExport.getVoucherDateEnd());
            }
            cssCostInvoiceDetailWrapper.orderByDesc(CssCostInvoiceDetail::getInvoiceDetailId);
            List<CssCostInvoiceDetail> cssCostInvoiceDetailList = cssCostInvoiceDetailService.list(cssCostInvoiceDetailWrapper);
            list = cssCostInvoiceDetailList.stream().map(cssCostInvoiceDetail -> {
                CssVoucherExport voucherExport = new CssVoucherExport();
                BeanUtils.copyProperties(cssCostInvoiceDetail, voucherExport);
                voucherExport.setAmountStr(FormatUtils.formatWithQWF(cssCostInvoiceDetail.getAmount(), 2) + " (" + cssCostInvoiceDetail.getCurrency() + ")");
                return voucherExport;
            }).collect(Collectors.toList());
        } else if (cssVoucherExport.getType() == 2) {
            LambdaQueryWrapper<CssIncomeInvoiceDetailWriteoff> cssIncomeInvoiceDetailWriteoffWrapper = Wrappers.<CssIncomeInvoiceDetailWriteoff>lambdaQuery();
            cssIncomeInvoiceDetailWriteoffWrapper.eq(CssIncomeInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssIncomeInvoiceDetailWriteoff::getBusinessScope, cssVoucherExport.getBusinessScope());
            if (StrUtil.isNotBlank(cssVoucherExport.getCustomerName())) {
                cssIncomeInvoiceDetailWriteoffWrapper.like(CssIncomeInvoiceDetailWriteoff::getCustomerName, cssVoucherExport.getCustomerName());
            }
            if (cssVoucherExport.getVoucherStatus() != null) {
                if (cssVoucherExport.getVoucherStatus()) {
                    cssIncomeInvoiceDetailWriteoffWrapper.isNotNull(CssIncomeInvoiceDetailWriteoff::getVoucherDate);
                } else {
                    cssIncomeInvoiceDetailWriteoffWrapper.isNull(CssIncomeInvoiceDetailWriteoff::getVoucherDate);
                }
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getWriteoffNum())) {
                cssIncomeInvoiceDetailWriteoffWrapper.like(CssIncomeInvoiceDetailWriteoff::getWriteoffNum, cssVoucherExport.getWriteoffNum());
            }
            if (cssVoucherExport.getWriteoffDateStart() != null) {
                cssIncomeInvoiceDetailWriteoffWrapper.ge(CssIncomeInvoiceDetailWriteoff::getWriteoffDate, cssVoucherExport.getWriteoffDateStart());
            }
            if (cssVoucherExport.getWriteoffDateEnd() != null) {
                cssIncomeInvoiceDetailWriteoffWrapper.le(CssIncomeInvoiceDetailWriteoff::getWriteoffDate, cssVoucherExport.getWriteoffDateEnd());
            }

            if (cssVoucherExport.getVoucherDateStart() != null) {
                cssIncomeInvoiceDetailWriteoffWrapper.ge(CssIncomeInvoiceDetailWriteoff::getVoucherDate, cssVoucherExport.getVoucherDateStart());
            }
            if (cssVoucherExport.getVoucherDateEnd() != null) {
                cssIncomeInvoiceDetailWriteoffWrapper.le(CssIncomeInvoiceDetailWriteoff::getVoucherDate, cssVoucherExport.getVoucherDateEnd());
            }

            if (cssVoucherExport.getInvoiceDateStart() != null || cssVoucherExport.getInvoiceDateEnd() != null || StrUtil.isNotBlank(cssVoucherExport.getInvoiceNum())) {
                LambdaQueryWrapper<CssIncomeInvoiceDetail> cssIncomeInvoiceDetailWrapper = Wrappers.<CssIncomeInvoiceDetail>lambdaQuery();
                cssIncomeInvoiceDetailWrapper.eq(CssIncomeInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssIncomeInvoiceDetail::getBusinessScope, cssVoucherExport.getBusinessScope());
                if (cssVoucherExport.getInvoiceDateStart() != null) {
                    cssIncomeInvoiceDetailWrapper.ge(CssIncomeInvoiceDetail::getInvoiceDate, cssVoucherExport.getInvoiceDateStart());
                }
                if (cssVoucherExport.getInvoiceDateEnd() != null) {
                    cssIncomeInvoiceDetailWrapper.le(CssIncomeInvoiceDetail::getInvoiceDate, cssVoucherExport.getInvoiceDateEnd());
                }
                if (StrUtil.isNotBlank(cssVoucherExport.getInvoiceNum())) {
                    cssIncomeInvoiceDetailWrapper.like(CssIncomeInvoiceDetail::getInvoiceNum, cssVoucherExport.getInvoiceNum());
                }
                List<Integer> invoiceDetailIds = cssIncomeInvoiceDetailService.list(cssIncomeInvoiceDetailWrapper).stream().map(CssIncomeInvoiceDetail::getInvoiceDetailId).collect(Collectors.toList());
                if (invoiceDetailIds.isEmpty()) {
                    return new ArrayList<>();
                }
                cssIncomeInvoiceDetailWriteoffWrapper.in(CssIncomeInvoiceDetailWriteoff::getInvoiceDetailId, invoiceDetailIds);
            }

            if (StrUtil.isNotBlank(cssVoucherExport.getVoucherCreatorName())) {
                cssIncomeInvoiceDetailWriteoffWrapper.like(CssIncomeInvoiceDetailWriteoff::getVoucherCreatorName, cssVoucherExport.getVoucherCreatorName());
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getVoucherNumber())) {
                cssIncomeInvoiceDetailWriteoffWrapper.like(CssIncomeInvoiceDetailWriteoff::getVoucherNumber, cssVoucherExport.getVoucherNumber());
            }
            cssIncomeInvoiceDetailWriteoffWrapper.orderByDesc(CssIncomeInvoiceDetailWriteoff::getInvoiceDetailWriteoffId);
            List<CssIncomeInvoiceDetailWriteoff> cssIncomeInvoiceDetailWriteoffList = cssIncomeInvoiceDetailWriteoffService.list(cssIncomeInvoiceDetailWriteoffWrapper);
            list = cssIncomeInvoiceDetailWriteoffList.stream().map(cssIncomeInvoiceDetailWriteoff -> {
                CssVoucherExport voucherExport = new CssVoucherExport();
                BeanUtils.copyProperties(cssIncomeInvoiceDetailWriteoff, voucherExport);
                voucherExport.setWriteoffAmount(cssIncomeInvoiceDetailWriteoff.getAmountWriteoff());
                voucherExport.setWriteoffAmountStr(FormatUtils.formatWithQWF(voucherExport.getWriteoffAmount(), 2) + " (" + voucherExport.getCurrency() + ")");
                if (cssIncomeInvoiceDetailWriteoff.getVoucherNumber() != null) {
                    voucherExport.setVoucherNumber(cssIncomeInvoiceDetailWriteoff.getVoucherNumber().toString());
                }
                CssIncomeInvoiceDetail cssIncomeInvoiceDetail = cssIncomeInvoiceDetailService.getById(cssIncomeInvoiceDetailWriteoff.getInvoiceDetailId());
                if (cssIncomeInvoiceDetail != null) {
                    voucherExport.setBankFinancialAccount(StrUtil.isBlank(voucherExport.getFinancialAccountName()) ? "" : voucherExport.getFinancialAccountName().split(" ")[0]);
                    voucherExport.setInvoiceTitle(cssIncomeInvoiceDetail.getInvoiceTitle());
                    voucherExport.setInvoiceNum(cssIncomeInvoiceDetail.getInvoiceNum());
                    voucherExport.setInvoiceDate(cssIncomeInvoiceDetail.getInvoiceDate());
                    voucherExport.setInvoiceType(cssIncomeInvoiceDetail.getInvoiceType());
                    voucherExport.setAmount(cssIncomeInvoiceDetail.getAmount());
                    voucherExport.setAmountStr(FormatUtils.formatWithQWF(cssIncomeInvoiceDetail.getAmount(), 2) + " (" + cssIncomeInvoiceDetail.getCurrency() + ")");
                }
                return voucherExport;
            }).collect(Collectors.toList());
        } else if (cssVoucherExport.getType() == 3) {
            LambdaQueryWrapper<CssCostInvoiceDetailWriteoff> cssCostInvoiceDetailWriteoffWrapper = Wrappers.<CssCostInvoiceDetailWriteoff>lambdaQuery();
            cssCostInvoiceDetailWriteoffWrapper.eq(CssCostInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoiceDetailWriteoff::getBusinessScope, cssVoucherExport.getBusinessScope());
            if (StrUtil.isNotBlank(cssVoucherExport.getCustomerName())) {
                cssCostInvoiceDetailWriteoffWrapper.like(CssCostInvoiceDetailWriteoff::getCustomerName, cssVoucherExport.getCustomerName());
            }
            if (cssVoucherExport.getVoucherStatus() != null) {
                if (cssVoucherExport.getVoucherStatus()) {
                    cssCostInvoiceDetailWriteoffWrapper.isNotNull(CssCostInvoiceDetailWriteoff::getVoucherDate);
                } else {
                    cssCostInvoiceDetailWriteoffWrapper.isNull(CssCostInvoiceDetailWriteoff::getVoucherDate);
                }
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getWriteoffNum())) {
                cssCostInvoiceDetailWriteoffWrapper.like(CssCostInvoiceDetailWriteoff::getWriteoffNum, cssVoucherExport.getWriteoffNum());
            }
            if (cssVoucherExport.getWriteoffDateStart() != null) {
                cssCostInvoiceDetailWriteoffWrapper.ge(CssCostInvoiceDetailWriteoff::getWriteoffDate, cssVoucherExport.getWriteoffDateStart());
            }
            if (cssVoucherExport.getWriteoffDateEnd() != null) {
                cssCostInvoiceDetailWriteoffWrapper.le(CssCostInvoiceDetailWriteoff::getWriteoffDate, cssVoucherExport.getWriteoffDateEnd());
            }

            if (cssVoucherExport.getVoucherDateStart() != null) {
                cssCostInvoiceDetailWriteoffWrapper.ge(CssCostInvoiceDetailWriteoff::getVoucherDate, cssVoucherExport.getVoucherDateStart());
            }
            if (cssVoucherExport.getVoucherDateEnd() != null) {
                cssCostInvoiceDetailWriteoffWrapper.le(CssCostInvoiceDetailWriteoff::getVoucherDate, cssVoucherExport.getVoucherDateEnd());
            }

            if (cssVoucherExport.getInvoiceDateStart() != null || cssVoucherExport.getInvoiceDateEnd() != null || StrUtil.isNotBlank(cssVoucherExport.getInvoiceNum())) {
                LambdaQueryWrapper<CssCostInvoiceDetail> cssCostInvoiceDetailWrapper = Wrappers.<CssCostInvoiceDetail>lambdaQuery();
                cssCostInvoiceDetailWrapper.eq(CssCostInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoiceDetail::getBusinessScope, cssVoucherExport.getBusinessScope());
                if (cssVoucherExport.getInvoiceDateStart() != null) {
                    cssCostInvoiceDetailWrapper.ge(CssCostInvoiceDetail::getInvoiceDate, cssVoucherExport.getInvoiceDateStart());
                }
                if (cssVoucherExport.getInvoiceDateEnd() != null) {
                    cssCostInvoiceDetailWrapper.le(CssCostInvoiceDetail::getInvoiceDate, cssVoucherExport.getInvoiceDateEnd());
                }
                if (StrUtil.isNotBlank(cssVoucherExport.getInvoiceNum())) {
                    cssCostInvoiceDetailWrapper.like(CssCostInvoiceDetail::getInvoiceNum, cssVoucherExport.getInvoiceNum());
                }
                List<Integer> invoiceDetailIds = cssCostInvoiceDetailService.list(cssCostInvoiceDetailWrapper).stream().map(CssCostInvoiceDetail::getInvoiceDetailId).collect(Collectors.toList());
                if (invoiceDetailIds.isEmpty()) {
                    return new ArrayList<>();
                }
                cssCostInvoiceDetailWriteoffWrapper.in(CssCostInvoiceDetailWriteoff::getInvoiceDetailId, invoiceDetailIds);
            }

            if (StrUtil.isNotBlank(cssVoucherExport.getVoucherCreatorName())) {
                cssCostInvoiceDetailWriteoffWrapper.like(CssCostInvoiceDetailWriteoff::getVoucherCreatorName, cssVoucherExport.getVoucherCreatorName());
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getVoucherNumber())) {
                cssCostInvoiceDetailWriteoffWrapper.like(CssCostInvoiceDetailWriteoff::getVoucherNumber, cssVoucherExport.getVoucherNumber());
            }
            cssCostInvoiceDetailWriteoffWrapper.orderByDesc(CssCostInvoiceDetailWriteoff::getInvoiceDetailWriteoffId);
            List<CssCostInvoiceDetailWriteoff> cssCostInvoiceDetailWriteoffList = cssCostInvoiceDetailWriteoffService.list(cssCostInvoiceDetailWriteoffWrapper);
            list = cssCostInvoiceDetailWriteoffList.stream().map(cssCostInvoiceDetailWriteoff -> {
                CssVoucherExport voucherExport = new CssVoucherExport();
                BeanUtils.copyProperties(cssCostInvoiceDetailWriteoff, voucherExport);
                voucherExport.setWriteoffAmount(cssCostInvoiceDetailWriteoff.getAmountWriteoff());
                voucherExport.setWriteoffAmountStr(FormatUtils.formatWithQWF(voucherExport.getWriteoffAmount(), 2) + " (" + voucherExport.getCurrency() + ")");
                if (cssCostInvoiceDetailWriteoff.getVoucherNumber() != null) {
                    voucherExport.setVoucherNumber(cssCostInvoiceDetailWriteoff.getVoucherNumber().toString());
                }
                CssCostInvoiceDetail cssCostInvoiceDetail = cssCostInvoiceDetailService.getById(cssCostInvoiceDetailWriteoff.getInvoiceDetailId());
                if (cssCostInvoiceDetail != null) {
                    voucherExport.setBankFinancialAccount(voucherExport.getFinancialAccountName());
                    voucherExport.setInvoiceNum(cssCostInvoiceDetail.getInvoiceNum());
                    voucherExport.setInvoiceDate(cssCostInvoiceDetail.getInvoiceDate());
                    voucherExport.setInvoiceType(cssCostInvoiceDetail.getInvoiceType());
                    voucherExport.setAmount(cssCostInvoiceDetail.getAmount());
                    voucherExport.setAmountStr(FormatUtils.formatWithQWF(cssCostInvoiceDetail.getAmount(), 2) + " (" + cssCostInvoiceDetail.getCurrency() + ")");
                }
                return voucherExport;
            }).collect(Collectors.toList());
        } else if (cssVoucherExport.getType() == 4) {
            list = cssVoucherExportMapper.listExpenseReport(cssVoucherExport);
            list.stream().forEach(voucher -> {
                voucher.setExpenseAmountStr(FormatUtils.formatWithQWF(voucher.getExpenseAmount(), 2));
                voucher.setFinancialAccount((StrUtil.isBlank(voucher.getExpenseFinancialAccountCode()) ? "" : voucher.getExpenseFinancialAccountCode()) + " " + (StrUtil.isBlank(voucher.getExpenseFinancialAccountName()) ? "" : voucher.getExpenseFinancialAccountName()));
                voucher.setBankFinancialAccount((StrUtil.isBlank(voucher.getBankFinancialAccountCode()) ? "" : voucher.getBankFinancialAccountCode()) + " " + (StrUtil.isBlank(voucher.getBankFinancialAccountName()) ? "" : voucher.getBankFinancialAccountName()));
            });
        }
        return list;
    }

    @Override
    public CssVoucherExport total(CssVoucherExport cssVoucherExport) {
        CssVoucherExport total = new CssVoucherExport();
        if (cssVoucherExport.getType() == 0 || cssVoucherExport.getType() == 1) {
            HashMap<String, BigDecimal> sumAmountMap = new HashMap<>();
            getList(cssVoucherExport).stream().forEach(item -> {
                total.setBusinessScope("合计:");
                if (sumAmountMap.get(item.getCurrency()) == null) {
                    sumAmountMap.put(item.getCurrency(), item.getAmount() == null ? BigDecimal.ZERO : item.getAmount());
                } else {
                    sumAmountMap.put(item.getCurrency(), sumAmountMap.get(item.getCurrency()).add(item.getAmount() == null ? BigDecimal.ZERO : item.getAmount()));
                }

            });
            StringBuffer sumAmountBuffer = new StringBuffer();
            for (Map.Entry<String, BigDecimal> entry : sumAmountMap.entrySet()) {
                if (sumAmountBuffer.length() == 0) {
                    sumAmountBuffer.append(FormatUtils.formatWithQWF(entry.getValue(), 2)).append(" (").append(entry.getKey()).append(")");
                } else {
                    sumAmountBuffer.append("  ").append(FormatUtils.formatWithQWF(entry.getValue(), 2)).append(" (").append(entry.getKey()).append(")");
                }
            }
            total.setAmountStr(sumAmountBuffer.toString());
        } else if (cssVoucherExport.getType() == 2 || cssVoucherExport.getType() == 3) {
            HashMap<String, BigDecimal> sumAmountMap = new HashMap<>();
            HashMap<String, BigDecimal> sumMap = new HashMap<>();
            getList(cssVoucherExport).stream().forEach(item -> {
                total.setBusinessScope("合计:");
                if (sumAmountMap.get(item.getCurrency()) == null) {
                    sumAmountMap.put(item.getCurrency(), item.getAmount() == null ? BigDecimal.ZERO : item.getAmount());
                } else {
                    sumAmountMap.put(item.getCurrency(), sumAmountMap.get(item.getCurrency()).add(item.getAmount() == null ? BigDecimal.ZERO : item.getAmount()));
                }
                if (sumMap.get(item.getCurrency()) == null) {
                    sumMap.put(item.getCurrency(), item.getWriteoffAmount() == null ? BigDecimal.ZERO : item.getWriteoffAmount());
                } else {
                    sumMap.put(item.getCurrency(), sumMap.get(item.getCurrency()).add(item.getWriteoffAmount() == null ? BigDecimal.ZERO : item.getWriteoffAmount()));
                }
            });
            StringBuffer sumAmountBuffer = new StringBuffer();
            for (Map.Entry<String, BigDecimal> entry : sumAmountMap.entrySet()) {
                if (sumAmountBuffer.length() == 0) {
                    sumAmountBuffer.append(FormatUtils.formatWithQWF(entry.getValue(), 2)).append(" (").append(entry.getKey()).append(")");
                } else {
                    sumAmountBuffer.append("  ").append(FormatUtils.formatWithQWF(entry.getValue(), 2)).append(" (").append(entry.getKey()).append(")");
                }
            }
            StringBuffer sumBuffer = new StringBuffer();
            for (Map.Entry<String, BigDecimal> entry : sumMap.entrySet()) {
                if (sumBuffer.length() == 0) {
                    sumBuffer.append(FormatUtils.formatWithQWF(entry.getValue(), 2)).append(" (").append(entry.getKey()).append(")");
                } else {
                    sumBuffer.append("  ").append(FormatUtils.formatWithQWF(entry.getValue(), 2)).append(" (").append(entry.getKey()).append(")");
                }
            }
            total.setAmountStr(sumAmountBuffer.toString());
            total.setWriteoffAmountStr(sumBuffer.toString());
        } else if (cssVoucherExport.getType() == 4) {
            getList(cssVoucherExport).stream().forEach(order -> {
                total.setBusinessScope("合计:");
                if (total.getExpenseAmount() == null) {
                    total.setExpenseAmount(order.getExpenseAmount() == null ? BigDecimal.ZERO : order.getExpenseAmount());
                } else {
                    total.setExpenseAmount(total.getExpenseAmount().add(order.getExpenseAmount() == null ? BigDecimal.ZERO : order.getExpenseAmount()));
                }
            });
            total.setExpenseAmountStr(total.getExpenseAmount() == null ? "" : FormatUtils.formatWithQWFNoBit(total.getExpenseAmount()));
        }

        if (StrUtil.isBlank(total.getBusinessScope())) {
            return null;
        }

        return total;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voucherCallback(CssVoucherExport cssVoucherExport) {
        System.out.println("------------------------退回凭证测试时间开始-------------------------");
        long start = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
        if (cssVoucherExport.getType() == 0) {
            //收入挂账

            //校验
            cssVoucherExport.getCheckedList().stream().forEach(item -> {
                CssIncomeInvoiceDetail cssIncomeInvoiceDetail = cssIncomeInvoiceDetailService.getById(item.getInvoiceDetailId());
                if (cssIncomeInvoiceDetail == null) {
                    throw new RuntimeException(item.getInvoiceNum() + " 发票不存在,请刷新重试");
                }
                if (!cssIncomeInvoiceDetail.getRowUuid().equals(item.getRowUuid())) {
                    throw new RuntimeException(cssIncomeInvoiceDetail.getInvoiceNum() + " 发票数据已变更,请刷新重试");
                }
            });
            cssVoucherExport.getCheckedList().stream().forEach(item -> {
                //回退
                LambdaQueryWrapper<CssIncomeInvoiceDetail> cssIncomeInvoiceDetailWrapper = Wrappers.<CssIncomeInvoiceDetail>lambdaQuery();
                cssIncomeInvoiceDetailWrapper.eq(CssIncomeInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssIncomeInvoiceDetail::getVoucherDate, item.getVoucherDate()).eq(CssIncomeInvoiceDetail::getVoucherNumber, item.getVoucherNumber());
                cssIncomeInvoiceDetailService.list(cssIncomeInvoiceDetailWrapper).stream().forEach(detail -> {
                    detail.setRowUuid(UUID.randomUUID().toString());
                    detail.setVoucherNumber(null);
                    detail.setVoucherDate(null);
                    detail.setVoucherCreatorName(null);
                    detail.setVoucherCreatorId(null);
                    detail.setVoucherCreateTime(null);
                    cssIncomeInvoiceDetailService.updateById(detail);
                });
                //更新凭证日志表
                LambdaQueryWrapper<CssFinancialVoucherNumberLog> cssFinancialVoucherNumberLogWrapper = Wrappers.<CssFinancialVoucherNumberLog>lambdaQuery();
                cssFinancialVoucherNumberLogWrapper.eq(CssFinancialVoucherNumberLog::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssFinancialVoucherNumberLog::getVoucherNumber, Integer.parseInt(item.getVoucherNumber())).isNull(CssFinancialVoucherNumberLog::getReturnVoucherCreatorId).last(" and DATE_FORMAT(voucher_date,'%Y-%m')='" + item.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM")) + "'");
                CssFinancialVoucherNumberLog financialVoucherNumberLog = cssFinancialVoucherNumberLogService.getOne(cssFinancialVoucherNumberLogWrapper);
                if (financialVoucherNumberLog != null) {
                    financialVoucherNumberLog.setReturnVoucherCreateTime(LocalDateTime.now());
                    financialVoucherNumberLog.setReturnVoucherCreatorId(SecurityUtils.getUser().getId());
                    financialVoucherNumberLog.setReturnVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    cssFinancialVoucherNumberLogService.updateById(financialVoucherNumberLog);
                }
            });
            System.out.println("总耗时：" + (LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")) - start) + "s");
        } else if (cssVoucherExport.getType() == 1) {
            //成本挂账

            //校验
            cssVoucherExport.getCheckedList().stream().forEach(item -> {
                CssCostInvoiceDetail cssCostInvoiceDetail = cssCostInvoiceDetailService.getById(item.getInvoiceDetailId());
                if (cssCostInvoiceDetail == null) {
                    throw new RuntimeException(item.getInvoiceNum() + " 发票不存在,请刷新重试");
                }
                if (!cssCostInvoiceDetail.getRowUuid().equals(item.getRowUuid())) {
                    throw new RuntimeException(cssCostInvoiceDetail.getInvoiceNum() + " 发票数据已变更,请刷新重试");
                }
            });
            cssVoucherExport.getCheckedList().stream().forEach(item -> {
                //回退
                LambdaQueryWrapper<CssCostInvoiceDetail> cssCostInvoiceDetailWrapper = Wrappers.<CssCostInvoiceDetail>lambdaQuery();
                cssCostInvoiceDetailWrapper.eq(CssCostInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoiceDetail::getVoucherDate, item.getVoucherDate()).eq(CssCostInvoiceDetail::getVoucherNumber, item.getVoucherNumber());
                cssCostInvoiceDetailService.list(cssCostInvoiceDetailWrapper).stream().forEach(detail -> {
                    detail.setRowUuid(UUID.randomUUID().toString());
                    detail.setVoucherNumber(null);
                    detail.setVoucherDate(null);
                    detail.setVoucherCreatorName(null);
                    detail.setVoucherCreatorId(null);
                    detail.setVoucherCreateTime(null);
                    cssCostInvoiceDetailService.updateById(detail);
                });
                //更新凭证日志表
                LambdaQueryWrapper<CssFinancialVoucherNumberLog> cssFinancialVoucherNumberLogWrapper = Wrappers.<CssFinancialVoucherNumberLog>lambdaQuery();
                cssFinancialVoucherNumberLogWrapper.eq(CssFinancialVoucherNumberLog::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssFinancialVoucherNumberLog::getVoucherNumber, Integer.parseInt(item.getVoucherNumber())).isNull(CssFinancialVoucherNumberLog::getReturnVoucherCreatorId).last(" and DATE_FORMAT(voucher_date,'%Y-%m')='" + item.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM")) + "'");
                CssFinancialVoucherNumberLog financialVoucherNumberLog = cssFinancialVoucherNumberLogService.getOne(cssFinancialVoucherNumberLogWrapper);
                if (financialVoucherNumberLog != null) {
                    financialVoucherNumberLog.setReturnVoucherCreateTime(LocalDateTime.now());
                    financialVoucherNumberLog.setReturnVoucherCreatorId(SecurityUtils.getUser().getId());
                    financialVoucherNumberLog.setReturnVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    cssFinancialVoucherNumberLogService.updateById(financialVoucherNumberLog);
                }
            });
            System.out.println("总耗时：" + (LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")) - start) + "s");
        } else if (cssVoucherExport.getType() == 2) {
            //收入核销

            //校验
            cssVoucherExport.getCheckedList().stream().forEach(item -> {
                CssIncomeInvoiceDetailWriteoff cssIncomeInvoiceDetailWriteoff = cssIncomeInvoiceDetailWriteoffService.getById(item.getInvoiceDetailWriteoffId());
                if (cssIncomeInvoiceDetailWriteoff == null) {
                    throw new RuntimeException(item.getWriteoffNum() + " 核销单不存在，请刷新重试");
                }
                if (!cssIncomeInvoiceDetailWriteoff.getRowUuid().equals(item.getRowUuid())) {
                    throw new RuntimeException(cssIncomeInvoiceDetailWriteoff.getWriteoffNum() + " 核销单数据已变更，请刷新重试");
                }
            });

            cssVoucherExport.getCheckedList().stream().forEach(item -> {
                //回退
                LambdaQueryWrapper<CssIncomeInvoiceDetailWriteoff> cssIncomeInvoiceDetailWriteoffWrapper = Wrappers.<CssIncomeInvoiceDetailWriteoff>lambdaQuery();
                cssIncomeInvoiceDetailWriteoffWrapper.eq(CssIncomeInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssIncomeInvoiceDetailWriteoff::getVoucherDate, item.getVoucherDate()).eq(CssIncomeInvoiceDetailWriteoff::getVoucherNumber, item.getVoucherNumber());
                cssIncomeInvoiceDetailWriteoffService.list(cssIncomeInvoiceDetailWriteoffWrapper).stream().forEach(detail -> {
                    detail.setRowUuid(UUID.randomUUID().toString());
                    detail.setVoucherNumber(null);
                    detail.setVoucherDate(null);
                    detail.setVoucherCreatorName(null);
                    detail.setVoucherCreatorId(null);
                    detail.setVoucherCreateTime(null);
                    cssIncomeInvoiceDetailWriteoffService.updateById(detail);
                });
                //更新凭证日志表
                LambdaQueryWrapper<CssFinancialVoucherNumberLog> cssFinancialVoucherNumberLogWrapper = Wrappers.<CssFinancialVoucherNumberLog>lambdaQuery();
                cssFinancialVoucherNumberLogWrapper.eq(CssFinancialVoucherNumberLog::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssFinancialVoucherNumberLog::getVoucherNumber, item.getVoucherNumber()).isNull(CssFinancialVoucherNumberLog::getReturnVoucherCreatorId).last(" and DATE_FORMAT(voucher_date,'%Y-%m')='" + item.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM")) + "'");
                CssFinancialVoucherNumberLog financialVoucherNumberLog = cssFinancialVoucherNumberLogService.getOne(cssFinancialVoucherNumberLogWrapper);
                if (financialVoucherNumberLog != null) {
                    financialVoucherNumberLog.setReturnVoucherCreateTime(LocalDateTime.now());
                    financialVoucherNumberLog.setReturnVoucherCreatorId(SecurityUtils.getUser().getId());
                    financialVoucherNumberLog.setReturnVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    cssFinancialVoucherNumberLogService.updateById(financialVoucherNumberLog);
                }
            });
            System.out.println("总耗时：" + (LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")) - start) + "s");
        } else if (cssVoucherExport.getType() == 3) {
            //成本核销

            //校验
            cssVoucherExport.getCheckedList().stream().forEach(item -> {
                CssCostInvoiceDetailWriteoff cssCostInvoiceDetailWriteoff = cssCostInvoiceDetailWriteoffService.getById(item.getInvoiceDetailWriteoffId());
                if (cssCostInvoiceDetailWriteoff == null) {
                    throw new RuntimeException(item.getWriteoffNum() + " 核销单不存在，请刷新重试");
                }
                if (!cssCostInvoiceDetailWriteoff.getRowUuid().equals(item.getRowUuid())) {
                    throw new RuntimeException(cssCostInvoiceDetailWriteoff.getWriteoffNum() + " 核销单数据已变更，请刷新重试");
                }
            });

            cssVoucherExport.getCheckedList().stream().forEach(item -> {
                //回退
                LambdaQueryWrapper<CssCostInvoiceDetailWriteoff> cssCostInvoiceDetailWriteoffWrapper = Wrappers.<CssCostInvoiceDetailWriteoff>lambdaQuery();
                cssCostInvoiceDetailWriteoffWrapper.eq(CssCostInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoiceDetailWriteoff::getVoucherDate, item.getVoucherDate()).eq(CssCostInvoiceDetailWriteoff::getVoucherNumber, item.getVoucherNumber());
                cssCostInvoiceDetailWriteoffService.list(cssCostInvoiceDetailWriteoffWrapper).stream().forEach(detail -> {
                    detail.setRowUuid(UUID.randomUUID().toString());
                    detail.setVoucherNumber(null);
                    detail.setVoucherDate(null);
                    detail.setVoucherCreatorName(null);
                    detail.setVoucherCreatorId(null);
                    detail.setVoucherCreateTime(null);
                    cssCostInvoiceDetailWriteoffService.updateById(detail);
                });
                //更新凭证日志表
                LambdaQueryWrapper<CssFinancialVoucherNumberLog> cssFinancialVoucherNumberLogWrapper = Wrappers.<CssFinancialVoucherNumberLog>lambdaQuery();
                cssFinancialVoucherNumberLogWrapper.eq(CssFinancialVoucherNumberLog::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssFinancialVoucherNumberLog::getVoucherNumber, item.getVoucherNumber()).isNull(CssFinancialVoucherNumberLog::getReturnVoucherCreatorId).last(" and DATE_FORMAT(voucher_date,'%Y-%m')='" + item.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM")) + "'");
                CssFinancialVoucherNumberLog financialVoucherNumberLog = cssFinancialVoucherNumberLogService.getOne(cssFinancialVoucherNumberLogWrapper);
                if (financialVoucherNumberLog != null) {
                    financialVoucherNumberLog.setReturnVoucherCreateTime(LocalDateTime.now());
                    financialVoucherNumberLog.setReturnVoucherCreatorId(SecurityUtils.getUser().getId());
                    financialVoucherNumberLog.setReturnVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    cssFinancialVoucherNumberLogService.updateById(financialVoucherNumberLog);
                }
            });
            System.out.println("总耗时：" + (LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")) - start) + "s");
        } else if (cssVoucherExport.getType() == 4) {
            //费用核销
            cssVoucherExport.setOrgId(SecurityUtils.getUser().getOrgId());
            cssVoucherExport.setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
            cssVoucherExport.setVoucherCreatorId(SecurityUtils.getUser().getId());
            Map<String, String> message = new HashMap<>();
            StringBuffer sqlBuffer = new StringBuffer("INSERT INTO TEMP_expense_report_sql VALUES");
            cssVoucherExport.getCheckedList().stream().forEach(voucher -> {
                sqlBuffer.append("(").append(voucher.getExpenseReportId()).append(",");
                if (voucher.getVoucherDate() != null) {
                    sqlBuffer.append("'" + voucher.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("',");
                } else {
                    sqlBuffer.append("null,");
                }
                if (StrUtil.isNotBlank(voucher.getVoucherNumber())) {
                    sqlBuffer.append(voucher.getVoucherNumber()).append("),");
                } else {
                    sqlBuffer.append("null),");
                }
            });
            String sql = sqlBuffer.replace(sqlBuffer.length() - 1, sqlBuffer.length(), ";").toString();
            System.out.println(sql);
            cssVoucherExport.setSql(sql);
            message = cssVoucherExportMapper.voucherCallbackForExpenseReport(cssVoucherExport);
            if (!message.get("ret_message").contains("成功")) {
                throw new RuntimeException(message.get("ret_message"));
            }
        }
    }

    @Override
    public Integer getMaxVoucherNumber(String voucherDate) {
        Integer maxVoucherNumber = cssVoucherExportMapper.getMaxVoucherNumber(voucherDate, SecurityUtils.getUser().getOrgId());
        if (maxVoucherNumber != null) {
            maxVoucherNumber++;
        }
        return maxVoucherNumber;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voucherGenerate(CssVoucherExport cssVoucherExport) {
        //所选择记录是否做过凭证
        boolean isCreateVoucher = true;
        if (StrUtil.isNotBlank(cssVoucherExport.getCheckedList().get(0).getVoucherNumber())) {
            isCreateVoucher = false;
        }
        if (cssVoucherExport.getType() == 0) {
            //收入挂账

            //校验
            cssVoucherExport.getCheckedList().stream().forEach(item -> {
                CssIncomeInvoiceDetail costInvoiceDetail = cssIncomeInvoiceDetailService.getById(item.getInvoiceDetailId());
                if (costInvoiceDetail == null) {
                    throw new RuntimeException(item.getInvoiceNum() + " 发票已不存在，请刷新页面重试");
                }
                if (!costInvoiceDetail.getRowUuid().equals(item.getRowUuid())) {
                    throw new RuntimeException(item.getInvoiceNum() + " 发票数据已更新，请刷新页面重试");
                }
            });
            List<CssIncomeInvoiceDetail> dataSource = null;
            LambdaQueryWrapper<CssIncomeInvoiceDetail> cssIncomeInvoiceDetailWrapper = Wrappers.<CssIncomeInvoiceDetail>lambdaQuery();
            cssIncomeInvoiceDetailWrapper.eq(CssIncomeInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId());
            if (isCreateVoucher) {
                cssIncomeInvoiceDetailWrapper.in(CssIncomeInvoiceDetail::getInvoiceDetailId, cssVoucherExport.getCheckedList().stream().map(CssVoucherExport::getInvoiceDetailId).collect(Collectors.toList()));
                dataSource = cssIncomeInvoiceDetailService.list(cssIncomeInvoiceDetailWrapper);
            } else {
                cssIncomeInvoiceDetailWrapper.eq(CssIncomeInvoiceDetail::getVoucherNumber, cssVoucherExport.getCheckedList().get(0).getVoucherNumber()).eq(CssIncomeInvoiceDetail::getVoucherDate, cssVoucherExport.getCheckedList().get(0).getVoucherDate());
                dataSource = cssIncomeInvoiceDetailService.list(cssIncomeInvoiceDetailWrapper);
            }

            //校验并保存
            if (cssVoucherExport.getVoucherIsDetail().equals(0)) {
                int count = cssVoucherExportMapper.checkVoucherNumber(cssVoucherExport.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM")), cssVoucherExport.getVoucherNumber(), SecurityUtils.getUser().getOrgId());
                if (count > 0) {
                    throw new RuntimeException(cssVoucherExport.getVoucherNumber() + " 凭证号 已存在，不能生成凭证。");
                }
                dataSource.stream().forEach(item -> {
                    item.setVoucherCreateTime(LocalDateTime.now());
                    item.setVoucherCreatorId(SecurityUtils.getUser().getId());
                    item.setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    item.setVoucherDate(cssVoucherExport.getVoucherDate());
                    item.setVoucherNumber(cssVoucherExport.getVoucherNumber());
                    item.setRowUuid(UUID.randomUUID().toString());
                });
            } else if (cssVoucherExport.getVoucherIsDetail().equals(1)) {
                StringBuilder voucherNumbers = new StringBuilder();
                int voucherNumber = Integer.parseInt(cssVoucherExport.getVoucherNumber());
                for (int i = 0; i < dataSource.size(); i++) {
                    if (voucherNumbers.length() == 0) {
                        voucherNumbers.append(voucherNumber);
                    } else {
                        voucherNumbers.append(",").append(voucherNumber);
                    }
                    dataSource.get(i).setVoucherCreateTime(LocalDateTime.now());
                    dataSource.get(i).setVoucherCreatorId(SecurityUtils.getUser().getId());
                    dataSource.get(i).setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    dataSource.get(i).setVoucherDate(cssVoucherExport.getVoucherDate());
                    dataSource.get(i).setVoucherNumber(voucherNumber + "");
                    dataSource.get(i).setRowUuid(UUID.randomUUID().toString());
                    voucherNumber++;
                }
                int count = cssVoucherExportMapper.checkVoucherNumber(cssVoucherExport.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM")), voucherNumbers.toString(), SecurityUtils.getUser().getOrgId());
                if (count > 0) {
                    throw new RuntimeException(voucherNumbers.toString() + " 凭证号 已存在，不能生成凭证。");
                }
            } else {
                throw new RuntimeException("缺失必要参数");
            }
            cssIncomeInvoiceDetailService.updateBatchById(dataSource);
            //添加日志表
            if (cssVoucherExport.getVoucherIsDetail().equals(0)) {
                CssFinancialVoucherNumberLog cssFinancialVoucherNumberLog = new CssFinancialVoucherNumberLog();
                cssFinancialVoucherNumberLog.setOrgId(SecurityUtils.getUser().getOrgId());
                cssFinancialVoucherNumberLog.setVoucherCreateTime(LocalDateTime.now());
                cssFinancialVoucherNumberLog.setVoucherCreatorId(SecurityUtils.getUser().getId());
                cssFinancialVoucherNumberLog.setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                cssFinancialVoucherNumberLog.setVoucherDate(LocalDateTime.parse(cssVoucherExport.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                cssFinancialVoucherNumberLog.setVoucherFrom("收入挂账");
                cssFinancialVoucherNumberLog.setVoucherNumber(Integer.parseInt(cssVoucherExport.getVoucherNumber()));
                cssFinancialVoucherNumberLogService.save(cssFinancialVoucherNumberLog);
            } else {
                dataSource.stream().forEach(item -> {
                    CssFinancialVoucherNumberLog cssFinancialVoucherNumberLog = new CssFinancialVoucherNumberLog();
                    cssFinancialVoucherNumberLog.setOrgId(SecurityUtils.getUser().getOrgId());
                    cssFinancialVoucherNumberLog.setVoucherCreateTime(LocalDateTime.now());
                    cssFinancialVoucherNumberLog.setVoucherCreatorId(SecurityUtils.getUser().getId());
                    cssFinancialVoucherNumberLog.setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    cssFinancialVoucherNumberLog.setVoucherDate(LocalDateTime.parse(cssVoucherExport.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    cssFinancialVoucherNumberLog.setVoucherFrom("收入挂账");
                    cssFinancialVoucherNumberLog.setVoucherNumber(Integer.parseInt(item.getVoucherNumber()));
                    cssFinancialVoucherNumberLogService.save(cssFinancialVoucherNumberLog);
                });
            }
            //更新凭证日志表
            if (!isCreateVoucher) {
                LambdaQueryWrapper<CssFinancialVoucherNumberLog> cssFinancialVoucherNumberLogWrapper = Wrappers.<CssFinancialVoucherNumberLog>lambdaQuery();
                cssFinancialVoucherNumberLogWrapper.eq(CssFinancialVoucherNumberLog::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssFinancialVoucherNumberLog::getVoucherNumber, Integer.parseInt(cssVoucherExport.getCheckedList().get(0).getVoucherNumber())).isNull(CssFinancialVoucherNumberLog::getReturnVoucherCreatorId).last(" and DATE_FORMAT(voucher_date,'%Y-%m')='" + cssVoucherExport.getCheckedList().get(0).getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM")) + "'");
                CssFinancialVoucherNumberLog financialVoucherNumberLog = cssFinancialVoucherNumberLogService.getOne(cssFinancialVoucherNumberLogWrapper);
                if (financialVoucherNumberLog != null) {
                    financialVoucherNumberLog.setReturnVoucherCreateTime(LocalDateTime.now());
                    financialVoucherNumberLog.setReturnVoucherCreatorId(SecurityUtils.getUser().getId());
                    financialVoucherNumberLog.setReturnVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    cssFinancialVoucherNumberLogService.updateById(financialVoucherNumberLog);
                }
            }
            //导出
            this.exportVoucher(cssVoucherExport.getType(), dataSource.stream().map(CssIncomeInvoiceDetail::getInvoiceDetailId).collect(Collectors.toList()), dataSource.get(0).getBusinessScope());
        } else if (cssVoucherExport.getType() == 1) {
            //成本挂账

            //校验
            cssVoucherExport.getCheckedList().stream().forEach(item -> {
                CssCostInvoiceDetail costInvoiceDetail = cssCostInvoiceDetailService.getById(item.getInvoiceDetailId());
                if (costInvoiceDetail == null) {
                    throw new RuntimeException(item.getInvoiceNum() + " 发票已不存在，请刷新页面重试");
                }
                if (!costInvoiceDetail.getRowUuid().equals(item.getRowUuid())) {
                    throw new RuntimeException(item.getInvoiceNum() + " 发票数据已更新，请刷新页面重试");
                }
            });

            List<CssCostInvoiceDetail> dataSource = null;
            LambdaQueryWrapper<CssCostInvoiceDetail> cssCostInvoiceDetailWrapper = Wrappers.<CssCostInvoiceDetail>lambdaQuery();
            cssCostInvoiceDetailWrapper.eq(CssCostInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId());
            if (isCreateVoucher) {
                cssCostInvoiceDetailWrapper.in(CssCostInvoiceDetail::getInvoiceDetailId, cssVoucherExport.getCheckedList().stream().map(CssVoucherExport::getInvoiceDetailId).collect(Collectors.toList()));
                dataSource = cssCostInvoiceDetailService.list(cssCostInvoiceDetailWrapper);
            } else {
                cssCostInvoiceDetailWrapper.eq(CssCostInvoiceDetail::getVoucherNumber, cssVoucherExport.getCheckedList().get(0).getVoucherNumber()).eq(CssCostInvoiceDetail::getVoucherDate, cssVoucherExport.getCheckedList().get(0).getVoucherDate());
                dataSource = cssCostInvoiceDetailService.list(cssCostInvoiceDetailWrapper);
            }

            //校验并保存
            if (cssVoucherExport.getVoucherIsDetail().equals(0)) {
                int count = cssVoucherExportMapper.checkVoucherNumber(cssVoucherExport.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM")), cssVoucherExport.getVoucherNumber(), SecurityUtils.getUser().getOrgId());
                if (count > 0) {
                    throw new RuntimeException(cssVoucherExport.getVoucherNumber() + " 凭证号 已存在，不能生成凭证。");
                }
                dataSource.stream().forEach(item -> {
                    item.setVoucherCreateTime(LocalDateTime.now());
                    item.setVoucherCreatorId(SecurityUtils.getUser().getId());
                    item.setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    item.setVoucherDate(cssVoucherExport.getVoucherDate());
                    item.setVoucherNumber(cssVoucherExport.getVoucherNumber());
                    item.setRowUuid(UUID.randomUUID().toString());
                });
            } else if (cssVoucherExport.getVoucherIsDetail().equals(1)) {
                StringBuilder voucherNumbers = new StringBuilder();
                int voucherNumber = Integer.parseInt(cssVoucherExport.getVoucherNumber());
                for (int i = 0; i < dataSource.size(); i++) {
                    if (voucherNumbers.length() == 0) {
                        voucherNumbers.append(voucherNumber);
                    } else {
                        voucherNumbers.append(",").append(voucherNumber);
                    }
                    dataSource.get(i).setVoucherCreateTime(LocalDateTime.now());
                    dataSource.get(i).setVoucherCreatorId(SecurityUtils.getUser().getId());
                    dataSource.get(i).setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    dataSource.get(i).setVoucherDate(cssVoucherExport.getVoucherDate());
                    dataSource.get(i).setVoucherNumber(voucherNumber + "");
                    dataSource.get(i).setRowUuid(UUID.randomUUID().toString());
                    voucherNumber++;
                }
                int count = cssVoucherExportMapper.checkVoucherNumber(cssVoucherExport.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM")), voucherNumbers.toString(), SecurityUtils.getUser().getOrgId());
                if (count > 0) {
                    throw new RuntimeException(voucherNumbers.toString() + " 凭证号 已存在，不能生成凭证。");
                }
            } else {
                throw new RuntimeException("缺失必要参数");
            }
            cssCostInvoiceDetailService.updateBatchById(dataSource);
            //添加日志表
            if (cssVoucherExport.getVoucherIsDetail().equals(0)) {
                CssFinancialVoucherNumberLog cssFinancialVoucherNumberLog = new CssFinancialVoucherNumberLog();
                cssFinancialVoucherNumberLog.setOrgId(SecurityUtils.getUser().getOrgId());
                cssFinancialVoucherNumberLog.setVoucherCreateTime(LocalDateTime.now());
                cssFinancialVoucherNumberLog.setVoucherCreatorId(SecurityUtils.getUser().getId());
                cssFinancialVoucherNumberLog.setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                cssFinancialVoucherNumberLog.setVoucherDate(LocalDateTime.parse(cssVoucherExport.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                cssFinancialVoucherNumberLog.setVoucherFrom("成本挂账");
                cssFinancialVoucherNumberLog.setVoucherNumber(Integer.parseInt(cssVoucherExport.getVoucherNumber()));
                cssFinancialVoucherNumberLogService.save(cssFinancialVoucherNumberLog);
            } else {
                dataSource.stream().forEach(item -> {
                    CssFinancialVoucherNumberLog cssFinancialVoucherNumberLog = new CssFinancialVoucherNumberLog();
                    cssFinancialVoucherNumberLog.setOrgId(SecurityUtils.getUser().getOrgId());
                    cssFinancialVoucherNumberLog.setVoucherCreateTime(LocalDateTime.now());
                    cssFinancialVoucherNumberLog.setVoucherCreatorId(SecurityUtils.getUser().getId());
                    cssFinancialVoucherNumberLog.setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    cssFinancialVoucherNumberLog.setVoucherDate(LocalDateTime.parse(cssVoucherExport.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    cssFinancialVoucherNumberLog.setVoucherFrom("成本挂账");
                    cssFinancialVoucherNumberLog.setVoucherNumber(Integer.parseInt(item.getVoucherNumber()));
                    cssFinancialVoucherNumberLogService.save(cssFinancialVoucherNumberLog);
                });
            }
            //更新凭证日志表
            if (!isCreateVoucher) {
                LambdaQueryWrapper<CssFinancialVoucherNumberLog> cssFinancialVoucherNumberLogWrapper = Wrappers.<CssFinancialVoucherNumberLog>lambdaQuery();
                cssFinancialVoucherNumberLogWrapper.eq(CssFinancialVoucherNumberLog::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssFinancialVoucherNumberLog::getVoucherNumber, Integer.parseInt(cssVoucherExport.getCheckedList().get(0).getVoucherNumber())).isNull(CssFinancialVoucherNumberLog::getReturnVoucherCreatorId).last(" and DATE_FORMAT(voucher_date,'%Y-%m')='" + cssVoucherExport.getCheckedList().get(0).getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM")) + "'");
                CssFinancialVoucherNumberLog financialVoucherNumberLog = cssFinancialVoucherNumberLogService.getOne(cssFinancialVoucherNumberLogWrapper);
                if (financialVoucherNumberLog != null) {
                    financialVoucherNumberLog.setReturnVoucherCreateTime(LocalDateTime.now());
                    financialVoucherNumberLog.setReturnVoucherCreatorId(SecurityUtils.getUser().getId());
                    financialVoucherNumberLog.setReturnVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    cssFinancialVoucherNumberLogService.updateById(financialVoucherNumberLog);
                }
            }
            //导出
            this.exportVoucher(cssVoucherExport.getType(), dataSource.stream().map(CssCostInvoiceDetail::getInvoiceDetailId).collect(Collectors.toList()), dataSource.get(0).getBusinessScope());
        } else if (cssVoucherExport.getType() == 2) {
            //收入核销

            //校验
            cssVoucherExport.getCheckedList().stream().forEach(item -> {
                CssIncomeInvoiceDetailWriteoff costInvoiceDetailWriteoff = cssIncomeInvoiceDetailWriteoffService.getById(item.getInvoiceDetailWriteoffId());
                if (costInvoiceDetailWriteoff == null) {
                    throw new RuntimeException(item.getWriteoffNum() + " 核销单已不存在，请刷新页面重试");
                }
                if (!costInvoiceDetailWriteoff.getRowUuid().equals(item.getRowUuid())) {
                    throw new RuntimeException(item.getWriteoffNum() + " 核销单数据已更新，请刷新页面重试");
                }
            });

            List<CssIncomeInvoiceDetailWriteoff> dataSource = null;
            LambdaQueryWrapper<CssIncomeInvoiceDetailWriteoff> cssIncomeInvoiceDetailWriteoffWrapper = Wrappers.<CssIncomeInvoiceDetailWriteoff>lambdaQuery();
            cssIncomeInvoiceDetailWriteoffWrapper.eq(CssIncomeInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId());
            if (isCreateVoucher) {
                cssIncomeInvoiceDetailWriteoffWrapper.in(CssIncomeInvoiceDetailWriteoff::getInvoiceDetailWriteoffId, cssVoucherExport.getCheckedList().stream().map(CssVoucherExport::getInvoiceDetailWriteoffId).collect(Collectors.toList()));
                dataSource = cssIncomeInvoiceDetailWriteoffService.list(cssIncomeInvoiceDetailWriteoffWrapper);
            } else {
                cssIncomeInvoiceDetailWriteoffWrapper.eq(CssIncomeInvoiceDetailWriteoff::getVoucherNumber, cssVoucherExport.getCheckedList().get(0).getVoucherNumber()).eq(CssIncomeInvoiceDetailWriteoff::getVoucherDate, cssVoucherExport.getCheckedList().get(0).getVoucherDate());
                dataSource = cssIncomeInvoiceDetailWriteoffService.list(cssIncomeInvoiceDetailWriteoffWrapper);
            }

            //校验并保存
            if (cssVoucherExport.getVoucherIsDetail().equals(0)) {
                int count = cssVoucherExportMapper.checkVoucherNumber(cssVoucherExport.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM")), cssVoucherExport.getVoucherNumber(), SecurityUtils.getUser().getOrgId());
                if (count > 0) {
                    throw new RuntimeException(cssVoucherExport.getVoucherNumber() + " 凭证号 已存在，不能生成凭证。");
                }
                dataSource.stream().forEach(item -> {
                    item.setVoucherCreateTime(LocalDateTime.now());
                    item.setVoucherCreatorId(SecurityUtils.getUser().getId());
                    item.setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    item.setVoucherDate(cssVoucherExport.getVoucherDate());
                    item.setVoucherNumber(Integer.parseInt(cssVoucherExport.getVoucherNumber()));
                    item.setRowUuid(UUID.randomUUID().toString());
                });
            } else if (cssVoucherExport.getVoucherIsDetail().equals(1)) {
                StringBuilder voucherNumbers = new StringBuilder();
                int voucherNumber = Integer.parseInt(cssVoucherExport.getVoucherNumber());
                for (int i = 0; i < dataSource.size(); i++) {
                    if (voucherNumbers.length() == 0) {
                        voucherNumbers.append(voucherNumber);
                    } else {
                        voucherNumbers.append(",").append(voucherNumber);
                    }
                    dataSource.get(i).setVoucherCreateTime(LocalDateTime.now());
                    dataSource.get(i).setVoucherCreatorId(SecurityUtils.getUser().getId());
                    dataSource.get(i).setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    dataSource.get(i).setVoucherDate(cssVoucherExport.getVoucherDate());
                    dataSource.get(i).setVoucherNumber(voucherNumber);
                    dataSource.get(i).setRowUuid(UUID.randomUUID().toString());
                    voucherNumber++;
                }
                int count = cssVoucherExportMapper.checkVoucherNumber(cssVoucherExport.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM")), voucherNumbers.toString(), SecurityUtils.getUser().getOrgId());
                if (count > 0) {
                    throw new RuntimeException(voucherNumbers.toString() + " 凭证号 已存在，不能生成凭证。");
                }
            } else {
                throw new RuntimeException("缺失必要参数");
            }
            cssIncomeInvoiceDetailWriteoffService.updateBatchById(dataSource);
            //添加日志表
            if (cssVoucherExport.getVoucherIsDetail().equals(0)) {
                CssFinancialVoucherNumberLog cssFinancialVoucherNumberLog = new CssFinancialVoucherNumberLog();
                cssFinancialVoucherNumberLog.setOrgId(SecurityUtils.getUser().getOrgId());
                cssFinancialVoucherNumberLog.setVoucherCreateTime(LocalDateTime.now());
                cssFinancialVoucherNumberLog.setVoucherCreatorId(SecurityUtils.getUser().getId());
                cssFinancialVoucherNumberLog.setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                cssFinancialVoucherNumberLog.setVoucherDate(LocalDateTime.parse(cssVoucherExport.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                cssFinancialVoucherNumberLog.setVoucherFrom("收入核销");
                cssFinancialVoucherNumberLog.setVoucherNumber(Integer.parseInt(cssVoucherExport.getVoucherNumber()));
                cssFinancialVoucherNumberLogService.save(cssFinancialVoucherNumberLog);
            } else {
                dataSource.stream().forEach(item -> {
                    CssFinancialVoucherNumberLog cssFinancialVoucherNumberLog = new CssFinancialVoucherNumberLog();
                    cssFinancialVoucherNumberLog.setOrgId(SecurityUtils.getUser().getOrgId());
                    cssFinancialVoucherNumberLog.setVoucherCreateTime(LocalDateTime.now());
                    cssFinancialVoucherNumberLog.setVoucherCreatorId(SecurityUtils.getUser().getId());
                    cssFinancialVoucherNumberLog.setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    cssFinancialVoucherNumberLog.setVoucherDate(LocalDateTime.parse(cssVoucherExport.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    cssFinancialVoucherNumberLog.setVoucherFrom("收入核销");
                    cssFinancialVoucherNumberLog.setVoucherNumber(item.getVoucherNumber());
                    cssFinancialVoucherNumberLogService.save(cssFinancialVoucherNumberLog);
                });
            }

            //更新凭证日志表
            if (!isCreateVoucher) {
                LambdaQueryWrapper<CssFinancialVoucherNumberLog> cssFinancialVoucherNumberLogWrapper = Wrappers.<CssFinancialVoucherNumberLog>lambdaQuery();
                cssFinancialVoucherNumberLogWrapper.eq(CssFinancialVoucherNumberLog::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssFinancialVoucherNumberLog::getVoucherNumber, Integer.parseInt(cssVoucherExport.getCheckedList().get(0).getVoucherNumber())).isNull(CssFinancialVoucherNumberLog::getReturnVoucherCreatorId).last(" and DATE_FORMAT(voucher_date,'%Y-%m')='" + cssVoucherExport.getCheckedList().get(0).getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM")) + "'");
                CssFinancialVoucherNumberLog financialVoucherNumberLog = cssFinancialVoucherNumberLogService.getOne(cssFinancialVoucherNumberLogWrapper);
                if (financialVoucherNumberLog != null) {
                    financialVoucherNumberLog.setReturnVoucherCreateTime(LocalDateTime.now());
                    financialVoucherNumberLog.setReturnVoucherCreatorId(SecurityUtils.getUser().getId());
                    financialVoucherNumberLog.setReturnVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    cssFinancialVoucherNumberLogService.updateById(financialVoucherNumberLog);
                }
            }
            //导出
            this.exportVoucher(cssVoucherExport.getType(), dataSource.stream().map(CssIncomeInvoiceDetailWriteoff::getInvoiceDetailWriteoffId).collect(Collectors.toList()), dataSource.get(0).getBusinessScope());
        } else if (cssVoucherExport.getType() == 3) {
            //成本核销

            //校验
            cssVoucherExport.getCheckedList().stream().forEach(item -> {
                CssCostInvoiceDetailWriteoff costInvoiceDetailWriteoff = cssCostInvoiceDetailWriteoffService.getById(item.getInvoiceDetailWriteoffId());
                if (costInvoiceDetailWriteoff == null) {
                    throw new RuntimeException(item.getWriteoffNum() + " 核销单已不存在，请刷新页面重试");
                }
                if (!costInvoiceDetailWriteoff.getRowUuid().equals(item.getRowUuid())) {
                    throw new RuntimeException(item.getWriteoffNum() + " 核销单数据已更新，请刷新页面重试");
                }
            });

            List<CssCostInvoiceDetailWriteoff> dataSource = null;
            LambdaQueryWrapper<CssCostInvoiceDetailWriteoff> cssCostInvoiceDetailWriteoffWrapper = Wrappers.<CssCostInvoiceDetailWriteoff>lambdaQuery();
            cssCostInvoiceDetailWriteoffWrapper.eq(CssCostInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId());
            if (isCreateVoucher) {
                cssCostInvoiceDetailWriteoffWrapper.in(CssCostInvoiceDetailWriteoff::getInvoiceDetailWriteoffId, cssVoucherExport.getCheckedList().stream().map(CssVoucherExport::getInvoiceDetailWriteoffId).collect(Collectors.toList()));
                dataSource = cssCostInvoiceDetailWriteoffService.list(cssCostInvoiceDetailWriteoffWrapper);
            } else {
                cssCostInvoiceDetailWriteoffWrapper.eq(CssCostInvoiceDetailWriteoff::getVoucherNumber, cssVoucherExport.getCheckedList().get(0).getVoucherNumber()).eq(CssCostInvoiceDetailWriteoff::getVoucherDate, cssVoucherExport.getCheckedList().get(0).getVoucherDate());
                dataSource = cssCostInvoiceDetailWriteoffService.list(cssCostInvoiceDetailWriteoffWrapper);
            }

            //校验并保存
            if (cssVoucherExport.getVoucherIsDetail().equals(0)) {
                int count = cssVoucherExportMapper.checkVoucherNumber(cssVoucherExport.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM")), cssVoucherExport.getVoucherNumber(), SecurityUtils.getUser().getOrgId());
                if (count > 0) {
                    throw new RuntimeException(cssVoucherExport.getVoucherNumber() + " 凭证号 已存在，不能生成凭证。");
                }
                dataSource.stream().forEach(item -> {
                    item.setVoucherCreateTime(LocalDateTime.now());
                    item.setVoucherCreatorId(SecurityUtils.getUser().getId());
                    item.setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    item.setVoucherDate(cssVoucherExport.getVoucherDate());
                    item.setVoucherNumber(Integer.parseInt(cssVoucherExport.getVoucherNumber()));
                    item.setRowUuid(UUID.randomUUID().toString());
                });
            } else if (cssVoucherExport.getVoucherIsDetail().equals(1)) {
                StringBuilder voucherNumbers = new StringBuilder();
                int voucherNumber = Integer.parseInt(cssVoucherExport.getVoucherNumber());
                for (int i = 0; i < dataSource.size(); i++) {
                    if (voucherNumbers.length() == 0) {
                        voucherNumbers.append(voucherNumber);
                    } else {
                        voucherNumbers.append(",").append(voucherNumber);
                    }
                    dataSource.get(i).setVoucherCreateTime(LocalDateTime.now());
                    dataSource.get(i).setVoucherCreatorId(SecurityUtils.getUser().getId());
                    dataSource.get(i).setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    dataSource.get(i).setVoucherDate(cssVoucherExport.getVoucherDate());
                    dataSource.get(i).setVoucherNumber(voucherNumber);
                    dataSource.get(i).setRowUuid(UUID.randomUUID().toString());
                    voucherNumber++;
                }
                int count = cssVoucherExportMapper.checkVoucherNumber(cssVoucherExport.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM")), voucherNumbers.toString(), SecurityUtils.getUser().getOrgId());
                if (count > 0) {
                    throw new RuntimeException(voucherNumbers.toString() + " 凭证号 已存在，不能生成凭证。");
                }
            } else {
                throw new RuntimeException("缺失必要参数");
            }
            cssCostInvoiceDetailWriteoffService.updateBatchById(dataSource);
            //添加日志表
            if (cssVoucherExport.getVoucherIsDetail().equals(0)) {
                CssFinancialVoucherNumberLog cssFinancialVoucherNumberLog = new CssFinancialVoucherNumberLog();
                cssFinancialVoucherNumberLog.setOrgId(SecurityUtils.getUser().getOrgId());
                cssFinancialVoucherNumberLog.setVoucherCreateTime(LocalDateTime.now());
                cssFinancialVoucherNumberLog.setVoucherCreatorId(SecurityUtils.getUser().getId());
                cssFinancialVoucherNumberLog.setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                cssFinancialVoucherNumberLog.setVoucherDate(LocalDateTime.parse(cssVoucherExport.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                cssFinancialVoucherNumberLog.setVoucherFrom("成本核销");
                cssFinancialVoucherNumberLog.setVoucherNumber(Integer.parseInt(cssVoucherExport.getVoucherNumber()));
                cssFinancialVoucherNumberLogService.save(cssFinancialVoucherNumberLog);
            } else {
                dataSource.stream().forEach(item -> {
                    CssFinancialVoucherNumberLog cssFinancialVoucherNumberLog = new CssFinancialVoucherNumberLog();
                    cssFinancialVoucherNumberLog.setOrgId(SecurityUtils.getUser().getOrgId());
                    cssFinancialVoucherNumberLog.setVoucherCreateTime(LocalDateTime.now());
                    cssFinancialVoucherNumberLog.setVoucherCreatorId(SecurityUtils.getUser().getId());
                    cssFinancialVoucherNumberLog.setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    cssFinancialVoucherNumberLog.setVoucherDate(LocalDateTime.parse(cssVoucherExport.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    cssFinancialVoucherNumberLog.setVoucherFrom("成本核销");
                    cssFinancialVoucherNumberLog.setVoucherNumber(item.getVoucherNumber());
                    cssFinancialVoucherNumberLogService.save(cssFinancialVoucherNumberLog);
                });
            }
            //更新凭证日志表
            if (!isCreateVoucher) {
                LambdaQueryWrapper<CssFinancialVoucherNumberLog> cssFinancialVoucherNumberLogWrapper = Wrappers.<CssFinancialVoucherNumberLog>lambdaQuery();
                cssFinancialVoucherNumberLogWrapper.eq(CssFinancialVoucherNumberLog::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssFinancialVoucherNumberLog::getVoucherNumber, Integer.parseInt(cssVoucherExport.getCheckedList().get(0).getVoucherNumber())).isNull(CssFinancialVoucherNumberLog::getReturnVoucherCreatorId).last(" and DATE_FORMAT(voucher_date,'%Y-%m')='" + cssVoucherExport.getCheckedList().get(0).getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM")) + "'");
                CssFinancialVoucherNumberLog financialVoucherNumberLog = cssFinancialVoucherNumberLogService.getOne(cssFinancialVoucherNumberLogWrapper);
                if (financialVoucherNumberLog != null) {
                    financialVoucherNumberLog.setReturnVoucherCreateTime(LocalDateTime.now());
                    financialVoucherNumberLog.setReturnVoucherCreatorId(SecurityUtils.getUser().getId());
                    financialVoucherNumberLog.setReturnVoucherCreatorName(SecurityUtils.getUser().buildOptName());
                    cssFinancialVoucherNumberLogService.updateById(financialVoucherNumberLog);
                }
            }
            //导出
            this.exportVoucher(cssVoucherExport.getType(), dataSource.stream().map(CssCostInvoiceDetailWriteoff::getInvoiceDetailWriteoffId).collect(Collectors.toList()), dataSource.get(0).getBusinessScope());
        } else if (cssVoucherExport.getType() == 4) {
            //费用核销
            cssVoucherExport.setOrgId(SecurityUtils.getUser().getOrgId());
            cssVoucherExport.setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
            cssVoucherExport.setVoucherCreatorId(SecurityUtils.getUser().getId());
            StringBuffer sqlBuffer = new StringBuffer("INSERT INTO TEMP_expense_report_sql VALUES");
            cssVoucherExport.getCheckedList().stream().forEach(voucher -> {
                sqlBuffer.append("(").append(voucher.getExpenseReportId()).append(",");
                if (voucher.getVoucherDate() != null) {
                    sqlBuffer.append("'" + voucher.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("',");
                } else {
                    sqlBuffer.append("null,");
                }
                if (StrUtil.isNotBlank(voucher.getVoucherNumber())) {
                    sqlBuffer.append(voucher.getVoucherNumber()).append("),");
                } else {
                    sqlBuffer.append("null),");
                }
            });
            String sql = sqlBuffer.replace(sqlBuffer.length() - 1, sqlBuffer.length(), ";").toString();
            System.out.println(sql);
            cssVoucherExport.setSql(sql);
            List list = cssVoucherExportMapper.voucherGenerateForExpenseReport(cssVoucherExport);
            if (list.size() == 1) {
                Map<String, String> errorMess = (HashMap<String, String>) list.get(0);
                System.out.println(errorMess.get("ret_message"));
                throw new RuntimeException(errorMess.get("ret_message"));
            }
            List<Map<String, Object>> result = (ArrayList<Map<String, Object>>) list.get(0);
            OrgVo orgVo = remoteServiceToHRS.getByOrgId(SecurityUtils.getUser().getOrgId()).getData();
            result.stream().forEach(map -> {
                if (orgVo != null && StrUtil.isNotBlank(orgVo.getFinancialVoucherOutCurrency()) && "CNY".equals(map.get("currency"))) {
                    map.put("currency", orgVo.getFinancialVoucherOutCurrency());
                }
            });
            HashMap<String, Object> map = new HashMap<>();
            map.put("data", result);
            if ("金蝶".equals(((ArrayList<Map<String, Object>>) list.get(1)).get(0).get("voucher_out_type"))) {
                JxlsUtils.exportExcelWithLocalModel(FilePathUtils.filePath + "/PDFtemplate/voucher_export_template_kingdee.xlsx", map);
            } else if ("用友".equals(((ArrayList<Map<String, Object>>) list.get(1)).get(0).get("voucher_out_type"))) {
                JxlsUtils.exportExcelWithLocalModel(FilePathUtils.filePath + "/PDFtemplate/voucher_export_template_yonyou.xlsx", map);
            }
        }
    }

    private void exportVoucher(Integer type, List<Integer> ids, String businessScope) {
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();
        String templatePath = "";
        System.out.println("------------------------生成凭证测试时间开始-------------------------");
        long start = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
        OrgVo org = remoteServiceToHRS.getByOrgId(SecurityUtils.getUser().getOrgId()).getData();
        if (type == 0) {
            LambdaQueryWrapper<CssFinancialAccount> cssFinancialAccountWrapperJ = Wrappers.<CssFinancialAccount>lambdaQuery();
            cssFinancialAccountWrapperJ.eq(CssFinancialAccount::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssFinancialAccount::getBusinessScope, businessScope).eq(CssFinancialAccount::getFinancialAccountType, "应收账款");
            CssFinancialAccount financialAccountJ = cssFinancialAccountService.getOne(cssFinancialAccountWrapperJ);

            LambdaQueryWrapper<CssFinancialAccount> cssFinancialAccountWrapperD = Wrappers.<CssFinancialAccount>lambdaQuery();
            cssFinancialAccountWrapperD.eq(CssFinancialAccount::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssFinancialAccount::getBusinessScope, businessScope).eq(CssFinancialAccount::getFinancialAccountType, "主营业务收入");
            CssFinancialAccount financialAccountD = cssFinancialAccountService.getOne(cssFinancialAccountWrapperD);

            LambdaQueryWrapper<CssIncomeInvoiceDetail> cssIncomeInvoiceDetailWrapper = Wrappers.<CssIncomeInvoiceDetail>lambdaQuery();
            cssIncomeInvoiceDetailWrapper.eq(CssIncomeInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).in(CssIncomeInvoiceDetail::getInvoiceDetailId, ids).orderByAsc(CssIncomeInvoiceDetail::getVoucherNumber, CssIncomeInvoiceDetail::getInvoiceDetailId);
            List<CssIncomeInvoiceDetail> cssIncomeInvoiceDetails = cssIncomeInvoiceDetailService.list(cssIncomeInvoiceDetailWrapper);
            cssIncomeInvoiceDetails.stream().forEach(item -> {
                //-----------------借方--------------------
                HashMap<String, Object> columnJ = new HashMap<>();
                columnJ.put("voucherDate", item.getVoucherDate());
                columnJ.put("voucherZ", "记");
                columnJ.put("voucherNumber", item.getVoucherNumber());
                columnJ.put("attachNumber", 0);
                columnJ.put("serial", 1);
                columnJ.put("summary", item.getCustomerName() + " / " + item.getInvoiceNum());
                CoopVo coopVo = null;
                boolean queryCoopAlready = false;
                if (financialAccountJ != null) {
                    if ("子科目".equals(financialAccountJ.getManageMode())) {
                        coopVo = remoteCoopService.viewCoop(item.getCustomerId().toString()).getData();
                        queryCoopAlready = true;
                        if (coopVo != null) {
                            columnJ.put("financialAccountCode", financialAccountJ.getFinancialAccountCode() + " " + (StrUtil.isNotBlank(coopVo.getFinancialCode()) ? coopVo.getFinancialCode() : coopVo.getCoop_code()));
                        } else {
                            columnJ.put("financialAccountCode", financialAccountJ.getFinancialAccountCode());
                        }
                    } else {
                        columnJ.put("financialAccountCode", financialAccountJ.getFinancialAccountCode());
                    }

                    if ("往来单位".equals(financialAccountJ.getSubsidiaryAccount())) {
                        if (!queryCoopAlready) {
                            coopVo = remoteCoopService.viewCoop(item.getCustomerId().toString()).getData();
                            queryCoopAlready = true;
                        }
                        if (coopVo != null) {
                            columnJ.put("customer", StrUtil.isNotBlank(coopVo.getFinancialCode()) ? coopVo.getFinancialCode() : coopVo.getCoop_code());
                        }
                    }
                }
                columnJ.put("financialAccountName", "应收账款");
                columnJ.put("amountJ", item.getAmount());
                columnJ.put("amountD", null);
                columnJ.put("functionAmount", item.getAmount());
                if (org != null && StrUtil.isNotBlank(org.getFinancialVoucherOutCurrency()) && "CNY".equals(item.getCurrency())) {
                    columnJ.put("currency", org.getFinancialVoucherOutCurrency());
                } else {
                    columnJ.put("currency", item.getCurrency());
                }
                if (item.getDebitNoteId() != null) {
                    CssDebitNote cssDebitNote = cssDebitNoteService.getById(item.getDebitNoteId());
                    if (cssDebitNote != null) {
                        LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitNoteCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
                        cssDebitNoteCurrencyWrapper.eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssDebitNoteCurrency::getDebitNoteId, cssDebitNote.getDebitNoteId()).eq(CssDebitNoteCurrency::getCurrency, item.getCurrency());
                        CssDebitNoteCurrency cssDebitNoteCurrency = cssDebitNoteCurrencyService.getOne(cssDebitNoteCurrencyWrapper);
                        if (cssDebitNoteCurrency != null) {
                            columnJ.put("exchangeRate", cssDebitNoteCurrency.getExchangeRate());
                        }
                    }
                } else if (item.getStatementId() != null) {
                    Statement statement = statementService.getById(item.getStatementId());
                    if (statement != null) {
                        LambdaQueryWrapper<StatementCurrency> statementCurrencyWrapper = Wrappers.<StatementCurrency>lambdaQuery();
                        statementCurrencyWrapper.eq(StatementCurrency::getOrgId, SecurityUtils.getUser().getOrgId()).eq(StatementCurrency::getStatementId, statement.getStatementId()).eq(StatementCurrency::getCurrency, item.getCurrency());
                        StatementCurrency statementCurrency = statementCurrencyService.getOne(statementCurrencyWrapper);
                        if (statementCurrency != null) {
                            columnJ.put("exchangeRate", statementCurrency.getExchangeRate());
                        }
                    }
                }
                list.add(columnJ);
                //-----------------贷方--------------------
                HashMap<String, Object> columnD = new HashMap<>();
                columnD.put("voucherDate", item.getVoucherDate());
                columnD.put("voucherZ", "记");
                columnD.put("voucherNumber", item.getVoucherNumber());
                columnD.put("attachNumber", 0);
                columnD.put("serial", 2);
                columnD.put("summary", item.getCustomerName() + " / " + item.getInvoiceNum());

                if (financialAccountD != null) {
                    if ("子科目".equals(financialAccountD.getManageMode())) {
                        if (!queryCoopAlready) {
                            coopVo = remoteCoopService.viewCoop(item.getCustomerId().toString()).getData();
                            queryCoopAlready = true;
                        }
                        if (coopVo != null) {
                            columnD.put("financialAccountCode", financialAccountD.getFinancialAccountCode() + " " + (StrUtil.isNotBlank(coopVo.getFinancialCode()) ? coopVo.getFinancialCode() : coopVo.getCoop_code()));
                        } else {
                            columnD.put("financialAccountCode", financialAccountD.getFinancialAccountCode());
                        }
                    } else {
                        columnD.put("financialAccountCode", financialAccountD.getFinancialAccountCode());
                    }
                    if ("往来单位".equals(financialAccountD.getSubsidiaryAccount())) {
                        if (!queryCoopAlready) {
                            coopVo = remoteCoopService.viewCoop(item.getCustomerId().toString()).getData();
                        }
                        if (coopVo != null) {
                            columnD.put("customer", StrUtil.isNotBlank(coopVo.getFinancialCode()) ? coopVo.getFinancialCode() : coopVo.getCoop_code());
                        }
                    }
                }
                columnD.put("financialAccountName", "主营业务收入");
                columnD.put("amountJ", null);
                columnD.put("amountD", item.getAmount());
                columnD.put("functionAmount", item.getAmount());
                columnD.put("currency", columnJ.get("currency"));
                columnD.put("exchangeRate", columnJ.get("exchangeRate"));
                list.add(columnD);
            });
            System.out.println("耗时：" + (LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")) - start) + "s");
            OrgVo orgVo = remoteServiceToHRS.getByOrgId(SecurityUtils.getUser().getOrgId()).getData();
            if ("金蝶".equals(orgVo.getFinancialVoucherOutType())) {
                templatePath = FilePathUtils.filePath + "/PDFtemplate/voucher_export_template0.xlsx";
            } else if ("用友".equals(orgVo.getFinancialVoucherOutType())) {
                templatePath = FilePathUtils.filePath + "/PDFtemplate/voucher_export_template0.xlsx";
            }else{
                templatePath = FilePathUtils.filePath + "/PDFtemplate/voucher_export_template0.xlsx";
            }
        } else if (type == 1) {
            LambdaQueryWrapper<CssFinancialAccount> cssFinancialAccountWrapperJ = Wrappers.<CssFinancialAccount>lambdaQuery();
            cssFinancialAccountWrapperJ.eq(CssFinancialAccount::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssFinancialAccount::getBusinessScope, businessScope).eq(CssFinancialAccount::getFinancialAccountType, "主营业务成本");
            CssFinancialAccount financialAccountJ = cssFinancialAccountService.getOne(cssFinancialAccountWrapperJ);

            LambdaQueryWrapper<CssFinancialAccount> cssFinancialAccountWrapperD = Wrappers.<CssFinancialAccount>lambdaQuery();
            cssFinancialAccountWrapperD.eq(CssFinancialAccount::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssFinancialAccount::getBusinessScope, businessScope).eq(CssFinancialAccount::getFinancialAccountType, "应付账款");
            CssFinancialAccount financialAccountD = cssFinancialAccountService.getOne(cssFinancialAccountWrapperD);

            LambdaQueryWrapper<CssCostInvoiceDetail> cssCostInvoiceDetailWrapper = Wrappers.<CssCostInvoiceDetail>lambdaQuery();
            cssCostInvoiceDetailWrapper.eq(CssCostInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).in(CssCostInvoiceDetail::getInvoiceDetailId, ids).orderByAsc(CssCostInvoiceDetail::getVoucherNumber, CssCostInvoiceDetail::getInvoiceDetailId);
            List<CssCostInvoiceDetail> cssCostInvoiceDetails = cssCostInvoiceDetailService.list(cssCostInvoiceDetailWrapper);
            cssCostInvoiceDetails.stream().forEach(item -> {
                //-----------------借方--------------------
                HashMap<String, Object> columnJ = new HashMap<>();
                columnJ.put("voucherDate", item.getVoucherDate());
                columnJ.put("voucherZ", "记");
                columnJ.put("voucherNumber", item.getVoucherNumber());
                columnJ.put("attachNumber", 0);
                columnJ.put("serial", 1);
                columnJ.put("summary", item.getCustomerName() + " / " + item.getInvoiceNum());

                CoopVo coopVo = null;
                boolean queryCoopAlready = false;
                if (financialAccountJ != null) {
                    if ("子科目".equals(financialAccountJ.getManageMode())) {
                        coopVo = remoteCoopService.viewCoop(item.getCustomerId().toString()).getData();
                        queryCoopAlready = true;
                        if (coopVo != null) {
                            columnJ.put("financialAccountCode", financialAccountJ.getFinancialAccountCode() + " " + (StrUtil.isNotBlank(coopVo.getFinancialCode()) ? coopVo.getFinancialCode() : coopVo.getCoop_code()));
                        } else {
                            columnJ.put("financialAccountCode", financialAccountJ.getFinancialAccountCode());
                        }
                    } else {
                        columnJ.put("financialAccountCode", financialAccountJ.getFinancialAccountCode());
                    }

                    if ("往来单位".equals(financialAccountJ.getSubsidiaryAccount())) {
                        if (!queryCoopAlready) {
                            coopVo = remoteCoopService.viewCoop(item.getCustomerId().toString()).getData();
                            queryCoopAlready = true;
                        }
                        if (coopVo != null) {
                            columnJ.put("customer", StrUtil.isNotBlank(coopVo.getFinancialCode()) ? coopVo.getFinancialCode() : coopVo.getCoop_code());
                        }
                    }
                }
                columnJ.put("financialAccountName", "主营业务成本");
                columnJ.put("amountJ", item.getAmount());
                columnJ.put("amountD", null);
                columnJ.put("functionAmount", item.getAmount());
                if (org != null && StrUtil.isNotBlank(org.getFinancialVoucherOutCurrency()) && "CNY".equals(item.getCurrency())) {
                    columnJ.put("currency", org.getFinancialVoucherOutCurrency());
                } else {
                    columnJ.put("currency", item.getCurrency());
                }
                CssPayment cssPayment = cssPaymentService.getById(item.getPaymentId());
                if (cssPayment != null) {
                    columnJ.put("exchangeRate", cssPayment.getExchangeRate());
                }
                list.add(columnJ);
                //-----------------贷方--------------------
                HashMap<String, Object> columnD = new HashMap<>();
                columnD.put("voucherDate", item.getVoucherDate());
                columnD.put("voucherZ", "记");
                columnD.put("voucherNumber", item.getVoucherNumber());
                columnD.put("attachNumber", 0);
                columnD.put("serial", 2);
                columnD.put("summary", item.getCustomerName() + " / " + item.getInvoiceNum());
                if (financialAccountD != null) {
                    if ("子科目".equals(financialAccountD.getManageMode())) {
                        if (!queryCoopAlready) {
                            coopVo = remoteCoopService.viewCoop(item.getCustomerId().toString()).getData();
                            queryCoopAlready = true;
                        }
                        if (coopVo != null) {
                            columnD.put("financialAccountCode", financialAccountD.getFinancialAccountCode() + " " + (StrUtil.isNotBlank(coopVo.getFinancialCode()) ? coopVo.getFinancialCode() : coopVo.getCoop_code()));
                        } else {
                            columnD.put("financialAccountCode", financialAccountD.getFinancialAccountCode());
                        }
                    } else {
                        columnD.put("financialAccountCode", financialAccountD.getFinancialAccountCode());
                    }
                    if ("往来单位".equals(financialAccountD.getSubsidiaryAccount())) {
                        if (!queryCoopAlready) {
                            coopVo = remoteCoopService.viewCoop(item.getCustomerId().toString()).getData();
                        }
                        if (coopVo != null) {
                            columnD.put("customer", StrUtil.isNotBlank(coopVo.getFinancialCode()) ? coopVo.getFinancialCode() : coopVo.getCoop_code());
                        }
                    }
                }
                columnD.put("financialAccountName", "应付账款");
                columnD.put("amountJ", null);
                columnD.put("amountD", item.getAmount());
                columnD.put("functionAmount", item.getAmount());
                columnD.put("currency", columnJ.get("currency"));

                if (cssPayment != null) {
                    columnD.put("exchangeRate", cssPayment.getExchangeRate());
                }
                list.add(columnD);
            });
            System.out.println("耗时：" + (LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")) - start) + "s");
            OrgVo orgVo = remoteServiceToHRS.getByOrgId(SecurityUtils.getUser().getOrgId()).getData();
            if ("金蝶".equals(orgVo.getFinancialVoucherOutType())) {
                templatePath = FilePathUtils.filePath + "/PDFtemplate/voucher_export_template1.xlsx";
            } else if ("用友".equals(orgVo.getFinancialVoucherOutType())) {
                templatePath = FilePathUtils.filePath + "/PDFtemplate/voucher_export_template1.xlsx";
            }else{
                templatePath = FilePathUtils.filePath + "/PDFtemplate/voucher_export_template1.xlsx";
            }
        } else if (type == 2) {
            LambdaQueryWrapper<CssFinancialAccount> cssFinancialAccountWrapperJ = Wrappers.<CssFinancialAccount>lambdaQuery();
            cssFinancialAccountWrapperJ.eq(CssFinancialAccount::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssFinancialAccount::getBusinessScope, businessScope).eq(CssFinancialAccount::getFinancialAccountType, "应收账款");
            CssFinancialAccount financialAccountJ = cssFinancialAccountService.getOne(cssFinancialAccountWrapperJ);

            LambdaQueryWrapper<CssIncomeInvoiceDetailWriteoff> cssIncomeInvoiceDetailWriteoffWrapper = Wrappers.<CssIncomeInvoiceDetailWriteoff>lambdaQuery();
            cssIncomeInvoiceDetailWriteoffWrapper.eq(CssIncomeInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).in(CssIncomeInvoiceDetailWriteoff::getInvoiceDetailWriteoffId, ids).orderByAsc(CssIncomeInvoiceDetailWriteoff::getVoucherNumber, CssIncomeInvoiceDetailWriteoff::getInvoiceDetailWriteoffId);
            List<CssIncomeInvoiceDetailWriteoff> cssIncomeInvoiceDetailWriteoffs = cssIncomeInvoiceDetailWriteoffService.list(cssIncomeInvoiceDetailWriteoffWrapper);
            cssIncomeInvoiceDetailWriteoffs.stream().forEach(item -> {
                //-----------------借方--------------------
                HashMap<String, Object> columnJ = new HashMap<>();
                columnJ.put("voucherDate", item.getVoucherDate());
                columnJ.put("voucherZ", "记");
                columnJ.put("voucherNumber", item.getVoucherNumber());
                columnJ.put("attachNumber", 0);
                columnJ.put("serial", 1);
                CssIncomeInvoiceDetail costInvoiceDetail = cssIncomeInvoiceDetailService.getById(item.getInvoiceDetailId());
                columnJ.put("summary", item.getCustomerName() + " / " + item.getWriteoffNum() + " / " + costInvoiceDetail.getInvoiceNum());
                CoopVo coopVo = null;
                boolean queryCoopAlready = false;

                if ("子科目".equals(item.getFinancialAccountType())) {
                    coopVo = remoteCoopService.viewCoop(item.getCustomerId().toString()).getData();
                    queryCoopAlready = true;
                    if (coopVo != null) {
                        columnJ.put("financialAccountCode", item.getFinancialAccountCode() + " " + (StrUtil.isNotBlank(coopVo.getFinancialCode()) ? coopVo.getFinancialCode() : coopVo.getCoop_code()));
                    } else {
                        columnJ.put("financialAccountCode", item.getFinancialAccountCode());
                    }
                } else {
                    columnJ.put("financialAccountCode", item.getFinancialAccountCode());
                }
                if ("往来单位".equals(item.getFinancialAccountType())) {
                    if (!queryCoopAlready) {
                        coopVo = remoteCoopService.viewCoop(item.getCustomerId().toString()).getData();
                        queryCoopAlready = true;
                    }
                    if (coopVo != null) {
                        columnJ.put("customer", StrUtil.isNotBlank(coopVo.getFinancialCode()) ? coopVo.getFinancialCode() : coopVo.getCoop_code());
                    }
                }
                columnJ.put("financialAccountName", StrUtil.isBlank(item.getFinancialAccountName()) ? "" : item.getFinancialAccountName().split(" ")[0]);
                columnJ.put("amountJ", item.getAmountWriteoff());
                columnJ.put("amountD", null);
                columnJ.put("functionAmount", item.getAmountWriteoff());
                if (org != null && StrUtil.isNotBlank(org.getFinancialVoucherOutCurrency()) && "CNY".equals(item.getCurrency())) {
                    columnJ.put("currency", org.getFinancialVoucherOutCurrency());
                } else {
                    columnJ.put("currency", item.getCurrency());
                }
                if (item.getDebitNoteId() != null) {
                    CssDebitNote cssDebitNote = cssDebitNoteService.getById(item.getDebitNoteId());
                    if (cssDebitNote != null) {
                        LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitNoteCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
                        cssDebitNoteCurrencyWrapper.eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssDebitNoteCurrency::getDebitNoteId, cssDebitNote.getDebitNoteId()).eq(CssDebitNoteCurrency::getCurrency, item.getCurrency());
                        CssDebitNoteCurrency cssDebitNoteCurrency = cssDebitNoteCurrencyService.getOne(cssDebitNoteCurrencyWrapper);
                        if (cssDebitNoteCurrency != null) {
                            columnJ.put("exchangeRate", cssDebitNoteCurrency.getExchangeRate());
                        }
                    }
                } else if (item.getStatementId() != null) {
                    Statement statement = statementService.getById(item.getStatementId());
                    if (statement != null) {
                        LambdaQueryWrapper<StatementCurrency> statementCurrencyWrapper = Wrappers.<StatementCurrency>lambdaQuery();
                        statementCurrencyWrapper.eq(StatementCurrency::getOrgId, SecurityUtils.getUser().getOrgId()).eq(StatementCurrency::getStatementId, statement.getStatementId()).eq(StatementCurrency::getCurrency, item.getCurrency());
                        StatementCurrency statementCurrency = statementCurrencyService.getOne(statementCurrencyWrapper);
                        if (statementCurrency != null) {
                            columnJ.put("exchangeRate", statementCurrency.getExchangeRate());
                        }
                    }
                }
                list.add(columnJ);
                //-----------------贷方--------------------
                HashMap<String, Object> columnD = new HashMap<>();
                columnD.put("voucherDate", item.getVoucherDate());
                columnD.put("voucherZ", "记");
                columnD.put("voucherNumber", item.getVoucherNumber());
                columnD.put("attachNumber", 0);
                columnD.put("serial", 2);
                columnD.put("summary", item.getCustomerName() + " / " + item.getWriteoffNum() + " / " + costInvoiceDetail.getInvoiceNum());

                if (financialAccountJ != null) {
                    if ("子科目".equals(financialAccountJ.getManageMode())) {
                        if (!queryCoopAlready) {
                            coopVo = remoteCoopService.viewCoop(item.getCustomerId().toString()).getData();
                            queryCoopAlready = true;
                        }
                        if (coopVo != null) {
                            columnD.put("financialAccountCode", financialAccountJ.getFinancialAccountCode() + " " + (StrUtil.isNotBlank(coopVo.getFinancialCode()) ? coopVo.getFinancialCode() : coopVo.getCoop_code()));
                        } else {
                            columnD.put("financialAccountCode", financialAccountJ.getFinancialAccountCode());
                        }
                    } else {
                        columnD.put("financialAccountCode", financialAccountJ.getFinancialAccountCode());
                    }

                    if ("往来单位".equals(financialAccountJ.getSubsidiaryAccount())) {
                        if (!queryCoopAlready) {
                            coopVo = remoteCoopService.viewCoop(item.getCustomerId().toString()).getData();
                        }
                        if (coopVo != null) {
                            columnD.put("customer", StrUtil.isNotBlank(coopVo.getFinancialCode()) ? coopVo.getFinancialCode() : coopVo.getCoop_code());
                        }
                    }
                }
                columnD.put("financialAccountName", "应收账款");
                columnD.put("amountJ", null);
                columnD.put("amountD", item.getAmountWriteoff());
                columnD.put("functionAmount", item.getAmountWriteoff());
                columnD.put("currency", columnJ.get("currency"));
                columnD.put("exchangeRate", columnJ.get("exchangeRate"));
                list.add(columnD);
            });
            System.out.println("耗时：" + (LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")) - start) + "s");
            OrgVo orgVo = remoteServiceToHRS.getByOrgId(SecurityUtils.getUser().getOrgId()).getData();
            if ("金蝶".equals(orgVo.getFinancialVoucherOutType())) {
                templatePath = FilePathUtils.filePath + "/PDFtemplate/voucher_export_template2.xlsx";
            } else if ("用友".equals(orgVo.getFinancialVoucherOutType())) {
                templatePath = FilePathUtils.filePath + "/PDFtemplate/voucher_export_template2.xlsx";
            }else{
                templatePath = FilePathUtils.filePath + "/PDFtemplate/voucher_export_template2.xlsx";
            }
        } else if (type == 3) {
            LambdaQueryWrapper<CssFinancialAccount> cssFinancialAccountWrapperJ = Wrappers.<CssFinancialAccount>lambdaQuery();
            cssFinancialAccountWrapperJ.eq(CssFinancialAccount::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssFinancialAccount::getBusinessScope, businessScope).eq(CssFinancialAccount::getFinancialAccountType, "应付账款");
            CssFinancialAccount financialAccountJ = cssFinancialAccountService.getOne(cssFinancialAccountWrapperJ);

            LambdaQueryWrapper<CssCostInvoiceDetailWriteoff> cssCostInvoiceDetailWriteoffWrapper = Wrappers.<CssCostInvoiceDetailWriteoff>lambdaQuery();
            cssCostInvoiceDetailWriteoffWrapper.eq(CssCostInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).in(CssCostInvoiceDetailWriteoff::getInvoiceDetailWriteoffId, ids).orderByAsc(CssCostInvoiceDetailWriteoff::getVoucherNumber, CssCostInvoiceDetailWriteoff::getInvoiceDetailWriteoffId);
            List<CssCostInvoiceDetailWriteoff> cssCostInvoiceDetailWriteoffs = cssCostInvoiceDetailWriteoffService.list(cssCostInvoiceDetailWriteoffWrapper);
            cssCostInvoiceDetailWriteoffs.stream().forEach(item -> {
                //-----------------借方--------------------
                HashMap<String, Object> columnJ = new HashMap<>();
                columnJ.put("voucherDate", item.getVoucherDate());
                columnJ.put("voucherZ", "记");
                columnJ.put("voucherNumber", item.getVoucherNumber());
                columnJ.put("attachNumber", 0);
                columnJ.put("serial", 1);
                CssCostInvoiceDetail costInvoiceDetail = cssCostInvoiceDetailService.getById(item.getInvoiceDetailId());
                columnJ.put("summary", item.getCustomerName() + " / " + item.getWriteoffNum() + " / " + costInvoiceDetail.getInvoiceNum());

                CoopVo coopVo = null;
                boolean queryCoopAlready = false;
                if (financialAccountJ != null) {
                    if ("子科目".equals(financialAccountJ.getManageMode())) {
                        coopVo = remoteCoopService.viewCoop(item.getCustomerId().toString()).getData();
                        queryCoopAlready = true;
                        if (coopVo != null) {
                            columnJ.put("financialAccountCode", financialAccountJ.getFinancialAccountCode() + " " + (StrUtil.isNotBlank(coopVo.getFinancialCode()) ? coopVo.getFinancialCode() : coopVo.getCoop_code()));
                        } else {
                            columnJ.put("financialAccountCode", financialAccountJ.getFinancialAccountCode());
                        }
                    } else {
                        columnJ.put("financialAccountCode", financialAccountJ.getFinancialAccountCode());
                    }

                    if ("往来单位".equals(financialAccountJ.getSubsidiaryAccount())) {
                        if (!queryCoopAlready) {
                            coopVo = remoteCoopService.viewCoop(item.getCustomerId().toString()).getData();
                            queryCoopAlready = true;
                        }
                        if (coopVo != null) {
                            columnJ.put("customer", StrUtil.isNotBlank(coopVo.getFinancialCode()) ? coopVo.getFinancialCode() : coopVo.getCoop_code());
                        }
                    }
                }
                columnJ.put("financialAccountName", "应付账款");
                columnJ.put("amountJ", item.getAmountWriteoff());
                columnJ.put("amountD", null);
                columnJ.put("functionAmount", item.getAmountWriteoff());
                if (org != null && StrUtil.isNotBlank(org.getFinancialVoucherOutCurrency()) && "CNY".equals(item.getCurrency())) {
                    columnJ.put("currency", org.getFinancialVoucherOutCurrency());
                } else {
                    columnJ.put("currency", item.getCurrency());
                }
                CssPayment cssPayment = cssPaymentService.getById(item.getPaymentId());
                if (cssPayment != null) {
                    columnJ.put("exchangeRate", cssPayment.getExchangeRate());
                }
                list.add(columnJ);
                //-----------------贷方--------------------
                HashMap<String, Object> columnD = new HashMap<>();
                columnD.put("voucherDate", item.getVoucherDate());
                columnD.put("voucherZ", "记");
                columnD.put("voucherNumber", item.getVoucherNumber());
                columnD.put("attachNumber", 0);
                columnD.put("serial", 2);
                columnD.put("summary", item.getCustomerName() + " / " + item.getWriteoffNum() + " / " + costInvoiceDetail.getInvoiceNum());
                if ("子科目".equals(item.getFinancialAccountType())) {
                    if (!queryCoopAlready) {
                        coopVo = remoteCoopService.viewCoop(item.getCustomerId().toString()).getData();
                        queryCoopAlready = true;
                    }
                    if (coopVo != null) {
                        columnD.put("financialAccountCode", item.getFinancialAccountCode() + " " + (StrUtil.isNotBlank(coopVo.getFinancialCode()) ? coopVo.getFinancialCode() : coopVo.getCoop_code()));
                    } else {
                        columnD.put("financialAccountCode", item.getFinancialAccountCode());
                    }
                } else {
                    columnD.put("financialAccountCode", item.getFinancialAccountCode());
                }
                if ("往来单位".equals(item.getFinancialAccountType())) {
                    if (!queryCoopAlready) {
                        coopVo = remoteCoopService.viewCoop(item.getCustomerId().toString()).getData();
                    }
                    if (coopVo != null) {
                        columnD.put("customer", StrUtil.isNotBlank(coopVo.getFinancialCode()) ? coopVo.getFinancialCode() : coopVo.getCoop_code());
                    }
                }
                columnD.put("financialAccountName", item.getFinancialAccountName());
                columnD.put("amountJ", null);
                columnD.put("amountD", item.getAmountWriteoff());
                columnD.put("functionAmount", item.getAmountWriteoff());
                columnD.put("currency", columnJ.get("currency"));

                if (cssPayment != null) {
                    columnD.put("exchangeRate", cssPayment.getExchangeRate());
                }
                list.add(columnD);
            });
            System.out.println("耗时：" + (LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")) - start) + "s");
            OrgVo orgVo = remoteServiceToHRS.getByOrgId(SecurityUtils.getUser().getOrgId()).getData();
            if ("金蝶".equals(orgVo.getFinancialVoucherOutType())) {
                templatePath = FilePathUtils.filePath + "/PDFtemplate/voucher_export_template3.xlsx";
            } else if ("用友".equals(orgVo.getFinancialVoucherOutType())) {
                templatePath = FilePathUtils.filePath + "/PDFtemplate/voucher_export_template3.xlsx";
            }else{
                templatePath = FilePathUtils.filePath + "/PDFtemplate/voucher_export_template3.xlsx";
            }
        }
        map.put("data", list);
        JxlsUtils.exportExcelWithLocalModel(templatePath, map);
    }
}
