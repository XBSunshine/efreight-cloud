package com.efreight.afbase.service.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.dao.AfCostMapper;
import com.efreight.afbase.dao.ScCostMapper;
import com.efreight.afbase.dao.TcCostMapper;
import com.efreight.afbase.service.*;
import com.efreight.afbase.utils.FieldValUtils;
import com.efreight.afbase.utils.FormatUtils;
import com.efreight.common.core.feign.RemoteCoopService;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.WebUtils;
import com.efreight.common.security.vo.CoopVo;
import com.efreight.common.security.util.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * AF 延伸服务 成本 服务实现类
 * </p>
 *
 * @author qipm
 * @since 2019-10-12
 */
@Service
@AllArgsConstructor
public class AfCostServiceImpl extends ServiceImpl<AfCostMapper, AfCost> implements AfCostService {

    private final AfOrderService afOrderService;

    private final ScOrderService scOrderService;

    private final ScCostService scCostService;

    private final CssPaymentDetailService cssPaymentDetailService;

    private final RemoteCoopService remoteCoopService;
    private final ScCostMapper scCostMapper;

    private final TcCostMapper tcCostMapper;

    private final TcCostService tcCostService;

    private final TcOrderService tcOrderService;

    private final LcOrderService lcOrderService;

    private final LcCostService lcCostService;

    private final IoOrderService ioOrderService;

    private final IoCostService ioCostService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doSave(AfCost bean) {
        bean.setCreateTime(LocalDateTime.now());
        bean.setCreatorId(SecurityUtils.getUser().getId());
        bean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        baseMapper.insert(bean);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doUpdate(AfCost bean) {
        bean.setEditTime(LocalDateTime.now());
        bean.setEditorId(SecurityUtils.getUser().getId());
        bean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        UpdateWrapper<AfCost> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("income_id", bean.getIncomeId());
        baseMapper.update(bean, updateWrapper);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doDelete(AfCost bean) {

        if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
            baseMapper.deleteById(bean.getCostId());
        } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
            scCostMapper.deleteById(bean.getCostId());
        } else if (bean.getBusinessScope().startsWith("T")) {
            tcCostMapper.deleteById(bean.getCostId());
        }
        return true;
    }

    @Override
    public List<AfCost> getCostList(AfCost afCost) {
        LambdaQueryWrapper<AfCost> wrapper = Wrappers.<AfCost>lambdaQuery();
        //关于订单的查询约束
        LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
        orderWrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId());
        boolean flag = false;
        if (afCost.getFlightDateStart() != null) {
            flag = true;
            if ("AE".equals(afCost.getBusinessScope())) {
                orderWrapper.ge(AfOrder::getExpectDeparture, afCost.getFlightDateStart());
            }
            if ("AI".equals(afCost.getBusinessScope())) {
                orderWrapper.ge(AfOrder::getExpectArrival, afCost.getFlightDateStart());
            }

        }
        if (afCost.getFlightDateEnd() != null) {
            flag = true;
            if ("AE".equals(afCost.getBusinessScope())) {
                orderWrapper.le(AfOrder::getExpectDeparture, afCost.getFlightDateEnd());
            }
            if ("AI".equals(afCost.getBusinessScope())) {
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
        List<AfCost> costList = list(wrapper);
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
        return costList.stream().sorted((e1, e2) -> {
            if (e1.getFlightDate().compareTo(e2.getFlightDate()) == 0) {
                return e1.getCostAmountNoPayment().compareTo(e2.getCostAmountNoPayment());
            }
            return e1.getFlightDate().compareTo(e2.getFlightDate());
        }).collect(Collectors.toList());
    }

    @Override
    public IPage getPageForAF(Page page, AfCost afCost) {
        IPage<AfCost> result = null;
        //查询汇总
        if (afCost.isGroupSum()) {
            afCost.setOrgId(SecurityUtils.getUser().getOrgId());
            if (StrUtil.isNotBlank(afCost.getAwbNumber())) {
                if (afCost.getAwbNumber().split("_").length == 2) {
                    afCost.setAwbNumber(afCost.getAwbNumber().split("_")[0]);
                    afCost.setHawbNumber(afCost.getAwbNumber().split("_")[1]);
                } else {
                    afCost.setHawbNumber(afCost.getAwbNumber());
                }
            }
            //订单号+供应商+币种
            result = baseMapper.queryAfCostPageAf(page, afCost);
            result.getRecords().stream().forEach(cost -> {
                if (cost.getCostAmountWriteoff() != null) {
                    cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
                } else {
                    cost.setCostAmountWriteoff(BigDecimal.ZERO);
                    cost.setCostAmountNoWriteoff(cost.getCostAmount());
                }
                cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
                cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
                cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
                cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
                cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
                cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));
                //已对帐金额
                if (cost.getCostAmountPayment() != null) {
                    BigDecimal p = cost.getCostAmountPayment();
                    String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                    cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountPaymentStr(str);
                    if (cost.getCostExchangeRate() != null) {
                        BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                        //.setScale(2,BigDecimal.ROUND_HALF_UP)
                        String str2 = FormatUtils.formatWith2AndQFW(p2);
                        cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        cost.setAmountFunctionalPaymentStr(str2);
                    }
                } else {
                    cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                    cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                }
                AfOrder order = afOrderService.getById(cost.getOrderId());
                if (order != null) {
                    if ("AE".equals(afCost.getBusinessScope())) {
                        //件数
                        if (!"".equals(order.getConfirmPieces()) && order.getConfirmPieces() != null) {
                            cost.setConfirmPieces(order.getConfirmPieces());
                        } else {
                            cost.setConfirmPieces(order.getPlanPieces());
                        }
                        //毛重
                        if (!"".equals(order.getConfirmWeight()) && order.getConfirmWeight() != null) {
                            cost.setConfirmWeight(order.getConfirmWeight());
                        } else {
                            cost.setConfirmWeight(order.getPlanWeight());
                        }
                        //体积
                        if (!"".equals(order.getConfirmVolume()) && order.getConfirmVolume() != null) {
                            cost.setConfirmVolume(order.getConfirmVolume());
                        } else {
                            cost.setConfirmVolume(order.getPlanVolume());
                        }
                        //计费重量
                        if (!"".equals(order.getConfirmChargeWeight()) && order.getConfirmChargeWeight() != null) {
                            cost.setConfirmChargeWeight(order.getConfirmChargeWeight());
                        } else {
                            cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                        }
                    } else if ("AI".equals(afCost.getBusinessScope())) {
                        //件数
                        cost.setConfirmPieces(order.getPlanPieces());
                        //毛重
                        cost.setConfirmWeight(order.getPlanWeight());
                        //体积
                        cost.setConfirmVolume(order.getPlanVolume());
                        //计费重量
                        cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                    }
                    //查询客户
                    CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                    if (coopVo1 != null) {
                        cost.setCoopName(coopVo1.getCoop_name());
                    }
                    //责任销售
                    String salesName = order.getSalesName();
                    if (!StrUtil.isEmpty(salesName)) {
                        cost.setSalesName(salesName.split(" ")[0]);
                    }
                    //责任客服
                    String servicerName = order.getServicerName();
                    if (!StrUtil.isEmpty(servicerName)) {
                        cost.setServicerName(servicerName.split(" ")[0]);
                    }
                }
            });


        } else {
            LambdaQueryWrapper<AfCost> wrapper = Wrappers.<AfCost>lambdaQuery();
            //整理订单查询条件
            if (StrUtil.isNotBlank(afCost.getAwbNumber()) || StrUtil.isNotBlank(afCost.getOrderCode()) || afCost.getFlightDateStart() != null || afCost.getFlightDateEnd() != null || StrUtil.isNotBlank(afCost.getCustomerNumber())) {
                LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
                if (StrUtil.isNotBlank(afCost.getOrderCode())) {
                    if (afCost.getOrderCode().split("_").length == 2) {
                        orderWrapper.like(AfOrder::getAwbNumber, "%" + afCost.getOrderCode().split("_")[0] + "%").like(AfOrder::getHawbNumber, "%" + afCost.getOrderCode().split("_")[1] + "%");
                    } else {
                        orderWrapper.and(i -> i.like(AfOrder::getAwbNumber, "%" + afCost.getOrderCode() + "%").or(j -> j.like(AfOrder::getHawbNumber, "%" + afCost.getOrderCode() + "%")).or(k -> k.like(AfOrder::getOrderCode, "%" + afCost.getOrderCode() + "%")));
                    }

                }
                if (StrUtil.isNotBlank(afCost.getCustomerNumber())) {
                    orderWrapper.like(AfOrder::getCustomerNumber, "%" + afCost.getCustomerNumber() + "%");
                }
                /*if (StrUtil.isNotBlank(afCost.getOrderCode())) {
                    orderWrapper.like(AfOrder::getOrderCode, "%" + afCost.getOrderCode() + "%");
                }*/
                if (afCost.getFlightDateStart() != null) {
                    if ("AE".equals(afCost.getBusinessScope())) {
                        orderWrapper.ge(AfOrder::getExpectDeparture, afCost.getFlightDateStart());
                    } else if ("AI".equals(afCost.getBusinessScope())) {
                        orderWrapper.ge(AfOrder::getExpectArrival, afCost.getFlightDateStart());
                    }
                }
                if (afCost.getFlightDateEnd() != null) {
                    if ("AE".equals(afCost.getBusinessScope())) {
                        orderWrapper.le(AfOrder::getExpectDeparture, afCost.getFlightDateEnd());
                    } else if ("AI".equals(afCost.getBusinessScope())) {
                        orderWrapper.le(AfOrder::getExpectArrival, afCost.getFlightDateEnd());
                    }
                }
                List<Integer> orderIds = afOrderService.list(orderWrapper).stream().map(afOrder -> afOrder.getOrderId()).collect(Collectors.toList());
                if (orderIds.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                wrapper.in(AfCost::getOrderId, orderIds);
            }

            //整理付款客户查询条件
            if (StrUtil.isNotBlank(afCost.getCustomerType())) {
                List<Integer> coopIds = remoteCoopService.listByType(afCost.getCustomerType()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
                if (coopIds.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                wrapper.in(AfCost::getCustomerId, coopIds);
            }

            //整理成本查询条件
            if (StrUtil.isNotBlank(afCost.getBusinessScope())) {
                wrapper.eq(AfCost::getBusinessScope, afCost.getBusinessScope());
            }

            if (StrUtil.isNotBlank(afCost.getCustomerName())) {
                wrapper.like(AfCost::getCustomerName, "%" + afCost.getCustomerName() + "%");
            }

            if (afCost.getServiceId() != null) {
                wrapper.eq(AfCost::getServiceId, afCost.getServiceId());
            }
            wrapper.eq(AfCost::getOrgId, SecurityUtils.getUser().getOrgId());

            result = page(page, wrapper);
            result.getRecords().stream().forEach(cost -> {
                CoopVo coopVo = remoteCoopService.viewCoop(cost.getCustomerId().toString()).getData();
                if (coopVo != null) {
                    cost.setCustomerType(coopVo.getCoop_type());
                }
                AfOrder order = afOrderService.getById(cost.getOrderId());
                if (order != null) {
                    cost.setOrderCode(order.getOrderCode());
                    cost.setAwbNumber(order.getAwbNumber());
                    cost.setCustomerNumber(order.getCustomerNumber());
                    if ("AE".equals(afCost.getBusinessScope())) {
                        cost.setFlightDate(order.getExpectDeparture());
                        //件数
                        if (!"".equals(order.getConfirmPieces()) && order.getConfirmPieces() != null) {
                            cost.setConfirmPieces(order.getConfirmPieces());
                        } else {
                            cost.setConfirmPieces(order.getPlanPieces());
                        }
                        //毛重
                        if (!"".equals(order.getConfirmWeight()) && order.getConfirmWeight() != null) {
                            cost.setConfirmWeight(order.getConfirmWeight());
                        } else {
                            cost.setConfirmWeight(order.getPlanWeight());
                        }
                        //体积
                        if (!"".equals(order.getConfirmVolume()) && order.getConfirmVolume() != null) {
                            cost.setConfirmVolume(order.getConfirmVolume());
                        } else {
                            cost.setConfirmVolume(order.getPlanVolume());
                        }
                        //计费重量
                        if (!"".equals(order.getConfirmChargeWeight()) && order.getConfirmChargeWeight() != null) {
                            cost.setConfirmChargeWeight(order.getConfirmChargeWeight());
                        } else {
                            cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                        }
                    } else if ("AI".equals(afCost.getBusinessScope())) {
                        cost.setFlightDate(order.getExpectArrival());
                        //件数
                        cost.setConfirmPieces(order.getPlanPieces());
                        //毛重
                        cost.setConfirmWeight(order.getPlanWeight());
                        //体积
                        cost.setConfirmVolume(order.getPlanVolume());
                        //计费重量
                        cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                    }
                    //查询客户
                    CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                    if (coopVo1 != null) {
                        cost.setCoopName(coopVo1.getCoop_name());
                    }
                    //责任销售
                    String salesName = order.getSalesName();
                    if (!StrUtil.isEmpty(salesName)) {
                        cost.setSalesName(salesName.split(" ")[0]);
                    }
                    //责任客服
                    String servicerName = order.getServicerName();
                    if (!StrUtil.isEmpty(servicerName)) {
                        cost.setServicerName(servicerName.split(" ")[0]);
                    }
                }
                if (cost.getCostAmountWriteoff() != null) {
                    cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
                } else {
                    cost.setCostAmountWriteoff(BigDecimal.ZERO);
                    cost.setCostAmountNoWriteoff(cost.getCostAmount());
                }
                cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
                cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
                cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
                cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
                cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
                cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));

                //已对帐金额
                HashMap map = baseMapper.queryAmountPaymentForAF(SecurityUtils.getUser().getOrgId(), cost.getCostId(), order.getOrderId(), afCost.getBusinessScope());
                if (map != null && map.containsKey("amount_payment")) {
                    BigDecimal p = new BigDecimal(map.get("amount_payment").toString());
                    String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                    cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountPaymentStr(str);
                    if (cost.getCostExchangeRate() != null) {
                        BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                        //.setScale(2,BigDecimal.ROUND_HALF_UP)
                        String str2 = FormatUtils.formatWith2AndQFW(p2);
                        cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        cost.setAmountFunctionalPaymentStr(str2);
                    }

                } else {
                    cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                    cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                }
            });

        }


        if (result.getRecords().size() != 0) {
            if (StrUtil.isNotBlank(afCost.getAwbNumber())) {
                if (afCost.getAwbNumber().split("_").length == 2) {
                    String awb = afCost.getAwbNumber().split("_")[0];
                    String hawb = afCost.getAwbNumber().split("_")[1];
                    afCost.setAwbNumber(awb);
                    afCost.setHawbNumber(hawb);
                } else {
                    afCost.setHawbNumber(null);
                }
            }
            afCost.setOrgId(SecurityUtils.getUser().getOrgId());
            List<AfCost> sumCostList = baseMapper.sumCostForAF(afCost);
            if (sumCostList != null && sumCostList.size() > 0) {

                StringBuffer costAmountStrBuffer = new StringBuffer();//成本金额(原币)
                BigDecimal costFunctionalAmount = new BigDecimal(0);//成本金额(本币)
                StringBuffer costAmountWriteoffStrBuffer = new StringBuffer();//已核销金额(原币)
                StringBuffer costAmountNoWriteoffStrBuffer = new StringBuffer();//未付款金额(原币)
                BigDecimal costFunctionalAmountNoWriteoff = new BigDecimal(0);//未付款金额(本币)
                StringBuffer amountPaymentStrBuffer = new StringBuffer();//已对账金额(原币)
                BigDecimal amountFunctionalPayment = new BigDecimal(0);//已对账金额(本币)
                for (int i = 0; i < sumCostList.size(); i++) {
                    //拼接成本金额(原币)
                    costAmountStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmount())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmount = costFunctionalAmount.add(sumCostList.get(i).getCostFunctionalAmount());
                    //拼接已核销金额(原币)
                    costAmountWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //拼接未付款金额(原币)
                    costAmountNoWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountNoWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmountNoWriteoff = costFunctionalAmountNoWriteoff.add(sumCostList.get(i).getCostFunctionalAmountNoWriteoff());
                    //拼接已对账金额(原币)
                    if (sumCostList.get(i).getCostAmountPayment() != null) {
                        amountPaymentStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountPayment())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    }
                    //已对账金额(本币)
                    amountFunctionalPayment = amountFunctionalPayment.add(BigDecimal.valueOf(sumCostList.get(i).getAmountFunctionalPayment()));
                }
                AfCost sumCost = new AfCost();
                sumCost.setBusinessScope("合计");
                sumCost.setCostAmountStr(costAmountStrBuffer.toString());
                sumCost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(costFunctionalAmount));
                sumCost.setCostAmountWriteoffStr(costAmountWriteoffStrBuffer.toString());
                sumCost.setCostAmountNoWriteoffStr(costAmountNoWriteoffStrBuffer.toString());
                sumCost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(costFunctionalAmountNoWriteoff));
                sumCost.setAmountPaymentStr(amountPaymentStrBuffer.toString());
                sumCost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(amountFunctionalPayment));

                //DoubleSummaryStatistics amountPaymentStr = result.getRecords().stream().mapToDouble(cost -> cost.getAmountPayment()).summaryStatistics();
                //sumCost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(new BigDecimal(amountPaymentStr.getSum() + "")));

                //DoubleSummaryStatistics amountFunctionalPaymentStr = result.getRecords().stream().mapToDouble(cost -> cost.getAmountFunctionalPayment()).summaryStatistics();
                //sumCost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(new BigDecimal(amountFunctionalPaymentStr.getSum() + "")));

                result.getRecords().add(sumCost);
            }
        }
        return result;
    }

    @Override
    public IPage getPageForSC(Page page, ScCost scCost) {
        IPage<ScCost> result = null;
        //查询汇总
        if (scCost.isGroupSum()) {
            scCost.setOrgId(SecurityUtils.getUser().getOrgId());
            if (StrUtil.isNotBlank(scCost.getAwbNumber())) {
                if (scCost.getAwbNumber().split("_").length == 2) {
                    scCost.setAwbNumber(scCost.getAwbNumber().split("_")[0]);
                    scCost.setHawbNumber(scCost.getAwbNumber().split("_")[1]);
                } else {
                    scCost.setHawbNumber(scCost.getAwbNumber());
                }
            }
            //订单号+供应商+币种
            result = baseMapper.queryScCostPageSc(page, scCost);
            result.getRecords().stream().forEach(cost -> {
                if (cost.getCostAmountWriteoff() != null) {
                    cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
                } else {
                    cost.setCostAmountWriteoff(BigDecimal.ZERO);
                    cost.setCostAmountNoWriteoff(cost.getCostAmount());
                }
                cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
                cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
                cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
                cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
                cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
                cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));
                //已对帐金额
                if (cost.getCostAmountPayment() != null) {
                    BigDecimal p = cost.getCostAmountPayment();
                    String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                    cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountPaymentStr(str);
                    if (cost.getCostExchangeRate() != null) {
                        BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                        //.setScale(2,BigDecimal.ROUND_HALF_UP)
                        String str2 = FormatUtils.formatWith2AndQFW(p2);
                        cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        cost.setAmountFunctionalPaymentStr(str2);
                    }
                } else {
                    cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                    cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                }

                ScOrder order = scOrderService.getById(cost.getOrderId());
                if (order != null) {
                    //件数
                    cost.setConfirmPieces(order.getPlanPieces());
                    //毛重
                    cost.setConfirmWeight(order.getPlanWeight());
                    //体积
                    cost.setConfirmVolume(order.getPlanVolume());
                    //计费重量
                    cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                    //查询客户
                    CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                    if (coopVo1 != null) {
                        cost.setCoopName(coopVo1.getCoop_name());
                    }
                    //责任销售
                    String salesName = order.getSalesName();
                    if (!StrUtil.isEmpty(salesName)) {
                        cost.setSalesName(salesName.split(" ")[0]);
                    }
                    //责任客服
                    String servicerName = order.getServicerName();
                    if (!StrUtil.isEmpty(servicerName)) {
                        cost.setServicerName(servicerName.split(" ")[0]);
                    }
                }
            });


        } else {
            LambdaQueryWrapper<ScCost> wrapper = Wrappers.<ScCost>lambdaQuery();
            //整理订单查询条件
            if (StrUtil.isNotBlank(scCost.getAwbNumber()) || StrUtil.isNotBlank(scCost.getOrderCode()) || scCost.getFlightDateStart() != null || scCost.getFlightDateEnd() != null || StrUtil.isNotBlank(scCost.getCustomerNumber())) {
                LambdaQueryWrapper<ScOrder> orderWrapper = Wrappers.<ScOrder>lambdaQuery();
               /*if (StrUtil.isNotBlank(scCost.getAwbNumber())) {
                    if (scCost.getAwbNumber().split("_").length == 2) {
                        orderWrapper.like(ScOrder::getMblNumber, "%" + scCost.getAwbNumber().split("_")[0] + "%").like(ScOrder::getHblNumber, "%" + scCost.getAwbNumber().split("_")[1] + "%");
                    } else {
                        orderWrapper.and(i -> i.like(ScOrder::getMblNumber, "%" + scCost.getAwbNumber() + "%").or(j -> j.like(ScOrder::getHblNumber, "%" + scCost.getAwbNumber() + "%")));
                    }

                }
                if (StrUtil.isNotBlank(scCost.getOrderCode())) {
                    orderWrapper.like(ScOrder::getOrderCode, "%" + scCost.getOrderCode() + "%");
                }*/

                if (StrUtil.isNotBlank(scCost.getOrderCode())) {
                    if (scCost.getOrderCode().split("_").length == 2) {
                        orderWrapper.like(ScOrder::getMblNumber, "%" + scCost.getOrderCode().split("_")[0] + "%").like(ScOrder::getHblNumber, "%" + scCost.getOrderCode().split("_")[1] + "%");
                    } else {
                        orderWrapper.and(i -> i.like(ScOrder::getMblNumber, "%" + scCost.getOrderCode() + "%").or(j -> j.like(ScOrder::getHblNumber, "%" + scCost.getOrderCode() + "%")).or(k -> k.like(ScOrder::getOrderCode, "%" + scCost.getOrderCode() + "%")));
                    }

                }
                if (StrUtil.isNotBlank(scCost.getCustomerNumber())) {
                    orderWrapper.like(ScOrder::getCustomerNumber, "%" + scCost.getCustomerNumber() + "%");
                }
                if (scCost.getFlightDateStart() != null) {
                    if ("SE".equals(scCost.getBusinessScope())) {
                        orderWrapper.ge(ScOrder::getExpectDeparture, scCost.getFlightDateStart());
                    } else if ("SI".equals(scCost.getBusinessScope())) {
                        orderWrapper.ge(ScOrder::getExpectArrival, scCost.getFlightDateStart());
                    }
                }
                if (scCost.getFlightDateEnd() != null) {
                    if ("SE".equals(scCost.getBusinessScope())) {
                        orderWrapper.le(ScOrder::getExpectDeparture, scCost.getFlightDateEnd());
                    } else if ("SI".equals(scCost.getBusinessScope())) {
                        orderWrapper.le(ScOrder::getExpectArrival, scCost.getFlightDateEnd());
                    }
                }
                List<Integer> orderIds = scOrderService.list(orderWrapper).stream().map(scOrder -> scOrder.getOrderId()).collect(Collectors.toList());
                if (orderIds.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                wrapper.in(ScCost::getOrderId, orderIds);
            }

            //整理付款客户查询条件
            if (StrUtil.isNotBlank(scCost.getCustomerType())) {
                List<Integer> coopIds = remoteCoopService.listByType(scCost.getCustomerType()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
                if (coopIds.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                wrapper.in(ScCost::getCustomerId, coopIds);
            }

            //整理成本查询条件
            if (StrUtil.isNotBlank(scCost.getBusinessScope())) {
                wrapper.eq(ScCost::getBusinessScope, scCost.getBusinessScope());
            }

            if (StrUtil.isNotBlank(scCost.getCustomerName())) {
                wrapper.like(ScCost::getCustomerName, "%" + scCost.getCustomerName() + "%");
            }

            if (scCost.getServiceId() != null) {
                wrapper.eq(ScCost::getServiceId, scCost.getServiceId());
            }
            wrapper.eq(ScCost::getOrgId, SecurityUtils.getUser().getOrgId());

            result = scCostService.page(page, wrapper);
            result.getRecords().stream().forEach(cost -> {
                CoopVo coopVo = remoteCoopService.viewCoop(cost.getCustomerId().toString()).getData();
                if (coopVo != null) {
                    cost.setCustomerType(coopVo.getCoop_type());
                }
                ScOrder order = scOrderService.getById(cost.getOrderId());
                if (order != null) {
                    cost.setOrderCode(order.getOrderCode());
                    cost.setAwbNumber(order.getMblNumber());
                    cost.setHawbNumber(order.getHblNumber());
                    cost.setCustomerNumber(order.getCustomerNumber());
                    if ("SE".equals(scCost.getBusinessScope())) {
                        cost.setFlightDate(order.getExpectDeparture());
                    } else if ("SI".equals(scCost.getBusinessScope())) {
                        cost.setFlightDate(order.getExpectArrival());
                    }
                    //件数
                    cost.setConfirmPieces(order.getPlanPieces());
                    //毛重
                    cost.setConfirmWeight(order.getPlanWeight());
                    //体积
                    cost.setConfirmVolume(order.getPlanVolume());
                    //计费重量
                    cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                    //查询客户
                    CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                    if (coopVo1 != null) {
                        cost.setCoopName(coopVo1.getCoop_name());
                    }
                    //责任销售
                    String salesName = order.getSalesName();
                    if (!StrUtil.isEmpty(salesName)) {
                        cost.setSalesName(salesName.split(" ")[0]);
                    }
                    //责任客服
                    String servicerName = order.getServicerName();
                    if (!StrUtil.isEmpty(servicerName)) {
                        cost.setServicerName(servicerName.split(" ")[0]);
                    }
                }
                if (cost.getCostAmountWriteoff() != null) {
                    cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
                } else {
                    cost.setCostAmountWriteoff(BigDecimal.ZERO);
                    cost.setCostAmountNoWriteoff(cost.getCostAmount());
                }
                cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
                cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
                cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
                cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
                cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
                cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));

                //已对帐金额
                HashMap map = baseMapper.queryAmountPaymentForSC(SecurityUtils.getUser().getOrgId(), cost.getCostId(), order.getOrderId(), scCost.getBusinessScope());
                if (map != null && map.containsKey("amount_payment")) {
                    BigDecimal p = new BigDecimal(map.get("amount_payment").toString());
                    String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                    cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountPaymentStr(str);
                    if (cost.getCostExchangeRate() != null) {
                        BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                        //.setScale(2,BigDecimal.ROUND_HALF_UP)
                        String str2 = FormatUtils.formatWith2AndQFW(p2);
                        cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        cost.setAmountFunctionalPaymentStr(str2);
                    }

                } else {
                    cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                    cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                }
            });
        }
        if (result.getRecords().size() != 0) {
            if (StrUtil.isNotBlank(scCost.getAwbNumber())) {
                if (scCost.getAwbNumber().split("_").length == 2) {
                    String awb = scCost.getAwbNumber().split("_")[0];
                    String hawb = scCost.getAwbNumber().split("_")[1];
                    scCost.setAwbNumber(awb);
                    scCost.setHawbNumber(hawb);
                } else {
                    scCost.setHawbNumber(null);
                }
            }
            scCost.setOrgId(SecurityUtils.getUser().getOrgId());
            List<ScCost> sumCostList = baseMapper.sumCostForSC(scCost);
            if (sumCostList != null && sumCostList.size() > 0) {

                StringBuffer costAmountStrBuffer = new StringBuffer();//成本金额(原币)
                BigDecimal costFunctionalAmount = new BigDecimal(0);//成本金额(本币)
                StringBuffer costAmountWriteoffStrBuffer = new StringBuffer();//已核销金额(原币)
                StringBuffer costAmountNoWriteoffStrBuffer = new StringBuffer();//未付款金额(原币)
                BigDecimal costFunctionalAmountNoWriteoff = new BigDecimal(0);//未付款金额(本币)
                StringBuffer amountPaymentStrBuffer = new StringBuffer();//已对账金额(原币)
                for (int i = 0; i < sumCostList.size(); i++) {
                    //拼接成本金额(原币)
                    costAmountStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmount())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmount = costFunctionalAmount.add(sumCostList.get(i).getCostFunctionalAmount());
                    //拼接已核销金额(原币)
                    costAmountWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //拼接未付款金额(原币)
                    costAmountNoWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountNoWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmountNoWriteoff = costFunctionalAmountNoWriteoff.add(sumCostList.get(i).getCostFunctionalAmountNoWriteoff());
                    //拼接已对账金额(原币)
                    if (sumCostList.get(i).getCostAmountPayment() != null) {
                        amountPaymentStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountPayment())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    }
                }
                ScCost sumCost = new ScCost();
                sumCost.setBusinessScope("合计");
                sumCost.setCostAmountStr(costAmountStrBuffer.toString());
                sumCost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(costFunctionalAmount));
                sumCost.setCostAmountWriteoffStr(costAmountWriteoffStrBuffer.toString());
                sumCost.setCostAmountNoWriteoffStr(costAmountNoWriteoffStrBuffer.toString());
                sumCost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(costFunctionalAmountNoWriteoff));
                sumCost.setAmountPaymentStr(amountPaymentStrBuffer.toString());

                //DoubleSummaryStatistics amountPaymentStr = result.getRecords().stream().mapToDouble(cost -> cost.getAmountPayment()).summaryStatistics();
                //sumCost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(new BigDecimal(amountPaymentStr.getSum() + "")));

                DoubleSummaryStatistics amountFunctionalPaymentStr = result.getRecords().stream().mapToDouble(cost -> cost.getAmountFunctionalPayment()).summaryStatistics();
                sumCost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(new BigDecimal(amountFunctionalPaymentStr.getSum() + "")));

                result.getRecords().add(sumCost);
            }
        }
        return result;
    }

    @Override
    public IPage getPageForTC(Page page, TcCost tcCost) {
        IPage<TcCost> result = null;
        //查询汇总
        if (tcCost.isGroupSum()) {
            tcCost.setOrgId(SecurityUtils.getUser().getOrgId());
            if (StrUtil.isNotBlank(tcCost.getAwbNumber())) {
                tcCost.setRwbNumber(tcCost.getAwbNumber());
            }
            //订单号+供应商+币种
            result = baseMapper.queryTcCostPageTC(page, tcCost);
            result.getRecords().stream().forEach(cost -> {
                if (cost.getCostAmountWriteoff() != null) {
                    cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
                } else {
                    cost.setCostAmountWriteoff(BigDecimal.ZERO);
                    cost.setCostAmountNoWriteoff(cost.getCostAmount());
                }
                cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
                cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
                cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
                cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
                cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
                cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));
                //已对帐金额
                if (cost.getCostAmountPayment() != null) {
                    BigDecimal p = cost.getCostAmountPayment();
                    String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                    cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountPaymentStr(str);
                    if (cost.getCostExchangeRate() != null) {
                        BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                        //.setScale(2,BigDecimal.ROUND_HALF_UP)
                        String str2 = FormatUtils.formatWith2AndQFW(p2);
                        cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        cost.setAmountFunctionalPaymentStr(str2);
                    }
                } else {
                    cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                    cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                }

                TcOrder order = tcOrderService.getById(cost.getOrderId());
                if (order != null) {
                    //件数
                    cost.setConfirmPieces(order.getPlanPieces());
                    //毛重
                    cost.setConfirmWeight(order.getPlanWeight());
                    //体积
                    cost.setConfirmVolume(order.getPlanVolume());
                    //计费重量
                    cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                    //查询客户
                    CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                    if (coopVo1 != null) {
                        cost.setCoopName(coopVo1.getCoop_name());
                    }
                    //责任销售
                    String salesName = order.getSalesName();
                    if (!StrUtil.isEmpty(salesName)) {
                        cost.setSalesName(salesName.split(" ")[0]);
                    }
                    //责任客服
                    String servicerName = order.getServicerName();
                    if (!StrUtil.isEmpty(servicerName)) {
                        cost.setServicerName(servicerName.split(" ")[0]);
                    }
                }
            });


        } else {
            LambdaQueryWrapper<TcCost> wrapper = Wrappers.<TcCost>lambdaQuery();
            //整理订单查询条件
            if (StrUtil.isNotBlank(tcCost.getAwbNumber()) || StrUtil.isNotBlank(tcCost.getOrderCode()) || tcCost.getFlightDateStart() != null || tcCost.getFlightDateEnd() != null || StrUtil.isNotBlank(tcCost.getCustomerNumber())) {
                LambdaQueryWrapper<TcOrder> orderWrapper = Wrappers.<TcOrder>lambdaQuery();
                /*if (StrUtil.isNotBlank(tcCost.getAwbNumber())) {
                   orderWrapper.like(TcOrder::getRwbNumber,tcCost.getAwbNumber());
                }
                if (StrUtil.isNotBlank(tcCost.getOrderCode())) {
                    orderWrapper.like(TcOrder::getOrderCode,tcCost.getOrderCode());
                }*/

                if (StrUtil.isNotBlank(tcCost.getOrderCode())) {
                    orderWrapper.and(i -> i.like(TcOrder::getRwbNumber, "%" + tcCost.getOrderCode() + "%").or(k -> k.like(TcOrder::getOrderCode, "%" + tcCost.getOrderCode() + "%")));
                }
                if (StrUtil.isNotBlank(tcCost.getCustomerNumber())) {
                    orderWrapper.like(TcOrder::getCustomerNumber, "%" + tcCost.getCustomerNumber() + "%");
                }
                if (tcCost.getFlightDateStart() != null) {
                    if ("TE".equals(tcCost.getBusinessScope())) {
                        orderWrapper.ge(TcOrder::getExpectDeparture, tcCost.getFlightDateStart());
                    } else if ("TI".equals(tcCost.getBusinessScope())) {
                        orderWrapper.ge(TcOrder::getExpectArrival, tcCost.getFlightDateStart());
                    }
                }
                if (tcCost.getFlightDateEnd() != null) {
                    if ("TE".equals(tcCost.getBusinessScope())) {
                        orderWrapper.le(TcOrder::getExpectDeparture, tcCost.getFlightDateEnd());
                    } else if ("TI".equals(tcCost.getBusinessScope())) {
                        orderWrapper.le(TcOrder::getExpectArrival, tcCost.getFlightDateEnd());
                    }
                }
                List<Integer> orderIds = tcOrderService.list(orderWrapper).stream().map(tcOrder -> tcOrder.getOrderId()).collect(Collectors.toList());
                if (orderIds.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                wrapper.in(TcCost::getOrderId, orderIds);
            }

            //整理付款客户查询条件
            if (StrUtil.isNotBlank(tcCost.getCustomerType())) {
                List<Integer> coopIds = remoteCoopService.listByType(tcCost.getCustomerType()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
                if (coopIds.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                wrapper.in(TcCost::getCustomerId, coopIds);
            }

            //整理成本查询条件
            if (StrUtil.isNotBlank(tcCost.getBusinessScope())) {
                wrapper.eq(TcCost::getBusinessScope, tcCost.getBusinessScope());
            }

            if (StrUtil.isNotBlank(tcCost.getCustomerName())) {
                wrapper.like(TcCost::getCustomerName, tcCost.getCustomerName());
            }

            if (tcCost.getServiceId() != null) {
                wrapper.eq(TcCost::getServiceId, tcCost.getServiceId());
            }
            wrapper.eq(TcCost::getOrgId, SecurityUtils.getUser().getOrgId());

            result = tcCostService.page(page, wrapper);
            result.getRecords().stream().forEach(cost -> {
                CoopVo coopVo = remoteCoopService.viewCoop(cost.getCustomerId().toString()).getData();
                if (coopVo != null) {
                    cost.setCustomerType(coopVo.getCoop_type());
                }
                TcOrder order = tcOrderService.getById(cost.getOrderId());
                if (order != null) {
                    cost.setOrderCode(order.getOrderCode());
                    cost.setAwbNumber(order.getRwbNumber());
                    cost.setCustomerNumber(order.getCustomerNumber());
                    if ("TE".equals(tcCost.getBusinessScope())) {
                        cost.setFlightDate(order.getExpectDeparture());
                    } else if ("TI".equals(tcCost.getBusinessScope())) {
                        cost.setFlightDate(order.getExpectArrival());
                    }
                    //件数
                    cost.setConfirmPieces(order.getPlanPieces());
                    //毛重
                    cost.setConfirmWeight(order.getPlanWeight());
                    //体积
                    cost.setConfirmVolume(order.getPlanVolume());
                    //计费重量
                    cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                    //查询客户
                    CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                    if (coopVo1 != null) {
                        cost.setCoopName(coopVo1.getCoop_name());
                    }
                    //责任销售
                    String salesName = order.getSalesName();
                    if (!StrUtil.isEmpty(salesName)) {
                        cost.setSalesName(salesName.split(" ")[0]);
                    }
                    //责任客服
                    String servicerName = order.getServicerName();
                    if (!StrUtil.isEmpty(servicerName)) {
                        cost.setServicerName(servicerName.split(" ")[0]);
                    }
                }
                if (cost.getCostAmountWriteoff() != null) {
                    cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
                } else {
                    cost.setCostAmountWriteoff(BigDecimal.ZERO);
                    cost.setCostAmountNoWriteoff(cost.getCostAmount());
                }
                cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
                cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
                cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
                cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
                cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
                cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));

                //已对帐金额
                HashMap map = baseMapper.queryAmountPaymentForAF(SecurityUtils.getUser().getOrgId(), cost.getCostId(), order.getOrderId(), tcCost.getBusinessScope());
                if (map != null && map.containsKey("amount_payment")) {
                    BigDecimal p = new BigDecimal(map.get("amount_payment").toString());
                    String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                    cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountPaymentStr(str);
                    if (cost.getCostExchangeRate() != null) {
                        BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                        //.setScale(2,BigDecimal.ROUND_HALF_UP)
                        String str2 = FormatUtils.formatWith2AndQFW(p2);
                        cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        cost.setAmountFunctionalPaymentStr(str2);
                    }

                } else {
                    cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                    cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                }
            });
        }
        if (result.getRecords().size() != 0) {
            if (StrUtil.isNotBlank(tcCost.getAwbNumber())) {
                tcCost.setRwbNumber(tcCost.getAwbNumber());
            }
            tcCost.setOrgId(SecurityUtils.getUser().getOrgId());
            List<TcCost> sumCostList = baseMapper.sumCostForTC(tcCost);
            if (sumCostList != null && sumCostList.size() > 0) {

                StringBuffer costAmountStrBuffer = new StringBuffer();//成本金额(原币)
                BigDecimal costFunctionalAmount = new BigDecimal(0);//成本金额(本币)
                StringBuffer costAmountWriteoffStrBuffer = new StringBuffer();//已核销金额(原币)
                StringBuffer costAmountNoWriteoffStrBuffer = new StringBuffer();//未付款金额(原币)
                BigDecimal costFunctionalAmountNoWriteoff = new BigDecimal(0);//未付款金额(本币)
                StringBuffer amountPaymentStrBuffer = new StringBuffer();//已对账金额(原币)
                for (int i = 0; i < sumCostList.size(); i++) {
                    //拼接成本金额(原币)
                    costAmountStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmount())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmount = costFunctionalAmount.add(sumCostList.get(i).getCostFunctionalAmount());
                    //拼接已核销金额(原币)
                    costAmountWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //拼接未付款金额(原币)
                    costAmountNoWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountNoWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmountNoWriteoff = costFunctionalAmountNoWriteoff.add(sumCostList.get(i).getCostFunctionalAmountNoWriteoff());
                    //拼接已对账金额(原币)
                    if (sumCostList.get(i).getCostAmountPayment() != null) {
                        amountPaymentStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountPayment())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    } else {
                        amountPaymentStrBuffer.append("0.00").append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    }
                }
                TcCost sumCost = new TcCost();
                sumCost.setBusinessScope("合计");
                sumCost.setCostAmountStr(costAmountStrBuffer.toString());
                sumCost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(costFunctionalAmount));
                sumCost.setCostAmountWriteoffStr(costAmountWriteoffStrBuffer.toString());
                sumCost.setCostAmountNoWriteoffStr(costAmountNoWriteoffStrBuffer.toString());
                sumCost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(costFunctionalAmountNoWriteoff));
                sumCost.setAmountPaymentStr(amountPaymentStrBuffer.toString());

                //DoubleSummaryStatistics amountPaymentStr = result.getRecords().stream().mapToDouble(cost -> cost.getAmountPayment()).summaryStatistics();
                //sumCost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(new BigDecimal(amountPaymentStr.getSum() + "")));

                DoubleSummaryStatistics amountFunctionalPaymentStr = result.getRecords().stream().mapToDouble(cost -> cost.getAmountFunctionalPayment()).summaryStatistics();
                sumCost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(new BigDecimal(amountFunctionalPaymentStr.getSum() + "")));

                result.getRecords().add(sumCost);
            }
        }
        return result;
    }

    @Override
    public IPage getPageForLC(Page page, LcCost lcCost) {
        IPage<LcCost> result = null;
        //查询汇总
        if (lcCost.isGroupSum()) {
            lcCost.setOrgId(SecurityUtils.getUser().getOrgId());
            if (StrUtil.isNotBlank(lcCost.getAwbNumber())) {
                lcCost.setCustomerNumber(lcCost.getAwbNumber());
            }
            //订单号+供应商+币种
            result = baseMapper.queryTcCostPageLC(page, lcCost);
            result.getRecords().stream().forEach(cost -> {
                if (cost.getCostAmountWriteoff() != null) {
                    cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
                } else {
                    cost.setCostAmountWriteoff(BigDecimal.ZERO);
                    cost.setCostAmountNoWriteoff(cost.getCostAmount());
                }
                cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
                cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
                cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
                cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
                cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
                cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));
                //已对帐金额
                if (cost.getCostAmountPayment() != null) {
                    BigDecimal p = cost.getCostAmountPayment();
                    String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                    cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountPaymentStr(str);
                    if (cost.getCostExchangeRate() != null) {
                        BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                        //.setScale(2,BigDecimal.ROUND_HALF_UP)
                        String str2 = FormatUtils.formatWith2AndQFW(p2);
                        cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        cost.setAmountFunctionalPaymentStr(str2);
                    }
                } else {
                    cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                    cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                }

                LcOrder order = lcOrderService.getById(cost.getOrderId());
                if (order != null) {
                    //件数
                    cost.setConfirmPieces(order.getConfirmPieces() != null ? order.getConfirmPieces() : order.getPlanPieces());
                    //毛重
                    cost.setConfirmWeight(order.getConfirmWeight() != null ? order.getConfirmWeight() : order.getPlanWeight());
                    //体积
                    cost.setConfirmVolume(order.getConfirmVolume() != null ? order.getConfirmVolume() : order.getPlanVolume());
                    //计费重量
                    cost.setConfirmChargeWeight(order.getConfirmChargeWeight() != null ? order.getConfirmChargeWeight() : order.getPlanChargeWeight());
                    //查询客户
                    CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                    if (coopVo1 != null) {
                        cost.setCoopName(coopVo1.getCoop_name());
                    }
                    //责任销售
                    String salesName = order.getSalesName();
                    if (!StrUtil.isEmpty(salesName)) {
                        cost.setSalesName(salesName.split(" ")[0]);
                    }
                    //责任客服
                    String servicerName = order.getServicerName();
                    if (!StrUtil.isEmpty(servicerName)) {
                        cost.setServicerName(servicerName.split(" ")[0]);
                    }
                }
            });
        } else {
            LambdaQueryWrapper<LcCost> wrapper = Wrappers.<LcCost>lambdaQuery();
            //整理订单查询条件
            if (StrUtil.isNotBlank(lcCost.getAwbNumber()) || StrUtil.isNotBlank(lcCost.getOrderCode()) || lcCost.getFlightDateStart() != null || lcCost.getFlightDateEnd() != null || StrUtil.isNotBlank(lcCost.getCustomerNumber())) {
                LambdaQueryWrapper<LcOrder> orderWrapper = Wrappers.<LcOrder>lambdaQuery();
                if (StrUtil.isNotBlank(lcCost.getCustomerNumber())) {
                    orderWrapper.like(LcOrder::getCustomerNumber, lcCost.getCustomerNumber());
                }
                if (StrUtil.isNotBlank(lcCost.getOrderCode())) {
                    orderWrapper.like(LcOrder::getOrderCode, lcCost.getOrderCode());
                }
                if (lcCost.getFlightDateStart() != null) {
                    orderWrapper.ge(LcOrder::getDrivingTime, lcCost.getFlightDateStart());
                }
                if (lcCost.getFlightDateEnd() != null) {
                    orderWrapper.le(LcOrder::getDrivingTime, lcCost.getFlightDateEnd());
                }
                List<Integer> orderIds = lcOrderService.list(orderWrapper).stream().map(lcOrder -> lcOrder.getOrderId()).collect(Collectors.toList());
                if (orderIds.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                wrapper.in(LcCost::getOrderId, orderIds);
            }

            //整理付款客户查询条件
            if (StrUtil.isNotBlank(lcCost.getCustomerType())) {
                List<Integer> coopIds = remoteCoopService.listByType(lcCost.getCustomerType()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
                if (coopIds.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                wrapper.in(LcCost::getCustomerId, coopIds);
            }

            //整理成本查询条件
            if (StrUtil.isNotBlank(lcCost.getBusinessScope())) {
                wrapper.eq(LcCost::getBusinessScope, lcCost.getBusinessScope());
            }

            if (StrUtil.isNotBlank(lcCost.getCustomerName())) {
                wrapper.like(LcCost::getCustomerName, lcCost.getCustomerName());
            }

            if (lcCost.getServiceId() != null) {
                wrapper.eq(LcCost::getServiceId, lcCost.getServiceId());
            }
            wrapper.eq(LcCost::getOrgId, SecurityUtils.getUser().getOrgId());

            result = lcCostService.page(page, wrapper);
            result.getRecords().stream().forEach(cost -> {
                CoopVo coopVo = remoteCoopService.viewCoop(cost.getCustomerId().toString()).getData();
                if (coopVo != null) {
                    cost.setCustomerType(coopVo.getCoop_type());
                }
                LcOrder order = lcOrderService.getById(cost.getOrderId());
                if (order != null) {
                    cost.setOrderCode(order.getOrderCode());
                    cost.setAwbNumber(order.getCustomerNumber());
                    cost.setCustomerNumber(order.getCustomerNumber());
                    cost.setFlightDate(order.getDrivingTime().toLocalDate());
                    //件数
                    cost.setConfirmPieces(order.getConfirmPieces() != null ? order.getConfirmPieces() : order.getPlanPieces());
                    //毛重
                    cost.setConfirmWeight(order.getConfirmWeight() != null ? order.getConfirmWeight() : order.getPlanWeight());
                    //体积
                    cost.setConfirmVolume(order.getConfirmVolume() != null ? order.getConfirmVolume() : order.getPlanVolume());
                    //计费重量
                    cost.setConfirmChargeWeight(order.getConfirmChargeWeight() != null ? order.getConfirmChargeWeight() : order.getPlanChargeWeight());
                    //查询客户
                    CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                    if (coopVo1 != null) {
                        cost.setCoopName(coopVo1.getCoop_name());
                    }
                    //责任销售
                    String salesName = order.getSalesName();
                    if (!StrUtil.isEmpty(salesName)) {
                        cost.setSalesName(salesName.split(" ")[0]);
                    }
                    //责任客服
                    String servicerName = order.getServicerName();
                    if (!StrUtil.isEmpty(servicerName)) {
                        cost.setServicerName(servicerName.split(" ")[0]);
                    }
                }
                if (cost.getCostAmountWriteoff() != null) {
                    cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
                } else {
                    cost.setCostAmountWriteoff(BigDecimal.ZERO);
                    cost.setCostAmountNoWriteoff(cost.getCostAmount());
                }
                cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
                cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
                cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
                cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
                cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
                cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));

                //已对帐金额
                HashMap map = baseMapper.queryAmountPaymentForAF(SecurityUtils.getUser().getOrgId(), cost.getCostId(), order.getOrderId(), lcCost.getBusinessScope());
                if (map != null && map.containsKey("amount_payment")) {
                    BigDecimal p = new BigDecimal(map.get("amount_payment").toString());
                    String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                    cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountPaymentStr(str);
                    if (cost.getCostExchangeRate() != null) {
                        BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                        //.setScale(2,BigDecimal.ROUND_HALF_UP)
                        String str2 = FormatUtils.formatWith2AndQFW(p2);
                        cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        cost.setAmountFunctionalPaymentStr(str2);
                    }

                } else {
                    cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                    cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                }
            });
        }
        if (result.getRecords().size() != 0) {
            if (StrUtil.isNotBlank(lcCost.getAwbNumber())) {
                lcCost.setCustomerNumber(lcCost.getAwbNumber());
            }
            lcCost.setOrgId(SecurityUtils.getUser().getOrgId());
            List<LcCost> sumCostList = baseMapper.sumCostForLC(lcCost);
            if (sumCostList != null && sumCostList.size() > 0) {

                StringBuffer costAmountStrBuffer = new StringBuffer();//成本金额(原币)
                BigDecimal costFunctionalAmount = new BigDecimal(0);//成本金额(本币)
                StringBuffer costAmountWriteoffStrBuffer = new StringBuffer();//已核销金额(原币)
                StringBuffer costAmountNoWriteoffStrBuffer = new StringBuffer();//未付款金额(原币)
                BigDecimal costFunctionalAmountNoWriteoff = new BigDecimal(0);//未付款金额(本币)
                StringBuffer amountPaymentStrBuffer = new StringBuffer();//已对账金额(原币)
                for (int i = 0; i < sumCostList.size(); i++) {
                    //拼接成本金额(原币)
                    costAmountStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmount())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmount = costFunctionalAmount.add(sumCostList.get(i).getCostFunctionalAmount());
                    //拼接已核销金额(原币)
                    costAmountWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //拼接未付款金额(原币)
                    costAmountNoWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountNoWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmountNoWriteoff = costFunctionalAmountNoWriteoff.add(sumCostList.get(i).getCostFunctionalAmountNoWriteoff());
                    //拼接已对账金额(原币)
                    if (sumCostList.get(i).getCostAmountPayment() != null) {
                        amountPaymentStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountPayment())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    } else {
                        amountPaymentStrBuffer.append("0.00").append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    }
                }
                LcCost sumCost = new LcCost();
                sumCost.setBusinessScope("合计");
                sumCost.setCostAmountStr(costAmountStrBuffer.toString());
                sumCost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(costFunctionalAmount));
                sumCost.setCostAmountWriteoffStr(costAmountWriteoffStrBuffer.toString());
                sumCost.setCostAmountNoWriteoffStr(costAmountNoWriteoffStrBuffer.toString());
                sumCost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(costFunctionalAmountNoWriteoff));
                sumCost.setAmountPaymentStr(amountPaymentStrBuffer.toString());

                //DoubleSummaryStatistics amountPaymentStr = result.getRecords().stream().mapToDouble(cost -> cost.getAmountPayment()).summaryStatistics();
                //sumCost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(new BigDecimal(amountPaymentStr.getSum() + "")));

                DoubleSummaryStatistics amountFunctionalPaymentStr = result.getRecords().stream().mapToDouble(cost -> cost.getAmountFunctionalPayment()).summaryStatistics();
                sumCost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(new BigDecimal(amountFunctionalPaymentStr.getSum() + "")));

                result.getRecords().add(sumCost);
            }
        }
        return result;
    }

    @Override
    public IPage getPageForIO(Page page, IoCost ioCost) {
        IPage<IoCost> result = null;
        //查询汇总
        if (ioCost.isGroupSum()) {
            ioCost.setOrgId(SecurityUtils.getUser().getOrgId());
            if (StrUtil.isNotBlank(ioCost.getAwbNumber())) {
                ioCost.setCustomerNumber(ioCost.getAwbNumber());
            }
            //订单号+供应商+币种
            result = baseMapper.queryIOCostPage(page, ioCost);
            result.getRecords().stream().forEach(cost -> {
                if (cost.getCostAmountWriteoff() != null) {
                    cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
                } else {
                    cost.setCostAmountWriteoff(BigDecimal.ZERO);
                    cost.setCostAmountNoWriteoff(cost.getCostAmount());
                }
                cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
                cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
                cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
                cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
                cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
                cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));
                //已对帐金额
                if (cost.getCostAmountPayment() != null) {
                    BigDecimal p = cost.getCostAmountPayment();
                    String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                    cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountPaymentStr(str);
                    if (cost.getCostExchangeRate() != null) {
                        BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                        //.setScale(2,BigDecimal.ROUND_HALF_UP)
                        String str2 = FormatUtils.formatWith2AndQFW(p2);
                        cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        cost.setAmountFunctionalPaymentStr(str2);
                    }
                } else {
                    cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                    cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                }

                IoOrder order = ioOrderService.getById(cost.getOrderId());
                if (order != null) {
                    //件数
                    cost.setConfirmPieces(order.getPlanPieces());
                    //毛重
                    cost.setConfirmWeight(order.getPlanWeight());
                    //体积
                    cost.setConfirmVolume(order.getPlanVolume());
                    //计费重量
                    cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                    //查询客户
                    CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                    if (coopVo1 != null) {
                        cost.setCoopName(coopVo1.getCoop_name());
                    }
                    //责任销售
                    String salesName = order.getSalesName();
                    if (!StrUtil.isEmpty(salesName)) {
                        cost.setSalesName(salesName.split(" ")[0]);
                    }
                    //责任客服
                    String servicerName = order.getServicerName();
                    if (!StrUtil.isEmpty(servicerName)) {
                        cost.setServicerName(servicerName.split(" ")[0]);
                    }
                }
            });
        } else {
            LambdaQueryWrapper<IoCost> wrapper = Wrappers.<IoCost>lambdaQuery();
            //整理订单查询条件
            if (StrUtil.isNotBlank(ioCost.getAwbNumber()) || StrUtil.isNotBlank(ioCost.getOrderCode()) || ioCost.getFlightDateStart() != null || ioCost.getFlightDateEnd() != null || StrUtil.isNotBlank(ioCost.getCustomerNumber())) {
                LambdaQueryWrapper<IoOrder> orderWrapper = Wrappers.<IoOrder>lambdaQuery();
                if (StrUtil.isNotBlank(ioCost.getCustomerNumber())) {
                    orderWrapper.like(IoOrder::getCustomerNumber, ioCost.getCustomerNumber());
                }
                if (StrUtil.isNotBlank(ioCost.getOrderCode())) {
                    orderWrapper.like(IoOrder::getOrderCode, ioCost.getOrderCode());
                }
                if (ioCost.getFlightDateStart() != null) {
                    orderWrapper.ge(IoOrder::getBusinessDate, ioCost.getFlightDateStart());
                }
                if (ioCost.getFlightDateEnd() != null) {
                    orderWrapper.le(IoOrder::getBusinessDate, ioCost.getFlightDateEnd());
                }
                List<Integer> orderIds = ioOrderService.list(orderWrapper).stream().map(lcOrder -> lcOrder.getOrderId()).collect(Collectors.toList());
                if (orderIds.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                wrapper.in(IoCost::getOrderId, orderIds);
            }

            //整理付款客户查询条件
            if (StrUtil.isNotBlank(ioCost.getCustomerType())) {
                List<Integer> coopIds = remoteCoopService.listByType(ioCost.getCustomerType()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
                if (coopIds.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                wrapper.in(IoCost::getCustomerId, coopIds);
            }

            //整理成本查询条件
            if (StrUtil.isNotBlank(ioCost.getBusinessScope())) {
                wrapper.eq(IoCost::getBusinessScope, ioCost.getBusinessScope());
            }

            if (StrUtil.isNotBlank(ioCost.getCustomerName())) {
                wrapper.like(IoCost::getCustomerName, ioCost.getCustomerName());
            }

            if (ioCost.getServiceId() != null) {
                wrapper.eq(IoCost::getServiceId, ioCost.getServiceId());
            }
            wrapper.eq(IoCost::getOrgId, SecurityUtils.getUser().getOrgId());

            result = ioCostService.page(page, wrapper);
            result.getRecords().stream().forEach(cost -> {
                CoopVo coopVo = remoteCoopService.viewCoop(cost.getCustomerId().toString()).getData();
                if (coopVo != null) {
                    cost.setCustomerType(coopVo.getCoop_type());
                }
                IoOrder order = ioOrderService.getById(cost.getOrderId());
                if (order != null) {
                    cost.setOrderCode(order.getOrderCode());
                    cost.setAwbNumber(order.getCustomerNumber());
                    cost.setCustomerNumber(order.getCustomerNumber());
                    cost.setFlightDate(order.getBusinessDate());
                    //件数
                    cost.setConfirmPieces(order.getPlanPieces());
                    //毛重
                    cost.setConfirmWeight(order.getPlanWeight());
                    //体积
                    cost.setConfirmVolume(order.getPlanVolume());
                    //计费重量
                    cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                    //查询客户
                    CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                    if (coopVo1 != null) {
                        cost.setCoopName(coopVo1.getCoop_name());
                    }
                    //责任销售
                    String salesName = order.getSalesName();
                    if (!StrUtil.isEmpty(salesName)) {
                        cost.setSalesName(salesName.split(" ")[0]);
                    }
                    //责任客服
                    String servicerName = order.getServicerName();
                    if (!StrUtil.isEmpty(servicerName)) {
                        cost.setServicerName(servicerName.split(" ")[0]);
                    }
                }
                if (cost.getCostAmountWriteoff() != null) {
                    cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
                } else {
                    cost.setCostAmountWriteoff(BigDecimal.ZERO);
                    cost.setCostAmountNoWriteoff(cost.getCostAmount());
                }
                cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
                cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
                cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
                cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
                cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
                cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));

                //已对帐金额
                HashMap map = baseMapper.queryAmountPaymentForAF(SecurityUtils.getUser().getOrgId(), cost.getCostId(), order.getOrderId(), ioCost.getBusinessScope());
                if (map != null && map.containsKey("amount_payment")) {
                    BigDecimal p = new BigDecimal(map.get("amount_payment").toString());
                    String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                    cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountPaymentStr(str);
                    if (cost.getCostExchangeRate() != null) {
                        BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                        String str2 = FormatUtils.formatWith2AndQFW(p2);
                        cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        cost.setAmountFunctionalPaymentStr(str2);
                    }

                } else {
                    cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                    cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                }
            });
        }
        if (result.getRecords().size() != 0) {
            if (StrUtil.isNotBlank(ioCost.getAwbNumber())) {
                ioCost.setCustomerNumber(ioCost.getAwbNumber());
            }
            ioCost.setOrgId(SecurityUtils.getUser().getOrgId());
            List<IoCost> sumCostList = baseMapper.sumCostForIO(ioCost);
            if (sumCostList != null && sumCostList.size() > 0) {

                StringBuffer costAmountStrBuffer = new StringBuffer();//成本金额(原币)
                BigDecimal costFunctionalAmount = new BigDecimal(0);//成本金额(本币)
                StringBuffer costAmountWriteoffStrBuffer = new StringBuffer();//已核销金额(原币)
                StringBuffer costAmountNoWriteoffStrBuffer = new StringBuffer();//未付款金额(原币)
                BigDecimal costFunctionalAmountNoWriteoff = new BigDecimal(0);//未付款金额(本币)
                StringBuffer amountPaymentStrBuffer = new StringBuffer();//已对账金额(原币)
                for (int i = 0; i < sumCostList.size(); i++) {
                    //拼接成本金额(原币)
                    costAmountStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmount())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmount = costFunctionalAmount.add(sumCostList.get(i).getCostFunctionalAmount());
                    //拼接已核销金额(原币)
                    costAmountWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //拼接未付款金额(原币)
                    costAmountNoWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountNoWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmountNoWriteoff = costFunctionalAmountNoWriteoff.add(sumCostList.get(i).getCostFunctionalAmountNoWriteoff());
                    //拼接已对账金额(原币)
                    if (sumCostList.get(i).getCostAmountPayment() != null) {
                        amountPaymentStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountPayment())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    } else {
                        amountPaymentStrBuffer.append("0.00").append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    }
                }
                IoCost sumCost = new IoCost();
                sumCost.setBusinessScope("合计");
                sumCost.setCostAmountStr(costAmountStrBuffer.toString());
                sumCost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(costFunctionalAmount));
                sumCost.setCostAmountWriteoffStr(costAmountWriteoffStrBuffer.toString());
                sumCost.setCostAmountNoWriteoffStr(costAmountNoWriteoffStrBuffer.toString());
                sumCost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(costFunctionalAmountNoWriteoff));
                sumCost.setAmountPaymentStr(amountPaymentStrBuffer.toString());

                //DoubleSummaryStatistics amountPaymentStr = result.getRecords().stream().mapToDouble(cost -> cost.getAmountPayment()).summaryStatistics();
                //sumCost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(new BigDecimal(amountPaymentStr.getSum() + "")));

                DoubleSummaryStatistics amountFunctionalPaymentStr = result.getRecords().stream().mapToDouble(cost -> cost.getAmountFunctionalPayment()).summaryStatistics();
                sumCost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(new BigDecimal(amountFunctionalPaymentStr.getSum() + "")));

                result.getRecords().add(sumCost);
            }
        }
        return result;
    }

    private List<AfCost> findCostListForAF(AfCost afCost) {
        LambdaQueryWrapper<AfCost> wrapper = Wrappers.<AfCost>lambdaQuery();
        //整理订单查询条件
        if (StrUtil.isNotBlank(afCost.getAwbNumber()) || StrUtil.isNotBlank(afCost.getOrderCode()) || afCost.getFlightDateStart() != null || afCost.getFlightDateEnd() != null || StrUtil.isNotBlank(afCost.getCustomerNumber())) {
            LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
            if (StrUtil.isNotBlank(afCost.getOrderCode())) {
                if (afCost.getOrderCode().split("_").length == 2) {
                    orderWrapper.like(AfOrder::getAwbNumber, "%" + afCost.getOrderCode().split("_")[0] + "%").like(AfOrder::getHawbNumber, "%" + afCost.getOrderCode().split("_")[1] + "%");
                } else {
                    orderWrapper.and(i -> i.like(AfOrder::getAwbNumber, "%" + afCost.getOrderCode() + "%").or(j -> j.like(AfOrder::getHawbNumber, "%" + afCost.getOrderCode() + "%")).or(k -> k.like(AfOrder::getOrderCode, "%" + afCost.getOrderCode() + "%")));
                }

            }
            if (StrUtil.isNotBlank(afCost.getCustomerNumber())) {
                orderWrapper.like(AfOrder::getCustomerNumber, "%" + afCost.getCustomerNumber() + "%");
            }
            if (afCost.getFlightDateStart() != null) {
                if ("AE".equals(afCost.getBusinessScope())) {
                    orderWrapper.ge(AfOrder::getExpectDeparture, afCost.getFlightDateStart());
                } else if ("AI".equals(afCost.getBusinessScope())) {
                    orderWrapper.ge(AfOrder::getExpectArrival, afCost.getFlightDateStart());
                }
            }
            if (afCost.getFlightDateEnd() != null) {
                if ("AE".equals(afCost.getBusinessScope())) {
                    orderWrapper.le(AfOrder::getExpectDeparture, afCost.getFlightDateEnd());
                } else if ("AI".equals(afCost.getBusinessScope())) {
                    orderWrapper.le(AfOrder::getExpectArrival, afCost.getFlightDateEnd());
                }
            }
            List<Integer> orderIds = afOrderService.list(orderWrapper).stream().map(afOrder -> afOrder.getOrderId()).collect(Collectors.toList());
            if (orderIds.size() == 0) {
                return new ArrayList();
            }
            wrapper.in(AfCost::getOrderId, orderIds);
        }

        //整理付款客户查询条件
        if (StrUtil.isNotBlank(afCost.getCustomerType())) {
            List<Integer> coopIds = remoteCoopService.listByType(afCost.getCustomerType()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
            if (coopIds.size() == 0) {
                return new ArrayList();
            }
            wrapper.in(AfCost::getCustomerId, coopIds);
        }

        //整理成本查询条件
        if (StrUtil.isNotBlank(afCost.getBusinessScope())) {
            wrapper.eq(AfCost::getBusinessScope, afCost.getBusinessScope());
        }

        if (StrUtil.isNotBlank(afCost.getCustomerName())) {
            wrapper.like(AfCost::getCustomerName, "%" + afCost.getCustomerName() + "%");
        }

        if (afCost.getServiceId() != null) {
            wrapper.eq(AfCost::getServiceId, afCost.getServiceId());
        }
        wrapper.eq(AfCost::getOrgId, SecurityUtils.getUser().getOrgId());
        List<AfCost> list = list(wrapper);
        list.stream().forEach(cost -> {
            CoopVo coopVo = remoteCoopService.viewCoop(cost.getCustomerId().toString()).getData();
            if (coopVo != null) {
                cost.setCustomerType(coopVo.getCoop_type());
            }
            AfOrder order = afOrderService.getById(cost.getOrderId());
            if (order != null) {
                cost.setOrderCode(order.getOrderCode());
                cost.setAwbNumber(order.getAwbNumber());
                cost.setCustomerNumber(order.getCustomerNumber());
                if ("AE".equals(afCost.getBusinessScope())) {
                    cost.setFlightDate(order.getExpectDeparture());
                    //件数
                    if (!"".equals(order.getConfirmPieces()) && order.getConfirmPieces() != null) {
                        cost.setConfirmPieces(order.getConfirmPieces());
                    } else {
                        cost.setConfirmPieces(order.getPlanPieces());
                    }
                    //毛重
                    if (!"".equals(order.getConfirmWeight()) && order.getConfirmWeight() != null) {
                        cost.setConfirmWeight(order.getConfirmWeight());
                    } else {
                        cost.setConfirmWeight(order.getPlanWeight());
                    }
                    //体积
                    if (!"".equals(order.getConfirmVolume()) && order.getConfirmVolume() != null) {
                        cost.setConfirmVolume(order.getConfirmVolume());
                    } else {
                        cost.setConfirmVolume(order.getPlanVolume());
                    }
                    //计费重量
                    if (!"".equals(order.getConfirmChargeWeight()) && order.getConfirmChargeWeight() != null) {
                        cost.setConfirmChargeWeight(order.getConfirmChargeWeight());
                    } else {
                        cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                    }
                } else if ("AI".equals(afCost.getBusinessScope())) {
                    cost.setFlightDate(order.getExpectArrival());
                    //件数
                    cost.setConfirmPieces(order.getPlanPieces());
                    //毛重
                    cost.setConfirmWeight(order.getPlanWeight());
                    //体积
                    cost.setConfirmVolume(order.getPlanVolume());
                    //计费重量
                    cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                }
                //查询客户
                CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                if (coopVo1 != null) {
                    cost.setCoopName(coopVo1.getCoop_name());
                }
                //责任销售
                String salesName = order.getSalesName();
                if (!StrUtil.isEmpty(salesName)) {
                    cost.setSalesName(salesName.split(" ")[0]);
                }
                //责任客服
                String servicerName = order.getServicerName();
                if (!StrUtil.isEmpty(servicerName)) {
                    cost.setServicerName(servicerName.split(" ")[0]);
                }
            }
            if (cost.getCostAmountWriteoff() != null) {
                cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
            } else {
                cost.setCostAmountWriteoff(BigDecimal.ZERO);
                cost.setCostAmountNoWriteoff(cost.getCostAmount());
            }
            cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
            cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
            cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
            cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
            //cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff().multiply(cost.getCostExchangeRate())));
            cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
            cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));
            //已对帐金额
            if (cost.getCostAmountPayment() != null) {
                BigDecimal p = cost.getCostAmountPayment();
                String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                cost.setAmountPaymentStr(str);
                if (cost.getCostExchangeRate() != null) {
                    BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                    String str2 = FormatUtils.formatWith2AndQFW(p2);
                    cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountFunctionalPaymentStr(str2);
                }
            } else {
                cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
            }
        });
        return list;
    }

    private List<LcCost> findCostListForLC(LcCost lcCost) {
        LambdaQueryWrapper<LcCost> wrapper = Wrappers.<LcCost>lambdaQuery();
        //整理订单查询条件
        if (StrUtil.isNotBlank(lcCost.getAwbNumber()) || StrUtil.isNotBlank(lcCost.getOrderCode()) || lcCost.getFlightDateStart() != null || lcCost.getFlightDateEnd() != null || StrUtil.isNotBlank(lcCost.getCustomerNumber())) {
            LambdaQueryWrapper<LcOrder> orderWrapper = Wrappers.<LcOrder>lambdaQuery();
            if (StrUtil.isNotBlank(lcCost.getCustomerNumber())) {
                orderWrapper.like(LcOrder::getCustomerNumber, lcCost.getCustomerNumber());
            }
            if (StrUtil.isNotBlank(lcCost.getOrderCode())) {
                orderWrapper.like(LcOrder::getOrderCode, lcCost.getOrderCode());
            }
            if (lcCost.getFlightDateStart() != null) {
                orderWrapper.ge(LcOrder::getDrivingTime, lcCost.getFlightDateStart());
            }
            if (lcCost.getFlightDateEnd() != null) {
                orderWrapper.le(LcOrder::getDrivingTime, lcCost.getFlightDateEnd());
            }
            List<Integer> orderIds = lcOrderService.list(orderWrapper).stream().map(lcOrder -> lcOrder.getOrderId()).collect(Collectors.toList());
            if (orderIds.size() == 0) {
                return new ArrayList();
            }
            wrapper.in(LcCost::getOrderId, orderIds);
        }

        //整理付款客户查询条件
        if (StrUtil.isNotBlank(lcCost.getCustomerType())) {
            List<Integer> coopIds = remoteCoopService.listByType(lcCost.getCustomerType()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
            if (coopIds.size() == 0) {
                return new ArrayList();
            }
            wrapper.in(LcCost::getCustomerId, coopIds);
        }

        //整理成本查询条件
        if (StrUtil.isNotBlank(lcCost.getBusinessScope())) {
            wrapper.eq(LcCost::getBusinessScope, lcCost.getBusinessScope());
        }

        if (StrUtil.isNotBlank(lcCost.getCustomerName())) {
            wrapper.like(LcCost::getCustomerName, lcCost.getCustomerName());
        }

        if (lcCost.getServiceId() != null) {
            wrapper.eq(LcCost::getServiceId, lcCost.getServiceId());
        }
        wrapper.eq(LcCost::getOrgId, SecurityUtils.getUser().getOrgId());

        List<LcCost> list = lcCostService.list(wrapper);
        list.stream().forEach(cost -> {
            CoopVo coopVo = remoteCoopService.viewCoop(cost.getCustomerId().toString()).getData();
            if (coopVo != null) {
                cost.setCustomerType(coopVo.getCoop_type());
            }
            LcOrder order = lcOrderService.getById(cost.getOrderId());
            if (order != null) {
                cost.setOrderCode(order.getOrderCode());
                cost.setAwbNumber(order.getCustomerNumber());
                cost.setCustomerNumber(order.getCustomerNumber());
                cost.setFlightDate(order.getDrivingTime().toLocalDate());
                //件数
                cost.setConfirmPieces(order.getConfirmPieces() != null ? order.getConfirmPieces() : order.getPlanPieces());
                //毛重
                cost.setConfirmWeight(order.getConfirmWeight() != null ? order.getConfirmWeight() : order.getPlanWeight());
                //体积
                cost.setConfirmVolume(order.getConfirmVolume() != null ? order.getConfirmVolume() : order.getPlanVolume());
                //计费重量
                cost.setConfirmChargeWeight(order.getConfirmChargeWeight() != null ? order.getConfirmChargeWeight() : order.getPlanChargeWeight());
                //查询客户
                CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                if (coopVo1 != null) {
                    cost.setCoopName(coopVo1.getCoop_name());
                }
                //责任销售
                String salesName = order.getSalesName();
                if (!StrUtil.isEmpty(salesName)) {
                    cost.setSalesName(salesName.split(" ")[0]);
                }
                //责任客服
                String servicerName = order.getServicerName();
                if (!StrUtil.isEmpty(servicerName)) {
                    cost.setServicerName(servicerName.split(" ")[0]);
                }
            }
            if (cost.getCostAmountWriteoff() != null) {
                cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
            } else {
                cost.setCostAmountWriteoff(BigDecimal.ZERO);
                cost.setCostAmountNoWriteoff(cost.getCostAmount());
            }
            cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
            cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
            cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
            cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
            //核销本币
            //cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff().multiply(cost.getCostExchangeRate())));
            cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
            cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));
            //已对帐金额
            if (cost.getCostAmountPayment() != null) {
                BigDecimal p = cost.getCostAmountPayment();
                String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                cost.setAmountPaymentStr(str);
                if (cost.getCostExchangeRate() != null) {
                    BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                    String str2 = FormatUtils.formatWith2AndQFW(p2);
                    cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountFunctionalPaymentStr(str2);
                }
            } else {
                cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
            }
        });
        return list;
    }

    private List<IoCost> findCostListForIO(IoCost ioCost) {
        LambdaQueryWrapper<IoCost> wrapper = Wrappers.<IoCost>lambdaQuery();
        //整理订单查询条件
        if (StrUtil.isNotBlank(ioCost.getAwbNumber()) || StrUtil.isNotBlank(ioCost.getOrderCode()) || ioCost.getFlightDateStart() != null || ioCost.getFlightDateEnd() != null || StrUtil.isNotBlank(ioCost.getCustomerNumber())) {
            LambdaQueryWrapper<IoOrder> orderWrapper = Wrappers.<IoOrder>lambdaQuery();
            if (StrUtil.isNotBlank(ioCost.getCustomerNumber())) {
                orderWrapper.like(IoOrder::getCustomerNumber, ioCost.getCustomerNumber());
            }
            if (StrUtil.isNotBlank(ioCost.getOrderCode())) {
                orderWrapper.like(IoOrder::getOrderCode, ioCost.getOrderCode());
            }
            if (ioCost.getFlightDateStart() != null) {
                orderWrapper.ge(IoOrder::getBusinessDate, ioCost.getFlightDateStart());
            }
            if (ioCost.getFlightDateEnd() != null) {
                orderWrapper.le(IoOrder::getBusinessDate, ioCost.getFlightDateEnd());
            }
            List<Integer> orderIds = ioOrderService.list(orderWrapper).stream().map(ioOrder -> ioOrder.getOrderId()).collect(Collectors.toList());
            if (orderIds.size() == 0) {
                return new ArrayList();
            }
            wrapper.in(IoCost::getOrderId, orderIds);
        }

        //整理付款客户查询条件
        if (StrUtil.isNotBlank(ioCost.getCustomerType())) {
            List<Integer> coopIds = remoteCoopService.listByType(ioCost.getCustomerType()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
            if (coopIds.size() == 0) {
                return new ArrayList();
            }
            wrapper.in(IoCost::getCustomerId, coopIds);
        }

        //整理成本查询条件
        if (StrUtil.isNotBlank(ioCost.getBusinessScope())) {
            wrapper.eq(IoCost::getBusinessScope, ioCost.getBusinessScope());
        }

        if (StrUtil.isNotBlank(ioCost.getCustomerName())) {
            wrapper.like(IoCost::getCustomerName, ioCost.getCustomerName());
        }

        if (ioCost.getServiceId() != null) {
            wrapper.eq(IoCost::getServiceId, ioCost.getServiceId());
        }
        wrapper.eq(IoCost::getOrgId, SecurityUtils.getUser().getOrgId());

        List<IoCost> list = ioCostService.list(wrapper);
        list.stream().forEach(cost -> {
            CoopVo coopVo = remoteCoopService.viewCoop(cost.getCustomerId().toString()).getData();
            if (coopVo != null) {
                cost.setCustomerType(coopVo.getCoop_type());
            }
            IoOrder order = ioOrderService.getById(cost.getOrderId());
            if (order != null) {
                cost.setOrderCode(order.getOrderCode());
                cost.setAwbNumber(order.getCustomerNumber());
                cost.setCustomerNumber(order.getCustomerNumber());
                cost.setFlightDate(order.getBusinessDate());
                //件数
                cost.setConfirmPieces(order.getPlanPieces());
                //毛重
                cost.setConfirmWeight(order.getPlanWeight());
                //体积
                cost.setConfirmVolume(order.getPlanVolume());
                //计费重量
                cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                //查询客户
                CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                if (coopVo1 != null) {
                    cost.setCoopName(coopVo1.getCoop_name());
                }
                //责任销售
                String salesName = order.getSalesName();
                if (!StrUtil.isEmpty(salesName)) {
                    cost.setSalesName(salesName.split(" ")[0]);
                }
                //责任客服
                String servicerName = order.getServicerName();
                if (!StrUtil.isEmpty(servicerName)) {
                    cost.setServicerName(servicerName.split(" ")[0]);
                }
            }
            if (cost.getCostAmountWriteoff() != null) {
                cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
            } else {
                cost.setCostAmountWriteoff(BigDecimal.ZERO);
                cost.setCostAmountNoWriteoff(cost.getCostAmount());
            }
            cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
            cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
            cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
            cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
            //核销本币
            cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
            cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));
            //已对帐金额
            if (cost.getCostAmountPayment() != null) {
                BigDecimal p = cost.getCostAmountPayment();
                String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                cost.setAmountPaymentStr(str);
                if (cost.getCostExchangeRate() != null) {
                    BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                    String str2 = FormatUtils.formatWith2AndQFW(p2);
                    cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountFunctionalPaymentStr(str2);
                }
            } else {
                cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
            }
        });
        return list;
    }

    private List<TcCost> findCostListForTC(TcCost tcCost) {
        LambdaQueryWrapper<TcCost> wrapper = Wrappers.<TcCost>lambdaQuery();
        //整理订单查询条件
        if (StrUtil.isNotBlank(tcCost.getAwbNumber()) || StrUtil.isNotBlank(tcCost.getOrderCode()) || tcCost.getFlightDateStart() != null || tcCost.getFlightDateEnd() != null || StrUtil.isNotBlank(tcCost.getCustomerNumber())) {
            LambdaQueryWrapper<TcOrder> orderWrapper = Wrappers.<TcOrder>lambdaQuery();
            /*if (StrUtil.isNotBlank(tcCost.getAwbNumber())) {
            	orderWrapper.like(TcOrder::getRwbNumber,tcCost.getOrderCode());
            }
            if (StrUtil.isNotBlank(tcCost.getOrderCode())) {
                orderWrapper.like(TcOrder::getOrderCode,tcCost.getOrderCode());
            }*/
            if (StrUtil.isNotBlank(tcCost.getOrderCode())) {
                orderWrapper.and(i -> i.like(TcOrder::getRwbNumber, "%" + tcCost.getOrderCode() + "%").or(k -> k.like(TcOrder::getOrderCode, "%" + tcCost.getOrderCode() + "%")));
            }
            if (StrUtil.isNotBlank(tcCost.getCustomerNumber())) {
                orderWrapper.like(TcOrder::getCustomerNumber, "%" + tcCost.getCustomerNumber() + "%");
            }
            if (tcCost.getFlightDateStart() != null) {
                if ("TE".equals(tcCost.getBusinessScope())) {
                    orderWrapper.ge(TcOrder::getExpectDeparture, tcCost.getFlightDateStart());
                } else if ("TI".equals(tcCost.getBusinessScope())) {
                    orderWrapper.ge(TcOrder::getExpectArrival, tcCost.getFlightDateStart());
                }
            }
            if (tcCost.getFlightDateEnd() != null) {
                if ("TE".equals(tcCost.getBusinessScope())) {
                    orderWrapper.le(TcOrder::getExpectDeparture, tcCost.getFlightDateEnd());
                } else if ("TI".equals(tcCost.getBusinessScope())) {
                    orderWrapper.le(TcOrder::getExpectArrival, tcCost.getFlightDateEnd());
                }
            }
            List<Integer> orderIds = tcOrderService.list(orderWrapper).stream().map(tcOrder -> tcOrder.getOrderId()).collect(Collectors.toList());
            if (orderIds.size() == 0) {
                return new ArrayList();
            }
            wrapper.in(TcCost::getOrderId, orderIds);
        }

        //整理付款客户查询条件
        if (StrUtil.isNotBlank(tcCost.getCustomerType())) {
            List<Integer> coopIds = remoteCoopService.listByType(tcCost.getCustomerType()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
            if (coopIds.size() == 0) {
                return new ArrayList();
            }
            wrapper.in(TcCost::getCustomerId, coopIds);
        }

        //整理成本查询条件
        if (StrUtil.isNotBlank(tcCost.getBusinessScope())) {
            wrapper.eq(TcCost::getBusinessScope, tcCost.getBusinessScope());
        }

        if (StrUtil.isNotBlank(tcCost.getCustomerName())) {
            wrapper.like(TcCost::getCustomerName, tcCost.getCustomerName());
        }

        if (tcCost.getServiceId() != null) {
            wrapper.eq(TcCost::getServiceId, tcCost.getServiceId());
        }
        wrapper.eq(TcCost::getOrgId, SecurityUtils.getUser().getOrgId());

        List<TcCost> list = tcCostService.list(wrapper);
        list.stream().forEach(cost -> {
            CoopVo coopVo = remoteCoopService.viewCoop(cost.getCustomerId().toString()).getData();
            if (coopVo != null) {
                cost.setCustomerType(coopVo.getCoop_type());
            }
            TcOrder order = tcOrderService.getById(cost.getOrderId());
            if (order != null) {
                cost.setOrderCode(order.getOrderCode());
                cost.setAwbNumber(order.getRwbNumber());
                cost.setCustomerNumber(order.getCustomerNumber());
                if ("TE".equals(tcCost.getBusinessScope())) {
                    cost.setFlightDate(order.getExpectDeparture());
                } else if ("TI".equals(tcCost.getBusinessScope())) {
                    cost.setFlightDate(order.getExpectArrival());
                }
                //件数
                cost.setConfirmPieces(order.getPlanPieces());
                //毛重
                cost.setConfirmWeight(order.getPlanWeight());
                //体积
                cost.setConfirmVolume(order.getPlanVolume());
                //计费重量
                cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                //查询客户
                CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                if (coopVo1 != null) {
                    cost.setCoopName(coopVo1.getCoop_name());
                }
                //责任销售
                String salesName = order.getSalesName();
                if (!StrUtil.isEmpty(salesName)) {
                    cost.setSalesName(salesName.split(" ")[0]);
                }
                //责任客服
                String servicerName = order.getServicerName();
                if (!StrUtil.isEmpty(servicerName)) {
                    cost.setServicerName(servicerName.split(" ")[0]);
                }
            }
            if (cost.getCostAmountWriteoff() != null) {
                cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
            } else {
                cost.setCostAmountWriteoff(BigDecimal.ZERO);
                cost.setCostAmountNoWriteoff(cost.getCostAmount());
            }
            cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
            cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
            cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
            cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
            //核销本币
            //cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff().multiply(cost.getCostExchangeRate())));
            cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
            cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));
            //已对帐金额
            if (cost.getCostAmountPayment() != null) {
                BigDecimal p = cost.getCostAmountPayment();
                String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                cost.setAmountPaymentStr(str);
                if (cost.getCostExchangeRate() != null) {
                    BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                    String str2 = FormatUtils.formatWith2AndQFW(p2);
                    cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountFunctionalPaymentStr(str2);
                }
            } else {
                cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
            }
        });
        return list;
    }

    private List<ScCost> findCostListForSC(ScCost scCost) {
        LambdaQueryWrapper<ScCost> wrapper = Wrappers.<ScCost>lambdaQuery();
        //整理订单查询条件
        if (StrUtil.isNotBlank(scCost.getAwbNumber()) || StrUtil.isNotBlank(scCost.getOrderCode()) || scCost.getFlightDateStart() != null || scCost.getFlightDateEnd() != null || StrUtil.isNotBlank(scCost.getCustomerNumber())) {
            LambdaQueryWrapper<ScOrder> orderWrapper = Wrappers.<ScOrder>lambdaQuery();
            /*if (StrUtil.isNotBlank(scCost.getAwbNumber())) {
                if (scCost.getAwbNumber().split("_").length == 2) {
                    orderWrapper.like(ScOrder::getMblNumber, "%" + scCost.getAwbNumber().split("_")[0] + "%").like(ScOrder::getHblNumber, "%" + scCost.getAwbNumber().split("_")[1] + "%");
                } else {
                    orderWrapper.and(i -> i.like(ScOrder::getMblNumber, "%" + scCost.getAwbNumber() + "%").or(j -> j.like(ScOrder::getHblNumber, "%" + scCost.getAwbNumber() + "%")));
                }

            }
            if (StrUtil.isNotBlank(scCost.getOrderCode())) {
                orderWrapper.like(ScOrder::getOrderCode, "%" + scCost.getOrderCode() + "%");
            }*/
            if (StrUtil.isNotBlank(scCost.getOrderCode())) {
                if (scCost.getOrderCode().split("_").length == 2) {
                    orderWrapper.like(ScOrder::getMblNumber, "%" + scCost.getOrderCode().split("_")[0] + "%").like(ScOrder::getHblNumber, "%" + scCost.getOrderCode().split("_")[1] + "%");
                } else {
                    orderWrapper.and(i -> i.like(ScOrder::getMblNumber, "%" + scCost.getOrderCode() + "%").or(j -> j.like(ScOrder::getHblNumber, "%" + scCost.getOrderCode() + "%")).or(k -> k.like(ScOrder::getOrderCode, "%" + scCost.getOrderCode() + "%")));
                }

            }
            if (StrUtil.isNotBlank(scCost.getCustomerNumber())) {
                orderWrapper.like(ScOrder::getCustomerNumber, "%" + scCost.getCustomerNumber() + "%");
            }
            if (scCost.getFlightDateStart() != null) {
                if ("SE".equals(scCost.getBusinessScope())) {
                    orderWrapper.ge(ScOrder::getExpectDeparture, scCost.getFlightDateStart());
                } else if ("SI".equals(scCost.getBusinessScope())) {
                    orderWrapper.ge(ScOrder::getExpectArrival, scCost.getFlightDateStart());
                }
            }
            if (scCost.getFlightDateEnd() != null) {
                if ("SE".equals(scCost.getBusinessScope())) {
                    orderWrapper.le(ScOrder::getExpectDeparture, scCost.getFlightDateEnd());
                } else if ("SI".equals(scCost.getBusinessScope())) {
                    orderWrapper.le(ScOrder::getExpectArrival, scCost.getFlightDateEnd());
                }
            }
            List<Integer> orderIds = scOrderService.list(orderWrapper).stream().map(scOrder -> scOrder.getOrderId()).collect(Collectors.toList());
            if (orderIds.size() == 0) {
                return new ArrayList();
            }
            wrapper.in(ScCost::getOrderId, orderIds);
        }

        //整理付款客户查询条件
        if (StrUtil.isNotBlank(scCost.getCustomerType())) {
            List<Integer> coopIds = remoteCoopService.listByType(scCost.getCustomerType()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
            if (coopIds.size() == 0) {
                return new ArrayList();
            }
            wrapper.in(ScCost::getCustomerId, coopIds);
        }

        //整理成本查询条件
        if (StrUtil.isNotBlank(scCost.getBusinessScope())) {
            wrapper.eq(ScCost::getBusinessScope, scCost.getBusinessScope());
        }

        if (StrUtil.isNotBlank(scCost.getCustomerName())) {
            wrapper.like(ScCost::getCustomerName, "%" + scCost.getCustomerName() + "%");
        }

        if (scCost.getServiceId() != null) {
            wrapper.eq(ScCost::getServiceId, scCost.getServiceId());
        }
        wrapper.eq(ScCost::getOrgId, SecurityUtils.getUser().getOrgId());

        List<ScCost> list = scCostService.list(wrapper);
        list.stream().forEach(cost -> {
            CoopVo coopVo = remoteCoopService.viewCoop(cost.getCustomerId().toString()).getData();
            if (coopVo != null) {
                cost.setCustomerType(coopVo.getCoop_type());
            }
            ScOrder order = scOrderService.getById(cost.getOrderId());
            if (order != null) {
                cost.setOrderCode(order.getOrderCode());
                cost.setAwbNumber(order.getMblNumber());
                cost.setHawbNumber(order.getHblNumber());
                cost.setCustomerNumber(order.getCustomerNumber());
                if ("SE".equals(scCost.getBusinessScope())) {
                    cost.setFlightDate(order.getExpectDeparture());
                } else if ("SI".equals(scCost.getBusinessScope())) {
                    cost.setFlightDate(order.getExpectArrival());
                }
                //件数
                cost.setConfirmPieces(order.getPlanPieces());
                //毛重
                cost.setConfirmWeight(order.getPlanWeight());
                //体积
                cost.setConfirmVolume(order.getPlanVolume());
                //计费重量
                cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                //查询客户
                CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                if (coopVo1 != null) {
                    cost.setCoopName(coopVo1.getCoop_name());
                }
                //责任销售
                String salesName = order.getSalesName();
                if (!StrUtil.isEmpty(salesName)) {
                    cost.setSalesName(salesName.split(" ")[0]);
                }
                //责任客服
                String servicerName = order.getServicerName();
                if (!StrUtil.isEmpty(servicerName)) {
                    cost.setServicerName(servicerName.split(" ")[0]);
                }
            }
            if (cost.getCostAmountWriteoff() != null) {
                cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
            } else {
                cost.setCostAmountWriteoff(BigDecimal.ZERO);
                cost.setCostAmountNoWriteoff(cost.getCostAmount());
            }
            cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
            cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
            cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
            cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
            //核销本币
            //cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff().multiply(cost.getCostExchangeRate())));
            cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
            cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));
            //已对帐金额
            if (cost.getCostAmountPayment() != null) {
                BigDecimal p = cost.getCostAmountPayment();
                String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                cost.setAmountPaymentStr(str);
                if (cost.getCostExchangeRate() != null) {
                    BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                    String str2 = FormatUtils.formatWith2AndQFW(p2);
                    cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountFunctionalPaymentStr(str2);
                }
            } else {
                cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
            }
        });
        return list;
    }

    @Override
    public void exportExcelForAF(AfCost afCost) {
        List<AfCost> listAf = null;
        if (afCost.isGroupSum()) {
            listAf = this.findCostListForAFGroupSum(afCost);
        } else {
            listAf = this.findCostListForAF(afCost);
        }
        listAf.stream().forEach(cost -> {
            if (cost.getConfirmWeight() != null) {
                cost.setConfirmWeightStr(com.efreight.common.core.utils.FormatUtils.formatWithQWF(cost.getConfirmWeight(), 1));
            }
            if (cost.getConfirmVolume() != null) {
                cost.setConfirmVolumeStr(com.efreight.common.core.utils.FormatUtils.formatWithQWF(BigDecimal.valueOf(cost.getConfirmVolume()), 3));
            }
            if (cost.getConfirmChargeWeight() != null) {
                cost.setConfirmChargeWeightStr(com.efreight.common.core.utils.FormatUtils.formatWithQWF(BigDecimal.valueOf(cost.getConfirmChargeWeight()), 1));
            }
        });
        if (listAf.size() > 0) {
            afCost.setOrgId(SecurityUtils.getUser().getOrgId());
            List<AfCost> sumCostList = baseMapper.sumCostForAF(afCost);
            if (sumCostList != null && sumCostList.size() > 0) {
                StringBuffer costAmountStrBuffer = new StringBuffer();//成本金额(原币)
                BigDecimal costFunctionalAmount = new BigDecimal(0);//成本金额(本币)
                StringBuffer costAmountWriteoffStrBuffer = new StringBuffer();//已核销金额(原币)
                BigDecimal amountFunctionalPayment = new BigDecimal(0);//已核销金额(本币)
                StringBuffer costAmountNoWriteoffStrBuffer = new StringBuffer();//未付款金额(原币)
                BigDecimal costFunctionalAmountNoWriteoff = new BigDecimal(0);//未付款金额(本币)
                StringBuffer amountPaymentStrBuffer = new StringBuffer();//已对账金额(原币)
                for (int i = 0; i < sumCostList.size(); i++) {
                    //拼接成本金额(原币)
                    costAmountStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmount())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmount = costFunctionalAmount.add(sumCostList.get(i).getCostFunctionalAmount());
                    //已核销金额(本币)
                    amountFunctionalPayment = amountFunctionalPayment.add(new BigDecimal(String.valueOf(sumCostList.get(i).getAmountFunctionalPayment())));
                    //拼接已核销金额(原币)
                    costAmountWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //拼接未付款金额(原币)
                    costAmountNoWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountNoWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmountNoWriteoff = costFunctionalAmountNoWriteoff.add(sumCostList.get(i).getCostFunctionalAmountNoWriteoff());
                    //拼接已对账金额(原币)
                    if (sumCostList.get(i).getCostAmountPayment() != null) {
                        amountPaymentStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountPayment())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    }
                }
                AfCost sumCost = new AfCost();
                sumCost.setBusinessScope("合计");
                sumCost.setCostAmountStr(costAmountStrBuffer.toString().replaceAll("  ", "\r\n"));
                sumCost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(costFunctionalAmount).replaceAll("  ", "\r\n"));
                sumCost.setCostAmountWriteoffStr(costAmountWriteoffStrBuffer.toString().replaceAll("  ", "\r\n"));
                sumCost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(amountFunctionalPayment).replaceAll("  ", "\r\n"));
                sumCost.setCostAmountNoWriteoffStr(costAmountNoWriteoffStrBuffer.toString().replaceAll("  ", "\r\n"));
                sumCost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(costFunctionalAmountNoWriteoff).replaceAll("  ", "\r\n"));
                sumCost.setAmountPaymentStr(amountPaymentStrBuffer.toString());

                listAf.add(sumCost);
            }
        }

        //自定义字段
        if (StrUtil.isNotBlank(afCost.getColumnStrs())) {
            List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();

            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(afCost.getColumnStrs());
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
            if (listAf != null && listAf.size() > 0) {
                for (AfCost order : listAf) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        if ("confirmWeight".equals(colunmStrs[j]) || "confirmVolume".equals(colunmStrs[j]) || "confirmChargeWeight".equals(colunmStrs[j])) {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j] + "Str", order));
                        } else {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], order));
                        }
                    }
                    listExcel.add(map);
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMapForXlsx(WebUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");

        } else {
            throw new RuntimeException("缺失必要传参");
        }
    }


    @Override
    public void exportExcelForLC(LcCost lcCost) {
        List<LcCost> listLc = null;
        if (lcCost.isGroupSum()) {
            listLc = this.findCostListForLCGroupSum(lcCost);
        } else {
            listLc = this.findCostListForLC(lcCost);
        }
        listLc.stream().forEach(cost -> {
            if (cost.getConfirmWeight() != null) {
                cost.setConfirmWeightStr(com.efreight.common.core.utils.FormatUtils.formatWithQWF(cost.getConfirmWeight(), 1));
            }
            if (cost.getConfirmVolume() != null) {
                cost.setConfirmVolumeStr(com.efreight.common.core.utils.FormatUtils.formatWithQWF(cost.getConfirmVolume(), 3));
            }
            if (cost.getConfirmChargeWeight() != null) {
                cost.setConfirmChargeWeightStr(com.efreight.common.core.utils.FormatUtils.formatWithQWF(cost.getConfirmChargeWeight(), 3));
            }
        });
        if (listLc.size() > 0) {
            lcCost.setOrgId(SecurityUtils.getUser().getOrgId());
            List<LcCost> sumCostList = baseMapper.sumCostForLC(lcCost);
            if (sumCostList != null && sumCostList.size() > 0) {

                StringBuffer costAmountStrBuffer = new StringBuffer();//成本金额(原币)
                BigDecimal costFunctionalAmount = new BigDecimal(0);//成本金额(本币)
                StringBuffer costAmountWriteoffStrBuffer = new StringBuffer();//已核销金额(原币)
                BigDecimal amountFunctionalPayment = new BigDecimal(0);//已核销金额(本币)
                StringBuffer costAmountNoWriteoffStrBuffer = new StringBuffer();//未付款金额(原币)
                BigDecimal costFunctionalAmountNoWriteoff = new BigDecimal(0);//未付款金额(本币)
                StringBuffer amountPaymentStrBuffer = new StringBuffer();//已对账金额(原币)
                for (int i = 0; i < sumCostList.size(); i++) {
                    //拼接成本金额(原币)
                    costAmountStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmount())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmount = costFunctionalAmount.add(sumCostList.get(i).getCostFunctionalAmount());

                    amountFunctionalPayment = amountFunctionalPayment.add(new BigDecimal(String.valueOf(sumCostList.get(i).getAmountFunctionalPayment())));
                    //拼接已核销金额(原币)
                    costAmountWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //拼接未付款金额(原币)
                    costAmountNoWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountNoWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmountNoWriteoff = costFunctionalAmountNoWriteoff.add(sumCostList.get(i).getCostFunctionalAmountNoWriteoff());
                    //拼接已对账金额(原币)
                    if (sumCostList.get(i).getCostAmountPayment() != null) {
                        amountPaymentStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountPayment())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    } else {
                        amountPaymentStrBuffer.append("0.00").append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    }
                }
                LcCost sumCost = new LcCost();
                sumCost.setBusinessScope("合计");
                sumCost.setCostAmountStr(costAmountStrBuffer.toString().replaceAll("  ", "\r\n"));
                sumCost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(costFunctionalAmount).replaceAll("  ", "\r\n"));
                sumCost.setCostAmountWriteoffStr(costAmountWriteoffStrBuffer.toString().replaceAll("  ", "\r\n"));
                sumCost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(amountFunctionalPayment).replaceAll("  ", "\r\n"));
                sumCost.setCostAmountNoWriteoffStr(costAmountNoWriteoffStrBuffer.toString().replaceAll("  ", "\r\n"));
                sumCost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(costFunctionalAmountNoWriteoff).replaceAll("  ", "\r\n"));
                sumCost.setAmountPaymentStr(amountPaymentStrBuffer.toString());
                listLc.add(sumCost);
            }

        }

        //自定义字段
        if (!StringUtils.isEmpty(lcCost.getColumnStrs())) {
            List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();

            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(lcCost.getColumnStrs());
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
            if (listLc != null && listLc.size() > 0) {
                for (LcCost order : listLc) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        if ("confirmWeight".equals(colunmStrs[j]) || "confirmVolume".equals(colunmStrs[j]) || "confirmChargeWeight".equals(colunmStrs[j])) {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j] + "Str", order));
                        } else {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], order));
                        }
                    }
                    listExcel.add(map);
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMapForXlsx(WebUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");

        } else {
            throw new RuntimeException("缺失必要传参");
        }
    }

    @Override
    public void exportExcelForIO(IoCost ioCost) {
        List<IoCost> listIO = null;
        if (ioCost.isGroupSum()) {
            listIO = this.findCostListForIOGroupSum(ioCost);
        } else {
            listIO = this.findCostListForIO(ioCost);
        }
        listIO.stream().forEach(cost -> {
            if (cost.getConfirmWeight() != null) {
                cost.setConfirmWeightStr(com.efreight.common.core.utils.FormatUtils.formatWithQWF(cost.getConfirmWeight(), 1));
            }
            if (cost.getConfirmVolume() != null) {
                cost.setConfirmVolumeStr(com.efreight.common.core.utils.FormatUtils.formatWithQWF(cost.getConfirmVolume(), 3));
            }
            if (cost.getConfirmChargeWeight() != null) {
                cost.setConfirmChargeWeightStr(com.efreight.common.core.utils.FormatUtils.formatWithQWF(cost.getConfirmChargeWeight(), 3));
            }
        });
        if (listIO.size() > 0) {
            ioCost.setOrgId(SecurityUtils.getUser().getOrgId());
            List<IoCost> sumCostList = baseMapper.sumCostForIO(ioCost);
            if (sumCostList != null && sumCostList.size() > 0) {

                StringBuffer costAmountStrBuffer = new StringBuffer();//成本金额(原币)
                BigDecimal costFunctionalAmount = new BigDecimal(0);//成本金额(本币)
                StringBuffer costAmountWriteoffStrBuffer = new StringBuffer();//已核销金额(原币)
                BigDecimal amountFunctionalPayment = new BigDecimal(0);//已核销金额(本币)
                StringBuffer costAmountNoWriteoffStrBuffer = new StringBuffer();//未付款金额(原币)
                BigDecimal costFunctionalAmountNoWriteoff = new BigDecimal(0);//未付款金额(本币)
                StringBuffer amountPaymentStrBuffer = new StringBuffer();//已对账金额(原币)
                for (int i = 0; i < sumCostList.size(); i++) {
                    //拼接成本金额(原币)
                    costAmountStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmount())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmount = costFunctionalAmount.add(sumCostList.get(i).getCostFunctionalAmount());

                    amountFunctionalPayment = amountFunctionalPayment.add(new BigDecimal(String.valueOf(sumCostList.get(i).getAmountFunctionalPayment())));
                    //拼接已核销金额(原币)
                    costAmountWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //拼接未付款金额(原币)
                    costAmountNoWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountNoWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmountNoWriteoff = costFunctionalAmountNoWriteoff.add(sumCostList.get(i).getCostFunctionalAmountNoWriteoff());
                    //拼接已对账金额(原币)
                    if (sumCostList.get(i).getCostAmountPayment() != null) {
                        amountPaymentStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountPayment())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    } else {
                        amountPaymentStrBuffer.append("0.00").append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    }
                }
                IoCost sumCost = new IoCost();
                sumCost.setBusinessScope("合计");
                sumCost.setCostAmountStr(costAmountStrBuffer.toString().replaceAll("  ", "\r\n"));
                sumCost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(costFunctionalAmount).replaceAll("  ", "\r\n"));
                sumCost.setCostAmountWriteoffStr(costAmountWriteoffStrBuffer.toString().replaceAll("  ", "\r\n"));
                sumCost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(amountFunctionalPayment).replaceAll("  ", "\r\n"));
                sumCost.setCostAmountNoWriteoffStr(costAmountNoWriteoffStrBuffer.toString().replaceAll("  ", "\r\n"));
                sumCost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(costFunctionalAmountNoWriteoff).replaceAll("  ", "\r\n"));
                sumCost.setAmountPaymentStr(amountPaymentStrBuffer.toString());
                listIO.add(sumCost);
            }

        }

        //自定义字段
        if (!StringUtils.isEmpty(ioCost.getColumnStrs())) {
            List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();

            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(ioCost.getColumnStrs());
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
            if (listIO != null && listIO.size() > 0) {
                for (IoCost order : listIO) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        if ("confirmWeight".equals(colunmStrs[j]) || "confirmVolume".equals(colunmStrs[j]) || "confirmChargeWeight".equals(colunmStrs[j])) {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j] + "Str", order));
                        } else {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], order));
                        }
                    }
                    listExcel.add(map);
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMapForXlsx(WebUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");

        } else {
            throw new RuntimeException("缺失必要传参");
        }
    }

    @Override
    public void exportExcelForTC(TcCost tcCost) {
        List<TcCost> listTc = null;
        if (tcCost.isGroupSum()) {
            listTc = this.findCostListForTCGroupSum(tcCost);
        } else {
            listTc = this.findCostListForTC(tcCost);
        }
        listTc.stream().forEach(cost -> {
            if (cost.getConfirmWeight() != null) {
                cost.setConfirmWeightStr(com.efreight.common.core.utils.FormatUtils.formatWithQWF(cost.getConfirmWeight(), 1));
            }
            if (cost.getConfirmVolume() != null) {
                cost.setConfirmVolumeStr(com.efreight.common.core.utils.FormatUtils.formatWithQWF(cost.getConfirmVolume(), 3));
            }
            if (cost.getConfirmChargeWeight() != null) {
                cost.setConfirmChargeWeightStr(com.efreight.common.core.utils.FormatUtils.formatWithQWF(cost.getConfirmChargeWeight(), 3));
            }
        });
        if (listTc.size() > 0) {
            tcCost.setOrgId(SecurityUtils.getUser().getOrgId());
            List<TcCost> sumCostList = baseMapper.sumCostForTC(tcCost);
            if (sumCostList != null && sumCostList.size() > 0) {

                StringBuffer costAmountStrBuffer = new StringBuffer();//成本金额(原币)
                BigDecimal costFunctionalAmount = new BigDecimal(0);//成本金额(本币)
                StringBuffer costAmountWriteoffStrBuffer = new StringBuffer();//已核销金额(原币)
                BigDecimal amountFunctionalPayment = new BigDecimal(0);//已核销金额(本币)
                StringBuffer costAmountNoWriteoffStrBuffer = new StringBuffer();//未付款金额(原币)
                BigDecimal costFunctionalAmountNoWriteoff = new BigDecimal(0);//未付款金额(本币)
                StringBuffer amountPaymentStrBuffer = new StringBuffer();//已对账金额(原币)
                for (int i = 0; i < sumCostList.size(); i++) {
                    //拼接成本金额(原币)
                    costAmountStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmount())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmount = costFunctionalAmount.add(sumCostList.get(i).getCostFunctionalAmount());

                    amountFunctionalPayment = amountFunctionalPayment.add(new BigDecimal(String.valueOf(sumCostList.get(i).getAmountFunctionalPayment())));
                    //拼接已核销金额(原币)
                    costAmountWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //拼接未付款金额(原币)
                    costAmountNoWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountNoWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmountNoWriteoff = costFunctionalAmountNoWriteoff.add(sumCostList.get(i).getCostFunctionalAmountNoWriteoff());
                    //拼接已对账金额(原币)
                    if (sumCostList.get(i).getCostAmountPayment() != null) {
                        amountPaymentStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountPayment())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    } else {
                        amountPaymentStrBuffer.append("0.00").append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    }
                }
                TcCost sumCost = new TcCost();
                sumCost.setBusinessScope("合计");
                sumCost.setCostAmountStr(costAmountStrBuffer.toString().replaceAll("  ", "\r\n"));
                sumCost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(costFunctionalAmount).replaceAll("  ", "\r\n"));
                sumCost.setCostAmountWriteoffStr(costAmountWriteoffStrBuffer.toString().replaceAll("  ", "\r\n"));
                sumCost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(amountFunctionalPayment).replaceAll("  ", "\r\n"));
                sumCost.setCostAmountNoWriteoffStr(costAmountNoWriteoffStrBuffer.toString().replaceAll("  ", "\r\n"));
                sumCost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(costFunctionalAmountNoWriteoff).replaceAll("  ", "\r\n"));
                sumCost.setAmountPaymentStr(amountPaymentStrBuffer.toString());

                listTc.add(sumCost);
            }

        }

        //自定义字段
        if (!StringUtils.isEmpty(tcCost.getColumnStrs())) {
            List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();

            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(tcCost.getColumnStrs());
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
            if (listTc != null && listTc.size() > 0) {
                for (TcCost order : listTc) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        if ("confirmWeight".equals(colunmStrs[j]) || "confirmVolume".equals(colunmStrs[j]) || "confirmChargeWeight".equals(colunmStrs[j])) {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j] + "Str", order));
                        } else {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], order));
                        }
                    }
                    listExcel.add(map);
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMapForXlsx(WebUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");

        } else {
            throw new RuntimeException("缺失必要传参");
        }
    }

    @Override
    public void exportExcelForSC(ScCost scCost) {
        List<ScCost> listSc = null;
        if (scCost.isGroupSum()) {
            listSc = this.findCostListForSCGroupSum(scCost);
        } else {
            listSc = this.findCostListForSC(scCost);
        }
        listSc.stream().forEach(cost -> {
            if (StrUtil.isNotBlank(cost.getAwbNumber()) && StrUtil.isNotBlank(cost.getHawbNumber())) {
                cost.setAwbNumber(cost.getAwbNumber() + "_" + cost.getHawbNumber());
            }
            if (StrUtil.isBlank(cost.getAwbNumber()) && StrUtil.isNotBlank(cost.getHawbNumber())) {
                cost.setAwbNumber(cost.getHawbNumber());
            }
            if (StrUtil.isNotBlank(cost.getAwbNumber()) && StrUtil.isBlank(cost.getHawbNumber())) {
                cost.setAwbNumber(cost.getAwbNumber());
            }
            if (cost.getConfirmWeight() != null) {
                cost.setConfirmWeightStr(com.efreight.common.core.utils.FormatUtils.formatWithQWF(cost.getConfirmWeight(), 1));
            }
            if (cost.getConfirmVolume() != null) {
                cost.setConfirmVolumeStr(com.efreight.common.core.utils.FormatUtils.formatWithQWF(cost.getConfirmVolume(), 3));
            }
            if (cost.getConfirmChargeWeight() != null) {
                cost.setConfirmChargeWeightStr(com.efreight.common.core.utils.FormatUtils.formatWithQWF(cost.getConfirmChargeWeight(), 3));
            }
        });
        if (listSc.size() > 0) {
            scCost.setOrgId(SecurityUtils.getUser().getOrgId());
            List<ScCost> sumCostList = baseMapper.sumCostForSC(scCost);
            if (sumCostList != null && sumCostList.size() > 0) {

                StringBuffer costAmountStrBuffer = new StringBuffer();//成本金额(原币)
                BigDecimal costFunctionalAmount = new BigDecimal(0);//成本金额(本币)
                StringBuffer costAmountWriteoffStrBuffer = new StringBuffer();//已核销金额(原币)
                BigDecimal amountFunctionalPayment = new BigDecimal(0);//已核销金额(本币)
                StringBuffer costAmountNoWriteoffStrBuffer = new StringBuffer();//未付款金额(原币)
                BigDecimal costFunctionalAmountNoWriteoff = new BigDecimal(0);//未付款金额(本币)
                StringBuffer amountPaymentStrBuffer = new StringBuffer();//已对账金额(原币)
                for (int i = 0; i < sumCostList.size(); i++) {
                    //拼接成本金额(原币)
                    costAmountStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmount())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmount = costFunctionalAmount.add(sumCostList.get(i).getCostFunctionalAmount());

                    amountFunctionalPayment = amountFunctionalPayment.add(new BigDecimal(String.valueOf(sumCostList.get(i).getAmountFunctionalPayment())));
                    //拼接已核销金额(原币)
                    costAmountWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //拼接未付款金额(原币)
                    costAmountNoWriteoffStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountNoWriteoff())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    //计算成本金额(本币)
                    costFunctionalAmountNoWriteoff = costFunctionalAmountNoWriteoff.add(sumCostList.get(i).getCostFunctionalAmountNoWriteoff());
                    //拼接已对账金额(原币)
                    if (sumCostList.get(i).getCostAmountPayment() != null) {
                        amountPaymentStrBuffer.append(FormatUtils.formatWith2AndQFW(sumCostList.get(i).getCostAmountPayment())).append(" (").append(sumCostList.get(i).getCostCurrency()).append(")  ");
                    }
                }
                ScCost sumCost = new ScCost();
                sumCost.setBusinessScope("合计");
                sumCost.setCostAmountStr(costAmountStrBuffer.toString().replaceAll("  ", "\r\n"));
                sumCost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(costFunctionalAmount).replaceAll("  ", "\r\n"));
                sumCost.setCostAmountWriteoffStr(costAmountWriteoffStrBuffer.toString().replaceAll("  ", "\r\n"));
                sumCost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(amountFunctionalPayment).replaceAll("  ", "\r\n"));
                sumCost.setCostAmountNoWriteoffStr(costAmountNoWriteoffStrBuffer.toString().replaceAll("  ", "\r\n"));
                sumCost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(costFunctionalAmountNoWriteoff).replaceAll("  ", "\r\n"));
                sumCost.setAmountPaymentStr(amountPaymentStrBuffer.toString());

                listSc.add(sumCost);
            }

        }

        //自定义字段
        if (!StringUtils.isEmpty(scCost.getColumnStrs())) {
            List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();

            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(scCost.getColumnStrs());
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
            if (listSc != null && listSc.size() > 0) {
                for (ScCost order : listSc) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        if ("confirmWeight".equals(colunmStrs[j]) || "confirmVolume".equals(colunmStrs[j]) || "confirmChargeWeight".equals(colunmStrs[j])) {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j] + "Str", order));
                        } else {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], order));
                        }
                    }
                    listExcel.add(map);
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMapForXlsx(WebUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");

        } else {
            throw new RuntimeException("缺失必要传参");
        }
    }

    private List<AfCost> findCostListForAFGroupSum(AfCost afCost) {
        afCost.setOrgId(SecurityUtils.getUser().getOrgId());
        List<AfCost> list = baseMapper.queryAfCostPageAfExcel(afCost);
        list.stream().forEach(cost -> {
            if (cost.getCostAmountWriteoff() != null) {
                cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
            } else {
                cost.setCostAmountWriteoff(BigDecimal.ZERO);
                cost.setCostAmountNoWriteoff(cost.getCostAmount());
            }
            cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
            //成本原币
            cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
            //成本本币
            cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
            //核销原币
            cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
            //核销本币
            //cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff().multiply(cost.getCostExchangeRate())));
            //未核销原币
            cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
            //未核销本币
            cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));
            //已对帐金额
            if (cost.getCostAmountPayment() != null) {
                BigDecimal p = cost.getCostAmountPayment();
                String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                cost.setAmountPaymentStr(str);
                if (cost.getCostExchangeRate() != null) {
                    BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                    String str2 = FormatUtils.formatWith2AndQFW(p2);
                    cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountFunctionalPaymentStr(str2);
                }
            } else {
                cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
            }
            AfOrder order = afOrderService.getById(cost.getOrderId());
            if (order != null) {
                if ("AE".equals(afCost.getBusinessScope())) {
                    //件数
                    if (!"".equals(order.getConfirmPieces()) && order.getConfirmPieces() != null) {
                        cost.setConfirmPieces(order.getConfirmPieces());
                    } else {
                        cost.setConfirmPieces(order.getPlanPieces());
                    }
                    //毛重
                    if (!"".equals(order.getConfirmWeight()) && order.getConfirmWeight() != null) {
                        cost.setConfirmWeight(order.getConfirmWeight());
                    } else {
                        cost.setConfirmWeight(order.getPlanWeight());
                    }
                    //体积
                    if (!"".equals(order.getConfirmVolume()) && order.getConfirmVolume() != null) {
                        cost.setConfirmVolume(order.getConfirmVolume());
                    } else {
                        cost.setConfirmVolume(order.getPlanVolume());
                    }
                    //计费重量
                    if (!"".equals(order.getConfirmChargeWeight()) && order.getConfirmChargeWeight() != null) {
                        cost.setConfirmChargeWeight(order.getConfirmChargeWeight());
                    } else {
                        cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                    }
                } else if ("AI".equals(afCost.getBusinessScope())) {
                    //件数
                    cost.setConfirmPieces(order.getPlanPieces());
                    //毛重
                    cost.setConfirmWeight(order.getPlanWeight());
                    //体积
                    cost.setConfirmVolume(order.getPlanVolume());
                    //计费重量
                    cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                }
                //查询客户
                CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                if (coopVo1 != null) {
                    cost.setCoopName(coopVo1.getCoop_name());
                }
                //责任销售
                String salesName = order.getSalesName();
                if (!StrUtil.isEmpty(salesName)) {
                    cost.setSalesName(salesName.split(" ")[0]);
                }
                //责任客服
                String servicerName = order.getServicerName();
                if (!StrUtil.isEmpty(servicerName)) {
                    cost.setServicerName(servicerName.split(" ")[0]);
                }
            }
        });
        return list;
    }

    private List<ScCost> findCostListForSCGroupSum(ScCost scCost) {
        scCost.setOrgId(SecurityUtils.getUser().getOrgId());
        List<ScCost> list = baseMapper.queryScCostPageScExcel(scCost);
        list.stream().forEach(cost -> {
            if (cost.getCostAmountWriteoff() != null) {
                cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
            } else {
                cost.setCostAmountWriteoff(BigDecimal.ZERO);
                cost.setCostAmountNoWriteoff(cost.getCostAmount());
            }
            cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
            cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
            cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
            cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
            //核销本币
            //cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountPayment().multiply(cost.getCostExchangeRate())));
            cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
            cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));
            //已对帐金额
            if (cost.getCostAmountPayment() != null) {
                BigDecimal p = cost.getCostAmountPayment();
                String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                cost.setAmountPaymentStr(str);
                if (cost.getCostExchangeRate() != null) {
                    BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                    String str2 = FormatUtils.formatWith2AndQFW(p2);
                    cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountFunctionalPaymentStr(str2);
                }
            } else {
                cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
            }

            ScOrder order = scOrderService.getById(cost.getOrderId());
            if (order != null) {
                //件数
                cost.setConfirmPieces(order.getPlanPieces());
                //毛重
                cost.setConfirmWeight(order.getPlanWeight());
                //体积
                cost.setConfirmVolume(order.getPlanVolume());
                //计费重量
                cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                //查询客户
                CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                if (coopVo1 != null) {
                    cost.setCoopName(coopVo1.getCoop_name());
                }
                //责任销售
                String salesName = order.getSalesName();
                if (!StrUtil.isEmpty(salesName)) {
                    cost.setSalesName(salesName.split(" ")[0]);
                }
                //责任客服
                String servicerName = order.getServicerName();
                if (!StrUtil.isEmpty(servicerName)) {
                    cost.setServicerName(servicerName.split(" ")[0]);
                }
            }
        });
        return list;
    }

    private List<TcCost> findCostListForTCGroupSum(TcCost tcCost) {
        tcCost.setOrgId(SecurityUtils.getUser().getOrgId());
        List<TcCost> list = baseMapper.queryTcCostPageTcExcel(tcCost);
        list.stream().forEach(cost -> {
            if (cost.getCostAmountWriteoff() != null) {
                cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
            } else {
                cost.setCostAmountWriteoff(BigDecimal.ZERO);
                cost.setCostAmountNoWriteoff(cost.getCostAmount());
            }
            cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
            cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
            cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
            cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
            //核销本币
            //cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff().multiply(cost.getCostExchangeRate())));
            cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
            cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));
            //已对帐金额
            if (cost.getCostAmountPayment() != null) {
                BigDecimal p = cost.getCostAmountPayment();
                String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                cost.setAmountPaymentStr(str);
                if (cost.getCostExchangeRate() != null) {
                    BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                    String str2 = FormatUtils.formatWith2AndQFW(p2);
                    cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountFunctionalPaymentStr(str2);
                }
            } else {
                cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
            }

            TcOrder order = tcOrderService.getById(cost.getOrderId());
            if (order != null) {
                //件数
                cost.setConfirmPieces(order.getPlanPieces());
                //毛重
                cost.setConfirmWeight(order.getPlanWeight());
                //体积
                cost.setConfirmVolume(order.getPlanVolume());
                //计费重量
                cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                //查询客户
                CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                if (coopVo1 != null) {
                    cost.setCoopName(coopVo1.getCoop_name());
                }
                //责任销售
                String salesName = order.getSalesName();
                if (!StrUtil.isEmpty(salesName)) {
                    cost.setSalesName(salesName.split(" ")[0]);
                }
                //责任客服
                String servicerName = order.getServicerName();
                if (!StrUtil.isEmpty(servicerName)) {
                    cost.setServicerName(servicerName.split(" ")[0]);
                }
            }
        });
        return list;
    }

    private List<LcCost> findCostListForLCGroupSum(LcCost lcCost) {
        lcCost.setOrgId(SecurityUtils.getUser().getOrgId());
        List<LcCost> list = baseMapper.queryLcCostPageLcExcel(lcCost);
        list.stream().forEach(cost -> {
            if (cost.getCostAmountWriteoff() != null) {
                cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
            } else {
                cost.setCostAmountWriteoff(BigDecimal.ZERO);
                cost.setCostAmountNoWriteoff(cost.getCostAmount());
            }
            cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
            cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
            cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
            cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
            //核销本币
            //cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff().multiply(cost.getCostExchangeRate())));
            cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
            cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));
            //已对帐金额
            if (cost.getCostAmountPayment() != null) {
                BigDecimal p = cost.getCostAmountPayment();
                String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                cost.setAmountPaymentStr(str);
                if (cost.getCostExchangeRate() != null) {
                    BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                    String str2 = FormatUtils.formatWith2AndQFW(p2);
                    cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountFunctionalPaymentStr(str2);
                }
            } else {
                cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
            }

            LcOrder order = lcOrderService.getById(cost.getOrderId());
            if (order != null) {
                //件数
                cost.setConfirmPieces(order.getConfirmPieces() != null ? order.getConfirmPieces() : order.getPlanPieces());
                //毛重
                cost.setConfirmWeight(order.getConfirmWeight() != null ? order.getConfirmWeight() : order.getPlanWeight());
                //体积
                cost.setConfirmVolume(order.getConfirmVolume() != null ? order.getConfirmVolume() : order.getPlanVolume());
                //计费重量
                cost.setConfirmChargeWeight(order.getConfirmChargeWeight() != null ? order.getConfirmChargeWeight() : order.getPlanChargeWeight());
                //查询客户
                CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                if (coopVo1 != null) {
                    cost.setCoopName(coopVo1.getCoop_name());
                }
                //责任销售
                String salesName = order.getSalesName();
                if (!StrUtil.isEmpty(salesName)) {
                    cost.setSalesName(salesName.split(" ")[0]);
                }
                //责任客服
                String servicerName = order.getServicerName();
                if (!StrUtil.isEmpty(servicerName)) {
                    cost.setServicerName(servicerName.split(" ")[0]);
                }
            }
        });
        return list;
    }

    private List<IoCost> findCostListForIOGroupSum(IoCost ioCost) {
        ioCost.setOrgId(SecurityUtils.getUser().getOrgId());
        List<IoCost> list = baseMapper.queryLcCostPageIoExcel(ioCost);
        list.stream().forEach(cost -> {
            if (cost.getCostAmountWriteoff() != null) {
                cost.setCostAmountNoWriteoff(cost.getCostAmount().subtract(cost.getCostAmountWriteoff()));
            } else {
                cost.setCostAmountWriteoff(BigDecimal.ZERO);
                cost.setCostAmountNoWriteoff(cost.getCostAmount());
            }
            cost.setCostFunctionalAmountNoWriteoff(cost.getCostAmountNoWriteoff().multiply(cost.getCostExchangeRate()));
            cost.setCostAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostAmount()));
            cost.setCostFunctionalAmountStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmount()));
            cost.setCostAmountWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountWriteoff()));
            cost.setCostAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostAmountNoWriteoff()));
            cost.setCostFunctionalAmountNoWriteoffStr(FormatUtils.formatWith2AndQFW(cost.getCostFunctionalAmountNoWriteoff()));
            //已对帐金额
            if (cost.getCostAmountPayment() != null) {
                BigDecimal p = cost.getCostAmountPayment();
                String str = FormatUtils.formatWith2AndQFW(p != null ? p : BigDecimal.ZERO);
                cost.setAmountPayment(p.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                cost.setAmountPaymentStr(str);
                if (cost.getCostExchangeRate() != null) {
                    BigDecimal p2 = p.multiply(cost.getCostExchangeRate());
                    String str2 = FormatUtils.formatWith2AndQFW(p2);
                    cost.setAmountFunctionalPayment(p2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    cost.setAmountFunctionalPaymentStr(str2);
                }
            } else {
                cost.setAmountPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
                cost.setAmountFunctionalPaymentStr(FormatUtils.formatWith2AndQFW(BigDecimal.ZERO));
            }

            IoOrder order = ioOrderService.getById(cost.getOrderId());
            if (order != null) {
                //件数
                cost.setConfirmPieces(order.getPlanPieces());
                //毛重
                cost.setConfirmWeight(order.getPlanWeight());
                //体积
                cost.setConfirmVolume(order.getPlanVolume());
                //计费重量
                cost.setConfirmChargeWeight(order.getPlanChargeWeight());
                //查询客户
                CoopVo coopVo1 = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                if (coopVo1 != null) {
                    cost.setCoopName(coopVo1.getCoop_name());
                }
                //责任销售
                String salesName = order.getSalesName();
                if (!StrUtil.isEmpty(salesName)) {
                    cost.setSalesName(salesName.split(" ")[0]);
                }
                //责任客服
                String servicerName = order.getServicerName();
                if (!StrUtil.isEmpty(servicerName)) {
                    cost.setServicerName(servicerName.split(" ")[0]);
                }
            }
        });
        return list;
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
