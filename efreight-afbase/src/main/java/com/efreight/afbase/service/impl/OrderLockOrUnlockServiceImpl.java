package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.dao.OrderLockOrUnlockMapper;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.entity.exportExcel.OrderLockOrUnlockExcel;
import com.efreight.afbase.service.*;
import com.efreight.afbase.utils.FieldValUtils;
import com.efreight.afbase.utils.PDFUtils;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.core.utils.WebUtils;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderLockOrUnlockServiceImpl implements OrderLockOrUnlockService {

    private final OrderLockOrUnlockMapper orderLockOrUnlockMapper;
    private final AfOrderService afOrderService;
    private final ScOrderService scOrderService;
    private final LogService logService;
    private final ScLogService scLogService;

    private final TcOrderService tcOrderService;

    private final TcLogService tcLogService;
    private final LcOrderService lcOrderService;
    private final LcLogService lcLogService;

    private final IoOrderService ioOrderService;
    private final IoLogService ioLogService;

    @Override
    public IPage page(Page page, OrderLockOrUnlock orderLockOrUnlock) {
        orderLockOrUnlock.setOrgId(SecurityUtils.getUser().getOrgId());
        if (StrUtil.isBlank(orderLockOrUnlock.getBusinessScope())) {
            throw new RuntimeException("请选择业务范畴");
        }
        if (!StrUtil.isBlank(orderLockOrUnlock.getCoopName())) {
            orderLockOrUnlock.setCoopName(orderLockOrUnlock.getCoopName().toUpperCase());
        }
        if (!StrUtil.isBlank(orderLockOrUnlock.getOrderCode())) {
            orderLockOrUnlock.setOrderCode(orderLockOrUnlock.getOrderCode().toUpperCase());
        }
        if (!StrUtil.isBlank(orderLockOrUnlock.getFlightNo())) {
            orderLockOrUnlock.setFlightNo(orderLockOrUnlock.getFlightNo().toUpperCase());
        }
        IPage<OrderLockOrUnlock> iPage = null;
        if (orderLockOrUnlock.getBusinessScope().startsWith("A")) {
            iPage = orderLockOrUnlockMapper.pageForAF(page, orderLockOrUnlock);
        } else if (orderLockOrUnlock.getBusinessScope().startsWith("S")) {
            iPage = orderLockOrUnlockMapper.pageForSC(page, orderLockOrUnlock);
        } else if (orderLockOrUnlock.getBusinessScope().startsWith("T")) {
            iPage = orderLockOrUnlockMapper.pageForTC(page, orderLockOrUnlock);
        } else if (orderLockOrUnlock.getBusinessScope().startsWith("L")) {
            iPage = orderLockOrUnlockMapper.pageForLC(page, orderLockOrUnlock);
        } else if (orderLockOrUnlock.getBusinessScope().equals("IO")) {
            iPage = orderLockOrUnlockMapper.pageForIO(page, orderLockOrUnlock);
        }
        if (iPage != null && iPage.getRecords().size() > 0) {
            iPage.getRecords().stream().forEach(order -> {
                //设置收入完成和成本完成（排序使用）
                if (order.getIncomeFinishStatus() == true) {
                    order.setIncomeFinishStatusForSort(2);
                } else if (org.apache.commons.lang.StringUtils.isNotBlank(order.getIncomeStatus()) && !"未录收入".equals(order.getIncomeStatus())) {
                    order.setIncomeFinishStatusForSort(1);
                } else {
                    order.setIncomeFinishStatusForSort(0);
                }
                if (order.getCostFinishStatus() == true) {
                    order.setCostFinishStatusForSort(2);
                } else if (org.apache.commons.lang.StringUtils.isNotBlank(order.getCostStatus()) && !"未录成本".equals(order.getCostStatus())) {
                    order.setCostFinishStatusForSort(1);
                } else {
                    order.setCostFinishStatusForSort(0);
                }
                if (order.getCostAmount() == null) {
                    order.setCostAmount(BigDecimal.ZERO);
                }
                if (order.getIncomeAmount() == null) {
                    order.setIncomeAmount(BigDecimal.ZERO);
                }
                if (order.getProfitAmount() == null) {
                    order.setProfitAmount(BigDecimal.ZERO);
                }
            });
        }
        return iPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lock(String orderIds, String businessScope, LocalDateTime lockDate) {
        //日志
        LogBean log = new LogBean();
        log.setPageName(businessScope + "订单");
        log.setPageFunction("财务锁账");

        log.setLogRemark("财务日期：" + DateTimeFormatter.ofPattern("yyyy-MM-dd").format(lockDate));
        log.setBusinessScope(businessScope);

        if (businessScope.startsWith("A")) {
            LambdaQueryWrapper<AfOrder> afOrderWrapper = Wrappers.<AfOrder>lambdaQuery();
            afOrderWrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId()).in(AfOrder::getOrderId, orderIds.split(",")).eq(AfOrder::getOrderStatus, "财务锁账");
            List<AfOrder> list = afOrderService.list(afOrderWrapper);
            if (list.size() > 0) {
                throw new RuntimeException("所选订单中含有已经财务锁账的订单，无法再次锁账");
            }
            LambdaQueryWrapper<AfOrder> wrapper = Wrappers.<AfOrder>lambdaQuery();
            wrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId()).in(AfOrder::getOrderId, orderIds.split(","));
            afOrderService.list(wrapper).stream().forEach(afOrder -> {
                log.setOrderNumber(afOrder.getOrderCode());
                log.setOrderId(afOrder.getOrderId());
                log.setOrderUuid(afOrder.getOrderUuid());
                logService.saveLog(log);
            });
            lockForAF(orderIds, lockDate);
        } else if (businessScope.startsWith("S")) {
            LambdaQueryWrapper<ScOrder> scOrderWrapper = Wrappers.<ScOrder>lambdaQuery();
            scOrderWrapper.eq(ScOrder::getOrgId, SecurityUtils.getUser().getOrgId()).in(ScOrder::getOrderId, orderIds.split(",")).eq(ScOrder::getOrderStatus, "财务锁账");
            List<ScOrder> list = scOrderService.list(scOrderWrapper);
            if (list.size() > 0) {
                throw new RuntimeException("所选订单中含有已经财务锁账的订单，无法再次锁账");
            }
            LambdaQueryWrapper<ScOrder> wrapper = Wrappers.<ScOrder>lambdaQuery();
            wrapper.eq(ScOrder::getOrgId, SecurityUtils.getUser().getOrgId()).in(ScOrder::getOrderId, orderIds.split(","));
            ScLog scLog = new ScLog();
            scOrderService.list(wrapper).stream().forEach(scOrder -> {
                log.setOrderNumber(scOrder.getOrderCode());
                log.setOrderId(scOrder.getOrderId());
                log.setOrderUuid(scOrder.getOrderUuid());
                BeanUtils.copyProperties(log, scLog);
                scLogService.saveLog(scLog);
            });
            lockForSC(orderIds, lockDate);
        } else if (businessScope.startsWith("T")) {
            LambdaQueryWrapper<TcOrder> tcOrderWrapper = Wrappers.<TcOrder>lambdaQuery();
            tcOrderWrapper.eq(TcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).in(TcOrder::getOrderId, orderIds.split(",")).eq(TcOrder::getOrderStatus, "财务锁账");
            List<TcOrder> list = tcOrderService.list(tcOrderWrapper);
            if (list.size() > 0) {
                throw new RuntimeException("所选订单中含有已经财务锁账的订单，无法再次锁账");
            }
            LambdaQueryWrapper<TcOrder> wrapper = Wrappers.<TcOrder>lambdaQuery();
            wrapper.eq(TcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).in(TcOrder::getOrderId, orderIds.split(","));
            TcLog tcLog = new TcLog();
            tcOrderService.list(wrapper).stream().forEach(tcOrder -> {
                log.setOrderNumber(tcOrder.getOrderCode());
                log.setOrderId(tcOrder.getOrderId());
                log.setOrderUuid(tcOrder.getOrderUuid());
                BeanUtils.copyProperties(log, tcLog);
                tcLog.setCreatorId(SecurityUtils.getUser().getId());
                tcLog.setCreatorName(SecurityUtils.getUser().buildOptName());
                tcLog.setCreatTime(LocalDateTime.now());
                tcLog.setOrgId(SecurityUtils.getUser().getOrgId());
                tcLogService.save(tcLog);
            });
            this.lockForTC(orderIds, lockDate);

        } else if (businessScope.startsWith("L")) {
            LambdaQueryWrapper<LcOrder> lcOrderWrapper = Wrappers.<LcOrder>lambdaQuery();
            lcOrderWrapper.eq(LcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).in(LcOrder::getOrderId, orderIds.split(",")).eq(LcOrder::getOrderStatus, "财务锁账");
            List<LcOrder> list = lcOrderService.list(lcOrderWrapper);
            if (list.size() > 0) {
                throw new RuntimeException("所选订单中含有已经财务锁账的订单，无法再次锁账");
            }
            LambdaQueryWrapper<LcOrder> wrapper = Wrappers.<LcOrder>lambdaQuery();
            wrapper.eq(LcOrder::getOrgId, SecurityUtils.getUser().getOrgId()).in(LcOrder::getOrderId, orderIds.split(","));
            LcLog lcLog = new LcLog();
            lcOrderService.list(wrapper).stream().forEach(lcOrder -> {
                log.setOrderNumber(lcOrder.getOrderCode());
                log.setOrderId(lcOrder.getOrderId());
                log.setOrderUuid(lcOrder.getOrderUuid());
                BeanUtils.copyProperties(log, lcLog);
                lcLog.setCreatorId(SecurityUtils.getUser().getId());
                lcLog.setCreatorName(SecurityUtils.getUser().buildOptName());
                lcLog.setCreatTime(LocalDateTime.now());
                lcLog.setOrgId(SecurityUtils.getUser().getOrgId());
                lcLogService.save(lcLog);
            });
            this.lockForLC(orderIds, lockDate);
        } else if (businessScope.equals("IO")) {
            LambdaQueryWrapper<IoOrder> ioOrderWrapper = Wrappers.<IoOrder>lambdaQuery();
            ioOrderWrapper.eq(IoOrder::getOrgId, SecurityUtils.getUser().getOrgId()).in(IoOrder::getOrderId, orderIds.split(",")).eq(IoOrder::getOrderStatus, "财务锁账");
            List<IoOrder> list = ioOrderService.list(ioOrderWrapper);
            if (list.size() > 0) {
                throw new RuntimeException("所选订单中含有已经财务锁账的订单，无法再次锁账");
            }
            LambdaQueryWrapper<IoOrder> wrapper = Wrappers.<IoOrder>lambdaQuery();
            wrapper.eq(IoOrder::getOrgId, SecurityUtils.getUser().getOrgId()).in(IoOrder::getOrderId, orderIds.split(","));
            IoLog ioLog = new IoLog();
            ioOrderService.list(wrapper).stream().forEach(ioOrder -> {
                log.setOrderNumber(ioOrder.getOrderCode());
                log.setOrderId(ioOrder.getOrderId());
                log.setOrderUuid(ioOrder.getOrderUuid());
                BeanUtils.copyProperties(log, ioLog);
                ioLog.setCreatorId(SecurityUtils.getUser().getId());
                ioLog.setCreatorName(SecurityUtils.getUser().buildOptName());
                ioLog.setCreatTime(LocalDateTime.now());
                ioLog.setOrgId(SecurityUtils.getUser().getOrgId());
                ioLogService.save(ioLog);
            });
            this.lockForIO(orderIds, lockDate);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void lockForLC(String orderIds, LocalDateTime lockDate) {
        orderLockOrUnlockMapper.lockOrderForLC(orderIds, SecurityUtils.getUser().getOrgId(), LocalDateTime.now(), SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail(), SecurityUtils.getUser().getId(), UUID.randomUUID().toString());
        orderLockOrUnlockMapper.lockIncomeForLC(orderIds, SecurityUtils.getUser().getOrgId(), lockDate);
        orderLockOrUnlockMapper.lockCostForLC(orderIds, SecurityUtils.getUser().getOrgId(), lockDate);
    }

    @Transactional(rollbackFor = Exception.class)
    public void lockForIO(String orderIds, LocalDateTime lockDate) {
        orderLockOrUnlockMapper.lockOrderForIO(orderIds, SecurityUtils.getUser().getOrgId(), LocalDateTime.now(), SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail(), SecurityUtils.getUser().getId(), UUID.randomUUID().toString());
        orderLockOrUnlockMapper.lockIncomeForIO(orderIds, SecurityUtils.getUser().getOrgId(), lockDate);
        orderLockOrUnlockMapper.lockCostForIO(orderIds, SecurityUtils.getUser().getOrgId(), lockDate);
    }

    @Transactional(rollbackFor = Exception.class)
    public void lockForTC(String orderIds, LocalDateTime lockDate) {
        orderLockOrUnlockMapper.lockOrderForTC(orderIds, SecurityUtils.getUser().getOrgId(), LocalDateTime.now(), SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail(), SecurityUtils.getUser().getId(), UUID.randomUUID().toString());
        orderLockOrUnlockMapper.lockIncomeForTC(orderIds, SecurityUtils.getUser().getOrgId(), lockDate);
        orderLockOrUnlockMapper.lockCostForTC(orderIds, SecurityUtils.getUser().getOrgId(), lockDate);
    }

    @Transactional(rollbackFor = Exception.class)
    public void lockForSC(String orderIds, LocalDateTime lockDate) {
        orderLockOrUnlockMapper.lockOrderForSC(orderIds, SecurityUtils.getUser().getOrgId(), LocalDateTime.now(), SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail(), SecurityUtils.getUser().getId(), UUID.randomUUID().toString());
        orderLockOrUnlockMapper.lockIncomeForSC(orderIds, SecurityUtils.getUser().getOrgId(), lockDate);
        orderLockOrUnlockMapper.lockCostForSC(orderIds, SecurityUtils.getUser().getOrgId(), lockDate);
    }

    @Transactional(rollbackFor = Exception.class)
    public void lockForAF(String orderIds, LocalDateTime lockDate) {
        orderLockOrUnlockMapper.lockOrderForAF(orderIds, SecurityUtils.getUser().getOrgId(), LocalDateTime.now(), SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail(), SecurityUtils.getUser().getId(), UUID.randomUUID().toString());
        orderLockOrUnlockMapper.lockIncomeForAF(orderIds, SecurityUtils.getUser().getOrgId(), lockDate);
        orderLockOrUnlockMapper.lockCostForAF(orderIds, SecurityUtils.getUser().getOrgId(), lockDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlock(Integer orderId, String businessScope) {
        //日志
        LogBean log = new LogBean();
        log.setPageName(businessScope + "订单");
        log.setPageFunction("财务解锁");
        log.setLogRemark("");
        log.setBusinessScope(businessScope);

        if (businessScope.startsWith("A")) {
            //校验
            AfOrder order = afOrderService.getById(orderId);
            if (order == null) {
                throw new RuntimeException("该订单不存在，解锁失败");
            }
            if (!"财务锁账".equals(order.getOrderStatus())) {
                throw new RuntimeException("该订单已解锁，无法再次解锁");
            }
            String orderStatus = "";
            if ("AE".equals(businessScope)) {
                if (order.getDeliverySignDate() != null) {
                    orderStatus = "目的港签收";
                } else if (order.getArrivalCustomsClearanceDate() != null) {
                    orderStatus = "目的港放行";
                } else if (order.getArrivalCustomsInspectionDate() != null) {
                    orderStatus = "目的港查验";
                } else if (order.getCustomsClearanceDate() != null) {
                    orderStatus = "海关放行";
                } else if (order.getCustomsInspectionDate() != null) {
                    orderStatus = "海关查验";
                } else if (order.getConfirmWeight() != null) {
                    orderStatus = "货物出重";
                } else if (order.getAwbId() != null) {
                    orderStatus = "舱位确认";
                } else {
                    orderStatus = "订单创建";
                }
            } else {
                if (order.getDeliverySignDate() != null) {
                    orderStatus = "派送签收";
                } else if (order.getOutboundDate() != null) {
                    orderStatus = "货物出库";
                } else if (order.getCustomsClearanceDate() != null) {
                    orderStatus = "海关放行";
                } else if (order.getCustomsInspectionDate() != null) {
                    orderStatus = "海关查验";
                } else if (order.getInboundDate() != null) {
                    orderStatus = "货物入库";
                } else {
                    orderStatus = "订单创建";
                }
            }
            orderLockOrUnlockMapper.unlockOrderForAF(orderId, SecurityUtils.getUser().getOrgId(), LocalDateTime.now(), SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail(), SecurityUtils.getUser().getId(), UUID.randomUUID().toString(), orderStatus);
            //保存日志
            log.setOrderNumber(order.getOrderCode());
            log.setOrderId(order.getOrderId());
            log.setOrderUuid(order.getOrderUuid());
            logService.saveLog(log);
        } else if (businessScope.startsWith("S")) {
            //校验
            ScOrder order = scOrderService.getById(orderId);
            if (order == null) {
                throw new RuntimeException("该订单不存在，解锁失败");
            }
            if (!"财务锁账".equals(order.getOrderStatus())) {
                throw new RuntimeException("该订单已解锁，无法再次解锁");
            }
            String orderStatus = "";
            if ("SE".equals(businessScope)) {
                if (order.getDeliverySignDate() != null) {
                    orderStatus = "目的港签收";
                } else if (order.getArrivalCustomsClearanceDate() != null) {
                    orderStatus = "目的港放行";
                } else if (order.getArrivalCustomsInspectionDate() != null) {
                    orderStatus = "目的港查验";
                } else if (order.getCustomsClearanceDate() != null) {
                    orderStatus = "海关放行";
                } else if (order.getCustomsInspectionDate() != null) {
                    orderStatus = "海关查验";
                } else {
                    orderStatus = "订单创建";
                }
            } else {
                if (order.getDeliverySignDate() != null) {
                    orderStatus = "派送签收";
                } else if (order.getOutboundDate() != null) {
                    orderStatus = "货物出库";
                } else if (order.getCustomsClearanceDate() != null) {
                    orderStatus = "海关放行";
                } else if (order.getCustomsInspectionDate() != null) {
                    orderStatus = "海关查验";
                } else if (order.getInboundDate() != null) {
                    orderStatus = "货物入库";
                } else {
                    orderStatus = "订单创建";
                }
            }
            orderLockOrUnlockMapper.unlockOrderForSC(orderId, SecurityUtils.getUser().getOrgId(), LocalDateTime.now(), SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail(), SecurityUtils.getUser().getId(), UUID.randomUUID().toString(), orderStatus);
            //保存日志
            log.setOrderNumber(order.getOrderCode());
            log.setOrderId(order.getOrderId());
            log.setOrderUuid(order.getOrderUuid());
            ScLog scLog = new ScLog();
            BeanUtils.copyProperties(log, scLog);
            scLogService.saveLog(scLog);
        } else if (businessScope.startsWith("T")) {
            //校验
            TcOrder order = tcOrderService.getById(orderId);
            if (order == null) {
                throw new RuntimeException("该订单不存在，解锁失败");
            }
            if (!"财务锁账".equals(order.getOrderStatus())) {
                throw new RuntimeException("该订单已解锁，无法再次解锁");
            }
            String orderStatus = "";
            if ("TE".equals(businessScope)) {
                if (order.getDeliverySignDate() != null) {
                    orderStatus = "目的港签收";
                } else if (order.getArrivalCustomsClearanceDate() != null) {
                    orderStatus = "目的港放行";
                } else if (order.getArrivalCustomsInspectionDate() != null) {
                    orderStatus = "目的港查验";
                } else if (order.getCustomsClearanceDate() != null) {
                    orderStatus = "海关放行";
                } else if (order.getCustomsInspectionDate() != null) {
                    orderStatus = "海关查验";
                } else {
                    orderStatus = "订单创建";
                }
            }else {
                if (order.getDeliverySignDate() != null) {
                    orderStatus = "目的港签收";
                } else if (order.getArrivalCustomsClearanceDate() != null) {
                    orderStatus = "目的港放行";
                } else if (order.getArrivalCustomsInspectionDate() != null) {
                    orderStatus = "目的港查验";
                } else if (order.getCustomsClearanceDate() != null) {
                    orderStatus = "海关放行";
                } else if (order.getCustomsInspectionDate() != null) {
                    orderStatus = "海关查验";
                } else {
                    orderStatus = "订单创建";
                }
            }
            orderLockOrUnlockMapper.unlockOrderForTC(orderId, SecurityUtils.getUser().getOrgId(), LocalDateTime.now(), SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail(), SecurityUtils.getUser().getId(), UUID.randomUUID().toString(), orderStatus);
            //保存日志
            log.setOrderNumber(order.getOrderCode());
            log.setOrderId(order.getOrderId());
            log.setOrderUuid(order.getOrderUuid());
            TcLog tcLog = new TcLog();
            BeanUtils.copyProperties(log, tcLog);
            tcLog.setCreatorId(SecurityUtils.getUser().getId());
            tcLog.setCreatorName(SecurityUtils.getUser().buildOptName());
            tcLog.setCreatTime(LocalDateTime.now());
            tcLog.setOrgId(SecurityUtils.getUser().getOrgId());
            tcLogService.save(tcLog);
        } else if (businessScope.startsWith("L")) {
            //校验
            LcOrder order = lcOrderService.getById(orderId);
            if (order == null) {
                throw new RuntimeException("该订单不存在，解锁失败");
            }
            if (!"财务锁账".equals(order.getOrderStatus())) {
                throw new RuntimeException("该订单已解锁，无法再次解锁");
            }
            String orderStatus = "";
            if (order.getConfirmWeight() != null) {
                orderStatus = "货物出重";
            } else {
                orderStatus = "订单创建";
            }
            orderLockOrUnlockMapper.unlockOrderForLC(orderId, SecurityUtils.getUser().getOrgId(), LocalDateTime.now(), SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail(), SecurityUtils.getUser().getId(), UUID.randomUUID().toString(), orderStatus);
            //保存日志
            log.setOrderNumber(order.getOrderCode());
            log.setOrderId(order.getOrderId());
            log.setOrderUuid(order.getOrderUuid());
            LcLog lcLog = new LcLog();
            BeanUtils.copyProperties(log, lcLog);
            lcLog.setCreatorId(SecurityUtils.getUser().getId());
            lcLog.setCreatorName(SecurityUtils.getUser().buildOptName());
            lcLog.setCreatTime(LocalDateTime.now());
            lcLog.setOrgId(SecurityUtils.getUser().getOrgId());
            lcLogService.save(lcLog);
        } else if (businessScope.equals("IO")) {
            //校验
            IoOrder order = ioOrderService.getById(orderId);
            if (order == null) {
                throw new RuntimeException("该订单不存在，解锁失败");
            }
            if (!"财务锁账".equals(order.getOrderStatus())) {
                throw new RuntimeException("该订单已解锁，无法再次解锁");
            }
            String orderStatus = "订单创建";
            orderLockOrUnlockMapper.unlockOrderForIO(orderId, SecurityUtils.getUser().getOrgId(), LocalDateTime.now(), SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail(), SecurityUtils.getUser().getId(), UUID.randomUUID().toString(), orderStatus);
            //保存日志
            log.setOrderNumber(order.getOrderCode());
            log.setOrderId(order.getOrderId());
            log.setOrderUuid(order.getOrderUuid());
            IoLog ioLog = new IoLog();
            BeanUtils.copyProperties(log, ioLog);
            ioLog.setCreatorId(SecurityUtils.getUser().getId());
            ioLog.setCreatorName(SecurityUtils.getUser().buildOptName());
            ioLog.setCreatTime(LocalDateTime.now());
            ioLog.setOrgId(SecurityUtils.getUser().getOrgId());
            ioLogService.save(ioLog);
        }
    }

    @Override
    public void rollBackToFinishIncome(Integer orderId, String businessScope) {
        //日志
        LogBean log = new LogBean();
        log.setPageName(businessScope + "订单");
        log.setPageFunction("退回收入完成");

        log.setLogRemark("退回日期：" + DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now()));
        log.setBusinessScope(businessScope);

        if (businessScope.startsWith("A")) {
            AfOrder afOrder = afOrderService.getById(orderId);
            if (afOrder == null) {
                throw new RuntimeException("该订单不存在");
            }
            if ("财务锁账".equals(afOrder.getOrderStatus())) {
                throw new RuntimeException("订单已做财务锁账，请先解锁 再退回完成");
            }
            if (afOrder.getIncomeRecorded() == null || !afOrder.getIncomeRecorded()) {
                throw new RuntimeException("该订单已经回退收入完成，无需再次回退");
            }
            afOrder.setRowUuid(UUID.randomUUID().toString());
            afOrder.setEditTime(new Date());
            afOrder.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            afOrder.setEditorId(SecurityUtils.getUser().getId());
            afOrder.setIncomeRecorded(false);
            afOrderService.updateById(afOrder);
            log.setOrderNumber(afOrder.getOrderCode());
            log.setOrderId(afOrder.getOrderId());
            log.setOrderUuid(afOrder.getOrderUuid());
            logService.saveLog(log);
        } else if (businessScope.startsWith("S")) {
            ScOrder scOrder = scOrderService.getById(orderId);
            if (scOrder == null) {
                throw new RuntimeException("该订单不存在");
            }
            if ("财务锁账".equals(scOrder.getOrderStatus())) {
                throw new RuntimeException("订单已做财务锁账，请先解锁 再退回完成");
            }
            if (scOrder.getIncomeRecorded() == null || !scOrder.getIncomeRecorded()) {
                throw new RuntimeException("该订单已经回退收入完成，无需再次回退");
            }
            scOrder.setEditTime(LocalDateTime.now());
            scOrder.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            scOrder.setEditorId(SecurityUtils.getUser().getId());
            scOrder.setIncomeRecorded(false);
            scOrderService.updateById(scOrder);
            log.setOrderNumber(scOrder.getOrderCode());
            log.setOrderId(scOrder.getOrderId());
            log.setOrderUuid(scOrder.getOrderUuid());
            ScLog scLog = new ScLog();
            BeanUtils.copyProperties(log, scLog);
            scLogService.saveLog(scLog);
        } else if (businessScope.startsWith("T")) {
            TcOrder tcOrder = tcOrderService.getById(orderId);
            if (tcOrder == null) {
                throw new RuntimeException("该订单不存在");
            }
            if ("财务锁账".equals(tcOrder.getOrderStatus())) {
                throw new RuntimeException("订单已做财务锁账，请先解锁 再退回完成");
            }
            if (tcOrder.getIncomeRecorded() == null || !tcOrder.getIncomeRecorded()) {
                throw new RuntimeException("该订单已经回退收入完成，无需再次回退");
            }
            tcOrder.setEditTime(LocalDateTime.now());
            tcOrder.setEditorName(SecurityUtils.getUser().buildOptName());
            tcOrder.setEditorId(SecurityUtils.getUser().getId());
            tcOrder.setIncomeRecorded(false);
            tcOrderService.updateById(tcOrder);
            log.setOrderNumber(tcOrder.getOrderCode());
            log.setOrderId(tcOrder.getOrderId());
            log.setOrderUuid(tcOrder.getOrderUuid());
            TcLog tcLog = new TcLog();
            BeanUtils.copyProperties(log, tcLog);
            tcLog.setCreatorId(SecurityUtils.getUser().getId());
            tcLog.setCreatorName(SecurityUtils.getUser().buildOptName());
            tcLog.setCreatTime(LocalDateTime.now());
            tcLog.setOrgId(SecurityUtils.getUser().getOrgId());
            tcLogService.save(tcLog);
        } else if (businessScope.startsWith("L")) {
            LcOrder lcOrder = lcOrderService.getById(orderId);
            if (lcOrder == null) {
                throw new RuntimeException("该订单不存在");
            }
            if ("财务锁账".equals(lcOrder.getOrderStatus())) {
                throw new RuntimeException("订单已做财务锁账，请先解锁 再退回完成");
            }
            if (lcOrder.getIncomeRecorded() == null || !lcOrder.getIncomeRecorded()) {
                throw new RuntimeException("该订单已经回退收入完成，无需再次回退");
            }
            lcOrder.setEditTime(LocalDateTime.now());
            lcOrder.setEditorName(SecurityUtils.getUser().buildOptName());
            lcOrder.setEditorId(SecurityUtils.getUser().getId());
            lcOrder.setIncomeRecorded(false);
            lcOrderService.updateById(lcOrder);
            log.setOrderNumber(lcOrder.getOrderCode());
            log.setOrderId(lcOrder.getOrderId());
            log.setOrderUuid(lcOrder.getOrderUuid());
            LcLog lcLog = new LcLog();
            BeanUtils.copyProperties(log, lcLog);
            lcLog.setCreatorId(SecurityUtils.getUser().getId());
            lcLog.setCreatorName(SecurityUtils.getUser().buildOptName());
            lcLog.setCreatTime(LocalDateTime.now());
            lcLog.setOrgId(SecurityUtils.getUser().getOrgId());
            lcLogService.save(lcLog);
        } else if (businessScope.equals("IO")) {
            IoOrder ioOrder = ioOrderService.getById(orderId);
            if (ioOrder == null) {
                throw new RuntimeException("该订单不存在");
            }
            if ("财务锁账".equals(ioOrder.getOrderStatus())) {
                throw new RuntimeException("订单已做财务锁账，请先解锁 再退回完成");
            }
            if (ioOrder.getIncomeRecorded() == null || !ioOrder.getIncomeRecorded()) {
                throw new RuntimeException("该订单已经回退收入完成，无需再次回退");
            }
            ioOrder.setEditTime(LocalDateTime.now());
            ioOrder.setEditorName(SecurityUtils.getUser().buildOptName());
            ioOrder.setEditorId(SecurityUtils.getUser().getId());
            ioOrder.setIncomeRecorded(false);
            ioOrderService.updateById(ioOrder);
            log.setOrderNumber(ioOrder.getOrderCode());
            log.setOrderId(ioOrder.getOrderId());
            log.setOrderUuid(ioOrder.getOrderUuid());
            IoLog ioLog = new IoLog();
            BeanUtils.copyProperties(log, ioLog);
            ioLog.setCreatorId(SecurityUtils.getUser().getId());
            ioLog.setCreatorName(SecurityUtils.getUser().buildOptName());
            ioLog.setCreatTime(LocalDateTime.now());
            ioLog.setOrgId(SecurityUtils.getUser().getOrgId());
            ioLogService.save(ioLog);
        }
    }

    @Override
    public void rollBackToFinishCost(Integer orderId, String businessScope) {
        //日志
        LogBean log = new LogBean();
        log.setPageName(businessScope + "订单");
        log.setPageFunction("退回成本完成");

        log.setLogRemark("退回日期：" + DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now()));
        log.setBusinessScope(businessScope);
        if (businessScope.startsWith("A")) {
            AfOrder afOrder = afOrderService.getById(orderId);
            if (afOrder == null) {
                throw new RuntimeException("该订单不存在");
            }
            if ("财务锁账".equals(afOrder.getOrderStatus())) {
                throw new RuntimeException("订单已做财务锁账，请先解锁 再退回完成");
            }
            if (afOrder.getCostRecorded() == null || !afOrder.getCostRecorded()) {
                throw new RuntimeException("该订单已经回退成本完成，无需再次回退");
            }
            afOrder.setRowUuid(UUID.randomUUID().toString());
            afOrder.setEditTime(new Date());
            afOrder.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            afOrder.setEditorId(SecurityUtils.getUser().getId());
            afOrder.setCostRecorded(false);
            afOrderService.updateById(afOrder);
            log.setOrderNumber(afOrder.getOrderCode());
            log.setOrderId(afOrder.getOrderId());
            log.setOrderUuid(afOrder.getOrderUuid());
            logService.saveLog(log);
        } else if (businessScope.startsWith("S")) {
            ScOrder scOrder = scOrderService.getById(orderId);
            if (scOrder == null) {
                throw new RuntimeException("该订单不存在");
            }
            if ("财务锁账".equals(scOrder.getOrderStatus())) {
                throw new RuntimeException("订单已做财务锁账，请先解锁 再退回完成");
            }
            if (scOrder.getCostRecorded() == null || !scOrder.getCostRecorded()) {
                throw new RuntimeException("该订单已经回退成本完成，无需再次回退");
            }
            scOrder.setEditTime(LocalDateTime.now());
            scOrder.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            scOrder.setEditorId(SecurityUtils.getUser().getId());
            scOrder.setCostRecorded(false);
            scOrderService.updateById(scOrder);
            log.setOrderNumber(scOrder.getOrderCode());
            log.setOrderId(scOrder.getOrderId());
            log.setOrderUuid(scOrder.getOrderUuid());
            ScLog scLog = new ScLog();
            BeanUtils.copyProperties(log, scLog);
            scLogService.saveLog(scLog);
        } else if (businessScope.startsWith("T")) {
            TcOrder tcOrder = tcOrderService.getById(orderId);
            if (tcOrder == null) {
                throw new RuntimeException("该订单不存在");
            }
            if ("财务锁账".equals(tcOrder.getOrderStatus())) {
                throw new RuntimeException("订单已做财务锁账，请先解锁 再退回完成");
            }
            if (tcOrder.getCostRecorded() == null || !tcOrder.getCostRecorded()) {
                throw new RuntimeException("该订单已经回退成本完成，无需再次回退");
            }
            tcOrder.setEditTime(LocalDateTime.now());
            tcOrder.setEditorName(SecurityUtils.getUser().buildOptName());
            tcOrder.setEditorId(SecurityUtils.getUser().getId());
            tcOrder.setCostRecorded(false);
            tcOrderService.updateById(tcOrder);
            log.setOrderNumber(tcOrder.getOrderCode());
            log.setOrderId(tcOrder.getOrderId());
            log.setOrderUuid(tcOrder.getOrderUuid());
            TcLog tcLog = new TcLog();
            BeanUtils.copyProperties(log, tcLog);
            tcLog.setCreatorId(SecurityUtils.getUser().getId());
            tcLog.setCreatorName(SecurityUtils.getUser().buildOptName());
            tcLog.setCreatTime(LocalDateTime.now());
            tcLog.setOrgId(SecurityUtils.getUser().getOrgId());
            tcLogService.save(tcLog);
        } else if (businessScope.startsWith("L")) {
            LcOrder lcOrder = lcOrderService.getById(orderId);
            if (lcOrder == null) {
                throw new RuntimeException("该订单不存在");
            }
            if ("财务锁账".equals(lcOrder.getOrderStatus())) {
                throw new RuntimeException("订单已做财务锁账，请先解锁 再退回完成");
            }
            if (lcOrder.getCostRecorded() == null || !lcOrder.getCostRecorded()) {
                throw new RuntimeException("该订单已经回退成本完成，无需再次回退");
            }
            lcOrder.setEditTime(LocalDateTime.now());
            lcOrder.setEditorName(SecurityUtils.getUser().buildOptName());
            lcOrder.setEditorId(SecurityUtils.getUser().getId());
            lcOrder.setCostRecorded(false);
            lcOrderService.updateById(lcOrder);
            log.setOrderNumber(lcOrder.getOrderCode());
            log.setOrderId(lcOrder.getOrderId());
            log.setOrderUuid(lcOrder.getOrderUuid());
            LcLog lcLog = new LcLog();
            BeanUtils.copyProperties(log, lcLog);
            lcLog.setCreatorId(SecurityUtils.getUser().getId());
            lcLog.setCreatorName(SecurityUtils.getUser().buildOptName());
            lcLog.setCreatTime(LocalDateTime.now());
            lcLog.setOrgId(SecurityUtils.getUser().getOrgId());
            lcLogService.save(lcLog);
        } else if (businessScope.equals("IO")) {
            IoOrder ioOrder = ioOrderService.getById(orderId);
            if (ioOrder == null) {
                throw new RuntimeException("该订单不存在");
            }
            if ("财务锁账".equals(ioOrder.getOrderStatus())) {
                throw new RuntimeException("订单已做财务锁账，请先解锁 再退回完成");
            }
            if (ioOrder.getCostRecorded() == null || !ioOrder.getCostRecorded()) {
                throw new RuntimeException("该订单已经回退成本完成，无需再次回退");
            }
            ioOrder.setEditTime(LocalDateTime.now());
            ioOrder.setEditorName(SecurityUtils.getUser().buildOptName());
            ioOrder.setEditorId(SecurityUtils.getUser().getId());
            ioOrder.setCostRecorded(false);
            ioOrderService.updateById(ioOrder);
            log.setOrderNumber(ioOrder.getOrderCode());
            log.setOrderId(ioOrder.getOrderId());
            log.setOrderUuid(ioOrder.getOrderUuid());
            IoLog ioLog = new IoLog();
            BeanUtils.copyProperties(log, ioLog);
            ioLog.setCreatorId(SecurityUtils.getUser().getId());
            ioLog.setCreatorName(SecurityUtils.getUser().buildOptName());
            ioLog.setCreatTime(LocalDateTime.now());
            ioLog.setOrgId(SecurityUtils.getUser().getOrgId());
            ioLogService.save(ioLog);
        }
    }

    @Override
    public void exportExcel(OrderLockOrUnlock orderLockOrUnlock) {
        if (StrUtil.isBlank(orderLockOrUnlock.getColumnStrs())) {
            throw new RuntimeException("缺失必要传参，无法打印");
        }
        List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
        //转json为数组
        JSONArray jsonArr = JSONArray.parseArray(orderLockOrUnlock.getColumnStrs());
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
        List<OrderLockOrUnlockExcel> list = getList(orderLockOrUnlock);
        if (list != null && list.size() > 0) {
            for (OrderLockOrUnlockExcel lockOrUnlock : list) {
                LinkedHashMap map = new LinkedHashMap();
                for (int j = 0; j < colunmStrs.length; j++) {
                    map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], lockOrUnlock));
                }
                listExcel.add(map);
            }
        }
        ExcelExportUtils u = new ExcelExportUtils();
        u.exportExcelLinkListMap(WebUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");
    }

    @Override
    @SneakyThrows
    public String printBusinessCalculationBillMany(String busnessScope, String orderIds) {
        ArrayList<String> filePathList = new ArrayList<>();
        Arrays.stream(orderIds.split(",")).forEach(orderId -> {
            filePathList.add(afOrderService.printBusinessCalculationBill(busnessScope, Integer.valueOf(orderId), false));
        });
        //拼接多个PDF文件
        String lastFilePath = PDFUtils.filePath + "/PDFtemplate/temp/orderLockAndUnlock/print/ORDER_ACCOUNT_SHEET_" + new Date().getTime() + ".pdf";
        PDFUtils.loadAllPDFForFile(filePathList, lastFilePath, PDFUtils.PAGE_CH);
        return lastFilePath.replace(PDFUtils.filePath, "");
    }

    private List<OrderLockOrUnlockExcel> getList(OrderLockOrUnlock orderLockOrUnlock) {
        orderLockOrUnlock.setOrgId(SecurityUtils.getUser().getOrgId());
        if (StrUtil.isBlank(orderLockOrUnlock.getBusinessScope())) {
            throw new RuntimeException("请选择业务范畴");
        }
        if (!StrUtil.isBlank(orderLockOrUnlock.getCoopName())) {
            orderLockOrUnlock.setCoopName(orderLockOrUnlock.getCoopName().toUpperCase());
        }
        if (!StrUtil.isBlank(orderLockOrUnlock.getOrderCode())) {
            orderLockOrUnlock.setOrderCode(orderLockOrUnlock.getOrderCode().toUpperCase());
        }
        if (!StrUtil.isBlank(orderLockOrUnlock.getFlightNo())) {
            orderLockOrUnlock.setFlightNo(orderLockOrUnlock.getFlightNo().toUpperCase());
        }
        List<OrderLockOrUnlock> list = new ArrayList<>();
        if (orderLockOrUnlock.getBusinessScope().startsWith("A")) {
            list = orderLockOrUnlockMapper.getListForAF(orderLockOrUnlock);
        } else if (orderLockOrUnlock.getBusinessScope().startsWith("S")) {
            list = orderLockOrUnlockMapper.getListForSC(orderLockOrUnlock);
        } else if (orderLockOrUnlock.getBusinessScope().startsWith("T")) {
            list = orderLockOrUnlockMapper.getListForTC(orderLockOrUnlock);
        } else if (orderLockOrUnlock.getBusinessScope().startsWith("L")) {
            list = orderLockOrUnlockMapper.getListForLC(orderLockOrUnlock);
        } else if (orderLockOrUnlock.getBusinessScope().equals("IO")) {
            list = orderLockOrUnlockMapper.getListForIO(orderLockOrUnlock);
        }

        return list.stream().map(lockOrUnlock -> {
            OrderLockOrUnlockExcel orderLockOrUnlockExcel = new OrderLockOrUnlockExcel();
            BeanUtils.copyProperties(lockOrUnlock, orderLockOrUnlockExcel);
            orderLockOrUnlockExcel.setPlanChargeWeight(FormatUtils.formatWithQWFNoBit(lockOrUnlock.getPlanChargeWeight()));
            orderLockOrUnlockExcel.setPlanPieces(lockOrUnlock.getPlanPieces());
            orderLockOrUnlockExcel.setPlanVolume(FormatUtils.formatWithQWFNoBit(lockOrUnlock.getPlanVolume()));
            orderLockOrUnlockExcel.setPlanWeight(FormatUtils.formatWithQWFNoBit(lockOrUnlock.getPlanWeight()));
            orderLockOrUnlockExcel.setCostFinishStatus(lockOrUnlock.getCostFinishStatus() || (StrUtil.isNotBlank(lockOrUnlock.getCostStatus()) && !"未录成本".equals(lockOrUnlock.getCostStatus())) ? "√" : "");
            orderLockOrUnlockExcel.setIncomeFinishStatus(lockOrUnlock.getIncomeFinishStatus() || (StrUtil.isNotBlank(lockOrUnlock.getIncomeStatus()) && !"未录收入".equals(lockOrUnlock.getIncomeStatus())) ? "√" : "");
            orderLockOrUnlockExcel.setOrderLockStatus(lockOrUnlock.getOrderLockStatus() ? "√" : "");
            orderLockOrUnlockExcel.setCostAmount(lockOrUnlock.getCostAmount() != null ? FormatUtils.formatWithQWF(lockOrUnlock.getCostAmount(), 2) : "0.00");
            orderLockOrUnlockExcel.setIncomeAmount(lockOrUnlock.getIncomeAmount() != null ? FormatUtils.formatWithQWF(lockOrUnlock.getIncomeAmount(), 2) : "0.00");
            orderLockOrUnlockExcel.setProfitAmount(lockOrUnlock.getProfitAmount() != null ? FormatUtils.formatWithQWF(lockOrUnlock.getProfitAmount(), 2) : "0.00");
            return orderLockOrUnlockExcel;
        }).collect(Collectors.toList());
    }
}
