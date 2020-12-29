package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.component.BirtComponent;
import com.efreight.afbase.dao.AfOrderMapper;
import com.efreight.afbase.dao.CoopProjectMapper;
import com.efreight.afbase.dao.OperationPlanMapper;
import com.efreight.afbase.dao.WarehouseMapper;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.service.*;
import com.efreight.afbase.utils.FilePathUtils;
import com.efreight.afbase.utils.PDFUtils;
import com.efreight.common.core.utils.JxlsUtils;
import com.efreight.common.core.utils.PoiUtils;
import com.efreight.common.security.util.SecurityUtils;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BadPdfFormatException;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
@AllArgsConstructor
public class OperationPlanServiceImpl extends ServiceImpl<OperationPlanMapper, OperationPlan> implements OperationPlanService {
    private final CoopProjectMapper coopProjectMapper;
    private final WarehouseMapper warehouseMapper;
    private final AfOrderService afOrderService;
    private final CarrierService carrierService;
    private final AfShipperLetterService afShipperLetterService;
    private final LogService logService;
    private final AfOrderMapper afOrderMapper;
    public static String filePath = FilePathUtils.filePath;

    @Resource
    private BirtComponent birtComponent;

    @Override
    public IPage getPage(Page page, OperationPlan bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        return baseMapper.getListPage(page, bean);
    }

    @Override
    public List<Warehouse> findStorehouse(String departure) {
        LambdaQueryWrapper<Warehouse> wrapper = Wrappers.<Warehouse>lambdaQuery();
        wrapper.eq(Warehouse::getOrgId, SecurityUtils.getUser().getOrgId());
        wrapper.eq(Warehouse::getBusinessScope, "AE");
        wrapper.eq(Warehouse::getCustomsSupervision, "普货库");
        if (!StringUtils.isEmpty(departure)) {
            wrapper.eq(Warehouse::getApCode, departure);
        }
        wrapper.eq(Warehouse::getWarehouseStatus, 1);
        wrapper.orderByAsc(Warehouse::getWarehouseCode);
        List<Warehouse> list = warehouseMapper.selectList(wrapper);
        return list;
    }

    @Override
    public List<Warehouse> findWarehouse(String departure) {
        LambdaQueryWrapper<Warehouse> wrapper = Wrappers.<Warehouse>lambdaQuery();
        wrapper.eq(Warehouse::getOrgId, SecurityUtils.getUser().getOrgId());
        wrapper.eq(Warehouse::getBusinessScope, "AE");
        wrapper.like(Warehouse::getCustomsSupervision, "%监管");
        wrapper.eq(Warehouse::getApCode, departure);
        wrapper.orderByAsc(Warehouse::getWarehouseCode);
        List<Warehouse> list = warehouseMapper.selectList(wrapper);
        return list;
    }

    @Override
    public List<OperationPlanExcel> queryListForExcle(String orderIds) {
        List<OperationPlanExcel> list = baseMapper.callListForExcel(SecurityUtils.getUser().getOrgId(), orderIds);
        return list;
    }

    @Override
    public Boolean printTag(Integer orgId, String printScope, String orderUuid, String slIds) {
        //主单号条码	主单号两侧增加星号，例如 *020-11111111*
        //分单号条码	分单号两侧增加星号，例如 *HHH1111111*
        //航司两字码	主单号前3位对应航司的两字码；
        //主运单号	订单号
        //分单件数	格式：当前件数/当前分单总件数； 例如：3/10
        //总流水号	可认为是范围截止号码的序号；四位字符，不够补0，例如  23 则 0023；
        //始发港	主单的始发港；任意一票订单；
        //主单目的港	订单的 目的港；
        //主单件数	主单托书件数；
        //分单号	分单托书分单号；
        //分单目的港	分单目的港；当前分单托书目的港；

        //订单信息
        List<String> slIdList = new ArrayList<>();
        if (StrUtil.isNotBlank(slIds)) {
            slIdList = Arrays.asList(slIds.replace(" ", "").split(","));
        }

        LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
        orderWrapper.eq(AfOrder::getOrgId, orgId).eq(AfOrder::getOrderUuid, orderUuid);
        AfOrder order = afOrderService.getOne(orderWrapper);
        if (StrUtil.isBlank(order.getAwbNumber())) {
            throw new RuntimeException("该订单无主单信息，不可打印标签");
        }

        //分单托书信息
        LambdaQueryWrapper<AfShipperLetter> letterHawbWrapper = Wrappers.<AfShipperLetter>lambdaQuery();
        if (slIdList.size() != 0) {
            letterHawbWrapper.eq(AfShipperLetter::getOrgId, orgId).eq(AfShipperLetter::getSlType, "HAWB").eq(AfShipperLetter::getOrderUuid, orderUuid).in(AfShipperLetter::getSlId, slIdList);
        } else {
            letterHawbWrapper.eq(AfShipperLetter::getOrgId, orgId).eq(AfShipperLetter::getSlType, "HAWB").eq(AfShipperLetter::getOrderUuid, orderUuid);
        }
        List<AfShipperLetter> letterHawbList = afShipperLetterService.list(letterHawbWrapper);

        //主单托书信息
        Integer mwbPieces = null;//主单件数
        AfOrder dbOrder = afOrderService.getOrderByUUID(orderUuid);
        if (null != dbOrder) {
            mwbPieces = Optional.ofNullable(dbOrder.getConfirmPieces()).orElse(dbOrder.getPlanPieces());
        }
        if (mwbPieces == 0) {
            mwbPieces = null;
        }


        //航司信息
        LambdaQueryWrapper<Carrier> carrierWrapper = Wrappers.<Carrier>lambdaQuery();
        carrierWrapper.eq(Carrier::getCarrierPrefix, order.getAwbNumber().split("-")[0]);
        Carrier carrier = carrierService.getOne(carrierWrapper);


        //整合标签信息
        ArrayList<OperationPlanPrintTag> list = new ArrayList<>();
        int sequenceNumber = 1;
        if (slIdList.size() > 0) {
            for (AfShipperLetter letterHawb : letterHawbList) {
                if (letterHawb.getPlanPieces() == null) {
                    //合成流水号sequence
                    String sequence = "0000".substring(0, 4 - (sequenceNumber + "").length()) + sequenceNumber;
                    OperationPlanPrintTag operationPlanPrintTag = new OperationPlanPrintTag();
                    operationPlanPrintTag.setAwbNumber(order.getAwbNumber());
                    operationPlanPrintTag.setCarrierCode(carrier.getCarrierCode());
                    operationPlanPrintTag.setDeparture(order.getDepartureStation());
                    operationPlanPrintTag.setDestinationHawb(letterHawb.getArrivalStation());
                    operationPlanPrintTag.setDestinatonAwb(order.getArrivalStation());
                    operationPlanPrintTag.setHawbNumber(letterHawb.getHawbNumber());
                    operationPlanPrintTag.setOrderCode(order.getOrderCode());
                    operationPlanPrintTag.setPiecesAwb(mwbPieces);
                    operationPlanPrintTag.setPiecesHawb(letterHawb.getPlanPieces());
                    operationPlanPrintTag.setSequence(sequence);
                    sequenceNumber++;
                    operationPlanPrintTag.setPieceNumber(null);
                    list.add(operationPlanPrintTag);
                } else if (letterHawb.getPlanPieces() == 0) {
                    //合成流水号sequence
                    String sequence = "0000".substring(0, 4 - (sequenceNumber + "").length()) + sequenceNumber;
                    OperationPlanPrintTag operationPlanPrintTag = new OperationPlanPrintTag();
                    operationPlanPrintTag.setAwbNumber(order.getAwbNumber());
                    operationPlanPrintTag.setCarrierCode(carrier.getCarrierCode());
                    operationPlanPrintTag.setDeparture(order.getDepartureStation());
                    operationPlanPrintTag.setDestinationHawb(letterHawb.getArrivalStation());
                    operationPlanPrintTag.setDestinatonAwb(order.getArrivalStation());
                    operationPlanPrintTag.setHawbNumber(letterHawb.getHawbNumber());
                    operationPlanPrintTag.setOrderCode(order.getOrderCode());
                    operationPlanPrintTag.setPiecesAwb(mwbPieces);
                    operationPlanPrintTag.setPiecesHawb(letterHawb.getPlanPieces());
                    operationPlanPrintTag.setSequence(sequence);
                    sequenceNumber++;
                    operationPlanPrintTag.setPieceNumber(0);
                    list.add(operationPlanPrintTag);
                } else {
                    int piecesNumber = 1;
                    for (int i = 0; i < letterHawb.getPlanPieces(); i++) {
                        //合成流水号sequence2
                        String sequence = "0000".substring(0, 4 - (sequenceNumber + "").length()) + sequenceNumber;
                        OperationPlanPrintTag operationPlanPrintTag = new OperationPlanPrintTag();
                        operationPlanPrintTag.setAwbNumber(order.getAwbNumber());
                        operationPlanPrintTag.setCarrierCode(carrier.getCarrierCode());
                        operationPlanPrintTag.setDeparture(order.getDepartureStation());
                        operationPlanPrintTag.setDestinationHawb(letterHawb.getArrivalStation());
                        operationPlanPrintTag.setDestinatonAwb(order.getArrivalStation());
                        operationPlanPrintTag.setHawbNumber(letterHawb.getHawbNumber());
                        operationPlanPrintTag.setOrderCode(order.getOrderCode());
                        operationPlanPrintTag.setPiecesAwb(mwbPieces);
                        operationPlanPrintTag.setPiecesHawb(letterHawb.getPlanPieces());
                        operationPlanPrintTag.setSequence(sequence);
                        operationPlanPrintTag.setPieceNumber(piecesNumber);
                        list.add(operationPlanPrintTag);
                        piecesNumber++;
                        sequenceNumber++;
                    }

                }


            }
        } else {
            //合成流水号sequence
            String sequence = "0000".substring(0, 4 - (sequenceNumber + "").length()) + sequenceNumber;
            OperationPlanPrintTag operationPlanPrintTag = new OperationPlanPrintTag();
            operationPlanPrintTag.setAwbNumber(order.getAwbNumber());
            operationPlanPrintTag.setCarrierCode(carrier.getCarrierCode());
            operationPlanPrintTag.setDeparture(order.getDepartureStation());
            operationPlanPrintTag.setDestinatonAwb(order.getArrivalStation());
            operationPlanPrintTag.setOrderCode(order.getOrderCode());
            operationPlanPrintTag.setPiecesAwb(mwbPieces);
            operationPlanPrintTag.setSequence(sequence);
            list.add(operationPlanPrintTag);
        }

        if (StrUtil.isNotBlank(printScope)) {
            ArrayList<OperationPlanPrintTag> resultList = new ArrayList<>();
            int start = Integer.parseInt(printScope.split("-")[0]);
            int end = Integer.parseInt(printScope.split("-")[1]);
            list.stream().forEach(operationPlanPrintTag -> {
                String tempSequence = operationPlanPrintTag.getSequence();
                if (StringUtils.isEmpty(tempSequence)) {
                    tempSequence = "0";
                } else {
                    if (tempSequence.startsWith("0")) {
                        tempSequence = tempSequence.substring(1, tempSequence.length());
                        if (tempSequence.startsWith("0")) {
                            tempSequence = tempSequence.substring(1, tempSequence.length());
                            if (tempSequence.startsWith("0")) {
                                tempSequence = tempSequence.substring(1, tempSequence.length());
                            }
                        }
                    }
                }

                if (Integer.parseInt(tempSequence) > start - 1 && Integer.parseInt(tempSequence) < end + 1) {
                    resultList.add(operationPlanPrintTag);
                }
            });

            if (resultList != null && resultList.size() > 0) {
                PDFUtils.printAllTag(resultList);
            } else {
                PDFUtils.printAllTag(list);
            }

        } else {
            PDFUtils.printAllTag(list);
        }
        return true;
    }

    /**
     * 返回路径
     *
     * @param orgId
     * @param printScope
     * @param orderUuid
     * @param slIds
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String printTagNew(Integer orgId, String printScope, String orderUuid, String slIds, String pageName) throws Exception {
        //主单号条码	主单号两侧增加星号，例如 *020-11111111*
        //分单号条码	分单号两侧增加星号，例如 *HHH1111111*
        //航司两字码	主单号前3位对应航司的两字码；
        //主运单号	订单号
        //分单件数	格式：当前件数/当前分单总件数； 例如：3/10
        //总流水号	可认为是范围截止号码的序号；四位字符，不够补0，例如  23 则 0023；
        //始发港	主单的始发港；任意一票订单；
        //主单目的港	订单的 目的港；
        //主单件数	主单托书件数；
        //分单号	分单托书分单号；
        //分单目的港	分单目的港；当前分单托书目的港；

        //订单信息
        List<String> slIdList = new ArrayList<>();
        if (StrUtil.isNotBlank(slIds)) {
            slIdList = Arrays.asList(slIds.replace(" ", "").split(","));
        }

        LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
        orderWrapper.eq(AfOrder::getOrgId, orgId).eq(AfOrder::getOrderUuid, orderUuid);
        AfOrder order = afOrderService.getOne(orderWrapper);
        if (StrUtil.isBlank(order.getAwbNumber())) {
            throw new RuntimeException("该订单无主单信息，不可打印标签");
        }

        //分单托书信息
        LambdaQueryWrapper<AfShipperLetter> letterHawbWrapper = Wrappers.<AfShipperLetter>lambdaQuery();
        if (slIdList.size() != 0) {
            letterHawbWrapper.eq(AfShipperLetter::getOrgId, orgId).eq(AfShipperLetter::getSlType, "HAWB").eq(AfShipperLetter::getOrderUuid, orderUuid).in(AfShipperLetter::getSlId, slIdList);
        } else {
            letterHawbWrapper.eq(AfShipperLetter::getOrgId, orgId).eq(AfShipperLetter::getSlType, "HAWB").eq(AfShipperLetter::getOrderUuid, orderUuid);
        }
        List<AfShipperLetter> letterHawbList = afShipperLetterService.list(letterHawbWrapper);

        //主单托书信息
        Integer mwbPieces = null;//主单件数
        AfOrder dbOrder = afOrderService.getOrderByUUID(orderUuid);
        if (null != dbOrder) {
            mwbPieces = Optional.ofNullable(dbOrder.getConfirmPieces()).orElse(dbOrder.getPlanPieces());
        }
        if (mwbPieces == 0) {
            mwbPieces = null;
        }


        //航司信息
        LambdaQueryWrapper<Carrier> carrierWrapper = Wrappers.<Carrier>lambdaQuery();
        carrierWrapper.eq(Carrier::getCarrierPrefix, order.getAwbNumber().split("-")[0]);
        Carrier carrier = carrierService.getOne(carrierWrapper);


        //整合标签信息
        ArrayList<OperationPlanPrintTag> list = new ArrayList<>();
        int sequenceNumber = 1;
        if (slIdList.size() > 0) {
            for (AfShipperLetter letterHawb : letterHawbList) {
                if (letterHawb.getPlanPieces() == null) {
                    //合成流水号sequence
                    String sequence = "0000".substring(0, 4 - (sequenceNumber + "").length()) + sequenceNumber;
                    OperationPlanPrintTag operationPlanPrintTag = new OperationPlanPrintTag();
                    operationPlanPrintTag.setAwbNumber(order.getAwbNumber());
                    operationPlanPrintTag.setCarrierCode(carrier.getCarrierCode());
                    operationPlanPrintTag.setDeparture(order.getDepartureStation());
                    operationPlanPrintTag.setDestinationHawb(letterHawb.getArrivalStation());
                    operationPlanPrintTag.setDestinatonAwb(order.getArrivalStation());
                    operationPlanPrintTag.setHawbNumber(letterHawb.getHawbNumber());
                    operationPlanPrintTag.setOrderCode(order.getOrderCode());
                    operationPlanPrintTag.setPiecesAwb(mwbPieces);
                    operationPlanPrintTag.setPiecesHawb(letterHawb.getPlanPieces());
                    operationPlanPrintTag.setSequence(sequence);
                    sequenceNumber++;
                    operationPlanPrintTag.setPieceNumber(null);
                    list.add(operationPlanPrintTag);
                } else if (letterHawb.getPlanPieces() == 0) {
                    //合成流水号sequence
                    String sequence = "0000".substring(0, 4 - (sequenceNumber + "").length()) + sequenceNumber;
                    OperationPlanPrintTag operationPlanPrintTag = new OperationPlanPrintTag();
                    operationPlanPrintTag.setAwbNumber(order.getAwbNumber());
                    operationPlanPrintTag.setCarrierCode(carrier.getCarrierCode());
                    operationPlanPrintTag.setDeparture(order.getDepartureStation());
                    operationPlanPrintTag.setDestinationHawb(letterHawb.getArrivalStation());
                    operationPlanPrintTag.setDestinatonAwb(order.getArrivalStation());
                    operationPlanPrintTag.setHawbNumber(letterHawb.getHawbNumber());
                    operationPlanPrintTag.setOrderCode(order.getOrderCode());
                    operationPlanPrintTag.setPiecesAwb(mwbPieces);
                    operationPlanPrintTag.setPiecesHawb(letterHawb.getPlanPieces());
                    operationPlanPrintTag.setSequence(sequence);
                    sequenceNumber++;
                    operationPlanPrintTag.setPieceNumber(0);
                    list.add(operationPlanPrintTag);
                } else {
                    int piecesNumber = 1; Set<String> tmpSet = new HashSet<>();
                    for (int i = 0; i < letterHawb.getPlanPieces(); i++) {
                        if(!tmpSet.contains(letterHawb.getHawbNumber())){
                            sequenceNumber = 1;
                            tmpSet.add(letterHawb.getHawbNumber());
                        }
                        //合成流水号sequence2
                        String sequence = "0000".substring(0, 4 - (sequenceNumber + "").length()) + sequenceNumber;
                        OperationPlanPrintTag operationPlanPrintTag = new OperationPlanPrintTag();
                        operationPlanPrintTag.setAwbNumber(order.getAwbNumber());
                        operationPlanPrintTag.setCarrierCode(carrier.getCarrierCode());
                        operationPlanPrintTag.setDeparture(order.getDepartureStation());
                        operationPlanPrintTag.setDestinationHawb(letterHawb.getArrivalStation());
                        operationPlanPrintTag.setDestinatonAwb(order.getArrivalStation());
                        operationPlanPrintTag.setHawbNumber(letterHawb.getHawbNumber());
                        operationPlanPrintTag.setOrderCode(order.getOrderCode());
                        operationPlanPrintTag.setPiecesAwb(mwbPieces);
                        operationPlanPrintTag.setPiecesHawb(letterHawb.getPlanPieces());
                        operationPlanPrintTag.setSequence(sequence);
                        operationPlanPrintTag.setPieceNumber(piecesNumber);
                        list.add(operationPlanPrintTag);
                        piecesNumber++;
                        sequenceNumber++;
                    }

                }


            }
        } else {
            //合成流水号sequence
            int loopCount = (mwbPieces == null || mwbPieces == 0) ? 1 : mwbPieces;
            mwbPieces = Optional.ofNullable(mwbPieces).orElse(0);

            for (int i = 0; i < loopCount; i++) {
                String sequence = "0000".substring(0, 4 - (sequenceNumber + "").length()) + sequenceNumber;
                OperationPlanPrintTag operationPlanPrintTag = new OperationPlanPrintTag();
                operationPlanPrintTag.setAwbNumber(order.getAwbNumber());
                operationPlanPrintTag.setCarrierCode(carrier.getCarrierCode());
                operationPlanPrintTag.setDeparture(order.getDepartureStation());
                operationPlanPrintTag.setDestinatonAwb(order.getArrivalStation());
                operationPlanPrintTag.setOrderCode(order.getOrderCode());
                operationPlanPrintTag.setPiecesAwb(mwbPieces);
                operationPlanPrintTag.setSequence(sequence);
                list.add(operationPlanPrintTag);
                sequenceNumber++;
            }
        }

        if (StrUtil.isNotBlank(printScope)) {
            ArrayList<OperationPlanPrintTag> resultList = new ArrayList<>();
            int start = Integer.parseInt(printScope.split("-")[0]);
            int end = Integer.parseInt(printScope.split("-")[1]);
            for (int i = start-1; i <= end-1; i++) {
                resultList.add(list.get(i));
            }
            if (resultList != null && resultList.size() > 0) {
                //添加日志信息
                LogBean logBean = new LogBean();
                logBean.setOrgId(SecurityUtils.getUser().getOrgId());
                logBean.setPageName(pageName);
                logBean.setPageFunction("标签打印");
                logBean.setBusinessScope("AE");
                logBean.setOrderNumber(order.getOrderCode());
                logBean.setOrderId(order.getOrderId());
                logBean.setOrderUuid(order.getOrderUuid());
                logBean.setCreatorId(SecurityUtils.getUser().getId());
                logBean.setCreatTime(LocalDateTime.now());
                logBean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                logService.saveLog(logBean);
                return printTag(resultList);
            }

        }
        //添加日志信息
        LogBean logBean = new LogBean();
        logBean.setOrgId(SecurityUtils.getUser().getOrgId());
        logBean.setPageName(pageName);
        logBean.setPageFunction("标签打印");
        logBean.setBusinessScope("AE");
        logBean.setOrderNumber(order.getOrderCode());
        logBean.setOrderId(order.getOrderId());
        logBean.setOrderUuid(order.getOrderUuid());
        logBean.setCreatorId(SecurityUtils.getUser().getId());
        logBean.setCreatTime(LocalDateTime.now());
        logBean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        logService.saveLog(logBean);
        return printTag(list);
    }


    @Override
    public List<AfShipperLetter> getShipperLetterByOrderUuid(String orderUuid, String type) {
        LambdaQueryWrapper<AfShipperLetter> wrapper = Wrappers.<AfShipperLetter>lambdaQuery();
        wrapper.eq(AfShipperLetter::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfShipperLetter::getOrderUuid, orderUuid);
        if ("mawb".equals(type)) {
            wrapper.eq(AfShipperLetter::getSlType, "MAWB");
        } else if ("hawb".equals(type)) {
            wrapper.eq(AfShipperLetter::getSlType, "HAWB");
        }
        return afShipperLetterService.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String printTagMany(Integer orgId, String orderUuids, String pageName) throws Exception {
        //订单信息
        List<String> orderUuidList = new ArrayList<>();
        if (StrUtil.isNotBlank(orderUuids)) {
            orderUuidList = Arrays.asList(orderUuids.split(","));
        }

        LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
        orderWrapper.eq(AfOrder::getOrgId, orgId).in(AfOrder::getOrderUuid, orderUuidList);

        ArrayList<OperationPlanPrintTag> list = new ArrayList<>();
        afOrderService.list(orderWrapper).stream().forEach(order -> {
            if (StrUtil.isBlank(order.getAwbNumber())) {
                throw new RuntimeException("该订单无主单信息，不可打印标签");
            }

            //分单托书信息
            LambdaQueryWrapper<AfShipperLetter> letterHawbWrapper = Wrappers.<AfShipperLetter>lambdaQuery();
            letterHawbWrapper.eq(AfShipperLetter::getOrgId, orgId).eq(AfShipperLetter::getSlType, "HAWB").eq(AfShipperLetter::getOrderUuid, order.getOrderUuid());
            List<AfShipperLetter> letterHawbList = afShipperLetterService.list(letterHawbWrapper);

            //主单托书信息
            Integer mwbPieces = null;//主单件数
            AfOrder dbOrder = afOrderService.getOrderByUUID(order.getOrderUuid());
            if (null != dbOrder) {
                mwbPieces = Optional.ofNullable(dbOrder.getConfirmPieces()).orElse(dbOrder.getPlanPieces());
            }
            if (mwbPieces == 0) {
                mwbPieces = null;
            }


            //航司信息
            LambdaQueryWrapper<Carrier> carrierWrapper = Wrappers.<Carrier>lambdaQuery();
            carrierWrapper.eq(Carrier::getCarrierPrefix, order.getAwbNumber().split("-")[0]);
            Carrier carrier = carrierService.getOne(carrierWrapper);


            //整合标签信息

            int sequenceNumber = 1;
            if (letterHawbList.size() > 0) {
                for (AfShipperLetter letterHawb : letterHawbList) {
                    if (letterHawb.getPlanPieces() == null) {
                        //合成流水号sequence
                        String sequence = "0000".substring(0, 4 - (sequenceNumber + "").length()) + sequenceNumber;
                        OperationPlanPrintTag operationPlanPrintTag = new OperationPlanPrintTag();
                        operationPlanPrintTag.setAwbNumber(order.getAwbNumber());
                        operationPlanPrintTag.setCarrierCode(carrier.getCarrierCode());
                        operationPlanPrintTag.setDeparture(order.getDepartureStation());
                        operationPlanPrintTag.setDestinationHawb(letterHawb.getArrivalStation());
                        operationPlanPrintTag.setDestinatonAwb(order.getArrivalStation());
                        operationPlanPrintTag.setHawbNumber(letterHawb.getHawbNumber());
                        operationPlanPrintTag.setOrderCode(order.getOrderCode());
                        operationPlanPrintTag.setPiecesAwb(mwbPieces);
                        operationPlanPrintTag.setPiecesHawb(letterHawb.getPlanPieces());
                        operationPlanPrintTag.setSequence(sequence);
                        sequenceNumber++;
                        operationPlanPrintTag.setPieceNumber(null);
                        list.add(operationPlanPrintTag);
                    } else if (letterHawb.getPlanPieces() == 0) {
                        //合成流水号sequence
                        String sequence = "0000".substring(0, 4 - (sequenceNumber + "").length()) + sequenceNumber;
                        OperationPlanPrintTag operationPlanPrintTag = new OperationPlanPrintTag();
                        operationPlanPrintTag.setAwbNumber(order.getAwbNumber());
                        operationPlanPrintTag.setCarrierCode(carrier.getCarrierCode());
                        operationPlanPrintTag.setDeparture(order.getDepartureStation());
                        operationPlanPrintTag.setDestinationHawb(letterHawb.getArrivalStation());
                        operationPlanPrintTag.setDestinatonAwb(order.getArrivalStation());
                        operationPlanPrintTag.setHawbNumber(letterHawb.getHawbNumber());
                        operationPlanPrintTag.setOrderCode(order.getOrderCode());
                        operationPlanPrintTag.setPiecesAwb(mwbPieces);
                        operationPlanPrintTag.setPiecesHawb(letterHawb.getPlanPieces());
                        operationPlanPrintTag.setSequence(sequence);
                        sequenceNumber++;
                        operationPlanPrintTag.setPieceNumber(0);
                        list.add(operationPlanPrintTag);
                    } else {
                        int piecesNumber = 1;
                        Set<String> tmpSet = new HashSet<>();
                        for (int i = 0; i < letterHawb.getPlanPieces(); i++) {
                            if(!tmpSet.contains(letterHawb.getHawbNumber())){
                                sequenceNumber = 1;
                                tmpSet.add(letterHawb.getHawbNumber());
                            }
                            //合成流水号sequence2
                            String sequence = "0000".substring(0, 4 - (sequenceNumber + "").length()) + sequenceNumber;
                            OperationPlanPrintTag operationPlanPrintTag = new OperationPlanPrintTag();
                            operationPlanPrintTag.setAwbNumber(order.getAwbNumber());
                            operationPlanPrintTag.setCarrierCode(carrier.getCarrierCode());
                            operationPlanPrintTag.setDeparture(order.getDepartureStation());
                            operationPlanPrintTag.setDestinationHawb(letterHawb.getArrivalStation());
                            operationPlanPrintTag.setDestinatonAwb(order.getArrivalStation());
                            operationPlanPrintTag.setHawbNumber(letterHawb.getHawbNumber());
                            operationPlanPrintTag.setOrderCode(order.getOrderCode());
                            operationPlanPrintTag.setPiecesAwb(mwbPieces);
                            operationPlanPrintTag.setPiecesHawb(letterHawb.getPlanPieces());
                            operationPlanPrintTag.setSequence(sequence);
                            operationPlanPrintTag.setPieceNumber(piecesNumber);
                            list.add(operationPlanPrintTag);
                            piecesNumber++;
                            sequenceNumber++;
                        }

                    }


                }
            } else {
                //合成流水号sequence
                if(mwbPieces != null){
                    for (int i = 0; i < mwbPieces; i++) {
                        String sequence = "0000".substring(0, 4 - (sequenceNumber + "").length()) + sequenceNumber;
                        OperationPlanPrintTag operationPlanPrintTag = new OperationPlanPrintTag();
                        operationPlanPrintTag.setAwbNumber(order.getAwbNumber());
                        operationPlanPrintTag.setCarrierCode(carrier.getCarrierCode());
                        operationPlanPrintTag.setDeparture(order.getDepartureStation());
                        operationPlanPrintTag.setDestinatonAwb(order.getArrivalStation());
                        operationPlanPrintTag.setOrderCode(order.getOrderCode());
                        operationPlanPrintTag.setPiecesAwb(mwbPieces);
                        operationPlanPrintTag.setSequence(sequence);
                        list.add(operationPlanPrintTag);
                        sequenceNumber++;
                    }
                }
            }
            //添加日志信息
            LogBean logBean = new LogBean();
            logBean.setOrgId(SecurityUtils.getUser().getOrgId());
            logBean.setPageName(pageName);
            logBean.setPageFunction("标签打印");
            logBean.setBusinessScope("AE");
            logBean.setOrderNumber(order.getOrderCode());
            logBean.setOrderId(order.getOrderId());
            logBean.setOrderUuid(order.getOrderUuid());
            logBean.setCreatorId(SecurityUtils.getUser().getId());
            logBean.setCreatTime(LocalDateTime.now());
            logBean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            logService.saveLog(logBean);
        });

        return printTag(list);
    }

    @Override
    public Boolean printLetters(Integer orgId, String awbUUIds) {
        List<Letters> letterList = baseMapper.printLetters(orgId, awbUUIds);
//    	List<Letters> bgsLetterList=new ArrayList<Letters>();
//    	List<Letters> caLetterList=new ArrayList<Letters>();
//    	for (int i = 0; i < letterList.size(); i++) {
//			if ("Letter_BGS.pdf".equals(letterList.get(i).getLetterPdf())) {
//				bgsLetterList.add(letterList.get(i));
//			}else if("Letter_CA.pdf".equals(letterList.get(i).getLetterPdf())){
//				caLetterList.add(letterList.get(i));
//			}
//		}
        try {
//    		if ("BGS".equals(letterType)) {
//    			PDFUtils.printAllLetters(bgsLetterList, "Letter_BGS");
//			} else {
//				PDFUtils.printAllLetters(caLetterList, "Letter_CA");
//			}
            if (letterList.size() > 0) {
                PDFUtils.printAllLetters(letterList);
            }
        } catch (BadPdfFormatException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public String printLetters1(Integer orgId, String awbUuid) throws IOException, DocumentException {
        List<Letters> letterList = baseMapper.printLetters(orgId, awbUuid);

        ArrayList<String> newFilePaths = new ArrayList<>();
        if (letterList.size() > 0 && letterList != null) {
            for (int i = 0; i < letterList.size(); i++) {
                String path = print(letterList.get(i), false);
                newFilePaths.add(path);
                //当前订单主单号 前三位 与 当前货站模板附属表 航司一致，且 维护了 PDF模板 ，则 将附属航司模板填充数据后的PDF 拼接到 货站托书中 一同输出；
                if(letterList.get(i).getSecurityNotePdf() != null && !"".equals(letterList.get(i).getSecurityNotePdf())){
                    letterList.get(i).setLetterPdf(letterList.get(i).getSecurityNotePdf());
                    String pathPdf = print(letterList.get(i), false);
                    newFilePaths.add(pathPdf);
                }
            }
        }
        //单票打印：改为 DELIVERY_NOTE_主单号_流水号.pdf
        //批量打印：改为 DELIVERY_NOTE_流水号.pdf
        String lastFilePath = "";
        if (awbUuid.indexOf(",") != -1) {
            lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/printBillTemp/DELIVERY_NOTE_" + new Date().getTime() + ".pdf";
        } else {
            if (letterList.size() > 0 && letterList != null) {
                lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/printBillTemp/DELIVERY_NOTE_" + letterList.get(0).getInput001() + '_' + new Date().getTime() + ".pdf";
            }
        }
        PDFUtils.loadAllPDFForFile(newFilePaths, lastFilePath, null);
        return lastFilePath.replace(PDFUtils.filePath, "");
    }

    @Override
    public void exportExcel(String awbUuid) {
        List<Letters> letterList = baseMapper.printLetters(SecurityUtils.getUser().getOrgId(), awbUuid);
        String templateFilePath = "";
        String templateFileSecurityNotePath = "";
        if (letterList.size() > 0 && letterList != null) {
            Letters letters = letterList.get(0);
            String path = letters.getLetterExcel();
            if (StrUtil.isNotBlank(path)) {
                templateFilePath = PDFUtils.filePath + "/PDFtemplate/temp/opertionPlan/letter/" + SecurityUtils.getUser().getOrgId() + "/" + path.substring(path.lastIndexOf("/") + 1, path.length());
                PDFUtils.downloadFile(path, templateFilePath);
            } else {
                throw new RuntimeException("导出失败，无托书模板");
            }
            String securityNotePath = letters.getSecurityNoteExcel();
            if (StrUtil.isNotBlank(securityNotePath)) {
                templateFileSecurityNotePath = PDFUtils.filePath + "/PDFtemplate/temp/opertionPlan/letter/" + SecurityUtils.getUser().getOrgId() + "/" + securityNotePath.substring(securityNotePath.lastIndexOf("/") + 1, securityNotePath.length());
                PDFUtils.downloadFile(securityNotePath, templateFileSecurityNotePath);
            }
            String lastPath = PDFUtils.filePath + "/PDFtemplate/temp/opertionPlan/letter/" + SecurityUtils.getUser().getOrgId() + "/lastShipperLetter_" + new Date().getTime() + ".xlsx";
            String lastShipperFilePath = PDFUtils.filePath + "/PDFtemplate/temp/opertionPlan/letter/" + SecurityUtils.getUser().getOrgId() + "/shipperLetter_" + new Date().getTime() + ".xlsx";
            String lastSecurityNotePath = PDFUtils.filePath + "/PDFtemplate/temp/opertionPlan/letter/" + SecurityUtils.getUser().getOrgId() + "/securityNote_" + new Date().getTime() + ".xlsx";
            //封装数据
            HashMap<String, Object> context = new HashMap<>();
            context.put("data", letters);
            if (StrUtil.isNotBlank(templateFileSecurityNotePath)) {
                JxlsUtils.exportExcelToFile(templateFileSecurityNotePath, lastSecurityNotePath, context);
                JxlsUtils.exportExcelToFile(templateFilePath, lastShipperFilePath, context);
                PoiUtils.multiplySheetForShipperLetterPrint(lastPath, lastShipperFilePath, lastSecurityNotePath);
                JxlsUtils.responseExcel(lastPath);
            } else {
                JxlsUtils.exportExcelWithLocalModel(templateFilePath, context);
            }

        }

    }

    @Override
    public String printLetters2(Integer orgId, String awbUuid) throws IOException, DocumentException {
        List<Letters> letterList = baseMapper.printLetters(orgId, awbUuid);

        //根据货站的机场代码查询是否有模板
        String awbUUIds = "'" + awbUuid.replaceAll(",", "','") + "'";
        List<WarehouseLetter> warehouseLetterList = baseMapper.checkWarehouseLetter(SecurityUtils.getUser().getOrgId(), awbUUIds);
        String letterPdf = "";
        if (warehouseLetterList != null && warehouseLetterList.size() > 0) {
            letterPdf = warehouseLetterList.get(0).getShipperTemplateFile();
        }

        ArrayList<String> newFilePaths = new ArrayList<>();
        if (letterList.size() > 0 && letterList != null) {
            for (int i = 0; i < letterList.size(); i++) {
                letterList.get(i).setLetterPdf(letterPdf);
                String path = print(letterList.get(i), false);
                newFilePaths.add(path);
                //当前订单主单号 前三位 与 当前货站模板附属表 航司一致，且 维护了 PDF模板 ，则 将附属航司模板填充数据后的PDF 拼接到 货站托书中 一同输出；
                if(letterList.get(i).getSecurityNotePdf() != null && !"".equals(letterList.get(i).getSecurityNotePdf())){
                    letterList.get(i).setLetterPdf(letterList.get(i).getSecurityNotePdf());
                    String pathPdf = print(letterList.get(i), false);
                    newFilePaths.add(pathPdf);
                }
            }
        }
        //单票打印：改为 DELIVERY_NOTE_主单号_流水号.pdf
        //批量打印：改为 DELIVERY_NOTE_流水号.pdf
        String lastFilePath = "";
        if (awbUuid.indexOf(",") != -1) {
            lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/printBillTemp/DELIVERY_NOTE_" + new Date().getTime() + ".pdf";
        } else {
            if (letterList.size() > 0 && letterList != null) {
                lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/printBillTemp/DELIVERY_NOTE_" + letterList.get(0).getInput001() + '_' + new Date().getTime() + ".pdf";
            }
        }
        PDFUtils.loadAllPDFForFile(newFilePaths, lastFilePath, null);
        return lastFilePath.replace(PDFUtils.filePath, "");
    }

    @Override
    public String printLetters3(Integer orgId, String awbUuid, String shipperTemplateFile) throws IOException, DocumentException {
        List<Letters> letterList = baseMapper.printLetters(orgId, awbUuid);

        ArrayList<String> newFilePaths = new ArrayList<>();
        if (letterList.size() > 0 && letterList != null) {
            for (int i = 0; i < letterList.size(); i++) {
                letterList.get(i).setLetterPdf(shipperTemplateFile);
                String path = print(letterList.get(i), false);
                newFilePaths.add(path);
                //当前订单主单号 前三位 与 当前货站模板附属表 航司一致，且 维护了 PDF模板 ，则 将附属航司模板填充数据后的PDF 拼接到 货站托书中 一同输出；
                if(letterList.get(i).getSecurityNotePdf() != null && !"".equals(letterList.get(i).getSecurityNotePdf())){
                    letterList.get(i).setLetterPdf(letterList.get(i).getSecurityNotePdf());
                    String pathPdf = print(letterList.get(i), false);
                    newFilePaths.add(pathPdf);
                }
            }
        }
        //单票打印：改为 DELIVERY_NOTE_主单号_流水号.pdf
        //批量打印：改为 DELIVERY_NOTE_流水号.pdf
        String lastFilePath = "";
        if (awbUuid.indexOf(",") != -1) {
            lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/printBillTemp/DELIVERY_NOTE_" + new Date().getTime() + ".pdf";
        } else {
            if (letterList.size() > 0 && letterList != null) {
                lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/printBillTemp/DELIVERY_NOTE_" + letterList.get(0).getInput001() + '_' + new Date().getTime() + ".pdf";
            }
        }
        PDFUtils.loadAllPDFForFile(newFilePaths, lastFilePath, null);
        return lastFilePath.replace(PDFUtils.filePath, "");
    }

    @Override
    @SneakyThrows
    public String print(Letters orderLetters, boolean flag) {

        if (flag) {
            return fillTemplate1(orderLetters, PDFUtils.filePath);
        } else {
            return fillTemplate1(orderLetters, "");
        }
    }

    public String fillTemplate1(Letters order, String replacePath) throws IOException, DocumentException {

        String mwbId = order.getInput001();

        String templateName = order.getLetterPdf();
        // 模板路径
//            String templatePath = filePath + "/PDFtemplate/" + templateName + ".pdf";
        String templatePath = PDFUtils.filePath + "/PDFtemplate/temp/" + templateName.split("/")[templateName.split("/").length - 1];
        PDFUtils.downloadFile(templateName, templatePath);

        String savePath = filePath + "/PDFtemplate/temp/opertionPlan/letter";

        //得到文件保存的名称
        String saveFilename = PDFUtils.makeFileName(mwbId + ".pdf");
        //得到文件的保存目录
        String newPDFPath = PDFUtils.makePath(saveFilename, savePath) + "/" + saveFilename;
        //PDFPathList.add(newPDFPath);

        Map<String, String> valueData = new HashMap<>();
        //月份全部大写
        String Etd = "";
//            if (order.getEtd()!=null) {
//            	Etd = new SimpleDateFormat("MMM d", Locale.US).format(order.getEtd()).toUpperCase();
//            }
        valueData.put("Input001", order.getInput001());
        valueData.put("Input002", order.getInput002());
        valueData.put("Input003", order.getInput003());
        valueData.put("Input004", order.getInput004());
        valueData.put("Input005", order.getInput005());
        valueData.put("Input006", order.getInput006());
        valueData.put("Input007", order.getInput007());
        valueData.put("Input008", order.getInput008());//此处代表主单数量
        valueData.put("Input009", order.getInput009());
        valueData.put("Input010", order.getInput010());
        valueData.put("Input011", order.getInput011());
        valueData.put("Input012", order.getInput012());
        valueData.put("Input013", order.getInput013());
        valueData.put("Input014", order.getInput014());
        valueData.put("Input015", order.getInput015());
        valueData.put("Input016", order.getInput016());

        valueData.put("Input0071", order.getInput0071());
        valueData.put("Input0072", order.getInput0072());
        valueData.put("Input0073", order.getInput0073());
        valueData.put("Input0091", order.getInput0091());
        valueData.put("Input0092", order.getInput0092());
        valueData.put("Input017", order.getInput017());
        valueData.put("Input018", order.getInput018());
        valueData.put("Input0191", order.getInput0191());
        valueData.put("Input0192", order.getInput0192());
        valueData.put("Input0193", order.getInput0193());
        valueData.put("Input0194", order.getInput0194());
        valueData.put("Input019", order.getInput019());
        valueData.put("Input020", order.getInput020());
        valueData.put("Input021", order.getInput021());
        valueData.put("Input022", order.getInput022());
        valueData.put("Input023", order.getInput023());
        valueData.put("Input024", order.getInput024());
        valueData.put("Input025", order.getInput025());
        valueData.put("Input026", order.getInput026());
        valueData.put("Input027", order.getInput027());
        valueData.put("Input028", order.getInput028());
        valueData.put("Input029", order.getInput029());
        valueData.put("Input030", order.getInput030());
        valueData.put("Input031", order.getInput031());
        valueData.put("Input032", order.getInput032());
        valueData.put("Input033", order.getInput033());
        valueData.put("Input034", order.getInput034());
        valueData.put("Input035", order.getInput035());
        valueData.put("Input036", order.getInput036());
        valueData.put("Input037", order.getInput037());
        valueData.put("Input038", order.getInput038());
        valueData.put("Input039", order.getInput039());
        valueData.put("Input040", order.getInput040());
        valueData.put("Input041", order.getInput041());
        valueData.put("Input042", order.getInput042());
        valueData.put("Input043", order.getInput043());
        valueData.put("Input044", order.getInput044());
        valueData.put("Input045", order.getInput045());
        valueData.put("Input046", order.getInput046());
        valueData.put("Input047", order.getInput047());
        valueData.put("Input048", order.getInput048());
        valueData.put("Input049", order.getInput049());
        valueData.put("Input050", order.getInput050());
        valueData.put("Input051", order.getInput051());
        valueData.put("Input052", order.getInput052());
        valueData.put("Input053", order.getInput053());
        valueData.put("Input054", order.getInput054());
        valueData.put("Input055", order.getInput055());
        valueData.put("Input056", order.getInput056());
        valueData.put("Input057", order.getInput057());
        valueData.put("Input058", order.getInput058());
        valueData.put("Input059", order.getInput059());
        valueData.put("Input060", order.getInput060());
        valueData.put("Input061", order.getInput061());
        valueData.put("Input062", order.getInput062());
        valueData.put("Input063", order.getInput063());
        valueData.put("Input064", order.getInput064());
        valueData.put("Input065", order.getInput065());
        valueData.put("Input066", order.getInput066());
        valueData.put("Input067", order.getInput067());
        valueData.put("Input068", order.getInput068());
        valueData.put("Input069", order.getInput069());
        valueData.put("Input070", order.getInput070());

        //打印运单确认件字段赋值开始
        valueData.put("txtAWBTop", StrUtil.isBlank(order.getTxtAWBTop()) ? "" : order.getTxtAWBTop());
        valueData.put("txtAWBBottom", StrUtil.isBlank(order.getTxtAWBBottom()) ? "" : order.getTxtAWBBottom());
        valueData.put("txtAWB_Prefix", StrUtil.isBlank(order.getTxtAWBPrefix()) ? "" : order.getTxtAWBPrefix());//主单号前三位
        valueData.put("txtOrigin_Code", StrUtil.isBlank(order.getTxtOriginCode()) ? "" : order.getTxtOriginCode());//始发港
        valueData.put("txtAWB_Suffix", StrUtil.isBlank(order.getTxtAWBSuffix()) ? "" : order.getTxtAWBSuffix());//主单号后八位
        valueData.put("txtCarrier_Name", order.getTxtCarrierName() == null ? "" : order.getTxtCarrierName());//航空公司
        valueData.put("txtAgent_Name", order.getTxtAgentName() == null ? "" : order.getTxtAgentName());//代理名称
        valueData.put("txtAgent_Iata_Code", order.getTxtAgentIataCode() == null ? "" : order.getTxtAgentIataCode());//代理IATA代码
        valueData.put("txtAgent_Account", order.getTxtAgentAccount() == null ? "" : order.getTxtAgentAccount());//代理Account代码
        valueData.put("txtDeparture", order.getTxtDeparture() == null ? "" : order.getTxtDeparture());//始发港英文全称
        valueData.put("txtFlight1_Carr", StrUtil.isBlank(order.getTxtFlight1Carr()) ? "" : order.getTxtFlight1Carr());//第一承运人
        valueData.put("txtBy2", order.getTxtBy2() == null ? "" : order.getTxtBy2());//中转港1航班两字码
        valueData.put("txtBy3", order.getTxtBy3() == null ? "" : order.getTxtBy3());//中转港2航班两字码
        valueData.put("txtDestination", order.getTxtDestination() == null ? "" : order.getTxtDestination());//目的港
        valueData.put("txtAccountingInfo_Text", order.getTxtAccountingInfoText() == null ? "" : order.getTxtAccountingInfoText());//AccountingInfomation
        valueData.put("txtDefault_CurrCode", order.getTxtDefaultCurrCode() == null ? "" : order.getTxtDefaultCurrCode());//币种
        valueData.put("txtPC_WtgPP", order.getTxtPCWtgPP() == null ? "" : order.getTxtPCWtgPP());//到付预付运费方式
        valueData.put("txtPC_OthPP", order.getTxtPCOthPP() == null ? "" : order.getTxtPCOthPP());//到付预付杂费方式
        valueData.put("txtHandlingInfo_Text", StrUtil.isBlank(order.getTxtHandlingInfoText()) ? "" : order.getTxtHandlingInfoText());
        valueData.put("txtGoods_Desc1", StrUtil.isBlank(order.getTxtGoodsDesc1()) ? "" : order.getTxtGoodsDesc1());//品名
        valueData.put("txtGoods_Volume", order.getTxtGoodsVolume() == null ? "" : order.getTxtGoodsVolume());//体积
        valueData.put("txtRCP_Pcs1", order.getTxtRCPPcs1() == null ? "" : order.getTxtRCPPcs1());//件数
        valueData.put("txtTotal_Rcp", order.getTxtTotalRcp() == null ? "" : order.getTxtTotalRcp());//小件数
        valueData.put("txtGross_Wtg1", order.getTxtGrossWtg1() == null ? "" : order.getTxtGrossWtg1());//毛重
        valueData.put("txtDefault_WgtCode1", order.getTxtDefaultWgtCode1() == null ? "" : order.getTxtDefaultWgtCode1());//重量单位
        valueData.put("txtChg_Wtg1", order.getTxtChgWtg1() == null ? "" : order.getTxtChgWtg1());//计重
        valueData.put("txtRate_Class1", order.getTxtRateClass1() == null ? "" : order.getTxtRateClass1());//运价等级
        valueData.put("txtRate_Chg_Dis1", order.getTxtRateChgDis1() == null ? "" : order.getTxtRateChgDis1());//费率
        valueData.put("txtTotal_Chg1", order.getTxtTotalChg1() == null ? "" : order.getTxtTotalChg1());//运费合计
        valueData.put("txtGoods_Size", order.getTxtGoodsSize() == null ? "" : order.getTxtGoodsSize());//唛头
        valueData.put("txtTotal_Wtg_Chg_PP", order.getTxtTotalWtgChgPP() == null ? "" : order.getTxtTotalWtgChgPP());//预付-重量价值费
        valueData.put("txtTotal_Wtg_Chg_CC", order.getTxtTotalWtgChgCC() == null ? "" : order.getTxtTotalWtgChgCC());//到付-重量价值费
        valueData.put("txtChg_Due_Carr_PP", order.getTxtChgDueCarrPP() == null ? "" : order.getTxtChgDueCarrPP());//预付杂费金额
        valueData.put("txtChg_Due_Carr_CC", order.getTxtChgDueCarrCC() == null ? "" : order.getTxtChgDueCarrCC());//到付杂费金额
        valueData.put("txtShipperRemark1", order.getTxtShipperRemark1() == null ? "" : order.getTxtShipperRemark1());//发货人或代理签字
        valueData.put("txtShipperRemark2", order.getTxtShipperRemark2() == null ? "" : order.getTxtShipperRemark2());//承运代理公司名称
        valueData.put("txtTotalPP", order.getTxtTotalPP() == null ? "" : order.getTxtTotalPP());//预付总金额
        valueData.put("txtTotalCC", order.getTxtTotalCC() == null ? "" : order.getTxtTotalCC());//到付总金额
        valueData.put("txtOtherCharges1", order.getTxtOtherCharges1() == null ? "" : order.getTxtOtherCharges1());//其他杂费
        valueData.put("txtAWBBarCode", StrUtil.isBlank(order.getTxtAWBBarCode()) ? "" : "*" + order.getTxtAWBBarCode() + "*");//扫码
        valueData.put("txtChgsCode", order.getTxtChgsCode() == null ? "" : order.getTxtChgsCode());//CHGS代码
        valueData.put("txtCVD_Carriage", order.getTxtCVDCarriage() == null ? "" : order.getTxtCVDCarriage());//声明价值
        valueData.put("txtCVD_Custom", order.getTxtCVDCustom() == null ? "" : order.getTxtCVDCustom());//海关声明价值
        valueData.put("txtCVD_Insurance", order.getTxtCVDInsurance() == null ? "" : order.getTxtCVDInsurance());//保险价值
        valueData.put("txtItem_Num1", order.getTxtItemNum1() == null ? "" : order.getTxtItemNum1());//商品名编号
        valueData.put("txtVal_Chg_PP", order.getTxtValChgPP() == null ? "" : order.getTxtValChgPP());//预付-声明价值费
        valueData.put("txtVal_Chg_CC", order.getTxtValChgCC() == null ? "" : order.getTxtValChgCC());//到付-声明价值费
        valueData.put("txtTax_Chg_PP", order.getTxtTaxChgPP() == null ? "" : order.getTxtTaxChgPP());//预付-税款
        valueData.put("txtTax_Chg_CC", order.getTxtTaxChgCC() == null ? "" : order.getTxtTaxChgCC());//到付-税款
        valueData.put("txtChg_Due_Agt_PP", order.getTxtChgDueAgtPP() == null ? "" : order.getTxtChgDueAgtPP());//预付-代理人的其它费用总额
        valueData.put("txtChg_Due_Agt_CC", order.getTxtChgDueAgtCC() == null ? "" : order.getTxtChgDueAgtCC());//到付-代理人的其它费用总额
        valueData.put("txtCCR", order.getTxtCCR() == null ? "" : order.getTxtCCR());//汇率
        valueData.put("txtCDC", order.getTxtCDC() == null ? "" : order.getTxtCDC());//到付费用(目的国货币)
        valueData.put("txtChg_Dest", order.getTxtChgDest() == null ? "" : order.getTxtChgDest());//目的国收费
        valueData.put("txtTot_Coll", order.getTxtTotColl() == null ? "" : order.getTxtTotColl());//到付费用总计
        valueData.put("txtTo1", StrUtil.isBlank(order.getTxtDestination()) ? "" : order.getTxtDestination());//
        valueData.put("txtTo2", StrUtil.isBlank(order.getTxtTo2()) ? "" : order.getTxtTo2());
        valueData.put("txtTo3", StrUtil.isBlank(order.getTxtTo3()) ? "" : order.getTxtTo3());
        String destinationCode = StrUtil.isBlank(order.getTxtTo3()) ? StrUtil.isBlank(order.getTxtTo2()) ? StrUtil.isBlank(order.getTxtDestination()) ? "" : order.getTxtDestination() : order.getTxtTo2() : order.getTxtTo3();
        if (StrUtil.isBlank(destinationCode)) {
            valueData.put("txtDestination", "");//目的港
        } else {
            String txtDestination = afOrderMapper.getApNameEnByCode(destinationCode);
            valueData.put("txtDestination", txtDestination);//目的港
        }
        valueData.put("txtFlight2_Carr", StrUtil.isBlank(order.getTxtFlight2Carr()) ? "" : order.getTxtFlight2Carr());//航班号
        valueData.put("txtFlight3_Carr", StrUtil.isBlank(order.getTxtFlight3Carr()) ? "" : order.getTxtFlight3Carr());//航班日期
        valueData.put("txtShipper_Name", StrUtil.isBlank(order.getTxtShipperName()) ? "" : order.getTxtShipperName());//发货人
        valueData.put("txtConsignee_Name", StrUtil.isBlank(order.getTxtConsigneeName()) ? "" : order.getTxtConsigneeName());//收货人
        //打印运单确认件字段赋值结束

        //填充每个PDF
        PDFUtils.loadPDF2(templatePath, newPDFPath, valueData, true, false);
        return newPDFPath.replace(replacePath, "");
    }


    @Override
    public String checkLetters(String awbUUIds) {
        awbUUIds = "'" + awbUUIds.replaceAll(",", "','") + "'";
        List<Letters> letterList = baseMapper.checkLetters(SecurityUtils.getUser().getOrgId(), awbUUIds);
        String messge2 = "";
        String messge1 = "";
        for (int i = 0; i < letterList.size(); i++) {
            Letters letter = letterList.get(i);
            if (letter.getWarehouseNameCn() == null || "".equals(letter.getWarehouseNameCn())) {
                messge1 = messge1 + letter.getInput001() + ",";
            }
            if (letter.getLetterPdf() == null || "".equals(letter.getLetterPdf())) {
                messge2 = messge2 + letter.getInput001() + ",";
            }
        }
        if (messge1.length() > 1) {
            return messge1 + "没有选择交货货站，无法打印交货托书";
        }
        if (messge2.length() > 1) {
            //如果是多选只要有一个没有托书模板则不许打印
            if (awbUUIds.indexOf(",") != -1) {
                return messge2 + "没有可使用的托书模板，无法打印交货托书";
            } else {
                //根据货站的机场代码查询是否有模板
                List<WarehouseLetter> warehouseLetterList = baseMapper.checkWarehouseLetter(SecurityUtils.getUser().getOrgId(), awbUUIds);
                if (warehouseLetterList != null && warehouseLetterList.size() > 0) {
                    if (warehouseLetterList.size() == 1) {
                        return "当前机场有一个可使用的托书模板";
                    } else {
                        return "当前机场有多个可使用的托书模板";
                    }
                } else {
                    return "当前机场没有可使用的托书模板，请联系翌飞管理员";
                }
            }
        }
        return "货站存在托书模板";
    }

    @Override
    public String isExistExcelTemplate(String awbUUIds) {
        awbUUIds = "'" + awbUUIds.replaceAll(",", "','") + "'";
        List<Letters> letterList = baseMapper.isExistExcelTemplate(SecurityUtils.getUser().getOrgId(), awbUUIds);
        if (letterList != null && letterList.size() > 0) {
            for (int i = 0; i < letterList.size(); i++) {
                Letters letter = letterList.get(i);
                if (letter.getLetterExcel() != null && !"".equals(letter.getLetterExcel())) {
                    return "exist";
                }
            }
        }
        return "unExist";
    }

    @Override
    public IPage<WarehouseLetter> selectTemplate(Page page, OperationPlan bean) {
        String awbUuid = "'" + bean.getAwbUuid().replaceAll(",", "','") + "'";
        return baseMapper.selectTemplate(page, SecurityUtils.getUser().getOrgId(), awbUuid);
    }

    @Override
    public List<OperationPlan> exportOperationPlanExcel(OperationPlan bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        return baseMapper.exportOperationPlanExcel(bean);
    }

    /**
     * 根据主单号生成文件, 规则为：固定字符串(Tag-) + 企业ID + 6位时间 + 主单号前6位
     *
     * @param awbNumber 主单号
     * @return
     */
    private static String getFileName(String awbNumber) {
        StringBuilder builder = new StringBuilder(32);
        builder.append(FilePathUtils.filePath);
        builder.append("/PDFtemplate/temp/Tag-");
        builder.append(SecurityUtils.getUser().getOrgId());
        builder.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
        builder.append(awbNumber.substring(0, 7).replace("-", ""));
        builder.append(".pdf");
        return builder.toString();
    }


    private String printTag(List<OperationPlanPrintTag> tagList) throws Exception {
        Assert.notEmpty(tagList, "未获取到相关标签数据");
        long startTime = System.currentTimeMillis();
        Map<String, Object> data = new HashMap<>(1);
        data.put("APP_CONTEXT_KEY_TAG-DATA_SET", tagList);
        String destFilePath = getFileName(tagList.get(0).getAwbNumber());
        birtComponent.pdfReport("ef-tag.rptdesign", data, new File(destFilePath));
        long endTime = System.currentTimeMillis();
        System.out.println("BIRT print tag use time:" + (endTime - startTime) / 1000.0 + "s");
        return destFilePath.replace(FilePathUtils.filePath, "");
    }
}
