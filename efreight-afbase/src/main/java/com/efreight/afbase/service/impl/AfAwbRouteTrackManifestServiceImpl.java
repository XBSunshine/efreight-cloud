package com.efreight.afbase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.AfAwbRouteTrackManifestMapper;
import com.efreight.afbase.entity.route.AfAwbRouteTrackManifest;
import com.efreight.afbase.service.AfAwbRouteTrackManifestService;
import com.efreight.common.security.constant.CommonConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;

/**
 * @author lc
 * @date 2020/5/6 14:09
 */
@Service
public class AfAwbRouteTrackManifestServiceImpl  extends ServiceImpl<AfAwbRouteTrackManifestMapper, AfAwbRouteTrackManifest> implements AfAwbRouteTrackManifestService {

    @Override
    public List<AfAwbRouteTrackManifest> getByAwbNumber(String awbNumber, String businessScope) {
        if(StringUtils.isBlank(awbNumber)){
            return Collections.emptyList();
        }
        LambdaQueryWrapper<AfAwbRouteTrackManifest> lambdaQueryWrapper = buildQueryWrapper(awbNumber, businessScope);
        lambdaQueryWrapper.orderByAsc(AfAwbRouteTrackManifest::getEventTime);
        return this.baseMapper.selectList(lambdaQueryWrapper);
    }

    @Override
    public List<AfAwbRouteTrackManifest> operationLookList(String awbNumber, String hawbNumber, String businessScope) {
        Assert.hasLength(awbNumber, "主单号不能为空");
        LambdaQueryWrapper<AfAwbRouteTrackManifest> condition = buildQueryWrapper(awbNumber, businessScope);
        if(StringUtils.isNotBlank(hawbNumber)){
            condition.eq(AfAwbRouteTrackManifest::getHawbNumber, hawbNumber);
        }else{
            // and (awb_number is null or awb_number = "")
            condition.and(true, queryWrapper -> queryWrapper.isNull(AfAwbRouteTrackManifest::getHawbNumber).or().eq(AfAwbRouteTrackManifest::getHawbNumber, ""));
        }
        condition.orderByDesc(AfAwbRouteTrackManifest::getSourceSyscode);
        return this.baseMapper.selectList(condition);
    }

    private LambdaQueryWrapper<AfAwbRouteTrackManifest> buildQueryWrapper(String  awbNumber, String businessScope){
        LambdaQueryWrapper<AfAwbRouteTrackManifest> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(AfAwbRouteTrackManifest::getAwbNumber, awbNumber);

        CommonConstants.TRACK_MANIFEST_FLAG flag = getWidthBusinessScope(businessScope);
        if(null != flag){
            lambdaQueryWrapper.and(true, (condition)->condition.eq(AfAwbRouteTrackManifest::getFlag, flag.value()).or().isNull(AfAwbRouteTrackManifest::getFlag));
        }
        return lambdaQueryWrapper;
    }

    private CommonConstants.TRACK_MANIFEST_FLAG getWidthBusinessScope(String businessScope){
        switch (businessScope){
            case "AE":
                return CommonConstants.TRACK_MANIFEST_FLAG.EXPORT;
            case "AI":
                return CommonConstants.TRACK_MANIFEST_FLAG.IMPORT;
            default:
                throw new IllegalStateException("未知业务域!");

        }
    }
}
