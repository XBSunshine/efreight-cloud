package com.efreight.sc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.core.feign.RemoteServiceToAF;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.remoteVo.AfOrder;
import com.efreight.common.remoteVo.AfOrderFiles;
import com.efreight.common.remoteVo.Airport;
import com.efreight.common.remoteVo.OrderForVL;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.common.security.vo.CurrencyRateVo;
import com.efreight.sc.dao.VlOrderFilesMapper;
import com.efreight.sc.entity.*;
import com.efreight.sc.dao.VlOrderMapper;
import com.efreight.sc.entity.view.AfVPrmCoop;
import com.efreight.sc.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.sc.utils.FieldValUtils;
import com.efreight.sc.utils.LoginUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
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
 * VL 订单管理 派车订单 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-31
 */
@Service
@AllArgsConstructor
public class VlOrderServiceImpl extends ServiceImpl<VlOrderMapper, VlOrder> implements VlOrderService {

    private final AfVPrmCoopService afVPrmCoopService;
    private final VlLogService vlLogService;
    private final VlOrderDetailOrderService vlOrderDetailOrderService;
    private final LcTruckService lcTruckService;
    private final AfCostService afCostService;
    private final CostService costService;
    private final LcCostService lcCostService;
    private final TcCostService tcCostService;

    private final RemoteServiceToAF remoteServiceToAF;
    private final OrderService orderService;
    private final TcOrderService tcOrderService;
    private final LcOrderService lcOrderService;

    private final VlOrderFilesMapper vlOrderFilesMapper;
    private final OrderFilesService orderFilesService;
    private final TcOrderFilesService tcOrderFilesService;
    private final LcOrderFilesService lcOrderFilesService;

    @Override
    public IPage getPage(Page page, VlOrder vlOrder) {
        LambdaQueryWrapper<VlOrder> wrapper = getWrapper(vlOrder);
        if (wrapper == null) {
            page.setRecords(new ArrayList());
            page.setTotal(0);
            return page;
        }
        IPage<VlOrder> result = page(page, wrapper);
        fixRecord(result.getRecords());
        return result;
    }

    @Override
    public VlOrder total(VlOrder vlOrder) {
        LambdaQueryWrapper<VlOrder> wrapper = getWrapper(vlOrder);
        if (wrapper == null) {
            return null;
        }
        VlOrder total = new VlOrder();
        list(wrapper).stream().forEach(order -> {
            total.setOrderStatus("合计:");
            Map<String, BigDecimal> weightAndVolume = getWeightAndVolumeNew(order.getOrderId());
            //统计重量
            if (total.getWeight() == null) {
                total.setWeight(weightAndVolume.get("weight"));
            } else {
                total.setWeight(total.getWeight().add(weightAndVolume.get("weight")));
            }
            //统计体积
            if (total.getVolume() == null) {
                total.setVolume(weightAndVolume.get("volume"));
            } else {
                total.setVolume(total.getVolume().add(weightAndVolume.get("volume")));
            }
        });
        if (StrUtil.isBlank(total.getOrderStatus())) {
            return null;
        }
        total.setWeightStr(FormatUtils.formatWithQWF(total.getWeight(), 1));
        total.setVolumeStr(FormatUtils.formatWithQWF(total.getVolume(), 3));
        return total;
    }

    @Override
    public VlOrder viewForFinishOrder(Integer orderId) {
        VlOrder vlOrder = getById(orderId);
        if ("强制关闭".equals(vlOrder.getOrderStatus())) {
            throw new RuntimeException("该派车单已强制关闭,无法进行订单完成操作,请刷新页面");
        }
        if ("完成订单".equals(vlOrder.getOrderStatus())) {
            throw new RuntimeException("该派车单已完成,无需重复操作,请刷新页面");
        }
        //查询客户
        LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopLambdaQueryWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
        afVPrmCoopLambdaQueryWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfVPrmCoop::getCoopId, vlOrder.getCoopId());
        AfVPrmCoop coop = afVPrmCoopService.getOne(afVPrmCoopLambdaQueryWrapper);
        if (coop != null) {
            vlOrder.setCoopName(coop.getCoopName());
        }
        //查询订单详情
        //LambdaQueryWrapper<VlOrderDetailOrder> vlOrderDetailOrderLambdaQueryWrapper = Wrappers.<VlOrderDetailOrder>lambdaQuery();
        //vlOrderDetailOrderLambdaQueryWrapper.eq(VlOrderDetailOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(VlOrderDetailOrder::getVlOrderId, orderId);
        //List<VlOrderDetailOrder> detailOrderList = vlOrderDetailOrderService.list(vlOrderDetailOrderLambdaQueryWrapper);

        List<VlOrderDetailOrder> detailOrderList = baseMapper.selectVlOrderDetailOrderList(SecurityUtils.getUser().getOrgId(),orderId);
        String businessScopes = detailOrderList.stream().map(item -> item.getBusinessScope()).distinct().collect(Collectors.joining("','"));
        Map<String, List<com.efreight.common.remoteVo.Service>> groupBy = new HashMap();
        if(StringUtils.isNotBlank(businessScopes)){
            businessScopes = "'" + businessScopes + "'";
            List<com.efreight.common.remoteVo.Service> businessScopeList = baseMapper.getServices(SecurityUtils.getUser().getOrgId(),businessScopes);
            groupBy.putAll(businessScopeList.stream().collect(Collectors.groupingBy(com.efreight.common.remoteVo.Service::getBusinessScope)));
        }

        //封装分摊金额
        if (vlOrder.getCostAmount() != null) {
            //成本形式为总价
            Map<String, Integer> countMap = new HashMap<>();
            Map<String, BigDecimal> sumMap = new HashMap<>();
            if (vlOrder.getCostShareMethod().equals("订单")) {
                countMap.put("count", 0);
            } else {
                sumMap.put("sum", BigDecimal.ZERO);
            }
            detailOrderList.stream().forEach(vlOrderDetailOrder -> {
                vlOrderDetailOrder.setServices(groupBy.get(vlOrderDetailOrder.getBusinessScope()));
                insertOrderInfo(vlOrderDetailOrder, false);
                if (vlOrder.getCostShareMethod().equals("订单")) {
                    countMap.put("count", countMap.get("count") + 1);
                } else if (vlOrder.getCostShareMethod().equals("计重")) {
                    sumMap.put("sum", sumMap.get("sum").add(vlOrderDetailOrder.getConfirmChargeWeight() == null ? BigDecimal.valueOf(vlOrderDetailOrder.getPlanChargeWeight()) : BigDecimal.valueOf(vlOrderDetailOrder.getConfirmChargeWeight())));
                } else if (vlOrder.getCostShareMethod().equals("毛重")) {
                    sumMap.put("sum", sumMap.get("sum").add(vlOrderDetailOrder.getConfirmWeight() == null ? vlOrderDetailOrder.getPlanWeight() : vlOrderDetailOrder.getConfirmWeight()));
                } else if (vlOrder.getCostShareMethod().equals("体积")) {
                    sumMap.put("sum", sumMap.get("sum").add(vlOrderDetailOrder.getConfirmVolume() == null ? BigDecimal.valueOf(vlOrderDetailOrder.getPlanVolume()) : BigDecimal.valueOf(vlOrderDetailOrder.getConfirmVolume())));
                }
            });
            Map<String, Integer> index = new HashMap<>();
            index.put("index", 0);
            HashMap<String, BigDecimal> orderSum = new HashMap<>();
            orderSum.put("orderSum", BigDecimal.ZERO);
            detailOrderList.stream().forEach(vlOrderDetailOrder -> {
                if (index.get("index").equals(detailOrderList.size() - 1)) {
                    vlOrderDetailOrder.setCostAmount(vlOrder.getCostAmount().subtract(orderSum.get("orderSum")).setScale(2, BigDecimal.ROUND_HALF_UP));
                } else {
                    if (vlOrder.getCostShareMethod().equals("订单")) {
                        vlOrderDetailOrder.setCostAmount(vlOrder.getCostAmount().divide(BigDecimal.valueOf(countMap.get("count")), 2, BigDecimal.ROUND_HALF_UP));
                    } else if (vlOrder.getCostShareMethod().equals("计重")) {
                        BigDecimal chargeWeight = vlOrderDetailOrder.getConfirmChargeWeight() == null ? BigDecimal.valueOf(vlOrderDetailOrder.getPlanChargeWeight()) : BigDecimal.valueOf(vlOrderDetailOrder.getConfirmChargeWeight());
                        vlOrderDetailOrder.setCostAmount(chargeWeight.multiply(vlOrder.getCostAmount()).divide(sumMap.get("sum"), 2, BigDecimal.ROUND_HALF_UP));
                    } else if (vlOrder.getCostShareMethod().equals("毛重")) {
                        BigDecimal weight = vlOrderDetailOrder.getConfirmWeight() == null ? vlOrderDetailOrder.getPlanWeight() : vlOrderDetailOrder.getConfirmWeight();
                        vlOrderDetailOrder.setCostAmount(weight.multiply(vlOrder.getCostAmount()).divide(sumMap.get("sum"), 2, BigDecimal.ROUND_HALF_UP));
                    } else if (vlOrder.getCostShareMethod().equals("体积")) {
                        BigDecimal volume = vlOrderDetailOrder.getConfirmVolume() == null ? BigDecimal.valueOf(vlOrderDetailOrder.getPlanVolume()) : BigDecimal.valueOf(vlOrderDetailOrder.getConfirmVolume());
                        vlOrderDetailOrder.setCostAmount(volume.multiply(vlOrder.getCostAmount()).divide(sumMap.get("sum"), 2, BigDecimal.ROUND_HALF_UP));
                    }
                    orderSum.put("orderSum", orderSum.get("orderSum").add(vlOrderDetailOrder.getCostAmount()));
                }
                vlOrderDetailOrder.setCostAmountStr(FormatUtils.formatWithQWF(vlOrderDetailOrder.getCostAmount(), 2) + "(" + vlOrderDetailOrder.getCostCurrecnyCode() + ")");
                index.put("index", index.get("index") + 1);
            });
        } else if (vlOrder.getCostUnitprice() != null) {
            //成本形式为单价
            detailOrderList.stream().forEach(vlOrderDetailOrder -> {
                vlOrderDetailOrder.setServices(groupBy.get(vlOrderDetailOrder.getBusinessScope()));
                insertOrderInfo(vlOrderDetailOrder, false);
                if (vlOrder.getCostShareMethod().equals("计重")) {
                    vlOrderDetailOrder.setCostAmount(vlOrder.getCostUnitprice().multiply(vlOrderDetailOrder.getConfirmChargeWeight() == null ? BigDecimal.valueOf(vlOrderDetailOrder.getPlanChargeWeight()) : BigDecimal.valueOf(vlOrderDetailOrder.getConfirmChargeWeight())).setScale(2, BigDecimal.ROUND_HALF_UP));
                } else if (vlOrder.getCostShareMethod().equals("订单")) {
                    vlOrderDetailOrder.setCostAmount(vlOrder.getCostUnitprice().setScale(2, BigDecimal.ROUND_HALF_UP));
                } else if (vlOrder.getCostShareMethod().equals("毛重")) {
                    vlOrderDetailOrder.setCostAmount(vlOrder.getCostUnitprice().multiply(vlOrderDetailOrder.getConfirmWeight() == null ? vlOrderDetailOrder.getPlanWeight() : vlOrderDetailOrder.getConfirmWeight()).setScale(2, BigDecimal.ROUND_HALF_UP));
                } else if (vlOrder.getCostShareMethod().equals("体积")) {
                    vlOrderDetailOrder.setCostAmount(vlOrder.getCostUnitprice().multiply(vlOrderDetailOrder.getConfirmVolume() == null ? BigDecimal.valueOf(vlOrderDetailOrder.getPlanVolume()) : BigDecimal.valueOf(vlOrderDetailOrder.getConfirmVolume())).setScale(2, BigDecimal.ROUND_HALF_UP));
                }
                vlOrderDetailOrder.setCostAmountStr(FormatUtils.formatWithQWF(vlOrderDetailOrder.getCostAmount(), 2) + "(" + vlOrderDetailOrder.getCostCurrecnyCode() + ")");
            });
        }

        vlOrder.setDetailOrderList(detailOrderList);
        if (vlOrder.getCostUnitprice() != null) {
            vlOrder.setCostPriceAmount(vlOrder.getCostUnitprice());
            vlOrder.setCostPriceType("单价");
        } else if (vlOrder.getCostAmount() != null) {
            vlOrder.setCostPriceAmount(vlOrder.getCostAmount());
            vlOrder.setCostPriceType("总价");
        }
        return vlOrder;
    }

    private void insertOrderInfo(VlOrderDetailOrder vlOrderDetailOrder, boolean ifFinish) {
        if (vlOrderDetailOrder.getBusinessScope().startsWith("A")) {
            //AfOrder afOrder = remoteServiceToAF.getAfOrderById(vlOrderDetailOrder.getOrderId()).getData();
            if (!ifFinish) {
                vlOrderDetailOrder.setPlanChargeWeight(vlOrderDetailOrder.getPlanChargeWeightNew().doubleValue());
                vlOrderDetailOrder.setPlanDensity(vlOrderDetailOrder.getPlanDensityNew());
                vlOrderDetailOrder.setPlanPieces(vlOrderDetailOrder.getPlanPiecesNew());
                vlOrderDetailOrder.setPlanVolume(vlOrderDetailOrder.getPlanVolumeNew());
                vlOrderDetailOrder.setPlanWeight(vlOrderDetailOrder.getPlanWeightNew());
                vlOrderDetailOrder.setConfirmChargeWeight(vlOrderDetailOrder.getConfirmChargeWeightNew());
                vlOrderDetailOrder.setConfirmPieces(vlOrderDetailOrder.getConfirmPiecesNew());
                vlOrderDetailOrder.setConfirmVolume(vlOrderDetailOrder.getConfirmVolumeNew());
                vlOrderDetailOrder.setConfirmWeight(vlOrderDetailOrder.getConfirmWeightNew());
            }
            if ("AE".equals(vlOrderDetailOrder.getBusinessScope())) {
                vlOrderDetailOrder.setAwbNumber(vlOrderDetailOrder.getAwbNumber());
            } else {
                if (StrUtil.isNotBlank(vlOrderDetailOrder.getAwbNumber()) && StrUtil.isNotBlank(vlOrderDetailOrder.getHawbNumber())) {
                    vlOrderDetailOrder.setAwbNumber(vlOrderDetailOrder.getAwbNumber() + "_" + vlOrderDetailOrder.getHawbNumber());
                } else if (StrUtil.isNotBlank(vlOrderDetailOrder.getAwbNumber())) {
                    vlOrderDetailOrder.setAwbNumber(vlOrderDetailOrder.getAwbNumber());
                } else if (StrUtil.isNotBlank(vlOrderDetailOrder.getHawbNumber())) {
                    vlOrderDetailOrder.setAwbNumber(vlOrderDetailOrder.getHawbNumber());
                }
            }
            //vlOrderDetailOrder.setOrderCode(afOrder.getOrderCode());
            //vlOrderDetailOrder.setOrderUuid(afOrder.getOrderUuid());
            //vlOrderDetailOrder.setCustomerNumber(afOrder.getCustomerNumber());
        } else if (vlOrderDetailOrder.getBusinessScope().startsWith("S")) {
            //Order scOrder = orderService.view(vlOrderDetailOrder.getOrderId());
            if (!ifFinish) {
                vlOrderDetailOrder.setPlanChargeWeight(vlOrderDetailOrder.getPlanChargeWeightNew() != null ? vlOrderDetailOrder.getPlanChargeWeightNew().multiply(BigDecimal.valueOf(1000)).doubleValue() : null);
                vlOrderDetailOrder.setPlanPieces(vlOrderDetailOrder.getPlanPiecesNew());
                vlOrderDetailOrder.setPlanVolume(vlOrderDetailOrder.getPlanVolumeNew() != null ? vlOrderDetailOrder.getPlanVolumeNew().doubleValue() : null);
                vlOrderDetailOrder.setPlanWeight(vlOrderDetailOrder.getPlanWeightNew());
            }
            if (StrUtil.isNotBlank(vlOrderDetailOrder.getAwbNumber()) && StrUtil.isNotBlank(vlOrderDetailOrder.getHawbNumber())) {
                vlOrderDetailOrder.setAwbNumber(vlOrderDetailOrder.getAwbNumber() + "_" + vlOrderDetailOrder.getHawbNumber());
            } else if (StrUtil.isNotBlank(vlOrderDetailOrder.getAwbNumber())) {
                vlOrderDetailOrder.setAwbNumber(vlOrderDetailOrder.getAwbNumber());
            } else if (StrUtil.isNotBlank(vlOrderDetailOrder.getHawbNumber())) {
                vlOrderDetailOrder.setAwbNumber(vlOrderDetailOrder.getHawbNumber());
            }
            //vlOrderDetailOrder.setOrderCode(scOrder.getOrderCode());
            //vlOrderDetailOrder.setOrderUuid(scOrder.getOrderUuid());
            //vlOrderDetailOrder.setCustomerNumber(scOrder.getCustomerNumber());
        } else if (vlOrderDetailOrder.getBusinessScope().startsWith("T")) {
            //TcOrder tcOrder = tcOrderService.view(vlOrderDetailOrder.getOrderId());
            if (!ifFinish) {
                vlOrderDetailOrder.setPlanChargeWeight(vlOrderDetailOrder.getPlanChargeWeightNew() != null ? vlOrderDetailOrder.getPlanChargeWeightNew().multiply(BigDecimal.valueOf(1000)).doubleValue() : null);
                vlOrderDetailOrder.setPlanPieces(vlOrderDetailOrder.getPlanPiecesNew());
                vlOrderDetailOrder.setPlanVolume(vlOrderDetailOrder.getPlanVolumeNew() != null ? vlOrderDetailOrder.getPlanVolumeNew().doubleValue() : null);
                vlOrderDetailOrder.setPlanWeight(vlOrderDetailOrder.getPlanWeightNew());
            }
            //vlOrderDetailOrder.setAwbNumber(tcOrder.getRwbNumber());
            //vlOrderDetailOrder.setOrderCode(tcOrder.getOrderCode());
            //vlOrderDetailOrder.setOrderUuid(tcOrder.getOrderUuid());
            //vlOrderDetailOrder.setCustomerNumber(tcOrder.getCustomerNumber());
        } else if (vlOrderDetailOrder.getBusinessScope().startsWith("L")) {
            //LcOrder lcOrder = lcOrderService.view(vlOrderDetailOrder.getOrderId());
            if (!ifFinish) {
                vlOrderDetailOrder.setPlanChargeWeight(vlOrderDetailOrder.getPlanChargeWeightNew() != null ? vlOrderDetailOrder.getPlanChargeWeightNew().doubleValue() : null);
                vlOrderDetailOrder.setPlanDensity(vlOrderDetailOrder.getPlanDensityNew());
                vlOrderDetailOrder.setPlanPieces(vlOrderDetailOrder.getPlanPiecesNew());
                vlOrderDetailOrder.setPlanVolume(vlOrderDetailOrder.getPlanVolumeNew() != null ? vlOrderDetailOrder.getPlanVolumeNew().doubleValue() : null);
                vlOrderDetailOrder.setPlanWeight(vlOrderDetailOrder.getPlanWeightNew());
                vlOrderDetailOrder.setConfirmChargeWeight(vlOrderDetailOrder.getConfirmChargeWeightNew() != null ? vlOrderDetailOrder.getConfirmChargeWeightNew().doubleValue() : null);
                vlOrderDetailOrder.setConfirmPieces(vlOrderDetailOrder.getConfirmPiecesNew());
                vlOrderDetailOrder.setConfirmVolume(vlOrderDetailOrder.getConfirmVolumeNew() != null ? vlOrderDetailOrder.getConfirmVolumeNew().doubleValue() : null);
                vlOrderDetailOrder.setConfirmWeight(vlOrderDetailOrder.getConfirmWeightNew());
            }
            //vlOrderDetailOrder.setAwbNumber(lcOrder.getCustomerNumber());
            //vlOrderDetailOrder.setOrderCode(lcOrder.getOrderCode());
            //vlOrderDetailOrder.setOrderUuid(lcOrder.getOrderUuid());
            //vlOrderDetailOrder.setCustomerNumber(lcOrder.getCustomerNumber());
        }
        //设置服务数据源
        //List<com.efreight.common.remoteVo.Service> services = remoteServiceToAF.queryServiceListForVL(vlOrderDetailOrder.getBusinessScope()).getData();
        //vlOrderDetailOrder.setServices(services);
        if (vlOrderDetailOrder.getCostAmount() != null) {
            vlOrderDetailOrder.setCostAmountStr(FormatUtils.formatWithQWF(vlOrderDetailOrder.getCostAmount(), 2) + "(" + vlOrderDetailOrder.getCostCurrecnyCode() + ")");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(VlOrder vlOrder) {
        //检验
        VlOrder order = getById(vlOrder.getOrderId());
        if (order == null) {
            throw new RuntimeException("派车单不存在,无法操作");
        }

        if (StrUtil.isNotBlank(order.getRowUuid()) && !order.getRowUuid().equals(vlOrder.getRowUuid())) {
            throw new RuntimeException("当前页面不是最新数据，请刷新再操作");
        }

        if ("完成订单".equals(vlOrder.getOrderStatus())) {
            throw new RuntimeException("派车单已经是完成状态，无需重复操作");
        }
        if ("强制关闭".equals(vlOrder.getOrderStatus())) {
            throw new RuntimeException("派车单已经是关闭状态，无法操作");
        }

        //校验包含订单的状态
        vlOrder.getDetailOrderList().stream().forEach(vlOrderDetailOrder -> {
            if (vlOrderDetailOrder.getBusinessScope().startsWith("A")) {
                AfOrder afOrder = remoteServiceToAF.getAfOrderById(vlOrderDetailOrder.getOrderId()).getData();
                if ("强制关闭".equals(afOrder.getOrderStatus())) {
                    throw new RuntimeException("订单" + afOrder.getOrderCode() + "已经强制关闭，无法完成");
                }
                if ("财务锁账".equals(afOrder.getOrderStatus())) {
                    throw new RuntimeException("订单" + afOrder.getOrderCode() + "已经财务锁账，无法完成");
                }
                if (afOrder.getCostRecorded()) {
                    throw new RuntimeException("订单" + afOrder.getOrderCode() + "已经成本完成，无法完成");
                }
            } else if (vlOrderDetailOrder.getBusinessScope().startsWith("S")) {
                Order scOrder = orderService.view(vlOrderDetailOrder.getOrderId());
                if ("强制关闭".equals(scOrder.getOrderStatus())) {
                    throw new RuntimeException("订单" + scOrder.getOrderCode() + "已经强制关闭，无法完成");
                }
                if ("财务锁账".equals(scOrder.getOrderStatus())) {
                    throw new RuntimeException("订单" + scOrder.getOrderCode() + "已经财务锁账，无法完成");
                }
                if (scOrder.getCostRecorded()) {
                    throw new RuntimeException("订单" + scOrder.getOrderCode() + "已经成本完成，无法完成");
                }
            } else if (vlOrderDetailOrder.getBusinessScope().startsWith("T")) {
                TcOrder tcOrder = tcOrderService.view(vlOrderDetailOrder.getOrderId());
                if ("强制关闭".equals(tcOrder.getOrderStatus())) {
                    throw new RuntimeException("订单" + tcOrder.getOrderCode() + "已经强制关闭，无法完成");
                }
                if ("财务锁账".equals(tcOrder.getOrderStatus())) {
                    throw new RuntimeException("订单" + tcOrder.getOrderCode() + "已经财务锁账，无法完成");
                }
                if (tcOrder.getCostRecorded()) {
                    throw new RuntimeException("订单" + tcOrder.getOrderCode() + "已经成本完成，无法完成");
                }
            } else if (vlOrderDetailOrder.getBusinessScope().startsWith("L")) {
                LcOrder lcOrder = lcOrderService.view(vlOrderDetailOrder.getOrderId());
                if ("强制关闭".equals(lcOrder.getOrderStatus())) {
                    throw new RuntimeException("订单" + lcOrder.getOrderCode() + "已经强制关闭，无法完成");
                }
                if ("财务锁账".equals(lcOrder.getOrderStatus())) {
                    throw new RuntimeException("订单" + lcOrder.getOrderCode() + "已经财务锁账，无法完成");
                }
                if (lcOrder.getCostRecorded()) {
                    throw new RuntimeException("订单" + lcOrder.getOrderCode() + "已经成本完成，无法完成");
                }
            }
        });

        //更新派车单
        order.setOrderStatus("完成订单");
        order.setRowUuid(UUID.randomUUID().toString());
        order.setEditorId(SecurityUtils.getUser().getId());
        order.setEditorName(SecurityUtils.getUser().buildOptName());
        order.setEditTime(LocalDateTime.now());
        updateById(order);


        //新增cost表
        BigDecimal amount = vlOrder.getCostAmount() == null ? vlOrder.getCostUnitprice() : vlOrder.getCostAmount();
        if (amount.compareTo(BigDecimal.ZERO) != 0) {
            vlOrder.getDetailOrderList().stream().forEach(vlOrderDetailOrder -> {
                if (vlOrderDetailOrder.getCostAmount().compareTo(BigDecimal.ZERO) != 0) {
                    if (vlOrderDetailOrder.getBusinessScope().startsWith("A")) {
                        AfCost afCost = new AfCost();
                        afCost.setCustomerId(vlOrder.getCoopId());
                        afCost.setCustomerName(vlOrder.getCoopName());
                        afCost.setServiceId(vlOrderDetailOrder.getServiceId());
                        afCost.setServiceName(vlOrderDetailOrder.getServiceName());
                        afCost.setCostCurrency(vlOrderDetailOrder.getCostCurrecnyCode());
                        afCost.setCostQuantity(BigDecimal.valueOf(1));
                        afCost.setCostUnitPrice(vlOrderDetailOrder.getCostAmount());
                        afCost.setCostAmount(vlOrderDetailOrder.getCostAmount());
                        List<CurrencyRateVo> currencyRateVoList = remoteServiceToAF.getCurrentListByOrgId().getData().stream().filter(currencyRateVo -> currencyRateVo.getCurrencyCode().equals(vlOrderDetailOrder.getCostCurrecnyCode())).collect(Collectors.toList());
                        if (currencyRateVoList.size() > 0) {
                            afCost.setCostExchangeRate(BigDecimal.valueOf(Double.parseDouble(currencyRateVoList.get(0).getCurrencyRate())));
                            afCost.setCostFunctionalAmount(afCost.getCostAmount().multiply(afCost.getCostExchangeRate()).setScale(2, BigDecimal.ROUND_HALF_UP));
                        }
                        if (afCost.getServiceName().startsWith("干线")) {
                            afCost.setMainRouting(afCost.getCostFunctionalAmount());
                        } else if (afCost.getServiceName().startsWith("支线")) {
                            afCost.setFeeder(afCost.getCostFunctionalAmount());
                        } else if (afCost.getServiceName().startsWith("操作")) {
                            afCost.setOperation(afCost.getCostFunctionalAmount());
                        } else if (afCost.getServiceName().startsWith("包装")) {
                            afCost.setPackaging(afCost.getCostFunctionalAmount());
                        } else if (afCost.getServiceName().startsWith("仓储")) {
                            afCost.setStorage(afCost.getCostFunctionalAmount());
                        } else if (afCost.getServiceName().startsWith("快递")) {
                            afCost.setPostage(afCost.getCostFunctionalAmount());
                        } else if (afCost.getServiceName().startsWith("关检")) {
                            afCost.setClearance(afCost.getCostFunctionalAmount());
                        } else if (afCost.getServiceName().startsWith("数据")) {
                            afCost.setExchange(afCost.getCostFunctionalAmount());
                        }
                        afCost.setBusinessScope(vlOrderDetailOrder.getBusinessScope());
                        afCost.setOrderId(vlOrderDetailOrder.getOrderId());
                        afCost.setOrderUuid(vlOrderDetailOrder.getOrderUuid());
                        afCost.setOrgId(SecurityUtils.getUser().getOrgId());
                        afCost.setCreateTime(LocalDateTime.now());
                        afCost.setCreatorId(SecurityUtils.getUser().getId());
                        afCost.setCreatorName(SecurityUtils.getUser().buildOptName());
                        afCost.setEditorName(SecurityUtils.getUser().buildOptName());
                        afCost.setEditTime(LocalDateTime.now());
                        afCost.setEditorId(SecurityUtils.getUser().getId());
                        afCost.setRowUuid(UUID.randomUUID().toString());
                        afCost.setServiceNote("派车单号:" + order.getOrderCode());
                        afCostService.save(afCost);
                        AfOrder afOrder = remoteServiceToAF.getAfOrderById(vlOrderDetailOrder.getOrderId()).getData();
                        if ("未录成本".equals(afOrder.getCostStatus())) {
                            //修改af订单成本状态
                            baseMapper.updateAfOrderCostStatus("已录成本", vlOrderDetailOrder.getOrderId(), UUID.randomUUID().toString());
                        }
                        vlOrderDetailOrder.setCostId(afCost.getCostId());
                    } else if (vlOrderDetailOrder.getBusinessScope().startsWith("S")) {
                        Cost cost = new Cost();
                        cost.setCustomerId(vlOrder.getCoopId());
                        cost.setCustomerName(vlOrder.getCoopName());
                        cost.setServiceId(vlOrderDetailOrder.getServiceId());
                        cost.setServiceName(vlOrderDetailOrder.getServiceName());
                        cost.setCostCurrency(vlOrderDetailOrder.getCostCurrecnyCode());
                        cost.setCostQuantity(BigDecimal.valueOf(1));
                        cost.setCostUnitPrice(vlOrderDetailOrder.getCostAmount());
                        cost.setCostAmount(vlOrderDetailOrder.getCostAmount());
                        List<CurrencyRateVo> currencyRateVoList = remoteServiceToAF.getCurrentListByOrgId().getData().stream().filter(currencyRateVo -> currencyRateVo.getCurrencyCode().equals(vlOrderDetailOrder.getCostCurrecnyCode())).collect(Collectors.toList());
                        if (currencyRateVoList.size() > 0) {
                            cost.setCostExchangeRate(BigDecimal.valueOf(Double.parseDouble(currencyRateVoList.get(0).getCurrencyRate())));
                            cost.setCostFunctionalAmount(cost.getCostAmount().multiply(cost.getCostExchangeRate()).setScale(2, BigDecimal.ROUND_HALF_UP));
                        }
                        if (cost.getServiceName().startsWith("干线")) {
                            cost.setMainRouting(cost.getCostFunctionalAmount());
                        } else if (cost.getServiceName().startsWith("支线")) {
                            cost.setFeeder(cost.getCostFunctionalAmount());
                        } else if (cost.getServiceName().startsWith("操作")) {
                            cost.setOperation(cost.getCostFunctionalAmount());
                        } else if (cost.getServiceName().startsWith("包装")) {
                            cost.setPackaging(cost.getCostFunctionalAmount());
                        } else if (cost.getServiceName().startsWith("仓储")) {
                            cost.setStorage(cost.getCostFunctionalAmount());
                        } else if (cost.getServiceName().startsWith("快递")) {
                            cost.setPostage(cost.getCostFunctionalAmount());
                        } else if (cost.getServiceName().startsWith("关检")) {
                            cost.setClearance(cost.getCostFunctionalAmount());
                        } else if (cost.getServiceName().startsWith("数据")) {
                            cost.setExchange(cost.getCostFunctionalAmount());
                        }
                        cost.setBusinessScope(vlOrderDetailOrder.getBusinessScope());
                        cost.setOrderId(vlOrderDetailOrder.getOrderId());
                        cost.setOrderUuid(vlOrderDetailOrder.getOrderUuid());
                        cost.setOrgId(SecurityUtils.getUser().getOrgId());
                        cost.setCreateTime(LocalDateTime.now());
                        cost.setCreatorId(SecurityUtils.getUser().getId());
                        cost.setCreatorName(SecurityUtils.getUser().buildOptName());
                        cost.setEditorName(SecurityUtils.getUser().buildOptName());
                        cost.setEditTime(LocalDateTime.now());
                        cost.setEditorId(SecurityUtils.getUser().getId());
                        cost.setRowUuid(UUID.randomUUID().toString());
                        cost.setServiceNote("派车单号:" + order.getOrderCode());
                        costService.save(cost);
                        Order scOrder = orderService.getById(vlOrderDetailOrder.getOrderId());
                        if ("未录成本".equals(scOrder.getCostStatus())) {
                            scOrder.setCostStatus("已录成本");
                            orderService.updateById(scOrder);
                        }
                        vlOrderDetailOrder.setCostId(cost.getCostId());
                    } else if (vlOrderDetailOrder.getBusinessScope().startsWith("T")) {
                        TcCost tcCost = new TcCost();
                        tcCost.setCustomerId(vlOrder.getCoopId());
                        tcCost.setCustomerName(vlOrder.getCoopName());
                        tcCost.setServiceId(vlOrderDetailOrder.getServiceId());
                        tcCost.setServiceName(vlOrderDetailOrder.getServiceName());
                        tcCost.setCostCurrency(vlOrderDetailOrder.getCostCurrecnyCode());
                        tcCost.setCostQuantity(BigDecimal.valueOf(1));
                        tcCost.setCostUnitPrice(vlOrderDetailOrder.getCostAmount());
                        tcCost.setCostAmount(vlOrderDetailOrder.getCostAmount());
                        List<CurrencyRateVo> currencyRateVoList = remoteServiceToAF.getCurrentListByOrgId().getData().stream().filter(currencyRateVo -> currencyRateVo.getCurrencyCode().equals(vlOrderDetailOrder.getCostCurrecnyCode())).collect(Collectors.toList());
                        if (currencyRateVoList.size() > 0) {
                            tcCost.setCostExchangeRate(BigDecimal.valueOf(Double.parseDouble(currencyRateVoList.get(0).getCurrencyRate())));
                            tcCost.setCostFunctionalAmount(tcCost.getCostAmount().multiply(tcCost.getCostExchangeRate()).setScale(2, BigDecimal.ROUND_HALF_UP));
                        }
                        if (tcCost.getServiceName().startsWith("干线")) {
                            tcCost.setMainRouting(tcCost.getCostFunctionalAmount());
                        } else if (tcCost.getServiceName().startsWith("支线")) {
                            tcCost.setFeeder(tcCost.getCostFunctionalAmount());
                        } else if (tcCost.getServiceName().startsWith("操作")) {
                            tcCost.setOperation(tcCost.getCostFunctionalAmount());
                        } else if (tcCost.getServiceName().startsWith("包装")) {
                            tcCost.setPackaging(tcCost.getCostFunctionalAmount());
                        } else if (tcCost.getServiceName().startsWith("仓储")) {
                            tcCost.setStorage(tcCost.getCostFunctionalAmount());
                        } else if (tcCost.getServiceName().startsWith("快递")) {
                            tcCost.setPostage(tcCost.getCostFunctionalAmount());
                        } else if (tcCost.getServiceName().startsWith("关检")) {
                            tcCost.setClearance(tcCost.getCostFunctionalAmount());
                        } else if (tcCost.getServiceName().startsWith("数据")) {
                            tcCost.setExchange(tcCost.getCostFunctionalAmount());
                        }
                        tcCost.setBusinessScope(vlOrderDetailOrder.getBusinessScope());
                        tcCost.setOrderId(vlOrderDetailOrder.getOrderId());
                        tcCost.setOrderUuid(vlOrderDetailOrder.getOrderUuid());
                        tcCost.setOrgId(SecurityUtils.getUser().getOrgId());
                        tcCost.setCreateTime(LocalDateTime.now());
                        tcCost.setCreatorId(SecurityUtils.getUser().getId());
                        tcCost.setCreatorName(SecurityUtils.getUser().buildOptName());
                        tcCost.setEditorName(SecurityUtils.getUser().buildOptName());
                        tcCost.setEditTime(LocalDateTime.now());
                        tcCost.setEditorId(SecurityUtils.getUser().getId());
                        tcCost.setRowUuid(UUID.randomUUID().toString());
                        tcCost.setServiceNote("派车单号:" + order.getOrderCode());
                        tcCostService.save(tcCost);
                        TcOrder tcOrder = tcOrderService.getById(vlOrderDetailOrder.getOrderId());
                        if ("未录成本".equals(tcOrder.getCostStatus())) {
                            tcOrder.setCostStatus("已录成本");
                            tcOrder.setRowUuid(UUID.randomUUID().toString());
                            tcOrderService.updateById(tcOrder);
                        }
                        vlOrderDetailOrder.setCostId(tcCost.getCostId());
                    } else if (vlOrderDetailOrder.getBusinessScope().startsWith("L")) {
                        LcCost lcCost = new LcCost();
                        lcCost.setCustomerId(vlOrder.getCoopId());
                        lcCost.setCustomerName(vlOrder.getCoopName());
                        lcCost.setServiceId(vlOrderDetailOrder.getServiceId());
                        lcCost.setServiceName(vlOrderDetailOrder.getServiceName());
                        lcCost.setCostCurrency(vlOrderDetailOrder.getCostCurrecnyCode());
                        lcCost.setCostQuantity(BigDecimal.valueOf(1));
                        lcCost.setCostUnitPrice(vlOrderDetailOrder.getCostAmount());
                        lcCost.setCostAmount(vlOrderDetailOrder.getCostAmount());
                        List<CurrencyRateVo> currencyRateVoList = remoteServiceToAF.getCurrentListByOrgId().getData().stream().filter(currencyRateVo -> currencyRateVo.getCurrencyCode().equals(vlOrderDetailOrder.getCostCurrecnyCode())).collect(Collectors.toList());
                        if (currencyRateVoList.size() > 0) {
                            lcCost.setCostExchangeRate(BigDecimal.valueOf(Double.parseDouble(currencyRateVoList.get(0).getCurrencyRate())));
                            lcCost.setCostFunctionalAmount(lcCost.getCostAmount().multiply(lcCost.getCostExchangeRate()).setScale(2, BigDecimal.ROUND_HALF_UP));
                        }
                        if (lcCost.getServiceName().startsWith("干线")) {
                            lcCost.setMainRouting(lcCost.getCostFunctionalAmount());
                        } else if (lcCost.getServiceName().startsWith("支线")) {
                            lcCost.setFeeder(lcCost.getCostFunctionalAmount());
                        } else if (lcCost.getServiceName().startsWith("操作")) {
                            lcCost.setOperation(lcCost.getCostFunctionalAmount());
                        } else if (lcCost.getServiceName().startsWith("包装")) {
                            lcCost.setPackaging(lcCost.getCostFunctionalAmount());
                        } else if (lcCost.getServiceName().startsWith("仓储")) {
                            lcCost.setStorage(lcCost.getCostFunctionalAmount());
                        } else if (lcCost.getServiceName().startsWith("快递")) {
                            lcCost.setPostage(lcCost.getCostFunctionalAmount());
                        } else if (lcCost.getServiceName().startsWith("关检")) {
                            lcCost.setClearance(lcCost.getCostFunctionalAmount());
                        } else if (lcCost.getServiceName().startsWith("数据")) {
                            lcCost.setExchange(lcCost.getCostFunctionalAmount());
                        }
                        lcCost.setBusinessScope(vlOrderDetailOrder.getBusinessScope());
                        lcCost.setOrderId(vlOrderDetailOrder.getOrderId());
                        lcCost.setOrderUuid(vlOrderDetailOrder.getOrderUuid());
                        lcCost.setOrgId(SecurityUtils.getUser().getOrgId());
                        lcCost.setCreateTime(LocalDateTime.now());
                        lcCost.setCreatorId(SecurityUtils.getUser().getId());
                        lcCost.setCreatorName(SecurityUtils.getUser().buildOptName());
                        lcCost.setEditorName(SecurityUtils.getUser().buildOptName());
                        lcCost.setEditTime(LocalDateTime.now());
                        lcCost.setEditorId(SecurityUtils.getUser().getId());
                        lcCost.setRowUuid(UUID.randomUUID().toString());
                        lcCost.setServiceNote("派车单号:" + order.getOrderCode());
                        lcCostService.save(lcCost);
                        LcOrder lcOrder = lcOrderService.getById(vlOrderDetailOrder.getOrderId());
                        if ("未录成本".equals(lcOrder.getCostStatus())) {
                            lcOrder.setCostStatus("已录成本");
                            lcOrder.setRowUuid(UUID.randomUUID().toString());
                            lcOrderService.updateById(lcOrder);
                        }
                        vlOrderDetailOrder.setCostId(lcCost.getCostId());
                    }
                }
                vlOrderDetailOrder.setEditTime(LocalDateTime.now());
                vlOrderDetailOrder.setEditorName(SecurityUtils.getUser().buildOptName());
                vlOrderDetailOrder.setEditorId(SecurityUtils.getUser().getId());

            });
        }

        //更新明细订单表
        vlOrderDetailOrderService.updateBatchById(vlOrder.getDetailOrderList());

        //日志
        VlLog vlLog = new VlLog();
        vlLog.setBusinessScope("VL");
        vlLog.setPageName("VL 派车单");
        vlLog.setPageFunction("派车单完成");
        vlLog.setOrderId(vlOrder.getOrderId());
        vlLog.setOrderNumber(vlOrder.getOrderCode());
        vlLog.setOrderUuid(vlOrder.getOrderUuid());
        vlLogService.insert(vlLog);

        //每个订单明细插入派车单附件
        LambdaQueryWrapper<VlOrderFiles> vlOrderFilesLambdaQueryWrapper = Wrappers.<VlOrderFiles>lambdaQuery();
        vlOrderFilesLambdaQueryWrapper.eq(VlOrderFiles::getOrderId, order.getOrderId()).eq(VlOrderFiles::getOrgId, SecurityUtils.getUser().getOrgId());
        List<VlOrderFiles> vlOrderFilesList = vlOrderFilesMapper.selectList(vlOrderFilesLambdaQueryWrapper);
        ArrayList<AfOrderFiles> afOrderFiles = new ArrayList<>();
        vlOrder.getDetailOrderList().stream().forEach(vlOrderDetailOrder -> {
            if (vlOrderDetailOrder.getBusinessScope().startsWith("A")) {
                List<AfOrderFiles> orderFilesList = vlOrderFilesList.stream().map(vlOrderFiles -> {
                    AfOrderFiles orderFiles = new AfOrderFiles();
                    BeanUtils.copyProperties(vlOrderFiles, orderFiles);
                    orderFiles.setBusinessScope(vlOrderDetailOrder.getBusinessScope());
                    orderFiles.setOrderId(vlOrderDetailOrder.getOrderId());
                    orderFiles.setCreateTime(null);
                    orderFiles.setFileRemark("派车单号:" + order.getOrderCode());
                    return orderFiles;
                }).collect(Collectors.toList());
                afOrderFiles.addAll(orderFilesList);
            } else if (vlOrderDetailOrder.getBusinessScope().startsWith("S")) {
                List<OrderFiles> orderFilesList = vlOrderFilesList.stream().map(vlOrderFiles -> {
                    OrderFiles orderFiles = new OrderFiles();
                    BeanUtils.copyProperties(vlOrderFiles, orderFiles);
                    orderFiles.setBusinessScope(vlOrderDetailOrder.getBusinessScope());
                    orderFiles.setCreateTime(LocalDateTime.now());
                    orderFiles.setCreatorId(SecurityUtils.getUser().getId());
                    orderFiles.setCreatorName(SecurityUtils.getUser().buildOptName());
                    orderFiles.setOrderId(vlOrderDetailOrder.getOrderId());
                    orderFiles.setFileRemark("派车单号:" + order.getOrderCode());
                    return orderFiles;
                }).collect(Collectors.toList());
                orderFilesService.saveBatch(orderFilesList);
            } else if (vlOrderDetailOrder.getBusinessScope().startsWith("T")) {
                List<TcOrderFiles> orderFilesList = vlOrderFilesList.stream().map(vlOrderFiles -> {
                    TcOrderFiles orderFiles = new TcOrderFiles();
                    BeanUtils.copyProperties(vlOrderFiles, orderFiles);
                    orderFiles.setBusinessScope(vlOrderDetailOrder.getBusinessScope());
                    orderFiles.setCreateTime(LocalDateTime.now());
                    orderFiles.setCreatorId(SecurityUtils.getUser().getId());
                    orderFiles.setCreatorName(SecurityUtils.getUser().buildOptName());
                    orderFiles.setOrderId(vlOrderDetailOrder.getOrderId());
                    orderFiles.setFileRemark("派车单号:" + order.getOrderCode());
                    return orderFiles;
                }).collect(Collectors.toList());
                tcOrderFilesService.saveBatch(orderFilesList);
            } else if (vlOrderDetailOrder.getBusinessScope().startsWith("L")) {
                List<LcOrderFiles> orderFilesList = vlOrderFilesList.stream().map(vlOrderFiles -> {
                    LcOrderFiles orderFiles = new LcOrderFiles();
                    BeanUtils.copyProperties(vlOrderFiles, orderFiles);
                    orderFiles.setBusinessScope(vlOrderDetailOrder.getBusinessScope());
                    orderFiles.setCreateTime(LocalDateTime.now());
                    orderFiles.setCreatorId(SecurityUtils.getUser().getId());
                    orderFiles.setCreatorName(SecurityUtils.getUser().buildOptName());
                    orderFiles.setOrderId(vlOrderDetailOrder.getOrderId());
                    orderFiles.setFileRemark("派车单号:" + order.getOrderCode());
                    return orderFiles;
                }).collect(Collectors.toList());
                lcOrderFilesService.saveBatch(orderFilesList);
            }
        });
        //防止AF远程调用报错事务无法回滚，估提到最后保存
        if (afOrderFiles.size() > 0) {
            MessageInfo messageInfo = remoteServiceToAF.doOrderFilesBatchSaveForAF(afOrderFiles);
            if (messageInfo.getCode() == 1) {
                throw new RuntimeException(messageInfo.getMessageInfo());
            }
        }

    }

    @Override
    public List<OrderForVL> getOrderList(OrderForVL orderForVL) {
        if (orderForVL.getBusinessScope().startsWith("A")) {
            return remoteServiceToAF.getAFOrderListForVL(orderForVL).getData();
        } else if (orderForVL.getBusinessScope().startsWith("S")) {
            return orderService.getSCOrderListForVL(orderForVL);
        } else if (orderForVL.getBusinessScope().startsWith("T")) {
            return tcOrderService.getTCOrderListForVL(orderForVL);
        } else if (orderForVL.getBusinessScope().startsWith("L")) {
            return lcOrderService.getLCOrderListForVL(orderForVL);
        }
        return new ArrayList<>();
    }

    @Override
    public void exportExcel(VlOrder vlOrder) {
        LambdaQueryWrapper<VlOrder> wrapper = getWrapper(vlOrder);
        List<VlOrder> list = list(wrapper);
        fixRecord(list);

        List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
        if (StrUtil.isNotBlank(vlOrder.getColumnStrs())) {
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(vlOrder.getColumnStrs());
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

            if (list != null && list.size() > 0) {
                //设置总数显示列
                VlOrder total = total(vlOrder);
                total.setOrderStatus("");
                FieldValUtils.setFieldValueByFieldName(colunmStrs[0], "合计：", total);
                list.add(total);
                for (VlOrder excel : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        if ("drivingTime".equals(colunmStrs[j])) {
                            if (excel.getDrivingTime() != null) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                                map.put("drivingTime", formatter.format(excel.getDrivingTime()));
                            } else {
                                map.put("drivingTime", "");
                            }
                        } else {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], excel));
                        }
                    }
                    listExcel.add(map);
                }
            }
            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(LoginUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");
        }
    }

    @Override
    public VlOrder view(Integer orderId) {
        VlOrder vlOrder = getById(orderId);
        if ("强制关闭".equals(vlOrder.getOrderStatus())) {
            throw new RuntimeException("该派车单已强制关闭");
        }
        //查询客户
        LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopLambdaQueryWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
        afVPrmCoopLambdaQueryWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfVPrmCoop::getCoopId, vlOrder.getCoopId());
        AfVPrmCoop coop = afVPrmCoopService.getOne(afVPrmCoopLambdaQueryWrapper);
        if (coop != null) {
            vlOrder.setCoopName(coop.getCoopName());
        }
        //查询订单详情
        //LambdaQueryWrapper<VlOrderDetailOrder> vlOrderDetailOrderLambdaQueryWrapper = Wrappers.<VlOrderDetailOrder>lambdaQuery();
        //vlOrderDetailOrderLambdaQueryWrapper.eq(VlOrderDetailOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(VlOrderDetailOrder::getVlOrderId, orderId);
        //List<VlOrderDetailOrder> detailOrderList = vlOrderDetailOrderService.list(vlOrderDetailOrderLambdaQueryWrapper);
        List<VlOrderDetailOrder> detailOrderList = baseMapper.selectVlOrderDetailOrderList(SecurityUtils.getUser().getOrgId(),orderId);
        String businessScopes = detailOrderList.stream().map(item -> item.getBusinessScope()).distinct().collect(Collectors.joining("','"));
        if(StringUtils.isNotBlank(businessScopes)){
            businessScopes = "'" + businessScopes + "'";
            List<com.efreight.common.remoteVo.Service> businessScopeList = baseMapper.getServices(SecurityUtils.getUser().getOrgId(),businessScopes);
            Map<String, List<com.efreight.common.remoteVo.Service>> groupBy = businessScopeList.stream().collect(Collectors.groupingBy(com.efreight.common.remoteVo.Service::getBusinessScope));
            detailOrderList.stream().forEach(vlOrderDetailOrder -> {
                vlOrderDetailOrder.setServices(groupBy.get(vlOrderDetailOrder.getBusinessScope()));
                if ("完成订单".equals(vlOrder.getOrderStatus())) {
                    insertOrderInfo(vlOrderDetailOrder, true);
                } else {
                    insertOrderInfo(vlOrderDetailOrder, false);
                }
            });
        }
        vlOrder.setDetailOrderList(detailOrderList);
        if (vlOrder.getCostUnitprice() != null) {
            vlOrder.setCostPriceAmount(vlOrder.getCostUnitprice());
            vlOrder.setCostPriceType("单价");
        } else if (vlOrder.getCostAmount() != null) {
            vlOrder.setCostPriceAmount(vlOrder.getCostAmount());
            vlOrder.setCostPriceType("总价");
        }

        return vlOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(VlOrder vlOrder) {
        //校验

        //新增派车单
        vlOrder.setOrderStatus("创建订单");
        vlOrder.setBusinessScope("VL");
        vlOrder.setOrderUuid(createUuid());
        vlOrder.setOrderCode(createOrderCode());
        vlOrder.setRowUuid(UUID.randomUUID().toString());
        vlOrder.setCreateTime(LocalDateTime.now());
        vlOrder.setCreatorId(SecurityUtils.getUser().getId());
        vlOrder.setCreatorName(SecurityUtils.getUser().buildOptName());
        vlOrder.setEditorId(SecurityUtils.getUser().getId());
        vlOrder.setEditorName(SecurityUtils.getUser().buildOptName());
        vlOrder.setEditTime(LocalDateTime.now());
        vlOrder.setOrgId(SecurityUtils.getUser().getOrgId());
        save(vlOrder);
        //新增派车单明细订单
        vlOrder.getDetailOrderList().stream().forEach(vlOrderDetailOrder -> {
            vlOrderDetailOrder.setConfirmWeight(null);
            vlOrderDetailOrder.setConfirmVolume(null);
            vlOrderDetailOrder.setConfirmPieces(null);
            vlOrderDetailOrder.setConfirmChargeWeight(null);
            vlOrderDetailOrder.setPlanWeight(null);
            vlOrderDetailOrder.setPlanVolume(null);
            vlOrderDetailOrder.setPlanPieces(null);
            vlOrderDetailOrder.setPlanChargeWeight(null);
            vlOrderDetailOrder.setPlanDensity(null);
            vlOrderDetailOrder.setCostAmount(null);
            vlOrderDetailOrder.setCreateTime(LocalDateTime.now());
            vlOrderDetailOrder.setCreatorId(SecurityUtils.getUser().getId());
            vlOrderDetailOrder.setCreatorName(SecurityUtils.getUser().buildOptName());
            vlOrderDetailOrder.setEditorId(SecurityUtils.getUser().getId());
            vlOrderDetailOrder.setEditorName(SecurityUtils.getUser().buildOptName());
            vlOrderDetailOrder.setEditTime(LocalDateTime.now());
            vlOrderDetailOrder.setOrgId(SecurityUtils.getUser().getOrgId());
            vlOrderDetailOrder.setVlOrderId(vlOrder.getOrderId());
        });
        vlOrderDetailOrderService.saveBatch(vlOrder.getDetailOrderList());

        //日志
        VlLog vlLog = new VlLog();
        vlLog.setBusinessScope("VL");
        vlLog.setPageName("VL 派车单");
        vlLog.setPageFunction("派车单创建");
        vlLog.setOrderId(vlOrder.getOrderId());
        vlLog.setOrderNumber(vlOrder.getOrderCode());
        vlLog.setOrderUuid(vlOrder.getOrderUuid());
        vlLogService.insert(vlLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modify(VlOrder vlOrder) {
        //校验
        VlOrder order = getById(vlOrder.getOrderId());
        if (order == null) {
            throw new RuntimeException("派车单不存在,无法修改");
        }

        if (StrUtil.isNotBlank(order.getRowUuid()) && !order.getRowUuid().equals(vlOrder.getRowUuid())) {
            throw new RuntimeException("当前页面不是最新数据，请刷新再操作");
        }

        if ("完成订单".equals(vlOrder.getOrderStatus())) {
            throw new RuntimeException("派车单已经是完成状态，无法修改");
        }
        if ("强制关闭".equals(vlOrder.getOrderStatus())) {
            throw new RuntimeException("派车单已经是关闭状态，无法修改");
        }
        //修改派车单
        vlOrder.setEditorId(SecurityUtils.getUser().getId());
        vlOrder.setEditorName(SecurityUtils.getUser().buildOptName());
        vlOrder.setEditTime(LocalDateTime.now());
        vlOrder.setRowUuid(UUID.randomUUID().toString());
        updateById(vlOrder);
        //修改派车单明细订单
        LambdaQueryWrapper<VlOrderDetailOrder> vlOrderDetailOrderLambdaQueryWrapper = Wrappers.<VlOrderDetailOrder>lambdaQuery();
        vlOrderDetailOrderLambdaQueryWrapper.eq(VlOrderDetailOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(VlOrderDetailOrder::getVlOrderId, vlOrder.getOrderId());
        vlOrderDetailOrderService.remove(vlOrderDetailOrderLambdaQueryWrapper);
        vlOrder.getDetailOrderList().stream().forEach(vlOrderDetailOrder -> {
            vlOrderDetailOrder.setConfirmWeight(null);
            vlOrderDetailOrder.setConfirmVolume(null);
            vlOrderDetailOrder.setConfirmPieces(null);
            vlOrderDetailOrder.setConfirmChargeWeight(null);
            vlOrderDetailOrder.setPlanWeight(null);
            vlOrderDetailOrder.setPlanVolume(null);
            vlOrderDetailOrder.setPlanPieces(null);
            vlOrderDetailOrder.setPlanChargeWeight(null);
            vlOrderDetailOrder.setPlanDensity(null);
            vlOrderDetailOrder.setCostAmount(null);
            vlOrderDetailOrder.setCreateTime(LocalDateTime.now());
            vlOrderDetailOrder.setCreatorId(SecurityUtils.getUser().getId());
            vlOrderDetailOrder.setCreatorName(SecurityUtils.getUser().buildOptName());
            vlOrderDetailOrder.setEditorId(SecurityUtils.getUser().getId());
            vlOrderDetailOrder.setEditorName(SecurityUtils.getUser().buildOptName());
            vlOrderDetailOrder.setEditTime(LocalDateTime.now());
            vlOrderDetailOrder.setOrgId(SecurityUtils.getUser().getOrgId());
            vlOrderDetailOrder.setVlOrderId(vlOrder.getOrderId());
        });
        vlOrderDetailOrderService.saveBatch(vlOrder.getDetailOrderList());

        //日志
        VlLog vlLog = new VlLog();
        vlLog.setBusinessScope("VL");
        vlLog.setPageName("VL 派车单");
        vlLog.setPageFunction("派车单修改");
        vlLog.setOrderId(vlOrder.getOrderId());
        vlLog.setOrderNumber(vlOrder.getOrderCode());
        vlLog.setOrderUuid(vlOrder.getOrderUuid());
        vlLogService.insert(vlLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer orderId) {
        //校验
        VlOrder vlOrder = getById(orderId);
        if (vlOrder == null) {
            throw new RuntimeException("派车单不存在,无法删除");
        }
        //删除派车单
        removeById(orderId);
        //删除明细订单表
        LambdaQueryWrapper<VlOrderDetailOrder> vlOrderDetailOrderLambdaQueryWrapper = Wrappers.<VlOrderDetailOrder>lambdaQuery();
        vlOrderDetailOrderLambdaQueryWrapper.eq(VlOrderDetailOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(VlOrderDetailOrder::getVlOrderId, orderId);
        vlOrderDetailOrderService.remove(vlOrderDetailOrderLambdaQueryWrapper);
        //日志
        VlLog vlLog = new VlLog();
        vlLog.setOrderId(orderId);
        vlLog.setBusinessScope("VL");
        vlLog.setPageName("VL 派车单");
        vlLog.setPageFunction("派车单删除");
        vlLog.setOrderNumber(vlOrder.getOrderCode());
        vlLog.setOrderUuid(vlOrder.getOrderUuid());
        vlLogService.insert(vlLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stop(Integer orderId, String reason, String rowUuid) {
        //校验
        VlOrder vlOrder = getById(orderId);
        if (vlOrder == null) {
            throw new RuntimeException("派车单不存在,无法关闭");
        }

        if (StrUtil.isNotBlank(vlOrder.getRowUuid()) && !vlOrder.getRowUuid().equals(rowUuid)) {
            throw new RuntimeException("当前页面不是最新数据，请刷新再操作");
        }

        if ("完成订单".equals(vlOrder.getOrderStatus())) {
            throw new RuntimeException("派车单已经是完成状态，无法关闭");
        }
        if ("强制关闭".equals(vlOrder.getOrderStatus())) {
            throw new RuntimeException("派车单已经是关闭状态，无需重复关闭");
        }
        //关闭派车单
        vlOrder.setOrderStatus("强制关闭");
        vlOrder.setRowUuid(UUID.randomUUID().toString());
        vlOrder.setEditorId(SecurityUtils.getUser().getId());
        vlOrder.setEditorName(SecurityUtils.getUser().buildOptName());
        vlOrder.setEditTime(LocalDateTime.now());
        updateById(vlOrder);
        //日志
        VlLog vlLog = new VlLog();
        vlLog.setOrderId(orderId);
        vlLog.setBusinessScope("VL");
        vlLog.setPageName("VL 派车单");
        vlLog.setPageFunction("派车单强制关闭");
        vlLog.setOrderNumber(vlOrder.getOrderCode());
        vlLog.setOrderUuid(vlOrder.getOrderUuid());
        vlLogService.insert(vlLog);
    }

    /**
     * 获取重量
     *
     * @param orderId
     * @return
     */
    private BigDecimal getWeight(Integer orderId) {
        LambdaQueryWrapper<VlOrderDetailOrder> vlOrderDetailOrderLambdaQueryWrapper = Wrappers.<VlOrderDetailOrder>lambdaQuery();
        vlOrderDetailOrderLambdaQueryWrapper.eq(VlOrderDetailOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(VlOrderDetailOrder::getVlOrderId, orderId);
        List<VlOrderDetailOrder> detailOrderList = vlOrderDetailOrderService.list(vlOrderDetailOrderLambdaQueryWrapper);
        HashMap<String, BigDecimal> sumWeight = new HashMap<>();
        sumWeight.put("sumWeight", BigDecimal.ZERO);
        detailOrderList.stream().forEach(vlOrderDetailOrder -> {
            if (vlOrderDetailOrder.getBusinessScope().startsWith("A")) {
                AfOrder afOrder = remoteServiceToAF.getAfOrderById(vlOrderDetailOrder.getOrderId()).getData();
                BigDecimal weight = afOrder.getConfirmChargeWeight() == null ? (afOrder.getPlanChargeWeight() == null ? BigDecimal.ZERO : BigDecimal.valueOf(afOrder.getPlanChargeWeight())) : BigDecimal.valueOf(afOrder.getConfirmChargeWeight());
                sumWeight.put("sumWeight", sumWeight.get("sumWeight").add(weight));
            } else if (vlOrderDetailOrder.getBusinessScope().startsWith("S")) {
                Order scOrder = orderService.view(vlOrderDetailOrder.getOrderId());
                BigDecimal weight = scOrder.getPlanChargeWeight() == null ? BigDecimal.ZERO : scOrder.getPlanChargeWeight();
                sumWeight.put("sumWeight", sumWeight.get("sumWeight").add(weight.multiply(BigDecimal.valueOf(1000))));
            } else if (vlOrderDetailOrder.getBusinessScope().startsWith("T")) {
                TcOrder tcOrder = tcOrderService.view(vlOrderDetailOrder.getOrderId());
                BigDecimal weight = tcOrder.getPlanChargeWeight() == null ? BigDecimal.ZERO : tcOrder.getPlanChargeWeight();
                sumWeight.put("sumWeight", sumWeight.get("sumWeight").add(weight.multiply(BigDecimal.valueOf(1000))));
            } else if (vlOrderDetailOrder.getBusinessScope().startsWith("L")) {
                LcOrder lcOrder = lcOrderService.view(vlOrderDetailOrder.getOrderId());
                BigDecimal weight = lcOrder.getConfirmChargeWeight() == null ? (lcOrder.getPlanChargeWeight() == null ? BigDecimal.ZERO : lcOrder.getPlanChargeWeight()) : lcOrder.getConfirmChargeWeight();
                sumWeight.put("sumWeight", sumWeight.get("sumWeight").add(weight));
            }
        });
        return sumWeight.get("sumWeight").divide(BigDecimal.valueOf(1000), 1, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 获取体积
     *
     * @param orderId
     * @return
     */
    private BigDecimal getVolume(Integer orderId) {
        LambdaQueryWrapper<VlOrderDetailOrder> vlOrderDetailOrderLambdaQueryWrapper = Wrappers.<VlOrderDetailOrder>lambdaQuery();
        vlOrderDetailOrderLambdaQueryWrapper.eq(VlOrderDetailOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(VlOrderDetailOrder::getVlOrderId, orderId);
        List<VlOrderDetailOrder> detailOrderList = vlOrderDetailOrderService.list(vlOrderDetailOrderLambdaQueryWrapper);
        HashMap<String, BigDecimal> sumVolume = new HashMap<>();
        sumVolume.put("sumVolume", BigDecimal.ZERO);
        detailOrderList.stream().forEach(vlOrderDetailOrder -> {
            if (vlOrderDetailOrder.getBusinessScope().startsWith("A")) {
                AfOrder afOrder = remoteServiceToAF.getAfOrderById(vlOrderDetailOrder.getOrderId()).getData();
                BigDecimal volume = afOrder.getConfirmVolume() == null ? (afOrder.getPlanVolume() == null ? BigDecimal.ZERO : BigDecimal.valueOf(afOrder.getPlanVolume())) : BigDecimal.valueOf(afOrder.getConfirmVolume());
                sumVolume.put("sumVolume", sumVolume.get("sumVolume").add(volume));
            } else if (vlOrderDetailOrder.getBusinessScope().startsWith("S")) {
                Order scOrder = orderService.view(vlOrderDetailOrder.getOrderId());
                BigDecimal volume = scOrder.getPlanVolume() == null ? BigDecimal.ZERO : scOrder.getPlanVolume();
                sumVolume.put("sumVolume", sumVolume.get("sumVolume").add(volume));
            } else if (vlOrderDetailOrder.getBusinessScope().startsWith("T")) {
                TcOrder tcOrder = tcOrderService.view(vlOrderDetailOrder.getOrderId());
                BigDecimal volume = tcOrder.getPlanVolume() == null ? BigDecimal.ZERO : tcOrder.getPlanVolume();
                sumVolume.put("sumVolume", sumVolume.get("sumVolume").add(volume));
            } else if (vlOrderDetailOrder.getBusinessScope().startsWith("L")) {
                LcOrder lcOrder = lcOrderService.view(vlOrderDetailOrder.getOrderId());
                BigDecimal volume = lcOrder.getConfirmVolume() == null ? (lcOrder.getPlanVolume() == null ? BigDecimal.ZERO : lcOrder.getPlanVolume()) : lcOrder.getConfirmVolume();
                sumVolume.put("sumVolume", sumVolume.get("sumVolume").add(volume));
            }
        });
        return sumVolume.get("sumVolume");
    }

    private Map<String, BigDecimal> getWeightAndVolume(Integer orderId) {
        LambdaQueryWrapper<VlOrderDetailOrder> vlOrderDetailOrderLambdaQueryWrapper = Wrappers.<VlOrderDetailOrder>lambdaQuery();
        vlOrderDetailOrderLambdaQueryWrapper.eq(VlOrderDetailOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(VlOrderDetailOrder::getVlOrderId, orderId);
        List<VlOrderDetailOrder> detailOrderList = vlOrderDetailOrderService.list(vlOrderDetailOrderLambdaQueryWrapper);
        HashMap<String, BigDecimal> sumWeight = new HashMap<>();
        sumWeight.put("sumWeight", BigDecimal.ZERO);
        HashMap<String, BigDecimal> sumVolume = new HashMap<>();
        sumVolume.put("sumVolume", BigDecimal.ZERO);
        detailOrderList.stream().forEach(vlOrderDetailOrder -> {
            if (vlOrderDetailOrder.getBusinessScope().startsWith("A")) {
                AfOrder afOrder = remoteServiceToAF.getAfOrderById(vlOrderDetailOrder.getOrderId()).getData();
                BigDecimal weight = afOrder.getConfirmChargeWeight() == null ? (afOrder.getPlanChargeWeight() == null ? BigDecimal.ZERO : BigDecimal.valueOf(afOrder.getPlanChargeWeight())) : BigDecimal.valueOf(afOrder.getConfirmChargeWeight());
                sumWeight.put("sumWeight", sumWeight.get("sumWeight").add(weight));
                BigDecimal volume = afOrder.getConfirmVolume() == null ? (afOrder.getPlanVolume() == null ? BigDecimal.ZERO : BigDecimal.valueOf(afOrder.getPlanVolume())) : BigDecimal.valueOf(afOrder.getConfirmVolume());
                sumVolume.put("sumVolume", sumVolume.get("sumVolume").add(volume));
            } else if (vlOrderDetailOrder.getBusinessScope().startsWith("S")) {
                Order scOrder = orderService.view(vlOrderDetailOrder.getOrderId());
                BigDecimal weight = scOrder.getPlanChargeWeight() == null ? BigDecimal.ZERO : scOrder.getPlanChargeWeight();
                sumWeight.put("sumWeight", sumWeight.get("sumWeight").add(weight.multiply(BigDecimal.valueOf(1000))));
                BigDecimal volume = scOrder.getPlanVolume() == null ? BigDecimal.ZERO : scOrder.getPlanVolume();
                sumVolume.put("sumVolume", sumVolume.get("sumVolume").add(volume));
            } else if (vlOrderDetailOrder.getBusinessScope().startsWith("T")) {
                TcOrder tcOrder = tcOrderService.view(vlOrderDetailOrder.getOrderId());
                BigDecimal weight = tcOrder.getPlanChargeWeight() == null ? BigDecimal.ZERO : tcOrder.getPlanChargeWeight();
                sumWeight.put("sumWeight", sumWeight.get("sumWeight").add(weight.multiply(BigDecimal.valueOf(1000))));
                BigDecimal volume = tcOrder.getPlanVolume() == null ? BigDecimal.ZERO : tcOrder.getPlanVolume();
                sumVolume.put("sumVolume", sumVolume.get("sumVolume").add(volume));
            } else if (vlOrderDetailOrder.getBusinessScope().startsWith("L")) {
                LcOrder lcOrder = lcOrderService.view(vlOrderDetailOrder.getOrderId());
                BigDecimal weight = lcOrder.getConfirmChargeWeight() == null ? (lcOrder.getPlanChargeWeight() == null ? BigDecimal.ZERO : lcOrder.getPlanChargeWeight()) : lcOrder.getConfirmChargeWeight();
                sumWeight.put("sumWeight", sumWeight.get("sumWeight").add(weight));
                BigDecimal volume = lcOrder.getConfirmVolume() == null ? (lcOrder.getPlanVolume() == null ? BigDecimal.ZERO : lcOrder.getPlanVolume()) : lcOrder.getConfirmVolume();
                sumVolume.put("sumVolume", sumVolume.get("sumVolume").add(volume));
            }
        });
        Map<String, BigDecimal> weightAndVolume = new HashMap<>();
        weightAndVolume.put("weight", sumWeight.get("sumWeight"));
        weightAndVolume.put("volume", sumVolume.get("sumVolume"));
        return weightAndVolume;
    }

    private Map<String, BigDecimal> getWeightAndVolumeNew(Integer orderId) {
        LambdaQueryWrapper<VlOrderDetailOrder> vlOrderDetailOrderLambdaQueryWrapper = Wrappers.<VlOrderDetailOrder>lambdaQuery();
        vlOrderDetailOrderLambdaQueryWrapper.eq(VlOrderDetailOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(VlOrderDetailOrder::getVlOrderId, orderId);
        List<VlOrderDetailOrder> detailOrderList = vlOrderDetailOrderService.list(vlOrderDetailOrderLambdaQueryWrapper);

        String afOrderIds = detailOrderList.stream().filter(vlOrderDetailOrder -> vlOrderDetailOrder.getBusinessScope().startsWith("A")).map(vlOrderDetailOrder -> vlOrderDetailOrder.getOrderId().toString()).collect(Collectors.joining(","));
        String scOrderIds = detailOrderList.stream().filter(vlOrderDetailOrder -> vlOrderDetailOrder.getBusinessScope().startsWith("S")).map(vlOrderDetailOrder -> vlOrderDetailOrder.getOrderId().toString()).collect(Collectors.joining(","));
        String tcOrderIds = detailOrderList.stream().filter(vlOrderDetailOrder -> vlOrderDetailOrder.getBusinessScope().startsWith("T")).map(vlOrderDetailOrder -> vlOrderDetailOrder.getOrderId().toString()).collect(Collectors.joining(","));
        String lcOrderIds = detailOrderList.stream().filter(vlOrderDetailOrder -> vlOrderDetailOrder.getBusinessScope().startsWith("L")).map(vlOrderDetailOrder -> vlOrderDetailOrder.getOrderId().toString()).collect(Collectors.joining(","));

        HashMap<String, BigDecimal> sumWeight = new HashMap<>();
        sumWeight.put("sumWeight", BigDecimal.ZERO);
        HashMap<String, BigDecimal> sumVolume = new HashMap<>();
        sumVolume.put("sumVolume", BigDecimal.ZERO);
        if (afOrderIds.length() > 0) {
            Map<String, Double> weightAndVolumeSum = baseMapper.getWeightAndVolumeSumForAF(afOrderIds, SecurityUtils.getUser().getOrgId());
            sumWeight.put("sumWeight", sumWeight.get("sumWeight").add(BigDecimal.valueOf(weightAndVolumeSum.get("weightSum"))));
            sumVolume.put("sumVolume", sumVolume.get("sumVolume").add(BigDecimal.valueOf(weightAndVolumeSum.get("volumeSum"))));
        }
        if (scOrderIds.length() > 0) {
            Map<String, BigDecimal> weightAndVolumeSum = baseMapper.getWeightAndVolumeSumForSC(scOrderIds, SecurityUtils.getUser().getOrgId());
            sumWeight.put("sumWeight", sumWeight.get("sumWeight").add(weightAndVolumeSum.get("weightSum").multiply(BigDecimal.valueOf(1000))));
            sumVolume.put("sumVolume", sumVolume.get("sumVolume").add(weightAndVolumeSum.get("volumeSum")));
        }
        if (tcOrderIds.length() > 0) {
            Map<String, BigDecimal> weightAndVolumeSum = baseMapper.getWeightAndVolumeSumForTC(tcOrderIds, SecurityUtils.getUser().getOrgId());
            sumWeight.put("sumWeight", sumWeight.get("sumWeight").add(weightAndVolumeSum.get("weightSum").multiply(BigDecimal.valueOf(1000))));
            sumVolume.put("sumVolume", sumVolume.get("sumVolume").add(weightAndVolumeSum.get("volumeSum")));
        }
        if (lcOrderIds.length() > 0) {
            Map<String, BigDecimal> weightAndVolumeSum = baseMapper.getWeightAndVolumeSumForLC(lcOrderIds, SecurityUtils.getUser().getOrgId());
            sumWeight.put("sumWeight", sumWeight.get("sumWeight").add(weightAndVolumeSum.get("weightSum")));
            sumVolume.put("sumVolume", sumVolume.get("sumVolume").add(weightAndVolumeSum.get("volumeSum")));
        }

        Map<String, BigDecimal> weightAndVolume = new HashMap<>();
        weightAndVolume.put("weight", sumWeight.get("sumWeight"));
        weightAndVolume.put("volume", sumVolume.get("sumVolume"));
        return weightAndVolume;
    }

    private LambdaQueryWrapper<VlOrder> getWrapper(VlOrder vlOrder) {
        //拼接查询查询
        LambdaQueryWrapper<VlOrder> wrapper = Wrappers.<VlOrder>lambdaQuery();

        //订单号
        if (StrUtil.isNotBlank(vlOrder.getOrderCode())) {
            wrapper.like(VlOrder::getOrderCode, vlOrder.getOrderCode());
        }

        //车队
        if (StrUtil.isNotBlank(vlOrder.getCoopName())) {
            LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopLambdaQueryWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
            afVPrmCoopLambdaQueryWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).like(AfVPrmCoop::getCoopName, vlOrder.getCoopName()).eq(AfVPrmCoop::getBusinessScopeVL, "VL");
            List<Integer> coopIds = afVPrmCoopService.list(afVPrmCoopLambdaQueryWrapper).stream().map(afVPrmCoop -> afVPrmCoop.getCoopId()).collect(Collectors.toList());
            if (coopIds.size() == 0) {
                return null;
            }
            wrapper.in(VlOrder::getCoopId, coopIds);
        }

        //车牌号
        if (StrUtil.isNotBlank(vlOrder.getTruckNumber())) {
            LambdaQueryWrapper<LcTruck> lcTruckLambdaQueryWrapper = Wrappers.<LcTruck>lambdaQuery();
            lcTruckLambdaQueryWrapper.eq(LcTruck::getOrgId, SecurityUtils.getUser().getOrgId()).eq(LcTruck::getIsValid, true).like(LcTruck::getTruckNumber, vlOrder.getTruckNumber());
            List<Integer> lcTruckIds = lcTruckService.list(lcTruckLambdaQueryWrapper).stream().map(lcTruck -> lcTruck.getTruckId()).collect(Collectors.toList());
            if (lcTruckIds.size() == 0) {
                return null;
            }
            wrapper.in(VlOrder::getTruckId, lcTruckIds);
        }

        //提单号和客户单号
        if (StrUtil.isNotBlank(vlOrder.getAwbNumber()) || StrUtil.isNotBlank(vlOrder.getCustomerNumber())) {
            List<Integer> lastVlOrderIds = new ArrayList<>();
            OrderForVL param = new OrderForVL();
            if (StrUtil.isNotBlank(vlOrder.getAwbNumber())) {
                param.setAwbNumber(vlOrder.getAwbNumber());
            }
            if (StrUtil.isNotBlank(vlOrder.getCustomerNumber())) {
                param.setCustomerNumber(vlOrder.getCustomerNumber());
            }
            param.setBusinessScope("AE");
            List<Integer> afOrderIds = getOrderList(param).stream().map(orderForVL -> orderForVL.getOrderId()).collect(Collectors.toList());
            if (afOrderIds.size() > 0) {
                LambdaQueryWrapper<VlOrderDetailOrder> vlOrderDetailOrderLambdaQueryWrapper = Wrappers.<VlOrderDetailOrder>lambdaQuery();
                vlOrderDetailOrderLambdaQueryWrapper.eq(VlOrderDetailOrder::getOrgId, SecurityUtils.getUser().getOrgId()).in(VlOrderDetailOrder::getOrderId, afOrderIds).likeRight(VlOrderDetailOrder::getBusinessScope, "A");
                List<Integer> vlOrderIds = vlOrderDetailOrderService.list(vlOrderDetailOrderLambdaQueryWrapper).stream().map(vlOrderDetailOrder -> vlOrderDetailOrder.getVlOrderId()).collect(Collectors.toList());
                if (vlOrderIds.size() > 0) {
                    lastVlOrderIds.addAll(vlOrderIds);
                }
            }
            param.setBusinessScope("SC");
            List<Integer> scOrderIds = getOrderList(param).stream().map(orderForVL -> orderForVL.getOrderId()).collect(Collectors.toList());
            if (scOrderIds.size() > 0) {
                LambdaQueryWrapper<VlOrderDetailOrder> vlOrderDetailOrderLambdaQueryWrapper = Wrappers.<VlOrderDetailOrder>lambdaQuery();
                vlOrderDetailOrderLambdaQueryWrapper.eq(VlOrderDetailOrder::getOrgId, SecurityUtils.getUser().getOrgId()).in(VlOrderDetailOrder::getOrderId, scOrderIds).likeRight(VlOrderDetailOrder::getBusinessScope, "S");
                List<Integer> vlOrderIds = vlOrderDetailOrderService.list(vlOrderDetailOrderLambdaQueryWrapper).stream().map(vlOrderDetailOrder -> vlOrderDetailOrder.getVlOrderId()).collect(Collectors.toList());
                if (vlOrderIds.size() > 0) {
                    lastVlOrderIds.addAll(vlOrderIds);
                }
            }
            param.setBusinessScope("TC");
            List<Integer> tcOrderIds = getOrderList(param).stream().map(orderForVL -> orderForVL.getOrderId()).collect(Collectors.toList());
            if (tcOrderIds.size() > 0) {
                LambdaQueryWrapper<VlOrderDetailOrder> vlOrderDetailOrderLambdaQueryWrapper = Wrappers.<VlOrderDetailOrder>lambdaQuery();
                vlOrderDetailOrderLambdaQueryWrapper.eq(VlOrderDetailOrder::getOrgId, SecurityUtils.getUser().getOrgId()).in(VlOrderDetailOrder::getOrderId, tcOrderIds).likeRight(VlOrderDetailOrder::getBusinessScope, "T");
                List<Integer> vlOrderIds = vlOrderDetailOrderService.list(vlOrderDetailOrderLambdaQueryWrapper).stream().map(vlOrderDetailOrder -> vlOrderDetailOrder.getVlOrderId()).collect(Collectors.toList());
                if (vlOrderIds.size() > 0) {
                    lastVlOrderIds.addAll(vlOrderIds);
                }
            }
            param.setBusinessScope("LC");
            List<Integer> lcOrderIds = getOrderList(param).stream().map(orderForVL -> orderForVL.getOrderId()).collect(Collectors.toList());
            if (lcOrderIds.size() > 0) {
                LambdaQueryWrapper<VlOrderDetailOrder> vlOrderDetailOrderLambdaQueryWrapper = Wrappers.<VlOrderDetailOrder>lambdaQuery();
                vlOrderDetailOrderLambdaQueryWrapper.eq(VlOrderDetailOrder::getOrgId, SecurityUtils.getUser().getOrgId()).in(VlOrderDetailOrder::getOrderId, lcOrderIds).likeRight(VlOrderDetailOrder::getBusinessScope, "L");
                List<Integer> vlOrderIds = vlOrderDetailOrderService.list(vlOrderDetailOrderLambdaQueryWrapper).stream().map(vlOrderDetailOrder -> vlOrderDetailOrder.getVlOrderId()).collect(Collectors.toList());
                if (vlOrderIds.size() > 0) {
                    lastVlOrderIds.addAll(vlOrderIds);
                }
            }
            if (lastVlOrderIds.size() == 0) {
                return null;
            }
            wrapper.in(VlOrder::getOrderId, lastVlOrderIds);
        }

        if (vlOrder.getDrivingTimeStart() != null) {
            wrapper.ge(VlOrder::getDrivingTime, vlOrder.getDrivingTimeStart());
        }

        if (vlOrder.getDrivingTimeEnd() != null) {
            wrapper.le(VlOrder::getDrivingTime, vlOrder.getDrivingTimeEnd());
        }

        if (vlOrder.getCreateTimeStart() != null) {
            wrapper.ge(VlOrder::getCreateTime, vlOrder.getCreateTimeStart());
        }

        if (vlOrder.getCreateTimeEnd() != null) {
            wrapper.le(VlOrder::getCreateTime, vlOrder.getCreateTimeEnd());
        }

        if ("已完成".equals(vlOrder.getOrderStatus())) {
            wrapper.eq(VlOrder::getOrderStatus, "完成订单");
        }
        if ("未完成".equals(vlOrder.getOrderStatus())) {
            wrapper.ne(VlOrder::getOrderStatus, "完成订单");
        }
        wrapper.eq(VlOrder::getOrgId, SecurityUtils.getUser().getOrgId()).ne(VlOrder::getOrderStatus, "强制关闭").orderByDesc(VlOrder::getOrderId);
        return wrapper;
    }

    private void fixRecord(List<VlOrder> records) {
        records.stream().forEach(vlOrder -> {
            LambdaQueryWrapper<AfVPrmCoop> wrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
            wrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfVPrmCoop::getCoopId, vlOrder.getCoopId());
            AfVPrmCoop coop = afVPrmCoopService.getOne(wrapper);
            if (coop != null) {
                vlOrder.setCoopName(coop.getCoopName());
            }
            if (vlOrder.getTruckId() != null) {
                LcTruck lcTruck = lcTruckService.getById(vlOrder.getTruckId());
                if (lcTruck != null) {
                    vlOrder.setTruckNumber(lcTruck.getTruckNumber());
                    vlOrder.setTon(lcTruck.getTon());
                    vlOrder.setTonStr(FormatUtils.formatWithQWFNoBit(vlOrder.getTon()));
                    vlOrder.setWeightLimit(lcTruck.getWeightLimit());
                    vlOrder.setWeightLimitStr(FormatUtils.formatWithQWFNoBit(vlOrder.getWeightLimit()));
                    vlOrder.setVolumeLimit(lcTruck.getVolumeLimit());
                    vlOrder.setVolumeLimitStr(FormatUtils.formatWithQWFNoBit(vlOrder.getVolumeLimit()));
                }
            }
            Map<String, BigDecimal> weightAndVolume = getWeightAndVolumeNew(vlOrder.getOrderId());
            vlOrder.setWeight(weightAndVolume.get("weight"));
            vlOrder.setWeightStr(FormatUtils.formatWithQWF(vlOrder.getWeight(), 1));
            vlOrder.setVolume(weightAndVolume.get("volume"));
            vlOrder.setVolumeStr(FormatUtils.formatWithQWF(vlOrder.getVolume(), 3));

            LambdaQueryWrapper<VlOrderDetailOrder> vlOrderDetailOrderLambdaQueryWrapper = Wrappers.<VlOrderDetailOrder>lambdaQuery();
            vlOrderDetailOrderLambdaQueryWrapper.eq(VlOrderDetailOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(VlOrderDetailOrder::getVlOrderId, vlOrder.getOrderId());
            List<VlOrderDetailOrder> detailOrderList = vlOrderDetailOrderService.list(vlOrderDetailOrderLambdaQueryWrapper);
            if (detailOrderList.size() == 1) {
                vlOrder.setTruckType("一车一单");
            } else if (detailOrderList.size() > 1) {
                vlOrder.setTruckType("一车多单");
            }

            if (StrUtil.isNotBlank(vlOrder.getDepartureStation())) {
                Airport airport = remoteServiceToAF.viewAirportCity(vlOrder.getDepartureStation()).getData();
                if (airport != null) {
                    vlOrder.setDepartureStationName(airport.getCityNameCn());
                }
            }
            if (StrUtil.isNotBlank(vlOrder.getArrivalStation())) {
                Airport airport = remoteServiceToAF.viewAirportCity(vlOrder.getArrivalStation()).getData();
                if (airport != null) {
                    vlOrder.setArrivalStationName(airport.getCityNameCn());
                }
            }
        });
    }

    private String createOrderCode() {
        String numberPrefix = "VL-" + DateTimeFormatter.ofPattern("yyMMdd").format(LocalDate.now());
        LambdaQueryWrapper<VlOrder> wrapper = Wrappers.<VlOrder>lambdaQuery();
        wrapper.eq(VlOrder::getOrgId, SecurityUtils.getUser().getOrgId()).like(VlOrder::getOrderCode, "%" + numberPrefix + "%").orderByDesc(VlOrder::getOrderCode).last(" limit 1");

        VlOrder order = getOne(wrapper);

        String numberSuffix = "";
        if (order == null) {
            numberSuffix = "0001";
        } else if (order.getOrderCode().substring(order.getOrderCode().length() - 4).equals("9999")) {
            throw new RuntimeException("今天订单已满无法创建,明天再整吧亲");
        } else {
            String n = Integer.valueOf(order.getOrderCode().substring(order.getOrderCode().length() - 4)) + 1 + "";
            numberSuffix = "0000".substring(0, 4 - n.length()) + n;
        }
        return numberPrefix + numberSuffix;
    }

    private String createUuid() {
        return baseMapper.getUuid();
    }
}
