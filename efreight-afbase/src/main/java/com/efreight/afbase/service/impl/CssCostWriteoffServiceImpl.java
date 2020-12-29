package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.dao.CssCostWriteoffMapper;
import com.efreight.afbase.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * CSS 应付：核销 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-14
 */
@Service
@AllArgsConstructor
public class CssCostWriteoffServiceImpl extends ServiceImpl<CssCostWriteoffMapper, CssCostWriteoff> implements CssCostWriteoffService {

    private final CssPaymentDetailService cssPaymentDetailService;

    private final CssPaymentService cssPaymentService;

    private final AfOrderService afOrderService;

    private final ScOrderService scOrderService;
    private final CssCostWriteoffDetailService cssCostWriteoffDetailService;

    private final AfCostService afCostService;
    private final ScCostService scCostService;

    private final TcCostService tcCostService;
    private final TcOrderService tcOrderService;
    private final LcCostService lcCostService;
    private final IoCostService ioCostService;
    private final LcOrderService lcOrderService;
    private final IoOrderService ioOrderService;

    @Override
    public List<CssCostWriteoffDetail> automatch(Integer paymentId, BigDecimal amountWriteoff) {
        LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
        cssPaymentDetailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getPaymentId, paymentId);
        HashMap<String, BigDecimal> automatchSum = new HashMap<>();
//        ArrayList<CssCostWriteoffDetail> automatchResult = new ArrayList<>();
        automatchSum.put("automatchSum", BigDecimal.ZERO);
        List<CssCostWriteoffDetail> result = cssPaymentDetailService.list(cssPaymentDetailWrapper).stream().map(cssPaymentDetail -> {
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

            CssPayment cssPayment = cssPaymentService.getById(paymentId);
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
                            awbOrOrderNumber = StrUtil.isNotBlank(order.getHblNumber()) ? (order.getMblNumber() + "_" + order.getHblNumber()) : order.getHblNumber();
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
        try {
            result.stream().forEach(cssCostWriteoffDetail -> {
                if (automatchSum.get("automatchSum").compareTo(amountWriteoff) == -1) {
                    if (automatchSum.get("automatchSum").add(cssCostWriteoffDetail.getAmountPaymentNoWriteoff()).compareTo(amountWriteoff) == -1) {
                        automatchSum.put("automatchSum", automatchSum.get("automatchSum").add(cssCostWriteoffDetail.getAmountPaymentNoWriteoff()));
                        cssCostWriteoffDetail.setAmountWriteoff(cssCostWriteoffDetail.getAmountPaymentNoWriteoff());
//                        automatchResult.add(cssCostWriteoffDetail);
                    } else if (automatchSum.get("automatchSum").add(cssCostWriteoffDetail.getAmountPaymentNoWriteoff()).compareTo(amountWriteoff) == 0) {
                        cssCostWriteoffDetail.setAmountWriteoff(cssCostWriteoffDetail.getAmountPaymentNoWriteoff());
//                        automatchResult.add(cssCostWriteoffDetail);
                        throw new RuntimeException("end");
                    } else {
                        cssCostWriteoffDetail.setAmountWriteoff(amountWriteoff.subtract(automatchSum.get("automatchSum")));
//                        automatchResult.add(cssCostWriteoffDetail);
                        throw new RuntimeException("end");
                    }
                }
            });
        } catch (Exception e) {
            if (!e.getMessage().equals("end")) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void insert(CssCostWriteoff cssCostWriteoff) {
        //校验对账单是否是最新数据
        CssPayment payment = cssPaymentService.getById(cssCostWriteoff.getPaymentId());
        if (payment == null) {
            throw new RuntimeException("对账单不存在，核销失败");
        }

        if (!payment.getRowUuid().equals(cssCostWriteoff.getRowUuid())) {
            throw new RuntimeException("对账单不是最新数据，请刷新页面后再操作");
        }
        //0.校验核销金额与对账金额是否匹配
        cssCostWriteoff.getCssCostWriteoffDetails().stream().forEach(cssCostWriteoffDetail -> {
            LambdaQueryWrapper<CssPaymentDetail> paymentDetailWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
            paymentDetailWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getPaymentId, cssCostWriteoff.getPaymentId()).eq(CssPaymentDetail::getCostId, cssCostWriteoffDetail.getCostId());
            CssPaymentDetail paymentDetail = cssPaymentDetailService.getOne(paymentDetailWrapper);
            if (paymentDetail == null) {
                throw new RuntimeException("单号为:" + cssCostWriteoffDetail.getAwbOrOrderNumber() + "的对账明细不存在,保存失败");
            }
            BigDecimal amountNoWriteoff = null;
            if (paymentDetail.getAmountPaymentWriteoff() == null) {
                amountNoWriteoff = paymentDetail.getAmountPayment();
            } else {
                amountNoWriteoff = paymentDetail.getAmountPayment().subtract(paymentDetail.getAmountPaymentWriteoff());
            }

            if (amountNoWriteoff.signum() == -1) {
                if (cssCostWriteoffDetail.getAmountWriteoff().signum() == 1) {
                    throw new RuntimeException("单号为:" + cssCostWriteoffDetail.getAwbOrOrderNumber() + "的核销明细本次核销金额不能>0,保存失败");
                } else {
                    if (amountNoWriteoff.abs().compareTo(cssCostWriteoffDetail.getAmountWriteoff().abs()) == -1) {
                        throw new RuntimeException("单号为:" + cssCostWriteoffDetail.getAwbOrOrderNumber() + "的核销明细本次核销金额不能超过未核销金额,保存失败");
                    }
                }
            } else if (amountNoWriteoff.signum() == 0) {
                if (cssCostWriteoffDetail.getAmountWriteoff().signum() != 0) {
                    throw new RuntimeException("单号为:" + cssCostWriteoffDetail.getAwbOrOrderNumber() + "的核销明细已经完全核销,不能再核销,保存失败");
                }
            } else {
                if (cssCostWriteoffDetail.getAmountWriteoff().signum() == -1) {
                    throw new RuntimeException("单号为:" + cssCostWriteoffDetail.getAwbOrOrderNumber() + "的核销明细本次核销金额不能<0,保存失败");
                } else {
                    if (amountNoWriteoff.compareTo(cssCostWriteoffDetail.getAmountWriteoff()) == -1) {
                        throw new RuntimeException("单号为:" + cssCostWriteoffDetail.getAwbOrOrderNumber() + "的核销明细本次核销金额不能超过未核销金额,保存失败");
                    }
                }
            }

        });

        //1.保存核销表
        cssCostWriteoff.setWriteoffNum(getPaymentNum(cssCostWriteoff.getBusinessScope()));
        cssCostWriteoff.setCreateTime(LocalDateTime.now());
        cssCostWriteoff.setCreatorId(SecurityUtils.getUser().getId());
        cssCostWriteoff.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        cssCostWriteoff.setOrgId(SecurityUtils.getUser().getOrgId());
        if(cssCostWriteoff.getFinancialAccountName() != null && !"".equals(cssCostWriteoff.getFinancialAccountName())){
            cssCostWriteoff.setFinancialAccountName(cssCostWriteoff.getFinancialAccountName().substring(0,cssCostWriteoff.getFinancialAccountName().lastIndexOf(" ")));
        }
        save(cssCostWriteoff);
        //2.保存核销明细表
        if (cssCostWriteoff.getCssCostWriteoffDetails().size() > 0) {
            cssCostWriteoff.getCssCostWriteoffDetails().stream().forEach(cssCostWriteoffDetail -> {
                cssCostWriteoffDetail.setOrgId(SecurityUtils.getUser().getOrgId());
                cssCostWriteoffDetail.setCostWriteoffId(cssCostWriteoff.getCostWriteoffId());
            });
            cssCostWriteoffDetailService.saveBatch(cssCostWriteoff.getCssCostWriteoffDetails());
        }
        //2.更新对账单表
        LambdaQueryWrapper<CssCostWriteoffDetail> cssCostWriteoffDetailLambdaQueryWrapper = Wrappers.<CssCostWriteoffDetail>lambdaQuery();
        cssCostWriteoffDetailLambdaQueryWrapper.eq(CssCostWriteoffDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostWriteoffDetail::getPaymentId, cssCostWriteoff.getPaymentId());
        HashMap<String, BigDecimal> amountWriteoffSum = new HashMap<>();
        amountWriteoffSum.put("amountWriteoffSum", BigDecimal.ZERO);
        cssCostWriteoffDetailService.list(cssCostWriteoffDetailLambdaQueryWrapper).stream().forEach(cssCostWriteoffDetail -> {
            if (cssCostWriteoffDetail.getAmountWriteoff() != null) {
                amountWriteoffSum.put("amountWriteoffSum", amountWriteoffSum.get("amountWriteoffSum").add(cssCostWriteoffDetail.getAmountWriteoff()));
            }
        });
        CssPayment cssPayment = cssPaymentService.getById(cssCostWriteoff.getPaymentId());
        cssPayment.setAmountPaymentWriteoff(amountWriteoffSum.get("amountWriteoffSum"));
        if (cssPayment.getAmountPayment().compareTo(cssPayment.getAmountPaymentWriteoff()) == 1) {
            cssPayment.setWriteoffComplete(0);
        } else if (cssPayment.getAmountPayment().compareTo(cssPayment.getAmountPaymentWriteoff()) == 0) {
            cssPayment.setWriteoffComplete(1);
        } else {
            throw new RuntimeException("该对账单核销总金额大于对账金额，无法保存");
        }
        cssPayment.setRowUuid(UUID.randomUUID().toString());
        cssPaymentService.updateById(cssPayment);
        //3.更新对账明细表
        cssCostWriteoff.getCssCostWriteoffDetails().stream().forEach(cssCostWriteoffDetail -> {
            LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailLambdaQueryWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
            cssPaymentDetailLambdaQueryWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, cssCostWriteoffDetail.getCostId()).eq(CssPaymentDetail::getPaymentId, cssCostWriteoffDetail.getPaymentId());
            CssPaymentDetail paymentDetail = cssPaymentDetailService.getOne(cssPaymentDetailLambdaQueryWrapper);
            LambdaQueryWrapper<CssCostWriteoffDetail> cssCostWriteoffDetailWrapper = Wrappers.<CssCostWriteoffDetail>lambdaQuery();
            cssCostWriteoffDetailWrapper.eq(CssCostWriteoffDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostWriteoffDetail::getPaymentId, cssCostWriteoff.getPaymentId()).eq(CssCostWriteoffDetail::getCostId, cssCostWriteoffDetail.getCostId());
            List<CssCostWriteoffDetail> writeoffDetails = cssCostWriteoffDetailService.list(cssCostWriteoffDetailWrapper);
            HashMap<String, BigDecimal> amountWriteoffDetailSum = new HashMap<>();
            amountWriteoffDetailSum.put("amountWriteoffDetailSum", BigDecimal.ZERO);
            writeoffDetails.stream().forEach(cssCostWriteoffDetail1 -> {
                if (cssCostWriteoffDetail1.getAmountWriteoff() != null) {
                    amountWriteoffDetailSum.put("amountWriteoffDetailSum", amountWriteoffDetailSum.get("amountWriteoffDetailSum").add(cssCostWriteoffDetail1.getAmountWriteoff()));
                }
            });
            if (paymentDetail.getAmountPayment().abs().compareTo(amountWriteoffDetailSum.get("amountWriteoffDetailSum").abs()) == -1) {
                throw new RuntimeException("单号：" + cssCostWriteoffDetail.getAwbOrOrderNumber() + "核销明细核销总金额超出对账明细对账金额");
            } else {
                paymentDetail.setAmountPaymentWriteoff(amountWriteoffDetailSum.get("amountWriteoffDetailSum"));
                cssPaymentDetailService.updateById(paymentDetail);
            }
        });
        //4.更新应付费用明细表cost
        LambdaQueryWrapper<CssCostWriteoff> cssCostWriteoffyWrapper = Wrappers.<CssCostWriteoff>lambdaQuery();
        cssCostWriteoffyWrapper.eq(CssCostWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostWriteoff::getBusinessScope, cssPayment.getBusinessScope());
        List<Integer> cssCostWriteoffIds = list(cssCostWriteoffyWrapper).stream().map(cssCostWriteoff1 -> cssCostWriteoff1.getCostWriteoffId()).collect(Collectors.toList());
        cssCostWriteoff.getCssCostWriteoffDetails().stream().forEach(cssCostWriteoffDetail -> {
            LambdaQueryWrapper<CssCostWriteoffDetail> cssCostWriteoffDetailWrapper = Wrappers.<CssCostWriteoffDetail>lambdaQuery();
            cssCostWriteoffDetailWrapper.eq(CssCostWriteoffDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostWriteoffDetail::getCostId, cssCostWriteoffDetail.getCostId()).in(CssCostWriteoffDetail::getCostWriteoffId, cssCostWriteoffIds);
            List<CssCostWriteoffDetail> writeoffDetails = cssCostWriteoffDetailService.list(cssCostWriteoffDetailWrapper);
            HashMap<String, BigDecimal> amountWriteoffDetailSum = new HashMap<>();
            amountWriteoffDetailSum.put("amountWriteoffDetailSum", BigDecimal.ZERO);
            writeoffDetails.stream().forEach(cssCostWriteoffDetail1 -> {
                if (cssCostWriteoffDetail1.getAmountWriteoff() != null) {
                    amountWriteoffDetailSum.put("amountWriteoffDetailSum", amountWriteoffDetailSum.get("amountWriteoffDetailSum").add(cssCostWriteoffDetail1.getAmountWriteoff()));
                }
            });
            if ("AE".equals(cssPayment.getBusinessScope()) || "AI".equals(cssPayment.getBusinessScope())) {
                AfCost afCost = afCostService.getById(cssCostWriteoffDetail.getCostId());
                if (afCost.getCostAmountPayment().abs().compareTo(amountWriteoffDetailSum.get("amountWriteoffDetailSum").abs()) == -1) {
                    throw new RuntimeException("单号：" + cssCostWriteoffDetail.getAwbOrOrderNumber() + "成本总核销金额超出成本对账金额");
                } else {
                    afCost.setCostAmountWriteoff(amountWriteoffDetailSum.get("amountWriteoffDetailSum"));
                    afCostService.updateById(afCost);
                }
            }
            if ("SE".equals(cssPayment.getBusinessScope()) || "SI".equals(cssPayment.getBusinessScope())) {
                ScCost scCost = scCostService.getById(cssCostWriteoffDetail.getCostId());
                if (scCost.getCostAmountPayment().abs().compareTo(amountWriteoffDetailSum.get("amountWriteoffDetailSum").abs()) == -1) {
                    throw new RuntimeException("单号：" + cssCostWriteoffDetail.getAwbOrOrderNumber() + "成本总核销金额超出成本对账金额");
                } else {
                    scCost.setCostAmountWriteoff(amountWriteoffDetailSum.get("amountWriteoffDetailSum"));
                    scCostService.updateById(scCost);
                }
            }

            if (cssPayment.getBusinessScope().startsWith("T")) {
                TcCost tcCost = tcCostService.getById(cssCostWriteoffDetail.getCostId());
                if (tcCost.getCostAmountPayment().abs().compareTo(amountWriteoffDetailSum.get("amountWriteoffDetailSum").abs()) == -1) {
                    throw new RuntimeException("单号：" + cssCostWriteoffDetail.getAwbOrOrderNumber() + "成本总核销金额超出成本对账金额");
                } else {
                    tcCost.setCostAmountWriteoff(amountWriteoffDetailSum.get("amountWriteoffDetailSum"));
                    tcCostService.updateById(tcCost);
                }
            }
            if (cssPayment.getBusinessScope().startsWith("L")) {
                LcCost lcCost = lcCostService.getById(cssCostWriteoffDetail.getCostId());
                if (lcCost.getCostAmountPayment().abs().compareTo(amountWriteoffDetailSum.get("amountWriteoffDetailSum").abs()) == -1) {
                    throw new RuntimeException("单号：" + cssCostWriteoffDetail.getAwbOrOrderNumber() + "成本总核销金额超出成本对账金额");
                } else {
                    lcCost.setCostAmountWriteoff(amountWriteoffDetailSum.get("amountWriteoffDetailSum"));
                    lcCostService.updateById(lcCost);
                }
            }
            if (cssPayment.getBusinessScope().equals("IO")) {
                IoCost ioCost = ioCostService.getById(cssCostWriteoffDetail.getCostId());
                if (ioCost.getCostAmountPayment().abs().compareTo(amountWriteoffDetailSum.get("amountWriteoffDetailSum").abs()) == -1) {
                    throw new RuntimeException("单号：" + cssCostWriteoffDetail.getAwbOrOrderNumber() + "成本总核销金额超出成本对账金额");
                } else {
                    ioCost.setCostAmountWriteoff(amountWriteoffDetailSum.get("amountWriteoffDetailSum"));
                    ioCostService.updateById(ioCost);
                }
            }

        });
        //6.更新订单表
        cssCostWriteoff.getCssCostWriteoffDetails().stream().map(cssCostWriteoffDetail -> cssCostWriteoffDetail.getOrderId()).distinct().forEach(orderId -> {
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
    public CssCostWriteoff getVoucherDate(Integer costWriteoffId) {
        return baseMapper.selectById(costWriteoffId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer costWriteoffId) {
        CssCostWriteoff costWriteoff = getById(costWriteoffId);

        if (costWriteoff == null) {
            throw new RuntimeException("核销单不存在，请刷新页面再操作");
        }
        //1.删除核销单
        removeById(costWriteoffId);
        //2.删除核销明细
        LambdaQueryWrapper<CssCostWriteoffDetail> writeoffDetailWrapper = Wrappers.<CssCostWriteoffDetail>lambdaQuery();
        writeoffDetailWrapper.eq(CssCostWriteoffDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostWriteoffDetail::getCostWriteoffId, costWriteoffId);
        List<CssCostWriteoffDetail> writeoffDetails = cssCostWriteoffDetailService.list(writeoffDetailWrapper);
        cssCostWriteoffDetailService.remove(writeoffDetailWrapper);
        //3.更新对账单
        LambdaQueryWrapper<CssCostWriteoffDetail> cssCostWriteoffDetailLambdaQueryWrapper = Wrappers.<CssCostWriteoffDetail>lambdaQuery();
        cssCostWriteoffDetailLambdaQueryWrapper.eq(CssCostWriteoffDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostWriteoffDetail::getPaymentId, costWriteoff.getPaymentId());
        HashMap<String, BigDecimal> amountWriteoffSum = new HashMap<>();
        amountWriteoffSum.put("amountWriteoffSum", BigDecimal.ZERO);
        cssCostWriteoffDetailService.list(cssCostWriteoffDetailLambdaQueryWrapper).stream().forEach(cssCostWriteoffDetail -> {
            if (cssCostWriteoffDetail.getAmountWriteoff() != null) {
                amountWriteoffSum.put("amountWriteoffSum", amountWriteoffSum.get("amountWriteoffSum").add(cssCostWriteoffDetail.getAmountWriteoff()));
            }
        });
        CssPayment cssPayment = cssPaymentService.getById(costWriteoff.getPaymentId());
        cssPayment.setAmountPaymentWriteoff(amountWriteoffSum.get("amountWriteoffSum"));
        if (cssPayment.getAmountPayment().signum() == -1) {
            cssPayment.setWriteoffComplete(null);
        } else {
            if (cssPayment.getAmountPayment().compareTo(cssPayment.getAmountPaymentWriteoff()) == 1) {
                if (cssPayment.getAmountPaymentWriteoff().compareTo(BigDecimal.ZERO) == 0) {
                    cssPayment.setWriteoffComplete(null);
                } else {
                    cssPayment.setWriteoffComplete(0);
                }
            } else {
                throw new RuntimeException("该对账单已核销金额大于对账金额");
            }
        }
        cssPayment.setRowUuid(UUID.randomUUID().toString());
        cssPaymentService.updateById(cssPayment);
        //4.更新对账单明细
        writeoffDetails.stream().forEach(cssCostWriteoffDetail -> {
            LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailLambdaQueryWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
            cssPaymentDetailLambdaQueryWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, cssCostWriteoffDetail.getCostId()).eq(CssPaymentDetail::getPaymentId, cssCostWriteoffDetail.getPaymentId());
            CssPaymentDetail paymentDetail = cssPaymentDetailService.getOne(cssPaymentDetailLambdaQueryWrapper);
            LambdaQueryWrapper<CssCostWriteoffDetail> cssCostWriteoffDetailWrapper = Wrappers.<CssCostWriteoffDetail>lambdaQuery();
            cssCostWriteoffDetailWrapper.eq(CssCostWriteoffDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostWriteoffDetail::getPaymentId, costWriteoff.getPaymentId()).eq(CssCostWriteoffDetail::getCostId, cssCostWriteoffDetail.getCostId());
            List<CssCostWriteoffDetail> cssWriteoffDetails = cssCostWriteoffDetailService.list(cssCostWriteoffDetailWrapper);
            HashMap<String, BigDecimal> amountWriteoffDetailSum = new HashMap<>();
            amountWriteoffDetailSum.put("amountWriteoffDetailSum", BigDecimal.ZERO);
            cssWriteoffDetails.stream().forEach(cssCostWriteoffDetail1 -> {
                if (cssCostWriteoffDetail1.getAmountWriteoff() != null) {
                    amountWriteoffDetailSum.put("amountWriteoffDetailSum", amountWriteoffDetailSum.get("amountWriteoffDetailSum").add(cssCostWriteoffDetail1.getAmountWriteoff()));
                }
            });
            if (paymentDetail.getAmountPayment().abs().compareTo(amountWriteoffDetailSum.get("amountWriteoffDetailSum").abs()) == -1) {
                throw new RuntimeException("单号：" + costWriteoff.getWriteoffNum() + "核销明细核销总金额超出对账明细对账金额");
            } else {
                paymentDetail.setAmountPaymentWriteoff(amountWriteoffDetailSum.get("amountWriteoffDetailSum"));
                cssPaymentDetailService.updateById(paymentDetail);
            }
        });
        //5.更新成本表cost
        LambdaQueryWrapper<CssCostWriteoff> cssCostWriteoffWrapper = Wrappers.<CssCostWriteoff>lambdaQuery();
        cssCostWriteoffWrapper.eq(CssCostWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostWriteoff::getBusinessScope, cssPayment.getBusinessScope());
        List<Integer> cssCostWriteoffIds = list(cssCostWriteoffWrapper).stream().map(cssCostWriteoff -> cssCostWriteoff.getCostWriteoffId()).collect(Collectors.toList());
        writeoffDetails.stream().forEach(cssCostWriteoffDetail -> {
            if (cssCostWriteoffIds.size() == 0) {
                if ("AE".equals(cssPayment.getBusinessScope()) || "AI".equals(cssPayment.getBusinessScope())) {
                    AfCost afCost = afCostService.getById(cssCostWriteoffDetail.getCostId());
                    afCost.setCostAmountWriteoff(null);
                    afCostService.updateById(afCost);
                } else if ("SE".equals(cssPayment.getBusinessScope()) || "SI".equals(cssPayment.getBusinessScope())) {
                    ScCost scCost = scCostService.getById(cssCostWriteoffDetail.getCostId());
                    scCost.setCostAmountWriteoff(null);
                    scCostService.updateById(scCost);
                } else if (cssPayment.getBusinessScope().startsWith("T")) {
                    TcCost tcCost = tcCostService.getById(cssCostWriteoffDetail.getCostId());
                    tcCost.setCostAmountWriteoff(null);
                    tcCostService.updateById(tcCost);
                } else if (cssPayment.getBusinessScope().startsWith("L")) {
                    LcCost lcCost = lcCostService.getById(cssCostWriteoffDetail.getCostId());
                    lcCost.setCostAmountWriteoff(null);
                    lcCostService.updateById(lcCost);
                } else if (cssPayment.getBusinessScope().equals("IO")) {
                    IoCost ioCost = ioCostService.getById(cssCostWriteoffDetail.getCostId());
                    ioCost.setCostAmountWriteoff(null);
                    ioCostService.updateById(ioCost);
                }
            } else {
                LambdaQueryWrapper<CssCostWriteoffDetail> cssCostWriteoffDetailWrapper = Wrappers.<CssCostWriteoffDetail>lambdaQuery();
                cssCostWriteoffDetailWrapper.eq(CssCostWriteoffDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostWriteoffDetail::getCostId, cssCostWriteoffDetail.getCostId()).in(CssCostWriteoffDetail::getCostWriteoffId, cssCostWriteoffIds);
                List<CssCostWriteoffDetail> cssWriteoffDetails = cssCostWriteoffDetailService.list(cssCostWriteoffDetailWrapper);
                HashMap<String, BigDecimal> amountWriteoffDetailSum = new HashMap<>();
                amountWriteoffDetailSum.put("amountWriteoffDetailSum", BigDecimal.ZERO);
                cssWriteoffDetails.stream().forEach(cssCostWriteoffDetail1 -> {
                    if (cssCostWriteoffDetail1.getAmountWriteoff() != null) {
                        amountWriteoffDetailSum.put("amountWriteoffDetailSum", amountWriteoffDetailSum.get("amountWriteoffDetailSum").add(cssCostWriteoffDetail1.getAmountWriteoff()));
                    }
                });
                if ("AE".equals(cssPayment.getBusinessScope()) || "AI".equals(cssPayment.getBusinessScope())) {
                    AfCost afCost = afCostService.getById(cssCostWriteoffDetail.getCostId());
                    if (afCost.getCostAmountPayment().abs().compareTo(amountWriteoffDetailSum.get("amountWriteoffDetailSum").abs()) == -1) {
                        throw new RuntimeException("单号：" + costWriteoff.getWriteoffNum() + "成本总核销金额超出成本对账金额");
                    } else {
                        afCost.setCostAmountWriteoff(amountWriteoffDetailSum.get("amountWriteoffDetailSum"));
                        afCostService.updateById(afCost);
                    }
                }
                if ("SE".equals(cssPayment.getBusinessScope()) || "SI".equals(cssPayment.getBusinessScope())) {
                    ScCost scCost = scCostService.getById(cssCostWriteoffDetail.getCostId());
                    if (scCost.getCostAmountPayment().abs().compareTo(amountWriteoffDetailSum.get("amountWriteoffDetailSum").abs()) == -1) {
                        throw new RuntimeException("单号：" + costWriteoff.getWriteoffNum() + "成本总核销金额超出成本对账金额");
                    } else {
                        scCost.setCostAmountWriteoff(amountWriteoffDetailSum.get("amountWriteoffDetailSum"));
                        scCostService.updateById(scCost);
                    }
                }

                if (cssPayment.getBusinessScope().startsWith("T")) {
                    TcCost tcCost = tcCostService.getById(cssCostWriteoffDetail.getCostId());
                    if (tcCost.getCostAmountPayment().abs().compareTo(amountWriteoffDetailSum.get("amountWriteoffDetailSum").abs()) == -1) {
                        throw new RuntimeException("单号：" + costWriteoff.getWriteoffNum() + "成本总核销金额超出成本对账金额");
                    } else {
                        tcCost.setCostAmountWriteoff(amountWriteoffDetailSum.get("amountWriteoffDetailSum"));
                        tcCostService.updateById(tcCost);
                    }
                }
                if (cssPayment.getBusinessScope().startsWith("L")) {
                    LcCost lcCost = lcCostService.getById(cssCostWriteoffDetail.getCostId());
                    if (lcCost.getCostAmountPayment().abs().compareTo(amountWriteoffDetailSum.get("amountWriteoffDetailSum").abs()) == -1) {
                        throw new RuntimeException("单号：" + costWriteoff.getWriteoffNum() + "成本总核销金额超出成本对账金额");
                    } else {
                        lcCost.setCostAmountWriteoff(amountWriteoffDetailSum.get("amountWriteoffDetailSum"));
                        lcCostService.updateById(lcCost);
                    }
                }
                if (cssPayment.getBusinessScope().equals("IO")) {
                    IoCost ioCost = ioCostService.getById(cssCostWriteoffDetail.getCostId());
                    if (ioCost.getCostAmountPayment().abs().compareTo(amountWriteoffDetailSum.get("amountWriteoffDetailSum").abs()) == -1) {
                        throw new RuntimeException("单号：" + costWriteoff.getWriteoffNum() + "成本总核销金额超出成本对账金额");
                    } else {
                        ioCost.setCostAmountWriteoff(amountWriteoffDetailSum.get("amountWriteoffDetailSum"));
                        ioCostService.updateById(ioCost);
                    }
                }
            }
        });
        //6.更新订单表
        writeoffDetails.stream().map(cssCostWriteoffDetail -> cssCostWriteoffDetail.getOrderId()).distinct().forEach(orderId -> {
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
    public CssCostWriteoff view(Integer costWriteoffId) {
        CssCostWriteoff cssCostWriteoff = getById(costWriteoffId);
        if (cssCostWriteoff == null) {
            throw new RuntimeException("核销单不存在，请刷新页面再操作");
        }
        cssCostWriteoff.setAmountWriteoffStr(formatWith2AndQFW(cssCostWriteoff.getAmountWriteoff()));
        CssPayment cssPayment = cssPaymentService.getById(cssCostWriteoff.getPaymentId());
        cssCostWriteoff.setPaymentNum(cssPayment.getPaymentNum());
        LambdaQueryWrapper<CssCostWriteoffDetail> wrapper = Wrappers.<CssCostWriteoffDetail>lambdaQuery();
        wrapper.eq(CssCostWriteoffDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostWriteoffDetail::getCostWriteoffId, costWriteoffId);
        List<CssCostWriteoffDetail> list = cssCostWriteoffDetailService.list(wrapper);
        list.stream().forEach(cssCostWriteoffDetail -> {
            LambdaQueryWrapper<CssPaymentDetail> cssPaymentDetailLambdaQueryWrapper = Wrappers.<CssPaymentDetail>lambdaQuery();
            cssPaymentDetailLambdaQueryWrapper.eq(CssPaymentDetail::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssPaymentDetail::getCostId, cssCostWriteoffDetail.getCostId()).eq(CssPaymentDetail::getPaymentId, cssCostWriteoffDetail.getPaymentId());
            CssPaymentDetail paymentDetail = cssPaymentDetailService.getOne(cssPaymentDetailLambdaQueryWrapper);
            cssCostWriteoffDetail.setAmountPayment(paymentDetail.getAmountPayment());
            cssCostWriteoffDetail.setAmountPaymentStr(formatWith2AndQFW(cssCostWriteoffDetail.getAmountPayment()) + " (" + cssCostWriteoffDetail.getCurrency() + ")");
            cssCostWriteoffDetail.setAmountPaymentWriteoff(paymentDetail.getAmountPaymentWriteoff());
            cssCostWriteoffDetail.setAmountPaymentWriteoffStr(paymentDetail.getAmountPaymentWriteoff() == null ? "0.00" + " (" + paymentDetail.getCurrency() + ")" : formatWith2AndQFW(paymentDetail.getAmountPaymentWriteoff()) + " (" + paymentDetail.getCurrency() + ")");
            if (paymentDetail.getAmountPaymentWriteoff() == null) {
                cssCostWriteoffDetail.setAmountPaymentNoWriteoff(paymentDetail.getAmountPayment());
            } else {
                cssCostWriteoffDetail.setAmountPaymentNoWriteoff(paymentDetail.getAmountPayment().subtract(paymentDetail.getAmountPaymentWriteoff()));
            }
            cssCostWriteoffDetail.setAmountPaymentNoWriteoffStr(formatWith2AndQFW(cssCostWriteoffDetail.getAmountPaymentNoWriteoff()) + " (" + cssCostWriteoffDetail.getCurrency() + ")");
            cssCostWriteoffDetail.setAmountWriteoffStr(formatWith2AndQFW(cssCostWriteoffDetail.getAmountWriteoff()) + " (" + cssCostWriteoffDetail.getCurrency() + ")");

            if ("AE".equals(cssPayment.getBusinessScope()) || "AI".equals(cssPayment.getBusinessScope())) {
                AfCost cost = afCostService.getById(cssCostWriteoffDetail.getCostId());
                cssCostWriteoffDetail.setServiceName(cost.getServiceName());
                AfOrder afOrder = afOrderService.getById(cssCostWriteoffDetail.getOrderId());
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
                ScOrder order = scOrderService.getById(cssCostWriteoffDetail.getOrderId());
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
                TcOrder order = tcOrderService.getById(cssCostWriteoffDetail.getOrderId());
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
                LcOrder order = lcOrderService.getById(cssCostWriteoffDetail.getOrderId());
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
                IoOrder order = ioOrderService.getById(cssCostWriteoffDetail.getOrderId());
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

        });
        list.stream().sorted((e1, e2) -> {
            if (e1.getFlightDate().compareTo(e2.getFlightDate()) == 0) {
                return e1.getAmountPaymentNoWriteoff().compareTo(e2.getAmountPaymentNoWriteoff());
            }
            return e1.getFlightDate().compareTo(e2.getFlightDate());
        }).collect(Collectors.toList());

        cssCostWriteoff.setCssCostWriteoffDetails(list);
        return cssCostWriteoff;
    }

    @Override
    public IPage getPage(Page page, CssCostWriteoff cssCostWriteoff) {
        LambdaQueryWrapper<CssCostWriteoff> wrapper = Wrappers.<CssCostWriteoff>lambdaQuery();
        if (StrUtil.isNotBlank(cssCostWriteoff.getAwbNumberOrOrderCode())) {
            List<Integer> orderIds = null;
            if (cssCostWriteoff.getBusinessScope().startsWith("A")) {
                LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
                orderWrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(AfOrder::getAwbNumber, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%").or(j -> j.like(AfOrder::getOrderCode, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%")));
                List<AfOrder> orderList = afOrderService.list(orderWrapper);
                if (orderList.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                orderIds = orderList.stream().map(AfOrder::getOrderId).collect(Collectors.toList());
            }
            if (cssCostWriteoff.getBusinessScope().startsWith("S")) {
                LambdaQueryWrapper<ScOrder> orderWrapper = Wrappers.<ScOrder>lambdaQuery();
                orderWrapper.eq(ScOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(ScOrder::getMblNumber, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%").or(j -> j.like(ScOrder::getOrderCode, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%")));
                List<ScOrder> orderList = scOrderService.list(orderWrapper);
                if (orderList.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                orderIds = orderList.stream().map(ScOrder::getOrderId).collect(Collectors.toList());
            }
            if (cssCostWriteoff.getBusinessScope().startsWith("T")) {
                LambdaQueryWrapper<TcOrder> orderWrapper = Wrappers.<TcOrder>lambdaQuery();
                orderWrapper.eq(TcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(TcOrder::getRwbNumber, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%").or(j -> j.like(TcOrder::getOrderCode, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%")));
                List<TcOrder> orderList = tcOrderService.list(orderWrapper);
                if (orderList.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                orderIds = orderList.stream().map(TcOrder::getOrderId).collect(Collectors.toList());
            }
            if (cssCostWriteoff.getBusinessScope().startsWith("L")) {
                LambdaQueryWrapper<LcOrder> orderWrapper = Wrappers.<LcOrder>lambdaQuery();
                orderWrapper.eq(LcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(LcOrder::getCustomerNumber, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%").or(j -> j.like(LcOrder::getOrderCode, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%")));
                List<LcOrder> orderList = lcOrderService.list(orderWrapper);
                if (orderList.size() == 0) {
                    page.setTotal(0);
                    page.setRecords(new ArrayList());
                    return page;
                }
                orderIds = orderList.stream().map(LcOrder::getOrderId).collect(Collectors.toList());
            }
            if (cssCostWriteoff.getBusinessScope().equals("IO")) {
                LambdaQueryWrapper<IoOrder> orderWrapper = Wrappers.<IoOrder>lambdaQuery();
                orderWrapper.eq(IoOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(IoOrder::getCustomerNumber, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%").or(j -> j.like(IoOrder::getOrderCode, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%")));
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
        wrapper.eq(CssCostWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostWriteoff::getBusinessScope, cssCostWriteoff.getBusinessScope());
        if (StrUtil.isNotBlank(cssCostWriteoff.getCurrency())) {
            wrapper.eq(CssCostWriteoff::getCurrency, cssCostWriteoff.getCurrency());
        }
        if (StrUtil.isNotBlank(cssCostWriteoff.getCustomerName())) {
            wrapper.like(CssCostWriteoff::getCustomerName, "%" + cssCostWriteoff.getCustomerName() + "%");
        }
        if (StrUtil.isNotBlank(cssCostWriteoff.getPaymentNum())) {
            LambdaQueryWrapper<CssPayment> paymentWrapper = Wrappers.<CssPayment>lambdaQuery();
            paymentWrapper.eq(CssPayment::getOrgId, SecurityUtils.getUser().getOrgId()).like(CssPayment::getPaymentNum, "%" + cssCostWriteoff.getPaymentNum() + "%");
            List<Integer> paymentIds = cssPaymentService.list(paymentWrapper).stream().map(cssPayment -> cssPayment.getPaymentId()).collect(Collectors.toList());
            if (paymentIds.size() == 0) {
                page.setTotal(0);
                page.setRecords(new ArrayList());
                return page;
            }
            wrapper.in(CssCostWriteoff::getPaymentId, paymentIds);
        }
        if (cssCostWriteoff.getWriteoffDateStart() != null) {
            wrapper.ge(CssCostWriteoff::getWriteoffDate, cssCostWriteoff.getWriteoffDateStart());
        }
        if (cssCostWriteoff.getWriteoffDateEnd() != null) {
            wrapper.le(CssCostWriteoff::getWriteoffDate, cssCostWriteoff.getWriteoffDateEnd());
        }

        if (StrUtil.isNotBlank(cssCostWriteoff.getWriteoffNum())) {
            wrapper.like(CssCostWriteoff::getWriteoffNum, "%" + cssCostWriteoff.getWriteoffNum() + "%");
        }
        if (StrUtil.isNotBlank(cssCostWriteoff.getCreatorName())) {
            wrapper.like(CssCostWriteoff::getCreatorName, cssCostWriteoff.getCreatorName());
        }
        wrapper.orderByDesc(CssCostWriteoff::getWriteoffDate);
        IPage<CssCostWriteoff> result = page(page, wrapper);
        result.getRecords().stream().forEach(writeoff -> {
            CssPayment cssPayment = cssPaymentService.getById(writeoff.getPaymentId());
            writeoff.setAmountPayment(cssPayment.getAmountPayment());
            writeoff.setPaymentNum(cssPayment.getPaymentNum());
            writeoff.setAmountPaymentStr(formatWith2AndQFW(writeoff.getAmountPayment()) + " (" + writeoff.getCurrency() + ")");
            writeoff.setAmountWriteoffStr(formatWith2AndQFW(writeoff.getAmountWriteoff()) + " (" + writeoff.getCurrency() + ")");
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
                    amountWriteoffBuffer.append(formatWith2AndQFW(entry.getValue()) + " (" + entry.getKey() + ")");
                } else {
                    amountWriteoffBuffer.append("|").append(formatWith2AndQFW(entry.getValue()) + " (" + entry.getKey() + ")");
                }
            }
            CssCostWriteoff writeoff = new CssCostWriteoff();
            writeoff.setAmountWriteoffStr(amountWriteoffBuffer.toString());
            result.getRecords().add(writeoff);
        }
        return result;
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

    private String getPaymentNum(String businessScope) {
        String numberPrefix = "-PW-" + DateTimeFormatter.ofPattern("yyMMdd").format(LocalDate.now());
        LambdaQueryWrapper<CssCostWriteoff> wrapper = Wrappers.<CssCostWriteoff>lambdaQuery();
        wrapper.eq(CssCostWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).like(CssCostWriteoff::getWriteoffNum, "%" + numberPrefix + "%").orderByDesc(CssCostWriteoff::getWriteoffNum).last(" limit 1");

        CssCostWriteoff cssCostWriteoff = getOne(wrapper);

        String numberSuffix = "";
        if (cssCostWriteoff == null) {
            numberSuffix = "0001";
        } else if (cssCostWriteoff.getWriteoffNum().substring(cssCostWriteoff.getWriteoffNum().length() - 4).equals("9999")) {
            throw new RuntimeException("当天核销单已满无法创建");
        } else {
            String n = Integer.valueOf(cssCostWriteoff.getWriteoffNum().substring(cssCostWriteoff.getWriteoffNum().length() - 4)) + 1 + "";
            numberSuffix = "0000".substring(0, 4 - n.length()) + n;
        }
        return businessScope + numberPrefix + numberSuffix;
    }

    @Override
    public List<CssCostWriteoff> exportWriteoffExcel(CssCostWriteoff cssCostWriteoff) {
        LambdaQueryWrapper<CssCostWriteoff> wrapper = Wrappers.<CssCostWriteoff>lambdaQuery();
        if (StrUtil.isNotBlank(cssCostWriteoff.getAwbNumberOrOrderCode())) {
            List<Integer> orderIds = null;
            if (cssCostWriteoff.getBusinessScope().startsWith("A")) {
                LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
                orderWrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(AfOrder::getAwbNumber, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%").or(j -> j.like(AfOrder::getOrderCode, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%")));
                List<AfOrder> orderList = afOrderService.list(orderWrapper);
                if (orderList.size() == 0) {
                    //page.setTotal(0);
                    //page.setRecords(new ArrayList());
                    return null;
                }
                orderIds = orderList.stream().map(AfOrder::getOrderId).collect(Collectors.toList());
            }
            if (cssCostWriteoff.getBusinessScope().startsWith("S")) {
                LambdaQueryWrapper<ScOrder> orderWrapper = Wrappers.<ScOrder>lambdaQuery();
                orderWrapper.eq(ScOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(ScOrder::getMblNumber, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%").or(j -> j.like(ScOrder::getOrderCode, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%")));
                List<ScOrder> orderList = scOrderService.list(orderWrapper);
                if (orderList.size() == 0) {
                    //page.setTotal(0);
                    //page.setRecords(new ArrayList());
                    return null;
                }
                orderIds = orderList.stream().map(ScOrder::getOrderId).collect(Collectors.toList());
            }
            if (cssCostWriteoff.getBusinessScope().startsWith("T")) {
                LambdaQueryWrapper<TcOrder> orderWrapper = Wrappers.<TcOrder>lambdaQuery();
                orderWrapper.eq(TcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(TcOrder::getRwbNumber, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%").or(j -> j.like(TcOrder::getOrderCode, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%")));
                List<TcOrder> orderList = tcOrderService.list(orderWrapper);
                if (orderList.size() == 0) {
                    //page.setTotal(0);
                    //page.setRecords(new ArrayList());
                    return null;
                }
                orderIds = orderList.stream().map(TcOrder::getOrderId).collect(Collectors.toList());
            }
            if (cssCostWriteoff.getBusinessScope().startsWith("L")) {
                LambdaQueryWrapper<LcOrder> orderWrapper = Wrappers.<LcOrder>lambdaQuery();
                orderWrapper.eq(LcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(LcOrder::getCustomerNumber, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%").or(j -> j.like(LcOrder::getOrderCode, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%")));
                List<LcOrder> orderList = lcOrderService.list(orderWrapper);
                if (orderList.size() == 0) {
                    return null;
                }
                orderIds = orderList.stream().map(LcOrder::getOrderId).collect(Collectors.toList());
            }
            if (cssCostWriteoff.getBusinessScope().equals("IO")) {
                LambdaQueryWrapper<IoOrder> orderWrapper = Wrappers.<IoOrder>lambdaQuery();
                orderWrapper.eq(IoOrder::getOrgId, SecurityUtils.getUser().getOrgId()).and(i -> i.like(IoOrder::getCustomerNumber, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%").or(j -> j.like(IoOrder::getOrderCode, "%" + cssCostWriteoff.getAwbNumberOrOrderCode() + "%")));
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
                    //page.setTotal(0);
                    //page.setRecords(new ArrayList());
                    return null;
                }
                List<Integer> costWriteoffIds = detailList.stream().map(CssCostWriteoffDetail::getCostWriteoffId).distinct().collect(Collectors.toList());
                wrapper.in(CssCostWriteoff::getCostWriteoffId, costWriteoffIds);
            }
        }
        wrapper.eq(CssCostWriteoff::getOrgId, SecurityUtils.getUser().getOrgId()).eq(CssCostWriteoff::getBusinessScope, cssCostWriteoff.getBusinessScope());
        if (StrUtil.isNotBlank(cssCostWriteoff.getCurrency())) {
            wrapper.eq(CssCostWriteoff::getCurrency, cssCostWriteoff.getCurrency());
        }
        if (StrUtil.isNotBlank(cssCostWriteoff.getCustomerName())) {
            wrapper.like(CssCostWriteoff::getCustomerName, "%" + cssCostWriteoff.getCustomerName() + "%");
        }
        if (StrUtil.isNotBlank(cssCostWriteoff.getPaymentNum())) {
            LambdaQueryWrapper<CssPayment> paymentWrapper = Wrappers.<CssPayment>lambdaQuery();
            paymentWrapper.eq(CssPayment::getOrgId, SecurityUtils.getUser().getOrgId()).like(CssPayment::getPaymentNum, "%" + cssCostWriteoff.getPaymentNum() + "%");
            List<Integer> paymentIds = cssPaymentService.list(paymentWrapper).stream().map(cssPayment -> cssPayment.getPaymentId()).collect(Collectors.toList());
            if (paymentIds.size() == 0) {
                //page.setTotal(0);
                //page.setRecords(new ArrayList());
                return null;
            }
            wrapper.in(CssCostWriteoff::getPaymentId, paymentIds);
        }
        if (cssCostWriteoff.getWriteoffDateStart() != null) {
            wrapper.ge(CssCostWriteoff::getWriteoffDate, cssCostWriteoff.getWriteoffDateStart());
        }
        if (cssCostWriteoff.getWriteoffDateEnd() != null) {
            wrapper.le(CssCostWriteoff::getWriteoffDate, cssCostWriteoff.getWriteoffDateEnd());
        }

        if (StrUtil.isNotBlank(cssCostWriteoff.getWriteoffNum())) {
            wrapper.like(CssCostWriteoff::getWriteoffNum, "%" + cssCostWriteoff.getWriteoffNum() + "%");
        }
        if (StrUtil.isNotBlank(cssCostWriteoff.getCreatorName())) {
            wrapper.like(CssCostWriteoff::getCreatorName, cssCostWriteoff.getCreatorName());
        }
        wrapper.orderByDesc(CssCostWriteoff::getWriteoffDate);
        //IPage<CssCostWriteoff> result = page(page, wrapper);
        List<CssCostWriteoff> result = baseMapper.selectList(wrapper);
        result.stream().forEach(writeoff -> {
            CssPayment cssPayment = cssPaymentService.getById(writeoff.getPaymentId());
            writeoff.setAmountPayment(cssPayment.getAmountPayment());
            writeoff.setPaymentNum(cssPayment.getPaymentNum());
            writeoff.setAmountPaymentStr(formatWith2AndQFW(writeoff.getAmountPayment()) + " (" + writeoff.getCurrency() + ")");
            writeoff.setAmountWriteoffStr(formatWith2AndQFW(writeoff.getAmountWriteoff()) + " (" + writeoff.getCurrency() + ")");
        });
        //拼接合计
        if (result.size() != 0) {
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
                    amountWriteoffBuffer.append(formatWith2AndQFW(entry.getValue()) + " (" + entry.getKey() + ")");
                } else {
                    amountWriteoffBuffer.append(String.valueOf((char) 10)).append(formatWith2AndQFW(entry.getValue()) + " (" + entry.getKey() + ")");
                }
            }
            CssCostWriteoff writeoff = new CssCostWriteoff();
            writeoff.setAmountWriteoffStr(amountWriteoffBuffer.toString());
            result.add(writeoff);
        }
        return result;
    }
}
