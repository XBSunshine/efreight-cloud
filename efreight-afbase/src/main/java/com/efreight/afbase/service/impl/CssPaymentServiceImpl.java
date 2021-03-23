package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.dao.CssCostInvoiceMapper;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.dao.CssPaymentMapper;
import com.efreight.afbase.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.utils.LoginUtils;
import com.efreight.common.core.feign.RemoteCoopService;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.core.utils.JxlsUtils;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.common.security.vo.CoopVo;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>
 * CSS 成本对账单 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-05
 */
@Service
@AllArgsConstructor
public class CssPaymentServiceImpl extends ServiceImpl<CssPaymentMapper, CssPayment> implements CssPaymentService {

    private final CssPaymentDetailService cssPaymentDetailService;

    private final AfOrderService afOrderService;
    private final ScOrderService scOrderService;
    private final TcOrderService tcOrderService;
    private final LcOrderService lcOrderService;
    private final IoOrderService ioOrderService;
    private final AfCostService afCostService;
    private final ScCostService scCostService;
    private final TcCostService tcCostService;
    private final LcCostService lcCostService;
    private final IoCostService ioCostService;
    private final RemoteCoopService remoteCoopService;

    private final ServiceService serviceService;

    private final CssCostInvoiceMapper cssCostInvoiceMapper;

    private final CssCostInvoiceDetailService cssCostInvoiceDetailService;

    private final CssCostInvoiceDetailWriteoffService cssCostInvoiceDetailWriteoffService;

    @Override
    public IPage getPage(Page page, CssPayment cssPayment) {
        LambdaQueryWrapper<CssPayment> wrapper = Wrappers.<CssPayment>lambdaQuery();
        if (StrUtil.isNotBlank(cssPayment.getAwbNumberOrOrderCode())) {
            List<Integer> orderIds = null;
            if (cssPayment.getBusinessScope().startsWith("A")) {
                LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
                orderWrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(AfOrder::getAwbNumber, "%" + cssPayment.getAwbNumberOrOrderCode() + "%").or(j -> j.like(AfOrder::getOrderCode, "%" + cssPayment.getAwbNumberOrOrderCode() + "%")));
                List<AfOrder> orderList = afOrderService.list(orderWrapper);
                if (orderList.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                orderIds = orderList.stream().map(AfOrder::getOrderId).collect(Collectors.toList());
            }
            if (cssPayment.getBusinessScope().startsWith("S")) {
                LambdaQueryWrapper<ScOrder> orderWrapper = Wrappers.<ScOrder>lambdaQuery();
                orderWrapper.eq(ScOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(ScOrder::getMblNumber, "%" + cssPayment.getAwbNumberOrOrderCode() + "%").or(j -> j.like(ScOrder::getOrderCode, "%" + cssPayment.getAwbNumberOrOrderCode() + "%")));
                List<ScOrder> orderList = scOrderService.list(orderWrapper);
                if (orderList.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                orderIds = orderList.stream().map(ScOrder::getOrderId).collect(Collectors.toList());
            }
            if (cssPayment.getBusinessScope().startsWith("T")) {
                LambdaQueryWrapper<TcOrder> orderWrapper = Wrappers.<TcOrder>lambdaQuery();
                orderWrapper.eq(TcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(TcOrder::getRwbNumber, "%" + cssPayment.getAwbNumberOrOrderCode() + "%").or(j -> j.like(TcOrder::getOrderCode, "%" + cssPayment.getAwbNumberOrOrderCode() + "%")));
                List<TcOrder> orderList = tcOrderService.list(orderWrapper);
                if (orderList.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                orderIds = orderList.stream().map(TcOrder::getOrderId).collect(Collectors.toList());
            }
            if (cssPayment.getBusinessScope().startsWith("L")) {
                LambdaQueryWrapper<LcOrder> orderWrapper = Wrappers.<LcOrder>lambdaQuery();
                orderWrapper.eq(LcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(LcOrder::getCustomerNumber, "%" + cssPayment.getAwbNumberOrOrderCode() + "%").or(j -> j.like(LcOrder::getOrderCode, "%" + cssPayment.getAwbNumberOrOrderCode() + "%")));
                List<LcOrder> orderList = lcOrderService.list(orderWrapper);
                if (orderList.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                orderIds = orderList.stream().map(LcOrder::getOrderId).collect(Collectors.toList());
            }

            if (cssPayment.getBusinessScope().equals("IO")) {
                LambdaQueryWrapper<IoOrder> orderWrapper = Wrappers.<IoOrder>lambdaQuery();
                orderWrapper.eq(IoOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(IoOrder::getCustomerNumber, "%" + cssPayment.getAwbNumberOrOrderCode() + "%").or(j -> j.like(IoOrder::getOrderCode, "%" + cssPayment.getAwbNumberOrOrderCode() + "%")));
                List<IoOrder> orderList = ioOrderService.list(orderWrapper);
                if (orderList.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                orderIds = orderList.stream().map(IoOrder::getOrderId).collect(Collectors.toList());
            }

            if (orderIds != null) {
                LambdaQueryWrapper<CssPaymentDetail> detailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                detailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).in(CssPaymentDetail::getOrderId, orderIds);
                List<CssPaymentDetail> detailList = cssPaymentDetailService.list(detailWrapper);
                if (detailList.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                List<Integer> paymentIds = detailList.stream().map(CssPaymentDetail::getPaymentId).distinct().collect(Collectors.toList());
                wrapper.in(CssPayment::getPaymentId, paymentIds);
            }
        }
        if (StrUtil.isNotBlank(cssPayment.getInvoiceNum())) {
            LambdaQueryWrapper<CssCostInvoiceDetail> cssCostInvoiceDetailWrapper = Wrappers.<CssCostInvoiceDetail>lambdaQuery();
            cssCostInvoiceDetailWrapper.eq(CssCostInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId());
            if (StrUtil.isNotBlank(cssPayment.getInvoiceNum())) {
                cssCostInvoiceDetailWrapper.like(CssCostInvoiceDetail::getInvoiceNum, cssPayment.getInvoiceNum());
            }
            List<Integer> paymentIds = cssCostInvoiceDetailService.list(cssCostInvoiceDetailWrapper).stream().map(CssCostInvoiceDetail::getPaymentId).distinct().collect(Collectors.toList());
            if (paymentIds.size() == 0) {
                page.setTotal(0);
                page.setRecords(new ArrayList());
                return page;
            }
            wrapper.in(CssPayment::getPaymentId, paymentIds);
        }
        if (StrUtil.isNotBlank(cssPayment.getInvoiceCreatorName()) || StrUtil.isNotBlank(cssPayment.getInvoiceDateStart()) || StrUtil.isNotBlank(cssPayment.getInvoiceDateEnd())) {
            LambdaQueryWrapper<CssCostInvoice> cssCostInvoiceWrapper = Wrappers.<CssCostInvoice>lambdaQuery();
            cssCostInvoiceWrapper.eq(CssCostInvoice::getOrgId, SecurityUtils.getUser().getOrgId());
            if (StrUtil.isNotBlank(cssPayment.getInvoiceCreatorName())) {
                cssCostInvoiceWrapper.like(CssCostInvoice::getCreatorName, cssPayment.getInvoiceCreatorName());
            }
            if (StrUtil.isNotBlank(cssPayment.getInvoiceDateStart())) {
                cssCostInvoiceWrapper.ge(CssCostInvoice::getCreateTime, cssPayment.getInvoiceDateStart());
            }
            if (StrUtil.isNotBlank(cssPayment.getInvoiceDateEnd())) {
                cssCostInvoiceWrapper.le(CssCostInvoice::getCreateTime, cssPayment.getInvoiceDateEnd());
            }
            List<Integer> paymentIds = cssCostInvoiceMapper.selectList(cssCostInvoiceWrapper).stream().map(CssCostInvoice::getPaymentId).collect(Collectors.toList());
            if (paymentIds.size() == 0) {
                page.setTotal(0);
                page.setRecords(new ArrayList());
                return page;
            }
            wrapper.in(CssPayment::getPaymentId, paymentIds);
        }
        wrapper.eq(CssPayment::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPayment::getBusinessScope, cssPayment.getBusinessScope());
        if (StrUtil.isNotBlank(cssPayment.getCurrency())) {
            wrapper.eq(CssPayment::getCurrency, cssPayment.getCurrency());
        }
        if (StrUtil.isNotBlank(cssPayment.getCustomerName())) {
            wrapper.like(CssPayment::getCustomerName, "%" + cssPayment.getCustomerName() + "%");
        }
        if (StrUtil.isNotBlank(cssPayment.getPaymentNum())) {
            wrapper.like(CssPayment::getPaymentNum, "%" + cssPayment.getPaymentNum() + "%");
        }
        if (cssPayment.getPaymentDateStart() != null) {
            wrapper.ge(CssPayment::getPaymentDate, cssPayment.getPaymentDateStart());
        }
        if (cssPayment.getPaymentDateEnd() != null) {
            wrapper.le(CssPayment::getPaymentDate, cssPayment.getPaymentDateEnd());
        }
        if (StrUtil.isNotBlank(cssPayment.getCreatorName())) {
            wrapper.like(CssPayment::getCreatorName, cssPayment.getCreatorName());
        }
        if (StrUtil.isNotBlank(cssPayment.getWriteoffCompletes())) {
            StringBuffer lastSql = new StringBuffer(" and (");
            if (cssPayment.getWriteoffCompletes().contains("2")) {
                lastSql.append(" payment_id not in (select payment_id from css_cost_invoice where org_id=").append(SecurityUtils.getUser().getOrgId()).append(") or");
            }
            if (cssPayment.getWriteoffCompletes().contains("3")) {
                lastSql.append(" payment_id in (select payment_id from css_cost_invoice where invoice_status=-1 and org_id=").append(SecurityUtils.getUser().getOrgId()).append(") or");
            }
            if (cssPayment.getWriteoffCompletes().contains("4")) {
                lastSql.append(" payment_id in (select payment_id from css_cost_invoice where invoice_status=0 and org_id=").append(SecurityUtils.getUser().getOrgId()).append(") or");
            }
            if (cssPayment.getWriteoffCompletes().contains("5")) {
                lastSql.append(" writeoff_complete is null and payment_id in (select payment_id from css_cost_invoice where invoice_status=1 and org_id=").append(SecurityUtils.getUser().getOrgId()).append(") or");
            }
            if (cssPayment.getWriteoffCompletes().contains("0")) {
                lastSql.append(" writeoff_complete=0 or");
            }
            if (cssPayment.getWriteoffCompletes().contains("1")) {
                lastSql.append(" writeoff_complete=1 or");
            }
            wrapper.last(lastSql.substring(0, lastSql.length() - 3) + ") ORDER BY payment_num DESC");
        } else {
            wrapper.orderByDesc(CssPayment::getPaymentNum);
        }

//        if (StrUtil.isNotBlank(cssPayment.getWriteoffCompletes())) {
//            if (cssPayment.getWriteoffCompletes().contains("2")) {
//                wrapper.and(i -> i.in(CssPayment::getWriteoffComplete, cssPayment.getWriteoffCompletes().split(",")).or(j -> j.isNull(CssPayment::getWriteoffComplete)));
//            } else {
//                wrapper.in(CssPayment::getWriteoffComplete, cssPayment.getWriteoffCompletes().split(","));
//            }
//        }
        IPage<CssPayment> iPage = baseMapper.selectPage(page, wrapper);
        iPage.getRecords().stream().forEach(item -> {
            //拼接发票号
            LambdaQueryWrapper<CssCostInvoiceDetail> cssCostInvoiceDetailLambdaQueryWrapper = Wrappers.<CssCostInvoiceDetail>lambdaQuery();
            cssCostInvoiceDetailLambdaQueryWrapper.eq(CssCostInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoiceDetail::getPaymentId, item.getPaymentId());
            List<CssCostInvoiceDetail> cssCostInvoiceDetails = cssCostInvoiceDetailService.list(cssCostInvoiceDetailLambdaQueryWrapper);
            StringBuffer invoiceNumBuffer = new StringBuffer();
            cssCostInvoiceDetails.stream().forEach(cssCostInvoiceDetail -> {
                invoiceNumBuffer.append(cssCostInvoiceDetail.getInvoiceDetailId());
                invoiceNumBuffer.append(" ");
                invoiceNumBuffer.append(cssCostInvoiceDetail.getInvoiceNum()).append("（").append(cssCostInvoiceDetail.getInvoiceDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))).append("）");
                invoiceNumBuffer.append("  ");
            });
            item.setInvoiceNum(invoiceNumBuffer.toString());
            //取核销单号
            LambdaQueryWrapper<CssCostInvoiceDetailWriteoff> cssCostInvoiceDetailWriteoffLambdaQueryWrapper = Wrappers.<CssCostInvoiceDetailWriteoff>lambdaQuery();
            cssCostInvoiceDetailWriteoffLambdaQueryWrapper.eq(CssCostInvoiceDetailWriteoff::getPaymentId, item.getPaymentId()).eq(CssCostInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId());
            List<CssCostInvoiceDetailWriteoff> cssCostInvoiceDetailWriteoffs = cssCostInvoiceDetailWriteoffService.list(cssCostInvoiceDetailWriteoffLambdaQueryWrapper);
            StringBuffer writeoffNumBuffer = new StringBuffer();
            cssCostInvoiceDetailWriteoffs.stream().forEach(cssCostInvoiceDetailWriteoff -> {
                writeoffNumBuffer.append(cssCostInvoiceDetailWriteoff.getInvoiceDetailWriteoffId());
                writeoffNumBuffer.append(" ");
                writeoffNumBuffer.append(cssCostInvoiceDetailWriteoff.getWriteoffNum());
                writeoffNumBuffer.append("  ");
            });
            item.setWriteoffNum(writeoffNumBuffer.toString());

            if (item.getAmountPayment() != null) {
                item.setAmountPaymentStr(formatWith2AndQFW(item.getAmountPayment()) + " (" + item.getCurrency() + ")");
                if (item.getAmountPaymentWriteoff() == null) {
                    item.setAmountPaymentWriteoffStr("0.00 (" + item.getCurrency() + ")");
                    item.setAmountPaymentNoWriteoff(item.getAmountPayment());
                } else {
                    item.setAmountPaymentWriteoffStr(formatWith2AndQFW(item.getAmountPaymentWriteoff()) + " (" + item.getCurrency() + ")");
                    item.setAmountPaymentNoWriteoff(item.getAmountPayment().subtract(item.getAmountPaymentWriteoff()));
                }
                item.setAmountPaymentNoWriteoffStr(formatWith2AndQFW(item.getAmountPaymentNoWriteoff()) + " (" + item.getCurrency() + ")");
            }

            LambdaQueryWrapper<CssCostInvoice> cssCostInvoiceWrapper = Wrappers.<CssCostInvoice>lambdaQuery();
            cssCostInvoiceWrapper.eq(CssCostInvoice::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoice::getPaymentId, item.getPaymentId());
            CssCostInvoice cssCostInvoice = cssCostInvoiceMapper.selectOne(cssCostInvoiceWrapper);
            if (cssCostInvoice != null) {
                item.setInvoiceTime(cssCostInvoice.getCreateTime());
                item.setInvoiceCreatorName(cssCostInvoice.getCreatorName());
                item.setInvoiceInqurityRemark(cssCostInvoice.getApplyRemark());
            }
            if (StrUtil.isBlank(item.getInvoiceInqurityRemark())) {
                item.setInvoiceInqurityRemark(item.getInvoiceRemark());
            }
            if (item.getWriteoffComplete() == null) {
                if (cssCostInvoice == null) {
                    item.setPaymentStatus("已对账");
                } else if (cssCostInvoice.getInvoiceStatus() == -1) {
                    item.setPaymentStatus("待收票");
                } else if (cssCostInvoice.getInvoiceStatus() == 1) {
                    item.setPaymentStatus("收票完毕");
                } else if (cssCostInvoice.getInvoiceStatus() == 0) {
                    item.setPaymentStatus("部分收票");
                }
            } else if (item.getWriteoffComplete() == 1) {
                item.setPaymentStatus("核销完毕");
            } else if (item.getWriteoffComplete() == 0) {
                item.setPaymentStatus("部分核销");
            }


        });
        //拼接合计
        if (iPage.getRecords().size() != 0) {
            HashMap<String, BigDecimal> amountWriteoffMap = new HashMap<>();//未核销金额汇总
            HashMap<String, BigDecimal> amountPaymentWriteoffMap = new HashMap<>();//已核销金额汇总
            HashMap<String, BigDecimal> amountPaymentMap = new HashMap<>();//对账金额汇总
            list(wrapper).stream().forEach(writeoff -> {
                BigDecimal writOff = BigDecimal.ZERO;
                if (writeoff.getAmountPaymentWriteoff() != null) {
                    writOff = writeoff.getAmountPaymentWriteoff();
                }
                //对账
                if (amountPaymentMap.containsKey(writeoff.getCurrency())) {
                    amountPaymentMap.put(writeoff.getCurrency(), writeoff.getAmountPayment().add(amountPaymentMap.get(writeoff.getCurrency())));
                } else {
                    amountPaymentMap.put(writeoff.getCurrency(), writeoff.getAmountPayment());
                }
                //核销
                if (amountPaymentWriteoffMap.containsKey(writeoff.getCurrency())) {
                    amountPaymentWriteoffMap.put(writeoff.getCurrency(), amountPaymentWriteoffMap.get(writeoff.getCurrency()).add(writOff));
                } else {
                    amountPaymentWriteoffMap.put(writeoff.getCurrency(), writOff);
                }
                //未核销
                if (amountWriteoffMap.get(writeoff.getCurrency()) == null) {
                    amountWriteoffMap.put(writeoff.getCurrency(), writeoff.getAmountPayment().subtract(writOff));
                } else {
                    amountWriteoffMap.put(writeoff.getCurrency(), amountWriteoffMap.get(writeoff.getCurrency()).add(writeoff.getAmountPayment().subtract(writOff)));
                }
            });
            StringBuffer amountWriteoffBuffer = new StringBuffer();
            StringBuffer amountPaymentWriteoffBuffer = new StringBuffer();
            StringBuffer amountPaymentBuffer = new StringBuffer();
            for (Map.Entry<String, BigDecimal> entry : amountWriteoffMap.entrySet()) {
                if (amountWriteoffBuffer.length() == 0) {
                    amountWriteoffBuffer.append(formatWith2AndQFW(entry.getValue()) + " (" + entry.getKey() + ")");
                } else {
                    amountWriteoffBuffer.append("|").append(formatWith2AndQFW(entry.getValue()) + " (" + entry.getKey() + ")");
                }
            }
            for (Map.Entry<String, BigDecimal> entry : amountPaymentWriteoffMap.entrySet()) {
                if (amountPaymentWriteoffBuffer.length() == 0) {
                    amountPaymentWriteoffBuffer.append(formatWith2AndQFW(entry.getValue()) + " (" + entry.getKey() + ")");
                } else {
                    amountPaymentWriteoffBuffer.append("|").append(formatWith2AndQFW(entry.getValue()) + " (" + entry.getKey() + ")");
                }
            }
            for (Map.Entry<String, BigDecimal> entry : amountPaymentMap.entrySet()) {
                if (amountPaymentBuffer.length() == 0) {
                    amountPaymentBuffer.append(formatWith2AndQFW(entry.getValue()) + " (" + entry.getKey() + ")");
                } else {
                    amountPaymentBuffer.append("|").append(formatWith2AndQFW(entry.getValue()) + " (" + entry.getKey() + ")");
                }
            }
            CssPayment writeoff = new CssPayment();
            writeoff.setAmountPaymentNoWriteoffStr(amountWriteoffBuffer.toString());
            writeoff.setAmountPaymentStr(amountPaymentBuffer.toString());
            writeoff.setAmountPaymentWriteoffStr(amountPaymentWriteoffBuffer.toString());
            iPage.getRecords().add(writeoff);
        }
        return iPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void insert(CssPayment cssPayment) {
        //保存条件-本次对账金额不能大于未对账金额
        cssPayment.getDetails().stream().forEach(cssPaymentDetail -> {
            AfCost cost = afCostService.getById(cssPaymentDetail.getCostId());
            if (cost == null) {
                throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的成本不存在,保存失败");
            }
            if (StrUtil.isNotBlank(cost.getRowUuid()) && !cost.getRowUuid().equals(cssPaymentDetail.getRowUuid())) {
                throw new RuntimeException("对账单下成本不是最新数据，请刷新页面重新操作");
            }
            BigDecimal costAmountNoPayment = null;
            if (cost.getCostAmountPayment() == null) {
                costAmountNoPayment = cost.getCostAmount();
            } else {
                costAmountNoPayment = cost.getCostAmount().subtract(cost.getCostAmountPayment());
            }

            if (costAmountNoPayment.signum() == -1) {
                if (cssPaymentDetail.getAmountPayment().signum() == 1) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能>0,保存失败");
                } else {
                    if (costAmountNoPayment.abs().compareTo(cssPaymentDetail.getAmountPayment().abs()) == -1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                    }
                }
            } else if (costAmountNoPayment.signum() == 0) {
                if (cssPaymentDetail.getAmountPayment().signum() != 0) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细已经完全对账,不能再对账,保存失败");
                }
            } else {
                if (cssPaymentDetail.getAmountPayment().signum() == -1) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能<0,保存失败");
                } else {
                    if (costAmountNoPayment.compareTo(cssPaymentDetail.getAmountPayment()) == -1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                    }
                }
            }

        });
        //保存对账单

        //生成对账编号
        String paymentNum = getPaymentNum(cssPayment.getBusinessScope());
        cssPayment.setPaymentNum(paymentNum);
        cssPayment.setRowUuid(UUID.randomUUID().toString());
        LocalDateTime now = LocalDateTime.now();
        cssPayment.setCreateTime(now);
        cssPayment.setEditTime(now);
        cssPayment.setCreatorId(SecurityUtils.getUser().getId());
        cssPayment.setEditorId(SecurityUtils.getUser().getId());
        cssPayment.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        cssPayment.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        cssPayment.setOrgId(SecurityUtils.getUser().getOrgId());
        save(cssPayment);
        //保存对账明细
        ArrayList<Integer> costIds = new ArrayList<>();
        ArrayList<Integer> orderIds = new ArrayList<>();
        if (cssPayment.getDetails() != null && cssPayment.getDetails().size() > 0) {
            cssPayment.getDetails().stream().forEach(cssPaymentDetail -> {
                cssPaymentDetail.setOrgId(SecurityUtils.getUser().getOrgId());
                cssPaymentDetail.setPaymentId(cssPayment.getPaymentId());
                costIds.add(cssPaymentDetail.getCostId());
                orderIds.add(cssPaymentDetail.getOrderId());
            });
            cssPaymentDetailService.saveBatch(cssPayment.getDetails());
        }
        //更新cost对账金额
        if (costIds.size() != 0) {
            LambdaQueryWrapper<CssPayment> wrapperCssPayment = Wrappers.<CssPayment>lambdaQuery();
            wrapperCssPayment.eq(CssPayment::getBusinessScope, cssPayment.getBusinessScope()).eq(CssPayment::getOrgId, SecurityUtils.getUser().getOrgId());
            List<Integer> listPayment = list(wrapperCssPayment).stream().map(c -> c.getPaymentId()).collect(Collectors.toList());
            costIds.stream().forEach(costId -> {
                LambdaQueryWrapper<CssPaymentDetail> detailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                detailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, costId).in(CssPaymentDetail::getPaymentId, listPayment);
                HashMap<String, BigDecimal> sumMap = new HashMap<>();
                cssPaymentDetailService.list(detailWrapper).stream().forEach(cssPaymentDetail -> {
                    if (cssPaymentDetail.getAmountPayment() != null) {
                        if (sumMap.get("sum") == null) {
                            sumMap.put("sum", cssPaymentDetail.getAmountPayment());
                        } else {
                            sumMap.put("sum", sumMap.get("sum").add(cssPaymentDetail.getAmountPayment()));
                        }
                    }
                });
                AfCost cost = afCostService.getById(costId);
                cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                //修复汇率，本币金额，对应费用类型费用，条件：币种!=CNY
                if (!"CNY".equals(cost.getCostCurrency())) {
                    cost.setCostExchangeRate(cssPayment.getExchangeRate());
                    if (cost.getCostAmount() != null && cssPayment.getExchangeRate() != null) {
                        BigDecimal decimal = cssPayment.getExchangeRate().multiply(cost.getCostAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
                        cost.setCostFunctionalAmount(decimal);
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("干线")) {
                            cost.setMainRouting(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("支线")) {
                            cost.setFeeder(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("操作")) {
                            cost.setOperation(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("包装")) {
                            cost.setPackaging(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("仓储")) {
                            cost.setStorage(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("快递")) {
                            cost.setPostage(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("关检")) {
                            cost.setClearance(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("数据")) {
                            cost.setExchange(decimal);
                        }
                    }
                }
                cost.setRowUuid(UUID.randomUUID().toString());
                afCostService.updateById(cost);
            });
        }

        //更新订单状态
        if (orderIds.size() != 0) {
            orderIds.stream().distinct().forEach(orderId -> {
                AfOrder order = afOrderService.getById(orderId);
                order.setCostStatus(afOrderService.getOrderCostStatusForAF(orderId));
                order.setRowUuid(UUID.randomUUID().toString());
                afOrderService.updateById(order);
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void insertTc(CssPayment cssPayment) {
        //保存条件-本次对账金额不能大于未对账金额
        cssPayment.getDetails().stream().forEach(cssPaymentDetail -> {
            TcCost cost = tcCostService.getById(cssPaymentDetail.getCostId());
            if (cost == null) {
                throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的成本不存在,保存失败");
            }
            if (StrUtil.isNotBlank(cost.getRowUuid()) && !cost.getRowUuid().equals(cssPaymentDetail.getRowUuid())) {
                throw new RuntimeException("对账单下成本不是最新数据，请刷新页面重新操作");
            }
            BigDecimal costAmountNoPayment = null;
            if (cost.getCostAmountPayment() == null) {
                costAmountNoPayment = cost.getCostAmount();
            } else {
                costAmountNoPayment = cost.getCostAmount().subtract(cost.getCostAmountPayment());
            }

            if (costAmountNoPayment.signum() == -1) {
                if (cssPaymentDetail.getAmountPayment().signum() == 1) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能>0,保存失败");
                } else {
                    if (costAmountNoPayment.abs().compareTo(cssPaymentDetail.getAmountPayment().abs()) == -1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                    }
                }
            } else if (costAmountNoPayment.signum() == 0) {
                if (cssPaymentDetail.getAmountPayment().signum() != 0) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细已经完全对账,不能再对账,保存失败");
                }
            } else {
                if (cssPaymentDetail.getAmountPayment().signum() == -1) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能<0,保存失败");
                } else {
                    if (costAmountNoPayment.compareTo(cssPaymentDetail.getAmountPayment()) == -1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                    }
                }
            }

        });
        //保存对账单

        //生成对账编号
        String paymentNum = getPaymentNum(cssPayment.getBusinessScope());
        cssPayment.setPaymentNum(paymentNum);
        cssPayment.setRowUuid(UUID.randomUUID().toString());
        LocalDateTime now = LocalDateTime.now();
        cssPayment.setCreateTime(now);
        cssPayment.setEditTime(now);
        cssPayment.setCreatorId(SecurityUtils.getUser().getId());
        cssPayment.setEditorId(SecurityUtils.getUser().getId());
        cssPayment.setCreatorName(SecurityUtils.getUser().buildOptName());
        cssPayment.setEditorName(SecurityUtils.getUser().buildOptName());
        cssPayment.setOrgId(SecurityUtils.getUser().getOrgId());
        save(cssPayment);
        //保存对账明细
        ArrayList<Integer> costIds = new ArrayList<>();
        ArrayList<Integer> orderIds = new ArrayList<>();
        if (cssPayment.getDetails() != null && cssPayment.getDetails().size() > 0) {
            cssPayment.getDetails().stream().forEach(cssPaymentDetail -> {
                cssPaymentDetail.setOrgId(SecurityUtils.getUser().getOrgId());
                cssPaymentDetail.setPaymentId(cssPayment.getPaymentId());
                costIds.add(cssPaymentDetail.getCostId());
                orderIds.add(cssPaymentDetail.getOrderId());
            });
            cssPaymentDetailService.saveBatch(cssPayment.getDetails());
        }
        //更新cost对账金额
        if (costIds.size() != 0) {
            LambdaQueryWrapper<CssPayment> wrapperCssPayment = Wrappers.<CssPayment>lambdaQuery();
            wrapperCssPayment.eq(CssPayment::getBusinessScope, cssPayment.getBusinessScope()).eq(CssPayment::getOrgId, SecurityUtils.getUser().getOrgId());
            List<Integer> listPayment = list(wrapperCssPayment).stream().map(c -> c.getPaymentId()).collect(Collectors.toList());
            costIds.stream().forEach(costId -> {
                LambdaQueryWrapper<CssPaymentDetail> detailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                detailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, costId).in(CssPaymentDetail::getPaymentId, listPayment);
                HashMap<String, BigDecimal> sumMap = new HashMap<>();
                cssPaymentDetailService.list(detailWrapper).stream().forEach(cssPaymentDetail -> {
                    if (cssPaymentDetail.getAmountPayment() != null) {
                        if (sumMap.get("sum") == null) {
                            sumMap.put("sum", cssPaymentDetail.getAmountPayment());
                        } else {
                            sumMap.put("sum", sumMap.get("sum").add(cssPaymentDetail.getAmountPayment()));
                        }
                    }
                });
                TcCost cost = tcCostService.getById(costId);
                cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                //修复汇率，本币金额，条件：币种!=CNY
                if (!"CNY".equals(cost.getCostCurrency())) {
                    cost.setCostExchangeRate(cssPayment.getExchangeRate());
                    if (cost.getCostAmount() != null && cssPayment.getExchangeRate() != null) {
                        BigDecimal decimal = cssPayment.getExchangeRate().multiply(cost.getCostAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
                        cost.setCostFunctionalAmount(decimal);

                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("干线")) {
                            cost.setMainRouting(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("支线")) {
                            cost.setFeeder(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("操作")) {
                            cost.setOperation(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("包装")) {
                            cost.setPackaging(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("仓储")) {
                            cost.setStorage(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("快递")) {
                            cost.setPostage(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("关检")) {
                            cost.setClearance(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("数据")) {
                            cost.setExchange(decimal);
                        }
                    }
                }
                cost.setRowUuid(UUID.randomUUID().toString());
                tcCostService.updateById(cost);
            });
        }

        //更新订单状态
        if (orderIds.size() != 0) {
            orderIds.stream().distinct().forEach(orderId -> {
                TcOrder order = tcOrderService.getById(orderId);
                order.setCostStatus(afOrderService.getOrderCostStatusForTC(orderId));
                order.setRowUuid(UUID.randomUUID().toString());
                tcOrderService.updateById(order);
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void insertLc(CssPayment cssPayment) {
        //保存条件-本次对账金额不能大于未对账金额
        cssPayment.getDetails().stream().forEach(cssPaymentDetail -> {
            LcCost cost = lcCostService.getById(cssPaymentDetail.getCostId());
            if (cost == null) {
                throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的成本不存在,保存失败");
            }
            if (StrUtil.isNotBlank(cost.getRowUuid()) && !cost.getRowUuid().equals(cssPaymentDetail.getRowUuid())) {
                throw new RuntimeException("对账单下成本不是最新数据，请刷新页面重新操作");
            }
            BigDecimal costAmountNoPayment = null;
            if (cost.getCostAmountPayment() == null) {
                costAmountNoPayment = cost.getCostAmount();
            } else {
                costAmountNoPayment = cost.getCostAmount().subtract(cost.getCostAmountPayment());
            }

            if (costAmountNoPayment.signum() == -1) {
                if (cssPaymentDetail.getAmountPayment().signum() == 1) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能>0,保存失败");
                } else {
                    if (costAmountNoPayment.abs().compareTo(cssPaymentDetail.getAmountPayment().abs()) == -1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                    }
                }
            } else if (costAmountNoPayment.signum() == 0) {
                if (cssPaymentDetail.getAmountPayment().signum() != 0) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细已经完全对账,不能再对账,保存失败");
                }
            } else {
                if (cssPaymentDetail.getAmountPayment().signum() == -1) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能<0,保存失败");
                } else {
                    if (costAmountNoPayment.compareTo(cssPaymentDetail.getAmountPayment()) == -1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                    }
                }
            }

        });
        //保存对账单

        //生成对账编号
        String paymentNum = getPaymentNum(cssPayment.getBusinessScope());
        cssPayment.setPaymentNum(paymentNum);
        cssPayment.setRowUuid(UUID.randomUUID().toString());
        LocalDateTime now = LocalDateTime.now();
        cssPayment.setCreateTime(now);
        cssPayment.setEditTime(now);
        cssPayment.setCreatorId(SecurityUtils.getUser().getId());
        cssPayment.setEditorId(SecurityUtils.getUser().getId());
        cssPayment.setCreatorName(SecurityUtils.getUser().buildOptName());
        cssPayment.setEditorName(SecurityUtils.getUser().buildOptName());
        cssPayment.setOrgId(SecurityUtils.getUser().getOrgId());
        save(cssPayment);
        //保存对账明细
        ArrayList<Integer> costIds = new ArrayList<>();
        ArrayList<Integer> orderIds = new ArrayList<>();
        if (cssPayment.getDetails() != null && cssPayment.getDetails().size() > 0) {
            cssPayment.getDetails().stream().forEach(cssPaymentDetail -> {
                cssPaymentDetail.setOrgId(SecurityUtils.getUser().getOrgId());
                cssPaymentDetail.setPaymentId(cssPayment.getPaymentId());
                costIds.add(cssPaymentDetail.getCostId());
                orderIds.add(cssPaymentDetail.getOrderId());
            });
            cssPaymentDetailService.saveBatch(cssPayment.getDetails());
        }
        //更新cost对账金额
        if (costIds.size() != 0) {
            LambdaQueryWrapper<CssPayment> wrapperCssPayment = Wrappers.<CssPayment>lambdaQuery();
            wrapperCssPayment.eq(CssPayment::getBusinessScope, cssPayment.getBusinessScope()).eq(CssPayment::getOrgId, SecurityUtils.getUser().getOrgId());
            List<Integer> listPayment = list(wrapperCssPayment).stream().map(c -> c.getPaymentId()).collect(Collectors.toList());
            costIds.stream().forEach(costId -> {
                LambdaQueryWrapper<CssPaymentDetail> detailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                detailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, costId).in(CssPaymentDetail::getPaymentId, listPayment);
                HashMap<String, BigDecimal> sumMap = new HashMap<>();
                cssPaymentDetailService.list(detailWrapper).stream().forEach(cssPaymentDetail -> {
                    if (cssPaymentDetail.getAmountPayment() != null) {
                        if (sumMap.get("sum") == null) {
                            sumMap.put("sum", cssPaymentDetail.getAmountPayment());
                        } else {
                            sumMap.put("sum", sumMap.get("sum").add(cssPaymentDetail.getAmountPayment()));
                        }
                    }
                });
                LcCost cost = lcCostService.getById(costId);
                cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                //修复汇率，本币金额，条件：币种!=CNY
                if (!"CNY".equals(cost.getCostCurrency())) {
                    cost.setCostExchangeRate(cssPayment.getExchangeRate());
                    if (cost.getCostAmount() != null && cssPayment.getExchangeRate() != null) {
                        BigDecimal decimal = cssPayment.getExchangeRate().multiply(cost.getCostAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
                        cost.setCostFunctionalAmount(decimal);

                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("干线")) {
                            cost.setMainRouting(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("支线")) {
                            cost.setFeeder(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("操作")) {
                            cost.setOperation(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("包装")) {
                            cost.setPackaging(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("仓储")) {
                            cost.setStorage(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("快递")) {
                            cost.setPostage(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("关检")) {
                            cost.setClearance(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("数据")) {
                            cost.setExchange(decimal);
                        }
                    }
                }
                cost.setRowUuid(UUID.randomUUID().toString());
                lcCostService.updateById(cost);
            });
        }

        //更新订单状态
        if (orderIds.size() != 0) {
            orderIds.stream().distinct().forEach(orderId -> {
                LcOrder order = lcOrderService.getById(orderId);
                order.setCostStatus(afOrderService.getOrderCostStatusForLC(orderId));
                order.setRowUuid(UUID.randomUUID().toString());
                lcOrderService.updateById(order);
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void insertIO(CssPayment cssPayment) {
        //保存条件-本次对账金额不能大于未对账金额
        cssPayment.getDetails().stream().forEach(cssPaymentDetail -> {
            IoCost cost = ioCostService.getById(cssPaymentDetail.getCostId());
            if (cost == null) {
                throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的成本不存在,保存失败");
            }
            if (StrUtil.isNotBlank(cost.getRowUuid()) && !cost.getRowUuid().equals(cssPaymentDetail.getRowUuid())) {
                throw new RuntimeException("对账单下成本不是最新数据，请刷新页面重新操作");
            }
            BigDecimal costAmountNoPayment = null;
            if (cost.getCostAmountPayment() == null) {
                costAmountNoPayment = cost.getCostAmount();
            } else {
                costAmountNoPayment = cost.getCostAmount().subtract(cost.getCostAmountPayment());
            }

            if (costAmountNoPayment.signum() == -1) {
                if (cssPaymentDetail.getAmountPayment().signum() == 1) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能>0,保存失败");
                } else {
                    if (costAmountNoPayment.abs().compareTo(cssPaymentDetail.getAmountPayment().abs()) == -1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                    }
                }
            } else if (costAmountNoPayment.signum() == 0) {
                if (cssPaymentDetail.getAmountPayment().signum() != 0) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细已经完全对账,不能再对账,保存失败");
                }
            } else {
                if (cssPaymentDetail.getAmountPayment().signum() == -1) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能<0,保存失败");
                } else {
                    if (costAmountNoPayment.compareTo(cssPaymentDetail.getAmountPayment()) == -1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                    }
                }
            }

        });
        //保存对账单

        //生成对账编号
        String paymentNum = getPaymentNum(cssPayment.getBusinessScope());
        cssPayment.setPaymentNum(paymentNum);
        cssPayment.setRowUuid(UUID.randomUUID().toString());
        LocalDateTime now = LocalDateTime.now();
        cssPayment.setCreateTime(now);
        cssPayment.setEditTime(now);
        cssPayment.setCreatorId(SecurityUtils.getUser().getId());
        cssPayment.setEditorId(SecurityUtils.getUser().getId());
        cssPayment.setCreatorName(SecurityUtils.getUser().buildOptName());
        cssPayment.setEditorName(SecurityUtils.getUser().buildOptName());
        cssPayment.setOrgId(SecurityUtils.getUser().getOrgId());
        save(cssPayment);
        //保存对账明细
        ArrayList<Integer> costIds = new ArrayList<>();
        ArrayList<Integer> orderIds = new ArrayList<>();
        if (cssPayment.getDetails() != null && cssPayment.getDetails().size() > 0) {
            cssPayment.getDetails().stream().forEach(cssPaymentDetail -> {
                cssPaymentDetail.setOrgId(SecurityUtils.getUser().getOrgId());
                cssPaymentDetail.setPaymentId(cssPayment.getPaymentId());
                costIds.add(cssPaymentDetail.getCostId());
                orderIds.add(cssPaymentDetail.getOrderId());
            });
            cssPaymentDetailService.saveBatch(cssPayment.getDetails());
        }
        //更新cost对账金额
        if (costIds.size() != 0) {
            LambdaQueryWrapper<CssPayment> wrapperCssPayment = Wrappers.<CssPayment>lambdaQuery();
            wrapperCssPayment.eq(CssPayment::getBusinessScope, cssPayment.getBusinessScope()).eq(CssPayment::getOrgId, SecurityUtils.getUser().getOrgId());
            List<Integer> listPayment = list(wrapperCssPayment).stream().map(c -> c.getPaymentId()).collect(Collectors.toList());
            costIds.stream().forEach(costId -> {
                LambdaQueryWrapper<CssPaymentDetail> detailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                detailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, costId).in(CssPaymentDetail::getPaymentId, listPayment);
                HashMap<String, BigDecimal> sumMap = new HashMap<>();
                cssPaymentDetailService.list(detailWrapper).stream().forEach(cssPaymentDetail -> {
                    if (cssPaymentDetail.getAmountPayment() != null) {
                        if (sumMap.get("sum") == null) {
                            sumMap.put("sum", cssPaymentDetail.getAmountPayment());
                        } else {
                            sumMap.put("sum", sumMap.get("sum").add(cssPaymentDetail.getAmountPayment()));
                        }
                    }
                });
                IoCost cost = ioCostService.getById(costId);
                cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                //修复汇率，本币金额，条件：币种!=CNY
                if (!"CNY".equals(cost.getCostCurrency())) {
                    cost.setCostExchangeRate(cssPayment.getExchangeRate());
                    if (cost.getCostAmount() != null && cssPayment.getExchangeRate() != null) {
                        BigDecimal decimal = cssPayment.getExchangeRate().multiply(cost.getCostAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
                        cost.setCostFunctionalAmount(decimal);

                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("干线")) {
                            cost.setMainRouting(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("支线")) {
                            cost.setFeeder(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("操作")) {
                            cost.setOperation(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("包装")) {
                            cost.setPackaging(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("仓储")) {
                            cost.setStorage(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("快递")) {
                            cost.setPostage(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("关检")) {
                            cost.setClearance(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("数据")) {
                            cost.setExchange(decimal);
                        }
                    }
                }
                cost.setRowUuid(UUID.randomUUID().toString());
                ioCostService.updateById(cost);
            });
        }

        //更新订单状态
        if (orderIds.size() != 0) {
            orderIds.stream().distinct().forEach(orderId -> {
                IoOrder order = ioOrderService.getById(orderId);
                order.setCostStatus(afOrderService.getOrderCostStatusForIO(orderId));
                order.setRowUuid(UUID.randomUUID().toString());
                ioOrderService.updateById(order);
            });
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void insertSc(CssPayment cssPayment) {
        //保存条件-本次对账金额不能大于未对账金额
        cssPayment.getDetails().stream().forEach(cssPaymentDetail -> {
            ScCost cost = scCostService.getById(cssPaymentDetail.getCostId());
            if (cost == null) {
                throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的成本不存在,保存失败");
            }
            if (StrUtil.isNotBlank(cost.getRowUuid()) && !cost.getRowUuid().equals(cssPaymentDetail.getRowUuid())) {
                throw new RuntimeException("对账单下成本不是最新数据，请刷新页面重新操作");
            }
            BigDecimal costAmountNoPayment = null;
            if (cost.getCostAmountPayment() == null) {
                costAmountNoPayment = cost.getCostAmount();
            } else {
                costAmountNoPayment = cost.getCostAmount().subtract(cost.getCostAmountPayment());
            }

            if (costAmountNoPayment.signum() == -1) {
                if (cssPaymentDetail.getAmountPayment().signum() == 1) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能>0,保存失败");
                } else {
                    if (costAmountNoPayment.abs().compareTo(cssPaymentDetail.getAmountPayment().abs()) == -1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                    }
                }
            } else if (costAmountNoPayment.signum() == 0) {
                if (cssPaymentDetail.getAmountPayment().signum() != 0) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细已经完全对账,不能再对账,保存失败");
                }
            } else {
                if (cssPaymentDetail.getAmountPayment().signum() == -1) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能<0,保存失败");
                } else {
                    if (costAmountNoPayment.compareTo(cssPaymentDetail.getAmountPayment()) == -1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                    }
                }
            }

        });
        //保存对账单

        //生成对账编号
        String paymentNum = getPaymentNum(cssPayment.getBusinessScope());
        cssPayment.setPaymentNum(paymentNum);
        cssPayment.setRowUuid(UUID.randomUUID().toString());
        LocalDateTime now = LocalDateTime.now();
        cssPayment.setCreateTime(now);
        cssPayment.setEditTime(now);
        cssPayment.setCreatorId(SecurityUtils.getUser().getId());
        cssPayment.setEditorId(SecurityUtils.getUser().getId());
        cssPayment.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        cssPayment.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        cssPayment.setOrgId(SecurityUtils.getUser().getOrgId());
        save(cssPayment);
        //保存对账明细
        ArrayList<Integer> costIds = new ArrayList<>();
        ArrayList<Integer> orderIds = new ArrayList<>();
        if (cssPayment.getDetails() != null && cssPayment.getDetails().size() > 0) {
            cssPayment.getDetails().stream().forEach(cssPaymentDetail -> {
                cssPaymentDetail.setOrgId(SecurityUtils.getUser().getOrgId());
                cssPaymentDetail.setPaymentId(cssPayment.getPaymentId());
                costIds.add(cssPaymentDetail.getCostId());
                orderIds.add(cssPaymentDetail.getOrderId());
            });
            cssPaymentDetailService.saveBatch(cssPayment.getDetails());
        }
        //更新cost对账金额
        if (costIds.size() != 0) {
            LambdaQueryWrapper<CssPayment> wrapperCssPayment = Wrappers.<CssPayment>lambdaQuery();
            wrapperCssPayment.eq(CssPayment::getBusinessScope, cssPayment.getBusinessScope()).eq(CssPayment::getOrgId, SecurityUtils.getUser().getOrgId());
            List<Integer> listPayment = list(wrapperCssPayment).stream().map(c -> c.getPaymentId()).collect(Collectors.toList());
            costIds.stream().forEach(costId -> {
                LambdaQueryWrapper<CssPaymentDetail> detailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                detailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, costId).in(CssPaymentDetail::getPaymentId, listPayment);
                HashMap<String, BigDecimal> sumMap = new HashMap<>();
                cssPaymentDetailService.list(detailWrapper).stream().forEach(cssPaymentDetail -> {
                    if (cssPaymentDetail.getAmountPayment() != null) {
                        if (sumMap.get("sum") == null) {
                            sumMap.put("sum", cssPaymentDetail.getAmountPayment());
                        } else {
                            sumMap.put("sum", sumMap.get("sum").add(cssPaymentDetail.getAmountPayment()));
                        }
                    }
                });
                ScCost cost = scCostService.getById(costId);
                cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                //修复汇率，本币金额，条件：币种!=CNY
                if (!"CNY".equals(cost.getCostCurrency())) {
                    cost.setCostExchangeRate(cssPayment.getExchangeRate());
                    if (cost.getCostAmount() != null && cssPayment.getExchangeRate() != null) {
                        BigDecimal decimal = cssPayment.getExchangeRate().multiply(cost.getCostAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
                        cost.setCostFunctionalAmount(decimal);

                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("干线")) {
                            cost.setMainRouting(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("支线")) {
                            cost.setFeeder(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("操作")) {
                            cost.setOperation(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("包装")) {
                            cost.setPackaging(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("仓储")) {
                            cost.setStorage(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("快递")) {
                            cost.setPostage(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("关检")) {
                            cost.setClearance(decimal);
                        }
                        if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("数据")) {
                            cost.setExchange(decimal);
                        }
                    }
                }
                cost.setRowUuid(UUID.randomUUID().toString());
                scCostService.updateById(cost);
            });
        }

        //更新订单状态
        if (orderIds.size() != 0) {
            orderIds.stream().distinct().forEach(orderId -> {
                afOrderService.updateOrderCostStatusForSC(orderId);
            });
        }
    }


    private String getPaymentNum(String businessScope) {
        String numberPrefix = "-CP-" + DateTimeFormatter.ofPattern("yyMMdd").format(LocalDate.now());
        LambdaQueryWrapper<CssPayment> wrapper = Wrappers.<CssPayment>lambdaQuery();
        wrapper.eq(CssPayment::getBusinessScope, businessScope).eq(CssPayment::getOrgId, SecurityUtils.getUser().getOrgId()).like(CssPayment::getPaymentNum, "%" + numberPrefix + "%").orderByDesc(CssPayment::getPaymentNum).last(" limit 1");

        CssPayment cssPayment = getOne(wrapper);

        String numberSuffix = "";
        if (cssPayment == null) {
            numberSuffix = "0001";
        } else if (cssPayment.getPaymentNum().substring(cssPayment.getPaymentNum().length() - 4).equals("9999")) {
            throw new RuntimeException("当天对账单已满无法创建");
        } else {
            String n = Integer.valueOf(cssPayment.getPaymentNum().substring(cssPayment.getPaymentNum().length() - 4)) + 1 + "";
            numberSuffix = "0000".substring(0, 4 - n.length()) + n;
        }
        return businessScope + numberPrefix + numberSuffix;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modify(CssPayment cssPayment) {
        //校验对账单是否被修改过
        CssPayment payment = getById(cssPayment.getPaymentId());
        if (payment == null) {
            throw new RuntimeException("对账单不存在，无法修改");
        }
        if (!cssPayment.getRowUuid().equals(payment.getRowUuid())) {
            throw new RuntimeException("对账单不是最新数据，请刷新页面再操作");
        }

        //0.校验对账单状态可否修改
        if (cssPayment.getWriteoffComplete() != null) {
            throw new RuntimeException("对账单已核销，无法修改");
        }

        //校验如何已经发票申请不可修改
        LambdaQueryWrapper<CssCostInvoice> cssCostInvoiceWrapper = Wrappers.<CssCostInvoice>lambdaQuery();
        cssCostInvoiceWrapper.eq(CssCostInvoice::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoice::getPaymentId, payment.getPaymentId());
        CssCostInvoice cssCostInvoice = cssCostInvoiceMapper.selectOne(cssCostInvoiceWrapper);
        if (cssCostInvoice != null) {
            throw new RuntimeException(payment.getPaymentNum() + "对账单 已申请付款，不能修改。");
        }
        //1.删除对账明细
        LambdaQueryWrapper<CssPaymentDetail> detailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
        detailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getPaymentId, cssPayment.getPaymentId());
        List<CssPaymentDetail> list = cssPaymentDetailService.list(detailWrapper);
        List<Integer> costIdsOperationBefore = list.stream().map(CssPaymentDetail::getCostId).collect(Collectors.toList());
        List<Integer> orderIdsOperationBefore = list.stream().map(CssPaymentDetail::getOrderId).collect(Collectors.toList());
        cssPaymentDetailService.remove(detailWrapper);

        //2.去除该对账单对成本cost的影响
        List<Integer> costIds = new ArrayList<>();
        List<Integer> orderIds = new ArrayList<>();
        if (cssPayment.getDetails() != null && cssPayment.getDetails().size() > 0) {
            cssPayment.getDetails().stream().forEach(cssPaymentDetail -> {
                costIds.add(cssPaymentDetail.getCostId());
                orderIds.add(cssPaymentDetail.getOrderId());
            });
        }
        //更新cost对账金额为了校验条件
        if (costIds.size() != 0) {
            LambdaQueryWrapper<CssPayment> wrapperCssPayment = Wrappers.<CssPayment>lambdaQuery();
            wrapperCssPayment.eq(CssPayment::getBusinessScope, cssPayment.getBusinessScope()).eq(CssPayment::getOrgId, SecurityUtils.getUser().getOrgId());
            List<Integer> listPayment = list(wrapperCssPayment).stream().map(c -> c.getPaymentId()).collect(Collectors.toList());
            costIds.stream().forEach(costId -> {
                LambdaQueryWrapper<CssPaymentDetail> detailWrapperForCost = Wrappers.<CssPaymentDetail>lambdaQuery();
                detailWrapperForCost.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, costId).in(CssPaymentDetail::getPaymentId, listPayment);
                HashMap<String, BigDecimal> sumMap = new HashMap<>();
                cssPaymentDetailService.list(detailWrapperForCost).stream().forEach(cssPaymentDetail -> {
                    if (cssPaymentDetail.getAmountPayment() != null) {
                        if (sumMap.get("sum") == null) {
                            sumMap.put("sum", cssPaymentDetail.getAmountPayment());
                        } else {
                            sumMap.put("sum", sumMap.get("sum").add(cssPaymentDetail.getAmountPayment()));
                        }
                    }
                });
                if ("AE".equals(cssPayment.getBusinessScope()) || "AI".equals(cssPayment.getBusinessScope())) {
                    AfCost cost = afCostService.getById(costId);
                    cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                    afCostService.updateById(cost);
                }
                if ("SE".equals(cssPayment.getBusinessScope()) || "SI".equals(cssPayment.getBusinessScope())) {
                    ScCost cost = scCostService.getById(costId);
                    cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                    scCostService.updateById(cost);
                }
                if (cssPayment.getBusinessScope().startsWith("T")) {
                    TcCost cost = tcCostService.getById(costId);
                    cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                    tcCostService.updateById(cost);
                }
                if (cssPayment.getBusinessScope().startsWith("L")) {
                    LcCost cost = lcCostService.getById(costId);
                    cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                    lcCostService.updateById(cost);
                }
                if (cssPayment.getBusinessScope().equals("IO")) {
                    IoCost cost = ioCostService.getById(costId);
                    cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                    ioCostService.updateById(cost);
                }

            });
        }
        //3.保存条件-本次对账金额不能大于未对账金额
        cssPayment.getDetails().stream().forEach(cssPaymentDetail -> {

            if ("AE".equals(cssPayment.getBusinessScope()) || "AI".equals(cssPayment.getBusinessScope())) {
                AfCost cost = afCostService.getById(cssPaymentDetail.getCostId());
                if (cost == null) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的成本不存在,保存失败");
                }
                if (StrUtil.isNotBlank(cost.getRowUuid()) && !cost.getRowUuid().equals(cssPaymentDetail.getRowUuid())) {
                    throw new RuntimeException("对账单下成本不是最新数据，请刷新页面重新操作");
                }
                BigDecimal costAmountNoPayment = null;
                if (cost.getCostAmountPayment() == null) {
                    costAmountNoPayment = cost.getCostAmount();
                } else {
                    costAmountNoPayment = cost.getCostAmount().subtract(cost.getCostAmountPayment());
                }

                if (costAmountNoPayment.signum() == -1) {
                    if (cssPaymentDetail.getAmountPayment().signum() == 1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能>0,保存失败");
                    } else {
                        if (costAmountNoPayment.abs().compareTo(cssPaymentDetail.getAmountPayment().abs()) == -1) {
                            throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                        }
                    }
                } else if (costAmountNoPayment.signum() == 0) {
                    if (cssPaymentDetail.getAmountPayment().signum() != 0) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细已经完全对账,不能再对账,保存失败");
                    }
                } else {
                    if (cssPaymentDetail.getAmountPayment().signum() == -1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能<0,保存失败");
                    } else {
                        if (costAmountNoPayment.compareTo(cssPaymentDetail.getAmountPayment()) == -1) {
                            throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                        }
                    }
                }
            }
            if ("SE".equals(cssPayment.getBusinessScope()) || "SI".equals(cssPayment.getBusinessScope())) {
                ScCost cost = scCostService.getById(cssPaymentDetail.getCostId());
                if (cost == null) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的成本不存在,保存失败");
                }
                if (StrUtil.isNotBlank(cost.getRowUuid()) && !cost.getRowUuid().equals(cssPaymentDetail.getRowUuid())) {
                    throw new RuntimeException("对账单下成本不是最新数据，请刷新页面重新操作");
                }
                BigDecimal costAmountNoPayment = null;
                if (cost.getCostAmountPayment() == null) {
                    costAmountNoPayment = cost.getCostAmount();
                } else {
                    costAmountNoPayment = cost.getCostAmount().subtract(cost.getCostAmountPayment());
                }

                if (costAmountNoPayment.signum() == -1) {
                    if (cssPaymentDetail.getAmountPayment().signum() == 1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能>0,保存失败");
                    } else {
                        if (costAmountNoPayment.abs().compareTo(cssPaymentDetail.getAmountPayment().abs()) == -1) {
                            throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                        }
                    }
                } else if (costAmountNoPayment.signum() == 0) {
                    if (cssPaymentDetail.getAmountPayment().signum() != 0) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细已经完全对账,不能再对账,保存失败");
                    }
                } else {
                    if (cssPaymentDetail.getAmountPayment().signum() == -1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能<0,保存失败");
                    } else {
                        if (costAmountNoPayment.compareTo(cssPaymentDetail.getAmountPayment()) == -1) {
                            throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                        }
                    }
                }

            }

            if (cssPayment.getBusinessScope().startsWith("L")) {
                LcCost cost = lcCostService.getById(cssPaymentDetail.getCostId());
                if (cost == null) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的成本不存在,保存失败");
                }
                if (StrUtil.isNotBlank(cost.getRowUuid()) && !cost.getRowUuid().equals(cssPaymentDetail.getRowUuid())) {
                    throw new RuntimeException("对账单下成本不是最新数据，请刷新页面重新操作");
                }
                BigDecimal costAmountNoPayment = null;
                if (cost.getCostAmountPayment() == null) {
                    costAmountNoPayment = cost.getCostAmount();
                } else {
                    costAmountNoPayment = cost.getCostAmount().subtract(cost.getCostAmountPayment());
                }

                if (costAmountNoPayment.signum() == -1) {
                    if (cssPaymentDetail.getAmountPayment().signum() == 1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能>0,保存失败");
                    } else {
                        if (costAmountNoPayment.abs().compareTo(cssPaymentDetail.getAmountPayment().abs()) == -1) {
                            throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                        }
                    }
                } else if (costAmountNoPayment.signum() == 0) {
                    if (cssPaymentDetail.getAmountPayment().signum() != 0) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细已经完全对账,不能再对账,保存失败");
                    }
                } else {
                    if (cssPaymentDetail.getAmountPayment().signum() == -1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能<0,保存失败");
                    } else {
                        if (costAmountNoPayment.compareTo(cssPaymentDetail.getAmountPayment()) == -1) {
                            throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                        }
                    }
                }

            }
            if (cssPayment.getBusinessScope().equals("IO")) {
                IoCost cost = ioCostService.getById(cssPaymentDetail.getCostId());
                if (cost == null) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的成本不存在,保存失败");
                }
                if (StrUtil.isNotBlank(cost.getRowUuid()) && !cost.getRowUuid().equals(cssPaymentDetail.getRowUuid())) {
                    throw new RuntimeException("对账单下成本不是最新数据，请刷新页面重新操作");
                }
                BigDecimal costAmountNoPayment = null;
                if (cost.getCostAmountPayment() == null) {
                    costAmountNoPayment = cost.getCostAmount();
                } else {
                    costAmountNoPayment = cost.getCostAmount().subtract(cost.getCostAmountPayment());
                }

                if (costAmountNoPayment.signum() == -1) {
                    if (cssPaymentDetail.getAmountPayment().signum() == 1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能>0,保存失败");
                    } else {
                        if (costAmountNoPayment.abs().compareTo(cssPaymentDetail.getAmountPayment().abs()) == -1) {
                            throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                        }
                    }
                } else if (costAmountNoPayment.signum() == 0) {
                    if (cssPaymentDetail.getAmountPayment().signum() != 0) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细已经完全对账,不能再对账,保存失败");
                    }
                } else {
                    if (cssPaymentDetail.getAmountPayment().signum() == -1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能<0,保存失败");
                    } else {
                        if (costAmountNoPayment.compareTo(cssPaymentDetail.getAmountPayment()) == -1) {
                            throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                        }
                    }
                }

            }
            if (cssPayment.getBusinessScope().startsWith("T")) {
                TcCost cost = tcCostService.getById(cssPaymentDetail.getCostId());
                if (cost == null) {
                    throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的成本不存在,保存失败");
                }
                if (StrUtil.isNotBlank(cost.getRowUuid()) && !cost.getRowUuid().equals(cssPaymentDetail.getRowUuid())) {
                    throw new RuntimeException("对账单下成本不是最新数据，请刷新页面重新操作");
                }
                BigDecimal costAmountNoPayment = null;
                if (cost.getCostAmountPayment() == null) {
                    costAmountNoPayment = cost.getCostAmount();
                } else {
                    costAmountNoPayment = cost.getCostAmount().subtract(cost.getCostAmountPayment());
                }

                if (costAmountNoPayment.signum() == -1) {
                    if (cssPaymentDetail.getAmountPayment().signum() == 1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能>0,保存失败");
                    } else {
                        if (costAmountNoPayment.abs().compareTo(cssPaymentDetail.getAmountPayment().abs()) == -1) {
                            throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                        }
                    }
                } else if (costAmountNoPayment.signum() == 0) {
                    if (cssPaymentDetail.getAmountPayment().signum() != 0) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细已经完全对账,不能再对账,保存失败");
                    }
                } else {
                    if (cssPaymentDetail.getAmountPayment().signum() == -1) {
                        throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能<0,保存失败");
                    } else {
                        if (costAmountNoPayment.compareTo(cssPaymentDetail.getAmountPayment()) == -1) {
                            throw new RuntimeException("单号为:" + cssPaymentDetail.getAwbOrOrderNumber() + "的对账明细本次对账金额不能超过未对账金额,保存失败");
                        }
                    }
                }

            }

        });
        //4.修改对账单
        cssPayment.setEditTime(LocalDateTime.now());
        cssPayment.setEditorId(SecurityUtils.getUser().getId());
        cssPayment.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        cssPayment.setRowUuid(UUID.randomUUID().toString());
        updateById(cssPayment);

        //5.修改对账明细
        if (cssPayment.getDetails() != null && cssPayment.getDetails().size() > 0) {
            cssPayment.getDetails().stream().forEach(cssPaymentDetail -> {
                cssPaymentDetail.setOrgId(SecurityUtils.getUser().getOrgId());
                cssPaymentDetail.setPaymentId(cssPayment.getPaymentId());
            });
            cssPaymentDetailService.saveBatch(cssPayment.getDetails());
        }

        //6.最终更新cost对账金额
        costIds.addAll(costIdsOperationBefore);
        orderIds.addAll(orderIdsOperationBefore);
        List<Integer> costIdsFinal = costIds.stream().distinct().collect(Collectors.toList());
        if (costIdsFinal.size() != 0) {
            LambdaQueryWrapper<CssPayment> wrapperCssPayment = Wrappers.<CssPayment>lambdaQuery();
            wrapperCssPayment.eq(CssPayment::getBusinessScope, cssPayment.getBusinessScope()).eq(CssPayment::getOrgId, SecurityUtils.getUser().getOrgId());
            List<Integer> listPayment = list(wrapperCssPayment).stream().map(c -> c.getPaymentId()).collect(Collectors.toList());
            costIdsFinal.stream().forEach(costId -> {
                LambdaQueryWrapper<CssPaymentDetail> detailWrapperForCost = Wrappers.<CssPaymentDetail>lambdaQuery();
                detailWrapperForCost.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, costId).in(CssPaymentDetail::getPaymentId, listPayment);
                HashMap<String, BigDecimal> sumMap = new HashMap<>();
                cssPaymentDetailService.list(detailWrapperForCost).stream().forEach(cssPaymentDetail -> {
                    if (cssPaymentDetail.getAmountPayment() != null) {
                        if (sumMap.get("sum") == null) {
                            sumMap.put("sum", cssPaymentDetail.getAmountPayment());
                        } else {
                            sumMap.put("sum", sumMap.get("sum").add(cssPaymentDetail.getAmountPayment()));
                        }
                    }
                });
                if ("AE".equals(cssPayment.getBusinessScope()) || "AI".equals(cssPayment.getBusinessScope())) {
                    AfCost cost = afCostService.getById(costId);
                    cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                    //修复汇率，本币金额，条件：币种!=CNY
                    if (!"CNY".equals(cost.getCostCurrency())) {
                        cost.setCostExchangeRate(cssPayment.getExchangeRate());
                        if (cost.getCostAmount() != null && cssPayment.getExchangeRate() != null) {
                            BigDecimal decimal = cssPayment.getExchangeRate().multiply(cost.getCostAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
                            cost.setCostFunctionalAmount(decimal);
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("干线")) {
                                cost.setMainRouting(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("支线")) {
                                cost.setFeeder(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("操作")) {
                                cost.setOperation(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("包装")) {
                                cost.setPackaging(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("仓储")) {
                                cost.setStorage(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("快递")) {
                                cost.setPostage(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("关检")) {
                                cost.setClearance(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("数据")) {
                                cost.setExchange(decimal);
                            }
                        }
                    }
                    cost.setRowUuid(UUID.randomUUID().toString());
                    afCostService.updateById(cost);
                }
                if ("SE".equals(cssPayment.getBusinessScope()) || "SI".equals(cssPayment.getBusinessScope())) {
                    ScCost cost = scCostService.getById(costId);
                    cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                    //修复汇率，本币金额，条件：币种!=CNY
                    if (!"CNY".equals(cost.getCostCurrency())) {
                        cost.setCostExchangeRate(cssPayment.getExchangeRate());
                        if (cost.getCostAmount() != null && cssPayment.getExchangeRate() != null) {
                            BigDecimal decimal = cssPayment.getExchangeRate().multiply(cost.getCostAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
                            cost.setCostFunctionalAmount(decimal);
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("干线")) {
                                cost.setMainRouting(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("支线")) {
                                cost.setFeeder(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("操作")) {
                                cost.setOperation(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("包装")) {
                                cost.setPackaging(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("仓储")) {
                                cost.setStorage(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("快递")) {
                                cost.setPostage(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("关检")) {
                                cost.setClearance(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("数据")) {
                                cost.setExchange(decimal);
                            }
                        }
                    }
                    cost.setRowUuid(UUID.randomUUID().toString());
                    scCostService.updateById(cost);
                }
                if (cssPayment.getBusinessScope().startsWith("T")) {
                    TcCost cost = tcCostService.getById(costId);
                    cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                    //修复汇率，本币金额，条件：币种!=CNY
                    if (!"CNY".equals(cost.getCostCurrency())) {
                        cost.setCostExchangeRate(cssPayment.getExchangeRate());
                        if (cost.getCostAmount() != null && cssPayment.getExchangeRate() != null) {
                            BigDecimal decimal = cssPayment.getExchangeRate().multiply(cost.getCostAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
                            cost.setCostFunctionalAmount(decimal);
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("干线")) {
                                cost.setMainRouting(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("支线")) {
                                cost.setFeeder(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("操作")) {
                                cost.setOperation(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("包装")) {
                                cost.setPackaging(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("仓储")) {
                                cost.setStorage(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("快递")) {
                                cost.setPostage(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("关检")) {
                                cost.setClearance(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("数据")) {
                                cost.setExchange(decimal);
                            }
                        }
                    }
                    cost.setRowUuid(UUID.randomUUID().toString());
                    tcCostService.updateById(cost);
                }

                if ("LC".equals(cssPayment.getBusinessScope())) {
                    LcCost cost = lcCostService.getById(costId);
                    cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                    //修复汇率，本币金额，条件：币种!=CNY
                    if (!"CNY".equals(cost.getCostCurrency())) {
                        cost.setCostExchangeRate(cssPayment.getExchangeRate());
                        if (cost.getCostAmount() != null && cssPayment.getExchangeRate() != null) {
                            BigDecimal decimal = cssPayment.getExchangeRate().multiply(cost.getCostAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
                            cost.setCostFunctionalAmount(decimal);
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("干线")) {
                                cost.setMainRouting(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("支线")) {
                                cost.setFeeder(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("操作")) {
                                cost.setOperation(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("包装")) {
                                cost.setPackaging(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("仓储")) {
                                cost.setStorage(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("快递")) {
                                cost.setPostage(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("关检")) {
                                cost.setClearance(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("数据")) {
                                cost.setExchange(decimal);
                            }
                        }
                    }
                    cost.setRowUuid(UUID.randomUUID().toString());
                    lcCostService.updateById(cost);
                }
                if ("IO".equals(cssPayment.getBusinessScope())) {
                    IoCost cost = ioCostService.getById(costId);
                    cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                    //修复汇率，本币金额，条件：币种!=CNY
                    if (!"CNY".equals(cost.getCostCurrency())) {
                        cost.setCostExchangeRate(cssPayment.getExchangeRate());
                        if (cost.getCostAmount() != null && cssPayment.getExchangeRate() != null) {
                            BigDecimal decimal = cssPayment.getExchangeRate().multiply(cost.getCostAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
                            cost.setCostFunctionalAmount(decimal);
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("干线")) {
                                cost.setMainRouting(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("支线")) {
                                cost.setFeeder(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("操作")) {
                                cost.setOperation(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("包装")) {
                                cost.setPackaging(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("仓储")) {
                                cost.setStorage(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("快递")) {
                                cost.setPostage(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("关检")) {
                                cost.setClearance(decimal);
                            }
                            if (StrUtil.isNotBlank(cost.getServiceName()) && cost.getServiceName().startsWith("数据")) {
                                cost.setExchange(decimal);
                            }
                        }
                    }
                    cost.setRowUuid(UUID.randomUUID().toString());
                    ioCostService.updateById(cost);
                }
            });
        }

        //更新订单状态
        if (orderIds.size() != 0) {
            if ("AE".equals(cssPayment.getBusinessScope()) || "AI".equals(cssPayment.getBusinessScope())) {
                orderIds.stream().distinct().forEach(orderId -> {
                    AfOrder order = afOrderService.getById(orderId);
                    order.setCostStatus(afOrderService.getOrderCostStatusForAF(orderId));
                    order.setRowUuid(UUID.randomUUID().toString());
                    afOrderService.updateById(order);
                });
            }
            if ("SE".equals(cssPayment.getBusinessScope()) || "SI".equals(cssPayment.getBusinessScope())) {
                orderIds.stream().distinct().forEach(orderId -> {
                    afOrderService.updateOrderCostStatusForSC(orderId);
                });
            }
            if ("TE".equals(cssPayment.getBusinessScope()) || "TI".equals(cssPayment.getBusinessScope())) {
                orderIds.stream().distinct().forEach(orderId -> {
                    TcOrder order = tcOrderService.getById(orderId);
                    order.setCostStatus(afOrderService.getOrderCostStatusForTC(orderId));
                    order.setRowUuid(UUID.randomUUID().toString());
                    tcOrderService.updateById(order);
                });
            }
            if ("LC".equals(cssPayment.getBusinessScope())) {
                orderIds.stream().distinct().forEach(orderId -> {
                    LcOrder order = lcOrderService.getById(orderId);
                    order.setCostStatus(afOrderService.getOrderCostStatusForLC(orderId));
                    order.setRowUuid(UUID.randomUUID().toString());
                    lcOrderService.updateById(order);
                });
            }
            if ("IO".equals(cssPayment.getBusinessScope())) {
                orderIds.stream().distinct().forEach(orderId -> {
                    IoOrder order = ioOrderService.getById(orderId);
                    order.setCostStatus(afOrderService.getOrderCostStatusForIO(orderId));
                    order.setRowUuid(UUID.randomUUID().toString());
                    ioOrderService.updateById(order);
                });
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer paymentId, String rowUuid) {
        //0.校验对账单状态可否修改
        CssPayment cssPayment = getById(paymentId);
        if (cssPayment == null) {
            throw new RuntimeException("对账单不存在，请刷新页面再操作");
        }
        if (!cssPayment.getRowUuid().equals(rowUuid)) {
            throw new RuntimeException("对账单不是最新数据，请刷新页面再操作");
        }
        if (cssPayment.getWriteoffComplete() != null) {
            throw new RuntimeException("对账单已核销，无法删除");
        }
        //校验如何已经发票申请不可删除
        LambdaQueryWrapper<CssCostInvoice> cssCostInvoiceWrapper = Wrappers.<CssCostInvoice>lambdaQuery();
        cssCostInvoiceWrapper.eq(CssCostInvoice::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoice::getPaymentId, cssPayment.getPaymentId());
        CssCostInvoice cssCostInvoice = cssCostInvoiceMapper.selectOne(cssCostInvoiceWrapper);
        if (cssCostInvoice != null) {
            throw new RuntimeException(cssPayment.getPaymentNum() + "对账单 已申请付款，不能删除。");
        }
        //删除对账单
        removeById(paymentId);
        //删除明细
        LambdaQueryWrapper<CssPaymentDetail> detailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
        detailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getPaymentId, paymentId);

        List<CssPaymentDetail> list = cssPaymentDetailService.list(detailWrapper);
        List<Integer> costIds = list.stream().map(CssPaymentDetail::getCostId).collect(Collectors.toList());
        List<Integer> orderIds = list.stream().map(CssPaymentDetail::getOrderId).collect(Collectors.toList());
        cssPaymentDetailService.remove(detailWrapper);
        //更新cost对账金额
        if (costIds.size() != 0) {
            LambdaQueryWrapper<CssPayment> wrapperCssPayment = Wrappers.<CssPayment>lambdaQuery();
            wrapperCssPayment.eq(CssPayment::getBusinessScope, cssPayment.getBusinessScope()).eq(CssPayment::getOrgId, SecurityUtils.getUser().getOrgId());
            List<Integer> listPayment = list(wrapperCssPayment).stream().map(c -> c.getPaymentId()).collect(Collectors.toList());
            costIds.stream().forEach(costId -> {
                LambdaQueryWrapper<CssPaymentDetail> detailWrapperForCost = Wrappers.<CssPaymentDetail>lambdaQuery();
                detailWrapperForCost.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, costId);
                HashMap<String, BigDecimal> sumMap = new HashMap<>();
                if (listPayment != null && listPayment.size() > 0) {
                    detailWrapperForCost.in(CssPaymentDetail::getPaymentId, listPayment);
                    cssPaymentDetailService.list(detailWrapperForCost).stream().forEach(cssPaymentDetail -> {
                        if (cssPaymentDetail.getAmountPayment() != null) {
                            if (sumMap.get("sum") == null) {
                                sumMap.put("sum", cssPaymentDetail.getAmountPayment());
                            } else {
                                sumMap.put("sum", sumMap.get("sum").add(cssPaymentDetail.getAmountPayment()));
                            }
                        }
                    });
                }
                if ("SE".equals(cssPayment.getBusinessScope()) || "SI".equals(cssPayment.getBusinessScope())) {
                    ScCost cost = scCostService.getById(costId);
                    cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                    cost.setRowUuid(UUID.randomUUID().toString());
                    scCostService.updateById(cost);
                }
                if ("AE".equals(cssPayment.getBusinessScope()) || "AI".equals(cssPayment.getBusinessScope())) {
                    AfCost cost = afCostService.getById(costId);
                    cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                    cost.setRowUuid(UUID.randomUUID().toString());
                    afCostService.updateById(cost);
                }
                if ("TE".equals(cssPayment.getBusinessScope()) || "TI".equals(cssPayment.getBusinessScope())) {
                    TcCost cost = tcCostService.getById(costId);
                    cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                    cost.setRowUuid(UUID.randomUUID().toString());
                    tcCostService.updateById(cost);
                }
                if ("LC".equals(cssPayment.getBusinessScope())) {
                    LcCost cost = lcCostService.getById(costId);
                    cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                    cost.setRowUuid(UUID.randomUUID().toString());
                    lcCostService.updateById(cost);
                }
                if ("IO".equals(cssPayment.getBusinessScope())) {
                    IoCost cost = ioCostService.getById(costId);
                    cost.setCostAmountPayment(sumMap.get("sum") == null ? BigDecimal.ZERO : sumMap.get("sum"));
                    cost.setRowUuid(UUID.randomUUID().toString());
                    ioCostService.updateById(cost);
                }
            });
        }
        //更新订单状态
        if (orderIds.size() != 0) {
            if ("AE".equals(cssPayment.getBusinessScope()) || "AI".equals(cssPayment.getBusinessScope())) {
                orderIds.stream().distinct().forEach(orderId -> {
                    AfOrder order = afOrderService.getById(orderId);
                    order.setCostStatus(afOrderService.getOrderCostStatusForAF(orderId));
                    order.setRowUuid(UUID.randomUUID().toString());
                    afOrderService.updateById(order);
                });
            }
            if ("SE".equals(cssPayment.getBusinessScope()) || "SI".equals(cssPayment.getBusinessScope())) {
                orderIds.stream().distinct().forEach(orderId -> {
                    afOrderService.updateOrderCostStatusForSC(orderId);
                });
            }
            if (cssPayment.getBusinessScope().startsWith("T")) {
                orderIds.stream().distinct().forEach(orderId -> {
                    TcOrder order = tcOrderService.getById(orderId);
                    order.setCostStatus(afOrderService.getOrderCostStatusForTC(orderId));
                    order.setRowUuid(UUID.randomUUID().toString());
                    tcOrderService.updateById(order);
                });
            }
            if (cssPayment.getBusinessScope().startsWith("L")) {
                orderIds.stream().distinct().forEach(orderId -> {
                    LcOrder order = lcOrderService.getById(orderId);
                    order.setCostStatus(afOrderService.getOrderCostStatusForLC(orderId));
                    order.setRowUuid(UUID.randomUUID().toString());
                    lcOrderService.updateById(order);
                });
            }
            if (cssPayment.getBusinessScope().equals("IO")) {
                orderIds.stream().distinct().forEach(orderId -> {
                    IoOrder order = ioOrderService.getById(orderId);
                    order.setCostStatus(afOrderService.getOrderCostStatusForIO(orderId));
                    order.setRowUuid(UUID.randomUUID().toString());
                    ioOrderService.updateById(order);
                });
            }
        }
    }


    @Override
    public CssPayment view(Integer paymentId) {
        CssPayment cssPayment = getById(paymentId);
        if (cssPayment == null) {
            throw new RuntimeException("对账单不存在，请刷新页面再操作");
        }
        if (cssPayment.getAmountPaymentWriteoff() == null) {
            cssPayment.setAmountPaymentNoWriteoff(cssPayment.getAmountPayment());
        } else {
            cssPayment.setAmountPaymentNoWriteoff(cssPayment.getAmountPayment().subtract(cssPayment.getAmountPaymentWriteoff()));
        }
        LambdaQueryWrapper<CssPaymentDetail> detailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
        detailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getPaymentId, paymentId);
        List<CssPaymentDetail> details = cssPaymentDetailService.list(detailWrapper);
        details.stream().forEach(cssPaymentDetail -> {
            //空运 AE,AI
            if ("AE".equals(cssPayment.getBusinessScope()) || "AI".equals(cssPayment.getBusinessScope())) {
                AfCost cost = afCostService.getById(cssPaymentDetail.getCostId());
                cssPaymentDetail.setCostAmount(cost.getCostAmount());
                cssPaymentDetail.setServiceName(cost.getServiceName());
                cssPaymentDetail.setRowUuid(cost.getRowUuid());

                //封装金额显示格式xxx(CNY)
                if (cssPaymentDetail.getAmountPayment() != null) {
                    cssPaymentDetail.setAmountPaymentStr(formatWith2AndQFW(cssPaymentDetail.getAmountPayment()) + " (" + cssPaymentDetail.getCurrency() + ")");
                }
                if (cssPaymentDetail.getCostAmount() != null) {
                    cssPaymentDetail.setCostAmountStr(formatWith2AndQFW(cssPaymentDetail.getCostAmount()) + " (" + cssPaymentDetail.getCurrency() + ")");
                }

                AfOrder order = afOrderService.getById(cssPaymentDetail.getOrderId());
                String awbOrOrderNumber = "";
                if (StrUtil.isNotBlank(order.getAwbNumber())) {
                    if ("AE".equals(cssPayment.getBusinessScope())) {
                        awbOrOrderNumber = order.getAwbNumber();
                    }
                    if ("AI".equals(cssPayment.getBusinessScope())) {
                        awbOrOrderNumber = StrUtil.isNotBlank(order.getHawbNumber()) ? (order.getAwbNumber() + "_" + order.getHawbNumber()) : order.getAwbNumber();
                    }
                } else if (StrUtil.isNotBlank(order.getHawbNumber()) && "AI".equals(cssPayment.getBusinessScope())) {
                    awbOrOrderNumber = order.getHawbNumber();
                } else {
                    awbOrOrderNumber = order.getOrderCode();
                }
                if ("AE".equals(cssPayment.getBusinessScope())) {
                    cssPaymentDetail.setFlightDate(order.getExpectDeparture());
                }
                if ("AI".equals(cssPayment.getBusinessScope())) {
                    cssPaymentDetail.setFlightDate(order.getExpectArrival());
                }
                cssPaymentDetail.setAwbOrOrderNumber(awbOrOrderNumber);
                //添加客户单号
                cssPaymentDetail.setCustomerNumber(order.getCustomerNumber());
            }
            //海运 SE,SI
            if ("SE".equals(cssPayment.getBusinessScope()) || "SI".equals(cssPayment.getBusinessScope())) {
                ScCost cost = scCostService.getById(cssPaymentDetail.getCostId());
                cssPaymentDetail.setCostAmount(cost.getCostAmount());
                cssPaymentDetail.setServiceName(cost.getServiceName());
                cssPaymentDetail.setRowUuid(cost.getRowUuid());
                //封装金额显示格式xxx(CNY)
                if (cssPaymentDetail.getAmountPayment() != null) {
                    cssPaymentDetail.setAmountPaymentStr(formatWith2AndQFW(cssPaymentDetail.getAmountPayment()) + " (" + cssPaymentDetail.getCurrency() + ")");
                }
                if (cssPaymentDetail.getCostAmount() != null) {
                    cssPaymentDetail.setCostAmountStr(formatWith2AndQFW(cssPaymentDetail.getCostAmount()) + " (" + cssPaymentDetail.getCurrency() + ")");
                }

                ScOrder order = scOrderService.getById(cssPaymentDetail.getOrderId());
                String awbOrOrderNumber = "";
                if (StrUtil.isNotBlank(order.getMblNumber())) {
                    if ("SE".equals(cssPayment.getBusinessScope())) {
                        awbOrOrderNumber = order.getMblNumber();
                    }
                    if ("SI".equals(cssPayment.getBusinessScope())) {
                        awbOrOrderNumber = StrUtil.isNotBlank(order.getHblNumber()) ? (order.getMblNumber() + "_" + order.getHblNumber()) : order.getMblNumber();
                    }
                } else if (StrUtil.isNotBlank(order.getHblNumber()) && "SI".equals(cssPayment.getBusinessScope())) {
                    awbOrOrderNumber = order.getHblNumber();
                } else {
                    awbOrOrderNumber = order.getOrderCode();
                }
                cssPaymentDetail.setAwbOrOrderNumber(awbOrOrderNumber);
                if ("SE".equals(cssPayment.getBusinessScope())) {
                    cssPaymentDetail.setFlightDate(order.getExpectDeparture());
                }
                if ("SI".equals(cssPayment.getBusinessScope())) {
                    cssPaymentDetail.setFlightDate(order.getExpectArrival());
                }
                //添加客户单号
                cssPaymentDetail.setCustomerNumber(order.getCustomerNumber());
            }
            if (cssPayment.getBusinessScope().startsWith("T")) {
                TcCost cost = tcCostService.getById(cssPaymentDetail.getCostId());
                cssPaymentDetail.setCostAmount(cost.getCostAmount());
                cssPaymentDetail.setServiceName(cost.getServiceName());
                cssPaymentDetail.setRowUuid(cost.getRowUuid());
                //封装金额显示格式xxx(CNY)
                if (cssPaymentDetail.getAmountPayment() != null) {
                    cssPaymentDetail.setAmountPaymentStr(formatWith2AndQFW(cssPaymentDetail.getAmountPayment()) + " (" + cssPaymentDetail.getCurrency() + ")");
                }
                if (cssPaymentDetail.getCostAmount() != null) {
                    cssPaymentDetail.setCostAmountStr(formatWith2AndQFW(cssPaymentDetail.getCostAmount()) + " (" + cssPaymentDetail.getCurrency() + ")");
                }

                TcOrder order = tcOrderService.getById(cssPaymentDetail.getOrderId());
                String awbOrOrderNumber = "";
                if (StrUtil.isNotBlank(order.getRwbNumber())) {
                    awbOrOrderNumber = order.getRwbNumber();
                } else {
                    awbOrOrderNumber = order.getOrderCode();
                }
                cssPaymentDetail.setAwbOrOrderNumber(awbOrOrderNumber);
                if ("TE".equals(cssPayment.getBusinessScope())) {
                    cssPaymentDetail.setFlightDate(order.getExpectDeparture());
                }
                if ("TI".equals(cssPayment.getBusinessScope())) {
                    cssPaymentDetail.setFlightDate(order.getExpectArrival());
                }
                //添加客户单号
                cssPaymentDetail.setCustomerNumber(order.getCustomerNumber());
            }

            if (cssPayment.getBusinessScope().startsWith("L")) {
                LcCost cost = lcCostService.getById(cssPaymentDetail.getCostId());
                cssPaymentDetail.setCostAmount(cost.getCostAmount());
                cssPaymentDetail.setServiceName(cost.getServiceName());
                cssPaymentDetail.setRowUuid(cost.getRowUuid());
                //封装金额显示格式xxx(CNY)
                if (cssPaymentDetail.getAmountPayment() != null) {
                    cssPaymentDetail.setAmountPaymentStr(formatWith2AndQFW(cssPaymentDetail.getAmountPayment()) + " (" + cssPaymentDetail.getCurrency() + ")");
                }
                if (cssPaymentDetail.getCostAmount() != null) {
                    cssPaymentDetail.setCostAmountStr(formatWith2AndQFW(cssPaymentDetail.getCostAmount()) + " (" + cssPaymentDetail.getCurrency() + ")");
                }

                LcOrder order = lcOrderService.getById(cssPaymentDetail.getOrderId());
                String awbOrOrderNumber = "";
                if (StrUtil.isNotBlank(order.getCustomerNumber())) {
                    awbOrOrderNumber = order.getCustomerNumber();
                } else {
                    awbOrOrderNumber = order.getOrderCode();
                }
                cssPaymentDetail.setAwbOrOrderNumber(awbOrOrderNumber);
                if (order.getDrivingTime() != null) {
                    cssPaymentDetail.setFlightDate(order.getDrivingTime().toLocalDate());
                }
                //添加客户单号
                cssPaymentDetail.setCustomerNumber(order.getCustomerNumber());
            }

            if (cssPayment.getBusinessScope().equals("IO")) {
                IoCost cost = ioCostService.getById(cssPaymentDetail.getCostId());
                cssPaymentDetail.setCostAmount(cost.getCostAmount());
                cssPaymentDetail.setServiceName(cost.getServiceName());
                cssPaymentDetail.setRowUuid(cost.getRowUuid());
                //封装金额显示格式xxx(CNY)
                if (cssPaymentDetail.getAmountPayment() != null) {
                    cssPaymentDetail.setAmountPaymentStr(formatWith2AndQFW(cssPaymentDetail.getAmountPayment()) + " (" + cssPaymentDetail.getCurrency() + ")");
                }
                if (cssPaymentDetail.getCostAmount() != null) {
                    cssPaymentDetail.setCostAmountStr(formatWith2AndQFW(cssPaymentDetail.getCostAmount()) + " (" + cssPaymentDetail.getCurrency() + ")");
                }

                IoOrder order = ioOrderService.getById(cssPaymentDetail.getOrderId());
                String awbOrOrderNumber = "";
                if (StrUtil.isNotBlank(order.getCustomerNumber())) {
                    awbOrOrderNumber = order.getCustomerNumber();
                } else {
                    awbOrOrderNumber = order.getOrderCode();
                }
                cssPaymentDetail.setAwbOrOrderNumber(awbOrOrderNumber);
                if (order.getBusinessDate() != null) {
                    cssPaymentDetail.setFlightDate(order.getBusinessDate());
                }
                //添加客户单号
                cssPaymentDetail.setCustomerNumber(order.getCustomerNumber());
            }

        });
        cssPayment.setDetails(details);
        return cssPayment;
    }

    @Override
    public void exportExcel(Integer paymentId) {
        CssPayment payment = getById(paymentId);
        LambdaQueryWrapper<CssPaymentDetail> wrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
        wrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getPaymentId, paymentId);

        CoopVo customer = remoteCoopService.viewCoop(payment.getCustomerId().toString()).getData();
        List<PaymentExcel> list = cssPaymentDetailService.list(wrapper).stream().map(cssPaymentDetail -> {
            PaymentExcel paymentExcel = new PaymentExcel();
            paymentExcel.setAmountPayment(formatWith2AndQFW(cssPaymentDetail.getAmountPayment()));
            BigDecimal writeOff = BigDecimal.ZERO;
            if (cssPaymentDetail.getAmountPaymentWriteoff() != null) {
                writeOff = cssPaymentDetail.getAmountPaymentWriteoff();
            }
            paymentExcel.setCostAmount(formatWith2AndQFW(writeOff));
            paymentExcel.setAmountNoWriteOffPayment(formatWith2AndQFW(cssPaymentDetail.getAmountPayment().subtract(writeOff)));

            if ("AE".equals(payment.getBusinessScope()) || "AI".equals(payment.getBusinessScope())) {
                AfOrder order = afOrderService.getById(cssPaymentDetail.getOrderId());
                if (order != null) {
                    if ("AE".equals(payment.getBusinessScope())) {
                        paymentExcel.setAwbNumber(order.getAwbNumber());
                        Integer pieces = order.getConfirmPieces() == null ? order.getPlanPieces() : order.getConfirmPieces();
                        Double volume = order.getConfirmVolume() == null ? order.getPlanVolume() : order.getConfirmVolume();
                        BigDecimal weight = order.getConfirmWeight() == null ? order.getPlanWeight() : order.getConfirmWeight();
                        Double chargeWeight = order.getConfirmChargeWeight() == null ? order.getPlanChargeWeight() : order.getConfirmChargeWeight();
                        paymentExcel.setPieces(pieces);
                        if (volume != null) {
                            paymentExcel.setVolume(FormatUtils.formatWithQWFNoBit(BigDecimal.valueOf(volume)));
                        }
                        if (chargeWeight != null) {
                            paymentExcel.setChargeWeight(FormatUtils.formatWithQWFNoBit(BigDecimal.valueOf(chargeWeight)));
                        }
                        if (weight != null) {
                            paymentExcel.setWeight(FormatUtils.formatWithQWFNoBit(weight));
                        }
                        paymentExcel.setFlightDate(order.getExpectDeparture());
                    } else {
                        String str = "";
                        if (!StringUtils.isEmpty(order.getHawbNumber())) {
                            str = "_" + order.getHawbNumber();
                        }
                        paymentExcel.setAwbNumber(order.getAwbNumber() + str);

                        paymentExcel.setPieces(order.getPlanPieces());
                        if (order.getPlanVolume() != null) {
                            paymentExcel.setVolume(FormatUtils.formatWithQWFNoBit(BigDecimal.valueOf(order.getPlanVolume())));
                        }
                        if (order.getPlanChargeWeight() != null) {
                            paymentExcel.setChargeWeight(FormatUtils.formatWithQWFNoBit(BigDecimal.valueOf(order.getPlanChargeWeight())));
                        }
                        if (order.getPlanWeight() != null) {
                            paymentExcel.setWeight(FormatUtils.formatWithQWFNoBit(order.getPlanWeight()));
                        }
                        paymentExcel.setFlightDate(order.getExpectArrival());
                    }

                    paymentExcel.setOrderCode(order.getOrderCode());
                    paymentExcel.setDepartureStation(order.getDepartureStation());
                    paymentExcel.setArrivalStation(order.getArrivalStation());
                    paymentExcel.setFlightNumber(order.getExpectFlight());

                    CoopVo coopVo = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                    if (coopVo != null) {
                        paymentExcel.setCoopCode(coopVo.getCoop_code());
                        paymentExcel.setCoopName(coopVo.getCoop_name());
                    }
                }
            }
            if ("SE".equals(payment.getBusinessScope()) || "SI".equals(payment.getBusinessScope())) {
                ScOrder order = scOrderService.getById(cssPaymentDetail.getOrderId());
                if (order != null) {
                    String str = "";
                    if (!StringUtils.isEmpty(order.getHblNumber())) {
                        str = "_" + order.getHblNumber();
                    }
                    paymentExcel.setAwbNumber(order.getMblNumber() + str);
                    if ("SE".equals(payment.getBusinessScope())) {
                        paymentExcel.setFlightDate(order.getExpectDeparture());
                    } else {
                        paymentExcel.setFlightDate(order.getExpectArrival());
                    }
                    paymentExcel.setOrderCode(order.getOrderCode());
                    if (StrUtil.isNotBlank(order.getDepartureStation())) {
                        Map<String, String> portName = baseMapper.getPortName(order.getDepartureStation());
                        if (portName != null) {
                            paymentExcel.setDepartureStation(portName.get("port_name_en"));
                        }
                    }
                    if (StrUtil.isNotBlank(order.getArrivalStation())) {
                        Map<String, String> portName = baseMapper.getPortName(order.getArrivalStation());
                        if (portName != null) {
                            paymentExcel.setArrivalStation(portName.get("port_name_en"));
                        }
                    }
                    paymentExcel.setFlightNumber(order.getShipVoyageNumber());

                    paymentExcel.setPieces(order.getPlanPieces());
                    if (order.getPlanVolume() != null) {
                        paymentExcel.setVolume(FormatUtils.formatWithQWFNoBit(order.getPlanVolume()));
                    }
                    if (order.getPlanChargeWeight() != null) {
                        paymentExcel.setChargeWeight(FormatUtils.formatWithQWFNoBit(order.getPlanChargeWeight()));
                    }
                    if (order.getPlanWeight() != null) {
                        paymentExcel.setWeight(FormatUtils.formatWithQWFNoBit(order.getPlanWeight()));
                    }
                    CoopVo coopVo = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                    if (coopVo != null) {
                        paymentExcel.setCoopCode(coopVo.getCoop_code());
                        paymentExcel.setCoopName(coopVo.getCoop_name());
                    }
                }
            }

            if (payment.getBusinessScope().startsWith("T")) {
                TcOrder order = tcOrderService.getById(cssPaymentDetail.getOrderId());
                if (order != null) {
                    String str = "";
                    if (!StringUtils.isEmpty(order.getRwbNumber())) {
                        str = order.getRwbNumber();
                    }
                    paymentExcel.setAwbNumber(str);
                    if ("TE".equals(payment.getBusinessScope())) {
                        paymentExcel.setFlightDate(order.getExpectDeparture());
                    } else {
                        paymentExcel.setFlightDate(order.getExpectArrival());
                    }
                    paymentExcel.setOrderCode(order.getOrderCode());
                    if (StrUtil.isNotBlank(order.getDepartureStation())) {
                        paymentExcel.setDepartureStation(order.getDepartureStation());
                    }
                    if (StrUtil.isNotBlank(order.getArrivalStation())) {
                        paymentExcel.setArrivalStation(order.getArrivalStation());
                    }
                    paymentExcel.setFlightNumber(order.getProductType());

                    paymentExcel.setPieces(order.getPlanPieces());
                    if (order.getPlanVolume() != null) {
                        paymentExcel.setVolume(FormatUtils.formatWithQWFNoBit(order.getPlanVolume()));
                    }
                    if (order.getPlanChargeWeight() != null) {
                        paymentExcel.setChargeWeight(FormatUtils.formatWithQWFNoBit(order.getPlanChargeWeight()));
                    }
                    if (order.getPlanWeight() != null) {
                        paymentExcel.setWeight(FormatUtils.formatWithQWFNoBit(order.getPlanWeight()));
                    }
                    CoopVo coopVo = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                    if (coopVo != null) {
                        paymentExcel.setCoopCode(coopVo.getCoop_code());
                        paymentExcel.setCoopName(coopVo.getCoop_name());
                    }
                }
            }
            if (payment.getBusinessScope().startsWith("L")) {
                LcOrder order = lcOrderService.getById(cssPaymentDetail.getOrderId());
                if (order != null) {
                    String str = "";
                    if (!StringUtils.isEmpty(order.getCustomerNumber())) {
                        str = order.getCustomerNumber();
                    }
                    paymentExcel.setAwbNumber(str);
                    if (order.getDrivingTime() != null) {
                        paymentExcel.setFlightDate(order.getDrivingTime().toLocalDate());
                    }
                    paymentExcel.setOrderCode(order.getOrderCode());
                    if (StrUtil.isNotBlank(order.getDepartureStation())) {
                        paymentExcel.setDepartureStation(order.getDepartureStation());
                    }
                    if (StrUtil.isNotBlank(order.getArrivalStation())) {
                        paymentExcel.setArrivalStation(order.getArrivalStation());
                    }
                    paymentExcel.setFlightNumber(order.getShippingMethod());

                    if (order.getConfirmPieces() != null) {
                        paymentExcel.setPieces(order.getConfirmPieces());
                    } else {
                        paymentExcel.setPieces(order.getPlanPieces());
                    }
                    if (order.getConfirmVolume() != null) {
                        paymentExcel.setVolume(FormatUtils.formatWithQWFNoBit(order.getConfirmVolume()));
                    } else {
                        if (order.getPlanVolume() != null) {
                            paymentExcel.setVolume(FormatUtils.formatWithQWFNoBit(order.getPlanVolume()));
                        }
                    }

                    if (order.getConfirmChargeWeight() != null) {
                        paymentExcel.setChargeWeight(FormatUtils.formatWithQWFNoBit(order.getConfirmChargeWeight()));
                    } else {
                        if (order.getPlanChargeWeight() != null) {
                            paymentExcel.setChargeWeight(FormatUtils.formatWithQWFNoBit(order.getPlanChargeWeight()));
                        }
                    }

                    if (order.getConfirmWeight() != null) {
                        paymentExcel.setWeight(FormatUtils.formatWithQWFNoBit(order.getConfirmWeight()));
                    } else {
                        if (order.getPlanWeight() != null) {
                            paymentExcel.setWeight(FormatUtils.formatWithQWFNoBit(order.getPlanWeight()));
                        }
                    }

                    CoopVo coopVo = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                    if (coopVo != null) {
                        paymentExcel.setCoopCode(coopVo.getCoop_code());
                        paymentExcel.setCoopName(coopVo.getCoop_name());
                    }
                }
            }
            if (payment.getBusinessScope().equals("IO")) {
                IoOrder order = ioOrderService.getById(cssPaymentDetail.getOrderId());
                if (order != null) {
                    String str = "";
                    if (!StringUtils.isEmpty(order.getCustomerNumber())) {
                        str = order.getCustomerNumber();
                    }
                    paymentExcel.setAwbNumber(str);
                    if (order.getBusinessDate() != null) {
                        paymentExcel.setFlightDate(order.getBusinessDate());
                    }
                    paymentExcel.setOrderCode(order.getOrderCode());
                    if (StrUtil.isNotBlank(order.getDepartureStation())) {
                        paymentExcel.setDepartureStation(order.getDepartureStation());
                    }
                    if (StrUtil.isNotBlank(order.getArrivalStation())) {
                        paymentExcel.setArrivalStation(order.getArrivalStation());
                    }
                    paymentExcel.setFlightNumber(order.getBusinessMethod());
                    paymentExcel.setPieces(order.getPlanPieces());
                    if (order.getPlanVolume() != null) {
                        paymentExcel.setVolume(FormatUtils.formatWithQWFNoBit(order.getPlanVolume()));
                    }

                    if (order.getPlanChargeWeight() != null) {
                        paymentExcel.setChargeWeight(FormatUtils.formatWithQWFNoBit(order.getPlanChargeWeight()));
                    }

                    if (order.getPlanWeight() != null) {
                        paymentExcel.setWeight(FormatUtils.formatWithQWFNoBit(order.getPlanWeight()));
                    }

                    CoopVo coopVo = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                    if (coopVo != null) {
                        paymentExcel.setCoopCode(coopVo.getCoop_code());
                        paymentExcel.setCoopName(coopVo.getCoop_name());
                    }
                }
            }
            //对账状态
            CssPayment cp = baseMapper.selectById(cssPaymentDetail.getPaymentId());
            if (cp.getWriteoffComplete() == null) {
                LambdaQueryWrapper<CssCostInvoice> cssCostInvoiceWrapper = Wrappers.<CssCostInvoice>lambdaQuery();
                cssCostInvoiceWrapper.eq(CssCostInvoice::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoice::getPaymentId, cp.getPaymentId());
                CssCostInvoice cssCostInvoice = cssCostInvoiceMapper.selectOne(cssCostInvoiceWrapper);
                if (cssCostInvoice == null) {
                    paymentExcel.setPaymentStatus("已对账");
                } else if (cssCostInvoice.getInvoiceStatus() == -1) {
                    paymentExcel.setPaymentStatus("待收票");
                } else if (cssCostInvoice.getInvoiceStatus() == 1) {
                    paymentExcel.setPaymentStatus("收票完毕");
                } else if (cssCostInvoice.getInvoiceStatus() == 0) {
                    paymentExcel.setPaymentStatus("部分收票");
                }
            } else if (cp.getWriteoffComplete() == 1) {
                paymentExcel.setPaymentStatus("核销完毕");
            } else if (cp.getWriteoffComplete() == 0) {
                paymentExcel.setPaymentStatus("部分核销");
            }
            //主单号为空取订单号
            if (StringUtils.isEmpty(paymentExcel.getAwbNumber())) {
                paymentExcel.setAwbNumber(paymentExcel.getOrderCode());
            }
            paymentExcel.setCurrency(cssPaymentDetail.getCurrency());
            paymentExcel.setCustomerCode(customer.getCoop_code());
            paymentExcel.setCustomerName(payment.getCustomerName());
            paymentExcel.setPaymentDate(payment.getPaymentDate());
            paymentExcel.setPaymentNum(payment.getPaymentNum());
            return paymentExcel;
        }).collect(Collectors.toList());
        ExportExcel<PaymentExcel> ex = new ExportExcel<PaymentExcel>();
        String[] headers = null;
        if ("AE".equals(payment.getBusinessScope())) {
            headers = new String[]{"对账单编号", "对账日期", "对账单状态 ", "提单/订单号", "订单号", "始发港", "目的港", "航班号", "开航日期", "件数", "毛重", "体积", "计重", "客户代码", "客户名称", "供应商代码", "供应商", "币种", "对账单金额", "已核销金额", "未核销金额"};
        } else if ("AI".equals(payment.getBusinessScope())) {
            headers = new String[]{"对账单编号", "对账日期", "对账单状态 ", "提单/订单号", "订单号", "始发港", "目的港", "航班号", "到港日期", "件数", "毛重", "体积", "计重", "客户代码", "客户名称", "供应商代码", "供应商", "币种", "对账单金额", "已核销金额", "未核销金额"};
        } else if ("SE".equals(payment.getBusinessScope())) {
            headers = new String[]{"对账单编号", "对账日期", "对账单状态 ", "提单/订单号", "订单号", "始发港", "目的港", "船次号", "开航日期", "件数", "毛重", "体积", "计费吨", "客户代码", "客户名称", "供应商代码", "供应商", "币种", "对账单金额", "已核销金额", "未核销金额"};
        } else if ("SI".equals(payment.getBusinessScope())) {
            headers = new String[]{"对账单编号", "对账日期", "对账单状态 ", "提单/订单号", "订单号", "始发港", "目的港", "船次号", "到港日期", "件数", "毛重", "体积", "计费吨", "客户代码", "客户名称", "供应商代码", "供应商", "币种", "对账单金额", "已核销金额", "未核销金额"};
        } else if ("TE".equals(payment.getBusinessScope())) {
            headers = new String[]{"对账单编号", "对账日期", "对账单状态 ", "运单号", "订单号", "始发地", "目的地", "产品类型", "发车日期", "件数", "毛重", "体积", "计费吨", "客户代码", "客户名称", "供应商代码", "供应商", "币种", "对账单金额", "已核销金额", "未核销金额"};
        } else if ("TI".equals(payment.getBusinessScope())) {
            headers = new String[]{"对账单编号", "对账日期", "对账单状态 ", "运单号", "订单号", "始发地", "目的地", "产品类型", "到达日期", "件数", "毛重", "体积", "计费吨", "客户代码", "客户名称", "供应商代码", "供应商", "币种", "对账单金额", "已核销金额", "未核销金额"};
        } else if (payment.getBusinessScope().startsWith("L")) {
            headers = new String[]{"对账单编号", "对账日期", "对账单状态 ", "客户单号", "订单号", "始发地", "目的地", "运输方式", "用车日期", "件数", "毛重", "体积", "计重", "客户代码", "客户名称", "供应商代码", "供应商", "币种", "对账单金额", "已核销金额", "未核销金额"};
        } else if (payment.getBusinessScope().equals("IO")) {
            headers = new String[]{"对账单编号", "对账日期", "对账单状态 ", "客户单号", "订单号", "始发地", "目的地", "业务分类", "业务日期", "件数", "毛重", "体积", "计重", "客户代码", "客户名称", "供应商代码", "供应商", "币种", "对账单金额", "已核销金额", "未核销金额"};
        }
        ex.exportExcel(LoginUtils.getResponse(), "导出EXCEL", headers, list, "Export");
    }

    private String formatWith2AndQFW(BigDecimal number) {
        String numberStr = new DecimalFormat("###,###.00").format(number.setScale(2, BigDecimal.ROUND_HALF_UP));
        if (numberStr.split("\\.")[0].equals("")) {
            numberStr = "0." + numberStr.split("\\.")[1];
        } else if (numberStr.split("\\.")[0].equals("-")) {
            numberStr = "-0." + numberStr.split("\\.")[1];
        }
        return numberStr;
    }

    private List<AfCost> getAutomatchCostListForAF(AfCost afCost) {
        LambdaQueryWrapper<AfCost> wrapper = Wrappers.<AfCost>lambdaQuery();
        //关于订单的查询约束
        LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
        orderWrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfOrder::getBusinessScope, afCost.getBusinessScope());
        boolean flag = false;
        if (afCost.getFlightDateStart() != null) {
            flag = true;
            if (afCost.getBusinessScope().equals("AE")) {
                orderWrapper.ge(AfOrder::getExpectDeparture, afCost.getFlightDateStart());
            } else if (afCost.getBusinessScope().equals("AI")) {
                orderWrapper.ge(AfOrder::getExpectArrival, afCost.getFlightDateStart());
            }
        }
        if (afCost.getFlightDateEnd() != null) {
            flag = true;
            if (afCost.getBusinessScope().equals("AE")) {
                orderWrapper.le(AfOrder::getExpectDeparture, afCost.getFlightDateEnd());
            } else if (afCost.getBusinessScope().equals("AI")) {
                orderWrapper.le(AfOrder::getExpectArrival, afCost.getFlightDateEnd());
            }
        }
        if (StrUtil.isNotBlank(afCost.getAwbOrOrderNumbers())) {
            flag = true;
            if (afCost.getAwbOrOrderNumbers().contains(",")) {
                orderWrapper.and(i -> i.in(AfOrder::getAwbNumber, afCost.getAwbOrOrderNumbers().split(",")).or(j -> j.in(AfOrder::getOrderCode, afCost.getAwbOrOrderNumbers().split(","))));
            } else {
                orderWrapper.and(i -> i.like(AfOrder::getAwbNumber, "%" + afCost.getAwbOrOrderNumbers() + "%").or(j -> j.like(AfOrder::getOrderCode, "%" + afCost.getAwbOrOrderNumbers() + "%")));
            }
        }
        if (flag) {
            List<Integer> orderIds = afOrderService.list(orderWrapper).stream().map(AfOrder::getOrderId).collect(Collectors.toList());
            if (orderIds.size() == 0) {
                return new ArrayList<AfCost>();
            }
            wrapper.in(AfCost::getOrderId, orderIds);
        }

        wrapper.eq(AfCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfCost::getBusinessScope, afCost.getBusinessScope()).eq(AfCost::getCustomerId, afCost.getCustomerId()).eq(AfCost::getCostCurrency, afCost.getCostCurrency());
        if (StrUtil.isNotBlank(afCost.getNoCostIds())) {
            wrapper.notIn(AfCost::getCostId, afCost.getNoCostIds().split(","));
        }
        if (StrUtil.isNotBlank(afCost.getDeleteCostIds())) {
            wrapper.last(" and (cost_amount <> cost_amount_payment or cost_amount_payment is null or cost_id in (" + afCost.getDeleteCostIds() + "))");
        } else {
            wrapper.last(" and (cost_amount <> cost_amount_payment or cost_amount_payment is null)");
        }
        if (StrUtil.isNotBlank(afCost.getServiceIds())) {
            wrapper.in(AfCost::getServiceId, afCost.getServiceIds().split(","));
        }
        List<AfCost> costList = afCostService.list(wrapper);
        //封装页面显示金额
        costList.stream().forEach(cost -> {
            //在对账单编辑页面中，进入新增服务后对上个页面删除的对账明细（一定是编辑的对账明细，不是新增）的已对账金额虚拟处理显示
            if (StrUtil.isNotBlank(afCost.getDeleteCostIds())) {
                if (Arrays.asList(afCost.getDeleteCostIds().split(",")).stream().filter(costId -> cost.getCostId().equals(Integer.parseInt(costId))).collect(Collectors.toList()).size() > 0) {
                    LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailLambdaQueryWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                    cssPaymentDetailLambdaQueryWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, cost.getCostId()).eq(CssPaymentDetail::getPaymentId, afCost.getPaymentId());
                    CssPaymentDetail one = cssPaymentDetailService.getOne(cssPaymentDetailLambdaQueryWrapper);
                    if (one != null && one.getAmountPayment() != null) {
                        cost.setCostAmountPayment(cost.getCostAmountPayment().subtract(one.getAmountPayment()));
                    }
                }
            }


            if (cost.getCostAmount() != null) {
                cost.setCostAmountStr(formatWith2AndQFW(cost.getCostAmount()) + " (" + cost.getCostCurrency() + ")");
                if (cost.getCostAmountPayment() != null) {
                    cost.setCostAmountPaymentStr(formatWith2AndQFW(cost.getCostAmountPayment()) + " (" + cost.getCostCurrency() + ")");
                    cost.setCostAmountNoPayment(cost.getCostAmount().subtract(cost.getCostAmountPayment()));
                } else {
                    cost.setCostAmountPaymentStr("0.00 (" + cost.getCostCurrency() + ")");
                    cost.setCostAmountNoPayment(cost.getCostAmount());
                }
                cost.setCostAmountNoPaymentStr(formatWith2AndQFW(cost.getCostAmountNoPayment()) + " (" + cost.getCostCurrency() + ")");
            }
            AfOrder afOrder = afOrderService.getById(cost.getOrderId());
            if (afOrder != null) {
                //开航日期：AE 出口：取 离港日期 ；AI 进口：取 到港日期
                if ("AE".equals(afCost.getBusinessScope())) {
                    cost.setOrderCode(StrUtil.isBlank(afOrder.getAwbNumber()) ? afOrder.getOrderCode() : afOrder.getAwbNumber());
                    cost.setFlightDate(afOrder.getExpectDeparture());
                }
                if ("AI".equals(afCost.getBusinessScope())) {
                    if (StrUtil.isNotBlank(afOrder.getAwbNumber()) && StrUtil.isNotBlank(afOrder.getHawbNumber())) {
                        cost.setOrderCode(afOrder.getAwbNumber() + "_" + afOrder.getHawbNumber());
                    } else if (StrUtil.isNotBlank(afOrder.getHawbNumber())) {
                        cost.setOrderCode(afOrder.getHawbNumber());
                    } else if (StrUtil.isNotBlank(afOrder.getAwbNumber())) {
                        cost.setOrderCode(afOrder.getAwbNumber());
                    } else {
                        cost.setOrderCode(afOrder.getOrderCode());
                    }
                    cost.setFlightDate(afOrder.getExpectArrival());
                }

                //客户单号
                cost.setCustomerNumber(afOrder.getCustomerNumber());
            }
        });
        HashMap<String, BigDecimal> automatchSum = new HashMap<>();
        ArrayList<AfCost> automatchResult = new ArrayList<>();
        automatchSum.put("automatchSum", BigDecimal.ZERO);
        try {
            costList = costList.stream().sorted((e1, e2) -> {
                if (e1.getFlightDate().compareTo(e2.getFlightDate()) == 0) {
                    return e1.getCostAmountNoPayment().compareTo(e2.getCostAmountNoPayment());
                }
                return e1.getFlightDate().compareTo(e2.getFlightDate());
            }).collect(Collectors.toList());
            costList.forEach(cost -> {
                if (automatchSum.get("automatchSum").compareTo(afCost.getAutomatchAmount()) == -1) {
                    if (automatchSum.get("automatchSum").add(cost.getCostAmountNoPayment()).compareTo(afCost.getAutomatchAmount()) == -1) {
                        automatchSum.put("automatchSum", automatchSum.get("automatchSum").add(cost.getCostAmountNoPayment()));
                        cost.setCostCurrAmountPayment(cost.getCostAmountNoPayment().toString());
//                        automatchResult.add(cost);
                    } else if (automatchSum.get("automatchSum").add(cost.getCostAmountNoPayment()).compareTo(afCost.getAutomatchAmount()) == 0) {
                        cost.setCostCurrAmountPayment(cost.getCostAmountNoPayment().toString());
//                        automatchResult.add(cost);
                        throw new RuntimeException("end");
                    } else {
                        cost.setCostCurrAmountPayment(afCost.getAutomatchAmount().subtract(automatchSum.get("automatchSum")).toString());
//                        automatchResult.add(cost);
                        throw new RuntimeException("end");
                    }
                }
            });
        } catch (Exception e) {
            if (!e.getMessage().equals("end")) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return costList;
    }


    private List<AfCost> getAutomatchCostListForLC(AfCost afCost) {
        LambdaQueryWrapper<LcCost> wrapper = Wrappers.<LcCost>lambdaQuery();
        //关于订单的查询约束
        LambdaQueryWrapper<LcOrder> orderWrapper = Wrappers.<LcOrder>lambdaQuery();
        orderWrapper.eq(LcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(LcOrder::getBusinessScope, afCost.getBusinessScope());
        boolean flag = false;
        if (afCost.getFlightDateStart() != null) {
            flag = true;
            orderWrapper.ge(LcOrder::getDrivingTime, afCost.getFlightDateStart());
        }
        if (afCost.getFlightDateEnd() != null) {
            flag = true;
            orderWrapper.le(LcOrder::getDrivingTime, afCost.getFlightDateEnd());
        }
        if (StrUtil.isNotBlank(afCost.getAwbOrOrderNumbers())) {
            flag = true;
            if (afCost.getAwbOrOrderNumbers().contains(",")) {
                orderWrapper.and(i -> i.in(LcOrder::getCustomerNumber, afCost.getAwbOrOrderNumbers().split(",")).or(j -> j.in(LcOrder::getOrderCode, afCost.getAwbOrOrderNumbers().split(","))));
            } else {
                orderWrapper.and(i -> i.like(LcOrder::getCustomerNumber, "%" + afCost.getAwbOrOrderNumbers() + "%").or(j -> j.like(LcOrder::getOrderCode, "%" + afCost.getAwbOrOrderNumbers() + "%")));
            }
        }
        if (flag) {
            List<Integer> orderIds = lcOrderService.list(orderWrapper).stream().map(LcOrder::getOrderId).collect(Collectors.toList());
            if (orderIds.size() == 0) {
                return new ArrayList<AfCost>();
            }
            wrapper.in(LcCost::getOrderId, orderIds);
        }

        wrapper.eq(LcCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(LcCost::getBusinessScope, afCost.getBusinessScope()).eq(LcCost::getCustomerId, afCost.getCustomerId()).eq(LcCost::getCostCurrency, afCost.getCostCurrency());
        if (StrUtil.isNotBlank(afCost.getNoCostIds())) {
            wrapper.notIn(LcCost::getCostId, afCost.getNoCostIds().split(","));
        }
        if (StrUtil.isNotBlank(afCost.getDeleteCostIds())) {
            wrapper.last(" and (cost_amount <> cost_amount_payment or cost_amount_payment is null or cost_id in (" + afCost.getDeleteCostIds() + "))");
        } else {
            wrapper.last(" and (cost_amount <> cost_amount_payment or cost_amount_payment is null)");
        }
        if (StrUtil.isNotBlank(afCost.getServiceIds())) {
            wrapper.in(LcCost::getServiceId, afCost.getServiceIds().split(","));
        }
        List<LcCost> costList = lcCostService.list(wrapper);
        //封装页面显示金额
        costList.stream().forEach(cost -> {
            //在对账单编辑页面中，进入新增服务后对上个页面删除的对账明细（一定是编辑的对账明细，不是新增）的已对账金额虚拟处理显示
            if (StrUtil.isNotBlank(afCost.getDeleteCostIds())) {
                if (Arrays.asList(afCost.getDeleteCostIds().split(",")).stream().filter(costId -> cost.getCostId().equals(Integer.parseInt(costId))).collect(Collectors.toList()).size() > 0) {
                    LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailLambdaQueryWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                    cssPaymentDetailLambdaQueryWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, cost.getCostId()).eq(CssPaymentDetail::getPaymentId, afCost.getPaymentId());
                    CssPaymentDetail one = cssPaymentDetailService.getOne(cssPaymentDetailLambdaQueryWrapper);
                    if (one != null && one.getAmountPayment() != null) {
                        cost.setCostAmountPayment(cost.getCostAmountPayment().subtract(one.getAmountPayment()));
                    }
                }
            }


            if (cost.getCostAmount() != null) {
                cost.setCostAmountStr(formatWith2AndQFW(cost.getCostAmount()) + " (" + cost.getCostCurrency() + ")");
                if (cost.getCostAmountPayment() != null) {
                    cost.setCostAmountPaymentStr(formatWith2AndQFW(cost.getCostAmountPayment()) + " (" + cost.getCostCurrency() + ")");
                    cost.setCostAmountNoPayment(cost.getCostAmount().subtract(cost.getCostAmountPayment()));
                } else {
                    cost.setCostAmountPaymentStr("0.00 (" + cost.getCostCurrency() + ")");
                    cost.setCostAmountNoPayment(cost.getCostAmount());
                }
                cost.setCostAmountNoPaymentStr(formatWith2AndQFW(cost.getCostAmountNoPayment()) + " (" + cost.getCostCurrency() + ")");
            }
            LcOrder lcOrder = lcOrderService.getById(cost.getOrderId());
            if (lcOrder != null) {
                cost.setOrderCode(StrUtil.isBlank(lcOrder.getCustomerNumber()) ? lcOrder.getOrderCode() : lcOrder.getCustomerNumber());
                if (lcOrder.getDrivingTime() != null) {
                    cost.setFlightDate(lcOrder.getDrivingTime().toLocalDate());
                }
                //客户单号
                cost.setCustomerNumber(lcOrder.getCustomerNumber());
            }
        });
        HashMap<String, BigDecimal> automatchSum = new HashMap<>();
//        ArrayList<AfCost> automatchResult = new ArrayList<>();
        automatchSum.put("automatchSum", BigDecimal.ZERO);
        try {
            costList = costList.stream().sorted((e1, e2) -> {
                if (e1.getFlightDate().compareTo(e2.getFlightDate()) == 0) {
                    return e1.getCostAmountNoPayment().compareTo(e2.getCostAmountNoPayment());
                }
                return e1.getFlightDate().compareTo(e2.getFlightDate());
            }).collect(Collectors.toList());
            costList.forEach(cost -> {
                if (automatchSum.get("automatchSum").compareTo(afCost.getAutomatchAmount()) == -1) {
                    if (automatchSum.get("automatchSum").add(cost.getCostAmountNoPayment()).compareTo(afCost.getAutomatchAmount()) == -1) {
                        automatchSum.put("automatchSum", automatchSum.get("automatchSum").add(cost.getCostAmountNoPayment()));
                        cost.setCostCurrAmountPayment(cost.getCostAmountNoPayment().toString());
//                        automatchResult.add(cost);
                    } else if (automatchSum.get("automatchSum").add(cost.getCostAmountNoPayment()).compareTo(afCost.getAutomatchAmount()) == 0) {
                        cost.setCostCurrAmountPayment(cost.getCostAmountNoPayment().toString());
//                        automatchResult.add(cost);
                        throw new RuntimeException("end");
                    } else {
                        cost.setCostCurrAmountPayment(afCost.getAutomatchAmount().subtract(automatchSum.get("automatchSum")).toString());
//                        automatchResult.add(cost);
                        throw new RuntimeException("end");
                    }
                }
            });
        } catch (Exception e) {
            if (!e.getMessage().equals("end")) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return costList.stream().map(scCost -> {
            AfCost cost = new AfCost();
            BeanUtils.copyProperties(scCost, cost);
            return cost;
        }).collect(Collectors.toList());
    }

    private List<AfCost> getAutomatchCostListForIO(AfCost afCost) {
        LambdaQueryWrapper<IoCost> wrapper = Wrappers.<IoCost>lambdaQuery();
        //关于订单的查询约束
        LambdaQueryWrapper<IoOrder> orderWrapper = Wrappers.<IoOrder>lambdaQuery();
        orderWrapper.eq(IoOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(IoOrder::getBusinessScope, afCost.getBusinessScope());
        boolean flag = false;
        if (afCost.getFlightDateStart() != null) {
            flag = true;
            orderWrapper.ge(IoOrder::getBusinessDate, afCost.getFlightDateStart());
        }
        if (afCost.getFlightDateEnd() != null) {
            flag = true;
            orderWrapper.le(IoOrder::getBusinessDate, afCost.getFlightDateEnd());
        }
        if (StrUtil.isNotBlank(afCost.getAwbOrOrderNumbers())) {
            flag = true;
            if (afCost.getAwbOrOrderNumbers().contains(",")) {
                orderWrapper.and(i -> i.in(IoOrder::getCustomerNumber, afCost.getAwbOrOrderNumbers().split(",")).or(j -> j.in(IoOrder::getOrderCode, afCost.getAwbOrOrderNumbers().split(","))));
            } else {
                orderWrapper.and(i -> i.like(IoOrder::getCustomerNumber, "%" + afCost.getAwbOrOrderNumbers() + "%").or(j -> j.like(IoOrder::getOrderCode, "%" + afCost.getAwbOrOrderNumbers() + "%")));
            }
        }
        if (flag) {
            List<Integer> orderIds = ioOrderService.list(orderWrapper).stream().map(IoOrder::getOrderId).collect(Collectors.toList());
            if (orderIds.size() == 0) {
                return new ArrayList<AfCost>();
            }
            wrapper.in(IoCost::getOrderId, orderIds);
        }

        wrapper.eq(IoCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(IoCost::getBusinessScope, afCost.getBusinessScope()).eq(IoCost::getCustomerId, afCost.getCustomerId()).eq(IoCost::getCostCurrency, afCost.getCostCurrency());
        if (StrUtil.isNotBlank(afCost.getNoCostIds())) {
            wrapper.notIn(IoCost::getCostId, afCost.getNoCostIds().split(","));
        }
        if (StrUtil.isNotBlank(afCost.getDeleteCostIds())) {
            wrapper.last(" and (cost_amount <> cost_amount_payment or cost_amount_payment is null or cost_id in (" + afCost.getDeleteCostIds() + "))");
        } else {
            wrapper.last(" and (cost_amount <> cost_amount_payment or cost_amount_payment is null)");
        }
        if (StrUtil.isNotBlank(afCost.getServiceIds())) {
            wrapper.in(IoCost::getServiceId, afCost.getServiceIds().split(","));
        }
        List<IoCost> costList = ioCostService.list(wrapper);
        //封装页面显示金额
        costList.stream().forEach(cost -> {
            //在对账单编辑页面中，进入新增服务后对上个页面删除的对账明细（一定是编辑的对账明细，不是新增）的已对账金额虚拟处理显示
            if (StrUtil.isNotBlank(afCost.getDeleteCostIds())) {
                if (Arrays.asList(afCost.getDeleteCostIds().split(",")).stream().filter(costId -> cost.getCostId().equals(Integer.parseInt(costId))).collect(Collectors.toList()).size() > 0) {
                    LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailLambdaQueryWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                    cssPaymentDetailLambdaQueryWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, cost.getCostId()).eq(CssPaymentDetail::getPaymentId, afCost.getPaymentId());
                    CssPaymentDetail one = cssPaymentDetailService.getOne(cssPaymentDetailLambdaQueryWrapper);
                    if (one != null && one.getAmountPayment() != null) {
                        cost.setCostAmountPayment(cost.getCostAmountPayment().subtract(one.getAmountPayment()));
                    }
                }
            }


            if (cost.getCostAmount() != null) {
                cost.setCostAmountStr(formatWith2AndQFW(cost.getCostAmount()) + " (" + cost.getCostCurrency() + ")");
                if (cost.getCostAmountPayment() != null) {
                    cost.setCostAmountPaymentStr(formatWith2AndQFW(cost.getCostAmountPayment()) + " (" + cost.getCostCurrency() + ")");
                    cost.setCostAmountNoPayment(cost.getCostAmount().subtract(cost.getCostAmountPayment()));
                } else {
                    cost.setCostAmountPaymentStr("0.00 (" + cost.getCostCurrency() + ")");
                    cost.setCostAmountNoPayment(cost.getCostAmount());
                }
                cost.setCostAmountNoPaymentStr(formatWith2AndQFW(cost.getCostAmountNoPayment()) + " (" + cost.getCostCurrency() + ")");
            }
            IoOrder ioOrder = ioOrderService.getById(cost.getOrderId());
            if (ioOrder != null) {
                cost.setOrderCode(StrUtil.isBlank(ioOrder.getCustomerNumber()) ? ioOrder.getOrderCode() : ioOrder.getCustomerNumber());
                if (ioOrder.getBusinessDate() != null) {
                    cost.setFlightDate(ioOrder.getBusinessDate());
                }
                //客户单号
                cost.setCustomerNumber(ioOrder.getCustomerNumber());
            }
        });
        HashMap<String, BigDecimal> automatchSum = new HashMap<>();
        automatchSum.put("automatchSum", BigDecimal.ZERO);
        try {
            costList = costList.stream().sorted((e1, e2) -> {
                if (e1.getFlightDate().compareTo(e2.getFlightDate()) == 0) {
                    return e1.getCostAmountNoPayment().compareTo(e2.getCostAmountNoPayment());
                }
                return e1.getFlightDate().compareTo(e2.getFlightDate());
            }).collect(Collectors.toList());
            costList.forEach(cost -> {
                if (automatchSum.get("automatchSum").compareTo(afCost.getAutomatchAmount()) == -1) {
                    if (automatchSum.get("automatchSum").add(cost.getCostAmountNoPayment()).compareTo(afCost.getAutomatchAmount()) == -1) {
                        automatchSum.put("automatchSum", automatchSum.get("automatchSum").add(cost.getCostAmountNoPayment()));
                        cost.setCostCurrAmountPayment(cost.getCostAmountNoPayment().toString());
                    } else if (automatchSum.get("automatchSum").add(cost.getCostAmountNoPayment()).compareTo(afCost.getAutomatchAmount()) == 0) {
                        cost.setCostCurrAmountPayment(cost.getCostAmountNoPayment().toString());
                        throw new RuntimeException("end");
                    } else {
                        cost.setCostCurrAmountPayment(afCost.getAutomatchAmount().subtract(automatchSum.get("automatchSum")).toString());
                        throw new RuntimeException("end");
                    }
                }
            });
        } catch (Exception e) {
            if (!e.getMessage().equals("end")) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return costList.stream().map(scCost -> {
            AfCost cost = new AfCost();
            BeanUtils.copyProperties(scCost, cost);
            return cost;
        }).collect(Collectors.toList());
    }

    private List<AfCost> getAutomatchCostListForTC(AfCost afCost) {
        LambdaQueryWrapper<TcCost> wrapper = Wrappers.<TcCost>lambdaQuery();
        //关于订单的查询约束
        LambdaQueryWrapper<TcOrder> orderWrapper = Wrappers.<TcOrder>lambdaQuery();
        orderWrapper.eq(TcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TcOrder::getBusinessScope, afCost.getBusinessScope());
        boolean flag = false;
        if (afCost.getFlightDateStart() != null) {
            flag = true;
            if (afCost.getBusinessScope().equals("TE")) {
                orderWrapper.ge(TcOrder::getExpectDeparture, afCost.getFlightDateStart());
            } else if (afCost.getBusinessScope().equals("TI")) {
                orderWrapper.ge(TcOrder::getExpectArrival, afCost.getFlightDateStart());
            }
        }
        if (afCost.getFlightDateEnd() != null) {
            flag = true;
            if (afCost.getBusinessScope().equals("TE")) {
                orderWrapper.le(TcOrder::getExpectDeparture, afCost.getFlightDateEnd());
            } else if (afCost.getBusinessScope().equals("TI")) {
                orderWrapper.le(TcOrder::getExpectArrival, afCost.getFlightDateEnd());
            }
        }
        if (StrUtil.isNotBlank(afCost.getAwbOrOrderNumbers())) {
            flag = true;
            if (afCost.getAwbOrOrderNumbers().contains(",")) {
                orderWrapper.and(i -> i.in(TcOrder::getRwbNumber, afCost.getAwbOrOrderNumbers().split(",")).or(j -> j.in(TcOrder::getOrderCode, afCost.getAwbOrOrderNumbers().split(","))));
            } else {
                orderWrapper.and(i -> i.like(TcOrder::getRwbNumber, "%" + afCost.getAwbOrOrderNumbers() + "%").or(j -> j.like(TcOrder::getOrderCode, "%" + afCost.getAwbOrOrderNumbers() + "%")));
            }
        }
        if (flag) {
            List<Integer> orderIds = tcOrderService.list(orderWrapper).stream().map(TcOrder::getOrderId).collect(Collectors.toList());
            if (orderIds.size() == 0) {
                return new ArrayList<AfCost>();
            }
            wrapper.in(TcCost::getOrderId, orderIds);
        }

        wrapper.eq(TcCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TcCost::getBusinessScope, afCost.getBusinessScope()).eq(TcCost::getCustomerId, afCost.getCustomerId()).eq(TcCost::getCostCurrency, afCost.getCostCurrency());
        if (StrUtil.isNotBlank(afCost.getNoCostIds())) {
            wrapper.notIn(TcCost::getCostId, afCost.getNoCostIds().split(","));
        }
        if (StrUtil.isNotBlank(afCost.getDeleteCostIds())) {
            wrapper.last(" and (cost_amount <> cost_amount_payment or cost_amount_payment is null or cost_id in (" + afCost.getDeleteCostIds() + "))");
        } else {
            wrapper.last(" and (cost_amount <> cost_amount_payment or cost_amount_payment is null)");
        }
        if (StrUtil.isNotBlank(afCost.getServiceIds())) {
            wrapper.in(TcCost::getServiceId, afCost.getServiceIds().split(","));
        }
        List<TcCost> costList = tcCostService.list(wrapper);
        //封装页面显示金额
        costList.stream().forEach(cost -> {
            //在对账单编辑页面中，进入新增服务后对上个页面删除的对账明细（一定是编辑的对账明细，不是新增）的已对账金额虚拟处理显示
            if (StrUtil.isNotBlank(afCost.getDeleteCostIds())) {
                if (Arrays.asList(afCost.getDeleteCostIds().split(",")).stream().filter(costId -> cost.getCostId().equals(Integer.parseInt(costId))).collect(Collectors.toList()).size() > 0) {
                    LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailLambdaQueryWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                    cssPaymentDetailLambdaQueryWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, cost.getCostId()).eq(CssPaymentDetail::getPaymentId, afCost.getPaymentId());
                    CssPaymentDetail one = cssPaymentDetailService.getOne(cssPaymentDetailLambdaQueryWrapper);
                    if (one != null && one.getAmountPayment() != null) {
                        cost.setCostAmountPayment(cost.getCostAmountPayment().subtract(one.getAmountPayment()));
                    }
                }
            }


            if (cost.getCostAmount() != null) {
                cost.setCostAmountStr(formatWith2AndQFW(cost.getCostAmount()) + " (" + cost.getCostCurrency() + ")");
                if (cost.getCostAmountPayment() != null) {
                    cost.setCostAmountPaymentStr(formatWith2AndQFW(cost.getCostAmountPayment()) + " (" + cost.getCostCurrency() + ")");
                    cost.setCostAmountNoPayment(cost.getCostAmount().subtract(cost.getCostAmountPayment()));
                } else {
                    cost.setCostAmountPaymentStr("0.00 (" + cost.getCostCurrency() + ")");
                    cost.setCostAmountNoPayment(cost.getCostAmount());
                }
                cost.setCostAmountNoPaymentStr(formatWith2AndQFW(cost.getCostAmountNoPayment()) + " (" + cost.getCostCurrency() + ")");
            }
            TcOrder tcOrder = tcOrderService.getById(cost.getOrderId());
            if (tcOrder != null) {
                cost.setOrderCode(StrUtil.isBlank(tcOrder.getRwbNumber()) ? tcOrder.getOrderCode() : tcOrder.getRwbNumber());
                if ("TE".equals(afCost.getBusinessScope())) {
                    cost.setFlightDate(tcOrder.getExpectDeparture());
                } else {
                    cost.setFlightDate(tcOrder.getExpectArrival());
                }
                //客户单号
                cost.setCustomerNumber(tcOrder.getCustomerNumber());
            }
        });
        HashMap<String, BigDecimal> automatchSum = new HashMap<>();
//        ArrayList<AfCost> automatchResult = new ArrayList<>();
        automatchSum.put("automatchSum", BigDecimal.ZERO);
        try {
            costList = costList.stream().sorted((e1, e2) -> {
                if (e1.getFlightDate().compareTo(e2.getFlightDate()) == 0) {
                    return e1.getCostAmountNoPayment().compareTo(e2.getCostAmountNoPayment());
                }
                return e1.getFlightDate().compareTo(e2.getFlightDate());
            }).collect(Collectors.toList());
            costList.forEach(cost -> {
                if (automatchSum.get("automatchSum").compareTo(afCost.getAutomatchAmount()) == -1) {
                    if (automatchSum.get("automatchSum").add(cost.getCostAmountNoPayment()).compareTo(afCost.getAutomatchAmount()) == -1) {
                        automatchSum.put("automatchSum", automatchSum.get("automatchSum").add(cost.getCostAmountNoPayment()));
                        cost.setCostCurrAmountPayment(cost.getCostAmountNoPayment().toString());
//                        automatchResult.add(cost);
                    } else if (automatchSum.get("automatchSum").add(cost.getCostAmountNoPayment()).compareTo(afCost.getAutomatchAmount()) == 0) {
                        cost.setCostCurrAmountPayment(cost.getCostAmountNoPayment().toString());
//                        automatchResult.add(cost);
                        throw new RuntimeException("end");
                    } else {
                        cost.setCostCurrAmountPayment(afCost.getAutomatchAmount().subtract(automatchSum.get("automatchSum")).toString());
//                        automatchResult.add(cost);
                        throw new RuntimeException("end");
                    }
                }
            });
        } catch (Exception e) {
            if (!e.getMessage().equals("end")) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return costList.stream().map(scCost -> {
            AfCost cost = new AfCost();
            BeanUtils.copyProperties(scCost, cost);
            return cost;
        }).collect(Collectors.toList());
    }

    private List<AfCost> getAutomatchCostListForSC(AfCost afCost) {
        LambdaQueryWrapper<ScCost> wrapper = Wrappers.<ScCost>lambdaQuery();
        //关于订单的查询约束
        LambdaQueryWrapper<ScOrder> orderWrapper = Wrappers.<ScOrder>lambdaQuery();
        orderWrapper.eq(ScOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(ScOrder::getBusinessScope, afCost.getBusinessScope());
        boolean flag = false;
        if (afCost.getFlightDateStart() != null) {
            flag = true;
            if (afCost.getBusinessScope().equals("SE")) {
                orderWrapper.ge(ScOrder::getExpectDeparture, afCost.getFlightDateStart());
            } else if (afCost.getBusinessScope().equals("SI")) {
                orderWrapper.ge(ScOrder::getExpectArrival, afCost.getFlightDateStart());
            }
        }
        if (afCost.getFlightDateEnd() != null) {
            flag = true;
            if (afCost.getBusinessScope().equals("SE")) {
                orderWrapper.le(ScOrder::getExpectDeparture, afCost.getFlightDateEnd());
            } else if (afCost.getBusinessScope().equals("SI")) {
                orderWrapper.le(ScOrder::getExpectArrival, afCost.getFlightDateEnd());
            }
        }
        if (StrUtil.isNotBlank(afCost.getAwbOrOrderNumbers())) {
            flag = true;
            if (afCost.getAwbOrOrderNumbers().contains(",")) {
                orderWrapper.and(i -> i.in(ScOrder::getMblNumber, afCost.getAwbOrOrderNumbers().split(",")).or(j -> j.in(ScOrder::getOrderCode, afCost.getAwbOrOrderNumbers().split(","))));
            } else {
                orderWrapper.and(i -> i.like(ScOrder::getMblNumber, "%" + afCost.getAwbOrOrderNumbers() + "%").or(j -> j.like(ScOrder::getOrderCode, "%" + afCost.getAwbOrOrderNumbers() + "%")));
            }
        }
        if (flag) {
            List<Integer> orderIds = scOrderService.list(orderWrapper).stream().map(ScOrder::getOrderId).collect(Collectors.toList());
            if (orderIds.size() == 0) {
                return new ArrayList<AfCost>();
            }
            wrapper.in(ScCost::getOrderId, orderIds);
        }

        wrapper.eq(ScCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(ScCost::getBusinessScope, afCost.getBusinessScope()).eq(ScCost::getCustomerId, afCost.getCustomerId()).eq(ScCost::getCostCurrency, afCost.getCostCurrency());
        if (StrUtil.isNotBlank(afCost.getNoCostIds())) {
            wrapper.notIn(ScCost::getCostId, afCost.getNoCostIds().split(","));
        }
        if (StrUtil.isNotBlank(afCost.getDeleteCostIds())) {
            wrapper.last(" and (cost_amount <> cost_amount_payment or cost_amount_payment is null or cost_id in (" + afCost.getDeleteCostIds() + "))");
        } else {
            wrapper.last(" and (cost_amount <> cost_amount_payment or cost_amount_payment is null)");
        }
        if (StrUtil.isNotBlank(afCost.getServiceIds())) {
            wrapper.in(ScCost::getServiceId, afCost.getServiceIds().split(","));
        }
        List<ScCost> costList = scCostService.list(wrapper);
        //封装页面显示金额
        costList.stream().forEach(cost -> {
            //在对账单编辑页面中，进入新增服务后对上个页面删除的对账明细（一定是编辑的对账明细，不是新增）的已对账金额虚拟处理显示
            if (StrUtil.isNotBlank(afCost.getDeleteCostIds())) {
                if (Arrays.asList(afCost.getDeleteCostIds().split(",")).stream().filter(costId -> cost.getCostId().equals(Integer.parseInt(costId))).collect(Collectors.toList()).size() > 0) {
                    LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailLambdaQueryWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                    cssPaymentDetailLambdaQueryWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, cost.getCostId()).eq(CssPaymentDetail::getPaymentId, afCost.getPaymentId());
                    CssPaymentDetail one = cssPaymentDetailService.getOne(cssPaymentDetailLambdaQueryWrapper);
                    if (one != null && one.getAmountPayment() != null) {
                        cost.setCostAmountPayment(cost.getCostAmountPayment().subtract(one.getAmountPayment()));
                    }
                }
            }


            if (cost.getCostAmount() != null) {
                cost.setCostAmountStr(formatWith2AndQFW(cost.getCostAmount()) + " (" + cost.getCostCurrency() + ")");
                if (cost.getCostAmountPayment() != null) {
                    cost.setCostAmountPaymentStr(formatWith2AndQFW(cost.getCostAmountPayment()) + " (" + cost.getCostCurrency() + ")");
                    cost.setCostAmountNoPayment(cost.getCostAmount().subtract(cost.getCostAmountPayment()));
                } else {
                    cost.setCostAmountPaymentStr("0.00 (" + cost.getCostCurrency() + ")");
                    cost.setCostAmountNoPayment(cost.getCostAmount());
                }
                cost.setCostAmountNoPaymentStr(formatWith2AndQFW(cost.getCostAmountNoPayment()) + " (" + cost.getCostCurrency() + ")");
            }
            ScOrder scOrder = scOrderService.getById(cost.getOrderId());
            if (scOrder != null) {
                //开航日期：SE 出口：取 离港日期 ；SI 进口：取 到港日期
                if ("SE".equals(afCost.getBusinessScope())) {
                    cost.setOrderCode(StrUtil.isBlank(scOrder.getMblNumber()) ? scOrder.getOrderCode() : scOrder.getMblNumber());
                    cost.setFlightDate(scOrder.getExpectDeparture());
                }
                if ("SI".equals(afCost.getBusinessScope())) {
                    if (StrUtil.isNotBlank(scOrder.getMblNumber()) && StrUtil.isNotBlank(scOrder.getHblNumber())) {
                        cost.setOrderCode(scOrder.getMblNumber() + "_" + scOrder.getHblNumber());
                    } else if (StrUtil.isNotBlank(scOrder.getHblNumber())) {
                        cost.setOrderCode(scOrder.getHblNumber());
                    } else if (StrUtil.isNotBlank(scOrder.getMblNumber())) {
                        cost.setOrderCode(scOrder.getMblNumber());
                    } else {
                        cost.setOrderCode(scOrder.getOrderCode());
                    }
                    cost.setFlightDate(scOrder.getExpectArrival());
                }
                //客户单号
                cost.setCustomerNumber(scOrder.getCustomerNumber());
            }
        });
        HashMap<String, BigDecimal> automatchSum = new HashMap<>();
//        ArrayList<AfCost> automatchResult = new ArrayList<>();
        automatchSum.put("automatchSum", BigDecimal.ZERO);
        try {
            costList = costList.stream().sorted((e1, e2) -> {
                if (e1.getFlightDate().compareTo(e2.getFlightDate()) == 0) {
                    return e1.getCostAmountNoPayment().compareTo(e2.getCostAmountNoPayment());
                }
                return e1.getFlightDate().compareTo(e2.getFlightDate());
            }).collect(Collectors.toList());
            costList.forEach(cost -> {
                if (automatchSum.get("automatchSum").compareTo(afCost.getAutomatchAmount()) == -1) {
                    if (automatchSum.get("automatchSum").add(cost.getCostAmountNoPayment()).compareTo(afCost.getAutomatchAmount()) == -1) {
                        automatchSum.put("automatchSum", automatchSum.get("automatchSum").add(cost.getCostAmountNoPayment()));
                        cost.setCostCurrAmountPayment(cost.getCostAmountNoPayment().toString());
//                        automatchResult.add(cost);
                    } else if (automatchSum.get("automatchSum").add(cost.getCostAmountNoPayment()).compareTo(afCost.getAutomatchAmount()) == 0) {
                        cost.setCostCurrAmountPayment(cost.getCostAmountNoPayment().toString());
//                        automatchResult.add(cost);
                        throw new RuntimeException("end");
                    } else {
                        cost.setCostCurrAmountPayment(afCost.getAutomatchAmount().subtract(automatchSum.get("automatchSum")).toString());
//                        automatchResult.add(cost);
                        throw new RuntimeException("end");
                    }
                }
            });
        } catch (Exception e) {
            if (!e.getMessage().equals("end")) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return costList.stream().map(scCost -> {
            AfCost cost = new AfCost();
            BeanUtils.copyProperties(scCost, cost);
            return cost;
        }).collect(Collectors.toList());
    }

    @Override
    public List<AfCost> getAutomatchCostList(AfCost afCost) {
        if (StrUtil.isBlank(afCost.getBusinessScope())) {
            throw new RuntimeException("业务范畴不能为空");
        }
        if (afCost.getBusinessScope().equals("AE") || afCost.getBusinessScope().equals("AI")) {
            return getAutomatchCostListForAF(afCost);
        } else if (afCost.getBusinessScope().equals("SE") || afCost.getBusinessScope().equals("SI")) {
            return getAutomatchCostListForSC(afCost);
        } else if (afCost.getBusinessScope().startsWith("T")) {
            return getAutomatchCostListForTC(afCost);
        } else if (afCost.getBusinessScope().startsWith("L")) {
            return getAutomatchCostListForLC(afCost);
        } else if (afCost.getBusinessScope().equals("IO")) {
            return getAutomatchCostListForIO(afCost);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<CssCostWriteoffDetail> getPaymentDetailByPaymentId(Integer paymentId) {
        LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
        cssPaymentDetailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getPaymentId, paymentId);
        return cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(cssPaymentDetail -> {
            CssCostWriteoffDetail cssCostWriteoffDetail = new CssCostWriteoffDetail();
            cssCostWriteoffDetail.setAmountPayment(cssPaymentDetail.getAmountPayment());
            cssCostWriteoffDetail.setAmountPaymentStr(formatWith2AndQFW(cssPaymentDetail.getAmountPayment()) + " (" + cssPaymentDetail.getCurrency() + ")");
            cssCostWriteoffDetail.setAmountPaymentWriteoff(cssPaymentDetail.getAmountPaymentWriteoff());
            cssCostWriteoffDetail.setAmountPaymentWriteoffStr(cssPaymentDetail.getAmountPaymentWriteoff() == null ? "0.00" + " (" + cssPaymentDetail.getCurrency() + ")" : formatWith2AndQFW(cssPaymentDetail.getAmountPaymentWriteoff()) + " (" + cssPaymentDetail.getCurrency() + ")");
            if (cssPaymentDetail.getAmountPaymentWriteoff() == null) {
                cssCostWriteoffDetail.setAmountPaymentNoWriteoff(cssPaymentDetail.getAmountPayment());
            } else {
                cssCostWriteoffDetail.setAmountPaymentNoWriteoff(cssPaymentDetail.getAmountPayment().subtract(cssPaymentDetail.getAmountPaymentWriteoff()));
            }
            cssCostWriteoffDetail.setAmountPaymentNoWriteoffStr(formatWith2AndQFW(cssCostWriteoffDetail.getAmountPaymentNoWriteoff()) + " (" + cssPaymentDetail.getCurrency() + ")");
            cssCostWriteoffDetail.setAmountWriteoff(BigDecimal.ZERO.setScale(2));
            cssCostWriteoffDetail.setCostId(cssPaymentDetail.getCostId());
            cssCostWriteoffDetail.setCurrency(cssPaymentDetail.getCurrency());
            cssCostWriteoffDetail.setPaymentId(cssPaymentDetail.getPaymentId());
            cssCostWriteoffDetail.setOrderId(cssPaymentDetail.getOrderId());

            CssPayment cssPayment = getById(paymentId);

            if ("AE".equals(cssPayment.getBusinessScope()) || "AI".equals(cssPayment.getBusinessScope())) {
                AfCost cost = afCostService.getById(cssCostWriteoffDetail.getCostId());
                cssCostWriteoffDetail.setServiceName(cost.getServiceName());
                AfOrder afOrder = afOrderService.getById(cssPaymentDetail.getOrderId());
                if (afOrder != null) {
                    String awbOrOrderNumber = "";
                    if (StrUtil.isNotBlank(afOrder.getAwbNumber())) {
                        if ("AE".equals(cssPayment.getBusinessScope())) {
                            awbOrOrderNumber = afOrder.getAwbNumber();
                        }
                        if ("AI".equals(cssPayment.getBusinessScope())) {
                            awbOrOrderNumber = StrUtil.isNotBlank(afOrder.getHawbNumber()) ? (afOrder.getAwbNumber() + "_" + afOrder.getHawbNumber()) : afOrder.getAwbNumber();
                        }
                    } else if (StrUtil.isNotBlank(afOrder.getHawbNumber()) && "AI".equals(cssPayment.getBusinessScope())) {
                        awbOrOrderNumber = afOrder.getHawbNumber();
                    } else {
                        awbOrOrderNumber = afOrder.getOrderCode();
                    }
                    if ("AE".equals(cssPayment.getBusinessScope())) {
                        cssCostWriteoffDetail.setFlightDate(afOrder.getExpectDeparture());
                    }
                    if ("AI".equals(cssPayment.getBusinessScope())) {
                        cssCostWriteoffDetail.setFlightDate(afOrder.getExpectArrival());
                    }
                    cssCostWriteoffDetail.setAwbOrOrderNumber(awbOrOrderNumber);
                }
            }

            if ("SE".equals(cssPayment.getBusinessScope()) || "SI".equals(cssPayment.getBusinessScope())) {
                ScCost cost = scCostService.getById(cssCostWriteoffDetail.getCostId());
                cssCostWriteoffDetail.setServiceName(cost.getServiceName());
                ScOrder order = scOrderService.getById(cssPaymentDetail.getOrderId());
                if (order != null) {
                    String awbOrOrderNumber = "";
                    if (StrUtil.isNotBlank(order.getMblNumber())) {
                        if ("SE".equals(cssPayment.getBusinessScope())) {
                            awbOrOrderNumber = order.getMblNumber();
                        }
                        if ("SI".equals(cssPayment.getBusinessScope())) {
                            awbOrOrderNumber = StrUtil.isNotBlank(order.getHblNumber()) ? (order.getMblNumber() + "_" + order.getHblNumber()) : order.getMblNumber();
                        }
                    } else if (StrUtil.isNotBlank(order.getHblNumber()) && "SI".equals(cssPayment.getBusinessScope())) {
                        awbOrOrderNumber = order.getHblNumber();
                    } else {
                        awbOrOrderNumber = order.getOrderCode();
                    }
                    cssCostWriteoffDetail.setAwbOrOrderNumber(awbOrOrderNumber);
                    if ("SE".equals(cssPayment.getBusinessScope())) {
                        cssCostWriteoffDetail.setFlightDate(order.getExpectDeparture());
                    }
                    if ("SI".equals(cssPayment.getBusinessScope())) {
                        cssCostWriteoffDetail.setFlightDate(order.getExpectArrival());
                    }
                }
            }

            if (cssPayment.getBusinessScope().startsWith("T")) {
                TcCost cost = tcCostService.getById(cssCostWriteoffDetail.getCostId());
                cssCostWriteoffDetail.setServiceName(cost.getServiceName());
                TcOrder order = tcOrderService.getById(cssPaymentDetail.getOrderId());
                if (order != null) {
                    String awbOrOrderNumber = "";
                    if (StrUtil.isNotBlank(order.getRwbNumber())) {
                        awbOrOrderNumber = order.getRwbNumber();
                    } else {
                        awbOrOrderNumber = order.getOrderCode();
                    }
                    cssCostWriteoffDetail.setAwbOrOrderNumber(awbOrOrderNumber);
                    if ("TE".equals(cssPayment.getBusinessScope())) {
                        cssCostWriteoffDetail.setFlightDate(order.getExpectDeparture());
                    }
                    if ("TI".equals(cssPayment.getBusinessScope())) {
                        cssCostWriteoffDetail.setFlightDate(order.getExpectArrival());
                    }
                }
            }

            if (cssPayment.getBusinessScope().startsWith("L")) {
                LcCost cost = lcCostService.getById(cssCostWriteoffDetail.getCostId());
                cssCostWriteoffDetail.setServiceName(cost.getServiceName());
                LcOrder order = lcOrderService.getById(cssPaymentDetail.getOrderId());
                if (order != null) {
                    String awbOrOrderNumber = "";
                    if (StrUtil.isNotBlank(order.getCustomerNumber())) {
                        awbOrOrderNumber = order.getCustomerNumber();
                    } else {
                        awbOrOrderNumber = order.getOrderCode();
                    }
                    cssCostWriteoffDetail.setAwbOrOrderNumber(awbOrOrderNumber);
                    if (order.getDrivingTime() != null) {
                        cssCostWriteoffDetail.setFlightDate(order.getDrivingTime().toLocalDate());
                    }

                }
            }
            if (cssPayment.getBusinessScope().equals("IO")) {
                IoCost cost = ioCostService.getById(cssCostWriteoffDetail.getCostId());
                cssCostWriteoffDetail.setServiceName(cost.getServiceName());
                IoOrder order = ioOrderService.getById(cssPaymentDetail.getOrderId());
                if (order != null) {
                    String awbOrOrderNumber = "";
                    if (StrUtil.isNotBlank(order.getCustomerNumber())) {
                        awbOrOrderNumber = order.getCustomerNumber();
                    } else {
                        awbOrOrderNumber = order.getOrderCode();
                    }
                    cssCostWriteoffDetail.setAwbOrOrderNumber(awbOrOrderNumber);
                    if (order.getBusinessDate() != null) {
                        cssCostWriteoffDetail.setFlightDate(order.getBusinessDate());
                    }

                }
            }
            return cssCostWriteoffDetail;
        }).sorted((e1, e2) -> {
            if (e1.getFlightDate().compareTo(e2.getFlightDate()) == 0) {
                return e1.getAmountPaymentNoWriteoff().compareTo(e2.getAmountPaymentNoWriteoff());
            }
            return e1.getFlightDate().compareTo(e2.getFlightDate());
        }).collect(Collectors.toList());
    }

    @Override
    public List<CssPayment> exportPaymentExcel(CssPayment cssPayment) {
        LambdaQueryWrapper<CssPayment> wrapper = Wrappers.<CssPayment>lambdaQuery();
        if (StrUtil.isNotBlank(cssPayment.getAwbNumberOrOrderCode())) {
            List<Integer> orderIds = null;
            if (cssPayment.getBusinessScope().startsWith("A")) {
                LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
                orderWrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(AfOrder::getAwbNumber, "%" + cssPayment.getAwbNumberOrOrderCode() + "%").or(j -> j.like(AfOrder::getOrderCode, "%" + cssPayment.getAwbNumberOrOrderCode() + "%")));
                List<AfOrder> orderList = afOrderService.list(orderWrapper);
                if (orderList.size() == 0) {
                    return null;
                }
                orderIds = orderList.stream().map(AfOrder::getOrderId).collect(Collectors.toList());
            }
            if (cssPayment.getBusinessScope().startsWith("S")) {
                LambdaQueryWrapper<ScOrder> orderWrapper = Wrappers.<ScOrder>lambdaQuery();
                orderWrapper.eq(ScOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(ScOrder::getMblNumber, "%" + cssPayment.getAwbNumberOrOrderCode() + "%").or(j -> j.like(ScOrder::getOrderCode, "%" + cssPayment.getAwbNumberOrOrderCode() + "%")));
                List<ScOrder> orderList = scOrderService.list(orderWrapper);
                if (orderList.size() == 0) {
                    return null;
                }
                orderIds = orderList.stream().map(ScOrder::getOrderId).collect(Collectors.toList());
            }
            if (cssPayment.getBusinessScope().startsWith("T")) {
                LambdaQueryWrapper<TcOrder> orderWrapper = Wrappers.<TcOrder>lambdaQuery();
                orderWrapper.eq(TcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(TcOrder::getRwbNumber, "%" + cssPayment.getAwbNumberOrOrderCode() + "%").or(j -> j.like(TcOrder::getOrderCode, "%" + cssPayment.getAwbNumberOrOrderCode() + "%")));
                List<TcOrder> orderList = tcOrderService.list(orderWrapper);
                if (orderList.size() == 0) {
                    return null;
                }
                orderIds = orderList.stream().map(TcOrder::getOrderId).collect(Collectors.toList());
            }
            if (cssPayment.getBusinessScope().startsWith("L")) {
                LambdaQueryWrapper<LcOrder> orderWrapper = Wrappers.<LcOrder>lambdaQuery();
                orderWrapper.eq(LcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(LcOrder::getCustomerNumber, "%" + cssPayment.getAwbNumberOrOrderCode() + "%").or(j -> j.like(LcOrder::getOrderCode, "%" + cssPayment.getAwbNumberOrOrderCode() + "%")));
                List<LcOrder> orderList = lcOrderService.list(orderWrapper);
                if (orderList.size() == 0) {
                    return null;
                }
                orderIds = orderList.stream().map(LcOrder::getOrderId).collect(Collectors.toList());
            }
            if (cssPayment.getBusinessScope().equals("IO")) {
                LambdaQueryWrapper<IoOrder> orderWrapper = Wrappers.<IoOrder>lambdaQuery();
                orderWrapper.eq(IoOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(IoOrder::getCustomerNumber, "%" + cssPayment.getAwbNumberOrOrderCode() + "%").or(j -> j.like(IoOrder::getOrderCode, "%" + cssPayment.getAwbNumberOrOrderCode() + "%")));
                List<IoOrder> orderList = ioOrderService.list(orderWrapper);
                if (orderList.size() == 0) {
                    return null;
                }
                orderIds = orderList.stream().map(IoOrder::getOrderId).collect(Collectors.toList());
            }

            if (orderIds != null) {
                LambdaQueryWrapper<CssPaymentDetail> detailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                detailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).in(CssPaymentDetail::getOrderId, orderIds);
                List<CssPaymentDetail> detailList = cssPaymentDetailService.list(detailWrapper);
                if (detailList.size() == 0) {
                    return null;
                }
                List<Integer> paymentIds = detailList.stream().map(CssPaymentDetail::getPaymentId).distinct().collect(Collectors.toList());
                wrapper.in(CssPayment::getPaymentId, paymentIds);
            }
        }
        if (StrUtil.isNotBlank(cssPayment.getInvoiceNum())) {
            LambdaQueryWrapper<CssCostInvoiceDetail> cssCostInvoiceDetailWrapper = Wrappers.<CssCostInvoiceDetail>lambdaQuery();
            cssCostInvoiceDetailWrapper.eq(CssCostInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId());
            if (StrUtil.isNotBlank(cssPayment.getInvoiceNum())) {
                cssCostInvoiceDetailWrapper.like(CssCostInvoiceDetail::getInvoiceNum, cssPayment.getInvoiceNum());
            }
            List<Integer> paymentIds = cssCostInvoiceDetailService.list(cssCostInvoiceDetailWrapper).stream().map(CssCostInvoiceDetail::getPaymentId).distinct().collect(Collectors.toList());
            if (paymentIds.size() == 0) {
                return null;
            }
            wrapper.in(CssPayment::getPaymentId, paymentIds);
        }
        if (StrUtil.isNotBlank(cssPayment.getInvoiceCreatorName()) || StrUtil.isNotBlank(cssPayment.getInvoiceDateStart()) || StrUtil.isNotBlank(cssPayment.getInvoiceDateEnd())) {
            LambdaQueryWrapper<CssCostInvoice> cssCostInvoiceWrapper = Wrappers.<CssCostInvoice>lambdaQuery();
            cssCostInvoiceWrapper.eq(CssCostInvoice::getOrgId, SecurityUtils.getUser().getOrgId());
            if (StrUtil.isNotBlank(cssPayment.getInvoiceCreatorName())) {
                cssCostInvoiceWrapper.like(CssCostInvoice::getCreatorName, cssPayment.getInvoiceCreatorName());
            }
            if (StrUtil.isNotBlank(cssPayment.getInvoiceDateStart())) {
                cssCostInvoiceWrapper.ge(CssCostInvoice::getCreateTime, cssPayment.getInvoiceDateStart());
            }
            if (StrUtil.isNotBlank(cssPayment.getInvoiceDateEnd())) {
                cssCostInvoiceWrapper.le(CssCostInvoice::getCreateTime, cssPayment.getInvoiceDateEnd());
            }
            List<Integer> paymentIds = cssCostInvoiceMapper.selectList(cssCostInvoiceWrapper).stream().map(CssCostInvoice::getPaymentId).collect(Collectors.toList());
            if (paymentIds.size() == 0) {
                return null;
            }
            wrapper.in(CssPayment::getPaymentId, paymentIds);
        }
        wrapper.eq(CssPayment::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPayment::getBusinessScope, cssPayment.getBusinessScope());
        if (StrUtil.isNotBlank(cssPayment.getCurrency())) {
            wrapper.eq(CssPayment::getCurrency, cssPayment.getCurrency());
        }
        if (StrUtil.isNotBlank(cssPayment.getCustomerName())) {
            wrapper.like(CssPayment::getCustomerName, "%" + cssPayment.getCustomerName() + "%");
        }
        if (StrUtil.isNotBlank(cssPayment.getPaymentNum())) {
            wrapper.like(CssPayment::getPaymentNum, "%" + cssPayment.getPaymentNum() + "%");
        }
        if (cssPayment.getPaymentDateStart() != null) {
            wrapper.ge(CssPayment::getPaymentDate, cssPayment.getPaymentDateStart());
        }
        if (cssPayment.getPaymentDateEnd() != null) {
            wrapper.le(CssPayment::getPaymentDate, cssPayment.getPaymentDateEnd());
        }
        if (StrUtil.isNotBlank(cssPayment.getCreatorName())) {
            wrapper.like(CssPayment::getCreatorName, cssPayment.getCreatorName());
        }
        if (StrUtil.isNotBlank(cssPayment.getWriteoffCompletes())) {
            StringBuffer lastSql = new StringBuffer(" and (");
            if (cssPayment.getWriteoffCompletes().contains("2")) {
                lastSql.append(" payment_id not in (select payment_id from css_cost_invoice where org_id=").append(SecurityUtils.getUser().getOrgId()).append(") or");
            }
            if (cssPayment.getWriteoffCompletes().contains("3")) {
                lastSql.append(" payment_id in (select payment_id from css_cost_invoice where invoice_status=-1 and org_id=").append(SecurityUtils.getUser().getOrgId()).append(") or");
            }
            if (cssPayment.getWriteoffCompletes().contains("4")) {
                lastSql.append(" payment_id in (select payment_id from css_cost_invoice where invoice_status=0 and org_id=").append(SecurityUtils.getUser().getOrgId()).append(") or");
            }
            if (cssPayment.getWriteoffCompletes().contains("5")) {
                lastSql.append(" payment_id in (select payment_id from css_cost_invoice where invoice_status=1 and org_id=").append(SecurityUtils.getUser().getOrgId()).append(") or");
            }
            if (cssPayment.getWriteoffCompletes().contains("0")) {
                lastSql.append(" writeoff_complete=0 or");
            }
            if (cssPayment.getWriteoffCompletes().contains("1")) {
                lastSql.append(" writeoff_complete=1 or");
            }
            wrapper.last(lastSql.substring(0, lastSql.length() - 3) + ") ORDER BY payment_num DESC");
        } else {
            wrapper.orderByDesc(CssPayment::getPaymentNum);
        }
        List<CssPayment> iPage = baseMapper.selectList(wrapper);
        iPage.stream().forEach(item -> {

            //拼接发票号
            LambdaQueryWrapper<CssCostInvoiceDetail> cssCostInvoiceDetailLambdaQueryWrapper = Wrappers.<CssCostInvoiceDetail>lambdaQuery();
            cssCostInvoiceDetailLambdaQueryWrapper.eq(CssCostInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoiceDetail::getPaymentId, item.getPaymentId());
            List<CssCostInvoiceDetail> cssCostInvoiceDetails = cssCostInvoiceDetailService.list(cssCostInvoiceDetailLambdaQueryWrapper);
            StringBuffer invoiceNumBuffer = new StringBuffer();
            cssCostInvoiceDetails.stream().forEach(cssCostInvoiceDetail -> {
                if (invoiceNumBuffer.length() == 0) {
                    invoiceNumBuffer.append(cssCostInvoiceDetail.getInvoiceNum()).append(" (").append(cssCostInvoiceDetail.getInvoiceDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))).append(")");
                } else {
                    invoiceNumBuffer.append("\n").append(cssCostInvoiceDetail.getInvoiceNum()).append(" (").append(cssCostInvoiceDetail.getInvoiceDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))).append(")");
                }
            });
            item.setInvoiceNum(invoiceNumBuffer.toString());
            //取核销单号
            LambdaQueryWrapper<CssCostInvoiceDetailWriteoff> cssCostInvoiceDetailWriteoffLambdaQueryWrapper = Wrappers.<CssCostInvoiceDetailWriteoff>lambdaQuery();
            cssCostInvoiceDetailWriteoffLambdaQueryWrapper.eq(CssCostInvoiceDetailWriteoff::getPaymentId, item.getPaymentId()).eq(CssCostInvoiceDetailWriteoff::getOrgId, SecurityUtils.getUser().getOrgId());
            List<CssCostInvoiceDetailWriteoff> cssCostInvoiceDetailWriteoffs = cssCostInvoiceDetailWriteoffService.list(cssCostInvoiceDetailWriteoffLambdaQueryWrapper);
            StringBuffer writeoffNumBuffer = new StringBuffer();
            cssCostInvoiceDetailWriteoffs.stream().forEach(cssCostInvoiceDetailWriteoff -> {
                if (writeoffNumBuffer.length() == 0) {
                    writeoffNumBuffer.append(cssCostInvoiceDetailWriteoff.getWriteoffNum());
                } else {
                    writeoffNumBuffer.append("\n").append(cssCostInvoiceDetailWriteoff.getWriteoffNum());
                }
            });
            item.setWriteoffNum(writeoffNumBuffer.toString());

            if (item.getAmountPayment() != null) {
                item.setAmountPaymentStr(formatWith2AndQFW(item.getAmountPayment()) + " (" + item.getCurrency() + ")");
                if (item.getAmountPaymentWriteoff() == null) {
                    item.setAmountPaymentWriteoffStr("0.00 (" + item.getCurrency() + ")");
                    item.setAmountPaymentNoWriteoff(item.getAmountPayment());
                } else {
                    item.setAmountPaymentWriteoffStr(formatWith2AndQFW(item.getAmountPaymentWriteoff()) + " (" + item.getCurrency() + ")");
                    item.setAmountPaymentNoWriteoff(item.getAmountPayment().subtract(item.getAmountPaymentWriteoff()));
                }
                item.setAmountPaymentNoWriteoffStr(formatWith2AndQFW(item.getAmountPaymentNoWriteoff()) + " (" + item.getCurrency() + ")");
            }

            LambdaQueryWrapper<CssCostInvoice> cssCostInvoiceWrapper = Wrappers.<CssCostInvoice>lambdaQuery();
            cssCostInvoiceWrapper.eq(CssCostInvoice::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoice::getPaymentId, item.getPaymentId());
            CssCostInvoice cssCostInvoice = cssCostInvoiceMapper.selectOne(cssCostInvoiceWrapper);
            if (cssCostInvoice != null) {
                item.setInvoiceTime(cssCostInvoice.getCreateTime());
                item.setInvoiceCreatorName(cssCostInvoice.getCreatorName());
                item.setInvoiceInqurityRemark(cssCostInvoice.getApplyRemark());
            }
            if (StrUtil.isBlank(item.getInvoiceInqurityRemark())) {
                item.setInvoiceInqurityRemark(item.getInvoiceRemark());
            }
            if (item.getWriteoffComplete() == null) {
                if (cssCostInvoice == null) {
                    item.setPaymentStatus("已对账");
                } else if (cssCostInvoice.getInvoiceStatus() == -1) {
                    item.setPaymentStatus("待收票");
                } else if (cssCostInvoice.getInvoiceStatus() == 1) {
                    item.setPaymentStatus("收票完毕");
                } else if (cssCostInvoice.getInvoiceStatus() == 0) {
                    item.setPaymentStatus("部分收票");
                }
            } else if (item.getWriteoffComplete() == 1) {
                item.setPaymentStatus("核销完毕");
            } else if (item.getWriteoffComplete() == 0) {
                item.setPaymentStatus("部分核销");
            }
        });
        //拼接合计
        if (iPage.size() != 0) {
            HashMap<String, BigDecimal> amountWriteoffMap = new HashMap<>();//未核销金额汇总
            HashMap<String, BigDecimal> amountPaymentWriteoffMap = new HashMap<>();//已核销金额汇总
            HashMap<String, BigDecimal> amountPaymentMap = new HashMap<>();//对账金额汇总
            list(wrapper).stream().forEach(writeoff -> {
                BigDecimal writOff = BigDecimal.ZERO;
                if (writeoff.getAmountPaymentWriteoff() != null) {
                    writOff = writeoff.getAmountPaymentWriteoff();
                }
                //对账
                if (amountPaymentMap.containsKey(writeoff.getCurrency())) {
                    amountPaymentMap.put(writeoff.getCurrency(), writeoff.getAmountPayment().add(amountPaymentMap.get(writeoff.getCurrency())));
                } else {
                    amountPaymentMap.put(writeoff.getCurrency(), writeoff.getAmountPayment());
                }
                //核销
                if (amountPaymentWriteoffMap.containsKey(writeoff.getCurrency())) {
                    amountPaymentWriteoffMap.put(writeoff.getCurrency(), amountPaymentWriteoffMap.get(writeoff.getCurrency()).add(writOff));
                } else {
                    amountPaymentWriteoffMap.put(writeoff.getCurrency(), writOff);
                }
                //未核销
                if (amountWriteoffMap.get(writeoff.getCurrency()) == null) {
                    amountWriteoffMap.put(writeoff.getCurrency(), writeoff.getAmountPayment().subtract(writOff));
                } else {
                    amountWriteoffMap.put(writeoff.getCurrency(), amountWriteoffMap.get(writeoff.getCurrency()).add(writeoff.getAmountPayment().subtract(writOff)));
                }
            });
            StringBuffer amountWriteoffBuffer = new StringBuffer();
            StringBuffer amountPaymentWriteoffBuffer = new StringBuffer();
            StringBuffer amountPaymentBuffer = new StringBuffer();
            for (Map.Entry<String, BigDecimal> entry : amountWriteoffMap.entrySet()) {
                if (amountWriteoffBuffer.length() == 0) {
                    amountWriteoffBuffer.append(formatWith2AndQFW(entry.getValue()) + " (" + entry.getKey() + ")");
                } else {
                    amountWriteoffBuffer.append(String.valueOf((char) 10)).append(formatWith2AndQFW(entry.getValue()) + " (" + entry.getKey() + ")");
                }
            }
            for (Map.Entry<String, BigDecimal> entry : amountPaymentWriteoffMap.entrySet()) {
                if (amountPaymentWriteoffBuffer.length() == 0) {
                    amountPaymentWriteoffBuffer.append(formatWith2AndQFW(entry.getValue()) + " (" + entry.getKey() + ")");
                } else {
                    amountPaymentWriteoffBuffer.append(String.valueOf((char) 10)).append(formatWith2AndQFW(entry.getValue()) + " (" + entry.getKey() + ")");
                }
            }
            for (Map.Entry<String, BigDecimal> entry : amountPaymentMap.entrySet()) {
                if (amountPaymentBuffer.length() == 0) {
                    amountPaymentBuffer.append(formatWith2AndQFW(entry.getValue()) + " (" + entry.getKey() + ")");
                } else {
                    amountPaymentBuffer.append(String.valueOf((char) 10)).append(formatWith2AndQFW(entry.getValue()) + " (" + entry.getKey() + ")");
                }
            }
            CssPayment writeoff = new CssPayment();
            writeoff.setAmountPaymentNoWriteoffStr(amountWriteoffBuffer.toString());
            writeoff.setAmountPaymentStr(amountPaymentBuffer.toString());
            writeoff.setAmountPaymentWriteoffStr(amountPaymentWriteoffBuffer.toString());
            iPage.add(writeoff);
        }
        return iPage;
    }

    @Override
    public void invoiceRemark(CssPayment bean) {
        CssPayment cssPayment = getById(bean.getPaymentId());
        if (cssPayment == null) {
            throw new RuntimeException("该成本账单不存在");
        }
        if (cssPayment.getWriteoffComplete() != null && cssPayment.getWriteoffComplete() == 1) {
            throw new IllegalStateException("您好，该对账单已经完全核销，不能修改发票信息。");
        }
        cssPayment.setInvoiceRemark(bean.getInvoiceRemark());
        cssPayment.setInvoiceDate(bean.getInvoiceDate());
        cssPayment.setInvoiceNum(bean.getInvoiceNum());
        cssPayment.setInvoiceTitle(bean.getInvoiceTitle());
        cssPayment.setEditorName(SecurityUtils.getUser().buildOptName());
        cssPayment.setEditorId(SecurityUtils.getUser().getId());
        cssPayment.setEditTime(LocalDateTime.now());
        cssPayment.setRowUuid(UUID.randomUUID().toString());

        updateById(cssPayment);
    }

    @Override
    public List<PaymentBatchDetail> readExcel(String businessScope, Integer customerId, String currency, String serviceIds, MultipartFile file) {
        List<PaymentBatchDetail> paymentBatchDetails = this.parse(file);
        if (paymentBatchDetails.isEmpty()) {
            throw new RuntimeException("上传文件为空或金额格式有误");
        }
        List<PaymentBatchDetail> lastList = new ArrayList<>();
        LinkedHashMap<Integer, PaymentBatchDetail> map = new LinkedHashMap<>();
        if ("AE".equals(businessScope)) {
            //去重（主单号和订单号有可能是同一个订单，聚合显示主单号）
            paymentBatchDetails.stream().forEach(paymentBatchDetail -> {
                if (StrUtil.isNotBlank(paymentBatchDetail.getCode())) {
                    //查询订单
                    LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
                    orderWrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfOrder::getBusinessScope, businessScope).orderByDesc(AfOrder::getCreateTime);
                    if (paymentBatchDetail.getIsOrderCode()) {
                        orderWrapper.eq(AfOrder::getOrderCode, paymentBatchDetail.getCode());
                    } else {
                        orderWrapper.eq(AfOrder::getAwbNumber, paymentBatchDetail.getCode());
                    }
                    List<AfOrder> orders = afOrderService.list(orderWrapper);
                    if (!orders.isEmpty()) {
                        //设置id信息
                        paymentBatchDetail.setId(orders.get(0).getOrderId());
                        //设置航班日期
                        paymentBatchDetail.setFlightDate(orders.get(0).getExpectDeparture());
                        paymentBatchDetail.setOrderRowUuid(orders.get(0).getRowUuid());

                        if (map.get(orders.get(0).getOrderId()) == null) {
                            map.put(orders.get(0).getOrderId(), paymentBatchDetail);
                        } else {
                            if (paymentBatchDetail.getIsOrderCode()) {
                                map.get(orders.get(0).getOrderId()).setUploadAmount(paymentBatchDetail.getUploadAmount().add(map.get(orders.get(0).getOrderId()).getUploadAmount()));
                            } else {
                                PaymentBatchDetail detail = map.get(orders.get(0).getOrderId());
                                detail.setCode(paymentBatchDetail.getCode());
                                detail.setUploadAmount(detail.getUploadAmount().add(paymentBatchDetail.getUploadAmount()));
                                map.put(orders.get(0).getOrderId(), detail);
                            }
                        }
                    } else {
                        lastList.add(paymentBatchDetail);
                    }
                } else {
                    lastList.add(paymentBatchDetail);
                }
            });
            lastList.addAll(map.values());
            lastList.stream().forEach(paymentBatchDetail -> {
                if (StrUtil.isNotBlank(paymentBatchDetail.getCode())) {
                    LambdaQueryWrapper<AfCost> wrapper = Wrappers.<AfCost>lambdaQuery();
                    wrapper.eq(AfCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfCost::getBusinessScope, businessScope).eq(AfCost::getCostCurrency, currency).eq(AfCost::getCustomerId, customerId);
                    if (StrUtil.isNotBlank(serviceIds)) {
                        wrapper.in(AfCost::getServiceId, serviceIds.split(","));
                    }

                    if (paymentBatchDetail.getId() != null) {
                        wrapper.eq(AfCost::getOrderId, paymentBatchDetail.getId());
                        List<AfCost> list = afCostService.list(wrapper);
                        //设置原成本金额与可对账金额
                        if (list.isEmpty()) {
                            paymentBatchDetail.setAmount(BigDecimal.ZERO);
                            paymentBatchDetail.setNoPaymentAmount(BigDecimal.ZERO);
                        } else {
                            HashMap<String, BigDecimal> sum = new HashMap<>();
                            sum.put("amount", BigDecimal.ZERO);
                            sum.put("noPaymentAmount", BigDecimal.ZERO);
                            list.stream().forEach(cost -> {
                                sum.put("amount", sum.get("amount").add(cost.getCostAmount()));
                                if (cost.getCostAmountPayment() == null) {
                                    sum.put("noPaymentAmount", sum.get("noPaymentAmount").add(cost.getCostAmount()));
                                } else {
                                    sum.put("noPaymentAmount", sum.get("noPaymentAmount").add(cost.getCostAmount().subtract(cost.getCostAmountPayment())));
                                }
                            });
                            paymentBatchDetail.setAmount(sum.get("amount"));
                            paymentBatchDetail.setNoPaymentAmount(sum.get("noPaymentAmount"));
                        }

                    }
                }
                if (paymentBatchDetail.getNoPaymentAmount() != null) {
                    paymentBatchDetail.setErrorAmount(paymentBatchDetail.getUploadAmount().subtract(paymentBatchDetail.getNoPaymentAmount()));
                }

                //拼接页面金额显示
                if (paymentBatchDetail.getAmount() != null) {
                    paymentBatchDetail.setAmountStr(FormatUtils.formatWithQWF(paymentBatchDetail.getAmount(), 2) + "(" + currency + ")");
                }
                if (paymentBatchDetail.getNoPaymentAmount() != null) {
                    paymentBatchDetail.setNoPaymentAmountStr(FormatUtils.formatWithQWF(paymentBatchDetail.getNoPaymentAmount(), 2) + "(" + currency + ")");
                }
                if (paymentBatchDetail.getErrorAmount() != null) {
                    paymentBatchDetail.setErrorAmountStr(FormatUtils.formatWithQWF(paymentBatchDetail.getErrorAmount(), 2) + "(" + currency + ")");
                }
                paymentBatchDetail.setUploadAmountStr(FormatUtils.formatWithQWF(paymentBatchDetail.getUploadAmount(), 2) + "(" + currency + ")");
            });
        } else if (businessScope.startsWith("S")) {
            paymentBatchDetails.stream().forEach(paymentBatchDetail -> {

            });
        } else if (businessScope.startsWith("T")) {
            paymentBatchDetails.stream().forEach(paymentBatchDetail -> {

            });
        } else if (businessScope.startsWith("L")) {
            paymentBatchDetails.stream().forEach(paymentBatchDetail -> {

            });
        } else if (businessScope.startsWith("I")) {
            paymentBatchDetails.stream().forEach(paymentBatchDetail -> {

            });
        }
        return lastList.stream().sorted((o1, o2) -> {
            if (o1.getAmount() == null) {
                return -1;
            }
            if (o2.getAmount() == null) {
                return 1;
            }
            if (o1.getFlightDate() == null) {
                return -1;
            }
            return o1.getFlightDate().compareTo(o2.getFlightDate());
        }).collect(Collectors.toList());
    }

    @Override
    public void exportBatchDetail(PaymentBatchDetail paymentBatchDetail) {
        List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
        String firstHeader = "";
        if ("LC".equals(paymentBatchDetail.getBusinessScope()) || "IO".equals(paymentBatchDetail.getBusinessScope())) {
            firstHeader = "客户单号/订单号";
        } else {
            firstHeader = "提单号/订单号";
        }
        String secondHeader = "";
        if ("AE".equals(paymentBatchDetail.getBusinessScope()) || "SE".equals(paymentBatchDetail.getBusinessScope())) {
            secondHeader = "开航日期";
        } else if ("AI".equals(paymentBatchDetail.getBusinessScope()) || "SI".equals(paymentBatchDetail.getBusinessScope())) {
            secondHeader = "到港日期";
        } else if ("TE".equals(paymentBatchDetail.getBusinessScope())) {
            secondHeader = "发车日期";
        } else if ("TI".equals(paymentBatchDetail.getBusinessScope())) {
            secondHeader = "到达日期";
        } else if ("LC".equals(paymentBatchDetail.getBusinessScope())) {
            secondHeader = "用车日期";
        } else if ("IO".equals(paymentBatchDetail.getBusinessScope())) {
            secondHeader = "业务日期";
        }
        String[] headers = {firstHeader, secondHeader, "原成本金额", "可对账金额", "供应商(Excel)金额", "误差金额"};

        //遍历结果集 区相应字段封装 linkedHashMap后存储list发往工具类
        paymentBatchDetail.getList().stream().forEach(detail -> {
            LinkedHashMap<String, String> map = new LinkedHashMap<>();
            map.put("code", detail.getCode());
            map.put("flightDate", detail.getAmount() == null ? "未找到" : (detail.getFlightDate() == null ? "" : detail.getFlightDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
            map.put("amount", detail.getAmount() == null ? "" : FormatUtils.formatWithQWF(detail.getAmount(), 2));
            map.put("noPaymentAmount", detail.getNoPaymentAmount() == null ? "" : FormatUtils.formatWithQWF(detail.getNoPaymentAmount(), 2));
            map.put("uploadAmount", detail.getUploadAmount() == null ? "" : FormatUtils.formatWithQWF(detail.getUploadAmount(), 2));
            map.put("errorAmount", detail.getErrorAmount() == null ? "" : FormatUtils.formatWithQWF(detail.getErrorAmount(), 2));
            listExcel.add(map);
        });

        ExcelExportUtils u = new ExcelExportUtils();
        u.exportExcelLinkListMap(LoginUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");
    }

    @Override
    public void downloadModel() {
        JxlsUtils.responseExcel(JxlsUtils.modelRootPath + "paymentBatch.xlsx");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void savePaymentBatch(PaymentBatchDetail paymentBatchDetail) {
        //一、保存对账单
        CssPayment cssPayment = new CssPayment();
        //生成对账编号
        String paymentNum = getPaymentNum(paymentBatchDetail.getBusinessScope());
        cssPayment.setPaymentNum(paymentNum);
        cssPayment.setRowUuid(UUID.randomUUID().toString());
        cssPayment.setBusinessScope(paymentBatchDetail.getBusinessScope());
        cssPayment.setPaymentDate(paymentBatchDetail.getPaymentDate());
        cssPayment.setCustomerId(paymentBatchDetail.getCustomerId());
        String customerName = remoteCoopService.viewCoop(paymentBatchDetail.getCustomerId().toString()).getData().getCoop_name();
        cssPayment.setCustomerName(customerName);
        cssPayment.setCurrency(paymentBatchDetail.getCurrency());
        if (paymentBatchDetail.getIfAdjust()) {
            cssPayment.setAmountPayment(paymentBatchDetail.getUploadAmountSum());
            //如果调整状态，赋值serviceName
            com.efreight.afbase.entity.Service service = serviceService.getById(paymentBatchDetail.getServiceId());
            if (service != null) {
                paymentBatchDetail.setServiceName(service.getServiceType() + " - " + service.getServiceNameCn());
            }
        } else {
            cssPayment.setAmountPayment(paymentBatchDetail.getNoPaymentAmountSum());
        }
        cssPayment.setFunctionalAmountPayment(cssPayment.getAmountPayment().multiply(paymentBatchDetail.getExchangeRate()).setScale(2, BigDecimal.ROUND_HALF_UP));
        cssPayment.setExchangeRate(paymentBatchDetail.getExchangeRate());
        cssPayment.setPaymentRemark(paymentBatchDetail.getPaymentRemark());
        LocalDateTime now = LocalDateTime.now();
        cssPayment.setCreateTime(now);
        cssPayment.setEditTime(now);
        cssPayment.setCreatorId(SecurityUtils.getUser().getId());
        cssPayment.setEditorId(SecurityUtils.getUser().getId());
        cssPayment.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        cssPayment.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        cssPayment.setOrgId(SecurityUtils.getUser().getOrgId());
        save(cssPayment);


        //二、保存对账明细和更细cost表
        ArrayList<CssPaymentDetail> cssPaymentDetails = new ArrayList<>();

        if (paymentBatchDetail.getBusinessScope().startsWith("A")) {
            //1.查询出需要更新的所有cost记录
            ArrayList<AfCost> afCosts = new ArrayList<>();
            paymentBatchDetail.getList().stream().forEach(detail -> {
                AfOrder afOrder = afOrderService.getById(detail.getId());
                if (afOrder == null) {
                    throw new RuntimeException("未找到相关订单，生成失败");
                }
                if (StrUtil.isNotBlank(afOrder.getRowUuid()) && !afOrder.getRowUuid().equals(detail.getOrderRowUuid())) {
                    throw new RuntimeException("当前页面不是最新数据，请重新上传");
                }
                LambdaQueryWrapper<AfCost> wrapper = Wrappers.<AfCost>lambdaQuery();
                wrapper.eq(AfCost::getBusinessScope, paymentBatchDetail.getBusinessScope()).eq(AfCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfCost::getCustomerId, paymentBatchDetail.getCustomerId()).eq(AfCost::getCostCurrency, paymentBatchDetail.getCurrency()).eq(AfCost::getOrderId, detail.getId());
                if (StrUtil.isNotBlank(paymentBatchDetail.getServiceIds())) {
                    wrapper.in(AfCost::getServiceId, paymentBatchDetail.getServiceIds().split(","));
                }
                wrapper.last(" and (cost_amount != cost_amount_payment or cost_amount_payment is null)");
                afCosts.addAll(afCostService.list(wrapper));


                //2.判断是否调整创建新的成本费用Cost
                if (paymentBatchDetail.getIfAdjust() && detail.getErrorAmount().signum() != 0) {
                    AfCost afCost = new AfCost();
                    afCost.setOrgId(SecurityUtils.getUser().getOrgId());
                    afCost.setBusinessScope(paymentBatchDetail.getBusinessScope());
                    afCost.setOrderId(afOrder.getOrderId());
                    afCost.setOrderUuid(afOrder.getOrderUuid());
                    afCost.setCustomerId(paymentBatchDetail.getCustomerId());
                    afCost.setCustomerName(customerName);
                    afCost.setServiceId(paymentBatchDetail.getServiceId());
                    afCost.setServiceName(paymentBatchDetail.getServiceName());
                    afCost.setCostUnitPrice(detail.getErrorAmount());
                    afCost.setCostQuantity(BigDecimal.ONE);
                    afCost.setCostAmount(detail.getErrorAmount());
                    afCost.setCostFunctionalAmount(afCost.getCostAmount().multiply(paymentBatchDetail.getExchangeRate()).setScale(2, BigDecimal.ROUND_HALF_UP));
                    afCost.setCostAmountNotTax(afCost.getCostFunctionalAmount());
                    afCost.setCostAmountPayment(afCost.getCostAmount());
                    afCost.setCostCurrency(paymentBatchDetail.getCurrency());
                    afCost.setCostExchangeRate(paymentBatchDetail.getExchangeRate());
                    if (paymentBatchDetail.getFinancialDate() != null) {
                        afCost.setFinancialDate(LocalDateTime.of(paymentBatchDetail.getFinancialDate(), LocalTime.of(0, 0, 0)));
                    }
                    afCost.setCreateTime(now);
                    afCost.setEditTime(now);
                    afCost.setCreatorId(SecurityUtils.getUser().getId());
                    afCost.setEditorId(SecurityUtils.getUser().getId());
                    afCost.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    afCost.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    afCost.setRowUuid(UUID.randomUUID().toString());
                    if (StrUtil.isNotBlank(afCost.getServiceName()) && afCost.getServiceName().startsWith("干线")) {
                        afCost.setMainRouting(afCost.getCostFunctionalAmount());
                    }
                    if (StrUtil.isNotBlank(afCost.getServiceName()) && afCost.getServiceName().startsWith("支线")) {
                        afCost.setFeeder(afCost.getCostFunctionalAmount());
                    }
                    if (StrUtil.isNotBlank(afCost.getServiceName()) && afCost.getServiceName().startsWith("操作")) {
                        afCost.setOperation(afCost.getCostFunctionalAmount());
                    }
                    if (StrUtil.isNotBlank(afCost.getServiceName()) && afCost.getServiceName().startsWith("包装")) {
                        afCost.setPackaging(afCost.getCostFunctionalAmount());
                    }
                    if (StrUtil.isNotBlank(afCost.getServiceName()) && afCost.getServiceName().startsWith("仓储")) {
                        afCost.setStorage(afCost.getCostFunctionalAmount());
                    }
                    if (StrUtil.isNotBlank(afCost.getServiceName()) && afCost.getServiceName().startsWith("快递")) {
                        afCost.setPostage(afCost.getCostFunctionalAmount());
                    }
                    if (StrUtil.isNotBlank(afCost.getServiceName()) && afCost.getServiceName().startsWith("关检")) {
                        afCost.setClearance(afCost.getCostFunctionalAmount());
                    }
                    if (StrUtil.isNotBlank(afCost.getServiceName()) && afCost.getServiceName().startsWith("数据")) {
                        afCost.setExchange(afCost.getCostFunctionalAmount());
                    }
                    afCostService.save(afCost);

                    //3.构造对账明细
                    CssPaymentDetail cssPaymentDetail = new CssPaymentDetail();
                    cssPaymentDetail.setOrgId(SecurityUtils.getUser().getOrgId());
                    cssPaymentDetail.setPaymentId(cssPayment.getPaymentId());
                    cssPaymentDetail.setOrderId(afCost.getOrderId());
                    cssPaymentDetail.setCostId(afCost.getCostId());
                    cssPaymentDetail.setCurrency(afCost.getCostCurrency());
                    cssPaymentDetail.setAmountPayment(afCost.getCostAmount());
                    cssPaymentDetails.add(cssPaymentDetail);
                }
            });
            afCosts.stream().forEach(afCost -> {
                //3.构造对账明细
                CssPaymentDetail cssPaymentDetail = new CssPaymentDetail();
                cssPaymentDetail.setOrgId(SecurityUtils.getUser().getOrgId());
                cssPaymentDetail.setPaymentId(cssPayment.getPaymentId());
                cssPaymentDetail.setOrderId(afCost.getOrderId());
                cssPaymentDetail.setCostId(afCost.getCostId());
                cssPaymentDetail.setCurrency(afCost.getCostCurrency());
                cssPaymentDetail.setAmountPayment(afCost.getCostAmount().subtract(afCost.getCostAmountPayment() == null ? BigDecimal.ZERO : afCost.getCostAmountPayment()));
                cssPaymentDetails.add(cssPaymentDetail);

                //4.更新系统现有成本费用cost
                afCost.setCostAmountPayment(afCost.getCostAmount());
                if (!"CNY".equals(afCost.getCostCurrency())) {
                    afCost.setCostExchangeRate(paymentBatchDetail.getExchangeRate());
                    if (afCost.getCostAmount() != null && paymentBatchDetail.getExchangeRate() != null) {
                        BigDecimal decimal = paymentBatchDetail.getExchangeRate().multiply(afCost.getCostAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
                        afCost.setCostFunctionalAmount(decimal);
                        if (StrUtil.isNotBlank(afCost.getServiceName()) && afCost.getServiceName().startsWith("干线")) {
                            afCost.setMainRouting(decimal);
                        }
                        if (StrUtil.isNotBlank(afCost.getServiceName()) && afCost.getServiceName().startsWith("支线")) {
                            afCost.setFeeder(decimal);
                        }
                        if (StrUtil.isNotBlank(afCost.getServiceName()) && afCost.getServiceName().startsWith("操作")) {
                            afCost.setOperation(decimal);
                        }
                        if (StrUtil.isNotBlank(afCost.getServiceName()) && afCost.getServiceName().startsWith("包装")) {
                            afCost.setPackaging(decimal);
                        }
                        if (StrUtil.isNotBlank(afCost.getServiceName()) && afCost.getServiceName().startsWith("仓储")) {
                            afCost.setStorage(decimal);
                        }
                        if (StrUtil.isNotBlank(afCost.getServiceName()) && afCost.getServiceName().startsWith("快递")) {
                            afCost.setPostage(decimal);
                        }
                        if (StrUtil.isNotBlank(afCost.getServiceName()) && afCost.getServiceName().startsWith("关检")) {
                            afCost.setClearance(decimal);
                        }
                        if (StrUtil.isNotBlank(afCost.getServiceName()) && afCost.getServiceName().startsWith("数据")) {
                            afCost.setExchange(decimal);
                        }
                    }
                }
                afCost.setRowUuid(UUID.randomUUID().toString());

            });

            //5.更新cost执行
            if (!afCosts.isEmpty()) {
                afCostService.updateBatchById(afCosts);
            }

            //6.更新订单状态
            cssPaymentDetails.stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId -> {
                AfOrder afOrder = afOrderService.getById(orderId);
                afOrder.setCostStatus(afOrderService.getOrderCostStatusForAF(orderId));
                afOrder.setRowUuid(UUID.randomUUID().toString());
                afOrderService.updateById(afOrder);
            });

        } else if (paymentBatchDetail.getBusinessScope().startsWith("S")) {

        } else if (paymentBatchDetail.getBusinessScope().startsWith("T")) {

        } else if (paymentBatchDetail.getBusinessScope().startsWith("L")) {

        } else if (paymentBatchDetail.getBusinessScope().startsWith("I")) {

        }
        //7.创建成本对账明细执行
        cssPaymentDetailService.saveBatch(cssPaymentDetails);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void writeoff(CssCostInvoiceDetail cssCostInvoiceDetail) {
        //校验
        CssPayment cssPayment = checkIfCanWriteoff(cssCostInvoiceDetail.getPaymentId(), cssCostInvoiceDetail.getRowUuid());

        //保存付款申请
        CssCostInvoice cssCostInvoice = new CssCostInvoice();
        cssCostInvoice.setBusinessScope(cssPayment.getBusinessScope());
        cssCostInvoice.setCustomerId(cssPayment.getCustomerId());
        cssCostInvoice.setCustomerName(cssPayment.getCustomerName());
        cssCostInvoice.setPaymentId(cssPayment.getPaymentId());
        cssCostInvoice.setOrgId(SecurityUtils.getUser().getOrgId());
        cssCostInvoice.setCreateTime(LocalDateTime.now());
        cssCostInvoice.setCreatorId(SecurityUtils.getUser().getId());
        cssCostInvoice.setCreatorName(SecurityUtils.getUser().buildOptName());
        cssCostInvoice.setInvoiceStatus(1);
        cssCostInvoice.setRowUuid(UUID.randomUUID().toString());
        cssCostInvoiceMapper.insert(cssCostInvoice);

        //保存收票（完全收票）
        cssCostInvoiceDetail.setBusinessScope(cssPayment.getBusinessScope());
        cssCostInvoiceDetail.setInvoiceId(cssCostInvoice.getInvoiceId());
        cssCostInvoiceDetail.setPaymentId(cssPayment.getPaymentId());
        cssCostInvoiceDetail.setCustomerId(cssPayment.getCustomerId());
        cssCostInvoiceDetail.setCustomerName(cssPayment.getCustomerName());
        cssCostInvoiceDetail.setWriteoffComplete(1);
        cssCostInvoiceDetail.setCurrency(cssPayment.getCurrency());
        cssCostInvoiceDetail.setAmount(cssPayment.getAmountPayment());
        cssCostInvoiceDetail.setAmountWriteoff(cssPayment.getAmountPayment());
        cssCostInvoiceDetail.setOrgId(SecurityUtils.getUser().getOrgId());
        cssCostInvoiceDetail.setRowUuid(UUID.randomUUID().toString());
        cssCostInvoiceDetail.setCreatorId(SecurityUtils.getUser().getId());
        cssCostInvoiceDetail.setCreateTime(LocalDateTime.now());
        cssCostInvoiceDetail.setCreatorName(SecurityUtils.getUser().buildOptName());
        cssCostInvoiceDetailService.save(cssCostInvoiceDetail);

        //保存核销单
        CssCostInvoiceDetailWriteoff cssCostInvoiceDetailWriteoff = new CssCostInvoiceDetailWriteoff();
        cssCostInvoiceDetailWriteoff.setBusinessScope(cssPayment.getBusinessScope());
        cssCostInvoiceDetailWriteoff.setRowUuid(UUID.randomUUID().toString());
        cssCostInvoiceDetailWriteoff.setInvoiceDetailId(cssCostInvoiceDetail.getInvoiceDetailId());
        cssCostInvoiceDetailWriteoff.setPaymentId(cssPayment.getPaymentId());
        cssCostInvoiceDetailWriteoff.setInvoiceId(cssCostInvoice.getInvoiceId());
        cssCostInvoiceDetailWriteoff.setWriteoffNum(cssCostInvoiceDetailWriteoffService.getWriteoffNum(cssPayment.getBusinessScope()));
        cssCostInvoiceDetailWriteoff.setCustomerId(cssPayment.getCustomerId());
        cssCostInvoiceDetailWriteoff.setCustomerName(cssPayment.getCustomerName());
        cssCostInvoiceDetailWriteoff.setCurrency(cssPayment.getCurrency());
        cssCostInvoiceDetailWriteoff.setAmountWriteoff(cssPayment.getAmountPayment());
        cssCostInvoiceDetailWriteoff.setFinancialAccountCode(cssCostInvoiceDetail.getFinancialAccountCode());
        cssCostInvoiceDetailWriteoff.setFinancialAccountName(cssCostInvoiceDetail.getFinancialAccountName());
        cssCostInvoiceDetailWriteoff.setFinancialAccountType(cssCostInvoiceDetail.getFinancialAccountType());
        cssCostInvoiceDetailWriteoff.setWriteoffDate(cssCostInvoiceDetail.getWriteoffDate());
        cssCostInvoiceDetailWriteoff.setWriteoffRemark(cssCostInvoiceDetail.getWriteoffRemark());
        cssCostInvoiceDetailWriteoff.setOrgId(SecurityUtils.getUser().getOrgId());
        cssCostInvoiceDetailWriteoff.setCreatorId(SecurityUtils.getUser().getId());
        cssCostInvoiceDetailWriteoff.setCreatorName(SecurityUtils.getUser().buildOptName());
        cssCostInvoiceDetailWriteoff.setCreateTime(LocalDateTime.now());
        cssCostInvoiceDetailWriteoffService.save(cssCostInvoiceDetailWriteoff);

        //更新账单
        cssPayment.setAmountPaymentInvoice(cssPayment.getAmountPayment());
        cssPayment.setAmountPaymentWriteoff(cssPayment.getAmountPayment());
        cssPayment.setFunctionalAmountPaymentWriteoff(cssPayment.getFunctionalAmountPayment());
        cssPayment.setWriteoffComplete(1);
        cssPayment.setRowUuid(UUID.randomUUID().toString());
        updateById(cssPayment);

        //更新账单明细
        LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailLambdaQueryWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
        cssPaymentDetailLambdaQueryWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getPaymentId, cssPayment.getPaymentId());
        List<CssPaymentDetail> cssPaymentDetailList = cssPaymentDetailService.list(cssPaymentDetailLambdaQueryWrapper);
        cssPaymentDetailList.stream().forEach(cssPaymentDetail -> {
            cssPaymentDetail.setAmountPaymentWriteoff(cssPaymentDetail.getAmountPayment());
        });
        cssPaymentDetailService.updateBatchById(cssPaymentDetailList);

        //更新cost表
        LambdaQueryWrapper<CssPayment> cssPaymentLambdaQueryWrapper = Wrappers.<CssPayment>lambdaQuery();
        cssPaymentLambdaQueryWrapper.eq(CssPayment::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPayment::getBusinessScope, cssPayment.getBusinessScope());
        List<Integer> paymentIds = list(cssPaymentLambdaQueryWrapper).stream().map(CssPayment::getPaymentId).collect(Collectors.toList());
        HashMap<String, BigDecimal> costWriteoffAmountSum = new HashMap<>();
        if (cssPayment.getBusinessScope().startsWith("A")) {
            cssPaymentDetailList.stream().map(CssPaymentDetail::getCostId).collect(Collectors.toList()).stream().forEach(costId -> {
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
            cssPaymentDetailList.stream().map(CssPaymentDetail::getCostId).collect(Collectors.toList()).stream().forEach(costId -> {
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
            cssPaymentDetailList.stream().map(CssPaymentDetail::getCostId).collect(Collectors.toList()).stream().forEach(costId -> {
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
            cssPaymentDetailList.stream().map(CssPaymentDetail::getCostId).collect(Collectors.toList()).stream().forEach(costId -> {
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
            cssPaymentDetailList.stream().map(CssPaymentDetail::getCostId).collect(Collectors.toList()).stream().forEach(costId -> {
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
        cssPaymentDetailList.stream().map(CssPaymentDetail::getOrderId).distinct().collect(Collectors.toList()).stream().forEach(orderId -> {
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
    public CssPayment checkIfCanWriteoff(Integer paymentId, String rowUuid) {
        CssPayment cssPayment = getById(paymentId);
        if (cssPayment == null) {
            throw new RuntimeException("账单不存在，请刷新页面再试");
        }
        if (!cssPayment.getRowUuid().equals(rowUuid)) {
            throw new RuntimeException("账单已变更，请刷新页面重新操作");
        }
        LambdaQueryWrapper<CssCostInvoice> cssCostInvoiceWrapper = Wrappers.<CssCostInvoice>lambdaQuery();
        cssCostInvoiceWrapper.eq(CssCostInvoice::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoice::getPaymentId, paymentId);
        CssCostInvoice cssCostInvoice = cssCostInvoiceMapper.selectOne(cssCostInvoiceWrapper);
        if (cssCostInvoice != null) {
            throw new RuntimeException("您好，对账单号" + cssPayment.getPaymentNum() + " 已做付款申请或已收票，请在发票页面进行核销。");
        }
        return cssPayment;
    }

    private List<PaymentBatchDetail> parse(MultipartFile file) {
        if (!(file.getOriginalFilename().endsWith(".xls") || file.getOriginalFilename().endsWith(".xlsx"))) {
            throw new RuntimeException("请上传正确格式的文件，仅支持.xls或.xlsx");
        }
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);//默认第一个sheet
            if (sheet == null) {
                return Collections.emptyList();
            }
            HashMap<String, BigDecimal> sumAmount = new LinkedHashMap<>();
            for (int line = 1; line <= sheet.getLastRowNum(); line++) {
                Row row = sheet.getRow(line);
                if (null == row) {
                    continue;
                }
                if (row.getCell(0) == null && row.getCell(1) == null) {
                    continue;
                }
                String code = "";
                BigDecimal amount = BigDecimal.ZERO;
                //第一个字段主单或订单号
                if (row.getCell(0) != null) {
                    if ("NUMERIC".equals(row.getCell(0).getCellType().name())) {
                        code = row.getCell(0).getNumericCellValue() + "";
                        code = code.substring(0, code.length() - 2);
                    } else {
                        code = row.getCell(0).getStringCellValue();
                    }
                }
                //格式化订单号
                if (StrUtil.isNotBlank(code)) {
                    Pattern p = Pattern.compile("\\s*|\t|\r|\n|");
                    Matcher m = p.matcher(code);
                    code = m.replaceAll("").replaceAll("-", "");
                }


                //第二个字段供应商金额
                if (row.getCell(1) != null) {
                    if ("NUMERIC".equals(row.getCell(1).getCellType().name())) {
                        amount = BigDecimal.valueOf(row.getCell(1).getNumericCellValue());
                    } else {
                        return Collections.emptyList();
                    }
                }


                if (sumAmount.get(code) == null) {
                    sumAmount.put(code, amount);
                } else {
                    sumAmount.put(code, sumAmount.get(code).add(amount));
                }
            }
            List<PaymentBatchDetail> details = new ArrayList<>();
            Set<Map.Entry<String, BigDecimal>> entries = sumAmount.entrySet();
            for (Map.Entry<String, BigDecimal> entry : entries) {
                PaymentBatchDetail paymentBatchDetail = new PaymentBatchDetail();
                if (StrUtil.isNotBlank(entry.getKey())) {
                    if (entry.getKey().startsWith("A") || entry.getKey().startsWith("S") || entry.getKey().startsWith("T") || entry.getKey().startsWith("LC") || entry.getKey().startsWith("IO")) {
                        if (entry.getKey().length() > 2) {
                            paymentBatchDetail.setCode(entry.getKey().substring(0, 2) + "-" + entry.getKey().substring(2));
                        } else {
                            paymentBatchDetail.setCode(entry.getKey());
                        }
                        paymentBatchDetail.setIsOrderCode(true);
                    } else {
                        if (entry.getKey().length() > 3) {
                            paymentBatchDetail.setCode(entry.getKey().substring(0, 3) + "-" + entry.getKey().substring(3));
                        } else {
                            paymentBatchDetail.setCode(entry.getKey());
                        }
                        paymentBatchDetail.setIsOrderCode(false);
                    }
                }
                paymentBatchDetail.setUploadAmount(entry.getValue().setScale(2, BigDecimal.ROUND_HALF_UP));
                details.add(paymentBatchDetail);
            }
            return details;
        } catch (IOException ex) {
            return Collections.emptyList();
        }

    }
}
