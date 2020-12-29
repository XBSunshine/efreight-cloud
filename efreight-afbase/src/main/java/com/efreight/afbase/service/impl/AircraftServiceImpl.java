package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.Aircraft;
import com.efreight.afbase.dao.AircraftMapper;
import com.efreight.afbase.service.AircraftService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * AF 基础信息 飞机类型码表 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-04-23
 */
@Service
public class AircraftServiceImpl extends ServiceImpl<AircraftMapper, Aircraft> implements AircraftService {

    @Override
    public Aircraft getOneByType(String aircraftType) {
        LambdaQueryWrapper<Aircraft> wrapper = Wrappers.<Aircraft>lambdaQuery();
        wrapper.eq(Aircraft::getAircraftType, aircraftType);
        return getOne(wrapper);
    }

    @Override
    public IPage getPage(Page page, Aircraft aircraft) {
        LambdaQueryWrapper<Aircraft> wrapper = Wrappers.<Aircraft>lambdaQuery();
        if (StrUtil.isNotBlank(aircraft.getAircraftType())) {
            wrapper.and(i -> i.like(Aircraft::getAircraftType, "%" + aircraft.getAircraftType() + "%").or(j -> j.like(Aircraft::getAircraftTypeNameEn, "%" + aircraft.getAircraftType() + "%")));
        }
        if (StrUtil.isNotBlank(aircraft.getAircraftTypePc())) {
            wrapper.eq(Aircraft::getAircraftTypePc, aircraft.getAircraftTypePc());
        }
        wrapper.orderByAsc(Aircraft::getAircraftType);
        IPage result = page(page, wrapper);
        return result;
    }
}
