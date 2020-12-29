package com.efreight.sc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.entity.LcCost;
import com.efreight.sc.dao.LcCostMapper;
import com.efreight.sc.service.LcCostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * LC 费用录入 成本 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
@Service
public class LcCostServiceImpl extends ServiceImpl<LcCostMapper, LcCost> implements LcCostService {

    @Override
    public List<LcCost> getList(Integer orderId) {
        LambdaQueryWrapper<LcCost> lcCostWrapper = Wrappers.<LcCost>lambdaQuery();
        lcCostWrapper.eq(LcCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(LcCost::getOrderId, orderId);
        List<LcCost> list = list(lcCostWrapper);
        return list;
    }

    @Override
    public void modify(LcCost lcCost) {
        LcCost cost = getById(lcCost.getCostId());
        if (cost == null) {
            throw new RuntimeException("成本不存在");
        }
        if (StrUtil.isNotBlank(cost.getRowUuid()) && !cost.getRowUuid().equals(lcCost.getRowUuid())) {
            throw new RuntimeException("当前页面数据已更新，请刷新再操作");
        }
        if (!checkCostIfCompleteBill(lcCost.getCostId())) {
            LambdaUpdateWrapper<LcCost> updateWrapper = Wrappers.<LcCost>lambdaUpdate();
            updateWrapper.isNull(LcCost::getFinancialDate).eq(LcCost::getCostId, lcCost.getCostId());
            lcCost.setOrgId(SecurityUtils.getUser().getOrgId());
            lcCost.setEditorId(SecurityUtils.getUser().getId());
            lcCost.setEditTime(LocalDateTime.now());
            lcCost.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            lcCost.setRowUuid(UUID.randomUUID().toString());
            update(lcCost, updateWrapper);
        }
    }

    @Override
    public void insert(LcCost lcCost) {
        lcCost.setOrgId(SecurityUtils.getUser().getOrgId());
        lcCost.setCreateTime(LocalDateTime.now());
        lcCost.setCreatorId(SecurityUtils.getUser().getId());
        lcCost.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());

        lcCost.setEditorId(SecurityUtils.getUser().getId());
        lcCost.setEditTime(LocalDateTime.now());
        lcCost.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        lcCost.setRowUuid(UUID.randomUUID().toString());
        save(lcCost);
    }

    @Override
    public void delete(Integer costId) {
        removeById(costId);
    }

    @Override
    public LcCost view(Integer costId) {
        return getById(costId);
    }

    private Boolean checkCostIfCompleteBill(Integer costId) {
        try {
            List<Map<String, Object>> list = baseMapper.getPaymentDetailByCostId(costId, SecurityUtils.getUser().getOrgId());
            if (list.size() > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
