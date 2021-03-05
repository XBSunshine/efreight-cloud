package com.efreight.sc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.core.feign.RemoteCoopService;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.core.utils.JxlsUtils;
import com.efreight.common.remoteVo.OrderForVL;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.common.security.vo.CoopVo;
import com.efreight.sc.dao.OrderFilesMapper;
import com.efreight.sc.dao.OrderMapper;
import com.efreight.sc.entity.*;
import com.efreight.sc.entity.view.AfVPrmCoop;
import com.efreight.sc.entity.view.OrderTrackVO;
import com.efreight.sc.service.*;
import com.efreight.sc.utils.FieldValUtils;
import com.efreight.sc.utils.LoginUtils;
import com.efreight.sc.utils.PDFUtils;
import com.itextpdf.text.DocumentException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.jxls.util.Util;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * CS 订单管理 SI订单 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-02
 */
@Service
@AllArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final RemoteCoopService remoteCoopService;

    private final OrderShipperConsigneeService orderShipperConsigneeService;

    private final OrderShipInfoService orderShipInfoService;

    private final LogService logService;

    private final IncomeService incomeService;

    private final CostService costService;

    private final AfVPrmCoopService afVPrmCoopService;

    private final ShipCompanyService shipCompanyService;

    private final OrderContainerDetailsService orderContainerDetailsService;

    private final PortMaintenanceService portMaintenanceService;

    private final OrderFilesMapper orderFilesMapper;

    @Override
    public IPage getSIPage(Page page, Order order) {
        order.setCurrentUserId(SecurityUtils.getUser().getId());
        LambdaQueryWrapper<Order> wrapper = Wrappers.<Order>lambdaQuery();
        if (StrUtil.isNotBlank(order.getCustomerName())) {
            List<Integer> coopIds = remoteCoopService.listByCoopName(order.getCustomerName()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
            if (coopIds.size() == 0) {
                page.setRecords(new ArrayList());
                page.setTotal(0);
                return page;
            }
            wrapper.in(Order::getCoopId, coopIds);
        }

        if (StrUtil.isNotBlank(order.getCustomerNumber())) {
            wrapper.like(Order::getCustomerNumber, "%" + order.getCustomerNumber() + "%");
        }

        if (order.getExpectArrivalStart() != null) {
            wrapper.ge(Order::getExpectArrival, order.getExpectArrivalStart());
        }
        if (order.getExpectArrivalEnd() != null) {
            wrapper.le(Order::getExpectArrival, order.getExpectArrivalEnd());
        }
        if (order.getCreateTimeBegin() != null) {
            wrapper.ge(Order::getCreateTime, order.getCreateTimeBegin());
        }
        if (order.getCreateTimeEnd() != null) {
            wrapper.le(Order::getCreateTime, order.getCreateTimeEnd());
        }
        //过滤强制关闭订单
        wrapper.ne(Order::getOrderStatus, "强制关闭");
        if ("未锁账".equals(order.getOrderStatus())) {
            wrapper.ne(Order::getOrderStatus, "财务锁账");
        }
        if ("已锁账".equals(order.getOrderStatus())) {
            wrapper.eq(Order::getOrderStatus, "财务锁账");
        }
        if (StrUtil.isNotBlank(order.getMblNumber())) {
            wrapper.like(Order::getMblNumber, "%" + order.getMblNumber() + "%");
        }
        if (StrUtil.isNotBlank(order.getHblNumber())) {
            wrapper.like(Order::getHblNumber, "%" + order.getHblNumber() + "%");
        }
        if (StrUtil.isNotBlank(order.getOrderCode())) {
            wrapper.like(Order::getOrderCode, "%" + order.getOrderCode() + "%");
        }
        if (StrUtil.isNotBlank(order.getServicerName())) {
            wrapper.like(Order::getServicerName, "%" + order.getServicerName() + "%");
        }
        if (StrUtil.isNotBlank(order.getSalesName())) {
            wrapper.like(Order::getSalesName, "%" + order.getSalesName() + "%");
        }
        if (StrUtil.isNotBlank(order.getCreatorName())) {
            wrapper.like(Order::getCreatorName, "%" + order.getCreatorName() + "%");
        }
//        if (StrUtil.isNotBlank(order.getArrivalStation())) {
//            wrapper.eq(Order::getArrivalStation, order.getArrivalStation());
//        }
//        if (StrUtil.isNotBlank(order.getDepartureStation())) {
//            wrapper.eq(Order::getDepartureStation, order.getDepartureStation());
//        }
        if (StrUtil.isNotBlank(order.getContainerMethod())) {
            wrapper.eq(Order::getContainerMethod, order.getContainerMethod());
        }

        if (order.getIncomeRecorded() != null && order.getIncomeRecorded()) {
            wrapper.eq(Order::getIncomeRecorded, order.getIncomeRecorded());
        }
        if (order.getIncomeRecorded() != null && !order.getIncomeRecorded()) {
            wrapper.and(i -> i.eq(Order::getIncomeRecorded, order.getIncomeRecorded()).or(j -> j.isNull(Order::getIncomeRecorded)));
        }
        if (order.getCostRecorded() != null && order.getCostRecorded()) {
            wrapper.eq(Order::getCostRecorded, order.getCostRecorded());
        }
        if (order.getCostRecorded() != null && !order.getCostRecorded()) {
            wrapper.and(i -> i.eq(Order::getCostRecorded, order.getCostRecorded()).or(j -> j.isNull(Order::getCostRecorded)));
        }
        if (order.getOrderPermission() != null && order.getOrderPermission() == 1) {
            wrapper.and(i -> i.eq(Order::getCreatorId, order.getCurrentUserId()).or(j -> j.eq(Order::getSalesId, order.getCurrentUserId())).or(k -> k.eq(Order::getServicerId, order.getCurrentUserId())));
        }
        if (order.getOrderPermission() != null && order.getOrderPermission() == 2) {
            List<Integer> WorkgroupIds = baseMapper.getWorkgroupIds(order.getCurrentUserId());
            wrapper.and(i -> i.eq(Order::getCreatorId, order.getCurrentUserId()).or(j -> j.eq(Order::getSalesId, order.getCurrentUserId())).or(k -> k.eq(Order::getServicerId, order.getCurrentUserId())).or(m -> m.in(Order::getWorkgroupId, WorkgroupIds)));
        }
        wrapper.eq(Order::getOrgId, SecurityUtils.getUser().getOrgId()).eq(Order::getBusinessScope, "SI").orderByDesc(Order::getOrderId);
//        IPage<Order> iPage = page(page, wrapper);
        IPage<Order> iPage = baseMapper.getPageForSE(page, wrapper, order);
        iPage.getRecords().stream().forEach(scOrder -> {
            CoopVo coopVo = remoteCoopService.viewCoop(scOrder.getCoopId().toString()).getData();
            if (coopVo != null) {
                scOrder.setCustomerName(coopVo.getCoop_name());
            }
            LambdaQueryWrapper<PortMaintenance> departureStationWrapper = Wrappers.<PortMaintenance>lambdaQuery();
            departureStationWrapper.eq(PortMaintenance::getPortCode, scOrder.getDepartureStation());
            PortMaintenance departure = portMaintenanceService.getOne(departureStationWrapper);
            LambdaQueryWrapper<PortMaintenance> arrivalStationWrapper = Wrappers.<PortMaintenance>lambdaQuery();
            arrivalStationWrapper.eq(PortMaintenance::getPortCode, scOrder.getArrivalStation());
            PortMaintenance arrival = portMaintenanceService.getOne(arrivalStationWrapper);
            if (departure != null) {
                scOrder.setDepartureStation(departure.getPortNameEn());
            }
            if (arrival != null) {
                scOrder.setArrivalStation(arrival.getPortNameEn());
            }
            //设置收入完成和成本完成（排序使用）
            if (scOrder.getIncomeRecorded() == true) {
                scOrder.setIncomeRecordedForSort(2);
            } else if (StringUtils.isNotBlank(scOrder.getIncomeStatus()) && !"未录收入".equals(scOrder.getIncomeStatus())) {
                scOrder.setIncomeRecordedForSort(1);
            } else {
                scOrder.setIncomeRecordedForSort(0);
            }
            if (scOrder.getCostRecorded() == true) {
                scOrder.setCostRecordedForSort(2);
            } else if (StringUtils.isNotBlank(scOrder.getCostStatus()) && !"未录成本".equals(scOrder.getCostStatus())) {
                scOrder.setCostRecordedForSort(1);
            } else {
                scOrder.setCostRecordedForSort(0);
            }
        });
        return iPage;
    }

    @Override
    public void exportExcelListSi(Order order) {
        List<SiOrderExcel> list = new ArrayList<SiOrderExcel>();
        order.setCurrentUserId(SecurityUtils.getUser().getId());
        LambdaQueryWrapper<Order> wrapper = Wrappers.<Order>lambdaQuery();
        if (StrUtil.isNotBlank(order.getCustomerName())) {
            List<Integer> coopIds = remoteCoopService.listByCoopName(order.getCustomerName()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
            /*if (coopIds.size() == 0) {
                page.setRecords(new ArrayList());
                page.setTotal(0);
                return page;
            }*/
            wrapper.in(Order::getCoopId, coopIds);
        }

        if (StrUtil.isNotBlank(order.getCustomerNumber())) {
            wrapper.like(Order::getCustomerNumber, "%" + order.getCustomerNumber() + "%");
        }

        if (order.getExpectArrivalStart() != null) {
            wrapper.ge(Order::getExpectArrival, order.getExpectArrivalStart());
        }
        if (order.getExpectArrivalEnd() != null) {
            wrapper.le(Order::getExpectArrival, order.getExpectArrivalEnd());
        }
        if (order.getCreateTimeBegin() != null) {
            wrapper.ge(Order::getCreateTime, order.getCreateTimeBegin());
        }
        if (order.getCreateTimeEnd() != null) {
            wrapper.le(Order::getCreateTime, order.getCreateTimeEnd());
        }
        wrapper.ne(Order::getOrderStatus, "强制关闭");
        if ("未锁账".equals(order.getOrderStatus())) {
            wrapper.ne(Order::getOrderStatus, "财务锁账");
        }
        if ("已锁账".equals(order.getOrderStatus())) {
            wrapper.eq(Order::getOrderStatus, "财务锁账");
        }
        if (StrUtil.isNotBlank(order.getMblNumber())) {
            wrapper.like(Order::getMblNumber, "%" + order.getMblNumber() + "%");
        }
        if (StrUtil.isNotBlank(order.getHblNumber())) {
            wrapper.like(Order::getHblNumber, "%" + order.getHblNumber() + "%");
        }
        if (StrUtil.isNotBlank(order.getOrderCode())) {
            wrapper.like(Order::getOrderCode, "%" + order.getOrderCode() + "%");
        }
        if (StrUtil.isNotBlank(order.getServicerName())) {
            wrapper.like(Order::getServicerName, "%" + order.getServicerName() + "%");
        }
        if (StrUtil.isNotBlank(order.getSalesName())) {
            wrapper.like(Order::getSalesName, "%" + order.getSalesName() + "%");
        }
        if (StrUtil.isNotBlank(order.getCreatorName())) {
            wrapper.like(Order::getCreatorName, "%" + order.getCreatorName() + "%");
        }
//        if (StrUtil.isNotBlank(order.getArrivalStation())) {
//            wrapper.eq(Order::getArrivalStation, order.getArrivalStation());
//        }
//        if (StrUtil.isNotBlank(order.getDepartureStation())) {
//            wrapper.eq(Order::getDepartureStation, order.getDepartureStation());
//        }
        if (StrUtil.isNotBlank(order.getContainerMethod())) {
            wrapper.eq(Order::getContainerMethod, order.getContainerMethod());
        }

        if (order.getIncomeRecorded() != null && order.getIncomeRecorded()) {
            wrapper.eq(Order::getIncomeRecorded, order.getIncomeRecorded());
        }
        if (order.getIncomeRecorded() != null && !order.getIncomeRecorded()) {
            wrapper.and(i -> i.eq(Order::getIncomeRecorded, order.getIncomeRecorded()).or(j -> j.isNull(Order::getIncomeRecorded)));
        }
        if (order.getCostRecorded() != null && order.getCostRecorded()) {
            wrapper.eq(Order::getCostRecorded, order.getCostRecorded());
        }
        if (order.getCostRecorded() != null && !order.getCostRecorded()) {
            wrapper.and(i -> i.eq(Order::getCostRecorded, order.getCostRecorded()).or(j -> j.isNull(Order::getCostRecorded)));
        }
        if (order.getOrderPermission() == 1) {
            wrapper.and(i -> i.eq(Order::getCreatorId, order.getCurrentUserId()).or(j -> j.eq(Order::getSalesId, order.getCurrentUserId())).or(k -> k.eq(Order::getServicerId, order.getCurrentUserId())));
        }
        if (order.getOrderPermission() == 2) {
            List<Integer> WorkgroupIds = baseMapper.getWorkgroupIds(order.getCurrentUserId());
            wrapper.and(i -> i.eq(Order::getCreatorId, order.getCurrentUserId()).or(j -> j.eq(Order::getSalesId, order.getCurrentUserId())).or(k -> k.eq(Order::getServicerId, order.getCurrentUserId())).or(m -> m.in(Order::getWorkgroupId, WorkgroupIds)));
        }
        wrapper.eq(Order::getOrgId, SecurityUtils.getUser().getOrgId()).eq(Order::getBusinessScope, "SI").orderByDesc(Order::getOrderId);
//        List<Order> orderList = baseMapper.selectList(wrapper);
        List<Order> orderList = baseMapper.getPageForSE2(wrapper, order);
        Order total = new Order();
        orderList.stream().forEach(scOrder -> {
            CoopVo coopVo = remoteCoopService.viewCoop(scOrder.getCoopId().toString()).getData();
            if (coopVo != null) {
                scOrder.setCustomerName(coopVo.getCoop_name());
            }
            LambdaQueryWrapper<PortMaintenance> departureStationWrapper = Wrappers.<PortMaintenance>lambdaQuery();
            departureStationWrapper.eq(PortMaintenance::getPortCode, scOrder.getDepartureStation());
            PortMaintenance departure = portMaintenanceService.getOne(departureStationWrapper);
            LambdaQueryWrapper<PortMaintenance> arrivalStationWrapper = Wrappers.<PortMaintenance>lambdaQuery();
            arrivalStationWrapper.eq(PortMaintenance::getPortCode, scOrder.getArrivalStation());
            PortMaintenance arrival = portMaintenanceService.getOne(arrivalStationWrapper);
            if (departure != null) {
                scOrder.setDepartureStation(departure.getPortNameEn());
            }
            if (arrival != null) {
                scOrder.setArrivalStation(arrival.getPortNameEn());
            }

            //计算合计
            //统计标箱数量
            if (total.getContainerNumber() == null) {
                total.setContainerNumber(scOrder.getContainerNumber() == null ? 0 : scOrder.getContainerNumber());
            } else {
                total.setContainerNumber(total.getContainerNumber() + (scOrder.getContainerNumber() == null ? 0 : scOrder.getContainerNumber()));
            }
            //统计件数
            if (total.getPlanPieces() == null) {
                total.setPlanPieces(scOrder.getPlanPieces() == null ? 0 : scOrder.getPlanPieces());
            } else {
                total.setPlanPieces(total.getPlanPieces() + (scOrder.getPlanPieces() == null ? 0 : scOrder.getPlanPieces()));
            }
            //统计毛重
            if (total.getPlanWeight() == null) {
                total.setPlanWeight(scOrder.getPlanWeight() == null ? BigDecimal.ZERO : scOrder.getPlanWeight());
            } else {
                total.setPlanWeight(total.getPlanWeight().add(scOrder.getPlanWeight() == null ? BigDecimal.ZERO : scOrder.getPlanWeight()));
            }
            //统计体积
            if (total.getPlanVolume() == null) {
                total.setPlanVolume(scOrder.getPlanVolume() == null ? BigDecimal.ZERO : scOrder.getPlanVolume());
            } else {
                total.setPlanVolume(total.getPlanVolume().add(scOrder.getPlanVolume() == null ? BigDecimal.ZERO : scOrder.getPlanVolume()));
            }
            //统计计重
            if (total.getPlanChargeWeight() == null) {
                total.setPlanChargeWeight(scOrder.getPlanChargeWeight() == null ? BigDecimal.ZERO : scOrder.getPlanChargeWeight());
            } else {
                total.setPlanChargeWeight(total.getPlanChargeWeight().add(scOrder.getPlanChargeWeight() == null ? BigDecimal.ZERO : scOrder.getPlanChargeWeight()));
            }

            SiOrderExcel oe = new SiOrderExcel();
            oe.setOrderCode(scOrder.getOrderCode());
            oe.setMblNumber(scOrder.getMblNumber());
            oe.setCustomerNumber(scOrder.getCustomerNumber());
            oe.setOrderStatus(scOrder.getOrderStatus());
            if ((scOrder.getIncomeRecorded() != null && scOrder.getIncomeRecorded()) || (StrUtil.isNotBlank(scOrder.getIncomeStatus()) && !"未录收入".equals(scOrder.getIncomeStatus()))) {
                oe.setIncomeRecorded("√");
            } else {
                oe.setIncomeRecorded("");
            }
            if ((scOrder.getCostRecorded() != null && scOrder.getCostRecorded()) || (StrUtil.isNotBlank(scOrder.getCostStatus()) && !"未录成本".equals(scOrder.getCostStatus()))) {
                oe.setCostRecorded("√");
            } else {
                oe.setCostRecorded("");
            }

            String expectArrival = "";
            if (scOrder.getExpectArrival() != null && !"".equals(scOrder.getExpectArrival())) {
                expectArrival = "" + scOrder.getExpectArrival();
            }
            oe.setExpectArrival(expectArrival);
            oe.setShipNameAndNumber(scOrder.getShipName() + " / " + scOrder.getShipVoyageNumber());
            oe.setDepartureStation(scOrder.getDepartureStation());
            oe.setArrivalStation(scOrder.getArrivalStation());
            oe.setContainerMethod(scOrder.getContainerMethod());
            oe.setContainerList(scOrder.getContainerList());
            if (scOrder.getContainerNumber() != null && !"".equals(scOrder.getContainerNumber())) {
                oe.setContainerNumber("" + scOrder.getContainerNumber());
            } else {
                oe.setContainerNumber("");
            }
            oe.setPlanPieces("" + scOrder.getPlanPieces());
            oe.setPlanWeight("" + scOrder.getPlanWeight());
            oe.setPlanVolume("" + scOrder.getPlanVolume());
            oe.setPlanChargeWeight("" + scOrder.getPlanChargeWeight());
            if (scOrder.getChangeOrderService()) {
                oe.setChangeOrderService("√");
            } else {
                oe.setChangeOrderService("");
            }
            if (scOrder.getWarehouseService()) {
                oe.setWarehouseService("√");
            } else {
                oe.setWarehouseService("");
            }
            if (scOrder.getCustomsClearanceService()) {
                oe.setCustomsClearanceService("√");
            } else {
                oe.setCustomsClearanceService("");
            }
            if (scOrder.getDeliveryService()) {
                oe.setDeliveryService("√");
            } else {
                oe.setDeliveryService("");
            }
            oe.setHblNumber(scOrder.getHblNumber());
            oe.setGoodsType(scOrder.getGoodsType());
            oe.setGoodsNameCn(scOrder.getGoodsNameCn());
            oe.setTransitClause(scOrder.getTransitClause());
            oe.setDamageRemark(scOrder.getDamageRemark());
            if (scOrder.getServicerName() != null && !"".equals(scOrder.getServicerName())) {
                oe.setServicerName(scOrder.getServicerName().split(" ")[0]);
            } else {
                oe.setServicerName("");
            }
            if (scOrder.getSalesName() != null && !"".equals(scOrder.getSalesName())) {
                oe.setSalesName(scOrder.getSalesName().split(" ")[0]);
            } else {
                oe.setSalesName("");
            }
            if (scOrder.getCreatorName() != null && !"".equals(scOrder.getCreatorName())) {
                oe.setCreatorName(scOrder.getCreatorName().split(" ")[0]);
            } else {
                oe.setCreatorName("");
            }
            oe.setOrderRemark(scOrder.getOrderRemark());
            list.add(oe);
        });

        SiOrderExcel oeTotal = new SiOrderExcel();
        oeTotal.setOrderCode("合计：");
        oeTotal.setContainerNumber("" + total.getContainerNumber());
        oeTotal.setPlanPieces("" + total.getPlanPieces());
        oeTotal.setPlanWeight("" + FormatUtils.formatWithQWF(total.getPlanWeight(), 3));
        oeTotal.setPlanVolume("" + FormatUtils.formatWithQWF(total.getPlanVolume(), 3));
        oeTotal.setPlanChargeWeight("" + FormatUtils.formatWithQWF(total.getPlanChargeWeight(), 3));
        list.add(oeTotal);
        //自定义字段
        if (!StringUtils.isEmpty(order.getColumnStrs())) {
            List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();

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
                for (SiOrderExcel excel : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        if ("shipName".equals(colunmStrs[j])) {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName("shipNameAndNumber", excel));
                        } else {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], excel));
                        }
                    }
                    listExcel.add(map);
                }
            }
            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(LoginUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");
        } else {
            ExportExcel<SiOrderExcel> ex = new ExportExcel<SiOrderExcel>();
            String[] headers = {"订单号", "客户单号", "主提单号", "操作节点", "收入完成", "成本完成", "到港日期", "起运港", "目的港", "装箱方式", "集装箱量", "标箱数量(TEU)", "件数", "毛重(KG)", "体积(CBM)", "计费吨(TON)", "换单服务",
                    "库内操作", "报关服务", "派送服务", "分提单号", "船名/船次", "运输条款", "货物类型", "中文品名", "破损记录",
                    "责任客服", "责任销售", "责任操作", "订单备注"};
            ex.exportExcel(LoginUtils.getResponse(), "导出EXCEL", headers, list, "Export");
        }
    }

    @Override
    public OrderTrackVO getOrderTrack(String orderUUID) {
        Assert.hasLength(orderUUID, "非法订单虚拟ID");

        LambdaQueryWrapper<Order> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Order::getOrderUuid, orderUUID);
        Order order = baseMapper.selectOne(wrapper);
        if (null == order) {
            throw new RuntimeException("没查到该订单信息");
        }
        //加载附件信息
        LambdaQueryWrapper<OrderFiles> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(OrderFiles::getOrderId, order.getOrderId());
        lambdaQueryWrapper.eq(OrderFiles::getIsDisplay, 1);
        lambdaQueryWrapper.orderByDesc(OrderFiles::getCreateTime);
        List<OrderFiles> attachments = orderFilesMapper.selectList(lambdaQueryWrapper);

        OrderTrackVO orderTrack = new OrderTrackVO();
        orderTrack.addOrder(order);
        orderTrack.setAttachments(attachments);
        orderTrack.setRouteTracks(new ArrayList());

        //修改港口信息
        orderTrack.setDepartureStation(getPortMaintenanceEnName(order.getDepartureStation()));
        orderTrack.setArrivalStation(getPortMaintenanceEnName(order.getArrivalStation()));

        return orderTrack;
    }

    private String getPortMaintenanceEnName(String code) {
        PortMaintenance portMaintenance = portMaintenanceService.getByCode(code);
        return null == portMaintenance ? null : portMaintenance.getPortNameEn();
    }

    @Override
    public IPage getSEPage(Page page, Order order) {
        order.setCurrentUserId(SecurityUtils.getUser().getId());
        LambdaQueryWrapper<Order> wrapper = Wrappers.<Order>lambdaQuery();
        if (StrUtil.isNotBlank(order.getCustomerName())) {
            List<Integer> coopIds = remoteCoopService.listByCoopName(order.getCustomerName()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
            if (coopIds.size() == 0) {
                page.setRecords(new ArrayList());
                page.setTotal(0);
                return page;
            }
            wrapper.in(Order::getCoopId, coopIds);
        }

        if (StrUtil.isNotBlank(order.getBillingType())) {
            wrapper.eq(Order::getBillingType, order.getBillingType());
        }

        if (StrUtil.isNotBlank(order.getCustomerNumber())) {
            wrapper.like(Order::getCustomerNumber, "%" + order.getCustomerNumber() + "%");
        }

        if (order.getExpectDepartureStart() != null) {
//        	if (order.getExpectDeparture() != null) {
            wrapper.ge(Order::getExpectDeparture, order.getExpectDepartureStart());
        }
        if (order.getExpectDepartureEnd() != null) {
//        	if (order.getExpectArrivalEnd() != null) {
            wrapper.le(Order::getExpectDeparture, order.getExpectDepartureEnd());
        }
        
        if (order.getExpectArrivalStart() != null) {
            wrapper.ge(Order::getExpectArrival, order.getExpectArrivalStart());
        }
        if (order.getExpectArrivalEnd() != null) {
            wrapper.le(Order::getExpectArrival, order.getExpectArrivalEnd());
        }
        
        if (order.getCreateTimeBegin() != null) {
            wrapper.ge(Order::getCreateTime, order.getCreateTimeBegin());
        }
        if (order.getCreateTimeEnd() != null) {
            wrapper.le(Order::getCreateTime, order.getCreateTimeEnd());
        }

        if (order.getDocumentOffDateStart() != null) {
            wrapper.ge(Order::getDocumentOffDate, order.getDocumentOffDateStart());
        }
        if (order.getDocumentOffDateEnd() != null) {
            wrapper.le(Order::getDocumentOffDate, order.getDocumentOffDateEnd());
        }

        if (order.getCustomsClosingDateStart() != null) {
            wrapper.ge(Order::getCustomsClosingDate, order.getCustomsClosingDateStart());
        }
        if (order.getCustomsClosingDateEnd() != null) {
            wrapper.le(Order::getCustomsClosingDate, order.getCustomsClosingDateEnd());
        }
        //过滤强制关闭订单
        wrapper.ne(Order::getOrderStatus, "强制关闭");
        if ("未锁账".equals(order.getOrderStatus())) {
            wrapper.ne(Order::getOrderStatus, "财务锁账");
        }
        if ("已锁账".equals(order.getOrderStatus())) {
            wrapper.eq(Order::getOrderStatus, "财务锁账");
        }
        if (StrUtil.isNotBlank(order.getMblNumber())) {
            wrapper.like(Order::getMblNumber, "%" + order.getMblNumber() + "%");
        }
        if(StrUtil.isNotBlank(order.getBookingNumber())){
            wrapper.like(Order::getBookingNumber,order.getBookingNumber());
        }
        if (StrUtil.isNotBlank(order.getHblNumber())) {
            wrapper.like(Order::getHblNumber, "%" + order.getHblNumber() + "%");
        }
        if (StrUtil.isNotBlank(order.getOrderCode())) {
            wrapper.like(Order::getOrderCode, "%" + order.getOrderCode() + "%");
        }
        if (StrUtil.isNotBlank(order.getServicerName())) {
            wrapper.like(Order::getServicerName, "%" + order.getServicerName() + "%");
        }
        if (StrUtil.isNotBlank(order.getSalesName())) {
            wrapper.like(Order::getSalesName, "%" + order.getSalesName() + "%");
        }
        if (StrUtil.isNotBlank(order.getCreatorName())) {
            wrapper.like(Order::getCreatorName, "%" + order.getCreatorName() + "%");
        }
//        if (StrUtil.isNotBlank(order.getArrivalStation())) {
//            wrapper.eq(Order::getArrivalStation, order.getArrivalStation());
//        }
//        if (StrUtil.isNotBlank(order.getDepartureStation())) {
//            wrapper.eq(Order::getDepartureStation, order.getDepartureStation());
//        }
        if (StrUtil.isNotBlank(order.getContainerMethod())) {
            wrapper.eq(Order::getContainerMethod, order.getContainerMethod());
        }
        if (order.getIncomeRecorded() != null && order.getIncomeRecorded()) {
            wrapper.eq(Order::getIncomeRecorded, order.getIncomeRecorded());
        }
        if (order.getIncomeRecorded() != null && !order.getIncomeRecorded()) {
            wrapper.and(i -> i.eq(Order::getIncomeRecorded, order.getIncomeRecorded()).or(j -> j.isNull(Order::getIncomeRecorded)));
        }
        if (order.getCostRecorded() != null && order.getCostRecorded()) {
            wrapper.eq(Order::getCostRecorded, order.getCostRecorded());
        }
        if (order.getCostRecorded() != null && !order.getCostRecorded()) {
            wrapper.and(i -> i.eq(Order::getCostRecorded, order.getCostRecorded()).or(j -> j.isNull(Order::getCostRecorded)));
        }
        if (order.getOrderPermission() != null && order.getOrderPermission() == 1) {
            wrapper.and(i -> i.eq(Order::getCreatorId, order.getCurrentUserId()).or(j -> j.eq(Order::getSalesId, order.getCurrentUserId())).or(k -> k.eq(Order::getServicerId, order.getCurrentUserId())));
        }
        if (order.getOrderPermission() != null && order.getOrderPermission() == 2) {
            List<Integer> WorkgroupIds = baseMapper.getWorkgroupIds(order.getCurrentUserId());
            wrapper.and(i -> i.eq(Order::getCreatorId, order.getCurrentUserId()).or(j -> j.eq(Order::getSalesId, order.getCurrentUserId())).or(k -> k.eq(Order::getServicerId, order.getCurrentUserId())).or(m -> m.in(Order::getWorkgroupId, WorkgroupIds)));
        }

        //订舱代理
        if (StrUtil.isNotBlank(order.getBookingAgentName())) {
            LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
            afVPrmCoopWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).like(AfVPrmCoop::getCoopName, "%" + order.getBookingAgentName() + "%").eq(AfVPrmCoop::getBusinessScopeSe, "SE").in(AfVPrmCoop::getCoopType, "互为代理", "海外代理");
            List<Integer> bookAgentIds = afVPrmCoopService.list(afVPrmCoopWrapper).stream().map(AfVPrmCoop::getCoopId).collect(Collectors.toList());
            if (bookAgentIds.size() == 0) {
                page.setRecords(new ArrayList());
                page.setTotal(0);
                return page;
            }
            wrapper.in(Order::getBookingAgentId, bookAgentIds);
        }
        wrapper.eq(Order::getOrgId, SecurityUtils.getUser().getOrgId()).eq(Order::getBusinessScope, "SE").orderByDesc(Order::getOrderId);
//        IPage<Order> iPage = page(page, wrapper);
        IPage<Order> iPage = baseMapper.getPageForSE(page, wrapper, order);
//        iPage.setRecords(resultForSC(result.getRecords()));
        iPage.getRecords().stream().forEach(scOrder -> {
            CoopVo coopVo = remoteCoopService.viewCoop(scOrder.getCoopId().toString()).getData();
            if (coopVo != null) {
                scOrder.setCustomerName(coopVo.getCoop_name());
            }
            LambdaQueryWrapper<PortMaintenance> departureStationWrapper = Wrappers.<PortMaintenance>lambdaQuery();
            departureStationWrapper.eq(PortMaintenance::getPortCode, scOrder.getDepartureStation());
            PortMaintenance departure = portMaintenanceService.getOne(departureStationWrapper);
            LambdaQueryWrapper<PortMaintenance> arrivalStationWrapper = Wrappers.<PortMaintenance>lambdaQuery();
            arrivalStationWrapper.eq(PortMaintenance::getPortCode, scOrder.getArrivalStation());
            PortMaintenance arrival = portMaintenanceService.getOne(arrivalStationWrapper);
            if (departure != null) {
                scOrder.setDepartureStation(departure.getPortNameEn());
            }
            if (arrival != null) {
                scOrder.setArrivalStation(arrival.getPortNameEn());
            }
            if (scOrder.getCarrierId() != null) {
                ShipCompany shipCompany = shipCompanyService.getById(scOrder.getCarrierId());
                if (shipCompany != null) {
                    scOrder.setCarrierName(shipCompany.getShipCompanyNameEn());
                }
            }

            if (scOrder.getBookingAgentId() != null) {
                LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
                afVPrmCoopWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfVPrmCoop::getCoopId, scOrder.getBookingAgentId());
                AfVPrmCoop bookingAgent = afVPrmCoopService.getOne(afVPrmCoopWrapper);
                if (bookingAgent != null) {
                    scOrder.setBookingAgentName(bookingAgent.getCoopName());
                }
            }
            StringBuffer buffer = new StringBuffer();
            LambdaQueryWrapper<OrderContainerDetails> orderContainerDetailsLambdaQueryWrapper = Wrappers.<OrderContainerDetails>lambdaQuery();
            orderContainerDetailsLambdaQueryWrapper.eq(OrderContainerDetails::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrderContainerDetails::getOrderId, scOrder.getOrderId());
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
            scOrder.setContainerNumberAndContainerSealNo(buffer.toString());
            //设置收入完成和成本完成（排序使用）
            if (scOrder.getIncomeRecorded() == true) {
                scOrder.setIncomeRecordedForSort(2);
            } else if (StringUtils.isNotBlank(scOrder.getIncomeStatus()) && !"未录收入".equals(scOrder.getIncomeStatus())) {
                scOrder.setIncomeRecordedForSort(1);
            } else {
                scOrder.setIncomeRecordedForSort(0);
            }
            if (scOrder.getCostRecorded() == true) {
                scOrder.setCostRecordedForSort(2);
            } else if (StringUtils.isNotBlank(scOrder.getCostStatus()) && !"未录成本".equals(scOrder.getCostStatus())) {
                scOrder.setCostRecordedForSort(1);
            } else {
                scOrder.setCostRecordedForSort(0);
            }

        });
        return iPage;
    }

    @Override
    public void exportExcelListSe(Order order) {
        List<OrderExcel> list = new ArrayList<OrderExcel>();
        order.setCurrentUserId(SecurityUtils.getUser().getId());
        LambdaQueryWrapper<Order> wrapper = Wrappers.<Order>lambdaQuery();
        if (StrUtil.isNotBlank(order.getCustomerName())) {
            List<Integer> coopIds = remoteCoopService.listByCoopName(order.getCustomerName()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
            /*if (coopIds.size() == 0) {
                page.setRecords(new ArrayList());
                page.setTotal(0);
                return page;
            }*/
            wrapper.in(Order::getCoopId, coopIds);
        }

        if (StrUtil.isNotBlank(order.getBillingType())) {
            wrapper.eq(Order::getBillingType, order.getBillingType());
        }

        if (StrUtil.isNotBlank(order.getCustomerNumber())) {
            wrapper.like(Order::getCustomerNumber, "%" + order.getCustomerNumber() + "%");
        }

        if (order.getExpectDepartureStart() != null) {
            wrapper.ge(Order::getExpectDeparture, order.getExpectDepartureStart());
        }
        if (order.getExpectDepartureEnd() != null) {
            wrapper.le(Order::getExpectDeparture, order.getExpectDepartureEnd());
        }
        if (order.getCreateTimeBegin() != null) {
            wrapper.ge(Order::getCreateTime, order.getCreateTimeBegin());
        }
        if (order.getCreateTimeEnd() != null) {
            wrapper.le(Order::getCreateTime, order.getCreateTimeEnd());
        }

        if (order.getExpectArrivalStart() != null) {
            wrapper.ge(Order::getExpectArrival, order.getExpectArrivalStart());
        }
        if (order.getExpectArrivalEnd() != null) {
            wrapper.le(Order::getExpectArrival, order.getExpectArrivalEnd());
        }
        
        if (order.getDocumentOffDateStart() != null) {
            wrapper.ge(Order::getDocumentOffDate, order.getDocumentOffDateStart());
        }
        if (order.getDocumentOffDateEnd() != null) {
            wrapper.le(Order::getDocumentOffDate, order.getDocumentOffDateEnd());
        }

        if (order.getCustomsClosingDateStart() != null) {
            wrapper.ge(Order::getCustomsClosingDate, order.getCustomsClosingDateStart());
        }
        if (order.getCustomsClosingDateEnd() != null) {
            wrapper.le(Order::getCustomsClosingDate, order.getCustomsClosingDateEnd());
        }
        wrapper.ne(Order::getOrderStatus, "强制关闭");
        if ("未锁账".equals(order.getOrderStatus())) {
            wrapper.ne(Order::getOrderStatus, "财务锁账");
        }
        if ("已锁账".equals(order.getOrderStatus())) {
            wrapper.eq(Order::getOrderStatus, "财务锁账");
        }
        if (StrUtil.isNotBlank(order.getMblNumber())) {
            wrapper.like(Order::getMblNumber, "%" + order.getMblNumber() + "%");
        }
        if (StrUtil.isNotBlank(order.getHblNumber())) {
            wrapper.like(Order::getHblNumber, "%" + order.getHblNumber() + "%");
        }
        if (StrUtil.isNotBlank(order.getOrderCode())) {
            wrapper.like(Order::getOrderCode, "%" + order.getOrderCode() + "%");
        }
        if (StrUtil.isNotBlank(order.getServicerName())) {
            wrapper.like(Order::getServicerName, "%" + order.getServicerName() + "%");
        }
        if (StrUtil.isNotBlank(order.getSalesName())) {
            wrapper.like(Order::getSalesName, "%" + order.getSalesName() + "%");
        }
        if (StrUtil.isNotBlank(order.getCreatorName())) {
            wrapper.like(Order::getCreatorName, "%" + order.getCreatorName() + "%");
        }
//        if (StrUtil.isNotBlank(order.getArrivalStation())) {
//            wrapper.eq(Order::getArrivalStation, order.getArrivalStation());
//        }
//        if (StrUtil.isNotBlank(order.getDepartureStation())) {
//            wrapper.eq(Order::getDepartureStation, order.getDepartureStation());
//        }
        if (StrUtil.isNotBlank(order.getContainerMethod())) {
            wrapper.eq(Order::getContainerMethod, order.getContainerMethod());
        }
        if (order.getIncomeRecorded() != null && order.getIncomeRecorded()) {
            wrapper.eq(Order::getIncomeRecorded, order.getIncomeRecorded());
        }
        if (order.getIncomeRecorded() != null && !order.getIncomeRecorded()) {
            wrapper.and(i -> i.eq(Order::getIncomeRecorded, order.getIncomeRecorded()).or(j -> j.isNull(Order::getIncomeRecorded)));
        }
        if (order.getCostRecorded() != null && order.getCostRecorded()) {
            wrapper.eq(Order::getCostRecorded, order.getCostRecorded());
        }
        if (order.getCostRecorded() != null && !order.getCostRecorded()) {
            wrapper.and(i -> i.eq(Order::getCostRecorded, order.getCostRecorded()).or(j -> j.isNull(Order::getCostRecorded)));
        }
        if (order.getOrderPermission() == 1) {
            wrapper.and(i -> i.eq(Order::getCreatorId, order.getCurrentUserId()).or(j -> j.eq(Order::getSalesId, order.getCurrentUserId())).or(k -> k.eq(Order::getServicerId, order.getCurrentUserId())));
        }
        if (order.getOrderPermission() == 2) {
            List<Integer> WorkgroupIds = baseMapper.getWorkgroupIds(order.getCurrentUserId());
            wrapper.and(i -> i.eq(Order::getCreatorId, order.getCurrentUserId()).or(j -> j.eq(Order::getSalesId, order.getCurrentUserId())).or(k -> k.eq(Order::getServicerId, order.getCurrentUserId())).or(m -> m.in(Order::getWorkgroupId, WorkgroupIds)));
        }
        //订舱代理
        if (StrUtil.isNotBlank(order.getBookingAgentName())) {
            LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
            afVPrmCoopWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).like(AfVPrmCoop::getCoopName, "%" + order.getBookingAgentName() + "%").eq(AfVPrmCoop::getBusinessScopeSe, "SE").in(AfVPrmCoop::getCoopType, "互为代理", "海外代理");
            List<Integer> bookAgentIds = afVPrmCoopService.list(afVPrmCoopWrapper).stream().map(AfVPrmCoop::getCoopId).collect(Collectors.toList());
            /*if (bookAgentIds.size() == 0) {
                page.setRecords(new ArrayList());
                page.setTotal(0);
                return page;
            }*/
            wrapper.in(Order::getBookingAgentId, bookAgentIds);
        }
        wrapper.eq(Order::getOrgId, SecurityUtils.getUser().getOrgId()).eq(Order::getBusinessScope, "SE").orderByDesc(Order::getOrderId);
//        List<Order> orderList = baseMapper.selectList(wrapper);
        List<Order> orderList = baseMapper.getPageForSE2(wrapper, order);
        Order total = new Order();
        orderList.stream().forEach(scOrder -> {
            CoopVo coopVo = remoteCoopService.viewCoop(scOrder.getCoopId().toString()).getData();
            if (coopVo != null) {
                scOrder.setCustomerName(coopVo.getCoop_name());
            }
            LambdaQueryWrapper<PortMaintenance> departureStationWrapper = Wrappers.<PortMaintenance>lambdaQuery();
            departureStationWrapper.eq(PortMaintenance::getPortCode, scOrder.getDepartureStation());
            PortMaintenance departure = portMaintenanceService.getOne(departureStationWrapper);
            LambdaQueryWrapper<PortMaintenance> arrivalStationWrapper = Wrappers.<PortMaintenance>lambdaQuery();
            arrivalStationWrapper.eq(PortMaintenance::getPortCode, scOrder.getArrivalStation());
            PortMaintenance arrival = portMaintenanceService.getOne(arrivalStationWrapper);
            if (departure != null) {
                scOrder.setDepartureStation(departure.getPortNameEn());
            }
            if (arrival != null) {
                scOrder.setArrivalStation(arrival.getPortNameEn());
            }
            if (scOrder.getCarrierId() != null) {
                ShipCompany shipCompany = shipCompanyService.getById(scOrder.getCarrierId());
                if (shipCompany != null) {
                    scOrder.setCarrierName(shipCompany.getShipCompanyNameEn());
                }
            }

            if (scOrder.getBookingAgentId() != null) {
                LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
                afVPrmCoopWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfVPrmCoop::getCoopId, scOrder.getBookingAgentId());
                AfVPrmCoop bookingAgent = afVPrmCoopService.getOne(afVPrmCoopWrapper);
                if (bookingAgent != null) {
                    scOrder.setBookingAgentName(bookingAgent.getCoopName());
                }
            }
            StringBuffer buffer = new StringBuffer();
            LambdaQueryWrapper<OrderContainerDetails> orderContainerDetailsLambdaQueryWrapper = Wrappers.<OrderContainerDetails>lambdaQuery();
            orderContainerDetailsLambdaQueryWrapper.eq(OrderContainerDetails::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrderContainerDetails::getOrderId, scOrder.getOrderId());
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
            scOrder.setContainerNumberAndContainerSealNo(buffer.toString());
            //计算合计
            //统计标箱数量
            if (total.getContainerNumber() == null) {
                total.setContainerNumber(scOrder.getContainerNumber() == null ? 0 : scOrder.getContainerNumber());
            } else {
                total.setContainerNumber(total.getContainerNumber() + (scOrder.getContainerNumber() == null ? 0 : scOrder.getContainerNumber()));
            }
            //统计件数
            if (total.getPlanPieces() == null) {
                total.setPlanPieces(scOrder.getPlanPieces() == null ? 0 : scOrder.getPlanPieces());
            } else {
                total.setPlanPieces(total.getPlanPieces() + (scOrder.getPlanPieces() == null ? 0 : scOrder.getPlanPieces()));
            }
            //统计毛重
            if (total.getPlanWeight() == null) {
                total.setPlanWeight(scOrder.getPlanWeight() == null ? BigDecimal.ZERO : scOrder.getPlanWeight());
            } else {
                total.setPlanWeight(total.getPlanWeight().add(scOrder.getPlanWeight() == null ? BigDecimal.ZERO : scOrder.getPlanWeight()));
            }
            //统计体积
            if (total.getPlanVolume() == null) {
                total.setPlanVolume(scOrder.getPlanVolume() == null ? BigDecimal.ZERO : scOrder.getPlanVolume());
            } else {
                total.setPlanVolume(total.getPlanVolume().add(scOrder.getPlanVolume() == null ? BigDecimal.ZERO : scOrder.getPlanVolume()));
            }
            //统计计重
            if (total.getPlanChargeWeight() == null) {
                total.setPlanChargeWeight(scOrder.getPlanChargeWeight() == null ? BigDecimal.ZERO : scOrder.getPlanChargeWeight());
            } else {
                total.setPlanChargeWeight(total.getPlanChargeWeight().add(scOrder.getPlanChargeWeight() == null ? BigDecimal.ZERO : scOrder.getPlanChargeWeight()));
            }

            OrderExcel oe = new OrderExcel();
            oe.setOrderCode(scOrder.getOrderCode());
            oe.setMblNumber(scOrder.getMblNumber());
            oe.setCustomerNumber(scOrder.getCustomerNumber());
            oe.setOrderStatus(scOrder.getOrderStatus());
            if ((scOrder.getIncomeRecorded() != null && scOrder.getIncomeRecorded()) || (StrUtil.isNotBlank(scOrder.getIncomeStatus()) && !"未录收入".equals(scOrder.getIncomeStatus()))) {
                oe.setIncomeRecorded("√");
            } else {
                oe.setIncomeRecorded("");
            }
            if ((scOrder.getCostRecorded() != null && scOrder.getCostRecorded()) || (StrUtil.isNotBlank(scOrder.getCostStatus()) && !"未录成本".equals(scOrder.getCostStatus()))) {
                oe.setCostRecorded("√");
            } else {
                oe.setCostRecorded("");
            }
            oe.setCustomerName(scOrder.getCustomerName());
            oe.setCarrierName(scOrder.getCarrierName());
            oe.setBookingAgentName(scOrder.getBookingAgentName());

            String expectDeparture = "";
            if (scOrder.getExpectDeparture() != null && !"".equals(scOrder.getExpectDeparture())) {
                expectDeparture = "" + scOrder.getExpectDeparture();
            }
            oe.setExpectDeparture(expectDeparture);
            if(scOrder.getExpectArrival()!=null) {
            	oe.setExpectArrival(""+scOrder.getExpectArrival());
            }else {
            	oe.setExpectArrival("");
            }
            oe.setShipNameAndNumber(scOrder.getShipName() + " / " + scOrder.getShipVoyageNumber());
            oe.setDepartureStation(scOrder.getDepartureStation());
            oe.setArrivalStation(scOrder.getArrivalStation());
            oe.setContainerMethod(scOrder.getContainerMethod());
            oe.setContainerList(scOrder.getContainerList());
            if (scOrder.getContainerNumber() != null && !"".equals(scOrder.getContainerNumber())) {
                oe.setContainerNumber("" + scOrder.getContainerNumber());
            } else {
                oe.setContainerNumber("");
            }
            oe.setPlanPieces("" + scOrder.getPlanPieces());
            oe.setPlanWeight("" + scOrder.getPlanWeight());
            oe.setPlanVolume("" + scOrder.getPlanVolume());
            oe.setPlanChargeWeight("" + scOrder.getPlanChargeWeight());
            if (scOrder.getContainerPickupService()) {
                oe.setContainerPickupService("√");
            } else {
                oe.setContainerPickupService("");
            }
            if (scOrder.getContainerLoadService()) {
                oe.setContainerLoadService("√");
            } else {
                oe.setContainerLoadService("");
            }
            if (scOrder.getCustomsClearanceService()) {
                oe.setCustomsClearanceService("√");
            } else {
                oe.setCustomsClearanceService("");
            }
            if (scOrder.getArrivalCustomsClearanceService()) {
                oe.setArrivalCustomsClearanceService("√");
            } else {
                oe.setArrivalCustomsClearanceService("");
            }
            if (scOrder.getDeliveryService()) {
                oe.setDeliveryService("√");
            } else {
                oe.setDeliveryService("");
            }
            oe.setHblNumber(scOrder.getHblNumber());
            oe.setBillingType(scOrder.getBillingType());

            String billingMethod = "";
            if (scOrder.getBillingMethod() != null && !"".equals(scOrder.getBillingMethod())) {
                billingMethod = scOrder.getBillingMethod();
            }
            String originalsNumber = "";
            if (!"".equals(scOrder.getOriginalsNumber()) && scOrder.getOriginalsNumber() != null) {
                originalsNumber = scOrder.getOriginalsNumber() + "正";
            }
            String copyNumber = "";
            if (!"".equals(scOrder.getCopyNumber()) && scOrder.getCopyNumber() != null) {
                copyNumber = scOrder.getCopyNumber() + "副";
            }
            oe.setOutOrder(billingMethod + " / " + originalsNumber + copyNumber);

            String documentOffDate = "";
            if (scOrder.getDocumentOffDate() != null && !"".equals(scOrder.getDocumentOffDate())) {
                documentOffDate = "" + scOrder.getDocumentOffDate();
            }
            oe.setDocumentOffDate(documentOffDate);

            String customsClosingDate = "";
            if (scOrder.getCustomsClosingDate() != null && !"".equals(scOrder.getCustomsClosingDate())) {
                customsClosingDate = "" + scOrder.getCustomsClosingDate();
            }
            oe.setCustomsClosingDate(customsClosingDate);

            String issueDate = "";
            if (scOrder.getIssueDate() != null && !"".equals(scOrder.getIssueDate())) {
                issueDate = "" + scOrder.getIssueDate();
            }
            oe.setIssueDate(issueDate);
            oe.setGoodsType(scOrder.getGoodsType());
            oe.setGoodsNameCn(scOrder.getGoodsNameCn());
            oe.setTransitClause(scOrder.getTransitClause());
            if (scOrder.getServicerName() != null && !"".equals(scOrder.getServicerName())) {
                oe.setServicerName(scOrder.getServicerName().split(" ")[0]);
            } else {
                oe.setServicerName("");
            }
            if (scOrder.getSalesName() != null && !"".equals(scOrder.getSalesName())) {
                oe.setSalesName(scOrder.getSalesName().split(" ")[0]);
            } else {
                oe.setSalesName("");
            }
            if (scOrder.getCreatorName() != null && !"".equals(scOrder.getCreatorName())) {
                oe.setCreatorName(scOrder.getCreatorName().split(" ")[0]);
            } else {
                oe.setCreatorName("");
            }
            oe.setOrderRemark(scOrder.getOrderRemark());
            oe.setContainerNumberAndContainerSealNo(scOrder.getContainerNumberAndContainerSealNo());
            oe.setBookingNumber(scOrder.getBookingNumber());
            list.add(oe);
        });

        OrderExcel oeTotal = new OrderExcel();
        oeTotal.setOrderCode("合计：");
        oeTotal.setContainerNumber("" + total.getContainerNumber());
        oeTotal.setPlanPieces("" + total.getPlanPieces());
        oeTotal.setPlanWeight("" + FormatUtils.formatWithQWF(total.getPlanWeight(), 3));
        oeTotal.setPlanVolume("" + FormatUtils.formatWithQWF(total.getPlanVolume(), 3));
        oeTotal.setPlanChargeWeight("" + FormatUtils.formatWithQWF(total.getPlanChargeWeight(), 3));
        list.add(oeTotal);
        //自定义字段
        if (!StringUtils.isEmpty(order.getColumnStrs())) {
            List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();

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
                for (OrderExcel excel : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        if ("shipName".equals(colunmStrs[j])) {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName("shipNameAndNumber", excel));
                        } else {
                            map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], excel));
                        }

                    }
                    listExcel.add(map);
                }
            }
            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(LoginUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");
        } else {
            ExportExcel<OrderExcel> ex = new ExportExcel<OrderExcel>();
            String[] headers = {"订单号", "主提单号", "订仓编号","合约号", "操作节点", "收入完成", "成本完成", "客户名称", "船公司", "订舱代理", "离港日期","抵港日期", "船名/船次", "起运港", "目的港", "装箱方式", "集装箱量", "箱号/铅封号", "标箱数量(TEU)", "件数",
                    "毛重(KG)", "体积(CBM)", "计费吨(TON)", "提柜服务", "装箱服务", "报关服务", "目的港清关", "目的港派送", "分提单号", "提单类型", "出单信息", "截单日期", "截关日期", "签发日期", "货物类型", "中文品名", "运输条款",
                    "责任客服", "责任销售", "责任操作", "订单备注"};
            ex.exportExcel(LoginUtils.getResponse(), "导出EXCEL", headers, list, "Export");
        }
    }

    @Override
    public Order view(Integer orderId) {
        Order order = getById(orderId);
        LambdaQueryWrapper<OrderShipperConsignee> shipperWrapper = Wrappers.<OrderShipperConsignee>lambdaQuery();
        shipperWrapper.eq(OrderShipperConsignee::getOrderId, orderId).eq(OrderShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrderShipperConsignee::getScType, 0);
        OrderShipperConsignee shipper = orderShipperConsigneeService.getOne(shipperWrapper);
        if (shipper == null) {
            shipper = new OrderShipperConsignee();
            shipper.setScPrintRemark("");
        }
        order.setShipper(shipper);

        LambdaQueryWrapper<OrderShipperConsignee> consigneeWrapper = Wrappers.<OrderShipperConsignee>lambdaQuery();
        consigneeWrapper.eq(OrderShipperConsignee::getOrderId, orderId).eq(OrderShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrderShipperConsignee::getScType, 1);
        OrderShipperConsignee consignee = orderShipperConsigneeService.getOne(consigneeWrapper);
        if (consignee == null) {
            consignee = new OrderShipperConsignee();
            consignee.setScPrintRemark("");
        }
        order.setConsignee(consignee);

        CoopVo coop = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
        if (coop != null) {
            order.setCustomerName(coop.getCoop_name());
        }

        LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
        afVPrmCoopWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfVPrmCoop::getCoopId, order.getBookingAgentId());
        AfVPrmCoop bookingAgent = afVPrmCoopService.getOne(afVPrmCoopWrapper);
        if (bookingAgent != null) {
            order.setBookingAgentName(bookingAgent.getCoopName());
        }
        ShipCompany carrier = shipCompanyService.getById(order.getCarrierId());
        if (carrier != null) {
            order.setCarrierName(carrier.getShipCompanyNameCn());
        }
        LambdaQueryWrapper<OrderContainerDetails> orderContainerDetailsWrapper = Wrappers.<OrderContainerDetails>lambdaQuery();
        orderContainerDetailsWrapper.eq(OrderContainerDetails::getOrderId, orderId).eq(OrderContainerDetails::getOrgId, SecurityUtils.getUser().getOrgId());
        List<OrderContainerDetails> orderContainerDetails = orderContainerDetailsService.list(orderContainerDetailsWrapper);
        order.setContainerDetails(orderContainerDetails);
        //查询起运港英文名称
        LambdaQueryWrapper<PortMaintenance> portMaintenanceWrapper = Wrappers.<PortMaintenance>lambdaQuery();
        portMaintenanceWrapper.eq(PortMaintenance::getPortCode, order.getDepartureStation());
        PortMaintenance portMaintenance = portMaintenanceService.getOne(portMaintenanceWrapper);
        if(portMaintenance != null){
            order.setDepartureStationNameEn(portMaintenance.getPortNameEn());
            order.setDepartureStationNameCn(portMaintenance.getPortNameCn() + "(" + portMaintenance.getCountryNameCn() + ")");
        }
        //查询目的港英文名称
        LambdaQueryWrapper<PortMaintenance> portMaintenanceWrapper1 = Wrappers.<PortMaintenance>lambdaQuery();
        portMaintenanceWrapper1.eq(PortMaintenance::getPortCode, order.getArrivalStation());
        PortMaintenance portMaintenance1 = portMaintenanceService.getOne(portMaintenanceWrapper1);
        if(portMaintenance1 != null){
            order.setArrivalStationNameEn(portMaintenance1.getPortNameEn());
            order.setArrivalStationNameCn(portMaintenance1.getPortNameCn() + "(" + portMaintenance1.getCountryNameCn() + ")");
        }
        //查询中转港英文名称
        if(!"".equals(order.getTransitStation())){
            LambdaQueryWrapper<PortMaintenance> portMaintenanceWrapper2 = Wrappers.<PortMaintenance>lambdaQuery();
            portMaintenanceWrapper2.eq(PortMaintenance::getPortCode, order.getTransitStation());
            PortMaintenance portMaintenance2 = portMaintenanceService.getOne(portMaintenanceWrapper2);
            if(portMaintenance2 != null){
                order.setTransitStationNameEn(portMaintenance2.getPortNameEn());
                order.setTransitStationNameCn(portMaintenance2.getPortNameCn() + "(" + portMaintenance2.getCountryNameCn() + ")");
            }
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void insertSI(Order order) {

        //1.新建订单
        order.setOrderCode(createOrderCode("SI"));
        order.setBusinessScope("SI");
        order.setOrderUuid(createUuid());
        if (order.getDeliverySignDate() != null) {
            order.setOrderStatus("派送签收");
        } else if (order.getOutboundDate() != null) {
            order.setOrderStatus("货物出库");
        } else if (order.getCustomsClearanceDate() != null) {
            order.setOrderStatus("海关放行");
        } else if (order.getCustomsInspectionDate() != null) {
            order.setOrderStatus("海关查验");
        } else if (order.getInboundDate() != null) {
            order.setOrderStatus("货物入库");
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
        order.setCreateTime(now);
        order.setEditTime(now);
        order.setCreatorId(SecurityUtils.getUser().getId());
        order.setEditorId(SecurityUtils.getUser().getId());
        order.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        order.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        order.setOrgId(SecurityUtils.getUser().getOrgId());
        order.setIncomeStatus("未录收入");
        order.setCostStatus("未录成本");
        order.setIncomeRecorded(false);
        order.setCostRecorded(false);
        save(order);

        //2.新建订单收发货人
        OrderShipperConsignee shipper = order.getShipper();
        OrderShipperConsignee consignee = order.getConsignee();
        if (StrUtil.isNotBlank(shipper.getScPrintRemark())) {
            shipper.setCreateTime(now);
            shipper.setCreatorId(SecurityUtils.getUser().getId());
            shipper.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            shipper.setOrgId(SecurityUtils.getUser().getOrgId());
            shipper.setEditorId(SecurityUtils.getUser().getId());
            shipper.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            shipper.setEditTime(now);
            shipper.setOrderId(order.getOrderId());
            orderShipperConsigneeService.save(shipper);
        }

        if (StrUtil.isNotBlank(consignee.getScPrintRemark())) {
            consignee.setCreateTime(now);
            consignee.setCreatorId(SecurityUtils.getUser().getId());
            consignee.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            consignee.setOrgId(SecurityUtils.getUser().getOrgId());
            consignee.setEditorId(SecurityUtils.getUser().getId());
            consignee.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            consignee.setEditTime(now);
            consignee.setOrderId(order.getOrderId());
            orderShipperConsigneeService.save(consignee);
        }

        //3.如果无船名新建船名
        if (StrUtil.isNotBlank(order.getShipName())) {
            LambdaQueryWrapper<OrderShipInfo> shipWrapper = Wrappers.<OrderShipInfo>lambdaQuery();
            shipWrapper.eq(OrderShipInfo::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrderShipInfo::getShipNameEn, order.getShipName());
            List<OrderShipInfo> shipInfoList = orderShipInfoService.list(shipWrapper);
            if (shipInfoList.size() == 0) {
                OrderShipInfo orderShipInfo = new OrderShipInfo();
                orderShipInfo.setCreateTime(now);
                orderShipInfo.setEditTime(now);
                orderShipInfo.setCreatorId(SecurityUtils.getUser().getId());
                orderShipInfo.setEditorId(SecurityUtils.getUser().getId());
                orderShipInfo.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                orderShipInfo.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                orderShipInfo.setOrgId(SecurityUtils.getUser().getOrgId());
                orderShipInfo.setIsValid(true);
                orderShipInfo.setShipNameEn(order.getShipName());
                orderShipInfoService.save(orderShipInfo);
            }
        }
        //4.保存集装箱量明细
        if (order.getContainerDetails().size() != 0) {
            order.getContainerDetails().stream().forEach(orderContainerDetail -> {
                orderContainerDetail.setOrderId(order.getOrderId());
                orderContainerDetail.setOrgId(SecurityUtils.getUser().getOrgId());
            });
            orderContainerDetailsService.saveBatch(order.getContainerDetails());
        }
        //5.创建节点日志
//        Log log = new Log();
//        log.setAwbNumber(order.getMblNumber());
//        log.setBusinessScope("SI");
//        log.setCreatTime(now);
//        log.setCreatorId(SecurityUtils.getUser().getId());
//        log.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
//        log.setLogRemark("SI订单创建" + order.getOrderCode());
//        log.setLogType("SI 订单");
//        log.setNodeName("订单创建");
//        log.setOrderNumber(order.getOrderCode());
//        log.setOrderUuid(order.getOrderUuid());
//        log.setOrgId(SecurityUtils.getUser().getOrgId());
//        log.setPageFunction("订单创建");
//        log.setPageName("SI 订单");
//        logService.save(log);
        Log logBean = new Log();
        logBean.setPageName("SI订单");
        logBean.setPageFunction("订单创建");
        logBean.setBusinessScope("SI");
        logBean.setOrderNumber(order.getOrderCode());
        logBean.setOrderId(order.getOrderId());
        logBean.setOrderUuid(order.getOrderUuid());
        logService.saveLog(logBean);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void insertSE(Order order) {

        //1.新建订单
        order.setOrderCode(createOrderCode("SE"));
        if (StrUtil.isBlank(order.getBookingNumber())) {
            order.setBookingNumber(order.getOrderCode());
        }
        order.setBusinessScope("SE");
        order.setOrderUuid(createUuid());
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
        order.setCreateTime(now);
        order.setEditTime(now);
        order.setCreatorId(SecurityUtils.getUser().getId());
        order.setEditorId(SecurityUtils.getUser().getId());
        order.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        order.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        order.setOrgId(SecurityUtils.getUser().getOrgId());
        boolean incomeFlag = false;
        boolean costFlag = false;
        List<Income> listIncome = null;
        List<Cost> listCost = null;
        //复制新增 是否需要复制费用
        if (order.getAmountFlag() != null && order.getAmountFlagOrderId() != null) {
            Order orderCopy = baseMapper.selectById(order.getAmountFlagOrderId());
            //查询 income
            LambdaQueryWrapper<Income> incomeWrapper = Wrappers.<Income>lambdaQuery();
            incomeWrapper.eq(Income::getOrgId, SecurityUtils.getUser().getOrgId()).eq(Income::getOrderId, orderCopy.getOrderId());
            listIncome = incomeService.list(incomeWrapper);
            if (listIncome != null && listIncome.size() > 0) {
                incomeFlag = true;
                order.setIncomeStatus("已录收入");
            }
            //查询 cost
            LambdaQueryWrapper<Cost> costWrapper = Wrappers.<Cost>lambdaQuery();
            costWrapper.eq(Cost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(Cost::getOrderId, orderCopy.getOrderId());
            listCost = costService.list(costWrapper);
            if (listCost != null && listCost.size() > 0) {
                costFlag = true;
                order.setCostStatus("已录成本");
            }

        }
        if (!incomeFlag) {
            order.setIncomeStatus("未录收入");
        }
        if (!costFlag) {
            order.setCostStatus("未录成本");
        }
        order.setIncomeRecorded(false);
        order.setCostRecorded(false);
        save(order);

        //2.新建订单收发货人
        OrderShipperConsignee shipper = order.getShipper();
        OrderShipperConsignee consignee = order.getConsignee();
        if (StrUtil.isNotBlank(shipper.getScPrintRemark())) {
            shipper.setCreateTime(now);
            shipper.setCreatorId(SecurityUtils.getUser().getId());
            shipper.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            shipper.setOrgId(SecurityUtils.getUser().getOrgId());
            shipper.setEditorId(SecurityUtils.getUser().getId());
            shipper.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            shipper.setEditTime(now);
            shipper.setOrderId(order.getOrderId());
            orderShipperConsigneeService.save(shipper);
        }

        if (StrUtil.isNotBlank(consignee.getScPrintRemark())) {
            consignee.setCreateTime(now);
            consignee.setCreatorId(SecurityUtils.getUser().getId());
            consignee.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            consignee.setOrgId(SecurityUtils.getUser().getOrgId());
            consignee.setEditorId(SecurityUtils.getUser().getId());
            consignee.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            consignee.setEditTime(now);
            consignee.setOrderId(order.getOrderId());
            orderShipperConsigneeService.save(consignee);
        }

        //3.如果无船名新建船名
        if (StrUtil.isNotBlank(order.getShipName())) {
            LambdaQueryWrapper<OrderShipInfo> shipWrapper = Wrappers.<OrderShipInfo>lambdaQuery();
            shipWrapper.eq(OrderShipInfo::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrderShipInfo::getShipNameEn, order.getShipName());
            List<OrderShipInfo> shipInfoList = orderShipInfoService.list(shipWrapper);
            if (shipInfoList.size() == 0) {
                OrderShipInfo orderShipInfo = new OrderShipInfo();
                orderShipInfo.setCreateTime(now);
                orderShipInfo.setEditTime(now);
                orderShipInfo.setCreatorId(SecurityUtils.getUser().getId());
                orderShipInfo.setEditorId(SecurityUtils.getUser().getId());
                orderShipInfo.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                orderShipInfo.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                orderShipInfo.setOrgId(SecurityUtils.getUser().getOrgId());
                orderShipInfo.setIsValid(true);
                orderShipInfo.setShipNameEn(order.getShipName());
                orderShipInfoService.save(orderShipInfo);
            }
        }
        //4.保存集装箱量明细
        if (order.getContainerDetails().size() != 0) {
            order.getContainerDetails().stream().forEach(orderContainerDetail -> {
                orderContainerDetail.setOrderId(order.getOrderId());
                orderContainerDetail.setOrgId(SecurityUtils.getUser().getOrgId());
            });
            orderContainerDetailsService.saveBatch(order.getContainerDetails());
        }

        if (incomeFlag) {
            listIncome.stream().forEach(o -> {
                o.setIncomeId(null);
                o.setEditorId(null);
                o.setEditorName(null);
                o.setEditTime(null);
                o.setDebitNoteId(null);
                o.setFinancialDate(null);
                o.setIncomeAmountWriteoff(null);
                o.setRowUuid(UUID.randomUUID().toString());
                o.setCreatorId(SecurityUtils.getUser().getId());
                o.setCreateTime(now);
                o.setCreatorName(SecurityUtils.getUser().buildOptName());
                o.setOrderId(order.getOrderId());
                o.setOrderUuid(order.getOrderUuid());
                o.setCustomerId(order.getCoopId());
                o.setCustomerName(order.getCustomerName());
                incomeService.save(o);
            });
        }
        if (costFlag) {
            listCost.stream().forEach(o -> {
                o.setCostId(null);
                o.setEditorId(null);
                o.setEditorName(null);
                o.setEditTime(null);
                o.setPaymentId(null);
                o.setFinancialDate(null);
                o.setRowUuid(UUID.randomUUID().toString());
                o.setCreatorId(SecurityUtils.getUser().getId());
                o.setCreateTime(now);
                o.setCreatorName(SecurityUtils.getUser().buildOptName());
                o.setOrderId(order.getOrderId());
                o.setOrderUuid(order.getOrderUuid());
                o.setIncomeId(null);
                o.setCostAmountWriteoff(null);
                o.setCostAmountPayment(null);
                costService.save(o);
            });
        }

        //5.创建节点日志
//        Log log = new Log();
//        log.setAwbNumber(order.getMblNumber());
//        log.setBusinessScope("SE");
//        log.setCreatTime(now);
//        log.setCreatorId(SecurityUtils.getUser().getId());
//        log.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
//        log.setLogRemark("SE订单创建" + order.getOrderCode());
//        log.setLogType("SE 订单");
//        log.setNodeName("订单创建");
//        log.setOrderNumber(order.getOrderCode());
//        log.setOrderUuid(order.getOrderUuid());
//        log.setOrgId(SecurityUtils.getUser().getOrgId());
//        log.setPageFunction("订单创建");
//        log.setPageName("SE 订单");
//        logService.save(log);
        Log logBean = new Log();
        logBean.setPageName("SE订单");
        logBean.setPageFunction("订单创建");
        logBean.setBusinessScope("SE");
        logBean.setOrderNumber(order.getOrderCode());
        logBean.setOrderId(order.getOrderId());
        logBean.setOrderUuid(order.getOrderUuid());
        logService.saveLog(logBean);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifySI(Order order) {
        //0.校验状态-财务锁账不可修改
        Order orderForCheck = getById(order.getOrderId());
        if (orderForCheck == null) {
            throw new RuntimeException("订单不存在，修改失败");
        }
        if ("财务锁账".equals(orderForCheck.getOrderStatus())) {
            throw new RuntimeException("订单财务已锁账，无法修改");
        }

        //1.修改订单
        Order bean = baseMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), order.getOrderUuid());
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
            order.setOrderStatus("派送签收");
        } else if (order.getOutboundDate() != null) {
            order.setOrderStatus("货物出库");
        } else if (order.getCustomsClearanceDate() != null) {
            order.setOrderStatus("海关放行");
        } else if (order.getCustomsInspectionDate() != null) {
            order.setOrderStatus("海关查验");
        } else if (order.getInboundDate() != null) {
            order.setOrderStatus("货物入库");
        } else {
            order.setOrderStatus("订单创建");
        }
        LocalDateTime now = LocalDateTime.now();
        order.setEditTime(now);
        order.setEditorId(SecurityUtils.getUser().getId());
        order.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        updateById(order);

        //2.修改收发货人
        OrderShipperConsignee shipper = order.getShipper();
        OrderShipperConsignee consignee = order.getConsignee();
        if (StrUtil.isNotBlank(shipper.getScPrintRemark())) {
            if (shipper.getOrderScId() == null) {
                shipper.setCreateTime(now);
                shipper.setCreatorId(SecurityUtils.getUser().getId());
                shipper.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                shipper.setOrgId(SecurityUtils.getUser().getOrgId());
                shipper.setEditorId(SecurityUtils.getUser().getId());
                shipper.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                shipper.setEditTime(now);
                shipper.setOrderId(order.getOrderId());
                orderShipperConsigneeService.save(shipper);
            } else {
                shipper.setEditorId(SecurityUtils.getUser().getId());
                shipper.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
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
                consignee.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                consignee.setOrgId(SecurityUtils.getUser().getOrgId());
                consignee.setEditorId(SecurityUtils.getUser().getId());
                consignee.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                consignee.setEditTime(now);
                consignee.setOrderId(order.getOrderId());
                orderShipperConsigneeService.save(consignee);
            } else {
                consignee.setEditorId(SecurityUtils.getUser().getId());
                consignee.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                consignee.setEditTime(now);
                orderShipperConsigneeService.updateById(consignee);
            }
        } else {
            if (consignee.getOrderScId() != null) {
                orderShipperConsigneeService.removeById(consignee.getOrderScId());
            }
        }
        //3.如果无船名新建船名
        if (StrUtil.isNotBlank(order.getShipName())) {
            LambdaQueryWrapper<OrderShipInfo> shipWrapper = Wrappers.<OrderShipInfo>lambdaQuery();
            shipWrapper.eq(OrderShipInfo::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrderShipInfo::getShipNameEn, order.getShipName());
            List<OrderShipInfo> shipInfoList = orderShipInfoService.list(shipWrapper);
            if (shipInfoList.size() == 0) {
                OrderShipInfo orderShipInfo = new OrderShipInfo();
                orderShipInfo.setCreateTime(now);
                orderShipInfo.setEditTime(now);
                orderShipInfo.setCreatorId(SecurityUtils.getUser().getId());
                orderShipInfo.setEditorId(SecurityUtils.getUser().getId());
                orderShipInfo.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                orderShipInfo.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                orderShipInfo.setOrgId(SecurityUtils.getUser().getOrgId());
                orderShipInfo.setIsValid(true);
                orderShipInfo.setShipNameEn(order.getShipName());
                orderShipInfoService.save(orderShipInfo);
            }
        }
        //4.保存集装箱量明细
        LambdaQueryWrapper<OrderContainerDetails> orderContainerDetailsWrapper = Wrappers.<OrderContainerDetails>lambdaQuery();
        orderContainerDetailsWrapper.eq(OrderContainerDetails::getOrderId, order.getOrderId()).eq(OrderContainerDetails::getOrgId, SecurityUtils.getUser().getOrgId());
        orderContainerDetailsService.remove(orderContainerDetailsWrapper);
        if (order.getContainerDetails().size() != 0) {
            order.getContainerDetails().stream().forEach(orderContainerDetail -> {
                orderContainerDetail.setOrderId(order.getOrderId());
                orderContainerDetail.setOrgId(SecurityUtils.getUser().getOrgId());
            });
            orderContainerDetailsService.saveBatch(order.getContainerDetails());
        }
        //5.修改日志
        Log logBean = new Log();
        logBean.setPageName("SI订单");
        logBean.setPageFunction("订单编辑");
        logBean.setLogRemark(this.getLogRemarkSI(bean, order));
        logBean.setBusinessScope("SI");
        logBean.setOrderNumber(order.getOrderCode());
        logBean.setOrderId(order.getOrderId());
        logBean.setOrderUuid(order.getOrderUuid());
        logService.saveLog(logBean);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifySE(Order order) {
        //0.校验状态-财务锁账不可修改
        Order orderForCheck = getById(order.getOrderId());
        if (orderForCheck == null) {
            throw new RuntimeException("订单不存在，修改失败");
        }
        if ("财务锁账".equals(orderForCheck.getOrderStatus())) {
            throw new RuntimeException("订单财务已锁账，无法修改");
        }

        //1.修改订单
        Order bean = baseMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), order.getOrderUuid());
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
        order.setEditTime(now);
        order.setEditorId(SecurityUtils.getUser().getId());
        order.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        updateById(order);

        //2.修改收发货人
        OrderShipperConsignee shipper = order.getShipper();
        OrderShipperConsignee consignee = order.getConsignee();
        if (StrUtil.isNotBlank(shipper.getScPrintRemark())) {
            if (shipper.getOrderScId() == null) {
                shipper.setCreateTime(now);
                shipper.setCreatorId(SecurityUtils.getUser().getId());
                shipper.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                shipper.setOrgId(SecurityUtils.getUser().getOrgId());
                shipper.setEditorId(SecurityUtils.getUser().getId());
                shipper.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                shipper.setEditTime(now);
                shipper.setOrderId(order.getOrderId());
                orderShipperConsigneeService.save(shipper);
            } else {
                shipper.setEditorId(SecurityUtils.getUser().getId());
                shipper.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
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
                consignee.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                consignee.setOrgId(SecurityUtils.getUser().getOrgId());
                consignee.setEditorId(SecurityUtils.getUser().getId());
                consignee.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                consignee.setEditTime(now);
                consignee.setOrderId(order.getOrderId());
                orderShipperConsigneeService.save(consignee);
            } else {
                consignee.setEditorId(SecurityUtils.getUser().getId());
                consignee.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                consignee.setEditTime(now);
                orderShipperConsigneeService.updateById(consignee);
            }
        } else {
            if (consignee.getOrderScId() != null) {
                orderShipperConsigneeService.removeById(consignee.getOrderScId());
            }
        }
        //3.如果无船名新建船名
        if (StrUtil.isNotBlank(order.getShipName())) {
            LambdaQueryWrapper<OrderShipInfo> shipWrapper = Wrappers.<OrderShipInfo>lambdaQuery();
            shipWrapper.eq(OrderShipInfo::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrderShipInfo::getShipNameEn, order.getShipName());
            List<OrderShipInfo> shipInfoList = orderShipInfoService.list(shipWrapper);
            if (shipInfoList.size() == 0) {
                OrderShipInfo orderShipInfo = new OrderShipInfo();
                orderShipInfo.setCreateTime(now);
                orderShipInfo.setEditTime(now);
                orderShipInfo.setCreatorId(SecurityUtils.getUser().getId());
                orderShipInfo.setEditorId(SecurityUtils.getUser().getId());
                orderShipInfo.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                orderShipInfo.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                orderShipInfo.setOrgId(SecurityUtils.getUser().getOrgId());
                orderShipInfo.setIsValid(true);
                orderShipInfo.setShipNameEn(order.getShipName());
                orderShipInfoService.save(orderShipInfo);
            }
        }
        //4.保存集装箱量明细
        LambdaQueryWrapper<OrderContainerDetails> orderContainerDetailsWrapper = Wrappers.<OrderContainerDetails>lambdaQuery();
        orderContainerDetailsWrapper.eq(OrderContainerDetails::getOrderId, order.getOrderId()).eq(OrderContainerDetails::getOrgId, SecurityUtils.getUser().getOrgId());
        orderContainerDetailsService.remove(orderContainerDetailsWrapper);
        if (order.getContainerDetails().size() != 0) {
            order.getContainerDetails().stream().forEach(orderContainerDetail -> {
                orderContainerDetail.setOrderId(order.getOrderId());
                orderContainerDetail.setOrgId(SecurityUtils.getUser().getOrgId());
            });
            orderContainerDetailsService.saveBatch(order.getContainerDetails());
        }
        //5.修改日志
        Log logBean = new Log();
        logBean.setPageName("SE订单");
        logBean.setPageFunction("订单编辑");
        logBean.setLogRemark(this.getLogRemarkSE(bean, order));
        logBean.setBusinessScope("SE");
        logBean.setOrderNumber(order.getOrderCode());
        logBean.setOrderId(order.getOrderId());
        logBean.setOrderUuid(order.getOrderUuid());
        logService.saveLog(logBean);
    }

    private String getLogRemarkSI(Order order, Order bean) {
        StringBuffer logremark = new StringBuffer();
        String coopName = this.getStr(order.getCustomerName(), bean.getCustomerName());
        logremark.append(StringUtils.isBlank(coopName) ? "" : "客户：" + coopName);
        String salesName = this.getStr(order.getSalesName(), bean.getSalesName());
        logremark.append(StringUtils.isBlank(salesName) ? "" : "销售：" + salesName);
        String customerNumber = this.getStr(order.getCustomerNumber(), bean.getCustomerNumber());
        logremark.append(StringUtils.isBlank(customerNumber) ? "" : "客单：" + customerNumber);
        String expectArrival = this.getStr("" + order.getExpectArrival(), "" + bean.getExpectArrival());
        logremark.append(StringUtils.isBlank(expectArrival) ? "" : "到港：" + expectArrival);
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
//    		orderStr22=""+order.getPlanWeight();
            orderStr22 = this.fmtMicrometer3(String.valueOf(order.getPlanWeight()));
        }
        if (order.getPlanVolume() != null || !"null".equals("" + order.getPlanVolume())) {
//    		orderStr33=""+order.getPlanVolume();
            orderStr33 = this.fmtMicrometer3(String.valueOf(order.getPlanVolume()));
        }


        if (bean.getPlanPieces() != null || !"null".equals("" + bean.getPlanPieces())) {
            beanStr11 = "" + bean.getPlanPieces();
        }
        if (bean.getPlanWeight() != null || !"null".equals("" + bean.getPlanWeight())) {
//    		beanStr22=""+bean.getPlanWeight();
            beanStr22 = this.fmtMicrometer3(String.valueOf(bean.getPlanWeight()));
        }
        if (bean.getPlanVolume() != null || !"null".equals("" + bean.getPlanVolume())) {
//    		beanStr33=""+bean.getPlanVolume();
            beanStr33 = this.fmtMicrometer3(String.valueOf(bean.getPlanVolume()));
        }

        if (!(orderStr11 + "/" + orderStr22 + "/" + orderStr33).equals(beanStr11 + "/" + beanStr22 + "/" + beanStr33)) {
            logremark.append("件/毛/体：" + orderStr11 + "/" + orderStr22 + "/" + orderStr33 + " -> " + beanStr11 + "/" + beanStr22 + "/" + beanStr33 + "  ");
        }
//    	String planPieces=this.getStr(""+order.getPlanPieces(),""+bean.getPlanPieces());
//    	logremark.append(StringUtils.isBlank(planPieces)?"":"预件："+planPieces);
//    	String planWeight=this.getStr(""+order.getPlanWeight(),""+bean.getPlanWeight());
//    	logremark.append(StringUtils.isBlank(planWeight)?"":"预毛："+planWeight);
//    	String planVolume=this.getStr(""+order.getPlanVolume(),""+bean.getPlanVolume());
//    	logremark.append(StringUtils.isBlank(planVolume)?"":"预体："+planVolume);

        String planChargeWeight = this.getStr("" + order.getPlanChargeWeight(), this.fmtMicrometer3(String.valueOf(bean.getPlanChargeWeight())));
        logremark.append(StringUtils.isBlank(planChargeWeight) ? "" : "计费吨：" + planChargeWeight);

        String changeOrderService = this.getStr(order.getChangeOrderService() ? "是" : "否", bean.getChangeOrderService() ? "是" : "否");
        logremark.append(StringUtils.isBlank(changeOrderService) ? "" : "调单：" + changeOrderService);
        String warehouseService = this.getStr(order.getWarehouseService() ? "是" : "否", bean.getWarehouseService() ? "是" : "否");
        logremark.append(StringUtils.isBlank(warehouseService) ? "" : "库内：" + warehouseService);

        String customsClearanceService = this.getStr(order.getCustomsClearanceService() ? "是" : "否", bean.getCustomsClearanceService() ? "是" : "否");
        logremark.append(StringUtils.isBlank(customsClearanceService) ? "" : "报关：" + customsClearanceService);

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

    private String getLogRemarkSE(Order order, Order bean) {
        StringBuffer logremark = new StringBuffer();
        String coopName = this.getStr(order.getCustomerName(), bean.getCustomerName());
        logremark.append(StringUtils.isBlank(coopName) ? "" : "客户：" + coopName);
        String salesName = this.getStr(order.getSalesName(), bean.getSalesName());
        logremark.append(StringUtils.isBlank(salesName) ? "" : "销售：" + salesName);
        String mblNumber = this.getStr(order.getMblNumber(), bean.getMblNumber());
        logremark.append(StringUtils.isBlank(mblNumber) ? "" : "主单：" + mblNumber);
        String customerNumber = this.getStr(order.getCustomerNumber(), bean.getCustomerNumber());
        logremark.append(StringUtils.isBlank(customerNumber) ? "" : "合约号：" + customerNumber);
        String expectDeparture = this.getStr("" + order.getExpectDeparture(), "" + bean.getExpectDeparture());
        logremark.append(StringUtils.isBlank(expectDeparture) ? "" : "离港：" + expectDeparture);
        //船名/航次
        String orderStr1 = "空";
        String orderStr2 = "空";
        String beanStr1 = "空";
        String beanStr2 = "空";
        if (order.getShipName() != null && !"".equals(order.getShipName())) {
            orderStr1 = order.getShipName();
        }
        if (order.getShipVoyageNumber() != null && !"".equals(order.getShipVoyageNumber())) {
            orderStr2 = order.getShipVoyageNumber();
        }
        if (bean.getShipName() != null && !"".equals(bean.getShipName())) {
            beanStr1 = bean.getShipName();
        }
        if (bean.getShipVoyageNumber() != null && !"".equals(bean.getShipVoyageNumber())) {
            beanStr2 = bean.getShipVoyageNumber();
        }

        if (!(orderStr1 + "/" + orderStr2).equals(beanStr1 + "/" + beanStr2)) {
            logremark.append("船名/航次：" + orderStr1 + "/" + orderStr2 + " -> " + beanStr1 + "/" + beanStr2 + "  ");
        }


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
//    		orderStr22=""+order.getPlanWeight();
            orderStr22 = this.fmtMicrometer3(String.valueOf(order.getPlanWeight()));
        }
        if (order.getPlanVolume() != null || !"null".equals("" + order.getPlanVolume())) {
//    		orderStr33=""+order.getPlanVolume();
            orderStr33 = this.fmtMicrometer3(String.valueOf(order.getPlanVolume()));
        }


        if (bean.getPlanPieces() != null || !"null".equals("" + bean.getPlanPieces())) {
            beanStr11 = "" + bean.getPlanPieces();
        }
        if (bean.getPlanWeight() != null || !"null".equals("" + bean.getPlanWeight())) {
//    		beanStr22=""+bean.getPlanWeight();
            beanStr22 = this.fmtMicrometer3(String.valueOf(bean.getPlanWeight()));
        }
        if (bean.getPlanVolume() != null || !"null".equals("" + bean.getPlanVolume())) {
//    		beanStr33=""+bean.getPlanVolume();
            beanStr33 = this.fmtMicrometer3(String.valueOf(bean.getPlanVolume()));
        }

        if (!(orderStr11 + "/" + orderStr22 + "/" + orderStr33).equals(beanStr11 + "/" + beanStr22 + "/" + beanStr33)) {
            logremark.append("件/毛/体：" + orderStr11 + "/" + orderStr22 + "/" + orderStr33 + " -> " + beanStr11 + "/" + beanStr22 + "/" + beanStr33 + "  ");
        }

//    	String planPieces=this.getStr(""+order.getPlanPieces(),""+bean.getPlanPieces());
//    	logremark.append(StringUtils.isBlank(planPieces)?"":"预件："+planPieces);
//    	String planWeight=this.getStr(""+order.getPlanWeight(),""+bean.getPlanWeight());
//    	logremark.append(StringUtils.isBlank(planWeight)?"":"预毛："+planWeight);
//    	String planVolume=this.getStr(""+order.getPlanVolume(),""+bean.getPlanVolume());
//    	logremark.append(StringUtils.isBlank(planVolume)?"":"预体："+planVolume);

        String planChargeWeight = this.getStr("" + Double.parseDouble("" + order.getPlanChargeWeight()), this.fmtMicrometer3(String.valueOf(bean.getPlanChargeWeight())));
        logremark.append(StringUtils.isBlank(planChargeWeight) ? "" : "计费吨：" + planChargeWeight);


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

    @Override
    public Order getSITotal(Order order) {
        order.setCurrentUserId(SecurityUtils.getUser().getId());
        LambdaQueryWrapper<Order> wrapper = Wrappers.<Order>lambdaQuery();
        if (StrUtil.isNotBlank(order.getCustomerName())) {
            List<Integer> coopIds = remoteCoopService.listByCoopName(order.getCustomerName()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
            if (coopIds.size() == 0) {
                return null;
            }
            wrapper.in(Order::getCoopId, coopIds);
        }

        if (StrUtil.isNotBlank(order.getCustomerNumber())) {
            wrapper.like(Order::getCustomerNumber, "%" + order.getCustomerNumber() + "%");
        }

        if (order.getExpectArrivalStart() != null) {
            wrapper.ge(Order::getExpectArrival, order.getExpectArrivalStart());
        }
        if (order.getExpectArrivalEnd() != null) {
            wrapper.le(Order::getExpectArrival, order.getExpectArrivalEnd());
        }
        if (order.getCreateTimeBegin() != null) {
            wrapper.ge(Order::getCreateTime, order.getCreateTimeBegin());
        }
        if (order.getCreateTimeEnd() != null) {
            wrapper.le(Order::getCreateTime, order.getCreateTimeEnd());
        }
        //过滤强制关闭订单
        wrapper.ne(Order::getOrderStatus, "强制关闭");
        if ("未锁账".equals(order.getOrderStatus())) {
            wrapper.ne(Order::getOrderStatus, "财务锁账");
        }
        if ("已锁账".equals(order.getOrderStatus())) {
            wrapper.eq(Order::getOrderStatus, "财务锁账");
        }
        if (StrUtil.isNotBlank(order.getMblNumber())) {
            wrapper.like(Order::getMblNumber, "%" + order.getMblNumber() + "%");
        }
        if (StrUtil.isNotBlank(order.getHblNumber())) {
            wrapper.like(Order::getHblNumber, "%" + order.getHblNumber() + "%");
        }
        if (StrUtil.isNotBlank(order.getOrderCode())) {
            wrapper.like(Order::getOrderCode, "%" + order.getOrderCode() + "%");
        }
        if (StrUtil.isNotBlank(order.getServicerName())) {
            wrapper.like(Order::getServicerName, "%" + order.getServicerName() + "%");
        }
        if (StrUtil.isNotBlank(order.getSalesName())) {
            wrapper.like(Order::getSalesName, "%" + order.getSalesName() + "%");
        }
        if (StrUtil.isNotBlank(order.getCreatorName())) {
            wrapper.like(Order::getCreatorName, "%" + order.getCreatorName() + "%");
        }
        if (StrUtil.isNotBlank(order.getArrivalStation())) {
            wrapper.eq(Order::getArrivalStation, order.getArrivalStation());
        }
        if (StrUtil.isNotBlank(order.getDepartureStation())) {
            wrapper.eq(Order::getDepartureStation, order.getDepartureStation());
        }
        if (StrUtil.isNotBlank(order.getContainerMethod())) {
            wrapper.eq(Order::getContainerMethod, order.getContainerMethod());
        }
        if (order.getIncomeRecorded() != null && order.getIncomeRecorded()) {
            wrapper.eq(Order::getIncomeRecorded, order.getIncomeRecorded());
        }
        if (order.getIncomeRecorded() != null && !order.getIncomeRecorded()) {
            wrapper.and(i -> i.eq(Order::getIncomeRecorded, order.getIncomeRecorded()).or(j -> j.isNull(Order::getIncomeRecorded)));
        }
        if (order.getCostRecorded() != null && order.getCostRecorded()) {
            wrapper.eq(Order::getCostRecorded, order.getCostRecorded());
        }
        if (order.getCostRecorded() != null && !order.getCostRecorded()) {
            wrapper.and(i -> i.eq(Order::getCostRecorded, order.getCostRecorded()).or(j -> j.isNull(Order::getCostRecorded)));
        }
        if (order.getOrderPermission() == 1) {
            wrapper.and(i -> i.eq(Order::getCreatorId, order.getCurrentUserId()).or(j -> j.eq(Order::getSalesId, order.getCurrentUserId())).or(k -> k.eq(Order::getServicerId, order.getCurrentUserId())));
        }
        if (order.getOrderPermission() == 2) {
            List<Integer> WorkgroupIds = baseMapper.getWorkgroupIds(order.getCurrentUserId());
            wrapper.and(i -> i.eq(Order::getCreatorId, order.getCurrentUserId()).or(j -> j.eq(Order::getSalesId, order.getCurrentUserId())).or(k -> k.eq(Order::getServicerId, order.getCurrentUserId())).or(m -> m.in(Order::getWorkgroupId, WorkgroupIds)));
        }
        wrapper.eq(Order::getOrgId, SecurityUtils.getUser().getOrgId()).eq(Order::getBusinessScope, "SI");
        Order total = new Order();
        list(wrapper).stream().forEach(scOrder -> {
            total.setOrderStatus("合计:");
            //统计标箱数量
            if (total.getContainerNumber() == null) {
                total.setContainerNumber(scOrder.getContainerNumber() == null ? 0 : scOrder.getContainerNumber());
            } else {
                total.setContainerNumber(total.getContainerNumber() + (scOrder.getContainerNumber() == null ? 0 : scOrder.getContainerNumber()));
            }
            //统计件数
            if (total.getPlanPieces() == null) {
                total.setPlanPieces(scOrder.getPlanPieces() == null ? 0 : scOrder.getPlanPieces());
            } else {
                total.setPlanPieces(total.getPlanPieces() + (scOrder.getPlanPieces() == null ? 0 : scOrder.getPlanPieces()));
            }
            //统计毛重
            if (total.getPlanWeight() == null) {
                total.setPlanWeight(scOrder.getPlanWeight() == null ? BigDecimal.ZERO : scOrder.getPlanWeight());
            } else {
                total.setPlanWeight(total.getPlanWeight().add(scOrder.getPlanWeight() == null ? BigDecimal.ZERO : scOrder.getPlanWeight()));
            }
            //统计体积
            if (total.getPlanVolume() == null) {
                total.setPlanVolume(scOrder.getPlanVolume() == null ? BigDecimal.ZERO : scOrder.getPlanVolume());
            } else {
                total.setPlanVolume(total.getPlanVolume().add(scOrder.getPlanVolume() == null ? BigDecimal.ZERO : scOrder.getPlanVolume()));
            }
            //统计计重
            if (total.getPlanChargeWeight() == null) {
                total.setPlanChargeWeight(scOrder.getPlanChargeWeight() == null ? BigDecimal.ZERO : scOrder.getPlanChargeWeight());
            } else {
                total.setPlanChargeWeight(total.getPlanChargeWeight().add(scOrder.getPlanChargeWeight() == null ? BigDecimal.ZERO : scOrder.getPlanChargeWeight()));
            }
        });
        if (StrUtil.isBlank(total.getOrderStatus())) {
            return null;
        }
        total.setPlanWeightStr(FormatUtils.formatWithQWF(total.getPlanWeight(), 3));
        total.setPlanChargeWeightStr(FormatUtils.formatWithQWF(total.getPlanChargeWeight(), 3));
        total.setPlanVolumeStr(FormatUtils.formatWithQWF(total.getPlanVolume(), 3));
        return total;
    }

    @Override
    public Order getSETotal(Order order) {
        order.setCurrentUserId(SecurityUtils.getUser().getId());
        LambdaQueryWrapper<Order> wrapper = Wrappers.<Order>lambdaQuery();
        if (StrUtil.isNotBlank(order.getCustomerName())) {
            List<Integer> coopIds = remoteCoopService.listByCoopName(order.getCustomerName()).getData().stream().map(coopVo -> coopVo.getCoop_id()).collect(Collectors.toList());
            if (coopIds.size() == 0) {
                return null;
            }
            wrapper.in(Order::getCoopId, coopIds);
        }

        if (StrUtil.isNotBlank(order.getBillingType())) {
            wrapper.eq(Order::getBillingType, order.getBillingType());
        }

        if (StrUtil.isNotBlank(order.getCustomerNumber())) {
            wrapper.like(Order::getCustomerNumber, "%" + order.getCustomerNumber() + "%");
        }

        if (order.getExpectDepartureStart() != null) {
            wrapper.ge(Order::getExpectDeparture, order.getExpectDepartureStart());
        }
        if (order.getExpectArrivalEnd() != null) {
            wrapper.le(Order::getExpectDeparture, order.getExpectDepartureEnd());
        }
        if (order.getCreateTimeBegin() != null) {
            wrapper.ge(Order::getCreateTime, order.getCreateTimeBegin());
        }
        if (order.getCreateTimeEnd() != null) {
            wrapper.le(Order::getCreateTime, order.getCreateTimeEnd());
        }
        //过滤强制关闭订单
        wrapper.ne(Order::getOrderStatus, "强制关闭");
        if ("未锁账".equals(order.getOrderStatus())) {
            wrapper.ne(Order::getOrderStatus, "财务锁账");
        }
        if ("已锁账".equals(order.getOrderStatus())) {
            wrapper.eq(Order::getOrderStatus, "财务锁账");
        }
        if (StrUtil.isNotBlank(order.getMblNumber())) {
            wrapper.like(Order::getMblNumber, "%" + order.getMblNumber() + "%");
        }
        if (StrUtil.isNotBlank(order.getHblNumber())) {
            wrapper.like(Order::getHblNumber, "%" + order.getHblNumber() + "%");
        }
        if(StrUtil.isNotBlank(order.getBookingNumber())){
            wrapper.like(Order::getBookingNumber,order.getBookingNumber());
        }
        if (StrUtil.isNotBlank(order.getOrderCode())) {
            wrapper.like(Order::getOrderCode, "%" + order.getOrderCode() + "%");
        }
        if (StrUtil.isNotBlank(order.getServicerName())) {
            wrapper.like(Order::getServicerName, "%" + order.getServicerName() + "%");
        }
        if (StrUtil.isNotBlank(order.getSalesName())) {
            wrapper.like(Order::getSalesName, "%" + order.getSalesName() + "%");
        }
        if (StrUtil.isNotBlank(order.getCreatorName())) {
            wrapper.like(Order::getCreatorName, "%" + order.getCreatorName() + "%");
        }
        if (StrUtil.isNotBlank(order.getArrivalStation())) {
            wrapper.eq(Order::getArrivalStation, order.getArrivalStation());
        }
        if (StrUtil.isNotBlank(order.getDepartureStation())) {
            wrapper.eq(Order::getDepartureStation, order.getDepartureStation());
        }
        if (StrUtil.isNotBlank(order.getContainerMethod())) {
            wrapper.eq(Order::getContainerMethod, order.getContainerMethod());
        }
        if (order.getIncomeRecorded() != null && order.getIncomeRecorded()) {
            wrapper.eq(Order::getIncomeRecorded, order.getIncomeRecorded());
        }
        if (order.getIncomeRecorded() != null && !order.getIncomeRecorded()) {
            wrapper.and(i -> i.eq(Order::getIncomeRecorded, order.getIncomeRecorded()).or(j -> j.isNull(Order::getIncomeRecorded)));
        }
        if (order.getCostRecorded() != null && order.getCostRecorded()) {
            wrapper.eq(Order::getCostRecorded, order.getCostRecorded());
        }
        if (order.getCostRecorded() != null && !order.getCostRecorded()) {
            wrapper.and(i -> i.eq(Order::getCostRecorded, order.getCostRecorded()).or(j -> j.isNull(Order::getCostRecorded)));
        }
        if (order.getOrderPermission() == 1) {
            wrapper.and(i -> i.eq(Order::getCreatorId, order.getCurrentUserId()).or(j -> j.eq(Order::getSalesId, order.getCurrentUserId())).or(k -> k.eq(Order::getServicerId, order.getCurrentUserId())));
        }
        if (order.getOrderPermission() == 2) {
            List<Integer> WorkgroupIds = baseMapper.getWorkgroupIds(order.getCurrentUserId());
            wrapper.and(i -> i.eq(Order::getCreatorId, order.getCurrentUserId()).or(j -> j.eq(Order::getSalesId, order.getCurrentUserId())).or(k -> k.eq(Order::getServicerId, order.getCurrentUserId())).or(m -> m.in(Order::getWorkgroupId, WorkgroupIds)));
        }

        //订舱代理
        if (StrUtil.isNotBlank(order.getBookingAgentName())) {
            LambdaQueryWrapper<AfVPrmCoop> afVPrmCoopWrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
            afVPrmCoopWrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId()).like(AfVPrmCoop::getCoopName, "%" + order.getBookingAgentName() + "%");
            List<Integer> bookAgentIds = afVPrmCoopService.list(afVPrmCoopWrapper).stream().map(AfVPrmCoop::getCoopId).collect(Collectors.toList());
            if (bookAgentIds.size() == 0) {
                return null;
            }
            wrapper.in(Order::getBookingAgentId, bookAgentIds);
        }
        wrapper.eq(Order::getOrgId, SecurityUtils.getUser().getOrgId()).eq(Order::getBusinessScope, "SE");
        Order total = new Order();
        list(wrapper).stream().forEach(scOrder -> {
            total.setOrderStatus("合计:");
            //统计标箱数量
            if (total.getContainerNumber() == null) {
                total.setContainerNumber(scOrder.getContainerNumber() == null ? 0 : scOrder.getContainerNumber());
            } else {
                total.setContainerNumber(total.getContainerNumber() + (scOrder.getContainerNumber() == null ? 0 : scOrder.getContainerNumber()));
            }
            //统计件数
            if (total.getPlanPieces() == null) {
                total.setPlanPieces(scOrder.getPlanPieces() == null ? 0 : scOrder.getPlanPieces());
            } else {
                total.setPlanPieces(total.getPlanPieces() + (scOrder.getPlanPieces() == null ? 0 : scOrder.getPlanPieces()));
            }
            //统计毛重
            if (total.getPlanWeight() == null) {
                total.setPlanWeight(scOrder.getPlanWeight() == null ? BigDecimal.ZERO : scOrder.getPlanWeight());
            } else {
                total.setPlanWeight(total.getPlanWeight().add(scOrder.getPlanWeight() == null ? BigDecimal.ZERO : scOrder.getPlanWeight()));
            }
            //统计体积
            if (total.getPlanVolume() == null) {
                total.setPlanVolume(scOrder.getPlanVolume() == null ? BigDecimal.ZERO : scOrder.getPlanVolume());
            } else {
                total.setPlanVolume(total.getPlanVolume().add(scOrder.getPlanVolume() == null ? BigDecimal.ZERO : scOrder.getPlanVolume()));
            }
            //统计计重
            if (total.getPlanChargeWeight() == null) {
                total.setPlanChargeWeight(scOrder.getPlanChargeWeight() == null ? BigDecimal.ZERO : scOrder.getPlanChargeWeight());
            } else {
                total.setPlanChargeWeight(total.getPlanChargeWeight().add(scOrder.getPlanChargeWeight() == null ? BigDecimal.ZERO : scOrder.getPlanChargeWeight()));
            }
        });
        if (StrUtil.isBlank(total.getOrderStatus())) {
            return null;
        }
        total.setPlanWeightStr(FormatUtils.formatWithQWF(total.getPlanWeight(), 3));
        total.setPlanChargeWeightStr(FormatUtils.formatWithQWF(total.getPlanChargeWeight(), 3));
        total.setPlanVolumeStr(FormatUtils.formatWithQWF(total.getPlanVolume(), 3));
        return total;
    }

    @Override
    public void forceStopSI(Integer orderId, String reason) {
        //1.校验可否强制关闭
        checkIfForceStop(orderId);
        //2.执行强制关闭
        Order order = getById(orderId);
        order.setOrderStatus("强制关闭");
        order.setEditorId(SecurityUtils.getUser().getId());
        order.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        order.setEditTime(LocalDateTime.now());
        updateById(order);
        //日志
        Log logBean = new Log();
        logBean.setPageName("SI订单");
        logBean.setPageFunction("强制关闭");
        logBean.setBusinessScope("SI");
        logBean.setLogRemark(reason);
        logBean.setOrderNumber(order.getOrderCode());
        logBean.setOrderId(order.getOrderId());
        logBean.setOrderUuid(order.getOrderUuid());
        logService.saveLog(logBean);
        //HRS日志
        baseMapper.insertHrsLog("SI订单", "订单号:" + order.getOrderCode(),
                SecurityUtils.getUser().getId(), LocalDateTime.now(), SecurityUtils.getUser().getOrgId(), SecurityUtils.getUser().getDeptId());

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceStopSE(Integer orderId, String reason) {
        //1.校验可否强制关闭
        checkIfForceStop(orderId);
        //2.执行强制关闭
        Order order = getById(orderId);
        order.setOrderStatus("强制关闭");
        order.setEditorId(SecurityUtils.getUser().getId());
        order.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        order.setEditTime(LocalDateTime.now());
        updateById(order);

        //3.删除费用明细
        LambdaQueryWrapper<Income> incomeWrapper = Wrappers.<Income>lambdaQuery();
        LambdaQueryWrapper<Cost> costWrapper = Wrappers.<Cost>lambdaQuery();
        incomeWrapper.eq(Income::getOrgId, SecurityUtils.getUser().getOrgId()).eq(Income::getOrderId, orderId);
        costWrapper.eq(Cost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(Cost::getOrderId, orderId);
        incomeService.remove(incomeWrapper);
        costService.remove(costWrapper);
        //日志
        Log logBean = new Log();
        logBean.setPageName("SE订单");
        logBean.setPageFunction("强制关闭");
        logBean.setBusinessScope("SE");
        logBean.setLogRemark(reason);
        logBean.setOrderNumber(order.getOrderCode());
        logBean.setOrderId(order.getOrderId());
        logBean.setOrderUuid(order.getOrderUuid());
        logService.saveLog(logBean);
        //HRS日志
        baseMapper.insertHrsLog("SE订单", "订单号:" + order.getOrderCode(),
                SecurityUtils.getUser().getId(), LocalDateTime.now(), SecurityUtils.getUser().getOrgId(), SecurityUtils.getUser().getDeptId());


    }

    private void checkIfForceStop(Integer orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("无该订单,强制关闭失败");
        }
        if ("强制关闭".equals(order.getOrderStatus())) {
            throw new RuntimeException("订单状态为强制关闭,无法再次强制关闭");
        }

        if ("财务锁账".equals(order.getOrderStatus())) {
            throw new RuntimeException("订单状态为财务锁账,强制关闭失败");
        }
        LambdaQueryWrapper<Income> incomeWrapper = Wrappers.<Income>lambdaQuery();
        incomeWrapper.eq(Income::getOrgId, SecurityUtils.getUser().getOrgId()).eq(Income::getOrderId, orderId).isNotNull(Income::getDebitNoteId);
        List<Income> incomeList = incomeService.list(incomeWrapper);
        if (incomeList.size() != 0) {
            throw new RuntimeException("订单已应收对账,强制关闭失败");
        }
        List list = baseMapper.queryPaymentForIfForceStop(orderId, SecurityUtils.getUser().getOrgId());
        if (list.size() != 0) {
            throw new RuntimeException("订单已成本对账,强制关闭失败");
        }
    }

    private String createOrderCode(String businessScope) {
        String numberPrefix = businessScope + "-" + DateTimeFormatter.ofPattern("yyMMdd").format(LocalDate.now());
        LambdaQueryWrapper<Order> wrapper = Wrappers.<Order>lambdaQuery();
        wrapper.eq(Order::getOrgId, SecurityUtils.getUser().getOrgId()).like(Order::getOrderCode, "%" + numberPrefix + "%").orderByDesc(Order::getOrderCode).last(" limit 1");

        Order order = getOne(wrapper);

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

    /**
     * SE订单 分提单制作 采用存储过程数据源
     */
    @Override
    public String printHawMake(Integer orderId, String businessScope) throws IOException, DocumentException {
        //查询存储过程
        SeHawMakeProcedure seHawMakeProcedure = baseMapper.printHawMake(SecurityUtils.getUser().getOrgId(), orderId, businessScope);
        Assert.notNull(seHawMakeProcedure, "该订单无数据，不可制作分提单");

        String path = print(seHawMakeProcedure, false);
        ArrayList<String> newFilePaths = new ArrayList<>();
        newFilePaths.add(path);
        String lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/seOrder/" + new Date().getTime() + ".pdf";
        PDFUtils.loadAllPDFForFile(newFilePaths, lastFilePath, null);
        return lastFilePath.replace(PDFUtils.filePath, "");
    }

    /**
     * SE订单 订舱托书打印 采用存储过程数据源
     */
    @Override
    public String printOrderLetter(Integer orderId, String businessScope) throws IOException, DocumentException {
        //查询存储过程
        SeOrderLetterPrint seOrderLetterPrint = baseMapper.printOrderLetter(SecurityUtils.getUser().getOrgId(), orderId, businessScope);
        Assert.notNull(seOrderLetterPrint, "该订单无数据，无法打印订舱托书");

        String path = this.printOrderLetterInfo(seOrderLetterPrint);
        ArrayList<String> newFilePaths = new ArrayList<>();
        newFilePaths.add(path);
        String lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/seOrder/" + new Date().getTime() + ".pdf";
        PDFUtils.loadAllPDFForFile(newFilePaths, lastFilePath, null);
        return lastFilePath.replace(PDFUtils.filePath, "");
    }

    private String printOrderLetterInfo(SeOrderLetterPrint seOrderLetterPrint) throws IOException, DocumentException {
//    	String orderCode = seHawMakeProcedure.getInput032();
        String templatePath = PDFUtils.filePath + "/PDFtemplate/HBL_ORGINAL.pdf";
        String savePath = PDFUtils.filePath + "/PDFtemplate/temp/seOrder";

        //得到文件保存的名称
        String saveFilename = PDFUtils.makeFileName(new Date().getTime() + ".pdf");
        //得到文件的保存目录
        String newPDFPath = PDFUtils.makePath(saveFilename, savePath) + "/" + saveFilename;

        Map<String, String> valueData = new HashMap<>();

        valueData.put("Input001", seOrderLetterPrint.getInput001());
        valueData.put("Input002", seOrderLetterPrint.getInput002());
        valueData.put("Input003", seOrderLetterPrint.getInput003());
        valueData.put("Input004", seOrderLetterPrint.getInput004());
        valueData.put("Input008", seOrderLetterPrint.getInput008());
        valueData.put("Input009", seOrderLetterPrint.getInput009());
        valueData.put("Input010", seOrderLetterPrint.getInput010());
        valueData.put("Input012", seOrderLetterPrint.getInput012());
        valueData.put("Input013", seOrderLetterPrint.getInput013());
        valueData.put("Input014", seOrderLetterPrint.getInput014());
        valueData.put("Input015", seOrderLetterPrint.getInput015());
        valueData.put("Input016", seOrderLetterPrint.getInput016());
        valueData.put("Input020", seOrderLetterPrint.getInput020());
        valueData.put("Input035", seOrderLetterPrint.getInput035());
        valueData.put("Input036", seOrderLetterPrint.getInput036());
        valueData.put("Input037", seOrderLetterPrint.getInput037());
        valueData.put("Input039", seOrderLetterPrint.getInput039());
        valueData.put("Input044", seOrderLetterPrint.getInput044());
        valueData.put("Input045", seOrderLetterPrint.getInput045());
        valueData.put("Input046", seOrderLetterPrint.getInput046());
        valueData.put("Input047", seOrderLetterPrint.getInput047());
        //填充每个PDF
        PDFUtils.loadPDF2(templatePath, newPDFPath, valueData, true, false);
        return newPDFPath;
    }

    public String print(SeHawMakeProcedure seHawMakeProcedure, boolean flag) throws IOException, DocumentException {
        return fillTemplate(seHawMakeProcedure, flag ? PDFUtils.filePath : "");
    }

    public static String fillTemplate(SeHawMakeProcedure seHawMakeProcedure, String replacePath) throws IOException, DocumentException {

        String orderCode = seHawMakeProcedure.getInput032();
        String templatePath = PDFUtils.filePath + "/PDFtemplate/HBL_ORGINAL.pdf";
        String savePath = PDFUtils.filePath + "/PDFtemplate/temp/seOrder";

        //得到文件保存的名称
        String saveFilename = PDFUtils.makeFileName(orderCode + ".pdf");
        //得到文件的保存目录
        String newPDFPath = PDFUtils.makePath(saveFilename, savePath) + "/" + saveFilename;

        Map<String, String> valueData = new HashMap<>();

        valueData.put("Input001", seHawMakeProcedure.getInput001());
        valueData.put("Input002", seHawMakeProcedure.getInput002());
        valueData.put("Input003", seHawMakeProcedure.getInput003());
        valueData.put("Input004", seHawMakeProcedure.getInput004());
        valueData.put("Input005", seHawMakeProcedure.getInput005());
        valueData.put("Input006", seHawMakeProcedure.getInput006());
        valueData.put("Input007", seHawMakeProcedure.getInput007());
        valueData.put("Input008", seHawMakeProcedure.getInput008());
        valueData.put("Input009", seHawMakeProcedure.getInput009());
        valueData.put("Input010", seHawMakeProcedure.getInput010());
        valueData.put("Input011", seHawMakeProcedure.getInput011());
        valueData.put("Input012", seHawMakeProcedure.getInput012());
        valueData.put("Input013", seHawMakeProcedure.getInput013());
        valueData.put("Input014", seHawMakeProcedure.getInput014());
        valueData.put("Input015", seHawMakeProcedure.getInput015());
        valueData.put("Input016", seHawMakeProcedure.getInput016());
        valueData.put("Input017", seHawMakeProcedure.getInput017());
        valueData.put("Input018", seHawMakeProcedure.getInput018());
        valueData.put("Input019", seHawMakeProcedure.getInput019());
        valueData.put("Input020", seHawMakeProcedure.getInput020());
        valueData.put("Input021", seHawMakeProcedure.getInput021());
        valueData.put("Input022", seHawMakeProcedure.getInput022());
        valueData.put("Input023", seHawMakeProcedure.getInput023());
        valueData.put("Input024", seHawMakeProcedure.getInput024());
        valueData.put("Input025", seHawMakeProcedure.getInput025());
        valueData.put("Input026", seHawMakeProcedure.getInput026());
        valueData.put("Input027", seHawMakeProcedure.getInput027());
        valueData.put("Input028", seHawMakeProcedure.getInput028());
        valueData.put("Input029", seHawMakeProcedure.getInput029());
        valueData.put("Input030", seHawMakeProcedure.getInput030());
        valueData.put("Input031", seHawMakeProcedure.getInput031());
        valueData.put("Input032", seHawMakeProcedure.getInput032());
        valueData.put("Input033", seHawMakeProcedure.getInput033());
        valueData.put("Input034", seHawMakeProcedure.getInput034());
        valueData.put("Input035", seHawMakeProcedure.getInput035());
        valueData.put("Input036", seHawMakeProcedure.getInput036());
        valueData.put("Input037", seHawMakeProcedure.getInput037());
        valueData.put("Input038", seHawMakeProcedure.getInput038());
        valueData.put("Input039", seHawMakeProcedure.getInput039());
        valueData.put("Input040", seHawMakeProcedure.getInput040());
        valueData.put("Input041", seHawMakeProcedure.getInput041());
        valueData.put("Input042", seHawMakeProcedure.getInput042());
        valueData.put("Input043", seHawMakeProcedure.getInput043());
        valueData.put("Input044", seHawMakeProcedure.getInput044());
        valueData.put("Input045", seHawMakeProcedure.getInput045());
        valueData.put("Input046", seHawMakeProcedure.getInput046());
        valueData.put("Input047", seHawMakeProcedure.getInput047());
        valueData.put("Input048", seHawMakeProcedure.getInput048());
        valueData.put("Input049", seHawMakeProcedure.getInput049());
        valueData.put("Input050", seHawMakeProcedure.getInput050());
        valueData.put("Input051", seHawMakeProcedure.getInput051());
        valueData.put("Input052", seHawMakeProcedure.getInput052());
        valueData.put("Input053", seHawMakeProcedure.getInput053());
        valueData.put("Input054", seHawMakeProcedure.getInput054());
        valueData.put("Input055", seHawMakeProcedure.getInput055());
        valueData.put("Input056", seHawMakeProcedure.getInput056());
        valueData.put("Input057", seHawMakeProcedure.getInput057());
        valueData.put("Input058", seHawMakeProcedure.getInput058());
        valueData.put("Input059", seHawMakeProcedure.getInput059());
        valueData.put("Input060", seHawMakeProcedure.getInput060());

        //填充每个PDF
        PDFUtils.loadPDF2(templatePath, newPDFPath, valueData, true, false);
        return newPDFPath.replace(replacePath, "");
    }

    @Override
    public void exportHawMakeExcel(Integer orderId, String businessScope) {

        String templateFilePath = PDFUtils.filePath + "/PDFtemplate/HBL_ORGINAL.xlsx";
        //查询存储过程
        SeHawMakeProcedure seHawMakeProcedure = baseMapper.printHawMake(SecurityUtils.getUser().getOrgId(), orderId, businessScope);
        if (seHawMakeProcedure != null) {
            HashMap<String, Object> context = new HashMap<>();
            context.put("data", seHawMakeProcedure);
            JxlsUtils.exportExcelWithLocalModel(templateFilePath, context);

        } else {
            throw new RuntimeException("该订单无数据，不可导出");
        }
    }

    @Override
    public void exportOrderLetterExcel(Integer orderId, String businessScope) {

        String templateFilePath = PDFUtils.filePath + "/PDFtemplate/ORDER_LETTER_EXCEL.xlsx";
        //查询存储过程
        SeOrderLetterPrint seOrderLetterPrint = baseMapper.printOrderLetter(SecurityUtils.getUser().getOrgId(), orderId, businessScope);
        if (seOrderLetterPrint != null) {
            HashMap<String, Object> context = new HashMap<>();
            if (StrUtil.isNotEmpty(seOrderLetterPrint.getOrgLogo())) {
                String imagePath = PDFUtils.filePath + "/PDFtemplate/temp/img/orderLetter/" + UUID.randomUUID().toString() + "/" + seOrderLetterPrint.getOrgLogo().substring(seOrderLetterPrint.getOrgLogo().lastIndexOf("/") + 1, seOrderLetterPrint.getOrgLogo().length());
                downloadFile(seOrderLetterPrint.getOrgLogo(), imagePath);
                try {
                    byte[] imageBytes = Util.toByteArray(new FileInputStream(imagePath));
                    context.put("org_logo", imageBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            context.put("data", seOrderLetterPrint);
            JxlsUtils.exportExcelWithLocalModel(templateFilePath, context);

        } else {
            throw new RuntimeException("该订单无数据，不可导出");
        }
    }

    @Override
    public void exportTrailerPrintExcel(Integer orderId, String businessScope) {

        String templateFilePath = PDFUtils.filePath + "/PDFtemplate/SE_trailer_letter.xlsx";
        //查询存储过程
        SeTrailerPrint seTrailerPrint = baseMapper.exportTrailerPrintExcel(SecurityUtils.getUser().getOrgId(), orderId, businessScope);
        if (seTrailerPrint != null) {
            seTrailerPrint.setInput058(SecurityUtils.getUser().getUserCname());
            seTrailerPrint.setInput059(SecurityUtils.getUser().getPhoneNumber());
            seTrailerPrint.setInput060(SecurityUtils.getUser().getUserEmail());
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            seTrailerPrint.setInput061(df.format(new Date()));
            HashMap<String, Object> context = new HashMap<>();
            if (StrUtil.isNotEmpty(seTrailerPrint.getOrgLogo())) {
                String imagePath = PDFUtils.filePath + "/PDFtemplate/temp/img/orderLetter/" + UUID.randomUUID().toString() + "/" + seTrailerPrint.getOrgLogo().substring(seTrailerPrint.getOrgLogo().lastIndexOf("/") + 1, seTrailerPrint.getOrgLogo().length());
                downloadFile(seTrailerPrint.getOrgLogo(), imagePath);
                try {
                    byte[] imageBytes = Util.toByteArray(new FileInputStream(imagePath));
                    context.put("org_logo", imageBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            context.put("data", seTrailerPrint);
            JxlsUtils.exportExcelWithLocalModel(templateFilePath, context);

        } else {
            throw new RuntimeException("该订单无数据，不可导出");
        }
    }

    @Override
    public void exportNoticeArrivalExcel(Integer orderId, String businessScope) {

        String templateFilePath = PDFUtils.filePath + "/PDFtemplate/SI_notice_arrival.xlsx";
        //查询存储过程
        SeTrailerPrint seTrailerPrint = baseMapper.exportNoticeArrivalExcel(SecurityUtils.getUser().getOrgId(), orderId, businessScope);
        if (seTrailerPrint != null) {
            seTrailerPrint.setInput058(SecurityUtils.getUser().getUserCname());
            seTrailerPrint.setInput059(SecurityUtils.getUser().getPhoneNumber());
            seTrailerPrint.setInput060(SecurityUtils.getUser().getUserEmail());
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            seTrailerPrint.setInput061(df.format(new Date()));
            //设置船名船次
            if ("/".equals(seTrailerPrint.getInput009())) {
                seTrailerPrint.setInput009("");
            } else if (seTrailerPrint.getInput009().startsWith("/") || seTrailerPrint.getInput009().endsWith("/")) {
                seTrailerPrint.setInput009(seTrailerPrint.getInput009().replace("/", ""));
            }
            HashMap<String, Object> context = new HashMap<>();
            if (StrUtil.isNotEmpty(seTrailerPrint.getOrgLogo())) {
                String imagePath = PDFUtils.filePath + "/PDFtemplate/temp/img/orderLetter/" + UUID.randomUUID().toString() + "/" + seTrailerPrint.getOrgLogo().substring(seTrailerPrint.getOrgLogo().lastIndexOf("/") + 1, seTrailerPrint.getOrgLogo().length());
                downloadFile(seTrailerPrint.getOrgLogo(), imagePath);
                try {
                    byte[] imageBytes = Util.toByteArray(new FileInputStream(imagePath));
                    context.put("org_logo", imageBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            context.put("data", seTrailerPrint);
            JxlsUtils.exportExcelWithLocalModel(templateFilePath, context);

        } else {
            throw new RuntimeException("该订单无数据，不可导出");
        }
    }

    @Override
    public List<OrderForVL> getSCOrderListForVL(OrderForVL orderForVL) {
        LambdaQueryWrapper<Order> wrapper = Wrappers.<Order>lambdaQuery();
        if (StrUtil.isNotBlank(orderForVL.getOrderCode())) {
            wrapper.like(Order::getOrderCode, orderForVL.getOrderCode());
        }

        if (StrUtil.isNotBlank(orderForVL.getAwbNumber())) {
            wrapper.like(Order::getMblNumber, orderForVL.getAwbNumber());
        }

        if (StrUtil.isNotBlank(orderForVL.getCustomerNumber())) {
            wrapper.like(Order::getCustomerNumber, orderForVL.getCustomerNumber());
        }

        if (orderForVL.getBusinessScope().equals("SE") && orderForVL.getFlightDateStart() != null) {
            wrapper.ge(Order::getExpectDeparture, orderForVL.getFlightDateStart());
        }
        if (orderForVL.getBusinessScope().equals("SE") && orderForVL.getFlightDateEnd() != null) {
            wrapper.le(Order::getExpectDeparture, orderForVL.getFlightDateEnd());
        }
        if (orderForVL.getBusinessScope().equals("SI") && orderForVL.getFlightDateStart() != null) {
            wrapper.ge(Order::getExpectArrival, orderForVL.getFlightDateStart());
        }
        if (orderForVL.getBusinessScope().equals("SI") && orderForVL.getFlightDateEnd() != null) {
            wrapper.le(Order::getExpectArrival, orderForVL.getFlightDateEnd());
        }
        if (StrUtil.isNotBlank(orderForVL.getNoOrderIds())) {
            wrapper.notIn(Order::getOrderId, orderForVL.getNoOrderIds().split(","));
        }
        wrapper.eq(Order::getOrgId, SecurityUtils.getUser().getOrgId()).eq(Order::getBusinessScope, orderForVL.getBusinessScope()).ne(Order::getContainerMethod, "整箱").ne(Order::getCostRecorded, true).notIn(Order::getOrderStatus, "强制关闭", "财务锁账");
        if (orderForVL.getBusinessScope().equals("SE")) {
            wrapper.orderByAsc(Order::getExpectDeparture, Order::getMblNumber);
        } else {
            wrapper.orderByAsc(Order::getExpectArrival, Order::getMblNumber);
        }
        return list(wrapper).stream().map(scOrder -> {
            OrderForVL order = new OrderForVL();
            BeanUtils.copyProperties(scOrder, order);
            order.setAwbNumber(scOrder.getMblNumber());
            if (order.getPlanChargeWeight() != null) {
                order.setPlanChargeWeight(order.getPlanChargeWeight().multiply(BigDecimal.valueOf(1000)));
            }
            if (orderForVL.getBusinessScope().equals("SE")) {
                order.setFlightDate(scOrder.getExpectDeparture());
            } else {
                order.setFlightDate(scOrder.getExpectArrival());
            }
            return order;
        }).collect(Collectors.toList());
    }

    public static boolean downloadFile(String fileURL, String fileName) {
        try {
            String path = fileName.substring(0, fileName.lastIndexOf("/"));
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            URL url = new URL(fileURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName));
            byte[] buffer = new byte[4096];
            int count = 0;
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
            out.close();
            in.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
