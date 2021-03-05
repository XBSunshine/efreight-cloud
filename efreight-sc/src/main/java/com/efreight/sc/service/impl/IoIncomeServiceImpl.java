package com.efreight.sc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.common.remoteVo.IncomeCostList;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.entity.*;
import com.efreight.sc.dao.IoIncomeMapper;
import com.efreight.sc.dao.LcIncomeMapper;
import com.efreight.sc.service.IoIncomeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.sc.service.IoCostService;
import com.efreight.sc.service.IoLogService;
import com.efreight.sc.service.IoOrderService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * IO 费用录入 应收 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
@Service
@AllArgsConstructor
public class IoIncomeServiceImpl extends ServiceImpl<IoIncomeMapper, IoIncome> implements IoIncomeService {

    private final IoOrderService ioOrderService;

    private final IoLogService ioLogService;

    private final IoCostService ioCostService;
    private final LcIncomeMapper lcIncomeMapper;

    @Override
    public List<IoIncome> getList(Integer orderId) {
        LambdaQueryWrapper<IoIncome> ioIncomeWrapper = Wrappers.<IoIncome>lambdaQuery();
        ioIncomeWrapper.eq(IoIncome::getOrderId, orderId).eq(IoIncome::getOrgId, SecurityUtils.getUser().getOrgId());
        List<IoIncome> list = list(ioIncomeWrapper);
        return list;
    }

    @Override
    public void modify(IoIncome ioIncome) {
        IoIncome income = getById(ioIncome.getIncomeId());
        if (income == null) {
            throw new RuntimeException("收入不存在");
        }
        if (StrUtil.isNotBlank(income.getRowUuid()) && !income.getRowUuid().equals(ioIncome.getRowUuid())) {
            throw new RuntimeException("当前页面数据已更新，请刷新再操作");
        }
        LambdaUpdateWrapper<IoIncome> updateWrapper = Wrappers.<IoIncome>lambdaUpdate();
        updateWrapper.isNull(IoIncome::getFinancialDate).eq(IoIncome::getIncomeId, ioIncome.getIncomeId()).isNull(IoIncome::getDebitNoteId);
        ioIncome.setEditorId(SecurityUtils.getUser().getId());
        ioIncome.setEditTime(LocalDateTime.now());
        ioIncome.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        ioIncome.setRowUuid(UUID.randomUUID().toString());
        update(ioIncome, updateWrapper);
    }

    @Override
    public void insert(IoIncome ioIncome) {
        ioIncome.setOrgId(SecurityUtils.getUser().getOrgId());
        ioIncome.setCreateTime(LocalDateTime.now());
        ioIncome.setCreatorId(SecurityUtils.getUser().getId());
        ioIncome.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        ioIncome.setEditorId(SecurityUtils.getUser().getId());
        ioIncome.setEditTime(LocalDateTime.now());
        ioIncome.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        ioIncome.setRowUuid(UUID.randomUUID().toString());
        save(ioIncome);
    }

    @Override
    public void delete(Integer incomeId) {
        removeById(incomeId);
    }

    @Override
    public IoIncome view(Integer incomeId) {
        return getById(incomeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrderIncomeAndCost(IncomeCostList<IoIncome,IoCost> incomeCostList) {
        IoOrder ioOrder = ioOrderService.getById(incomeCostList.getOrderId());
        if (ioOrder == null) {
            throw new RuntimeException("该订单不存在");
        }

        //日志
        IoLog logBean = new IoLog();
        logBean.setPageName("费用录入");
        logBean.setPageFunction("保存费用");
        logBean.setLogRemark("");
        logBean.setBusinessScope(incomeCostList.getBusinessScope());
        logBean.setOrderNumber(ioOrder.getOrderCode());
        logBean.setOrderId(ioOrder.getOrderId());
        logBean.setOrderUuid(ioOrder.getOrderUuid());
        ioLogService.insert(logBean);

        //保存应收
        StringBuffer flag1 = new StringBuffer();

        incomeCostList.getIncomeList().stream().forEach(ioIncome -> {
            IoIncome income = new IoIncome();
            BeanUtils.copyProperties(ioIncome, income);
            if (ioIncome.getIncomeId() == null) {
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

        incomeCostList.getCostList().stream().forEach(ioCost -> {
            IoCost cost = new IoCost();
            BeanUtils.copyProperties(ioCost, cost);
            if (cost.getCostId() == null) {
                //增加
                ioCostService.insert(cost);
                flag2.append("1");
            } else {
                //修改
                ioCostService.modify(cost);
            }
        });

        //删除相应费用
        incomeCostList.getIncomeDeleteList().stream().forEach(ioIncome -> {
            delete(ioIncome.getIncomeId());
        });
        incomeCostList.getCostDeleteList().stream().forEach(ioCost -> {
            ioCostService.delete(ioCost.getCostId());
        });

        //状态更新
//        if (StrUtil.isNotBlank(flag1.toString())) {
//            if ("核销完毕".equals(ioOrder.getIncomeStatus())) {
//                ioOrder.setIncomeStatus("部分核销");
//                ioOrderService.modify(ioOrder);
//            }
//        } else if (incomeCostList.getIncomeDeleteList().size() > 0) {
//            List<Map<String, Object>> billList = baseMapper.getOrderBill(SecurityUtils.getUser().getOrgId(), incomeCostList.getOrderId());
//
//            int incomeStatus = 1;
//            for (int i = 0; i < billList.size(); i++) {
//                Map<String, Object> bill = billList.get(i);
//                if (bill.get("writeoff_complete") == null || (Integer) bill.get("writeoff_complete") == 0) {
//                    incomeStatus = 0;
//                    break;
//                }
//            }
//            LambdaQueryWrapper<IoIncome> ioIncomeLambdaQueryWrapper = Wrappers.<IoIncome>lambdaQuery();
//            ioIncomeLambdaQueryWrapper.eq(IoIncome::getOrderId, incomeCostList.getOrderId()).eq(IoIncome::getOrgId, SecurityUtils.getUser().getOrgId()).isNull(IoIncome::getDebitNoteId);
//            List<IoIncome> listNoBill = list(ioIncomeLambdaQueryWrapper);
//
//            LambdaQueryWrapper<IoIncome> ioIncomeWrapper = Wrappers.<IoIncome>lambdaQuery();
//            ioIncomeWrapper.eq(IoIncome::getOrderId, incomeCostList.getOrderId()).eq(IoIncome::getOrgId, SecurityUtils.getUser().getOrgId()).isNotNull(IoIncome::getDebitNoteId);
//            List<IoIncome> listCompleteBill = list(ioIncomeWrapper);
//            if (listNoBill.size() == 0 && listCompleteBill.size() > 0 && incomeStatus == 1) {
//                ioOrder.setIncomeStatus("核销完毕");
//                ioOrderService.modify(ioOrder);
//            }
//        }
        List<Map> listMap = lcIncomeMapper.getOrderIncomeStatus(SecurityUtils.getUser().getOrgId(), incomeCostList.getOrderId().toString(),"IO");
		if(listMap!=null&&listMap.size()>0) {
			for(Map map:listMap) {
//				lcIncomeMapper.updateOrderIncomeStatus(Integer.valueOf(map.get("org_id").toString()),Integer.valueOf(map.get("order_id").toString()),map.get("income_status").toString(),UUID.randomUUID().toString(),"IO");
				ioOrder.setIncomeStatus(map.get("income_status").toString());
			}
		}
//        if (StrUtil.isNotBlank(flag2.toString())) {
//            IoOrder order = ioOrderService.getById(incomeCostList.getOrderId());
//            if ("核销完毕".equals(order.getCostStatus())) {
//                order.setCostStatus("部分核销");
//                ioOrderService.modify(order);
//            }
//        } else {
//            LambdaQueryWrapper<IoCost> ioCostLambdaQueryWrapper = Wrappers.<IoCost>lambdaQuery();
//            ioCostLambdaQueryWrapper.eq(IoCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(IoCost::getOrderId, incomeCostList.getOrderId());
//            List<IoCost> list = ioCostService.list(ioCostLambdaQueryWrapper);
//            boolean ifCostCompleteWriteoff = true;
//            try {
//                if (list.size() > 0) {
//                    list.stream().forEach(ioCost -> {
//                        if (ioCost.getCostAmountWriteoff() == null || ioCost.getCostAmount().compareTo(ioCost.getCostAmountWriteoff()) != 0 || ioCost.getCostAmount().compareTo(BigDecimal.ZERO) == 0) {
//                            throw new RuntimeException();
//                        }
//                    });
//                } else {
//                    ifCostCompleteWriteoff = false;
//                }
//            } catch (Exception e) {
//                ifCostCompleteWriteoff = false;
//            }
//            if (ifCostCompleteWriteoff) {
//                IoOrder order = ioOrderService.getById(incomeCostList.getOrderId());
//                order.setCostStatus("核销完毕");
//                ioOrderService.modify(order);
//            }
//        }
        //修改订单成本状态
        ioOrder.setRowUuid(UUID.randomUUID().toString());
		ioOrder.setCostStatus(ioCostService.getOrderCostStatusForIO(incomeCostList.getOrderId()));
		ioOrderService.updateById(ioOrder);
    }
}
