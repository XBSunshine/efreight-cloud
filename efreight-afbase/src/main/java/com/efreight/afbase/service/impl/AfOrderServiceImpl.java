package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.*;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.entity.cargo.track.CargoRoute;
import com.efreight.afbase.entity.cargo.track.CargoTrack;
import com.efreight.afbase.entity.cargo.track.CargoTrackQuery;
import com.efreight.afbase.entity.procedure.AfPAwbSubmitPrintProcedure;
import com.efreight.afbase.entity.procedure.AirCargoManifestPrint;
import com.efreight.afbase.entity.route.AfAwbRouteTrackAwb;
import com.efreight.afbase.entity.route.AfAwbRouteTrackManifest;
import com.efreight.afbase.entity.shipping.APIType;
import com.efreight.afbase.entity.shipping.ShippingBillData;
import com.efreight.afbase.entity.view.*;
import com.efreight.afbase.service.*;
import com.efreight.afbase.utils.FilePathUtils;
import com.efreight.afbase.utils.LoginUtils;
import com.efreight.afbase.utils.PDFUtils;
import com.efreight.afbase.utils.RemoteSendUtils;
import com.efreight.common.core.feign.RemoteCoopService;
import com.efreight.common.core.feign.RemoteServiceToHRS;
import com.efreight.common.core.feign.RemoteServiceToSC;
import com.efreight.common.core.jms.MailSendService;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.core.utils.JxlsUtils;
import com.efreight.common.remoteVo.OrderForVL;
import com.efreight.common.security.constant.CommonConstants;
import com.efreight.common.security.constant.SecurityConstants;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.common.security.vo.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class AfOrderServiceImpl extends ServiceImpl<AfOrderMapper, AfOrder> implements AfOrderService {
    private final AwbNumberService awbservice;
    private final LogService logService;
    private final AfOrderMapper afOrderMapper;
    private final ScLogService scLogService;
    private final TcLogService tcLogService;
    private final AfShipperLetterService afShipperLetterService;
    private final AirportService airportService;
    private final AfOrderShipperConsigneeMapper afOrderShipperConsigneeMapper;
    private final AfOrderShipperConsigneeService afOrderShipperConsigneeService;
    private final OrderFilesMapper orderFilesMapper;
    private final AfIncomeMapper afIncomeMapper;
    private final AfCostMapper afCostMapper;
    private final MailSendService mailSendService;
    private final AfAwbRouteMapper afAwbRouteMapper;
    private final RemoteServiceToHRS remoteServiceToHRS;
    private final RemoteCoopService remoteCoopService;
    private final CssPaymentMapper cssPaymentMapper;
    private final AfAwbRouteTrackAwbService afAwbRouteTrackAwbService;
    private final ScCostService scCostService;
    private final ScIncomeService scIncomeService;
    private final ScOrderService scOrderService;
    public static String filePath = FilePathUtils.filePath;
    private final WarehouseService warehouseService;
    private final AfAwbRouteTrackManifestService afAwbRouteTrackManifestService;
    private final OrderInquiryService orderInquiryService;
    private final OrderInquiryQuotationService orderInquiryQuotationService;

    private final TcIncomeMapper tcIncomeService;
    private final TcCostMapper tcCostService;
    private final TcOrderService tcOrderService;
    private final LcIncomeService lcIncomeService;
    private final IoIncomeService ioIncomeService;
    private final LcCostService lcCostService;
    private final IoCostService ioCostService;
    private final LcOrderService lcOrderService;
    private final IoOrderService ioOrderService;
    private final AfOrderShareService afOrderShareService;
    private final RemoteServiceToSC remoteServiceToSC;
    private final AfAwbRouteService awbRouteService;
    private final AwbSubscriptionService awbSubscriptionService;
    private final AfRountingSignMapper afRountingSignMapper;


    @Override
    public IPage<AfOrder> getListPage(Page page, AfOrder bean) {

        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        bean.setCurrentUserId(SecurityUtils.getUser().getId());
        IPage<AfOrder> afPage = baseMapper.getListPage(page, bean);
        if (afPage != null && afPage.getRecords().size() > 0) {
            DecimalFormat decimalFormat = new DecimalFormat("##########.##########");
            afPage.getRecords().stream().forEach(order -> {
                order.setPlanWeight(new BigDecimal(decimalFormat.format(order.getPlanWeight() != null ? order.getPlanWeight() : BigDecimal.ZERO)));
                order.setConfirmWeight(new BigDecimal(decimalFormat.format(order.getConfirmWeight() != null ? order.getConfirmWeight() : BigDecimal.ZERO)));
                order.setPlanVolume(Double.valueOf(decimalFormat.format(order.getPlanVolume() != null ? order.getPlanVolume() : 0)));
                order.setConfirmVolume(Double.valueOf(decimalFormat.format(order.getConfirmVolume() != null ? order.getConfirmVolume() : 0)));
                order.setPlanChargeWeight(Double.valueOf(decimalFormat.format(order.getPlanChargeWeight() != null ? order.getPlanChargeWeight() : 0)));
                order.setConfirmChargeWeight(Double.valueOf(decimalFormat.format(order.getConfirmChargeWeight() != null ? order.getConfirmChargeWeight() : 0)));
                if ("AI".equals(bean.getBusinessScope())) {
                    if (StrUtil.isNotBlank(order.getAwbNumber()) && StrUtil.isNotBlank(order.getHawbNumber())) {
                        order.setAwbNumber(order.getAwbNumber() + " / " + order.getHawbNumber());
                    } else if (StrUtil.isNotBlank(order.getHawbNumber())) {
                        order.setAwbNumber(order.getHawbNumber());
                    }
                }
                //设置收入完成和成本完成（排序使用）
                if (order.getIncomeRecorded() == true) {
                    order.setIncomeRecordedForSort(2);
                } else if (StringUtils.isNotBlank(order.getIncomeStatus()) && !"未录收入".equals(order.getIncomeStatus())) {
                    order.setIncomeRecordedForSort(1);
                } else {
                    order.setIncomeRecordedForSort(0);
                }
                if (order.getCostRecorded() == true) {
                    order.setCostRecordedForSort(2);
                } else if (StringUtils.isNotBlank(order.getCostStatus()) && !"未录成本".equals(order.getCostStatus())) {
                    order.setCostRecordedForSort(1);
                } else {
                    order.setCostRecordedForSort(0);
                }
            });
        }
        return afPage;
    }

    @Override
    public List<AIOrder> exportAiExcel(AfOrder bean) {

        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        bean.setCurrentUserId(SecurityUtils.getUser().getId());
        List<AIOrder> list = baseMapper.exportAiExcel(bean);
        if (list.size() > 0) {
            List<AIOrder> listSum = baseMapper.exportAiExcelSUM(bean);
            list.add(listSum.get(0));
        }
        return list;
    }

    @Override
    public List<AEOrder> exportAeExcel(AfOrder bean) {

        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        bean.setCurrentUserId(SecurityUtils.getUser().getId());
        List<AEOrder> list = baseMapper.exportAeExcel(bean);
        if (list.size() > 0) {
            DecimalFormat decimalFormat = new DecimalFormat("#,###,###,###.##########");
            list.stream().forEach(order -> {
                order.setPlanPieces(decimalFormat.format(order.getPlanPieces() != null ? Double.valueOf(order.getPlanPieces()) : 0));
                order.setPlanDensity(decimalFormat.format(order.getPlanDensity() != null ? Double.valueOf(order.getPlanDensity()) : 0));
                order.setPlanWeight(decimalFormat.format(order.getPlanWeight() != null ? Double.valueOf(order.getPlanWeight()) : 0));
                order.setPlanVolume(decimalFormat.format(order.getPlanVolume() != null ? Double.valueOf(order.getPlanVolume()) : 0));
                order.setPlanChargeWeight(decimalFormat.format(order.getPlanChargeWeight() != null ? Double.valueOf(order.getPlanChargeWeight()) : 0));
                if (!"-".equals(order.getConfirmPieces())) {
                    order.setConfirmPieces(decimalFormat.format(Double.valueOf(order.getConfirmPieces())));
                }
                if (!"-".equals(order.getConfirmDensity())) {
                    order.setConfirmDensity(decimalFormat.format(Double.valueOf(order.getConfirmDensity())));
                }
                if (!"-".equals(order.getConfirmWeight())) {
                    order.setConfirmWeight(decimalFormat.format(Double.valueOf(order.getConfirmWeight())));
                }
                if (!"-".equals(order.getConfirmVolume())) {
                    order.setConfirmVolume(decimalFormat.format(Double.valueOf(order.getConfirmVolume())));
                }
                if (!"-".equals(order.getConfirmChargeWeight())) {
                    order.setConfirmChargeWeight(decimalFormat.format(Double.valueOf(order.getConfirmChargeWeight())));
                }
            });
            List<AEOrder> listSum = baseMapper.exportAeExcelSUM(bean);
            AEOrder aeOrder = listSum.get(0);
            aeOrder.setPlanWeight(decimalFormat.format(aeOrder.getPlanWeight() != null ? Double.valueOf(aeOrder.getPlanWeight()) : 0));
            aeOrder.setPlanVolume(decimalFormat.format(aeOrder.getPlanVolume() != null ? Double.valueOf(aeOrder.getPlanVolume()) : 0));
            aeOrder.setPlanChargeWeight(decimalFormat.format(aeOrder.getPlanChargeWeight() != null ? Double.valueOf(aeOrder.getPlanChargeWeight()) : 0));
            aeOrder.setPlanPieces(decimalFormat.format(aeOrder.getPlanPieces() != null ? Double.valueOf(aeOrder.getPlanPieces()) : 0));
            aeOrder.setPlanDensity(decimalFormat.format(aeOrder.getPlanDensity() != null ? Double.valueOf(aeOrder.getPlanDensity()) : 0));
            if (!"-".equals(aeOrder.getConfirmWeight())) {
                aeOrder.setConfirmWeight(decimalFormat.format(Double.valueOf(aeOrder.getConfirmWeight())));
            }
            if (!"-".equals(aeOrder.getConfirmVolume())) {
                aeOrder.setConfirmVolume(decimalFormat.format(Double.valueOf(aeOrder.getConfirmVolume())));
            }
            if (!"-".equals(aeOrder.getConfirmChargeWeight())) {
                aeOrder.setConfirmChargeWeight(decimalFormat.format(Double.valueOf(aeOrder.getConfirmChargeWeight())));
            }
            if (!"-".equals(aeOrder.getConfirmPieces())) {
                aeOrder.setConfirmPieces(decimalFormat.format(Double.valueOf(aeOrder.getConfirmPieces())));
            }
            if (!"-".equals(aeOrder.getConfirmDensity())) {
                aeOrder.setConfirmDensity(decimalFormat.format(Double.valueOf(aeOrder.getConfirmDensity())));
            }
            list.add(aeOrder);
        }
        return list;
    }

    @Override
    public List<AfOrder> getTatol(AfOrder bean) {

        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        bean.setCurrentUserId(SecurityUtils.getUser().getId());
        List<AfOrder> list = baseMapper.getTatol(bean);
        List<AfOrder> tatolList = new ArrayList<AfOrder>();
        AfOrder order = new AfOrder();

        Integer planPieces = 0;
        BigDecimal planWeight = new BigDecimal(0);
        BigDecimal planVolume = new BigDecimal(0);
//        Double planVolume = 0.0;  不要用double做这种计算  会有精度缺失问题
        BigDecimal planChargeWeight = new BigDecimal(0);
//        Double planChargeWeight = 0.0;
        Integer confirmPieces = 0;
        BigDecimal confirmWeight = new BigDecimal(0);
        BigDecimal confirmVolume = new BigDecimal(0);
//        Double confirmVolume = 0.0;
        BigDecimal confirmChargeWeight = new BigDecimal(0);
//        Double confirmChargeWeight = 0.0;
        for (int i = 0; i < list.size(); i++) {
            AfOrder afOrder = list.get(i);
            if (afOrder.getPlanPieces() != null) {
                planPieces = planPieces + afOrder.getPlanPieces();
            }
            if (afOrder.getPlanWeight() != null) {
                planWeight = planWeight.add(afOrder.getPlanWeight());
            }
            if (afOrder.getPlanVolume() != null) {
                planVolume = planVolume.add(new BigDecimal(afOrder.getPlanVolume()));
            }
            if (afOrder.getPlanChargeWeight() != null) {
                planChargeWeight = planChargeWeight.add(new BigDecimal(afOrder.getPlanChargeWeight()));
            }
            if (afOrder.getConfirmPieces() != null) {
                confirmPieces = confirmPieces + afOrder.getConfirmPieces();
            }
            if (afOrder.getConfirmWeight() != null) {
                confirmWeight = confirmWeight.add(afOrder.getConfirmWeight());
            }
            if (afOrder.getConfirmVolume() != null) {
                confirmVolume = confirmVolume.add(new BigDecimal(afOrder.getConfirmVolume()));
            }
            if (afOrder.getConfirmChargeWeight() != null) {
                confirmChargeWeight = confirmChargeWeight.add(new BigDecimal(afOrder.getConfirmChargeWeight()));
            }
        }
        DecimalFormat decimalFormat = new DecimalFormat("##########.##########");
        order.setAwbNumber("合计");
        order.setPlanPieces(planPieces);
        order.setPlanWeight(new BigDecimal(decimalFormat.format(planWeight)));
        order.setPlanVolume(Double.valueOf(decimalFormat.format(planVolume)));
        order.setPlanChargeWeight(Double.valueOf(decimalFormat.format(planChargeWeight)));

        order.setConfirmPieces(confirmPieces);
        order.setConfirmWeight(new BigDecimal(decimalFormat.format(confirmWeight)));
        order.setConfirmVolume(Double.valueOf(decimalFormat.format(confirmVolume)));
        order.setConfirmChargeWeight(Double.valueOf(decimalFormat.format(confirmChargeWeight)));
        tatolList.add(order);
        return tatolList;
    }

    @Override
    public IPage<VPrmCoop> selectCoop(Page page, VPrmCoop bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        return baseMapper.selectCoop(page, bean);
    }

    @Override
    public IPage<VPrmCoop> selectAICoop(Page page, VPrmCoop bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        return baseMapper.selectAICoop(page, bean);
    }

    @Override
    public IPage<VPrmCoop> selectPrmCoop(Page page, VPrmCoop bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        return baseMapper.selectPrmCoop(page, bean);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AfOrder doSave(AfOrder bean) {
        if (StringUtils.isNotBlank(bean.getExpectFlight())) {
            String expectFlight = bean.getExpectFlight();
            if (expectFlight.length() > 2) {
                expectFlight = bean.getExpectFlight().substring(0, 2);
            }
        }
        EUserDetails user = SecurityUtils.getUser();
        Integer orgId = user.getOrgId();
        String awbNumber = bean.getAwbNumber();
        int isHaveAWB = 0;
        bean.setOrderStatus("订单创建");
        if (bean.getAwbNumber() != null && !"".equals(bean.getAwbNumber())) {
            List<AwbNumber> awbList = baseMapper.selectAwb(orgId, awbNumber);

            if (awbList.size() == 0) {//主单号不存在
                checkCoop(bean, user, orgId, awbNumber);
            } else {
                bean.setAwbId(awbList.get(0).getAwbId());
                bean.setAwbUuid(awbList.get(0).getAwbUuid());
                //状态
                baseMapper.updateAwbStatus(awbList.get(0).getAwbId(), "已配单", orgId);
            }

            isHaveAWB = 1;
            bean.setOrderStatus("舱位确认");
        }
        if (bean.getDeliverySignDate() != null) {
            bean.setOrderStatus("目的港签收");
        } else if (bean.getArrivalCustomsClearanceDate() != null) {
            bean.setOrderStatus("目的港放行");
        } else if (bean.getArrivalCustomsInspectionDate() != null) {
            bean.setOrderStatus("目的港查验");
        } else if (bean.getCustomsClearanceDate() != null) {
            bean.setOrderStatus("海关放行");
        } else if (bean.getCustomsInspectionDate() != null) {
            bean.setOrderStatus("海关查验");
        } else if (isHaveAWB == 1) {
            bean.setOrderStatus("舱位确认");
        } else {
            bean.setOrderStatus("订单创建");
        }
        //生成订单号
        String code = getOrderCode();
        List<AfOrder> codeList = baseMapper.selectCode(orgId, code);

        if (codeList.size() == 0) {
            bean.setOrderCode(code + "0001");
        } else if (codeList.size() < 9999) {
            bean.setOrderCode(code + String.format("%04d", codeList.size() + 1));
        } else {
            throw new RuntimeException("每天最多可以创建9999个AE订单");
        }
        //客户单号
        if (bean.getCustomerNumber() == null || "".equals(bean.getCustomerNumber().trim())) {
            bean.setCustomerNumber(bean.getOrderCode());
        }
        bean.setRowUuid(UUID.randomUUID().toString());
        bean.setOrderUuid(baseMapper.getUUID());
        bean.setCreateTime(new Date());
        bean.setCreatorId(user.getId());
        bean.setCreatorName(user.getUserCname() + " " + user.getUserEmail());
        bean.setOrgId(orgId);
        bean.setBusinessScope("AE");
        bean.setIncomeStatus("未录收入");
        bean.setCostStatus("未录成本");
        bean.setIncomeRecorded(false);
        bean.setCostRecorded(false);

//        if ("放单业务".equals(bean.getBusinessProduct())) {
//            bean.setConfirmPieces(bean.getPlanPieces());
//            bean.setConfirmWeight(bean.getPlanWeight());
//            bean.setConfirmVolume(bean.getPlanVolume());
//            bean.setConfirmChargeWeight(bean.getPlanChargeWeight());
//        }
        baseMapper.insert(bean);

        //插入出口订单 附属表
        baseMapper.insertOrderExtend(orgId, bean);

        //联系人
        for (int i = 0; i < bean.getOrderContacts().size(); i++) {
            baseMapper.insertOrderContacts(orgId, bean.getOrderId(), bean.getOrderContacts().get(i));
        }
        //收发货人
        AfOrderShipperConsignee afOrderShipperConsignee = bean.getAfOrderShipperConsignee1();
        if (afOrderShipperConsignee != null) {
            afOrderShipperConsignee.setOrderId(bean.getOrderId());
            afOrderShipperConsignee.setCreateTime(LocalDateTime.now());
            afOrderShipperConsignee.setCreatorId(user.getId());
            afOrderShipperConsignee.setCreatorName(user.getUserCname() + " " + user.getUserEmail());
            afOrderShipperConsignee.setOrgId(orgId);
            afOrderShipperConsigneeMapper.insert(afOrderShipperConsignee);
        }
        AfOrderShipperConsignee afOrderShipperConsignee2 = bean.getAfOrderShipperConsignee2();
        if (afOrderShipperConsignee2 != null) {
            afOrderShipperConsignee2.setOrderId(bean.getOrderId());
            afOrderShipperConsignee2.setCreateTime(LocalDateTime.now());
            afOrderShipperConsignee2.setCreatorId(user.getId());
            afOrderShipperConsignee2.setCreatorName(user.getUserCname() + " " + user.getUserEmail());
            afOrderShipperConsignee2.setOrgId(orgId);
            afOrderShipperConsigneeMapper.insert(afOrderShipperConsignee2);
        }
        //添加日志信息

        LogBean logBean = new LogBean();
        logBean.setPageName("AE订单");
        logBean.setPageFunction("订单创建");
        logBean.setBusinessScope("AE");
        logBean.setOrderNumber(bean.getOrderCode());
        logBean.setOrderId(bean.getOrderId());
        logBean.setOrderUuid(bean.getOrderUuid());
        logService.saveLog(logBean);
        if (isHaveAWB == 1) {
            LogBean logBean2 = new LogBean();
            logBean2.setPageName("AE订单");
            logBean2.setPageFunction("舱位确认");
            logBean2.setLogRemark("主单：" + bean.getAwbNumber());
            logBean2.setBusinessScope("AE");
            logBean2.setOrderNumber(bean.getOrderCode());
            logBean2.setOrderId(bean.getOrderId());
            logBean2.setOrderUuid(bean.getOrderUuid());
            logService.saveLog(logBean2);
        }
        if (bean.getAwbNumber() != null && !"".equals(bean.getAwbNumber())) {
            //自动生成 收入、成本
            baseMapper.createIncomeAndCost(bean.getOrderUuid(), orgId);
        }

        if (bean.getAwbNumber() != null && !"".equals(bean.getAwbNumber())) {
            //创建订单时 查询是否运单表中存在主单号，若不存在则插入 运单表
            LambdaQueryWrapper<AfAwbRoute> afAwbRouteWrapper = new LambdaQueryWrapper<AfAwbRoute>();
            afAwbRouteWrapper.eq(AfAwbRoute::getAwbNumber, bean.getAwbNumber());
            AfAwbRoute route = afAwbRouteMapper.selectOne(afAwbRouteWrapper);
            if (route == null) {
                AfAwbRoute insertRoute = new AfAwbRoute();
                insertRoute.setAwbNumber(bean.getAwbNumber());
//        		insertRoute.setActualFlightNum(bean.getExpectFlight());
//        		insertRoute.setDepartureStation(bean.getDepartureStation());
//        		insertRoute.setArrivalStation(bean.getArrivalStation());
//        		insertRoute.setActualDeparture(bean.getExpectDeparture());
//        		insertRoute.setActualArrival(bean.getExpectArrival());
//        		insertRoute.setGrossWeight(bean.getPlanWeight());
//        		insertRoute.setQuantity(bean.getPlanPieces());
                insertRoute.setCreateTime(LocalDateTime.now());
                insertRoute.setIsTrack(0);
                afAwbRouteMapper.insert(insertRoute);
            }

        }

        //插入分单信息
        if (bean.getShipperLetters().size() > 0) {
            bean.getShipperLetters().stream().forEach(afShipperLetter -> {
                afShipperLetter.setOrderId(bean.getOrderId());
                afShipperLetter.setOrgId(orgId);
                afShipperLetter.setSlType("HAWB");
                afShipperLetter.setOrderUuid(bean.getOrderUuid());
                afShipperLetter.setCreateTime(LocalDateTime.now());
                afShipperLetter.setCreatorId(user.getId());
                afShipperLetter.setCreatorName(user.getUserCname() + " " + user.getUserEmail());

                afShipperLetter.setEditTime(LocalDateTime.now());
                afShipperLetter.setEditorId(user.getId());
                afShipperLetter.setEditorName(user.getUserCname() + " " + user.getUserEmail());

            });
            afShipperLetterService.saveBatch(bean.getShipperLetters());

            //新增分单收发货人
            ArrayList<AfOrderShipperConsignee> afOrderConsigneeList = new ArrayList<>();
            ArrayList<AfOrderShipperConsignee> afOrderShipperList = new ArrayList<>();
            bean.getShipperLetters().stream().forEach(afShipperLetter -> {
                AfOrderShipperConsignee afOrderConsignee = afShipperLetter.getAfOrderShipperConsignee1();
                if (afOrderConsignee != null) {
                    afOrderConsignee.setOrderId(afShipperLetter.getOrderId());
                    afOrderConsignee.setSlId(afShipperLetter.getSlId());
                    afOrderConsignee.setCreateTime(LocalDateTime.now());
                    afOrderConsignee.setCreatorId(user.getId());
                    afOrderConsignee.setCreatorName(user.getUserCname() + " " + user.getUserEmail());
                    afOrderConsignee.setOrgId(orgId);
                    afOrderConsigneeList.add(afOrderConsignee);
                }
                AfOrderShipperConsignee afOrderShipper = afShipperLetter.getAfOrderShipperConsignee2();
                if (afOrderShipper != null) {
                    afOrderShipper.setSlId(afShipperLetter.getSlId());
                    afOrderShipper.setOrderId(afShipperLetter.getOrderId());
                    afOrderShipper.setCreateTime(LocalDateTime.now());
                    afOrderShipper.setCreatorId(user.getId());
                    afOrderShipper.setCreatorName(user.getUserCname() + " " + user.getUserEmail());
                    afOrderShipper.setOrgId(orgId);
                    afOrderShipperList.add(afOrderShipper);
                }
            });
            afOrderShipperConsigneeService.saveBatch(afOrderConsigneeList);
            afOrderShipperConsigneeService.saveBatch(afOrderShipperList);
        }

        //更新询价单
        if (bean.getOrderInquiryId() != null) {
            OrderInquiry orderInquiry = orderInquiryService.getById(bean.getOrderInquiryId());
            if (!orderInquiry.getRowUuid().equals(bean.getOrderInquiryRowUuid())) {
                throw new RuntimeException("询价单数据被修改，请刷新页面");
            }
            if (orderInquiry == null) {
                throw new RuntimeException("询价单不存在，无法保存");
            }
            if ("已转订单".equals(orderInquiry.getOrderInquiryStatus())) {
                throw new RuntimeException("询价单已转订单，无法保存");
            }
            if ("已关闭".equals(orderInquiry.getOrderInquiryStatus())) {
                throw new RuntimeException("询价单已关闭，无法保存");
            }
            orderInquiry.setOrderInquiryStatus("已转订单");
            orderInquiry.setEditorId(user.getId());
            orderInquiry.setEditorName(user.getUserCname() + " " + user.getUserEmail());
            orderInquiry.setEditTime(LocalDateTime.now());
            orderInquiry.setOrderUuid(bean.getOrderUuid());
            orderInquiry.setOrderId(bean.getOrderId());
            orderInquiry.setRowUuid(UUID.randomUUID().toString());
            orderInquiryService.updateById(orderInquiry);

            OrderInquiryQuotation orderInquiryQuotation = orderInquiryQuotationService.getById(bean.getOrderInquiryQuotationId());
            LambdaQueryWrapper<OrderInquiryQuotation> orderInquiryQuotationWrapper = Wrappers.<OrderInquiryQuotation>lambdaQuery();
            orderInquiryQuotationWrapper.eq(OrderInquiryQuotation::getOrgId, orgId).eq(OrderInquiryQuotation::getIsValid, true).eq(OrderInquiryQuotation::getQuotationSelected, true).eq(OrderInquiryQuotation::getOrderInquiryId, bean.getOrderInquiryId());
            OrderInquiryQuotation inquiryQuotation = orderInquiryQuotationService.getOne(orderInquiryQuotationWrapper);
            if (orderInquiryQuotation == null) {
                throw new RuntimeException("报价方案不存在，无法保存");
            }
            if (!orderInquiryQuotation.getIsValid()) {
                throw new RuntimeException("报价方案已失效，无法保存");
            }
            if (!orderInquiryQuotation.getQuotationSelected()) {
                orderInquiryQuotation.setQuotationSelected(true);
                orderInquiryQuotation.setEditorId(user.getId());
                orderInquiryQuotation.setEditorName(user.getUserCname() + " " + user.getUserEmail());
                orderInquiryQuotation.setEditTime(LocalDateTime.now());
                orderInquiryQuotationService.updateById(orderInquiryQuotation);
                if (inquiryQuotation != null) {
                    inquiryQuotation.setQuotationSelected(false);
                    inquiryQuotation.setEditorId(user.getId());
                    inquiryQuotation.setEditorName(user.getUserCname() + " " + user.getUserEmail());
                    inquiryQuotation.setEditTime(LocalDateTime.now());
                    orderInquiryQuotationService.updateById(inquiryQuotation);
                }
            }

        }
        //更新分享 协作单   当前用户订单ID
        if (bean.getOrderShareOrgId() != null) {
            LambdaQueryWrapper<AfOrderShare> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(AfOrderShare::getShareScope, "订单协作").eq(AfOrderShare::getBusinessScope, "AE").eq(AfOrderShare::getProcess, "out").eq(AfOrderShare::getOrgId, bean.getOrderShareOrgId());
            wrapper.eq(AfOrderShare::getOrderId, bean.getOrderShareOrderId()).eq(AfOrderShare::getShareCoopId, bean.getOrderShareCoopId());
            AfOrderShare aos = afOrderShareService.getOne(wrapper);
            aos.setShareOrderId(bean.getOrderId());
            aos.setEditorId(user.getId());
            aos.setEditorName(user.buildOptName());
            aos.setEditTime(LocalDateTime.now());
            afOrderShareService.updateById(aos);
            //插入新订单  分享协作
            AfOrderShare updateAos = new AfOrderShare();
            updateAos.setShareScope("订单协作");
            updateAos.setProcess("in");
            updateAos.setOrderId(bean.getOrderId());
            updateAos.setOrgId(orgId);
            updateAos.setBusinessScope("AE");
            updateAos.setShareCoopId(bean.getCoopOrgCoopId());
            updateAos.setShareOrgId(bean.getOrderShareOrgId());
            updateAos.setShareOrderId(bean.getOrderShareOrderId());
            updateAos.setCreateTime(LocalDateTime.now());
            updateAos.setCreatorId(user.getId());
            updateAos.setCreatorName(user.buildOptName());
            afOrderShareService.save(updateAos);
            //日志
            LogBean logBeanShare = new LogBean();
            logBeanShare.setPageName("AE订单");
            logBeanShare.setPageFunction("订单协作");
            logBeanShare.setBusinessScope("AE");
            logBeanShare.setOrderNumber(bean.getOrderCode());
            logBeanShare.setOrderId(bean.getOrderId());
            logBeanShare.setOrderUuid(bean.getOrderUuid());
            CoopVo coopVo = remoteCoopService.viewCoop(bean.getCoopId().toString()).getData();
            if (coopVo != null) {
                logBeanShare.setLogRemark("订单协作接收：" + coopVo.getCoop_name());
            }
            logService.saveLog(logBeanShare);
        }
        //保存签单信息
        AfRountingSign afRountingSign = new AfRountingSign();
        afRountingSign.setOrderId(bean.getOrderId());
        afRountingSign.setOrgId(orgId);
        afRountingSign.setBusinessScope("AE");
        afRountingSign.setSignState(0);
        afRountingSign.setRowUuid(UUID.randomUUID().toString());
        afRountingSign.setEditorId(user.getId());
        afRountingSign.setEdit_time(new Date());
        afRountingSign.setEditorName(user.getUserCname() + " " + user.getUserEmail());
        //查询当前汇率
        BigDecimal currencyRate = null;
        if (!"".equals(bean.getMsrCurrecnyCode())) {
            currencyRate = baseMapper.getCurrencyRate(orgId, bean.getMsrCurrecnyCode());
        }
        if (bean.getMsrUnitprice() != null) {
            if (currencyRate != null) {
                BigDecimal msrUnitprice = BigDecimal.valueOf(bean.getMsrUnitprice()).multiply(currencyRate);
                afRountingSign.setMsrUnitprice(msrUnitprice.setScale(2, BigDecimal.ROUND_HALF_UP));
                BigDecimal msrAmountWriteoff = BigDecimal.valueOf(bean.getPlanChargeWeight()).multiply(msrUnitprice);
                afRountingSign.setMsrAmountWriteoff(msrAmountWriteoff.setScale(2, BigDecimal.ROUND_HALF_UP));
            } else {
                afRountingSign.setMsrUnitprice(new BigDecimal("0.00"));
                afRountingSign.setMsrAmountWriteoff(new BigDecimal("0.00"));
            }
            afRountingSign.setIncomeWeight(bean.getPlanChargeWeight());
        } else if (bean.getMsrAmount() != null) {
            if (currencyRate != null) {
                BigDecimal msrUnitprice = BigDecimal.valueOf(bean.getMsrAmount()).multiply(currencyRate);
                afRountingSign.setMsrUnitprice(msrUnitprice.setScale(2, BigDecimal.ROUND_HALF_UP));
                afRountingSign.setMsrAmountWriteoff(msrUnitprice.setScale(2, BigDecimal.ROUND_HALF_UP));
            } else {
                afRountingSign.setMsrUnitprice(new BigDecimal("0.00"));
                afRountingSign.setMsrAmountWriteoff(new BigDecimal("0.00"));
            }
            afRountingSign.setIncomeWeight(1.00);
        } else {
            afRountingSign.setMsrUnitprice(new BigDecimal("0.00"));
            afRountingSign.setMsrAmountWriteoff(new BigDecimal("0.00"));
            afRountingSign.setIncomeWeight(bean.getPlanChargeWeight());
        }
        afRountingSignMapper.insert(afRountingSign);
        return bean;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AfOrder doSaveAI(AfOrder bean) {
        Integer orgId = SecurityUtils.getUser().getOrgId();
//    	if (StringUtils.isNotBlank(bean.getExpectFlight())) {
//    		String expectFlight = bean.getExpectFlight();
//    		if (expectFlight.length() > 2) {
//    			expectFlight = bean.getExpectFlight().substring(0, 2);
//    		}
//    		Carrier carrier = carrierService.queryOne(expectFlight);
//    		if (carrier == null) {
//    			throw new RuntimeException("未查询到相关航司信息");
//    		}
//    	}
        int isHaveAWB = 0;
        bean.setOrderStatus("订单创建");
        bean.setIncomeStatus("未录收入");
        bean.setCostStatus("未录成本");
        bean.setIncomeRecorded(false);
        bean.setCostRecorded(false);
//    	if (bean.getAwbNumber() != null && !"".equals(bean.getAwbNumber())) {
//    		List<AwbNumber> awbList = baseMapper.selectAwb(orgId, bean.getAwbNumber());
//    		if (awbList.size() == 0) {
//    			throw new RuntimeException("主单号不存在");
//    		}
//    		bean.setAwbId(awbList.get(0).getAwbId());
//    		bean.setAwbUuid(awbList.get(0).getAwbUuid());
//    		//状态
//    		baseMapper.updateAwbStatus(awbList.get(0).getAwbId(), "已配单");
//    		isHaveAWB = 1;
//    		bean.setOrderStatus("舱位确认");
//    	}
        if (bean.getDeliverySignDate() != null) {
            bean.setOrderStatus("派送签收");
        } else if (bean.getOutboundDate() != null) {
            bean.setOrderStatus("货物出库");
        } else if (bean.getCustomsClearanceDate() != null) {
            bean.setOrderStatus("海关放行");
        } else if (bean.getCustomsInspectionDate() != null) {
            bean.setOrderStatus("海关查验");
        } else if (bean.getInboundDate() != null) {
            bean.setOrderStatus("货物入库");
        } else {
            bean.setOrderStatus("订单创建");
        }
        //生成订单号
        String code = getAICode();
        List<AfOrder> codeList = baseMapper.selectCode(orgId, code);

        if (codeList.size() == 0) {
            bean.setOrderCode(code + "0001");
        } else if (codeList.size() < 9999) {
            bean.setOrderCode(code + String.format("%04d", codeList.size() + 1));
        } else {
            throw new RuntimeException("每天最多可以创建9999个AI订单");
        }
        //客户单号
        if (bean.getCustomerNumber() == null || "".equals(bean.getCustomerNumber().trim())) {
            bean.setCustomerNumber(bean.getOrderCode());
        }
        bean.setRowUuid(UUID.randomUUID().toString());
        bean.setOrderUuid(baseMapper.getUUID());
        bean.setCreateTime(new Date());
        bean.setCreatorId(SecurityUtils.getUser().getId());
        bean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setOrgId(orgId);
        bean.setBusinessScope("AI");

        baseMapper.insert(bean);

//    	//联系人
//    	for (int i = 0; i < bean.getOrderContacts().size(); i++) {
//    		baseMapper.insertOrderContacts(orgId, bean.getOrderId(), bean.getOrderContacts().get(i));
//    	}
        //收发货人
        AfOrderShipperConsignee afOrderShipperConsignee = bean.getAfOrderShipperConsignee1();
        if (afOrderShipperConsignee != null) {
            afOrderShipperConsignee.setOrderId(bean.getOrderId());
            afOrderShipperConsignee.setCreateTime(LocalDateTime.now());
            afOrderShipperConsignee.setCreatorId(SecurityUtils.getUser().getId());
            afOrderShipperConsignee.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            afOrderShipperConsignee.setOrgId(orgId);
            afOrderShipperConsigneeMapper.insert(afOrderShipperConsignee);
        }
        AfOrderShipperConsignee afOrderShipperConsignee2 = bean.getAfOrderShipperConsignee2();
        if (afOrderShipperConsignee2 != null) {
            afOrderShipperConsignee2.setOrderId(bean.getOrderId());
            afOrderShipperConsignee2.setCreateTime(LocalDateTime.now());
            afOrderShipperConsignee2.setCreatorId(SecurityUtils.getUser().getId());
            afOrderShipperConsignee2.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            afOrderShipperConsignee2.setOrgId(orgId);
            afOrderShipperConsigneeMapper.insert(afOrderShipperConsignee2);
        }

        //日志

//        LogBean logBean = new LogBean();
//        logBean.setBusinessScope("AI");
//        logBean.setLogType("AI订单");
//        logBean.setNodeName("订单创建");
//        logBean.setPageName("AI订单");
//        logBean.setPageFunction("订单创建");
//        logBean.setOrderNumber(bean.getOrderCode());
//        logBean.setOrderUuid(bean.getOrderUuid());
//        logBean.setAwbNumber(bean.getAwbNumber());
//        logBean.setAwbUuid(bean.getAwbUuid());
//        logBean.setCreatorId(SecurityUtils.getUser().getId());
//        logBean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
//        logBean.setCreatTime(LocalDateTime.now());
//        logBean.setOrgId(orgId);
//        logMapper.insert(logBean);
        //添加日志信息
        LogBean logBean = new LogBean();
        logBean.setPageName("AI订单");
        logBean.setPageFunction("订单创建");

//        logBean.setLogRemark(reason);
        logBean.setBusinessScope("AI");
        logBean.setOrderNumber(bean.getOrderCode());
        logBean.setOrderId(bean.getOrderId());
        logBean.setOrderUuid(bean.getOrderUuid());
        logService.saveLog(logBean);

//    	if (bean.getAwbNumber() != null && !"".equals(bean.getAwbNumber())) {
//    		//自动生成 收入、成本
//    		baseMapper.createIncomeAndCost(bean.getOrderUuid(), orgId);
//    	}
//
//    	if (bean.getAwbNumber() != null && !"".equals(bean.getAwbNumber())) {
//    		//创建订单时 查询是否运单表中存在主单号，若不存在则插入 运单表
//    		LambdaQueryWrapper<AfAwbRoute> afAwbRouteWrapper = new LambdaQueryWrapper<AfAwbRoute>();
//    		afAwbRouteWrapper.eq(AfAwbRoute::getAwbNumber,bean.getAwbNumber());
//    		AfAwbRoute route = afAwbRouteMapper.selectOne(afAwbRouteWrapper);
//    		if(route==null) {
//    			AfAwbRoute insertRoute = new AfAwbRoute();
//    			insertRoute.setAwbNumber(bean.getAwbNumber());
//    			insertRoute.setActualFlightNum(bean.getExpectFlight());
//    			insertRoute.setDepartureStation(bean.getDepartureStation());
//    			insertRoute.setArrivalStation(bean.getArrivalStation());
//    			insertRoute.setActualDeparture(bean.getExpectDeparture());
//    			insertRoute.setActualArrival(bean.getExpectArrival());
//    			insertRoute.setGrossWeight(bean.getPlanWeight());
//    			insertRoute.setQuantity(bean.getPlanPieces());
//    			insertRoute.setCreateTime(LocalDateTime.now());
//    			insertRoute.setIsTrack(0);
//    			afAwbRouteMapper.insert(insertRoute);
//    		}
//
//    	}
        this.saveRouteInfo(bean.getAwbNumber(), bean.getHawbNumber(), CommonConstants.BUSINESS_SCOPE.AI);
        return bean;
    }

    private String getLogRemark(AfOrder order, AfOrder bean) {
        StringBuffer logremark = new StringBuffer();
        String coopName = this.getStr(order.getCoopName(), bean.getCoopName());
        logremark.append(StringUtils.isBlank(coopName) ? "" : "客户：" + coopName);
        String salesName = this.getStr(order.getSalesName(), bean.getSalesName());
        logremark.append(StringUtils.isBlank(salesName) ? "" : "销售：" + salesName);

        String awbNumber = this.getStr(order.getAwbNumber(), bean.getAwbNumber());
        logremark.append(StringUtils.isBlank(awbNumber) ? "" : "主单：" + awbNumber);
        String planPieces = this.getStr("" + order.getPlanPieces(), "" + bean.getPlanPieces());
        logremark.append(StringUtils.isBlank(planPieces) ? "" : "件数：" + planPieces);
//        String planWeight = this.getStr("" + order.getPlanWeight(), "" + Double.parseDouble("" + bean.getPlanWeight()));
//        String planWeight = this.getStr("" + order.getPlanWeight(), "" + "" + bean.getPlanWeight());
        String planWeight = this.getStr(String.valueOf(order.getPlanWeight()), this.fmtMicrometer2(String.valueOf(bean.getPlanWeight())));
        logremark.append(StringUtils.isBlank(planWeight) ? "" : "毛重：" + planWeight);
        String planVolume = this.getStr("" + order.getPlanVolume(), "" + bean.getPlanVolume());
        logremark.append(StringUtils.isBlank(planVolume) ? "" : "体积：" + planVolume);
        String planChargeWeight = this.getStr("" + order.getPlanChargeWeight(), "" + bean.getPlanChargeWeight());
        logremark.append(StringUtils.isBlank(planChargeWeight) ? "" : "计重：" + planChargeWeight);
        String businessProduct = this.getStr("" + order.getBusinessProduct(), "" + bean.getBusinessProduct());
        logremark.append(StringUtils.isBlank(businessProduct) ? "" : "产品：" + businessProduct);
        //卖价
        String orderStr1 = "空";
        String orderStr2 = "空";
        if (order.getFreightUnitprice() != null && !"".equals(order.getFreightUnitprice())) {
            orderStr1 = order.getCurrecnyCode() + " " + order.getFreightUnitprice() + "/CW";
        } else if (order.getFreightAmount() != null && !"".equals(order.getFreightAmount())) {
            orderStr1 = order.getCurrecnyCode() + " " + order.getFreightAmount() + "/ORDER";
        }
        if (bean.getFreightUnitprice() != null && !"".equals(bean.getFreightUnitprice())) {
            orderStr2 = bean.getCurrecnyCode() + " " + bean.getFreightUnitprice() + "/CW";
        } else if (bean.getFreightAmount() != null && !"".equals(bean.getFreightAmount())) {
            orderStr2 = bean.getCurrecnyCode() + " " + bean.getFreightAmount() + "/ORDER";
        }
        if (!orderStr1.equals(orderStr2)) {
            logremark.append("卖价：" + orderStr1 + " -> " + orderStr2 + "  ");
        }
        //成本
        String orderStr11 = "空";
        String orderStr22 = "空";
        if (order.getMsrUnitprice() != null && !"".equals(order.getMsrUnitprice())) {
            orderStr11 = order.getMsrCurrecnyCode() + " " + order.getMsrUnitprice() + "/CW";
        } else if (order.getMsrAmount() != null && !"".equals(order.getMsrAmount())) {
            orderStr11 = order.getMsrCurrecnyCode() + " " + order.getMsrAmount() + "/ORDER";
        }
        if (bean.getMsrUnitprice() != null && !"".equals(bean.getMsrUnitprice())) {
            orderStr22 = bean.getMsrCurrecnyCode() + " " + bean.getMsrUnitprice() + "/CW";
        } else if (bean.getMsrAmount() != null && !"".equals(bean.getMsrAmount())) {
            orderStr22 = bean.getMsrCurrecnyCode() + " " + bean.getMsrAmount() + "/ORDER";
        }
        if (!orderStr11.equals(orderStr22)) {
            logremark.append("成本：" + orderStr11 + " -> " + orderStr22 + "  ");
        }
        String goodsType = this.getStr("" + order.getGoodsType(), "" + bean.getGoodsType());
        logremark.append(StringUtils.isBlank(goodsType) ? "" : "货质：" + goodsType);
        String batteryType = this.getStr("" + order.getBatteryType(), "" + bean.getBatteryType());
        logremark.append(StringUtils.isBlank(batteryType) ? "" : "电池：" + batteryType);
        String pickUpDeliveryService = this.getStr(order.getPickUpDeliveryService() ? "是" : "否", (bean.getPickUpDeliveryService() != null && bean.getPickUpDeliveryService()) ? "是" : "否");
        logremark.append(StringUtils.isBlank(pickUpDeliveryService) ? "" : "提货：" + pickUpDeliveryService);
        String warehouseService = this.getStr(order.getWarehouseService() ? "是" : "否", (bean.getWarehouseService() != null && bean.getWarehouseService()) ? "是" : "否");
        logremark.append(StringUtils.isBlank(warehouseService) ? "" : "库内：" + warehouseService);
        String outfieldService = this.getStr(order.getOutfieldService() ? "是" : "否", (bean.getOutfieldService() != null && bean.getOutfieldService()) ? "是" : "否");
        logremark.append(StringUtils.isBlank(outfieldService) ? "" : "外场：" + outfieldService);
        String customsClearanceService = this.getStr(order.getCustomsClearanceService() ? "是" : "否", (bean.getCustomsClearanceService() != null && bean.getCustomsClearanceService()) ? "是" : "否");
        logremark.append(StringUtils.isBlank(customsClearanceService) ? "" : "报关：" + customsClearanceService);
        String arrivalCustomsClearanceService = this.getStr(order.getArrivalCustomsClearanceService() ? "是" : "否", (bean.getArrivalCustomsClearanceService() != null && bean.getArrivalCustomsClearanceService()) ? "是" : "否");
        logremark.append(StringUtils.isBlank(arrivalCustomsClearanceService) ? "" : "清关：" + arrivalCustomsClearanceService);
        String deliveryService = this.getStr(order.getDeliveryService() ? "是" : "否", (bean.getDeliveryService() != null && bean.getDeliveryService()) ? "是" : "否");
        logremark.append(StringUtils.isBlank(deliveryService) ? "" : "派送：" + deliveryService);

        String freightProfitRatioRemark = this.getStr(order.getFreightProfitRatioRemark(), bean.getFreightProfitRatioRemark());
        logremark.append(StringUtils.isBlank(freightProfitRatioRemark) ? "" : "客户分泡：" + freightProfitRatioRemark);
        String msrProfitRatioRemark = this.getStr(order.getMsrProfitRatioRemark(), bean.getMsrProfitRatioRemark());
        logremark.append(StringUtils.isBlank(msrProfitRatioRemark) ? "" : "成本分泡：" + msrProfitRatioRemark);
        return logremark.toString();
    }

    private String getLogRemarkAI(AfOrder order, AfOrder bean) {
        StringBuffer logremark = new StringBuffer();
        String coopName = this.getStr(order.getCoopName(), bean.getCoopName());
        logremark.append(StringUtils.isBlank(coopName) ? "" : "客户：" + coopName);
        String salesName = this.getStr(order.getSalesName(), bean.getSalesName());
        logremark.append(StringUtils.isBlank(salesName) ? "" : "销售：" + salesName);

        String awbNumber = this.getStr(order.getAwbNumber(), bean.getAwbNumber());
        logremark.append(StringUtils.isBlank(awbNumber) ? "" : "主单：" + awbNumber);
        String hawbNumber = this.getStr(order.getHawbNumber(), bean.getHawbNumber());
        logremark.append(StringUtils.isBlank(hawbNumber) ? "" : "分单：" + hawbNumber);

        String planPieces = this.getStr("" + order.getPlanPieces(), "" + bean.getPlanPieces());
        logremark.append(StringUtils.isBlank(planPieces) ? "" : "件数：" + planPieces);
//        String planWeight = this.getStr("" + order.getPlanWeight(), "" + bean.getPlanWeight());
        String planWeight = this.getStr(String.valueOf(order.getPlanWeight()), this.fmtMicrometer2(String.valueOf(bean.getPlanWeight())));
        logremark.append(StringUtils.isBlank(planWeight) ? "" : "毛重：" + planWeight);
        String planVolume = this.getStr("" + order.getPlanVolume(), "" + bean.getPlanVolume());
        logremark.append(StringUtils.isBlank(planVolume) ? "" : "体积：" + planVolume);
        String planChargeWeight = this.getStr("" + order.getPlanChargeWeight(), "" + bean.getPlanChargeWeight());
        logremark.append(StringUtils.isBlank(planChargeWeight) ? "" : "计重：" + planChargeWeight);

        String switchAwbService = this.getStr(order.getSwitchAwbService() ? "是" : "否", bean.getSwitchAwbService() ? "是" : "否");
        logremark.append(StringUtils.isBlank(switchAwbService) ? "" : "调单：" + switchAwbService);
        String warehouseService = this.getStr(order.getWarehouseService() ? "是" : "否", bean.getWarehouseService() ? "是" : "否");
        logremark.append(StringUtils.isBlank(warehouseService) ? "" : "库内：" + warehouseService);

        String customsClearanceService = this.getStr(order.getCustomsClearanceService() ? "是" : "否", bean.getCustomsClearanceService() ? "是" : "否");
        logremark.append(StringUtils.isBlank(customsClearanceService) ? "" : "报关：" + customsClearanceService);

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

    public static String fmtMicrometer2(String text) {
        DecimalFormat df = null;

        df = new DecimalFormat("#####0.0");

        double number = 0.0;
        try {
            number = Double.parseDouble(text);
            return df.format(number);
        } catch (Exception e) {
            number = 0.0;
            return "";
        }
//		return df.format(number);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doUpdate(AfOrder bean) {
        EUserDetails user = SecurityUtils.getUser();
        Integer orgId = user.getOrgId();
        AfOrder order = baseMapper.getOrderByUUID(orgId, bean.getOrderUuid());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!bean.getRowUuid().equals(order.getRowUuid())) {
            throw new RuntimeException("订单不是最新数据，请刷新页面再操作");
        }
        if (StringUtils.isNotBlank(bean.getExpectFlight())) {
            String expectFlight = bean.getExpectFlight();
            if (expectFlight.length() > 2) {
                expectFlight = bean.getExpectFlight().substring(0, 2);
            }
        }
        int isNew = 0;
        Integer awbId = bean.getAwbId();
        int isHave = 0;
        String awbNumber = bean.getAwbNumber();
        if (awbId == null) {
            isNew = 1;
            if (StringUtils.isNotEmpty(awbNumber)) {
                List<AwbNumber> awbList = baseMapper.selectAwb(orgId, awbNumber);
                if (awbList.size() == 0) {//主单号不存在
                    checkCoop(bean, user, orgId, awbNumber);
                } else {
                    bean.setAwbId(awbList.get(0).getAwbId());
                    bean.setAwbUuid(awbList.get(0).getAwbUuid());
                    baseMapper.updateAwbStatus(awbList.get(0).getAwbId(), "已配单", orgId);
                }
                bean.setOrderStatus("舱位确认");
                isHave = 1;
            }
        }
        if (bean.getDeliverySignDate() != null) {
            bean.setOrderStatus("目的港签收");
        } else if (bean.getArrivalCustomsClearanceDate() != null) {
            bean.setOrderStatus("目的港放行");
        } else if (bean.getArrivalCustomsInspectionDate() != null) {
            bean.setOrderStatus("目的港查验");
        } else if (bean.getCustomsClearanceDate() != null) {
            bean.setOrderStatus("海关放行");
        } else if (bean.getCustomsInspectionDate() != null) {
            bean.setOrderStatus("海关查验");
        } else if (bean.getConfirmWeight() != null) {
            bean.setOrderStatus("货物出重");
        } else if (StringUtils.isNotEmpty(awbNumber)) {
            bean.setOrderStatus("舱位确认");
        } else {
            bean.setOrderStatus("订单创建");
        }
        //添加日志信息
        LogBean logBean = new LogBean();
        logBean.setPageName("AE订单");
        logBean.setPageFunction("修改订单");
        logBean.setLogRemark(this.getLogRemark(order, bean));
        logBean.setBusinessScope("AE");
        logBean.setOrderNumber(bean.getOrderCode());
        logBean.setOrderId(bean.getOrderId());
        logBean.setOrderUuid(bean.getOrderUuid());
        logService.saveLog(logBean);
        if (isNew == 1 && isHave == 1) {
            LogBean logBean2 = new LogBean();
            logBean2.setPageName("AE订单");
            logBean2.setPageFunction("舱位确认");
            logBean2.setLogRemark("主单：" + awbNumber);
            logBean2.setBusinessScope("AE");
            logBean2.setOrderNumber(bean.getOrderCode());
            logBean2.setOrderId(bean.getOrderId());
            logBean2.setOrderUuid(bean.getOrderUuid());
            logService.saveLog(logBean2);
        }
        bean.setRowUuid(UUID.randomUUID().toString());
        bean.setEditTime(new Date());
        bean.setEditorId(user.getId());
        bean.setEditorName(user.getUserCname() + " " + user.getUserEmail());
        bean.setOrgId(orgId);
        UpdateWrapper<AfOrder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("order_id", bean.getOrderId());
        baseMapper.update(bean, updateWrapper);
        //联系人先删除再增加
        baseMapper.deleteOrderContacts(orgId, bean.getOrderId());
        for (int i = 0; i < bean.getOrderContacts().size(); i++) {
            baseMapper.insertOrderContacts(orgId, bean.getOrderId(), bean.getOrderContacts().get(i));
        }
        //附属表先删除在增加
        baseMapper.deleteOrderExtend(orgId, bean.getOrderId());
        baseMapper.insertOrderExtend(orgId, bean);
        //收发货人修改
        AfOrderShipperConsignee afOrderShipperConsignee = bean.getAfOrderShipperConsignee1();
        if (afOrderShipperConsignee != null) {
            afOrderShipperConsignee.setEditTime(LocalDateTime.now());
            afOrderShipperConsignee.setEditorId(user.getId());
            afOrderShipperConsignee.setEditorName(user.getUserCname() + " " + user.getUserEmail());

            afOrderShipperConsigneeMapper.updateById(afOrderShipperConsignee);
        }
        AfOrderShipperConsignee afOrderShipperConsignee2 = bean.getAfOrderShipperConsignee2();
        if (afOrderShipperConsignee2 != null) {
            afOrderShipperConsignee2.setEditTime(LocalDateTime.now());
            afOrderShipperConsignee2.setEditorId(user.getId());
            afOrderShipperConsignee2.setEditorName(user.getUserCname() + " " + user.getUserEmail());
            afOrderShipperConsigneeMapper.updateById(afOrderShipperConsignee2);
        }
        if (isNew == 1 && StringUtils.isNotEmpty(awbNumber)) {
            //自动生成 收入、成本
            baseMapper.createIncomeAndCost(bean.getOrderUuid(), orgId);
        }
        if (StringUtils.isNotEmpty(awbNumber)) {
            //创建订单时 查询是否运单表中存在主单号，若不存在则插入 运单表
            LambdaQueryWrapper<AfAwbRoute> afAwbRouteWrapper = new LambdaQueryWrapper<AfAwbRoute>();
            afAwbRouteWrapper.eq(AfAwbRoute::getAwbNumber, awbNumber);
            AfAwbRoute route = afAwbRouteMapper.selectOne(afAwbRouteWrapper);
            if (route == null) {
                AfAwbRoute insertRoute = new AfAwbRoute();
                insertRoute.setAwbNumber(awbNumber);
                insertRoute.setCreateTime(LocalDateTime.now());
                insertRoute.setIsTrack(0);
                afAwbRouteMapper.insert(insertRoute);
            }

        }

        //插入分单信息

        LambdaQueryWrapper<AfShipperLetter> afShipperLetterWrapper = Wrappers.<AfShipperLetter>lambdaQuery();
        afShipperLetterWrapper.eq(AfShipperLetter::getOrgId, orgId).eq(AfShipperLetter::getOrderId, bean.getOrderId());
        afShipperLetterService.remove(afShipperLetterWrapper);

        LambdaQueryWrapper<AfOrderShipperConsignee> afOrderShipperConsigneeWrapper = Wrappers.<AfOrderShipperConsignee>lambdaQuery();
        afOrderShipperConsigneeWrapper.eq(AfOrderShipperConsignee::getOrgId, orgId).eq(AfOrderShipperConsignee::getOrderId, bean.getOrderId()).isNotNull(AfOrderShipperConsignee::getSlId);
        afOrderShipperConsigneeService.remove(afOrderShipperConsigneeWrapper);
        if (bean.getShipperLetters().size() > 0) {
            bean.getShipperLetters().stream().forEach(afShipperLetter -> {
                afShipperLetter.setOrderId(bean.getOrderId());
                afShipperLetter.setOrgId(orgId);
                afShipperLetter.setSlType("HAWB");
                afShipperLetter.setOrderUuid(bean.getOrderUuid());
                afShipperLetter.setCreateTime(LocalDateTime.now());
                afShipperLetter.setCreatorId(user.getId());
                afShipperLetter.setCreatorName(user.getUserCname() + " " + user.getUserEmail());

                afShipperLetter.setEditTime(LocalDateTime.now());
                afShipperLetter.setEditorId(user.getId());
                afShipperLetter.setEditorName(user.getUserCname() + " " + user.getUserEmail());
            });
            afShipperLetterService.saveBatch(bean.getShipperLetters());


            //新增分单收发货人
            ArrayList<AfOrderShipperConsignee> afOrderConsigneeList = new ArrayList<>();
            ArrayList<AfOrderShipperConsignee> afOrderShipperList = new ArrayList<>();
            bean.getShipperLetters().stream().forEach(afShipperLetter -> {
                AfOrderShipperConsignee afOrderConsignee = afShipperLetter.getAfOrderShipperConsignee1();
                if (afOrderConsignee != null) {
                    afOrderConsignee.setOrderId(afShipperLetter.getOrderId());
                    afOrderConsignee.setSlId(afShipperLetter.getSlId());
                    afOrderConsignee.setCreateTime(LocalDateTime.now());
                    afOrderConsignee.setCreatorId(user.getId());
                    afOrderConsignee.setCreatorName(user.getUserCname() + " " + user.getUserEmail());
                    afOrderConsignee.setOrgId(orgId);
                    afOrderConsigneeList.add(afOrderConsignee);
                }
                AfOrderShipperConsignee afOrderShipper = afShipperLetter.getAfOrderShipperConsignee2();
                if (afOrderShipper != null) {
                    afOrderShipper.setSlId(afShipperLetter.getSlId());
                    afOrderShipper.setOrderId(afShipperLetter.getOrderId());
                    afOrderShipper.setCreateTime(LocalDateTime.now());
                    afOrderShipper.setCreatorId(user.getId());
                    afOrderShipper.setCreatorName(user.getUserCname() + " " + user.getUserEmail());
                    afOrderShipper.setOrgId(orgId);
                    afOrderShipperList.add(afOrderShipper);
                }
            });
            afOrderShipperConsigneeService.saveBatch(afOrderConsigneeList);
            afOrderShipperConsigneeService.saveBatch(afOrderShipperList);
        }
        //修改签单信息
        if (bean.getRountingSign() != null && bean.getRountingSign() == 1) {
            if (bean.getSignState() != null && bean.getSignState() == 0 && bean.getBusinessProduct() != null && bean.getRountingSignBusinessProduct().contains(bean.getBusinessProduct())) {
                AfRountingSign afRountingSign = new AfRountingSign();
                afRountingSign.setRountingSignId(bean.getRountingSignId());
                afRountingSign.setEditorId(user.getId());
                afRountingSign.setEdit_time(new Date());
                afRountingSign.setEditorName(user.getUserCname() + " " + user.getUserEmail());
                //查询当前汇率
                BigDecimal currencyRate = null;
                if (!"".equals(bean.getMsrCurrecnyCode())) {
                    currencyRate = baseMapper.getCurrencyRate(orgId, bean.getMsrCurrecnyCode());
                }
                if (bean.getMsrUnitprice() != null) {
                    if (currencyRate != null) {
                        BigDecimal msrUnitprice = BigDecimal.valueOf(bean.getMsrUnitprice()).multiply(currencyRate);
                        afRountingSign.setMsrUnitprice(msrUnitprice.setScale(2, BigDecimal.ROUND_HALF_UP));
                        BigDecimal msrAmountWriteoff = BigDecimal.valueOf(bean.getConfirmChargeWeight() == null ? bean.getPlanChargeWeight() : bean.getConfirmChargeWeight()).multiply(msrUnitprice);
                        afRountingSign.setMsrAmountWriteoff(msrAmountWriteoff.setScale(2, BigDecimal.ROUND_HALF_UP));
                    } else {
                        afRountingSign.setMsrUnitprice(new BigDecimal("0.00"));
                        afRountingSign.setMsrAmountWriteoff(new BigDecimal("0.00"));
                    }
                    afRountingSign.setIncomeWeight(bean.getConfirmChargeWeight() == null ? bean.getPlanChargeWeight() : bean.getConfirmChargeWeight());
                } else if (bean.getMsrAmount() != null) {
                    if (currencyRate != null) {
                        BigDecimal msrUnitprice = BigDecimal.valueOf(bean.getMsrAmount()).multiply(currencyRate);
                        afRountingSign.setMsrUnitprice(msrUnitprice.setScale(2, BigDecimal.ROUND_HALF_UP));
                        afRountingSign.setMsrAmountWriteoff(msrUnitprice.setScale(2, BigDecimal.ROUND_HALF_UP));
                    } else {
                        afRountingSign.setMsrUnitprice(new BigDecimal("0.00"));
                        afRountingSign.setMsrAmountWriteoff(new BigDecimal("0.00"));
                    }
                    afRountingSign.setIncomeWeight(1.00);
                } else {
                    afRountingSign.setMsrUnitprice(new BigDecimal("0.00"));
                    afRountingSign.setMsrAmountWriteoff(new BigDecimal("0.00"));
                    afRountingSign.setIncomeWeight(bean.getConfirmChargeWeight() == null ? bean.getPlanChargeWeight() : bean.getConfirmChargeWeight());
                }
                afRountingSignMapper.updateById(afRountingSign);
            }
        } else {
            if (bean.getRountingSignId() != null) {
                AfRountingSign afRountingSign = new AfRountingSign();
                afRountingSign.setRountingSignId(bean.getRountingSignId());
                afRountingSign.setSignState(0);
                afRountingSign.setRoutingPersonId(null);
                afRountingSign.setRoutingPersonName(null);
//                afRountingSign.setEditorId(user.getId());
//                afRountingSign.setEdit_time(new Date());
//                afRountingSign.setEditorName(user.getUserCname() + " " + user.getUserEmail());
                //查询cost表有没有干线运输-空运费
                BigDecimal costFunctionalAmount = null;
                costFunctionalAmount = baseMapper.getAostFunctionalAmount(orgId, bean.getOrderId());
                afRountingSign.setMsrUnitprice(costFunctionalAmount == null ? new BigDecimal("0.00") : costFunctionalAmount);
                afRountingSign.setMsrAmountWriteoff(costFunctionalAmount == null ? new BigDecimal("0.00") : costFunctionalAmount);
                afRountingSign.setIncomeWeight(1.00);
                afRountingSignMapper.updateById(afRountingSign);
            }
        }
        return true;
    }

    private void checkCoop(AfOrder bean, EUserDetails user, Integer orgId, String awbNumber) {
        String awbNumberThree = awbNumber.substring(0, 3);
        //@author limr 20201222 校验互为代理/干线承运人若只存在一个，直接保存
        List<Map<String, Object>> list = awbservice.selectCarrier(awbNumberThree);
        if (list.size() == 0) {
            throw new RuntimeException(awbNumberThree + "航司不存在");
        }
        //校验后八位
        String awbNumberEnd = awbNumber.substring(4);
        if (awbNumberEnd.length() != 8 || (Integer.parseInt(awbNumberEnd.substring(0, 7)) % 7 != Integer.parseInt(awbNumberEnd.substring(7)))) {
            throw new RuntimeException("主单号不符合规则");
        }
        List<CoopVo> coopList = remoteCoopService.selectPrmCoopsForAwb(orgId, "AE").getData();
        if (coopList != null && coopList.size() == 1) {
            AwbNumber awbNumberBean = new AwbNumber();
            awbNumberBean.setCreatTime(new Date());
            awbNumberBean.setCreatorName(user.getUserCname() + " " + user.getUserEmail());
            awbNumberBean.setCreatorId(user.getId());
            awbNumberBean.setOrgId(orgId);

            awbNumberBean.setAwbNumber(awbNumber);
            CoopVo vo = coopList.get(0);
            awbNumberBean.setAwbFromId(String.valueOf(vo.getCoop_id()));
            awbNumberBean.setAwbFromName(vo.getCoop_name());
            awbNumberBean.setAwbFromType(vo.getCoop_type());
            awbNumberBean.setAwbStatus("已配单");
            awbNumberBean.setAwbUuid(UUID.randomUUID().toString());
            awbservice.save(awbNumberBean);
            bean.setAwbId(awbNumberBean.getAwbId());
            bean.setAwbUuid(awbNumberBean.getAwbUuid());

        } else {
            throw new RuntimeException("主单号不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doUpdateAI(AfOrder bean) {
//        AfOrder order = getById(bean.getOrderId());
        AfOrder order = baseMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!bean.getRowUuid().equals(order.getRowUuid())) {
            throw new RuntimeException("订单不是最新数据，请刷新页面再操作");
        }
        if (bean.getDeliverySignDate() != null) {
            bean.setOrderStatus("派送签收");
        } else if (bean.getOutboundDate() != null) {
            bean.setOrderStatus("货物出库");
        } else if (bean.getCustomsClearanceDate() != null) {
            bean.setOrderStatus("海关放行");
        } else if (bean.getCustomsInspectionDate() != null) {
            bean.setOrderStatus("海关查验");
        } else if (bean.getInboundDate() != null) {
            bean.setOrderStatus("货物入库");
        } else {
            bean.setOrderStatus("订单创建");
        }
        bean.setEditTime(new Date());
        bean.setEditorId(SecurityUtils.getUser().getId());
        bean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        bean.setRowUuid(UUID.randomUUID().toString());
        UpdateWrapper<AfOrder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("order_id", bean.getOrderId());
        baseMapper.update(bean, updateWrapper);
        //收发货人修改
        AfOrderShipperConsignee afOrderShipperConsignee = bean.getAfOrderShipperConsignee1();
        if (afOrderShipperConsignee != null) {
            afOrderShipperConsignee.setEditTime(LocalDateTime.now());
            afOrderShipperConsignee.setEditorId(SecurityUtils.getUser().getId());
            afOrderShipperConsignee.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());

            afOrderShipperConsigneeMapper.updateById(afOrderShipperConsignee);
        }
        AfOrderShipperConsignee afOrderShipperConsignee2 = bean.getAfOrderShipperConsignee2();
        if (afOrderShipperConsignee2 != null) {
            afOrderShipperConsignee2.setEditTime(LocalDateTime.now());
            afOrderShipperConsignee2.setEditorId(SecurityUtils.getUser().getId());
            afOrderShipperConsignee2.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            afOrderShipperConsigneeMapper.updateById(afOrderShipperConsignee2);
        }

        //添加日志信息
        LogBean logBean = new LogBean();
        logBean.setPageName("订单编辑");
        logBean.setPageFunction("修改订单");

        logBean.setLogRemark(this.getLogRemarkAI(order, bean));
        logBean.setBusinessScope("AI");
        logBean.setOrderNumber(bean.getOrderCode());
        logBean.setOrderId(bean.getOrderId());
        logBean.setOrderUuid(bean.getOrderUuid());
        logService.saveLog(logBean);
        this.saveRouteInfo(bean.getAwbNumber(), bean.getHawbNumber(), CommonConstants.BUSINESS_SCOPE.AI);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doOrderMatch(AfOrderMatch bean) {
        List<AfOrder> orders = bean.getOrders();
        double amount = 0;
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        double amountW = 0;
        for (int i = 0; i < orders.size(); i++) {
            AfOrder aforder = orders.get(i);
            amountW = amountW + aforder.getConfirmChargeWeight();
        }
        for (int i = 0; i < orders.size(); i++) {
            AfOrder aforder = orders.get(i);
            aforder.setAwbCostChargeWeight(bean.getAwbCostChargeWeight());
            aforder.setAwbMsrUnitprice(bean.getAwbMsrUnitprice());
            aforder.setAwbMsrAmount(bean.getAwbMsrAmount());
            if (orders.size() > 1) {
                if (i == orders.size() - 1) {
                    BigDecimal bg3 = new BigDecimal(bean.getPriceValue3() - amount);
                    aforder.setMsrAmount(bg3.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                } else {
                    BigDecimal bg = new BigDecimal(aforder.getConfirmChargeWeight() / amountW);
                    double MsrAmount = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    BigDecimal bg2 = new BigDecimal(bean.getPriceValue3() * MsrAmount);
                    aforder.setMsrAmount(bg2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
                amount = amount + aforder.getMsrAmount();
            } else {
                aforder.setMsrAmount(bean.getPriceValue3());
            }

            if ("单价".equals(aforder.getPriceType2())) {
                aforder.setFreightUnitprice(aforder.getPriceValue2());
                aforder.setFreightAmount(null);
            } else {
                aforder.setFreightUnitprice(null);
                aforder.setFreightAmount(aforder.getPriceValue2());
            }
            baseMapper.doOrderMatch(SecurityUtils.getUser().getOrgId(), aforder);
            //日志
//            logMapper.updateLog(SecurityUtils.getUser().getOrgId(), aforder.getOrderUuid(), "单货匹配", SecurityUtils.getUser().getId(),
//                    SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail(), LocalDateTime.now());
        }

        //调存储过程
        baseMapper.doOrderMatch2(SecurityUtils.getUser().getOrgId(), bean.getAwbUuid());
        return true;
    }

    @Override
    public List<Integer> getOrderStatus(AfOrder bean) {
        List<Integer> list = new ArrayList<Integer>();
        if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
            list = baseMapper.getOrderStatus("财务锁账", bean.getOrderUuid());
        } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
            list = baseMapper.getSEOrderStatus("财务锁账", bean.getOrderUuid());
        } else if (bean.getBusinessScope().startsWith("T")) {
            list = baseMapper.getTEOrderStatus("财务锁账", bean.getOrderUuid());
        }
        return list;
    }

    @Override
    public List<Integer> getOrderIncomeStatus(AfOrder bean) {
        return baseMapper.getOrderIncomeStatus(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doIncome(AfOrder bean) {
        //收入完成已做的 ，不能重复做
        List<Integer> list = new ArrayList<Integer>();
        if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
            list = baseMapper.getAfList(SecurityUtils.getUser().getOrgId(), bean.getOrderId());
        } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
            list = baseMapper.getScList(SecurityUtils.getUser().getOrgId(), bean.getOrderId());
        } else if (bean.getBusinessScope().startsWith("T")) {
            list = baseMapper.getTcList(SecurityUtils.getUser().getOrgId(), bean.getOrderId());
        }
        if (list.size() > 0) {
            throw new RuntimeException("该业务收入已经录入完毕");
        }
        LogBean logBean = new LogBean();
        logBean.setPageName("费用录入");
        logBean.setPageFunction("收入完成");
        logBean.setBusinessScope(bean.getBusinessScope());
        logBean.setOrderNumber(bean.getOrderCode());
        logBean.setOrderId(bean.getOrderId());
        logBean.setOrderUuid(bean.getOrderUuid());


        if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
            logService.saveLog(logBean);
            baseMapper.doIncome(bean.getOrderUuid(), SecurityUtils.getUser().getOrgId(), UUID.randomUUID().toString());
        } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
            ScLog logBean2 = new ScLog();
            BeanUtils.copyProperties(logBean, logBean2);
            scLogService.saveLog(logBean2);
            baseMapper.doIncomeSE(bean.getOrderUuid(), SecurityUtils.getUser().getOrgId(), UUID.randomUUID().toString());
        } else if (bean.getBusinessScope().startsWith("T")) {
            TcLog logBean2 = new TcLog();
            BeanUtils.copyProperties(logBean, logBean2);
            logBean2.setCreatorId(SecurityUtils.getUser().getId());
            logBean2.setCreatorName(SecurityUtils.getUser().buildOptName());
            logBean2.setCreatTime(LocalDateTime.now());
            logBean2.setOrgId(SecurityUtils.getUser().getOrgId());
            tcLogService.save(logBean2);
            baseMapper.doIncomeTC(bean.getOrderUuid(), SecurityUtils.getUser().getOrgId(), UUID.randomUUID().toString());
        } else if ("LC".equals(bean.getBusinessScope())) {
            MessageInfo messageInfo = remoteServiceToSC.incomeComplete(bean.getOrderId());
            if (messageInfo.getCode() == 1) {
                throw new RuntimeException(messageInfo.getMessageInfo());
            }
        } else if ("IO".equals(bean.getBusinessScope())) {
            MessageInfo messageInfo = remoteServiceToSC.ioIncomeComplete(bean.getOrderId());
            if (messageInfo.getCode() == 1) {
                throw new RuntimeException(messageInfo.getMessageInfo());
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doCost(AfOrder bean) {
        //成本完成已做的 ，不能重复做
        List<Integer> list = new ArrayList<Integer>();
        if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
            list = baseMapper.getAfList2(SecurityUtils.getUser().getOrgId(), bean.getOrderId());
        } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
            list = baseMapper.getScList2(SecurityUtils.getUser().getOrgId(), bean.getOrderId());
        } else if (bean.getBusinessScope().startsWith("T")) {
            list = baseMapper.getTcList2(SecurityUtils.getUser().getOrgId(), bean.getOrderId());
        }
        if (list.size() > 0) {
            throw new RuntimeException("该业务成本已经录入完毕");
        }
        LogBean logBean = new LogBean();
        logBean.setPageName("费用录入");
        logBean.setPageFunction("成本完成");
        logBean.setBusinessScope(bean.getBusinessScope());
        logBean.setOrderNumber(bean.getOrderCode());
        logBean.setOrderId(bean.getOrderId());
        logBean.setOrderUuid(bean.getOrderUuid());

        if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
            logService.saveLog(logBean);
            baseMapper.doCost(bean.getOrderUuid(), SecurityUtils.getUser().getOrgId(), UUID.randomUUID().toString());
        } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
            ScLog logBean2 = new ScLog();
            BeanUtils.copyProperties(logBean, logBean2);
            scLogService.saveLog(logBean2);
            baseMapper.doCostSE(bean.getOrderUuid(), SecurityUtils.getUser().getOrgId(), UUID.randomUUID().toString());
        } else if (bean.getBusinessScope().startsWith("T")) {
            TcLog logBean2 = new TcLog();
            BeanUtils.copyProperties(logBean, logBean2);
            logBean2.setCreatorId(SecurityUtils.getUser().getId());
            logBean2.setCreatorName(SecurityUtils.getUser().buildOptName());
            logBean2.setCreatTime(LocalDateTime.now());
            logBean2.setOrgId(SecurityUtils.getUser().getOrgId());
            tcLogService.save(logBean2);
            baseMapper.doCostTC(bean.getOrderUuid(), SecurityUtils.getUser().getOrgId(), UUID.randomUUID().toString());
        } else if ("LC".equals(bean.getBusinessScope())) {
            MessageInfo messageInfo = remoteServiceToSC.costComplete(bean.getOrderId());
            if (messageInfo.getCode() == 1) {
                throw new RuntimeException(messageInfo.getMessageInfo());
            }
        } else if ("IO".equals(bean.getBusinessScope())) {
            MessageInfo messageInfo = remoteServiceToSC.ioCostComplete(bean.getOrderId());
            if (messageInfo.getCode() == 1) {
                throw new RuntimeException(messageInfo.getMessageInfo());
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doFinish(AfOrder bean) {
        //财务锁账已做的 ，不能重复做
        List<Integer> list = this.getOrderStatus(bean);
        if (list.size() > 0) {
            throw new RuntimeException("已经做过财务锁账");
        }

        //日志
//        logMapper.updateLog(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), "财务锁账", SecurityUtils.getUser().getId(),
//                SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail(), LocalDateTime.now());
        //
        LogBean logBean = new LogBean();
        logBean.setPageName(bean.getBusinessScope() + "订单");
        logBean.setPageFunction("财务锁账");

        logBean.setLogRemark("财务日期：" + bean.getReceiptDate());
        logBean.setBusinessScope(bean.getBusinessScope());
        logBean.setOrderNumber(bean.getOrderCode());
        logBean.setOrderId(bean.getOrderId());
        logBean.setOrderUuid(bean.getOrderUuid());


        if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
            logService.saveLog(logBean);
            baseMapper.updateOrder(bean.getOrderUuid(), "财务锁账", UUID.randomUUID().toString());
            baseMapper.updateIncome(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), bean.getReceiptDate());
            baseMapper.updateCost(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), bean.getReceiptDate());
        } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
            ScLog logBean2 = new ScLog();
            BeanUtils.copyProperties(logBean, logBean2);
            scLogService.saveLog(logBean2);
            baseMapper.updateOrderSE(bean.getOrderUuid(), "财务锁账", UUID.randomUUID().toString());
            baseMapper.updateIncomeSE(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), bean.getReceiptDate());
            baseMapper.updateCostSE(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), bean.getReceiptDate());
        } else if (bean.getBusinessScope().startsWith("T")) {
            TcLog logBean2 = new TcLog();
            BeanUtils.copyProperties(logBean, logBean2);
            logBean2.setCreatorId(SecurityUtils.getUser().getId());
            logBean2.setCreatorName(SecurityUtils.getUser().buildOptName());
            logBean2.setCreatTime(LocalDateTime.now());
            logBean2.setOrgId(SecurityUtils.getUser().getOrgId());
            tcLogService.save(logBean2);
            baseMapper.updateOrderTC(bean.getOrderUuid(), "财务锁账", UUID.randomUUID().toString());
            baseMapper.updateIncomeTC(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), bean.getReceiptDate());
            baseMapper.updateCostTC(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), bean.getReceiptDate());
        }

        return true;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doCancel(AfOrder bean) {

        //未做财务锁账 ，不能做撤销完成
        List<Integer> list = this.getOrderStatus(bean);
        if (list.size() == 0) {
            throw new RuntimeException("未做财务锁账");
        }
        //日志
        LogBean logBean = new LogBean();
        logBean.setPageName(bean.getBusinessScope() + "订单");
        logBean.setPageFunction("财务解锁");

        logBean.setLogRemark("");
        logBean.setBusinessScope(bean.getBusinessScope());
        logBean.setOrderNumber(bean.getOrderCode());
        logBean.setOrderId(bean.getOrderId());
        logBean.setOrderUuid(bean.getOrderUuid());

        if ("AE".equals(bean.getBusinessScope())) {
            logService.saveLog(logBean);
//            String nodeName = baseMapper.getNodeName(bean.getOrderUuid());
            String nodeName = "订单创建";
            AfOrder order = baseMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//            if (order!=null && order.getConfirmWeight()!=null) {
//            	nodeName = "货物出重";
//			} else if(order!=null && order.getAwbId()!=null){
//				nodeName = "舱位确认";
//			}
            if (order.getDeliverySignDate() != null) {
                nodeName = "目的港签收";
            } else if (order.getArrivalCustomsClearanceDate() != null) {
                nodeName = "目的港放行";
            } else if (order.getArrivalCustomsInspectionDate() != null) {
                nodeName = "目的港查验";
            } else if (order.getCustomsClearanceDate() != null) {
                nodeName = "海关放行";
            } else if (order.getCustomsInspectionDate() != null) {
                nodeName = "海关查验";
            } else if (order.getConfirmWeight() != null) {
                nodeName = "货物出重";
            } else if (order.getAwbId() != null) {
                nodeName = "舱位确认";
            } else {
                nodeName = "订单创建";
            }
            baseMapper.updateOrder(bean.getOrderUuid(), nodeName, UUID.randomUUID().toString());
        } else if ("AI".equals(bean.getBusinessScope())) {
            logService.saveLog(logBean);
            String nodeName = "订单创建";
            AfOrder order = baseMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
            if (order.getDeliverySignDate() != null) {
                nodeName = "派送签收";
            } else if (order.getOutboundDate() != null) {
                nodeName = "货物出库";
            } else if (order.getCustomsClearanceDate() != null) {
                nodeName = "海关放行";
            } else if (order.getCustomsInspectionDate() != null) {
                nodeName = "海关查验";
            } else if (order.getInboundDate() != null) {
                nodeName = "货物入库";
            } else {
                nodeName = "订单创建";
            }
            baseMapper.updateOrder(bean.getOrderUuid(), nodeName, UUID.randomUUID().toString());
        } else if ("SE".equals(bean.getBusinessScope())) {
            ScLog logBean2 = new ScLog();
            BeanUtils.copyProperties(logBean, logBean2);
            scLogService.saveLog(logBean2);
            String nodeName = "订单创建";
            AfOrder order = baseMapper.getSEOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
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
            baseMapper.updateOrderSE(bean.getOrderUuid(), nodeName, UUID.randomUUID().toString());
        } else if ("SI".equals(bean.getBusinessScope())) {
            ScLog logBean2 = new ScLog();
            BeanUtils.copyProperties(logBean, logBean2);
            scLogService.saveLog(logBean2);
            String nodeName = "订单创建";
            AfOrder order = baseMapper.getSEOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
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
            baseMapper.updateOrderSE(bean.getOrderUuid(), nodeName, UUID.randomUUID().toString());
        } else if (bean.getBusinessScope().startsWith("T")) {
            TcLog logBean2 = new TcLog();
            BeanUtils.copyProperties(logBean, logBean2);
            logBean2.setCreatorId(SecurityUtils.getUser().getId());
            logBean2.setCreatorName(SecurityUtils.getUser().buildOptName());
            logBean2.setCreatTime(LocalDateTime.now());
            logBean2.setOrgId(SecurityUtils.getUser().getOrgId());
            tcLogService.save(logBean2);
            String nodeName = "订单创建";
            AfOrder order = baseMapper.getTCOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
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
            baseMapper.updateOrderTC(bean.getOrderUuid(), nodeName, UUID.randomUUID().toString());
        }
//        baseMapper.updateIncome2(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
//        baseMapper.updateCost2(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doUninstall(AfOrder bean) {
        //校验订单是否变更
        AfOrder afOrder = getById(bean.getOrderId());
        if (afOrder == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!afOrder.getRowUuid().equals(bean.getRowUuid())) {
            throw new RuntimeException("卸载的主单不是最新数据，请刷新后重试。");
        }

        //财务锁账已做的 ，不能重复做
        bean.setBusinessScope("AE");
        List<Integer> list = this.getOrderStatus(bean);
        if (list.size() > 0) {
            throw new RuntimeException("订单已经财务锁账,不能卸载主单");
        }
        AfOrder order = baseMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        //校验是否可以卸载主单 确认收益过不可以
//        LambdaQueryWrapper<LogBean> wrapper = Wrappers.<LogBean>lambdaQuery();
//        wrapper.eq(LogBean::getOrgId, SecurityUtils.getUser().getOrgId()).eq(LogBean::getOrderUuid, bean.getOrderUuid()).eq(LogBean::getNodeName, "财务锁账").eq(LogBean::getPageFunction, "新建");
//        LogBean logBean = logMapper.selectOne(wrapper);
//        if (logBean.getCreatTime() != null) {
//            throw new RuntimeException("订单已经财务锁账,不能卸载主单");
//        }
        baseMapper.updateAwbStatus(bean.getAwbId(), "未使用", SecurityUtils.getUser().getOrgId());
        //日志
        LogBean logBean = new LogBean();
        logBean.setPageName("AE订单");
        logBean.setPageFunction("卸载主单");

        logBean.setLogRemark("原主单号：" + order.getAwbNumber() + "， 状态为：未使用");
        logBean.setBusinessScope("AE");
        logBean.setOrderNumber(order.getOrderCode());
        logBean.setOrderId(order.getOrderId());
        logBean.setOrderUuid(order.getOrderUuid());
        logService.saveLog(logBean);
//        logMapper.updateLog2(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), "舱位确认");
//        String nodeName = baseMapper.getNodeName(bean.getOrderUuid());
        String nodeName = "订单创建";

//        if (order!=null && order.getConfirmWeight()!=null) {
//        	nodeName = "货物出重";
//		}
        if (order != null && order.getDeliverySignDate() != null) {
            nodeName = "目的港签收";
        } else if (order != null && order.getArrivalCustomsClearanceDate() != null) {
            nodeName = "目的港放行";
        } else if (order != null && order.getArrivalCustomsInspectionDate() != null) {
            nodeName = "目的港查验";
        } else if (order != null && order.getCustomsClearanceDate() != null) {
            nodeName = "海关放行";
        } else if (order != null && order.getCustomsInspectionDate() != null) {
            nodeName = "海关查验";
        } else if (order != null && order.getConfirmWeight() != null) {
            nodeName = "货物出重";
        } else {
            nodeName = "订单创建";
        }
        baseMapper.updateOrderNumber(bean.getOrderUuid(), nodeName, SecurityUtils.getUser().getOrgId(), UUID.randomUUID().toString());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doStop(AfOrder bean) {
        //校验订单是否变更
        AfOrder afOrder = getById(bean.getOrderId());
        if (afOrder == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!afOrder.getRowUuid().equals(bean.getRowUuid())) {
            throw new RuntimeException("卸载的主单不是最新数据，请刷新后重试。");
        }
        //财务锁账已做的 ，不能重复做
        bean.setBusinessScope("AE");
        List<Integer> list = this.getOrderStatus(bean);
        if (list.size() > 0) {
            throw new RuntimeException("订单已经财务锁账,不能卸载主单");
        }
        AfOrder order = baseMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        //校验是否可以卸载主单 确认收益过不可以
//        LambdaQueryWrapper<LogBean> wrapper = Wrappers.<LogBean>lambdaQuery();
//        wrapper.eq(LogBean::getOrgId, SecurityUtils.getUser().getOrgId()).eq(LogBean::getOrderUuid, bean.getOrderUuid()).eq(LogBean::getNodeName, "财务锁账").eq(LogBean::getPageFunction, "新建");
//        LogBean logBean = logMapper.selectOne(wrapper);
//        if (logBean.getCreatTime() != null) {
//            throw new RuntimeException("订单已经财务锁账,不能卸载主单");
//        }

        //baseMapper.updateAwbStatus(bean.getAwbId(), "已废单");//更改为物理删除
        //日志
        LogBean logBean = new LogBean();
        logBean.setPageName("AE订单");
        logBean.setPageFunction("卸载主单");

        logBean.setLogRemark("原主单号：" + order.getAwbNumber() + "， 状态为：删除");
        logBean.setBusinessScope("AE");
        logBean.setOrderNumber(order.getOrderCode());
        logBean.setOrderId(order.getOrderId());
        logBean.setOrderUuid(order.getOrderUuid());
        logService.saveLog(logBean);
//        logMapper.updateLog2(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), "舱位确认");
//        String nodeName = baseMapper.getNodeName(bean.getOrderUuid());
        String nodeName = "订单创建";

//        if (order!=null && order.getConfirmWeight()!=null) {
//        	nodeName = "货物出重";
//		}
        if (order != null && order.getDeliverySignDate() != null) {
            nodeName = "目的港签收";
        } else if (order != null && order.getArrivalCustomsClearanceDate() != null) {
            nodeName = "目的港放行";
        } else if (order != null && order.getArrivalCustomsInspectionDate() != null) {
            nodeName = "目的港查验";
        } else if (order != null && order.getCustomsClearanceDate() != null) {
            nodeName = "海关放行";
        } else if (order != null && order.getCustomsInspectionDate() != null) {
            nodeName = "海关查验";
        } else if (order != null && order.getConfirmWeight() != null) {
            nodeName = "货物出重";
        } else {
            nodeName = "订单创建";
        }
        baseMapper.updateOrderNumber(bean.getOrderUuid(), nodeName, SecurityUtils.getUser().getOrgId(), UUID.randomUUID().toString());
        //根据AwbId和org_id物理删除
        baseMapper.deleteByAwbId(bean.getAwbId(), SecurityUtils.getUser().getOrgId());
        return true;
    }

    @Override
    public AfOrder getOrderById(Integer orderId, Integer letterId) {

        AfOrder bean = baseMapper.selectById(orderId);
        if (bean.getAwbId() != null) {
            AwbNumber awbN = awbservice.getById(bean.getAwbId());
            bean.setAwbFromId(Integer.valueOf(awbN.getAwbFromId()));
            bean.setAwbFromName(awbN.getAwbFromName());
        }
        //联系人
        bean.setCoopName(baseMapper.getCoopName(bean.getCoopId()));
        bean.setOrderContacts(baseMapper.getorderContacts(bean.getOrgId(), orderId));

        //订单附属表
        AfOrderExtend extend = baseMapper.getOrderExtend(bean.getOrgId(), orderId);
        if (extend != null) {
            bean.setPalletMaterial(extend.getPalletMaterial());
            bean.setSpecialPackage(extend.getSpecialPackage());
            bean.setCelsiusRequire(extend.getCelsiusRequire());
            bean.setThermometer(extend.getThermometer());
            bean.setIsCelsiusRequire(extend.getIsCelsiusRequire());
        }

        AfOrderShipperConsignee bean1 = baseMapper.getAfOrderShipperConsignee(bean.getOrgId(), orderId, 1);
        AfOrderShipperConsignee bean2 = baseMapper.getAfOrderShipperConsignee(bean.getOrgId(), orderId, 0);
        bean.setAfOrderShipperConsignee1(bean1 == null ? new AfOrderShipperConsignee() : bean1);
        bean.setAfOrderShipperConsignee2(bean2 == null ? new AfOrderShipperConsignee() : bean2);

        LambdaQueryWrapper<AfShipperLetter> afShipperLetterWrapper = Wrappers.<AfShipperLetter>lambdaQuery();
        afShipperLetterWrapper.eq(AfShipperLetter::getOrgId, bean.getOrgId()).eq(AfShipperLetter::getOrderId, orderId);
        List<AfShipperLetter> afShipperLetters = afShipperLetterService.list(afShipperLetterWrapper);
        afShipperLetters.stream().forEach(afShipperLetter -> {
            LambdaQueryWrapper<AfOrderShipperConsignee> afOrderShipperWrapper = Wrappers.<AfOrderShipperConsignee>lambdaQuery();
            if (letterId != null && afShipperLetter.getSlId().equals(letterId)) {
                afShipperLetter.setHawbChecked(true);
            }
            afOrderShipperWrapper.eq(AfOrderShipperConsignee::getOrgId, bean.getOrgId()).eq(AfOrderShipperConsignee::getSlId, afShipperLetter.getSlId()).eq(AfOrderShipperConsignee::getScType, 0);
            LambdaQueryWrapper<AfOrderShipperConsignee> afOrderConsigneeWrapper = Wrappers.<AfOrderShipperConsignee>lambdaQuery();
            afOrderConsigneeWrapper.eq(AfOrderShipperConsignee::getOrgId, bean.getOrgId()).eq(AfOrderShipperConsignee::getSlId, afShipperLetter.getSlId()).eq(AfOrderShipperConsignee::getScType, 1);
            AfOrderShipperConsignee afOrderShipperConsignee2 = afOrderShipperConsigneeService.getOne(afOrderShipperWrapper);
            AfOrderShipperConsignee afOrderShipperConsignee1 = afOrderShipperConsigneeService.getOne(afOrderConsigneeWrapper);
            if (afOrderShipperConsignee2 == null) {
                afOrderShipperConsignee2 = new AfOrderShipperConsignee();
                afOrderShipperConsignee2.setScName("");
                afOrderShipperConsignee2.setScType(0);
                afOrderShipperConsignee2.setScPrintRemark("");

            }
            if (afOrderShipperConsignee1 == null) {
                afOrderShipperConsignee1 = new AfOrderShipperConsignee();
                afOrderShipperConsignee1.setScName("");
                afOrderShipperConsignee1.setScType(1);
                afOrderShipperConsignee1.setScPrintRemark("");
            }
            afShipperLetter.setAfOrderShipperConsignee1(afOrderShipperConsignee1);
            afShipperLetter.setAfOrderShipperConsignee2(afOrderShipperConsignee2);

            afShipperLetter.setDepartureStation(bean.getDepartureStation());
            afShipperLetter.setExpectDeparture(bean.getExpectDeparture());
            afShipperLetter.setExpectFlight(bean.getExpectFlight());
            afShipperLetter.setAwbNumber(StrUtil.isNotBlank(bean.getAwbNumber()) ? bean.getAwbNumber() : bean.getOrderCode());
        });
        bean.setShipperLetters(afShipperLetters);
        //签单信息
        AfRountingSign afRountingSign = baseMapper.getSignByOrderId(orderId);
        if (afRountingSign != null) {
            bean.setSignState(afRountingSign.getSignState());
            bean.setRountingSignId(afRountingSign.getRountingSignId());
        }
        return bean;
    }

    @Override
    public AfOrderMatch getOrderMatch(String awbUuid) {

        AfOrderMatch bean = baseMapper.getAwbNumber(SecurityUtils.getUser().getOrgId(), awbUuid);
        if (bean != null) {
            bean.setOrders(baseMapper.getOrders(SecurityUtils.getUser().getOrgId(), awbUuid));
        }
        return bean;
    }

    @Override
    public Boolean selectOrderStatus(String node_name, String order_uuid) {

//		AfOrder bean=baseMapper.selectById(orderId);

        List<Integer> list = baseMapper.getOrderStatus(node_name, order_uuid);
        if (list.size() > 0) {
            return true;
        } else {
            return false;
        }

    }

    private String getOrderCode() {
        Date dt = new Date();

        String year = String.format("%ty", dt);

        String mon = String.format("%tm", dt);

        String day = String.format("%td", dt);
        return "AE-" + year + mon + day;
    }

    private String getAICode() {
        Date dt = new Date();

        String year = String.format("%ty", dt);

        String mon = String.format("%tm", dt);

        String day = String.format("%td", dt);
        return "AI-" + year + mon + day;
    }

    @Override
    public Boolean printOrderLetter(Integer orgId, String orderUuid, String userId) {
        List<OrderLetters> letterList = baseMapper.printOrderLetter(orgId, orderUuid, userId);
        try {
            if (letterList.size() > 0) {
                PDFUtils.printArderLetters(letterList);
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
    public String printOrderLetter1(String orderUuid, Integer orgId, String userId) throws IOException, DocumentException {
        try {
            List<OrderLetters> letterList = baseMapper.printOrderLetter(orgId, orderUuid, userId);
            ArrayList<String> newFilePaths = new ArrayList<>();
            String awbOrOrderNum = "";
            if (letterList.size() > 0 && letterList != null) {
                OrderLetters ol = letterList.get(0);
                if (StringUtils.isNotEmpty(ol.getInput18())) {
                    awbOrOrderNum = ol.getInput18();
                } else {
                    awbOrOrderNum = ol.getInput04();
                }
                for (int i = 0; i < letterList.size(); i++) {
                    String path = print(letterList.get(i), false);
                    newFilePaths.add(path);
                }
            }
            String lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/printBillTemp/BOOKING_" + awbOrOrderNum + "_" + new Date().getTime() + ".pdf";
            PDFUtils.loadAllPDFForFile(newFilePaths, lastFilePath, null);
            return lastFilePath.replace(PDFUtils.filePath, "");
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }

    }

    @Override
    @SneakyThrows
    public String print(OrderLetters orderLetters, boolean flag) {

        if (flag) {
            return fillTemplate1(orderLetters, PDFUtils.filePath);
        } else {
            return fillTemplate1(orderLetters, "");
        }
    }

    public static String fillTemplate1(OrderLetters order, String replacePath) throws IOException, DocumentException {

        String mwbId = order.getInput04();

        // 模板路径
        String templatePath = filePath + "/PDFtemplate/AE_Letter_Agent.pdf";
        String savePath = filePath + "/PDFtemplate/temp/printBillTemp";
        //String templatePath ="C:/Users/bxs/Documents/WeChat Files/wxid_h7jugpb9y65q22/FileStorage/File/2019-11/AE_Letter_Agent.pdf";
        //String savePath ="C:/xshell/PDFtemplate";

        //得到文件保存的名称
        String saveFilename = makeFileName(mwbId + ".pdf");
        //得到文件的保存目录
        String newPDFPath = makePath(saveFilename, savePath) + "/" + saveFilename;
        //PDFPathList.add(newPDFPath);

        Map<String, String> valueData = new HashMap<>();
        //月份全部大写
        String Etd = "";
//            if (order.getEtd()!=null) {
//            	Etd = new SimpleDateFormat("MMM d", Locale.US).format(order.getEtd()).toUpperCase();
//            }
        valueData.put("Input01", order.getInput01());
        valueData.put("Input02", order.getInput02());
        valueData.put("Input03", order.getInput03());
        valueData.put("Input04", order.getInput04());
        valueData.put("Input05", order.getInput05());
        valueData.put("Input06", order.getInput06());
        valueData.put("Input07", order.getInput07());
        valueData.put("Input08", order.getInput08());
        valueData.put("Input09", order.getInput09());
        valueData.put("Input10", order.getInput10());
        valueData.put("Input11", order.getInput11());
        valueData.put("Input12", order.getInput12());
        valueData.put("Input13", order.getInput13());
        valueData.put("Input14", order.getInput14());
        valueData.put("Input15", order.getInput15());
        valueData.put("Input16", order.getInput16());
        valueData.put("Input17", order.getInput17());
        valueData.put("Input18", order.getInput18());
        valueData.put("Input19", order.getInput19());
        valueData.put("Input20", order.getInput20());
        valueData.put("Input21", order.getInput21());
        valueData.put("Input22", order.getInput22());
        valueData.put("Input23", order.getInput23());
        valueData.put("Input24", order.getInput24());
        valueData.put("Input25", order.getInput25());
        valueData.put("Input26", order.getInput26());
        valueData.put("Input27", order.getInput27());
        valueData.put("Input28", order.getInput28());
        valueData.put("Input29", order.getInput29());
        valueData.put("Input30", order.getInput30());
        valueData.put("Input31", order.getInput31());
        valueData.put("Input32", order.getInput32());
        valueData.put("Input33", order.getInput33());
        valueData.put("Input34", order.getInput34());
        valueData.put("Input35", order.getInput35());
        valueData.put("Input36", order.getInput36());
        valueData.put("Input37", order.getInput37());
        valueData.put("Input38", order.getInput38());
        valueData.put("Input39", order.getInput39());
        valueData.put("Input40", order.getInput40());
        valueData.put("org_seal", order.getOrgSeal());

        //填充每个PDF
        if (StrUtil.isNotBlank(order.getOrgSeal())) {
            loadOrderLetterPDF(templatePath, newPDFPath, valueData, false, true, true);
        } else {
            loadOrderLetterPDF(templatePath, newPDFPath, valueData, false, false, true);
        }
        return newPDFPath.replace(replacePath, "");
    }

    public static File loadOrderLetterPDF(String templatePath, String newPDFPath, Map<String, String> valueData, boolean font, boolean ifSeal, boolean ifLogo)
            throws FileNotFoundException, IOException, DocumentException, BadPdfFormatException {
        File file = new File(newPDFPath);
        if (!file.exists()) {
            file = new File(newPDFPath);
        }
        FileOutputStream out = new FileOutputStream(file);// 输出流
        PdfReader reader = new PdfReader(templatePath);// 读取pdf模板
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, bos);
        AcroFields form = stamper.getAcroFields();


        // 给表单添加中文字体 这里采用系统字体。不设置的话，中文可能无法显示
        BaseFont bf = null;
        if (font) {
            bf = BaseFont.createFont(PDFUtils.yaheiPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } else {
            bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        }

        form.addSubstitutionFont(bf);
        String modTop = "";
        String modRight = "";
        String modBottom = "";
        String modLeft = "";


        for (String key : valueData.keySet()) {
            if (!"org_seal".equals(key) && !"Input21".equals(key)) {
                form.setField(key, valueData.get(key));
            }
            if (key.equals("modLeft")) {
                modLeft = valueData.get(key);
            } else if (key.equals("modTop")) {
                modTop = valueData.get(key);
            } else if (key.equals("modRight")) {
                modRight = valueData.get(key);
            } else if (key.equals("modBottom")) {
                modBottom = valueData.get(key);
            }
        }
        stamper.setFormFlattening(true);// 如果为false那么生成的PDF文件还能编辑，一定要设为true

        if (ifSeal) {
            int pageNo = form.getFieldPositions("org_seal").get(0).page;
            Rectangle signRect = form.getFieldPositions("org_seal").get(0).position;
            float x = signRect.getLeft();
            float y = signRect.getBottom();
            // 读图片
            String imageUrl = filePath + "/PDFtemplate/temp/img/orderLetter/" + valueData.get("org_seal").substring(valueData.get("org_seal").lastIndexOf("/") + 1);
            PDFUtils.downloadFile(valueData.get("org_seal"), imageUrl);
            Image image = Image.getInstance(imageUrl);
            // 获取操作的页面
            PdfContentByte under = stamper.getOverContent(pageNo);
            // 根据域的大小缩放图片
            image.scaleToFit(signRect.getWidth(), signRect.getHeight());
            // 添加图片
            image.setAbsolutePosition(x, y);
            under.addImage(image);
        }
        if (ifLogo && StrUtil.isNotBlank(valueData.get("Input21"))) {
            int pageNo = form.getFieldPositions("Input21").get(0).page;
            Rectangle signRect = form.getFieldPositions("Input21").get(0).position;
            float x = signRect.getLeft();
            float y = signRect.getBottom();
            // 读图片
            String imageUrl = PDFUtils.filePath + "/PDFtemplate/temp/img/orderLetter/" + valueData.get("Input21").substring(valueData.get("Input21").lastIndexOf("/") + 1);
            PDFUtils.downloadFile(valueData.get("Input21"), imageUrl);
            Image image = Image.getInstance(imageUrl);
            // 获取操作的页面
            PdfContentByte under = stamper.getOverContent(pageNo);
            // 根据域的大小缩放图片
            image.scaleToFit(signRect.getWidth(), signRect.getHeight());
            // 添加图片
            image.setAbsolutePosition(x, y + 18);
            under.addImage(image);
        }

        stamper.close();

        Document doc = new Document();

        PdfCopy copy = new PdfCopy(doc, out);
        doc.open();
        for (int i = 1, len = reader.getNumberOfPages(); i <= len; i++) {
            PdfImportedPage importPage = copy.getImportedPage(new PdfReader(bos.toByteArray()), i);
            copy.addPage(importPage);
        }
        doc.close();
        return file;
    }

    @Override
    public String awbSubmit(String orderUuid, Integer orgId) throws IOException, DocumentException {
        LambdaQueryWrapper<AfOrder> wrapper = Wrappers.<AfOrder>lambdaQuery();
        wrapper.eq(AfOrder::getOrderUuid, orderUuid).eq(AfOrder::getOrgId, orgId);
        AfOrder order = getOne(wrapper);
        ArrayList<String> newFilePaths = new ArrayList<>();
        //查询主单信息
        AfPAwbSubmitPrintProcedure afPAwbSubmitPrintProcedure = baseMapper.printAwbSubmit(orgId, orderUuid, "PRINT_MAWB_PRE");
        if (afPAwbSubmitPrintProcedure == null) {
            throw new RuntimeException("没查到该订单信息");
        }
        String newFilePath = fillTemplate(afPAwbSubmitPrintProcedure, PDFUtils.filePath + "/PDFtemplate/MAWB-FORMAT.pdf", PDFUtils.filePath + "/PDFtemplate/temp/order/mawb", PDFUtils.filePath, "MAWB");
        newFilePaths.add(newFilePath);
        //查询分单信息
        List<AfPAwbSubmitPrintProcedure> afPAwbSubmitPrintProcedureList = baseMapper.printHawbSubmit(orgId, orderUuid, "PRINT_HAWB_PRE");

        if (afPAwbSubmitPrintProcedureList.size() > 0 && afPAwbSubmitPrintProcedureList != null) {
            for (int i = 0; i < afPAwbSubmitPrintProcedureList.size(); i++) {
                String path = fillTemplate(afPAwbSubmitPrintProcedureList.get(i), PDFUtils.filePath + "/PDFtemplate/HAWB-FORMAT.pdf", PDFUtils.filePath + "/PDFtemplate/temp/order/mawb", PDFUtils.filePath, "HAWB");
                newFilePaths.add(path);
            }
        }

        String lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/order/MAWB_" + (StrUtil.isBlank(order.getAwbNumber()) ? order.getOrderCode() : order.getAwbNumber()) + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss")) + ".pdf";
        PDFUtils.loadAllPDFForFile(newFilePaths, lastFilePath, null);
        return lastFilePath.replace(PDFUtils.filePath, "");
        /*HashMap<String, String> order = new HashMap<>();
        if (StrUtil.isNotBlank(afPAwbSubmitPrintProcedure.getAwbNumber())) {
            order.put("awbNumber", afPAwbSubmitPrintProcedure.getAwbNumber());
        }
        if (StrUtil.isNotBlank(afPAwbSubmitPrintProcedure.getDepartureStation())) {
            order.put("departureStation", afPAwbSubmitPrintProcedure.getDepartureStation());
        }*/
        //如果 有 中转港1 则 显示 中转港1 ，transit_station， 没有 则取 目的港
        /*if (StrUtil.isNotBlank(afOrder.getTransitStation())) {
            order.put("destinationStation", afOrder.getTransitStation());

            LambdaQueryWrapper<Airport> airportWrapper = Wrappers.<Airport>lambdaQuery();
            airportWrapper.eq(Airport::getApCode, afOrder.getTransitStation());
            Airport one = airportService.getOne(airportWrapper);
            if (one != null && StrUtil.isNotBlank(one.getCityNameEn())) {
                order.put("destinationStationName", one.getCityNameEn().toUpperCase());
            }
        } else {
            order.put("destinationStation", afOrder.getArrivalStation());

            LambdaQueryWrapper<Airport> airportWrapper = Wrappers.<Airport>lambdaQuery();
            airportWrapper.eq(Airport::getApCode, afOrder.getArrivalStation());
            Airport one = airportService.getOne(airportWrapper);
            if (one != null && StrUtil.isNotBlank(one.getCityNameEn())) {
                order.put("destinationStationName", one.getCityNameEn().toUpperCase());
            }
        }*/
        //如果 有 中转港2 则 取 中转港2， 如有 中转港1 则 取 目的港；否则空
        /*if (StrUtil.isNotBlank(afOrder.getTransitStation2())) {
            order.put("destinationStation2", afOrder.getTransitStation2());
        } else if (StrUtil.isNotBlank(afOrder.getTransitStation())) {
            order.put("destinationStation2", afOrder.getArrivalStation());
        } else {
            order.put("destinationStation2", "");
        }*/
        //如果 有 中装港2 transit_station2 则，取 目的港，没有 则空
        /*if (StrUtil.isNotBlank(afOrder.getTransitStation2())) {
            order.put("destinationStation3", afOrder.getArrivalStation());
        } else {
            order.put("destinationStation3", "");
        }*/
        /*if (StrUtil.isNotBlank(afPAwbSubmitPrintProcedure.getArrivalStation())) {
            order.put("destinationStation", afPAwbSubmitPrintProcedure.getArrivalStation());
        }
        if (StrUtil.isNotBlank(afPAwbSubmitPrintProcedure.getDestNameEn())) {
            order.put("destinationStationName", afPAwbSubmitPrintProcedure.getDestNameEn());
        }

        if (StrUtil.isNotBlank(afPAwbSubmitPrintProcedure.getTransitStation())) {
            order.put("destinationStation2", afPAwbSubmitPrintProcedure.getTransitStation());
        }
        if (StrUtil.isNotBlank(afPAwbSubmitPrintProcedure.getTransitStation2())) {
            order.put("destinationStation3", afPAwbSubmitPrintProcedure.getTransitStation2());
        }

        if (StrUtil.isNotBlank(afPAwbSubmitPrintProcedure.getFlightNumber())) {
            order.put("flightNo", afPAwbSubmitPrintProcedure.getFlightNumber());
        }
        if (afPAwbSubmitPrintProcedure.getFlightDate() != null) {
            order.put("flightDate", afPAwbSubmitPrintProcedure.getFlightDate().getDayOfMonth() + afPAwbSubmitPrintProcedure.getFlightDate().getMonth().toString().substring(0, 3) + (afPAwbSubmitPrintProcedure.getFlightDate() + "").substring(2, 4));
        }*/

        //件数有实际 取实际，没有实际取预计
       /* if (StrUtil.isNotBlank(afPAwbSubmitPrintProcedure.getAwbPieces())) {
            order.put("pieces", afPAwbSubmitPrintProcedure.getAwbPieces());
        }

        //发货人
        if (StrUtil.isNotBlank(afPAwbSubmitPrintProcedure.getShipperAddress())) {
            order.put("shipper", afPAwbSubmitPrintProcedure.getShipperAddress());
        }
        //收货人
        if (StrUtil.isNotBlank(afPAwbSubmitPrintProcedure.getConsigneeAddress())) {
            order.put("consignee", afPAwbSubmitPrintProcedure.getConsigneeAddress());
        }

        if (StrUtil.isNotBlank(afPAwbSubmitPrintProcedure.getGoodsDescription())) {
            order.put("goodName", afPAwbSubmitPrintProcedure.getGoodsDescription());
        }
*/
        /*LambdaQueryWrapper<AfShipperLetter> afShipperLetterWrapper = Wrappers.<AfShipperLetter>lambdaQuery();
        afShipperLetterWrapper.eq(AfShipperLetter::getOrderUuid, orderUuid).eq(AfShipperLetter::getOrgId, orgId).eq(AfShipperLetter::getSlType, "MAWB");
        AfShipperLetter oneMawb = afShipperLetterService.getOne(afShipperLetterWrapper);

        LambdaQueryWrapper<AfShipperLetter> shipperLetterWrapper = Wrappers.<AfShipperLetter>lambdaQuery();
        shipperLetterWrapper.eq(AfShipperLetter::getOrderUuid, orderUuid).eq(AfShipperLetter::getOrgId, orgId).eq(AfShipperLetter::getSlType, "HAWB");
        List<AfShipperLetter> list = afShipperLetterService.list(shipperLetterWrapper);


        if (oneMawb != null && StrUtil.isNotBlank(oneMawb.getGoodsNameEn())) {
            order.put("goodName", oneMawb.getGoodsNameEn());
        } else {
            if (list != null && list.size() > 0) {
                for (AfShipperLetter afShipperLetter :
                        list) {
                    if (StrUtil.isNotBlank(afShipperLetter.getGoodsNameEn())) {
                        order.put("goodName", afShipperLetter.getGoodsNameEn());
                        break;
                    }
                }
            }
        }*/
    }

    public String fillTemplate(AfPAwbSubmitPrintProcedure afPAwbSubmitPrintProcedure, String templateFilePath, String savePath, String replacePath, String isMwb) {
        String saveFilename = "";
        if ("MAWB".equals(isMwb)) {
            saveFilename = makeFileName(afPAwbSubmitPrintProcedure.getTxtAWBTop() + ".pdf");
        } else {
            saveFilename = makeFileName(afPAwbSubmitPrintProcedure.getHawbNumber() + ".pdf");
        }
        //得到文件的保存目录
        String newPDFPath = makePath(saveFilename, savePath) + "/" + saveFilename;

        try {

            Map<String, String> valueData = new HashMap<>();
            //月份全部大写

            valueData.put("txtAWBTop", StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtAWBTop()) ? "" : afPAwbSubmitPrintProcedure.getTxtAWBTop());
            valueData.put("txtAWBBottom", StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtAWBBottom()) ? "" : afPAwbSubmitPrintProcedure.getTxtAWBBottom());
            valueData.put("txtAWB_Prefix", StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtAWBPrefix()) ? "" : afPAwbSubmitPrintProcedure.getTxtAWBPrefix());//主单号前三位
            valueData.put("txtOrigin_Code", StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtOriginCode()) ? "" : afPAwbSubmitPrintProcedure.getTxtOriginCode());//始发港
            valueData.put("txtAWB_Suffix", StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtAWBSuffix()) ? "" : afPAwbSubmitPrintProcedure.getTxtAWBSuffix());//主单号后八位
            valueData.put("txtCarrier_Name", afPAwbSubmitPrintProcedure.getTxtCarrierName() == null ? "" : afPAwbSubmitPrintProcedure.getTxtCarrierName());//航空公司
            valueData.put("txtAgent_Name", afPAwbSubmitPrintProcedure.getTxtAgentName() == null ? "" : afPAwbSubmitPrintProcedure.getTxtAgentName());//代理名称
            valueData.put("txtAgent_Iata_Code", afPAwbSubmitPrintProcedure.getTxtAgentIataCode() == null ? "" : afPAwbSubmitPrintProcedure.getTxtAgentIataCode());//代理IATA代码
            valueData.put("txtAgent_Account", afPAwbSubmitPrintProcedure.getTxtAgentAccount() == null ? "" : afPAwbSubmitPrintProcedure.getTxtAgentAccount());//代理Account代码
            valueData.put("txtDeparture", afPAwbSubmitPrintProcedure.getTxtDeparture() == null ? "" : afPAwbSubmitPrintProcedure.getTxtDeparture());//始发港英文全称
            valueData.put("txtFlight1_Carr", StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtFlight1Carr()) ? "" : afPAwbSubmitPrintProcedure.getTxtFlight1Carr());//第一承运人
            valueData.put("txtBy2", afPAwbSubmitPrintProcedure.getTxtBy2() == null ? "" : afPAwbSubmitPrintProcedure.getTxtBy2());//中转港1航班两字码
            valueData.put("txtBy3", afPAwbSubmitPrintProcedure.getTxtBy3() == null ? "" : afPAwbSubmitPrintProcedure.getTxtBy3());//中转港2航班两字码
            valueData.put("txtDestination", afPAwbSubmitPrintProcedure.getTxtDestination() == null ? "" : afPAwbSubmitPrintProcedure.getTxtDestination());//目的港
            valueData.put("txtAccountingInfo_Text", afPAwbSubmitPrintProcedure.getTxtAccountingInfoText() == null ? "" : afPAwbSubmitPrintProcedure.getTxtAccountingInfoText());//AccountingInfomation
            // valueData.put("txtFlight2_Carr", afPAwbSubmitPrintProcedure.getTxtFlight2Carr() + " " + afPAwbSubmitPrintProcedure.getTxtFlight2Carr());//航班号
            // valueData.put("txtFlight3_Carr", afPAwbSubmitPrintProcedure.getTxtFlight3Carr() + " " + afPAwbSubmitPrintProcedure.getTxtFlight3Carr());//航班日期
            valueData.put("txtDefault_CurrCode", afPAwbSubmitPrintProcedure.getTxtDefaultCurrCode() == null ? "" : afPAwbSubmitPrintProcedure.getTxtDefaultCurrCode());//币种
            valueData.put("txtPC_WtgPP", afPAwbSubmitPrintProcedure.getTxtPCWtgPP() == null ? "" : afPAwbSubmitPrintProcedure.getTxtPCWtgPP());//到付预付运费方式
            valueData.put("txtPC_OthPP", afPAwbSubmitPrintProcedure.getTxtPCOthPP() == null ? "" : afPAwbSubmitPrintProcedure.getTxtPCOthPP());//到付预付杂费方式
            valueData.put("txtHandlingInfo_Text", StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtHandlingInfoText()) ? "" : afPAwbSubmitPrintProcedure.getTxtHandlingInfoText());
            valueData.put("txtGoods_Desc1", StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtGoodsDesc1()) ? "" : afPAwbSubmitPrintProcedure.getTxtGoodsDesc1());//品名
            valueData.put("txtGoods_Volume", afPAwbSubmitPrintProcedure.getTxtGoodsVolume() == null ? "" : afPAwbSubmitPrintProcedure.getTxtGoodsVolume());//体积
            valueData.put("txtRCP_Pcs1", afPAwbSubmitPrintProcedure.getTxtRCPPcs1() == null ? "" : afPAwbSubmitPrintProcedure.getTxtRCPPcs1());//件数
            valueData.put("txtTotal_Rcp", afPAwbSubmitPrintProcedure.getTxtTotalRcp() == null ? "" : afPAwbSubmitPrintProcedure.getTxtTotalRcp());//小件数
            valueData.put("txtGross_Wtg1", afPAwbSubmitPrintProcedure.getTxtGrossWtg1() == null ? "" : afPAwbSubmitPrintProcedure.getTxtGrossWtg1());//毛重
            valueData.put("txtDefault_WgtCode1", afPAwbSubmitPrintProcedure.getTxtDefaultWgtCode1() == null ? "" : afPAwbSubmitPrintProcedure.getTxtDefaultWgtCode1());//重量单位
            valueData.put("txtChg_Wtg1", afPAwbSubmitPrintProcedure.getTxtChgWtg1() == null ? "" : afPAwbSubmitPrintProcedure.getTxtChgWtg1());//计重
            valueData.put("txtRate_Class1", afPAwbSubmitPrintProcedure.getTxtRateClass1() == null ? "" : afPAwbSubmitPrintProcedure.getTxtRateClass1());//运价等级
            valueData.put("txtRate_Chg_Dis1", afPAwbSubmitPrintProcedure.getTxtRateChgDis1() == null ? "" : afPAwbSubmitPrintProcedure.getTxtRateChgDis1());//费率
            valueData.put("txtTotal_Chg1", afPAwbSubmitPrintProcedure.getTxtTotalChg1() == null ? "" : afPAwbSubmitPrintProcedure.getTxtTotalChg1());//运费合计
            valueData.put("txtGoods_Size", afPAwbSubmitPrintProcedure.getTxtGoodsSize() == null ? "" : afPAwbSubmitPrintProcedure.getTxtGoodsSize());//唛头
            valueData.put("txtTotal_Wtg_Chg_PP", afPAwbSubmitPrintProcedure.getTxtTotalWtgChgPP() == null ? "" : afPAwbSubmitPrintProcedure.getTxtTotalWtgChgPP());//预付-重量价值费
            valueData.put("txtTotal_Wtg_Chg_CC", afPAwbSubmitPrintProcedure.getTxtTotalWtgChgCC() == null ? "" : afPAwbSubmitPrintProcedure.getTxtTotalWtgChgCC());//到付-重量价值费
            valueData.put("txtChg_Due_Carr_PP", afPAwbSubmitPrintProcedure.getTxtChgDueCarrPP() == null ? "" : afPAwbSubmitPrintProcedure.getTxtChgDueCarrPP());//预付杂费金额
            valueData.put("txtChg_Due_Carr_CC", afPAwbSubmitPrintProcedure.getTxtChgDueCarrCC() == null ? "" : afPAwbSubmitPrintProcedure.getTxtChgDueCarrCC());//到付杂费金额
            valueData.put("txtShipperRemark1", afPAwbSubmitPrintProcedure.getTxtShipperRemark1() == null ? "" : afPAwbSubmitPrintProcedure.getTxtShipperRemark1());//发货人或代理签字
            valueData.put("txtShipperRemark2", afPAwbSubmitPrintProcedure.getTxtShipperRemark2() == null ? "" : afPAwbSubmitPrintProcedure.getTxtShipperRemark2());//承运代理公司名称
            valueData.put("txtTotalPP", afPAwbSubmitPrintProcedure.getTxtTotalPP() == null ? "" : afPAwbSubmitPrintProcedure.getTxtTotalPP());//预付总金额
            valueData.put("txtTotalCC", afPAwbSubmitPrintProcedure.getTxtTotalCC() == null ? "" : afPAwbSubmitPrintProcedure.getTxtTotalCC());//到付总金额
            valueData.put("txtOtherCharges1", afPAwbSubmitPrintProcedure.getTxtOtherCharges1() == null ? "" : afPAwbSubmitPrintProcedure.getTxtOtherCharges1());//其他杂费
            valueData.put("txtAWBBarCode", StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtAWBBarCode()) ? "" : "*" + afPAwbSubmitPrintProcedure.getTxtAWBBarCode() + "*");//扫码

            valueData.put("txtChgsCode", afPAwbSubmitPrintProcedure.getTxtChgsCode() == null ? "" : afPAwbSubmitPrintProcedure.getTxtChgsCode());//CHGS代码
            valueData.put("txtCVD_Carriage", afPAwbSubmitPrintProcedure.getTxtCVDCarriage() == null ? "" : afPAwbSubmitPrintProcedure.getTxtCVDCarriage());//声明价值
            valueData.put("txtCVD_Custom", afPAwbSubmitPrintProcedure.getTxtCVDCustom() == null ? "" : afPAwbSubmitPrintProcedure.getTxtCVDCustom());//海关声明价值
            valueData.put("txtCVD_Insurance", afPAwbSubmitPrintProcedure.getTxtCVDInsurance() == null ? "" : afPAwbSubmitPrintProcedure.getTxtCVDInsurance());//保险价值
            valueData.put("txtItem_Num1", afPAwbSubmitPrintProcedure.getTxtItemNum1() == null ? "" : afPAwbSubmitPrintProcedure.getTxtItemNum1());//商品名编号
            valueData.put("txtVal_Chg_PP", afPAwbSubmitPrintProcedure.getTxtValChgPP() == null ? "" : afPAwbSubmitPrintProcedure.getTxtValChgPP());//预付-声明价值费
            valueData.put("txtVal_Chg_CC", afPAwbSubmitPrintProcedure.getTxtValChgCC() == null ? "" : afPAwbSubmitPrintProcedure.getTxtValChgCC());//到付-声明价值费
            valueData.put("txtTax_Chg_PP", afPAwbSubmitPrintProcedure.getTxtTaxChgPP() == null ? "" : afPAwbSubmitPrintProcedure.getTxtTaxChgPP());//预付-税款
            valueData.put("txtTax_Chg_CC", afPAwbSubmitPrintProcedure.getTxtTaxChgCC() == null ? "" : afPAwbSubmitPrintProcedure.getTxtTaxChgCC());//到付-税款
            valueData.put("txtChg_Due_Agt_PP", afPAwbSubmitPrintProcedure.getTxtChgDueAgtPP() == null ? "" : afPAwbSubmitPrintProcedure.getTxtChgDueAgtPP());//预付-代理人的其它费用总额
            valueData.put("txtChg_Due_Agt_CC", afPAwbSubmitPrintProcedure.getTxtChgDueAgtCC() == null ? "" : afPAwbSubmitPrintProcedure.getTxtChgDueAgtCC());//到付-代理人的其它费用总额
            valueData.put("txtCCR", afPAwbSubmitPrintProcedure.getTxtCCR() == null ? "" : afPAwbSubmitPrintProcedure.getTxtCCR());//汇率
            valueData.put("txtCDC", afPAwbSubmitPrintProcedure.getTxtCDC() == null ? "" : afPAwbSubmitPrintProcedure.getTxtCDC());//到付费用(目的国货币)
            valueData.put("txtChg_Dest", afPAwbSubmitPrintProcedure.getTxtChgDest() == null ? "" : afPAwbSubmitPrintProcedure.getTxtChgDest());//目的国收费
            valueData.put("txtTot_Coll", afPAwbSubmitPrintProcedure.getTxtTotColl() == null ? "" : afPAwbSubmitPrintProcedure.getTxtTotColl());//到付费用总计

            valueData.put("txtTo1", StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtDestination()) ? "" : afPAwbSubmitPrintProcedure.getTxtDestination());//
            valueData.put("txtTo2", StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtTo2()) ? "" : afPAwbSubmitPrintProcedure.getTxtTo2());
            valueData.put("txtTo3", StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtTo3()) ? "" : afPAwbSubmitPrintProcedure.getTxtTo3());
            String destinationCode = StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtTo3()) ? StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtTo2()) ? StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtDestination()) ? "" : afPAwbSubmitPrintProcedure.getTxtDestination() : afPAwbSubmitPrintProcedure.getTxtTo2() : afPAwbSubmitPrintProcedure.getTxtTo3();
            if (StrUtil.isBlank(destinationCode)) {
                valueData.put("txtDestination", "");//目的港
            } else {
                String txtDestination = baseMapper.getApNameEnByCode(destinationCode);
                valueData.put("txtDestination", txtDestination);//目的港
            }
            //valueData.put("txtDestination", StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtDestination()) ? "" : afPAwbSubmitPrintProcedure.getTxtDestination());//目的港
            valueData.put("txtFlight2_Carr", StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtFlight2Carr()) ? "" : afPAwbSubmitPrintProcedure.getTxtFlight2Carr());//航班号
            valueData.put("txtFlight3_Carr", StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtFlight3Carr()) ? "" : afPAwbSubmitPrintProcedure.getTxtFlight3Carr());//航班日期
            //valueData.put("txtFlight3_Carr", afPAwbSubmitPrintProcedure.getFlightDate().getDayOfMonth() + afPAwbSubmitPrintProcedure.getFlightDate().getMonth().toString().substring(0, 3) + (afPAwbSubmitPrintProcedure.getFlightDate() + "").substring(2, 4));//航班日期
            valueData.put("txtShipper_Name", StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtShipperName()) ? "" : afPAwbSubmitPrintProcedure.getTxtShipperName());//发货人
            valueData.put("txtConsignee_Name", StrUtil.isBlank(afPAwbSubmitPrintProcedure.getTxtConsigneeName()) ? "" : afPAwbSubmitPrintProcedure.getTxtConsigneeName());//收货人

            //pdf填充数据以及下载
            loadPDF(templateFilePath, newPDFPath, valueData, false);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Exception : " + e.getMessage());
        }
        return newPDFPath.replace("", "");
    }

    @SneakyThrows
    public static File loadPDF(String templatePath, String newPDFPath, Map<String, String> valueData, boolean flag) {
        File file = new File(newPDFPath);
        if (!file.exists()) {
            file = new File(newPDFPath);
        }
        FileOutputStream out = new FileOutputStream(file);// 输出流
        PdfReader reader = new PdfReader(templatePath);// 读取pdf模板
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, bos);
        AcroFields form = stamper.getAcroFields();


        // 给表单添加中文字体 这里采用系统字体。不设置的话，中文可能无法显示
        BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);

        form.addSubstitutionFont(bf);

        for (String key : valueData.keySet()) {
            form.setField(key, valueData.get(key));
        }
        stamper.setFormFlattening(true);// 如果为false那么生成的PDF文件还能编辑，一定要设为true
        stamper.close();

        Document doc = new Document();

        PdfCopy copy = new PdfCopy(doc, out);
        doc.open();
        PdfImportedPage importPage = copy.getImportedPage(new PdfReader(bos.toByteArray()), 1);
        copy.addPage(importPage);
        doc.close();
        return file;
    }

    public static String makeFileName(String filename) {  //2.jpg
        //为防止文件覆盖的现象发生，要为上传文件产生一个唯一的文件名
        return UUID.randomUUID().toString() + "_" + filename;
    }

    public static String makePath(String filename, String savePath) {
        //得到文件名的hashCode的值，得到的就是filename这个字符串对象在内存中的地址
        String hashDir = makeHashDir(filename);
        //构造新的保存目录
        String dir = savePath + hashDir;  //upload\2\3  upload\3\5
        //File既可以代表文件也可以代表目录
        File file = new File(dir);
        //如果目录不存在
        if (!file.exists()) {
            //创建目录
            file.mkdirs();
        }
        return dir;
    }

    public static String makeHashDir(String filename) {
        //得到文件名的hashCode的值，得到的就是filename这个字符串对象在内存中的地址
        int hashcode = filename.hashCode();
        int dir1 = hashcode & 0xf;  //0--15
        int dir2 = (hashcode & 0xf0) >> 4;  //0-15
        //构造新的保存目录
        String hashDir = "/" + dir1 + "/" + dir2;  //upload/2/3  upload/3/5
        return hashDir;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void forceStop(String reason, String orderUuid, String businessScope) {
        //校验是否可以卸载主单 1确认收益过不可以
//        LambdaQueryWrapper<LogBean> LogWrapper = Wrappers.<LogBean>lambdaQuery();
//        LogWrapper.eq(LogBean::getOrgId, SecurityUtils.getUser().getOrgId()).eq(LogBean::getOrderUuid, orderUuid).eq(LogBean::getNodeName, "财务锁账").eq(LogBean::getPageFunction, "新建");
//        LogBean log = logMapper.selectOne(LogWrapper);
//        if (log.getCreatTime() != null) {
//            throw new RuntimeException("订单已经财务锁账,不能强制关闭");
//        }
        AfOrder bean = new AfOrder();
        bean.setOrderUuid(orderUuid);
        bean.setBusinessScope(businessScope);
        List<Integer> list = this.getOrderStatus(bean);
        if (list.size() > 0) {
            throw new RuntimeException("订单已经财务锁账,不能强制关闭");
        }
        LambdaQueryWrapper<AfOrder> wrapper = Wrappers.<AfOrder>lambdaQuery();
        wrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfOrder::getOrderUuid, orderUuid);
        AfOrder afOrder = baseMapper.selectOne(wrapper);
        if ("强制关闭".equals(afOrder.getOrderStatus())) {
            throw new RuntimeException("该订单已经强制关闭，无需再关闭");
        }

        if (afOrder.getAwbId() != null) {
            throw new RuntimeException("该订单已配主单号，请先卸载主单，再关闭订单");
        }

        /**
         2020-2-24新增需求
         强制关闭 增加判断规则： 如果 该订单 已做 账单 或 已做 成本对账单，则 不允许 关闭；
         已做 账单 ，提示“该订单已做 应收账单 ，不允许 关闭订单。”
         已做 成本对账单，提示 “该订单已做 成本对账单，不允许 关闭订单。”
         如果所有条件都满足：
         关闭订单后， 删除 该订单下 所有 应收、成本 明细；
         */
        //查询应收 是否有账单
        List<AfIncome> listAfIncome = afIncomeMapper.queryAfIncomeList(SecurityUtils.getUser().getOrgId(), orderUuid, businessScope);
        if (listAfIncome != null && listAfIncome.size() > 0) {
            throw new RuntimeException("该订单已做 应收账单 ，不允许 关闭订单");
        }
        //查询应付是否有成本对账单
//          List<AfCost> listafCost = afCostMapper.queryAfCostList(SecurityUtils.getUser().getOrgId(),orderUuid);
//          if(listafCost!=null&&listafCost.size()>0) {
//        	  throw new RuntimeException("该订单已做 成本对账单，不允许 关闭订单");
//          }
        /**
         * 2020-3-4 变更需求
         * 关闭订单 判断 成本 cost 是否 制作对账单  改为 判断 对账单明细表，如果 存在数据 则认为 已做对账单；
         */
        List<CssPayment> listCssPayment = cssPaymentMapper.queryCssPaymentListForWhere(SecurityUtils.getUser().getOrgId(), afOrder.getBusinessScope(), orderUuid);
        if (listCssPayment != null && listCssPayment.size() > 0) {
            throw new RuntimeException("该订单已做对账单，不允许 关闭订单");
        }
        //修改订单信息
        afOrder.setOrderStatus("强制关闭");
        afOrder.setEditorId(SecurityUtils.getUser().getId());
        afOrder.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        afOrder.setEditTime(new Date());
        baseMapper.update(afOrder, wrapper);

        //都通过 关闭订单后， 删除 该订单下 所有 应收、成本 明细
        //删除应收
        LambdaQueryWrapper<AfIncome> afIncomeWrapper = Wrappers.<AfIncome>lambdaQuery();
        afIncomeWrapper.eq(AfIncome::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfIncome::getOrderUuid, orderUuid);
        afIncomeMapper.delete(afIncomeWrapper);
        //删除应付
        LambdaQueryWrapper<AfCost> afCostWrapper = Wrappers.<AfCost>lambdaQuery();
        afCostWrapper.eq(AfCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfCost::getOrderUuid, orderUuid);
        afCostMapper.delete(afCostWrapper);

        //添加日志信息
        LogBean logBean = new LogBean();

        logBean.setPageName(businessScope + "订单");
        logBean.setPageFunction("强制关闭");
        logBean.setLogRemark(reason);
        logBean.setBusinessScope(businessScope);
        logBean.setOrderNumber(afOrder.getOrderCode());
        logBean.setOrderId(afOrder.getOrderId());
        logBean.setOrderUuid(afOrder.getOrderUuid());

        logService.saveLog(logBean);
        //HRS日志
        baseMapper.insertHrsLog(businessScope + "订单", "订单号:" + afOrder.getOrderCode(),
                SecurityUtils.getUser().getId(), LocalDateTime.now(), SecurityUtils.getUser().getOrgId(), SecurityUtils.getUser().getDeptId());
    }

    @Override
    public AfOrder queryOrderByOrderUuid(String orderUuid) {
        LambdaQueryWrapper<AfOrder> wrapper = Wrappers.<AfOrder>lambdaQuery();
        wrapper.eq(AfOrder::getOrderUuid, orderUuid).eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId());
        AfOrder afOrder = baseMapper.selectOne(wrapper);
        if (StrUtil.isNotBlank(afOrder.getDepartureStation())) {
            LambdaQueryWrapper<Airport> airPortWrapperForDeparture = Wrappers.<Airport>lambdaQuery();
            airPortWrapperForDeparture.eq(Airport::getApCode, afOrder.getDepartureStation().toUpperCase());
            Airport airportD = airportService.getOne(airPortWrapperForDeparture);
            afOrder.setDepartureWarehouseName(airportD.getApNameEn().toUpperCase() + "(" + airportD.getApCode().toUpperCase() + ")");
        }

        if (StrUtil.isNotBlank(afOrder.getArrivalStation())) {
            LambdaQueryWrapper<Airport> airPortWrapperForDestination = Wrappers.<Airport>lambdaQuery();
            airPortWrapperForDestination.eq(Airport::getApCode, afOrder.getArrivalStation().toUpperCase());
            Airport airportA = airportService.getOne(airPortWrapperForDestination);
            afOrder.setDepartureStorehouseName(airportA.getApNameEn().toUpperCase() + "(" + airportA.getApCode().toUpperCase() + ")");
        }

        LambdaQueryWrapper<AfOrderShipperConsignee> shipperWrapper = Wrappers.<AfOrderShipperConsignee>lambdaQuery();
        LambdaQueryWrapper<AfOrderShipperConsignee> consigneeWrapper = Wrappers.<AfOrderShipperConsignee>lambdaQuery();
        shipperWrapper.eq(AfOrderShipperConsignee::getOrderId, afOrder.getOrderId()).eq(AfOrderShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfOrderShipperConsignee::getScType, 0).isNull(AfOrderShipperConsignee::getSlId);
        consigneeWrapper.eq(AfOrderShipperConsignee::getOrderId, afOrder.getOrderId()).eq(AfOrderShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfOrderShipperConsignee::getScType, 1).isNull(AfOrderShipperConsignee::getSlId);
        AfOrderShipperConsignee shipper = afOrderShipperConsigneeMapper.selectOne(shipperWrapper);
        AfOrderShipperConsignee consignee = afOrderShipperConsigneeMapper.selectOne(consigneeWrapper);
        afOrder.setAfOrderShipperConsignee1(shipper);
        afOrder.setAfOrderShipperConsignee2(consignee);
        return afOrder;
    }

    @Override
    public Boolean getShippingData(String apiType) {
        if (StringUtils.isBlank(apiType)) {
            return false;
        }
        Integer shippingDataNum = baseMapper.getShippingData(SecurityUtils.getUser().getOrgId(), apiType);
        if (shippingDataNum > 0) {//找到有效数据
            return true;
        } else {//没有找到有效数据
            return false;
        }
    }

    @Override
    public String getAwbPrintId(String awbUuid) {
        String awbPrintId = baseMapper.getAwbPrintId(awbUuid);
        return awbPrintId;
    }

    @Override
    public String getFlightNumber(String awbUuid) {
        String flightNumber = baseMapper.getFlightNumber(awbUuid);
        return flightNumber;
    }

    @Override
    public OrderTrack getOrderTrack(String orderUUID) {
        Assert.hasLength(orderUUID, "非法订单虚拟ID");

        LambdaQueryWrapper<AfOrder> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AfOrder::getOrderUuid, orderUUID);
        AfOrder afOrder = baseMapper.selectOne(wrapper);
        if (null == afOrder) {
            throw new RuntimeException("没查到该订单信息");
        }
        //加载附件信息
        LambdaQueryWrapper<OrderFiles> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(OrderFiles::getOrderId, afOrder.getOrderId());
        lambdaQueryWrapper.eq(OrderFiles::getIsDisplay, 1);
        lambdaQueryWrapper.orderByDesc(OrderFiles::getCreateTime);
        List<OrderFiles> attachments = orderFilesMapper.selectList(lambdaQueryWrapper);
        //加载轨迹信息
        String awbNumber = afOrder.getAwbNumber();
        List<AfAwbRouteTrackAwb> routeTrackAwbList = afAwbRouteTrackAwbService.getByAwbNumber(awbNumber);
        //舱单信息
        List<AfAwbRouteTrackManifest> manifestList = getAfAwbRouteTrackManifests(afOrder);

        //查看主单轨迹更新相关数据
        if (StrUtil.isNotBlank(awbNumber)) {
            Map<String, Object> trackInfo = baseMapper.selectAwbTrackInfo(awbNumber);
            if (trackInfo.get("departure_station") != null && StrUtil.isNotBlank(trackInfo.get("departure_station").toString())) {
                afOrder.setDepartureStation(trackInfo.get("departure_station").toString());
            }
            if (trackInfo.get("arrival_station") != null && StrUtil.isNotBlank(trackInfo.get("arrival_station").toString())) {
                afOrder.setArrivalStation(trackInfo.get("arrival_station").toString());
            }
            if (trackInfo.get("flight_num") != null && StrUtil.isNotBlank(trackInfo.get("flight_num").toString())) {
                afOrder.setExpectFlight(trackInfo.get("flight_num").toString());
            }
            if (trackInfo.get("event_time") != null) {
                Timestamp eventTime = (Timestamp) trackInfo.get("event_time");
                afOrder.setExpectDeparture(eventTime.toLocalDateTime().toLocalDate());
            }
        }
        OrderTrack orderTrack = new OrderTrack();
        orderTrack.addOrder(afOrder);
        orderTrack.setAttachments(attachments);
        orderTrack.setRouteTracks(routeTrackAwbList);
        orderTrack.setTrackManifest(manifestList);
        //构建舱单展示信息
        orderTrack.setManifestList(buildManifestVO(afOrder.getBusinessScope(), orderTrack.getTrackManifest()));

        return orderTrack;
    }

    /**
     * 获取舱单信息，当为AI业务时，则需要加载指定分单号
     *
     * @param afOrder
     * @return
     */
    private List<AfAwbRouteTrackManifest> getAfAwbRouteTrackManifests(AfOrder afOrder) {
        String businessScope = afOrder.getBusinessScope();

        List<AfAwbRouteTrackManifest> manifestList = afAwbRouteTrackManifestService.getByAwbNumber(afOrder.getAwbNumber(), businessScope);
        String hawbNumber = afOrder.getHawbNumber();

        //AI业务时只获取当前订单主单号与分单号的轨迹信息。 分单号是否为空 ? 返回主单舱单信息 : 返回主单舱单信息+与分单号相同的舱单信息
        if (CommonConstants.BUSINESS_SCOPE.AI.equals(businessScope)) {
            manifestList = manifestList.stream().filter((item) ->
                    StringUtils.isBlank(hawbNumber) ? StringUtils.isBlank(item.getHawbNumber()) : (StringUtils.isBlank(item.getHawbNumber()) || hawbNumber.equals(item.getHawbNumber()))
            ).collect(Collectors.toList());
        }
        return manifestList;
    }

    @Override
    public AfOrder getOrderByUUID(String orderUUID) {
        LambdaQueryWrapper<AfOrder> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(AfOrder::getOrderUuid, orderUUID);
        return baseMapper.selectOne(lambdaQueryWrapper);
    }

    @Override
    public void orderTrackShareWithEmail(OrderTrackShare orderTrackShare) throws Exception {
        orderTrackShare.checkRequired();
        String content = buildOrderTrackShareEmailContent(orderTrackShare);
        Path tmpPath = Files.createTempFile("ef_qr_", ".png");

        String imageBase64 = orderTrackShare.getImageURL();
        byte[] bytes = Base64.getDecoder().decode(imageBase64.substring(imageBase64.indexOf(",") + 1).getBytes(StandardCharsets.UTF_8));
        Path path = Files.write(tmpPath, bytes);
        HashMap<String, File> imgMap = new HashMap<>();
        imgMap.put("qr_image", path.toFile());
        try {
            mailSendService.sendHtmlMailNew(true, orderTrackShare.getToUsers().split(";"), orderTrackShare.getCcUsers().split(";"), null, orderTrackShare.getSubject(), content, imgMap);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            Files.deleteIfExists(tmpPath);
        }
    }

    @Override
    public ShippingBillData getMasterShippingBill(Integer orgId, String orderUUID) throws Exception {
        return this.getShippingBill(orgId, orderUUID, APIType.AE_CD_POST_MAWB);
    }

    @Override
    public ShippingBillData getShippersData(String hasMwb, String orderUUID, String letterIds) throws Exception {
        return this.getShippersData(hasMwb, orderUUID, APIType.AE_CD_POST_MAWB, letterIds);
    }

    @Override
    public Map<String, Object> sendShippersData(String hasMwb, String orderUUID, String letterIds) throws Exception {
        Map<String, Object> map = new HashMap();
        EUserDetails user = SecurityUtils.getUser();
        String apiType = APIType.AE_CD_AWB;
        OrgInterfaceVo config = getShippingBillConfig(user.getOrgId(), apiType);
        //获取总单数据XMl
        String mawbXML = "";
        //获取分单数据xml
        String hawbXML = "";
        if (!"hawb".equals(hasMwb)) {
            mawbXML = this.baseMapper.getNewMAWBXML(orderUUID, user.getId(), apiType);
        }
        if ("all".equals(hasMwb) || StringUtils.isNotEmpty(letterIds)) {
            hawbXML = this.baseMapper.getNewHAWBXML(orderUUID, letterIds, user.getId(), apiType);
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<data>");
        if (StringUtils.isNotBlank(mawbXML)) {
            mawbXML = mawbXML.substring(0, mawbXML.indexOf("<CargoTerminal>")) + "<IsOverride>1</IsOverride>" + mawbXML.substring(mawbXML.indexOf("<CargoTerminal>"));
            builder.append(mawbXML);
        }
        if (StringUtils.isNotBlank(hawbXML)) {
            if (mawbXML.length() > 0) {
                builder = new StringBuilder(builder.toString().replace("</AirwayBill>",
                        "<ConsolidationList>" + hawbXML.substring(hawbXML.indexOf("<ConsolidationList>") + 19, hawbXML.indexOf("</ConsolidationList>")) + "</ConsolidationList>"));
                builder.append("</AirwayBill>");
            } else {
                hawbXML = hawbXML.substring(0, hawbXML.indexOf("<CargoTerminal>")) + "<IsOverride>1</IsOverride>" + hawbXML.substring(hawbXML.indexOf("<CargoTerminal>"));
                builder.append(hawbXML);
            }
        }

//        if (StringUtils.isNotBlank(mawbXML)) {
//            builder.append(mawbXML);
//        }
//        if (StringUtils.isNotBlank(hawbXML)) {
//            builder.append(hawbXML);
//        }
        builder.append("</data>");

        ResponseEntity<String> responseEntity = RemoteSendUtils.sendToThirdUrl(config, builder.toString());
        String objStr = responseEntity.getBody();
        JSONObject jsonO = JSONObject.parseObject(objStr);
        String message = jsonO.getString("messageInfo");
        map.put("status", "success");
        map.put("message", "发送舱单成功");

        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            map.put("message", "发送舱单第三方接口调用异常：" + message);
            map.put("status", "error");
        }
        if ("01".equals(jsonO.getString("code"))) {
            LogBean logBean = new LogBean();
            logBean.setHasMwb(hasMwb);
            logBean.setOrderUuid(orderUUID);
            logBean.setLetterIds(letterIds);
            logBean.setPageFunction("发送舱单");
            logBean.setLogRemarkLarge(builder.toString());
            insertLogAfterSendShipper(logBean);
        } else {
            map.put("message", "发送舱单 报文异常：" + message);
            map.put("status", "exception");
        }
        return map;
    }

    @Override
    public Map<String, Object> deleteShipper(String orderUUID, String letterId) {
        Map<String, Object> map = new HashMap();
        EUserDetails user = SecurityUtils.getUser();
        String apiType = APIType.AE_CD_AWB;
        OrgInterfaceVo config = getShippingBillConfig(user.getOrgId(), apiType);

        StringBuilder builder = new StringBuilder();
        builder.append("<data>");
        //获取数据XMl
        String mawbXML = this.baseMapper.getDeleteMAWBXML(orderUUID, letterId, user.getId(), apiType);
        builder.append(mawbXML);

        builder.append("</data>");

        RestTemplate restTemplate = new RestTemplate();
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("data", builder.toString());

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(config.getUrlPost() + "Mft2201_Delete", body, String.class);
        String objStr = responseEntity.getBody();
        JSONObject jsonO = JSONObject.parseObject(objStr);
        String message = jsonO.getString("messageInfo");
        map.put("status", "success");
        map.put("message", "删除成功");
        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            map.put("message", "删除舱单第三方接口调用异常：" + message);
            map.put("status", "error");
        }
        if ("01".equals(jsonO.getString("code"))) {
            LogBean logBean = new LogBean();
            logBean.setHasMwb(null);
            logBean.setOrderUuid(orderUUID);
            logBean.setLetterIds(letterId);
            logBean.setPageFunction("删除舱单");
            logBean.setLogRemarkLarge(builder.toString());
            insertLogAfterSendShipper(logBean);
        } else {
            map.put("message", "删除舱单 报文异常：" + message);
            map.put("status", "exception");
        }
        return map;
    }

    @Override
    public void saveRouteInfo(String awbNumber, String hawNumber, String businessScope) {
        this.awbRouteService.saveRouteInfo(awbNumber, hawNumber, businessScope);
    }


    @Override
    public Map<String, Object> getAiOrderById(Integer orderId) {
        Map<String, Object> bean = baseMapper.selectByOrderId(orderId);
        return bean;
    }

    @Override
    public Map<String, Integer> checkCargoTrackingQuery(String awbNumber) {
        Assert.hasText(awbNumber, "主单号不能为空");
        AfAwbRoute afAwbRoute = awbRouteService.findByAwbNumber(awbNumber);
        int count = this.afOrderMapper.countCarrierTaskList(awbNumber.substring(0, 3));
        Map<String, Integer> result = new HashMap<>(2);
        result.put("awbRoute", afAwbRoute == null ? 0 : 1);
        result.put("carrierTracklist", count);
        return result;
    }

    @Override
    public void exportOperationLookExcel(AfOrder bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        Assert.notNull(bean, "参数不能为空");
        Assert.hasText(bean.getColumnStrs(), "未获取到导出的列信息");

        JSONArray jsonArr = JSONArray.parseArray(bean.getColumnStrs());
        String[] headers = new String[jsonArr.size()];
        ArrayList<String> keyList = new ArrayList<>(jsonArr.size());
        //生成表头跟字段
        if (jsonArr != null && jsonArr.size() > 0) {
            for (int i = 0; i < jsonArr.size(); i++) {
                JSONObject job = jsonArr.getJSONObject(i);
                headers[i] = job.getString("label");
                keyList.add(job.getString("prop"));
            }
        }

        List<List<Map<String, Object>>> list = baseMapper.getOperationLookPageList(bean, null, null);
        if (list.size() != 2) {
            throw new IllegalArgumentException("数据查询失败");
        }
        //列表数据
        List<Map<String, Object>> dataList = list.get(0);
        //合计数据
        Map<String, Object> summaryData = list.get(1).get(0);

        //数据重构
        LinkedHashMap<String, Object> exportSummary = new LinkedHashMap<>(1);
        List<LinkedHashMap> exportData = dataList.stream().map((item) -> {
            LinkedHashMap<String, Object> temp = new LinkedHashMap<>();
            keyList.forEach((key) -> {
                temp.put(key, item.get(key));
                exportSummary.put(key, summaryData.get(key));
            });
            return temp;
        }).collect(Collectors.toList());

        //数据处理
        String[] processKey = new String[]{"audit", "arrived", "passed", "checked", "tag"};
        exportData.stream().forEach((item) -> {
            for (String key : processKey) {
                if (item.containsKey(key)) {
                    item.put(key, Objects.equals(item.get(key).toString(), "1") ? "√" : "");
                }
            }
        });

        exportSummary.put(keyList.get(0), "合计");
        exportData.add(exportSummary);

        //数据导出
        new ExcelExportUtils().exportExcelLinkListMap(LoginUtils.getResponse(), "导出EXCEL", headers, exportData, "Export");
    }

    @Override
    public OrderTrack cargoTracking(String awbNumber, String hawbNumber, String businessScope) {
        EUserDetails loginUser = SecurityUtils.getUser();
        Subscribe subscribe = new Subscribe(loginUser.getId(), loginUser.getOrgId());
        subscribe.setAwbNumber(awbNumber);
        subscribe.setHawbNumber(hawbNumber);
        subscribe.setBusinessScope(businessScope);

        this.awbSubscriptionService.checkOrgAdditionalService(subscribe);
        OrderTrackQuery orderTrackQuery = new OrderTrackQuery(awbNumber, businessScope, loginUser.getOrgId());
        orderTrackQuery.setHawbNumber(hawbNumber);

        return this.getOrderTrack(orderTrackQuery);
    }

    @Override
    public CargoTrack cargoTrack(CargoTrackQuery trackQuery) {
        trackQuery.validate();
        trackQuery.setAwbNumber(formatNumber(trackQuery.getAwbNumber()));

        UserBaseVO userBase = getUserByPhone(trackQuery);
        Assert.notNull(userBase, "用户不存在");

        //订阅数据
        Subscribe subscribe = new Subscribe(userBase.getUserId(), userBase.getOrgId());
        subscribe.setAwbNumber(trackQuery.getAwbNumber());
        subscribe.setHawbNumber(trackQuery.getHawbNumber());
        subscribe.setBusinessScope(trackQuery.businessScope());
        subscribe.setCreateIp(trackQuery.getIp());
        awbSubscriptionService.cargoTrackingSubscribe(subscribe);

        CargoTrack cargoTrack = new CargoTrack();
        //套餐用量信息
        addServiceMeal(cargoTrack, userBase.getOrgId());
        //舱单,轨迹信息
        addTrackInformation(cargoTrack, trackQuery, userBase.getOrgId());
        return cargoTrack;
    }

    private void addServiceMeal(CargoTrack cargoTrack, Integer orgId) {
        OrgServiceMealConfigVo orgServiceMealConfig = awbSubscriptionService.orgAdditionalService(orgId, CommonConstants.ORG_ADDITIONAL_SERVICE_TYPE.TRACK_AE_AI);
        cargoTrack.setTotal(orgServiceMealConfig != null ? orgServiceMealConfig.getServiceNumberMax() : 0);
        cargoTrack.setUsed(orgServiceMealConfig != null ? orgServiceMealConfig.getServiceNumberUsed() : 0);
    }

    private void addTrackInformation(CargoTrack cargoTrack, CargoTrackQuery trackQuery, Integer orgId) {
        OrderTrackQuery orderTrackQuery = new OrderTrackQuery(trackQuery.getAwbNumber(), trackQuery.businessScope(), orgId);
        orderTrackQuery.setHawbNumber(trackQuery.getHawbNumber());

        OrderTrack orderTrack = this.getOrderTrack(orderTrackQuery);
        if (null == orderTrack) {
            return;
        }
        cargoTrack.setManifestList(orderTrack.getManifestList());

        //舱单信息
        List<AfAwbRouteTrackManifest> routeTrackManifestList = orderTrack.getTrackManifest();
        List<CargoRoute> routeTrackList = routeTrackManifestList.stream().map((item) -> {
            CargoRoute cargoRoute = new CargoRoute();
            cargoRoute.setAwbNumber(item.getAwbNumber());
            cargoRoute.setHawbNumber(item.getHawbNumber());
            cargoRoute.setEventTime(item.getEventTime());
            cargoRoute.setQuantity(item.getQuantity());
            cargoRoute.setGrossWeight(item.getGrossWeight());
            cargoRoute.setRemark(item.getRemark());
            cargoRoute.setSourceSyscode(item.getSourceSyscode());
            return cargoRoute;
        }).collect(Collectors.toList());
        cargoTrack.setTrackManifest(routeTrackList);

        //轨迹信息
        List<AfAwbRouteTrackAwb> routeTracks = orderTrack.getRouteTracks();
        List<CargoRoute> cargoRoutes = routeTracks.stream().map((item) -> {
            CargoRoute cargoRoute = new CargoRoute();
            cargoRoute.setAwbNumber(item.getAwbNumber());
            Optional.ofNullable(item.getEventTime()).ifPresent((date) -> {
                cargoRoute.setEventTime(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
            });
            cargoRoute.setQuantity(item.getQuantity());
            cargoRoute.setGrossWeight(item.getGrossWeight());
            cargoRoute.setRemark(item.getRemark());
            cargoRoute.setSourceSyscode(item.getSourceSyscode());

            cargoRoute.setFlightNum(item.getFlightNum());
            cargoRoute.setFlightStatusCode(item.getFlightStatusCode());
            cargoRoute.setFlightStatusName(item.getFlightStatusName());

            return cargoRoute;
        }).collect(Collectors.toList());
        cargoTrack.setRouteTracks(cargoRoutes);

    }

    private UserBaseVO getUserByPhone(CargoTrackQuery query) {
        MessageInfo<UserBaseVO> result = remoteServiceToHRS.findUserByPhone(query.getPhone(), query.getInternationalCountryCode(), SecurityConstants.FROM_IN);
        if (null == result || result.getCode() != 0) {
            throw new RuntimeException("用户信息加载失败");
        }
        return result.getData();
    }

    @Override
    public ShippingBillData getWaybillData(Integer orgId, String orderUUID) throws Exception {
        return this.getShippingBill(orgId, orderUUID, APIType.AE_DZ_POST_MAWB);
    }

    @Override
    public ShippingBillData getTagMakeData(Integer orgId, String orderUUID) throws Exception {
        return this.getShippingBill(orgId, orderUUID, APIType.BQ_POST_MAWB);
    }

    @Override
    public List<Map<String, Object>> homeStatistics(Integer orgId) {
        return this.baseMapper.homeStatistics(orgId);
    }

    @Override
    public List<Map<String, Object>> selectCompany(Integer orgId) {
        return this.baseMapper.selectCompany(orgId);
    }

    @Override
    public String getOrderCostStatusForAF(Integer orderId) {
        Map<String, String> result = baseMapper.getOrderCostStatusForAF(orderId);
        if (result == null) {
            return "未录成本";
        }
        if ("1".equals(result.get("completeWriteoffFlag"))) {
            return "核销完毕";
        }
        if ("1".equals(result.get("writeoffFlag"))) {
            return "部分核销";
        }
        Map<String, String> invoiceStatus = baseMapper.getOrderCostStatusForAFAboutInvoiceStatus(orderId, SecurityUtils.getUser().getOrgId());
        if ("1".equals(invoiceStatus.get("completeInvoice"))) {
            return "完全收票";
        }
        if ("1".equals(invoiceStatus.get("noInvoice"))) {
            return "付款申请";
        }
        if ("1".equals(invoiceStatus.get("InvoiceNoComplete"))) {
            return "部分收票";
        }
        if ("1".equals(result.get("paymentFlag"))) {
            return "已对账";
        }
        if ("1".equals(result.get("costFlag"))) {
            return "已录成本";
        }
        return null;
    }

    @Override
    public String getOrderCostStatusForLC(Integer orderId) {
        Map<String, String> result = baseMapper.getOrderCostStatusForLC(orderId);
        if (result == null) {
            return "未录成本";
        }
        if ("1".equals(result.get("completeWriteoffFlag"))) {
            return "核销完毕";
        }
        if ("1".equals(result.get("writeoffFlag"))) {
            return "部分核销";
        }
        Map<String, String> invoiceStatus = baseMapper.getOrderCostStatusForLCAboutInvoiceStatus(orderId, SecurityUtils.getUser().getOrgId());
        if ("1".equals(invoiceStatus.get("completeInvoice"))) {
            return "完全收票";
        }
        if ("1".equals(invoiceStatus.get("noInvoice"))) {
            return "付款申请";
        }
        if ("1".equals(invoiceStatus.get("InvoiceNoComplete"))) {
            return "部分收票";
        }
        if ("1".equals(result.get("paymentFlag"))) {
            return "已对账";
        }
        if ("1".equals(result.get("costFlag"))) {
            return "已录成本";
        }
        return null;
    }

    @Override
    public String getOrderCostStatusForIO(Integer orderId) {
        Map<String, String> result = baseMapper.getOrderCostStatusForIO(orderId);
        if (result == null) {
            return "未录成本";
        }
        if ("1".equals(result.get("completeWriteoffFlag"))) {
            return "核销完毕";
        }
        if ("1".equals(result.get("writeoffFlag"))) {
            return "部分核销";
        }
        Map<String, String> invoiceStatus = baseMapper.getOrderCostStatusForIOAboutInvoiceStatus(orderId, SecurityUtils.getUser().getOrgId());
        if ("1".equals(invoiceStatus.get("completeInvoice"))) {
            return "完全收票";
        }
        if ("1".equals(invoiceStatus.get("noInvoice"))) {
            return "付款申请";
        }
        if ("1".equals(invoiceStatus.get("InvoiceNoComplete"))) {
            return "部分收票";
        }
        if ("1".equals(result.get("paymentFlag"))) {
            return "已对账";
        }
        if ("1".equals(result.get("costFlag"))) {
            return "已录成本";
        }
        return null;
    }

    @Override
    public String getOrderCostStatusForSC(Integer orderId) {
        Map<String, String> result = baseMapper.getOrderCostStatusForSC(orderId);
        if (result == null) {
            return "未录成本";
        }
        if ("1".equals(result.get("completeWriteoffFlag"))) {
            return "核销完毕";
        }
        if ("1".equals(result.get("writeoffFlag"))) {
            return "部分核销";
        }
        Map<String, String> invoiceStatus = baseMapper.getOrderCostStatusForSCAboutInvoiceStatus(orderId, SecurityUtils.getUser().getOrgId());
        if ("1".equals(invoiceStatus.get("completeInvoice"))) {
            return "完全收票";
        }
        if ("1".equals(invoiceStatus.get("noInvoice"))) {
            return "付款申请";
        }
        if ("1".equals(invoiceStatus.get("InvoiceNoComplete"))) {
            return "部分收票";
        }
        if ("1".equals(result.get("paymentFlag"))) {
            return "已对账";
        }
        if ("1".equals(result.get("costFlag"))) {
            return "已录成本";
        }
        return null;
    }

    public String getOrderCostStatusForTC(Integer orderId) {
        Map<String, String> result = baseMapper.getOrderCostStatusForTC(orderId);
        if (result == null) {
            return "未录成本";
        }
        if ("1".equals(result.get("completeWriteoffFlag"))) {
            return "核销完毕";
        }
        if ("1".equals(result.get("writeoffFlag"))) {
            return "部分核销";
        }
        Map<String, String> invoiceStatus = baseMapper.getOrderCostStatusForTCAboutInvoiceStatus(orderId, SecurityUtils.getUser().getOrgId());
        if ("1".equals(invoiceStatus.get("completeInvoice"))) {
            return "完全收票";
        }
        if ("1".equals(invoiceStatus.get("noInvoice"))) {
            return "付款申请";
        }
        if ("1".equals(invoiceStatus.get("InvoiceNoComplete"))) {
            return "部分收票";
        }
        if ("1".equals(result.get("paymentFlag"))) {
            return "已对账";
        }
        if ("1".equals(result.get("costFlag"))) {
            return "已录成本";
        }
        return null;
    }

    public void updateOrderCostStatusForTC(Integer orderId) {
        baseMapper.updateOrderCostStatusForTC(orderId, getOrderCostStatusForTC(orderId), UUID.randomUUID().toString());
    }

    @Override
    public List<OrderForVL> getOrderListForVL(OrderForVL orderForVL) {
        LambdaQueryWrapper<AfOrder> wrapper = Wrappers.<AfOrder>lambdaQuery();
        if (StrUtil.isNotBlank(orderForVL.getOrderCode())) {
            wrapper.like(AfOrder::getOrderCode, orderForVL.getOrderCode());
        }

        if (StrUtil.isNotBlank(orderForVL.getAwbNumber())) {
            wrapper.like(AfOrder::getAwbNumber, orderForVL.getAwbNumber());
        }

        if (StrUtil.isNotBlank(orderForVL.getCustomerNumber())) {
            wrapper.like(AfOrder::getCustomerNumber, orderForVL.getCustomerNumber());
        }

        if (orderForVL.getBusinessScope().equals("AE") && orderForVL.getFlightDateStart() != null) {
            wrapper.ge(AfOrder::getExpectDeparture, orderForVL.getFlightDateStart());
        }
        if (orderForVL.getBusinessScope().equals("AE") && orderForVL.getFlightDateEnd() != null) {
            wrapper.le(AfOrder::getExpectDeparture, orderForVL.getFlightDateEnd());
        }
        if (orderForVL.getBusinessScope().equals("AI") && orderForVL.getFlightDateStart() != null) {
            wrapper.ge(AfOrder::getExpectArrival, orderForVL.getFlightDateStart());
        }
        if (orderForVL.getBusinessScope().equals("AI") && orderForVL.getFlightDateEnd() != null) {
            wrapper.le(AfOrder::getExpectArrival, orderForVL.getFlightDateEnd());
        }
        if (StrUtil.isNotBlank(orderForVL.getNoOrderIds())) {
            wrapper.notIn(AfOrder::getOrderId, orderForVL.getNoOrderIds().split(","));
        }
        wrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfOrder::getBusinessScope, orderForVL.getBusinessScope()).ne(AfOrder::getCostRecorded, true).notIn(AfOrder::getOrderStatus, "强制关闭", "财务锁账");
        if (orderForVL.getBusinessScope().equals("AE")) {
            wrapper.orderByAsc(AfOrder::getExpectDeparture, AfOrder::getAwbNumber);
        } else {
            wrapper.orderByAsc(AfOrder::getExpectArrival, AfOrder::getAwbNumber);
        }
        return list(wrapper).stream().map(afOrder -> {
            OrderForVL order = new OrderForVL();
            BeanUtils.copyProperties(afOrder, order);
            if (orderForVL.getBusinessScope().equals("AE")) {
                order.setFlightDate(afOrder.getExpectDeparture());
            } else {
                order.setFlightDate(afOrder.getExpectArrival());
            }
            order.setPlanVolume(afOrder.getPlanVolume() != null ? BigDecimal.valueOf(afOrder.getPlanVolume()) : null);
            order.setConfirmVolume(afOrder.getConfirmVolume() != null ? BigDecimal.valueOf(afOrder.getConfirmVolume()) : null);
            order.setPlanChargeWeight(afOrder.getPlanChargeWeight() != null ? BigDecimal.valueOf(afOrder.getPlanChargeWeight()) : null);
            order.setConfirmChargeWeight(afOrder.getConfirmChargeWeight() != null ? BigDecimal.valueOf(afOrder.getConfirmChargeWeight()) : null);
            return order;
        }).collect(Collectors.toList());
    }

    @Override
    public String getMasterShippingBillCheck(String type, String hasMwb, String orderUUID, String letterIds) {
        //校验主单必填信息
        return this.baseMapper.getMAWBXMLCheck(type, hasMwb, orderUUID, letterIds, SecurityUtils.getUser().getId());
    }

    @Override
    public void airCargoManifestPrint(Integer orderId) {
        List<List<AirCargoManifestPrint>> result = baseMapper.airCargoManifestPrint(orderId, SecurityUtils.getUser().getOrgId());
        ArrayList<AirCargoManifestPrint> list = new ArrayList<>();
        AirCargoManifestPrint airCargoManifestPrint = result.get(0).get(0);
        List<AirCargoManifestPrint> airCargoManifestPrintList = result.get(1);
        if (airCargoManifestPrintList.size() == 0) {
            AirCargoManifestPrint headerInfo = new AirCargoManifestPrint();
            headerInfo.setAwbNumber(airCargoManifestPrint.getAwbNumber());
            headerInfo.setDepartureStation(airCargoManifestPrint.getDepartureStation());
            headerInfo.setArrivalStation(airCargoManifestPrint.getArrivalStation());
            headerInfo.setFlightNo(airCargoManifestPrint.getFlightNo());
            headerInfo.setFlightDate(airCargoManifestPrint.getFlightDate());
            headerInfo.setPageInfo("1/1");
            ArrayList<AirCargoManifestPrint> hawbList = new ArrayList<>();
            hawbList.add(airCargoManifestPrint);
            headerInfo.setList(hawbList);
            list.add(headerInfo);
        } else {
            HashMap<String, Integer> num = new HashMap<>();
            num.put("num", 1);
            ArrayList<AirCargoManifestPrint> airCargoManifestPrints = new ArrayList<>();
            airCargoManifestPrints.add(airCargoManifestPrint);
            airCargoManifestPrints.addAll(airCargoManifestPrintList);
            List<List<AirCargoManifestPrint>> averageAssign = this.averageAssign(airCargoManifestPrints, 4);
            averageAssign.stream().forEach(itemList -> {
                AirCargoManifestPrint headerInfo = new AirCargoManifestPrint();
                headerInfo.setAwbNumber(airCargoManifestPrint.getAwbNumber());
                headerInfo.setDepartureStation(airCargoManifestPrint.getDepartureStation());
                headerInfo.setArrivalStation(airCargoManifestPrint.getArrivalStation());
                headerInfo.setFlightNo(airCargoManifestPrint.getFlightNo());
                headerInfo.setFlightDate(airCargoManifestPrint.getFlightDate());
                headerInfo.setPageInfo(num.get("num") + "/" + averageAssign.size());
                headerInfo.setList(itemList);
                list.add(headerInfo);
                num.put("num", num.get("num") + 1);
            });
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("list", list);
        JxlsUtils.exportExcelWithLocalModel(FilePathUtils.filePath + "/PDFtemplate/AIR CARGO MANIFEST.xlsx", map);
    }

    @Override
    public Boolean shippingSendCheckHasSend(String orderUUID) {
        AfOrder order = baseMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), orderUUID);
        if (StringUtils.isNotEmpty(order.getManifestStatus())) {//已经发送过舱单
            return true;
        } else {//未发送过舱单
            return false;
        }
    }

    @Override
    public Boolean insertLogAfterSendShipper(LogBean logBean) {
        try {
            String uuid = logBean.getOrderUuid();
            AfOrder bean = this.baseMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), uuid);
            logBean.setPageName("AE订单");
            logBean.setBusinessScope("AE");

            logBean.setOrderNumber(bean.getOrderCode());
            logBean.setOrderId(bean.getOrderId());
            logService.saveLog(logBean);

            //更新订单表/分单表 manifest_status
            this.baseMapper.updateManifestStatus(logBean.getHasMwb(), uuid, logBean.getLetterIds(), "HAS_SEND");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
//
//    @Override
//    public List<Map<String, Object>> getShipperByLetterId(Integer orderId,Integer letterId) {
////        AEOrder order = this.getOrderById(orderId);
//        return baseMapper.getShipperByLetterId(letterId);
//    }

    @Override
    public List<Map<String, Object>> getOpreationLookList(AfOrder bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        List<List<Map<String, Object>>> list = baseMapper.getOperationLookPageList(bean, null, null);
        return list.isEmpty() ? new ArrayList<>() : list.get(0);
    }

    @Override
    public HashMap<String, Object> getOperaLookListPage(Page page, AfOrder bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        List<List<Map<String, Object>>> list = baseMapper.getOperationLookPageList(bean, Long.valueOf(page.getCurrent()).intValue(), Long.valueOf(page.getSize()).intValue());

        HashMap map = new HashMap();
        if (list != null && list.size() > 0 && list.get(0) != null && list.get(0).size() > 0) {
            map.put("resultOne", list.get(0));
            map.put("resultTwo", list.get(1).get(0));
        }
        return map;
    }

    @Override

    public Boolean saveShippers(AfOrder bean) {
        AfOrder order = baseMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!bean.getRowUuid().equals(order.getRowUuid())) {
            throw new RuntimeException("订单不是最新数据，请刷新页面再操作");
        }

        //添加日志信息
        LogBean logBean = new LogBean();
        logBean.setPageName("操作看板");
        logBean.setPageFunction("编辑舱单");
        logBean.setLogRemark(this.getLogRemark(order, bean));
        logBean.setBusinessScope("AE");
        logBean.setOrderNumber(bean.getOrderCode());
        logBean.setOrderId(bean.getOrderId());
        logBean.setOrderUuid(bean.getOrderUuid());
        logService.saveLog(logBean);

        bean.setRowUuid(UUID.randomUUID().toString());
        bean.setEditTime(new Date());
        bean.setEditorId(SecurityUtils.getUser().getId());
        bean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        UpdateWrapper<AfOrder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("order_id", bean.getOrderId());
        baseMapper.update(bean, updateWrapper);

        //收发货人修改
        AfOrderShipperConsignee afOrderShipperConsignee = bean.getAfOrderShipperConsignee1();
        if (afOrderShipperConsignee != null) {
            afOrderShipperConsignee.setEditTime(LocalDateTime.now());
            afOrderShipperConsignee.setEditorId(SecurityUtils.getUser().getId());
            afOrderShipperConsignee.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());

            afOrderShipperConsigneeMapper.updateById(afOrderShipperConsignee);
        }
        AfOrderShipperConsignee afOrderShipperConsignee2 = bean.getAfOrderShipperConsignee2();
        if (afOrderShipperConsignee2 != null) {
            afOrderShipperConsignee2.setEditTime(LocalDateTime.now());
            afOrderShipperConsignee2.setEditorId(SecurityUtils.getUser().getId());
            afOrderShipperConsignee2.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            afOrderShipperConsigneeMapper.updateById(afOrderShipperConsignee2);
        }

        //插入分单信息

        LambdaQueryWrapper<AfShipperLetter> afShipperLetterWrapper = Wrappers.<AfShipperLetter>lambdaQuery();
        afShipperLetterWrapper.eq(AfShipperLetter::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfShipperLetter::getOrderId, bean.getOrderId());
        afShipperLetterService.remove(afShipperLetterWrapper);

        LambdaQueryWrapper<AfOrderShipperConsignee> afOrderShipperConsigneeWrapper = Wrappers.<AfOrderShipperConsignee>lambdaQuery();
        afOrderShipperConsigneeWrapper.eq(AfOrderShipperConsignee::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfOrderShipperConsignee::getOrderId, bean.getOrderId()).isNotNull(AfOrderShipperConsignee::getSlId);
        afOrderShipperConsigneeService.remove(afOrderShipperConsigneeWrapper);
        if (bean.getShipperLetters().size() > 0) {
            bean.getShipperLetters().stream().forEach(afShipperLetter -> {
                afShipperLetter.setOrderId(bean.getOrderId());
                afShipperLetter.setOrgId(SecurityUtils.getUser().getOrgId());
                afShipperLetter.setSlType("HAWB");
                afShipperLetter.setOrderUuid(bean.getOrderUuid());
                afShipperLetter.setCreateTime(LocalDateTime.now());
                afShipperLetter.setCreatorId(SecurityUtils.getUser().getId());
                afShipperLetter.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());

                afShipperLetter.setEditTime(LocalDateTime.now());
                afShipperLetter.setEditorId(SecurityUtils.getUser().getId());
                afShipperLetter.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            });
            afShipperLetterService.saveBatch(bean.getShipperLetters());


            //新增分单收发货人
            ArrayList<AfOrderShipperConsignee> afOrderConsigneeList = new ArrayList<>();
            ArrayList<AfOrderShipperConsignee> afOrderShipperList = new ArrayList<>();
            bean.getShipperLetters().stream().forEach(afShipperLetter -> {
                AfOrderShipperConsignee afOrderConsignee = afShipperLetter.getAfOrderShipperConsignee1();
                if (afOrderConsignee != null) {
                    afOrderConsignee.setOrderId(afShipperLetter.getOrderId());
                    afOrderConsignee.setSlId(afShipperLetter.getSlId());
                    afOrderConsignee.setCreateTime(LocalDateTime.now());
                    afOrderConsignee.setCreatorId(SecurityUtils.getUser().getId());
                    afOrderConsignee.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    afOrderConsignee.setOrgId(SecurityUtils.getUser().getOrgId());
                    afOrderConsigneeList.add(afOrderConsignee);
                }
                AfOrderShipperConsignee afOrderShipper = afShipperLetter.getAfOrderShipperConsignee2();
                if (afOrderShipper != null) {
                    afOrderShipper.setSlId(afShipperLetter.getSlId());
                    afOrderShipper.setOrderId(afShipperLetter.getOrderId());
                    afOrderShipper.setCreateTime(LocalDateTime.now());
                    afOrderShipper.setCreatorId(SecurityUtils.getUser().getId());
                    afOrderShipper.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    afOrderShipper.setOrgId(SecurityUtils.getUser().getOrgId());
                    afOrderShipperList.add(afOrderShipper);
                }
            });
            afOrderShipperConsigneeService.saveBatch(afOrderConsigneeList);
            afOrderShipperConsigneeService.saveBatch(afOrderShipperList);
        }
        return true;
    }

    private <T> List<List<T>> averageAssign(List<T> source, int n) {
        if (null == source || source.size() == 0 || n <= 0)
            return null;
        List<List<T>> result = new ArrayList<List<T>>();
        int sourceSize = source.size();
        int size = 0;
        if (sourceSize % n == 0) {
            size = source.size() / n;
        } else {
            size = (source.size() / n) + 1;
        }
        for (int i = 0; i < size; i++) {
            List<T> subset = new ArrayList<T>();
            for (int j = i * n; j < (i + 1) * n; j++) {
                if (j < sourceSize) {
                    subset.add(source.get(j));
                }
            }
            result.add(subset);
        }
        return result;
    }

    @Override
    public void updateOrderCostStatusForSC(Integer orderId) {
        log.info(getOrderCostStatusForSC(orderId));
        baseMapper.updateOrderCostStatusForSC(orderId, getOrderCostStatusForSC(orderId), UUID.randomUUID().toString());
    }

    @Override
    public String printBusinessCalculationBill(String businessScope, Integer orderId, Boolean ifReplace) {

        //整理数据
        String name = "";
        BusinessCalculationBill tableHeader = new BusinessCalculationBill();
        List<BusinessCalculationBill> businessCalculationBillList = new ArrayList<>();
        List<BusinessCalculationBill> incomeList = new ArrayList<>();
        List<BusinessCalculationBill> costList = new ArrayList<>();
        HashMap<String, BigDecimal> incomeMap = new HashMap<>();
        HashMap<String, BigDecimal> costMap = new HashMap<>();
        HashMap<String, BigDecimal> businessCalculationMap = new HashMap<>();
        if (businessScope.startsWith("L")) {
            //整理业务核销单上半部数据-数据来源于订单
            LcOrder lcOrder = lcOrderService.getById(orderId);
            name = "ORDER_ACCOUNT_SHEET_" + lcOrder.getOrderCode();

            tableHeader.setOrderCode(lcOrder.getOrderCode());
            if (lcOrder.getCoopId() != null) {
                CoopVo coopVo = remoteCoopService.viewCoop(lcOrder.getCoopId().toString()).getData();
                if (coopVo != null) {
                    tableHeader.setCustomerName(coopVo.getCoop_name());
                }
            }
            if (StrUtil.isNotBlank(lcOrder.getCustomerNumber())) {
                tableHeader.setAwbNumber(lcOrder.getCustomerNumber());
            } else {
                tableHeader.setAwbNumber("");
            }
            tableHeader.setSalesName(lcOrder.getSalesName().split(" ")[0]);
            tableHeader.setServicerName(lcOrder.getServicerName().split(" ")[0]);
            tableHeader.setDepartureStation(lcOrder.getDepartureStation());
            tableHeader.setArrivalStation(lcOrder.getArrivalStation());
            tableHeader.setFlightNumber("");
            tableHeader.setFlightDate(lcOrder.getDrivingTime().toLocalDate());
            tableHeader.setPwvInfo("");
            tableHeader.setChargeWeight(lcOrder.getPlanChargeWeight() == null ? "" : lcOrder.getPlanChargeWeight().toString());
            tableHeader.setCustomerNumber(lcOrder.getCustomerNumber());

            //整理应付明细数据
            LambdaQueryWrapper<LcCost> lcCostWrapper = Wrappers.<LcCost>lambdaQuery();
            lcCostWrapper.eq(LcCost::getOrderId, orderId).eq(LcCost::getOrgId, SecurityUtils.getUser().getOrgId());
            List<LcCost> lcCostList = lcCostService.list(lcCostWrapper);
            costList = lcCostList.stream().map(lcCost -> {

                if (costMap.get(lcCost.getCostCurrency()) == null) {
                    costMap.put(lcCost.getCostCurrency(), lcCost.getCostAmount());
                } else {
                    costMap.put(lcCost.getCostCurrency(), costMap.get(lcCost.getCostCurrency()).add(lcCost.getCostAmount()));
                }
                if (costMap.get("function") == null) {
                    costMap.put("function", lcCost.getCostFunctionalAmount());
                } else {
                    costMap.put("function", costMap.get("function").add(lcCost.getCostFunctionalAmount()));
                }

                BusinessCalculationBill businessCalculationBill = new BusinessCalculationBill();
                businessCalculationBill.setCostAmount(lcCost.getCostAmount());
                businessCalculationBill.setCostAmountStr(FormatUtils.formatWithQWF(lcCost.getCostAmount(), 2));
                businessCalculationBill.setCostFunctionalAmount(lcCost.getCostFunctionalAmount());
                businessCalculationBill.setCostFunctionalAmountStr(FormatUtils.formatWithQWF(lcCost.getCostFunctionalAmount(), 2));
                businessCalculationBill.setCustomerName(lcCost.getCustomerName());
                businessCalculationBill.setServiceName(lcCost.getServiceName());
                businessCalculationBill.setServiceRemark(lcCost.getServiceRemark());
                businessCalculationBill.setBusinessScope(lcCost.getBusinessScope());
                return businessCalculationBill;
            }).collect(Collectors.toList());

            //整理应付合计
            StringBuffer costSum = new StringBuffer();
            for (Map.Entry<String, BigDecimal> entry : costMap.entrySet()) {
                if (!"function".equals(entry.getKey())) {
                    if (StrUtil.isBlank(costSum.toString())) {
                        costSum.append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    } else {
                        costSum.append(" ").append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    }
                }
            }
            BusinessCalculationBill costSumBusinessCalculationBill = new BusinessCalculationBill();
            costSumBusinessCalculationBill.setBusinessScope("应付合计");
            costSumBusinessCalculationBill.setCostAmountStr(costSum.toString());
            costSumBusinessCalculationBill.setCostFunctionalAmountStr(FormatUtils.formatWithQWF(costMap.get("function"), 2));
            costList.add(costSumBusinessCalculationBill);


            //整理应收明细数据
            LambdaQueryWrapper<LcIncome> lcIncomeWrapper = Wrappers.<LcIncome>lambdaQuery();
            lcIncomeWrapper.eq(LcIncome::getOrderId, orderId).eq(LcIncome::getOrgId, SecurityUtils.getUser().getOrgId());
            List<LcIncome> lcIncomeList = lcIncomeService.list(lcIncomeWrapper);

            incomeList = lcIncomeList.stream().map(lcIncome -> {

                if (incomeMap.get(lcIncome.getIncomeCurrency()) == null) {
                    incomeMap.put(lcIncome.getIncomeCurrency(), lcIncome.getIncomeAmount());
                } else {
                    incomeMap.put(lcIncome.getIncomeCurrency(), incomeMap.get(lcIncome.getIncomeCurrency()).add(lcIncome.getIncomeAmount()));
                }
                if (incomeMap.get("function") == null) {
                    incomeMap.put("function", lcIncome.getIncomeFunctionalAmount());
                } else {
                    incomeMap.put("function", incomeMap.get("function").add(lcIncome.getIncomeFunctionalAmount()));
                }

                BusinessCalculationBill businessCalculationBill = new BusinessCalculationBill();
                businessCalculationBill.setIncomeAmount(lcIncome.getIncomeAmount());
                businessCalculationBill.setIncomeAmountStr(FormatUtils.formatWithQWF(lcIncome.getIncomeAmount(), 2));
                businessCalculationBill.setIncomeFunctionalAmount(lcIncome.getIncomeFunctionalAmount());
                businessCalculationBill.setIncomeFunctionalAmountStr(FormatUtils.formatWithQWF(lcIncome.getIncomeFunctionalAmount(), 2));
                businessCalculationBill.setCustomerName(lcIncome.getCustomerName());
                businessCalculationBill.setServiceName(lcIncome.getServiceName());
                businessCalculationBill.setServiceRemark(lcIncome.getServiceRemark());
                businessCalculationBill.setBusinessScope(lcIncome.getBusinessScope());
                return businessCalculationBill;
            }).collect(Collectors.toList());

            //整理应收合计
            StringBuffer incomeSum = new StringBuffer();
            for (Map.Entry<String, BigDecimal> entry : incomeMap.entrySet()) {
                if (!"function".equals(entry.getKey())) {
                    if (StrUtil.isBlank(incomeSum.toString())) {
                        incomeSum.append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    } else {
                        incomeSum.append(" ").append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    }
                }
            }
            BusinessCalculationBill incomeSumBusinessCalculationBill = new BusinessCalculationBill();
            incomeSumBusinessCalculationBill.setBusinessScope("应收合计");
            incomeSumBusinessCalculationBill.setIncomeAmountStr(incomeSum.toString());
            incomeSumBusinessCalculationBill.setIncomeFunctionalAmountStr(FormatUtils.formatWithQWF(incomeMap.get("function"), 2));
            incomeList.add(incomeSumBusinessCalculationBill);

            //整理毛利核算数据
            businessCalculationBillList = baseMapper.selectBusinessCalculationForLC(orderId);
        } else if (businessScope.equals("IO")) {
            //整理业务核销单上半部数据-数据来源于订单
            IoOrder ioOrder = ioOrderService.getById(orderId);
            name = "ORDER_ACCOUNT_SHEET_" + ioOrder.getOrderCode();

            tableHeader.setOrderCode(ioOrder.getOrderCode());
            if (ioOrder.getCoopId() != null) {
                CoopVo coopVo = remoteCoopService.viewCoop(ioOrder.getCoopId().toString()).getData();
                if (coopVo != null) {
                    tableHeader.setCustomerName(coopVo.getCoop_name());
                }
            }
            if (StrUtil.isNotBlank(ioOrder.getCustomerNumber())) {
                tableHeader.setAwbNumber(ioOrder.getCustomerNumber());
            } else {
                tableHeader.setAwbNumber("");
            }
            tableHeader.setSalesName(ioOrder.getSalesName().split(" ")[0]);
            tableHeader.setServicerName(ioOrder.getServicerName().split(" ")[0]);
            tableHeader.setDepartureStation(ioOrder.getDepartureStation());
            tableHeader.setArrivalStation(ioOrder.getArrivalStation());
            tableHeader.setFlightNumber("");
            tableHeader.setFlightDate(ioOrder.getBusinessDate());
            tableHeader.setPwvInfo("");
            tableHeader.setChargeWeight(ioOrder.getPlanChargeWeight() == null ? "" : ioOrder.getPlanChargeWeight().toString());
            tableHeader.setCustomerNumber(ioOrder.getCustomerNumber());

            //整理应付明细数据
            LambdaQueryWrapper<IoCost> ioCostWrapper = Wrappers.<IoCost>lambdaQuery();
            ioCostWrapper.eq(IoCost::getOrderId, orderId).eq(IoCost::getOrgId, SecurityUtils.getUser().getOrgId());
            List<IoCost> ioCostList = ioCostService.list(ioCostWrapper);
            costList = ioCostList.stream().map(ioCost -> {

                if (costMap.get(ioCost.getCostCurrency()) == null) {
                    costMap.put(ioCost.getCostCurrency(), ioCost.getCostAmount());
                } else {
                    costMap.put(ioCost.getCostCurrency(), costMap.get(ioCost.getCostCurrency()).add(ioCost.getCostAmount()));
                }
                if (costMap.get("function") == null) {
                    costMap.put("function", ioCost.getCostFunctionalAmount());
                } else {
                    costMap.put("function", costMap.get("function").add(ioCost.getCostFunctionalAmount()));
                }

                BusinessCalculationBill businessCalculationBill = new BusinessCalculationBill();
                businessCalculationBill.setCostAmount(ioCost.getCostAmount());
                businessCalculationBill.setCostAmountStr(FormatUtils.formatWithQWF(ioCost.getCostAmount(), 2));
                businessCalculationBill.setCostFunctionalAmount(ioCost.getCostFunctionalAmount());
                businessCalculationBill.setCostFunctionalAmountStr(FormatUtils.formatWithQWF(ioCost.getCostFunctionalAmount(), 2));
                businessCalculationBill.setCustomerName(ioCost.getCustomerName());
                businessCalculationBill.setServiceName(ioCost.getServiceName());
                businessCalculationBill.setServiceRemark(ioCost.getServiceRemark());
                businessCalculationBill.setBusinessScope(ioCost.getBusinessScope());
                return businessCalculationBill;
            }).collect(Collectors.toList());

            //整理应付合计
            StringBuffer costSum = new StringBuffer();
            for (Map.Entry<String, BigDecimal> entry : costMap.entrySet()) {
                if (!"function".equals(entry.getKey())) {
                    if (StrUtil.isBlank(costSum.toString())) {
                        costSum.append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    } else {
                        costSum.append(" ").append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    }
                }
            }
            BusinessCalculationBill costSumBusinessCalculationBill = new BusinessCalculationBill();
            costSumBusinessCalculationBill.setBusinessScope("应付合计");
            costSumBusinessCalculationBill.setCostAmountStr(costSum.toString());
            costSumBusinessCalculationBill.setCostFunctionalAmountStr(FormatUtils.formatWithQWF(costMap.get("function"), 2));
            costList.add(costSumBusinessCalculationBill);


            //整理应收明细数据
            LambdaQueryWrapper<IoIncome> ioIncomeWrapper = Wrappers.<IoIncome>lambdaQuery();
            ioIncomeWrapper.eq(IoIncome::getOrderId, orderId).eq(IoIncome::getOrgId, SecurityUtils.getUser().getOrgId());
            List<IoIncome> ioIncomeList = ioIncomeService.list(ioIncomeWrapper);

            incomeList = ioIncomeList.stream().map(ioIncome -> {

                if (incomeMap.get(ioIncome.getIncomeCurrency()) == null) {
                    incomeMap.put(ioIncome.getIncomeCurrency(), ioIncome.getIncomeAmount());
                } else {
                    incomeMap.put(ioIncome.getIncomeCurrency(), incomeMap.get(ioIncome.getIncomeCurrency()).add(ioIncome.getIncomeAmount()));
                }
                if (incomeMap.get("function") == null) {
                    incomeMap.put("function", ioIncome.getIncomeFunctionalAmount());
                } else {
                    incomeMap.put("function", incomeMap.get("function").add(ioIncome.getIncomeFunctionalAmount()));
                }

                BusinessCalculationBill businessCalculationBill = new BusinessCalculationBill();
                businessCalculationBill.setIncomeAmount(ioIncome.getIncomeAmount());
                businessCalculationBill.setIncomeAmountStr(FormatUtils.formatWithQWF(ioIncome.getIncomeAmount(), 2));
                businessCalculationBill.setIncomeFunctionalAmount(ioIncome.getIncomeFunctionalAmount());
                businessCalculationBill.setIncomeFunctionalAmountStr(FormatUtils.formatWithQWF(ioIncome.getIncomeFunctionalAmount(), 2));
                businessCalculationBill.setCustomerName(ioIncome.getCustomerName());
                businessCalculationBill.setServiceName(ioIncome.getServiceName());
                businessCalculationBill.setServiceRemark(ioIncome.getServiceRemark());
                businessCalculationBill.setBusinessScope(ioIncome.getBusinessScope());
                return businessCalculationBill;
            }).collect(Collectors.toList());

            //整理应收合计
            StringBuffer incomeSum = new StringBuffer();
            for (Map.Entry<String, BigDecimal> entry : incomeMap.entrySet()) {
                if (!"function".equals(entry.getKey())) {
                    if (StrUtil.isBlank(incomeSum.toString())) {
                        incomeSum.append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    } else {
                        incomeSum.append(" ").append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    }
                }
            }
            BusinessCalculationBill incomeSumBusinessCalculationBill = new BusinessCalculationBill();
            incomeSumBusinessCalculationBill.setBusinessScope("应收合计");
            incomeSumBusinessCalculationBill.setIncomeAmountStr(incomeSum.toString());
            incomeSumBusinessCalculationBill.setIncomeFunctionalAmountStr(FormatUtils.formatWithQWF(incomeMap.get("function"), 2));
            incomeList.add(incomeSumBusinessCalculationBill);

            //整理毛利核算数据
            businessCalculationBillList = baseMapper.selectBusinessCalculationForIO(orderId);
        } else if (businessScope.startsWith("T")) {
            //整理业务核销单上半部数据-数据来源于订单
            TcOrder tcOrder = tcOrderService.getById(orderId);
            name = "ORDER_ACCOUNT_SHEET_" + tcOrder.getOrderCode();

            tableHeader.setOrderCode(tcOrder.getOrderCode());
            if (tcOrder.getCoopId() != null) {
                CoopVo coopVo = remoteCoopService.viewCoop(tcOrder.getCoopId().toString()).getData();
                if (coopVo != null) {
                    tableHeader.setCustomerName(coopVo.getCoop_name());
                }
            }
            if (StrUtil.isNotBlank(tcOrder.getRwbNumber())) {
                tableHeader.setAwbNumber(tcOrder.getRwbNumber());
            } else {
                if (StrUtil.isNotBlank(tcOrder.getRwbNumber())) {
                    tableHeader.setAwbNumber(tcOrder.getRwbNumber());
                } else {
                    tableHeader.setAwbNumber("");
                }
            }
            if (tcOrder.getBookingAgentId() != null) {
                CoopVo bookingAgent = remoteCoopService.viewCoop(tcOrder.getBookingAgentId().toString()).getData();
                if (bookingAgent != null) {
                    tableHeader.setAwbFrom(bookingAgent.getCoop_name());
                }

            }
//            tableHeader.setSalesName(scOrder.getSalesName());
//            tableHeader.setServicerName(scOrder.getServicerName());
            tableHeader.setSalesName(tcOrder.getSalesName().split(" ")[0]);
            tableHeader.setServicerName(tcOrder.getServicerName().split(" ")[0]);
            tableHeader.setDepartureStation(tcOrder.getDepartureStation());
            tableHeader.setArrivalStation(tcOrder.getArrivalStation());
            tableHeader.setFlightNumber("");
            if (businessScope.equals("TE")) {
                tableHeader.setFlightDate(tcOrder.getExpectDeparture());
            } else {
                tableHeader.setFlightDate(tcOrder.getExpectArrival());
            }
            tableHeader.setPwvInfo(tcOrder.getContainerNumber() == null ? "" : tcOrder.getContainerNumber().toString());
            tableHeader.setChargeWeight(tcOrder.getPlanChargeWeight() == null ? "" : tcOrder.getPlanChargeWeight().toString());
            tableHeader.setCustomerNumber(tcOrder.getCustomerNumber());

            //整理应付明细数据
            LambdaQueryWrapper<TcCost> tcCostWrapper = Wrappers.<TcCost>lambdaQuery();
            tcCostWrapper.eq(TcCost::getOrderId, orderId).eq(TcCost::getOrgId, SecurityUtils.getUser().getOrgId());
            List<TcCost> tcCostList = tcCostService.selectList(tcCostWrapper);
            costList = tcCostList.stream().map(tcCost -> {

                if (costMap.get(tcCost.getCostCurrency()) == null) {
                    costMap.put(tcCost.getCostCurrency(), tcCost.getCostAmount());
                } else {
                    costMap.put(tcCost.getCostCurrency(), costMap.get(tcCost.getCostCurrency()).add(tcCost.getCostAmount()));
                }
                if (costMap.get("function") == null) {
                    costMap.put("function", tcCost.getCostFunctionalAmount());
                } else {
                    costMap.put("function", costMap.get("function").add(tcCost.getCostFunctionalAmount()));
                }

                BusinessCalculationBill businessCalculationBill = new BusinessCalculationBill();
                businessCalculationBill.setCostAmount(tcCost.getCostAmount());
                businessCalculationBill.setCostAmountStr(FormatUtils.formatWithQWF(tcCost.getCostAmount(), 2));
                businessCalculationBill.setCostFunctionalAmount(tcCost.getCostFunctionalAmount());
                businessCalculationBill.setCostFunctionalAmountStr(FormatUtils.formatWithQWF(tcCost.getCostFunctionalAmount(), 2));
                businessCalculationBill.setCustomerName(tcCost.getCustomerName());
                businessCalculationBill.setServiceName(tcCost.getServiceName());
                businessCalculationBill.setServiceRemark(tcCost.getServiceRemark());
                businessCalculationBill.setBusinessScope(tcCost.getBusinessScope());
                return businessCalculationBill;
            }).collect(Collectors.toList());

            //整理应付合计
            StringBuffer costSum = new StringBuffer();
            for (Map.Entry<String, BigDecimal> entry : costMap.entrySet()) {
                if (!"function".equals(entry.getKey())) {
                    if (StrUtil.isBlank(costSum.toString())) {
                        costSum.append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    } else {
                        costSum.append(" ").append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    }
                }
            }
            BusinessCalculationBill costSumBusinessCalculationBill = new BusinessCalculationBill();
            costSumBusinessCalculationBill.setBusinessScope("应付合计");
            costSumBusinessCalculationBill.setCostAmountStr(costSum.toString());
            costSumBusinessCalculationBill.setCostFunctionalAmountStr(FormatUtils.formatWithQWF(costMap.get("function"), 2));
            costList.add(costSumBusinessCalculationBill);


            //整理应收明细数据
            LambdaQueryWrapper<TcIncome> tcIncomeWrapper = Wrappers.<TcIncome>lambdaQuery();
            tcIncomeWrapper.eq(TcIncome::getOrderId, orderId).eq(TcIncome::getOrgId, SecurityUtils.getUser().getOrgId());
            List<TcIncome> tcIncomeList = tcIncomeService.selectList(tcIncomeWrapper);

            incomeList = tcIncomeList.stream().map(tcIncome -> {

                if (incomeMap.get(tcIncome.getIncomeCurrency()) == null) {
                    incomeMap.put(tcIncome.getIncomeCurrency(), tcIncome.getIncomeAmount());
                } else {
                    incomeMap.put(tcIncome.getIncomeCurrency(), incomeMap.get(tcIncome.getIncomeCurrency()).add(tcIncome.getIncomeAmount()));
                }
                if (incomeMap.get("function") == null) {
                    incomeMap.put("function", tcIncome.getIncomeFunctionalAmount());
                } else {
                    incomeMap.put("function", incomeMap.get("function").add(tcIncome.getIncomeFunctionalAmount()));
                }

                BusinessCalculationBill businessCalculationBill = new BusinessCalculationBill();
                businessCalculationBill.setIncomeAmount(tcIncome.getIncomeAmount());
                businessCalculationBill.setIncomeAmountStr(FormatUtils.formatWithQWF(tcIncome.getIncomeAmount(), 2));
                businessCalculationBill.setIncomeFunctionalAmount(tcIncome.getIncomeFunctionalAmount());
                businessCalculationBill.setIncomeFunctionalAmountStr(FormatUtils.formatWithQWF(tcIncome.getIncomeFunctionalAmount(), 2));
                businessCalculationBill.setCustomerName(tcIncome.getCustomerName());
                businessCalculationBill.setServiceName(tcIncome.getServiceName());
                businessCalculationBill.setServiceRemark(tcIncome.getServiceRemark());
                businessCalculationBill.setBusinessScope(tcIncome.getBusinessScope());
                return businessCalculationBill;
            }).collect(Collectors.toList());

            //整理应收合计
            StringBuffer incomeSum = new StringBuffer();
            for (Map.Entry<String, BigDecimal> entry : incomeMap.entrySet()) {
                if (!"function".equals(entry.getKey())) {
                    if (StrUtil.isBlank(incomeSum.toString())) {
                        incomeSum.append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    } else {
                        incomeSum.append(" ").append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    }
                }
            }
            BusinessCalculationBill incomeSumBusinessCalculationBill = new BusinessCalculationBill();
            incomeSumBusinessCalculationBill.setBusinessScope("应收合计");
            incomeSumBusinessCalculationBill.setIncomeAmountStr(incomeSum.toString());
            incomeSumBusinessCalculationBill.setIncomeFunctionalAmountStr(FormatUtils.formatWithQWF(incomeMap.get("function"), 2));
            incomeList.add(incomeSumBusinessCalculationBill);

            //整理毛利核算数据
            businessCalculationBillList = baseMapper.selectBusinessCalculationForTC(orderId);
        } else if (businessScope.startsWith("S")) {
            //整理业务核销单上半部数据-数据来源于订单
            ScOrder scOrder = scOrderService.getById(orderId);
            name = "ORDER_ACCOUNT_SHEET_" + scOrder.getOrderCode();

            tableHeader.setOrderCode(scOrder.getOrderCode());
            if (scOrder.getCoopId() != null) {
                CoopVo coopVo = remoteCoopService.viewCoop(scOrder.getCoopId().toString()).getData();
                if (coopVo != null) {
                    tableHeader.setCustomerName(coopVo.getCoop_name());
                }
            }
            if (StrUtil.isNotBlank(scOrder.getMblNumber()) && StrUtil.isNotBlank(scOrder.getHblNumber())) {
                tableHeader.setAwbNumber(scOrder.getMblNumber() + "_" + scOrder.getHblNumber());
            } else {
                if (StrUtil.isNotBlank(scOrder.getMblNumber())) {
                    tableHeader.setAwbNumber(scOrder.getMblNumber());
                } else if (StrUtil.isNotBlank(scOrder.getHblNumber())) {
                    tableHeader.setAwbNumber(scOrder.getHblNumber());
                } else {
                    tableHeader.setAwbNumber("");
                }
            }
            if (scOrder.getBookingAgentId() != null) {
                CoopVo bookingAgent = remoteCoopService.viewCoop(scOrder.getBookingAgentId().toString()).getData();
                if (bookingAgent != null) {
                    tableHeader.setAwbFrom(bookingAgent.getCoop_name());
                }

            }
//            tableHeader.setSalesName(scOrder.getSalesName());
//            tableHeader.setServicerName(scOrder.getServicerName());
            tableHeader.setSalesName(scOrder.getSalesName().split(" ")[0]);
            tableHeader.setServicerName(scOrder.getServicerName().split(" ")[0]);
            Map<String, String> scDeparture = baseMapper.queryScPortMaintenanceByCode(scOrder.getDepartureStation());
            Map<String, String> scArrival = baseMapper.queryScPortMaintenanceByCode(scOrder.getArrivalStation());
            if (scDeparture != null) {
                tableHeader.setDepartureStation(scDeparture.get("port_name_en").toUpperCase());
            }
            if (scArrival != null) {
                tableHeader.setArrivalStation(scArrival.get("port_name_en").toUpperCase());
            }
            tableHeader.setFlightNumber(scOrder.getShipVoyageNumber());
            if (businessScope.equals("SE")) {
                tableHeader.setFlightDate(scOrder.getExpectDeparture());
            } else {
                tableHeader.setFlightDate(scOrder.getExpectArrival());
            }
            tableHeader.setPwvInfo(scOrder.getContainerNumber() == null ? "" : scOrder.getContainerNumber().toString());
            tableHeader.setChargeWeight(scOrder.getPlanChargeWeight() == null ? "" : scOrder.getPlanChargeWeight().toString());
            tableHeader.setCustomerNumber(scOrder.getCustomerNumber());

            //整理应付明细数据
            LambdaQueryWrapper<ScCost> scCostWrapper = Wrappers.<ScCost>lambdaQuery();
            scCostWrapper.eq(ScCost::getOrderId, orderId).eq(ScCost::getOrgId, SecurityUtils.getUser().getOrgId());
            List<ScCost> scCostList = scCostService.list(scCostWrapper);
            costList = scCostList.stream().map(scCost -> {

                if (costMap.get(scCost.getCostCurrency()) == null) {
                    costMap.put(scCost.getCostCurrency(), scCost.getCostAmount());
                } else {
                    costMap.put(scCost.getCostCurrency(), costMap.get(scCost.getCostCurrency()).add(scCost.getCostAmount()));
                }
                if (costMap.get("function") == null) {
                    costMap.put("function", scCost.getCostFunctionalAmount());
                } else {
                    costMap.put("function", costMap.get("function").add(scCost.getCostFunctionalAmount()));
                }

                BusinessCalculationBill businessCalculationBill = new BusinessCalculationBill();
                businessCalculationBill.setCostAmount(scCost.getCostAmount());
                businessCalculationBill.setCostAmountStr(FormatUtils.formatWithQWF(scCost.getCostAmount(), 2));
                businessCalculationBill.setCostFunctionalAmount(scCost.getCostFunctionalAmount());
                businessCalculationBill.setCostFunctionalAmountStr(FormatUtils.formatWithQWF(scCost.getCostFunctionalAmount(), 2));
                businessCalculationBill.setCustomerName(scCost.getCustomerName());
                businessCalculationBill.setServiceName(scCost.getServiceName());
                businessCalculationBill.setServiceRemark(scCost.getServiceRemark());
                businessCalculationBill.setBusinessScope(scCost.getBusinessScope());
                return businessCalculationBill;
            }).collect(Collectors.toList());

            //整理应付合计
            StringBuffer costSum = new StringBuffer();
            for (Map.Entry<String, BigDecimal> entry : costMap.entrySet()) {
                if (!"function".equals(entry.getKey())) {
                    if (StrUtil.isBlank(costSum.toString())) {
                        costSum.append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    } else {
                        costSum.append(" ").append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    }
                }
            }
            BusinessCalculationBill costSumBusinessCalculationBill = new BusinessCalculationBill();
            costSumBusinessCalculationBill.setBusinessScope("应付合计");
            costSumBusinessCalculationBill.setCostAmountStr(costSum.toString());
            costSumBusinessCalculationBill.setCostFunctionalAmountStr(FormatUtils.formatWithQWF(costMap.get("function"), 2));
            costList.add(costSumBusinessCalculationBill);


            //整理应收明细数据
            LambdaQueryWrapper<ScIncome> scIncomeWrapper = Wrappers.<ScIncome>lambdaQuery();
            scIncomeWrapper.eq(ScIncome::getOrderId, orderId).eq(ScIncome::getOrgId, SecurityUtils.getUser().getOrgId());
            List<ScIncome> scIncomeList = scIncomeService.list(scIncomeWrapper);

            incomeList = scIncomeList.stream().map(scIncome -> {

                if (incomeMap.get(scIncome.getIncomeCurrency()) == null) {
                    incomeMap.put(scIncome.getIncomeCurrency(), scIncome.getIncomeAmount());
                } else {
                    incomeMap.put(scIncome.getIncomeCurrency(), incomeMap.get(scIncome.getIncomeCurrency()).add(scIncome.getIncomeAmount()));
                }
                if (incomeMap.get("function") == null) {
                    incomeMap.put("function", scIncome.getIncomeFunctionalAmount());
                } else {
                    incomeMap.put("function", incomeMap.get("function").add(scIncome.getIncomeFunctionalAmount()));
                }

                BusinessCalculationBill businessCalculationBill = new BusinessCalculationBill();
                businessCalculationBill.setIncomeAmount(scIncome.getIncomeAmount());
                businessCalculationBill.setIncomeAmountStr(FormatUtils.formatWithQWF(scIncome.getIncomeAmount(), 2));
                businessCalculationBill.setIncomeFunctionalAmount(scIncome.getIncomeFunctionalAmount());
                businessCalculationBill.setIncomeFunctionalAmountStr(FormatUtils.formatWithQWF(scIncome.getIncomeFunctionalAmount(), 2));
                businessCalculationBill.setCustomerName(scIncome.getCustomerName());
                businessCalculationBill.setServiceName(scIncome.getServiceName());
                businessCalculationBill.setServiceRemark(scIncome.getServiceRemark());
                businessCalculationBill.setBusinessScope(scIncome.getBusinessScope());
                return businessCalculationBill;
            }).collect(Collectors.toList());

            //整理应收合计
            StringBuffer incomeSum = new StringBuffer();
            for (Map.Entry<String, BigDecimal> entry : incomeMap.entrySet()) {
                if (!"function".equals(entry.getKey())) {
                    if (StrUtil.isBlank(incomeSum.toString())) {
                        incomeSum.append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    } else {
                        incomeSum.append(" ").append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    }
                }
            }
            BusinessCalculationBill incomeSumBusinessCalculationBill = new BusinessCalculationBill();
            incomeSumBusinessCalculationBill.setBusinessScope("应收合计");
            incomeSumBusinessCalculationBill.setIncomeAmountStr(incomeSum.toString());
            incomeSumBusinessCalculationBill.setIncomeFunctionalAmountStr(FormatUtils.formatWithQWF(incomeMap.get("function"), 2));
            incomeList.add(incomeSumBusinessCalculationBill);

            //整理毛利核算数据
            businessCalculationBillList = baseMapper.selectBusinessCalculationForSC(orderId);
        } else if (businessScope.startsWith("A")) {

            //整理业务核销单上半部数据-数据来源于订单
            AfOrder afOrder = getById(orderId);
            name = "ORDER_ACCOUNT_SHEET_" + afOrder.getOrderCode();

            tableHeader.setOrderCode(afOrder.getOrderCode());
            if (afOrder.getCoopId() != null) {
                CoopVo coopVo = remoteCoopService.viewCoop(afOrder.getCoopId().toString()).getData();
                if (coopVo != null) {
                    tableHeader.setCustomerName(coopVo.getCoop_name());
                }
            }
            if (businessScope.equals("AE")) {
                tableHeader.setAwbNumber(afOrder.getAwbNumber());
            } else {
                if (StrUtil.isNotBlank(afOrder.getAwbNumber()) && StrUtil.isNotBlank(afOrder.getHawbNumber())) {
                    tableHeader.setAwbNumber(afOrder.getAwbNumber() + "_" + afOrder.getHawbNumber());
                } else {
                    if (StrUtil.isNotBlank(afOrder.getAwbNumber())) {
                        tableHeader.setAwbNumber(afOrder.getAwbNumber());
                    } else if (StrUtil.isNotBlank(afOrder.getHawbNumber())) {
                        tableHeader.setAwbNumber(afOrder.getHawbNumber());
                    } else {
                        tableHeader.setAwbNumber("");
                    }
                }
            }
            if (afOrder.getAwbId() != null) {
                AwbNumber awbNumber = awbservice.getById(afOrder.getAwbId());
                if (awbNumber != null) {
                    tableHeader.setAwbFrom(awbNumber.getAwbFromName());
                }
            }
            tableHeader.setBusinessProduct(afOrder.getBusinessProduct());
            tableHeader.setSalesName(afOrder.getSalesName().split(" ")[0]);
            tableHeader.setServicerName(afOrder.getServicerName().split(" ")[0]);
//            tableHeader.setSalesName(afOrder.getSalesName());
//            tableHeader.setServicerName(afOrder.getServicerName());
            tableHeader.setDepartureStation(afOrder.getDepartureStation());
            tableHeader.setArrivalStation(afOrder.getArrivalStation());
            tableHeader.setFlightNumber(afOrder.getExpectFlight());
            tableHeader.setCustomerNumber(afOrder.getCustomerNumber());
            tableHeader.setPriceRemark(afOrder.getPriceRemark());
            if (businessScope.equals("AE")) {
                tableHeader.setFlightDate(afOrder.getExpectDeparture());
            } else {
                tableHeader.setFlightDate(afOrder.getExpectArrival());
            }
            String pwbInfo = (afOrder.getConfirmPieces() == null ? (afOrder.getPlanPieces() == null ? "" : afOrder.getPlanPieces()) : afOrder.getConfirmPieces()).toString() + afOrder.getPackageType() + "/" + (afOrder.getConfirmWeight() == null ? (afOrder.getPlanWeight() == null ? "" : afOrder.getPlanWeight()) : afOrder.getConfirmWeight()).toString() + "/" + (afOrder.getConfirmVolume() == null ? (afOrder.getPlanVolume() == null ? "" : afOrder.getPlanVolume()) : afOrder.getConfirmVolume()).toString();
            tableHeader.setPwvInfo(pwbInfo);
            tableHeader.setChargeWeight((afOrder.getConfirmChargeWeight() == null ? (afOrder.getPlanChargeWeight() == null ? "" : afOrder.getPlanChargeWeight()) : afOrder.getConfirmChargeWeight()).toString());

            //整理应付明细数据
            LambdaQueryWrapper<AfCost> afCostWrapper = Wrappers.<AfCost>lambdaQuery();
            afCostWrapper.eq(AfCost::getOrderId, orderId).eq(AfCost::getOrgId, SecurityUtils.getUser().getOrgId());
            List<AfCost> afCostList = afCostMapper.selectList(afCostWrapper);

            costList = afCostList.stream().map(afCost -> {
                if (costMap.get(afCost.getCostCurrency()) == null) {
                    costMap.put(afCost.getCostCurrency(), afCost.getCostAmount());
                } else {
                    costMap.put(afCost.getCostCurrency(), costMap.get(afCost.getCostCurrency()).add(afCost.getCostAmount()));
                }
                if (costMap.get("function") == null) {
                    costMap.put("function", afCost.getCostFunctionalAmount());
                } else {
                    costMap.put("function", costMap.get("function").add(afCost.getCostFunctionalAmount()));
                }

                BusinessCalculationBill businessCalculationBill = new BusinessCalculationBill();
                businessCalculationBill.setCostAmount(afCost.getCostAmount());
                businessCalculationBill.setCostAmountStr(FormatUtils.formatWithQWF(afCost.getCostAmount(), 2));
                businessCalculationBill.setCostFunctionalAmount(afCost.getCostFunctionalAmount());
                businessCalculationBill.setCostFunctionalAmountStr(FormatUtils.formatWithQWF(afCost.getCostFunctionalAmount(), 2));
                businessCalculationBill.setCustomerName(afCost.getCustomerName());
                businessCalculationBill.setServiceName(afCost.getServiceName());
                businessCalculationBill.setServiceRemark(afCost.getServiceRemark());
                businessCalculationBill.setBusinessScope(afCost.getBusinessScope());

                return businessCalculationBill;
            }).collect(Collectors.toList());

            //整理应付合计
            StringBuffer costSum = new StringBuffer();
            for (Map.Entry<String, BigDecimal> entry : costMap.entrySet()) {
                if (!entry.getKey().equals("function")) {
                    if (StrUtil.isBlank(costSum.toString())) {
                        costSum.append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    } else {
                        costSum.append(" ").append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    }
                }
            }
            BusinessCalculationBill costSumBusinessCalculationBill = new BusinessCalculationBill();
            costSumBusinessCalculationBill.setBusinessScope("应付合计");
            costSumBusinessCalculationBill.setCostAmountStr(costSum.toString());
            costSumBusinessCalculationBill.setCostFunctionalAmountStr(FormatUtils.formatWithQWF(costMap.get("function"), 2));
            costList.add(costSumBusinessCalculationBill);

            //整理应收明细数据
            LambdaQueryWrapper<AfIncome> afIncomeWrapper = Wrappers.<AfIncome>lambdaQuery();
            afIncomeWrapper.eq(AfIncome::getOrderId, orderId).eq(AfIncome::getOrgId, SecurityUtils.getUser().getOrgId());
            List<AfIncome> afIncomeList = afIncomeMapper.selectList(afIncomeWrapper);

            incomeList = afIncomeList.stream().map(afIncome -> {
                if (incomeMap.get(afIncome.getIncomeCurrency()) == null) {
                    incomeMap.put(afIncome.getIncomeCurrency(), afIncome.getIncomeAmount());
                } else {
                    incomeMap.put(afIncome.getIncomeCurrency(), incomeMap.get(afIncome.getIncomeCurrency()).add(afIncome.getIncomeAmount()));
                }
                if (incomeMap.get("function") == null) {
                    incomeMap.put("function", afIncome.getIncomeFunctionalAmount());
                } else {
                    incomeMap.put("function", incomeMap.get("function").add(afIncome.getIncomeFunctionalAmount()));
                }
                BusinessCalculationBill businessCalculationBill = new BusinessCalculationBill();
                businessCalculationBill.setIncomeAmount(afIncome.getIncomeAmount());
                businessCalculationBill.setIncomeAmountStr(FormatUtils.formatWithQWF(afIncome.getIncomeAmount(), 2));
                businessCalculationBill.setIncomeFunctionalAmount(afIncome.getIncomeFunctionalAmount());
                businessCalculationBill.setIncomeFunctionalAmountStr(FormatUtils.formatWithQWF(afIncome.getIncomeFunctionalAmount(), 2));
                businessCalculationBill.setCustomerName(afIncome.getCustomerName());
                businessCalculationBill.setServiceName(afIncome.getServiceName());
                businessCalculationBill.setServiceRemark(afIncome.getServiceRemark());
                businessCalculationBill.setBusinessScope(afIncome.getBusinessScope());
                return businessCalculationBill;
            }).collect(Collectors.toList());

            //整理应收合计
            StringBuffer incomeSum = new StringBuffer();
            for (Map.Entry<String, BigDecimal> entry : incomeMap.entrySet()) {
                if (!"function".equals(entry.getKey())) {
                    if (StrUtil.isBlank(incomeSum.toString())) {
                        incomeSum.append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    } else {
                        incomeSum.append(" ").append(entry.getKey()).append(" ").append(FormatUtils.formatWithQWF(entry.getValue(), 2));
                    }
                }
            }
            BusinessCalculationBill incomeSumBusinessCalculationBill = new BusinessCalculationBill();
            incomeSumBusinessCalculationBill.setBusinessScope("应收合计");
            incomeSumBusinessCalculationBill.setIncomeAmountStr(incomeSum.toString());
            incomeSumBusinessCalculationBill.setIncomeFunctionalAmountStr(FormatUtils.formatWithQWF(incomeMap.get("function"), 2));
            incomeList.add(incomeSumBusinessCalculationBill);

            //整理毛利核算数据
            businessCalculationBillList = baseMapper.selectBusinessCalculationForAF(orderId);
        }
        //整理显示数据并且合计
        businessCalculationBillList.stream().forEach(businessCalculationBill -> {
            businessCalculationBill.setIncomeFunctionalAmountStr(FormatUtils.formatWithQWF(businessCalculationBill.getIncomeFunctionalAmount(), 2));
            businessCalculationBill.setCostFunctionalAmountStr(FormatUtils.formatWithQWF(businessCalculationBill.getCostFunctionalAmount(), 2));
            businessCalculationBill.setProfitFunctionalAmountStr(FormatUtils.formatWithQWF(businessCalculationBill.getProfitFunctionalAmount(), 2));

            if (businessCalculationMap.get("income") == null) {
                businessCalculationMap.put("income", businessCalculationBill.getIncomeFunctionalAmount());
            } else {
                businessCalculationMap.put("income", businessCalculationMap.get("income").add(businessCalculationBill.getIncomeFunctionalAmount()));
            }
            if (businessCalculationMap.get("cost") == null) {
                businessCalculationMap.put("cost", businessCalculationBill.getCostFunctionalAmount());
            } else {
                businessCalculationMap.put("cost", businessCalculationMap.get("cost").add(businessCalculationBill.getCostFunctionalAmount()));
            }
            if (businessCalculationMap.get("profit") == null) {
                businessCalculationMap.put("profit", businessCalculationBill.getProfitFunctionalAmount());
            } else {
                businessCalculationMap.put("profit", businessCalculationMap.get("profit").add(businessCalculationBill.getProfitFunctionalAmount()));
            }
        });
        BusinessCalculationBill businessCalculationBill = new BusinessCalculationBill();
        businessCalculationBill.setBusinessScope("合计");
        businessCalculationBill.setIncomeFunctionalAmountStr(FormatUtils.formatWithQWF(businessCalculationMap.get("income"), 2));
        businessCalculationBill.setCostFunctionalAmountStr(FormatUtils.formatWithQWF(businessCalculationMap.get("cost"), 2));
        businessCalculationBill.setProfitFunctionalAmountStr(FormatUtils.formatWithQWF(businessCalculationMap.get("profit"), 2));
        businessCalculationBillList.add(businessCalculationBill);

        String savePath = PDFUtils.filePath + "/PDFtemplate/temp/businessCalculationBill";
        // 得到文件保存的名称
        String saveFilename = name + "_" + UUID.randomUUID().toString() + ".pdf";
        // 得到文件的保存目录
        String newPDFPath = PDFUtils.makePath(saveFilename, savePath) + "/" + saveFilename;

        // pdf写入数据与下载
        OrgVo orgVo = remoteServiceToHRS.getByOrgId(SecurityUtils.getUser().getOrgId()).getData();
        if (orgVo != null) {
            tableHeader.setOrgName(orgVo.getOrgName());
        }
        createBusinessCalculationBillPDF(tableHeader, businessCalculationBillList, incomeList, costList, newPDFPath, businessScope);
        // 打印预览
        if (ifReplace) {
            return newPDFPath.replace(PDFUtils.filePath, "");
        } else {
            return newPDFPath;
        }
    }

    @Override
    public OrderDeliveryNotice getOrderDeliveryNotice(String orderUuid, String flag) {
        Assert.hasLength(orderUuid, "参数错误");
        Assert.hasLength(flag, "参数错误");
        checkOrderDeliveryNotice(orderUuid, flag);
        return this.baseMapper.getOrderDeliveryNotice(orderUuid, flag);
    }

    @Override
    public OrderDeliveryNoticeCheck checkOrderDeliveryNotice(String orderUUID, String flag) {
        Assert.hasLength(orderUUID, "参数错误");
        AfOrder dbOrder = getOrderByUUID(orderUUID);
        Objects.requireNonNull(dbOrder, "未查询到相关数据");
        Warehouse warehouse = null;
        if ("warehouse".equals(flag)) {
            if (dbOrder.getDepartureWarehouseId() == null) {
                throw new RuntimeException("该订单未选择交货货站");
            }
            warehouse = warehouseService.getById(dbOrder.getDepartureWarehouseId());
            if (warehouse == null) {
                throw new RuntimeException("该订单的交货货站已不存在");
            }
            if (StrUtil.isBlank(warehouse.getWarehouseAddressGps()) || StrUtil.isBlank(warehouse.getWarehouseLatitude()) || StrUtil.isBlank(warehouse.getWarehouseLongitude())) {
                throw new RuntimeException("当前订单的送货货站为" + warehouse.getWarehouseNameCn() + "，此货站没有保存过定位信息，请到“基础数据维护 / 空运信息 / 货站仓库”下进行维护。");
            }
        } else if ("storehouse".equals(flag)) {
            if (dbOrder.getDepartureStorehouseId() == null) {
                throw new RuntimeException("该订单未选择普货库房");
            }
            warehouse = warehouseService.getById(dbOrder.getDepartureStorehouseId());
            if (warehouse == null) {
                throw new RuntimeException("该订单的普货库房已不存在");
            }
            if (StrUtil.isBlank(warehouse.getWarehouseAddressGps()) || StrUtil.isBlank(warehouse.getWarehouseLatitude()) || StrUtil.isBlank(warehouse.getWarehouseLongitude())) {
                throw new RuntimeException("当前订单的送货库房为" + warehouse.getWarehouseNameCn() + "，此库房没有保存过定位信息，请到“基础数据维护 / 空运信息 / 货站仓库”下进行维护。");
            }
        } else {
            throw new RuntimeException("缺失必要传参");
        }
        return new OrderDeliveryNoticeCheck(true, warehouse.getWarehouseNameCn());
    }

    @Override
    public IPage<VPrmCoop> getCoopList(Page page, VPrmCoop bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        return baseMapper.getCoopList(page, bean);
    }

    @Override
    public IPage<VPrmCoop> getCoopListNew(Page page, VPrmCoop bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        return baseMapper.getCoopListNew(page, bean);
    }

    private void createBusinessCalculationBillPDF(BusinessCalculationBill tableHeader, List<BusinessCalculationBill> businessCalculationBillList, List<BusinessCalculationBill> incomeList, List<BusinessCalculationBill> costList, String newPDFPath, String businessScope) {
        File file = new File(newPDFPath);
        FileOutputStream out = null;
        Document document = new Document(PageSize.A4);
        try {
            //实例化文档对象
            out = new FileOutputStream(file);
            //创建写入器
            PdfWriter writer = PdfWriter.getInstance(document, out);
            /* 添加页码 */
            writer.setPageEvent(new PdfPageEventHelper());
            // 打开文档准备写入内容
            document.open();

            // 加载报表
            loadBusinessCalculationBillTable(document, tableHeader, businessCalculationBillList, incomeList, costList, businessScope);
            // 关闭文档
            document.close();
            log.info("PDF文件生成成功，PDF文件名：" + file.getAbsolutePath());
        } catch (DocumentException e) {
            log.error("PDF文件" + file.getAbsolutePath() + "生成失败！--DocumentException:" + e);
            throw new RuntimeException("PDF文件" + file.getAbsolutePath() + "生成失败！--DocumentException:" + e);
        } catch (IOException e) {
            log.error("PDF文件" + file.getAbsolutePath() + "生成失败！--IOException:" + e);
            throw new RuntimeException("PDF文件" + file.getAbsolutePath() + "生成失败！--IOException:" + e);
        } finally {
            if (out != null) {
                try {
                    // 关闭输出文件流
                    out.close();
                } catch (IOException e) {
                    log.error("关闭输出文件流失败！--IOException:" + e);
                    throw new RuntimeException("关闭输出文件流失败！--IOException:" + e);
                }
            }
        }
    }

    @SneakyThrows
    private void loadBusinessCalculationBillTable(Document document, BusinessCalculationBill tableHeader, List<BusinessCalculationBill> businessCalculationBillList, List<BusinessCalculationBill> incomeList, List<BusinessCalculationBill> costList, String businessScope) {
        BaseFont bfChinese = BaseFont.createFont(PDFUtils.simhei, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        // 正文的字体
        Font contentFont = new Font(bfChinese, 8, Font.NORMAL);
        Font contentRedFont = new Font(bfChinese, 8, Font.NORMAL);
        contentRedFont.setColor(BaseColor.RED);

        // 大标题的字体
        Font bigTitleFont = new Font(bfChinese, 18, Font.BOLD);

        // 表格标题的字体
        Font titleFont = new Font(bfChinese, 10, Font.BOLD);
        // 头字体
        Font headerFont = new Font(bfChinese, 8, Font.BOLD);


        //设置标题
        PdfPTable tableTitle = new PdfPTable(1);
        tableTitle.setSpacingBefore(10f);
        tableTitle.setWidths(new float[]{1f});
        tableTitle.getDefaultCell().setBorderWidth(0f);
        tableTitle.setWidthPercentage(104);
        tableTitle.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        tableTitle.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell cellTitle;
        cellTitle = new PdfPCell(new Phrase(tableHeader.getOrgName(), bigTitleFont));
        cellTitle.setBorderWidth(0f);
        cellTitle.setFixedHeight(30f);
        cellTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellTitle.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableTitle.addCell(cellTitle);
        cellTitle = new PdfPCell(new Phrase("业务核算单", bigTitleFont));
        cellTitle.setFixedHeight(30f);
        cellTitle.setBorderWidth(0f);
        cellTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellTitle.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableTitle.addCell(cellTitle);

        document.add(tableTitle);
        // 读logo图片
        OrgVo orgVo = remoteServiceToHRS.getByOrgId(SecurityUtils.getUser().getOrgId()).getData();
        if (orgVo != null && StrUtil.isNotBlank(orgVo.getOrgLogo())) {
            String imageUrl = filePath + "/PDFtemplate/temp/img/businessCalculationBill/" + orgVo.getOrgCode() + "/" + orgVo.getOrgLogo().substring(orgVo.getOrgLogo().lastIndexOf("/") + 1);
            PDFUtils.downloadFile(orgVo.getOrgLogo(), imageUrl);
            Image image = Image.getInstance(imageUrl);
            // 根据域的大小缩放图片
            image.scaleToFit(80f, 40f);
            image.setAbsolutePosition(25f, 780f);
            // 添加图片
            document.add(image);
        }

        // 设置文档内容
        //上半部
        int colNums = 6;
        PdfPTable table = new PdfPTable(colNums);
        table.setSpacingBefore(10f);

        float[] widths = {0.1f, 0.20f, 0.1f, 0.22f, 0.1f, 0.28f};
        table.setWidths(widths);
        table.setWidthPercentage(104);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

        table.getDefaultCell().setBorderColor(new BaseColor(0, 0, 0)); // 边框颜色
        table.getDefaultCell().setPadding(2); // space between content and
        table.getDefaultCell().setSpaceCharRatio(2);
        table.getDefaultCell().setBorderWidth(0f);
        table.getDefaultCell().setMinimumHeight(20f);

        //业务单号
        PdfPCell cell;
        cell = new PdfPCell(new Phrase("业务号", headerFont));
        cell.setFixedHeight(20f);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setBorderWidth(0f);
        cell.setPaddingRight(5f);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(tableHeader.getOrderCode(), contentFont));
        cell.setFixedHeight(20f);
        cell.setBorderWidth(0f);
        cell.setBorderWidthBottom(0.5f);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        table.addCell(cell);

        //订单客户
        cell = new PdfPCell(new Phrase("订单客户", headerFont));
        cell.setFixedHeight(20f);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setPaddingRight(5f);
        cell.setBorderWidth(0f);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(tableHeader.getCustomerName(), contentFont));
        cell.setFixedHeight(20f);
        cell.setColspan(3);
        cell.setBorderWidth(0f);
        cell.setBorderWidthBottom(0.5f);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        table.addCell(cell);

        if (businessScope.equals("AE")) {
            //运单号
            cell = new PdfPCell(new Phrase("运单号", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getAwbNumber(), contentFont));
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setBorderWidthBottom(0.5f);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            table.addCell(cell);
            //运单来源
            cell = new PdfPCell(new Phrase("运单来源", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getAwbFrom(), contentFont));
            cell.setFixedHeight(20f);
            cell.setColspan(3);
            cell.setBorderWidthBottom(0.5f);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setBorderWidth(0f);
            table.addCell(cell);
        } else if (businessScope.equals("AI")) {
            //运单号
            cell = new PdfPCell(new Phrase("运单号", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getAwbNumber(), contentFont));
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setBorderWidthBottom(0.5f);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setColspan(5);
            table.addCell(cell);
        } else if (businessScope.equals("SE")) {
            //运单号
            cell = new PdfPCell(new Phrase("运单号", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getAwbNumber(), contentFont));
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setBorderWidthBottom(0.5f);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            table.addCell(cell);
            //运单来源
            cell = new PdfPCell(new Phrase("订舱代理", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getAwbFrom(), contentFont));
            cell.setFixedHeight(20f);
            cell.setColspan(3);
            cell.setBorderWidth(0f);
            cell.setBorderWidthBottom(0.5f);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            table.addCell(cell);
        } else if (businessScope.equals("SI")) {
            //运单号
            cell = new PdfPCell(new Phrase("运单号", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getAwbNumber(), contentFont));
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setBorderWidthBottom(0.5f);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setColspan(5);
            table.addCell(cell);
        } else if (businessScope.equals("TE")) {
            //运单号
            cell = new PdfPCell(new Phrase("运单号", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getAwbNumber(), contentFont));
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setBorderWidthBottom(0.5f);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            table.addCell(cell);
            //运单来源
            cell = new PdfPCell(new Phrase("订舱代理", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getAwbFrom(), contentFont));
            cell.setFixedHeight(20f);
            cell.setColspan(3);
            cell.setBorderWidth(0f);
            cell.setBorderWidthBottom(0.5f);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            table.addCell(cell);
        }
        //服务产品
/*        if (businessScope.equals("AE")) {
            //服务产品
            cell = new PdfPCell(new Phrase("服务产品", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getBusinessProduct(), contentFont));
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setBorderWidthBottom(0.5f);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            table.addCell(cell);
        } else {
            cell = new PdfPCell(new Phrase("", contentFont));
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setColspan(2);
            table.addCell(cell);
        }*/
        //客户单号
        cell = new PdfPCell(new Phrase("客户单号", headerFont));
        cell.setFixedHeight(20f);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setPaddingRight(5f);
        cell.setBorderWidth(0f);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(tableHeader.getCustomerNumber(), contentFont));
        cell.setFixedHeight(20f);
        cell.setBorderWidth(0f);
        cell.setBorderWidthBottom(0.5f);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        table.addCell(cell);

        //责任销售
        cell = new PdfPCell(new Phrase("责任销售", headerFont));
        cell.setFixedHeight(20f);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setPaddingRight(5f);
        cell.setBorderWidth(0f);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(tableHeader.getSalesName(), contentFont));
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setFixedHeight(20f);
        cell.setBorderWidthBottom(0.5f);
        cell.setBorderWidth(0f);
        table.addCell(cell);

        //责任客服
        cell = new PdfPCell(new Phrase("责任客服", headerFont));
        cell.setFixedHeight(20f);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setPaddingRight(5f);
        cell.setBorderWidth(0f);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(tableHeader.getServicerName(), contentFont));
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setFixedHeight(20f);
        cell.setBorderWidthBottom(0.5f);
        cell.setBorderWidth(0f);
        table.addCell(cell);

        //始发港
        cell = new PdfPCell(new Phrase("始发港", headerFont));
        cell.setFixedHeight(20f);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setPaddingRight(5f);
        cell.setBorderWidth(0f);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(tableHeader.getDepartureStation(), contentFont));
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setFixedHeight(20f);
        cell.setBorderWidthBottom(0.5f);
        cell.setBorderWidth(0f);
        table.addCell(cell);

        //航班号
        if (businessScope.startsWith("A")) {
            cell = new PdfPCell(new Phrase("航班号", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getFlightNumber(), contentFont));
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setFixedHeight(20f);
            cell.setBorderWidthBottom(0.5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
        } else if (businessScope.startsWith("S")) {
            cell = new PdfPCell(new Phrase("航次", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getFlightNumber(), contentFont));
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setFixedHeight(20f);
            cell.setBorderWidthBottom(0.5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
        }


        //件重体
        if (businessScope.startsWith("A")) {
            cell = new PdfPCell(new Phrase("件重体", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getPwvInfo(), contentFont));
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setFixedHeight(20f);
            cell.setBorderWidthBottom(0.5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
        } else if (businessScope.startsWith("S")) {
            cell = new PdfPCell(new Phrase("标箱数量", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getPwvInfo(), contentFont));
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setFixedHeight(20f);
            cell.setBorderWidthBottom(0.5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
        } else if (businessScope.startsWith("T")) {
            cell = new PdfPCell(new Phrase("标箱数量", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getPwvInfo(), contentFont));
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setFixedHeight(20f);
            cell.setBorderWidthBottom(0.5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
        }

        //目的港
        cell = new PdfPCell(new Phrase("目的港", headerFont));
        cell.setFixedHeight(20f);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setPaddingRight(5f);
        cell.setBorderWidth(0f);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(tableHeader.getArrivalStation(), contentFont));
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setFixedHeight(20f);
        cell.setBorderWidthBottom(0.5f);
        cell.setBorderWidth(0f);
        table.addCell(cell);

        //航班日期
        if (businessScope.endsWith("E")) {
            cell = new PdfPCell(new Phrase("开航日期", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getFlightDate() == null ? "" : tableHeader.getFlightDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), contentFont));
            cell.setFixedHeight(20f);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setBorderWidthBottom(0.5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
        } else if (businessScope.endsWith("I")) {
            cell = new PdfPCell(new Phrase("离港日期", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getFlightDate() == null ? "" : tableHeader.getFlightDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), contentFont));
            cell.setFixedHeight(20f);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setBorderWidthBottom(0.5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
        }

        //计费重量
        if (businessScope.startsWith("A")) {
            cell = new PdfPCell(new Phrase("计费重量", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getChargeWeight(), contentFont));
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setFixedHeight(20f);
            cell.setBorderWidthBottom(0.5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
        } else if (businessScope.startsWith("S")) {
            cell = new PdfPCell(new Phrase("计费吨", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getChargeWeight(), contentFont));
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setFixedHeight(20f);
            cell.setBorderWidthBottom(0.5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
        } else if (businessScope.startsWith("T")) {
            cell = new PdfPCell(new Phrase("计费吨", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getChargeWeight(), contentFont));
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setFixedHeight(20f);
            cell.setBorderWidthBottom(0.5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
        }

        //服务产品
        if (businessScope.equals("AE")) {
            //服务产品
            cell = new PdfPCell(new Phrase("服务产品", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getBusinessProduct(), contentFont));
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setBorderWidthBottom(0.5f);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            table.addCell(cell);

            //价格备注
            cell = new PdfPCell(new Phrase("价格备注", headerFont));
            cell.setFixedHeight(20f);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setPaddingRight(5f);
            cell.setBorderWidth(0f);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(tableHeader.getPriceRemark(), contentFont));
            cell.setFixedHeight(20f);
            cell.setColspan(3);
            cell.setBorderWidth(0f);
            cell.setBorderWidthBottom(0.5f);
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            table.addCell(cell);

/*            cell = new PdfPCell(new Phrase("", contentFont));
            cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell.setFixedHeight(20f);
            cell.setBorderWidth(0f);
            cell.setColspan(2);
            table.addCell(cell);*/
        }

        document.add(table);

        //设置文本内容下半部分
        //毛利核算
        PdfPTable businessCalculationTableBorder = new PdfPTable(1);
        businessCalculationTableBorder.setWidthPercentage(104);
        businessCalculationTableBorder.setSpacingBefore(10f);
        businessCalculationTableBorder.setSplitLate(false);

        PdfPTable businessCalculationTable = new PdfPTable(5);
        businessCalculationTable.setWidths(new float[]{0.1f, 0.3f, 0.2f, 0.2f, 0.2f});
        businessCalculationTable.getDefaultCell().setBorderWidth(0f);
        businessCalculationTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        businessCalculationTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        PdfPCell businessCalculationCell;

        businessCalculationCell = new PdfPCell(new Phrase("毛利核算", titleFont));
        businessCalculationCell.setColspan(5);
        businessCalculationCell.setFixedHeight(30f);
        businessCalculationCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        businessCalculationCell.setBorderWidth(0f);

        businessCalculationTable.addCell(businessCalculationCell);

        businessCalculationCell = new PdfPCell(new Phrase("序号", headerFont));
        businessCalculationCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        businessCalculationCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        businessCalculationCell.setBorderWidth(0f);
        businessCalculationTable.addCell(businessCalculationCell);

        businessCalculationCell = new PdfPCell(new Phrase("服务项目", headerFont));
        businessCalculationCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        businessCalculationCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        businessCalculationCell.setBorderWidth(0f);
        businessCalculationTable.addCell(businessCalculationCell);

        businessCalculationCell = new PdfPCell(new Phrase("应收本币金额", headerFont));
        businessCalculationCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        businessCalculationCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        businessCalculationCell.setBorderWidth(0f);
        businessCalculationTable.addCell(businessCalculationCell);

        businessCalculationCell = new PdfPCell(new Phrase("应付本币金额", headerFont));
        businessCalculationCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        businessCalculationCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        businessCalculationCell.setBorderWidth(0f);
        businessCalculationTable.addCell(businessCalculationCell);

        businessCalculationCell = new PdfPCell(new Phrase("毛利金额", headerFont));
        businessCalculationCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        businessCalculationCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        businessCalculationCell.setBorderWidth(0f);
        businessCalculationTable.addCell(businessCalculationCell);

        Paragraph paragraph = new Paragraph();
        paragraph.add(new Chunk(new DottedLineSeparator()));

        businessCalculationCell = new PdfPCell(paragraph);
        businessCalculationCell.setColspan(5);
        businessCalculationCell.setBorderWidth(0f);
        businessCalculationCell.setVerticalAlignment(Element.ALIGN_TOP);
        businessCalculationTable.addCell(businessCalculationCell);

        for (int i = 0; i < businessCalculationBillList.size(); i++) {
            BusinessCalculationBill businessCalculationBill = businessCalculationBillList.get(i);
            if ("合计".equals(businessCalculationBill.getBusinessScope())) {
                businessCalculationCell = new PdfPCell(new Phrase("", contentFont));
                businessCalculationCell.setFixedHeight(20f);
                businessCalculationCell.setColspan(5);
                businessCalculationCell.setBorderWidth(0F);
                businessCalculationTable.addCell(businessCalculationCell);
                PdfPCell pdfPCell = new PdfPCell(new Phrase(businessCalculationBill.getBusinessScope(), contentFont));
                pdfPCell.setColspan(2);
                pdfPCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                pdfPCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                pdfPCell.setBorderWidth(0f);
                businessCalculationTable.addCell(pdfPCell);
            } else {
                businessCalculationCell = new PdfPCell(new Phrase((i + 1) + "", contentFont));
                businessCalculationCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                businessCalculationCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                businessCalculationCell.setBorderWidth(0f);
                businessCalculationTable.addCell(businessCalculationCell);
                businessCalculationTable.addCell(new Phrase(businessCalculationBill.getServiceName(), contentFont));
            }
            if ("0.00".equals(businessCalculationBill.getIncomeFunctionalAmountStr()) || "0.00".equals(businessCalculationBill.getCostFunctionalAmountStr())) {
                businessCalculationCell = new PdfPCell(new Phrase(businessCalculationBill.getIncomeFunctionalAmountStr(), contentRedFont));
            } else {
                businessCalculationCell = new PdfPCell(new Phrase(businessCalculationBill.getIncomeFunctionalAmountStr(), contentFont));
            }
            businessCalculationCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            businessCalculationCell.setBorderWidth(0f);
            businessCalculationTable.addCell(businessCalculationCell);

            if ("0.00".equals(businessCalculationBill.getIncomeFunctionalAmountStr()) || "0.00".equals(businessCalculationBill.getCostFunctionalAmountStr())) {
                businessCalculationCell = new PdfPCell(new Phrase(businessCalculationBill.getCostFunctionalAmountStr(), contentRedFont));
            } else {
                businessCalculationCell = new PdfPCell(new Phrase(businessCalculationBill.getCostFunctionalAmountStr(), contentFont));
            }
            businessCalculationCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            businessCalculationCell.setBorderWidth(0f);
            businessCalculationTable.addCell(businessCalculationCell);
            if (StrUtil.isNotBlank(businessCalculationBill.getProfitFunctionalAmountStr()) && businessCalculationBill.getProfitFunctionalAmountStr().startsWith("-")) {
                businessCalculationCell = new PdfPCell(new Phrase(businessCalculationBill.getProfitFunctionalAmountStr(), contentRedFont));
            } else {
                businessCalculationCell = new PdfPCell(new Phrase(businessCalculationBill.getProfitFunctionalAmountStr(), contentFont));
            }
            businessCalculationCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            businessCalculationCell.setBorderWidth(0f);
            businessCalculationTable.addCell(businessCalculationCell);
        }

        //表格底部留白
        businessCalculationCell = new PdfPCell(new Phrase("", contentFont));
        businessCalculationCell.setColspan(6);
        businessCalculationCell.setFixedHeight(20f);
        businessCalculationCell.setBorderWidth(0f);
        businessCalculationTable.addCell(businessCalculationCell);

        PdfPCell businessCalculationCellBorder = new PdfPCell(businessCalculationTable);
        businessCalculationCellBorder.setBorderWidth(1f);
        businessCalculationCellBorder.setPaddingRight(5f);
        businessCalculationCellBorder.setPaddingLeft(5f);
        businessCalculationTableBorder.addCell(businessCalculationCellBorder);
        document.add(businessCalculationTableBorder);

        //收付明细
        PdfPTable incomeAndCostTableBorder = new PdfPTable(1);
        incomeAndCostTableBorder.setWidthPercentage(104);
        incomeAndCostTableBorder.setSpacingBefore(10f);
        incomeAndCostTableBorder.setSplitLate(false);
        PdfPTable incomeAndCostTable = new PdfPTable(6);
        incomeAndCostTable.setWidths(new float[]{0.1f, 0.18f, 0.18f, 0.18f, 0.18f, 0.18f});
        incomeAndCostTable.getDefaultCell().setBorderWidth(0f);
        incomeAndCostTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        incomeAndCostTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        PdfPCell incomeAndCostCell;

        incomeAndCostCell = new PdfPCell(new Phrase("收付明细", titleFont));
        incomeAndCostCell.setColspan(6);
        incomeAndCostCell.setFixedHeight(30f);
        incomeAndCostCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        incomeAndCostCell.setBorderWidth(0f);
        incomeAndCostTable.addCell(incomeAndCostCell);

        //应收服务
        incomeAndCostCell = new PdfPCell(new Phrase("序号", headerFont));
        incomeAndCostCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        incomeAndCostCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        incomeAndCostCell.setBorderWidth(0f);
        incomeAndCostTable.addCell(incomeAndCostCell);

        incomeAndCostCell = new PdfPCell(new Phrase("应收服务", headerFont));
        incomeAndCostCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        incomeAndCostCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        incomeAndCostCell.setBorderWidth(0f);
        incomeAndCostTable.addCell(incomeAndCostCell);

        incomeAndCostCell = new PdfPCell(new Phrase("收款客户", headerFont));
        incomeAndCostCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        incomeAndCostCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        incomeAndCostCell.setBorderWidth(0f);
        incomeAndCostTable.addCell(incomeAndCostCell);

        incomeAndCostCell = new PdfPCell(new Phrase("应收标准", headerFont));
        incomeAndCostCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        incomeAndCostCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        incomeAndCostCell.setBorderWidth(0f);
        incomeAndCostTable.addCell(incomeAndCostCell);

        incomeAndCostCell = new PdfPCell(new Phrase("应收金额", headerFont));
        incomeAndCostCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        incomeAndCostCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        incomeAndCostCell.setBorderWidth(0f);
        incomeAndCostTable.addCell(incomeAndCostCell);

        incomeAndCostCell = new PdfPCell(new Phrase("应收本币金额", headerFont));
        incomeAndCostCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        incomeAndCostCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        incomeAndCostCell.setBorderWidth(0f);
        incomeAndCostTable.addCell(incomeAndCostCell);

        Paragraph incomeParagraph = new Paragraph();
        incomeParagraph.add(new Chunk(new DottedLineSeparator()));
        incomeAndCostCell = new PdfPCell(incomeParagraph);
        incomeAndCostCell.setBorderWidth(0f);
        incomeAndCostCell.setColspan(6);
        incomeAndCostCell.setVerticalAlignment(Element.ALIGN_TOP);

        incomeAndCostTable.addCell(incomeAndCostCell);

        for (int i = 0; i < incomeList.size(); i++) {
            BusinessCalculationBill income = incomeList.get(i);
            if (!income.getBusinessScope().equals("应收合计")) {
                incomeAndCostCell = new PdfPCell(new Phrase((i + 1) + "", contentFont));
                incomeAndCostCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                incomeAndCostCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                incomeAndCostCell.setBorderWidth(0f);
                incomeAndCostTable.addCell(incomeAndCostCell);

                incomeAndCostTable.addCell(new Phrase(income.getServiceName(), contentFont));
                incomeAndCostTable.addCell(new Phrase(income.getCustomerName(), contentFont));
                incomeAndCostTable.addCell(new Phrase(income.getServiceRemark(), contentFont));
                incomeAndCostCell = new PdfPCell(new Phrase(income.getIncomeAmountStr(), contentFont));
                incomeAndCostCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                incomeAndCostCell.setBorderWidth(0f);
                incomeAndCostTable.addCell(incomeAndCostCell);
            } else {
                incomeAndCostCell = new PdfPCell(new Phrase("", contentFont));
                incomeAndCostCell.setFixedHeight(20f);
                incomeAndCostCell.setColspan(6);
                incomeAndCostCell.setBorderWidth(0F);
                incomeAndCostTable.addCell(incomeAndCostCell);
                PdfPCell pdfPCell1 = new PdfPCell(new Phrase(income.getBusinessScope(), contentFont));
                pdfPCell1.setColspan(1);
                PdfPCell pdfPCell2 = new PdfPCell(new Phrase(income.getIncomeAmountStr(), contentFont));
                pdfPCell2.setColspan(4);
                pdfPCell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                pdfPCell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
                pdfPCell1.setHorizontalAlignment(Element.ALIGN_LEFT);
                pdfPCell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
                pdfPCell1.setBorderWidth(0f);
                pdfPCell2.setBorderWidth(0f);
                incomeAndCostTable.addCell(pdfPCell1);
                incomeAndCostTable.addCell(pdfPCell2);
            }
            incomeAndCostCell = new PdfPCell(new Phrase(income.getIncomeFunctionalAmountStr(), contentFont));
            incomeAndCostCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            incomeAndCostCell.setBorderWidth(0f);
            incomeAndCostTable.addCell(incomeAndCostCell);
        }

        //应收与应付分水岭
        incomeAndCostCell = new PdfPCell(new Phrase("", contentFont));
        incomeAndCostCell.setColspan(6);
        incomeAndCostCell.setFixedHeight(30f);
        incomeAndCostCell.setBorderWidth(0f);
        incomeAndCostTable.addCell(incomeAndCostCell);

        //应付服务
        incomeAndCostCell = new PdfPCell(new Phrase("序号", headerFont));
        incomeAndCostCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        incomeAndCostCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        incomeAndCostCell.setBorderWidth(0f);
        incomeAndCostTable.addCell(incomeAndCostCell);
        incomeAndCostCell = new PdfPCell(new Phrase("应付服务", headerFont));
        incomeAndCostCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        incomeAndCostCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        incomeAndCostCell.setBorderWidth(0f);
        incomeAndCostTable.addCell(incomeAndCostCell);
        incomeAndCostCell = new PdfPCell(new Phrase("付款客户", headerFont));
        incomeAndCostCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        incomeAndCostCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        incomeAndCostCell.setBorderWidth(0f);
        incomeAndCostTable.addCell(incomeAndCostCell);
        incomeAndCostCell = new PdfPCell(new Phrase("应付标准", headerFont));
        incomeAndCostCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        incomeAndCostCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        incomeAndCostCell.setBorderWidth(0f);
        incomeAndCostTable.addCell(incomeAndCostCell);

        incomeAndCostCell = new PdfPCell(new Phrase("应付金额", headerFont));
        incomeAndCostCell.setFixedHeight(20f);
        incomeAndCostCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        incomeAndCostCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        incomeAndCostCell.setBorderWidth(0f);
        incomeAndCostTable.addCell(incomeAndCostCell);

        incomeAndCostCell = new PdfPCell(new Phrase("应付本币金额", headerFont));
        incomeAndCostCell.setFixedHeight(20f);
        incomeAndCostCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        incomeAndCostCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        incomeAndCostCell.setBorderWidth(0f);
        incomeAndCostTable.addCell(incomeAndCostCell);

        Paragraph costParagraph = new Paragraph();
        costParagraph.add(new Chunk(new DottedLineSeparator()));

        incomeAndCostCell = new PdfPCell(costParagraph);
        incomeAndCostCell.setColspan(6);
        incomeAndCostCell.setBorderWidth(0f);
        incomeAndCostCell.setVerticalAlignment(Element.ALIGN_TOP);
        incomeAndCostTable.addCell(incomeAndCostCell);

        for (int i = 0; i < costList.size(); i++) {
            BusinessCalculationBill cost = costList.get(i);
            if (!cost.getBusinessScope().equals("应付合计")) {
                incomeAndCostCell = new PdfPCell(new Phrase((i + 1) + "", contentFont));
                incomeAndCostCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                incomeAndCostCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                incomeAndCostCell.setBorderWidth(0f);
                incomeAndCostTable.addCell(incomeAndCostCell);
                incomeAndCostTable.addCell(new Phrase(cost.getServiceName(), contentFont));
                incomeAndCostTable.addCell(new Phrase(cost.getCustomerName(), contentFont));
                incomeAndCostTable.addCell(new Phrase(cost.getServiceRemark(), contentFont));
                incomeAndCostCell = new PdfPCell(new Phrase(cost.getCostAmountStr(), contentFont));
                incomeAndCostCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                incomeAndCostCell.setBorderWidth(0f);
                incomeAndCostTable.addCell(incomeAndCostCell);
            } else {
                incomeAndCostCell = new PdfPCell(new Phrase("", contentFont));
                incomeAndCostCell.setFixedHeight(20f);
                incomeAndCostCell.setColspan(6);
                incomeAndCostCell.setBorderWidth(0F);
                incomeAndCostTable.addCell(incomeAndCostCell);
                PdfPCell pdfPCell1 = new PdfPCell(new Phrase(cost.getBusinessScope(), contentFont));
                pdfPCell1.setColspan(1);
                PdfPCell pdfPCell2 = new PdfPCell(new Phrase(cost.getCostAmountStr(), contentFont));
                pdfPCell2.setColspan(4);
                pdfPCell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                pdfPCell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
                pdfPCell1.setHorizontalAlignment(Element.ALIGN_LEFT);
                pdfPCell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
                pdfPCell1.setBorderWidth(0f);
                pdfPCell2.setBorderWidth(0f);
                incomeAndCostTable.addCell(pdfPCell1);
                incomeAndCostTable.addCell(pdfPCell2);
            }
            incomeAndCostCell = new PdfPCell(new Phrase(cost.getCostFunctionalAmountStr(), contentFont));
            incomeAndCostCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            incomeAndCostCell.setBorderWidth(0f);
            incomeAndCostTable.addCell(incomeAndCostCell);

        }
        //表格底部留白
        incomeAndCostCell = new PdfPCell(new Phrase("", contentFont));
        incomeAndCostCell.setColspan(6);
        incomeAndCostCell.setFixedHeight(20f);
        incomeAndCostCell.setBorderWidth(0f);
        incomeAndCostTable.addCell(incomeAndCostCell);


        PdfPCell incomeAndCostCellBorder = new PdfPCell(incomeAndCostTable);
        incomeAndCostCellBorder.setBorderWidth(1f);
        incomeAndCostCellBorder.setPaddingLeft(5f);
        incomeAndCostCellBorder.setPaddingRight(5f);
        incomeAndCostTableBorder.addCell(incomeAndCostCellBorder);
        document.add(incomeAndCostTableBorder);


        PdfPTable pdfPTable = new PdfPTable(1);
        pdfPTable.setSpacingBefore(10f);
        pdfPTable.setWidthPercentage(104);
        PdfPCell printer = new PdfPCell(new Phrase("打印人员:" + SecurityUtils.getUser().getUserCname(), headerFont));
        printer.setVerticalAlignment(Element.ALIGN_MIDDLE);
        printer.setPaddingLeft(400f);
        printer.setBorderWidth(0f);

        PdfPCell printTime = new PdfPCell(new Phrase("打印时间:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")), headerFont));
        printTime.setVerticalAlignment(Element.ALIGN_MIDDLE);
        printTime.setPaddingLeft(400f);
        printTime.setBorderWidth(0f);

        pdfPTable.addCell(printer);
        pdfPTable.addCell(printTime);

        document.add(pdfPTable);


    }

    private ShippingBillData getShippingBill(Integer orgId, String orderUUID, String apiType) {
        Assert.hasLength(orgId + "", "非法企业ID");
        Assert.hasLength(orderUUID, "非法订单号");
        Assert.hasLength(apiType, "非法配置类型");

        OrgInterfaceVo config = getShippingBillConfig(orgId, apiType);
        String token = getShippingBillAccessToken(config);
        Assert.hasLength(token, "未获取到第三方舱单访问Token");

        //获取总单数据XMl
        String mawbXML = this.baseMapper.getMAWBXML(orderUUID, SecurityUtils.getUser().getId(), apiType);
        //获取分单数据xml
        String hawbXML = this.baseMapper.getHAWBXML(orderUUID, orgId);

        StringBuilder builder = new StringBuilder();
        builder.append("<data>");
        builder.append(mawbXML);
        if (StringUtils.isNotBlank(hawbXML)) {
            builder.append(hawbXML);
        }
        builder.append("</data>");

        ShippingBillData shippingBillData = new ShippingBillData();
        shippingBillData.setAuthToken(token);
        shippingBillData.setFunction(config.getFunction());
        shippingBillData.setPlatform(config.getPlatform());
        shippingBillData.setUrl(config.getUrlPost());

        shippingBillData.setData(builder.toString());
        return shippingBillData;
    }

    public static String getShippingBillAccessToken(OrgInterfaceVo config) {
        String url = config.getUrlAuth(),
                authToken = config.getAuthToken(),
                appId = config.getAppid();
        Assert.hasLength(url, "舱单配置信息异常，Token_URL");
        Assert.hasLength(authToken, "舱单配置信息异常,Access_Token");
        Assert.hasLength(appId, "舱单配置信息异常,APP_ID");


        Map<String, Object> params = new HashMap<>();
        params.put("appid", appId);
        params.put("auth_token", authToken);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url + "?appid={appid}&auth_token={auth_token}", String.class, params);
        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            throw new RuntimeException("舱单第三方接口调用异常：" + responseEntity.getBody());
        }
        return responseEntity.getBody();
    }

    private ShippingBillData getShippersData(String hasMwb, String orderUUID, String apiType, String letterIds) {
        EUserDetails user = SecurityUtils.getUser();
        Assert.hasLength(user.getOrgId() + "", "非法企业ID");
        Assert.hasLength(orderUUID, "非法订单号");
        Assert.hasLength(apiType, "非法配置类型");

        OrgInterfaceVo config = getShippingBillConfig(user.getOrgId(), apiType);
        String token = getShippingBillAccessToken(config);
        Assert.hasLength(token, "未获取到第三方舱单访问Token");

        //获取总单数据XMl
        String mawbXML = "";
        //获取分单数据xml
        String hawbXML = "";
        if (!"hawb".equals(hasMwb)) {
            mawbXML = this.baseMapper.getNewMAWBXML(orderUUID, user.getId(), apiType);
        }
        if ("all".equals(hasMwb) || StringUtils.isNotEmpty(letterIds)) {
            hawbXML = this.baseMapper.getNewHAWBXML(orderUUID, letterIds, user.getId(), apiType);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("<data>");
        if (StringUtils.isNotBlank(mawbXML)) {
            builder.append(mawbXML);
        }
        if (StringUtils.isNotBlank(hawbXML)) {
            builder.append(hawbXML);
        }
        builder.append("</data>");

        ShippingBillData shippingBillData = new ShippingBillData();
        shippingBillData.setAuthToken(token);
        shippingBillData.setFunction(config.getFunction());
        shippingBillData.setPlatform(config.getPlatform());
        shippingBillData.setUrl(config.getUrlPost());

        shippingBillData.setData(builder.toString());
        return shippingBillData;
    }

    public OrgInterfaceVo getShippingBillConfig(Integer orgId, String apiType) {
        MessageInfo<OrgInterfaceVo> messageInfo = remoteServiceToHRS.getShippingBillConfig(orgId, apiType);
        if (null == messageInfo || messageInfo.getCode() != 0) {
            throw new RuntimeException("舱单配置信息获取失败");
        }
        OrgInterfaceVo orgInterfaceVo = messageInfo.getData();
        Assert.notNull(orgInterfaceVo, "未获取到舱单配置信息");
        return orgInterfaceVo;
    }

    private String buildOrderTrackShareEmailContent(OrderTrackShare orderTrackShare) {
        String content = orderTrackShare.getContent();
        StringBuilder builder = new StringBuilder();
        builder.append(content.replaceAll("\n", "<br />"));
        builder.append("<br />");
        builder.append("<br />");

        if (StringUtils.isNotBlank(orderTrackShare.getOrderShareEndTime())) {
            builder.append("网址：");
            builder.append("<a href=\"");
            builder.append(orderTrackShare.getWebsite());
            builder.append("\">");
            builder.append(orderTrackShare.getWebsite());
            builder.append("</a>");
            builder.append("<br />");
            builder.append("（本地址" + orderTrackShare.getOrderShareEndTime() + "前有效）");

        } else {
            builder.append("网址：");
            builder.append(orderTrackShare.getWebsite());

        }
        builder.append("<br />");
        builder.append("<br />");

        builder.append("二维码:");
        builder.append("<div id='");
        builder.append(new Date().getTime());
        builder.append("'>");
        builder.append("<a href=\"");
        builder.append(orderTrackShare.getWebsite());
        builder.append("\">");
        builder.append("<img src='cid:qr_image' />");
        builder.append("</a>");
        builder.append("</div>");
        return builder.toString();
    }

    @Override
    public OrderTrack getOrderTrack(OrderTrackQuery trackQuery) {
        trackQuery.validate();

        String orderUUID = findOrderId(trackQuery);
        OrderTrack orderTrack;
        if (StringUtils.isNotBlank(orderUUID)) {
            orderTrack = getOrderTrack(orderUUID);
        } else {
            orderTrack = new OrderTrack();
            orderTrack.setAwbNumber(trackQuery.getAwbNumber());
            orderTrack.setHawbNumber(trackQuery.getHawbNumber());
            orderTrack.setTrackManifest(getAfAwbRouteTrackManifests(trackQuery));
            orderTrack.setRouteTracks(this.afAwbRouteTrackAwbService.getByAwbNumber(trackQuery.getAwbNumber()));
        }
        //构建舱单展示信息
        orderTrack.setManifestList(buildManifestVO(trackQuery.getBusinessScope(), orderTrack.getTrackManifest()));
        return orderTrack;
    }

    private LinkedList<ManifestVO> buildManifestVO(String businessScope, List<AfAwbRouteTrackManifest> manifestList) {
        LinkedList<ManifestVO> result = new LinkedList<>();
        LinkedHashMap<String, ManifestVO> linkedMap = new LinkedHashMap<>();
        String masterKey = "-1";
        manifestList.stream().forEach((item) -> {
            String hawbNumber = item.getHawbNumber();
            String key = StringUtils.isBlank(hawbNumber) ? masterKey : hawbNumber;
            ManifestVO manifest = linkedMap.get(key);
            if (null == manifest) {
                manifest = new ManifestVO();
                manifest.setMasterFlag(masterKey.equals(key));
                manifest.setAwbNumber(item.getAwbNumber());
                manifest.setHawbNumber(hawbNumber);
                manifest.setOrderNo(manifest.isMasterFlag() ? item.getAwbNumber() : hawbNumber);
                linkedMap.put(key, manifest);
            }
            //填充时间
            switch (businessScope) {
                case CommonConstants.BUSINESS_SCOPE.AI:
                    fillingAITime(item, manifest);
                    break;
                case CommonConstants.BUSINESS_SCOPE.AE:
                    fillingAETime(item, manifest);
                    if (StringUtils.isBlank(manifest.getDeclarationNumber())) {
                        manifest.setDeclarationNumber(parseDN(item.getRemark()));
                    }
                    break;
                default:
                    break;
            }
            //填充件重信息
            fillPieceWeight(manifest, item);
        });

        linkedMap.values().stream().forEach((item) -> {
            if (item.isMasterFlag()) {
                result.addFirst(item);
            } else {
                result.add(item);
            }
        });

        return result;
    }

    private void fillPieceWeight(ManifestVO manifest, AfAwbRouteTrackManifest item) {
        if (StringUtils.isNotBlank(item.getQuantity())) {
            manifest.setQuantity(item.getQuantity());
        }
        if (StringUtils.isNotBlank(item.getGrossWeight())) {
            manifest.setGrossWeight(item.getGrossWeight());
        }
    }

    private void fillingAITime(AfAwbRouteTrackManifest item, ManifestVO manifest) {
        String remark = item.getRemark();
        LocalDateTime eventTime = item.getEventTime();
        if (StringUtils.isBlank(remark) || remark.length() < 5 || null == eventTime) {
            return;
        }
        String remarkCode = item.getRemark().substring(0, 5);
        switch (remarkCode) {
            case "50003":
                //原始
                manifest.setOriginalTime(getEventTime(eventTime, manifest.getOriginalTime()));
                break;
            case "35301":
                //理货
                manifest.setTallyTime(getEventTime(eventTime, manifest.getTallyTime()));
                break;
            case "39301":
                //申请
                manifest.setApplyTime(getEventTime(eventTime, manifest.getApplyTime()));
                break;
            case "3B201":
                //运抵
                manifest.setArriveTime(getEventTime(eventTime, manifest.getArriveTime()));
                break;
            case "90001":
                //放行
                manifest.setPassedTime(getEventTime(eventTime, manifest.getPassedTime()));
                break;
            default:
                break;
        }
    }

    private void fillingAETime(AfAwbRouteTrackManifest item, ManifestVO manifest) {
        String remark = item.getRemark();
        LocalDateTime eventTime = item.getEventTime();
        if (StringUtils.isBlank(remark) || null == eventTime) {
            return;
        }
        if (remark.indexOf("预配成功") > -1) {
            manifest.setProvisionTime(getEventTime(eventTime, manifest.getProvisionTime()));
        } else if (remark.indexOf("运抵成功") > -1 || remark.indexOf("运抵暂存") > -1) {
            manifest.setArriveTime(getEventTime(eventTime, manifest.getArriveTime()));
        } else if (remark.indexOf("海关放行") > -1) {
            manifest.setPassedTime(getEventTime(eventTime, manifest.getPassedTime()));
        }

    }

    private String parseDN(String remark) {
        if (StringUtils.isBlank(remark)) {
            return null;
        }
        String pattern = "^报关单号：(\\d+)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(remark);
        return m.find() ? m.group(1) : null;
    }

    private LocalDateTime getEventTime(LocalDateTime eventTime, LocalDateTime time) {
        if (null == time) {
            return eventTime;
        }
        return eventTime.compareTo(time) > 0 ? time : eventTime;
    }

    private String findOrderId(OrderTrackQuery query) {
        LambdaQueryWrapper<AfOrder> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(AfOrder::getAwbNumber, query.getAwbNumber());
        lambdaQueryWrapper.eq(AfOrder::getBusinessScope, query.getBusinessScope());
        lambdaQueryWrapper.eq(AfOrder::getOrgId, query.getOrgId());

        if (CommonConstants.BUSINESS_SCOPE.AI.equals(query.getBusinessScope())) {
            if (StringUtils.isNotBlank(query.getHawbNumber())) {
                lambdaQueryWrapper.eq(AfOrder::getHawbNumber, query.getHawbNumber());
            } else {
                lambdaQueryWrapper.and(true, (wrapper) -> wrapper.eq(AfOrder::getHawbNumber, null).or().eq(AfOrder::getHawbNumber, ""));
            }
        }
        List<AfOrder> orderList = this.baseMapper.selectList(lambdaQueryWrapper);
        return orderList.isEmpty() ? null : orderList.get(0).getOrderUuid();
    }

    private List<AfAwbRouteTrackManifest> getAfAwbRouteTrackManifests(OrderTrackQuery orderTrackQuery) {
        AfOrder orderCondition = new AfOrder();
        orderCondition.setBusinessScope(orderTrackQuery.getBusinessScope());
        orderCondition.setAwbNumber(orderTrackQuery.getAwbNumber());
        orderCondition.setHawbNumber(orderTrackQuery.getHawbNumber());
        return this.getAfAwbRouteTrackManifests(orderCondition);
    }

    /**
     * 格式化单号
     *
     * @param number
     * @return
     */
    private String formatNumber(String number) {
        number = number.trim().replace("-", "");
        boolean result = number.matches("^[0-9]{11}");
        if (!result) {
            throw new IllegalArgumentException("主单号格式不正确");
        }
        String secNumber = number.substring(3, 11);
        number = number.substring(0, 3) + "-" + secNumber;
        if (Integer.valueOf(secNumber.substring(0, 7)) % 7 != Integer.valueOf(secNumber.substring(7)).intValue()) {
            throw new IllegalArgumentException("主单号格式不正确");
        }
        return number;
    }

}
