package com.efreight.afbase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.dao.CssCostInvoiceMapper;
import com.efreight.afbase.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * CSS 应付：发票申请表 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-04
 */
@Service
@AllArgsConstructor
public class CssCostInvoiceServiceImpl extends ServiceImpl<CssCostInvoiceMapper, CssCostInvoice> implements CssCostInvoiceService {

    private final CssPaymentService cssPaymentService;

    private final CssCostInvoiceDetailService cssCostInvoiceDetailService;

    private final AfOrderService afOrderService;

    private final ScOrderService scOrderService;

    private final TcOrderService tcOrderService;

    private final LcOrderService lcOrderService;

    private final IoOrderService ioOrderService;

    private final CssPaymentDetailService cssPaymentDetailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(CssCostInvoice cssCostInvoice) {
        //校验
        CssPayment cssPayment = cssPaymentService.getById(cssCostInvoice.getPaymentId());
        if (cssPayment == null) {
            throw new RuntimeException("账单不存在");
        }
        if (!cssPayment.getRowUuid().equals(cssCostInvoice.getRowUuid())) {
            throw new RuntimeException("账单已变更，请刷新页面重新操作");
        }
        LambdaQueryWrapper<CssCostInvoice> wrapper = Wrappers.<CssCostInvoice>lambdaQuery();
        wrapper.eq(CssCostInvoice::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoice::getPaymentId, cssCostInvoice.getPaymentId());
        CssCostInvoice invoice = getOne(wrapper);
        if (invoice != null) {
            throw new RuntimeException("您好，对账单号 " + cssPayment.getPaymentNum() + " 已做 付款申请 ，不能重复申请！");
        }
        cssCostInvoice.setOrgId(SecurityUtils.getUser().getOrgId());
        cssCostInvoice.setCreateTime(LocalDateTime.now());
        cssCostInvoice.setCreatorId(SecurityUtils.getUser().getId());
        cssCostInvoice.setCreatorName(SecurityUtils.getUser().buildOptName());
        cssCostInvoice.setInvoiceStatus(-1);
        cssCostInvoice.setRowUuid(UUID.randomUUID().toString());
        //保存
        save(cssCostInvoice);

        //修改账单
        cssPayment.setRowUuid(UUID.randomUUID().toString());
        cssPaymentService.updateById(cssPayment);

        //修改订单状态
        LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
        cssPaymentDetailWrapper.eq(CssPaymentDetail::getPaymentId,cssCostInvoice.getPaymentId()).eq(CssPaymentDetail::getOrgId,SecurityUtils.getUser().getOrgId());
        if(cssPayment.getBusinessScope().startsWith("A")){
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId->{
                AfOrder afOrder = afOrderService.getById(orderId);
                afOrder.setCostStatus(afOrderService.getOrderCostStatusForAF(orderId));
                afOrder.setRowUuid(UUID.randomUUID().toString());
                afOrderService.updateById(afOrder);
            });
        }else if(cssPayment.getBusinessScope().startsWith("S")){
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId->{
                ScOrder scOrder = scOrderService.getById(orderId);
                scOrder.setCostStatus(afOrderService.getOrderCostStatusForSC(orderId));
                scOrder.setRowUuid(UUID.randomUUID().toString());
                scOrderService.updateById(scOrder);
            });
        }else if(cssPayment.getBusinessScope().startsWith("T")){
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId->{
                TcOrder tcOrder = tcOrderService.getById(orderId);
                tcOrder.setCostStatus(afOrderService.getOrderCostStatusForTC(orderId));
                tcOrder.setRowUuid(UUID.randomUUID().toString());
                tcOrderService.updateById(tcOrder);
            });
        }else if("LC".equals(cssPayment.getBusinessScope())){
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId->{
                LcOrder lcOrder = lcOrderService.getById(orderId);
                lcOrder.setCostStatus(afOrderService.getOrderCostStatusForLC(orderId));
                lcOrder.setRowUuid(UUID.randomUUID().toString());
                lcOrderService.updateById(lcOrder);
            });
        }else if("IO".equals(cssPayment.getBusinessScope())){
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId->{
                IoOrder ioOrder = ioOrderService.getById(orderId);
                ioOrder.setCostStatus(afOrderService.getOrderCostStatusForIO(orderId));
                ioOrder.setRowUuid(UUID.randomUUID().toString());
                ioOrderService.updateById(ioOrder);
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Integer paymentId, String rowUuid) {
        //校验
        CssPayment cssPayment = cssPaymentService.getById(paymentId);
        if (cssPayment == null) {
            throw new RuntimeException("账单不存在");
        }
        if (!cssPayment.getRowUuid().equals(rowUuid)) {
            throw new RuntimeException("账单已变更，请刷新页面重新操作");
        }
        LambdaQueryWrapper<CssCostInvoice> wrapper = Wrappers.<CssCostInvoice>lambdaQuery();
        wrapper.eq(CssCostInvoice::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoice::getPaymentId, paymentId);
        CssCostInvoice invoice = getOne(wrapper);
        if (invoice == null) {
            throw new RuntimeException("您好，对账单号 " + cssPayment.getPaymentNum() + " 未做 付款申请 ，无法撤销申请！");
        }
        if (!invoice.getInvoiceStatus().equals(-1)) {
            throw new RuntimeException("您好，对账单号  " + cssPayment.getPaymentNum() + " 已收票， 无法撤销申请！");
        }

        //删除发票申请
        removeById(invoice.getInvoiceId());

        //更新账单
        cssPayment.setRowUuid(UUID.randomUUID().toString());
        cssPaymentService.updateById(cssPayment);

        //修改订单状态
        LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
        cssPaymentDetailWrapper.eq(CssPaymentDetail::getPaymentId,paymentId).eq(CssPaymentDetail::getOrgId,SecurityUtils.getUser().getOrgId());
        if(cssPayment.getBusinessScope().startsWith("A")){
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId->{
                AfOrder afOrder = afOrderService.getById(orderId);
                afOrder.setCostStatus(afOrderService.getOrderCostStatusForAF(orderId));
                afOrder.setRowUuid(UUID.randomUUID().toString());
                afOrderService.updateById(afOrder);
            });
        }else if(cssPayment.getBusinessScope().startsWith("S")){
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId->{
                ScOrder scOrder = scOrderService.getById(orderId);
                scOrder.setCostStatus(afOrderService.getOrderCostStatusForSC(orderId));
                scOrder.setRowUuid(UUID.randomUUID().toString());
                scOrderService.updateById(scOrder);
            });
        }else if(cssPayment.getBusinessScope().startsWith("T")){
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId->{
                TcOrder tcOrder = tcOrderService.getById(orderId);
                tcOrder.setCostStatus(afOrderService.getOrderCostStatusForTC(orderId));
                tcOrder.setRowUuid(UUID.randomUUID().toString());
                tcOrderService.updateById(tcOrder);
            });
        }else if("LC".equals(cssPayment.getBusinessScope())){
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId->{
                LcOrder lcOrder = lcOrderService.getById(orderId);
                lcOrder.setCostStatus(afOrderService.getOrderCostStatusForLC(orderId));
                lcOrder.setRowUuid(UUID.randomUUID().toString());
                lcOrderService.updateById(lcOrder);
            });
        }else if("IO".equals(cssPayment.getBusinessScope())){
            cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(CssPaymentDetail::getOrderId).distinct().forEach(orderId->{
                IoOrder ioOrder = ioOrderService.getById(orderId);
                ioOrder.setCostStatus(afOrderService.getOrderCostStatusForIO(orderId));
                ioOrder.setRowUuid(UUID.randomUUID().toString());
                ioOrderService.updateById(ioOrder);
            });
        }
    }

    @Override
    public CssCostInvoice view(Integer invoiceId) {
        CssCostInvoice costInvoice = getById(invoiceId);
        if (costInvoice == null) {
            throw new RuntimeException("该付款申请不存在，请刷新再操作");
        }
        LambdaQueryWrapper<CssCostInvoiceDetail> cssCostInvoiceDetailWrapper = Wrappers.<CssCostInvoiceDetail>lambdaQuery();
        cssCostInvoiceDetailWrapper.eq(CssCostInvoiceDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoiceDetail::getInvoiceId, invoiceId);
        List<CssCostInvoiceDetail> list = cssCostInvoiceDetailService.list(cssCostInvoiceDetailWrapper);
        list.stream().forEach(cssCostInvoiceDetail -> {
            cssCostInvoiceDetail.setAmountStr(FormatUtils.formatWithQWF(cssCostInvoiceDetail.getAmount(), 2) + " (" + cssCostInvoiceDetail.getCurrency() + ")");
        });
        costInvoice.setList(list);
        CssPayment payment = cssPaymentService.getById(costInvoice.getPaymentId());
        if (payment != null) {
            costInvoice.setPaymentNum(payment.getPaymentNum());
            costInvoice.setCurrency(payment.getCurrency());
            costInvoice.setAmount(payment.getAmountPayment());
            costInvoice.setAmountStr(FormatUtils.formatWithQWF(payment.getAmountPayment(), 2));
        }
        return costInvoice;
    }

    @Override
    public void checkIfInvoiceCompleteWhenInsertInvoiceDetail(Integer invoiceId, String rowUuid) {
        CssCostInvoice costInvoice = getById(invoiceId);
        if (costInvoice == null) {
            throw new RuntimeException("该付款申请不存在，请刷新再操作");
        }
        if (!costInvoice.getRowUuid().equals(rowUuid)) {
            throw new RuntimeException("您好，数据不是最新 请刷新重试。");
        }
        if (costInvoice.getInvoiceStatus().equals(1)) {
            throw new RuntimeException("您好，已收票完成，不能新增。");
        }
    }

    @Override
    public void checkIfCreateInvoice(Integer paymentId) {
        LambdaQueryWrapper<CssCostInvoice> wrapper = Wrappers.<CssCostInvoice>lambdaQuery();
        wrapper.eq(CssCostInvoice::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostInvoice::getPaymentId, paymentId);
        CssCostInvoice invoice = getOne(wrapper);
        if (invoice != null) {
            throw new RuntimeException("您好，对账单 已做 付款申请 ，不能重复申请！");
        }
    }
}
