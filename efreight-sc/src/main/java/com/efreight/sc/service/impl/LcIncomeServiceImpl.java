package com.efreight.sc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.common.remoteVo.IncomeCostList;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.entity.LcCost;
import com.efreight.sc.entity.LcIncome;
import com.efreight.sc.dao.LcIncomeMapper;
import com.efreight.sc.entity.LcLog;
import com.efreight.sc.entity.LcOrder;
import com.efreight.sc.service.LcCostService;
import com.efreight.sc.service.LcIncomeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.sc.service.LcLogService;
import com.efreight.sc.service.LcOrderService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * LC 费用录入 应收 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
@Service
@AllArgsConstructor
public class LcIncomeServiceImpl extends ServiceImpl<LcIncomeMapper, LcIncome> implements LcIncomeService {

    private final LcOrderService lcOrderService;

    private final LcLogService lcLogService;

    private final LcCostService lcCostService;

    @Override
    public List<LcIncome> getList(Integer orderId) {
        LambdaQueryWrapper<LcIncome> lcIncomeWrapper = Wrappers.<LcIncome>lambdaQuery();
        lcIncomeWrapper.eq(LcIncome::getOrderId, orderId).eq(LcIncome::getOrgId, SecurityUtils.getUser().getOrgId());
        List<LcIncome> list = list(lcIncomeWrapper);
        return list;
    }

    @Override
    public void modify(LcIncome lcIncome) {
        LcIncome income = getById(lcIncome.getIncomeId());
        if (income == null) {
            throw new RuntimeException("收入不存在");
        }
        if (StrUtil.isNotBlank(income.getRowUuid()) && !income.getRowUuid().equals(lcIncome.getRowUuid())) {
            throw new RuntimeException("当前页面数据已更新，请刷新再操作");
        }
        LambdaUpdateWrapper<LcIncome> updateWrapper = Wrappers.<LcIncome>lambdaUpdate();
        updateWrapper.isNull(LcIncome::getFinancialDate).eq(LcIncome::getIncomeId, lcIncome.getIncomeId()).isNull(LcIncome::getDebitNoteId);
        lcIncome.setEditorId(SecurityUtils.getUser().getId());
        lcIncome.setEditTime(LocalDateTime.now());
        lcIncome.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        lcIncome.setRowUuid(UUID.randomUUID().toString());
        update(lcIncome, updateWrapper);
    }

    @Override
    public void insert(LcIncome lcIncome) {
        lcIncome.setOrgId(SecurityUtils.getUser().getOrgId());
        lcIncome.setCreateTime(LocalDateTime.now());
        lcIncome.setCreatorId(SecurityUtils.getUser().getId());
        lcIncome.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        lcIncome.setEditorId(SecurityUtils.getUser().getId());
        lcIncome.setEditTime(LocalDateTime.now());
        lcIncome.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        lcIncome.setRowUuid(UUID.randomUUID().toString());
        save(lcIncome);
    }

    @Override
    public void delete(Integer incomeId) {
        removeById(incomeId);
    }

    @Override
    public LcIncome view(Integer incomeId) {
        return getById(incomeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrderIncomeAndCost(IncomeCostList<LcIncome,LcCost> incomeCostList) {
        LcOrder lcOrder = lcOrderService.getById(incomeCostList.getOrderId());
        if (lcOrder == null) {
            throw new RuntimeException("该订单不存在");
        }

        //更改订单状态
        boolean flag = false;
        if ("未录收入".equals(lcOrder.getIncomeStatus()) || "已录收入".equals(lcOrder.getIncomeStatus())) {
            lcOrder.setIncomeStatus(incomeCostList.getIncomeStatus());
            flag = true;
        }
        if ("未录成本".equals(lcOrder.getCostStatus()) || "已录成本".equals(lcOrder.getCostStatus())) {
            lcOrder.setCostStatus(incomeCostList.getCostStatus());
            flag = true;
        }
        if (flag) {
            lcOrderService.modify(lcOrder);
        }

        //日志
        LcLog logBean = new LcLog();
        logBean.setPageName("费用录入");
        logBean.setPageFunction("保存费用");
        logBean.setLogRemark("");
        logBean.setBusinessScope(incomeCostList.getBusinessScope());
        logBean.setOrderNumber(lcOrder.getOrderCode());
        logBean.setOrderId(lcOrder.getOrderId());
        logBean.setOrderUuid(lcOrder.getOrderUuid());
        lcLogService.insert(logBean);

        //保存应收
        StringBuffer flag1 = new StringBuffer();

        incomeCostList.getIncomeList().stream().forEach(lcIncome -> {
            LcIncome income = new LcIncome();
            BeanUtils.copyProperties(lcIncome, income);
            if (lcIncome.getIncomeId() == null) {
                //增加
                insert(income);
                flag1.append("1");
            } else {
                //修改
                modify(income);
            }
        });

        //保存应付
        StringBuffer flag2 = new StringBuffer();

        incomeCostList.getCostList().stream().forEach(lcCost -> {
            LcCost cost = new LcCost();
            BeanUtils.copyProperties(lcCost, cost);
            if (cost.getCostId() == null) {
                //增加
                lcCostService.insert(cost);
                flag2.append("1");
            } else {
                //修改
                lcCostService.modify(cost);
            }
        });

        //删除相应费用
        incomeCostList.getIncomeDeleteList().stream().forEach(lcIncome -> {
            delete(lcIncome.getIncomeId());
        });
        incomeCostList.getCostDeleteList().stream().forEach(lcCost -> {
            lcCostService.delete(lcCost.getCostId());
        });

        //状态更新
        if (StrUtil.isNotBlank(flag1.toString())) {
            if ("核销完毕".equals(lcOrder.getIncomeStatus())) {
                lcOrder.setIncomeStatus("部分核销");
                lcOrderService.modify(lcOrder);
            }
        } else if (incomeCostList.getIncomeDeleteList().size() > 0) {
            List<Map<String, Object>> billList = baseMapper.getOrderBill(SecurityUtils.getUser().getOrgId(), incomeCostList.getOrderId());

            int incomeStatus = 1;
            for (int i = 0; i < billList.size(); i++) {
                Map<String, Object> bill = billList.get(i);
                if (bill.get("writeoff_complete") == null || (Integer) bill.get("writeoff_complete") == 0) {
                    incomeStatus = 0;
                    break;
                }
            }
            LambdaQueryWrapper<LcIncome> lcIncomeLambdaQueryWrapper = Wrappers.<LcIncome>lambdaQuery();
            lcIncomeLambdaQueryWrapper.eq(LcIncome::getOrderId, incomeCostList.getOrderId()).eq(LcIncome::getOrgId, SecurityUtils.getUser().getOrgId()).isNull(LcIncome::getDebitNoteId);
            List<LcIncome> listNoBill = list(lcIncomeLambdaQueryWrapper);

            LambdaQueryWrapper<LcIncome> lcIncomeWrapper = Wrappers.<LcIncome>lambdaQuery();
            lcIncomeWrapper.eq(LcIncome::getOrderId, incomeCostList.getOrderId()).eq(LcIncome::getOrgId, SecurityUtils.getUser().getOrgId()).isNotNull(LcIncome::getDebitNoteId);
            List<LcIncome> listCompleteBill = list(lcIncomeWrapper);
            if (listNoBill.size() == 0 && listCompleteBill.size() > 0 && incomeStatus == 1) {
                lcOrder.setIncomeStatus("核销完毕");
                lcOrderService.modify(lcOrder);
            }
        }
        if (StrUtil.isNotBlank(flag2.toString())) {
            LcOrder order = lcOrderService.getById(incomeCostList.getOrderId());
            if ("核销完毕".equals(order.getCostStatus())) {
                order.setCostStatus("部分核销");
                lcOrderService.modify(order);
            }
        } else {
            LambdaQueryWrapper<LcCost> lcCostLambdaQueryWrapper = Wrappers.<LcCost>lambdaQuery();
            lcCostLambdaQueryWrapper.eq(LcCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(LcCost::getOrderId, incomeCostList.getOrderId());
            List<LcCost> list = lcCostService.list(lcCostLambdaQueryWrapper);
            boolean ifCostCompleteWriteoff = true;
            try {
                if (list.size() > 0) {
                    list.stream().forEach(lcCost -> {
                        if (lcCost.getCostAmountWriteoff() == null || lcCost.getCostAmount().compareTo(lcCost.getCostAmountWriteoff()) != 0 || lcCost.getCostAmount().compareTo(BigDecimal.ZERO) == 0) {
                            throw new RuntimeException();
                        }
                    });
                } else {
                    ifCostCompleteWriteoff = false;
                }
            } catch (Exception e) {
                ifCostCompleteWriteoff = false;
            }
            if (ifCostCompleteWriteoff) {
                LcOrder order = lcOrderService.getById(incomeCostList.getOrderId());
                order.setCostStatus("核销完毕");
                lcOrderService.modify(order);
            }
        }
    }
}
