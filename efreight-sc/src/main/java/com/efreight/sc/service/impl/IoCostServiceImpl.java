package com.efreight.sc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.entity.IoCost;
import com.efreight.sc.dao.IoCostMapper;
import com.efreight.sc.entity.IoCost;
import com.efreight.sc.service.IoCostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * IO 费用录入 成本 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
@Service
public class IoCostServiceImpl extends ServiceImpl<IoCostMapper, IoCost> implements IoCostService {

    @Override
    public List<IoCost> getList(Integer orderId) {
        LambdaQueryWrapper<IoCost> ioCostWrapper = Wrappers.<IoCost>lambdaQuery();
        ioCostWrapper.eq(IoCost::getOrgId, SecurityUtils.getUser().getOrgId()).eq(IoCost::getOrderId, orderId);
        List<IoCost> list = list(ioCostWrapper);
        return list;
    }

    @Override
    public void modify(IoCost ioCost) {
        IoCost cost = getById(ioCost.getCostId());
        if (cost == null) {
            throw new RuntimeException("成本不存在");
        }
        if (StrUtil.isNotBlank(cost.getRowUuid()) && !cost.getRowUuid().equals(ioCost.getRowUuid())) {
            throw new RuntimeException("当前页面数据已更新，请刷新再操作");
        }
        if (!checkCostIfCompleteBill(ioCost.getCostId())) {
            LambdaUpdateWrapper<IoCost> updateWrapper = Wrappers.<IoCost>lambdaUpdate();
            updateWrapper.isNull(IoCost::getFinancialDate).eq(IoCost::getCostId, ioCost.getCostId());
            ioCost.setOrgId(SecurityUtils.getUser().getOrgId());
            ioCost.setEditorId(SecurityUtils.getUser().getId());
            ioCost.setEditTime(LocalDateTime.now());
            ioCost.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            ioCost.setRowUuid(UUID.randomUUID().toString());
            update(ioCost, updateWrapper);
        }
    }

    @Override
    public void insert(IoCost ioCost) {
        ioCost.setOrgId(SecurityUtils.getUser().getOrgId());
        ioCost.setCreateTime(LocalDateTime.now());
        ioCost.setCreatorId(SecurityUtils.getUser().getId());
        ioCost.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());

        ioCost.setEditorId(SecurityUtils.getUser().getId());
        ioCost.setEditTime(LocalDateTime.now());
        ioCost.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        ioCost.setRowUuid(UUID.randomUUID().toString());
        save(ioCost);
    }

    @Override
    public void delete(Integer costId) {
        removeById(costId);
    }

    @Override
    public IoCost view(Integer costId) {
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
