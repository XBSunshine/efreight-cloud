package com.efreight.afbase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.AfAwbRouteTrackAwbMapper;
import com.efreight.afbase.entity.route.AfAwbRouteTrackAwb;
import com.efreight.afbase.service.AfAwbRouteTrackAwbService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class AfAwbRouteTrackAwbServiceImpl extends ServiceImpl<AfAwbRouteTrackAwbMapper, AfAwbRouteTrackAwb> implements AfAwbRouteTrackAwbService {

    @Override
    public List<AfAwbRouteTrackAwb> getByAwbNumber(String awbNumber) {
        if(StringUtils.isBlank(awbNumber)){
            return Collections.emptyList();
        }
        LambdaQueryWrapper<AfAwbRouteTrackAwb> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(AfAwbRouteTrackAwb::getAwbNumber, awbNumber);
        lambdaQueryWrapper.orderByAsc(AfAwbRouteTrackAwb::getSourceSyscode);
        return this.baseMapper.selectList(lambdaQueryWrapper);
    }
}
