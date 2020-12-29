package com.efreight.afbase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.efreight.afbase.entity.AfOrderIdentifyDetail;
import com.efreight.afbase.dao.AfOrderIdentifyDetailMapper;
import com.efreight.afbase.service.AfOrderIdentifyDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author mayt
 * @since 2020-10-13
 */
@Service
public class AfOrderIdentifyDetailServiceImpl extends ServiceImpl<AfOrderIdentifyDetailMapper, AfOrderIdentifyDetail> implements AfOrderIdentifyDetailService {

    @Override
    public List<AfOrderIdentifyDetail> getAfOrderIdentifiesDetailList(Integer masterid) {
        AfOrderIdentifyDetail detail = new AfOrderIdentifyDetail();
        detail.setMasterid(masterid);
        QueryWrapper<AfOrderIdentifyDetail> queryDetailWrapper = new QueryWrapper<>(detail);
        return  baseMapper.selectList(queryDetailWrapper);
    }
}
