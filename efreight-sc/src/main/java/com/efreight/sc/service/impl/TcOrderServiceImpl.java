package com.efreight.sc.service.impl;


import com.efreight.common.remoteVo.OrderForVL;
import com.efreight.sc.dao.TcOrderMapper;
import com.efreight.sc.entity.Log;
import com.efreight.sc.entity.Order;
import com.efreight.sc.entity.OrderContainerDetails;
import com.efreight.sc.entity.OrderExcel;
import com.efreight.sc.entity.OrderShipInfo;
import com.efreight.sc.entity.OrderShipperConsignee;
import com.efreight.sc.entity.TcCost;
import com.efreight.sc.entity.TcIncome;
import com.efreight.sc.entity.TcLog;
import com.efreight.sc.entity.TcOrder;
import com.efreight.sc.entity.TcOrderContainerDetails;
import com.efreight.sc.entity.TcOrderShipperConsignee;
import com.efreight.sc.entity.TcProduct;
import com.efreight.sc.entity.view.AfVPrmCoop;
import com.efreight.common.core.feign.RemoteCoopService;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.common.security.vo.CoopVo;
import com.efreight.sc.service.AfVPrmCoopService;
import com.efreight.sc.service.TcCostService;
import com.efreight.sc.service.TcIncomeService;
import com.efreight.sc.service.TcLogService;
import com.efreight.sc.service.TcOrderContainerDetailsService;
import com.efreight.sc.service.TcOrderService;
import com.efreight.sc.service.TcOrderShipperConsigneeService;
import com.efreight.sc.service.TcProductService;
import com.efreight.sc.utils.FieldValUtils;
import com.efreight.sc.utils.LoginUtils;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * TC 订单管理 TE、TI 订单 服务实现类
 * </p>
 *
 * @author caiwd
 * @since 2020-07-13
 */
@Service
@AllArgsConstructor
public class TcOrderServiceImpl extends ServiceImpl<TcOrderMapper, TcOrder> implements TcOrderService {

    private final RemoteCoopService remoteCoopService;
    private final TcOrderShipperConsigneeService orderShipperConsigneeService;
    private final TcOrderContainerDetailsService orderContainerDetailsService;
    private final TcLogService logService;
    private final AfVPrmCoopService afVPrmCoopService;
    private final TcProductService tcProductService;

    private final TcIncomeService tcIncomeService;
    private final TcCostService tcCostService;

    @Override
    public synchronized void saveTE(TcOrder order) {
        String bs = order.getBusinessScope();
        order.setOrderCode(this.createOrderCode(bs));
        if (StrUtil.isBlank(order.getBookingNumber())) {
            order.setBookingNumber(order.getOrderCode());
        }
        order.setOrderUuid(this.createUuid());
        if (order.getDeliverySignDate() != null) {
            order.setOrderStatus("目的港签收");
        } else if (order.getArrivalCustomsClearanceDate() != null) {
            order.setOrderStatus("目的港放行");
        } else if (order.getArrivalCustomsInspectionDate() != null) {
            order.setOrderStatus("目的港查验");
        } else if (order.getCustomsClearanceDate() != null) {
            order.setOrderStatus("海关放行");
        } else if (order.getCustomsInspectionDate() != null) {
            order.setOrderStatus("海关查验");
        } else {
            order.setOrderStatus("订单创建");
        }
        if (order.getPlanChargeWeight() == null) {
            order.setPlanChargeWeight(BigDecimal.ZERO);
        }
        if (order.getPlanVolume() == null) {
            order.setPlanVolume(BigDecimal.ZERO);
        }
        if (order.getPlanWeight() == null) {
            order.setPlanWeight(BigDecimal.ZERO);
        }
        if (order.getPlanPieces() == null) {
            order.setPlanPieces(0);
        }
        LocalDateTime now = LocalDateTime.now();
        if ("单价".equals(order.getMsrType())) {
            order.setMsrUnitprice(order.getMsrPrice());
        }
        if ("总价".equals(order.getMsrType())) {
            order.setMsrAmount(order.getMsrPrice());
        }
        if ("单价".equals(order.getFreightType())) {
            order.setFreightUnitprice(order.getFreightPrice());
        }
        if ("总价".equals(order.getFreightType())) {
            order.setFreightAmount(order.getFreightPrice());
        }
        order.setCreateTime(now);
        order.setEditTime(now);
        order.setCreatorId(SecurityUtils.getUser().getId());
        order.setCreatorName(SecurityUtils.getUser().buildOptName());
        order.setOrgId(SecurityUtils.getUser().getOrgId());
        order.setIncomeStatus("未录收入");
        order.setCostStatus("未录成本");
        order.setIncomeRecorded(false);
        order.setCostRecorded(false);
        save(order);

        //保存收发货人信息
        TcOrderShipperConsignee shipper = order.getShipper();
        TcOrderShipperConsignee consignee = order.getConsignee();
        if (StrUtil.isNotBlank(shipper.getScPrintRemark())) {
            shipper.setCreateTime(now);
            shipper.setCreatorId(SecurityUtils.getUser().getId());
            shipper.setCreatorName(SecurityUtils.getUser().buildOptName());
            shipper.setOrgId(SecurityUtils.getUser().getOrgId());
            shipper.setOrderId(order.getOrderId());
            orderShipperConsigneeService.save(shipper);
        }
        if (StrUtil.isNotBlank(consignee.getScPrintRemark())) {
            consignee.setCreateTime(now);
            consignee.setCreatorId(SecurityUtils.getUser().getId());
            consignee.setCreatorName(SecurityUtils.getUser().buildOptName());
            consignee.setOrgId(SecurityUtils.getUser().getOrgId());
            consignee.setOrderId(order.getOrderId());
            orderShipperConsigneeService.save(consignee);
        }

        //保存集装箱量明细
        if (order.getContainerDetails().size() != 0) {
            order.getContainerDetails().stream().forEach(orderContainerDetail -> {
                orderContainerDetail.setOrderId(order.getOrderId());
                orderContainerDetail.setOrgId(SecurityUtils.getUser().getOrgId());
            });
            orderContainerDetailsService.saveBatch(order.getContainerDetails());
        }

        TcLog logBean = new TcLog();
        logBean.setPageName(bs+"订单");
        logBean.setPageFunction("订单创建");
        logBean.setBusinessScope(bs);
        logBean.setOrderNumber(order.getOrderCode());
        logBean.setOrderId(order.getOrderId());
        logBean.setOrderUuid(order.getOrderUuid());
        logBean.setCreatorId(SecurityUtils.getUser().getId());
        logBean.setCreatorName(SecurityUtils.getUser().buildOptName());
        logBean.setCreatTime(LocalDateTime.now());
        logBean.setOrgId(SecurityUtils.getUser().getOrgId());
        logService.save(logBean);
    }

    private String createUuid() {
        return baseMapper.getUuid();
    }

    private String createOrderCode(String businessScope) {
        String numberPrefix = businessScope + "-" + DateTimeFormatter.ofPattern("yyMMdd").format(LocalDate.now());
        LambdaQueryWrapper<TcOrder> wrapper = Wrappers.<TcOrder>lambdaQuery();
        wrapper.eq(TcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).like(TcOrder::getOrderCode, "%" + numberPrefix + "%").orderByDesc(TcOrder::getOrderCode).last(" limit 1");

        TcOrder order = getOne(wrapper);

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

    @Override
    public IPage getTEPage(Page page, TcOrder order) {
        LambdaQueryWrapper<TcOrder> wrapper = Wrappers.<TcOrder>lambdaQuery();
//        String bs = order.getBusinessScope();
//        if (StrUtil.isNotBlank(order.getCoopName())) {
//            List<Integer> coopIds = remoteCoopService.listByCoopName(order.getCoopName()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
//            if (coopIds.size() == 0) {
//                page.setRecords(new ArrayList());
//                page.setTotal(0);
//                return page;
//            }
//            wrapper.in(TcOrder::getCoopId, coopIds);
//        }
//        if (StrUtil.isNotBlank(order.getCustomerNumber())) {
//            wrapper.like(TcOrder::getCustomerNumber, order.getCustomerNumber());
//        }
//        if (StrUtil.isNotBlank(order.getContainerMethod())) {
//            wrapper.eq(TcOrder::getContainerMethod, order.getContainerMethod());
//        }
//        if (order.getExpectDepartureStart() != null) {
//            if("TE".equals(bs)){
//                wrapper.ge(TcOrder::getExpectDeparture, order.getExpectDepartureStart());
//            }else{
//                wrapper.ge(TcOrder::getExpectArrival, order.getExpectDepartureStart());
//            }
//
//        }
//        if (order.getExpectDepartureEnd() != null) {
//            if("TE".equals(bs)){
//                wrapper.le(TcOrder::getExpectDeparture, order.getExpectDepartureEnd());
//            }else{
//                wrapper.le(TcOrder::getExpectArrival, order.getExpectDepartureEnd());
//            }
//
//        }
//        if (order.getCreatTimeStart() != null) {
//            wrapper.ge(TcOrder::getCreateTime, order.getCreatTimeStart());
//        }
//        if (order.getCreatTimeEnd() != null) {
//            wrapper.le(TcOrder::getCreateTime, LocalDateTime.of(order.getCreatTimeEnd(), LocalTime.parse("23:59:59")));
//        }
//        wrapper.ne(TcOrder::getOrderStatus, "强制关闭");
//        if ("未锁账".equals(order.getOrderStatus())) {
//            wrapper.ne(TcOrder::getOrderStatus, "财务锁账");
//        }
//        if ("已锁账".equals(order.getOrderStatus())) {
//            wrapper.eq(TcOrder::getOrderStatus, "财务锁账");
//        }
//        if (StrUtil.isNotBlank(order.getRwbNumber())) {
//            wrapper.like(TcOrder::getRwbNumber, order.getRwbNumber());
//        }
//        if (StrUtil.isNotBlank(order.getOrderCode())) {
//            wrapper.like(TcOrder::getOrderCode, order.getOrderCode());
//        }
//        if (StrUtil.isNotBlank(order.getDepartureStation())) {
//            wrapper.like(TcOrder::getDepartureStation, order.getDepartureStation());
//        }
//        if (StrUtil.isNotBlank(order.getArrivalStation())) {
//            wrapper.like(TcOrder::getArrivalStation, order.getArrivalStation());
//        }
//        if (StrUtil.isNotBlank(order.getProductType())) {
//            wrapper.eq(TcOrder::getProductType, order.getProductType());
//        }
//        if (order.getServicerId() != null) {
//            wrapper.eq(TcOrder::getServicerId, order.getServicerId());
//        }
//        if (order.getSalesId() != null) {
//            wrapper.eq(TcOrder::getSalesId, order.getSalesId());
//        }
//        if (order.getCreatorId() != null) {
//            wrapper.eq(TcOrder::getCreatorId, order.getCreatorId());
//        }
//        if (StrUtil.isNotBlank(order.getBookingAgentName())) {
//            LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
//            if("TE".equals(bs)){
//                afVPrmCoopWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).like(AfVPrmCoop::getCoopName, order.getBookingAgentName()).eq(AfVPrmCoop::getBusinessScopeTE, "TE").in(AfVPrmCoop::getCoopType, "互为代理", "海外代理");
//            }else{
//                afVPrmCoopWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).like(AfVPrmCoop::getCoopName, order.getBookingAgentName()).eq(AfVPrmCoop::getBusinessScopeTI, "TI").in(AfVPrmCoop::getCoopType, "互为代理", "海外代理");
//            }
//             List<Integer> bookAgentIds = afVPrmCoopService.list(afVPrmCoopWrapper).stream().map(AfVPrmCoop::getCoopId).collect(Collectors.toList());
////            if (bookAgentIds.size() == 0) {
////                page.setRecords(new ArrayList());
////                page.setTotal(0);
////                return page;
////            }
//            wrapper.in(TcOrder::getBookingAgentId, bookAgentIds);
//        }
//        if (order.getIncomeRecorded() != null && order.getIncomeRecorded()) {
//            wrapper.eq(TcOrder::getIncomeRecorded, order.getIncomeRecorded());
//        }
//        if (order.getIncomeRecorded() != null && !order.getIncomeRecorded()) {
//            wrapper.and(i -> i.eq(TcOrder::getIncomeRecorded, order.getIncomeRecorded()).or(j -> j.isNull(TcOrder::getIncomeRecorded)));
//        }
//        if (order.getCostRecorded() != null && order.getCostRecorded()) {
//            wrapper.eq(TcOrder::getCostRecorded, order.getCostRecorded());
//        }
//        if (order.getCostRecorded() != null && !order.getCostRecorded()) {
//            wrapper.and(i -> i.eq(TcOrder::getCostRecorded, order.getCostRecorded()).or(j -> j.isNull(TcOrder::getCostRecorded)));
//        }
//        if (order.getOrderPermission() == 1) {
//            wrapper.and(i -> i.eq(TcOrder::getCreatorId, order.getCurrentUserId()).or(j -> j.eq(TcOrder::getSalesId, order.getCurrentUserId())).or(k -> k.eq(TcOrder::getServicerId, order.getCurrentUserId())));
//        }
//        if (order.getOrderPermission() == 2) {
//            List<Integer> WorkgroupIds = baseMapper.getWorkgroupIds(order.getCurrentUserId());
//            wrapper.and(i -> i.eq(TcOrder::getCreatorId, order.getCurrentUserId()).or(j -> j.eq(TcOrder::getSalesId, order.getCurrentUserId())).or(k -> k.eq(TcOrder::getServicerId, order.getCurrentUserId())).or(m -> m.in(TcOrder::getWorkgroupId, WorkgroupIds)));
//        }
//        if (StrUtil.isNotBlank(order.getBookingNumber())) {
//            wrapper.like(TcOrder::getBookingNumber, order.getBookingNumber());
//        }
//        wrapper.eq(TcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TcOrder::getBusinessScope, bs).orderByDesc(TcOrder::getOrderId);

        if (getWrapper(order, wrapper)) {
            page.setRecords(new ArrayList());
            page.setTotal(0);
            return page;
        }
        IPage<TcOrder> iPage = baseMapper.selectPage(page, wrapper);
        if (iPage != null && iPage.getRecords() != null && iPage.getRecords().size() > 0) {
            iPage.getRecords().stream().forEach(tcOrder -> {
                if (tcOrder.getCoopId() != null) {
                    CoopVo coop = remoteCoopService.viewCoop(tcOrder.getCoopId().toString()).getData();
                    if (coop != null) {
                        tcOrder.setCustomerName(coop.getCoop_name());
                    }
                }
                if (tcOrder.getBookingAgentId() != null) {
                    LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
                    afVPrmCoopWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfVPrmCoop::getCoopId, tcOrder.getBookingAgentId());
                    AfVPrmCoop bookingAgent = afVPrmCoopService.getOne(afVPrmCoopWrapper);
                    if (bookingAgent != null) {
                        tcOrder.setBookingAgentName(bookingAgent.getCoopName());
                    }
                }
                //产品
                if (tcOrder.getRailwayProductId() != null) {
                    TcProduct product = tcProductService.getById(tcOrder.getRailwayProductId());
                    if (product != null) {
                        tcOrder.setProductName(product.getProductName());
                    }
                }
                StringBuffer buffer = new StringBuffer();
                LambdaQueryWrapper<TcOrderContainerDetails> orderContainerDetailsLambdaQueryWrapper = Wrappers.<TcOrderContainerDetails>lambdaQuery();
                orderContainerDetailsLambdaQueryWrapper.eq(TcOrderContainerDetails::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TcOrderContainerDetails::getOrderId, tcOrder.getOrderId());
                orderContainerDetailsService.list(orderContainerDetailsLambdaQueryWrapper).stream().forEach(orderContainerDetails -> {
                    if (buffer.length() == 0) {
                        if (StrUtil.isNotBlank(orderContainerDetails.getContainerNumber()) && StrUtil.isNotBlank(orderContainerDetails.getContainerSealNo())) {
                            buffer.append(orderContainerDetails.getContainerNumber()).append(" / ").append(orderContainerDetails.getContainerSealNo());
                        } else if (StrUtil.isNotBlank(orderContainerDetails.getContainerNumber())) {
                            buffer.append(orderContainerDetails.getContainerNumber());
                        } else if (StrUtil.isNotBlank(orderContainerDetails.getContainerSealNo())) {
                            buffer.append(orderContainerDetails.getContainerSealNo());
                        }
                    } else {
                        if (StrUtil.isNotBlank(orderContainerDetails.getContainerNumber()) && StrUtil.isNotBlank(orderContainerDetails.getContainerSealNo())) {
                            buffer.append("\n").append(orderContainerDetails.getContainerNumber()).append(" / ").append(orderContainerDetails.getContainerSealNo());
                        } else if (StrUtil.isNotBlank(orderContainerDetails.getContainerNumber())) {
                            buffer.append("\n").append(orderContainerDetails.getContainerNumber());
                        } else if (StrUtil.isNotBlank(orderContainerDetails.getContainerSealNo())) {
                            buffer.append("\n").append(orderContainerDetails.getContainerSealNo());
                        }
                    }
                });
                tcOrder.setContainerNumberAndContainerSealNo(buffer.toString());
                //设置收入完成和成本完成（排序使用）
                if(tcOrder.getIncomeRecorded() == true){
                    tcOrder.setIncomeRecordedForSort(2);
                }else if(StringUtils.isNotBlank(tcOrder.getIncomeStatus()) && !"未录收入".equals(tcOrder.getIncomeStatus())){
                    tcOrder.setIncomeRecordedForSort(1);
                }else{
                    tcOrder.setIncomeRecordedForSort(0);
                }
                if(tcOrder.getCostRecorded() == true){
                    tcOrder.setCostRecordedForSort(2);
                }else if(StringUtils.isNotBlank(tcOrder.getCostStatus()) && !"未录成本".equals(tcOrder.getCostStatus())){
                    tcOrder.setCostRecordedForSort(1);
                }else{
                    tcOrder.setCostRecordedForSort(0);
                }
            });
        }
        return iPage;
    }

    public TcOrder getTETotal(TcOrder order) {
        LambdaQueryWrapper<TcOrder> wrapper = Wrappers.<TcOrder>lambdaQuery();
        if (getWrapper(order, wrapper)) return null;
        List<TcOrder> list = baseMapper.selectList(wrapper);
        TcOrder total = new TcOrder();
        if (list != null && list.size() > 0) {
            list.stream().forEach(tcOrder -> {
                total.setOrderStatus("合计:");
                //统计标箱数量
                if (total.getContainerNumber() == null) {
                    total.setContainerNumber(tcOrder.getContainerNumber() == null ? 0 : tcOrder.getContainerNumber());
                } else {
                    total.setContainerNumber(total.getContainerNumber() + (tcOrder.getContainerNumber() == null ? 0 : tcOrder.getContainerNumber()));
                }
                //统计件数
                if (total.getPlanPieces() == null) {
                    total.setPlanPieces(tcOrder.getPlanPieces() == null ? 0 : tcOrder.getPlanPieces());
                } else {
                    total.setPlanPieces(total.getPlanPieces() + (tcOrder.getPlanPieces() == null ? 0 : tcOrder.getPlanPieces()));
                }
                //统计毛重
                if (total.getPlanWeight() == null) {
                    total.setPlanWeight(tcOrder.getPlanWeight() == null ? BigDecimal.ZERO : tcOrder.getPlanWeight());
                } else {
                    total.setPlanWeight(total.getPlanWeight().add(tcOrder.getPlanWeight() == null ? BigDecimal.ZERO : tcOrder.getPlanWeight()));
                }
                //统计体积
                if (total.getPlanVolume() == null) {
                    total.setPlanVolume(tcOrder.getPlanVolume() == null ? BigDecimal.ZERO : tcOrder.getPlanVolume());
                } else {
                    total.setPlanVolume(total.getPlanVolume().add(tcOrder.getPlanVolume() == null ? BigDecimal.ZERO : tcOrder.getPlanVolume()));
                }
                //统计计重
                if (total.getPlanChargeWeight() == null) {
                    total.setPlanChargeWeight(tcOrder.getPlanChargeWeight() == null ? BigDecimal.ZERO : tcOrder.getPlanChargeWeight());
                } else {
                    total.setPlanChargeWeight(total.getPlanChargeWeight().add(tcOrder.getPlanChargeWeight() == null ? BigDecimal.ZERO : tcOrder.getPlanChargeWeight()));
                }
            });
            if (StrUtil.isBlank(total.getOrderStatus())) {
                return null;
            }
            total.setPlanWeightStr(FormatUtils.formatWithQWF(total.getPlanWeight(), 3));
            total.setPlanChargeWeightStr(FormatUtils.formatWithQWF(total.getPlanChargeWeight(), 3));
            total.setPlanVolumeStr(FormatUtils.formatWithQWF(total.getPlanVolume(), 3));
        }
        return total;
    }

    private boolean getWrapper(TcOrder order, LambdaQueryWrapper<TcOrder> wrapper) {
        String bs = order.getBusinessScope();
        if (StrUtil.isNotBlank(order.getCoopName())) {
            List<Integer> coopIds = remoteCoopService.listByCoopName(order.getCoopName()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
            if (coopIds.size() == 0) {
                return true;
            }
            wrapper.in(TcOrder::getCoopId, coopIds);
        }
        if (StrUtil.isNotBlank(order.getCustomerNumber())) {
            wrapper.like(TcOrder::getCustomerNumber, order.getCustomerNumber());
        }
        if (StrUtil.isNotBlank(order.getContainerMethod())) {
            wrapper.eq(TcOrder::getContainerMethod, order.getContainerMethod());
        }
        if (order.getExpectDepartureStart() != null) {
            if("TE".equals(bs)){
                wrapper.ge(TcOrder::getExpectDeparture, order.getExpectDepartureStart());
            }else{
                wrapper.ge(TcOrder::getExpectArrival, order.getExpectDepartureStart());
            }
        }
        if (order.getExpectDepartureEnd() != null) {
            if("TE".equals(bs)){
                wrapper.le(TcOrder::getExpectDeparture, order.getExpectDepartureEnd());
            }else{
                wrapper.le(TcOrder::getExpectArrival, order.getExpectDepartureEnd());
            }
        }
        if (order.getCreatTimeStart() != null) {
            wrapper.ge(TcOrder::getCreateTime, order.getCreatTimeStart());
        }
        if (order.getCreatTimeEnd() != null) {
            wrapper.le(TcOrder::getCreateTime, LocalDateTime.of(order.getCreatTimeEnd(), LocalTime.parse("23:59:59")));
        }
        wrapper.ne(TcOrder::getOrderStatus, "强制关闭");
        if ("未锁账".equals(order.getOrderStatus())) {
            wrapper.ne(TcOrder::getOrderStatus, "财务锁账");
        }
        if ("已锁账".equals(order.getOrderStatus())) {
            wrapper.eq(TcOrder::getOrderStatus, "财务锁账");
        }
        if (StrUtil.isNotBlank(order.getRwbNumber())) {
            wrapper.like(TcOrder::getRwbNumber, order.getRwbNumber());
        }
        if (StrUtil.isNotBlank(order.getOrderCode())) {
            wrapper.like(TcOrder::getOrderCode, order.getOrderCode());
        }
        if (StrUtil.isNotBlank(order.getDepartureStation())) {
            wrapper.like(TcOrder::getDepartureStation, order.getDepartureStation());
        }
        if (StrUtil.isNotBlank(order.getArrivalStation())) {
            wrapper.like(TcOrder::getArrivalStation, order.getArrivalStation());
        }
        if (StrUtil.isNotBlank(order.getProductType())) {
            wrapper.eq(TcOrder::getProductType, order.getProductType());
        }
        if (order.getServicerId() != null) {
            wrapper.eq(TcOrder::getServicerId, order.getServicerId());
        }
        if (order.getSalesId() != null) {
            wrapper.eq(TcOrder::getSalesId, order.getSalesId());
        }
        if (order.getCreatorId() != null) {
            wrapper.eq(TcOrder::getCreatorId, order.getCreatorId());
        }
        if (StrUtil.isNotBlank(order.getBookingAgentName())) {
            LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
            if("TE".equals(bs)){
                afVPrmCoopWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).like(AfVPrmCoop::getCoopName, order.getBookingAgentName()).eq(AfVPrmCoop::getBusinessScopeTE, "TE").in(AfVPrmCoop::getCoopType, "互为代理", "海外代理");
            }else{
                afVPrmCoopWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).like(AfVPrmCoop::getCoopName, order.getBookingAgentName()).eq(AfVPrmCoop::getBusinessScopeTI, "TI").in(AfVPrmCoop::getCoopType, "互为代理", "海外代理");
            }
            List<Integer> bookAgentIds = afVPrmCoopService.list(afVPrmCoopWrapper).stream().map(AfVPrmCoop::getCoopId).collect(Collectors.toList());
            if (bookAgentIds.size() == 0) {
                return true;
            }
            wrapper.in(TcOrder::getBookingAgentId, bookAgentIds);
        }
        if (order.getIncomeRecorded() != null && order.getIncomeRecorded()) {
            wrapper.eq(TcOrder::getIncomeRecorded, order.getIncomeRecorded());
        }
        if (order.getIncomeRecorded() != null && !order.getIncomeRecorded()) {
            wrapper.and(i -> i.eq(TcOrder::getIncomeRecorded, order.getIncomeRecorded()).or(j -> j.isNull(TcOrder::getIncomeRecorded)));
        }
        if (order.getCostRecorded() != null && order.getCostRecorded()) {
            wrapper.eq(TcOrder::getCostRecorded, order.getCostRecorded());
        }
        if (order.getCostRecorded() != null && !order.getCostRecorded()) {
            wrapper.and(i -> i.eq(TcOrder::getCostRecorded, order.getCostRecorded()).or(j -> j.isNull(TcOrder::getCostRecorded)));
        }
        if (order.getOrderPermission() != null && order.getOrderPermission() == 1) {
            wrapper.and(i -> i.eq(TcOrder::getCreatorId, order.getCurrentUserId()).or(j -> j.eq(TcOrder::getSalesId, order.getCurrentUserId())).or(k -> k.eq(TcOrder::getServicerId, order.getCurrentUserId())));
        }
        if (order.getOrderPermission() != null && order.getOrderPermission() == 2) {
            List<Integer> WorkgroupIds = baseMapper.getWorkgroupIds(order.getCurrentUserId());
            wrapper.and(i -> i.eq(TcOrder::getCreatorId, order.getCurrentUserId()).or(j -> j.eq(TcOrder::getSalesId, order.getCurrentUserId())).or(k -> k.eq(TcOrder::getServicerId, order.getCurrentUserId())).or(m -> m.in(TcOrder::getWorkgroupId, WorkgroupIds)));
        }
        wrapper.eq(TcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TcOrder::getBusinessScope,  bs).orderByDesc(TcOrder::getOrderId);
        return false;
    }

    @Override
    public TcOrder view(Integer orderId) {
        TcOrder tcOrder = baseMapper.selectById(orderId);
        if (tcOrder != null) {
            LambdaQueryWrapper<TcOrderShipperConsignee> shipperWrapper = Wrappers.<TcOrderShipperConsignee>lambdaQuery();
            shipperWrapper.eq(TcOrderShipperConsignee::getOrderId, orderId).eq(TcOrderShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TcOrderShipperConsignee::getScType, 0);
            TcOrderShipperConsignee shipper = orderShipperConsigneeService.getOne(shipperWrapper);
            if (shipper == null) {
                shipper = new TcOrderShipperConsignee();
                shipper.setScPrintRemark("");
            }
            tcOrder.setShipper(shipper);

            LambdaQueryWrapper<TcOrderShipperConsignee> consigneeWrapper = Wrappers.<TcOrderShipperConsignee>lambdaQuery();
            consigneeWrapper.eq(TcOrderShipperConsignee::getOrderId, orderId).eq(TcOrderShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TcOrderShipperConsignee::getScType, 1);
            TcOrderShipperConsignee consignee = orderShipperConsigneeService.getOne(consigneeWrapper);
            if (consignee == null) {
                consignee = new TcOrderShipperConsignee();
                consignee.setScPrintRemark("");
            }
            tcOrder.setConsignee(consignee);

            CoopVo coop = remoteCoopService.viewCoop(tcOrder.getCoopId().toString()).getData();
            if (coop != null) {
                tcOrder.setCustomerName(coop.getCoop_name());
            }
            LambdaQueryWrapper<TcOrderContainerDetails> orderContainerDetailsWrapper = Wrappers.<TcOrderContainerDetails>lambdaQuery();
            orderContainerDetailsWrapper.eq(TcOrderContainerDetails::getOrderId, orderId).eq(TcOrderContainerDetails::getOrgId, SecurityUtils.getUser().getOrgId());
            List<TcOrderContainerDetails> orderContainerDetails = orderContainerDetailsService.list(orderContainerDetailsWrapper);
            tcOrder.setContainerDetails(orderContainerDetails);
            if (tcOrder.getBookingAgentId() != null) {
                LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
                afVPrmCoopWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfVPrmCoop::getCoopId, tcOrder.getBookingAgentId());
                AfVPrmCoop bookingAgent = afVPrmCoopService.getOne(afVPrmCoopWrapper);
                if (bookingAgent != null) {
                    tcOrder.setBookingAgentName(bookingAgent.getCoopName());
                }
            }
            //产品
            if (tcOrder.getRailwayProductId() != null) {
                TcProduct product = tcProductService.getById(tcOrder.getRailwayProductId());
                if (product != null) {
                    tcOrder.setProductName(product.getProductName());
                }
            }
            tcOrder.setMsrType("单价");
            if (tcOrder.getMsrAmount() != null) {
                tcOrder.setMsrPrice(tcOrder.getMsrAmount());
                tcOrder.setMsrType("总价");
            }
            if (tcOrder.getMsrUnitprice() != null) {
                tcOrder.setMsrPrice(tcOrder.getMsrUnitprice());
            }

            tcOrder.setFreightType("单价");
            if (tcOrder.getFreightAmount() != null) {
                tcOrder.setFreightPrice(tcOrder.getFreightAmount());
                tcOrder.setFreightType("总价");
            }
            if (tcOrder.getFreightUnitprice() != null) {
                tcOrder.setFreightPrice(tcOrder.getFreightUnitprice());
            }
        }

        return tcOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceStopTE(Integer orderId, String reason) {
        //1.校验可否强制关闭
        checkIfForceStop(orderId);
        //2.执行强制关闭
        TcOrder order = getById(orderId);
        order.setOrderStatus("强制关闭");
        order.setEditorId(SecurityUtils.getUser().getId());
        order.setEditorName(SecurityUtils.getUser().buildOptName());
        order.setEditTime(LocalDateTime.now());
        updateById(order);

        //3.删除费用明细
        LambdaQueryWrapper<TcIncome> incomeWrapper = Wrappers.<TcIncome>lambdaQuery();
        LambdaQueryWrapper<TcCost> costWrapper = Wrappers.<TcCost>lambdaQuery();
        incomeWrapper.eq(TcIncome::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TcIncome::getOrderId, orderId);
        costWrapper.eq(TcCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TcCost::getOrderId, orderId);
        tcIncomeService.remove(incomeWrapper);
        tcCostService.remove(costWrapper);
        //日志
        String bs = order.getBusinessScope();
        TcLog logBean = new TcLog();
        logBean.setPageName(bs+"订单");
        logBean.setPageFunction("强制关闭");
        logBean.setBusinessScope(bs);
        logBean.setLogRemark(reason);
        logBean.setOrderNumber(order.getOrderCode());
        logBean.setOrderId(order.getOrderId());
        logBean.setOrderUuid(order.getOrderUuid());
        logBean.setCreatorId(SecurityUtils.getUser().getId());
        logBean.setCreatorName(SecurityUtils.getUser().buildOptName());
        logBean.setCreatTime(LocalDateTime.now());
        logBean.setOrgId(SecurityUtils.getUser().getOrgId());
        logService.save(logBean);
        //HRS日志
        baseMapper.insertHrsLog(bs+"订单", "订单号:" + order.getOrderCode(),
                SecurityUtils.getUser().getId(), LocalDateTime.now(), SecurityUtils.getUser().getOrgId(), SecurityUtils.getUser().getDeptId());

    }

    private void checkIfForceStop(Integer orderId) {
        TcOrder order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("无该订单,强制关闭失败");
        }
        if ("强制关闭".equals(order.getOrderStatus())) {
            throw new RuntimeException("订单状态为强制关闭,无法再次强制关闭");
        }

        if ("财务锁账".equals(order.getOrderStatus())) {
            throw new RuntimeException("订单状态为财务锁账,强制关闭失败");
        }
        LambdaQueryWrapper<TcIncome> incomeWrapper = Wrappers.<TcIncome>lambdaQuery();
        incomeWrapper.eq(TcIncome::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TcIncome::getOrderId, orderId).isNotNull(TcIncome::getDebitNoteId);
        List<TcIncome> incomeList = tcIncomeService.list(incomeWrapper);
        if (incomeList.size() != 0) {
            throw new RuntimeException("订单已应收对账,强制关闭失败");
        }
        List<Integer> list = baseMapper.queryPaymentForIfForceStop(orderId, SecurityUtils.getUser().getOrgId(), order.getBusinessScope());
        if (list.size() != 0) {
            throw new RuntimeException("订单已成本对账,强制关闭失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyTE(TcOrder order) {
        //0.校验状态-财务锁账不可修改
        TcOrder orderForCheck = getById(order.getOrderId());
        if (orderForCheck == null) {
            throw new RuntimeException("订单不存在，修改失败");
        }
        if ("财务锁账".equals(orderForCheck.getOrderStatus())) {
            throw new RuntimeException("订单财务已锁账，无法修改");
        }

        //1.修改订单
        TcOrder bean = baseMapper.selectById(order.getOrderId());
        if (order.getPlanChargeWeight() == null) {
            order.setPlanChargeWeight(BigDecimal.ZERO);
        }
        if (order.getPlanVolume() == null) {
            order.setPlanVolume(BigDecimal.ZERO);
        }
        if (order.getPlanWeight() == null) {
            order.setPlanWeight(BigDecimal.ZERO);
        }
        if (order.getPlanPieces() == null) {
            order.setPlanPieces(0);
        }
        if (order.getDeliverySignDate() != null) {
            order.setOrderStatus("目的港签收");
        } else if (order.getArrivalCustomsClearanceDate() != null) {
            order.setOrderStatus("目的港放行");
        } else if (order.getArrivalCustomsInspectionDate() != null) {
            order.setOrderStatus("目的港查验");
        } else if (order.getCustomsClearanceDate() != null) {
            order.setOrderStatus("海关放行");
        } else if (order.getCustomsInspectionDate() != null) {
            order.setOrderStatus("海关查验");
        } else {
            order.setOrderStatus("订单创建");
        }
        LocalDateTime now = LocalDateTime.now();
        if ("单价".equals(order.getMsrType())) {
            order.setMsrUnitprice(order.getMsrPrice());
        } else {
            order.setMsrUnitprice(null);
        }
        if ("总价".equals(order.getMsrType())) {
            order.setMsrAmount(order.getMsrPrice());
        } else {
            order.setMsrAmount(null);
        }
        if ("单价".equals(order.getFreightType())) {
            order.setFreightUnitprice(order.getFreightPrice());
        } else {
            order.setFreightUnitprice(null);
        }
        if ("总价".equals(order.getFreightType())) {
            order.setFreightAmount(order.getFreightPrice());
        } else {
            order.setFreightAmount(null);
        }
        order.setEditTime(now);
        order.setEditorId(SecurityUtils.getUser().getId());
        order.setEditorName(SecurityUtils.getUser().buildOptName());
        updateById(order);

        //2.修改收发货人
        TcOrderShipperConsignee shipper = order.getShipper();
        TcOrderShipperConsignee consignee = order.getConsignee();
        if (StrUtil.isNotBlank(shipper.getScPrintRemark())) {
            if (shipper.getOrderScId() == null) {
                shipper.setCreateTime(now);
                shipper.setCreatorId(SecurityUtils.getUser().getId());
                shipper.setCreatorName(SecurityUtils.getUser().buildOptName());
                shipper.setOrgId(SecurityUtils.getUser().getOrgId());
                shipper.setEditorId(SecurityUtils.getUser().getId());
                shipper.setEditorName(SecurityUtils.getUser().buildOptName());
                shipper.setEditTime(now);
                shipper.setOrderId(order.getOrderId());
                orderShipperConsigneeService.save(shipper);
            } else {
                shipper.setEditorId(SecurityUtils.getUser().getId());
                shipper.setEditorName(SecurityUtils.getUser().buildOptName());
                shipper.setEditTime(now);
                orderShipperConsigneeService.updateById(shipper);
            }
        } else {
            if (shipper.getOrderScId() != null) {
                orderShipperConsigneeService.removeById(shipper.getOrderScId());
            }
        }

        if (StrUtil.isNotBlank(consignee.getScPrintRemark())) {
            if (consignee.getOrderScId() == null) {
                consignee.setCreateTime(now);
                consignee.setCreatorId(SecurityUtils.getUser().getId());
                consignee.setCreatorName(SecurityUtils.getUser().buildOptName());
                consignee.setOrgId(SecurityUtils.getUser().getOrgId());
                consignee.setEditorId(SecurityUtils.getUser().getId());
                consignee.setEditorName(SecurityUtils.getUser().buildOptName());
                consignee.setEditTime(now);
                consignee.setOrderId(order.getOrderId());
                orderShipperConsigneeService.save(consignee);
            } else {
                consignee.setEditorId(SecurityUtils.getUser().getId());
                consignee.setEditorName(SecurityUtils.getUser().buildOptName());
                consignee.setEditTime(now);
                orderShipperConsigneeService.updateById(consignee);
            }
        } else {
            if (consignee.getOrderScId() != null) {
                orderShipperConsigneeService.removeById(consignee.getOrderScId());
            }
        }
        //4.保存集装箱量明细
        LambdaQueryWrapper<TcOrderContainerDetails> orderContainerDetailsWrapper = Wrappers.<TcOrderContainerDetails>lambdaQuery();
        orderContainerDetailsWrapper.eq(TcOrderContainerDetails::getOrderId, order.getOrderId()).eq(TcOrderContainerDetails::getOrgId, SecurityUtils.getUser().getOrgId());
        orderContainerDetailsService.remove(orderContainerDetailsWrapper);
        if (order.getContainerDetails().size() != 0) {
            order.getContainerDetails().stream().forEach(orderContainerDetail -> {
                orderContainerDetail.setOrderId(order.getOrderId());
                orderContainerDetail.setOrgId(SecurityUtils.getUser().getOrgId());
            });
            orderContainerDetailsService.saveBatch(order.getContainerDetails());
        }
        //5.修改日志
        TcLog logBean = new TcLog();
        String bs = order.getBusinessScope();
        logBean.setPageName(bs+"订单");
        logBean.setPageFunction("订单编辑");
        logBean.setLogRemark(this.getLogRemarkTE(bean, order));
        logBean.setBusinessScope(bs);
        logBean.setOrderNumber(order.getOrderCode());
        logBean.setOrderId(order.getOrderId());
        logBean.setOrderUuid(order.getOrderUuid());
        logBean.setCreatorId(SecurityUtils.getUser().getId());
        logBean.setCreatorName(SecurityUtils.getUser().buildOptName());
        logBean.setCreatTime(LocalDateTime.now());
        logBean.setOrgId(SecurityUtils.getUser().getOrgId());
        logService.save(logBean);

    }

    private String getLogRemarkTE(TcOrder order, TcOrder bean) {
        StringBuffer logremark = new StringBuffer();
        String coopName = this.getStr(order.getCustomerName(), bean.getCustomerName());
        logremark.append(StringUtils.isBlank(coopName) ? "" : "客户：" + coopName);
        String salesName = this.getStr(order.getSalesName(), bean.getSalesName());
        logremark.append(StringUtils.isBlank(salesName) ? "" : "销售：" + salesName);
        String mblNumber = this.getStr(order.getRwbNumber(), bean.getRwbNumber());
        logremark.append(StringUtils.isBlank(mblNumber) ? "" : "运单：" + mblNumber);
        String customerNumber = this.getStr(order.getCustomerNumber(), bean.getCustomerNumber());
        logremark.append(StringUtils.isBlank(customerNumber) ? "" : "合约号：" + customerNumber);
        String expectDeparture = this.getStr("" + order.getExpectDeparture(), "" + bean.getExpectDeparture());
        logremark.append(StringUtils.isBlank(expectDeparture) ? "" : "离港：" + expectDeparture);


        String containerMethod = this.getStr(order.getContainerMethod(), "" + bean.getContainerMethod());
        logremark.append(StringUtils.isBlank(containerMethod) ? "" : "装箱：" + containerMethod);
        String containerList = this.getStr(order.getContainerList(), "" + bean.getContainerList());
        logremark.append(StringUtils.isBlank(containerList) ? "" : "箱量：" + containerList);

        //件/毛/体
        String orderStr11 = "空";
        String orderStr22 = "空";
        String orderStr33 = "空";
        String beanStr11 = "空";
        String beanStr22 = "空";
        String beanStr33 = "空";
        if (order.getPlanPieces() != null || !"null".equals("" + order.getPlanPieces())) {
            orderStr11 = "" + order.getPlanPieces();
        }
        if (order.getPlanWeight() != null || !"null".equals("" + order.getPlanWeight())) {
            orderStr22 = this.fmtMicrometer3(String.valueOf(order.getPlanWeight()));
        }
        if (order.getPlanVolume() != null || !"null".equals("" + order.getPlanVolume())) {
            orderStr33 = this.fmtMicrometer3(String.valueOf(order.getPlanVolume()));
        }


        if (bean.getPlanPieces() != null || !"null".equals("" + bean.getPlanPieces())) {
            beanStr11 = "" + bean.getPlanPieces();
        }
        if (bean.getPlanWeight() != null || !"null".equals("" + bean.getPlanWeight())) {
            beanStr22 = this.fmtMicrometer3(String.valueOf(bean.getPlanWeight()));
        }
        if (bean.getPlanVolume() != null || !"null".equals("" + bean.getPlanVolume())) {
            beanStr33 = this.fmtMicrometer3(String.valueOf(bean.getPlanVolume()));
        }

        if (!(orderStr11 + "/" + orderStr22 + "/" + orderStr33).equals(beanStr11 + "/" + beanStr22 + "/" + beanStr33)) {
            logremark.append("件/毛/体：" + orderStr11 + "/" + orderStr22 + "/" + orderStr33 + " -> " + beanStr11 + "/" + beanStr22 + "/" + beanStr33 + "  ");
        }

        String planChargeWeight = this.getStr("" + Double.parseDouble("" + order.getPlanChargeWeight()), this.fmtMicrometer3(String.valueOf(bean.getPlanChargeWeight())));
        logremark.append(StringUtils.isBlank(planChargeWeight) ? "" : "计费吨：" + planChargeWeight);


        String containerApply = this.getStr(order.getContainerApply() ? "是" : "否", bean.getContainerApply() ? "是" : "否");
        logremark.append(StringUtils.isBlank(containerApply) ? "" : "放箱：" + containerApply);
        String containerPickupService = this.getStr(order.getContainerPickupService() ? "是" : "否", bean.getContainerPickupService() ? "是" : "否");
        logremark.append(StringUtils.isBlank(containerPickupService) ? "" : "提柜：" + containerPickupService);
        String containerLoadService = this.getStr(order.getContainerLoadService() ? "是" : "否", bean.getContainerLoadService() ? "是" : "否");
        logremark.append(StringUtils.isBlank(containerLoadService) ? "" : "装箱：" + containerLoadService);
        String customsClearanceService = this.getStr(order.getCustomsClearanceService() ? "是" : "否", bean.getCustomsClearanceService() ? "是" : "否");
        logremark.append(StringUtils.isBlank(customsClearanceService) ? "" : "报关：" + customsClearanceService);
        String arrivalCustomsClearanceService = this.getStr(order.getArrivalCustomsClearanceService() ? "是" : "否", bean.getArrivalCustomsClearanceService() ? "是" : "否");
        logremark.append(StringUtils.isBlank(arrivalCustomsClearanceService) ? "" : "清关：" + arrivalCustomsClearanceService);
        String deliveryService = this.getStr(order.getDeliveryService() ? "是" : "否", bean.getDeliveryService() ? "是" : "否");
        logremark.append(StringUtils.isBlank(deliveryService) ? "" : "派送：" + deliveryService);

        return logremark.toString();
    }

    public static String fmtMicrometer3(String text) {
        DecimalFormat df = null;
        df = new DecimalFormat("#####0.000");
        double number = 0.0;
        try {
            number = Double.parseDouble(text);
            return df.format(number);
        } catch (Exception e) {
            number = 0.0;
            return "";
        }
    }

    private String getStr(String str1, String str2) {
        String str = "";
        if (StringUtils.isBlank(str1) || "null".equals(str1)) {
            str1 = "空";
        }
        if (StringUtils.isBlank(str2) || "null".equals(str2)) {
            str2 = "空";
        }
        if (!str1.equals(str2)) {
            str = str1 + " -> " + str2;
        }
        return str + "  ";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void exportExcelListTe(TcOrder order) {
        LambdaQueryWrapper<TcOrder> wrapper = Wrappers.<TcOrder>lambdaQuery();

//        String bs = order.getBusinessScope();
//        if (StrUtil.isNotBlank(order.getCoopName())) {
//            List<Integer> coopIds = remoteCoopService.listByCoopName(order.getCoopName()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
//            wrapper.in(TcOrder::getCoopId, coopIds);
//        }
//        if (StrUtil.isNotBlank(order.getCustomerNumber())) {
//            wrapper.like(TcOrder::getCustomerNumber, order.getCustomerNumber());
//        }
//        if (StrUtil.isNotBlank(order.getContainerMethod())) {
//            wrapper.eq(TcOrder::getContainerMethod, order.getContainerMethod());
//        }
//
//        if (order.getExpectDepartureStart() != null) {
//            if("TE".equals(bs)){
//                wrapper.ge(TcOrder::getExpectDeparture, order.getExpectDepartureStart());
//            }else{
//                wrapper.ge(TcOrder::getExpectArrival, order.getExpectDepartureStart());
//            }
//        }
//        if (order.getExpectDepartureEnd() != null) {
//            if("TE".equals(bs)){
//                wrapper.le(TcOrder::getExpectDeparture, order.getExpectDepartureEnd());
//            }else{
//                wrapper.le(TcOrder::getExpectArrival, order.getExpectDepartureEnd());
//            }
//        }
//
//        if (order.getCreatTimeStart() != null) {
//            wrapper.ge(TcOrder::getCreateTime, order.getCreatTimeStart());
//        }
//        if (order.getCreatTimeEnd() != null) {
//            wrapper.le(TcOrder::getCreateTime, LocalDateTime.of(order.getCreatTimeEnd(), LocalTime.parse("23:59:59")));
//        }
//        wrapper.ne(TcOrder::getOrderStatus, "强制关闭");
//        if ("未锁账".equals(order.getOrderStatus())) {
//            wrapper.ne(TcOrder::getOrderStatus, "财务锁账");
//        }
//        if ("已锁账".equals(order.getOrderStatus())) {
//            wrapper.eq(TcOrder::getOrderStatus, "财务锁账");
//        }
//        if (StrUtil.isNotBlank(order.getRwbNumber())) {
//            wrapper.like(TcOrder::getRwbNumber, order.getRwbNumber());
//        }
//        if (StrUtil.isNotBlank(order.getOrderCode())) {
//            wrapper.like(TcOrder::getOrderCode, order.getOrderCode());
//        }
//        if (StrUtil.isNotBlank(order.getDepartureStation())) {
//            wrapper.like(TcOrder::getDepartureStation, order.getDepartureStation());
//        }
//        if (StrUtil.isNotBlank(order.getArrivalStation())) {
//            wrapper.like(TcOrder::getArrivalStation, order.getArrivalStation());
//        }
//        if (StrUtil.isNotBlank(order.getProductType())) {
//            wrapper.eq(TcOrder::getProductType, order.getProductType());
//        }
//        if (order.getServicerId() != null) {
//            wrapper.eq(TcOrder::getServicerId, order.getServicerId());
//        }
//        if (order.getSalesId() != null) {
//            wrapper.eq(TcOrder::getSalesId, order.getSalesId());
//        }
//        if (order.getCreatorId() != null) {
//            wrapper.eq(TcOrder::getCreatorId, order.getCreatorId());
//        }
//        if (StrUtil.isNotBlank(order.getBookingAgentName())) {
//            LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
//            if("TE".equals(bs)){
//                afVPrmCoopWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).like(AfVPrmCoop::getCoopName, order.getBookingAgentName()).eq(AfVPrmCoop::getBusinessScopeTE, "TE").in(AfVPrmCoop::getCoopType, "互为代理", "海外代理");
//            }else{
//                afVPrmCoopWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).like(AfVPrmCoop::getCoopName, order.getBookingAgentName()).eq(AfVPrmCoop::getBusinessScopeTI, "TI").in(AfVPrmCoop::getCoopType, "互为代理", "海外代理");
//            }
//            List<Integer> bookAgentIds = afVPrmCoopService.list(afVPrmCoopWrapper).stream().map(AfVPrmCoop::getCoopId).collect(Collectors.toList());
//            wrapper.in(TcOrder::getBookingAgentId, bookAgentIds);
//        }
//        if (order.getIncomeRecorded() != null && order.getIncomeRecorded()) {
//            wrapper.eq(TcOrder::getIncomeRecorded, order.getIncomeRecorded());
//        }
//        if (order.getIncomeRecorded() != null && !order.getIncomeRecorded()) {
//            wrapper.and(i -> i.eq(TcOrder::getIncomeRecorded, order.getIncomeRecorded()).or(j -> j.isNull(TcOrder::getIncomeRecorded)));
//        }
//        if (order.getCostRecorded() != null && order.getCostRecorded()) {
//            wrapper.eq(TcOrder::getCostRecorded, order.getCostRecorded());
//        }
//        if (order.getCostRecorded() != null && !order.getCostRecorded()) {
//            wrapper.and(i -> i.eq(TcOrder::getCostRecorded, order.getCostRecorded()).or(j -> j.isNull(TcOrder::getCostRecorded)));
//        }
//        if (order.getOrderPermission() == 1) {
//            wrapper.and(i -> i.eq(TcOrder::getCreatorId, order.getCurrentUserId()).or(j -> j.eq(TcOrder::getSalesId, order.getCurrentUserId())).or(k -> k.eq(TcOrder::getServicerId, order.getCurrentUserId())));
//        }
//        if (order.getOrderPermission() == 2) {
//            List<Integer> WorkgroupIds = baseMapper.getWorkgroupIds(order.getCurrentUserId());
//            wrapper.and(i -> i.eq(TcOrder::getCreatorId, order.getCurrentUserId()).or(j -> j.eq(TcOrder::getSalesId, order.getCurrentUserId())).or(k -> k.eq(TcOrder::getServicerId, order.getCurrentUserId())).or(m -> m.in(TcOrder::getWorkgroupId, WorkgroupIds)));
//        }
//        if (StrUtil.isNotBlank(order.getBookingNumber())) {
//            wrapper.like(TcOrder::getBookingNumber, order.getBookingNumber());
//        }
//        wrapper.eq(TcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TcOrder::getBusinessScope, bs).orderByDesc(TcOrder::getOrderId);
        List<TcOrder> list = new ArrayList<>();
        if (getWrapper(order, wrapper)) list = null;
        list = baseMapper.selectList(wrapper);
        TcOrder total = this.getTETotal(order);
        if (list != null && list.size() > 0) {
            list.stream().forEach(tcOrder -> {
                if (tcOrder.getBookingAgentId() != null) {
                    LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
                    afVPrmCoopWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfVPrmCoop::getCoopId, tcOrder.getBookingAgentId());
                    AfVPrmCoop bookingAgent = afVPrmCoopService.getOne(afVPrmCoopWrapper);
                    if (bookingAgent != null) {
                        tcOrder.setBookingAgentName(bookingAgent.getCoopName());
                    }
                }
                //产品
                if (tcOrder.getRailwayProductId() != null) {
                    TcProduct product = tcProductService.getById(tcOrder.getRailwayProductId());
                    if (product != null) {
                        tcOrder.setProductName(product.getProductName());
                    }
                }
                StringBuffer buffer = new StringBuffer();
                LambdaQueryWrapper<TcOrderContainerDetails> orderContainerDetailsLambdaQueryWrapper = Wrappers.<TcOrderContainerDetails>lambdaQuery();
                orderContainerDetailsLambdaQueryWrapper.eq(TcOrderContainerDetails::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TcOrderContainerDetails::getOrderId, tcOrder.getOrderId());
                orderContainerDetailsService.list(orderContainerDetailsLambdaQueryWrapper).stream().forEach(orderContainerDetails -> {
                    if (buffer.length() == 0) {
                        if (StrUtil.isNotBlank(orderContainerDetails.getContainerNumber()) && StrUtil.isNotBlank(orderContainerDetails.getContainerSealNo())) {
                            buffer.append(orderContainerDetails.getContainerNumber()).append(" / ").append(orderContainerDetails.getContainerSealNo());
                        } else if (StrUtil.isNotBlank(orderContainerDetails.getContainerNumber())) {
                            buffer.append(orderContainerDetails.getContainerNumber());
                        } else if (StrUtil.isNotBlank(orderContainerDetails.getContainerSealNo())) {
                            buffer.append(orderContainerDetails.getContainerSealNo());
                        }
                    } else {
                        if (StrUtil.isNotBlank(orderContainerDetails.getContainerNumber()) && StrUtil.isNotBlank(orderContainerDetails.getContainerSealNo())) {
                            buffer.append("\n").append(orderContainerDetails.getContainerNumber()).append(" / ").append(orderContainerDetails.getContainerSealNo());
                        } else if (StrUtil.isNotBlank(orderContainerDetails.getContainerNumber())) {
                            buffer.append("\n").append(orderContainerDetails.getContainerNumber());
                        } else if (StrUtil.isNotBlank(orderContainerDetails.getContainerSealNo())) {
                            buffer.append("\n").append(orderContainerDetails.getContainerSealNo());
                        }
                    }
                });
                tcOrder.setContainerNumberAndContainerSealNo(buffer.toString());
            });
            total.setOrderStatus("");
            total.setOrderCode("合计");
            list.add(total);
        }
        List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
        //自定义字段
        if (!StringUtils.isEmpty(order.getColumnStrs())) {
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(order.getColumnStrs());
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
                for (TcOrder excel : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        if ("incomeRecorded".equals(colunmStrs[j])) {
                            //收 完成状态
                            if ((excel.getIncomeRecorded() != null && excel.getIncomeRecorded()) || (StrUtil.isNotBlank(excel.getIncomeStatus()) && !"未录收入".equals(excel.getIncomeStatus()))) {
                                map.put("incomeRecorded", "√");
                            } else {
                                map.put("incomeRecorded", "");
                            }
                        } else if ("costRecorded".equals(colunmStrs[j])) {
                            if ((excel.getCostRecorded() != null && excel.getCostRecorded()) || (StrUtil.isNotBlank(excel.getCostStatus()) && !"未录成本".equals(excel.getCostStatus()))) {
                                map.put("costRecorded", "√");
                            } else {
                                map.put("costRecorded", "");
                            }
                        } else if ("containerApply".equals(colunmStrs[j])) {
                            if (excel.getContainerApply() != null && excel.getContainerApply()) {
                                map.put("containerApply", "√");
                            } else {
                                map.put("containerApply", "");
                            }
                        } else if ("containerPickupService".equals(colunmStrs[j])) {
                            if (excel.getContainerPickupService() != null && excel.getContainerPickupService()) {
                                map.put("containerPickupService", "√");
                            } else {
                                map.put("containerPickupService", "");
                            }
                        } else if ("containerLoadService".equals(colunmStrs[j])) {
                            if (excel.getContainerLoadService() != null && excel.getContainerLoadService()) {
                                map.put("containerLoadService", "√");
                            } else {
                                map.put("containerLoadService", "");
                            }
                        } else if ("customsClearanceService".equals(colunmStrs[j])) {
                            if (excel.getCustomsClearanceService() != null && excel.getCustomsClearanceService()) {
                                map.put("customsClearanceService", "√");
                            } else {
                                map.put("customsClearanceService", "");
                            }
                        } else if ("arrivalCustomsClearanceService".equals(colunmStrs[j])) {
                            if (excel.getArrivalCustomsClearanceService() != null && excel.getArrivalCustomsClearanceService()) {
                                map.put("arrivalCustomsClearanceService", "√");
                            } else {
                                map.put("arrivalCustomsClearanceService", "");
                            }
                        } else if ("deliveryService".equals(colunmStrs[j])) {
                            if (excel.getDeliveryService() != null && excel.getDeliveryService()) {
                                map.put("deliveryService", "√");
                            } else {
                                map.put("deliveryService", "");
                            }
                        } else if ("servicerName".equals(colunmStrs[j])) {
                            if (!StringUtils.isEmpty(excel.getServicerName())) {
                                map.put("servicerName", excel.getServicerName().split(" ")[0]);
                            } else {
                                map.put("servicerName", "");
                            }
                        } else if ("salesName".equals(colunmStrs[j])) {
                            if (!StringUtils.isEmpty(excel.getSalesName())) {
                                map.put("salesName", excel.getSalesName().split(" ")[0]);
                            } else {
                                map.put("salesName", "");
                            }
                        } else if ("creatorName".equals(colunmStrs[j])) {
                            if (!StringUtils.isEmpty(excel.getCreatorName())) {
                                map.put("creatorName", excel.getCreatorName().split(" ")[0]);
                            } else {
                                map.put("creatorName", "");
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
    public List<OrderForVL> getTCOrderListForVL(OrderForVL orderForVL) {
        LambdaQueryWrapper<TcOrder> wrapper = Wrappers.<TcOrder>lambdaQuery();
        if (StrUtil.isNotBlank(orderForVL.getOrderCode())) {
            wrapper.like(TcOrder::getOrderCode, orderForVL.getOrderCode());
        }

        if (StrUtil.isNotBlank(orderForVL.getAwbNumber())) {
            wrapper.like(TcOrder::getRwbNumber, orderForVL.getAwbNumber());
        }

        if (StrUtil.isNotBlank(orderForVL.getCustomerNumber())) {
            wrapper.like(TcOrder::getCustomerNumber, orderForVL.getCustomerNumber());
        }

        if (orderForVL.getBusinessScope().equals("TE") && orderForVL.getFlightDateStart() != null) {
            wrapper.ge(TcOrder::getExpectDeparture, orderForVL.getFlightDateStart());
        }
        if (orderForVL.getBusinessScope().equals("TE") && orderForVL.getFlightDateEnd() != null) {
            wrapper.le(TcOrder::getExpectDeparture, orderForVL.getFlightDateEnd());
        }
        if (orderForVL.getBusinessScope().equals("TI") && orderForVL.getFlightDateStart() != null) {
            wrapper.ge(TcOrder::getExpectArrival, orderForVL.getFlightDateStart());
        }
        if (orderForVL.getBusinessScope().equals("TI") && orderForVL.getFlightDateEnd() != null) {
            wrapper.le(TcOrder::getExpectArrival, orderForVL.getFlightDateEnd());
        }

        if (StrUtil.isNotBlank(orderForVL.getNoOrderIds())) {
            wrapper.notIn(TcOrder::getOrderId, orderForVL.getNoOrderIds().split(","));
        }
        wrapper.eq(TcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TcOrder::getBusinessScope, orderForVL.getBusinessScope()).ne(TcOrder::getContainerMethod, "整箱").ne(TcOrder::getCostRecorded, true).notIn(TcOrder::getOrderStatus, "强制关闭", "财务锁账");
        if (orderForVL.getBusinessScope().equals("TE")) {
            wrapper.orderByAsc(TcOrder::getExpectDeparture, TcOrder::getRwbNumber);
        } else {
            wrapper.orderByAsc(TcOrder::getExpectArrival, TcOrder::getRwbNumber);
        }
        return list(wrapper).stream().map(tcOrder -> {
            OrderForVL order = new OrderForVL();
            BeanUtils.copyProperties(tcOrder, order);
            order.setAwbNumber(tcOrder.getRwbNumber());
            if (order.getPlanChargeWeight() != null) {
                order.setPlanChargeWeight(order.getPlanChargeWeight().multiply(BigDecimal.valueOf(1000)));
            }
            if (orderForVL.getBusinessScope().equals("TI")) {
                order.setFlightDate(tcOrder.getExpectArrival());
            } else {
                order.setFlightDate(tcOrder.getExpectDeparture());
            }
            return order;
        }).collect(Collectors.toList());
    }

}

