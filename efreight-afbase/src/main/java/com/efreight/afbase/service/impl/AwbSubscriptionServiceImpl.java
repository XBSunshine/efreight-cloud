package com.efreight.afbase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.AwbSubscriptionMapper;
import com.efreight.afbase.entity.AfAwbRoute;
import com.efreight.afbase.entity.AwbSubscription;
import com.efreight.afbase.entity.view.Subscribe;
import com.efreight.afbase.entity.view.SubscribeVO;
import com.efreight.afbase.service.AfAwbRouteService;
import com.efreight.afbase.service.AwbSubscriptionService;
import com.efreight.common.security.constant.CommonConstants;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.common.security.vo.OrgServiceMealConfigVo;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * AF 运单号 我的订阅 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-06-29
 */
@Service
@AllArgsConstructor
public class AwbSubscriptionServiceImpl extends ServiceImpl<AwbSubscriptionMapper, AwbSubscription> implements AwbSubscriptionService {

    private final AfAwbRouteService afAwbRouteService;

    @Override
    public List<AwbSubscription> getList(String businessScope) {
        Assert.notNull(businessScope, "业务类型不能为空");
        LambdaQueryWrapper<AwbSubscription> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AwbSubscription::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AwbSubscription::getUserId, SecurityUtils.getUser().getId()).eq(AwbSubscription::getSubscriptionFrom, "货物追踪").orderByDesc(AwbSubscription::getAwbSubscriptionId);
        wrapper.eq(AwbSubscription::getIsDisplay, true);
        wrapper.eq(AwbSubscription::getBusinessScope, businessScope);
        wrapper.last(" limit 40");
        return list(wrapper);
    }

    @Override
    public Boolean getRoute(String awbNumber) {
        LambdaQueryWrapper<AfAwbRoute> wrapper = Wrappers.<AfAwbRoute>lambdaQuery();
        wrapper.eq(AfAwbRoute::getAwbNumber, awbNumber);
        List<AfAwbRoute> list = afAwbRouteService.list(wrapper);
        return list.isEmpty();
    }

    @Override
    public void deleteAwbSubscription(Integer awbSubscriptionId) {
        AwbSubscription awbSubscription = new AwbSubscription();
        awbSubscription.setAwbSubscriptionId(awbSubscriptionId);
        awbSubscription.setIsDisplay(false);
        updateById(awbSubscription);
    }

    @Override
    public synchronized boolean cargoTrackingSubscribe(Subscribe subscribe) {
        String awbNumber = subscribe.getAwbNumber(),
                hawbNumber = subscribe.getHawbNumber(),
                businessScope = subscribe.getBusinessScope();
        Assert.notNull(subscribe.getOrgId(), "企业信息不能为空");
        Assert.notNull(subscribe.getUserId(), "用户信息不能为空");

        // 1, 订阅
        boolean isFirst = afAwbRouteService.saveRouteInfo(awbNumber, hawbNumber, businessScope);
        // 2，检查企业用量
        this.checkOrgAdditionalService(subscribe);
        // 3，根据检查来记录个人订阅历史
        AwbSubscription awbSubscription = new AwbSubscription();
        awbSubscription.setAwbNumber(awbNumber);
        awbSubscription.setHawbNumber(hawbNumber);
        awbSubscription.setBusinessScope(businessScope);
        awbSubscription.setOrgId(subscribe.getOrgId());
        awbSubscription.setUserId(subscribe.getUserId());
        awbSubscription.setCreateIp(subscribe.getCreateIp());
        this.recordHistory(awbSubscription);
        return isFirst;
    }

    @Override
    public OrgServiceMealConfigVo orgAdditionalService(Integer orgId, String serviceType) {
        return this.baseMapper.orgAdditionalServiceRemaining(orgId, serviceType);
    }

    @Override
    public void checkOrgAdditionalService(Subscribe subscribe) {
        Assert.notNull(subscribe.getOrgId(), "企业信息不能为空");
        Assert.notNull(subscribe.getUserId(), "用户信息不能为空");

        LambdaQueryWrapper<AwbSubscription> awbSubscriptionWrapper = Wrappers.lambdaQuery();
        awbSubscriptionWrapper.eq(AwbSubscription::getAwbNumber, subscribe.getAwbNumber())
                .eq(AwbSubscription::getBusinessScope, subscribe.getBusinessScope())
                .eq(AwbSubscription::getOrgId, subscribe.getOrgId())
                .eq(AwbSubscription::getUserId, subscribe.getUserId());
        if(StringUtils.isNotBlank(subscribe.getHawbNumber())){
            awbSubscriptionWrapper.eq(AwbSubscription::getHawbNumber, subscribe.getHawbNumber());
        }else{
            awbSubscriptionWrapper.isNull(AwbSubscription::getHawbNumber);
        }
        List<AwbSubscription> awbSubscriptionList = list(awbSubscriptionWrapper);
        if(awbSubscriptionList.size() > 0){
            return;
        }
        Integer orgId = subscribe.getOrgId();
        OrgServiceMealConfigVo orgServiceMealConfig = this.orgAdditionalService(orgId, CommonConstants.ORG_ADDITIONAL_SERVICE_TYPE.TRACK_AE_AI);
        if(null == orgServiceMealConfig){
            return;
        }
        if(orgServiceMealConfig.getRemaining() <= 0){
            throw new IllegalStateException(String.format("您的空运订阅量为：%d，已用 %d，<br />不能进行查询，请联系客服。", orgServiceMealConfig.getServiceNumberMax(), orgServiceMealConfig.getServiceNumberUsed()));
        }
    }

    @Override
    public void updateAdditionalServiceRemaining(Integer orgId, String serviceType) {
        int useCount = this.baseMapper.countSubscriptHistory(orgId);
        this.baseMapper.updateOrgAdditionalServiceRemaining(orgId, serviceType, useCount);
    }

    @Override
    public IPage<SubscribeVO> pageSubscribe(Page page, Integer orgId, String date) {
        Assert.hasLength(date, "日期不能为空");
        return this.baseMapper.pageSubscribe(page, orgId, date);
    }

    @Override
    public List<SubscribeVO> exportSubscribe(Integer orgId, String date) {
        Assert.hasLength(date, "日期不能为空");
        return this.baseMapper.exportSubscribe(orgId, date);
    }

    @Transactional(rollbackFor = Exception.class)
    public void recordHistory(AwbSubscription subscription){

        Assert.notNull(subscription.getOrgId(), "企业信息不能为空");
        Assert.notNull(subscription.getUserId(), "用户信息不能为空");

        LambdaQueryWrapper<AwbSubscription> awbSubscriptionWrapper = Wrappers.lambdaQuery();
        awbSubscriptionWrapper.eq(AwbSubscription::getAwbNumber, subscription.getAwbNumber())
                .eq(AwbSubscription::getBusinessScope, subscription.getBusinessScope())
                .eq(AwbSubscription::getOrgId, subscription.getOrgId())
                .eq(AwbSubscription::getIsDisplay, true)
                .eq(AwbSubscription::getUserId, subscription.getUserId());

        if(StringUtils.isNotBlank(subscription.getHawbNumber())){
            awbSubscriptionWrapper.eq(AwbSubscription::getHawbNumber, subscription.getHawbNumber());
        }else{
            subscription.setHawbNumber(null);
            awbSubscriptionWrapper.isNull(AwbSubscription::getHawbNumber);
        }
        List<AwbSubscription> awbSubscriptionList = list(awbSubscriptionWrapper);
        if (awbSubscriptionList.size() == 0) {
            subscription.setCreateTime(LocalDateTime.now());
            subscription.setOrgId(subscription.getOrgId());
            subscription.setUserId(subscription.getUserId());
            subscription.setSubscriptionFrom("货物追踪");
            subscription.setIsDisplay(true);
            save(subscription);
            // 4，更新企业用量信息
            this.updateAdditionalServiceRemaining(subscription.getOrgId(), CommonConstants.ORG_ADDITIONAL_SERVICE_TYPE.TRACK_AE_AI);
        }

    }

}
