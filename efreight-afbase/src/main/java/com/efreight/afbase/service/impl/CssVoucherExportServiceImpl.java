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
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.core.utils.JxlsUtils;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class CssVoucherExportServiceImpl implements CssVoucherExportService {

    private final CssVoucherExportMapper cssVoucherExportMapper;

    private final CssCostWriteoffService cssCostWriteoffService;

    private final CssPaymentService cssPaymentService;

    private final CssCostWriteoffDetailService cssCostWriteoffDetailService;

    private final AfOrderService afOrderService;

    private final ScOrderService scOrderService;

    private final TcOrderService tcOrderService;

    private final LcOrderService lcOrderService;

    private final IoOrderService ioOrderService;

    @Override
    public IPage getPage(Page page, CssVoucherExport cssVoucherExport) {
        IPage<CssVoucherExport> result = null;
        cssVoucherExport.setOrgId(SecurityUtils.getUser().getOrgId());
        if (cssVoucherExport.getType() == 0) {
            if (cssVoucherExport.getBusinessScope().startsWith("A")) {
                result = cssVoucherExportMapper.pageIncomeForAF(page, cssVoucherExport);
            } else if (cssVoucherExport.getBusinessScope().startsWith("S")) {
                result = cssVoucherExportMapper.pageIncomeForSC(page, cssVoucherExport);
            } else if (cssVoucherExport.getBusinessScope().startsWith("T")) {
                result = cssVoucherExportMapper.pageIncomeForTC(page, cssVoucherExport);
            } else if ("LC".equals(cssVoucherExport.getBusinessScope())) {
                result = cssVoucherExportMapper.pageIncomeForLC(page, cssVoucherExport);
            } else if ("IO".equals(cssVoucherExport.getBusinessScope())) {
                result = cssVoucherExportMapper.pageIncomeForIO(page, cssVoucherExport);
            }
        } else if (cssVoucherExport.getType() == 1) {
            if (cssVoucherExport.getBusinessScope().startsWith("A")) {
                result = cssVoucherExportMapper.pageCostForAF(page, cssVoucherExport);
            } else if (cssVoucherExport.getBusinessScope().startsWith("S")) {
                result = cssVoucherExportMapper.pageCostForSC(page, cssVoucherExport);
            } else if (cssVoucherExport.getBusinessScope().startsWith("T")) {
                result = cssVoucherExportMapper.pageCostForTC(page, cssVoucherExport);
            } else if ("LC".equals(cssVoucherExport.getBusinessScope())) {
                result = cssVoucherExportMapper.pageCostForLC(page, cssVoucherExport);
            } else if ("IO".equals(cssVoucherExport.getBusinessScope())) {
                result = cssVoucherExportMapper.pageCostForIO(page, cssVoucherExport);
            }
        } else if (cssVoucherExport.getType() == 2) {
            result = cssVoucherExportMapper.pageIncomeWriteoff(page, cssVoucherExport);
        } else if (cssVoucherExport.getType() == 3) {
            LambdaQueryWrapper<CssCostWriteoff> wrapper = Wrappers.<CssCostWriteoff>lambdaQuery();
            if (StrUtil.isNotBlank(cssVoucherExport.getAwbNumber())) {
                List<Integer> orderIds = null;
                if (cssVoucherExport.getBusinessScope().startsWith("A")) {
                    LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
                    orderWrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(AfOrder::getAwbNumber, "%" + cssVoucherExport.getAwbNumber() + "%").or(j -> j.like(AfOrder::getOrderCode, "%" + cssVoucherExport.getAwbNumber() + "%")));
                    List<AfOrder> orderList = afOrderService.list(orderWrapper);
                    if (orderList.size() == 0) {
                        page.setTotal(0);
                        page.setRecords(new ArrayList());
                        return page;
                    }
                    orderIds = orderList.stream().map(AfOrder::getOrderId).collect(Collectors.toList());
                }
                if (cssVoucherExport.getBusinessScope().startsWith("S")) {
                    LambdaQueryWrapper<ScOrder> orderWrapper = Wrappers.<ScOrder>lambdaQuery();
                    orderWrapper.eq(ScOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(ScOrder::getMblNumber, "%" + cssVoucherExport.getAwbNumber() + "%").or(j -> j.like(ScOrder::getOrderCode, "%" + cssVoucherExport.getAwbNumber() + "%")));
                    List<ScOrder> orderList = scOrderService.list(orderWrapper);
                    if (orderList.size() == 0) {
                        page.setTotal(0);
                        page.setRecords(new ArrayList());
                        return page;
                    }
                    orderIds = orderList.stream().map(ScOrder::getOrderId).collect(Collectors.toList());
                }
                if (cssVoucherExport.getBusinessScope().startsWith("T")) {
                    LambdaQueryWrapper<TcOrder> orderWrapper = Wrappers.<TcOrder>lambdaQuery();
                    orderWrapper.eq(TcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(TcOrder::getCustomerNumber, "%" + cssVoucherExport.getAwbNumber() + "%").or(j -> j.like(TcOrder::getOrderCode, "%" + cssVoucherExport.getAwbNumber() + "%")));
                    List<TcOrder> orderList = tcOrderService.list(orderWrapper);
                    if (orderList.size() == 0) {
                        page.setTotal(0);
                        page.setRecords(new ArrayList());
                        return page;
                    }
                    orderIds = orderList.stream().map(TcOrder::getOrderId).collect(Collectors.toList());
                }
                if (cssVoucherExport.getBusinessScope().startsWith("L")) {
                    LambdaQueryWrapper<LcOrder> orderWrapper = Wrappers.<LcOrder>lambdaQuery();
                    orderWrapper.eq(LcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(LcOrder::getCustomerNumber, "%" + cssVoucherExport.getAwbNumber() + "%").or(j -> j.like(LcOrder::getOrderCode, "%" + cssVoucherExport.getAwbNumber() + "%")));
                    List<LcOrder> orderList = lcOrderService.list(orderWrapper);
                    if (orderList.size() == 0) {
                        page.setTotal(0);
                        page.setRecords(new ArrayList());
                        return page;
                    }
                    orderIds = orderList.stream().map(LcOrder::getOrderId).collect(Collectors.toList());
                }
                if (cssVoucherExport.getBusinessScope().equals("IO")) {
                    LambdaQueryWrapper<IoOrder> orderWrapper = Wrappers.<IoOrder>lambdaQuery();
                    orderWrapper.eq(IoOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(IoOrder::getCustomerNumber, "%" + cssVoucherExport.getAwbNumber() + "%").or(j -> j.like(IoOrder::getOrderCode, "%" + cssVoucherExport.getAwbNumber() + "%")));
                    List<IoOrder> orderList = ioOrderService.list(orderWrapper);
                    if (orderList.size() == 0) {
                        page.setTotal(0);
                        page.setRecords(new ArrayList());
                        return page;
                    }
                    orderIds = orderList.stream().map(IoOrder::getOrderId).collect(Collectors.toList());
                }
                if (orderIds != null) {
                    LambdaQueryWrapper<CssCostWriteoffDetail> detailWrapper = Wrappers.<CssCostWriteoffDetail>lambdaQuery();
                    detailWrapper.eq(CssCostWriteoffDetail::getOrgId, SecurityUtils.getUser().getOrgId()).in(CssCostWriteoffDetail::getOrderId, orderIds);
                    List<CssCostWriteoffDetail> detailList = cssCostWriteoffDetailService.list(detailWrapper);
                    if (detailList.size() == 0) {
                        page.setTotal(0);
                        page.setRecords(new ArrayList());
                        return page;
                    }
                    List<Integer> costWriteoffIds = detailList.stream().map(CssCostWriteoffDetail::getCostWriteoffId).distinct().collect(Collectors.toList());
                    wrapper.in(CssCostWriteoff::getCostWriteoffId, costWriteoffIds);
                }
            }
            wrapper.eq(CssCostWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostWriteoff::getBusinessScope, cssVoucherExport.getBusinessScope());
            if (StrUtil.isNotBlank(cssVoucherExport.getCoopName())) {
                wrapper.like(CssCostWriteoff::getCustomerName, "%" + cssVoucherExport.getCoopName() + "%");
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getDebitnoteNumber()) || StrUtil.isNotBlank(cssVoucherExport.getInvoiceNumber()) || cssVoucherExport.getInvoiceDateStart() != null || cssVoucherExport.getInvoiceDateEnd() != null) {
                LambdaQueryWrapper<CssPayment> paymentWrapper = Wrappers.<CssPayment>lambdaQuery();
                paymentWrapper.eq(CssPayment::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPayment::getBusinessScope, cssVoucherExport.getBusinessScope());
                if (StrUtil.isNotBlank(cssVoucherExport.getDebitnoteNumber())) {
                    paymentWrapper.like(CssPayment::getPaymentNum, cssVoucherExport.getDebitnoteNumber());
                }
                if (StrUtil.isNotBlank(cssVoucherExport.getInvoiceNumber())) {
                    paymentWrapper.like(CssPayment::getInvoiceNum, cssVoucherExport.getInvoiceNumber());
                }
                if (cssVoucherExport.getInvoiceDateStart() != null) {
                    paymentWrapper.ge(CssPayment::getInvoiceDate, cssVoucherExport.getInvoiceDateStart());
                }
                if (cssVoucherExport.getInvoiceDateEnd() != null) {
                    paymentWrapper.le(CssPayment::getInvoiceDate, cssVoucherExport.getInvoiceDateEnd());
                }
                List<Integer> paymentIds = cssPaymentService.list(paymentWrapper).stream().map(cssPayment -> cssPayment.getPaymentId()).collect(Collectors.toList());
                if (paymentIds.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                wrapper.in(CssCostWriteoff::getPaymentId, paymentIds);
            }
            if (cssVoucherExport.getWriteoffDateStart() != null) {
                wrapper.ge(CssCostWriteoff::getWriteoffDate, cssVoucherExport.getWriteoffDateStart());
            }
            if (cssVoucherExport.getWriteoffDateEnd() != null) {
                wrapper.le(CssCostWriteoff::getWriteoffDate, cssVoucherExport.getWriteoffDateEnd());
            }
            if (cssVoucherExport.getVoucherDateStart() != null) {
                wrapper.ge(CssCostWriteoff::getVoucherDate, cssVoucherExport.getVoucherDateStart());
            }
            if (cssVoucherExport.getVoucherDateEnd() != null) {
                wrapper.le(CssCostWriteoff::getVoucherDate, cssVoucherExport.getVoucherDateEnd());
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getWriteoffNumber())) {
                wrapper.like(CssCostWriteoff::getWriteoffNum, "%" + cssVoucherExport.getWriteoffNumber() + "%");
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getWriteoffCreatorName())) {
                wrapper.like(CssCostWriteoff::getCreatorName, cssVoucherExport.getWriteoffCreatorName());
            }
            if (cssVoucherExport.getVoucherStatus() != null && cssVoucherExport.getVoucherStatus()) {
                wrapper.isNotNull(CssCostWriteoff::getVoucherDate);
            }
            if (cssVoucherExport.getVoucherStatus() != null && !cssVoucherExport.getVoucherStatus()) {
                wrapper.isNull(CssCostWriteoff::getVoucherDate);
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getVoucherCreatorName())) {
                wrapper.like(CssCostWriteoff::getVoucherCreatorName, cssVoucherExport.getVoucherCreatorName());
            }
            wrapper.orderByDesc(CssCostWriteoff::getCostWriteoffId);
            IPage<CssCostWriteoff> costWriteoffPage = cssCostWriteoffService.page(page, wrapper);
            List<CssVoucherExport> voucherExportList = costWriteoffPage.getRecords().stream().map(cssCostWriteoff -> {
                CssVoucherExport voucherExport = new CssVoucherExport();
                voucherExport.setWriteoffId(cssCostWriteoff.getCostWriteoffId());
                if (cssCostWriteoff.getVoucherDate() != null) {
                    voucherExport.setVoucherDate(cssCostWriteoff.getVoucherDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                }
                voucherExport.setVoucherNumber(cssCostWriteoff.getVoucherNumber());
                voucherExport.setVoucherCreatorName(cssCostWriteoff.getVoucherCreatorName());
                voucherExport.setWriteoffNumber(cssCostWriteoff.getWriteoffNum());
                voucherExport.setWriteoffDate(cssCostWriteoff.getWriteoffDate());
                voucherExport.setCoopName(cssCostWriteoff.getCustomerName());
                voucherExport.setWriteoffAmount(cssCostWriteoff.getAmountWriteoff());
                voucherExport.setWriteoffAmountStr(FormatUtils.formatWithQWF(voucherExport.getWriteoffAmount(), 2));
                voucherExport.setCurrency(cssCostWriteoff.getCurrency());
                CssPayment cssPayment = cssPaymentService.getById(cssCostWriteoff.getPaymentId());
                if (cssPayment != null) {
                    voucherExport.setInvoiceDate(cssPayment.getInvoiceDate());
                    voucherExport.setInvoiceNumber(cssPayment.getInvoiceNum());
                    voucherExport.setInvoiceRemark(cssPayment.getInvoiceRemark());
                    voucherExport.setInvoiceTitle(cssPayment.getInvoiceTitle());
                }
                voucherExport.setFinancialAccountCode(StrUtil.isBlank(cssCostWriteoff.getFinancialAccountCode()) ? "" : cssCostWriteoff.getFinancialAccountCode());
                voucherExport.setFinancialAccountName(StrUtil.isBlank(cssCostWriteoff.getFinancialAccountName()) ? "" : cssCostWriteoff.getFinancialAccountName());
                voucherExport.setBankFinancialAccount(voucherExport.getFinancialAccountCode() + " " + voucherExport.getFinancialAccountName());
                return voucherExport;
            }).collect(Collectors.toList());
            page.setTotal(costWriteoffPage.getTotal());
            page.setRecords(voucherExportList);
            page.setCurrent(costWriteoffPage.getCurrent());
            page.setSize(costWriteoffPage.getSize());
            result = page;
        } else if (cssVoucherExport.getType() == 4) {
            result = cssVoucherExportMapper.pageExpenseReport(page, cssVoucherExport);
        }


        if (cssVoucherExport.getType() == 0) {
            result.getRecords().stream().forEach(voucher -> {
                if (voucher.getIncomeFunctionalAmount() == null) {
                    voucher.setIncomeFunctionalAmount(BigDecimal.ZERO);
                }
                voucher.setIncomeFunctionalAmountStr(FormatUtils.formatWithQWF(voucher.getIncomeFunctionalAmount(), 2));
            });
        } else if (cssVoucherExport.getType() == 1) {
            result.getRecords().stream().forEach(voucher -> {
                if (voucher.getCostFunctionalAmount() == null) {
                    voucher.setCostFunctionalAmount(BigDecimal.ZERO);
                }
                voucher.setCostFunctionalAmountStr(FormatUtils.formatWithQWF(voucher.getCostFunctionalAmount(), 2));
            });
        } else if (cssVoucherExport.getType() == 2) {
            result.getRecords().stream().forEach(voucher -> {
                voucher.setWriteoffAmountStr(FormatUtils.formatWithQWF(voucher.getWriteoffAmount(), 2));
                voucher.setBankFinancialAccount((StrUtil.isBlank(voucher.getFinancialAccountCode()) ? "" : voucher.getFinancialAccountCode()) + " " + (StrUtil.isBlank(voucher.getFinancialAccountName()) ? "" : voucher.getFinancialAccountName()));
            });
        } else if (cssVoucherExport.getType() == 3) {

        } else if (cssVoucherExport.getType() == 4) {
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
                            } else if ("incomeFunctionalAmount".equals(colunmStrs[j])) {
                                map.put(colunmStrs[j], excel.getIncomeFunctionalAmountStr());
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
                            } else if ("costFunctionalAmount".equals(colunmStrs[j])) {
                                map.put(colunmStrs[j], excel.getCostFunctionalAmountStr());
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
                                map.put(colunmStrs[j], excel.getWriteoffAmountStr().replace(" ", "\n"));
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
                                map.put(colunmStrs[j], excel.getWriteoffAmountStr().replace(" ", "\n"));
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
            if (cssVoucherExport.getBusinessScope().startsWith("A")) {
                list = cssVoucherExportMapper.listIncomeForAF(cssVoucherExport);
            } else if (cssVoucherExport.getBusinessScope().startsWith("S")) {
                list = cssVoucherExportMapper.listIncomeForSC(cssVoucherExport);
            } else if (cssVoucherExport.getBusinessScope().startsWith("T")) {
                list = cssVoucherExportMapper.listIncomeForTC(cssVoucherExport);
            } else if ("LC".equals(cssVoucherExport.getBusinessScope())) {
                list = cssVoucherExportMapper.listIncomeForLC(cssVoucherExport);
            } else if ("IO".equals(cssVoucherExport.getBusinessScope())) {
                list = cssVoucherExportMapper.listIncomeForIO(cssVoucherExport);
            }
            list.stream().forEach(voucher -> {
                if (voucher.getIncomeFunctionalAmount() == null) {
                    voucher.setIncomeFunctionalAmount(BigDecimal.ZERO);
                }
                voucher.setIncomeFunctionalAmountStr(FormatUtils.formatWithQWF(voucher.getIncomeFunctionalAmount(), 2));
            });
        } else if (cssVoucherExport.getType() == 1) {
            if (cssVoucherExport.getBusinessScope().startsWith("A")) {
                list = cssVoucherExportMapper.listCostForAF(cssVoucherExport);
            } else if (cssVoucherExport.getBusinessScope().startsWith("S")) {
                list = cssVoucherExportMapper.listCostForSC(cssVoucherExport);
            } else if (cssVoucherExport.getBusinessScope().startsWith("T")) {
                list = cssVoucherExportMapper.listCostForTC(cssVoucherExport);
            } else if ("LC".equals(cssVoucherExport.getBusinessScope())) {
                list = cssVoucherExportMapper.listCostForLC(cssVoucherExport);
            } else if ("IO".equals(cssVoucherExport.getBusinessScope())) {
                list = cssVoucherExportMapper.listCostForIO(cssVoucherExport);
            }
            list.stream().forEach(voucher -> {
                if (voucher.getCostFunctionalAmount() == null) {
                    voucher.setCostFunctionalAmount(BigDecimal.ZERO);
                }
                voucher.setCostFunctionalAmountStr(FormatUtils.formatWithQWF(voucher.getCostFunctionalAmount(), 2));
            });
        } else if (cssVoucherExport.getType() == 2) {
            list = cssVoucherExportMapper.listIncomeWriteoff(cssVoucherExport);
            list.stream().forEach(voucher -> {
                voucher.setWriteoffAmountStr(FormatUtils.formatWithQWF(voucher.getWriteoffAmount(), 2));
                voucher.setBankFinancialAccount((StrUtil.isBlank(voucher.getFinancialAccountCode()) ? "" : voucher.getFinancialAccountCode()) + " " + (StrUtil.isBlank(voucher.getFinancialAccountName()) ? "" : voucher.getFinancialAccountName()));
            });
        } else if (cssVoucherExport.getType() == 3) {
            LambdaQueryWrapper<CssCostWriteoff> wrapper = Wrappers.<CssCostWriteoff>lambdaQuery();
            if (StrUtil.isNotBlank(cssVoucherExport.getAwbNumber())) {
                List<Integer> orderIds = null;
                if (cssVoucherExport.getBusinessScope().startsWith("A")) {
                    LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
                    orderWrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(AfOrder::getAwbNumber, "%" + cssVoucherExport.getAwbNumber() + "%").or(j -> j.like(AfOrder::getOrderCode, "%" + cssVoucherExport.getAwbNumber() + "%")));
                    List<AfOrder> orderList = afOrderService.list(orderWrapper);
                    if (orderList.size() == 0) {
                        return null;
                    }
                    orderIds = orderList.stream().map(AfOrder::getOrderId).collect(Collectors.toList());
                }
                if (cssVoucherExport.getBusinessScope().startsWith("S")) {
                    LambdaQueryWrapper<ScOrder> orderWrapper = Wrappers.<ScOrder>lambdaQuery();
                    orderWrapper.eq(ScOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(ScOrder::getMblNumber, "%" + cssVoucherExport.getAwbNumber() + "%").or(j -> j.like(ScOrder::getOrderCode, "%" + cssVoucherExport.getAwbNumber() + "%")));
                    List<ScOrder> orderList = scOrderService.list(orderWrapper);
                    if (orderList.size() == 0) {
                        return null;
                    }
                    orderIds = orderList.stream().map(ScOrder::getOrderId).collect(Collectors.toList());
                }
                if (cssVoucherExport.getBusinessScope().startsWith("T")) {
                    LambdaQueryWrapper<TcOrder> orderWrapper = Wrappers.<TcOrder>lambdaQuery();
                    orderWrapper.eq(TcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(TcOrder::getCustomerNumber, "%" + cssVoucherExport.getAwbNumber() + "%").or(j -> j.like(TcOrder::getOrderCode, "%" + cssVoucherExport.getAwbNumber() + "%")));
                    List<TcOrder> orderList = tcOrderService.list(orderWrapper);
                    if (orderList.size() == 0) {
                        return null;
                    }
                    orderIds = orderList.stream().map(TcOrder::getOrderId).collect(Collectors.toList());
                }
                if (cssVoucherExport.getBusinessScope().startsWith("L")) {
                    LambdaQueryWrapper<LcOrder> orderWrapper = Wrappers.<LcOrder>lambdaQuery();
                    orderWrapper.eq(LcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(LcOrder::getCustomerNumber, "%" + cssVoucherExport.getAwbNumber() + "%").or(j -> j.like(LcOrder::getOrderCode, "%" + cssVoucherExport.getAwbNumber() + "%")));
                    List<LcOrder> orderList = lcOrderService.list(orderWrapper);
                    if (orderList.size() == 0) {
                        return null;
                    }
                    orderIds = orderList.stream().map(LcOrder::getOrderId).collect(Collectors.toList());
                }
                if (cssVoucherExport.getBusinessScope().equals("IO")) {
                    LambdaQueryWrapper<IoOrder> orderWrapper = Wrappers.<IoOrder>lambdaQuery();
                    orderWrapper.eq(IoOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(IoOrder::getCustomerNumber, "%" + cssVoucherExport.getAwbNumber() + "%").or(j -> j.like(IoOrder::getOrderCode, "%" + cssVoucherExport.getAwbNumber() + "%")));
                    List<IoOrder> orderList = ioOrderService.list(orderWrapper);
                    if (orderList.size() == 0) {
                        return null;
                    }
                    orderIds = orderList.stream().map(IoOrder::getOrderId).collect(Collectors.toList());
                }
                if (orderIds != null) {
                    LambdaQueryWrapper<CssCostWriteoffDetail> detailWrapper = Wrappers.<CssCostWriteoffDetail>lambdaQuery();
                    detailWrapper.eq(CssCostWriteoffDetail::getOrgId, SecurityUtils.getUser().getOrgId()).in(CssCostWriteoffDetail::getOrderId, orderIds);
                    List<CssCostWriteoffDetail> detailList = cssCostWriteoffDetailService.list(detailWrapper);
                    if (detailList.size() == 0) {
                        return null;
                    }
                    List<Integer> costWriteoffIds = detailList.stream().map(CssCostWriteoffDetail::getCostWriteoffId).distinct().collect(Collectors.toList());
                    wrapper.in(CssCostWriteoff::getCostWriteoffId, costWriteoffIds);
                }
            }
            wrapper.eq(CssCostWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostWriteoff::getBusinessScope, cssVoucherExport.getBusinessScope());
            if (StrUtil.isNotBlank(cssVoucherExport.getCoopName())) {
                wrapper.like(CssCostWriteoff::getCustomerName, "%" + cssVoucherExport.getCoopName() + "%");
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getDebitnoteNumber()) || StrUtil.isNotBlank(cssVoucherExport.getInvoiceNumber()) || cssVoucherExport.getInvoiceDateStart() != null || cssVoucherExport.getInvoiceDateEnd() != null) {
                LambdaQueryWrapper<CssPayment> paymentWrapper = Wrappers.<CssPayment>lambdaQuery();
                paymentWrapper.eq(CssPayment::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPayment::getBusinessScope, cssVoucherExport.getBusinessScope());
                if (StrUtil.isNotBlank(cssVoucherExport.getDebitnoteNumber())) {
                    paymentWrapper.like(CssPayment::getPaymentNum, cssVoucherExport.getDebitnoteNumber());
                }
                if (StrUtil.isNotBlank(cssVoucherExport.getInvoiceNumber())) {
                    paymentWrapper.like(CssPayment::getInvoiceNum, cssVoucherExport.getInvoiceNumber());
                }
                if (cssVoucherExport.getInvoiceDateStart() != null) {
                    paymentWrapper.ge(CssPayment::getInvoiceDate, cssVoucherExport.getInvoiceDateStart());
                }
                if (cssVoucherExport.getInvoiceDateEnd() != null) {
                    paymentWrapper.le(CssPayment::getInvoiceDate, cssVoucherExport.getInvoiceDateEnd());
                }
                List<Integer> paymentIds = cssPaymentService.list(paymentWrapper).stream().map(cssPayment -> cssPayment.getPaymentId()).collect(Collectors.toList());
                if (paymentIds.size() == 0) {
                    return null;
                }
                wrapper.in(CssCostWriteoff::getPaymentId, paymentIds);
            }
            if (cssVoucherExport.getWriteoffDateStart() != null) {
                wrapper.ge(CssCostWriteoff::getWriteoffDate, cssVoucherExport.getWriteoffDateStart());
            }
            if (cssVoucherExport.getWriteoffDateEnd() != null) {
                wrapper.le(CssCostWriteoff::getWriteoffDate, cssVoucherExport.getWriteoffDateEnd());
            }
            if (cssVoucherExport.getVoucherDateStart() != null) {
                wrapper.ge(CssCostWriteoff::getVoucherDate, cssVoucherExport.getVoucherDateStart());
            }
            if (cssVoucherExport.getVoucherDateEnd() != null) {
                wrapper.le(CssCostWriteoff::getVoucherDate, cssVoucherExport.getVoucherDateEnd());
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getWriteoffNumber())) {
                wrapper.like(CssCostWriteoff::getWriteoffNum, "%" + cssVoucherExport.getWriteoffNumber() + "%");
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getWriteoffCreatorName())) {
                wrapper.like(CssCostWriteoff::getCreatorName, cssVoucherExport.getWriteoffCreatorName());
            }
            if (cssVoucherExport.getVoucherStatus() != null && cssVoucherExport.getVoucherStatus()) {
                wrapper.isNotNull(CssCostWriteoff::getVoucherDate);
            }
            if (cssVoucherExport.getVoucherStatus() != null && !cssVoucherExport.getVoucherStatus()) {
                wrapper.isNull(CssCostWriteoff::getVoucherDate);
            }
            if (StrUtil.isNotBlank(cssVoucherExport.getVoucherCreatorName())) {
                wrapper.like(CssCostWriteoff::getVoucherCreatorName, cssVoucherExport.getVoucherCreatorName());
            }
            wrapper.orderByDesc(CssCostWriteoff::getCostWriteoffId);
            list = cssCostWriteoffService.list(wrapper).stream().map(cssCostWriteoff -> {
                CssVoucherExport voucherExport = new CssVoucherExport();
                voucherExport.setWriteoffId(cssCostWriteoff.getCostWriteoffId());
                if (cssCostWriteoff.getVoucherDate() != null) {
                    voucherExport.setVoucherDate(cssCostWriteoff.getVoucherDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                }
                voucherExport.setVoucherNumber(cssCostWriteoff.getVoucherNumber());
                voucherExport.setVoucherCreatorName(cssCostWriteoff.getVoucherCreatorName());
                voucherExport.setWriteoffNumber(cssCostWriteoff.getWriteoffNum());
                voucherExport.setWriteoffDate(cssCostWriteoff.getWriteoffDate());
                voucherExport.setCoopName(cssCostWriteoff.getCustomerName());
                voucherExport.setWriteoffAmount(cssCostWriteoff.getAmountWriteoff());
                voucherExport.setWriteoffAmountStr(FormatUtils.formatWithQWF(voucherExport.getWriteoffAmount(), 2));
                voucherExport.setCurrency(cssCostWriteoff.getCurrency());
                CssPayment cssPayment = cssPaymentService.getById(cssCostWriteoff.getPaymentId());
                if (cssPayment != null) {
                    voucherExport.setInvoiceDate(cssPayment.getInvoiceDate());
                    voucherExport.setInvoiceNumber(cssPayment.getInvoiceNum());
                    voucherExport.setInvoiceRemark(cssPayment.getInvoiceRemark());
                    voucherExport.setInvoiceTitle(cssPayment.getInvoiceTitle());
                }
                voucherExport.setFinancialAccountCode(StrUtil.isBlank(cssCostWriteoff.getFinancialAccountCode()) ? "" : cssCostWriteoff.getFinancialAccountCode());
                voucherExport.setFinancialAccountName(StrUtil.isBlank(cssCostWriteoff.getFinancialAccountName()) ? "" : cssCostWriteoff.getFinancialAccountName());
                voucherExport.setBankFinancialAccount(voucherExport.getFinancialAccountCode() + " " + voucherExport.getFinancialAccountName());
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
        if (cssVoucherExport.getType() == 0) {
            getList(cssVoucherExport).stream().forEach(order -> {
                total.setBusinessScope("合计:");
                if (total.getIncomeFunctionalAmount() == null) {
                    total.setIncomeFunctionalAmount(order.getIncomeFunctionalAmount() == null ? BigDecimal.ZERO : order.getIncomeFunctionalAmount());
                } else {
                    total.setIncomeFunctionalAmount(total.getIncomeFunctionalAmount().add(order.getIncomeFunctionalAmount() == null ? BigDecimal.ZERO : order.getIncomeFunctionalAmount()));
                }

            });
            total.setIncomeFunctionalAmountStr(total.getIncomeFunctionalAmount() == null ? "" : FormatUtils.formatWithQWFNoBit(total.getIncomeFunctionalAmount()));
        } else if (cssVoucherExport.getType() == 1) {
            getList(cssVoucherExport).stream().forEach(order -> {
                total.setBusinessScope("合计:");
                if (total.getCostFunctionalAmount() == null) {
                    total.setCostFunctionalAmount(order.getCostFunctionalAmount() == null ? BigDecimal.ZERO : order.getCostFunctionalAmount());
                } else {
                    total.setCostFunctionalAmount(total.getCostFunctionalAmount().add(order.getCostFunctionalAmount() == null ? BigDecimal.ZERO : order.getCostFunctionalAmount()));
                }
            });
            total.setCostFunctionalAmountStr(total.getCostFunctionalAmount() == null ? "" : FormatUtils.formatWithQWFNoBit(total.getCostFunctionalAmount()));
        } else if (cssVoucherExport.getType() == 2) {
            HashMap<String, BigDecimal> sumMap = new HashMap<>();
            getList(cssVoucherExport).stream().forEach(order -> {
                total.setBusinessScope("合计:");
                if (sumMap.get(order.getCurrency()) == null) {
                    sumMap.put(order.getCurrency(), order.getWriteoffAmount() == null ? BigDecimal.ZERO : order.getWriteoffAmount());
                } else {
                    sumMap.put(order.getCurrency(), sumMap.get(order.getCurrency()).add(order.getWriteoffAmount() == null ? BigDecimal.ZERO : order.getWriteoffAmount()));
                }
            });
            StringBuffer sumBuffer = new StringBuffer();
            for (Map.Entry<String, BigDecimal> entry : sumMap.entrySet()) {
                if (sumBuffer.length() == 0) {
                    sumBuffer.append(FormatUtils.formatWithQWF(entry.getValue(), 2)).append(" (").append(entry.getKey()).append(")");
                } else {
                    sumBuffer.append("  ").append(FormatUtils.formatWithQWF(entry.getValue(), 2)).append(" (").append(entry.getKey()).append(")");
                }
            }
            total.setWriteoffAmountStr(sumBuffer.toString());
        } else if (cssVoucherExport.getType() == 3) {
            HashMap<String, BigDecimal> sumMap = new HashMap<>();
            getList(cssVoucherExport).stream().forEach(order -> {
                total.setBusinessScope("合计:");
                if (sumMap.get(order.getCurrency()) == null) {
                    sumMap.put(order.getCurrency(), order.getWriteoffAmount() == null ? BigDecimal.ZERO : order.getWriteoffAmount());
                } else {
                    sumMap.put(order.getCurrency(), sumMap.get(order.getCurrency()).add(order.getWriteoffAmount() == null ? BigDecimal.ZERO : order.getWriteoffAmount()));
                }
            });
            StringBuffer sumBuffer = new StringBuffer();
            for (Map.Entry<String, BigDecimal> entry : sumMap.entrySet()) {
                if (sumBuffer.length() == 0) {
                    sumBuffer.append(FormatUtils.formatWithQWF(entry.getValue(), 2)).append(" (").append(entry.getKey()).append(")");
                } else {
                    sumBuffer.append("  ").append(FormatUtils.formatWithQWF(entry.getValue(), 2)).append(" (").append(entry.getKey()).append(")");
                }
            }
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
    public void voucherCallback(CssVoucherExport cssVoucherExport) {
        cssVoucherExport.setOrgId(SecurityUtils.getUser().getOrgId());
        cssVoucherExport.setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
        cssVoucherExport.setVoucherCreatorId(SecurityUtils.getUser().getId());
        Map<String, String> message = new HashMap<>();
        if (cssVoucherExport.getType() == 0) {
            StringBuffer sqlBuffer = new StringBuffer("INSERT INTO TEMP_order_sql VALUES");
            cssVoucherExport.getCheckedList().stream().forEach(voucher -> {
                sqlBuffer.append("(").append(voucher.getOrderId()).append(",'").append(voucher.getLockDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("',");
                if (voucher.getVoucherDate() != null) {
                    sqlBuffer.append("'" + voucher.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("',");
                } else {
                    sqlBuffer.append("null,");
                }
                if (StrUtil.isNotBlank(voucher.getVoucherNumber())) {
                    sqlBuffer.append(voucher.getVoucherNumber()).append(",");
                } else {
                    sqlBuffer.append("null,");
                }
                if (voucher.getCustomerId() != null) {
                    sqlBuffer.append(voucher.getCustomerId()).append("),");
                } else {
                    sqlBuffer.append("null),");
                }
            });
            String sql = sqlBuffer.replace(sqlBuffer.length() - 1, sqlBuffer.length(), ";").toString();
            System.out.println(sql);
            cssVoucherExport.setSql(sql);
            message = cssVoucherExportMapper.voucherCallbackForIncome(cssVoucherExport);
        } else if (cssVoucherExport.getType() == 1) {
            StringBuffer sqlBuffer = new StringBuffer("INSERT INTO TEMP_order_sql VALUES");
            cssVoucherExport.getCheckedList().stream().forEach(voucher -> {
                sqlBuffer.append("(").append(voucher.getOrderId()).append(",'").append(voucher.getLockDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("',");
                if (voucher.getVoucherDate() != null) {
                    sqlBuffer.append("'" + voucher.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("',");
                } else {
                    sqlBuffer.append("null,");
                }
                if (StrUtil.isNotBlank(voucher.getVoucherNumber())) {
                    sqlBuffer.append(voucher.getVoucherNumber()).append(",");
                } else {
                    sqlBuffer.append("null,");
                }
                if (voucher.getCustomerId() != null) {
                    sqlBuffer.append(voucher.getCustomerId()).append("),");
                } else {
                    sqlBuffer.append("null),");
                }
            });
            String sql = sqlBuffer.replace(sqlBuffer.length() - 1, sqlBuffer.length(), ";").toString();
            System.out.println(sql);
            cssVoucherExport.setSql(sql);
            message = cssVoucherExportMapper.voucherCallbackForCost(cssVoucherExport);
        } else if (cssVoucherExport.getType() == 2 || cssVoucherExport.getType() == 3) {
            StringBuffer sqlBuffer = new StringBuffer("INSERT INTO TEMP_writeoff_num_sql VALUES");
            cssVoucherExport.getCheckedList().stream().forEach(voucher -> {
                sqlBuffer.append("(").append(voucher.getWriteoffId()).append(",");
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
            if (cssVoucherExport.getType() == 2) {
                cssVoucherExport.setWriteoffType("income");
            } else {
                cssVoucherExport.setWriteoffType("cost");
            }
            message = cssVoucherExportMapper.voucherCallbackForWriteoff(cssVoucherExport);
        } else if (cssVoucherExport.getType() == 4) {
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
        }
        if (!message.get("ret_message").contains("成功")) {
            throw new RuntimeException(message.get("ret_message"));
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
    public void voucherGenerate(CssVoucherExport cssVoucherExport) {
        cssVoucherExport.setOrgId(SecurityUtils.getUser().getOrgId());
        cssVoucherExport.setVoucherCreatorName(SecurityUtils.getUser().buildOptName());
        cssVoucherExport.setVoucherCreatorId(SecurityUtils.getUser().getId());
        List list = new ArrayList();
        if (cssVoucherExport.getType() == 0) {
            StringBuffer sqlBuffer = new StringBuffer("INSERT INTO TEMP_order_sql VALUES");
            cssVoucherExport.getCheckedList().stream().forEach(voucher -> {
                sqlBuffer.append("(").append(voucher.getOrderId()).append(",'").append(voucher.getLockDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("',");
                if (voucher.getVoucherDate() != null) {
                    sqlBuffer.append("'" + voucher.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("',");
                } else {
                    sqlBuffer.append("null,");
                }
                if (StrUtil.isNotBlank(voucher.getVoucherNumber())) {
                    sqlBuffer.append(voucher.getVoucherNumber()).append(",");
                } else {
                    sqlBuffer.append("null,");
                }
                if (voucher.getCustomerId() != null) {
                    sqlBuffer.append(voucher.getCustomerId()).append("),");
                } else {
                    sqlBuffer.append("null),");
                }
            });
            String sql = sqlBuffer.replace(sqlBuffer.length() - 1, sqlBuffer.length(), ";").toString();
            System.out.println(sql);
            cssVoucherExport.setSql(sql);
            list = cssVoucherExportMapper.voucherGenerateForIncome(cssVoucherExport);

        } else if (cssVoucherExport.getType() == 1) {
            StringBuffer sqlBuffer = new StringBuffer("INSERT INTO TEMP_order_sql VALUES");
            cssVoucherExport.getCheckedList().stream().forEach(voucher -> {
                sqlBuffer.append("(").append(voucher.getOrderId()).append(",'").append(voucher.getLockDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("',");
                if (voucher.getVoucherDate() != null) {
                    sqlBuffer.append("'" + voucher.getVoucherDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("',");
                } else {
                    sqlBuffer.append("null,");
                }
                if (StrUtil.isNotBlank(voucher.getVoucherNumber())) {
                    sqlBuffer.append(voucher.getVoucherNumber()).append(",");
                } else {
                    sqlBuffer.append("null,");
                }
                if (voucher.getCustomerId() != null) {
                    sqlBuffer.append(voucher.getCustomerId()).append("),");
                } else {
                    sqlBuffer.append("null),");
                }
            });
            String sql = sqlBuffer.replace(sqlBuffer.length() - 1, sqlBuffer.length(), ";").toString();
            System.out.println(sql);
            cssVoucherExport.setSql(sql);
            list = cssVoucherExportMapper.voucherGenerateForCost(cssVoucherExport);

        } else if (cssVoucherExport.getType() == 2 || cssVoucherExport.getType() == 3) {
            StringBuffer sqlBuffer = new StringBuffer("INSERT INTO TEMP_writeoff_num_sql VALUES");
            cssVoucherExport.getCheckedList().stream().forEach(voucher -> {
                sqlBuffer.append("(").append(voucher.getWriteoffId()).append(",");
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
            if (cssVoucherExport.getType() == 2) {
                cssVoucherExport.setWriteoffType("income");
            } else {
                cssVoucherExport.setWriteoffType("cost");
            }
            list = cssVoucherExportMapper.voucherGenerateForWriteoff(cssVoucherExport);
        } else if (cssVoucherExport.getType() == 4) {
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
            list = cssVoucherExportMapper.voucherGenerateForExpenseReport(cssVoucherExport);
        }
        if (list.size() == 1) {
            Map<String, String> errorMess = (HashMap<String, String>) list.get(0);
            System.out.println(errorMess.get("ret_message"));
            throw new RuntimeException(errorMess.get("ret_message"));
        }
        List<Map<String, Object>> result = (ArrayList<Map<String, Object>>) list.get(0);
        HashMap<String, Object> map = new HashMap<>();
        map.put("data", result);
        if ("金蝶".equals(((ArrayList<Map<String, Object>>) list.get(1)).get(0).get("voucher_out_type"))) {
            JxlsUtils.exportExcelWithLocalModel(FilePathUtils.filePath + "/PDFtemplate/voucher_export_template_kingdee.xlsx", map);
        } else if ("用友".equals(((ArrayList<Map<String, Object>>) list.get(1)).get(0).get("voucher_out_type"))) {
            JxlsUtils.exportExcelWithLocalModel(FilePathUtils.filePath + "/PDFtemplate/voucher_export_template_yonyou.xlsx", map);
        }
    }
}
