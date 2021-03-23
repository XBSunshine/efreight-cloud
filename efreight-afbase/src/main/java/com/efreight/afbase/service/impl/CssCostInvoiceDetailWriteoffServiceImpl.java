package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.dao.*;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.utils.LoginUtils;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * CSS 应收：发票明细 核销表 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-04
 */
@Service
@AllArgsConstructor
public class CssCostInvoiceDetailWriteoffServiceImpl extends ServiceImpl<CssCostInvoiceDetailWriteoffMapper, CssCostInvoiceDetailWriteoff> implements CssCostInvoiceDetailWriteoffService {

    private final CssPaymentMapper cssPaymentMapper;
    private final CssCostInvoiceMapper cssCostInvoiceMapper;
    private final CssCostInvoiceDetailMapper cssCostInvoiceDetailMapper;
    private final CssPaymentDetailService cssPaymentDetailService;
    private final AfCostService afCostService;
    private final ScCostService scCostService;
    private final TcCostService tcCostService;
    private final LcCostService lcCostService;
    private final IoCostService ioCostService;
    private final AfOrderService afOrderService;
    private final LcOrderService lcOrderService;
    private final IoOrderService ioOrderService;
    private final CssCostFilesMapper cssCostFilesMapper;

    @Override
    public IPage getPage(Page page, CssCostInvoiceDetailWriteoff cssCostInvoiceDetailWriteoff) {
        LambdaQueryWrapper<CssCostInvoiceDetailWriteoff> wrapper = Wrappers.<CssCostInvoiceDetailWriteoff>lambdaQuery();
        wrapper.eq(CssCostInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoiceDetailWriteoff::getBusinessScope, cssCostInvoiceDetailWriteoff.getBusinessScope());
        if (StrUtil.isNotBlank(cssCostInvoiceDetailWriteoff.getCurrency())) {
            wrapper.eq(CssCostInvoiceDetailWriteoff::getCurrency, cssCostInvoiceDetailWriteoff.getCurrency());
        }
        if (StrUtil.isNotBlank(cssCostInvoiceDetailWriteoff.getCustomerName())) {
            wrapper.like(CssCostInvoiceDetailWriteoff::getCustomerName, cssCostInvoiceDetailWriteoff.getCustomerName());
        }
        if (StrUtil.isNotBlank(cssCostInvoiceDetailWriteoff.getPaymentNum())) {
            LambdaQueryWrapper<CssPayment> cssPaymentLambdaQueryWrapper = Wrappers.<CssPayment>lambdaQuery();
            cssPaymentLambdaQueryWrapper.eq(CssPayment::getOrgId, SecurityUtils.getUser().getOrgId()).like(CssPayment::getPaymentNum, cssCostInvoiceDetailWriteoff.getPaymentNum()).eq(CssPayment::getBusinessScope, cssCostInvoiceDetailWriteoff.getBusinessScope());
            List<Integer> paymentIds = cssPaymentMapper.selectList(cssPaymentLambdaQueryWrapper).stream().map(CssPayment::getPaymentId).collect(Collectors.toList());
            if (paymentIds.isEmpty()) {
                page.setTotal(0);
                page.setRecords(new ArrayList());
                return page;
            }
            wrapper.in(CssCostInvoiceDetailWriteoff::getPaymentId, paymentIds);
        }
        if (StrUtil.isNotBlank(cssCostInvoiceDetailWriteoff.getInvoiceNum()) || cssCostInvoiceDetailWriteoff.getInvoiceDateStart() != null || cssCostInvoiceDetailWriteoff.getInvoiceDateEnd() != null) {
            LambdaQueryWrapper<CssCostInvoiceDetail> cssCostInvoiceDetailLambdaQueryWrapper = Wrappers.<CssCostInvoiceDetail>lambdaQuery();
            cssCostInvoiceDetailLambdaQueryWrapper.eq(CssCostInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoiceDetail::getBusinessScope, cssCostInvoiceDetailWriteoff.getBusinessScope());
            if (StrUtil.isNotBlank(cssCostInvoiceDetailWriteoff.getInvoiceNum())) {
                cssCostInvoiceDetailLambdaQueryWrapper.like(CssCostInvoiceDetail::getInvoiceNum, cssCostInvoiceDetailWriteoff.getInvoiceNum());
            }
            if (cssCostInvoiceDetailWriteoff.getInvoiceDateStart() != null) {
                cssCostInvoiceDetailLambdaQueryWrapper.ge(CssCostInvoiceDetail::getInvoiceDate, cssCostInvoiceDetailWriteoff.getInvoiceDateStart());
            }
            if (cssCostInvoiceDetailWriteoff.getInvoiceDateEnd() != null) {
                cssCostInvoiceDetailLambdaQueryWrapper.le(CssCostInvoiceDetail::getInvoiceDate, cssCostInvoiceDetailWriteoff.getInvoiceDateEnd());
            }
            List<Integer> invoiceDetailIds = cssCostInvoiceDetailMapper.selectList(cssCostInvoiceDetailLambdaQueryWrapper).stream().map(CssCostInvoiceDetail::getInvoiceDetailId).collect(Collectors.toList());
            if (invoiceDetailIds.isEmpty()) {
                page.setTotal(0);
                page.setRecords(new ArrayList());
                return page;
            }
            wrapper.in(CssCostInvoiceDetailWriteoff::getInvoiceDetailId, invoiceDetailIds);
        }
        if (StrUtil.isNotBlank(cssCostInvoiceDetailWriteoff.getWriteoffNum())) {
            wrapper.like(CssCostInvoiceDetailWriteoff::getWriteoffNum, cssCostInvoiceDetailWriteoff.getWriteoffNum());
        }
        if (StrUtil.isNotBlank(cssCostInvoiceDetailWriteoff.getCreatorName())) {
            wrapper.like(CssCostInvoiceDetailWriteoff::getCreatorName, cssCostInvoiceDetailWriteoff.getCreatorName());
        }
        if (cssCostInvoiceDetailWriteoff.getWriteoffDateStart() != null) {
            wrapper.ge(CssCostInvoiceDetailWriteoff::getWriteoffDate, cssCostInvoiceDetailWriteoff.getWriteoffDateStart());
        }
        if (cssCostInvoiceDetailWriteoff.getWriteoffDateEnd() != null) {
            wrapper.le(CssCostInvoiceDetailWriteoff::getWriteoffDate, cssCostInvoiceDetailWriteoff.getWriteoffDateEnd());
        }
        if (cssCostInvoiceDetailWriteoff.getCreateTimeStart() != null) {
            wrapper.ge(CssCostInvoiceDetailWriteoff::getCreateTime, cssCostInvoiceDetailWriteoff.getCreateTimeStart());
        }
        if (cssCostInvoiceDetailWriteoff.getCreateTimeEnd() != null) {
            wrapper.le(CssCostInvoiceDetailWriteoff::getCreateTime, cssCostInvoiceDetailWriteoff.getCreateTimeEnd());
        }
        if (StrUtil.isNotBlank(cssCostInvoiceDetailWriteoff.getFinancialAccountCode())) {
            wrapper.like(CssCostInvoiceDetailWriteoff::getFinancialAccountCode, cssCostInvoiceDetailWriteoff.getFinancialAccountCode());
        }
        wrapper.orderByDesc(CssCostInvoiceDetailWriteoff::getInvoiceDetailWriteoffId);
        IPage<CssCostInvoiceDetailWriteoff> result = page(page, wrapper);
        result.getRecords().stream().forEach(item -> {
            CssPayment cssPayment = cssPaymentMapper.selectById(item.getPaymentId());
            item.setPaymentNum(cssPayment.getPaymentNum());
            CssCostInvoiceDetail cssCostInvoiceDetail = cssCostInvoiceDetailMapper.selectById(item.getInvoiceDetailId());
            item.setInvoiceNum(cssCostInvoiceDetail.getInvoiceNum());
            item.setInvoiceDate(cssCostInvoiceDetail.getInvoiceDate());
            item.setInvoiceAmount(cssCostInvoiceDetail.getAmount());
            item.setInvoiceAmountStr(FormatUtils.formatWithQWF(item.getInvoiceAmount(), 2) + " (" + item.getCurrency() + ")");
            item.setAmountWriteoffStr(FormatUtils.formatWithQWF(item.getAmountWriteoff(), 2) + " (" + item.getCurrency() + ")");
            LambdaQueryWrapper<CssCostFiles> cssCostFilesLambdaQueryWrapper = Wrappers.<CssCostFiles>lambdaQuery();
            cssCostFilesLambdaQueryWrapper.eq(CssCostFiles::getInvoiceDetailWriteoffId, item.getInvoiceDetailWriteoffId()).eq(CssCostFiles::getOrgId, SecurityUtils.getUser().getOrgId());
            item.setFilesList(cssCostFilesMapper.selectList(cssCostFilesLambdaQueryWrapper));
        });
        //拼接合计
        if (result.getRecords().size() != 0) {
            HashMap<String, BigDecimal> amountWriteoffMap = new HashMap<>();
            list(wrapper).stream().forEach(writeoff -> {
                if (amountWriteoffMap.get(writeoff.getCurrency()) == null) {
                    amountWriteoffMap.put(writeoff.getCurrency(), writeoff.getAmountWriteoff());
                } else {
                    amountWriteoffMap.put(writeoff.getCurrency(), amountWriteoffMap.get(writeoff.getCurrency()).add(writeoff.getAmountWriteoff()));
                }
            });
            StringBuffer amountWriteoffBuffer = new StringBuffer();
            for (Map.Entry<String, BigDecimal> entry : amountWriteoffMap.entrySet()) {
                if (amountWriteoffBuffer.length() == 0) {
                    amountWriteoffBuffer.append(FormatUtils.formatWithQWF(entry.getValue(), 2) + " (" + entry.getKey() + ")");
                } else {
                    amountWriteoffBuffer.append("|").append(FormatUtils.formatWithQWF(entry.getValue(), 2) + " (" + entry.getKey() + ")");
                }
            }
            CssCostInvoiceDetailWriteoff writeoff = new CssCostInvoiceDetailWriteoff();
            writeoff.setAmountWriteoff(BigDecimal.ZERO);
            writeoff.setAmountWriteoffStr(amountWriteoffBuffer.toString());
            result.getRecords().add(writeoff);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(CssCostInvoiceDetailWriteoff cssCostInvoiceDetailWriteoff) {
        //校验
        CssCostInvoiceDetail cssCostInvoiceDetail = cssCostInvoiceDetailMapper.selectById(cssCostInvoiceDetailWriteoff.getInvoiceDetailId());
        if (cssCostInvoiceDetail == null) {
            throw new RuntimeException("收票不存在，请刷新重试");
        }
        CssCostInvoice cssCostInvoice = cssCostInvoiceMapper.selectById(cssCostInvoiceDetail.getInvoiceId());
        if (cssCostInvoice == null) {
            throw new RuntimeException("付款申请不存在，请刷新重试");
        }
        if (!cssCostInvoice.getRowUuid().equals(cssCostInvoiceDetailWriteoff.getRowUuid())) {
            throw new RuntimeException("您好，数据不是最新 请刷新重试。");
        }
        CssPayment cssPayment = cssPaymentMapper.selectById(cssCostInvoice.getPaymentId());
        if (cssPayment == null) {
            throw new RuntimeException("账单不存在，请刷新重试");
        }
        //保存核销单
        cssCostInvoiceDetailWriteoff.setOrgId(SecurityUtils.getUser().getOrgId());
        cssCostInvoiceDetailWriteoff.setCreatorId(SecurityUtils.getUser().getId());
        cssCostInvoiceDetailWriteoff.setCreatorName(SecurityUtils.getUser().buildOptName());
        cssCostInvoiceDetailWriteoff.setCreateTime(LocalDateTime.now());
        cssCostInvoiceDetailWriteoff.setWriteoffNum(getWriteoffNum(cssCostInvoiceDetail.getBusinessScope()));
        cssCostInvoiceDetailWriteoff.setInvoiceId(cssCostInvoice.getInvoiceId());
        cssCostInvoiceDetailWriteoff.setPaymentId(cssPayment.getPaymentId());
        cssCostInvoiceDetailWriteoff.setBusinessScope(cssCostInvoiceDetail.getBusinessScope());
        cssCostInvoiceDetailWriteoff.setRowUuid(UUID.randomUUID().toString());
        save(cssCostInvoiceDetailWriteoff);

        //更新收票表
        LambdaQueryWrapper<CssCostInvoiceDetailWriteoff> cssCostInvoiceDetailWriteoffWrapper = Wrappers.<CssCostInvoiceDetailWriteoff>lambdaQuery();
        cssCostInvoiceDetailWriteoffWrapper.eq(CssCostInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoiceDetailWriteoff::getInvoiceDetailId, cssCostInvoiceDetail.getInvoiceDetailId());
        List<CssCostInvoiceDetailWriteoff> writeoffList = list(cssCostInvoiceDetailWriteoffWrapper);
        HashMap<String, BigDecimal> amountWriteoffSum = new HashMap<>();
        amountWriteoffSum.put("sum", BigDecimal.ZERO);
        writeoffList.stream().forEach(item -> {
            if (item.getAmountWriteoff() != null) {
                amountWriteoffSum.put("sum", amountWriteoffSum.get("sum").add(item.getAmountWriteoff()));
            }
        });
        cssCostInvoiceDetail.setAmountWriteoff(amountWriteoffSum.get("sum"));
        if (cssCostInvoiceDetail.getAmountWriteoff().compareTo(cssCostInvoiceDetail.getAmount()) == 0) {
            cssCostInvoiceDetail.setWriteoffComplete(1);
        } else {
            cssCostInvoiceDetail.setWriteoffComplete(0);
        }
        cssCostInvoiceDetail.setRowUuid(UUID.randomUUID().toString());
        cssCostInvoiceDetailMapper.updateById(cssCostInvoiceDetail);

        //更新申请表
        cssCostInvoice.setRowUuid(UUID.randomUUID().toString());
        cssCostInvoiceMapper.updateById(cssCostInvoice);
        //更新账单及账单明细
        List<Integer> costIds = null;
        List<Integer> orderIds = null;
        if (cssPayment.getAmountPayment().signum() == -1 || (cssPayment.getAmountPaymentWriteoff() == null ? BigDecimal.ZERO : cssPayment.getAmountPaymentWriteoff()).add(cssCostInvoiceDetailWriteoff.getAmountWriteoff()).compareTo(cssPayment.getAmountPayment()) == 0) {
            //账单金额为负数或者已核销金额等于对象金额的时候只能完全核销
            //更新账单表
            cssPayment.setAmountPaymentWriteoff(cssPayment.getAmountPayment());
            cssPayment.setFunctionalAmountPaymentWriteoff(cssPayment.getFunctionalAmountPayment());
            cssPayment.setWriteoffComplete(1);
            cssPayment.setRowUuid(UUID.randomUUID().toString());
            cssPaymentMapper.updateById(cssPayment);
            //更新账单明细表
            LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailLambdaQueryWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
            cssPaymentDetailLambdaQueryWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getPaymentId, cssPayment.getPaymentId());
            List<CssPaymentDetail> paymentDetailList = cssPaymentDetailService.list(cssPaymentDetailLambdaQueryWrapper);
            paymentDetailList.stream().forEach(item -> item.setAmountPaymentWriteoff(item.getAmountPayment()));
            cssPaymentDetailService.updateBatchById(paymentDetailList);

            //整理涉及cost的数据为修改cost准备
            costIds = paymentDetailList.stream().map(CssPaymentDetail::getCostId).collect(Collectors.toList());
            //整理涉及order的数据为修改order准备
            orderIds = paymentDetailList.stream().map(CssPaymentDetail::getOrderId).distinct().collect(Collectors.toList());
        } else {
            //更新账单表
            LambdaQueryWrapper<CssCostInvoiceDetail> cssCostInvoiceDetailWrapper = Wrappers.<CssCostInvoiceDetail>lambdaQuery();
            cssCostInvoiceDetailWrapper.eq(CssCostInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoiceDetail::getPaymentId, cssPayment.getPaymentId());
            List<CssCostInvoiceDetail> costInvoiceDetailList = cssCostInvoiceDetailMapper.selectList(cssCostInvoiceDetailWrapper);
            HashMap<String, BigDecimal> paymentWriteoffAmountSum = new HashMap<>();
            paymentWriteoffAmountSum.put("sum", BigDecimal.ZERO);
            costInvoiceDetailList.stream().forEach(item -> {
                if (item.getAmountWriteoff() != null) {
                    paymentWriteoffAmountSum.put("sum", paymentWriteoffAmountSum.get("sum").add(item.getAmountWriteoff()));
                }
            });
            cssPayment.setAmountPaymentWriteoff(paymentWriteoffAmountSum.get("sum"));
            cssPayment.setFunctionalAmountPaymentWriteoff(cssPayment.getAmountPaymentWriteoff().multiply(cssPayment.getExchangeRate()).setScale(2, BigDecimal.ROUND_HALF_UP));
            cssPayment.setWriteoffComplete(0);
            cssPayment.setRowUuid(UUID.randomUUID().toString());
            cssPaymentMapper.updateById(cssPayment);
            //更新账单明细表
            List<CssPaymentDetail> paymentDetailList = baseMapper.getPaymentDetailListOrderByFlightDateAndAmountDESC(cssPayment.getBusinessScope(), cssPayment.getPaymentId(), SecurityUtils.getUser().getOrgId());
            //整理涉及cost的数据为修改cost准备
            costIds = paymentDetailList.stream().map(CssPaymentDetail::getCostId).collect(Collectors.toList());
            //整理涉及order的数据为修改order准备
            orderIds = paymentDetailList.stream().map(CssPaymentDetail::getOrderId).distinct().collect(Collectors.toList());
            try {
                paymentDetailList.stream().forEach(item -> {
                    if (cssPayment.getAmountPaymentWriteoff().compareTo(item.getAmountPayment()) == -1) {
                        item.setAmountPaymentWriteoff(cssPayment.getAmountPaymentWriteoff());
                        throw new RuntimeException("完成跳出循环");
                    } else if (cssPayment.getAmountPaymentWriteoff().compareTo(item.getAmountPayment()) == 1) {
                        item.setAmountPaymentWriteoff(item.getAmountPayment());
                        cssPayment.setAmountPaymentWriteoff(cssPayment.getAmountPaymentWriteoff().subtract(item.getAmountPayment()));
                    } else {
                        item.setAmountPaymentWriteoff(item.getAmountPayment());
                        throw new RuntimeException("完成跳出循环");
                    }
                });
            } catch (Exception e) {

            }
            cssPaymentDetailService.updateBatchById(paymentDetailList);
        }
        //更新cost表
        LambdaQueryWrapper<CssPayment> cssPaymentLambdaQueryWrapper = Wrappers.<CssPayment>lambdaQuery();
        cssPaymentLambdaQueryWrapper.eq(CssPayment::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPayment::getBusinessScope, cssPayment.getBusinessScope());
        List<Integer> paymentIds = cssPaymentMapper.selectList(cssPaymentLambdaQueryWrapper).stream().map(CssPayment::getPaymentId).collect(Collectors.toList());
        HashMap<String, BigDecimal> costWriteoffAmountSum = new HashMap<>();
        if (cssPayment.getBusinessScope().startsWith("A")) {
            costIds.stream().forEach(costId -> {
                LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                cssPaymentDetailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, costId).in(CssPaymentDetail::getPaymentId, paymentIds);
                costWriteoffAmountSum.put("sum", BigDecimal.ZERO);
                cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().forEach(item -> {
                    if (item.getAmountPaymentWriteoff() != null) {
                        costWriteoffAmountSum.put("sum", costWriteoffAmountSum.get("sum").add(item.getAmountPaymentWriteoff()));
                    }
                });
                AfCost afCost = afCostService.getById(costId);
                afCost.setRowUuid(UUID.randomUUID().toString());
                afCost.setCostAmountWriteoff(costWriteoffAmountSum.get("sum"));
                afCostService.updateById(afCost);
            });
        } else if (cssPayment.getBusinessScope().startsWith("S")) {
            costIds.stream().forEach(costId -> {
                LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                cssPaymentDetailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, costId).in(CssPaymentDetail::getPaymentId, paymentIds);
                costWriteoffAmountSum.put("sum", BigDecimal.ZERO);
                cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().forEach(item -> {
                    if (item.getAmountPaymentWriteoff() != null) {
                        costWriteoffAmountSum.put("sum", costWriteoffAmountSum.get("sum").add(item.getAmountPaymentWriteoff()));
                    }
                });
                ScCost scCost = scCostService.getById(costId);
                scCost.setRowUuid(UUID.randomUUID().toString());
                scCost.setCostAmountWriteoff(costWriteoffAmountSum.get("sum"));
                scCostService.updateById(scCost);
            });
        } else if (cssPayment.getBusinessScope().startsWith("T")) {
            costIds.stream().forEach(costId -> {
                LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                cssPaymentDetailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, costId).in(CssPaymentDetail::getPaymentId, paymentIds);
                costWriteoffAmountSum.put("sum", BigDecimal.ZERO);
                cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().forEach(item -> {
                    if (item.getAmountPaymentWriteoff() != null) {
                        costWriteoffAmountSum.put("sum", costWriteoffAmountSum.get("sum").add(item.getAmountPaymentWriteoff()));
                    }
                });
                TcCost tcCost = tcCostService.getById(costId);
                tcCost.setRowUuid(UUID.randomUUID().toString());
                tcCost.setCostAmountWriteoff(costWriteoffAmountSum.get("sum"));
                tcCostService.updateById(tcCost);
            });
        } else if (cssPayment.getBusinessScope().equals("LC")) {
            costIds.stream().forEach(costId -> {
                LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                cssPaymentDetailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, costId).in(CssPaymentDetail::getPaymentId, paymentIds);
                costWriteoffAmountSum.put("sum", BigDecimal.ZERO);
                cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().forEach(item -> {
                    if (item.getAmountPaymentWriteoff() != null) {
                        costWriteoffAmountSum.put("sum", costWriteoffAmountSum.get("sum").add(item.getAmountPaymentWriteoff()));
                    }
                });
                LcCost lcCost = lcCostService.getById(costId);
                lcCost.setRowUuid(UUID.randomUUID().toString());
                lcCost.setCostAmountWriteoff(costWriteoffAmountSum.get("sum"));
                lcCostService.updateById(lcCost);
            });
        } else if (cssPayment.getBusinessScope().equals("IO")) {
            costIds.stream().forEach(costId -> {
                LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                cssPaymentDetailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, costId).in(CssPaymentDetail::getPaymentId, paymentIds);
                costWriteoffAmountSum.put("sum", BigDecimal.ZERO);
                cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().forEach(item -> {
                    if (item.getAmountPaymentWriteoff() != null) {
                        costWriteoffAmountSum.put("sum", costWriteoffAmountSum.get("sum").add(item.getAmountPaymentWriteoff()));
                    }
                });
                IoCost ioCost = ioCostService.getById(costId);
                ioCost.setRowUuid(UUID.randomUUID().toString());
                ioCost.setCostAmountWriteoff(costWriteoffAmountSum.get("sum"));
                ioCostService.updateById(ioCost);
            });
        }

        //更新订单表
        orderIds.stream().forEach(orderId -> {
            if ("AE".equals(cssPayment.getBusinessScope()) || "AI".equals(cssPayment.getBusinessScope())) {
                AfOrder order = afOrderService.getById(orderId);
                order.setCostStatus(afOrderService.getOrderCostStatusForAF(orderId));
                order.setRowUuid(UUID.randomUUID().toString());
                afOrderService.updateById(order);
            }
            if ("SE".equals(cssPayment.getBusinessScope()) || "SI".equals(cssPayment.getBusinessScope())) {
                afOrderService.updateOrderCostStatusForSC(orderId);
            }
            if (cssPayment.getBusinessScope().startsWith("T")) {
                afOrderService.updateOrderCostStatusForTC(orderId);
            }
            if (cssPayment.getBusinessScope().startsWith("L")) {
                LcOrder order = lcOrderService.getById(orderId);
                order.setCostStatus(afOrderService.getOrderCostStatusForLC(orderId));
                order.setRowUuid(UUID.randomUUID().toString());
                lcOrderService.updateById(order);
            }
            if (cssPayment.getBusinessScope().equals("IO")) {
                IoOrder order = ioOrderService.getById(orderId);
                order.setCostStatus(afOrderService.getOrderCostStatusForIO(orderId));
                order.setRowUuid(UUID.randomUUID().toString());
                ioOrderService.updateById(order);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer invoiceDetailWriteoffId, String rowUuid) {
        //校验
        CssCostInvoiceDetailWriteoff cssCostInvoiceDetailWriteoff = getById(invoiceDetailWriteoffId);
        if (cssCostInvoiceDetailWriteoff == null) {
            throw new RuntimeException("核销单不存在，请刷新重试");
        }
        if (!cssCostInvoiceDetailWriteoff.getRowUuid().equals(rowUuid)) {
            throw new RuntimeException("您好，数据不是最新 请刷新重试。");
        }
        CssCostInvoiceDetail cssCostInvoiceDetail = cssCostInvoiceDetailMapper.selectById(cssCostInvoiceDetailWriteoff.getInvoiceDetailId());
        if (cssCostInvoiceDetail == null) {
            throw new RuntimeException("收票不存在，请刷新重试");
        }
        CssCostInvoice cssCostInvoice = cssCostInvoiceMapper.selectById(cssCostInvoiceDetail.getInvoiceId());
        if (cssCostInvoice == null) {
            throw new RuntimeException("付款申请不存在，请刷新重试");
        }
        CssPayment cssPayment = cssPaymentMapper.selectById(cssCostInvoice.getPaymentId());
        if (cssPayment == null) {
            throw new RuntimeException("账单不存在，请刷新重试");
        }
        //删除核销单
        removeById(invoiceDetailWriteoffId);

        //更新收票表
        LambdaQueryWrapper<CssCostInvoiceDetailWriteoff> cssCostInvoiceDetailWriteoffWrapper = Wrappers.<CssCostInvoiceDetailWriteoff>lambdaQuery();
        cssCostInvoiceDetailWriteoffWrapper.eq(CssCostInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoiceDetailWriteoff::getInvoiceDetailId, cssCostInvoiceDetail.getInvoiceDetailId());
        List<CssCostInvoiceDetailWriteoff> writeoffList = list(cssCostInvoiceDetailWriteoffWrapper);
        if (writeoffList.isEmpty()) {
            cssCostInvoiceDetail.setWriteoffComplete(null);
            cssCostInvoiceDetail.setAmountWriteoff(null);
        } else {
            cssCostInvoiceDetail.setWriteoffComplete(0);
            HashMap<String, BigDecimal> amountWriteoffSum = new HashMap<>();
            amountWriteoffSum.put("sum", BigDecimal.ZERO);
            writeoffList.stream().forEach(item -> {
                if (item.getAmountWriteoff() != null) {
                    amountWriteoffSum.put("sum", amountWriteoffSum.get("sum").add(item.getAmountWriteoff()));
                }
            });
            cssCostInvoiceDetail.setAmountWriteoff(amountWriteoffSum.get("sum"));
        }
        cssCostInvoiceDetail.setRowUuid(UUID.randomUUID().toString());
        cssCostInvoiceDetailMapper.updateById(cssCostInvoiceDetail);

        //更新申请表
        cssCostInvoice.setRowUuid(UUID.randomUUID().toString());
        cssCostInvoiceMapper.updateById(cssCostInvoice);
        //更新账单及账单明细
        List<Integer> costIds = null;
        List<Integer> orderIds = null;
        if (cssPayment.getAmountPayment().signum() == -1 || cssPayment.getAmountPaymentWriteoff().subtract(cssCostInvoiceDetailWriteoff.getAmountWriteoff()).signum() == 0) {
            //账单金额为负数或者已核销金额等于0的时候只能变成已对账状态
            //更新账单表
            cssPayment.setAmountPaymentWriteoff(null);
            cssPayment.setFunctionalAmountPaymentWriteoff(null);
            cssPayment.setWriteoffComplete(null);
            cssPayment.setRowUuid(UUID.randomUUID().toString());
            cssPaymentMapper.updateById(cssPayment);
            //更新账单明细表
            LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailLambdaQueryWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
            cssPaymentDetailLambdaQueryWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getPaymentId, cssPayment.getPaymentId());
            List<CssPaymentDetail> paymentDetailList = cssPaymentDetailService.list(cssPaymentDetailLambdaQueryWrapper);
            paymentDetailList.stream().forEach(item -> item.setAmountPaymentWriteoff(null));
            cssPaymentDetailService.updateBatchById(paymentDetailList);

            //整理涉及cost的数据为修改cost准备
            costIds = paymentDetailList.stream().map(CssPaymentDetail::getCostId).collect(Collectors.toList());
            //整理涉及order的数据为修改order准备
            orderIds = paymentDetailList.stream().map(CssPaymentDetail::getOrderId).distinct().collect(Collectors.toList());
        } else {
            //更新账单表
            LambdaQueryWrapper<CssCostInvoiceDetail> cssCostInvoiceDetailWrapper = Wrappers.<CssCostInvoiceDetail>lambdaQuery();
            cssCostInvoiceDetailWrapper.eq(CssCostInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoiceDetail::getPaymentId, cssPayment.getPaymentId());
            List<CssCostInvoiceDetail> costInvoiceDetailList = cssCostInvoiceDetailMapper.selectList(cssCostInvoiceDetailWrapper);
            HashMap<String, BigDecimal> paymentWriteoffAmountSum = new HashMap<>();
            paymentWriteoffAmountSum.put("sum", BigDecimal.ZERO);
            costInvoiceDetailList.stream().forEach(item -> {
                if (item.getAmountWriteoff() != null) {
                    paymentWriteoffAmountSum.put("sum", paymentWriteoffAmountSum.get("sum").add(item.getAmountWriteoff()));
                }
            });
            cssPayment.setAmountPaymentWriteoff(paymentWriteoffAmountSum.get("sum"));
            cssPayment.setFunctionalAmountPaymentWriteoff(cssPayment.getAmountPaymentWriteoff().multiply(cssPayment.getExchangeRate()).setScale(2, BigDecimal.ROUND_HALF_UP));
            cssPayment.setWriteoffComplete(0);
            cssPayment.setRowUuid(UUID.randomUUID().toString());
            cssPaymentMapper.updateById(cssPayment);
            //更新账单明细表
            List<CssPaymentDetail> paymentDetailList = baseMapper.getPaymentDetailListOrderByFlightDateAndAmountDESC(cssPayment.getBusinessScope(), cssPayment.getPaymentId(), SecurityUtils.getUser().getOrgId());
            costIds = paymentDetailList.stream().map(CssPaymentDetail::getCostId).collect(Collectors.toList());
            //整理涉及order的数据为修改order准备
            orderIds = paymentDetailList.stream().map(CssPaymentDetail::getOrderId).distinct().collect(Collectors.toList());
            try {
                paymentDetailList.stream().forEach(item -> {
                    if (cssPayment.getAmountPaymentWriteoff().compareTo(item.getAmountPayment()) == -1) {
                        item.setAmountPaymentWriteoff(cssPayment.getAmountPaymentWriteoff());
                        throw new RuntimeException("完成跳出循环");
                    } else if (cssPayment.getAmountPaymentWriteoff().compareTo(item.getAmountPayment()) == 1) {
                        item.setAmountPaymentWriteoff(item.getAmountPayment());
                        cssPayment.setAmountPaymentWriteoff(cssPayment.getAmountPaymentWriteoff().subtract(item.getAmountPayment()));
                    } else {
                        item.setAmountPaymentWriteoff(item.getAmountPayment());
                        throw new RuntimeException("完成跳出循环");
                    }
                });
            } catch (Exception e) {

            }
            cssPaymentDetailService.updateBatchById(paymentDetailList);
        }
        //更新cost表
        LambdaQueryWrapper<CssPayment> cssPaymentLambdaQueryWrapper = Wrappers.<CssPayment>lambdaQuery();
        cssPaymentLambdaQueryWrapper.eq(CssPayment::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPayment::getBusinessScope, cssPayment.getBusinessScope());
        List<Integer> paymentIds = cssPaymentMapper.selectList(cssPaymentLambdaQueryWrapper).stream().map(CssPayment::getPaymentId).collect(Collectors.toList());
        HashMap<String, BigDecimal> costWriteoffAmountSum = new HashMap<>();
        if (cssPayment.getBusinessScope().startsWith("A")) {
            costIds.stream().forEach(costId -> {
                LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                cssPaymentDetailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, costId).in(CssPaymentDetail::getPaymentId, paymentIds);
                costWriteoffAmountSum.put("sum", BigDecimal.ZERO);
                cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().forEach(item -> {
                    if (item.getAmountPaymentWriteoff() != null) {
                        costWriteoffAmountSum.put("sum", costWriteoffAmountSum.get("sum").add(item.getAmountPaymentWriteoff()));
                    }
                });
                AfCost afCost = afCostService.getById(costId);
                afCost.setRowUuid(UUID.randomUUID().toString());
                afCost.setCostAmountWriteoff(costWriteoffAmountSum.get("sum"));
                afCostService.updateById(afCost);
            });
        } else if (cssPayment.getBusinessScope().startsWith("S")) {
            costIds.stream().forEach(costId -> {
                LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                cssPaymentDetailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, costId).in(CssPaymentDetail::getPaymentId, paymentIds);
                costWriteoffAmountSum.put("sum", BigDecimal.ZERO);
                cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().forEach(item -> {
                    if (item.getAmountPaymentWriteoff() != null) {
                        costWriteoffAmountSum.put("sum", costWriteoffAmountSum.get("sum").add(item.getAmountPaymentWriteoff()));
                    }
                });
                ScCost scCost = scCostService.getById(costId);
                scCost.setRowUuid(UUID.randomUUID().toString());
                scCost.setCostAmountWriteoff(costWriteoffAmountSum.get("sum"));
                scCostService.updateById(scCost);
            });
        } else if (cssPayment.getBusinessScope().startsWith("T")) {
            costIds.stream().forEach(costId -> {
                LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                cssPaymentDetailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, costId).in(CssPaymentDetail::getPaymentId, paymentIds);
                costWriteoffAmountSum.put("sum", BigDecimal.ZERO);
                cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().forEach(item -> {
                    if (item.getAmountPaymentWriteoff() != null) {
                        costWriteoffAmountSum.put("sum", costWriteoffAmountSum.get("sum").add(item.getAmountPaymentWriteoff()));
                    }
                });
                TcCost tcCost = tcCostService.getById(costId);
                tcCost.setRowUuid(UUID.randomUUID().toString());
                tcCost.setCostAmountWriteoff(costWriteoffAmountSum.get("sum"));
                tcCostService.updateById(tcCost);
            });
        } else if (cssPayment.getBusinessScope().equals("LC")) {
            costIds.stream().forEach(costId -> {
                LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                cssPaymentDetailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, costId).in(CssPaymentDetail::getPaymentId, paymentIds);
                costWriteoffAmountSum.put("sum", BigDecimal.ZERO);
                cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().forEach(item -> {
                    if (item.getAmountPaymentWriteoff() != null) {
                        costWriteoffAmountSum.put("sum", costWriteoffAmountSum.get("sum").add(item.getAmountPaymentWriteoff()));
                    }
                });
                LcCost lcCost = lcCostService.getById(costId);
                lcCost.setRowUuid(UUID.randomUUID().toString());
                lcCost.setCostAmountWriteoff(costWriteoffAmountSum.get("sum"));
                lcCostService.updateById(lcCost);
            });
        } else if (cssPayment.getBusinessScope().equals("IO")) {
            costIds.stream().forEach(costId -> {
                LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                cssPaymentDetailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, costId).in(CssPaymentDetail::getPaymentId, paymentIds);
                costWriteoffAmountSum.put("sum", BigDecimal.ZERO);
                cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().forEach(item -> {
                    if (item.getAmountPaymentWriteoff() != null) {
                        costWriteoffAmountSum.put("sum", costWriteoffAmountSum.get("sum").add(item.getAmountPaymentWriteoff()));
                    }
                });
                IoCost ioCost = ioCostService.getById(costId);
                ioCost.setRowUuid(UUID.randomUUID().toString());
                ioCost.setCostAmountWriteoff(costWriteoffAmountSum.get("sum"));
                ioCostService.updateById(ioCost);
            });
        }

        //更新订单表
        orderIds.stream().forEach(orderId -> {
            if ("AE".equals(cssPayment.getBusinessScope()) || "AI".equals(cssPayment.getBusinessScope())) {
                AfOrder order = afOrderService.getById(orderId);
                order.setCostStatus(afOrderService.getOrderCostStatusForAF(orderId));
                order.setRowUuid(UUID.randomUUID().toString());
                afOrderService.updateById(order);
            }
            if ("SE".equals(cssPayment.getBusinessScope()) || "SI".equals(cssPayment.getBusinessScope())) {
                afOrderService.updateOrderCostStatusForSC(orderId);
            }
            if (cssPayment.getBusinessScope().startsWith("T")) {
                afOrderService.updateOrderCostStatusForTC(orderId);
            }
            if (cssPayment.getBusinessScope().startsWith("L")) {
                LcOrder order = lcOrderService.getById(orderId);
                order.setCostStatus(afOrderService.getOrderCostStatusForLC(orderId));
                order.setRowUuid(UUID.randomUUID().toString());
                lcOrderService.updateById(order);
            }
            if (cssPayment.getBusinessScope().equals("IO")) {
                IoOrder order = ioOrderService.getById(orderId);
                order.setCostStatus(afOrderService.getOrderCostStatusForIO(orderId));
                order.setRowUuid(UUID.randomUUID().toString());
                ioOrderService.updateById(order);
            }
        });
        //删除附件
        LambdaQueryWrapper<CssCostFiles> wrapper = Wrappers.<CssCostFiles>lambdaQuery();
        wrapper.eq(CssCostFiles::getInvoiceDetailWriteoffId, invoiceDetailWriteoffId).eq(CssCostFiles::getOrgId, SecurityUtils.getUser().getOrgId());
        cssCostFilesMapper.delete(wrapper);
    }

    @Override
    public void checkIfCompleteVoucher(Integer invoiceDetailWriteoffId) {
        CssCostInvoiceDetailWriteoff cssCostInvoiceDetailWriteoff = getById(invoiceDetailWriteoffId);
        if (cssCostInvoiceDetailWriteoff != null && cssCostInvoiceDetailWriteoff.getVoucherNumber() != null) {
            throw new RuntimeException("当前核销单已导凭证");
        }
    }

    @Override
    public void exportExcel(CssCostInvoiceDetailWriteoff cssCostInvoiceDetailWriteoff) {
        //自定义字段
        Page<CssCostInvoiceDetailWriteoff> page = new Page<>();
        page.setCurrent(1);
        page.setSize(10000);
        List<CssCostInvoiceDetailWriteoff> list = this.getPage(page, cssCostInvoiceDetailWriteoff).getRecords();
        if (!StringUtils.isEmpty(cssCostInvoiceDetailWriteoff.getColumnStrs())) {
            List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(cssCostInvoiceDetailWriteoff.getColumnStrs());
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
            //遍历结果集 区相应字段封装 linkedHashMap后存储list发往工具类
            if (list != null && list.size() > 0) {
                for (CssCostInvoiceDetailWriteoff writeoff : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        if ("creatorName".equals(colunmStrs[j])) {
                            if (StrUtil.isNotBlank(FieldValUtils.getFieldValueByFieldName(colunmStrs[j], writeoff))) {
                                map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], writeoff).split(" ")[0]);
                            } else {
                                map.put(colunmStrs[j], "");
                            }
                        } else if ("createTime".equals(colunmStrs[j])) {
                            if (writeoff.getCreateTime() != null) {
                                map.put(colunmStrs[j], writeoff.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                            } else {
                                map.put(colunmStrs[j], "");
                            }
                        } else if ("invoiceAmount".equals(colunmStrs[j]) || "amountWriteoff".equals(colunmStrs[j])) {
                            if (StrUtil.isNotBlank(FieldValUtils.getFieldValueByFieldName(colunmStrs[j], writeoff))) {
                                map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j] + "Str", writeoff).replaceAll("\\|", "\n"));
                            } else {
                                map.put(colunmStrs[j], "");
                            }
                        } else if ("filesList".equals(colunmStrs[j])) {
                            if (writeoff.getFilesList() != null && !writeoff.getFilesList().isEmpty()) {
                                StringBuilder stringBuilder = new StringBuilder();
                                writeoff.getFilesList().stream().forEach(item -> {
                                    stringBuilder.append(item.getFileUrl()).append("\n");
                                });
                                map.put(colunmStrs[j], stringBuilder.toString());
                            } else {
                                map.put(colunmStrs[j], "");
                            }
                        } else {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], writeoff));
                        }
                    }
                    listExcel.add(map);
                }
                listExcel.get(list.size() - 1).put(colunmStrs[0], "合计：");
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(LoginUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");

        }
    }

    @Override
    public String getWriteoffNum(String businessScope) {
        String numberPrefix = "-PW-" + DateTimeFormatter.ofPattern("yyMMdd").format(LocalDate.now());
        LambdaQueryWrapper<CssCostInvoiceDetailWriteoff> wrapper = Wrappers.<CssCostInvoiceDetailWriteoff>lambdaQuery();
        wrapper.eq(CssCostInvoiceDetailWriteoff::getBusinessScope, businessScope).eq(CssCostInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).like(CssCostInvoiceDetailWriteoff::getWriteoffNum, numberPrefix).orderByDesc(CssCostInvoiceDetailWriteoff::getWriteoffNum).last(" limit 1");

        CssCostInvoiceDetailWriteoff cssCostInvoiceDetailWriteoff = getOne(wrapper);

        String numberSuffix = "";
        if (cssCostInvoiceDetailWriteoff == null) {
            numberSuffix = "0001";
        } else if (cssCostInvoiceDetailWriteoff.getWriteoffNum().substring(cssCostInvoiceDetailWriteoff.getWriteoffNum().length() - 4).equals("9999")) {
            throw new RuntimeException("当天核销单已满无法创建");
        } else {
            String n = Integer.valueOf(cssCostInvoiceDetailWriteoff.getWriteoffNum().substring(cssCostInvoiceDetailWriteoff.getWriteoffNum().length() - 4)) + 1 + "";
            numberSuffix = "0000".substring(0, 4 - n.length()) + n;
        }
        return businessScope + numberPrefix + numberSuffix;
    }
}
