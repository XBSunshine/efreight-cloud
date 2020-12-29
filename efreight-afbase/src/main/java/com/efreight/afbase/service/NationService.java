package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.Carrier;
import com.efreight.afbase.entity.Nation;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
public interface NationService extends IService<Nation> {

    IPage queryPage(Page page, Nation nation);

    Nation queryOne(Integer id);

    void importData(List<Nation> list);
}
