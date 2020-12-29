package com.efreight.sc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.entity.LcInbound;
import com.efreight.sc.dao.LcInboundMapper;
import com.efreight.sc.entity.LcLog;
import com.efreight.sc.entity.LcOrder;
import com.efreight.sc.service.LcInboundService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.sc.service.LcLogService;
import com.efreight.sc.service.LcOrderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * LC 陆运订单： 操作出重表 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-28
 */
@Service
@AllArgsConstructor
public class LcInboundServiceImpl extends ServiceImpl<LcInboundMapper, LcInbound> implements LcInboundService {

    private final LcOrderService lcOrderService;
    private final LcLogService lcLogService;

    @Override
    public List<LcInbound> view(Integer orderId) {
        LambdaQueryWrapper<LcInbound> wrapper = Wrappers.<LcInbound>lambdaQuery();
        wrapper.eq(LcInbound::getOrderId, orderId).eq(LcInbound::getOrgId, SecurityUtils.getUser().getOrgId());
        List<LcInbound> list = list(wrapper);
        LcOrder lcOrder = lcOrderService.getById(orderId);
        if (list.size() == 0) {
            LcInbound lcInbound = new LcInbound();
            lcInbound.setOrderChargeWeight(lcOrder.getPlanChargeWeight());
            lcInbound.setOrderDimensions(lcOrder.getPlanDensity());
            lcInbound.setOrderGrossWeight(lcOrder.getPlanWeight());
            lcInbound.setOrderPieces(lcOrder.getPlanPieces());
            lcInbound.setOrderSize(lcOrder.getPlanDimensions());
            lcInbound.setOrderVolume(lcOrder.getPlanVolume());
            list.add(lcInbound);
        }
        list.get(0).setOrderVolumeWeight(list.get(0).getOrderVolume() == null ? BigDecimal.ZERO : list.get(0).getOrderVolume().multiply(BigDecimal.valueOf(1000000)).divide(BigDecimal.valueOf(6000), 3, BigDecimal.ROUND_HALF_UP));
        list.get(0).setCustomerNumber(lcOrder.getCustomerNumber());
        list.get(0).setOrderCode(lcOrder.getOrderCode());
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(LcInbound lcInbound) {

        //校验
        LcOrder lcOrder = lcOrderService.getById(lcInbound.getOrderId());
        if (lcOrder == null) {
            throw new RuntimeException("订单不存在");
        }
        if (StrUtil.isNotBlank(lcOrder.getRowUuid()) && !lcOrder.getRowUuid().equals(lcInbound.getRowUuid())) {
            throw new RuntimeException("当前页面不是最新数据，请刷新再操作");
        }

        if ("财务锁账".equals(lcOrder.getOrderStatus())) {
            throw new RuntimeException("订单已财务锁账，无法出重");
        }

        LambdaQueryWrapper<LcInbound> lcInboundLambdaQueryWrapper = Wrappers.<LcInbound>lambdaQuery();
        lcInboundLambdaQueryWrapper.eq(LcInbound::getOrderId, lcInbound.getOrderId()).eq(LcInbound::getOrgId, SecurityUtils.getUser().getOrgId());
        if (list(lcInboundLambdaQueryWrapper).size() > 0) {
            throw new RuntimeException("订单已出重，无法重复出重");
        }
        //保存出重
        lcInbound.setCreateTime(LocalDateTime.now());
        lcInbound.setCreatorId(SecurityUtils.getUser().getId());
        lcInbound.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        lcInbound.setEditorId(SecurityUtils.getUser().getId());
        lcInbound.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        lcInbound.setEditTime(LocalDateTime.now());
        lcInbound.setOrgId(SecurityUtils.getUser().getOrgId());
        save(lcInbound);

        //更新订单
        lcOrder.setOrderStatus("操作出重");
        lcOrder.setRowUuid(UUID.randomUUID().toString());
        lcOrder.setEditorId(SecurityUtils.getUser().getId());
        lcOrder.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        lcOrder.setEditTime(LocalDateTime.now());
        lcOrder.setConfirmChargeWeight(lcInbound.getOrderChargeWeight());
        lcOrder.setConfirmPieces(lcInbound.getOrderPieces());
        lcOrder.setConfirmVolume(lcInbound.getOrderVolume());
        lcOrder.setConfirmWeight(lcInbound.getOrderGrossWeight());
        lcOrder.setConfirmDensity(lcInbound.getOrderDimensions());
        lcOrderService.updateById(lcOrder);
        //日志
        LcLog lcLog = new LcLog();
        lcLog.setPageName("操作出重");
        lcLog.setPageFunction("保存出重");
        lcLog.setBusinessScope("LC");
        lcLog.setOrderNumber(lcOrder.getOrderCode());
        lcLog.setLogRemark(this.getLogRemarkForSave(lcOrder, lcInbound));
        lcLog.setOrderId(lcOrder.getOrderId());
        lcLog.setOrderUuid(lcOrder.getOrderUuid());
        lcLogService.insert(lcLog);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modify(LcInbound lcInbound) {
        //校验
        LcOrder lcOrder = lcOrderService.getById(lcInbound.getOrderId());
        if (lcOrder == null) {
            throw new RuntimeException("订单不存在");
        }
        if (StrUtil.isNotBlank(lcOrder.getRowUuid()) && !lcOrder.getRowUuid().equals(lcInbound.getRowUuid())) {
            throw new RuntimeException("当前页面不是最新数据，请刷新再操作");
        }

        if ("财务锁账".equals(lcOrder.getOrderStatus())) {
            throw new RuntimeException("订单已财务锁账，无法修改出重");
        }
        //修改出重
        lcInbound.setEditorId(SecurityUtils.getUser().getId());
        lcInbound.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        lcInbound.setEditTime(LocalDateTime.now());
        updateById(lcInbound);

        //日志
        LcLog lcLog = new LcLog();
        lcLog.setPageName("操作出重");
        lcLog.setPageFunction("修改出重");
        lcLog.setBusinessScope("LC");
        lcLog.setOrderNumber(lcOrder.getOrderCode());
        lcLog.setLogRemark(this.getLogRemarkForModify(lcOrder, lcInbound));
        lcLog.setOrderId(lcOrder.getOrderId());
        lcLog.setOrderUuid(lcOrder.getOrderUuid());
        lcLogService.insert(lcLog);

        //更改订单信息
        lcOrder.setRowUuid(UUID.randomUUID().toString());
        lcOrder.setEditorId(SecurityUtils.getUser().getId());
        lcOrder.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        lcOrder.setEditTime(LocalDateTime.now());
        lcOrder.setConfirmChargeWeight(lcInbound.getOrderChargeWeight());
        lcOrder.setConfirmPieces(lcInbound.getOrderPieces());
        lcOrder.setConfirmVolume(lcInbound.getOrderVolume());
        lcOrder.setConfirmWeight(lcInbound.getOrderGrossWeight());
        lcOrder.setConfirmDensity(lcInbound.getOrderDimensions());
        lcOrderService.updateById(lcOrder);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer inboundId, String rowUuid) {
        LcInbound lcInbound = getById(inboundId);
        //校验
        if (lcInbound == null) {
            throw new RuntimeException("数据已变更，该订单未出重");
        }
        LcOrder lcOrder = lcOrderService.getById(lcInbound.getOrderId());
        if (lcOrder == null) {
            throw new RuntimeException("订单不存在");
        }
        if (StrUtil.isNotBlank(lcOrder.getRowUuid()) && !lcOrder.getRowUuid().equals(rowUuid)) {
            throw new RuntimeException("当前页面不是最新数据，请刷新再操作");
        }

        if ("财务锁账".equals(lcOrder.getOrderStatus())) {
            throw new RuntimeException("订单已财务锁账，无法删除出重");
        }

        //删除出重
        removeById(inboundId);

        //清空订单实际
        lcOrder.setOrderStatus("订单创建");
        lcOrder.setRowUuid(UUID.randomUUID().toString());
        lcOrder.setEditorId(SecurityUtils.getUser().getId());
        lcOrder.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        lcOrder.setEditTime(LocalDateTime.now());
        lcOrder.setConfirmChargeWeight(null);
        lcOrder.setConfirmPieces(null);
        lcOrder.setConfirmVolume(null);
        lcOrder.setConfirmWeight(null);
        lcOrder.setConfirmDensity(null);
        lcOrderService.updateById(lcOrder);

        //日志
        LcLog lcLog = new LcLog();
        lcLog.setPageName("操作出重");
        lcLog.setPageFunction("删除出重");
        lcLog.setBusinessScope("LC");
        lcLog.setOrderNumber(lcOrder.getOrderCode());
        lcLog.setOrderId(lcOrder.getOrderId());
        lcLog.setOrderUuid(lcOrder.getOrderUuid());
        lcLogService.insert(lcLog);
    }

    private String getLogRemarkForSave(LcOrder lcOrder, LcInbound lcInbound) {
        StringBuffer result = new StringBuffer();
        result.append("件数: ").append(lcOrder.getPlanPieces() == null ? "" : lcOrder.getPlanPieces().toString()).append(" / ").append(lcInbound.getOrderPieces().toString());
        result.append("  毛重: ").append(lcOrder.getPlanWeight() == null ? "" : FormatUtils.formatWithQWFNoBit(lcOrder.getPlanWeight())).append(" / ").append(FormatUtils.formatWithQWFNoBit(lcInbound.getOrderGrossWeight()));
        result.append("  体积: ").append(lcOrder.getPlanVolume() == null ? "" : FormatUtils.formatWithQWFNoBit(lcOrder.getPlanVolume())).append(" / ").append(FormatUtils.formatWithQWFNoBit(lcInbound.getOrderVolume()));
        result.append("  计重: ").append(lcOrder.getPlanChargeWeight() == null ? "" : FormatUtils.formatWithQWFNoBit(lcOrder.getPlanChargeWeight())).append(" / ").append(FormatUtils.formatWithQWFNoBit(lcInbound.getOrderChargeWeight()));
        return result.toString();
    }

    private String getLogRemarkForModify(LcOrder lcOrder, LcInbound lcInbound) {
        StringBuffer result = new StringBuffer();
        result.append("件数: ").append(lcOrder.getConfirmPieces() == null ? "" : lcOrder.getConfirmPieces().toString()).append(" -> ").append(lcInbound.getOrderPieces().toString());
        result.append("  毛重: ").append(lcOrder.getConfirmWeight() == null ? "" : FormatUtils.formatWithQWFNoBit(lcOrder.getConfirmWeight())).append(" -> ").append(FormatUtils.formatWithQWFNoBit(lcInbound.getOrderGrossWeight()));
        result.append("  体积: ").append(lcOrder.getConfirmVolume() == null ? "" : FormatUtils.formatWithQWFNoBit(lcOrder.getConfirmVolume())).append(" -> ").append(FormatUtils.formatWithQWFNoBit(lcInbound.getOrderVolume()));
        result.append("  计重: ").append(lcOrder.getConfirmChargeWeight() == null ? "" : FormatUtils.formatWithQWFNoBit(lcOrder.getConfirmChargeWeight())).append(" -> ").append(FormatUtils.formatWithQWFNoBit(lcInbound.getOrderChargeWeight()));
        return result.toString();
    }
}
