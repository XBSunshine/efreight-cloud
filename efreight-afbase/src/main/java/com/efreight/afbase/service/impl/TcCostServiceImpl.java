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
import com.efreight.afbase.dao.TcCostMapper;
import com.efreight.afbase.entity.CssPaymentDetail;
import com.efreight.afbase.entity.TcCost;
import com.efreight.afbase.entity.TcOrder;
import com.efreight.afbase.service.CssPaymentDetailService;
import com.efreight.afbase.service.TcCostService;
import com.efreight.afbase.service.TcOrderService;
import com.efreight.common.security.util.SecurityUtils;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;

/**
 * <p>
 * TC 费用录入 成本 服务实现类
 * </p>
 *
 * @author caiwd
 * @since 2020-07-15
 */
@Service
@AllArgsConstructor
public class TcCostServiceImpl extends ServiceImpl<TcCostMapper, TcCost> implements TcCostService {

	private final TcOrderService tcOrderService;
	private final CssPaymentDetailService cssPaymentDetailService;
	@Override
	public List<TcCost> etCostList(TcCost tcCost) {

        LambdaQueryWrapper<TcCost> wrapper = Wrappers.<TcCost>lambdaQuery();
        //关于订单的查询约束
        LambdaQueryWrapper<TcOrder> orderWrapper = Wrappers.<TcOrder>lambdaQuery();
        orderWrapper.eq(TcOrder::getOrgId, SecurityUtils.getUser().getOrgId());
        boolean flag = false;
        if (tcCost.getFlightDateStart() != null) {
            flag = true;
            if("TE".equals(tcCost.getBusinessScope())) {
            	orderWrapper.ge(TcOrder::getExpectDeparture, tcCost.getFlightDateStart());
            }
            if("TI".equals(tcCost.getBusinessScope())) {
            	orderWrapper.ge(TcOrder::getExpectArrival, tcCost.getFlightDateStart());
            }
        }
        if (tcCost.getFlightDateEnd() != null) {
            flag = true;
            if("TE".equals(tcCost.getBusinessScope())) {
            	orderWrapper.le(TcOrder::getExpectDeparture, tcCost.getFlightDateEnd());
            }
            if("TI".equals(tcCost.getBusinessScope())) {
            	orderWrapper.le(TcOrder::getExpectArrival, tcCost.getFlightDateEnd());
            }
        }
        if (StrUtil.isNotBlank(tcCost.getAwbOrOrderNumbers())) {
            flag = true;
            if (tcCost.getAwbOrOrderNumbers().contains(",")) {
            	orderWrapper.and(i -> i.in(TcOrder::getRwbNumber, tcCost.getAwbOrOrderNumbers().split(",")).or(j -> j.in(TcOrder::getOrderCode, tcCost.getAwbOrOrderNumbers().split(","))));
            } else {
                orderWrapper.and(i -> i.like(TcOrder::getRwbNumber, "%" + tcCost.getAwbOrOrderNumbers() + "%").or(j -> j.like(TcOrder::getOrderCode, "%" + tcCost.getAwbOrOrderNumbers() + "%")));
            }
        }
        if (flag) {
            List<Integer> orderIds = tcOrderService.list(orderWrapper).stream().map(TcOrder::getOrderId).collect(Collectors.toList());
            if (orderIds.size() == 0) {
                return new ArrayList<TcCost>();
            }
            wrapper.in(TcCost::getOrderId, orderIds);
        }

        wrapper.eq(TcCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TcCost::getBusinessScope, tcCost.getBusinessScope()).eq(TcCost::getCustomerId, tcCost.getCustomerId()).eq(TcCost::getCostCurrency, tcCost.getCostCurrency());
        if (StrUtil.isNotBlank(tcCost.getNoCostIds())) {
            wrapper.notIn(TcCost::getCostId, tcCost.getNoCostIds().split(","));
        }
        if (StrUtil.isNotBlank(tcCost.getDeleteCostIds())) {
            wrapper.last(" and (cost_amount <> cost_amount_payment or cost_amount_payment is null or cost_id in (" + tcCost.getDeleteCostIds() + "))");
        } else {
            wrapper.last(" and (cost_amount <> cost_amount_payment or cost_amount_payment is null)");
        }
        if (StrUtil.isNotBlank(tcCost.getServiceIds())) {
            wrapper.in(TcCost::getServiceId, tcCost.getServiceIds().split(","));
        }
        List<TcCost> costList = list(wrapper);
        //封装页面显示金额
        costList.stream().forEach(cost -> {
            //在对账单编辑页面中，进入新增服务后对上个页面删除的对账明细（一定是编辑的对账明细，不是新增）的已对账金额虚拟处理显示
            if (StrUtil.isNotBlank(tcCost.getDeleteCostIds())) {
                if (Arrays.asList(tcCost.getDeleteCostIds().split(",")).stream().filter(costId -> cost.getCostId().equals(Integer.parseInt(costId))).collect(Collectors.toList()).size() > 0) {
                    LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailLambdaQueryWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                    cssPaymentDetailLambdaQueryWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, cost.getCostId()).eq(CssPaymentDetail::getPaymentId, tcCost.getPaymentId());
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
                
                if("TE".equals(tcCost.getBusinessScope())) {
                	cost.setOrderCode(StrUtil.isBlank(tcOrder.getRwbNumber()) ? tcOrder.getOrderCode() : tcOrder.getRwbNumber());
                	cost.setFlightDate(tcOrder.getExpectDeparture());
                }else {
                	cost.setFlightDate(tcOrder.getExpectArrival());
                }
                //客户单号
                cost.setCustomerNumber(tcOrder.getCustomerNumber());
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
