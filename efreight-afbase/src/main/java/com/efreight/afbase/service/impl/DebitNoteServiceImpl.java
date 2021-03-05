package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.dao.AfOrderMapper;
import com.efreight.afbase.dao.CssIncomeInvoiceDetailMapper;
import com.efreight.afbase.dao.DebitNoteMapper;
import com.efreight.afbase.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.utils.FieldValUtils;
import com.efreight.afbase.utils.LoginUtils;
import com.efreight.afbase.utils.PDFUtils;
import com.efreight.common.core.feign.RemoteCoopService;
import com.efreight.common.core.feign.RemoteServiceToHRS;
import com.efreight.common.core.feign.RemoteServiceToSC;
import com.efreight.common.core.jms.MailSendService;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.JxlsUtils;
import com.efreight.common.security.vo.CoopVo;
import com.efreight.common.security.vo.OrgVo;
import com.efreight.common.security.util.SecurityUtils;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.jxls.util.Util;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 清单 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2019-11-07
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DebitNoteServiceImpl extends ServiceImpl<DebitNoteMapper, DebitNote> implements DebitNoteService {
    private final LogService logService;
    private final ScLogService scLogService;
    private final AfOrderMapper afOrderMapper;
    private final AfOrderService afOrderService;
    private final CssIncomeInvoiceDetailWriteoffService cssIncomeInvoiceDetailWriteoffService;
    private final CssIncomeInvoiceDetailService cssIncomeInvoiceDetailService;
    private final RemoteServiceToHRS remoteServiceToHRS;

    private final RemoteServiceToSC remoteServiceToSC;

    private final RemoteCoopService remoteCoopService;

    private final AfIncomeService afIncomeService;

    private final ServiceService serviceService;

    private final MailSendService mailSendService;

    private final CssDebitNoteCurrencyService cssDebitNoteCurrencyService;

    private final CssIncomeWriteoffService cssIncomeWriteoffService;

    private final OrderFilesService orderFilesService;
    private final ScOrderFilesService scOrderFilesService;

    private final TcLogService tcLogService;


    private final TcOrderFilesService tcOrderFilesService;

    private final LcLogService lcLogService;
    private final IoLogService ioLogService;
    private final CssIncomeInvoiceDetailMapper detailMapper;


    @Override
    public IPage getPage2(Page page, DebitNote debitNote) {

        debitNote.setOrgId(SecurityUtils.getUser().getOrgId());
        //状态多选
        String billStatus[] = debitNote.getBillStatus().split(",");
        for (int i = 0; i < billStatus.length; i++) {
            if ("已制账单".equals(billStatus[i])) {
                debitNote.setBillStatus1("已制账单");
            }
            if ("已制清单".equals(billStatus[i])) {
                debitNote.setBillStatus2("已制清单");
            }
            if ("核销完毕".equals(billStatus[i])) {
                debitNote.setBillStatus3("核销完毕");
            }
            if ("部分核销".equals(billStatus[i])) {
                debitNote.setBillStatus4("部分核销");
            }
            if ("开票完毕".equals(billStatus[i])) {
                debitNote.setBillStatus5("开票完毕");
            }
            if ("部分开票".equals(billStatus[i])) {
                debitNote.setBillStatus6("部分开票");
            }
            if ("待开票".equals(billStatus[i])) {
                debitNote.setBillStatus7("待开票");
            }
        }
        if (!StringUtils.isEmpty(debitNote.getServiceIdStr())) {
            debitNote.setServiceIdStr("'" + debitNote.getServiceIdStr().replaceAll(",", "','") + "'");
        }
        if (!debitNote.getInvoiceDateStart().isEmpty() || !debitNote.getInvoiceDateEnd().isEmpty()
                || !debitNote.getInvoiceNum().isEmpty() || !debitNote.getInvoiceTitle().isEmpty()) {
            debitNote.setInvoiceQuery(1);
        }
        IPage<DebitNote> iPage = baseMapper.getPage(page, debitNote);
        HashMap<String, List<DebitNote>> map = new HashMap<>();
        HashMap<String, BigDecimal> mapAmount = new HashMap<>();
        HashMap<String, BigDecimal> mapWOff = new HashMap<>();
        HashMap<String, HashMap<String, BigDecimal>> mapBuffer = new HashMap<>();
        HashMap<String, HashMap<String, BigDecimal>> mapBuffer2 = new HashMap<>();
        iPage.getRecords().stream().forEach(one -> {
            //是否核销
            //根据清单号查询核销单号
//            List<CssIncomeWriteoff> cssIncomeWriteoffList = new ArrayList<>();
//            if (one.getStatementId() != null) {
//                LambdaQueryWrapper<CssIncomeWriteoff> cssIncomeWriteoffLambdaQueryWrapper1 = Wrappers.<CssIncomeWriteoff>lambdaQuery();
//                cssIncomeWriteoffLambdaQueryWrapper1.eq(CssIncomeWriteoff::getStatementId, one.getStatementId()).eq(CssIncomeWriteoff::getOrgId, SecurityUtils.getUser().getOrgId());
//                List<CssIncomeWriteoff> cssIncomeWriteoffList1 = cssIncomeWriteoffService.list(cssIncomeWriteoffLambdaQueryWrapper1);
//                if (!cssIncomeWriteoffList1.isEmpty()) {
//                    one.setIfWriteoff(true);
//                } else {
//                    one.setIfWriteoff(false);
//                }
//                LambdaQueryWrapper<CssIncomeWriteoffStatementDetail> cssIncomeWriteoffStatementDetailWrapper = Wrappers.<CssIncomeWriteoffStatementDetail>lambdaQuery();
//                cssIncomeWriteoffStatementDetailWrapper.eq(CssIncomeWriteoffStatementDetail::getDebitNoteId, one.getDebitNoteId());
//                List<Integer> incomeWriteoffIds = cssIncomeWriteoffStatementDetailService.list(cssIncomeWriteoffStatementDetailWrapper).stream().map(CssIncomeWriteoffStatementDetail::getIncomeWriteoffId).collect(Collectors.toList());
//                if (!incomeWriteoffIds.isEmpty()) {
//                    LambdaQueryWrapper<CssIncomeWriteoff> cssIncomeWriteoffWrapper = Wrappers.<CssIncomeWriteoff>lambdaQuery();
//                    cssIncomeWriteoffWrapper.eq(CssIncomeWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).in(CssIncomeWriteoff::getIncomeWriteoffId, incomeWriteoffIds);
//                    cssIncomeWriteoffList = cssIncomeWriteoffService.list(cssIncomeWriteoffWrapper);
//                }
//
//            } else {
//                LambdaQueryWrapper<CssIncomeWriteoff> cssIncomeWriteoffLambdaQueryWrapper = Wrappers.<CssIncomeWriteoff>lambdaQuery();
//                cssIncomeWriteoffLambdaQueryWrapper.eq(CssIncomeWriteoff::getDebitNoteId, one.getDebitNoteId()).eq(CssIncomeWriteoff::getOrgId, SecurityUtils.getUser().getOrgId());
//                cssIncomeWriteoffList = cssIncomeWriteoffService.list(cssIncomeWriteoffLambdaQueryWrapper);
//                if (cssIncomeWriteoffList.size() > 0) {
//                    one.setIfWriteoff(true);
//                } else {
//                    one.setIfWriteoff(false);
//                }
//            }


            //发票需求变更  
            if (one.getInvoiceStatus() != null && (one.getInvoiceStatus() == 1 || one.getInvoiceStatus() == 0)) {
                one.setIfWriteoff(true);
            } else {
                one.setIfWriteoff(false);
            }
            if (one.getStatementInvoiceStatus() != null && (one.getStatementInvoiceStatus() == 1 || one.getStatementInvoiceStatus() == 0)) {
                one.setIfWriteoff(true);
            } else {
                one.setIfWriteoff(false);
            }

            StringBuffer writeoffNumbuffer = new StringBuffer("");
            StringBuffer invoiceNumbuffer = new StringBuffer("");
            if (one.getStatementId() != null) {
                //清单核销号
                LambdaQueryWrapper<CssIncomeInvoiceDetailWriteoff> cssIncomeInvoiceDetailWriteoffWrapper = Wrappers.<CssIncomeInvoiceDetailWriteoff>lambdaQuery();
                cssIncomeInvoiceDetailWriteoffWrapper.eq(CssIncomeInvoiceDetailWriteoff::getStatementId, one.getStatementId()).eq(CssIncomeInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId());
                List<CssIncomeInvoiceDetailWriteoff> listCssIncomeInvoiceDetailWriteoff = cssIncomeInvoiceDetailWriteoffService.list(cssIncomeInvoiceDetailWriteoffWrapper);
                if (listCssIncomeInvoiceDetailWriteoff != null && listCssIncomeInvoiceDetailWriteoff.size() > 0) {
                    for (int i = 0; i < listCssIncomeInvoiceDetailWriteoff.size(); i++) {
                        CssIncomeInvoiceDetailWriteoff p = listCssIncomeInvoiceDetailWriteoff.get(i);
                        writeoffNumbuffer.append(p.getInvoiceDetailWriteoffId());
                        writeoffNumbuffer.append(" ");
                        writeoffNumbuffer.append(p.getWriteoffNum());
                        writeoffNumbuffer.append("  ");
                    }
                }
                //清单发票号
                LambdaQueryWrapper<CssIncomeInvoiceDetail> cssIncomeInvoiceDetailWrapper = Wrappers.<CssIncomeInvoiceDetail>lambdaQuery();
                cssIncomeInvoiceDetailWrapper.eq(CssIncomeInvoiceDetail::getStatementId, one.getStatementId()).eq(CssIncomeInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId());
                List<CssIncomeInvoiceDetail> listCssIncomeInvoiceDetail = cssIncomeInvoiceDetailService.list(cssIncomeInvoiceDetailWrapper);
                if (listCssIncomeInvoiceDetail != null && listCssIncomeInvoiceDetail.size() > 0) {
                    for (int i = 0; i < listCssIncomeInvoiceDetail.size(); i++) {
                        CssIncomeInvoiceDetail k = listCssIncomeInvoiceDetail.get(i);
                        invoiceNumbuffer.append(k.getInvoiceDetailId());
                        invoiceNumbuffer.append(" ");
                        invoiceNumbuffer.append(k.getInvoiceNum());
                        invoiceNumbuffer.append("  ");
                    }
                }
            } else {
                //账单核销号
                LambdaQueryWrapper<CssIncomeInvoiceDetailWriteoff> cssIncomeInvoiceDetailWriteoffWrapper = Wrappers.<CssIncomeInvoiceDetailWriteoff>lambdaQuery();
                cssIncomeInvoiceDetailWriteoffWrapper.eq(CssIncomeInvoiceDetailWriteoff::getDebitNoteId, one.getDebitNoteId()).eq(CssIncomeInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId());
                List<CssIncomeInvoiceDetailWriteoff> listCssIncomeInvoiceDetailWriteoff = cssIncomeInvoiceDetailWriteoffService.list(cssIncomeInvoiceDetailWriteoffWrapper);
                if (listCssIncomeInvoiceDetailWriteoff != null && listCssIncomeInvoiceDetailWriteoff.size() > 0) {
                    for (int i = 0; i < listCssIncomeInvoiceDetailWriteoff.size(); i++) {
                        CssIncomeInvoiceDetailWriteoff p = listCssIncomeInvoiceDetailWriteoff.get(i);
                        writeoffNumbuffer.append(p.getInvoiceDetailWriteoffId());
                        writeoffNumbuffer.append(" ");
                        writeoffNumbuffer.append(p.getWriteoffNum());
                        writeoffNumbuffer.append("  ");
                    }
                }
                //账单发票号
                LambdaQueryWrapper<CssIncomeInvoiceDetail> cssIncomeInvoiceDetailWrapper = Wrappers.<CssIncomeInvoiceDetail>lambdaQuery();
                cssIncomeInvoiceDetailWrapper.eq(CssIncomeInvoiceDetail::getDebitNoteId, one.getDebitNoteId()).eq(CssIncomeInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId());
                List<CssIncomeInvoiceDetail> listCssIncomeInvoiceDetail = cssIncomeInvoiceDetailService.list(cssIncomeInvoiceDetailWrapper);
                if (listCssIncomeInvoiceDetail != null && listCssIncomeInvoiceDetail.size() > 0) {
                    for (int i = 0; i < listCssIncomeInvoiceDetail.size(); i++) {
                        CssIncomeInvoiceDetail k = listCssIncomeInvoiceDetail.get(i);
                        invoiceNumbuffer.append(k.getInvoiceDetailId());
                        invoiceNumbuffer.append(" ");
                        invoiceNumbuffer.append(k.getInvoiceNum());
                        invoiceNumbuffer.append("  ");
                    }
                }
            }
            one.setWriteoffNum(writeoffNumbuffer.toString());
            one.setInvoiceNum(invoiceNumbuffer.toString());

            //操作信息
            if (StrUtil.isBlank(one.getEditorName())) {
                one.setEditorName(one.getCreatorName());
            }
            if (one.getEditTime() == null) {
                one.setEditTime(one.getCreateTime());
            }

            //状态
            if (one.getWriteoffComplete() != null && one.getWriteoffComplete() == 1) {
                one.setDebitNoteStatus("核销完毕");
            } else if (one.getWriteoffComplete() != null && one.getWriteoffComplete() == 0) {
                one.setDebitNoteStatus("部分核销");
            } else {
                if (one.getInvoiceStatus() != null && one.getInvoiceStatus() == 1) {
                    one.setDebitNoteStatus("开票完毕");
                } else if (one.getInvoiceStatus() != null && one.getInvoiceStatus() == 0) {
                    one.setDebitNoteStatus("部分开票");
                } else if (one.getInvoiceStatus() != null && one.getInvoiceStatus() == -1) {
                    one.setDebitNoteStatus("待开票");
                } else if (one.getInvoiceStatus() != null && one.getInvoiceStatus() == 1) {
                    one.setDebitNoteStatus("开票完毕");
                } else if (one.getInvoiceStatus() != null && one.getInvoiceStatus() == 0) {
                    one.setDebitNoteStatus("部分开票");
                } else if (one.getInvoiceStatus() != null && one.getInvoiceStatus() == -1) {
                    one.setDebitNoteStatus("待开票");
                } else {
                    if (one.getStatementId() != null) {
                        one.setDebitNoteStatus("已制清单");
                    } else {
                        one.setDebitNoteStatus("已制账单");
                    }
                }
            }
            //账单金额实现多币种显示
            LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
            cssDebitCurrencyWrapper.eq(CssDebitNoteCurrency::getDebitNoteId, one.getDebitNoteId()).eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
            List<CssDebitNoteCurrency> currencyList = cssDebitNoteCurrencyService.list(cssDebitCurrencyWrapper);
            StringBuffer buffer3 = new StringBuffer("");
            StringBuffer buffer = new StringBuffer();
            StringBuffer buffer2 = new StringBuffer("");
            HashMap<String, BigDecimal> currencyAmount = new HashMap<>();//账单金额（原币）
            HashMap<String, BigDecimal> currencyAmount2 = new HashMap<>();//已核销金额（原币）
            currencyList.stream().forEach(currency -> {
                if (currencyAmount.containsKey(currency.getCurrency())) {
                    currencyAmount.put(currency.getCurrency(), currencyAmount.get(currency.getCurrency()).add(currency.getAmount()));
                } else {
                    if (buffer3.toString().isEmpty()) {
                        buffer3.append(currency.getCurrency());
                    } else {
                        if (!buffer3.toString().contains(currency.getCurrency())) {
                            buffer3.append(",").append(currency.getCurrency());
                        }
                    }
                    currencyAmount.put(currency.getCurrency(), currency.getAmount());
                }
                String amount = new DecimalFormat("###,###.00").format(currency.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP));
                if (amount.startsWith(".")) {
                    amount = "0" + amount;
                }
                buffer.append(amount).append(" (").append(currency.getCurrency()).append(")  ");
                if (currency.getAmountWriteoff() != null) {
                    if (currencyAmount2.containsKey(currency.getCurrency())) {
                        currencyAmount2.put(currency.getCurrency(), currencyAmount2.get(currency.getCurrency()).add(currency.getAmountWriteoff()));
                    } else {
                        currencyAmount2.put(currency.getCurrency(), currency.getAmountWriteoff());
                    }
                    String amount1 = new DecimalFormat("###,###.00").format(currency.getAmountWriteoff().setScale(2, BigDecimal.ROUND_HALF_UP));
                    if (amount1.startsWith(".")) {
                        amount1 = "0" + amount1;
                    }

                    buffer2.append(amount1).append(" (").append(currency.getCurrency()).append(")  ");
                }

            });

            one.setMapAmountOne(JSONObject.toJSONString(currencyAmount));
            one.setCurrencyAmount(buffer.toString());
            one.setCurrencyAmount2(buffer2.toString());
            one.setCurrencyStr(buffer3.toString());

            //封装树结构
            String currency = "";
            if (StrUtil.isNotBlank(one.getCurrency())) {
                currency = one.getCurrency();
            }
            String amountTaxRateStr = "";
            if (one.getAmountTaxRate() != null) {
                amountTaxRateStr = one.getAmountTaxRate().toString();
            }

//            String key = one.getCustomerId() + "$%$" + currency + "$%$" + amountTaxRateStr;
            String key = one.getCustomerId() + "$%$" + amountTaxRateStr;
            if (map.get(key) == null) {
                ArrayList<DebitNote> debitNotes = new ArrayList<>();
                debitNotes.add(one);
                map.put(key, debitNotes);
            } else {
                map.get(key).add(one);
            }

            //账单金额本币
            if (mapAmount.containsKey(key)) {
                mapAmount.put(key, mapAmount.get(key).add(one.getFunctionalAmount()));
            } else {
                mapAmount.put(key, one.getFunctionalAmount());
            }
            //核销金额 本币
            if (one.getFunctionalAmountWriteoff() != null) {
                if (mapWOff.containsKey(key)) {
                    mapWOff.put(key, mapWOff.get(key).add(one.getFunctionalAmountWriteoff()));
                } else {
                    mapWOff.put(key, one.getFunctionalAmountWriteoff());
                }
            }

            //账单金额原币
            if (mapBuffer.containsKey(key)) {
                if (currencyAmount != null && !currencyAmount.isEmpty()) {
                    for (Map.Entry<String, BigDecimal> entry : currencyAmount.entrySet()) {
                        if (mapBuffer.get(key).containsKey(entry.getKey())) {

                            mapBuffer.get(key).put(entry.getKey(), mapBuffer.get(key).get(entry.getKey()).add(entry.getValue()));
                        } else {
                            mapBuffer.get(key).put(entry.getKey(), entry.getValue());
                        }

                    }
                }
            } else {
                mapBuffer.put(key, currencyAmount);
            }
            //核销金额原币
            if (mapBuffer2.containsKey(key)) {
                if (currencyAmount2 != null && !currencyAmount2.isEmpty()) {
                    for (Map.Entry<String, BigDecimal> entry : currencyAmount2.entrySet()) {
                        if (mapBuffer2.get(key).containsKey(entry.getKey())) {
                            mapBuffer2.get(key).put(entry.getKey(), mapBuffer2.get(key).get(entry.getKey()).add(entry.getValue()));
                        } else {
                            mapBuffer2.get(key).put(entry.getKey(), entry.getValue());
                        }

                    }
                }
            } else {
                if (currencyAmount2 != null && !currencyAmount2.isEmpty()) {
                    mapBuffer2.put(key, currencyAmount2);
                }
            }
        });

        List<DebitNoteTree> result = new ArrayList<>();
        for (Map.Entry<String, List<DebitNote>> entry : map.entrySet()) {
            DebitNoteTree debitNoteTree = new DebitNoteTree();
            debitNoteTree.setDebitNoteId("A" + entry.getValue().get(0).getDebitNoteId());
            debitNoteTree.setAmountTaxRate(entry.getValue().get(0).getAmountTaxRate());
            debitNoteTree.setCustomerName(entry.getValue().get(0).getCustomerName());
            debitNoteTree.setCurrency(entry.getValue().get(0).getCurrency());
            debitNoteTree.setChildren(entry.getValue());
            StringBuffer buffer3 = new StringBuffer();
            StringBuffer buffer4 = new StringBuffer();
            if (mapBuffer.containsKey(entry.getKey())) {
                if (mapBuffer.get(entry.getKey()) != null && !mapBuffer.get(entry.getKey()).isEmpty()) {
                    for (Map.Entry<String, BigDecimal> entryT : mapBuffer.get(entry.getKey()).entrySet()) {
                        buffer3.append(new DecimalFormat("###,##0.00").format(entryT.getValue().setScale(2, BigDecimal.ROUND_HALF_UP))).append(" (").append(entryT.getKey()).append(")  ");
                    }
                }
            }
            if (mapBuffer2.containsKey(entry.getKey())) {
                if (mapBuffer2.get(entry.getKey()) != null && !mapBuffer2.get(entry.getKey()).isEmpty()) {
                    for (Map.Entry<String, BigDecimal> entryT : mapBuffer2.get(entry.getKey()).entrySet()) {
                        buffer4.append(new DecimalFormat("###,##0.00").format(entryT.getValue().setScale(2, BigDecimal.ROUND_HALF_UP))).append(" (").append(entryT.getKey()).append(")  ");
                    }
                }
            }
            debitNoteTree.setFunctionalAmount(mapAmount.get(entry.getKey()));
            debitNoteTree.setFunctionalAmountWriteoff(mapWOff.get(entry.getKey()));
            debitNoteTree.setCurrencyAmount(buffer3.toString());
            debitNoteTree.setCurrencyAmount2(buffer4.toString());
            result.add(debitNoteTree);
        }
        page.setTotal(iPage.getTotal());
        page.setRecords(result);
        return page;
    }

    @Override
    public IPage getPage(Page page, DebitNote debitNote) {
        LambdaQueryWrapper<DebitNote> wrapper = Wrappers.<DebitNote>lambdaQuery();
        wrapper.eq(DebitNote::getOrgId, SecurityUtils.getUser().getOrgId());

        //编辑关联订单条件
        boolean flag = false;
        LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
        orderWrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId());
        if (StrUtil.isNotBlank(debitNote.getAwbNumber())) {
            flag = true;
            orderWrapper.like(AfOrder::getAwbNumber, "%" + debitNote.getAwbNumber() + "%");
        }

        if (StrUtil.isNotBlank(debitNote.getOrderCode())) {
            flag = true;
            orderWrapper.like(AfOrder::getOrderCode, "%" + debitNote.getOrderCode() + "%");
        }

        if (debitNote.getFlightDateStart() != null) {
            flag = true;
            orderWrapper.ge(AfOrder::getExpectDeparture, debitNote.getFlightDateStart());
        }

        if (debitNote.getFlightDateEnd() != null) {
            flag = true;
            orderWrapper.le(AfOrder::getExpectDeparture, debitNote.getFlightDateEnd());
        }

        if (StrUtil.isNotBlank(debitNote.getCustomerNumber())) {
            flag = true;
            orderWrapper.like(AfOrder::getCustomerNumber, "%" + debitNote.getCustomerNumber() + "%");
        }
        if (flag) {
            List<Integer> orderIds = afOrderService.list(orderWrapper).stream().map(AfOrder::getOrderId).collect(Collectors.toList());
            if (orderIds.size() == 0) {
                page.setTotal(0);
                page.setRecords(new ArrayList());
                return page;
            }
            wrapper.in(DebitNote::getOrderId, orderIds);
        }
        //编辑账单查询条件
        if (StrUtil.isNotBlank(debitNote.getBusinessScope())) {
            wrapper.eq(DebitNote::getBusinessScope, debitNote.getBusinessScope());
        }

        if (StrUtil.isNotBlank(debitNote.getDebitNoteNum())) {
            wrapper.like(DebitNote::getDebitNoteNum, "%" + debitNote.getDebitNoteNum() + "%");
        }


        if (StrUtil.isNotBlank(debitNote.getCustomerName())) {
            wrapper.like(DebitNote::getCustomerName, "%" + debitNote.getCustomerName() + "%");
        }

        if (debitNote.getDebitNoteDateStart() != null) {
            wrapper.ge(DebitNote::getDebitNoteDate, debitNote.getDebitNoteDateStart());
        }

        if (debitNote.getDebitNoteDateEnd() != null) {
            wrapper.le(DebitNote::getDebitNoteDate, debitNote.getDebitNoteDateEnd());
        }


        if (StrUtil.isNotBlank(debitNote.getDebitNoteStatus())) {
            if (!debitNote.getDebitNoteStatus().contains("全部")) {
                StringBuffer lastSql = new StringBuffer();
                lastSql.append(" and(");
                if (debitNote.getDebitNoteStatus().contains("发票开具")) {
                    lastSql.append(" invoice_id is not null or");
                }
                if (debitNote.getDebitNoteStatus().contains("制作清单")) {
                    lastSql.append(" statement_id is not null or");
                }
                if (debitNote.getDebitNoteStatus().contains("制作账单")) {
                    lastSql.append("(invoice_id is null and statement_id is null)");
                }
                lastSql.append(")");
                String sql = lastSql.toString();
                if (lastSql.toString().endsWith("or)")) {
                    sql = lastSql.substring(0, lastSql.length() - 3) + ")";
                }
                log.info(sql);
                if (!" and()".endsWith(sql)) {
                    wrapper.last(sql);
                }
            }
        }

        IPage<DebitNote> iPage = baseMapper.selectPage(page, wrapper);
        HashMap<String, List<DebitNote>> map = new HashMap<>();
        iPage.getRecords().stream().forEach(one -> {

            //填写订单相关信息
            AfOrder order = afOrderService.getOrderById(one.getOrderId(), null);
            if (order != null) {
                one.setAwbNumber(order.getAwbNumber());
                one.setOrderCode(order.getOrderCode());
                one.setCustomerNumber(order.getCustomerNumber());
                one.setFlightDate(order.getExpectDeparture());
            }

            //是否核销
            LambdaQueryWrapper<CssIncomeWriteoff> cssIncomeWriteoffLambdaQueryWrapper = Wrappers.<CssIncomeWriteoff>lambdaQuery();
            cssIncomeWriteoffLambdaQueryWrapper.eq(CssIncomeWriteoff::getDebitNoteId, one.getDebitNoteId()).eq(CssIncomeWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).last("limit 1");
            CssIncomeWriteoff cssIncomeWriteoff = cssIncomeWriteoffService.getOne(cssIncomeWriteoffLambdaQueryWrapper);
            if (cssIncomeWriteoff == null) {
                one.setIfWriteoff(false);
            } else {
                one.setIfWriteoff(true);
            }

            //操作信息
            if (StrUtil.isBlank(one.getEditorName())) {
                one.setEditorName(one.getCreatorName());
            }
            if (one.getEditTime() == null) {
                one.setEditTime(one.getCreateTime());
            }

            //状态
            if (one.getInvoiceId() != null) {
                one.setDebitNoteStatus("发票开具");
            } else {
                if (one.getStatementId() == null) {
                    one.setDebitNoteStatus("制作账单");
                } else {
                    one.setDebitNoteStatus("制作清单");
                }
            }
            //账单金额实现多币种显示
            LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
            cssDebitCurrencyWrapper.eq(CssDebitNoteCurrency::getDebitNoteId, one.getDebitNoteId()).eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
            List<CssDebitNoteCurrency> currencyList = cssDebitNoteCurrencyService.list(cssDebitCurrencyWrapper);
            StringBuffer buffer = new StringBuffer();
            currencyList.stream().forEach(currency -> {
                buffer.append(new DecimalFormat("###,###.00").format(currency.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP))).append(" (").append(currency.getCurrency()).append(")  ");
            });

            one.setCurrencyAmount(buffer.toString());

            //封装树结构
            String currency = "";
            if (StrUtil.isNotBlank(one.getCurrency())) {
                currency = one.getCurrency();
            }
            String amountTaxRateStr = "";
            if (one.getAmountTaxRate() != null) {
                amountTaxRateStr = one.getAmountTaxRate().toString();
            }

//            String key = one.getCustomerId() + "$%$" + currency + "$%$" + amountTaxRateStr;
            String key = one.getCustomerId() + "$%$" + amountTaxRateStr;
            if (map.get(key) == null) {
                ArrayList<DebitNote> debitNotes = new ArrayList<>();
                debitNotes.add(one);
                map.put(key, debitNotes);
            } else {
                map.get(key).add(one);
            }
        });

        List<DebitNoteTree> result = new ArrayList<>();
        for (Map.Entry<String, List<DebitNote>> entry : map.entrySet()) {
            DebitNoteTree debitNoteTree = new DebitNoteTree();
            debitNoteTree.setDebitNoteId("A" + entry.getValue().get(0).getDebitNoteId());
            debitNoteTree.setAmountTaxRate(entry.getValue().get(0).getAmountTaxRate());
            debitNoteTree.setCustomerName(entry.getValue().get(0).getCustomerName());
            debitNoteTree.setCurrency(entry.getValue().get(0).getCurrency());
            debitNoteTree.setChildren(entry.getValue());
            result.add(debitNoteTree);
        }
        page.setTotal(iPage.getTotal());
        page.setRecords(result);
        return page;
    }

    @Override
    public List<DebitNote> selectOperation1(DebitNote debitNote) {

        debitNote.setOrgId(SecurityUtils.getUser().getOrgId());
        List<DebitNote> list = baseMapper.selectOperation(debitNote);
//        HashMap<String, List<DebitNote>> map = new HashMap<>();
//        list.stream().forEach(one -> {
//
//            if (one.getInvoiceId() != null) {
//                one.setDebitNoteStatus("发票开具");
//            } else {
//                if (one.getStatementId() == null) {
//                    one.setDebitNoteStatus("制作账单");
//                } else {
//                    one.setDebitNoteStatus("制作清单");
//                }
//            }
//            //账单金额实现多币种显示
//            LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
//            cssDebitCurrencyWrapper.eq(CssDebitNoteCurrency::getDebitNoteId, one.getDebitNoteId()).eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
//            List<CssDebitNoteCurrency> currencyList = cssDebitNoteCurrencyService.list(cssDebitCurrencyWrapper);
//            StringBuffer buffer = new StringBuffer();
//            currencyList.stream().forEach(currency -> {
//                buffer.append(new DecimalFormat("###,###.00").format(currency.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP))).append(" (").append(currency.getCurrency()).append(")  ");
//            });
//
//            one.setCurrencyAmount(buffer.toString());
//            
//      
//        });

        return list;
    }

    @Override
    public List<DebitNote> selectOperation2(DebitNote debitNote) {

        debitNote.setOrgId(SecurityUtils.getUser().getOrgId());
        List<DebitNote> list = baseMapper.selectOperation2(debitNote);

        List<DebitNote> list2 = new ArrayList<DebitNote>();
        if (list.size() > 0) {
            list2.add(list.get(0));
        }
        return list2;
    }

    @Override
    public List<DebitNoteTree> selectOperation(DebitNote debitNote) {


        debitNote.setOrgId(SecurityUtils.getUser().getOrgId());
        List<DebitNote> list = baseMapper.selectOperation(debitNote);
        HashMap<String, List<DebitNote>> map = new HashMap<>();
        list.stream().forEach(one -> {
            //是否核销
            LambdaQueryWrapper<CssIncomeWriteoff> cssIncomeWriteoffLambdaQueryWrapper = Wrappers.<CssIncomeWriteoff>lambdaQuery();
            cssIncomeWriteoffLambdaQueryWrapper.eq(CssIncomeWriteoff::getDebitNoteId, one.getDebitNoteId()).eq(CssIncomeWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).last("limit 1");
            CssIncomeWriteoff cssIncomeWriteoff = cssIncomeWriteoffService.getOne(cssIncomeWriteoffLambdaQueryWrapper);
            if (cssIncomeWriteoff == null) {
                one.setIfWriteoff(false);
            } else {
                one.setIfWriteoff(true);
            }

            //设置账单状态
            if (one.getStatementId() != null && !"".equals(one.getStatementId())) {
                one.setDebitNoteStatus("已制清单");
            } else {
                if (one.getWriteoffComplete() == null || "".equals(one.getWriteoffComplete())) {
                    one.setDebitNoteStatus("已制账单");
                } else if (one.getWriteoffComplete() == 1) {
                    one.setDebitNoteStatus("核销完毕");
                } else if (one.getWriteoffComplete() == 0) {
                    one.setDebitNoteStatus("部分核销");
                }
            }
            //账单金额实现多币种显示
            LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
            cssDebitCurrencyWrapper.eq(CssDebitNoteCurrency::getDebitNoteId, one.getDebitNoteId()).eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
            List<CssDebitNoteCurrency> currencyList = cssDebitNoteCurrencyService.list(cssDebitCurrencyWrapper);
            StringBuffer buffer = new StringBuffer();
            currencyList.stream().forEach(currency -> {
                String amount = new DecimalFormat("###,###.00").format(currency.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP));
                if (amount.startsWith(".")) {
                    amount = "0" + amount;
                }
                buffer.append(amount).append(" (").append(currency.getCurrency()).append(")  ");
            });

            one.setCurrencyAmount(buffer.toString());

            //封装树结构
            String currency = "";
            if (StrUtil.isNotBlank(one.getCurrency())) {
                currency = one.getCurrency();
            }
            String amountTaxRateStr = "";
            if (one.getAmountTaxRate() != null) {
                amountTaxRateStr = one.getAmountTaxRate().toString();
            }

//            String key = one.getCustomerId() + "$%$" + currency + "$%$" + amountTaxRateStr;
            String key = one.getCustomerId() + "$%$" + amountTaxRateStr;
            if (map.get(key) == null) {
                ArrayList<DebitNote> debitNotes = new ArrayList<>();
                debitNotes.add(one);
                map.put(key, debitNotes);
            } else {
                map.get(key).add(one);
            }
        });
        List<DebitNoteTree> result = new ArrayList<>();
        for (Map.Entry<String, List<DebitNote>> entry : map.entrySet()) {
            DebitNoteTree debitNoteTree = new DebitNoteTree();
            debitNoteTree.setDebitNoteId("A" + entry.getValue().get(0).getDebitNoteId());
            debitNoteTree.setAmountTaxRate(entry.getValue().get(0).getAmountTaxRate());
            debitNoteTree.setCustomerName(entry.getValue().get(0).getCustomerName());
            debitNoteTree.setCurrency(entry.getValue().get(0).getCurrency());
            debitNoteTree.setChildren(entry.getValue());
            result.add(debitNoteTree);
        }

        return result;
    }

    @Override
    public List<DebitNote> select2(DebitNote debitNote) {

        debitNote.setOrgId(SecurityUtils.getUser().getOrgId());
        String[] debitNoteIds = null;
        if (debitNote.getDebitNoteIds() != null) {
            if (debitNote.getDebitNoteIds().contains(",")) {
                debitNoteIds = debitNote.getDebitNoteIds().split(",");
            } else {
                debitNoteIds = new String[]{debitNote.getDebitNoteIds()};
            }
        }
        List<DebitNote> list = baseMapper.select22(debitNote, debitNoteIds);

        list.stream().forEach(one -> {

            //账单金额实现多币种显示
            LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
            cssDebitCurrencyWrapper.eq(CssDebitNoteCurrency::getDebitNoteId, one.getDebitNoteId()).eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
            List<CssDebitNoteCurrency> currencyList = cssDebitNoteCurrencyService.list(cssDebitCurrencyWrapper);
            StringBuffer buffer = new StringBuffer();
            currencyList.stream().forEach(currency -> {
                buffer.append(new DecimalFormat("###,###.00").format(currency.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP))).append(" (").append(currency.getCurrency()).append(")  ");
            });

            one.setCurrencyAmount(buffer.toString());
        });

        return list;
    }

    @Override
    public List<DebitNote> select(DebitNote debitNote) {
        debitNote.setOrgId(SecurityUtils.getUser().getOrgId());
        List<DebitNote> list = baseMapper.select2(debitNote);

        list.stream().forEach(one -> {

            //账单金额实现多币种显示
            LambdaQueryWrapper<CssDebitNoteCurrency> cssDebitCurrencyWrapper = Wrappers.<CssDebitNoteCurrency>lambdaQuery();
            cssDebitCurrencyWrapper.eq(CssDebitNoteCurrency::getDebitNoteId, one.getDebitNoteId()).eq(CssDebitNoteCurrency::getOrgId, SecurityUtils.getUser().getOrgId());
            List<CssDebitNoteCurrency> currencyList = cssDebitNoteCurrencyService.list(cssDebitCurrencyWrapper);
            StringBuffer buffer = new StringBuffer();
            currencyList.stream().forEach(currency -> {
                String amount = new DecimalFormat("###,###.00").format(currency.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP));
                if (amount.startsWith(".")) {
                    amount = "0" + amount;
                }
                buffer.append(amount).append(" (").append(currency.getCurrency()).append(")  ");
            });

            one.setCurrencyAmount(buffer.toString());
        });

        return list;
    }


    public static boolean downloadFile(String fileURL, String fileName) {
        try {
            String path = fileName.substring(0, fileName.lastIndexOf("/"));
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            URL url = new URL(fileURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName));
            byte[] buffer = new byte[4096];
            int count = 0;
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
            out.close();
            in.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @SneakyThrows
    public String print(String modelType, Integer debitNoteId, boolean flag, String template) {


        String templateFilePath = "";

        if ("C".equals(modelType)) {
            templateFilePath = PDFUtils.filePath + "/PDFtemplate/EF_DEBITNOTE.pdf";
        } else if ("E".equals(modelType)) {
            templateFilePath = PDFUtils.filePath + "/PDFtemplate/EF_DEBITNOTE_EN.pdf";
        }
        //查询打印需要的信息
        DebitNotePrint debitNotePrint = new DebitNotePrint();
        DebitNote debitNote = baseMapper.selectById(debitNoteId);
        CoopVo coop = remoteCoopService.viewCoop(debitNote.getCustomerId()).getData();
        OrgVo org = remoteServiceToHRS.getByOrgId(SecurityUtils.getUser().getOrgId()).getData();
        if (flag) {
            if ("E".equals(modelType)) {
                /*if (coop != null) {
                    debitNotePrint.setCustomerName(coop.getCoop_ename());
                }*/
                if (org != null) {
                    debitNotePrint.setOrgName(org.getOrgEname());
                }
                if (StrUtil.isNotBlank(org.getEnBillTemplate())) {
                    templateFilePath = PDFUtils.filePath + "/PDFtemplate/temp/debitNote/" + org.getEnBillTemplate().split("/")[org.getEnBillTemplate().split("/").length - 1];
                    downloadFile(org.getEnBillTemplate(), templateFilePath);
                }
            } else if ("C".equals(modelType)) {
                /*if (coop != null) {
                    debitNotePrint.setCustomerName(coop.getCoop_name());
                }*/
                if (org != null) {
                    debitNotePrint.setOrgName(org.getOrgName());
                }
                if (StrUtil.isNotBlank(org.getChBillTemplate())) {
                    templateFilePath = PDFUtils.filePath + "/PDFtemplate/temp/debitNote/" + org.getChBillTemplate().split("/")[org.getChBillTemplate().split("/").length - 1];
                    downloadFile(org.getChBillTemplate(), templateFilePath);
                }
            }
        } else {
            if ("E".equals(modelType)) {
                /*if (coop != null) {
                    debitNotePrint.setCustomerName(coop.getCoop_ename());
                }*/
                if (org != null) {
                    debitNotePrint.setOrgName(org.getOrgEname());
                }
            } else if ("C".equals(modelType)) {
                /*if (coop != null) {
                    debitNotePrint.setCustomerName(coop.getCoop_name());
                }*/
                if (org != null) {
                    debitNotePrint.setOrgName(org.getOrgName());
                }
            }
            if (StrUtil.isNotBlank(template)) {
                templateFilePath = template;
            }
        }
        //先取办公地址，没有取注册地址，再没有取客商资料地址
        if (!"".equals(coop.getWorkAddress()) && coop.getWorkAddress() != null) {
            debitNotePrint.setOrgAddress(coop.getWorkAddress());
        } else if (!"".equals(coop.getRegisterAddress()) && coop.getRegisterAddress() != null) {
            debitNotePrint.setOrgAddress(coop.getRegisterAddress());
        } else {
            debitNotePrint.setOrgAddress(coop.getCoop_address());
        }
        debitNotePrint.setCustomerName(debitNote.getCustomerName());//使用原客商名称
        debitNotePrint.setOrgTelephone(coop.getPhone_number());
        debitNotePrint.setDebitNoteNum(debitNote.getDebitNoteNum());
        debitNotePrint.setDebitNoteDate(debitNote.getDebitNoteDate());
        debitNotePrint.setRemark(debitNote.getDebitNoteRemark());
        AfOrder order = afOrderService.getById(debitNote.getOrderId());
        debitNotePrint.setJobNumber(order.getCustomerNumber());
        debitNotePrint.setAwbNumber(order.getAwbNumber());
        debitNotePrint.setDeparture(order.getDepartureStation());
        debitNotePrint.setDestination(order.getArrivalStation());
        debitNotePrint.setFlightNo(order.getExpectFlight());
        debitNotePrint.setFlightDate(order.getExpectDeparture());
        debitNotePrint.setPieces(order.getConfirmPieces() == null ? order.getPlanPieces() : order.getConfirmPieces());
        debitNotePrint.setChargeWeight(order.getConfirmChargeWeight() == null ? new BigDecimal(order.getPlanChargeWeight()).setScale(1, BigDecimal.ROUND_HALF_UP) : new BigDecimal(order.getConfirmChargeWeight()).setScale(1, BigDecimal.ROUND_HALF_UP));
        debitNotePrint.setVolume(order.getConfirmVolume() == null ? new BigDecimal(order.getPlanVolume()).setScale(3, BigDecimal.ROUND_HALF_UP) : new BigDecimal(order.getConfirmVolume()).setScale(3, BigDecimal.ROUND_HALF_UP));

//        if ("CNY".equals(debitNote.getCurrency())) {
//            debitNotePrint.setAmount(debitNote.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
//        } else {
//            debitNotePrint.setAmount(new DecimalFormat("###,###.00").format(debitNote.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)) + "(" + debitNote.getCurrency() + ")" + "    " + new DecimalFormat("###,###.00").format(debitNote.getFunctionalAmount().setScale(2, BigDecimal.ROUND_HALF_UP)) + "(CNY)");
//        }

        String functionalAmountBig = baseMapper.calltomFFormatMoney(debitNote.getFunctionalAmount().setScale(2, BigDecimal.ROUND_HALF_UP));
        debitNotePrint.setFunctionalAmountBig(functionalAmountBig);

        String functionalAmount = new DecimalFormat("###,###.00").format(debitNote.getFunctionalAmount().setScale(2, BigDecimal.ROUND_HALF_UP));
        if (functionalAmount.startsWith(".")) {
            functionalAmount = "0" + functionalAmount;
        }
        debitNotePrint.setFunctionalAmount(functionalAmount + "(CNY)");

        debitNotePrint.setCreatorName(SecurityUtils.getUser().getUserCname());
        debitNotePrint.setCreateTime(LocalDateTime.now());
        debitNotePrint.setBankInfo("-");

        LambdaQueryWrapper<AfIncome> incomeWrapper = Wrappers.<AfIncome>lambdaQuery();
        incomeWrapper.eq(AfIncome::getDebitNoteId, debitNote.getDebitNoteId()).eq(AfIncome::getOrgId, SecurityUtils.getUser().getOrgId());
        List<AfIncome> incomeList = afIncomeService.list(incomeWrapper);

        HashMap<String, BigDecimal> map = new HashMap<>();
        for (AfIncome afIncome : incomeList) {
            if ("E".equals(modelType)) {
                com.efreight.afbase.entity.Service service = serviceService.getById(afIncome.getServiceId());
                if (service != null) {
                    afIncome.setServiceName(service.getServiceNameEn());
                }
            }
            if (map.get(afIncome.getIncomeCurrency()) == null) {
                map.put(afIncome.getIncomeCurrency(), afIncome.getIncomeAmount());
            } else {
                map.put(afIncome.getIncomeCurrency(), map.get(afIncome.getIncomeCurrency()).add(afIncome.getIncomeAmount()));
            }
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, BigDecimal> entry : map.entrySet()) {
            String amount = new DecimalFormat("###,###.00").format(entry.getValue().setScale(2, BigDecimal.ROUND_HALF_UP));
            if (amount.startsWith(".")) {
                amount = "0" + amount;
            }
            stringBuffer.append(amount + "(" + entry.getKey() + ")").append("  ");

        }

        debitNotePrint.setAmount(stringBuffer.toString());
        debitNotePrint.setIncomeList(incomeList);
        if (flag) {
            return fillTemplate(debitNotePrint, templateFilePath, PDFUtils.filePath + "/PDFtemplate/temp/debitNote", PDFUtils.filePath);
        } else {
            return fillTemplate(debitNotePrint, templateFilePath, PDFUtils.filePath + "/PDFtemplate/temp/debitNote", "");
        }
    }


    @Override
    @SneakyThrows
    public String printMany(String modelType, String debitNoteIds) {

        OrgVo org = remoteServiceToHRS.getByOrgId(SecurityUtils.getUser().getOrgId()).getData();
        String templateFilePath = "";
        if ("C".equals(modelType)) {
            if (StrUtil.isNotBlank(org.getChBillTemplate())) {
                templateFilePath = PDFUtils.filePath + "/PDFtemplate/temp/debitNote/" + org.getChBillTemplate().split("/")[org.getChBillTemplate().split("/").length - 1];
                downloadFile(org.getChBillTemplate(), templateFilePath);
            }
        } else if ("E".equals(modelType)) {
            if (StrUtil.isNotBlank(org.getEnBillTemplate())) {
                templateFilePath = PDFUtils.filePath + "/PDFtemplate/temp/debitNote/" + org.getEnBillTemplate().split("/")[org.getEnBillTemplate().split("/").length - 1];
                downloadFile(org.getEnBillTemplate(), templateFilePath);
            }
        }
        final String template = templateFilePath;
        ArrayList<String> newFilePaths = new ArrayList<>();
        Arrays.asList(debitNoteIds.split(",")).stream().forEach(debitNoteId -> {
            newFilePaths.add(print(modelType, Integer.parseInt(debitNoteId), false, template));
        });
        String lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/debitNote/DEBITNOTE_" + new Date().getTime() + ".pdf";
        if (newFilePaths.size() == 1) {
            DebitNote debitNote = baseMapper.selectById(debitNoteIds);
            lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/debitNote/DEBITNOTE_" + debitNote.getDebitNoteNum() + "_" + new Date().getTime() + ".pdf";
        }

        PDFUtils.loadAllPDFForFile(newFilePaths, lastFilePath, null);
        return lastFilePath.replace(PDFUtils.filePath, "");
    }


    public static String fillTemplate(DebitNotePrint debitNotePrint, String templateFilePath, String savePath, String replacePath) {
        String saveFilename = PDFUtils.makeFileName(debitNotePrint.getDebitNoteNum() + ".pdf");
        //得到文件的保存目录
        String newPDFPath = PDFUtils.makePath(saveFilename, savePath) + "/" + saveFilename;

        try {

            Map<String, String> valueData = new HashMap<>();

            valueData.put("Input00", StrUtil.isBlank(debitNotePrint.getOrgName()) ? "" : debitNotePrint.getOrgName());//签约公司名称
            valueData.put("Input01", StrUtil.isBlank(debitNotePrint.getCustomerName()) ? "" : debitNotePrint.getCustomerName());//收款客户名称
            valueData.put("Input14", StrUtil.isBlank(debitNotePrint.getOrgAddress()) ? "" : debitNotePrint.getOrgAddress());//签约公司地址
            valueData.put("Input15", StrUtil.isBlank(debitNotePrint.getOrgTelephone()) ? "" : debitNotePrint.getOrgTelephone());//签约公司电话
            valueData.put("Input02", StrUtil.isBlank(debitNotePrint.getDebitNoteNum()) ? "" : debitNotePrint.getDebitNoteNum());//账单编号
            valueData.put("Input03", debitNotePrint.getDebitNoteDate() == null ? "" : debitNotePrint.getDebitNoteDate().toString());//账单日期
            valueData.put("Input04", StrUtil.isBlank(debitNotePrint.getAwbNumber()) ? "" : debitNotePrint.getAwbNumber());//主单号
            valueData.put("Input05", StrUtil.isBlank(debitNotePrint.getJobNumber()) ? "" : debitNotePrint.getJobNumber());//工作号
            valueData.put("Input06", StrUtil.isBlank(debitNotePrint.getDeparture()) ? "" : debitNotePrint.getDeparture());//始发港
            valueData.put("Input07", StrUtil.isBlank(debitNotePrint.getDestination()) ? "" : debitNotePrint.getDestination());//目的港
            valueData.put("Input08", StrUtil.isBlank(debitNotePrint.getFlightNo()) ? "" : debitNotePrint.getFlightNo());//航班号
            valueData.put("Input09", debitNotePrint.getFlightDate() == null ? "" : debitNotePrint.getFlightDate().toString());//航班日期
            valueData.put("Input10", debitNotePrint.getPieces() == null ? "" : debitNotePrint.getPieces().toString());//件数
            valueData.put("Input11", debitNotePrint.getChargeWeight() == null ? "" : debitNotePrint.getChargeWeight().toString());//计费重
            valueData.put("Input12", debitNotePrint.getVolume() == null ? "" : debitNotePrint.getVolume().toString());//体积
            valueData.put("Input13", StrUtil.isBlank(debitNotePrint.getAmount()) ? "" : debitNotePrint.getAmount());//账单金额
            valueData.put("Input16", debitNotePrint.getFunctionalAmountBig() == null ? "" : debitNotePrint.getFunctionalAmountBig().toString());//本币金额
            valueData.put("Input17", StrUtil.isBlank(debitNotePrint.getCreatorName()) ? "" : debitNotePrint.getCreatorName());//制单人
            valueData.put("Input18", debitNotePrint.getCreateTime() == null ? "" : debitNotePrint.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//制单时间
//            valueData.put("Input19", StrUtil.isBlank(debitNotePrint.getBankInfo()) ? "" : debitNotePrint.getBankInfo());//银行信息
            valueData.put("Input19", StrUtil.isBlank(debitNotePrint.getRemark()) ? "" : debitNotePrint.getRemark());//账单备注信息
            valueData.put("Input20", StrUtil.isBlank(debitNotePrint.getFunctionalAmount()) ? "" : debitNotePrint.getFunctionalAmount());//账单本币金额


            //费用明细

            if (debitNotePrint.getIncomeList() != null) {
                HashMap<String, BigDecimal> mapAmount = new HashMap<>();
                HashMap<String, BigDecimal> mapQuantity = new HashMap<>();
                debitNotePrint.getIncomeList().stream().forEach(afIncome -> {
                    String s = afIncome.getIncomeUnitPrice().setScale(2, BigDecimal.ROUND_HALF_UP).toString() + ",hhssddbb," + afIncome.getServiceName() + ",hhssddbb," + afIncome.getIncomeCurrency();
                    if (mapAmount.get(s) == null) {
                        mapAmount.put(s, afIncome.getIncomeAmount().setScale(2, BigDecimal.ROUND_HALF_UP));
                        mapQuantity.put(s, afIncome.getIncomeQuantity().setScale(2, BigDecimal.ROUND_HALF_UP));
                    } else {
                        mapAmount.put(s, mapAmount.get(s).add(afIncome.getIncomeAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
                        mapQuantity.put(s, mapQuantity.get(s).add(afIncome.getIncomeQuantity().setScale(2, BigDecimal.ROUND_HALF_UP)));
                    }
                });

                ArrayList<AfIncome> afIncomes = new ArrayList<>();
                mapAmount.keySet().stream().forEach(key -> {
                    AfIncome afIncome = new AfIncome();
                    afIncome.setServiceName(key.split(",hhssddbb,")[1]);
                    afIncome.setIncomeUnitPrice(new BigDecimal(key.split(",hhssddbb,")[0]));
                    afIncome.setIncomeQuantity(mapQuantity.get(key));
                    if (mapAmount.get(key).doubleValue() == 0) {
                        return;
                    }
                    afIncome.setIncomeAmount(mapAmount.get(key));
                    afIncome.setIncomeCurrency(key.split(",hhssddbb,")[2]);
                    afIncomes.add(afIncome);
                });
                if (afIncomes.size() < 13 && afIncomes.size() > 0) {

                    for (int i = 0; i < afIncomes.size(); i++) {
                        valueData.put("Input" + (i + 11) + "1", StrUtil.isBlank(afIncomes.get(i).getServiceName()) ? "" : afIncomes.get(i).getServiceName());//费用-项目1

                        String incomeUnitPrice = new DecimalFormat("###,###.00").format(afIncomes.get(i).getIncomeUnitPrice().setScale(2, BigDecimal.ROUND_HALF_UP));
                        if (incomeUnitPrice.startsWith(".")) {
                            incomeUnitPrice = "0" + incomeUnitPrice;
                        }
                        valueData.put("Input" + (i + 11) + "2", afIncomes.get(i).getIncomeUnitPrice() == null ? "" : incomeUnitPrice);//费用-单价1

                        valueData.put("Input" + (i + 11) + "3", afIncomes.get(i).getIncomeQuantity() == null ? "" : afIncomes.get(i).getIncomeQuantity().setScale(2, BigDecimal.ROUND_HALF_UP).toString());//费用-数量1

                        String incomeAmount = new DecimalFormat("###,###.00").format(afIncomes.get(i).getIncomeAmount().setScale(2, BigDecimal.ROUND_HALF_UP)) + "(" + afIncomes.get(i).getIncomeCurrency() + ")";
                        if (incomeAmount.startsWith(".")) {
                            incomeAmount = "0" + incomeAmount;
                        }
                        valueData.put("Input" + (i + 11) + "4", afIncomes.get(i).getIncomeAmount() == null ? "" : incomeAmount);//费用-金额1
                        valueData.put("Input" + (i + 11) + "5", "");//费用-备注1
                    }
                } else if (afIncomes.size() > 12) {
                    for (int i = 0; i < 12; i++) {

                        valueData.put("Input" + (i + 11) + "1", StrUtil.isBlank(afIncomes.get(i).getServiceName()) ? "" : afIncomes.get(i).getServiceName());//费用-项目1
                        String incomeUnitPrice = new DecimalFormat("###,###.00").format(afIncomes.get(i).getIncomeUnitPrice().setScale(2, BigDecimal.ROUND_HALF_UP));
                        if (incomeUnitPrice.startsWith(".")) {
                            incomeUnitPrice = "0" + incomeUnitPrice;
                        }
                        valueData.put("Input" + (i + 11) + "2", afIncomes.get(i).getIncomeUnitPrice() == null ? "" : incomeUnitPrice);//费用-单价1

                        valueData.put("Input" + (i + 11) + "3", afIncomes.get(i).getIncomeQuantity() == null ? "" : afIncomes.get(i).getIncomeQuantity().setScale(2, BigDecimal.ROUND_HALF_UP).toString());//费用-数量1

                        String incomeAmount = new DecimalFormat("###,###.00").format(afIncomes.get(i).getIncomeAmount().setScale(2, BigDecimal.ROUND_HALF_UP)) + "(" + afIncomes.get(i).getIncomeCurrency() + ")";
                        if (incomeAmount.startsWith(".")) {
                            incomeAmount = "0" + incomeAmount;
                        }
                        valueData.put("Input" + (i + 11) + "4", afIncomes.get(i).getIncomeAmount() == null ? "" : incomeAmount);//费用-金额1
                        valueData.put("Input" + (i + 11) + "5", "");//费用-备注1
                    }
                }
            }


            //pdf填充数据以及下载
            loadPDF(templateFilePath, newPDFPath, valueData, false, false);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Exception : " + e.getMessage());
        }
        return newPDFPath.replace(replacePath, "");
    }

    @SneakyThrows
    public static File loadPDF(String templatePath, String newPDFPath, Map<String, String> valueData, boolean ifSeal, boolean ifLogo) {
        File file = new File(newPDFPath);
        if (!file.exists()) {
            file = new File(newPDFPath);
        }
        FileOutputStream out = new FileOutputStream(file);// 输出流
        PdfReader reader = new PdfReader(templatePath);// 读取pdf模板
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, bos);
        AcroFields form = stamper.getAcroFields();


        //BaseFont bf = BaseFont.createFont(PDFUtils.simhei, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);

        form.addSubstitutionFont(bf);

        for (String key : valueData.keySet()) {
            if (!"Input26".equals(key) && !"Input27".equals(key)) {
                form.setField(key, valueData.get(key));
            }
        }
        stamper.setFormFlattening(true);// 如果为false那么生成的PDF文件还能编辑，一定要设为true

        if (ifSeal && StrUtil.isNotBlank(valueData.get("Input26"))) {
            int pageNo = form.getFieldPositions("Input26").get(0).page;
            Rectangle signRect = form.getFieldPositions("Input26").get(0).position;
            float x = signRect.getLeft();
            float y = signRect.getBottom();
            // 读图片
            String imageUrl = PDFUtils.filePath + "/PDFtemplate/temp/img/DebitnotePrint/" + SecurityUtils.getUser().getOrgId() + "/" + valueData.get("Input26").substring(valueData.get("Input26").lastIndexOf("/") + 1);
            PDFUtils.downloadFile(valueData.get("Input26"), imageUrl);
            Image image = Image.getInstance(imageUrl);
            // 获取操作的页面
            PdfContentByte under = stamper.getOverContent(pageNo);
            // 根据域的大小缩放图片
            image.scaleToFit(signRect.getWidth(), signRect.getHeight());
            // 添加图片
            image.setAbsolutePosition(x, y);
            under.addImage(image);
        }
        if (ifLogo && StrUtil.isNotBlank(valueData.get("Input27"))) {
            int pageNo = form.getFieldPositions("Input27").get(0).page;
            Rectangle signRect = form.getFieldPositions("Input27").get(0).position;
            float x = signRect.getLeft();
            float y = signRect.getBottom();
            // 读图片
            String imageUrl = PDFUtils.filePath + "/PDFtemplate/temp/img/DebitnotePrint/" + SecurityUtils.getUser().getOrgId() + "/" + valueData.get("Input27").substring(valueData.get("Input27").lastIndexOf("/") + 1);
            PDFUtils.downloadFile(valueData.get("Input27"), imageUrl);
            Image image = Image.getInstance(imageUrl);
            // 获取操作的页面
            PdfContentByte under = stamper.getOverContent(pageNo);
            // 根据域的大小缩放图片
            image.scaleToFit(signRect.getWidth(), signRect.getHeight());
            // 添加图片
            image.setAbsolutePosition(x, y);
            under.addImage(image);
        }
        stamper.close();

        Document doc = new Document();

        PdfCopy copy = new PdfCopy(doc, out);
        doc.open();
        PdfImportedPage importPage = copy.getImportedPage(new PdfReader(bos.toByteArray()), 1);
        copy.addPage(importPage);
        doc.close();
        return file;
    }

    @Override
    public boolean send(DebitNoteSendEntity debitNoteSendEntity) {
        if (StrUtil.isBlank(debitNoteSendEntity.getReceiver())) {
            throw new RuntimeException("收件人不能为空");
        }
        if (StrUtil.isBlank(debitNoteSendEntity.getDebitNoteId())) {
            throw new RuntimeException("账单号不能为空");
        }
        if (StrUtil.isBlank(debitNoteSendEntity.getTemplateType())) {
            throw new RuntimeException("模板类型不能为空");
        }
        String ccUser = "";
        if (StrUtil.isNotBlank(debitNoteSendEntity.getCcUser())) {
            ccUser = debitNoteSendEntity.getCcUser();
        }
//        String path = this.printMany(debitNoteSendEntity.getTemplateType(), debitNoteSendEntity.getDebitNoteId());
        String path = this.printManyNew(debitNoteSendEntity.getTemplateType(), debitNoteSendEntity.getDebitNoteId(), debitNoteSendEntity.getBusinessScope());
        String filePath = PDFUtils.filePath + path;
        ArrayList<Map<String, String>> fileList = new ArrayList<>();
        HashMap<String, String> fileMap = new HashMap<>();
        fileMap.put("name", filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length()));
        fileMap.put("path", filePath);
        fileMap.put("flag", "local");
        fileList.add(fileMap);
        if (StrUtil.isNotBlank(debitNoteSendEntity.getOrderFileIds())) {
            if ("AE".equals(debitNoteSendEntity.getBusinessScope()) || "AI".equals(debitNoteSendEntity.getBusinessScope())) {
                LambdaQueryWrapper<OrderFiles> orderFilesLambdaQueryWrapper = Wrappers.<OrderFiles>lambdaQuery();
                orderFilesLambdaQueryWrapper.eq(OrderFiles::getOrgId, SecurityUtils.getUser().getOrgId()).in(OrderFiles::getOrderFileId, debitNoteSendEntity.getOrderFileIds().split(","));
                orderFilesService.list(orderFilesLambdaQueryWrapper).stream().forEach(orderFiles -> {
                    HashMap<String, String> orderFileMap = new HashMap<>();
                    String expand = orderFiles.getFileUrl().substring(orderFiles.getFileUrl().lastIndexOf("."));
                    //替换附件名称中的一些特殊字符
                    /*String regEx = "[`~!@#$%^&*()\\-+={}':;,\\[\\].<>￥%…（）_+|【】‘；：”“’。，、\\s]";
                    Pattern p = Pattern.compile(regEx);
                    Matcher m = p.matcher(orderFiles.getFileName());
                    String fileName = m.replaceAll("?");*/
                    orderFileMap.put("name", orderFiles.getFileName().endsWith(expand) ? orderFiles.getFileName() : orderFiles.getFileName() + expand);
                    orderFileMap.put("path", orderFiles.getFileUrl());
                    orderFileMap.put("flag", "upload");
                    fileList.add(orderFileMap);
                });
            } else if (debitNoteSendEntity.getBusinessScope().startsWith("T")) {
                LambdaQueryWrapper<TcOrderFiles> orderFilesLambdaQueryWrapper = Wrappers.<TcOrderFiles>lambdaQuery();
                orderFilesLambdaQueryWrapper.eq(TcOrderFiles::getOrgId, SecurityUtils.getUser().getOrgId()).in(TcOrderFiles::getOrderFileId, debitNoteSendEntity.getOrderFileIds().split(","));
                tcOrderFilesService.list(orderFilesLambdaQueryWrapper).stream().forEach(orderFiles -> {
                    HashMap<String, String> orderFileMap = new HashMap<>();
                    String expand = orderFiles.getFileUrl().substring(orderFiles.getFileUrl().lastIndexOf("."));
                    //替换附件名称中的一些特殊字符
 	                /*String regEx = "[`~!@#$%^&*()\\-+={}':;,\\[\\].<>￥%…（）_+|【】‘；：”“’。，、\\s]";
 	                Pattern p = Pattern.compile(regEx);
 	                Matcher m = p.matcher(orderFiles.getFileName());
 	                String fileName = m.replaceAll("?");*/
                    orderFileMap.put("name", orderFiles.getFileName().endsWith(expand) ? orderFiles.getFileName() : orderFiles.getFileName() + expand);
                    orderFileMap.put("path", orderFiles.getFileUrl());
                    orderFileMap.put("flag", "upload");
                    fileList.add(orderFileMap);
                });
            } else if (debitNoteSendEntity.getBusinessScope().equals("LC")) {
                if (StrUtil.isNotBlank(debitNoteSendEntity.getOrderFileIds())) {
                    remoteServiceToSC.listLcOrderFilesByOrderFileIds(debitNoteSendEntity.getOrderFileIds()).getData().stream().forEach(orderFiles -> {
                        HashMap<String, String> orderFileMap = new HashMap<>();
                        String expand = orderFiles.getFileUrl().substring(orderFiles.getFileUrl().lastIndexOf("."));
                        orderFileMap.put("name", orderFiles.getFileName().endsWith(expand) ? orderFiles.getFileName() : orderFiles.getFileName() + expand);
                        orderFileMap.put("path", orderFiles.getFileUrl());
                        orderFileMap.put("flag", "upload");
                        fileList.add(orderFileMap);
                    });
                }
            } else if (debitNoteSendEntity.getBusinessScope().equals("IO")) {
                if (StrUtil.isNotBlank(debitNoteSendEntity.getOrderFileIds())) {
                    remoteServiceToSC.listIoOrderFilesByOrderFileIds(debitNoteSendEntity.getOrderFileIds()).getData().stream().forEach(orderFiles -> {
                        HashMap<String, String> orderFileMap = new HashMap<>();
                        String expand = orderFiles.getFileUrl().substring(orderFiles.getFileUrl().lastIndexOf("."));
                        orderFileMap.put("name", orderFiles.getFileName().endsWith(expand) ? orderFiles.getFileName() : orderFiles.getFileName() + expand);
                        orderFileMap.put("path", orderFiles.getFileUrl());
                        orderFileMap.put("flag", "upload");
                        fileList.add(orderFileMap);
                    });
                }
            } else {
                LambdaQueryWrapper<ScOrderFiles> orderFilesLambdaQueryWrapper = Wrappers.<ScOrderFiles>lambdaQuery();
                orderFilesLambdaQueryWrapper.eq(ScOrderFiles::getOrgId, SecurityUtils.getUser().getOrgId()).in(ScOrderFiles::getOrderFileId, debitNoteSendEntity.getOrderFileIds().split(","));
                scOrderFilesService.list(orderFilesLambdaQueryWrapper).stream().forEach(orderFiles -> {
                    HashMap<String, String> orderFileMap = new HashMap<>();
                    String expand = orderFiles.getFileUrl().substring(orderFiles.getFileUrl().lastIndexOf("."));
                    //替换附件名称中的一些特殊字符
	                /*String regEx = "[`~!@#$%^&*()\\-+={}':;,\\[\\].<>￥%…（）_+|【】‘；：”“’。，、\\s]";
	                Pattern p = Pattern.compile(regEx);
	                Matcher m = p.matcher(orderFiles.getFileName());
	                String fileName = m.replaceAll("?");*/
                    orderFileMap.put("name", orderFiles.getFileName().endsWith(expand) ? orderFiles.getFileName() : orderFiles.getFileName() + expand);
                    orderFileMap.put("path", orderFiles.getFileUrl());
                    orderFileMap.put("flag", "upload");
                    fileList.add(orderFileMap);
                });
            }

        }
        //替换邮件标题和邮件正文中的一些特殊字符
       /* String regEx = "[`~!@#$%^&*()\\-+={}':;,\\[\\].<>￥%…（）_+|【】‘；：”“’。，、\\s]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(debitNoteSendEntity.getSubject());
        String subject = m.replaceAll("?");
        Matcher m1 = p.matcher(debitNoteSendEntity.getContent());
        String content = m1.replaceAll("?");*/
        String content = debitNoteSendEntity.getContent();
        StringBuilder builder = new StringBuilder();
        builder.append(content.replaceAll("\n", "<br />"));
        mailSendService.sendAttachmentsMailNew(true, debitNoteSendEntity.getReceiver().split(","), ccUser.split(","), null, debitNoteSendEntity.getSubject(), builder.toString(), fileList, null);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doDelete(DebitNote bean) {
        String businessScope = bean.getBusinessScope();
        List<DebitNote> list = baseMapper.queryDebitForWhere(SecurityUtils.getUser().getOrgId(), bean.getDebitNoteIds());
        String debitNoteIds = null;
        StringBuffer sb = new StringBuffer();
        StringBuffer sbOne = new StringBuffer();//是否审核
        StringBuffer sbTwo = new StringBuffer();//是否生成清单
        StringBuffer sbThree = new StringBuffer();//是否开票申请
        if (list != null && list.size() > 0) {
            for (DebitNote dn : list) {
                if (dn.getWriteoffComplete() != null) {
                    sbOne.append(dn.getDebitNoteNum()).append(",");
                } else if (dn.getWriteoffComplete() == null && dn.getStatementId() != null) {
                    sbTwo.append(dn.getDebitNoteNum()).append(",");
                } else if (dn.getWriteoffComplete() == null && dn.getInvoiceDebitNoteId() != null) {
                    sbThree.append(dn.getDebitNoteNum()).append(",");
                } else {
                    sb.append(dn.getDebitNoteId()).append(",");//需要更新的
                }
            }
        } else {
            throw new RuntimeException("账单信息异常");
        }
        if (sbOne != null && !sbOne.toString().isEmpty()) {
            throw new RuntimeException(sbOne.substring(0, sbOne.length() - 1) + "有核销记录的账单,不能删除账单");
        }
        if (sbTwo != null && !sbTwo.toString().isEmpty()) {
            throw new RuntimeException("账单号:【" + sbTwo.substring(0, sbTwo.length() - 1) + "】已做清单 ，不允许删除账单！");
        }
        if (sbThree != null && !sbThree.toString().isEmpty()) {
            throw new RuntimeException("账单号:【" + sbThree.substring(0, sbThree.length() - 1) + "】已做发票申请 或 已开票 ，不允许删除账单！");
        }
        debitNoteIds = sb.substring(0, sb.length() - 1).toString();
        //日志
        LogBean logBean = new LogBean();
        logBean.setPageName("费用录入");
        logBean.setPageFunction("删除账单");
        logBean.setLogRemark("账单号：" + bean.getDebitNoteNums());
        logBean.setBusinessScope(businessScope);
        AfOrder orderBean = null;
        if ("AE".equals(businessScope) || "AI".equals(businessScope)) {
            orderBean = afOrderMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        }
        if ("SE".equals(businessScope) || "SI".equals(businessScope)) {
            orderBean = afOrderMapper.getSEOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());

        } else if (businessScope.startsWith("T")) {
            orderBean = afOrderMapper.getTCOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        } else if ("LC".equals(businessScope)) {
            orderBean = afOrderMapper.getLCOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        } else if ("IO".equals(businessScope)) {
            orderBean = afOrderMapper.getIOOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        }
        logBean.setOrderNumber(orderBean.getOrderCode());
        logBean.setOrderId(orderBean.getOrderId());
        logBean.setOrderUuid(orderBean.getOrderUuid());


        baseMapper.doDeleteBill(SecurityUtils.getUser().getOrgId(), debitNoteIds);
        baseMapper.doDeleteBillCurrency(SecurityUtils.getUser().getOrgId(), debitNoteIds);

        List<Integer> incodmeStatusList = afOrderMapper.getOrderIncomeStatus(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        if ("AE".equals(businessScope) || "AI".equals(businessScope)) {
            baseMapper.doUpdateIncome(SecurityUtils.getUser().getOrgId(), debitNoteIds, UUID.randomUUID().toString());
            //日志
            logService.saveLog(logBean);
            //修改订单费用状态
//            if (incodmeStatusList.size() == 0) {
//                afOrderMapper.updateOrderIncomeStatus2(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), "已录收入", UUID.randomUUID().toString());
//            }
        } else if ("SE".equals(businessScope) || "SI".equals(businessScope)) {
            baseMapper.doUpdateIncomeSE(SecurityUtils.getUser().getOrgId(), debitNoteIds, UUID.randomUUID().toString());
            //日志
            ScLog logBean2 = new ScLog();
            BeanUtils.copyProperties(logBean, logBean2);
            scLogService.saveLog(logBean2);
            //修改订单费用状态
//            if (incodmeStatusList.size() == 0) {
//                afOrderMapper.updateOrderIncomeStatusSE2(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), "已录收入", UUID.randomUUID().toString());
//            }
        } else if (businessScope.startsWith("T")) {
            baseMapper.doUpdateIncomeTC(SecurityUtils.getUser().getOrgId(), debitNoteIds, UUID.randomUUID().toString());
            TcLog logBean2 = new TcLog();
            BeanUtils.copyProperties(logBean, logBean2);
            logBean2.setCreatorId(SecurityUtils.getUser().getId());
            logBean2.setCreatorName(SecurityUtils.getUser().buildOptName());
            logBean2.setCreatTime(LocalDateTime.now());
            logBean2.setOrgId(SecurityUtils.getUser().getOrgId());
            tcLogService.save(logBean2);
//            if (incodmeStatusList.size() == 0) {
//                afOrderMapper.updateOrderIncomeStatusTC2(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), "已录收入", UUID.randomUUID().toString());
//            }
        } else if ("LC".equals(businessScope)) {
            baseMapper.doUpdateIncomeLC(SecurityUtils.getUser().getOrgId(), debitNoteIds, UUID.randomUUID().toString());
            LcLog logBean2 = new LcLog();
            BeanUtils.copyProperties(logBean, logBean2);
            logBean2.setCreatorId(SecurityUtils.getUser().getId());
            logBean2.setCreatorName(SecurityUtils.getUser().buildOptName());
            logBean2.setCreatTime(LocalDateTime.now());
            logBean2.setOrgId(SecurityUtils.getUser().getOrgId());
            lcLogService.save(logBean2);
//            if (incodmeStatusList.size() == 0) {
//                afOrderMapper.updateOrderIncomeStatusLC2(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), "已录收入", UUID.randomUUID().toString());
//            }
        } else if ("IO".equals(businessScope)) {
            baseMapper.doUpdateIncomeIO(SecurityUtils.getUser().getOrgId(), debitNoteIds, UUID.randomUUID().toString());
            IoLog logBean2 = new IoLog();
            BeanUtils.copyProperties(logBean, logBean2);
            logBean2.setCreatorId(SecurityUtils.getUser().getId());
            logBean2.setCreatorName(SecurityUtils.getUser().buildOptName());
            logBean2.setCreatTime(LocalDateTime.now());
            logBean2.setOrgId(SecurityUtils.getUser().getOrgId());
            ioLogService.save(logBean2);
//            if (incodmeStatusList.size() == 0) {
//                afOrderMapper.updateOrderIncomeStatusIO2(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), "已录收入", UUID.randomUUID().toString());
//            }
        }
        //更新订单应收状态：（order. income_status）
        List<Map> listMap = detailMapper.getOrderIncomeStatus(SecurityUtils.getUser().getOrgId(), orderBean.getOrderId().toString(), businessScope);
        if (listMap != null && listMap.size() > 0) {
            for (Map map : listMap) {
                detailMapper.updateOrderIncomeStatus(Integer.valueOf(map.get("org_id").toString()), Integer.valueOf(map.get("order_id").toString()), map.get("income_status").toString(), UUID.randomUUID().toString(), businessScope);
            }
        }

        return true;
    }

    @Override
    public List<DebitNote> selectCheckDebit(DebitNote debitNote) {
        debitNote.setOrgId(SecurityUtils.getUser().getOrgId());
        String[] debitNoteIds = null;
        if (debitNote.getDebitNoteIds() != null) {
            if (debitNote.getDebitNoteIds().contains(",")) {
                debitNoteIds = debitNote.getDebitNoteIds().split(",");
            } else {
                debitNoteIds = new String[]{debitNote.getDebitNoteIds()};
            }
        }
        List<DebitNote> list = baseMapper.selectCheckDebit(debitNote, debitNoteIds);
        return list;
    }

    @Override
    public boolean deleteDebitNote(String debitNoteNum) {
        boolean flag = true;
        baseMapper.deleteDebitNote(SecurityUtils.getUser().getOrgId(), debitNoteNum);
        return flag;
    }

    @Override
    public boolean updateDebitNote(Integer debitNoteId, Integer statementId) {
        boolean flag = true;
        baseMapper.updateDebitNote(SecurityUtils.getUser().getOrgId(), debitNoteId, statementId);
        return flag;
    }

    /**
     * AE订单批量打印 采用存储过程数据源
     */
    @Override
    public String printManyNew(String modelType, String debitNoteIds, String businessScope) {
        String templateFilePath = "";
        String templateFilePathP = "http://doc.yctop.com/";
        if ("C".equals(modelType)) {
            templateFilePath = PDFUtils.filePath + "/PDFtemplate/EF_DEBITNOTE.pdf";
            templateFilePathP = templateFilePathP + "EF_DEBITNOTE.pdf";
            modelType = "CH";
        } else if ("E".equals(modelType)) {
            templateFilePath = PDFUtils.filePath + "/PDFtemplate/EF_DEBITNOTE_EN.pdf";
            templateFilePathP = templateFilePathP + "EF_DEBITNOTE_EN.pdf";
            modelType = "EN";
        }
        List<List<Map<String, String>>> list = new ArrayList<List<Map<String, String>>>();
        String[] strArr = debitNoteIds.split(",");
        if (strArr != null) {
            for (int i = 0; i < strArr.length; i++) {
                List<List<Map<String, String>>> listResult = baseMapper.printManyNew(SecurityUtils.getUser().getOrgId(), businessScope, strArr[i], modelType, SecurityUtils.getUser().getId(), templateFilePathP);
                if (listResult != null && listResult.get(0) != null && listResult.get(0).size() > 0) {
                    if (list.size() > 0) {
                        list.get(0).addAll(listResult.get(0));
                        list.get(1).addAll(listResult.get(1));
                    } else {
                        list = listResult;
                    }
                }
            }
        }
        //账单信息
        List<Map<String, String>> listDebit = new ArrayList<Map<String, String>>();
        //费用明细
        List<Map<String, String>> listIncome = new ArrayList<Map<String, String>>();
        if (list == null || list.size() == 0 || list.get(0) == null && list.get(0).size() == 0) {
            return "无账单数据";
        }

//        DecimalFormat decimalFormat = new DecimalFormat("##########.##########");
        DecimalFormat decimalFormat2 = new DecimalFormat("#,###,###,###.##########");
        Map<String, List<Map<String, String>>> mapIncome = new HashMap<String, List<Map<String, String>>>();
        if (list.size() > 1) {
            listDebit = list.get(0);
            if (StrUtil.isNotBlank(listDebit.get(0).get("templateFilePath")) && !templateFilePathP.equals(listDebit.get(0).get("templateFilePath"))) {
                templateFilePath = PDFUtils.filePath + "/PDFtemplate/temp/debitNote/" + listDebit.get(0).get("templateFilePath").split("/")[listDebit.get(0).get("templateFilePath").split("/").length - 1];
                downloadFile(listDebit.get(0).get("templateFilePath"), templateFilePath);
            }
            listIncome = list.get(1);
            mapIncome = listIncome.stream()
                    .collect(Collectors.groupingBy(item -> item.get("debit_note_id").toString()));
        }
        ArrayList<String> newFilePaths = new ArrayList<>();
        //封装数据
//	        listDebit.stream().forEach(item->{
        for (int j = 0; j < listDebit.size(); j++) {
            Map<String, String> item = listDebit.get(j);
            String saveFilename = PDFUtils.makeFileName(item.get("Input02") + ".pdf");//账单号
            //得到文件的保存目录
            String newPDFPath = PDFUtils.makePath(saveFilename, PDFUtils.filePath + "/PDFtemplate/temp/debitNote") + "/" + saveFilename;
            if (mapIncome.get(item.get("debit_note_id").toString()) != null && mapIncome.get(item.get("debit_note_id").toString()).size() > 0) {
                int index = 1;
                for (int i = 0; i < mapIncome.get(item.get("debit_note_id").toString()).size(); i++) {
                    Map<String, String> itemIn = mapIncome.get(item.get("debit_note_id").toString()).get(i);
                    int indexM = 1;
                    if (index <= 9) {
                        indexM = 1;
                    } else if (9 < index && index <= 19) {
                        indexM = 2;
                    } else if (19 < index && index <= 29) {
                        indexM = 3;
                    } else if (29 < index && index <= 39) {
                        indexM = 4;
                    }
                    String input1_3 = "";
                    if (StrUtil.isNotBlank(itemIn.get("Input1_3"))) {
                        input1_3 = decimalFormat2.format(Double.valueOf(itemIn.get("Input1_3")));
                    }
                    if (index >= 10) {
                        String indexStr = String.valueOf(index).substring(String.valueOf(index).length() - 1, String.valueOf(index).length());
                        item.put("Input" + indexM + indexStr + "1", itemIn.get("Input1_1").toString());
                        item.put("Input" + indexM + indexStr + "2", itemIn.get("Input1_2").toString());
                        item.put("Input" + indexM + indexStr + "3", input1_3);
                        item.put("Input" + indexM + indexStr + "4", itemIn.get("Input1_4").toString());
                        item.put("Input" + indexM + indexStr + "5", itemIn.get("Input1_5").toString());
                        item.put("Input" + indexM + indexStr + "6", itemIn.get("Input1_6").toString());
                        item.put("Input" + indexM + indexStr + "7", itemIn.get("Input1_7").toString());
                        item.put("Input" + indexM + indexStr + "8", itemIn.get("Input1_8").toString());
                        item.put("Input" + indexM + indexStr + "9", itemIn.get("Input1_9").toString());
                    } else {
                        item.put("Input" + indexM + index + "1", itemIn.get("Input1_1").toString());
                        item.put("Input" + indexM + index + "2", itemIn.get("Input1_2").toString());
                        item.put("Input" + indexM + index + "3", input1_3);
                        item.put("Input" + indexM + index + "4", itemIn.get("Input1_4").toString());
                        item.put("Input" + indexM + index + "5", itemIn.get("Input1_5").toString());
                        item.put("Input" + indexM + index + "6", itemIn.get("Input1_6").toString());
                        item.put("Input" + indexM + index + "7", itemIn.get("Input1_7").toString());
                        item.put("Input" + indexM + index + "8", itemIn.get("Input1_8").toString());
                        item.put("Input" + indexM + index + "9", itemIn.get("Input1_9").toString());
                    }


                    index++;
                }
            }
            //pdf填充数据以及下载
            loadPDF(templateFilePath, newPDFPath, item, true, true);
            newFilePaths.add(newPDFPath.replace("", ""));
        }
        String lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/debitNote/" + listDebit.get(0).get("Input04") + "_" + new Date().getTime() + ".pdf";
        if (newFilePaths.size() == 1) {
            lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/debitNote/" + listDebit.get(0).get("Input04") + "_" + listDebit.get(0).get("Input02") + "_" + new Date().getTime() + ".pdf";
        }
        try {
            PDFUtils.loadAllPDFForFile(newFilePaths, lastFilePath, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastFilePath.replace(PDFUtils.filePath, "");
    }

    @Override
    public List<String> printManyNewForStatementPrint(String modelType, String debitNoteIds, String businessScope) {

        String templateFilePath = "";
        String templateFilePathP = "http://doc.yctop.com/";
        if ("C".equals(modelType)) {
            templateFilePath = PDFUtils.filePath + "/PDFtemplate/EF_DEBITNOTE.pdf";
            templateFilePathP = templateFilePathP + "EF_DEBITNOTE.pdf";
            modelType = "CH";
        } else if ("E".equals(modelType)) {
            templateFilePath = PDFUtils.filePath + "/PDFtemplate/EF_DEBITNOTE_EN.pdf";
            templateFilePathP = templateFilePathP + "EF_DEBITNOTE_EN.pdf";
            modelType = "EN";
        }
        List<List<Map<String, String>>> list = new ArrayList<List<Map<String, String>>>();
        String[] strArr = null;
        if (debitNoteIds.contains(",")) {
            strArr = debitNoteIds.split(",");
        } else {
            strArr = new String[]{debitNoteIds};
        }
        if (strArr != null) {
            for (int i = 0; i < strArr.length; i++) {
                List<List<Map<String, String>>> listResult = baseMapper.printManyNew(SecurityUtils.getUser().getOrgId(), businessScope, strArr[i], modelType, SecurityUtils.getUser().getId(), templateFilePathP);
                if (listResult != null && listResult.get(0) != null && listResult.get(0).size() > 0) {
                    if (list.size() > 0) {
                        list.get(0).addAll(listResult.get(0));
                        list.get(1).addAll(listResult.get(1));
                    } else {
                        list = listResult;
                    }
                }
            }
        }
        List<Map<String, String>> listDebit = new ArrayList<Map<String, String>>();
        List<Map<String, String>> listIncome = new ArrayList<Map<String, String>>();
        if (list != null && list.size() > 0 && list.get(0) != null && list.get(0).size() > 0) {
        } else {
            return null;
        }
        DecimalFormat decimalFormat = new DecimalFormat("##########.##########");
        DecimalFormat decimalFormat2 = new DecimalFormat("#,###,###,###.##########");
        Map<String, List<Map<String, String>>> mapIncome = new HashMap<String, List<Map<String, String>>>();
        if (list != null && list.size() > 1 && list.get(0) != null && list.get(0).size() > 0) {
            listDebit = list.get(0);
            if (StrUtil.isNotBlank(listDebit.get(0).get("Input11"))) {
                String[] arrStr = listDebit.get(0).get("Input11").split(" ");
                String input11 = decimalFormat.format(Double.valueOf(arrStr[0]));
                listDebit.get(0).put("Input11", input11 + "  " + arrStr[1]);
            }
            if (StrUtil.isNotBlank(listDebit.get(0).get("Input12"))) {
                String[] arrStr = listDebit.get(0).get("Input12").split(" ");
                String input12 = decimalFormat.format(Double.valueOf(arrStr[0]));
                listDebit.get(0).put("Input12", input12 + "  " + arrStr[1]);
            }
            if (StrUtil.isNotBlank(listDebit.get(0).get("templateFilePath")) && !templateFilePathP.equals(listDebit.get(0).get("templateFilePath"))) {
                templateFilePath = PDFUtils.filePath + "/PDFtemplate/temp/debitNote/" + listDebit.get(0).get("templateFilePath").split("/")[listDebit.get(0).get("templateFilePath").split("/").length - 1];
                downloadFile(listDebit.get(0).get("templateFilePath"), templateFilePath);
            }
        }
        //分组清洗数据
        if (list != null && list.size() > 1 && list.get(1) != null && list.get(1).size() > 0) {
            listIncome = list.get(1);
            mapIncome = listIncome.stream()
                    .collect(Collectors.groupingBy(item -> item.get("debit_note_id").toString()));
        }
        ArrayList<String> newFilePaths = new ArrayList<>();
        //封装数据
//	        listDebit.stream().forEach(item->{
        for (int j = 0; j < listDebit.size(); j++) {
            Map<String, String> item = listDebit.get(j);
            String saveFilename = PDFUtils.makeFileName(item.get("Input02") + ".pdf");
            //得到文件的保存目录
            String newPDFPath = PDFUtils.makePath(saveFilename, PDFUtils.filePath + "/PDFtemplate/temp/debitNote") + "/" + saveFilename;
            if (mapIncome.get(item.get("debit_note_id").toString()) != null && mapIncome.get(item.get("debit_note_id").toString()).size() > 0) {
                int index = 1;
                for (int i = 0; i < mapIncome.get(item.get("debit_note_id").toString()).size(); i++) {
                    Map<String, String> itemIn = mapIncome.get(item.get("debit_note_id").toString()).get(i);
                    int indexM = 1;
                    if (index <= 9) {
                        indexM = 1;
                    } else if (9 < index && index <= 19) {
                        indexM = 2;
                    } else if (19 < index && index <= 29) {
                        indexM = 3;
                    } else if (29 < index && index <= 39) {
                        indexM = 4;
                    }
                    String input1_3 = "";
                    if (StrUtil.isNotBlank(itemIn.get("Input1_3"))) {
                        input1_3 = decimalFormat2.format(Double.valueOf(itemIn.get("Input1_3")));
                    }
                    if (index >= 10) {
                        String indexStr = String.valueOf(index).substring(String.valueOf(index).length() - 1, String.valueOf(index).length());
                        item.put("Input" + indexM + indexStr + "1", itemIn.get("Input1_1").toString());
                        item.put("Input" + indexM + indexStr + "2", itemIn.get("Input1_2").toString());
                        item.put("Input" + indexM + indexStr + "3", input1_3);
                        item.put("Input" + indexM + indexStr + "4", itemIn.get("Input1_4").toString());
                        item.put("Input" + indexM + indexStr + "5", itemIn.get("Input1_5").toString());
                        item.put("Input" + indexM + indexStr + "6", itemIn.get("Input1_6").toString());
                        item.put("Input" + indexM + indexStr + "7", itemIn.get("Input1_7").toString());
                        item.put("Input" + indexM + indexStr + "8", itemIn.get("Input1_8").toString());
                        item.put("Input" + indexM + indexStr + "9", itemIn.get("Input1_9").toString());
                    } else {
                        item.put("Input" + indexM + index + "1", itemIn.get("Input1_1").toString());
                        item.put("Input" + indexM + index + "2", itemIn.get("Input1_2").toString());
                        item.put("Input" + indexM + index + "3", input1_3);
                        item.put("Input" + indexM + index + "4", itemIn.get("Input1_4").toString());
                        item.put("Input" + indexM + index + "5", itemIn.get("Input1_5").toString());
                        item.put("Input" + indexM + index + "6", itemIn.get("Input1_6").toString());
                        item.put("Input" + indexM + index + "7", itemIn.get("Input1_7").toString());
                        item.put("Input" + indexM + index + "8", itemIn.get("Input1_8").toString());
                        item.put("Input" + indexM + index + "9", itemIn.get("Input1_9").toString());
                    }

                    index++;
                }
            }
            //pdf填充数据以及下载
            loadPDF(templateFilePath, newPDFPath, item, true, true);
            //PDFUtils.loadPDF2(templateFilePath, newPDFPath, item, false, false);
            newFilePaths.add(newPDFPath.replace("", ""));
        }
       /* String lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/debitNote/" + listDebit.get(0).get("Input04") + "_" + new Date().getTime() + ".pdf";
        if (newFilePaths.size() == 1) {
            lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/debitNote/" + listDebit.get(0).get("Input04") + "_" + listDebit.get(0).get("Input02") + "_" + new Date().getTime() + ".pdf";
        }
        try {
            PDFUtils.loadAllPDFForFile(newFilePaths, lastFilePath, null);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return newFilePaths;
    }

    @Override
    public void exportExcel(String modelType, String debitNoteIds, String businessScope) {

        String templateFilePath = "";
        if ("C".equals(modelType)) {
            templateFilePath = PDFUtils.filePath + "/PDFtemplate/DN_Cn.xlsx";
            modelType = "CH";
        } else if ("E".equals(modelType)) {
            templateFilePath = PDFUtils.filePath + "/PDFtemplate/DN_En.xlsx";
            modelType = "EN";
        }
        List<List<Map<String, Object>>> list = new ArrayList<List<Map<String, Object>>>();
        String[] strArr = null;
        if (debitNoteIds.contains(",")) {
            strArr = debitNoteIds.split(",");
        } else {
            strArr = new String[]{debitNoteIds};
        }
        if (strArr != null) {
            for (int i = 0; i < strArr.length; i++) {
                List<List<Map<String, Object>>> listResult = baseMapper.printManyDebitNoteNew(SecurityUtils.getUser().getOrgId(), businessScope, strArr[i], modelType, SecurityUtils.getUser().getId(), null);
                if (listResult != null && listResult.get(0) != null && listResult.get(0).size() > 0) {
                    if (list.size() > 0) {
                        list.get(0).addAll(listResult.get(0));
                        list.get(1).addAll(listResult.get(1));
                    } else {
                        list = listResult;
                    }
                }
            }
        }
        List<Map<String, Object>> listDebit = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> listIncome = new ArrayList<Map<String, Object>>();
        if (list != null && list.size() > 0 && list.get(0) != null && list.get(0).size() > 0) {
        } else {
            throw new RuntimeException("账单无数据");
        }

        DecimalFormat decimalFormat = new DecimalFormat("##########.##########");
        DecimalFormat decimalFormat2 = new DecimalFormat("#,###,###,###.##########");
        Map<String, List<Map<String, Object>>> mapIncome = new HashMap<String, List<Map<String, Object>>>();
        if (list != null && list.size() > 1 && list.get(0) != null && list.get(0).size() > 0) {
            listDebit = list.get(0);
            /*if (StrUtil.isNotBlank(listDebit.get(0).get("Input11"))) {
                String[] arrStr = listDebit.get(0).get("Input11").split(" ");
                String input11 = decimalFormat.format(Double.valueOf(arrStr[0]));
                listDebit.get(0).put("Input11", input11 + "  " + arrStr[1]);
            }
            if (StrUtil.isNotBlank(listDebit.get(0).get("Input12"))) {
                String[] arrStr = listDebit.get(0).get("Input12").split(" ");
                String input12 = decimalFormat.format(Double.valueOf(arrStr[0]));
                listDebit.get(0).put("Input12", input12 + "  " + arrStr[1]);
            }*/
            if (listDebit.get(0).get("templateFilePathExcel") != null && StrUtil.isNotBlank(listDebit.get(0).get("templateFilePathExcel").toString())) {
                templateFilePath = PDFUtils.filePath + "/PDFtemplate/temp/debitNote/" + SecurityUtils.getUser().getOrgId() + "/" + listDebit.get(0).get("templateFilePathExcel").toString().substring(listDebit.get(0).get("templateFilePathExcel").toString().lastIndexOf("/") + 1, listDebit.get(0).get("templateFilePathExcel").toString().length());
                downloadFile(listDebit.get(0).get("templateFilePathExcel").toString(), templateFilePath);
            }
        }
        //分组清洗数据
        if (list != null && list.size() > 1 && list.get(1) != null && list.get(1).size() > 0) {
            listIncome = list.get(1);
            mapIncome = listIncome.stream()
                    .collect(Collectors.groupingBy(item -> item.get("debit_note_id").toString()));
        }
        ArrayList<Map<String, Object>> itemList = new ArrayList<>();
        HashMap<String, Object> context = new HashMap<>();
        //封装数据
        for (int j = 0; j < listDebit.size(); j++) {
            Map<String, Object> item = listDebit.get(j);
            if (mapIncome.get(item.get("debit_note_id").toString()) != null && mapIncome.get(item.get("debit_note_id").toString()).size() > 0) {
                int index = 1;
                for (int i = 0; i < mapIncome.get(item.get("debit_note_id").toString()).size(); i++) {
                    Map<String, Object> itemIn = mapIncome.get(item.get("debit_note_id").toString()).get(i);
                    int indexM = 1;
                    if (index <= 9) {
                        indexM = 1;
                    } else if (9 < index && index <= 19) {
                        indexM = 2;
                    } else if (19 < index && index <= 29) {
                        indexM = 3;
                    } else if (29 < index && index <= 39) {
                        indexM = 4;
                    }
                    String input1_3 = "";
                    if (itemIn.get("Input1_3") != null && StrUtil.isNotBlank(itemIn.get("Input1_3").toString())) {
                        input1_3 = decimalFormat2.format(Double.valueOf(itemIn.get("Input1_3").toString()));
                    }
                    if (index >= 10) {
                        String indexStr = String.valueOf(index).substring(String.valueOf(index).length() - 1, String.valueOf(index).length());
                        item.put("Input" + indexM + indexStr + "1", itemIn.get("Input1_1").toString());
                        item.put("Input" + indexM + indexStr + "2", itemIn.get("Input1_2").toString());
                        item.put("Input" + indexM + indexStr + "3", input1_3);
                        item.put("Input" + indexM + indexStr + "4", itemIn.get("Input1_4").toString());
                        item.put("Input" + indexM + indexStr + "5", itemIn.get("Input1_5").toString());
                        item.put("Input" + indexM + indexStr + "6", itemIn.get("Input1_6").toString());
                        item.put("Input" + indexM + indexStr + "7", itemIn.get("Input1_7").toString());
                        item.put("Input" + indexM + indexStr + "8", itemIn.get("Input1_8").toString());
                        item.put("Input" + indexM + indexStr + "9", itemIn.get("Input1_9").toString());
                    } else {
                        item.put("Input" + indexM + index + "1", itemIn.get("Input1_1").toString());
                        item.put("Input" + indexM + index + "2", itemIn.get("Input1_2").toString());
                        item.put("Input" + indexM + index + "3", input1_3);
                        item.put("Input" + indexM + index + "4", itemIn.get("Input1_4").toString());
                        item.put("Input" + indexM + index + "5", itemIn.get("Input1_5").toString());
                        item.put("Input" + indexM + index + "6", itemIn.get("Input1_6").toString());
                        item.put("Input" + indexM + index + "7", itemIn.get("Input1_7").toString());
                        item.put("Input" + indexM + index + "8", itemIn.get("Input1_8").toString());
                        item.put("Input" + indexM + index + "9", itemIn.get("Input1_9").toString());
                    }
                    index++;
                }
            }
            if (item.get("Input26") == null || StrUtil.isBlank(item.get("Input26").toString())) {
                item.put("Input26", null);
            } else {
                String imagePath = PDFUtils.filePath + "/PDFtemplate/temp/debitnote/" + UUID.randomUUID().toString() + "/" + item.get("Input26").toString().substring(item.get("Input26").toString().lastIndexOf("/") + 1, item.get("Input26").toString().length());
                downloadFile(item.get("Input26").toString(), imagePath);
                try {
                    byte[] imageBytes = Util.toByteArray(new FileInputStream(imagePath));
                    item.put("Input26", imageBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            if (item.get("Input26") != null && StrUtil.isNotBlank(item.get("Input26").toString())) {
//                String imagePath = PDFUtils.filePath + "/PDFtemplate/temp/debitnote/" + UUID.randomUUID().toString() + "/" + item.get("Input26").toString().substring(item.get("Input26").toString().lastIndexOf("/") + 1, item.get("Input26").toString().length());
//                downloadFile(item.get("Input26").toString(), imagePath);
//                try {
//                    byte[] imageBytes = Util.toByteArray(new FileInputStream(imagePath));
//                    item.put("Input26", imageBytes);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
            if (item.get("Input27") == null || StrUtil.isBlank(item.get("Input27").toString())) {
                item.put("Input27", null);
            } else {
                String imagePath = PDFUtils.filePath + "/PDFtemplate/temp/debitnote/" + UUID.randomUUID().toString() + "/" + item.get("Input27").toString().substring(item.get("Input27").toString().lastIndexOf("/") + 1, item.get("Input27").toString().length());
                downloadFile(item.get("Input27").toString(), imagePath);
                try {
                    byte[] imageBytes = Util.toByteArray(new FileInputStream(imagePath));
                    item.put("Input27", imageBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            if (item.get("Input27") != null && StrUtil.isNotBlank(item.get("Input27").toString())) {
//                String imagePath = PDFUtils.filePath + "/PDFtemplate/temp/debitnote/" + UUID.randomUUID().toString() + "/" + item.get("Input27").toString().substring(item.get("Input27").toString().lastIndexOf("/") + 1, item.get("Input27").toString().length());
//                downloadFile(item.get("Input27").toString(), imagePath);
//                try {
//                    byte[] imageBytes = Util.toByteArray(new FileInputStream(imagePath));
//                    item.put("Input27", imageBytes);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
            itemList.add(item);
        }

        context.put("data", itemList);
        JxlsUtils.exportExcelWithLocalModel(templateFilePath, context);
    }


    public void exportExcelList(DebitNote bean) {
        List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
        String[] colunmStrs = null;
        String[] headers = null;
        if (!StringUtils.isEmpty(bean.getColumnStrs())) {
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(bean.getColumnStrs());
            int num = jsonArr.size() + 1;
            headers = new String[num];
            colunmStrs = new String[num];
            headers[0] = "账单编号";
            colunmStrs[0] = "debitNoteNum";
            //生成表头跟字段
            if (jsonArr != null && jsonArr.size() > 0) {
                int numStr = 1;
                for (int i = 0; i < jsonArr.size(); i++) {
                    JSONObject job = jsonArr.getJSONObject(i);
                    colunmStrs[numStr] = job.getString("prop");
                    headers[numStr] = job.getString("label");
                    numStr++;
                }
            }
        }
//        else {
//            //默认数据
//            String flightNoLabel = "航班号";
//            String flightDateLabel = "开航日期";
//            String flightDateLabelTwo = "flightNo";
//            if (bean.getBusinessScope().endsWith("I")) {
//                flightDateLabel = "到港日期";
//            }
//            if (bean.getBusinessScope().startsWith("S")) {
//                flightNoLabel = "航次号";
//            }
//            if (bean.getBusinessScope().startsWith("T")) {
//                flightDateLabel = "开车日期";
//                flightNoLabel = "产品类型";
//                flightDateLabelTwo = "productType";
//            }
//            if (bean.getBusinessScope().startsWith("L")) {
//                flightDateLabel = "用车日期";
//                flightNoLabel = "运输方式";
//                flightDateLabelTwo = "shippingMethod";
//            }
//            if (bean.getBusinessScope().equals("IO")) {
//                flightDateLabel = "业务日期";
//                flightNoLabel = "业务分类";
//                flightDateLabelTwo = "businessMethod";
//            }
//
//            if (bean.getBusinessScope().startsWith("L") || bean.getBusinessScope().equals("IO")) {
//                headers = new String[]{"账单编号", "账单状态", "账单日期", "订单号", flightNoLabel, flightDateLabel, "账单金额（原币）",
//                        "已核销金额（原币）", "账单金额（本币）", "已核销金额（本币）", "未核销金额（本币）", "客户单号", "清单号",
//                        "核销单号", "账单备注", "发票日期", "发票号码", "发票抬头", "发票备注", "制单人", "账单制作时间"};
//                colunmStrs = new String[]{"debitNoteNum", "debitNoteStatus", "debitNoteDate",
//                        "orderCode", flightDateLabelTwo, "flightDate", "currencyAmount", "currencyAmount2", "functionalAmount",
//                        "functionalAmountWriteoff", "functionalAmountNoWriteoff", "customerNumber", "statementNum",
//                        "writeoffNum", "debitNoteRemark", "invoiceDate", "invoiceNum", "invoiceTitle", "invoiceRemark", "creatorName", "createTime"};
//            } else {
//                headers = new String[]{"账单编号", "账单状态", "账单日期", "主单号", "订单号", flightNoLabel, flightDateLabel, "账单金额（原币）",
//                        "已核销金额（原币）", "账单金额（本币）", "已核销金额（本币）", "未核销金额（本币）", "客户单号", "清单号",
//                        "核销单号", "账单备注", "发票日期", "发票号码", "发票抬头", "发票备注", "制单人", "账单制作时间"};
//                colunmStrs = new String[]{"debitNoteNum", "debitNoteStatus", "debitNoteDate", "awbNumber",
//                        "orderCode", flightDateLabelTwo, "flightDate", "currencyAmount", "currencyAmount2", "functionalAmount",
//                        "functionalAmountWriteoff", "functionalAmountNoWriteoff", "customerNumber", "statementNum",
//                        "writeoffNum", "debitNoteRemark", "invoiceDate", "invoiceNum", "invoiceTitle", "invoiceRemark", "creatorName", "createTime"};
//            }
//        }
        //查询
        Page page = new Page();
        page.setCurrent(1);
        page.setSize(1000000);
        IPage result = this.getPage2(page, bean);
        DecimalFormat decimalFormat2 = new DecimalFormat("#,###,###,###.##########");
        if (result != null && result.getRecords() != null && result.getRecords().size() > 0) {
            List<DebitNoteTree> listA = result.getRecords();
            for (DebitNoteTree excel : listA) {
                if (excel != null && !"".equals(excel.getCurrencyAmount())) {
                    excel.setCurrencyAmount(excel.getCurrencyAmount().replaceAll("  ", String.valueOf((char) 10) + ""));
                }
                if (excel != null && !"".equals(excel.getCurrencyAmount2())) {
                    excel.setCurrencyAmount2(excel.getCurrencyAmount2().replaceAll("  ", String.valueOf((char) 10) + ""));
                }
                LinkedHashMap map = new LinkedHashMap();
                for (int j = 0; j < colunmStrs.length; j++) {
                    if (j == 0) {
                        map.put("debitNoteNum", FieldValUtils.getFieldValueByFieldName("customerName", excel));
                    } else {
                        if ("functionalAmountNoWriteoff".equals(colunmStrs[j])) {
                            if (excel.getFunctionalAmount() != null) {
                                BigDecimal noWriteoff = excel.getFunctionalAmountWriteoff() != null ? excel.getFunctionalAmountWriteoff() : BigDecimal.ZERO;
                                map.put("functionalAmountNoWriteoff", decimalFormat2.format(excel.getFunctionalAmount().subtract(noWriteoff)));
                            } else {
                                map.put("functionalAmountNoWriteoff", "");
                            }
                        } else if ("currencyAmount".equals(colunmStrs[j]) || "currencyAmount2".equals(colunmStrs[j])) {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], excel));
                        } else if ("functionalAmount".equals(colunmStrs[j])) {
                            if (excel.getFunctionalAmount() != null) {
                                map.put("functionalAmount", decimalFormat2.format(excel.getFunctionalAmount()));
                            } else {
                                map.put("functionalAmount", "");
                            }
                        } else if ("functionalAmountWriteoff".equals(colunmStrs[j])) {
                            if (excel.getFunctionalAmountWriteoff() != null) {
                                map.put("functionalAmountWriteoff", decimalFormat2.format(excel.getFunctionalAmountWriteoff()));
                            } else {
                                map.put("functionalAmountWriteoff", "");
                            }
                        } else {
                            map.put(colunmStrs[j], "");
                        }
                    }
                }
                listExcel.add(map);
                //取子集
                if (excel.getChildren() != null && excel.getChildren().size() > 0) {

                    for (DebitNote excel2 : excel.getChildren()) {
                        //处理制单人,只显示姓名
                        if (excel2.getCreatorName() != null && !"".equals(excel2.getCreatorName())) {
                            excel2.setCreatorName(excel2.getCreatorName().split(" ")[0]);
                        }
                        if (excel2 != null && !"".equals(excel2.getCurrencyAmount())) {
                            excel2.setCurrencyAmount(excel2.getCurrencyAmount().replaceAll("  ", String.valueOf((char) 10) + ""));
                        }
                        if (excel2 != null && !"".equals(excel2.getCurrencyAmount2())) {
                            excel2.setCurrencyAmount2(excel2.getCurrencyAmount2().replaceAll("  ", String.valueOf((char) 10) + ""));
                        }
                        //设置发票信息，如果存在statementId，则取清单的发票信息
//                        if(excel2 != null && excel2.getStatementId() != null){
//                            excel2.setInvoiceDate(excel2.getInvoiceDate2());
//                            excel2.setInvoiceNum(excel2.getInvoiceNum2());
//                            excel2.setInvoiceTitle(excel2.getInvoiceTitle2()); 
//                        }
                        LinkedHashMap mapTwo = new LinkedHashMap();
                        for (int j = 0; j < colunmStrs.length; j++) {
                            if (j == 0) {
                                mapTwo.put("debitNoteNum", FieldValUtils.getFieldValueByFieldName("debitNoteNum", excel2));
                            } else {
                                if ("functionalAmountNoWriteoff".equals(colunmStrs[j])) {
                                    if (excel2.getFunctionalAmount() != null) {
                                        BigDecimal noWriteoff = excel2.getFunctionalAmountWriteoff() != null ? excel2.getFunctionalAmountWriteoff() : BigDecimal.ZERO;
                                        mapTwo.put("functionalAmountNoWriteoff", decimalFormat2.format(excel2.getFunctionalAmount().subtract(noWriteoff)));
                                    } else {
                                        mapTwo.put("functionalAmountNoWriteoff", "");
                                    }
                                } else if ("functionalAmount".equals(colunmStrs[j])) {
                                    if (excel2.getFunctionalAmount() != null) {
                                        mapTwo.put("functionalAmount", decimalFormat2.format(excel2.getFunctionalAmount()));
                                    } else {
                                        mapTwo.put("functionalAmount", "");
                                    }
                                } else if ("functionalAmountWriteoff".equals(colunmStrs[j])) {
                                    if (excel2.getFunctionalAmountWriteoff() != null) {
                                        mapTwo.put("functionalAmountWriteoff", decimalFormat2.format(excel2.getFunctionalAmountWriteoff()));
                                    } else {
                                        mapTwo.put("functionalAmountWriteoff", "");
                                    }

                                } else if ("invoiceRemark".equals(colunmStrs[j])) {
                                    if (excel2.getStatementId() != null) {
                                        mapTwo.put("invoiceRemark", excel2.getInvoiceRemark2());
                                    } else {
                                        mapTwo.put("invoiceRemark", excel2.getInvoiceRemark());
                                    }
                                } else if ("createTime".equals(colunmStrs[j])) {
                                    if (excel2.getCreateTime() != null) {
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                        mapTwo.put("createTime", formatter.format(excel2.getCreateTime()));
                                    } else {
                                        mapTwo.put("createTime", "");
                                    }
                                } else if ("writeoffNum".equals(colunmStrs[j])) {
                                    StringBuffer sb = new StringBuffer();
                                    if (excel2.getWriteoffNum() != null && !"".equals(excel2.getWriteoffNum())) {
                                        if (excel2.getWriteoffNum().contains("  ")) {
                                            String[] array = excel2.getWriteoffNum().split("  ");
                                            for (int k = 0; k < array.length; k++) {
                                                sb.append(array[k].split(" ")[1]).append(" ");
                                            }
                                            mapTwo.put("writeoffNum", sb.toString());
                                        } else {
                                            mapTwo.put("writeoffNum", excel2.getWriteoffNum().split(" ")[1]);
                                        }
                                    } else {
                                        mapTwo.put("writeoffNum", "");
                                    }

                                } else if ("invoiceNum".equals(colunmStrs[j])) {
                                    StringBuffer sb = new StringBuffer();
                                    if (excel2.getInvoiceNum() != null && !"".equals(excel2.getInvoiceNum())) {
                                        if (excel2.getInvoiceNum().contains("  ")) {
                                            String[] array = excel2.getInvoiceNum().split("  ");
                                            for (int k = 0; k < array.length; k++) {
                                                sb.append(array[k].split(" ")[1]).append(" ");
                                            }
                                            mapTwo.put("invoiceNum", sb.toString());
                                        } else {
                                            mapTwo.put("invoiceNum", excel2.getInvoiceNum().split(" ")[1]);
                                        }
                                    } else {
                                        mapTwo.put("invoiceNum", "");
                                    }

                                } else if ("invoiceCreateTime".equals(colunmStrs[j])) {
                                    if (excel2.getInvoiceCreateTime() != null) {
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                        mapTwo.put("invoiceCreateTime", formatter.format(excel2.getInvoiceCreateTime()));
                                    } else {
                                        mapTwo.put("invoiceCreateTime", "");
                                    }
                                } else if ("invoiceCreatorName".equals(colunmStrs[j])) {
                                    if (excel2.getInvoiceCreatorName() != null) {
                                        mapTwo.put("invoiceCreatorName", excel2.getInvoiceCreatorName().split(" ")[0]);
                                    } else {
                                        mapTwo.put("invoiceCreatorName", "");
                                    }
                                } else {
                                    mapTwo.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], excel2));
                                }
                            }
                        }
                        listExcel.add(mapTwo);
                    }
                }
            }
            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(LoginUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");
        }
    }
}
