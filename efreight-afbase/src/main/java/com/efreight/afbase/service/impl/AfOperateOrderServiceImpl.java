package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.*;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.entity.shipping.APIType;
import com.efreight.afbase.entity.shipping.ShippingBillData;
import com.efreight.afbase.entity.view.*;
import com.efreight.afbase.service.*;
import com.efreight.afbase.utils.FilePathUtils;
import com.efreight.common.core.feign.RemoteCoopService;
import com.efreight.common.core.feign.RemoteServiceToHRS;
import com.efreight.common.core.utils.JxlsUtils;
import com.efreight.common.remoteVo.OrderForVL;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.common.security.vo.CoopVo;
import com.efreight.common.security.vo.OrgInterfaceVo;
import com.efreight.afbase.entity.exportExcel.OrderExcel;
import com.itextpdf.text.DocumentException;
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
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class AfOperateOrderServiceImpl extends ServiceImpl<AfOperateOrderMapper, AfOperateOrder> implements AfOperateOrderService {
    private final AfOrderService afOrderService;
    private final LogService logService;
    private final ScLogService scLogService;
    private final TcLogService tcLogService;
    private final AfAwbRouteMapper afAwbRouteMapper;
    private final AfShipperLetterService afShipperLetterService;
    private final AfOrderShipperConsigneeMapper afOrderShipperConsigneeMapper;
    private final AfOrderShipperConsigneeService afOrderShipperConsigneeService;
    private final OrderInquiryService orderInquiryService;
    private final OrderInquiryQuotationService orderInquiryQuotationService;
    private final AfOrderShareService afOrderShareService;
    private final RemoteCoopService remoteCoopService;
    private final AfIncomeMapper afIncomeMapper;
    private final AfCostMapper afCostMapper;
    private final CssPaymentMapper cssPaymentMapper;
    private final AirportService airportService;
    private final RemoteServiceToHRS remoteServiceToHRS;
    private final AfRountingSignMapper afRountingSignMapper;
    private final AwbNumberMapper awbMapper;

    @Override
    public IPage<AfOperateOrder> getListPage(Page page, AfOperateOrder bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        bean.setCurrentUserId(SecurityUtils.getUser().getId());
        IPage<AfOperateOrder> afPage = baseMapper.getListPage(page, bean);
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
    public List<AfOperateOrder> getTatol(AfOperateOrder bean) {

        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        List<AfOperateOrder> list = baseMapper.getTatol(bean);
        List<AfOperateOrder> tatolList = new ArrayList<AfOperateOrder>();
        AfOperateOrder order = new AfOperateOrder();

        Integer planPieces = 0;
        Integer storagePieces = 0;
        BigDecimal planWeight = new BigDecimal(0);
        BigDecimal storageWeight = new BigDecimal(0);
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
            AfOperateOrder afOrder = list.get(i);
            if (afOrder.getPlanPieces() != null) {
                planPieces = planPieces + afOrder.getPlanPieces();
            }
            if (afOrder.getStoragePieces() != null) {
                storagePieces = storagePieces + afOrder.getStoragePieces();
            }
            if (afOrder.getPlanWeight() != null) {
                planWeight = planWeight.add(afOrder.getPlanWeight());
            }
            if (afOrder.getStorageWeight() != null) {
                storageWeight = storageWeight.add(afOrder.getStorageWeight());
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
        order.setStoragePieces(storagePieces);
        order.setPlanWeight(new BigDecimal(decimalFormat.format(planWeight)));
        order.setStorageWeight(new BigDecimal(decimalFormat.format(storageWeight)));
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
    public List<AEOperateOrder> exportAeExcel(AfOperateOrder bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        List<AEOperateOrder> list = baseMapper.exportAeExcel(bean);
        if (list.size() > 0) {
            DecimalFormat decimalFormat = new DecimalFormat("#,###,###,###.##########");
            list.stream().forEach(order -> {
                order.setPlanPieces(decimalFormat.format(order.getPlanPieces() != null ? Double.valueOf(order.getPlanPieces()) : 0));
                order.setPlanDensity(decimalFormat.format(order.getPlanDensity() != null ? Double.valueOf(order.getPlanDensity()) : 0));
                order.setPlanWeight(decimalFormat.format(order.getPlanWeight() != null ? Double.valueOf(order.getPlanWeight()) : 0));
                order.setPlanVolume(decimalFormat.format(order.getPlanVolume() != null ? Double.valueOf(order.getPlanVolume()) : 0));
                order.setPlanChargeWeight(decimalFormat.format(order.getPlanChargeWeight() != null ? Double.valueOf(order.getPlanChargeWeight()) : 0));
                order.setStoragePieces(decimalFormat.format(order.getStoragePieces() != null ? Double.valueOf(order.getStoragePieces()) : 0));
                order.setStorageWeight(decimalFormat.format(order.getStorageWeight() != null ? Double.valueOf(order.getStorageWeight()) : 0));
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
            List<AEOperateOrder> listSum = baseMapper.exportAeExcelSUM(bean);
            AEOperateOrder aeOrder = listSum.get(0);
            aeOrder.setPlanWeight(decimalFormat.format(aeOrder.getPlanWeight() != null ? Double.valueOf(aeOrder.getPlanWeight()) : 0));
            aeOrder.setPlanVolume(decimalFormat.format(aeOrder.getPlanVolume() != null ? Double.valueOf(aeOrder.getPlanVolume()) : 0));
            aeOrder.setPlanChargeWeight(decimalFormat.format(aeOrder.getPlanChargeWeight() != null ? Double.valueOf(aeOrder.getPlanChargeWeight()) : 0));
            aeOrder.setPlanPieces(decimalFormat.format(aeOrder.getPlanPieces() != null ? Double.valueOf(aeOrder.getPlanPieces()) : 0));
            aeOrder.setPlanDensity(decimalFormat.format(aeOrder.getPlanDensity() != null ? Double.valueOf(aeOrder.getPlanDensity()) : 0));
            aeOrder.setStoragePieces(decimalFormat.format(aeOrder.getStoragePieces() != null ? Double.valueOf(aeOrder.getStoragePieces()) : 0));
            aeOrder.setStorageWeight(decimalFormat.format(aeOrder.getStorageWeight() != null ? Double.valueOf(aeOrder.getStorageWeight()) : 0));
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
    public IPage<VPrmCoop> selectCoop(Page page, VPrmCoop bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        return baseMapper.selectCoop(page, bean);
    }

    @Override
    public IPage<VPrmCoop> selectPrmCoop(Page page, VPrmCoop bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        return baseMapper.selectPrmCoop(page, bean);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AfOperateOrder doSave(AfOperateOrder bean) {
        if (StringUtils.isNotBlank(bean.getExpectFlight())) {
            String expectFlight = bean.getExpectFlight();
            if (expectFlight.length() > 2) {
                expectFlight = bean.getExpectFlight().substring(0, 2);
            }
        }
        int isHaveAWB = 0;
        bean.setOrderStatus("订单创建");
        if (bean.getAwbNumber() != null && !"".equals(bean.getAwbNumber())) {
            List<AwbNumber> awbList = baseMapper.selectAwb(SecurityUtils.getUser().getOrgId(), bean.getAwbNumber());
//            if (awbList.size() == 0) {
//                throw new RuntimeException("主单号不存在");
//            }
//            bean.setAwbId(awbList.get(0).getAwbId());
//            bean.setAwbUuid(awbList.get(0).getAwbUuid());
//            //状态
//            baseMapper.updateAwbStatus(awbList.get(0).getAwbId(), "已配单", SecurityUtils.getUser().getOrgId());
            if (awbList.size() > 0) {
            	bean.setAwbId(awbList.get(0).getAwbId());
                bean.setAwbUuid(awbList.get(0).getAwbUuid());
                //状态
                baseMapper.updateAwbStatus(awbList.get(0).getAwbId(), "已配单", SecurityUtils.getUser().getOrgId());
                
            }else {
            	List<VPrmCoop> coopList = baseMapper.selectVPrmCoop(SecurityUtils.getUser().getOrgId());
            	if (coopList.size()==1) {
            		AwbNumber awbNumber=new AwbNumber();
            		awbNumber.setCreatTime(new Date());
            		awbNumber.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            		awbNumber.setCreatorId(SecurityUtils.getUser().getId());
            		awbNumber.setOrgId(SecurityUtils.getUser().getOrgId());
            		
            		awbNumber.setAwbNumber(bean.getAwbNumber());
            		awbNumber.setAwbFromId(String.valueOf(coopList.get(0).getCoopId()));
            		awbNumber.setAwbFromName(coopList.get(0).getCoopName());
            		awbNumber.setAwbFromType(coopList.get(0).getCoopType());
//            		baseMapper.insertAwbNumber(awbNumber);
            		awbNumber.setAwbStatus("已配单"); 
            		awbNumber.setAwbUuid(UUID.randomUUID().toString());
            		awbMapper.insert(awbNumber);
            		bean.setAwbId(awbNumber.getAwbId());
                    bean.setAwbUuid(awbNumber.getAwbUuid());
				} else {
					throw new RuntimeException("主单号不存在");
				}
            	
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
        List<AfOperateOrder> codeList = baseMapper.selectCode(SecurityUtils.getUser().getOrgId(), code);

        if (codeList.size() == 0) {
            bean.setOrderCode(code + "0001");
        } else if (codeList.size() < 9999) {
            bean.setOrderCode(code + String.format("%04d", codeList.size() + 1));
        } else {
            throw new RuntimeException("每天最多可以创建9999个操作订单");
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
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        bean.setBusinessScope("AE");
        bean.setIncomeStatus("未录收入");
        bean.setCostStatus("未录成本");
        bean.setIncomeRecorded(false);
        bean.setCostRecorded(false);
        baseMapper.insert(bean);

      //插入出口订单 附属表
        baseMapper.insertOrderExtend(SecurityUtils.getUser().getOrgId(), bean);
        //联系人
        for (int i = 0; i < bean.getOrderContacts().size(); i++) {
            baseMapper.insertOrderContacts(SecurityUtils.getUser().getOrgId(), bean.getOrderId(), bean.getOrderContacts().get(i));
        }
        //收发货人
        AfOrderShipperConsignee afOrderShipperConsignee = bean.getAfOrderShipperConsignee1();
        if (afOrderShipperConsignee != null) {
            afOrderShipperConsignee.setOrderId(bean.getOrderId());
            afOrderShipperConsignee.setCreateTime(LocalDateTime.now());
            afOrderShipperConsignee.setCreatorId(SecurityUtils.getUser().getId());
            afOrderShipperConsignee.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            afOrderShipperConsignee.setOrgId(SecurityUtils.getUser().getOrgId());
            afOrderShipperConsigneeMapper.insert(afOrderShipperConsignee);
        }
        AfOrderShipperConsignee afOrderShipperConsignee2 = bean.getAfOrderShipperConsignee2();
        if (afOrderShipperConsignee2 != null) {
            afOrderShipperConsignee2.setOrderId(bean.getOrderId());
            afOrderShipperConsignee2.setCreateTime(LocalDateTime.now());
            afOrderShipperConsignee2.setCreatorId(SecurityUtils.getUser().getId());
            afOrderShipperConsignee2.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            afOrderShipperConsignee2.setOrgId(SecurityUtils.getUser().getOrgId());
            afOrderShipperConsigneeMapper.insert(afOrderShipperConsignee2);
        }
        //添加日志信息

        LogBean logBean = new LogBean();
        logBean.setPageName("操作订单");
        logBean.setPageFunction("订单创建");
        logBean.setBusinessScope("AE");
        logBean.setOrderNumber(bean.getOrderCode());
        logBean.setOrderId(bean.getOrderId());
        logBean.setOrderUuid(bean.getOrderUuid());
        logService.saveLog(logBean);
        if (isHaveAWB == 1) {
            LogBean logBean2 = new LogBean();
            logBean2.setPageName("操作订单");
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
            baseMapper.createIncomeAndCost(bean.getOrderUuid(), SecurityUtils.getUser().getOrgId());
        }

        if (bean.getAwbNumber() != null && !"".equals(bean.getAwbNumber())) {
            //创建订单时 查询是否运单表中存在主单号，若不存在则插入 运单表
            LambdaQueryWrapper<AfAwbRoute> afAwbRouteWrapper = new LambdaQueryWrapper<AfAwbRoute>();
            afAwbRouteWrapper.eq(AfAwbRoute::getAwbNumber, bean.getAwbNumber());
            AfAwbRoute route = afAwbRouteMapper.selectOne(afAwbRouteWrapper);
            if (route == null) {
                AfAwbRoute insertRoute = new AfAwbRoute();
                insertRoute.setAwbNumber(bean.getAwbNumber());
                insertRoute.setCreateTime(LocalDateTime.now());
                insertRoute.setIsTrack(0);
                afAwbRouteMapper.insert(insertRoute);
            }

        }

        //插入分单信息
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
            orderInquiry.setEditorId(SecurityUtils.getUser().getId());
            orderInquiry.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            orderInquiry.setEditTime(LocalDateTime.now());
            orderInquiry.setOrderUuid(bean.getOrderUuid());
            orderInquiry.setOrderId(bean.getOrderId());
            orderInquiry.setRowUuid(UUID.randomUUID().toString());
            orderInquiryService.updateById(orderInquiry);

            OrderInquiryQuotation orderInquiryQuotation = orderInquiryQuotationService.getById(bean.getOrderInquiryQuotationId());
            LambdaQueryWrapper<OrderInquiryQuotation> orderInquiryQuotationWrapper = Wrappers.<OrderInquiryQuotation>lambdaQuery();
            orderInquiryQuotationWrapper.eq(OrderInquiryQuotation::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrderInquiryQuotation::getIsValid, true).eq(OrderInquiryQuotation::getQuotationSelected, true).eq(OrderInquiryQuotation::getOrderInquiryId, bean.getOrderInquiryId());
            OrderInquiryQuotation inquiryQuotation = orderInquiryQuotationService.getOne(orderInquiryQuotationWrapper);
            if (orderInquiryQuotation == null) {
                throw new RuntimeException("报价方案不存在，无法保存");
            }
            if (!orderInquiryQuotation.getIsValid()) {
                throw new RuntimeException("报价方案已失效，无法保存");
            }
            if (!orderInquiryQuotation.getQuotationSelected()) {
                orderInquiryQuotation.setQuotationSelected(true);
                orderInquiryQuotation.setEditorId(SecurityUtils.getUser().getId());
                orderInquiryQuotation.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                orderInquiryQuotation.setEditTime(LocalDateTime.now());
                orderInquiryQuotationService.updateById(orderInquiryQuotation);
                if (inquiryQuotation != null) {
                    inquiryQuotation.setQuotationSelected(false);
                    inquiryQuotation.setEditorId(SecurityUtils.getUser().getId());
                    inquiryQuotation.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
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
            aos.setEditorId(SecurityUtils.getUser().getId());
            aos.setEditorName(SecurityUtils.getUser().buildOptName());
            aos.setEditTime(LocalDateTime.now());
            afOrderShareService.updateById(aos);
            //插入新订单  分享协作
            AfOrderShare updateAos = new AfOrderShare();
            updateAos.setShareScope("订单协作");
            updateAos.setProcess("in");
            updateAos.setOrderId(bean.getOrderId());
            updateAos.setOrgId(SecurityUtils.getUser().getOrgId());
            updateAos.setBusinessScope("AE");
            updateAos.setShareCoopId(bean.getCoopOrgCoopId());
            updateAos.setShareOrgId(bean.getOrderShareOrgId());
            updateAos.setShareOrderId(bean.getOrderShareOrderId());
            updateAos.setCreateTime(LocalDateTime.now());
            updateAos.setCreatorId(SecurityUtils.getUser().getId());
            updateAos.setCreatorName(SecurityUtils.getUser().buildOptName());
            afOrderShareService.save(updateAos);
            //日志
            LogBean logBeanShare = new LogBean();
            logBeanShare.setPageName("操作订单");
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
        afRountingSign.setOrgId(SecurityUtils.getUser().getOrgId());
        afRountingSign.setBusinessScope("AE");
        afRountingSign.setSignState(0);
        afRountingSign.setMsrUnitprice(new BigDecimal("0"));
        afRountingSign.setIncomeWeight(0.00);
        afRountingSign.setMsrAmountWriteoff(new BigDecimal("0"));
        afRountingSign.setRowUuid(UUID.randomUUID().toString());
        afRountingSign.setEditorId(SecurityUtils.getUser().getId());
        afRountingSign.setEdit_time(new Date());
        afRountingSign.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        
        afRountingSignMapper.insert(afRountingSign);
        return bean;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doUpdate(AfOperateOrder bean) {
        AfOperateOrder order = baseMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
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
        if (bean.getAwbId() == null || "null".equals(bean.getAwbId())) {
            isNew = 1;
        }
        int isHave = 0;
        if (bean.getAwbId() == null || "null".equals(bean.getAwbId())) {
            if (bean.getAwbNumber() != null && !"".equals(bean.getAwbNumber())) {
                List<AwbNumber> awbList = baseMapper.selectAwb(SecurityUtils.getUser().getOrgId(), bean.getAwbNumber());
//                if (awbList.size() == 0) {
//                    throw new RuntimeException("主单号不存在");
//                }
//                bean.setAwbId(awbList.get(0).getAwbId());
//                bean.setAwbUuid(awbList.get(0).getAwbUuid());
//                baseMapper.updateAwbStatus(awbList.get(0).getAwbId(), "已配单", SecurityUtils.getUser().getOrgId());
                if (awbList.size() > 0) {
                	bean.setAwbId(awbList.get(0).getAwbId());
                    bean.setAwbUuid(awbList.get(0).getAwbUuid());
                    //状态
                    baseMapper.updateAwbStatus(awbList.get(0).getAwbId(), "已配单", SecurityUtils.getUser().getOrgId());
                    
                }else {
                	List<VPrmCoop> coopList = baseMapper.selectVPrmCoop(SecurityUtils.getUser().getOrgId());
                	if (coopList.size()==1) {
                		AwbNumber awbNumber=new AwbNumber();
                		awbNumber.setCreatTime(new Date());
                		awbNumber.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                		awbNumber.setCreatorId(SecurityUtils.getUser().getId());
                		awbNumber.setOrgId(SecurityUtils.getUser().getOrgId());
                		
                		awbNumber.setAwbNumber(bean.getAwbNumber());
                		awbNumber.setAwbFromId(String.valueOf(coopList.get(0).getCoopId()));
                		awbNumber.setAwbFromName(coopList.get(0).getCoopName());
                		awbNumber.setAwbFromType(coopList.get(0).getCoopType());
//                		baseMapper.insertAwbNumber(awbNumber);
                		awbNumber.setAwbStatus("已配单"); 
                		awbNumber.setAwbUuid(UUID.randomUUID().toString());
                		awbMapper.insert(awbNumber);
                		bean.setAwbId(awbNumber.getAwbId());
                        bean.setAwbUuid(awbNumber.getAwbUuid());
    				} else {
    					throw new RuntimeException("主单号不存在");
    				}
                	
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
        } else if (bean.getAwbNumber() != null && !"".equals(bean.getAwbNumber())) {
            bean.setOrderStatus("舱位确认");
        } else {
            bean.setOrderStatus("订单创建");
        }
        //添加日志信息
        LogBean logBean = new LogBean();
        logBean.setPageName("操作订单");
        logBean.setPageFunction("修改订单");
        logBean.setLogRemark(this.getLogRemark(order, bean));
        logBean.setBusinessScope("AE");
        logBean.setOrderNumber(bean.getOrderCode());
        logBean.setOrderId(bean.getOrderId());
        logBean.setOrderUuid(bean.getOrderUuid());
        logService.saveLog(logBean);
        if (isNew == 1 && isHave == 1) {
            LogBean logBean2 = new LogBean();
            logBean2.setPageName("操作订单");
            logBean2.setPageFunction("舱位确认");
            logBean2.setLogRemark("主单：" + bean.getAwbNumber());
            logBean2.setBusinessScope("AE");
            logBean2.setOrderNumber(bean.getOrderCode());
            logBean2.setOrderId(bean.getOrderId());
            logBean2.setOrderUuid(bean.getOrderUuid());
            logService.saveLog(logBean2);
        }
        bean.setRowUuid(UUID.randomUUID().toString());
        bean.setEditTime(new Date());
        bean.setEditorId(SecurityUtils.getUser().getId());
        bean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        UpdateWrapper<AfOperateOrder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("order_id", bean.getOrderId());
        baseMapper.update(bean, updateWrapper);
        //联系人先删除再增加
        baseMapper.deleteOrderContacts(SecurityUtils.getUser().getOrgId(), bean.getOrderId());
        for (int i = 0; i < bean.getOrderContacts().size(); i++) {
            baseMapper.insertOrderContacts(SecurityUtils.getUser().getOrgId(), bean.getOrderId(), bean.getOrderContacts().get(i));
        }
      //附属表先删除在增加
        baseMapper.deleteOrderExtend(SecurityUtils.getUser().getOrgId(), bean.getOrderId());
        baseMapper.insertOrderExtend(SecurityUtils.getUser().getOrgId(), bean);
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
        if (isNew == 1) {
            if (bean.getAwbNumber() != null && !"".equals(bean.getAwbNumber())) {
                //自动生成 收入、成本
                baseMapper.createIncomeAndCost(bean.getOrderUuid(), SecurityUtils.getUser().getOrgId());
            }
        }
        if (bean.getAwbNumber() != null && !"".equals(bean.getAwbNumber())) {
            //创建订单时 查询是否运单表中存在主单号，若不存在则插入 运单表
            LambdaQueryWrapper<AfAwbRoute> afAwbRouteWrapper = new LambdaQueryWrapper<AfAwbRoute>();
            afAwbRouteWrapper.eq(AfAwbRoute::getAwbNumber, bean.getAwbNumber());
            AfAwbRoute route = afAwbRouteMapper.selectOne(afAwbRouteWrapper);
            if (route == null) {
                AfAwbRoute insertRoute = new AfAwbRoute();
                insertRoute.setAwbNumber(bean.getAwbNumber());
                insertRoute.setCreateTime(LocalDateTime.now());
                insertRoute.setIsTrack(0);
                afAwbRouteMapper.insert(insertRoute);
            }
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

    private String getOrderCode() {
        Date dt = new Date();

        String year = String.format("%ty", dt);

        String mon = String.format("%tm", dt);

        String day = String.format("%td", dt);
        return "AE-" + year + mon + day;
    }

    private String getLogRemark(AfOperateOrder order, AfOperateOrder bean) {
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
        String pickUpDeliveryService = this.getStr(order.getPickUpDeliveryService() ? "是" : "否", (bean.getPickUpDeliveryService()!=null && bean.getPickUpDeliveryService()) ? "是" : "否");
        logremark.append(StringUtils.isBlank(pickUpDeliveryService) ? "" : "提货：" + pickUpDeliveryService);
        String warehouseService = this.getStr(order.getWarehouseService() ? "是" : "否", (bean.getWarehouseService()!=null && bean.getWarehouseService()) ? "是" : "否");
        logremark.append(StringUtils.isBlank(warehouseService) ? "" : "库内：" + warehouseService);
        String outfieldService = this.getStr(order.getOutfieldService() ? "是" : "否", (bean.getOutfieldService()!=null && bean.getOutfieldService()) ? "是" : "否");
        logremark.append(StringUtils.isBlank(outfieldService) ? "" : "外场：" + outfieldService);
        String customsClearanceService = this.getStr(order.getCustomsClearanceService() ? "是" : "否", (bean.getCustomsClearanceService()!=null && bean.getCustomsClearanceService()) ? "是" : "否");
        logremark.append(StringUtils.isBlank(customsClearanceService) ? "" : "报关：" + customsClearanceService);
        String arrivalCustomsClearanceService = this.getStr(order.getArrivalCustomsClearanceService() ? "是" : "否", (bean.getArrivalCustomsClearanceService()!=null && bean.getArrivalCustomsClearanceService()) ? "是" : "否");
        logremark.append(StringUtils.isBlank(arrivalCustomsClearanceService) ? "" : "清关：" + arrivalCustomsClearanceService);
        String deliveryService = this.getStr(order.getDeliveryService() ? "是" : "否", (bean.getDeliveryService()!=null && bean.getDeliveryService()) ? "是" : "否");
        logremark.append(StringUtils.isBlank(deliveryService) ? "" : "派送：" + deliveryService);

        String freightProfitRatioRemark = this.getStr(order.getFreightProfitRatioRemark(), bean.getFreightProfitRatioRemark());
        logremark.append(StringUtils.isBlank(freightProfitRatioRemark) ? "" : "客户分泡：" + freightProfitRatioRemark);
        String msrProfitRatioRemark = this.getStr(order.getMsrProfitRatioRemark(), bean.getMsrProfitRatioRemark());
        logremark.append(StringUtils.isBlank(msrProfitRatioRemark) ? "" : "成本分泡：" + msrProfitRatioRemark);
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

    @Override
    public Boolean selectOrderStatus(String node_name, String order_uuid) {
        List<Integer> list = baseMapper.getOrderStatus(node_name, order_uuid);
        if (list.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public AfOperateOrder getOrderById(Integer orderId, Integer letterId) {
        AfOperateOrder bean = baseMapper.selectById(orderId);
        //联系人
        bean.setCoopName(baseMapper.getCoopName(bean.getCoopId()));
        bean.setOrderContacts(baseMapper.getorderContacts(bean.getOrgId(), orderId));
      //订单附属表
        AfOrderExtend  extend = baseMapper.getOrderExtend(bean.getOrgId(), orderId);
        if(extend != null){
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
            if(letterId != null && afShipperLetter.getSlId().equals(letterId)){
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
        return bean;
    }


    /**
     * TODO 需要确定是否可以卸载
     * @param bean
     * @return
     */
    @Override
    public Boolean doUninstall(AfOperateOrder bean) {
        //财务锁账已做的 ，不能重复做
        bean.setBusinessScope("AE");
        List<Integer> list = this.getOrderStatus(bean);
        if (list.size() > 0) {
            throw new RuntimeException("订单已操作完成,不能卸载主单");
        }
        AfOperateOrder order = baseMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        //校验是否可以卸载主单 确认收益过不可以
//        LambdaQueryWrapper<LogBean> wrapper = Wrappers.<LogBean>lambdaQuery();
//        wrapper.eq(LogBean::getOrgId, SecurityUtils.getUser().getOrgId()).eq(LogBean::getOrderUuid, bean.getOrderUuid()).eq(LogBean::getNodeName, "财务锁账").eq(LogBean::getPageFunction, "新建");
//        LogBean logBean = logMapper.selectOne(wrapper);
//        if (logBean.getCreatTime() != null) {
//            throw new RuntimeException("订单已操作完成,不能卸载主单");
//        }
        baseMapper.updateAwbStatus(bean.getAwbId(), "未使用", SecurityUtils.getUser().getOrgId());
        //日志
        LogBean logBean = new LogBean();
        logBean.setPageName("操作订单");
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
        baseMapper.updateOrderNumber(bean.getOrderUuid(), nodeName, SecurityUtils.getUser().getOrgId());
        return true;
    }

    @Override
    public List<Integer> getOrderStatus(AfOperateOrder bean) {
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean doFinish(AfOperateOrder bean) {
        //财务锁账已做的 ，不能重复做
        List<Integer> list = this.getOrderStatus(bean);
        if (list.size() > 0) {
            throw new RuntimeException("已操作完成");
        }

        //日志
//        logMapper.updateLog(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), "财务锁账", SecurityUtils.getUser().getId(),
//                SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail(), LocalDateTime.now());
        //
        LogBean logBean = new LogBean();
        logBean.setPageName("操作订单");
        logBean.setPageFunction("操作完成");

//        logBean.setLogRemark("财务日期：" + bean.getReceiptDate());
        logBean.setBusinessScope(bean.getBusinessScope());
        logBean.setOrderNumber(bean.getOrderCode());
        logBean.setOrderId(bean.getOrderId());
        logBean.setOrderUuid(bean.getOrderUuid());


        if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
            logService.saveLog(logBean);
            baseMapper.updateOrder(bean.getOrderUuid(), "财务锁账", UUID.randomUUID().toString());
            baseMapper.updateIncome(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), bean.getReceiptDate());
            baseMapper.updateCost(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid(), bean.getReceiptDate());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doCancel(AfOperateOrder bean) {
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
            AfOperateOrder order = baseMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
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
            AfOperateOrder order = baseMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
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
            AfOperateOrder order = baseMapper.getSEOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
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
            AfOperateOrder order = baseMapper.getSEOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
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
            AfOperateOrder order = baseMapper.getTCOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
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
        return true;
    }

    @Override
    public Boolean doStop(AfOperateOrder bean) {
        //财务锁账已做的 ，不能重复做
        bean.setBusinessScope("AE");
//        List<Integer> list = this.getOrderStatus(bean);
//        if (list.size() > 0) {
//            throw new RuntimeException("订单已操作完成,不能卸载主单");
//        }
        AfOperateOrder order = baseMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
        //校验是否可以卸载主单 确认收益过不可以
//        LambdaQueryWrapper<LogBean> wrapper = Wrappers.<LogBean>lambdaQuery();
//        wrapper.eq(LogBean::getOrgId, SecurityUtils.getUser().getOrgId()).eq(LogBean::getOrderUuid, bean.getOrderUuid()).eq(LogBean::getNodeName, "财务锁账").eq(LogBean::getPageFunction, "新建");
//        LogBean logBean = logMapper.selectOne(wrapper);
//        if (logBean.getCreatTime() != null) {
//            throw new RuntimeException("订单已操作完成,不能卸载主单");
//        }

        //baseMapper.updateAwbStatus(bean.getAwbId(), "已废单");//更改为物理删除
        //日志
        LogBean logBean = new LogBean();
        logBean.setPageName("操作订单");
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
        baseMapper.updateOrderNumber(bean.getOrderUuid(), nodeName, SecurityUtils.getUser().getOrgId());
        //根据AwbId和org_id物理删除
        baseMapper.deleteByAwbId(bean.getAwbId(), SecurityUtils.getUser().getOrgId());
        return true;
    }

    @Override
    public Boolean printOrderLetter(Integer orgId, String orderUuid, String userId) {
        return afOrderService.printOrderLetter(orgId,orderUuid,userId);
    }

    @Override
    public String printOrderLetter1(String orderUuid, Integer orgId, String userId) throws IOException, DocumentException {
        return afOrderService.printOrderLetter1(orderUuid,orgId,userId);
    }

    @Override
    @SneakyThrows
    public String print(OrderLetters orderLetters, boolean flag) {
        return afOrderService.print(orderLetters,flag);
    }

    @Override
    public void forceStop(String reason, String orderUuid, String businessScope) {
            //校验是否可以卸载主单 1确认收益过不可以
//        LambdaQueryWrapper<LogBean> LogWrapper = Wrappers.<LogBean>lambdaQuery();
//        LogWrapper.eq(LogBean::getOrgId, SecurityUtils.getUser().getOrgId()).eq(LogBean::getOrderUuid, orderUuid).eq(LogBean::getNodeName, "财务锁账").eq(LogBean::getPageFunction, "新建");
//        LogBean log = logMapper.selectOne(LogWrapper);
//        if (log.getCreatTime() != null) {
//            throw new RuntimeException("订单已操作完成,不能强制关闭");
//        }
            AfOperateOrder bean = new AfOperateOrder();
            bean.setOrderUuid(orderUuid);
            bean.setBusinessScope(businessScope);
            List<Integer> list = this.getOrderStatus(bean);
            if (list.size() > 0) {
                throw new RuntimeException("订单已操作完成,不能强制关闭");
            }
            LambdaQueryWrapper<AfOperateOrder> wrapper = Wrappers.<AfOperateOrder>lambdaQuery();
            wrapper.eq(AfOperateOrder::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfOperateOrder::getOrderUuid, orderUuid);
        AfOperateOrder afOperateOrder = baseMapper.selectOne(wrapper);
            if ("强制关闭".equals(afOperateOrder.getOrderStatus())) {
                throw new RuntimeException("该订单已经强制关闭，无需再关闭");
            }

            if (afOperateOrder.getAwbId() != null) {
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
            List<CssPayment> listCssPayment = cssPaymentMapper.queryCssPaymentListForWhere(SecurityUtils.getUser().getOrgId(), afOperateOrder.getBusinessScope(), orderUuid);
            if (listCssPayment != null && listCssPayment.size() > 0) {
                throw new RuntimeException("该订单已做对账单，不允许 关闭订单");
            }
            //修改订单信息
        afOperateOrder.setOrderStatus("强制关闭");
        afOperateOrder.setEditorId(SecurityUtils.getUser().getId());
        afOperateOrder.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        afOperateOrder.setEditTime(new Date());
            baseMapper.update(afOperateOrder, wrapper);

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
            logBean.setOrderNumber(afOperateOrder.getOrderCode());
            logBean.setOrderId(afOperateOrder.getOrderId());
            logBean.setOrderUuid(afOperateOrder.getOrderUuid());

            logService.saveLog(logBean);
            //HRS日志
            baseMapper.insertHrsLog(businessScope + "订单", "订单号:" + afOperateOrder.getOrderCode(),
                    SecurityUtils.getUser().getId(), LocalDateTime.now(), SecurityUtils.getUser().getOrgId(), SecurityUtils.getUser().getDeptId());
    }

    @Override
    public String awbSubmit(String orderUuid, Integer orgId) throws IOException, DocumentException {
        return afOrderService.awbSubmit(orderUuid,orgId);
    }
    @Override
    public AfOperateOrder queryOrderByOrderUuid(String orderUuid) {
        LambdaQueryWrapper<AfOperateOrder> wrapper = Wrappers.<AfOperateOrder>lambdaQuery();
        wrapper.eq(AfOperateOrder::getOrderUuid, orderUuid).eq(AfOperateOrder::getOrgId, SecurityUtils.getUser().getOrgId());
        AfOperateOrder afOrder = baseMapper.selectOne(wrapper);
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
        return afOrderService.getOrderTrack(orderUUID);
    }

    @Override
    public AfOperateOrder getOrderByUUID(String orderUUID) {
        LambdaQueryWrapper<AfOperateOrder> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(AfOperateOrder::getOrderUuid, orderUUID);
        return baseMapper.selectOne(lambdaQueryWrapper);
    }

    @Override
    public void orderTrackShareWithEmail(OrderTrackShare orderTrackShare) throws Exception {
        afOrderService.orderTrackShareWithEmail(orderTrackShare);
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
    public ShippingBillData getWaybillData(Integer orgId, String orderUUID) throws Exception {
        return this.getShippingBill(orgId, orderUUID, APIType.AE_DZ_POST_MAWB);
    }

    @Override
    public ShippingBillData getTagMakeData(Integer orgId, String orderUUID) throws Exception {
        return this.getShippingBill(orgId, orderUUID, APIType.BQ_POST_MAWB);
    }

    @Override
    public List<Map<String, Object>> homeStatistics(Integer orgId) {
        return afOrderService.homeStatistics(orgId);
    }

    @Override
    public List<Map<String, Object>> selectCompany(Integer orgId) {
        return afOrderService.selectCompany(orgId);
    }

    @Override
    public String getOrderCostStatusForAF(Integer orderId) {
        return afOrderService.getOrderCostStatusForAF(orderId);
    }

    @Override
    public String getOrderCostStatusForSC(Integer orderId) {
        return afOrderService.getOrderCostStatusForSC(orderId);
    }

    @Override
    public String getOrderCostStatusForLC(Integer orderId) {
        return afOrderService.getOrderCostStatusForLC(orderId);
    }

    @Override
    public String getOrderCostStatusForIO(Integer orderId) {
        return afOrderService.getOrderCostStatusForIO(orderId);
    }

    @Override
    public String getOrderCostStatusForTC(Integer orderId) {
        return afOrderService.getOrderCostStatusForTC(orderId);
    }

    @Override
    public void updateOrderCostStatusForSC(Integer orderId) {
        afOrderService.updateOrderCostStatusForSC(orderId);
    }

    @Override
    public String printBusinessCalculationBill(String businessScope, Integer orderId, Boolean ifReplace) {
        return afOrderService.printBusinessCalculationBill(businessScope,orderId,ifReplace);
    }

    @Override
    public OrderDeliveryNotice getOrderDeliveryNotice(String orderUuid, String flag) {
        return afOrderService.getOrderDeliveryNotice(orderUuid,flag);
    }

    @Override
    public OrderDeliveryNoticeCheck checkOrderDeliveryNotice(String orderUUID, String flag) {
        return afOrderService.checkOrderDeliveryNotice(orderUUID,flag);
    }

    @Override
    public IPage<VPrmCoop> getCoopList(Page page, VPrmCoop bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        return baseMapper.getCoopList(page, bean);
    }

    @Override
    public IPage<VPrmCoop> getCoopListNew(Page page, VPrmCoop bean) {
        return null;
    }

    @Override
    public void updateOrderCostStatusForTC(Integer orderId) {
        afOrderService.updateOrderCostStatusForTC(orderId);
    }

    @Override
    public List<OrderForVL> getOrderListForVL(OrderForVL orderForVL) {
        return afOrderService.getOrderListForVL(orderForVL);
    }

    @Override
    public String getMasterShippingBillCheck(String type, String hasMwb, String orderUUID, String letterIds) {
        return afOrderService.getMasterShippingBillCheck(type,hasMwb,orderUUID,letterIds);
    }

    @Override
    public void airCargoManifestPrint(Integer orderId) {
        afOrderService.airCargoManifestPrint(orderId);
    }

    @Override
    public Boolean shippingSendCheckHasSend(String orderUUID) {
        return afOrderService.shippingSendCheckHasSend(orderUUID);
    }

    @Override
    public Boolean insertLogAfterSendShipper(LogBean logbean) {
        return afOrderService.insertLogAfterSendShipper(logbean);
    }

    @Override
    public List<Map<String, Object>> getOpreationLookList(AfOrder bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        List<Map<String,Object>> list = baseMapper.getOpreationLookList(bean);
        return list;
    }

    @Override
    public IPage<Map<String, Object>> getOperaLookListPage(Page page, AfOrder bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        Number current = page.getCurrent();
        Number size = page.getSize();
        List<Map<String,Object>> list = baseMapper.getOpreationLookPageList(bean,current.intValue(),size.intValue());
        page.setRecords(list);
        long total = 0;
        if(list != null && list.size() > 0){
            total = (long) list.get(0).get("total");
        }
        page.setTotal(total);
        return page;
    }

    @Override
    public Boolean saveShippers(AfOperateOrder bean) {
        AfOperateOrder order = baseMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), bean.getOrderUuid());
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
        UpdateWrapper<AfOperateOrder> updateWrapper = new UpdateWrapper<>();
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

    @Override
    public Map<String, Object> sendShippersData(String hasMwb, String orderUUID, String letterIds) throws Exception {
        return afOrderService.sendShippersData(hasMwb,orderUUID,letterIds);
    }

    @Override
    public Map<String, Object> deleteShipper(String orderUUID, String letterId) {
        return afOrderService.deleteShipper(orderUUID,letterId);
    }

    @Override
    public void saveRouteInfo(String awbNumber, String hawNumber, String businessScope) {
        afOrderService.saveRouteInfo(awbNumber,hawNumber,businessScope);
    }

    @Override
    public OrderTrack getOrderTrack(String awbNumber, String hawbNumber, String businessScope) {
        OrderTrackQuery orderTrackQuery = new OrderTrackQuery(awbNumber, businessScope, SecurityUtils.getUser().getOrgId());
        orderTrackQuery.setHawbNumber(hawbNumber);
        return afOrderService.getOrderTrack(orderTrackQuery);
    }

    @Override
    public Map<String, Object> getAiOrderById(Integer orderId) {
        return afOrderService.getAiOrderById(orderId);
    }

    @Override
    public Map<String, Integer> checkCargoTrackingQuery(String awbNumber) {
        return afOrderService.checkCargoTrackingQuery(awbNumber);
    }


    @Override
    public OrderTrack cargoTracking(String awbNumber, String hawbNumber, String businessScope) {
        return afOrderService.cargoTracking(awbNumber,hawbNumber,businessScope);
    }

    @Override
    public Boolean insertAfLog(LogBean logBean) {
        try{
            String uuid = logBean.getOrderUuid();
            AfOperateOrder bean = this.baseMapper.getOrderByUUID(SecurityUtils.getUser().getOrgId(), uuid);
            logBean.setPageName("操作订单");
            logBean.setPageFunction("发送舱单");
            logBean.setBusinessScope("AE");
            logBean.setOrderNumber(bean.getOrderCode());
            logBean.setOrderId(bean.getOrderId());
            logService.saveLog(logBean);

//            if(!"mwb".equals(logBean.getHasMwb())){
//
//            }
            //更新订单表/分单表 manifest_status
            this.baseMapper.updateManifestStatus(logBean.getHasMwb(),uuid,logBean.getLetterIds(),"HAS_SEND");
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private ShippingBillData getShippingBill(Integer orgId, String orderUUID, String apiType) {
        Assert.hasLength(orgId+"", "非法企业ID");
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
        if(!"hwb".equals(hasMwb)){
            mawbXML = this.baseMapper.getNewMAWBXML(orderUUID, user.getId(), apiType);
        }
        if("all".equals(hasMwb) || StringUtils.isNotEmpty(letterIds)){
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

    private String getShippingBillAccessToken(OrgInterfaceVo config) {
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
    private OrgInterfaceVo getShippingBillConfig(Integer orgId, String apiType) {
        MessageInfo<OrgInterfaceVo> messageInfo = remoteServiceToHRS.getShippingBillConfig(orgId, apiType);
        if (null == messageInfo || messageInfo.getCode() != 0) {
            throw new RuntimeException("舱单配置信息获取失败");
        }
        OrgInterfaceVo orgInterfaceVo = messageInfo.getData();
        Assert.notNull(orgInterfaceVo, "未获取到舱单配置信息");
        return orgInterfaceVo;
    }

    @Override
    public void exitCard(AfOperateOrder bean) {
        String orgName=baseMapper.getOrgName(bean.getOrgId());

//    	ArrayList<OrderExcel> list = new ArrayList<>();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        OrderExcel excelBean=new OrderExcel();
        excelBean.setAwbNumber(bean.getAwbNumber().substring(0, 8)+" "+bean.getAwbNumber().substring(8));
        excelBean.setCoopName(orgName);
        excelBean.setGoodsNameCn(bean.getGoodsNameCn());
        excelBean.setCreateTime(getDate());
        excelBean.setStoragePieces(bean.getStoragePieces());
        excelBean.setStorageWeight(bean.getStorageWeight());
        excelBean.setStorageTime(df.format(bean.getStorageTime()));
//    	list.add(excelBean);
        HashMap<String, Object> map = new HashMap<>();
        map.put("data", excelBean);
        JxlsUtils.exportExcelWithLocalModel(FilePathUtils.filePath + "/PDFtemplate/EXIT_CARD.xls", map);
    }
    @Override
    public void doPrintGoodsName(AfOperateOrder bean) {
    	String orgName=baseMapper.getOrgName(bean.getOrgId());
    	Map<String,Object> obj=new HashMap<String, Object>();
    	List<CargoGoodsnames> list=baseMapper.queryGoodsNamelist(bean.getOrderId());
    	ArrayList<Map<String,Object>> resultlist = new ArrayList<Map<String,Object>>();
    	if (list.size()>0) {
    		for (int i = 0; i < list.size(); i++) {
    			Map<String,Object> bean1=new HashMap<String, Object>();
        		bean1.put("type", list.get(i).getCargoType());
//        		bean1.put("list2", list.get(i).getGoodsCnnames());
        		String goodsCnnames[]=list.get(i).getGoodsCnnames().split(",");
        		ArrayList<Map<String,Object>> list2 = new ArrayList<Map<String,Object>>();
        		Map<String,Object> bean2=new HashMap<String, Object>();
        		int index=0;
        		for (int j = 0; j < goodsCnnames.length; j++) {
        			index=(j+1)%4;
        			if (index==1 && j>3) {
        				list2.add(bean2);
        				bean2=new HashMap<String, Object>();
					}
        			bean2.put("name"+index,goodsCnnames[j]);
				}
        		list2.add(bean2);
        		bean1.put("list2", list2);
        		resultlist.add(bean1);
			}
    		
		}else {
			throw new RuntimeException("无货物品名清单数据");
		}
    	
    	DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    	obj.put("mawbcode", bean.getAwbNumber());
    	obj.put("company", orgName);
    	obj.put("date", df.format(LocalDateTime.now()));
    	obj.put("list1", resultlist);
    	
//    	list.add(excelBean);
    	HashMap<String, Object> map = new HashMap<>();
    	map.put("data", obj);
    	JxlsUtils.exportExcelWithLocalModel(FilePathUtils.filePath + "/PDFtemplate/GOODS_NAMES.xlsx", map);
    }
    public static String getDate() {
        String year = new SimpleDateFormat("yy", Locale.CHINESE).format(new Date());
        return year;
    }
}
