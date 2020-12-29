package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.afbase.dao.OrderInquiryMapper;
import com.efreight.afbase.entity.OrderInquiry;
import com.efreight.afbase.entity.OrderInquiryQuotation;
import com.efreight.afbase.dao.OrderInquiryQuotationMapper;
import com.efreight.afbase.service.OrderInquiryQuotationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.service.OrderInquiryService;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <p>
 * AF 询价：报价单明细 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-05-28
 */
@Service
@AllArgsConstructor
public class OrderInquiryQuotationServiceImpl extends ServiceImpl<OrderInquiryQuotationMapper, OrderInquiryQuotation> implements OrderInquiryQuotationService {

    private final OrderInquiryMapper orderInquiryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInquiryQuotation(List<OrderInquiryQuotation> orderInquiryQuotations) {
        //校验
        OrderInquiry orderInquiry = orderInquiryMapper.selectById(orderInquiryQuotations.get(0).getOrderInquiryId());
        if (orderInquiry == null) {
            throw new RuntimeException("询价单不出在,无法报价");
        }
        if (orderInquiry.getOrderId() != null || "已关闭".equals(orderInquiry.getOrderInquiryStatus())) {
            throw new RuntimeException("询价单已经失效,无法报价");
        }
        //保存必要数据
        orderInquiryQuotations.stream().forEach(orderInquiryQuotation -> {
            orderInquiryQuotation.setIsValid(true);
            orderInquiryQuotation.setQuotationSelected(false);
            orderInquiryQuotation.setOrgId(orderInquiry.getOrgId());
            orderInquiryQuotation.setCreatTime(LocalDateTime.now());
            orderInquiryQuotation.setEditTime(orderInquiryQuotation.getCreatTime());
        });
        //保存
        saveBatch(orderInquiryQuotations);
        //更新询价单UUID
        orderInquiry.setRowUuid(UUID.randomUUID().toString());
        orderInquiryMapper.updateById(orderInquiry);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modify(List<OrderInquiryQuotation> orderInquiryQuotations) {
        //校验
        OrderInquiry orderInquiry = orderInquiryMapper.selectById(orderInquiryQuotations.get(0).getOrderInquiryId());
        if (StrUtil.isNotBlank(orderInquiry.getRowUuid()) && !orderInquiry.getRowUuid().equals(orderInquiryQuotations.get(0).getRowUuid())) {
            throw new RuntimeException("询价单已发生更改，当前页面不是最新数据，请刷新页面再操作");
        }
        if (!"已创建".equals(orderInquiry.getOrderInquiryStatus())) {
            throw new RuntimeException("询价单不是已创建状态，无法暂存");
        }

        //如何报价明细为空，通过集合传递询价单信息，同时清空集合继续向下执行
        if (orderInquiryQuotations.size() == 1 && orderInquiryQuotations.get(0).getOrderInquiryQuotationId() == null) {
            orderInquiryQuotations.clear();
        }
        //封装删除和修改的数据
        List<OrderInquiryQuotation> deleteList = new ArrayList<>();
        List<OrderInquiryQuotation> modifyList = new ArrayList<>();
        LambdaQueryWrapper<OrderInquiryQuotation> inquiryQuotationWrapper = Wrappers.<OrderInquiryQuotation>lambdaQuery();
        inquiryQuotationWrapper.eq(OrderInquiryQuotation::getIsValid, true).eq(OrderInquiryQuotation::getOrderInquiryId, orderInquiry.getOrderInquiryId()).eq(OrderInquiryQuotation::getOrgId, SecurityUtils.getUser().getOrgId());
        List<OrderInquiryQuotation> list = list(inquiryQuotationWrapper);
        LocalDateTime now = LocalDateTime.now();
        list.stream().forEach(orderInquiryQuotation -> {
            List<OrderInquiryQuotation> sameList = null;
            if (orderInquiryQuotations.size() != 0) {
                sameList = orderInquiryQuotations.stream().filter(quotation -> quotation.getOrderInquiryQuotationId().equals(orderInquiryQuotation.getOrderInquiryQuotationId())).collect(Collectors.toList());
            }
            if (sameList == null || sameList.size() == 0) {
                orderInquiryQuotation.setIsValid(false);
                orderInquiryQuotation.setEditTime(now);
                orderInquiryQuotation.setEditorId(SecurityUtils.getUser().getId());
                orderInquiryQuotation.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                deleteList.add(orderInquiryQuotation);
            } else {
                if (!sameList.get(0).getQuotationSelected().equals(orderInquiryQuotation.getQuotationSelected())) {
                    orderInquiryQuotation.setQuotationSelected(sameList.get(0).getQuotationSelected());
                    orderInquiryQuotation.setEditTime(now);
                    orderInquiryQuotation.setEditorId(SecurityUtils.getUser().getId());
                    orderInquiryQuotation.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                    modifyList.add(orderInquiryQuotation);
                }
            }
        });

        deleteList.addAll(modifyList);
        //暂存
        if (deleteList.size() != 0) {
            updateBatchById(deleteList);
        }
    }
}
