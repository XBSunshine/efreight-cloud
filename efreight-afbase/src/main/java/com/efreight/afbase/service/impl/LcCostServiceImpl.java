package com.efreight.afbase.service.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.LcCostMapper;
import com.efreight.afbase.entity.CssPaymentDetail;
import com.efreight.afbase.entity.LcCost;
import com.efreight.afbase.entity.LcOrder;
import com.efreight.afbase.service.CssPaymentDetailService;
import com.efreight.afbase.service.LcCostService;
import com.efreight.afbase.service.LcOrderService;
import com.efreight.afbase.service.TcOrderService;
import com.efreight.common.security.util.SecurityUtils;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;

/**
 * <p>
 * LC 费用录入 成本 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
@Service
@AllArgsConstructor
public class LcCostServiceImpl extends ServiceImpl<LcCostMapper, LcCost> implements LcCostService {
	
	private final LcOrderService lcOrderService;
	private final CssPaymentDetailService cssPaymentDetailService;

	@Override
	public List<LcCost> getCostList(LcCost lcCost) {

        LambdaQueryWrapper<LcCost> wrapper = Wrappers.<LcCost>lambdaQuery();
        //关于订单的查询约束
        LambdaQueryWrapper<LcOrder> orderWrapper = Wrappers.<LcOrder>lambdaQuery();
        orderWrapper.eq(LcOrder::getOrgId, SecurityUtils.getUser().getOrgId());
        boolean flag = false;
        if (lcCost.getFlightDateStart() != null) {
            flag = true;
            orderWrapper.ge(LcOrder::getDrivingTime, lcCost.getFlightDateStart());
        }
        if (lcCost.getFlightDateEnd() != null) {
            flag = true;
            orderWrapper.le(LcOrder::getDrivingTime, lcCost.getFlightDateEnd());
        }
        if (StrUtil.isNotBlank(lcCost.getAwbOrOrderNumbers())) {
            flag = true;
            if (lcCost.getAwbOrOrderNumbers().contains(",")) {
            	orderWrapper.and(i -> i.in(LcOrder::getCustomerNumber, lcCost.getAwbOrOrderNumbers().split(",")).or(j -> j.in(LcOrder::getOrderCode, lcCost.getAwbOrOrderNumbers().split(","))));
            } else {
                orderWrapper.and(i -> i.like(LcOrder::getCustomerNumber, "%" + lcCost.getAwbOrOrderNumbers() + "%").or(j -> j.like(LcOrder::getOrderCode, "%" + lcCost.getAwbOrOrderNumbers() + "%")));
            }
        }
        if (flag) {
            List<Integer> orderIds = lcOrderService.list(orderWrapper).stream().map(LcOrder::getOrderId).collect(Collectors.toList());
            if (orderIds.size() == 0) {
                return new ArrayList<LcCost>();
            }
            wrapper.in(LcCost::getOrderId, orderIds);
        }

        wrapper.eq(LcCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(LcCost::getBusinessScope, lcCost.getBusinessScope()).eq(LcCost::getCustomerId, lcCost.getCustomerId()).eq(LcCost::getCostCurrency, lcCost.getCostCurrency());
        if (StrUtil.isNotBlank(lcCost.getNoCostIds())) {
            wrapper.notIn(LcCost::getCostId, lcCost.getNoCostIds().split(","));
        }
        if (StrUtil.isNotBlank(lcCost.getDeleteCostIds())) {
            wrapper.last(" and (cost_amount <> cost_amount_payment or cost_amount_payment is null or cost_id in (" + lcCost.getDeleteCostIds() + "))");
        } else {
            wrapper.last(" and (cost_amount <> cost_amount_payment or cost_amount_payment is null)");
        }
        if (StrUtil.isNotBlank(lcCost.getServiceIds())) {
            wrapper.in(LcCost::getServiceId, lcCost.getServiceIds().split(","));
        }
        List<LcCost> costList = list(wrapper);
        //封装页面显示金额
        costList.stream().forEach(cost -> {
            //在对账单编辑页面中，进入新增服务后对上个页面删除的对账明细（一定是编辑的对账明细，不是新增）的已对账金额虚拟处理显示
            if (StrUtil.isNotBlank(lcCost.getDeleteCostIds())) {
                if (Arrays.asList(lcCost.getDeleteCostIds().split(",")).stream().filter(costId -> cost.getCostId().equals(Integer.parseInt(costId))).collect(Collectors.toList()).size() > 0) {
                    LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailLambdaQueryWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                    cssPaymentDetailLambdaQueryWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, cost.getCostId()).eq(CssPaymentDetail::getPaymentId, lcCost.getPaymentId());
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
            	if(lcOrder.getDrivingTime()!=null) {
            		cost.setFlightDate(lcOrder.getDrivingTime().toLocalDate());
            	}
                //客户单号
                cost.setCustomerNumber(lcOrder.getCustomerNumber());
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
