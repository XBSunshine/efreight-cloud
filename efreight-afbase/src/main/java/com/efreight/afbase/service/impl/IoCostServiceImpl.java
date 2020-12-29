package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.afbase.entity.CssPaymentDetail;
import com.efreight.afbase.entity.IoCost;
import com.efreight.afbase.dao.IoCostMapper;
import com.efreight.afbase.entity.IoOrder;
import com.efreight.afbase.service.CssPaymentDetailService;
import com.efreight.afbase.service.IoCostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.service.IoOrderService;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * IO 费用录入 成本 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-18
 */
@Service
@AllArgsConstructor
public class IoCostServiceImpl extends ServiceImpl<IoCostMapper, IoCost> implements IoCostService {

    private final IoOrderService ioOrderService;
    private final CssPaymentDetailService cssPaymentDetailService;

    @Override
    public List<IoCost> getCostList(IoCost ioCost) {

        LambdaQueryWrapper<IoCost> wrapper = Wrappers.<IoCost>lambdaQuery();
        //关于订单的查询约束
        LambdaQueryWrapper<IoOrder> orderWrapper = Wrappers.<IoOrder>lambdaQuery();
        orderWrapper.eq(IoOrder::getOrgId, SecurityUtils.getUser().getOrgId());
        boolean flag = false;
        if (ioCost.getFlightDateStart() != null) {
            flag = true;
            orderWrapper.ge(IoOrder::getBusinessDate, ioCost.getFlightDateStart());
        }
        if (ioCost.getFlightDateEnd() != null) {
            flag = true;
            orderWrapper.le(IoOrder::getBusinessDate, ioCost.getFlightDateEnd());
        }
        if (StrUtil.isNotBlank(ioCost.getAwbOrOrderNumbers())) {
            flag = true;
            if (ioCost.getAwbOrOrderNumbers().contains(",")) {
                orderWrapper.and(i -> i.in(IoOrder::getCustomerNumber, ioCost.getAwbOrOrderNumbers().split(",")).or(j -> j.in(IoOrder::getOrderCode, ioCost.getAwbOrOrderNumbers().split(","))));
            } else {
                orderWrapper.and(i -> i.like(IoOrder::getCustomerNumber, "%" + ioCost.getAwbOrOrderNumbers() + "%").or(j -> j.like(IoOrder::getOrderCode, "%" + ioCost.getAwbOrOrderNumbers() + "%")));
            }
        }
        if (flag) {
            List<Integer> orderIds = ioOrderService.list(orderWrapper).stream().map(IoOrder::getOrderId).collect(Collectors.toList());
            if (orderIds.size() == 0) {
                return new ArrayList<IoCost>();
            }
            wrapper.in(IoCost::getOrderId, orderIds);
        }

        wrapper.eq(IoCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(IoCost::getBusinessScope, ioCost.getBusinessScope()).eq(IoCost::getCustomerId, ioCost.getCustomerId()).eq(IoCost::getCostCurrency, ioCost.getCostCurrency());
        if (StrUtil.isNotBlank(ioCost.getNoCostIds())) {
            wrapper.notIn(IoCost::getCostId, ioCost.getNoCostIds().split(","));
        }
        if (StrUtil.isNotBlank(ioCost.getDeleteCostIds())) {
            wrapper.last(" and (cost_amount <> cost_amount_payment or cost_amount_payment is null or cost_id in (" + ioCost.getDeleteCostIds() + "))");
        } else {
            wrapper.last(" and (cost_amount <> cost_amount_payment or cost_amount_payment is null)");
        }
        if (StrUtil.isNotBlank(ioCost.getServiceIds())) {
            wrapper.in(IoCost::getServiceId, ioCost.getServiceIds().split(","));
        }
        List<IoCost> costList = list(wrapper);
        //封装页面显示金额
        costList.stream().forEach(cost -> {
            //在对账单编辑页面中，进入新增服务后对上个页面删除的对账明细（一定是编辑的对账明细，不是新增）的已对账金额虚拟处理显示
            if (StrUtil.isNotBlank(ioCost.getDeleteCostIds())) {
                if (Arrays.asList(ioCost.getDeleteCostIds().split(",")).stream().filter(costId -> cost.getCostId().equals(Integer.parseInt(costId))).collect(Collectors.toList()).size() > 0) {
                    LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailLambdaQueryWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                    cssPaymentDetailLambdaQueryWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, cost.getCostId()).eq(CssPaymentDetail::getPaymentId, ioCost.getPaymentId());
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
                if(ioOrder.getBusinessDate()!=null) {
                    cost.setFlightDate(ioOrder.getBusinessDate());
                }
                //客户单号
                cost.setCustomerNumber(ioOrder.getCustomerNumber());
            }
        });
        return costList.stream().sorted((e1, e2) -> {
            if (e1.getFlightDate().compareTo(e2.getFlightDate()) == 0) {
                return e1.getCostAmountNoPayment().compareTo(e2.getCostAmountNoPayment());
            }
            return e1.getFlightDate().compareTo(e2.getFlightDate());
        }).collect(Collectors.toList());
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
}
