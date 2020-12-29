package com.efreight.afbase.service.impl;

import com.efreight.afbase.entity.CssPaymentDetail;
import com.efreight.afbase.entity.ScCost;
import com.efreight.afbase.entity.ScOrder;
import com.efreight.afbase.dao.ScCostMapper;
import com.efreight.afbase.service.CssPaymentDetailService;
import com.efreight.afbase.service.ScCostService;
import com.efreight.afbase.service.ScOrderService;
import com.efreight.common.security.util.SecurityUtils;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

/**
 * <p>
 * CS 延伸服务 成本 服务实现类
 * </p>
 *
 * @author qipm
 * @since 2020-03-06
 */
@Service
@AllArgsConstructor
public class ScCostServiceImpl extends ServiceImpl<ScCostMapper, ScCost> implements ScCostService {
	private final ScOrderService scOrderService;
	private final CssPaymentDetailService cssPaymentDetailService;

	@Override
	public List<ScCost> getCostList(ScCost scCost) {
        LambdaQueryWrapper<ScCost> wrapper = Wrappers.<ScCost>lambdaQuery();
        //关于订单的查询约束
        LambdaQueryWrapper<ScOrder> orderWrapper = Wrappers.<ScOrder>lambdaQuery();
        orderWrapper.eq(ScOrder::getOrgId, SecurityUtils.getUser().getOrgId());
        boolean flag = false;
        if (scCost.getFlightDateStart() != null) {
            flag = true;
            if("SE".equals(scCost.getBusinessScope())) {
            	orderWrapper.ge(ScOrder::getExpectDeparture, scCost.getFlightDateStart());
            }
            if("SI".equals(scCost.getBusinessScope())) {
            	orderWrapper.ge(ScOrder::getExpectArrival, scCost.getFlightDateStart());
            }
        }
        if (scCost.getFlightDateEnd() != null) {
            flag = true;
            if("SE".equals(scCost.getBusinessScope())) {
            	orderWrapper.le(ScOrder::getExpectDeparture, scCost.getFlightDateEnd());
            }
            if("SI".equals(scCost.getBusinessScope())) {
            	orderWrapper.le(ScOrder::getExpectArrival, scCost.getFlightDateEnd());
            }
        }
        if (StrUtil.isNotBlank(scCost.getAwbOrOrderNumbers())) {
            flag = true;
            if (scCost.getAwbOrOrderNumbers().contains(",")) {
            	orderWrapper.and(i -> i.in(ScOrder::getMblNumber, scCost.getAwbOrOrderNumbers().split(",")).or(j -> j.in(ScOrder::getOrderCode, scCost.getAwbOrOrderNumbers().split(","))));
            } else {
                orderWrapper.and(i -> i.like(ScOrder::getMblNumber, "%" + scCost.getAwbOrOrderNumbers() + "%").or(j -> j.like(ScOrder::getOrderCode, "%" + scCost.getAwbOrOrderNumbers() + "%")));
            }
        }
        if (flag) {
            List<Integer> orderIds = scOrderService.list(orderWrapper).stream().map(ScOrder::getOrderId).collect(Collectors.toList());
            if (orderIds.size() == 0) {
                return new ArrayList<ScCost>();
            }
            wrapper.in(ScCost::getOrderId, orderIds);
        }

        wrapper.eq(ScCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(ScCost::getBusinessScope, scCost.getBusinessScope()).eq(ScCost::getCustomerId, scCost.getCustomerId()).eq(ScCost::getCostCurrency, scCost.getCostCurrency());
        if (StrUtil.isNotBlank(scCost.getNoCostIds())) {
            wrapper.notIn(ScCost::getCostId, scCost.getNoCostIds().split(","));
        }
        if (StrUtil.isNotBlank(scCost.getDeleteCostIds())) {
            wrapper.last(" and (cost_amount <> cost_amount_payment or cost_amount_payment is null or cost_id in (" + scCost.getDeleteCostIds() + "))");
        } else {
            wrapper.last(" and (cost_amount <> cost_amount_payment or cost_amount_payment is null)");
        }
        if (StrUtil.isNotBlank(scCost.getServiceIds())) {
            wrapper.in(ScCost::getServiceId, scCost.getServiceIds().split(","));
        }
        List<ScCost> costList = list(wrapper);
        //封装页面显示金额
        costList.stream().forEach(cost -> {
            //在对账单编辑页面中，进入新增服务后对上个页面删除的对账明细（一定是编辑的对账明细，不是新增）的已对账金额虚拟处理显示
            if (StrUtil.isNotBlank(scCost.getDeleteCostIds())) {
                if (Arrays.asList(scCost.getDeleteCostIds().split(",")).stream().filter(costId -> cost.getCostId().equals(Integer.parseInt(costId))).collect(Collectors.toList()).size() > 0) {
                    LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailLambdaQueryWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
                    cssPaymentDetailLambdaQueryWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, cost.getCostId()).eq(CssPaymentDetail::getPaymentId, scCost.getPaymentId());
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
                if ("SE".equals(scCost.getBusinessScope())) {
                    cost.setOrderCode(StrUtil.isBlank(scOrder.getMblNumber()) ? scOrder.getOrderCode() : scOrder.getMblNumber());
                    cost.setFlightDate(scOrder.getExpectDeparture());
                }
                if ("SI".equals(scCost.getBusinessScope())) {
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
