package com.efreight.sc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.core.utils.JxlsUtils;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.dao.WaybillPrintMapper;
import com.efreight.sc.entity.*;
import com.efreight.sc.entity.view.VScCategory;
import com.efreight.sc.service.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * CS 订单管理 海运制单 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-08-17
 */
@Service
@AllArgsConstructor
public class WaybillPrintServiceImpl extends ServiceImpl<WaybillPrintMapper, WaybillPrint> implements WaybillPrintService {

    private final OrderService orderService;

    private final WaybillPrintDetailsService waybillPrintDetailsService;

    private final WaybillPrintShipperConsigneeService waybillPrintShipperConsigneeService;

    private final OrderShipperConsigneeService orderShipperConsigneeService;

    private final VScCategoryService vScCategoryService;

    @Override
    public List<Map<String, String>> getList(Integer orderId) {
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单已不存在,请刷新页面");
        }
        if ("强制关闭".equals(order.getOrderStatus())) {
            throw new RuntimeException("订单已经强制关闭,无法运单制作,请刷新页面");
        }
        if (StrUtil.isBlank(order.getMblNumber())) {
            throw new RuntimeException("订单未配置主提单号,请配置好主提单号后再进行操作");
        }
        ArrayList<Map<String, String>> list = new ArrayList<>();
        HashMap<String, String> mblMap = new HashMap<>();
        mblMap.put("orderId", order.getMblNumber());
        mblMap.put("blNumber", order.getMblNumber());
        list.add(mblMap);

        LambdaQueryWrapper<Order> wrapper = Wrappers.<Order>lambdaQuery();
        wrapper.eq(Order::getBusinessScope, "SE").eq(Order::getMblNumber, order.getMblNumber()).eq(Order::getOrgId, SecurityUtils.getUser().getOrgId()).ne(Order::getOrderStatus, "强制关闭").ne(Order::getHblNumber, "").isNotNull(Order::getHblNumber);
        List<Order> orderList = orderService.list(wrapper);
        ArrayList<Map<String, String>> hblList = new ArrayList<>();
        orderList.stream().forEach(orderInfo -> {
            HashMap<String, String> orderMap = new HashMap<>();
            orderMap.put("orderId", orderInfo.getOrderId().toString());
            orderMap.put("blNumber", orderInfo.getHblNumber());
            orderMap.put("orderCode", orderInfo.getOrderCode());
            hblList.add(orderMap);
        });

        //检查分单重复
        HashMap<String, Integer> indexMap = new HashMap<>();
        indexMap.put("index", 0);
        HashMap<String, String> checkRepeatMap = new HashMap<>();
        hblList.stream().forEach(map -> {
            if (checkRepeatMap.get(map.get("blNumber")) == null) {
                checkRepeatMap.put(map.get("blNumber"), indexMap.get("index").toString());
            } else {
                checkRepeatMap.put(map.get("blNumber"), checkRepeatMap.get(map.get("blNumber")) + "," + indexMap.get("index").toString());
            }
            indexMap.put("index", indexMap.get("index") + 1);
        });
        for (Map.Entry<String, String> entry : checkRepeatMap.entrySet()) {
            if (entry.getValue().length() > 1) {
                Arrays.asList(entry.getValue().split(",")).stream().forEach(index -> {
                    Map<String, String> map = hblList.get(Integer.parseInt(index));
                    map.put("blNumber", map.get("blNumber") + " (" + map.get("orderCode") + ")");
                });
            }
        }
        list.addAll(hblList);
        return list;
    }

    @Override
    public WaybillPrint view(String orderIdOrMblNumber, String flag) {
        LambdaQueryWrapper<WaybillPrint> wrapper = Wrappers.<WaybillPrint>lambdaQuery();
        if ("MBL".equals(flag)) {
            wrapper.eq(WaybillPrint::getMblNumber, orderIdOrMblNumber);
        } else if ("HBL".equals(flag)) {
            wrapper.eq(WaybillPrint::getOrderId, orderIdOrMblNumber);
        } else {
            throw new RuntimeException("无法识别标记");
        }
        wrapper.eq(WaybillPrint::getOrgId, SecurityUtils.getUser().getOrgId());
        WaybillPrint waybillPrint = getOne(wrapper);
        if (waybillPrint == null) {
            //调用存储过程初始化基础数据
            waybillPrint = baseMapper.initData(orderIdOrMblNumber, SecurityUtils.getUser().getOrgId(), flag);

            //初始化收发货人
            WaybillPrintShipperConsignee shipper = new WaybillPrintShipperConsignee();
            WaybillPrintShipperConsignee consignee = new WaybillPrintShipperConsignee();
            if ("MBL".equals(flag)) {
                shipper.setScType(0);
                consignee.setScType(1);
            } else if ("HBL".equals(flag)) {
                LambdaQueryWrapper<OrderShipperConsignee> orderShipperLambdaQueryWrapper = Wrappers.<OrderShipperConsignee>lambdaQuery();
                orderShipperLambdaQueryWrapper.eq(OrderShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrderShipperConsignee::getOrderId, orderIdOrMblNumber).eq(OrderShipperConsignee::getScType, 0);
                OrderShipperConsignee orderShipper = orderShipperConsigneeService.getOne(orderShipperLambdaQueryWrapper);
                if (orderShipper != null) {
                    BeanUtils.copyProperties(orderShipper, shipper);
                    waybillPrint.setShipperPrint(orderShipper.getScPrintRemark());
                } else {
                    shipper.setScType(0);
                }
                LambdaQueryWrapper<OrderShipperConsignee> orderConsigneeLambdaQueryWrapper = Wrappers.<OrderShipperConsignee>lambdaQuery();
                orderConsigneeLambdaQueryWrapper.eq(OrderShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrderShipperConsignee::getOrderId, orderIdOrMblNumber).eq(OrderShipperConsignee::getScType, 1);
                OrderShipperConsignee orderConsigee = orderShipperConsigneeService.getOne(orderConsigneeLambdaQueryWrapper);
                if (orderConsigee != null) {
                    BeanUtils.copyProperties(orderConsigee, consignee);
                    waybillPrint.setConsigneePrint(orderConsigee.getScPrintRemark());
                } else {
                    consignee.setScType(1);
                }
            }
            waybillPrint.setShipper(shipper);
            waybillPrint.setConsignee(consignee);

            //初始化运单制作明细
            ArrayList<WaybillPrintDetails> waybillPrintDetailsList = new ArrayList<>();
            if ("HBL".equals(flag)) {
                Order order = orderService.getById(orderIdOrMblNumber);
                WaybillPrintDetails waybillPrintDetails = new WaybillPrintDetails();
                waybillPrintDetails.setContainerSealNo(order.getShippingMarks());
                waybillPrintDetails.setNumber(order.getPlanPieces() == null ? "" : order.getPlanPieces().toString());
                LambdaQueryWrapper<VScCategory> vScCategoryLambdaQueryWrapper = Wrappers.<VScCategory>lambdaQuery();
                vScCategoryLambdaQueryWrapper.eq(VScCategory::getCategoryName, "包装类型").eq(VScCategory::getParamText, order.getPackageType());
                VScCategory vScCategory = vScCategoryService.getOne(vScCategoryLambdaQueryWrapper);
                waybillPrintDetails.setKindOfPackage(vScCategory.getEDICode1());
                waybillPrintDetails.setDescriptionOfGoods(order.getGoodsNameEn());
                waybillPrintDetails.setGrossWeight(order.getPlanWeight() == null ? "" : (FormatUtils.formatWithQWFNoBit(order.getPlanWeight()) + "(KG)"));
                waybillPrintDetails.setVolume(order.getPlanVolume() == null ? "" : (FormatUtils.formatWithQWFNoBit(order.getPlanVolume()) + "(CBM)"));
                waybillPrintDetailsList.add(waybillPrintDetails);
            }
            waybillPrint.setWaybillPrintDetailsList(waybillPrintDetailsList);
        } else {
            LambdaQueryWrapper<WaybillPrintShipperConsignee> waybillPrintShipperLambdaQueryWrapper = Wrappers.<WaybillPrintShipperConsignee>lambdaQuery();
            waybillPrintShipperLambdaQueryWrapper.eq(WaybillPrintShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).eq(WaybillPrintShipperConsignee::getWaybillPrintId, waybillPrint.getWaybillPrintId()).eq(WaybillPrintShipperConsignee::getScType, 0);
            WaybillPrintShipperConsignee shipper = waybillPrintShipperConsigneeService.getOne(waybillPrintShipperLambdaQueryWrapper);
            waybillPrint.setShipper(shipper);
            LambdaQueryWrapper<WaybillPrintShipperConsignee> waybillPrintConsigneeLambdaQueryWrapper = Wrappers.<WaybillPrintShipperConsignee>lambdaQuery();
            waybillPrintConsigneeLambdaQueryWrapper.eq(WaybillPrintShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).eq(WaybillPrintShipperConsignee::getWaybillPrintId, waybillPrint.getWaybillPrintId()).eq(WaybillPrintShipperConsignee::getScType, 1);
            WaybillPrintShipperConsignee consignee = waybillPrintShipperConsigneeService.getOne(waybillPrintConsigneeLambdaQueryWrapper);
            waybillPrint.setConsignee(consignee);

            LambdaQueryWrapper<WaybillPrintDetails> waybillPrintDetailsLambdaQueryWrapper = Wrappers.<WaybillPrintDetails>lambdaQuery();
            waybillPrintDetailsLambdaQueryWrapper.eq(WaybillPrintDetails::getOrgId, SecurityUtils.getUser().getOrgId()).eq(WaybillPrintDetails::getWaybillPrintId, waybillPrint.getWaybillPrintId());
            List<WaybillPrintDetails> waybillPrintDetailsList = waybillPrintDetailsService.list(waybillPrintDetailsLambdaQueryWrapper);
            waybillPrint.setWaybillPrintDetailsList(waybillPrintDetailsList);

            //分单实时获取订单中的blNumber信息mblNumber_hblNumber
            if ("HBL".equals(flag)) {
                Order order = orderService.getById(orderIdOrMblNumber);
                waybillPrint.setBlNumber(order.getHblNumber());
            }

        }
        return waybillPrint;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(WaybillPrint waybillPrint) {

        //校验
        LambdaQueryWrapper<WaybillPrint> wrapper = Wrappers.<WaybillPrint>lambdaQuery();
        if (StrUtil.isNotBlank(waybillPrint.getMblNumber())) {
            //主单新增
            wrapper.eq(WaybillPrint::getMblNumber, waybillPrint.getMblNumber()).eq(WaybillPrint::getOrgId, SecurityUtils.getUser().getOrgId());
            WaybillPrint one = getOne(wrapper);
            if (one != null) {
                throw new RuntimeException("该主单已制单,无法再次制单");
            }
        } else if (waybillPrint.getOrderId() != null) {
            //分单新增
            wrapper.eq(WaybillPrint::getOrderId, waybillPrint.getOrderId()).eq(WaybillPrint::getOrgId, SecurityUtils.getUser().getOrgId());
            WaybillPrint one = getOne(wrapper);
            if (one != null) {
                throw new RuntimeException("该分单已制单,无法再次制单");
            }
            waybillPrint.setBlNumber(null);
        } else {
            throw new RuntimeException("缺失必要数据,保存失败");
        }
        waybillPrint.setCreateTime(LocalDateTime.now());
        waybillPrint.setCreatorId(SecurityUtils.getUser().getId());
        waybillPrint.setCreatorName(SecurityUtils.getUser().buildOptName());
        waybillPrint.setEditorId(SecurityUtils.getUser().getId());
        waybillPrint.setEditorName(SecurityUtils.getUser().buildOptName());
        waybillPrint.setEditTime(waybillPrint.getCreateTime());
        waybillPrint.setBusinessScope("SE");
        waybillPrint.setOrgId(SecurityUtils.getUser().getOrgId());
        save(waybillPrint);

        //保存明细
        if (!waybillPrint.getWaybillPrintDetailsList().isEmpty()) {
            HashMap<String, Integer> noMap = new HashMap<>();
            noMap.put("no", 1);
            waybillPrint.getWaybillPrintDetailsList().stream().forEach(waybillPrintDetails -> {
                waybillPrintDetails.setWaybillPrintId(waybillPrint.getWaybillPrintId());
                waybillPrintDetails.setOrgId(SecurityUtils.getUser().getOrgId());
                waybillPrintDetails.setWaybillPrintDetailNo(noMap.get("no"));
                noMap.put("no", noMap.get("no") + 1);
            });
            waybillPrintDetailsService.saveBatch(waybillPrint.getWaybillPrintDetailsList());
        }

        //保存收发货人

        WaybillPrintShipperConsignee shipper = waybillPrint.getShipper();
        shipper.setCreateTime(LocalDateTime.now());
        shipper.setEditTime(LocalDateTime.now());
        shipper.setCreatorId(SecurityUtils.getUser().getId());
        shipper.setEditorId(SecurityUtils.getUser().getId());
        shipper.setCreatorName(SecurityUtils.getUser().buildOptName());
        shipper.setEditorName(SecurityUtils.getUser().buildOptName());
        shipper.setOrgId(SecurityUtils.getUser().getOrgId());
        shipper.setWaybillPrintId(waybillPrint.getWaybillPrintId());
        shipper.setScType(0);
        waybillPrintShipperConsigneeService.save(shipper);

        WaybillPrintShipperConsignee consignee = waybillPrint.getConsignee();
        consignee.setCreateTime(LocalDateTime.now());
        consignee.setEditTime(LocalDateTime.now());
        consignee.setCreatorId(SecurityUtils.getUser().getId());
        consignee.setEditorId(SecurityUtils.getUser().getId());
        consignee.setCreatorName(SecurityUtils.getUser().buildOptName());
        consignee.setEditorName(SecurityUtils.getUser().buildOptName());
        consignee.setOrgId(SecurityUtils.getUser().getOrgId());
        consignee.setWaybillPrintId(waybillPrint.getWaybillPrintId());
        consignee.setScType(1);
        waybillPrintShipperConsigneeService.save(consignee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modify(WaybillPrint waybillPrint) {
        if (waybillPrint.getOrderId() != null) {
            //分单修改
            waybillPrint.setBlNumber(null);
        }
        waybillPrint.setEditorId(SecurityUtils.getUser().getId());
        waybillPrint.setEditorName(SecurityUtils.getUser().buildOptName());
        waybillPrint.setEditTime(LocalDateTime.now());

        updateById(waybillPrint);

        //修改明细
        LambdaQueryWrapper<WaybillPrintDetails> waybillPrintDetailsWrapper = Wrappers.<WaybillPrintDetails>lambdaQuery();
        waybillPrintDetailsWrapper.eq(WaybillPrintDetails::getWaybillPrintId, waybillPrint.getWaybillPrintId()).eq(WaybillPrintDetails::getOrgId, SecurityUtils.getUser().getOrgId());
        waybillPrintDetailsService.remove(waybillPrintDetailsWrapper);
        if (!waybillPrint.getWaybillPrintDetailsList().isEmpty()) {
            HashMap<String, Integer> noMap = new HashMap<>();
            noMap.put("no", 1);
            waybillPrint.getWaybillPrintDetailsList().stream().forEach(waybillPrintDetails -> {
                waybillPrintDetails.setWaybillPrintId(waybillPrint.getWaybillPrintId());
                waybillPrintDetails.setOrgId(SecurityUtils.getUser().getOrgId());
                waybillPrintDetails.setWaybillPrintDetailNo(noMap.get("no"));
                noMap.put("no", noMap.get("no") + 1);
            });
            waybillPrintDetailsService.saveBatch(waybillPrint.getWaybillPrintDetailsList());
        }
        //修改收发货人
        LambdaQueryWrapper<WaybillPrintShipperConsignee> waybillPrintShipperLambdaQueryWrapper = Wrappers.<WaybillPrintShipperConsignee>lambdaQuery();
        waybillPrintShipperLambdaQueryWrapper.eq(WaybillPrintShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).eq(WaybillPrintShipperConsignee::getWaybillPrintId, waybillPrint.getWaybillPrintId()).eq(WaybillPrintShipperConsignee::getScType, 0);
        WaybillPrintShipperConsignee shipper = waybillPrintShipperConsigneeService.getOne(waybillPrintShipperLambdaQueryWrapper);
        shipper.setAeoCode(waybillPrint.getShipper().getAeoCode());
        shipper.setCityCode(waybillPrint.getShipper().getCityCode());
        shipper.setFaxNumber(waybillPrint.getShipper().getFaxNumber());
        shipper.setNationCode(waybillPrint.getShipper().getNationCode());
        shipper.setPostCode(waybillPrint.getShipper().getPostCode());
        shipper.setScAddress(waybillPrint.getShipper().getScAddress());
        shipper.setScCode(waybillPrint.getShipper().getScCode());
        shipper.setScCodeType(waybillPrint.getShipper().getScCodeType());
        shipper.setScName(waybillPrint.getShipper().getScName());
        shipper.setStateCode(waybillPrint.getShipper().getStateCode());
        shipper.setTelNumber(waybillPrint.getShipper().getTelNumber());
        shipper.setEditorId(SecurityUtils.getUser().getId());
        shipper.setEditorName(SecurityUtils.getUser().buildOptName());
        shipper.setEditTime(LocalDateTime.now());
        waybillPrintShipperConsigneeService.updateById(shipper);
        LambdaQueryWrapper<WaybillPrintShipperConsignee> waybillPrintConsigneeLambdaQueryWrapper = Wrappers.<WaybillPrintShipperConsignee>lambdaQuery();
        waybillPrintConsigneeLambdaQueryWrapper.eq(WaybillPrintShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).eq(WaybillPrintShipperConsignee::getWaybillPrintId, waybillPrint.getWaybillPrintId()).eq(WaybillPrintShipperConsignee::getScType, 1);
        WaybillPrintShipperConsignee consignee = waybillPrintShipperConsigneeService.getOne(waybillPrintConsigneeLambdaQueryWrapper);
        consignee.setAeoCode(waybillPrint.getConsignee().getAeoCode());
        consignee.setCityCode(waybillPrint.getConsignee().getCityCode());
        consignee.setFaxNumber(waybillPrint.getConsignee().getFaxNumber());
        consignee.setNationCode(waybillPrint.getConsignee().getNationCode());
        consignee.setPostCode(waybillPrint.getConsignee().getPostCode());
        consignee.setScAddress(waybillPrint.getConsignee().getScAddress());
        consignee.setScCode(waybillPrint.getConsignee().getScCode());
        consignee.setScCodeType(waybillPrint.getConsignee().getScCodeType());
        consignee.setScName(waybillPrint.getConsignee().getScName());
        consignee.setStateCode(waybillPrint.getConsignee().getStateCode());
        consignee.setTelNumber(waybillPrint.getConsignee().getTelNumber());
        consignee.setEditorId(SecurityUtils.getUser().getId());
        consignee.setEditorName(SecurityUtils.getUser().buildOptName());
        consignee.setEditTime(LocalDateTime.now());
        waybillPrintShipperConsigneeService.updateById(consignee);
    }

    @Override
    public void print(String type, Integer waybillPrintId) {
        WaybillPrint waybillPrint = baseMapper.printMain(waybillPrintId, SecurityUtils.getUser().getOrgId());
        if (StrUtil.isNotBlank(waybillPrint.getShipName()) && StrUtil.isNotBlank(waybillPrint.getShipVoyageNumber())) {
            waybillPrint.setShipNameAndShipVoyageNumber(waybillPrint.getShipName() + "/" + waybillPrint.getShipVoyageNumber());
        } else if (StrUtil.isNotBlank(waybillPrint.getShipName())) {
            waybillPrint.setShipNameAndShipVoyageNumber(waybillPrint.getShipName());
        } else if (StrUtil.isNotBlank(waybillPrint.getShipVoyageNumber())) {
            waybillPrint.setShipNameAndShipVoyageNumber(waybillPrint.getShipVoyageNumber());
        }
        List<WaybillPrintDetails> waybillPrintDetailsList = baseMapper.printDetail(waybillPrintId, SecurityUtils.getUser().getOrgId());
        waybillPrintDetailsList.stream().forEach(waybillPrintDetails -> {
            if (StrUtil.isNotBlank(waybillPrintDetails.getNumber()) && StrUtil.isNotBlank(waybillPrintDetails.getKindOfPackage())) {
                waybillPrintDetails.setNumberAndKindOfPackage(waybillPrintDetails.getNumber() + "/" + waybillPrintDetails.getKindOfPackage());
            } else if (StrUtil.isNotBlank(waybillPrintDetails.getNumber())) {
                waybillPrintDetails.setNumberAndKindOfPackage(waybillPrintDetails.getNumber());
            } else if (StrUtil.isNotBlank(waybillPrintDetails.getKindOfPackage())) {
                waybillPrintDetails.setNumberAndKindOfPackage(waybillPrintDetails.getKindOfPackage());
            }
        });
        waybillPrint.setWaybillPrintDetailsList(waybillPrintDetailsList);
        String modelPath = JxlsUtils.modelRootPath;
        if ("G".equals(type)) {
            modelPath = modelPath + "BL_ORGINAL_FORMAT.xlsx";
        } else if ("T".equals(type)) {
            modelPath = modelPath + "BL_ORGINAL.xlsx";
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("data", waybillPrint);
        JxlsUtils.exportExcelWithLocalModel(modelPath, map);
    }
}
