package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ctc.wstx.util.StringUtil;
import com.efreight.afbase.dao.AfOrderMapper;
import com.efreight.afbase.dao.AfRountingSignMapper;
import com.efreight.afbase.dao.InboundMapper;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.service.AfOrderService;
import com.efreight.afbase.service.InboundFilesService;
import com.efreight.afbase.service.InboundService;
import com.efreight.afbase.service.LogService;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InboundServiceImpl extends ServiceImpl<InboundMapper, Inbound> implements InboundService {

    private final LogService logService;
    private final AfOrderService afOrderService;
    private final InboundFilesService inboundFilesService;
    private final AfRountingSignMapper afRountingSignMapper;
    private final AfOrderMapper afOrderMapper;


    @Override
    public IPage getPage(Page page, Inbound inbound) {

        inbound.setOrgId(SecurityUtils.getUser().getOrgId());
        IPage result = baseMapper.getPage(page, inbound);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String number, String flag, String pageName) {
        //如果 该订单 或 主单下任意订单， 节点状态 财务锁账 = 是 ，则不允许 删除出重
//        LambdaQueryWrapper<LogBean> logWrapper = Wrappers.<LogBean>lambdaQuery();
//        logWrapper.eq(LogBean::getOrgId, SecurityUtils.getUser().getOrgId()).eq(LogBean::getNodeName, "财务锁账");
//        if (flag.equals("awb")) {
//
//            LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
//            orderWrapper.eq(AfOrder::getAwbUuid, number).eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId());
//            List<AfOrder> list = afOrderService.list(orderWrapper);
//            if (list.size() > 0) {
//                List<String> orderUuids = list.stream().map(AfOrder::getOrderUuid).collect(Collectors.toList());
//                logWrapper.in(LogBean::getOrderUuid, orderUuids);
//            } else {
//                throw new RuntimeException("该主单号下无订单，无法删除出重");
//            }
//        } else if (flag.equals("order")) {
//            logWrapper.eq(LogBean::getOrderUuid, number);
//        }
//        logService.list(logWrapper).stream().forEach(logBean -> {
//            if (logBean.getCreatTime() != null) {
//                if (flag.equals("awb")) {
//                    throw new RuntimeException("该主单下有财务锁账的订单，无法删除出重");
//                } else if (flag.equals("order")) {
//                    throw new RuntimeException("该订单已经财务锁账，无法删除出重");
//                }
//            }
//        });
		// 财务锁账已做的 ，不能重复做
		AfOrder order2 = new AfOrder();
		order2.setBusinessScope("AE");
		order2.setOrderUuid(number);
		List<Integer> list2 = afOrderService.getOrderStatus(order2);
		if (list2.size() > 0) {
			if ("操作订单".equals(pageName)) {
        		throw new RuntimeException("已操作完成");
			} else {
				throw new RuntimeException("已经做过财务锁账");
			}
		}

        //删除出重
        LambdaQueryWrapper<Inbound> wrapper = Wrappers.<Inbound>lambdaQuery();
        wrapper.eq(Inbound::getOrgId, SecurityUtils.getUser().getOrgId());
        if (flag.equals("awb")) {
            wrapper.eq(Inbound::getAwbUuid, number);
        } else if (flag.equals("order")) {
            wrapper.eq(Inbound::getOrderUuid, number);
        }
        List<Integer> list = baseMapper.selectList(wrapper).stream().map(Inbound::getInboundId).collect(Collectors.toList());
        baseMapper.delete(wrapper);

        //删除出重文件
        if (list.size() != 0) {
            LambdaQueryWrapper<InboundFiles> inboundFilesWrapper = Wrappers.<InboundFiles>lambdaQuery();
            inboundFilesWrapper.eq(InboundFiles::getOrgId, SecurityUtils.getUser().getOrgId()).in(InboundFiles::getInboundId, list);
            inboundFilesService.remove(inboundFilesWrapper);
        }

        //更新日志
        List<AfOrder> orders = new ArrayList<>();
        if (flag.equals("awb")) {
            LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
            orderWrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId());
            orderWrapper.eq(AfOrder::getAwbUuid, number);
            orders = afOrderService.list(orderWrapper);
        } else if (flag.equals("order")) {
            LambdaQueryWrapper<AfOrder> orderWrapper = Wrappers.<AfOrder>lambdaQuery();
            orderWrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId());
            orderWrapper.eq(AfOrder::getOrderUuid, number);
            AfOrder one = afOrderService.getOne(orderWrapper);
            orders.add(one);
        }

        orders.stream().forEach(order -> {
//            LogBean logBean = new LogBean();
//            logBean.setOrgId(SecurityUtils.getUser().getOrgId());
//            logBean.setOrderUuid(order.getOrderUuid());
//            logService.modifyForDeleteInbound(logBean);

            //添加删除出重操作日子
//            logBean.setPageName("操作出重");
//            logBean.setLogType("受控操作");
//            logBean.setPageFunction("删除出重信息");
//            logBean.setNodeName("受控操作");
//            logBean.setLogRemark(order.getOrderCode());
//            logBean.setBusinessScope("AE");
//            logBean.setOrderNumber(order.getOrderCode());
//            logBean.setAwbUuid(order.getAwbUuid());
//            logBean.setAwbNumber(order.getAwbNumber());
//            logService.saveLog(logBean);
            LogBean logBean = new LogBean();
    		logBean.setPageName(pageName);
    		logBean.setPageFunction("删除出重");
    		logBean.setBusinessScope("AE");
    		logBean.setOrderNumber(order.getOrderCode());
    		logBean.setOrderId(order.getOrderId());
    		logBean.setOrderUuid(order.getOrderUuid());
    		logService.saveLog(logBean);
            //更新订单表
//            LambdaQueryWrapper<LogBean> logBeanWrapper = Wrappers.<LogBean>lambdaQuery();
//            logBeanWrapper.eq(LogBean::getOrgId, SecurityUtils.getUser().getOrgId()).eq(LogBean::getOrderUuid, order.getOrderUuid()).eq(LogBean::getPageName, "订单管理").eq(LogBean::getPageFunction, "新建").isNotNull(LogBean::getCreatTime).orderByDesc(LogBean::getLogId).last("limit 1");
//            LogBean one = logService.getOne(logBeanWrapper);
//            baseMapper.updateOrderWhenDeleteInbound(order.getOrderId(), SecurityUtils.getUser().getOrgId(), one.getNodeName(),UUID.randomUUID().toString());
            String nodeName = "订单创建";
             if(order!=null && order.getAwbId()!=null){
				nodeName = "舱位确认";
			}
            baseMapper.updateOrderWhenDeleteInbound(order.getOrderId(), SecurityUtils.getUser().getOrgId(), nodeName,UUID.randomUUID().toString());
        });
    }

    @Override
    public List<Inbound> inboundView(String number, String flag) {
        LambdaQueryWrapper<AfOrder> wrapper = Wrappers.<AfOrder>lambdaQuery();
        if (flag.equals("awb")) {
            wrapper.eq(AfOrder::getAwbId, number).eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId()).orderByAsc(AfOrder::getOrderCode);
            List<AfOrder> afOrders = afOrderService.list(wrapper);
            List<Inbound> inbounds = new ArrayList<>();
            afOrders.stream().forEach(afOrder -> {
                LambdaQueryWrapper<Inbound> inboundWrapper = Wrappers.<Inbound>lambdaQuery();
                inboundWrapper.eq(Inbound::getOrderId, afOrder.getOrderId());
                Inbound inbound = baseMapper.selectOne(inboundWrapper);
                if (inbound != null) {
                    if (StrUtil.isNotBlank(afOrder.getAwbNumber())) {
                        inbound.setAwbNumber(afOrder.getAwbNumber());
                    } else {
                        inbound.setAwbNumber(afOrder.getOrderCode());
                    }
                    inbound.setOrderCode(afOrder.getOrderCode());
                    inbound.setCustomerNumber(afOrder.getCustomerNumber());
                    inbound.setInboundChargeWeight(inbound.getOrderChargeWeight());
                    inbound.setInboundGrossWeight(inbound.getOrderGrossWeight());
                    inbound.setInboundPieces(inbound.getOrderPieces());
                    inbound.setInboundVolume(inbound.getOrderVolume());
                    BigDecimal decimal = new BigDecimal(inbound.getOrderVolume()).multiply(new BigDecimal(1000000)).divide(new BigDecimal(6000), 1, BigDecimal.ROUND_HALF_UP);
                    inbound.setInboundVolumeWeight(decimal.doubleValue());
                    inbounds.add(inbound);
                } else {
                    Inbound inbound1 = new Inbound();
                    if (StrUtil.isNotBlank(afOrder.getAwbNumber())) {
                        inbound1.setAwbNumber(afOrder.getAwbNumber());
                    } else {
                        inbound1.setAwbNumber(afOrder.getOrderCode());
                    }
                    inbound1.setOrderCode(afOrder.getOrderCode());
                    inbound1.setCustomerNumber(afOrder.getCustomerNumber());
                    if (StrUtil.isNotBlank(afOrder.getPlanDimensions())) {
                        inbound1.setOrderSize(afOrder.getPlanDimensions());
                        inbound1.setInboundGrossWeight(afOrder.getPlanWeight() == null ? null : afOrder.getPlanWeight().doubleValue());
                    }

                    inbounds.add(inbound1);
                }
            });
            Inbound inbound = new Inbound();
//            inbound.setOrderCode(afOrders.get(0).getAwbNumber());
            inbound.setAwbNumber(afOrders.get(0).getAwbNumber());
            inbound.setAwbUuid(afOrders.get(0).getAwbUuid());
            inbound.setAwbId(Integer.parseInt(number));
            inbounds.add(0, inbound);
            return inbounds;
        } else if (flag.equals("order")) {
            wrapper.eq(AfOrder::getOrderCode, number).eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId());
            AfOrder afOrder = afOrderService.getOne(wrapper);
            ArrayList<Inbound> inbounds = new ArrayList<>();
            LambdaQueryWrapper<Inbound> inboundWrapper = Wrappers.<Inbound>lambdaQuery();
            inboundWrapper.eq(Inbound::getOrderId, afOrder.getOrderId());
            Inbound inboundExist = baseMapper.selectOne(inboundWrapper);
            if (inboundExist == null) {
                Inbound inbound = new Inbound();
                if (StrUtil.isNotBlank(afOrder.getAwbNumber())) {
                    inbound.setAwbNumber(afOrder.getAwbNumber());
                } else {
                    inbound.setAwbNumber(afOrder.getOrderCode());
                }
                inbound.setOrderCode(afOrder.getOrderCode());
                inbound.setCustomerNumber(afOrder.getCustomerNumber());
                inbound.setIfAwb(false);
                if (StrUtil.isNotBlank(afOrder.getPlanDimensions())) {
                    inbound.setOrderSize(afOrder.getPlanDimensions());
                    //inbound.setInboundGrossWeight(afOrder.getPlanWeight() == null ? null : afOrder.getPlanWeight().doubleValue());
                }
                inbound.setInboundGrossWeight(afOrder.getPlanWeight() == null ? null : afOrder.getPlanWeight().doubleValue());
                inbound.setInboundChargeWeight(afOrder.getPlanChargeWeight());
                inbound.setInboundPieces(afOrder.getPlanPieces());
                inbound.setInboundVolume(afOrder.getPlanVolume());
                BigDecimal decimal = new BigDecimal(afOrder.getPlanVolume()).multiply(new BigDecimal(1000000)).divide(new BigDecimal(6000), 1, BigDecimal.ROUND_HALF_UP);
                inbound.setInboundVolumeWeight(decimal.doubleValue());
                inbounds.add(inbound);
            } else {
                if (StrUtil.isNotBlank(afOrder.getAwbNumber())) {
                    inboundExist.setAwbNumber(afOrder.getAwbNumber());
                } else {
                    inboundExist.setAwbNumber(afOrder.getOrderCode());
                }
                inboundExist.setOrderCode(afOrder.getOrderCode());
                inboundExist.setCustomerNumber(afOrder.getCustomerNumber());
                inboundExist.setInboundChargeWeight(inboundExist.getOrderChargeWeight());
                inboundExist.setInboundGrossWeight(inboundExist.getOrderGrossWeight());
                inboundExist.setInboundPieces(inboundExist.getOrderPieces());
                inboundExist.setInboundVolume(inboundExist.getOrderVolume());
                BigDecimal decimal = new BigDecimal(inboundExist.getOrderVolume()).multiply(new BigDecimal(1000000)).divide(new BigDecimal(6000), 1, BigDecimal.ROUND_HALF_UP);
                inboundExist.setInboundVolumeWeight(decimal.doubleValue());
                inboundExist.setIfAwb(false);
                inbounds.add(inboundExist);
            }
            return inbounds;
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInbound(List<Inbound> data) {
        if (data.size() == 1) {
            //订单出重保存
            Inbound inbound = data.get(0);
            //保存出重表
            LambdaQueryWrapper<AfOrder> wrapper = Wrappers.<AfOrder>lambdaQuery();
            wrapper.eq(AfOrder::getOrderCode, inbound.getOrderCode());
            wrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId());
            AfOrder one = afOrderService.getOne(wrapper);

            LambdaQueryWrapper<Inbound> inboundWrapper = Wrappers.<Inbound>lambdaQuery();
            inboundWrapper.eq(Inbound::getOrderId, one.getOrderId());
            Inbound selectOne = baseMapper.selectOne(inboundWrapper);
            if (selectOne != null) {
                throw new RuntimeException("该订单已出重");
            }
            inbound.setOrgId(SecurityUtils.getUser().getOrgId());
            inbound.setOrderId(one.getOrderId());
            inbound.setOrderUuid(one.getOrderUuid());
            inbound.setOrderChargeWeight(inbound.getInboundChargeWeight());
            inbound.setOrderGrossWeight(inbound.getInboundGrossWeight());
            inbound.setOrderPieces(inbound.getInboundPieces());
            inbound.setOrderVolume(inbound.getInboundVolume());
            inbound.setOrderSize(inbound.getOrderSize());

            inbound.setCreateTime(LocalDateTime.now());
            inbound.setCreatorId(SecurityUtils.getUser().getId());
            inbound.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());

            inbound.setEditorId(SecurityUtils.getUser().getId());
            inbound.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            inbound.setEditTime(LocalDateTime.now());

            baseMapper.insert(inbound);

            //更新日志节点

//            LambdaQueryWrapper<LogBean> logWrapper = Wrappers.<LogBean>lambdaQuery();
//            logWrapper.eq(LogBean::getOrderNumber, inbound.getOrderCode());
//            logWrapper.eq(LogBean::getOrgId, SecurityUtils.getUser().getOrgId());
//            logWrapper.eq(LogBean::getNodeName, "货物出重");
//            LogBean logBean = new LogBean();
//            logBean.setCreatTime(LocalDateTime.now());
//            logBean.setCreatorId(SecurityUtils.getUser().getId());
//            logBean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
//            logService.update(logBean, logWrapper);
            LogBean logBean = new LogBean();
//    		logBean.setPageName("操作出重");
    		logBean.setPageName(inbound.getPageName());
    		logBean.setPageFunction("保存出重");
    		logBean.setBusinessScope("AE");
    		logBean.setOrderNumber(one.getOrderCode());
    		logBean.setLogRemark(this.getLogRemarkSave(one, inbound));
    		logBean.setOrderId(one.getOrderId());
    		logBean.setOrderUuid(one.getOrderUuid());
    		logService.saveLog(logBean);
            //更新订单表
//            LambdaQueryWrapper<LogBean> logQurayWrapper = Wrappers.<LogBean>lambdaQuery();
//            logQurayWrapper.eq(LogBean::getOrgId,SecurityUtils.getUser().getOrgId()).eq(LogBean::getOrderNumber,inbound.getOrderCode()).eq(LogBean::getPageFunction,"新建").eq(LogBean::getPageName, "订单管理").isNotNull(LogBean::getCreatTime).orderByDesc(LogBean::getLogId).last("limit 1");
//            LogBean log = logService.getOne(logQurayWrapper);

            one.setConfirmChargeWeight(inbound.getInboundChargeWeight());
            one.setConfirmPieces(inbound.getInboundPieces());
            one.setConfirmVolume(inbound.getInboundVolume());
            one.setConfirmWeight(new BigDecimal(inbound.getInboundGrossWeight()));
            one.setOrderStatus("货物出重");
            one.setRowUuid(UUID.randomUUID().toString());
            one.setConfirmDensity(inbound.getOrderDimensions());
            one.setConfirmDimensions(inbound.getOrderSize());

            afOrderService.update(one, wrapper);

            //修改签单信息
            //查询签单状态
            AfRountingSign ars = baseMapper.getSignStateByOrderId(one.getOrderId());

            if(inbound.getRountingSign() != null && inbound.getRountingSign() == 1){
                if(ars!= null && ars.getSignState() == 0 && one.getBusinessProduct() != null && inbound.getRountingSignBusinessProduct().contains(one.getBusinessProduct())){
                    AfRountingSign afRountingSign = new AfRountingSign();
                    afRountingSign.setRountingSignId(ars.getRountingSignId());
//                    afRountingSign.setEditorId(SecurityUtils.getUser().getId());
//                    afRountingSign.setEdit_time(new Date());
//                    afRountingSign.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    //查询当前汇率
                    BigDecimal currencyRate = null;
                    if(!"".equals(one.getMsrCurrecnyCode())){
                        currencyRate = afOrderMapper.getCurrencyRate(SecurityUtils.getUser().getOrgId(),one.getMsrCurrecnyCode());
                    }
                    if(one.getMsrUnitprice() != null){
                        if(currencyRate != null){
                            BigDecimal msrUnitprice = BigDecimal.valueOf(one.getMsrUnitprice()).multiply(currencyRate);
                            afRountingSign.setMsrUnitprice(msrUnitprice.setScale(2, BigDecimal.ROUND_HALF_UP));
                            BigDecimal msrAmountWriteoff = BigDecimal.valueOf(one.getConfirmChargeWeight() == null ? inbound.getInboundChargeWeight() : one.getConfirmChargeWeight()).multiply(msrUnitprice);
                            afRountingSign.setMsrAmountWriteoff(msrAmountWriteoff.setScale(2, BigDecimal.ROUND_HALF_UP));
                        }else{
                            afRountingSign.setMsrUnitprice(new BigDecimal("0.00"));
                            afRountingSign.setMsrAmountWriteoff(new BigDecimal("0.00"));
                        }
                        afRountingSign.setIncomeWeight(one.getConfirmChargeWeight() == null ? inbound.getInboundChargeWeight() : one.getConfirmChargeWeight());
                    }else if(one.getMsrAmount() != null){
                        if(currencyRate != null){
                            BigDecimal msrUnitprice = BigDecimal.valueOf(one.getMsrAmount()).multiply(currencyRate);
                            afRountingSign.setMsrUnitprice(msrUnitprice.setScale(2, BigDecimal.ROUND_HALF_UP));
                            afRountingSign.setMsrAmountWriteoff(msrUnitprice.setScale(2, BigDecimal.ROUND_HALF_UP));
                        }else{
                            afRountingSign.setMsrUnitprice(new BigDecimal("0.00"));
                            afRountingSign.setMsrAmountWriteoff(new BigDecimal("0.00"));
                        }
                        afRountingSign.setIncomeWeight(1.00);
                    }else{
                        afRountingSign.setMsrUnitprice(new BigDecimal("0.00"));
                        afRountingSign.setMsrAmountWriteoff(new BigDecimal("0.00"));
                        afRountingSign.setIncomeWeight(one.getConfirmChargeWeight() == null ? inbound.getInboundChargeWeight() : one.getConfirmChargeWeight());
                    }
                    afRountingSignMapper.updateById(afRountingSign);
                }
            }else {
                if(ars!= null){
                    AfRountingSign afRountingSign = new AfRountingSign();
                    afRountingSign.setRountingSignId(ars.getRountingSignId());
                    afRountingSign.setSignState(0);
                    afRountingSign.setRoutingPersonId(null);
                    afRountingSign.setRoutingPersonName(null);
//                    afRountingSign.setEditorId(SecurityUtils.getUser().getId());
//                    afRountingSign.setEdit_time(new Date());
//                    afRountingSign.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    //查询cost表有没有干线运输-空运费
                    BigDecimal costFunctionalAmount = null;
                    costFunctionalAmount = afOrderMapper.getAostFunctionalAmount(SecurityUtils.getUser().getOrgId(),one.getOrderId());
                    afRountingSign.setMsrUnitprice(costFunctionalAmount == null? new BigDecimal("0.00") : costFunctionalAmount);
                    afRountingSign.setMsrAmountWriteoff(costFunctionalAmount == null? new BigDecimal("0.00") : costFunctionalAmount);
                    afRountingSign.setIncomeWeight(1.00);
                    afRountingSignMapper.updateById(afRountingSign);
                }
            }


        } else {
            //主单出重保存
            Inbound awb = data.get(0);
            LocalDateTime now = LocalDateTime.now();
            //编辑订单出重列表
            for (int i = 1; i < data.size(); i++) {
                Inbound inbound = data.get(i);
                //保存出重表
                LambdaQueryWrapper<AfOrder> wrapper = Wrappers.<AfOrder>lambdaQuery();
                wrapper.eq(AfOrder::getOrderCode, inbound.getOrderCode());
                wrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId());
                AfOrder one = afOrderService.getOne(wrapper);

                LambdaQueryWrapper<Inbound> inboundWrapper = Wrappers.<Inbound>lambdaQuery();
                inboundWrapper.eq(Inbound::getOrderId, one.getOrderId());
                Inbound selectOne = baseMapper.selectOne(inboundWrapper);


                inbound.setOrgId(SecurityUtils.getUser().getOrgId());
                inbound.setOrderId(one.getOrderId());
                inbound.setOrderUuid(one.getOrderUuid());
                inbound.setOrderChargeWeight(inbound.getInboundChargeWeight());
                inbound.setOrderGrossWeight(inbound.getInboundGrossWeight());
                inbound.setOrderPieces(inbound.getInboundPieces());
                inbound.setOrderVolume(inbound.getInboundVolume());
                inbound.setOrderSize(inbound.getOrderSize());
                inbound.setAwbChargeWeight(awb.getInboundChargeWeight());
                inbound.setAwbGrossWeight(awb.getInboundGrossWeight());
                inbound.setAwbPieces(awb.getInboundPieces());
                inbound.setAwbVolume(awb.getInboundVolume());
                inbound.setAwbUuid(awb.getAwbUuid());
                inbound.setAwbId(awb.getAwbId());


                inbound.setEditorId(SecurityUtils.getUser().getId());
                inbound.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                inbound.setEditTime(now);
                if (selectOne == null) {
                    //新建
                    inbound.setCreateTime(now);
                    inbound.setCreatorId(SecurityUtils.getUser().getId());
                    inbound.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    baseMapper.insert(inbound);
                } else {
                    //修改
                    baseMapper.update(inbound, inboundWrapper);
                }

                //更新日志节点

                LambdaQueryWrapper<LogBean> logWrapper = Wrappers.<LogBean>lambdaQuery();
                logWrapper.eq(LogBean::getOrderNumber, inbound.getOrderCode());
                logWrapper.eq(LogBean::getOrgId, SecurityUtils.getUser().getOrgId());
                logWrapper.eq(LogBean::getNodeName, "货物出重");
                
                LogBean logBean = new LogBean();
                logBean.setLogRemark(this.getLogRemarkSave(one, inbound));
                logBean.setCreatTime(LocalDateTime.now());
                logBean.setCreatorId(SecurityUtils.getUser().getId());
                logBean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                logService.update(logBean, logWrapper);

                //更新订单表
                LambdaQueryWrapper<LogBean> logQurayWrapper = Wrappers.<LogBean>lambdaQuery();
                logQurayWrapper.eq(LogBean::getOrgId,SecurityUtils.getUser().getOrgId()).eq(LogBean::getOrderNumber,inbound.getOrderCode()).eq(LogBean::getPageFunction,"新建").eq(LogBean::getPageName, "订单管理").isNotNull(LogBean::getCreatTime).orderByDesc(LogBean::getLogId).last("limit 1");
                LogBean log = logService.getOne(logQurayWrapper);

                one.setConfirmChargeWeight(inbound.getInboundChargeWeight());
                one.setConfirmPieces(inbound.getInboundPieces());
                one.setConfirmVolume(inbound.getInboundVolume());
                one.setConfirmWeight(new BigDecimal(inbound.getInboundGrossWeight()));
                one.setOrderStatus(log.getNodeName());

                afOrderService.update(one, wrapper);
            }

        }
    }


    @Override
    public List<Inbound> detailView(String number, String flag) {
        LambdaQueryWrapper<AfOrder> wrapper = Wrappers.<AfOrder>lambdaQuery();
        if (flag.equals("awb")) {
            //查看是否出重
            LambdaQueryWrapper<Inbound> queryWrapper = Wrappers.<Inbound>lambdaQuery();
            queryWrapper.eq(Inbound::getOrgId, SecurityUtils.getUser().getOrgId()).eq(Inbound::getAwbId, number);
            List<Inbound> list = baseMapper.selectList(queryWrapper);
            if (list == null || list.size() == 0) {
                return null;
            }
            wrapper.eq(AfOrder::getAwbId, number).eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId()).orderByAsc(AfOrder::getOrderCode);
            List<AfOrder> afOrders = afOrderService.list(wrapper);
            List<Inbound> inbounds = new ArrayList<>();
            ArrayList<BigDecimal> volumeWeightList = new ArrayList<>();
            ArrayList<String> sizeList = new ArrayList<>();
            afOrders.stream().forEach(afOrder -> {
                LambdaQueryWrapper<Inbound> inboundWrapper = Wrappers.<Inbound>lambdaQuery();
                inboundWrapper.eq(Inbound::getOrderId, afOrder.getOrderId());
                Inbound inbound = baseMapper.selectOne(inboundWrapper);
                inbound.setOrderCode(afOrder.getOrderCode());
                inbound.setCustomerNumber(afOrder.getCustomerNumber());
                inbound.setInboundChargeWeight(inbound.getOrderChargeWeight());
                inbound.setInboundGrossWeight(inbound.getOrderGrossWeight());
                inbound.setInboundPieces(inbound.getOrderPieces());
                inbound.setInboundVolume(inbound.getOrderVolume());
                BigDecimal decimal = new BigDecimal(inbound.getOrderVolume()).multiply(new BigDecimal(1000000)).divide(new BigDecimal(6000), 1, BigDecimal.ROUND_HALF_UP);
                inbound.setInboundVolumeWeight(decimal.doubleValue());
                volumeWeightList.add(decimal);
                sizeList.add(inbound.getOrderSize());
                inbounds.add(inbound);
            });
            BigDecimal sumWolumeWeight = new BigDecimal(0.0);
            for (BigDecimal volumeWeight :
                    volumeWeightList) {
                sumWolumeWeight = sumWolumeWeight.add(volumeWeight);
            }
            StringBuffer orderSizeBuffer = new StringBuffer();
            for (String size :
                    sizeList) {
                if (StrUtil.isBlank(orderSizeBuffer.toString())) {
                    orderSizeBuffer.append(size);
                } else {
                    orderSizeBuffer.append(";").append(size);
                }
            }

            LambdaQueryWrapper<Inbound> inboundWrapper = Wrappers.<Inbound>lambdaQuery();
            inboundWrapper.eq(Inbound::getOrderId, afOrders.get(0).getOrderId());
            Inbound inbound1 = baseMapper.selectOne(inboundWrapper);
            Inbound inbound = new Inbound();
            inbound.setOrderCode(afOrders.get(0).getAwbNumber());
            inbound.setInboundVolume(inbound1.getAwbVolume());
            inbound.setInboundPieces(inbound1.getAwbPieces());
            inbound.setInboundGrossWeight(inbound1.getAwbGrossWeight());
            inbound.setInboundChargeWeight(inbound1.getAwbChargeWeight());
            inbound.setOrderSize(orderSizeBuffer.toString());
            inbound.setInboundVolumeWeight(sumWolumeWeight.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            inbounds.add(0, inbound);
            return inbounds;
        } else if (flag.equals("order")) {

            wrapper.eq(AfOrder::getOrderCode, number).eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId());
            AfOrder afOrder = afOrderService.getOne(wrapper);
            ArrayList<Inbound> inbounds = new ArrayList<>();
            LambdaQueryWrapper<Inbound> inboundWrapper = Wrappers.<Inbound>lambdaQuery();
            inboundWrapper.eq(Inbound::getOrderId, afOrder.getOrderId());
            Inbound inbound = baseMapper.selectOne(inboundWrapper);
            //查看是否出重
            if (inbound == null) {
                return null;
            }
            if(StrUtil.isNotBlank(afOrder.getAwbNumber())){
                inbound.setAwbNumber(afOrder.getAwbNumber());
            }else{
                inbound.setAwbNumber(afOrder.getOrderCode());
            }
            inbound.setOrderCode(afOrder.getOrderCode());
            inbound.setCustomerNumber(afOrder.getCustomerNumber());
            inbound.setIfAwb(false);
            inbound.setInboundChargeWeight(inbound.getOrderChargeWeight());
            inbound.setInboundGrossWeight(inbound.getOrderGrossWeight());
            inbound.setInboundPieces(inbound.getOrderPieces());
            inbound.setInboundVolume(inbound.getOrderVolume());
            inbound.setInboundVolumeWeight(new BigDecimal(inbound.getOrderVolume()).multiply(new BigDecimal(1000000)).divide(new BigDecimal(6000), 1, BigDecimal.ROUND_HALF_UP).doubleValue());
            inbounds.add(inbound);
            return inbounds;
        }
        return null;
    }

    @Override
    public List<Inbound> detailView2(String awb_uuid, String order_uuid) {
        return baseMapper.selectInbounds(SecurityUtils.getUser().getOrgId(), awb_uuid, order_uuid);
    }

	@Override
	public void modifyInbound(Inbound inbound) {
		
        LambdaQueryWrapper<Inbound> wrapper = Wrappers.<Inbound>lambdaQuery();
        wrapper.eq(Inbound::getInboundId, inbound.getInboundId());
       // wrapper.eq(Inbound::getOrgId, SecurityUtils.getUser().getOrgId());
        Inbound one = baseMapper.selectOne(wrapper);
        
//        LambdaQueryWrapper<LogBean> logWrapper = Wrappers.<LogBean>lambdaQuery();
//        logWrapper.eq(LogBean::getOrgId, SecurityUtils.getUser().getOrgId()).eq(LogBean::getNodeName, "财务锁账");
//		logWrapper.eq(LogBean::getOrderUuid, one.getOrderUuid());	
//		logService.list(logWrapper).stream().forEach(logBean -> {
//            if (logBean.getCreatTime() != null) {
//               throw new RuntimeException("该订单已经财务锁账，无法编辑出重");
//            }
//        });
		 
		//财务锁账已做的 ，不能重复做
		AfOrder order = new AfOrder();
		order.setBusinessScope("AE");
		order.setOrderUuid(one.getOrderUuid());
        List<Integer> list = afOrderService.getOrderStatus(order);
        if (list.size() > 0) {
        	if ("操作订单".equals(inbound.getPageName())) {
        		throw new RuntimeException("已操作完成");
			} else {
				throw new RuntimeException("已经做过财务锁账");
			}
            
        }
        //编辑出重
        if(one!=null) {
        	one.setOrderSize(inbound.getOrderSize());
        	one.setOrderPieces(inbound.getInboundPieces());
        	one.setOrderGrossWeight(inbound.getInboundGrossWeight());
        	one.setOrderVolume(inbound.getInboundVolume());
        	//one.setInboundVolumeWeight(inbound.getInboundVolumeWeight());
        	one.setOrderChargeWeight(inbound.getInboundChargeWeight());
        	one.setEditorId(SecurityUtils.getUser().getId());
        	one.setEditorName(SecurityUtils.getUser().getUsername());
        	one.setEditTime(LocalDateTime.now());
        	one.setOrderDimensions(inbound.getOrderDimensions());
        	baseMapper.update(one, wrapper);
        	
        	LambdaQueryWrapper<AfOrder> wrapperOrder = Wrappers.<AfOrder>lambdaQuery();
            wrapperOrder.eq(AfOrder::getOrderUuid, one.getOrderUuid());
            wrapperOrder.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId());
            AfOrder oneOrder = afOrderService.getOne(wrapperOrder);
            
            //添加日志
//            LogBean logBean = new LogBean();
//            logBean.setOrgId(SecurityUtils.getUser().getOrgId());
//            logBean.setOrderUuid(oneOrder.getOrderUuid());
//            logBean.setPageName("操作出重");
//            logBean.setLogType("受控操作");
//            logBean.setPageFunction("编辑出重信息");
//            logBean.setNodeName("受控操作");
//            logBean.setLogRemark(oneOrder.getOrderCode());
//            logBean.setBusinessScope("AE");
//            logBean.setOrderNumber(oneOrder.getOrderCode());
//            logBean.setAwbUuid(oneOrder.getAwbUuid());
//            logBean.setAwbNumber(oneOrder.getAwbNumber());
//            logService.saveLog(logBean);
            LogBean logBean = new LogBean();
    		logBean.setPageName(inbound.getPageName());
    		logBean.setPageFunction("修改出重");
    		logBean.setLogRemark(this.getLogRemark(oneOrder, inbound));
    		logBean.setBusinessScope("AE");
    		logBean.setOrderNumber(oneOrder.getOrderCode());
    		logBean.setOrderId(oneOrder.getOrderId());
    		logBean.setOrderUuid(oneOrder.getOrderUuid());
    		logService.saveLog(logBean);
            //更新订单表
//            LambdaQueryWrapper<LogBean> logQurayWrapper = Wrappers.<LogBean>lambdaQuery();
//            logQurayWrapper.eq(LogBean::getOrgId,SecurityUtils.getUser().getOrgId()).eq(LogBean::getOrderNumber,oneOrder.getOrderCode()).eq(LogBean::getPageFunction,"新建").eq(LogBean::getPageName, "订单管理").isNotNull(LogBean::getCreatTime).orderByDesc(LogBean::getLogId).last("limit 1");
//            LogBean log = logService.getOne(logQurayWrapper);
            
            oneOrder.setConfirmChargeWeight(inbound.getInboundChargeWeight());
            oneOrder.setConfirmPieces(inbound.getInboundPieces());
            oneOrder.setConfirmVolume(inbound.getInboundVolume());
            oneOrder.setConfirmWeight(new BigDecimal(inbound.getInboundGrossWeight()));
//            oneOrder.setOrderStatus(log.getNodeName());
            oneOrder.setEditorId(SecurityUtils.getUser().getId());
            oneOrder.setEditorName(SecurityUtils.getUser().getUsername());
            oneOrder.setEditTime(new Date());
            oneOrder.setRowUuid(UUID.randomUUID().toString());
            oneOrder.setConfirmDensity(inbound.getOrderDimensions());
            oneOrder.setConfirmDimensions(inbound.getOrderSize());
            afOrderService.update(oneOrder, wrapperOrder);

            //修改签单信息
            //查询签单状态
            AfRountingSign ars = baseMapper.getSignStateByOrderId(one.getOrderId());

            if(inbound.getRountingSign() != null && inbound.getRountingSign() == 1){
                if(ars!= null && ars.getSignState() == 0 && oneOrder.getBusinessProduct() != null && inbound.getRountingSignBusinessProduct().contains(oneOrder.getBusinessProduct())){
                    AfRountingSign afRountingSign = new AfRountingSign();
                    afRountingSign.setRountingSignId(ars.getRountingSignId());
//                    afRountingSign.setEditorId(SecurityUtils.getUser().getId());
//                    afRountingSign.setEdit_time(new Date());
//                    afRountingSign.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    //查询当前汇率
                    BigDecimal currencyRate = null;
                    if(!"".equals(oneOrder.getMsrCurrecnyCode())){
                        currencyRate = afOrderMapper.getCurrencyRate(SecurityUtils.getUser().getOrgId(),oneOrder.getMsrCurrecnyCode());
                    }
                    if(oneOrder.getMsrUnitprice() != null){
                        if(currencyRate != null){
                            BigDecimal msrUnitprice = BigDecimal.valueOf(oneOrder.getMsrUnitprice()).multiply(currencyRate);
                            afRountingSign.setMsrUnitprice(msrUnitprice.setScale(2, BigDecimal.ROUND_HALF_UP));
                            BigDecimal msrAmountWriteoff = BigDecimal.valueOf(oneOrder.getConfirmChargeWeight() == null ? inbound.getInboundChargeWeight() : oneOrder.getConfirmChargeWeight()).multiply(msrUnitprice);
                            afRountingSign.setMsrAmountWriteoff(msrAmountWriteoff.setScale(2, BigDecimal.ROUND_HALF_UP));
                        }else{
                            afRountingSign.setMsrUnitprice(new BigDecimal("0.00"));
                            afRountingSign.setMsrAmountWriteoff(new BigDecimal("0.00"));
                        }
                        afRountingSign.setIncomeWeight(oneOrder.getConfirmChargeWeight() == null ? inbound.getInboundChargeWeight() : oneOrder.getConfirmChargeWeight());
                    }else if(oneOrder.getMsrAmount() != null){
                        if(currencyRate != null){
                            BigDecimal msrUnitprice = BigDecimal.valueOf(oneOrder.getMsrAmount()).multiply(currencyRate);
                            afRountingSign.setMsrUnitprice(msrUnitprice.setScale(2, BigDecimal.ROUND_HALF_UP));
                            afRountingSign.setMsrAmountWriteoff(msrUnitprice.setScale(2, BigDecimal.ROUND_HALF_UP));
                        }else{
                            afRountingSign.setMsrUnitprice(new BigDecimal("0.00"));
                            afRountingSign.setMsrAmountWriteoff(new BigDecimal("0.00"));
                        }
                        afRountingSign.setIncomeWeight(1.00);
                    }else{
                        afRountingSign.setMsrUnitprice(new BigDecimal("0.00"));
                        afRountingSign.setMsrAmountWriteoff(new BigDecimal("0.00"));
                        afRountingSign.setIncomeWeight(oneOrder.getConfirmChargeWeight() == null ? inbound.getInboundChargeWeight() : oneOrder.getConfirmChargeWeight());
                    }
                    afRountingSignMapper.updateById(afRountingSign);
                }
            }else {
                if(ars!= null){
                    AfRountingSign afRountingSign = new AfRountingSign();
                    afRountingSign.setRountingSignId(ars.getRountingSignId());
                    afRountingSign.setSignState(0);
                    afRountingSign.setRoutingPersonId(null);
                    afRountingSign.setRoutingPersonName(null);
//                    afRountingSign.setEditorId(SecurityUtils.getUser().getId());
//                    afRountingSign.setEdit_time(new Date());
//                    afRountingSign.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    //查询cost表有没有干线运输-空运费
                    BigDecimal costFunctionalAmount = null;
                    costFunctionalAmount = afOrderMapper.getAostFunctionalAmount(SecurityUtils.getUser().getOrgId(),one.getOrderId());
                    afRountingSign.setMsrUnitprice(costFunctionalAmount == null? new BigDecimal("0.00") : costFunctionalAmount);
                    afRountingSign.setMsrAmountWriteoff(costFunctionalAmount == null? new BigDecimal("0.00") : costFunctionalAmount);
                    afRountingSign.setIncomeWeight(1.00);
                    afRountingSignMapper.updateById(afRountingSign);
                }
            }
        }
		
	}
	
	private String getLogRemark(AfOrder order, Inbound bean) {
        StringBuffer logremark = new StringBuffer();

        String planPieces = this.getStr("" + order.getConfirmPieces(), "" + bean.getInboundPieces());
        logremark.append(StringUtils.isBlank(planPieces) ? "" : "件数：" + planPieces);
        String planWeight = this.getStr(String.valueOf(order.getConfirmWeight()), this.fmtMicrometer2(String.valueOf(bean.getInboundGrossWeight())));
        logremark.append(StringUtils.isBlank(planWeight) ? "" : "毛重：" + planWeight);
        String planVolume = this.getStr("" + order.getConfirmVolume(), "" + bean.getInboundVolume());
        logremark.append(StringUtils.isBlank(planVolume) ? "" : "体积：" + planVolume);
        String planChargeWeight = this.getStr("" + order.getConfirmChargeWeight(), "" + bean.getInboundChargeWeight());
        logremark.append(StringUtils.isBlank(planChargeWeight) ? "" : "计重：" + planChargeWeight);
       
        return logremark.toString();
    }
	private String getLogRemarkSave(AfOrder order, Inbound bean) {
        StringBuffer logremark = new StringBuffer();

        String planPieces = this.getStrSave("" + order.getPlanPieces(), "" + bean.getInboundPieces());
        logremark.append(StringUtils.isBlank(planPieces) ? "" : "件数：" + planPieces);
        String planWeight = this.getStrSave(String.valueOf(order.getPlanWeight()), this.fmtMicrometer2(String.valueOf(bean.getInboundGrossWeight())));
        logremark.append(StringUtils.isBlank(planWeight) ? "" : "毛重：" + planWeight);
        String planVolume = this.getStrSave("" + order.getPlanVolume(), "" + bean.getInboundVolume());
        logremark.append(StringUtils.isBlank(planVolume) ? "" : "体积：" + planVolume);
        String planChargeWeight = this.getStrSave("" + order.getPlanChargeWeight(), "" + bean.getInboundChargeWeight());
        logremark.append(StringUtils.isBlank(planChargeWeight) ? "" : "计重：" + planChargeWeight);
       
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
	private String getStrSave(String str1, String str2) {
        String str = "";
        if (StringUtils.isBlank(str1) || "null".equals(str1)) {
            str1 = "-";
        }
        if (StringUtils.isBlank(str2) || "null".equals(str2)) {
            str2 = "-";
        }
        str = str1 + " / " + str2;
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
    }
}
