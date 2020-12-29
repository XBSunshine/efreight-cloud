package com.efreight.afbase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.AfAwbRouteHawbMapper;
import com.efreight.afbase.entity.route.AfAwbRouteHawb;
import com.efreight.afbase.service.AfAwbRouteHawbService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author lc
 * @date 2020/9/16 14:13
 */
@Service
public class AfAwbRouteHawbServiceImpl extends ServiceImpl<AfAwbRouteHawbMapper, AfAwbRouteHawb> implements AfAwbRouteHawbService {

    @Override
    public AfAwbRouteHawb find(String awbNumber, String hawbNumber) {
        if(StringUtils.isBlank(awbNumber) || StringUtils.isBlank(hawbNumber)){
            throw new IllegalArgumentException("The awbNumber and hawbNumber master be has value.");
        }
        LambdaQueryWrapper<AfAwbRouteHawb> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(AfAwbRouteHawb::getAwbNumber, awbNumber);
        queryWrapper.eq(AfAwbRouteHawb::getHawbNumber, hawbNumber);
        return this.baseMapper.selectOne(queryWrapper);
    }

    @Override
    public int insert(AfAwbRouteHawb afAwbRouteHawb) {
        AfAwbRouteHawb routeHawb = this.find(afAwbRouteHawb.getAwbNumber(), afAwbRouteHawb.getHawbNumber());
        int result = 0;
        if(null == routeHawb){
            afAwbRouteHawb.setCreateTime(LocalDateTime.now());
            result = this.save(afAwbRouteHawb) ? 1 : 0;
        }
        return result;
    }
}
