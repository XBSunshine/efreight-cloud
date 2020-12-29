package com.efreight.sc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.entity.VlOrder;
import com.efreight.sc.entity.VlOrderFiles;
import com.efreight.sc.dao.VlOrderFilesMapper;
import com.efreight.sc.service.VlOrderFilesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.sc.service.VlOrderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * TC 订单管理 订单附件 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-31
 */
@Service
@AllArgsConstructor
public class VlOrderFilesServiceImpl extends ServiceImpl<VlOrderFilesMapper, VlOrderFiles> implements VlOrderFilesService {

    private final VlOrderService vlOrderService;

    @Override
    public List<VlOrderFiles> getList(Integer orderId) {
        LambdaQueryWrapper<VlOrderFiles> lcOrderFilesWrapper = Wrappers.<VlOrderFiles>lambdaQuery();
        lcOrderFilesWrapper.eq(VlOrderFiles::getOrderId, orderId).eq(VlOrderFiles::getOrgId, SecurityUtils.getUser().getOrgId());
        List<VlOrderFiles> list = list(lcOrderFilesWrapper);
        return list;
    }

    @Override
    public void insert(VlOrderFiles vlOrderFiles) {
        VlOrder vlOrder = vlOrderService.getById(vlOrderFiles.getOrderId());
        if ("强制关闭".equals(vlOrder.getOrderStatus())) {
            throw new RuntimeException("当前订单已关闭，无法上传文件");
        }
        if ("完成订单".equals(vlOrder.getOrderStatus())) {
            throw new RuntimeException("当前订单已完成，无法上传文件");
        }
        vlOrderFiles.setCreateTime(LocalDateTime.now());
        vlOrderFiles.setCreatorId(SecurityUtils.getUser().getId());
        vlOrderFiles.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        vlOrderFiles.setOrgId(SecurityUtils.getUser().getOrgId());
        save(vlOrderFiles);
    }

    @Override
    public void modifty(VlOrderFiles vlOrderFiles) {
        VlOrder vlOrder = vlOrderService.getById(getById(vlOrderFiles.getOrderFileId()).getOrderId());
        if ("强制关闭".equals(vlOrder.getOrderStatus())) {
            throw new RuntimeException("当前订单已关闭，无法执行此操作");
        }
        if ("完成订单".equals(vlOrder.getOrderStatus())) {
            throw new RuntimeException("当前订单已完成，无法执行此操作");
        }
        updateById(vlOrderFiles);
    }

    @Override
    public void delete(Integer orderFileId) {
        VlOrder vlOrder = vlOrderService.getById(getById(orderFileId).getOrderId());
        if ("强制关闭".equals(vlOrder.getOrderStatus())) {
            throw new RuntimeException("当前订单已关闭，无法执行此操作");
        }
        if ("完成订单".equals(vlOrder.getOrderStatus())) {
            throw new RuntimeException("当前订单已完成，无法执行此操作");
        }
        removeById(orderFileId);
    }
}
