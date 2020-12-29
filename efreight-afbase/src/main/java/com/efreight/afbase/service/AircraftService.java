package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.Aircraft;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * AF 基础信息 飞机类型码表 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-04-23
 */
public interface AircraftService extends IService<Aircraft> {

    Aircraft getOneByType(String aircraftType);

    IPage getPage(Page page, Aircraft aircraft);
}
